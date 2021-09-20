package org.kp.tpmg.mykpmeds.activation.util;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class TextColorChanger extends ClickableSpan {

	private final int color_Code;
	private final OnClickListener mListener;

	public TextColorChanger(OnClickListener listener, int colorCode) {
		mListener = listener;
		color_Code = colorCode;
	}

	@Override
	public void onClick(View widget) {
		if (mListener != null) {
			mListener.onClick();
		}
	}

	public interface OnClickListener {
		void onClick();
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(color_Code); // Kaiser Specified Colour
	}
}
