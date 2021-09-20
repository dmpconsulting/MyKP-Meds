package com.montunosoftware.pillpopper.model;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TimePicker;

public class CustomTimePicker extends TimePicker {
 
    public CustomTimePicker(Context context) {
        super(context);
    }
 
    public CustomTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public CustomTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ViewParent parentView = getParent();
 
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (parentView != null) {
                parentView.requestDisallowInterceptTouchEvent(true);
            }
        }
 
        return false;
    }
}