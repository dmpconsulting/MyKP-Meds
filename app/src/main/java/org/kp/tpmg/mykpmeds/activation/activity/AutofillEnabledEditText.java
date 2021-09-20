package org.kp.tpmg.mykpmeds.activation.activity;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatEditText;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;

public class AutofillEnabledEditText extends AppCompatEditText {

    public interface ActionEDitListener {
        void onKeyPreIME(int keyCode, KeyEvent event);
    }

    public AutofillEnabledEditText(Context context) {
        super(context);
    }

    private ActionEDitListener actionEDitListener;

    public AutofillEnabledEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutofillEnabledEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }

    public void setActionEDitListener(Activity activity) {
        if (activity instanceof LoginActivity)
            actionEDitListener = (LoginActivity) activity;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        try {
            actionEDitListener.onKeyPreIME(keyCode, event);
        } catch (Exception e) {
            PillpopperLog.say(e.getMessage());
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
