package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * @author
 * Created by adhithyaravipati on 4/11/16.
 */
public class CalendarViewPager extends ViewPager {

    public CalendarViewPager(Context context, AttributeSet attrs) {
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

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        if(getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL){
            setCurrentItem(adapter.getCount()-1);
        }
    }

    @Override
    public void setLayoutDirection(int layoutDirection) {
        if(layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
            setRotationY(0);
        }
        else if(layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            setRotationY(180);
        }
    }
}
