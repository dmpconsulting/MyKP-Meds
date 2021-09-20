package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class ActionEditText extends androidx.appcompat.widget.AppCompatEditText
{
	public interface ActionActivityEDitListener{
		void onKeyPreIME(int keyCode, KeyEvent event);
	}

	private ActionActivityEDitListener actionEDitListener;
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

	/*public void setActionEDitListener(Activity activity){
		if (activity instanceof LoginActivity)
			actionEDitListener=(LoginActivity)activity;
	}*/

	@SuppressWarnings("ResourceType")
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		return super.onKeyPreIme(keyCode, event);
	}

	@Override
	public int getAutofillType() {
		return AUTOFILL_TYPE_NONE;
	}
}
