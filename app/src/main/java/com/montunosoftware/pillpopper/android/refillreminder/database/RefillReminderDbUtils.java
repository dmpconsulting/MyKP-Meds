package com.montunosoftware.pillpopper.android.refillreminder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderUtils;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.refillreminder.models.ReminderList;
import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbConstants.GET_ALL_FUTURE_REFILL_REMINDERS;
import static com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbConstants.GET_ALL_OVERDUE_REFILL_REMINDERS;
import static com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbConstants.GET_ALL_REFILL_REMINDER;
import static com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbConstants.GET_NEXT_REFILL_REMINDER_BY_NEXT_REMINDER_TIME;
import static com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbConstants.GET_NEXT_REFILL_REMINDER_WITH_DETAIL;

public class RefillReminderDbUtils {

    private Context context;
    private static RefillReminderDbHandler refillReminderDbHandler;

    private static RefillReminderDbUtils refillReminderDbUtils;

    public static RefillReminderDbUtils getInstance(Context context) {
        if (refillReminderDbUtils == null) {
            refillReminderDbUtils = new RefillReminderDbUtils(context);
        }
        return refillReminderDbUtils;
    }

    private RefillReminderDbUtils(Context context) {
        this.context = context;
        refillReminderDbHandler = RefillReminderDbHandler.getInstance(context);
    }

    public void addRefillReminder(Context context, RefillReminder refillReminder) {
        refillReminderDbHandler.insert(context, RefillReminderDbConstants.TABLE_REFILL_REMINDER, refillReminder, "");

    }

    public List<RefillReminder> getRefillReminders() {
        List<RefillReminder> refillReminders = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = refillReminderDbHandler.executeRawQuery(GET_ALL_REFILL_REMINDER, null);
            if (cursor != null && cursor.moveToNext()) {
                do {
                    RefillReminder refillReminder = new RefillReminder();
                    refillReminder.setReminderGuid(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_GUID)));
                    refillReminder.setUserId(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.USER_ID)));
                    refillReminder.setRecurring(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.RECURRING)) == 1);
                    refillReminder.setFrequency(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.FREQUENCY)));
                    refillReminder.setReminderNote(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_NOTE)));
                    refillReminder.setReminderEndDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_DATE)));
                    refillReminder.setReminderEndTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_TZ_SECS)));
                    refillReminder.setNextReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE)));
                    refillReminder.setOverdueReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_DATE)));
                    refillReminder.setOverdueReminderTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_END_TZ_SECS)));
                    refillReminder.setLastAcknowledgeDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE)));
                    refillReminder.setLastAcknowledgeTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS)));
                    refillReminders.add(refillReminder);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            RefillReminderLog.say(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    RefillReminderLog.say(e);
                }
            }
        }
        return refillReminders;
    }

    public List<RefillReminder> getNextRefillReminders() {
        List<RefillReminder> refillReminders = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = refillReminderDbHandler.executeRawQuery(GET_NEXT_REFILL_REMINDER_WITH_DETAIL, new String[]{});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    RefillReminder refillReminder = new RefillReminder();
                    refillReminder.setReminderGuid(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_GUID)));
                    refillReminder.setUserId(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.USER_ID)));
                    refillReminder.setRecurring(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.RECURRING)) == 1);
                    refillReminder.setFrequency(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.FREQUENCY)));
                    refillReminder.setReminderEndDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_DATE)));
                    refillReminder.setReminderEndTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_TZ_SECS)));
                    refillReminder.setNextReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE)));
                    refillReminder.setOverdueReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_DATE)));
                    refillReminder.setOverdueReminderTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_END_TZ_SECS)));
                    refillReminder.setLastAcknowledgeDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE)));
                    refillReminder.setLastAcknowledgeTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS)));
                    refillReminder.setReminderNote(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_NOTE)));
                    refillReminders.add(refillReminder);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            RefillReminderLog.say(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    RefillReminderLog.say(e);
                }
            }
        }
        return refillReminders;
    }

    public List<RefillReminder> getFutureRefillReminders() {
        List<RefillReminder> refillReminders = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = refillReminderDbHandler.executeRawQuery(GET_ALL_FUTURE_REFILL_REMINDERS, null);
            if (cursor != null && cursor.moveToNext()) {
                do {
                    String nextRefillDateStr = cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE));
                    if(RefillReminderUtils.isValidFutureRefill(nextRefillDateStr)) {
                        RefillReminder refillReminder = new RefillReminder();
                        refillReminder.setReminderGuid(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_GUID)));
                        refillReminder.setUserId(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.USER_ID)));
                        refillReminder.setRecurring(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.RECURRING)) == 1);
                        refillReminder.setFrequency(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.FREQUENCY)));
                        refillReminder.setReminderNote(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_NOTE)));
                        refillReminder.setReminderEndDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_DATE)));
                        refillReminder.setReminderEndTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_TZ_SECS)));
                        refillReminder.setNextReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE)));
                        refillReminder.setOverdueReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_DATE)));
                        refillReminder.setOverdueReminderTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_END_TZ_SECS)));
                        refillReminder.setLastAcknowledgeDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE)));
                        refillReminder.setLastAcknowledgeTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS)));
                        refillReminders.add(refillReminder);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            RefillReminderLog.say(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    RefillReminderLog.say(e);
                }
            }
        }
        return refillReminders;
    }

    public int getRefillRemindersCount() {
        Cursor cursor = null;
        try {
            cursor = refillReminderDbHandler.executeRawQuery(GET_ALL_FUTURE_REFILL_REMINDERS, null);
            if (cursor != null && cursor.getCount()>0 && cursor.moveToFirst()) {
                int count = 0;
                do {
                    String nextRefillDateStr = cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE));
                    if (RefillReminderUtils.isValidFutureRefill(nextRefillDateStr)) {
                        count++;
                    }
                } while (cursor.moveToNext());
                return count;
            }
        } catch (Exception e) {
            RefillReminderLog.say(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    RefillReminderLog.say(e);
                }
            }
        }
        return 0;
    }


    public int updateRefillReminder(RefillReminder refillReminder) {
        ContentValues contentValues = getContentValues(RefillReminderDbConstants.TABLE_REFILL_REMINDER, refillReminder);
        String whereClause = RefillReminderDbConstants.REMINDER_GUID + "=?";
        contentValues.remove(RefillReminderDbConstants.REMINDER_GUID);
        return refillReminderDbHandler.update(RefillReminderDbConstants.TABLE_REFILL_REMINDER, contentValues, whereClause, new String[]{refillReminder.getReminderGuid()});
    }


    public static ContentValues getContentValues(String tableName, Object object) {
        ContentValues contentValues = new ContentValues();
        if (tableName.equalsIgnoreCase(RefillReminderDbConstants.TABLE_REFILL_REMINDER)) {

            if(object instanceof RefillReminder){
                RefillReminder refillReminder = (RefillReminder) object;
                contentValues.put(RefillReminderDbConstants.REMINDER_GUID, refillReminder.getReminderGuid());
                contentValues.put(RefillReminderDbConstants.USER_ID, refillReminder.getUserId());
                contentValues.put(RefillReminderDbConstants.RECURRING, refillReminder.isRecurring() ? 1 : 0);
                contentValues.put(RefillReminderDbConstants.FREQUENCY, refillReminder.getFrequency());
                contentValues.put(RefillReminderDbConstants.REMINDER_END_DATE, refillReminder.getReminderEndDate());
                contentValues.put(RefillReminderDbConstants.REMINDER_END_TZ_SECS, refillReminder.getReminderEndTzSecs());
                contentValues.put(RefillReminderDbConstants.REMINDER_NOTE, refillReminder.getReminderNote());
                contentValues.put(RefillReminderDbConstants.NEXT_REMINDER_DATE, refillReminder.getNextReminderDate());
                contentValues.put(RefillReminderDbConstants.NEXT_REMINDER_END_TZ_SECS, refillReminder.getNextReminderTzSecs());
                contentValues.put(RefillReminderDbConstants.OVERDUE_REMINDER_DATE, refillReminder.getOverdueReminderDate());
                contentValues.put(RefillReminderDbConstants.OVERDUE_REMINDER_END_TZ_SECS, refillReminder.getOverdueReminderTzSecs());
                contentValues.put(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE, refillReminder.getLastAcknowledgeDate());
                contentValues.put(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS, refillReminder.getLastAcknowledgeTzSecs());
            } else if(object instanceof ReminderList){
                ReminderList refillReminder = (ReminderList) object;
                contentValues.put(RefillReminderDbConstants.REMINDER_GUID, refillReminder.getReminderGuid());
                contentValues.put(RefillReminderDbConstants.RECURRING, Boolean.parseBoolean(refillReminder.getRecurring()) ? 1 : 0);
                contentValues.put(RefillReminderDbConstants.FREQUENCY, refillReminder.getFrequency());
                contentValues.put(RefillReminderDbConstants.REMINDER_END_DATE, RefillReminderUtils.convertDateIsoToLong(refillReminder.getReminder_end_date()));
                contentValues.put(RefillReminderDbConstants.REMINDER_END_TZ_SECS, refillReminder.getReminder_end_tz_secs());
                contentValues.put(RefillReminderDbConstants.REMINDER_NOTE, refillReminder.getReminderNote());
                contentValues.put(RefillReminderDbConstants.NEXT_REMINDER_DATE, RefillReminderUtils.convertDateIsoToLong(refillReminder.getNext_reminder_date()));
                contentValues.put(RefillReminderDbConstants.NEXT_REMINDER_END_TZ_SECS, refillReminder.getNext_reminder_tz_secs());
                contentValues.put(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE, RefillReminderUtils.convertDateIsoToLong(refillReminder.getLast_acknowledge_date()));
                contentValues.put(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS, refillReminder.getLast_acknowledge_tz_secs());
                contentValues.put(RefillReminderDbConstants.OVERDUE_REMINDER_DATE, RefillReminderUtils.convertDateIsoToLong(refillReminder.getOverdue_reminder_date()));
                contentValues.put(RefillReminderDbConstants.OVERDUE_REMINDER_END_TZ_SECS, refillReminder.getOverdue_reminder_tz_secs());
            }

        }
        return contentValues;
    }

    /**
     * Inserts getAllRefillReminders API response into database.
     * @param context
     * @param reminderList
     */
    public void insertGetAllRefillReminderData(Context context, ReminderList[] reminderList) {
        for(ReminderList refillReminder : reminderList){
            refillReminderDbHandler.insert(context, RefillReminderDbConstants.TABLE_REFILL_REMINDER, refillReminder, "");
        }
    }

    public List<RefillReminder> getRefillRemindersByNextReminderTime(String nextReminderDate){
        List<RefillReminder> refillReminders = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = refillReminderDbHandler.executeRawQuery(GET_NEXT_REFILL_REMINDER_BY_NEXT_REMINDER_TIME, new String[]{nextReminderDate});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    RefillReminder refillReminder = new RefillReminder();
                    refillReminder.setReminderGuid(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_GUID)));
                    refillReminder.setUserId(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.USER_ID)));
                    refillReminder.setRecurring(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.RECURRING)) == 1);
                    refillReminder.setFrequency(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.FREQUENCY)));
                    refillReminder.setReminderEndDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_DATE)));
                    refillReminder.setReminderEndTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_TZ_SECS)));
                    refillReminder.setNextReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE)));
                    refillReminder.setOverdueReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_DATE)));
                    refillReminder.setOverdueReminderTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_END_TZ_SECS)));
                    refillReminder.setLastAcknowledgeDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE)));
                    refillReminder.setLastAcknowledgeTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS)));
                    refillReminder.setReminderNote(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_NOTE)));
                    refillReminders.add(refillReminder);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            RefillReminderLog.say(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    RefillReminderLog.say(e);
                }
            }
        }
        return refillReminders;
    }

    public List<RefillReminder> getOverdueRefillRemindersForRefresh() {
        List<RefillReminder> refillReminders = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = refillReminderDbHandler.executeRawQuery(GET_ALL_FUTURE_REFILL_REMINDERS, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String nextRefillDateStr = cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE));
//                    boolean isRecurring = (cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.RECURRING)) == 1);
                    if(!RefillReminderUtils.isValidFutureRefill(nextRefillDateStr) /*&& isRecurring*/) {
                        RefillReminder refillReminder = new RefillReminder();
                        refillReminder.setReminderGuid(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_GUID)));
                        refillReminder.setUserId(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.USER_ID)));
                        refillReminder.setRecurring(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.RECURRING)) == 1);
                        refillReminder.setFrequency(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.FREQUENCY)));
                        refillReminder.setReminderEndDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_DATE)));
                        refillReminder.setReminderEndTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_TZ_SECS)));
                        refillReminder.setNextReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE)));
                        refillReminder.setOverdueReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_DATE)));
                        refillReminder.setOverdueReminderTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_END_TZ_SECS)));
                        refillReminder.setLastAcknowledgeDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE)));
                        refillReminder.setLastAcknowledgeTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS)));
                        refillReminder.setReminderNote(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_NOTE)));
                        refillReminders.add(refillReminder);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            RefillReminderLog.say(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    RefillReminderLog.say(e);
                }
            }
        }
        return refillReminders;
    }

    /**
     * Deletes the refill reminder based on the reminderGUID
     * @param reminderGuid
     */
    public void deleteRefillReminderByReminderGUID(String reminderGuid) {
        try {
            RefillReminderNotificationUtil.getInstance(context).cancelRefillReminder(getNextRefillReminderByGUID(reminderGuid), context);
            refillReminderDbHandler.delete(RefillReminderDbConstants.TABLE_REFILL_REMINDER, RefillReminderDbConstants.REMINDER_GUID + "=?" , new String[]{reminderGuid});
        } catch (Exception e) {
            RefillReminderLog.say(e);
        }
    }


    private String getNextRefillReminderByGUID(String reminderGuid) {

        Cursor cursor = null;
        try {
            cursor =  refillReminderDbHandler.executeRawQuery(RefillReminderDbConstants.GET_NEXT_REFILL_REMINDER_BY_REMINDER_GUID, new String[]{reminderGuid});
            if (cursor != null && cursor.getCount()>0 && cursor.moveToFirst()) {
                do {
                    return cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            RefillReminderLog.say(e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    RefillReminderLog.say(e);
                }
            }
        }
        return null;
    }


    public List<RefillReminder> getOverdueRefillRemindersForCards() {
        List<RefillReminder> refillReminders = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = refillReminderDbHandler.executeRawQuery(GET_ALL_OVERDUE_REFILL_REMINDERS, new String[]{});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String overdueRefillDateStr = cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_DATE));
                    String lastAckDateStr = cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE));
                    if(!RefillReminderUtils.isEmptyString(overdueRefillDateStr) && isValidOverdueRefill(overdueRefillDateStr, lastAckDateStr)) {
                        RefillReminder refillReminder = new RefillReminder();
                        refillReminder.setReminderGuid(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_GUID)));
                        refillReminder.setUserId(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.USER_ID)));
                        refillReminder.setRecurring(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.RECURRING)) == 1);
                        refillReminder.setFrequency(cursor.getInt(cursor.getColumnIndex(RefillReminderDbConstants.FREQUENCY)));
                        refillReminder.setReminderEndDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_DATE)));
                        refillReminder.setReminderEndTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_END_TZ_SECS)));
                        refillReminder.setNextReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.NEXT_REMINDER_DATE)));
                        refillReminder.setOverdueReminderDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_DATE)));
                        refillReminder.setOverdueReminderTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.OVERDUE_REMINDER_END_TZ_SECS)));
                        refillReminder.setLastAcknowledgeDate(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE)));
                        refillReminder.setLastAcknowledgeTzSecs(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS)));
                        refillReminder.setReminderNote(cursor.getString(cursor.getColumnIndex(RefillReminderDbConstants.REMINDER_NOTE)));
                        refillReminders.add(refillReminder);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            RefillReminderLog.say(getClass().getName(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    RefillReminderLog.say(getClass().getName(), e);
                }
            }
        }
        return refillReminders;
    }

    public void clearDBTable(String table) {
        refillReminderDbHandler.delete(table, null, null);
    }

    /**
     * @param overdueRefillDateStr overdue refill date stored in the db
     * @param lastAckDateStr
     * @return true if it is valid overdue refill reminder, else return false.
     */
    private boolean isValidOverdueRefill(String overdueRefillDateStr, String lastAckDateStr) {
        try {
            Date overdueRefillDate = new Date();
            overdueRefillDate.setTime(Long.parseLong(overdueRefillDateStr) * 1000);
            // checks if the overdue refill is behind by more than 90days
            return overdueRefillDate.before(new Date())
                    && !isRefillOverDueOver90Days(overdueRefillDateStr) // checks if the overdue refill is behind by more than 90days
                    && !overdueRefillDateStr.equalsIgnoreCase(lastAckDateStr);
        } catch (Exception ex) {
            RefillReminderLog.say("isValidOverdueRefill-", ex);
            return false;
        }
    }

    private boolean isRefillOverDueOver90Days(String overdueRefillDateStr){
        Calendar currentDateTime = Calendar.getInstance();
        Calendar overdueDateTime = Calendar.getInstance();
        overdueDateTime.setTimeInMillis(Long.parseLong(overdueRefillDateStr) * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date overdueDatePart = dateFormat.parse(dateFormat.format(overdueDateTime.getTime()));
            Date currentDatePart = dateFormat.parse(dateFormat.format(currentDateTime.getTime()));

            currentDateTime.setTime(currentDatePart);
            overdueDateTime.setTime(overdueDatePart);

            long diff = currentDateTime.getTimeInMillis() - overdueDateTime.getTimeInMillis();
            long diffDays = diff / (24 * 60 * 60 * 1000);

            return diffDays > 90;

        } catch (ParseException e) {
            RefillReminderLog.say(e);
        }
        return false;
    }

    /**
     * Updates overdue date as null
     * Updates last_ack_date with most recent overdue_date, updates last_ack_tz_secs.
     * @param refillReminder object
     */
    public void acknowledgeRefillReminder(RefillReminder refillReminder) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(RefillReminderDbConstants.OVERDUE_REMINDER_DATE, "null");
        contentValues.put(RefillReminderDbConstants.LAST_ACKNOWLEDGE_DATE, refillReminder.getLastAcknowledgeDate());
        contentValues.put(RefillReminderDbConstants.LAST_ACKNOWLEDGE_TZ_SECS, refillReminder.getLastAcknowledgeTzSecs());
        String whereClause = RefillReminderDbConstants.REMINDER_GUID + "=?";
        refillReminderDbHandler.update(RefillReminderDbConstants.TABLE_REFILL_REMINDER, contentValues, whereClause, new String[]{refillReminder.getReminderGuid()});
    }
}
