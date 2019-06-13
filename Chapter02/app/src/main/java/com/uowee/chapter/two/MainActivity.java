package com.uowee.chapter.two;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.uowee.chapter.two.aidl.Book;
import com.uowee.chapter.two.aidl.BookManagerService;
import com.uowee.chapter.two.aidl.IBookManager;
import com.uowee.chapter.two.aidl.IOnNewBookArrivedListener;
import com.uowee.chapter.two.binderpool.BinderPool;
import com.uowee.chapter.two.binderpool.ICompute;
import com.uowee.chapter.two.binderpool.ISecurityCenter;
import com.uowee.chapter.two.model.User;
import com.uowee.chapter.two.provider.BookProvider;
import com.uowee.chapter.two.util.Constants;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private IBookManager mRemoteBookManager;
    private Messenger mService;
    private Messenger mGetReplyMessenger = new Messenger(new MessengerHandler());

    private ISecurityCenter mSecurityCenter;
    private ICompute mCompute;

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FROM_SERVICE:
                    Log.i(TAG, "receive msg from Service:" + msg.getData().getString("reply"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.d(TAG, "receive new book :" + msg.obj.toString());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };


    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binder died. tname:" + Thread.currentThread().getName());
            if (mRemoteBookManager == null)
                return;
            mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManager = null;
            // TODO:这里重新绑定远程Service
        }
    };


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRemoteBookManager = IBookManager.Stub.asInterface(service);

            try {
                mRemoteBookManager.asBinder().linkToDeath(mDeathRecipient, 0);
                List<Book> list = mRemoteBookManager.getBookList();
                Log.i(TAG, "query book list, list type:"
                        + list.getClass().getCanonicalName());
                Log.i(TAG, "query book list:" + list.toString());

                Book newBook = new Book(3, "Android进阶");
                mRemoteBookManager.addBook(newBook);
                Log.i(TAG, "add book:" + newBook);
                List<Book> newList = mRemoteBookManager.getBookList();
                Log.i(TAG, "query book list:" + newList.toString());
                mRemoteBookManager.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManager = null;
            Log.d(TAG, "onServiceDisconnected. tname:" + Thread.currentThread().getName());
        }
    };


    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook)
                    .sendToTarget();
        }
    };

    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            Log.d(TAG, "bind service");
            Message msg = Message.obtain(null, Constants.MSG_FROM_CLIENT);
            Bundle data = new Bundle();
            data.putString("msg", "hello, this is client.");
            msg.setData(data);
            msg.replyTo = mGetReplyMessenger;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.button0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BookManagerService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        });

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //当客户端发起远程请求时，由于当前线程会被挂起直至服务端进程返回数据，所以，如果一个远程方法是很耗时的，那么就不能在UI线程中发起此远程请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mRemoteBookManager != null) {
                            try {
                                List<Book> newList = mRemoteBookManager.getBookList();
                                for (Book book : newList) {
                                    Log.e(TAG, "Book Name:" + book.bookName);
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.uowee.MessengerService.launch");
                bindService(intent, mConn, Context.BIND_AUTO_CREATE);
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri bookUri = Uri.parse("content://com.uowee.chapter.two.book.provider/book");
                ContentValues values = new ContentValues();
                values.put("_id", 6);
                values.put("name", "Android开发艺术探索");
                getContentResolver().insert(bookUri, values);
                Cursor bookCursor = getContentResolver().query(bookUri, new String[]{"_id", "name"}, null, null, null);
                while (bookCursor.moveToNext()) {
                    Book book = new Book();
                    book.bookId = bookCursor.getInt(0);
                    book.bookName = bookCursor.getString(1);
                    Log.d(TAG, "query book:" + book.toString());
                }
                bookCursor.close();

                Uri userUri = Uri.parse("content://com.uowee.chapter.two.book.provider/user");
                Cursor userCursor = getContentResolver().query(userUri, new String[]{"_id", "name", "sex"}, null, null, null);
                while (userCursor.moveToNext()) {
                    User user = new User();
                    user.userId = userCursor.getInt(0);
                    user.userName = userCursor.getString(1);
                    user.isMale = userCursor.getInt(2) == 1;
                    Log.d(TAG, "query user:" + user.toString());
                }
                userCursor.close();
            }
        });

        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri specialBookUri = Uri.parse("content://com.uowee.chapter.two.book.provider");
                Bundle bundle = new Bundle();
                bundle.putInt("_id", 2);
                Bundle getResult = getContentResolver().call(specialBookUri, BookProvider.METHOD_GET_BOOK, null, bundle);
                getResult.setClassLoader(Book.class.getClassLoader());
                Log.d(TAG, "get book with id 2: " + getResult.getParcelable("book"));

                Bundle addBookBundle = new Bundle();
                addBookBundle.putParcelable("book", new Book(0, "数据结构"));
                Bundle setResult = getContentResolver().call(specialBookUri, BookProvider.METHOD_ADD_BOOK, null, addBookBundle);
                Log.d(TAG, "add book with id 0: " + setResult);

                bundle.putInt("_id", 0);
                Bundle getResult0 = getContentResolver().call(specialBookUri, BookProvider.METHOD_GET_BOOK, null, bundle);
                getResult0.setClassLoader(Book.class.getClassLoader());
                Log.d(TAG, "get book with id 0: " + getResult0.getParcelable("book"));

            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doWork();
                    }
                }).start();
            }
        });

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                startActivity(intent);
            }
        });


    }

    private void doWork() {
        BinderPool binderPool = BinderPool.getInstance(MainActivity.this);
        IBinder securityBinder = binderPool
                .queryBinder(BinderPool.BINDER_SECURITY_CENTER);

        //mSecurityCenter = (ISecurityCenter) SecurityCenterImpl.asInterface(securityBinder);

        mSecurityCenter = ISecurityCenter.Stub.asInterface(securityBinder);
        Log.d(TAG, "visit ISecurityCenter");
        String msg = "helloworld-安卓";
        System.out.println("content:" + msg);
        try {
            String password = mSecurityCenter.encrypt(msg);
            System.out.println("encrypt:" + password);
            System.out.println("decrypt:" + mSecurityCenter.decrypt(password));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "visit ICompute");
        IBinder computeBinder = binderPool
                .queryBinder(BinderPool.BINDER_COMPUTE);

        // mCompute = ComputeImpl.asInterface(computeBinder);

        mCompute = ICompute.Stub.asInterface(computeBinder);
        try {
            System.out.println("3+5=" + mCompute.add(3, 5));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mRemoteBookManager != null
                && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                Log.i(TAG, "unregister listener:" + mOnNewBookArrivedListener);
                mRemoteBookManager
                        .unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        unbindService(mConn);
        super.onDestroy();
    }
}
