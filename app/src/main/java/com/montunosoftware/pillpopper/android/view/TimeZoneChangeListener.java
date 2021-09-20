package com.montunosoftware.pillpopper.android.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.service.AdjustSchedulesOnTimezoneChangeAsyncTask;


/**
 * Created by M1023050 on 5/8/2017.
 */

public class TimeZoneChangeListener extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        PillpopperRunTime.getInstance().setTimeZoneChanged(true);
        UniqueDeviceId.getHardwareId(context); // initialization the cacheId to get Hardware Id.
        new AdjustSchedulesOnTimezoneChangeAsyncTask(context).execute();
    }
}
