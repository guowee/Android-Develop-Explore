package com.uowee.chapter.two;

import android.app.Application;
import android.util.Log;
import android.os.Process;

import com.uowee.chapter.two.binderpool.BinderPool;
import com.uowee.chapter.two.util.MyUtils;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = MyUtils.getProcessName(getApplicationContext(),
                Process.myPid());
        Log.d(TAG, "application start, process name:" + processName);
        new Thread(new Runnable() {

            @Override
            public void run() {
                doWorkInBackground();
            }
        }).start();
    }

    private void doWorkInBackground() {
        // init binder pool
        BinderPool.getInstance(getApplicationContext());
    }

}
