package com.montunosoftware.pillpopper.android.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.montunosoftware.pillpopper.android.AddOrEditMedicationActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;

public class ActionEditText extends androidx.appcompat.widget.AppCompatEditText
{
	public interface ActionEDitListener{
		void onKeyPreIME(int keyCode, KeyEvent event);
	}

	private ActionEDitListener actionEDitListener;
	public ActionEditText(Context context)
	{
		super(context);
	}

	public ActionEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ActionEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs)
	{
		InputConnection conn = super.onCreateInputConnection(outAttrs);
		outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
		return conn;
	}

	public void setActionEDitListener(Activity activity){
		if (activity instanceof AddOrEditMedicationActivity)
		actionEDitListener=(AddOrEditMedicationActivity)activity;
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		try{
			actionEDitListener.onKeyPreIME(keyCode,event);
		} catch(Exception e){
			PillpopperLog.say(e.getMessage());
		}
		return super.onKeyPreIme(keyCode, event);
	}

	@Override
	public int getAutofillType() {
		return AUTOFILL_TYPE_NONE;
	}
}
