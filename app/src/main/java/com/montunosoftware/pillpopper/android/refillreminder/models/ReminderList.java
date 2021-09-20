package com.montunosoftware.pillpopper.android.refillreminder.models;

/**
 * Created by M1023050 on 2/21/2018.
 */

public class ReminderList {

    private String reminder_end_date;

    private String last_acknowledge_date;

    private String last_acknowledge_tz_secs;

    private String reminderGuid;

    private String reminder_end_tz_secs;

    private String reminderNote;

    private String frequency;

    private String next_reminder_tz_secs;

    private String next_reminder_date;

    private String recurring;

    private String overdue_reminder_date;

    private String overdue_reminder_tz_secs;

    public String getOverdue_reminder_date() {
        return overdue_reminder_date;
    }

    public void setOverdue_reminder_date(String overdue_reminder_date) {
        this.overdue_reminder_date = overdue_reminder_date;
    }

    public String getOverdue_reminder_tz_secs() {
        return overdue_reminder_tz_secs;
    }

    public void setOverdue_reminder_tz_secs(String overdue_reminder_tz_secs) {
        this.overdue_reminder_tz_secs = overdue_reminder_tz_secs;
    }

    public String getLast_acknowledge_date() {
        return last_acknowledge_date;
    }

    public void setLast_acknowledge_date(String last_acknowledge_date) {
        this.last_acknowledge_date = last_acknowledge_date;
    }

    public String getLast_acknowledge_tz_secs() {
        return last_acknowledge_tz_secs;
    }

    public void setLast_acknowledge_tz_secs(String last_acknowledge_tz_secs) {
        this.last_acknowledge_tz_secs = last_acknowledge_tz_secs;
    }

    public String getReminderGuid() {
        return reminderGuid;
    }

    public void setReminderGuid(String reminderGuid) {
        this.reminderGuid = reminderGuid;
    }


    public String getReminderNote() {
        return reminderNote;
    }

    public void setReminderNote(String reminderNote) {
        this.reminderNote = reminderNote;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getNext_reminder_tz_secs() {
        return next_reminder_tz_secs;
    }

    public void setNext_reminder_tz_secs(String next_reminder_tz_secs) {
        this.next_reminder_tz_secs = next_reminder_tz_secs;
    }

    public String getNext_reminder_date() {
        return next_reminder_date;
    }

    public void setNext_reminder_date(String next_reminder_date) {
        this.next_reminder_date = next_reminder_date;
    }

    public String getRecurring() {
        return recurring;
    }

    public void setRecurring(String recurring) {
        this.recurring = recurring;
    }

    public String getReminder_end_date() {
        return reminder_end_date;
    }

    public void setReminder_end_date(String reminder_end_date) {
        this.reminder_end_date = reminder_end_date;
    }

    public String getReminder_end_tz_secs() {
        return reminder_end_tz_secs;
    }

    public void setReminder_end_tz_secs(String reminder_end_tz_secs) {
        this.reminder_end_tz_secs = reminder_end_tz_secs;
    }

}
