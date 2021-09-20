package com.montunosoftware.pillpopper.android.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.HasStatusUpdateAlert;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.service.AcknowledgeStatusAsyncTask;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("InflateParams")
public class DialogHelpers
{

	private static HasStatusUpdateAlert hasStatusUpdateAlert;
	private static boolean isPostSignInAlertVisible;

	public interface ConfirmedListener {
		void onConfirmed();
		//void onCanceled();

	}

	public interface Confirm_CancelListener {
		void onConfirmed();
		void onCanceled();

	}

	public static void showConfirmationDialog(
			Context context,
			String questionString,
			final ConfirmedListener confirmedListener
			)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setMessage(questionString)
		.setNegativeButton(R.string._cancel, (dialog, id) -> {
			dialog.cancel();
			//	confirmedListener.onCanceled();
		})
		.setPositiveButton(R.string._ok, (dialog, id) -> {
			dialog.cancel();
			confirmedListener.onConfirmed();
		});
		AlertDialog alert = builder.create();
		if(!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}

		Button btnPositive = alert.findViewById(android.R.id.button1);
		Button btnNegative = alert.findViewById(android.R.id.button2);

		btnPositive.setTextColor(Util.getColorWrapper(context,R.color.kp_theme_blue));
		btnNegative.setTextColor(Util.getColorWrapper(context,R.color.kp_theme_blue));
	}

	public static void showConfirmationDialog(
			Context context,
			int questionStringId,
			final ConfirmedListener confirmedListener
	) {
		showConfirmationDialog(context, context.getString(questionStringId), confirmedListener);
	}

	public static void showConfirm_CancelDialog(
			Context context,
			int questionStringId,
			final Confirm_CancelListener confirmeCancelListener
			) {
		showConfirm_CancelDialog(context, context.getString(questionStringId), confirmeCancelListener);
	}

	public static void showConfirm_CancelDialog(
			Context context,
			String questionString,
			final Confirm_CancelListener confirmedListener
			)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(questionString)
		.setNegativeButton(R.string._cancel, (dialog, id) -> {
            dialog.cancel();
            //	confirmedListener.onCanceled();
        })
		.setPositiveButton(R.string._ok, (dialog, id) -> {
            dialog.cancel();
            confirmedListener.onConfirmed();
        });
		AlertDialog alert = builder.create();
		if(!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}

		Button btnPositive = alert.findViewById(android.R.id.button1);
		Button btnNegative = alert.findViewById(android.R.id.button2);

		btnPositive.setTextColor(Util.getColorWrapper(context,R.color.kp_theme_blue));
		btnNegative.setTextColor(Util.getColorWrapper(context,R.color.kp_theme_blue));

	}

	public static void showAlertDialog(
			Context context,
			CharSequence message,
			final ConfirmedListener confirmedListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(message)
		.setPositiveButton(R.string._ok, (dialog, id) -> {
            dialog.cancel();

            if (confirmedListener != null) {
                confirmedListener.onConfirmed();
            }
        });
		AlertDialog alert = builder.create();
		alert.setCancelable(false);
		if(!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}
	}

	public static void showPostponeErrorAlert(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.unable_to_postpone_title));
		builder.setMessage(context.getResources().getString(R.string.postpone_error_alert_message));
		builder.setPositiveButton(R.string._ok, (dialog, id) -> dialog.cancel());
		AlertDialog alert = builder.create();
		alert.setCancelable(false);
		if (!((Activity) context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}
	}

	public static void showAlertDialog(Context context,	CharSequence message)
	{
		showAlertDialog(context, message, null);
	}

	public static void showAlertDialog(Context context, int stringId)
	{
		showAlertDialog(context, context.getString(stringId), null);
	}

	public static void showAlertDialog(Context context, int stringId, ConfirmedListener confirmedListener)
	{
		showAlertDialog(context, context.getString(stringId), confirmedListener);
	}

	//  US3399
	public static void showAlertDialogWithHeader(Context context, int stringTitle ,int stringMsg, ConfirmedListener confirmedListener)
	{
		showAlertDialogWithHeader(context, context.getString(stringTitle) , context.getString(stringMsg), confirmedListener);
	}

	public static void showAlertDialogWithHeader(
			Context context,
			CharSequence title,
			CharSequence message,
			final ConfirmedListener confirmedListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message)
				.setCancelable(false)
		.setPositiveButton(R.string._ok, (dialog, id) -> {
            dialog.cancel();

            if (confirmedListener != null) {
                confirmedListener.onConfirmed();
            }
        });
		AlertDialog alert = builder.create();
		if(!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}
	}

	public static void showAlertDialogWithOkButton(
			Context context,
			String title,
			String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string._ok, (dialog, id) -> dialog.cancel());
		AlertDialog alert = builder.create();
		if(!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}
	}

	public static void showAutoDismissalAlert(Context context, int title,
											  int message, int delayTime, ConfirmedListener confirmedListener){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setCancelable(false);
		Handler handler = new Handler();
		try {
			AlertDialog alert = builder.create();
			if(!((Activity)context).isFinishing()) {
				RunTimeData.getInstance().setAlertDialogInstance(alert);
				alert.show();
			}
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					alert.dismiss();
					handler.postDelayed(() -> {
						if (confirmedListener != null) {
							confirmedListener.onConfirmed();
						}
                    }, delayTime);
					timer.cancel();
				}
			}, delayTime);
		}catch (Exception e){
			PillpopperLog.exception(e.getMessage());
		}
	}


	public static AlertDialog showAlertWithConfirmCancelListeners(
			Context context,
			int title,
			int message,
			final Confirm_CancelListener confirmedCancelListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string._ok, (dialog, id) -> {
                    dialog.cancel();
                    if (confirmedCancelListener != null) {
                        confirmedCancelListener.onConfirmed();
                    }
                })
				.setNegativeButton(R.string.cancel_text, (dialog, id) -> {
                    dialog.cancel();
                    if (confirmedCancelListener != null) {
                        confirmedCancelListener.onCanceled();
                    }
                });
		AlertDialog alert = builder.create();
		if(!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}
		return alert;
	}

	public static AlertDialog showAlertWithConfirmDiscardListeners(
			Context context,
			int title,
			int message,
			final Confirm_CancelListener confirmedCancelListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.discard_text, (dialog, id) -> {
					dialog.cancel();
					if (confirmedCancelListener != null) {
						confirmedCancelListener.onConfirmed();
					}
				})
				.setNegativeButton(R.string.cancel_text, (dialog, id) -> {
					dialog.cancel();
					if (confirmedCancelListener != null) {
						confirmedCancelListener.onCanceled();
					}
				});
		AlertDialog alert = builder.create();
		if(!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}
		return alert;
	}


	public static AlertDialog showAlertWithSaveCancelListeners(
			Context context,
			int title,
			int message,
			final Confirm_CancelListener confirmedCancelListener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.save_text, (dialog, id) -> {
					dialog.cancel();
					if (confirmedCancelListener != null) {
						confirmedCancelListener.onConfirmed();
					}
				})
				.setNegativeButton(R.string.discard_text, (dialog, id) -> {
					dialog.cancel();
					if (confirmedCancelListener != null) {
						confirmedCancelListener.onCanceled();
					}
				});
		AlertDialog alert = builder.create();
		if(!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alert);
			alert.show();
		}
		return alert;
	}

	public static void showAlertDialogWithHeaderAndIcon(Context context,int icon, int stringTitle ,int stringMsg, Activity act)
	{
		showAlertDialogWithHeaderAndIcon(context, icon, context.getString(stringTitle) , context.getString(stringMsg), act);
	}

	public static void showAlertDialogWithHeaderAndIcon(
			Context context,
			int icon,
			CharSequence title,
			CharSequence message, Activity act)
	{
		Dialog dialog;
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		LayoutInflater inflater = act.getLayoutInflater();
		View view = inflater.inflate(R.layout.delete_med, null);
		alertDialog.setView(view);
		alertDialog.setCancelable(false);

		TextView titleTV = view.findViewById(R.id.delete_drug_title);
		titleTV.setText(title);


		TextView unWantedView = view.findViewById(R.id.delete_drug_hint);
		unWantedView.setVisibility(View.GONE);

		TextView messageTV = view.findViewById(R.id.delete_drug_description);

		messageTV.setText(message);
		alertDialog.setPositiveButton(
				Html.fromHtml("<b>" + context.getString(R.string._ok) + "</b>"),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});

		dialog = alertDialog.create();
		if(null!=dialog && !dialog.isShowing()){
			dialog.show();
		}
	}

	public static void showSkipMedDialog(Context context,String skipOrSkikAll, final ConfirmedListener confirmedListener){

		final LayoutInflater inflater = LayoutInflater.from(context);
		androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.MyAlertDialog);


		if (skipOrSkikAll.equals(PillpopperConstants.ACTION_SKIP_PILL)) {
			builder.setView(inflater.inflate(R.layout.skip_alert, null));
		} else {
			builder.setView(inflater.inflate(R.layout.skipall_alert, null));
		}
		builder.setCancelable(false);
		builder.setPositiveButton("OKAY", (dialog, which) -> {
            dialog.dismiss();
            confirmedListener.onConfirmed();
        }

        );
		builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()

        );
		androidx.appcompat.app.AlertDialog alertDialog = builder.create();
		if (!((Activity)context).isFinishing()) {
			RunTimeData.getInstance().setAlertDialogInstance(alertDialog);
			alertDialog.show();
		}

	}

	/**
	 * Shows Post SignIn Alerts like DropProxy/ScheduleChange
	 * @param pillpopperActivity
     */
	public static void showPostSignInAlert(final PillpopperActivity pillpopperActivity) {

		SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(
				pillpopperActivity, AppConstants.AUTH_CODE_PREF_NAME);

		String proxyStatusCodeValue = mSharedPrefManager.getString(AppConstants.PROXY_STATUS_CODE, "");
		String medicationScheduleChanged = mSharedPrefManager.getString(AppConstants.MEDICATION_SCHEDULE_CHANGED, "");
		if (!Util.isEmptyString(proxyStatusCodeValue) && !("N").equalsIgnoreCase(proxyStatusCodeValue) && !("P").equalsIgnoreCase(proxyStatusCodeValue)) {
			if (!isPostSignInAlertVisible) {
				hasStatusUpdateAlert = new HasStatusUpdateAlert(pillpopperActivity, pillpopperActivity.getString(R.string.proxy_post_signin), () -> hasStatusUpdateAlert.cancel(), () -> {
                    PillpopperConstants.setIsAlertActedOn(true);
                    isPostSignInAlertVisible = false;

                    // clear the hasStatusUpdateresponse and timeStamp values
                    Util.clearHasStatusUpdateValues(pillpopperActivity);
                },false);

				if (!hasStatusUpdateAlert.isshowing()) {
					isPostSignInAlertVisible = true;
					hasStatusUpdateAlert.showDialog();
				}
			}

		} else if (!Util.isEmptyString(medicationScheduleChanged) && ("Y").equalsIgnoreCase(medicationScheduleChanged)) {
			if (!isPostSignInAlertVisible) {
				hasStatusUpdateAlert = new HasStatusUpdateAlert(pillpopperActivity, pillpopperActivity.getString(R.string.medications_post_signin), () -> hasStatusUpdateAlert.cancel(), () -> {
                    PillpopperConstants.setIsAlertActedOn(true);
                    String primaryUserId = FrontController.getInstance(pillpopperActivity).getPrimaryUserIdIgnoreEnabled();
                    AcknowledgeStatusAsyncTask acknowledgeStatusAsyncTask = new AcknowledgeStatusAsyncTask(pillpopperActivity);
                    acknowledgeStatusAsyncTask.execute(AppConstants.getAcknowledgeStatusAPIUrl(), primaryUserId, "AcknowledgeScheduleChanges");
                    isPostSignInAlertVisible = false;

                    // clear the hasStatusUpdateresponse and timeStamp values
                    Util.clearHasStatusUpdateValues(pillpopperActivity);

                },false);

				if (!hasStatusUpdateAlert.isshowing()) {
					isPostSignInAlertVisible = true;
					hasStatusUpdateAlert.showDialog();
				}
			}
		} else{
			PillpopperConstants.setIsAlertActedOn(true);
		}
	}
}
