package com.uowee.chapter.one;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "[onCreate]");

        if (savedInstanceState != null) {
            String test = savedInstanceState.getString("extra_test");
            Log.d(TAG, "[onCreate]recovery extra_test:" + test);
        }
        setContentView(R.layout.activity_main);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "[onStart]");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "[onRestart]");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "[onResume]");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "[onNewIntent], time=" + intent.getLongExtra("time", 0));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "[onSaveInstanceState]");
        outState.putString("extra_test", "recovery test");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "[onPause]");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "[onStop]");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[onDestroy]");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "[onRestoreInstanceState]");
        String test = savedInstanceState.getString("extra_test");
        Log.e(TAG, "[onRestoreInstanceState] recovery --> " + test);
    }
}
