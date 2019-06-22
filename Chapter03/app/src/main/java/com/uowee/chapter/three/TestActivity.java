package com.uowee.chapter.three;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewTreeObserver;
import android.widget.Button;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TestActivity";

    private Button view;
    private View mButton2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
        Log.d(TAG, "onCreate --> measure width= " + view.getMeasuredWidth() + " height= " + view.getMeasuredHeight());
        measureView();
    }

    private void measureView() {
        //int widthMeasureSpec = MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY);
        //int heightMeasureSpec = MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY);

        int widthMeasureSpec = MeasureSpec.makeMeasureSpec((1 << 30) - 1, MeasureSpec.AT_MOST);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec((1 << 30) - 1, MeasureSpec.AT_MOST);

        view.measure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "measureView, width= " + view.getMeasuredWidth() + " height= " + view.getMeasuredHeight());

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart --> measure width= " + view.getMeasuredWidth() + " height= " + view.getMeasuredHeight());
        view.post(new Runnable() {
            @Override
            public void run() {
                int width = view.getMeasuredWidth();
                int height = view.getMeasuredHeight();
                Log.d(TAG, "onStart post --> measure width= " + width + " height= " + height);
            }
        });

        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = view.getMeasuredWidth();
                int height = view.getMeasuredHeight();
                Log.d(TAG, "onStart  ViewTreeObserver --> measure width= " + width + " height= " + height);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume --> measure width= " + view.getMeasuredWidth() + " height= " + view.getMeasuredHeight());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();
            Log.d(TAG, "onWindowFocusChanged --> measure width= " + width + " height= " + height);
        }
    }

    private void initView() {
        view = findViewById(R.id.button1);
        view.setOnClickListener(this);
        mButton2 = findViewById(R.id.button2);
    }


    @Override
    public void onClick(View v) {
        if (v == view) {
            Log.d(TAG, "measure width= " + mButton2.getMeasuredWidth() + " height= " + mButton2.getMeasuredHeight());
            Log.d(TAG, "layout width= " + mButton2.getWidth() + " height= " + mButton2.getHeight());
        }
    }
}
