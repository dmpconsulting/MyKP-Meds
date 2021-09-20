package com.montunosoftware.pillpopper.model;

/**
 * @author
 * Created by M1024581 on 7/7/2016.
 */
public class UserPreferences {

    private String userId;
    private boolean preventEarlyDoseWarningEnabled;
    private boolean signedOutRemindersEnabled;
    private String doseHistoryStorageDays;
    private String notificationSoundPath;
    private String repeatRemindersAfter;

    public String getDstOffset_secs() {
        return dstOffset_secs;
    }

    public void setDstOffset_secs(String dstOffset_secs) {
        this.dstOffset_secs = dstOffset_secs;
    }

    private String dstOffset_secs;

    public String getTz_name() {
        return tz_name;
    }

    public void setTz_name(String tz_name) {
        this.tz_name = tz_name;
    }

    private String tz_name;

    public String getTz_sec() {
        return tz_sec;
    }

    public void setTz_sec(String tz_sec) {
        this.tz_sec = tz_sec;
    }

    private String tz_sec;

    public String getAndroidReminderSoundFilename() {
        return androidReminderSoundFilename;
    }

    public void setAndroidReminderSoundFilename(String androidReminderSoundFilename) {
        this.androidReminderSoundFilename = androidReminderSoundFilename;
    }

    private String androidReminderSoundFilename;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isPreventEarlyDoseWarningEnabled() {
        return preventEarlyDoseWarningEnabled;
    }

    public void setPreventEarlyDoseWarningEnabled(boolean preventEarlyDoseWarningEnabled) {
        this.preventEarlyDoseWarningEnabled = preventEarlyDoseWarningEnabled;
    }

    public boolean isSignedOutRemindersEnabled() {
        return signedOutRemindersEnabled;
    }

    public void setSignedOutRemindersEnabled(boolean signedOutRemindersEnabled) {
        this.signedOutRemindersEnabled = signedOutRemindersEnabled;
    }

    public String getNotificationSoundPath() {
        return notificationSoundPath;
    }

    public void setNotificationSoundPath(String notificationSoundPath) {
        this.notificationSoundPath = notificationSoundPath;
    }

    public String getDoseHistoryStorageDays() {
        return doseHistoryStorageDays;
    }

    public void setDoseHistoryStorageDays(String doseHistoryStorageDays) {
        this.doseHistoryStorageDays = doseHistoryStorageDays;
    }

    public String getRepeatRemindersAfter() {
        return repeatRemindersAfter;
    }

    public void setRepeatRemindersAfter(String repeatRemindersAfter) {
        this.repeatRemindersAfter = repeatRemindersAfter;
    }
}
