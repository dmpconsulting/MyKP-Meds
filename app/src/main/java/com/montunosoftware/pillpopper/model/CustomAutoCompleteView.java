package com.montunosoftware.pillpopper.model;

import android.content.Context;
import android.util.AttributeSet;

public class CustomAutoCompleteView extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {
 
    public CustomAutoCompleteView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
     
    public CustomAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
 
    public CustomAutoCompleteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
 
    // this is how to disable AutoCompleteTextView filter
    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
     
        super.performFiltering(text, keyCode);
    }

}
