package com.montunosoftware.pillpopper.android.util;

/**
 * @author
 * Created by M1023050 on 3/7/2016.
 */
public class PillpopperConstants {


    // If we require Sync action, this parameter needs to be true else false.
//    public static boolean isSyncAPIRequired = false;
    /**
     * Flag to Enable or Disable Logging for PillpopperLog.class
     * ToDo: Change the IS_LOGGING flag to false before commit
     * ToDo: Read flag and assign during Gradle Build Script
     */
    public static final boolean IS_LOGGING = false;

    public static final int NO_ACTION_TAKEN = 0;
    public static final int TAKEN = 1;
    public static final int SKIPPED =2;
    public static final int TAKE_LATER = 3;
    public static final int TAKE_EARLIER = 4;
    public static final String NOT_OVERDUE = "NOTOVERDUE";
    public static final int TIME_PICKER_INTERVAL = 1;
    public static final int REFILL_TIME_PICKER_INTERVAL = 1;
    public static final int LATE_REMINDER_INTERVAL = 60 * 60 * 1000;     //changing for Reminder regression
    public static final int REQUEST_REFILL_CARD_DETAIL = 1111;
    public static final int REQUEST_SETUP_REMINDER_CARD_DETAIL = 1112;
    public static final int REQUEST_SETUP_VIEW_MEDS_CARD_DETAIL = 1113;
    public static final int REQUEST_SETUP_MANAGE_MEMBERS_CARD_DETAIL = 1114;
    public static final int REQUEST_NEW_KPHC_CARD_DETAIL = 1115;
    public static final int REQUEST_VIEW_REMINDER_CARD_DETAIL = 1116;
    public static final int REQUEST_UPDATED_KPHC_CARD_DETAIL = 1117;
    public static final int REQUEST_LATE_REMINDER_CARD_DETAIL = 1118;
    public static final int REQUEST_QUICK_ACCESS_MENU_SETUP_REMINDERS = 1120;
    public static final int REQUEST_REFILL_REMINDER_CARD_DETAIL = 1121;
    public static final int REQUEST_CURRENT_REMINDER_CARD_DETAIL = 1122;
    public static final int REQUEST_KPHC_DISCONTINUED_CARD_DETAIL = 1123;
    public static final int TEEN_PROXY_HOME_CARD_DETAIL = 1127;
    public static final int GENERIC_HOME_CARD_DETAIL = 1129;
    public static final int REQUEST_VIEW_BATTERY_OPTIMIZER_CARD_DETAIL = 1124;
    public static final int REQUEST_QUICK_ACCESS_CREATE_REFILL_REMINDER = 1125;
    public static final int REQUEST_SAVE_SCHEDULE = 1126;
    public static final String ACTION_TAKE_PILL = "TakePill";
    public static final String ACTION_SKIP_PILL = "SkipPill";
    public static final String ACTION_MISS_PILL = "MissPill";
    public static final String ACTION_TAKEN_EARLIER = "TakenEarlier";
    public static final String ACTION_SKIP_ALL_PILL = "SkipAllPill";
    public static final String ACTION_POST_PONE_PILL = "PostponePill";
    public static final String ACTION_CREATE_PILL = "CreatePill";
    public static final String ACTION_EDIT_PILL = "EditPill";
    public static final String ACTION_GET_STATE = "GetState";
    public static final String ACTION_CREATE_HISTORY_EVENT = "CreateHistoryEvent";
    public static final String ACTION_EDIT_HISTORY_EVENT = "EditHistoryEvent";
    public static final String PARTNER_ID = "KP";
    public static final String ACTION_TAKE_PILL_HISTORY = "takePill";
    public static final String ACTION_SKIP_PILL_HISTORY = "skipPill";
    public static final String ACTION_MISS_PILL_HISTORY = "missPill";
    public static final String ACTION_UPCOMING_PILL_HISTORY = "futurePill";
    public static final String ACTION_REMINDER_PILL_HISTORY = "EMPTY";
    public static final String ACTION_MIXED_PILL_HISTORY = "mixedPillStatus";
    public static final String ACTION_POSTPONE_PILL_HISTORY = "postponePill";

    public static final String ACTION_SETTINGS_HISTORY_DAYS = "doseHistoryDays";
    public static final String ACTION_SETTINGS_SIGNOUT_REMINDERS = "quickviewOptIned";
    public static final String ACTION_SETTINGS_REPEAT_REMINDERS = "secondaryReminderPeriodSecs";
    public static final String ACTION_SETTINGS_NOTIFICATION_FILE = "reminderSoundFilename";
    public static final String ACTION_SETTINGS_ANDROID_REMINDER_FILE_NAME = "androidReminderSoundFilename";

    public static final String IMAGE_TYPE_CUSTOM = "C";
    public static final String IMAGE_TYPE_NDC = "N";
    public static final String IMAGE_TYPE_SERVICE_ID = "S";

    public static final String SUCCESS_TEXT = "success";
    public static final String KEY_DUPLICATE_ENTRY = "Duplicate entry";
    public static final String KEY_DUPLICATE_KEY = "duplicate key";
    public static final String KEY_UNIQUE_TRANSACTION_CONSTRAINT = "unique_transaction_constraint";
    public static final String KEY_NO_SUCH_PILL = "no such pill";

    public static final int MISSING_DOSES_MAXIMUM_DAYS_CHECK = 31;

    //
    public static final int FDB_RETRY_COUNt = 2;


    public static final String QUICKVIEW_OPTED_IN = "1";
    public static final String QUICKVIEW_OPTED_OUT = "0";

    public static final String LAUNCH_MODE="launchMode";

    public static final String KPHC_NEW = "N";
    public static final String KPHC_UPDATED = "U";
    public static final String KPHC_REMOVED = "R";

    public static final String PROXY_NEW = "N";
    public static final String PROXY_ADD_REMOVED = "U";
    public static final String PROXY_REMOVED = "R";

    public static final String LAST_TAKEN_TZSECS = "last_taken";
    public static final String EFF_LAST_TAKEN_TZSECS = "eff_last_taken";
    public static final String NOTIFY_AFTER_TZSECS = "notify_after";
    public static final String MISSED_DOSES_LAST_CHECKED_TZSECS = "missedDosesLastChecked";
    public static final String SCHEDULECHANGED_TZSECS = "scheduleChanged_tz_secs";
    public static final String DOSAGE_TYPE_CUSTOM = "custom";

    private static boolean isAlertActedOn = false;
    private static boolean isRemindersDisplying = false;
    private static boolean isRemindersBeingShown = false;

    private static boolean isCurrentReminderRefreshRequired = false;

    private static boolean isDiscontinuedKPHCMedAlertShown = false;

    public static final String PILL_ID="pill_id";

    public static final int LIMIT_HISTORY = 2; //48hr period
    public static final String ACTION_HISTORY_EVENTS = "GetHistoryEvents";
    public static final String KEY_ACTION = "action";

    private static boolean canShowMedicationList = false;
    public static final String LAUNCH_SOURCE_SCHEDULE = "Schedule";
    public static final String LAUNCH_SOURCE="launchSource";
    public static final int NOTIFICATION_SOUND_MUTE_THRESHOLD=5000;
    private static boolean canShowScheduleScreen;

    public static final String WEB_LINK_REGEX = "(?i)\\b(?:(?:https?|ftp|file)://)?(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?\\b";
    public static final String HTML_TAG_PATTERN = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
    public static final String INVALID_CHARS_PATTERN = "[\"/&<>]";

    public static final int CONNECTION_READ_TIME_OUT_SECONDS = 60;

    public static boolean isAlertActedOn() {
        return isAlertActedOn;
    }

    public static void setIsAlertActedOn(boolean isAlertActedOn) {
        PillpopperConstants.isAlertActedOn = isAlertActedOn;
    }

    public static void setIsRemindersDisplying(boolean isRemindersDisplying) {
        PillpopperConstants.isRemindersDisplying = isRemindersDisplying;
    }

    public static boolean isRemindersBeingShown() {
        return isRemindersBeingShown;
    }

    public static void setIsRemindersBeingShown(boolean isRemindersBeingShown) {
        PillpopperConstants.isRemindersBeingShown = isRemindersBeingShown;
    }

    public static boolean isCurrentReminderRefreshRequired() {
        return isCurrentReminderRefreshRequired;
    }

    public static void setIsCurrentReminderRefreshRequired(boolean isCurrentReminderRefreshRequired) {
        PillpopperConstants.isCurrentReminderRefreshRequired = isCurrentReminderRefreshRequired;
    }

    public static boolean isDiscontinuedKPHCMedAlertShown() {
        return isDiscontinuedKPHCMedAlertShown;
    }

    public static void setIsDiscontinuedKPHCMedAlertShown(boolean isDiscontinuedKPHCMedAlertShown) {
        PillpopperConstants.isDiscontinuedKPHCMedAlertShown = isDiscontinuedKPHCMedAlertShown;
    }

    public static boolean isCanShowMedicationList() {
        return canShowMedicationList;
    }

    public static void setCanShowMedicationList(boolean canShowMedicationList) {
        PillpopperConstants.canShowMedicationList = canShowMedicationList;
    }

    public static boolean isCanShowScheduleScreen() {
        return canShowScheduleScreen;
    }

    public static void setCanShowScheduleScreen(boolean canShowScheduleScreen) {
        PillpopperConstants.canShowScheduleScreen = canShowScheduleScreen;
    }
}
