package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

/**
 * @author
 * Created by M1032185 on 7/26/2016.
 */
public class HasStatusUpdateAlert {

    private final Context mContext;
    private final String mMessage;
    private AlertDialog dialog;
    private OnPositiveClickListener onPositiveClickListener;
    private OnDismissClickListener dismissClickListener;
    private boolean isForceSignIn;

    public HasStatusUpdateAlert(Context context, String message, OnPositiveClickListener onPositiveClickListener,OnDismissClickListener onDismissClickListener, boolean isForceSignIn) {

        mContext = context;
        mMessage = message;
        this.onPositiveClickListener = onPositiveClickListener;
        this.dismissClickListener = onDismissClickListener;
        this.isForceSignIn = isForceSignIn;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(R.layout.hasstatus_update_alert);
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setOnDismissListener(dialog -> dismissClickListener.onDismiss());

    }

    public void showDialog() {
        dialog.show();
        RunTimeData.getInstance().setAlertDialogInstance(dialog);
        TextView msg = dialog.findViewById(R.id.text_alert);
        msg.setText(mMessage);
        Button btnOk = dialog.findViewById(R.id.btn_ok);
        if (isForceSignIn){
            btnOk.setText(mContext.getResources().getString(R.string.signin_myMeds));
        }
        btnOk.setOnClickListener(v -> onPositiveClickListener.positiveBtnClick());
    }

    public void cancel(){
        if(dialog != null && dialog.isShowing()) {
            dialog.cancel();
        }
    }

    public boolean isshowing(){
        return dialog.isShowing();
    }
    public interface OnDismissClickListener {
        void onDismiss();
    }
}
