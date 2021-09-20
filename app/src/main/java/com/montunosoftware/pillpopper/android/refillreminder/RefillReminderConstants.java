package com.montunosoftware.pillpopper.android.refillreminder;

/**
 * Created by M1023050 on 2/9/2018.
 */

/**
 * Holds all the constant fields.
 */
public class RefillReminderConstants {

    public static final boolean IS_LOGGING = false;

    public static final int REFILL_24HRS = 24;
    public static final int REFILL_12HRS = 12;

    public static final String REFILL_REMINDER_CHANNEL_ID = "refill_reminder_channel";
    public static final String REFILL_REMINDER_CHANNEL_NAME = "Refill Reminder";
    public static final String REFILL_REMINDER_CHANNEL_DESCRIPTION = "Description";
    public static final String WEB_LINK_REGEX = "(?i)\\b(?:(?:https?|ftp|file)://)?(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?\\b";
    public static final String HTML_TAG_PATTERN = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
    public static final String INVALID_CHARS_PATTERN = "[\"/&<>]";
    public static final String REFILL_REMINDER_NOTIFICATION_ID = "Notification ID";
    public static final String REFILL_REMINDER_NOTIFICATION_BUNDLE = "Notification_BUNDLE";

    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String PARTNER_ID = "KP";

    public static final String SUCCESS_STRING = "success";

    // Action Keys
    public static final String ACTION_LIST_REFILL_REMINDERS = "ListRefillReminder";
    public static final String ACTION_UPDATE_REFILL_REMINDERS = "UpdateRefillReminder";
    public static final String ACTION_DELETE_REFILL_REMINDER = "DeleteRefillReminder";
    public static final String ACTION_ACKNOWLEDGE_REFILL_REMINDER = "AcknowledgeRefillReminder";


    // Request Keys
    public static final String JSON_KEY_USER_ID = "userId";
    public static final String JSON_KEY_ACTION = "action";
    public static final String JSON_KEY_LANGUAGE = "language";
    public static final String JSON_KEY_CLIENT_VERSION = "clientVersion";
    public static final String JSON_KEY_PARTNER_ID = "partnerId";
    public static final String JSON_KEY_API_VERSION = "apiVersion";
    public static final String JSON_KEY_HARDWARE_ID = "hardwareId";
    public static final String JSON_KEY_REPLAY_ID = "replayId";
    public static final String JSON_KEY_REMINDER_GUID = "reminderGuid";
    public static final String JSON_KEY_PILLPOPPER_REQUEST = "pillpopperRequest";
    public static final String JSON_KEY_NEXT_REMINDER_DATE_REQUEST = "next_reminder_date";
    public static final String JSON_KEY_NEXT_REMINDER_TZ_SEC_REQUEST = "next_reminder_tz_secs";
    public static final String JSON_KEY_OVERDUE_REMINDER_DATE_REQUEST = "overdue_reminder_date";
    public static final String JSON_KEY_OVERDUE_REMINDER_TZ_SEC_REQUEST = "overdue_reminder_tz_secs";
    public static final String JSON_KEY_LAST_ACK_DATE_REQUEST = "last_acknowledge_date";
    public static final String JSON_KEY_LAST_ACK_TZ_SEC_REQUEST = "last_acknowledge_tz_secs";



}
