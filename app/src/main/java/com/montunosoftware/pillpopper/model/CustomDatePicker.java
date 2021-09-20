package com.montunosoftware.pillpopper.model;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.DatePicker;

public class CustomDatePicker extends DatePicker {
 
    public CustomDatePicker(Context context) {
        super(context);
    }
 
    public CustomDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public CustomDatePicker(Context context, AttributeSet attrs, int defStyle) {
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