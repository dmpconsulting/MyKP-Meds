package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.Util;

public class PremiumMaskView extends FrameLayout
{
	public PremiumMaskView(Context context) {
		super(context);
		_init(context);
	}

	public PremiumMaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_init(context);
	}
	
	public void _init(Context context) {
		if (!isInEditMode()) {
			this.setForeground(Util.getDrawableWrapper(context, R.drawable.translucentframe));
		}
	}

	public void setView(boolean maskPresent)
	{
		if (maskPresent) {
			this.setForeground(Util.getDrawableWrapper(getContext(), R.drawable.translucentframe));
		} else {
			this.setForeground(null);
		}
	}
}
