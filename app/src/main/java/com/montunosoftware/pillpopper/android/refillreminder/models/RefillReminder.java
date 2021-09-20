package com.montunosoftware.pillpopper.android.refillreminder.models;

import java.io.Serializable;

public class RefillReminder implements Serializable {

    private String reminderGuid;
    private String userId;
    private boolean recurring;
    private int frequency;
    private String reminderEndDate;
    private String reminderEndTzSecs;
    private String reminderNote;
    private String nextReminderDate;
    private String nextReminderTzSecs;
    private String overdueReminderDate;
    private String overdueReminderTzSecs;
    private String lastAcknowledgeDate;
    private String lastAcknowledgeTzSecs;

    public String getReminderGuid() {
        return reminderGuid;
    }

    public void setReminderGuid(String reminderGuid) {
        this.reminderGuid = reminderGuid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getReminderEndDate() {
        return reminderEndDate;
    }

    public void setReminderEndDate(String reminderEndDate) {
        this.reminderEndDate = reminderEndDate;
    }

    public String getReminderEndTzSecs() {
        return reminderEndTzSecs;
    }

    public void setReminderEndTzSecs(String reminderEndTzSecs) {
        this.reminderEndTzSecs = reminderEndTzSecs;
    }

    public String getReminderNote() {
        return reminderNote;
    }

    public void setReminderNote(String reminderNote) {
        this.reminderNote = reminderNote;
    }

    public String getNextReminderDate() {
        return nextReminderDate;
    }

    public void setNextReminderDate(String nextReminderDate) {
        this.nextReminderDate = nextReminderDate;
    }

    public String getNextReminderTzSecs() {
        return nextReminderTzSecs;
    }

    public void setNextReminderTzSecs(String nextReminderTzSecs) {
        this.nextReminderTzSecs = nextReminderTzSecs;
    }

    public String getOverdueReminderDate() {
        return overdueReminderDate;
    }

    public void setOverdueReminderDate(String overdueReminderDate) {
        this.overdueReminderDate = overdueReminderDate;
    }

    public String getOverdueReminderTzSecs() {
        return overdueReminderTzSecs;
    }

    public void setOverdueReminderTzSecs(String overdueReminderTzSecs) {
        this.overdueReminderTzSecs = overdueReminderTzSecs;
    }

    public String getLastAcknowledgeDate() {
        return lastAcknowledgeDate;
    }

    public void setLastAcknowledgeDate(String lastAcknowledgeDate) {
        this.lastAcknowledgeDate = lastAcknowledgeDate;
    }

    public String getLastAcknowledgeTzSecs() {
        return lastAcknowledgeTzSecs;
    }

    public void setLastAcknowledgeTzSecs(String lastAcknowledgeTzSecs) {
        this.lastAcknowledgeTzSecs = lastAcknowledgeTzSecs;
    }
}
