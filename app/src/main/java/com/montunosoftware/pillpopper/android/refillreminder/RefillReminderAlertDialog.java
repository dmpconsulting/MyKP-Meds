/**
 *
 */
package com.montunosoftware.pillpopper.android.refillreminder;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

public class RefillReminderAlertDialog {
	private final Context mContext;
	private final String mMessage;
	private final AlertDialog mAlertDialog;

	/*
	 * This is the constructor of the class.
	 *
	 * context - The context where this dialog is displayed. message - The
	 * message to be shown to the user. positiveButton - The text to displayed
	 * for positive button e.g "Ok". pListener - Listener for positive button.
	 * It implements the action to be performed on click of the button.
	 * negativeButton - The text to displayed for negative button e.g "Cancel".
	 * nListener - Listener for negative button. It implements the action to be
	 * performed on click of the button.
	 */

	public View findViewById(int id){
		return mAlertDialog.findViewById(id);
	}

	public void setView(View view){
		mAlertDialog.setView(view);
	}


	public RefillReminderAlertDialog(Context context, String title, String message,
                                     String positiveButton, OnClickListener pListener,
                                     String negativeButton, OnClickListener nListener) {
		mContext = context;
		mMessage = message;

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(title);
		builder.setMessage(mMessage);
		builder.setPositiveButton(positiveButton, pListener);
		builder.setNegativeButton(negativeButton, nListener);
		builder.setCancelable(false);
		mAlertDialog = builder.create();

	}

	public void setMessage(String mMessage) {
		mAlertDialog.setMessage(mMessage);

	}

	public void setDialogTitle(String title) {
		mAlertDialog.setTitle(title);
	}

	public boolean isShowing() {
		return mAlertDialog.isShowing();
	}

	public void showDialog(int messageTextSize) {
		RunTimeData.getInstance().setAlertDisplayedFlg(true);
		if(!((Activity)mContext).isFinishing()) {
			mAlertDialog.show();
			RunTimeData.getInstance().setAlertDialogInstance(mAlertDialog);
		}
		TextView msg = mAlertDialog.findViewById(android.R.id.message);
		msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageTextSize);
	}

	@SuppressWarnings("ResourceType")
	public void showDialog() {
		RunTimeData.getInstance().setAlertDisplayedFlg(true);
		if(!((Activity)mContext).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(mAlertDialog);
			mAlertDialog.show();
		}

		Button btnPositive = mAlertDialog.findViewById(android.R.id.button1);
		Button btnNegative = mAlertDialog.findViewById(android.R.id.button2);

		if (null != btnPositive) {
			btnPositive.setTextColor(ActivationUtil.getColorWrapper(mContext, R.color.kp_next_color));
		}

		if (null != btnNegative) {
			btnNegative.setTextColor(ActivationUtil.getColorWrapper(mContext, R.color.kp_next_color));
		}
	}

	public void showDialogWithPadding() {
		RunTimeData.getInstance().setAlertDisplayedFlg(true);
		if(!((Activity)mContext).isFinishing()) {
			mAlertDialog.show();
			RunTimeData.getInstance().setAlertDialogInstance(mAlertDialog);
		}
		int padding_in_dp = 16;
		final float scale = mContext.getResources().getDisplayMetrics().density;
		int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);
		params.setMargins(0, 0, -48, -8); //left,top,right,bottom

		Button btnPositive = mAlertDialog.findViewById(android.R.id.button1);
		Button btnNegative = mAlertDialog.findViewById(android.R.id.button2);

		if (null != btnPositive) {

			btnPositive.setLayoutParams(params);
			btnPositive.setTextColor(ActivationUtil.getColorWrapper(mContext, R.color.kp_next_color));
		}

		if (null != btnNegative) {

			btnNegative.setLayoutParams(params);
			btnNegative.setTextColor(ActivationUtil.getColorWrapper(mContext, R.color.kp_next_color));
		}
	}
}
