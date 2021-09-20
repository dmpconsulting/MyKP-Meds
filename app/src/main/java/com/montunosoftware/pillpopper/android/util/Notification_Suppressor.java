package com.montunosoftware.pillpopper.android.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class Notification_Suppressor extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(null!=intent && intent.getAction().equalsIgnoreCase(PillpopperAppContext.ACTION_REMINDER_NOTIFICATION_SUPPRESSOR)){
			try {
				NotificationBar_OverdueDose.cancelNotificationBar(context.getApplicationContext());
			} catch (Exception e) {
				PillpopperLog.say("Oops!, Exception while removing the notification from notification bar"
						+ e.getMessage());
			} finally {
				try {
					LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
				} catch (Exception e) {
					PillpopperLog.say("Oops!, Exception while Unregsitering the broadcast receiver" + e.getMessage());
				}
			}
		}
	}
}
