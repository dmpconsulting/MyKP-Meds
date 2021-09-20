package com.montunosoftware.pillpopper.android.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

/**
 * Created by M1023050 on 16-Apr-19.
 * View Pager will grow the height based on the content.
 */

public class WrapContentHeightViewPager extends ViewPager {

    public WrapContentHeightViewPager(Context context) {
        super(context);
    }

    public WrapContentHeightViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int heightMeasure;
        int height = 0;
        for(int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if(h > height) height = h;
        }

        heightMeasure = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasure);
    }

}