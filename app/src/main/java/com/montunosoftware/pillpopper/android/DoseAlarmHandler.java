package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.montunosoftware.pillpopper.android.util.NotificationBar_OverdueDose;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.model.State;

public class DoseAlarmHandler extends BroadcastReceiver
{
	// Called when the dose alarm fires, i.e., when a dose is due.
	@Override
	public void onReceive(Context context, Intent intent)
	{
		PillpopperLog.say("Dose alarm running");

		State currState = PillpopperAppContext.getGlobalAppContext(context).getState(context);
		//currState.recomputeSchedule();

		// Pop up a notification in the notification bar
		//if(currState.isNeedToNotify()){
		NotificationBar_OverdueDose.updateNotificationBar(context, currState);
		//}
		
		// Set the next alarm
		currState.setAlarm(context);
	}

}
