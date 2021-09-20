package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.NotificationBar;
import com.montunosoftware.pillpopper.android.util.NotificationBar_OverdueDose;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.HasStatusUpdateResponseObj;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.service.GetAppProfileUrlsService;
import com.montunosoftware.pillpopper.service.HasStatusUpdateAsyncTask;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.util.List;

public class NotificationBarOrderedBroadcastHandler extends BroadcastReceiver implements HasStatusUpdateAsyncTask.HasStatusUpdateResponseListener
{
	private Context mContext;
	private int notificationId;
	private String notificationActionTime = "NotificationActionTime";

	@Override
	public void onReceive(Context context, Intent intent)
	{
			mContext = context;

			PillpopperLog.say("Debug --s-- Got ordered broadcast from notification bar - launching drug list");
			/*Getting the values from shared preference*/
			SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(
					context, AppConstants.AUTH_CODE_PREF_NAME);

			boolean flagOptNotificationScreen = mSharedPrefManager.getBoolean(
					AppConstants.FLAG_OPT_IN_FROM_NOTIFICATION, false);  

			boolean flagOptFirstTime = mSharedPrefManager.getBoolean(
					AppConstants.FLAG_OPT_IN_FIRST_TIME, false);

			if(!flagOptNotificationScreen && !flagOptFirstTime ){
				mSharedPrefManager.putBoolean (AppConstants.FLAG_OPT_IN_FROM_NOTIFICATION, true, false);
				mSharedPrefManager.putBoolean (AppConstants.FLAG_OPT_IN_FIRST_TIME, true, false); 
			}

			if(!mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY,false) && AppConstants.INCLUDE_NOTIFICATION_ACTIONS_FEATURE && null != intent.getExtras()) {

				String tappedAction = intent.getExtras().getString(NotificationBar.NOTIFICATION_TAPPED_ACTION);
				notificationId = intent.getExtras().getInt(NotificationBar.NOTIFICATION_ID);

				LoggerUtils.info("Debug --s-- Performing notification action : " + tappedAction);

				LoggerUtils.info("Debug --s-- for notificationId " + notificationId + " -- " + Util.convertDateLongToIso(""+notificationId));

				List<Drug> drugsList = getDrugListForNotificationAction();

				LoggerUtils.info("Debug --s-- Debug DrugList size " + drugsList.size());

				if (!drugsList.isEmpty() && null != tappedAction) {
					if (!Util.hasPendingAlertsNeedForceSignIn(context)) {
							LoggerUtils.info("Debug --s-- Triggered notification time " + notificationId);
							RunTimeData.getInstance().setFromNotificationAction(true);
							performNotificationActionAndMakeServiceCall(mContext, drugsList, tappedAction,
									NotificationBar.NOTIFICATION_ACTION_TAKE.equalsIgnoreCase(tappedAction)
											? PillpopperConstants.ACTION_TAKE_PILL : PillpopperConstants.ACTION_SKIP_PILL, FireBaseConstants.ParamValue.RICH_NOTIFICATION);
							if (RunTimeData.getInstance().getReminderPillpopperTime() != null
									&& notificationId == RunTimeData.getInstance().getReminderPillpopperTime().getGmtSeconds()) {
								FrontController.getInstance(context).hideLateRemindersWhenFromNotifications(context);
							}
							// check for non secure base url and then
							// check for 15 min and make has status update response call
							boolean hasStatusAPICallRequired = true;
							if (Util.isHasStatusUpdateCallRequired(context)
									&& !Util.isEmptyString(FrontController.getInstance(mContext).getLocalNonSecureUrl(mContext))
									&& !RunTimeData.getInstance().isHasStatusCallInProgress()) {
								// have to call the HasStatusUpdateAsyncTask with callback expected,
								// Since once after the call back we have to scan the alerts again and post the notification if required.
								LoggerUtils.info("Debug -- making the hasStatusUpdate call " + notificationId + " -- " + tappedAction);
								hasStatusAPICallRequired = false;
								HasStatusUpdateAsyncTask statusUpdateAsyncTask = new HasStatusUpdateAsyncTask(context, this);
								statusUpdateAsyncTask.execute();
							}

							if (Util.isNonSecureAppProfileCallRequired(context) && !RunTimeData.getInstance().isAppProfileInProgress()) {
								new GetAppProfileUrlsService(context, FrontController.getInstance(mContext).getLocalNonSecureUrl(mContext), hasStatusAPICallRequired).
										executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							}
					} else {
						RunTimeData.getInstance().setFromNotificationAction(true);
						// show the notification -- TBD
						NotificationBar_OverdueDose.cancelNotificationBarById(context, notificationId);
						NotificationBar_OverdueDose.createSignInRequiredNotification(mContext);
					}
				} else {
					launchSplash(mContext);
				}
			}else {
				//NotificationBar_OverdueDose.cancelNotificationBar(mContext);
				launchSplash(mContext);
			}
	}

	private void launchSplash(Context context) {
		NotificationBar_OverdueDose.cancelNotificationBar(context);
		Intent launchApp = new Intent(context, Splash.class);
		launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		FrontController.getInstance(context).hideLateRemindersWhenFromNotifications(context);
		context.startActivity(launchApp);
	}

	private List<Drug> getDrugListForNotificationAction() {
		return Util.getInstance().getRemindersMapDataForNotificationAction(Util.getDrugListForAction(mContext), notificationId, mContext);
	}

	private void performNotificationActionAndMakeServiceCall(Context context, List<Drug> drugsList, String tappedAction, String actionForRequest, String source){
		/*if(RunTimeData.getInstance().getReminderPillpopperTime()!=null
				&& notificationId == RunTimeData.getInstance().getReminderPillpopperTime().getGmtSeconds()) {
			RunTimeData.getInstance().setFromNotificationActionForCurrent(true);
		}*/
		FrontController.getInstance(context).performNotificationAction(context, tappedAction, drugsList, PillpopperTime.now(), source);
		StateDownloadIntentService.startActionNonSecureIntermediateGetState(context);
		NotificationBar_OverdueDose.cancelNotificationBarById(context, notificationId);

		PillpopperLog.say("NotifiactionISSUE : Before if  notificationId : " + notificationId
		+ " And  " + FrontController.getInstance(context).getPendingRemindersStatus(context));
		if(RunTimeData.getInstance().getReminderPillpopperTime()!=null
				&& notificationId == RunTimeData.getInstance().getReminderPillpopperTime().getGmtSeconds()) {
			PillpopperLog.say("NotifiactionISSUE : Before inside if  runtime" + RunTimeData.getInstance().getReminderPillpopperTime().getGmtSeconds());
			//fix for Late reminders issue.
			FrontController.getInstance(context).updateAsPendingRemindersPresent(context);
			PillpopperLog.say("NotifiactionISSUE : After Updating " + FrontController.getInstance(context).getPendingRemindersStatus(context));
		}
	}

	@Override
	public void onHasStatusUpdateResponseReceived(HasStatusUpdateResponseObj response) {
		// Process the response and update the timeStamp and the response into shared Preferences.
		// And Again scan the response fields and invoke the notification if neccessory.
		if(Util.hasPendingAlertsNeedForceSignIn(mContext)){
			//show notification
			NotificationBar_OverdueDose.cancelNotificationBar(mContext);
			NotificationBar_OverdueDose.createSignInRequiredNotification(mContext);
		}
	}
}
