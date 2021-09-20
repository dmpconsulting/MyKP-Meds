package com.montunosoftware.pillpopper.android.refillreminder.controllers;

import android.content.Context;

import com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbConstants;
import com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbUtils;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.refillreminder.models.ReminderList;
import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;

import java.util.List;

public class RefillReminderController {

    private static RefillReminderDbUtils refillReminderDbUtils;
    private static RefillReminderController refillReminderController;

    public static RefillReminderController getInstance(Context context) {
        if (refillReminderController == null) {
            refillReminderController = new RefillReminderController(context.getApplicationContext());
        }
        return refillReminderController;
    }

    private RefillReminderController(Context context) {
        refillReminderDbUtils = RefillReminderDbUtils.getInstance(context);
    }

    public void addRefillReminder(Context context,RefillReminder refillReminder){
        refillReminderDbUtils.addRefillReminder(context,refillReminder);
        RefillReminderNotificationUtil.getInstance(context).createNextRefillReminderAlarms(context, refillReminder.getNextReminderDate());
    }


    /**
     * Inserts getAllRefillReminders API response into database.
     * @param context
     * @param reminderList
     */
    public void insertGetAllRefillReminderData(Context context, ReminderList[] reminderList) {
        refillReminderDbUtils.insertGetAllRefillReminderData(context, reminderList);
    }

    public List<RefillReminder> getRefillReminders(){
        return refillReminderDbUtils.getRefillReminders();
    }
    public List<RefillReminder> getFutureRefillReminders() {
        return refillReminderDbUtils.getFutureRefillReminders();
    }

    public int getRefillRemindersCount() {
        return refillReminderDbUtils.getRefillRemindersCount();
    }

    public List<RefillReminder> getNextRefillReminders(){
        return refillReminderDbUtils.getNextRefillReminders();
    }

    public List<RefillReminder> getRefillRemindersByNextReminderTime(String nextReminderDate){
        return refillReminderDbUtils.getRefillRemindersByNextReminderTime(nextReminderDate);
    }

    public int updateRefillReminder(RefillReminder refillReminder){
        return refillReminderDbUtils.updateRefillReminder(refillReminder);
    }

    public List<RefillReminder> getOverdueRefillRemindersForRefresh(){
        return refillReminderDbUtils.getOverdueRefillRemindersForRefresh();
    }

    /**
     * Deletes the refill reminder by taking the reminderGUID as an input.
     * @param reminderGuid
     */
    public void deleteRefillReminderByReminderGUID(String reminderGuid) {
        refillReminderDbUtils.deleteRefillReminderByReminderGUID(reminderGuid);
    }

    public List<RefillReminder> getOverdueRefillRemindersForCards() {
        return refillReminderDbUtils.getOverdueRefillRemindersForCards();
    }

    public void clearDBTable() {
        refillReminderDbUtils.clearDBTable(RefillReminderDbConstants.TABLE_REFILL_REMINDER);
    }

    /**
     * invokes DB Util's acknowledgeRefillReminder method
     * Updates overdue date as null
     * Updates last_ack_date with most recent overdue_date, updates last_ack_tz_secs.
     * @param refillReminder object
     */
    public void acknowledgeRefillReminder(RefillReminder refillReminder) {
        refillReminderDbUtils.acknowledgeRefillReminder(refillReminder);
    }
}
