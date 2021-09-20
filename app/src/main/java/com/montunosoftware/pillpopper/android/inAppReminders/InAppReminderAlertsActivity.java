package com.montunosoftware.pillpopper.android.inAppReminders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.RunTimeConstants;
import com.montunosoftware.pillpopper.android.util.NotificationBar;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class InAppReminderAlertsActivity extends FragmentActivity implements View.OnClickListener {

    private int alertId;
    private Context mContext;
    private AlertDialog medicationReminderAlert;
    private int drugsCount = 0;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mContext = this;
        if (null != getIntent()) {
            readIntentAndDisplayReminder(getIntent().getExtras());
        } else {
            //something is wrong, close the activity
            finish();
        }
    }

    private void readIntentAndDisplayReminder(Bundle intentBundle) {
        if (null != medicationReminderAlert && medicationReminderAlert.isShowing()) {
            medicationReminderAlert.dismiss();
        }
        if (null != intentBundle) {
            String reminderType = intentBundle.getString("ReminderType");
            alertId = intentBundle.getInt("AlertID");
            if (null != reminderType && reminderType.equalsIgnoreCase("Medication Reminder")) {
                showReminderAlert(this);
            }
        } else {
            PillpopperLog.say("error launching inApp Alert");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readIntentAndDisplayReminder(intent.getExtras());
    }

    @Override
    public void onResume() {
        super.onResume();
        RunTimeConstants.getInstance().setNotificationSuppressor(false);
        RunTimeData.getInstance().setAppVisibleFlg(true);
        LoggerUtils.info("Starting the Timer ");
        ActivationController.getInstance().startTimer(this);
        // launch the login screen if needed, or timed out
        PillpopperAppContext.getGlobalAppContext(this).kpMaybeLaunchLoginScreen(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RunTimeConstants.getInstance().setNotificationSuppressor(true);
        RunTimeData.getInstance().setAppVisibleFlg(false);
    }

    private void showReminderAlert(Context context) {
        AppConstants.isFromInAppAlerts = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View customLayout = getLayoutInflater().inflate(R.layout.inapp_medication_reminders_alert_layout, null);
        builder.setView(customLayout);
        TextView alertTitle = customLayout.findViewById(R.id.alert_title);
        alertTitle.setText(getString(R.string.inapp_reminder_title));
        TextView alertMessage = customLayout.findViewById(R.id.alert_message);
        alertMessage.setText(getAlertContentText(context));
        Button skipButton = customLayout.findViewById(R.id.skip_button);
        Button takenButton = customLayout.findViewById(R.id.taken_button);
        TextView viewMedicationsButton = customLayout.findViewById(R.id.view_medications);
        ImageView closeIcon = customLayout.findViewById(R.id.alert_close_icon);
        drugsCount = getCurrentReminderDrugsCount();
        viewMedicationsButton.setText(drugsCount > 1 ? getString(R.string.view_medication) + "s" : getString(R.string.view_medication));
        builder.setCancelable(false);
        medicationReminderAlert = builder.create();
        if (null != medicationReminderAlert && !medicationReminderAlert.isShowing() && !((Activity) context).isFinishing()) {
            medicationReminderAlert.show();
        }
        closeIcon.setOnClickListener(this);
        skipButton.setOnClickListener(this);
        takenButton.setOnClickListener(this);
        viewMedicationsButton.setOnClickListener(this);
    }

    private int getCurrentReminderDrugsCount() {
        List<Drug> drugList = getDrugListForAlertAction();
        return drugList.size();
    }

    private List<Drug> getDrugListForAlertAction() {
        return Util.getInstance().getRemindersMapDataForNotificationAction(Util.getDrugListForAction(this), alertId, this);
    }

    private static String getAlertContentText(Context context) {
        SimpleDateFormat simpleDate = new SimpleDateFormat("h:mm a");
        Calendar calendar = Calendar.getInstance();
        if (null != RunTimeData.getInstance().getReminderPillpopperTime()) {
            calendar.setTimeInMillis(RunTimeData.getInstance().getReminderPillpopperTime().getGmtMilliseconds());
        } else if (null != RunTimeData.getInstance().getSecondaryReminderPillpopperTime()) {
            calendar.setTimeInMillis(RunTimeData.getInstance().getSecondaryReminderPillpopperTime().getGmtMilliseconds());
        }
        return String.format(context.getString(R.string.reminder_notification_message), simpleDate.format(calendar.getTime()));
    }

    @Override
    public void onClick(View view) {
        medicationReminderAlert.dismiss();
        List<Drug> drugList = getDrugListForAlertAction();
        switch (view.getId()) {
            case R.id.skip_button:
                for(Drug drug : drugList){
                    drug.setIsActionDateRequired(false);
                }
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.IN_APP_NOTIFICATIONS_ACTIONS, FireBaseConstants.ParamName.ACTION_TYPE, FireBaseConstants.ParamValue.SKIPPED);
                FrontController.getInstance(mContext).performNotificationAction(mContext, NotificationBar.NOTIFICATION_ACTION_SKIP, getDrugListForAlertAction(), PillpopperTime.now(), FireBaseConstants.ParamValue.SKIPPED);
                RunTimeData.getInstance().setHistoryMedChanged(true); // to refresh history after taking action
                break;
            case R.id.taken_button:
                for(Drug drug : drugList){
                    drug.setIsActionDateRequired(true);
                }
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.IN_APP_NOTIFICATIONS_ACTIONS, FireBaseConstants.ParamName.ACTION_TYPE, FireBaseConstants.ParamValue.TAKEN);
                FrontController.getInstance(mContext).performNotificationAction(mContext, NotificationBar.NOTIFICATION_ACTION_TAKE, getDrugListForAlertAction(), PillpopperTime.now(), FireBaseConstants.ParamValue.TAKEN);
                RunTimeData.getInstance().setHistoryMedChanged(true); // to refresh history after taking action
                break;
            case R.id.view_medications:
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.IN_APP_NOTIFICATIONS_ACTIONS, FireBaseConstants.ParamName.ACTION_TYPE, FireBaseConstants.ParamValue.VIEW);
                Intent intent = new Intent(mContext, CurrentReminderDetailActivity.class);
                mContext.startActivity(intent);
                break;
            case R.id.alert_close_icon:
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.IN_APP_NOTIFICATIONS_ACTIONS, FireBaseConstants.ParamName.ACTION_TYPE, FireBaseConstants.ParamValue.DISMISS);
                RunTimeData.getInstance().setHistoryMedChanged(true); // to refresh history after current reminder occurs
                break;
        }
        AppConstants.isFromInAppAlerts = false;
        finish();
        // refresh the current reminders after action, if in home screen
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("REFRESH_CURRENT_REMINDERS"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppConstants.isFromInAppAlerts = false;
        if(null != medicationReminderAlert && medicationReminderAlert.isShowing()){
            medicationReminderAlert.dismiss();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onBackPressed();
    }
}
