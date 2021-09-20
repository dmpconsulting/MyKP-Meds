package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.model.State;

import java.util.Locale;

public class BootHandler extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		PillpopperLog.say("got boot notification: scheduling alarms");

        // cancel previously set alarm
        RefillReminderNotificationUtil.getInstance(context).clearNextRefillReminderAlarms(context);

        //create new alarm
        RefillReminderNotificationUtil.getInstance(context).createNextRefillReminderAlarms(context);

		State currState = PillpopperAppContext.getGlobalAppContext(context).getState(context);
		currState.setAlarm(context);
		PillpopperLog.say(String.format(Locale.US,"boot complete; %d drugs read", currState.getDrugList().size()));
	}

}
