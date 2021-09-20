package com.montunosoftware.pillpopper.android.refillreminder.services;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.pillpopper.android.RunTimeConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderUtils;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by M1024581 on 2/23/2018.
 */

public class RefreshRefillRemindersAsyncTask extends AsyncTask<Void, Void, Void> {

    private final Context mContext;

    public RefreshRefillRemindersAsyncTask(Context context){
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        long currentTimeStamp = new Date().getTime()/1000;
        List<RefillReminder> refillReminderList =
                RefillReminderController.getInstance(mContext).getOverdueRefillRemindersForRefresh();
        RefillReminderLog.say("Refill overdue size " + refillReminderList.size());
        if (null != refillReminderList && refillReminderList.size() > 0) {
            for (RefillReminder refillReminder : refillReminderList) {
                if(!refillReminder.isRecurring()){
                    refillReminder.setOverdueReminderDate(refillReminder.getNextReminderDate());
                    RefillReminderController.getInstance(mContext).updateRefillReminder(refillReminder);
                } else if (!RefillReminderUtils.isEmptyString(refillReminder.getNextReminderDate()) && !refillReminder.getNextReminderDate().equals("-1")) {
                    RefillReminderLog.say("Refill before GUID :" + refillReminder.getReminderGuid());
                    RefillReminderLog.say("Refill before NextReminder :" + RefillReminderUtils.convertDateLongToIso(refillReminder.getNextReminderDate()));
                    if(!RefillReminderUtils.isEmptyString(refillReminder.getOverdueReminderDate())) {
                        RefillReminderLog.say("Refill before Overdue :" + RefillReminderUtils.convertDateLongToIso(refillReminder.getOverdueReminderDate()));
                    }
                    Calendar nextReminder = Calendar.getInstance();
                    nextReminder.setTimeInMillis(Long.parseLong(refillReminder.getNextReminderDate()) * 1000);
                    Calendar dateForComparison = Calendar.getInstance();
                    Calendar endDate = Calendar.getInstance();
                    if (!RefillReminderUtils.isEmptyString(refillReminder.getReminderEndDate()) && !refillReminder.getReminderEndDate().equals("-1")) {
                        endDate.setTimeInMillis(Long.parseLong(refillReminder.getReminderEndDate()) * 1000);
                    }

                    if(dateForComparison.after(endDate)){
                        dateForComparison = endDate;
                    }

                    int i = 1;

                    Calendar tempRefillDate = Calendar.getInstance();
                    tempRefillDate.setTimeInMillis(nextReminder.getTimeInMillis());

                    do {
                        nextReminder.setTimeInMillis(tempRefillDate.getTimeInMillis()); // resets to refill date
                        nextReminder.add(Calendar.DATE, refillReminder.getFrequency() * i);
                        i++;
                    } while (nextReminder.before(dateForComparison));

                    String updatedNextRefillReminder = null;
                    String overdueRefillReminder = null;

                    Calendar overdueRefillReminderDate = Calendar.getInstance();
                    overdueRefillReminderDate.setTimeInMillis(nextReminder.getTimeInMillis());
                    overdueRefillReminderDate.add(Calendar.DATE, -refillReminder.getFrequency());
                    overdueRefillReminder = String.valueOf(Long.valueOf(overdueRefillReminderDate.getTimeInMillis() / 1000));

                    if (!RefillReminderUtils.isEmptyString(refillReminder.getReminderEndDate()) && !refillReminder.getReminderEndDate().equals("-1")) {
                        if (nextReminder.getTimeInMillis() < endDate.getTimeInMillis()
                                && currentTimeStamp < endDate.getTimeInMillis() / 1000) {
                            updatedNextRefillReminder = String.valueOf(Long.valueOf(nextReminder.getTimeInMillis() / 1000));
                        } else {
                            updatedNextRefillReminder = "-1"; // change this to "null" if required
                        }
                    } else {
                        // indicates no end date or end date set to never
                        updatedNextRefillReminder = String.valueOf(Long.valueOf(nextReminder.getTimeInMillis() / 1000));
                    }

                    refillReminder.setNextReminderDate(updatedNextRefillReminder);
                    refillReminder.setOverdueReminderDate(overdueRefillReminder);
                    RefillReminderLog.say("Refill GUID :" + refillReminder.getReminderGuid());
                    RefillReminderLog.say("Refill NextReminder :" + RefillReminderUtils.convertDateLongToIso(refillReminder.getNextReminderDate()));
                    if(!RefillReminderUtils.isEmptyString(refillReminder.getOverdueReminderDate())) {
                        RefillReminderLog.say("Refill Overdue :" + RefillReminderUtils.convertDateLongToIso(refillReminder.getOverdueReminderDate()));
                    }
                    RefillReminderController.getInstance(mContext).updateRefillReminder(refillReminder);
                    RefillReminderUtils.updateRefillAlarm(mContext.getApplicationContext(), refillReminder.getNextReminderDate());
                }
            }
        }

        setAlarmForUpcomingRefillReminders();

        return null;
    }

    private void setAlarmForUpcomingRefillReminders() {
        List<RefillReminder> futureRefillReminderList =
                RefillReminderController.getInstance(mContext).getFutureRefillReminders();

        RefillReminderLog.say("Refill futureRefillReminderList size " + futureRefillReminderList.size());
        if (null != futureRefillReminderList && !futureRefillReminderList.isEmpty()) {
            for (RefillReminder refillReminder : futureRefillReminderList) {
                if(!RefillReminderUtils.isEmptyString(refillReminder.getNextReminderDate()) && !refillReminder.getNextReminderDate().equals("-1")){
                    RefillReminderLog.say("Refill next futureRefillReminder :" + refillReminder.getNextReminderDate());
                    RefillReminderUtils.updateRefillAlarm(mContext, refillReminder.getNextReminderDate());
                }
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(RunTimeData.getInstance().isInitialGetStateCompleted() && !RunTimeConstants.getInstance().isNotificationSuppressor()) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_REFILL_REMINDERS"));
        }
    }
}
