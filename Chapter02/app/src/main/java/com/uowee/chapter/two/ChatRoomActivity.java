package com.uowee.chapter.two;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.uowee.chapter.two.socket.TCPServerService;
import com.uowee.chapter.two.util.MyUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mSendButton;
    private TextView mMessageTextView;
    private EditText mMessageEditText;

    private PrintWriter mPrintWriter;
    private Socket mClientSocket;


    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_RECEIVE_NEW_MSG: {
                    mMessageTextView.setText(mMessageTextView.getText() + (String) msg.obj);
                    break;
                }
                case MESSAGE_SOCKET_CONNECTED: {
                    mSendButton.setEnabled(true);
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        mMessageTextView = (TextView) findViewById(R.id.msg_container);
        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(this);
        mMessageEditText = (EditText) findViewById(R.id.msg);
        Intent service = new Intent(this, TCPServerService.class);
        startService(service);
        new Thread() {
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.send) {
            final String msg = mMessageEditText.getText().toString();
            if (!TextUtils.isEmpty(msg) && mPrintWriter != null) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPrintWriter.println(msg);
                    }
                }).start();

                mMessageEditText.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                final String showedMsg = "self " + time + ":" + msg + "\n";
                mMessageTextView.setText(mMessageTextView.getText() + showedMsg);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @SuppressLint("SimpleDateFormat")
    private String formatDateTime(long time) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                System.out.println("connect server success");
            } catch (IOException e) {
                SystemClock.sleep(1000);
                System.out.println("connect tcp server failed, retry...");
            }
        }

        try {
            // 接收服务器端的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            while (!ChatRoomActivity.this.isFinishing()) {
                String msg = br.readLine();
                System.out.println("receive :" + msg);
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "server " + time + ":" + msg
                            + "\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showedMsg)
                            .sendToTarget();
                }
            }
            System.out.println("quit...");
            MyUtils.close(mPrintWriter);
            MyUtils.close(br);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
