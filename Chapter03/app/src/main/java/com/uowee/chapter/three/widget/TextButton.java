package com.uowee.chapter.three.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class TextButton extends AppCompatTextView {
    public TextButton(Context context) {
        this(context, null);
    }

    public TextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
