package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

public class NotificationBarInternalBroadcastHandler extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent receivedIntent)
    {
        // This function is called when someone taps on an item in the notification bar.
        // We just re-send an ordered broadcast so that partner app code has a chance to catch the tap before we process it.
        // This trampoline is necessary because notification bar taps can only send normal broadcasts, not ordered broadcasts.
        PillpopperLog.say("Got internal broadcast from notification bar; sending ordered broadcast");

        PillpopperAppContext appContext = PillpopperAppContext.getGlobalAppContext(context);
        String broadcastName = null;

        switch (appContext.getEdition()) {
            case KP:
                broadcastName = "com.montunosoftware.dosecast.NotificationBarOrderedBroadcastKP";
                break;
            default:
                broadcastName = "com.montunosoftware.dosecast.NotificationBarOrderedBroadcastRetail";
                break;
        }

        // fix for notification becomes nonreactive,
        // register the broadcast receiver to receive the broadcast.
        Util.registerNotificationReceivers(context);

        Intent sentIntent = new Intent(broadcastName);
        sentIntent.putExtras(receivedIntent);
        sentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.sendOrderedBroadcast(sentIntent, null);
    }
}
