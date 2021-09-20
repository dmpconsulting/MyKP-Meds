package com.montunosoftware.pillpopper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.model.State;

public class AlarmResetWorkManager extends Worker {

    public AlarmResetWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
        @Override
        public Result doWork() {

            // cancel previously set alarm
            RefillReminderNotificationUtil.getInstance(getApplicationContext()).clearNextRefillReminderAlarms(getApplicationContext());

            //create new alarm
            RefillReminderNotificationUtil.getInstance(getApplicationContext()).createNextRefillReminderAlarms(getApplicationContext());

            State currState = PillpopperAppContext.getGlobalAppContext(getApplicationContext()).getState(getApplicationContext());
            currState.setAlarm(getApplicationContext());
            return Result.success();
        }
}
