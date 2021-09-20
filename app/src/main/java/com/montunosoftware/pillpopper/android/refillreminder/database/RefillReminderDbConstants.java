package com.montunosoftware.pillpopper.android.refillreminder.database;

public class RefillReminderDbConstants {

    public static final String MYMEDS_REFILL_DATABASE = "mykpmedrefill.db";

    public static final String DROP_TABLE_REFILL_REMINDER = "DROP TABLE IF EXISTS MMRefillReminder;";

    // if there is any change in CREATE TABLE schema, handle database onUpgrade Scenario.
    // write appropriate alter table queries which should be called from each version going upwards from 3.6
    public static final String CREATE_TABLE_REFILL_REMINDER ="CREATE TABLE IF NOT EXISTS MMRefillReminder (reminderGuid VARCHAR(300),userId VARCHAR(300),recurring INTEGER,frequency INTEGER,reminder_end_date VARCHAR(300),reminder_end_tz_secs VARCHAR(300),reminderNote VARCHAR(1000),next_reminder_date  VARCHAR(300),next_reminder_tz_secs VARCHAR(300),overdue_reminder_date  VARCHAR(300),overdue_reminder_tz_secs VARCHAR(300),last_acknowledge_date VARCHAR(300),last_acknowledge_tz_secs VARCHAR(300));";

    public static final String TABLE_REFILL_REMINDER = "MMRefillReminder";

    // MMRefillReminder TABLE FIELDS
    public static final String REMINDER_GUID = "reminderGuid";
    public static final String USER_ID = "userId";
    public static final String RECURRING = "recurring";
    public static final String FREQUENCY = "frequency";
    public static final String REMINDER_END_DATE = "reminder_end_date";
    public static final String REMINDER_END_TZ_SECS = "reminder_end_tz_secs";
    public static final String REMINDER_NOTE = "reminderNote";
    public static final String NEXT_REMINDER_DATE = "next_reminder_date";
    public static final String NEXT_REMINDER_END_TZ_SECS = "next_reminder_tz_secs";
    public static final String OVERDUE_REMINDER_DATE = "overdue_reminder_date";
    public static final String OVERDUE_REMINDER_END_TZ_SECS = "overdue_reminder_tz_secs";
    public static final String LAST_ACKNOWLEDGE_DATE = "last_acknowledge_date";
    public static final String LAST_ACKNOWLEDGE_TZ_SECS = "last_acknowledge_tz_secs";


    public static final String GET_ALL_REFILL_REMINDER = "select * from MMRefillReminder";
    public static final String GET_NEXT_REFILL_REMINDER_BY_NEXT_REMINDER_TIME = "select * from MMRefillReminder rr where rr.next_reminder_date <=? and rr.next_reminder_date != -1";
    public static final String GET_NEXT_REFILL_REMINDER_WITH_DETAIL = "select * from MMRefillReminder rr where (rr.recurring = 1 and rr.reminder_end_date =-1) or rr.reminder_end_date is null or rr.next_reminder_date != -1 or (rr.next_reminder_date BETWEEN rr.overdue_reminder_date and rr.reminder_end_date) group by rr.next_reminder_date";
    public static final String GET_ALL_FUTURE_REFILL_REMINDERS = "select * from MMRefillReminder rr where rr.next_reminder_date != -1 ORDER BY rr.next_reminder_date";
    public static final String GET_ALL_OVERDUE_REFILL_REMINDERS = "select * from MMRefillReminder rr where rr.overdue_reminder_date != -1 ORDER BY rr.overdue_reminder_date desc";
    public static final String GET_NEXT_REFILL_REMINDER_BY_REMINDER_GUID = "select rr.next_reminder_date from MMRefillReminder rr where rr.reminderGuid=?";

}
