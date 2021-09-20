package com.montunosoftware.pillpopper.android.refillreminder.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderUtils;
import com.montunosoftware.pillpopper.android.refillreminder.services.RefreshRefillRemindersAsyncTask;

public class RefillReminderAlarmHandler extends BroadcastReceiver {

    private String reminderTime;
    @Override
    public void onReceive(Context context, Intent intent) {
        RefillReminderNotificationUtil refillReminderNotificationUtil = RefillReminderNotificationUtil.getInstance(context.getApplicationContext());
        Bundle bundle = intent.getBundleExtra(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_BUNDLE);
        if (bundle != null) {
            reminderTime = (String)bundle.getSerializable(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_ID);
        }
        if (!"android.intent.action.BOOT_COMPLETED".equalsIgnoreCase(intent.getAction())) {
            try{
                // adding a second delay to generate refill reminder notification. fix for defect DE22693
                new Handler().postDelayed(() -> refillReminderNotificationUtil.generateNotification(context.getApplicationContext(), (Long.parseLong(reminderTime) + 1)), 1000);
                refillReminderNotificationUtil.updateOverDueDate(context.getApplicationContext(), reminderTime);
            }catch (Exception e){
                RefillReminderLog.say("Refill exception in RefillReminderAlarmHandler" + e);
                refillReminderNotificationUtil.generateNotification(context.getApplicationContext(), 101);
            }
        }
        RefillReminderLog.say("Refill Alarm triggered -- " + RefillReminderUtils.convertDateLongToIso(reminderTime));
        new RefreshRefillRemindersAsyncTask(context.getApplicationContext()).execute();

    }
}
