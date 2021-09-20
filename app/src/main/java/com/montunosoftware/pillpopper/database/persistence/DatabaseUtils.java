package com.montunosoftware.pillpopper.database.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.view.View;

import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.ScheduleMainTimeHeader;
import com.montunosoftware.pillpopper.android.util.NotificationBar;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.database.model.ArchiveListDataWrapper;
import com.montunosoftware.pillpopper.database.model.ArchiveListUserDropDownData;
import com.montunosoftware.pillpopper.database.model.GetHistoryEvents;
import com.montunosoftware.pillpopper.database.model.GetHistoryPreferences;
import com.montunosoftware.pillpopper.database.model.HistoryEditEvent;
import com.montunosoftware.pillpopper.database.model.HistoryEvent;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.database.model.PillList;
import com.montunosoftware.pillpopper.database.model.PillPreferences;
import com.montunosoftware.pillpopper.database.model.UserList;
import com.montunosoftware.pillpopper.model.ArchiveDetailDrug;
import com.montunosoftware.pillpopper.model.ArchiveListDrug;
import com.montunosoftware.pillpopper.model.BulkSchedule;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;
import com.montunosoftware.pillpopper.model.DoseEventCollection;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.HourMinute;
import com.montunosoftware.pillpopper.model.KphcDrug;
import com.montunosoftware.pillpopper.model.ManageMemberObj;
import com.montunosoftware.pillpopper.model.PendingImageRequest;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.Schedule;
import com.montunosoftware.pillpopper.model.ScheduleListItemDataWrapper;
import com.montunosoftware.pillpopper.model.ScheduleMainDrug;
import com.montunosoftware.pillpopper.model.State;
import com.montunosoftware.pillpopper.model.TimeList;
import com.montunosoftware.pillpopper.model.UserPreferences;
import com.montunosoftware.pillpopper.network.model.FailedImageObj;
import com.montunosoftware.pillpopper.service.images.sync.model.FdbImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.model.RxRefillUserData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * This class is a utility class for the database. This class will be used to
 * retrieve/Store any type of data from/to the database
 *
 * @author Madan S
 */

public class DatabaseUtils {
    private static DatabaseUtils sDatabaseUtils;
    private static DatabaseHandler databaseHandler;

    /***
     * Singleton instance of database handler.
     *
     * @return instance of database handler
     */
    public static DatabaseUtils getInstance(Context context) {
        if (sDatabaseUtils == null)
            sDatabaseUtils = new DatabaseUtils();

        databaseHandler = DatabaseHandler.getInstance(context);

        return sDatabaseUtils;
    }


    /***
     * Returns the content value object for a table which will be used to
     * insert.
     *
     *
     * @param context
     * @param tableName name of table
     * @param object    values
     * @return content value object
     */

    public static ContentValues getContentValues(Context context, String tableName, Object object, String userId) {
        ContentValues contentValues = new ContentValues();
        if (DatabaseConstants.PILL_TABLE.equalsIgnoreCase(tableName)) {
            if (object instanceof UserList) {
                contentValues = setContentValues_PillData((UserList) object);
            } else {
                contentValues = setContentValues_InsertPill((PillList) object, userId);
            }
        } else if (DatabaseConstants.USER_PREFERENCE_TABLE.equalsIgnoreCase(tableName)) {
            contentValues = setContentValues_User_PreferenceData(context, (UserList) object);
        } else if (DatabaseConstants.PILL_PREFERENCE_TABLE.equalsIgnoreCase(tableName)) {
            contentValues = setContentValues_Pill_PreferenceData((PillList) object);
        } else if (DatabaseConstants.PILL_SCHEDULE_TABLE.equalsIgnoreCase(tableName)) {
            contentValues = setContentValues_PillSchedule((PillList) object);
        } else if (DatabaseConstants.HISTORY_TABLE.equalsIgnoreCase(tableName)) {
            contentValues = setContentValues_Pill_HistoryData((GetHistoryEvents) object);
        } else if (DatabaseConstants.USER_TABLE.equalsIgnoreCase(tableName)) {
            contentValues = setContentValues_UsersData(context, (User) object);
        } else if (DatabaseConstants.LOG_ENTRY_TABLE.equalsIgnoreCase(tableName)) {
            contentValues = setContentValues_LogEntryData((LogEntryModel) object);
        } else if (DatabaseConstants.USER_REMINDERS_TABLE.equalsIgnoreCase(tableName)) {
            contentValues = setContentValues_UserRemindersData((User) object);
        } else if (DatabaseConstants.HISTORY_PREFERENCE_TABLE.equalsIgnoreCase(tableName)) {
            contentValues = setContentValues_History_PreferenceData((GetHistoryEvents) object);
        }
        return contentValues;
    }

    private static ContentValues setContentValues_LogEntryData(LogEntryModel logEntryModel) {
        ContentValues contentValues = new ContentValues();
        if (null != logEntryModel) {
            contentValues.put(DatabaseConstants.LOG_ENTRY_DATE_ADDED, logEntryModel.getDateAdded());
            contentValues.put(DatabaseConstants.LOG_ENTRY, logEntryModel.getEntryJSONObject().toString());
            contentValues.put(DatabaseConstants.LOG_ENTRY_GUID, logEntryModel.getReplyID());
            contentValues.put(DatabaseConstants.LOG_ENTRY_LAST_UPLOAD_ATTEMPT, logEntryModel.getLastUploadAttempt());
            contentValues.put(DatabaseConstants.LOG_ENTRY_LAST_UPLOAD_RESPONSE, logEntryModel.getLastUploadResponse());
            contentValues.put(DatabaseConstants.LOG_ENTRY_ACTION, logEntryModel.getAction());
        } else {
            PillpopperLog.say("No Log entry Model");
        }
        return contentValues;
    }

    private static ContentValues setContentValues_UsersData(Context context, User user) {
        ContentValues contentValues = new ContentValues();
        if (null != user) {
            try {
                contentValues.put(DatabaseConstants.USER_ID, user.getUserId());
                contentValues.put(DatabaseConstants.USER_TYPE, user.getUserType());
                contentValues.put(DatabaseConstants.RELATION_DESC, user.getRelationDesc());
                contentValues.put(DatabaseConstants.REL_ID, user.getRelId());
                contentValues.put(DatabaseConstants.ENABLED, user.getEnabled());
                contentValues.put(DatabaseConstants.NICK_NAME, user.getNickName());
                contentValues.put(DatabaseConstants.DISPLAY_NAME, user.getDisplayName());
                contentValues.put(DatabaseConstants.FIRST_NAME, user.getFirstName());
                contentValues.put(DatabaseConstants.LAST_NAME, user.getLastName());
                contentValues.put(DatabaseConstants.MIDDLE_NAME, user.getMiddleName());
                contentValues.put(DatabaseConstants.LAST_SYNC_TOKEN, user.getLastSyncToken());
                contentValues.put(DatabaseConstants.MRN, user.getMrn());
                contentValues.put(DatabaseConstants.AGE, user.getAge());
                contentValues.put(DatabaseConstants.ISTEEN, user.isTeen());
                contentValues.put(DatabaseConstants.GENDER, user.getGenderCode());
                contentValues.put(DatabaseConstants.ISTEEN_TOGGLE_ENABLED, user.isTeenToggleEnabled());
            } catch (Exception e) {
                PillpopperLog.say("Oops!, Exception while inserting users Data");
            }
        } else {
            PillpopperLog.say("Oops!, No Users object Found");
        }
        return contentValues;
    }

    private static ContentValues setContentValues_UserRemindersData(User user) {
        ContentValues contentValues = new ContentValues();
        if (null != user) {
            try {
                contentValues.put(DatabaseConstants.USER_ID, user.getUserId());
                if (null != user.getEnabled() && ("Y").equalsIgnoreCase(user.getEnabled())) {
                    contentValues.put(DatabaseConstants.REMINDERS_ENABLED, "Y");//default value
                } else {
                    contentValues.put(DatabaseConstants.REMINDERS_ENABLED, "N");//default value
                }
            } catch (Exception e) {
                PillpopperLog.say("Oops!, Exception while inserting users Data");
            }
        } else {
            PillpopperLog.say("Oops!, No Users object Found");
        }
        return contentValues;
    }

    public static ContentValues setContentValues_PillSchedule(String pillId, String pillTime) {
        ContentValues contentValues = null;
        if (null != pillId && !("null").equalsIgnoreCase(pillId) && !("").equalsIgnoreCase(pillId) && pillId.length() > 0) {
            try {
                contentValues = new ContentValues();
                contentValues.put(DatabaseConstants.PILL_ID, pillId);
                contentValues.put(DatabaseConstants.PILLTIME, pillTime);
            } catch (Exception e) {
                PillpopperLog.say("Exception while creating Pill Schedule ContentValue Object");
            }
        }
        return contentValues;
    }


    private static ContentValues setContentValues_PillSchedule(PillList pillList) {
        ContentValues contentValues = new ContentValues();

        if (null != pillList) {
            contentValues.put(DatabaseConstants.PILL_ID, pillList.getPillId());
            if (null != pillList.getSchedule() && pillList.getSchedule().length > 0) {
                for (int i = 0; i < pillList.getSchedule().length; i++) {
                    contentValues.put(DatabaseConstants.PILLTIME, pillList.getSchedule()[i]);
                }
            }
        } else {
            PillpopperLog.say("Oops!, No Schedules Found");
        }
        return contentValues;
    }


    private static ContentValues setContentValues_PillData(UserList userList) {
        ContentValues contentValues = new ContentValues();
        if (userList != null && userList.getPillList().length > 0) {
            PillList[] pill = userList.getPillList();
            for (PillList pillList : pill) {
                try {
                    contentValues.put(DatabaseConstants.PILL_ID, pillList.getPillId());
                    PillpopperLog.say("---inserting userid-- " + userList.getUserId());
                    contentValues.put(DatabaseConstants.PILL_USER_ID, userList.getUserId());
                    contentValues.put(DatabaseConstants.PILL_NAME, pillList.getName());
                    //contentValues.put(DatabaseConstants.SCHEDULE_TYPE, pillList.getType());
                    contentValues.put(DatabaseConstants.CREATED, pillList.getCreated());
                    contentValues.put(DatabaseConstants.DAY_PERIOD, pillList.getDayperiod());
                    contentValues.put(DatabaseConstants.DOSE, pillList.getDose());
                    contentValues.put(DatabaseConstants.NUM_PILLS, pillList.getNumpills());
                    contentValues.put(DatabaseConstants.INSTRUCTIONS, pillList.getInstructions());
                    contentValues.put(DatabaseConstants.HAS_UNDO, pillList.getHasUndo());
                    contentValues.put(DatabaseConstants.LAST_TAKEN, Util.convertDateLongToIso(pillList.getLastTaken()));
                    contentValues.put(DatabaseConstants.EFF_LAST_TAKEN, Util.convertDateLongToIso(pillList.getEff_last_taken()));
                    contentValues.put(DatabaseConstants.NEXT, pillList.getNext());
                    contentValues.put(DatabaseConstants.START, Util.convertDateLongToIso(pillList.getStart()));
                    contentValues.put(DatabaseConstants.END, Util.convertDateLongToIso(pillList.getEnd()));
                    contentValues.put(DatabaseConstants.TAKE_PILL_AFTER, Util.convertDateLongToIso(pillList.getTakePillAfter()));
                    contentValues.put(DatabaseConstants.SKIP_PILL_AFTER, Util.convertDateLongToIso(pillList.getSkipPillAfter()));
                    contentValues.put(DatabaseConstants.MAX_POSTPONE_TIME, Util.convertDateLongToIso(pillList.getMaxPostponeTime()));
                    contentValues.put(DatabaseConstants.SERVER_EDIT_TIME, pillList.getServerEditTime());
                    contentValues.put(DatabaseConstants.SERVER_EDIT_GUID, pillList.getServerEditGuid());
                    contentValues.put(DatabaseConstants.NOTIFY_AFTER, pillList.getNotify_after());

                    contentValues.put(DatabaseConstants.SCHEDULE_GUID, pillList.getScheduleGuid());

                } catch (Exception e) {
                    PillpopperLog.say("Oops Exception while inserting the data into Pill Table");
                }
            }
        } else {
            PillpopperLog.say("Pill object is null");
        }
        return contentValues;
    }

    private static ContentValues setContentValues_InsertPill(PillList pillList, String userId) {
        ContentValues contentValues = new ContentValues();
        if (null != pillList) {
            try {
                contentValues.put(DatabaseConstants.PILL_ID, pillList.getPillId());
                contentValues.put(DatabaseConstants.PILL_USER_ID, userId);
                contentValues.put(DatabaseConstants.PILL_NAME, pillList.getName());
                //contentValues.put(DatabaseConstants.SCHEDULE_TYPE, pillList.getType());
                contentValues.put(DatabaseConstants.CREATED, pillList.getCreated());
                contentValues.put(DatabaseConstants.DAY_PERIOD, pillList.getDayperiod());
                contentValues.put(DatabaseConstants.DOSE, pillList.getDose());
                contentValues.put(DatabaseConstants.NUM_PILLS, pillList.getNumpills());
                contentValues.put(DatabaseConstants.INSTRUCTIONS, pillList.getInstructions());
                contentValues.put(DatabaseConstants.HAS_UNDO, pillList.getHasUndo());
                contentValues.put(DatabaseConstants.LAST_TAKEN, Util.convertDateLongToIso(pillList.getLastTaken()));
                contentValues.put(DatabaseConstants.EFF_LAST_TAKEN, Util.convertDateLongToIso(pillList.getEff_last_taken()));
                contentValues.put(DatabaseConstants.NEXT, pillList.getNext());
                contentValues.put(DatabaseConstants.START, Util.convertDateLongToIso(pillList.getStart()));
                contentValues.put(DatabaseConstants.END, Util.convertDateLongToIso(pillList.getEnd()));
                contentValues.put(DatabaseConstants.TAKE_PILL_AFTER, Util.convertDateLongToIso(pillList.getTakePillAfter()));
                contentValues.put(DatabaseConstants.SKIP_PILL_AFTER, Util.convertDateLongToIso(pillList.getSkipPillAfter()));
                contentValues.put(DatabaseConstants.MAX_POSTPONE_TIME, Util.convertDateLongToIso(pillList.getMaxPostponeTime()));
                contentValues.put(DatabaseConstants.SERVER_EDIT_TIME, pillList.getServerEditTime());
                contentValues.put(DatabaseConstants.SERVER_EDIT_GUID, pillList.getServerEditGuid());
                contentValues.put(DatabaseConstants.INTERVAL, pillList.getInterval());
                contentValues.put(DatabaseConstants.NOTIFY_AFTER, pillList.getNotify_after());

                contentValues.put(DatabaseConstants.LAST_TAKEN_TZSECS, pillList.getLastTZsecs());
                contentValues.put(DatabaseConstants.NOTIFY_AFTER_TZSECS, pillList.getNotify_afterTZsecs());
                contentValues.put(DatabaseConstants.EFF_LAST_TAKEN_TZSECS, pillList.getEff_last_takenTZsecs());

                contentValues.put(DatabaseConstants.SCHEDULE_GUID, pillList.getScheduleGuid());

            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while inserting the data into Pill Table");
            }
        } else {
            PillpopperLog.say("Pill object is null");
        }
        return contentValues;
    }

    private static ContentValues setContentValues_Pill_PreferenceData(PillList _pill) {
        ContentValues contentValues = new ContentValues();
        if (_pill != null) {
            PillPreferences preferences = null;
            contentValues.put(DatabaseConstants.PILL_ID, _pill.getPillId());
            if (null != _pill.getPreferences()) {
                preferences = _pill.getPreferences();
                try {
                    contentValues.put(DatabaseConstants.ARCHIVED, preferences.getArchived());
                    contentValues.put(DatabaseConstants.CUSTOM_DESCRIPTION, preferences.getCustomDescription());
                    contentValues.put(DatabaseConstants.CUSTOM_DOSAGE_ID, preferences.getCustomDosageID());
                    contentValues.put(DatabaseConstants.DOCTOR_COUNT, preferences.getDoctorCount());
                    contentValues.put(DatabaseConstants.IMAGE_GUID, preferences.getImageGUID());
                    contentValues.put(DatabaseConstants.INVISIBLE, preferences.getInvisible());
                    contentValues.put(DatabaseConstants.LOG_MISSED_DOSES, preferences.getLogMissedDoses());
                    contentValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(preferences.getMissedDosesLastChecked()));
                    contentValues.put(DatabaseConstants.NO_PUSH, preferences.getNoPush());
                    contentValues.put(DatabaseConstants.NOTES, preferences.getNotes());
                    contentValues.put(DatabaseConstants.PERSON_ID, preferences.getPersonId());
                    contentValues.put(DatabaseConstants.PHARMACY_COUNT, preferences.getPharmacyCount());
                    contentValues.put(DatabaseConstants.PRESCRIPTION_NUM, preferences.getPrescriptionNum());
                    contentValues.put(DatabaseConstants.REFILL_ALERT_DOSES, preferences.getRefillAlertDoses());
                    contentValues.put(DatabaseConstants.REFILLS_REMAINING, preferences.getRefillsRemaining());
                    contentValues.put(DatabaseConstants.REMAINING_QUANTITY, preferences.getRefillQuantity());
                    contentValues.put(DatabaseConstants.SECONDARY_REMINDERS, preferences.getSecondaryReminders());
                    contentValues.put(DatabaseConstants.WEEKDAYS, preferences.getWeekdays());
                    contentValues.put(DatabaseConstants.LAST_MANAGED_ID_NOTIFIED, preferences.getLastManagedIdNotified());
                    contentValues.put(DatabaseConstants.LAST_MANAGED_ID_NEEDING_NOTIFY, preferences.getLastManagedIdNeedingNotify());
                    contentValues.put(DatabaseConstants.DOSAGE_TYPE, preferences.getDosageType());
                    contentValues.put(DatabaseConstants.MAX_NUM_DAILY_DOSES, preferences.getMaxNumDailyDoses());
                    contentValues.put(DatabaseConstants.DATABASE_NDC, preferences.getDatabaseNDC());
                    contentValues.put(DatabaseConstants.MANAGED_DROPPED, preferences.getManagedDropped());
                    contentValues.put(DatabaseConstants.MANAGED_MEDICATION_ID, preferences.getManagedMedicationId());
                    contentValues.put(DatabaseConstants.MANAGED_DESCRIPTION, preferences.getManagedDescription());
                    contentValues.put(DatabaseConstants.DATABASE_MED_FORM_TYPE, preferences.getDatabaseMedFormType());
                    contentValues.put(DatabaseConstants.DELETED, preferences.getDeleted());
                    contentValues.put(DatabaseConstants.LIMIT_TYPE, preferences.getLimitType());
                    contentValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED_TZSECS, preferences.getMissedDosesLastCheckedTZsecs());
                    contentValues.put(DatabaseConstants.SCHEDULEDATECHANGED_TZSECS, preferences.getScheduleDateChnagedTZsecs());
                    contentValues.put(DatabaseConstants.DEFAULT_IMAGE_CHOICE, preferences.getDefaultImageChoice());
                    contentValues.put(DatabaseConstants.DEFAULT_SERVICE_IMAGE_ID, preferences.getDefaultServiceImageID());
                    contentValues.put(DatabaseConstants.NEED_FDB_UPDATE, preferences.getNeedFDBUpdate());
                    contentValues.put(DatabaseConstants.SCHEDULED_FREQUENCY, preferences.getScheduleFrequency());
                    if (Util.isEmptyString(preferences.getScheduleChoice())) {
                        RunTimeData.getInstance().getPillIdList().add(_pill.getPillId());
                        if (null != _pill.getSchedule() && _pill.getSchedule().length > 0) {
                            contentValues.put(DatabaseConstants.SCHEDULE_CHOICE, AppConstants.SCHEDULE_CHOICE_SCHEDULED);
                        } else if (null != preferences.getMaxNumDailyDoses() && !"-1".equalsIgnoreCase(preferences.getMaxNumDailyDoses()) && !"0".equalsIgnoreCase(preferences.getMaxNumDailyDoses())) {
                            contentValues.put(DatabaseConstants.SCHEDULE_CHOICE, AppConstants.SCHEDULE_CHOICE_AS_NEEDED);
                        } else {
                            contentValues.put(DatabaseConstants.SCHEDULE_CHOICE, AppConstants.SCHEDULE_CHOICE_UNDEFINED);
                        }
                    } else {
                        contentValues.put(DatabaseConstants.SCHEDULE_CHOICE, preferences.getScheduleChoice());
                    }

                } catch (Exception e) {
                    PillpopperLog.say("Oops Exception while inserting the data into Pill Preferences Table");
                }
            }
        } else {
            PillpopperLog.say("Pill Preference object is null");
        }
        return contentValues;
    }

    private static ContentValues setContentValues_User_PreferenceData(Context context, UserList _userPrefrence) {
        ContentValues contentValues = new ContentValues();
        if (_userPrefrence != null) {
            try {
                contentValues.put(DatabaseConstants.USER_PREFERNECE_USER_ID, _userPrefrence.getUserId());
                contentValues.put(DatabaseConstants.BEDTIME_END, _userPrefrence.getBedTimeEnd());
                contentValues.put(DatabaseConstants.BEDTIME_START, _userPrefrence.getBedTimeStart());
                contentValues.put(DatabaseConstants.USER_PREFERENCE_CREATED, _userPrefrence.getCreated());
                contentValues.put(DatabaseConstants.ARCHIEVED_DRUG_DISPLAYED, _userPrefrence.getPreferences().getArchivedDrugsDisplayed());
                contentValues.put(DatabaseConstants.CUSTOM_DRUG_DOSAGE_NAME_COUNT, _userPrefrence.getPreferences().getCustomDrugDosageNamesCount());
                contentValues.put(DatabaseConstants.DOSE_HISTORY_DAYS, _userPrefrence.getPreferences().getDoseHistoryDays());
                contentValues.put(DatabaseConstants.DRUG_SORT_ORDER, _userPrefrence.getPreferences().getDrugSortOrder());
                contentValues.put(DatabaseConstants.DST_OFFSET_SECS, _userPrefrence.getPreferences().getDstOffset_secs());
                contentValues.put(DatabaseConstants.EARLY_DOSE_WARNING, _userPrefrence.getPreferences().getEarlyDoseWarning());
                contentValues.put(DatabaseConstants.LATE_DOSE_PERIOD_SECS, _userPrefrence.getPreferences().getLateDosePeriodSecs());
                contentValues.put(DatabaseConstants.PERSON_NAMES_COUNT, _userPrefrence.getPreferences().getPersonNamesCount());
                contentValues.put(DatabaseConstants.POSTPONES_DISPLAYED, _userPrefrence.getPreferences().getPostponesDisplayed());
                contentValues.put(DatabaseConstants.PRIVACY_MODE, _userPrefrence.getPreferences().getPrivacyMode());
                contentValues.put(DatabaseConstants.QUICKVIEW_OPTINED, _userPrefrence.getPreferences().getQuickviewOptIned());
                contentValues.put(DatabaseConstants.REMINDER_SOUND_FILENAME, Util.getNotificationUri(context, _userPrefrence.getPreferences().getReminderSoundFilename()));
                contentValues.put(DatabaseConstants.ANDROID_REMINDER_SOUND_FILENAME, _userPrefrence.getPreferences().getAndroidReminderSoundFilename());
                contentValues.put(DatabaseConstants.SECONDARY_REMINDER_PERIOD_SECS, getRepeatReminderValidValue(_userPrefrence.getPreferences().getSecondaryReminderPeriodSecs()));
                contentValues.put(DatabaseConstants.TZ_NAME, _userPrefrence.getPreferences().getTz_name());
                contentValues.put(DatabaseConstants.TZ_SECS, _userPrefrence.getPreferences().getTz_secs());
                contentValues.put(DatabaseConstants.SUBSCRIPTION_TYPE, _userPrefrence.getSubscriptionType());
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while inserting the data into User Preferences Table");
            }
        } else {
            PillpopperLog.say("User Preference object is null");
        }
        return contentValues;
    }

    private static String getRepeatReminderValidValue(String secondaryReminderPeriodSecs) {
        long repeatReminderValue = 600; // default value
        try {
            switch (Util.handleParseInt(secondaryReminderPeriodSecs)) {
                case -1:
                case 300:
                case 600:
                case 900:
                case 1800:
                    repeatReminderValue = Util.handleParseInt(secondaryReminderPeriodSecs);
                    break;
                default:
                    repeatReminderValue = 600;
                    break;
            }
        } catch (Exception ex) {
            repeatReminderValue = 600;
            LoggerUtils.exception(ex.getMessage());
        }
        return String.valueOf(repeatReminderValue);
    }


    private static ContentValues setContentValues_Pill_HistoryData(GetHistoryEvents historyEventsObject) {
        ContentValues contentValues = new ContentValues();
        if (historyEventsObject != null) {
            try {
                contentValues.put(DatabaseConstants.PILL_ID, historyEventsObject.getPillId());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_GUID, historyEventsObject.getGuid());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_OPID, historyEventsObject.getOpId());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, historyEventsObject.getCreationDate());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_DESCRIPTION, historyEventsObject.getEventDescription());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_OPERATION, historyEventsObject.getOperation());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_OPERATION_DATA, historyEventsObject.getOperationData());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_PERSON_ID, historyEventsObject.getPersonId());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(historyEventsObject.getScheduleDate()));
                contentValues.put(DatabaseConstants.HISTORY_EVENT_EDIT_TIME, historyEventsObject.getEditTime());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_DELETED, historyEventsObject.getDeleted());
                contentValues.put(DatabaseConstants.PILL_NAME, historyEventsObject.getPillName());
                contentValues.put(DatabaseConstants.CUSTOM_DESCRIPTION, historyEventsObject.getPreferences().getCustomDescription());
                contentValues.put(DatabaseConstants.MANAGED_DESCRIPTION, historyEventsObject.getPreferences().getManagedDescription());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_CREATE_USERID, historyEventsObject.getCreateUserId());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_UPDATE_USERID, historyEventsObject.getUpdateUserId());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_TZ_SEC, historyEventsObject.getTz_secs());
                contentValues.put(DatabaseConstants.HISTORY_EVENT_TZ_NAME, historyEventsObject.getTz_name());
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while inserting the data into HistoryEvents Table");
            }
        } else {
            PillpopperLog.say("HistoryEvents object is null");
        }
        return contentValues;
    }

    private static ContentValues setContentValues_History_PreferenceData(GetHistoryEvents historyEventsObject) {
        ContentValues contentValues = new ContentValues();
        if (historyEventsObject != null) {
            try {
                contentValues.put(DatabaseConstants.HISTORY_EVENT_GUID, historyEventsObject.getGuid());
                if (null != historyEventsObject.getPreferences().getActionDate()) {
                    contentValues.put(DatabaseConstants.HISTORY_ACTION_DATE, historyEventsObject.getPreferences().getActionDate());
                }
                if (null != historyEventsObject.getPreferences().getRecordDate()) {
                    contentValues.put(DatabaseConstants.HISTORY_RECORD_DATE, historyEventsObject.getPreferences().getRecordDate());
                }

                contentValues.put(DatabaseConstants.HISTORY_PREF_SCH_DATE, historyEventsObject.getScheduleDate());
                contentValues.put(DatabaseConstants.HISTORY_PREF_SCH_FREQUENCY, historyEventsObject.getPreferences().getScheduleFrequency());
                contentValues.put(DatabaseConstants.HISTORY_PREF_SCH_DAY_PERIOD, historyEventsObject.getPreferences().getDayperiod());
                contentValues.put(DatabaseConstants.HISTORY_PREF_WEEKDAYS, historyEventsObject.getPreferences().getWeekdays());
                contentValues.put(DatabaseConstants.HISTORY_PREF_START_DATE, Util.convertDateLongToIso(historyEventsObject.getPreferences().getStart()));
                contentValues.put(DatabaseConstants.HISTORY_PREF_END_DATE, Util.convertDateLongToIso(historyEventsObject.getPreferences().getEnd()));
                contentValues.put(DatabaseConstants.HISTORY_PREF_SCH_TYPE, historyEventsObject.getPreferences().getScheduleChoice());
                contentValues.put(DatabaseConstants.IS_POSTPONED_EVENT_ACTIVE, historyEventsObject.getPreferences().isPostponedEventActive());
                contentValues.put(DatabaseConstants.FINAL_POSTPONED_DATE_TIME, historyEventsObject.getPreferences().getFinalPostponedDateTime());
                contentValues.put(DatabaseConstants.HISTORY_PREF_SCH_GUID, historyEventsObject.getPreferences().getScheduleGuid());
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while inserting the data into History Preference Table");
            }
        } else {
            PillpopperLog.say("HistoryEvents object is null");
        }
        return contentValues;
    }


    public List<Drug> getDrugsListByUserId(String userId) {
        List<Drug> druglist = new ArrayList<>();
        Cursor cursor = null;
        Drug drug;

        try {

            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_MEDICATIONS_FOR_SCHEDULE, new String[]{userId});
            if (cursor != null && cursor.moveToFirst()) {
                /*String archived = cursor.getString(cursor.getColumnIndex(DatabaseConstants.ARCHIVED));
                String managedDropped = cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DROPPED));
                String invisible = cursor.getString(cursor.getColumnIndex(DatabaseConstants.INVISIBLE));*/
                do {
                    if (!isDroppedMedication(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)))) {
                        drug = new Drug();
                        drug.setId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                        drug.setName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
                        drug.setCreated(Util.convertStringtoPillpopperTime(cursor.getString(cursor.getColumnIndex(DatabaseConstants.CREATED))));
                        drug.setNotes(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES)));
                        drug.setPreferecences(getDrugPreferences(cursor));
                        Schedule schedule = new Schedule();
                        schedule.setTimeList(new TimeList(getSchdulesByPillId(drug.getGuid())));
                        schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START)))));
                        schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.END)))));
                       // schedule.setSchedType(getScheduleType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE))));
                        schedule.setDayPeriod(Util.parseJSONNonnegativeLong((cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)))));
                        schedule.setDays((cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS))));
                        drug.setSchedule(schedule);
                        drug.setHeader(false);
                        drug.setUserID(userId);
                        druglist.add(drug);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("--Schedule Query--" + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("--Closing the cursor--" + e.getMessage());
                }
            }
        }
        return druglist;
    }


    public void updateSchedule(PillpopperActivity _thisActivity, HashMap<String, String> data, List<String> drugIds, List<String> pillTimeList) {

        Cursor cursor = null;
        // ArrayList<String> tempPillList = new ArrayList<String>();
        try {
            for (int i = 0; i < drugIds.size(); i++) {

                // delete empty history entries if available
                deleteEmptyHistoryEntriesByPillID(drugIds.get(i));

                ContentValues values = new ContentValues();
                PillpopperLog.say("Adding end date : " + Util.convertDateLongToIso(data.get("end_date")));
                values.put(DatabaseConstants.START, Util.convertDateLongToIso(data.get("start_date")));
                values.put(DatabaseConstants.END, Util.convertDateLongToIso(data.get("end_date")));
                values.put(DatabaseConstants.DAY_PERIOD, data.get("type"));
                //values.put(DatabaseConstants.SCHEDULE_TYPE, "scheduled");
                databaseHandler.update(DatabaseConstants.PILL_TABLE, values, DatabaseConstants.PILL_ID + "=? and " + DatabaseConstants.USER_ID + "=?", new String[]{drugIds.get(i), data.get("user_id")});
                ContentValues pillPreferenceValues = new ContentValues();
                if (("W").equalsIgnoreCase(data.get("scheduledFrequency"))) {
                    pillPreferenceValues.put(DatabaseConstants.WEEKDAYS, data.get("on_days"));
                } else if (("D").equalsIgnoreCase(data.get("scheduledFrequency"))) {
                    pillPreferenceValues.putNull(DatabaseConstants.WEEKDAYS);
                } else if (("M").equalsIgnoreCase(data.get("scheduledFrequency"))) {
                    pillPreferenceValues.put(DatabaseConstants.WEEKDAYS, data.get("on_days"));
                }

                String missedDosesLastChecked = getMissedDoseLastCheckedValue(drugIds.get(i));

                //pillPreferenceValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso( String.valueOf(PillpopperTime.now().getGmtSeconds())));

                if (!Util.isEmptyString(missedDosesLastChecked) && !"-1".equalsIgnoreCase(missedDosesLastChecked)) {
                    PillpopperLog.say("MissedDoseCheck --- updateSchedule method : " + missedDosesLastChecked);
                    pillPreferenceValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(missedDosesLastChecked));
                } else {
                    PillpopperLog.say("MissedDoseCheck --- updateSchedule method : " + Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
                    pillPreferenceValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
                }
                String dosageType = getDosageTypeByPillID(drugIds.get(i));
                pillPreferenceValues.put(DatabaseConstants.DOSAGE_TYPE, !Util.isEmptyString(dosageType) ? dosageType : PillpopperConstants.DOSAGE_TYPE_CUSTOM);
                pillPreferenceValues.put(DatabaseConstants.SCHEDULE_CHOICE, AppConstants.SCHEDULE_CHOICE_SCHEDULED);

                pillPreferenceValues.put(DatabaseConstants.SCHEDULED_FREQUENCY, data.get("scheduledFrequency"));
                databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, pillPreferenceValues, DatabaseConstants.PILL_ID + "=?", new String[]{drugIds.get(i)});
                cursor = databaseHandler.executeRawQuery("select PILLTIME, PILLID from PILL_SCHEDULE where PILLID=?", new String[]{drugIds.get(i)});
                //tempPillList.clear();
                databaseHandler.delete(DatabaseConstants.PILL_SCHEDULE_TABLE, "PILLID=?", new String[]{drugIds.get(i)});
                for (int j = 0; j < pillTimeList.size(); j++) {
                    ContentValues values1 = new ContentValues();
                    values1.put(DatabaseConstants.PILL_ID, drugIds.get(i));
                    values1.put(DatabaseConstants.PILLTIME, Util.convertHHMMtoTimeFormat(pillTimeList.get(j)));
                    databaseHandler.insert(DatabaseConstants.PILL_SCHEDULE_TABLE, values1);
                }
            }
        } catch (Exception e) {
            PillpopperLog.exception("Exception while updating the schedule " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
    }

    public int updateSchedule(BulkSchedule data, List<String> drugIds, List<String> pillTimeList) {

        int updated = 0;
        try {
            for (int i = 0; i < drugIds.size(); i++) {

                updateLastMissedCheck(drugIds.get(i), "-1");
                // delete empty history entries if available
                deleteEmptyHistoryEntriesByPillID(drugIds.get(i));

                ContentValues values = new ContentValues();
                PillpopperLog.say("Adding end date : " + Util.convertDateLongToIso(data.getScheduledEndDate()));
                values.put(DatabaseConstants.START, Util.convertDateLongToIso(data.getScheduledStartDate()));
                values.put(DatabaseConstants.END, Util.convertDateLongToIso(data.getScheduledEndDate()));
                values.put(DatabaseConstants.SCHEDULE_GUID, data.getScheduleGUID());
                if (!Util.isEmptyString(data.getDayPeriod())) {
                    values.put(DatabaseConstants.DAY_PERIOD, data.getDayPeriod());
                }
                //values.put(DatabaseConstants.SCHEDULE_TYPE, "scheduled");
                updated = databaseHandler.update(DatabaseConstants.PILL_TABLE, values, DatabaseConstants.PILL_ID + "=? and " + DatabaseConstants.USER_ID + "=?", new String[]{drugIds.get(i), data.getUserId()});
                ContentValues pillPreferenceValues = new ContentValues();
                if (("W").equalsIgnoreCase(data.getScheduledFrequency())) {
                    pillPreferenceValues.put(DatabaseConstants.WEEKDAYS, data.getDaysSelectedForWeekly());
                } else if (("D").equalsIgnoreCase(data.getScheduledFrequency())) {
                    pillPreferenceValues.putNull(DatabaseConstants.WEEKDAYS);
                } else if (("M").equalsIgnoreCase(data.getScheduledFrequency())) {
                    pillPreferenceValues.putNull(DatabaseConstants.WEEKDAYS);
                }
                pillPreferenceValues.put(DatabaseConstants.SCHEDULED_FREQUENCY, data.getScheduledFrequency());

                String missedDosesLastChecked = getMissedDoseLastCheckedValue(drugIds.get(i));

                if (!Util.isEmptyString(missedDosesLastChecked) && !"-1".equalsIgnoreCase(missedDosesLastChecked)) {
                    PillpopperLog.say("MissedDoseCheck --- updateSchedule method : " + missedDosesLastChecked);
                    pillPreferenceValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(missedDosesLastChecked));
                } else {
                    PillpopperLog.say("MissedDoseCheck --- updateSchedule method : " + Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
                    pillPreferenceValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
                    updateLastMissedCheck(drugIds.get(i), Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
                }
                String dosageType = getDosageTypeByPillID(drugIds.get(i));
                pillPreferenceValues.put(DatabaseConstants.DOSAGE_TYPE, !Util.isEmptyString(dosageType) ? dosageType : PillpopperConstants.DOSAGE_TYPE_CUSTOM);
                pillPreferenceValues.put(DatabaseConstants.SCHEDULE_CHOICE, AppConstants.SCHEDULE_CHOICE_SCHEDULED);
                databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, pillPreferenceValues, DatabaseConstants.PILL_ID + "=?", new String[]{drugIds.get(i)});
                databaseHandler.delete(DatabaseConstants.PILL_SCHEDULE_TABLE, "PILLID=?", new String[]{drugIds.get(i)});
                for (int j = 0; j < pillTimeList.size(); j++) {
                    ContentValues values1 = new ContentValues();
                    values1.put(DatabaseConstants.PILL_ID, drugIds.get(i));
                    values1.put(DatabaseConstants.PILLTIME, Util.convertHHMMtoTimeFormat(pillTimeList.get(j)));
                    databaseHandler.insert(DatabaseConstants.PILL_SCHEDULE_TABLE, values1);
                }

                // reset the Notify After Value for the drug, as this is new and updated schedule
                // the upcoming schedule will be set as notify_after during the editPill API call.
                updateNotifyAfterValue(drugIds.get(i), new PillpopperTime(0L).getGmtSeconds());

            }
        } catch (Exception e) {
            PillpopperLog.exception("Exception while updating the schedule " + e.getMessage());
        }
        return updated;
    }

    public String getDosageTypeByPillID(String pillId) {
        Cursor cursor;
        cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_DOSAGE_BY_PILLID_QUERY, new String[]{pillId});
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE));
        }
        return PillpopperConstants.DOSAGE_TYPE_CUSTOM;// If its empty, will keep it as custom.
    }

    /**
     * This method will give the drugs based on the userID
     *
     * @param _thisActivity context of the class
     * @param userId        user id
     * @return list of drugs
     */

    public LinkedHashMap<String, List<Drug>> getDrugListByUser(PillpopperActivity _thisActivity, String userId) {
        LinkedHashMap<String, List<Drug>> list = new LinkedHashMap<>();
        List<Drug> druglist = new ArrayList<>();
        Cursor cursor = null;
        Drug drug;
        try {

            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_DRUG_LIST_BY_USER_ID_QUERY_WITHOUT_SCHEDULE_NEW, new String[]{userId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    drug = new Drug();
                    if (!isDroppedMedication(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID)))) {
                        drug.setId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID)));
                        drug.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_NAME)));
                        drug.setUserID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.USER_ID)));
                        drug.setPreferecences(getDrugPreferencesForDrugList(cursor));
                        drug.setMemberFirstName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.FIRST_NAME)));
                        Schedule schedule = new Schedule();
                        schedule.setTimeList(new TimeList(getSchdulesByPillId(drug.getGuid())));
                        schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START)))));
                        schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.END)))));
                       // schedule.setSchedType(getScheduleType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE))));
                        schedule.setDayPeriod(Util.parseJSONNonnegativeLong((cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)))));
                        schedule.setDays((cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS))));
                        drug.setSchedule(schedule);
                        drug.setHeader(false);
                        drug.setSchedule(schedule);
                        drug.setScheduleGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_GUID)));
                        if (getScheduleCountByPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID))) >= 1) {
                            drug.setScheduleCount(1);
                        } else {
                            drug.setScheduleCount(0);
                        }
                        druglist.add(drug);
                        list.put(userId, druglist);
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("getDrugListByUser --- >" + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return list;
    }


    public String getMissedDoseLastCheckedValue(String pillID) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_MISSED_DOSE_LAST_CHECKED_QUERY, new String[]{pillID});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    return cursor.getString(cursor.getColumnIndex(DatabaseConstants.MISSED_DOSES_LAST_CHECKED));
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return "";
    }


    /**
     * Returns true if the medication is Discontinued medication.
     *
     * @param pillID
     * @return
     */
    private boolean isDroppedMedication(String pillID) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.CHECK_MED_FOR_MANAGE_DROPPED_QUERY, new String[]{pillID});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    if ("managed".equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE)))) {
                        return "1".equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DROPPED)));
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * This method will give the schedules count based on the pillid.
     *
     * @param pillId id of pill
     * @return count of schedules
     */
    public int getScheduleCountByPillId(String pillId) {

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_SCHEDULE_COUNT_BY_PILL_ID_QUERY, new String[]{pillId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int rowCount = cursor.getInt(0);
                    return rowCount;
                } while (cursor.moveToNext());
            }

        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return 0;
    }

    public JSONObject getDrugPreferencesForDrugList(Cursor cursor) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("archived", cursor.getString(cursor.getColumnIndex(DatabaseConstants.ARCHIVED)));
            jsonObject.put("customDescription", cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION)));
            jsonObject.put("imageGUID", cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID)));
            jsonObject.put("invisible", cursor.getString(cursor.getColumnIndex(DatabaseConstants.INVISIBLE)));
            jsonObject.put("weekdays", cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS)));
            jsonObject.put("lastManagedIdNotified", cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_MANAGED_ID_NOTIFIED)));
            jsonObject.put("lastManagedIdNeedingNotify", cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_MANAGED_ID_NEEDING_NOTIFY)));
            jsonObject.put("limitType", cursor.getString(cursor.getColumnIndex(DatabaseConstants.LIMIT_TYPE)));
            jsonObject.put("managedDropped", cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DROPPED)));
            jsonObject.put("managedDescription", cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION)));
            jsonObject.put("deleted", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DELETED)));
            jsonObject.put("dosageType", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE)));
            jsonObject.put("notes", cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES)));
            jsonObject.put("maxNumDailyDoses", cursor.getString(cursor.getColumnIndex(DatabaseConstants.MAX_NUM_DAILY_DOSES)));
            jsonObject.put(DatabaseConstants.SCHEDULE_CHOICE, cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_CHOICE)));
        } catch (JSONException e) {
            PillpopperLog.say(e.getMessage());
        }
        return jsonObject;
    }

    public List<Drug> getDrugListForOverDue(Context _thisActivity) {
        List<Drug> list = new ArrayList<>();
        {
            Cursor cursor = null;
            Drug drug;
            try {

                cursor = databaseHandler.executeQuery(DatabaseConstants.GET_DRUG_LIST_QUERY);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        drug = new Drug();
                        drug.setId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                        drug.setName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
                        drug.setCreated(Util.convertStringtoPillpopperTime(cursor.getString(cursor.getColumnIndex(DatabaseConstants.CREATED))));
                        drug.setNotes(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES)));
                        drug.setLastTaken(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_TAKEN)))));
                        drug.set_effLastTaken(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.EFF_LAST_TAKEN)))));
                        drug.set_notifyAfter(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER)))));
                        // Preparing the drugPreferences object
                        drug.setPreferecences(getDrugPreferences(cursor));

                        Schedule schedule = new Schedule();
                        schedule.setTimeList(new TimeList(getSchdulesByPillId(drug.getGuid())));
                        schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START)))));
                        schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.END)))));
                        //schedule.setSchedType(getScheduleType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE))));
                        schedule.setDayPeriod(Util.parseJSONNonnegativeLong((cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)))));
                        schedule.setDays((cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS))));
                        drug.setSchedule(schedule);
                        drug.setUserID(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                        drug.setIsOverdue(cursor.getString(cursor.getColumnIndex(DatabaseConstants.OVERDUE)));
                        drug.setIsRemindersEnabled(cursor.getString(cursor.getColumnIndex(DatabaseConstants.REMINDERS_ENABLED)));
                        drug.setScheduledFrequency(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY)));
                        drug.setScheduleGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_GUID)));
                        list.add(drug);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while fetching the pill list" + e.getMessage());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        PillpopperLog.say(e.getMessage());
                    }
                }
            }
        }
        return list;
    }

    public List<Drug> getDrugListForDue(Context context) {
        List<Drug> list = new ArrayList<>();
        {
            Cursor cursor = null;
            Drug drug;
            try {
                cursor = databaseHandler.executeQuery(DatabaseConstants.GET_DRUG_LIST_QUERY);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        drug = new Drug();
                        drug.setId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                        drug.setName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
                        drug.setCreated(Util.convertStringtoPillpopperTime(cursor.getString(cursor.getColumnIndex(DatabaseConstants.CREATED))));
                        drug.setNotes(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES)));
                        drug.setLastTaken(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_TAKEN)))));
                        drug.set_effLastTaken(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.EFF_LAST_TAKEN)))));
                        // Preparing the drugPreferences object
                        drug.setPreferecences(getDrugPreferences(cursor));
                        Schedule schedule = new Schedule();
                        schedule.setTimeList(new TimeList(getSchdulesByPillId(drug.getGuid())));
                        schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START)))));
                        schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.END)))));
                        //schedule.setSchedType(getScheduleType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE))));
                        schedule.setDayPeriod(Util.parseJSONNonnegativeLong((cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)))));
                        schedule.setDays((cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS))));
                        drug.setSchedule(schedule);
                        drug.setUserID(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                        drug.setIsOverdue(cursor.getString(cursor.getColumnIndex(DatabaseConstants.OVERDUE)));
                        drug.setScheduledFrequency(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY)));
                        drug.setScheduleGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_GUID)));
                        list.add(drug);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while fetching the pill list" + e.getMessage());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        PillpopperLog.say(e.getMessage());
                    }
                }
            }
        }
        return list;
    }

    public JSONObject getDrugPreferences(Cursor cursor) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("archived", cursor.getString(cursor.getColumnIndex(DatabaseConstants.ARCHIVED)));
            jsonObject.put("customDescription", cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION)));
            jsonObject.put("customDosageID", cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DOSAGE_ID)));
            jsonObject.put("doctorCount", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOCTOR_COUNT)));
            jsonObject.put("imageGUID", cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID)));
            jsonObject.put("invisible", cursor.getString(cursor.getColumnIndex(DatabaseConstants.INVISIBLE)));
            jsonObject.put("logMissedDoses", cursor.getString(cursor.getColumnIndex(DatabaseConstants.LOG_MISSED_DOSES)));
            jsonObject.put("missedDosesLastChecked", Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.MISSED_DOSES_LAST_CHECKED))));
            jsonObject.put("noPush", cursor.getString(cursor.getColumnIndex(DatabaseConstants.NO_PUSH)));
            jsonObject.put("notes", cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES)));
            jsonObject.put("personId", cursor.getString(cursor.getColumnIndex(DatabaseConstants.PERSON_ID)));
            jsonObject.put("pharmacyCount", cursor.getString(cursor.getColumnIndex(DatabaseConstants.PHARMACY_COUNT)));
            jsonObject.put("prescriptionNum", cursor.getString(cursor.getColumnIndex(DatabaseConstants.PRESCRIPTION_NUM)));
            jsonObject.put("refillAlertDoses", cursor.getString(cursor.getColumnIndex(DatabaseConstants.REFILL_ALERT_DOSES)));
            jsonObject.put("refillsRemaining", cursor.getString(cursor.getColumnIndex(DatabaseConstants.REFILLS_REMAINING)));
            jsonObject.put("remainingQuantity", cursor.getString(cursor.getColumnIndex(DatabaseConstants.REMAINING_QUANTITY)));
            jsonObject.put("secondaryReminders", cursor.getString(cursor.getColumnIndex(DatabaseConstants.SECONDARY_REMINDERS)));
            jsonObject.put("weekdays", cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS)));
            jsonObject.put("lastManagedIdNotified", cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_MANAGED_ID_NOTIFIED)));
            jsonObject.put("lastManagedIdNeedingNotify", cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_MANAGED_ID_NEEDING_NOTIFY)));
            jsonObject.put("maxNumDailyDoses", cursor.getString(cursor.getColumnIndex(DatabaseConstants.MAX_NUM_DAILY_DOSES)));
            jsonObject.put("limitType", cursor.getString(cursor.getColumnIndex(DatabaseConstants.LIMIT_TYPE)));
            jsonObject.put("databaseNDC", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DATABASE_NDC)));
            jsonObject.put("managedDropped", cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DROPPED)));
            jsonObject.put("managedMedicationId", cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_MEDICATION_ID)));
            jsonObject.put("managedDescription", cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION)));
            jsonObject.put("databaseMedForm", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DATABASE_MED_FORM_TYPE)));
            jsonObject.put("deleted", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DELETED)));
            jsonObject.put("dosageType", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE)));
            jsonObject.put("defaultImageChoice", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DEFAULT_IMAGE_CHOICE)));
            jsonObject.put("defaultServiceImageID", cursor.getString(cursor.getColumnIndex(DatabaseConstants.DEFAULT_SERVICE_IMAGE_ID)));
            jsonObject.put("needFDBUpdate", cursor.getString(cursor.getColumnIndex(DatabaseConstants.NEED_FDB_UPDATE)));
            jsonObject.put("scheduleFrequency", cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY)));
            jsonObject.put(DatabaseConstants.SCHEDULE_CHOICE, cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_CHOICE)));

            PillpopperLog.say("Reminder Drug info setting jesonObject: " + jsonObject.toString());
        } catch (JSONException e) {
            LoggerUtils.exception("Exception in getDrugPreferences" + e.getMessage());
        }
        return jsonObject;
    }

    /**
     * It will give us the future or upcoming dates in history calendar
     */
    public List<HistoryEvent> getHistoryCalendarFutureDays(PillpopperDay focusDay, String userId) {
        final PillpopperTime focusDayAtMidnight;
        focusDayAtMidnight = new PillpopperTime(focusDay.atLocalTime(new HourMinute(0, 0)).getGmtSeconds());
        final String databaseQueryFocusDayAtMidnight = Util.convertDateLongToIso(Long.toString(focusDayAtMidnight.getGmtSeconds()));    //focus day comparison string for database query
        final String pillStartDate = databaseQueryFocusDayAtMidnight;
        final String pillEndDate = databaseQueryFocusDayAtMidnight;
        final List<HistoryEvent> futureHistoryEvents = new ArrayList<>();

        Cursor cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ALL_DRUGS_SCHEDULE_MAIN_SCREEN_QUERY,
                new String[]{"%" + focusDay.getDayOfWeek().getDayNumber() + "%", pillStartDate, pillEndDate});
        try {
            if (cursor.moveToFirst()) {
                ScheduleMainDrug drug;
                do {
                    if (!checkDayPeriodValidForSchedule(cursor, focusDay)) {
                        continue;
                    }
                    drug = getScheduleMainDrugFromCursor(cursor);
                    HistoryEvent historyEvent = new HistoryEvent();
                    historyEvent.setPillID(drug.getPillId());
                    historyEvent.setPillName(drug.getPillName());
                    historyEvent.setHeaderTime(convertToHistoryHeaderTime(drug, focusDay));
                    historyEvent.setHistoryEventGuid(Util.getRandomGuid());
                    historyEvent.setOperationStatus(PillpopperConstants.ACTION_UPCOMING_PILL_HISTORY);
                    long headerTime = Long.parseLong(historyEvent.getHeaderTime());
                    if (headerTime > PillpopperTime.now().getGmtSeconds() && drug.getUser().getUserId().equalsIgnoreCase(userId)) {
                        futureHistoryEvents.add(historyEvent);
                    }

                    GetHistoryPreferences preferences= new GetHistoryPreferences();
                    preferences.setScheduleFrequency(drug.getScheduledFrequency());
                    preferences.setDayperiod(drug.getDayPeriod());
                    preferences.setStart(Util.convertDateLongToIso(String.valueOf(drug.getStart().getGmtSeconds())));
                    preferences.setEnd(Util.convertDateLongToIso(String.valueOf(drug.getEnd().getGmtSeconds())));
                    preferences.setWeekdays((null != drug.getDrugPreference()
                            && null != drug.getDrugPreference().getWeekdays()) ? drug.getDrugPreference().getWeekdays() : "");
                    historyEvent.setPreferences(preferences);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
            }
        }
        return futureHistoryEvents;
    }

    /**
     * @param pillTime
     * @param focusTime
     * @return converting string
     */
    private String convertToHistoryHeaderTime(ScheduleMainDrug pillTime, PillpopperDay focusTime) {
        long value = new ScheduleMainTimeHeader(pillTime.getPillTime(), focusTime).getHeaderPillpopperTime().getGmtSeconds();
        return Long.toString(value);
    }

    public void getFutureMedications(final PillpopperDay focusDay) {

        final PillpopperTime focusDayAtMidnight;
        final List<ScheduleListItemDataWrapper> scheduleDrugListWrapperFuture = new ArrayList<>();
        focusDayAtMidnight = new PillpopperTime(focusDay.atLocalTime(new HourMinute(0, 0)).getGmtSeconds());

        final String databaseQueryFocusDayAtMidnight = Util.convertDateLongToIso(Long.toString(focusDayAtMidnight.getGmtSeconds()));    //focus day comparison string for database query
        PillpopperDay nextDay = focusDay.addDays(1);
        final String pillStartDate = databaseQueryFocusDayAtMidnight;    //Start date comparison value for database query
        final String pillEndDate = databaseQueryFocusDayAtMidnight;
        Thread getScheduleListPillsInFutureThread = new Thread(() -> {

            Cursor cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ALL_DRUGS_SCHEDULE_MAIN_SCREEN_QUERY,
                    new String[]{"%" + focusDay.getDayOfWeek().getDayNumber() + "%", pillStartDate, pillEndDate});
            List<ScheduleMainDrug> scheduleDrugListFuture = new ArrayList<>();


            try {
                if (cursor.moveToFirst()) {
                    ScheduleMainDrug drug = null;
                    ScheduleMainDrug referenceDrug = null;
                    do {
                        if (!checkDayPeriodValidForSchedule(cursor, focusDay)) {
                            continue;
                        }

                        if (drug != null) {
                            drug = getScheduleMainDrugFromCursor(cursor);
                            if ((!referenceDrug.getPillTime().equals(drug.getPillTime())
                                    || !referenceDrug.getUser().getUserId().equals(drug.getUser().getUserId()))) {
                                ScheduleListItemDataWrapper scheduleListItemDataWrapper = new ScheduleListItemDataWrapper();
                                scheduleListItemDataWrapper.setUserFirstName(scheduleDrugListFuture.get(0).getUser().getFirstName());
                                scheduleListItemDataWrapper.setDrugList(scheduleDrugListFuture);
                                scheduleListItemDataWrapper.setProxyAvailable(getAllUserIds().size() > 1 ? View.VISIBLE : View.GONE);
                                scheduleListItemDataWrapper.setPillTime(scheduleDrugListFuture.get(0).getPillTime(), focusDayAtMidnight.getLocalDay());
                                scheduleListItemDataWrapper.setUserId(scheduleDrugListFuture.get(0).getUser().getUserId());
                                scheduleListItemDataWrapper.setUserType(scheduleDrugListFuture.get(0).getUser().getUserType());

                                ArrayList<String> drugIdsForTakenAction = new ArrayList<>();
                                for (ScheduleMainDrug drugForTaken : scheduleDrugListFuture) {
                                    drugIdsForTakenAction.add(drugForTaken.getPillId());
                                }
                                scheduleListItemDataWrapper.setPillIdsForTakenAction(drugIdsForTakenAction);
                                if (!drugIdsForTakenAction.isEmpty()) {
                                    scheduleListItemDataWrapper.setPossibleNextActiveListItem(true);
                                }
                                if (scheduleListItemDataWrapper.getScheduleMainTimeHeader().getHeaderPillpopperTime().after(PillpopperTime.now())) {
                                    scheduleDrugListWrapperFuture.add(scheduleListItemDataWrapper);
                                }
                                scheduleDrugListFuture = new ArrayList<>();
                            }
                        } else {
                            drug = getScheduleMainDrugFromCursor(cursor);
                        }

                        referenceDrug = getScheduleMainDrugFromCursor(cursor);
                        scheduleDrugListFuture.add(drug);

                    } while (cursor.moveToNext());

                    if (!scheduleDrugListFuture.isEmpty()) {
                        ScheduleListItemDataWrapper scheduleListItemDataWrapper = new ScheduleListItemDataWrapper();
                        scheduleListItemDataWrapper.setUserFirstName(scheduleDrugListFuture.get(0).getUser().getFirstName());
                        scheduleListItemDataWrapper.setDrugList(scheduleDrugListFuture);
                        scheduleListItemDataWrapper.setProxyAvailable(getAllUserIds().size() > 1 ? View.VISIBLE : View.GONE);
                        scheduleListItemDataWrapper.setPillTime(scheduleDrugListFuture.get(0).getPillTime(), focusDayAtMidnight.getLocalDay());
                        scheduleListItemDataWrapper.setUserId(scheduleDrugListFuture.get(0).getUser().getUserId());
                        scheduleListItemDataWrapper.setUserType(scheduleDrugListFuture.get(0).getUser().getUserType());

                        ArrayList<String> drugIdsForTakenAction = new ArrayList<>();
                        for (ScheduleMainDrug drugForTaken : scheduleDrugListFuture) {
                            drugIdsForTakenAction.add(drugForTaken.getPillId());
                        }
                        scheduleListItemDataWrapper.setPillIdsForTakenAction(drugIdsForTakenAction);

                        if (!drugIdsForTakenAction.isEmpty()) {
                            scheduleListItemDataWrapper.setPossibleNextActiveListItem(true);
                        }

                        if (scheduleListItemDataWrapper.getScheduleMainTimeHeader().getHeaderPillpopperTime().after(PillpopperTime.now())) {
                            scheduleDrugListWrapperFuture.add(scheduleListItemDataWrapper);
                        }
                    }

                }
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }

        });

        getScheduleListPillsInFutureThread.start();


    }

    public List<ScheduleListItemDataWrapper> getMedicationScheduleForDay(final PillpopperActivity _thisActivty, final PillpopperDay focusDay) {
        List<ScheduleListItemDataWrapper> listForScheduleScreenAdapter = new ArrayList<>(); //List that is returned to the Schedule List screen adapter
        final PillpopperTime focusDayAtMidnight;
        final PillpopperTime focusDayAtMidnightForHistory;
        final PillpopperTime nextDayAtMidnight;
        final LinkedHashMap<Long, LinkedHashMap<String, List<ScheduleMainDrug>>> historyHashMap = new LinkedHashMap<>();

        focusDayAtMidnight = new PillpopperTime(focusDay.atLocalTime(new HourMinute(0, 0)).getGmtSeconds());
        focusDayAtMidnightForHistory = new PillpopperTime(focusDay.atLocalTime(new HourMinute(0, 0)).getGmtSeconds());
        final String databaseQueryFocusDayAtMidnight = Util.convertDateLongToIso(Long.toString(focusDayAtMidnight.getGmtSeconds()));    //focus day comparison string for database query
        PillpopperDay nextDay = focusDay.addDays(1);
        nextDayAtMidnight = new PillpopperTime(nextDay.atLocalTime(new HourMinute(0, 0)).getGmtSeconds());
        final String pillStartDate = databaseQueryFocusDayAtMidnight;    //Start date comparison value for database query
        final String pillEndDate = databaseQueryFocusDayAtMidnight;       //End date comparison value for database query
        final List<ScheduleListItemDataWrapper> scheduleDrugListWrapperHistory = new ArrayList<>();
        final List<ScheduleListItemDataWrapper> scheduleDrugListWrapperFuture = new ArrayList<>();

        final List<ScheduleListItemDataWrapper> historyEventsInFuture = new ArrayList<>();
        Thread getScheduleListPillsInHistoryThread = new Thread(() -> {
            if (null != databaseHandler) {
                Cursor historyCursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_HISTORY_EVENTS_SCHEDULE_SCREEN_QUERY, new String[]{Util.convertDateLongToIso(Long.toString(focusDayAtMidnightForHistory.getGmtSeconds())), Util.convertDateLongToIso(Long.toString(focusDayAtMidnightForHistory.getGmtSeconds() + 24 * 60 * 60))});
                List<ScheduleMainDrug> scheduleDrugListHistory = new ArrayList<>();
                ScheduleMainDrug drug;
                ScheduleMainDrug referenceDrug = null;
                try {
                    if (null != historyCursor && historyCursor.moveToFirst()) {
                        do {
                            drug = getScheduleMainDrugFromHistoryCursor(historyCursor);
                            if (referenceDrug == null) {
                                //Happens on the first iteration.
                            } else {
                                if (!referenceDrug.getPillTime().equals(drug.getPillTime())
                                        || !referenceDrug.getUser().getUserId().equals(drug.getUser().getUserId())) {
                                    ScheduleListItemDataWrapper scheduleListItemDataWrapper = new ScheduleListItemDataWrapper();
                                    scheduleListItemDataWrapper.setUserFirstName(scheduleDrugListHistory.get(0).getUser().getFirstName());
                                    Collections.sort(scheduleDrugListHistory);
                                    scheduleListItemDataWrapper.setProxyAvailable(getAllUserIds().size() > 1 ? View.VISIBLE : View.GONE);
                                    scheduleListItemDataWrapper.setDrugList(scheduleDrugListHistory);
                                    scheduleListItemDataWrapper.setPillTime(scheduleDrugListHistory.get(0).getPillTime(), focusDayAtMidnight.getLocalDay());
                                    scheduleListItemDataWrapper.setUserId(scheduleDrugListHistory.get(0).getUser().getUserId());
                                    scheduleListItemDataWrapper.setUserType(scheduleDrugListHistory.get(0).getUser().getUserType());
                                    scheduleDrugListWrapperHistory.add(scheduleListItemDataWrapper);

                                    if (scheduleListItemDataWrapper.getScheduleMainTimeHeader().getHeaderPillpopperTime().after(PillpopperTime.now())) {
                                        historyEventsInFuture.add(scheduleListItemDataWrapper);
                                    }
                                    scheduleDrugListHistory = new ArrayList<>();
                                }
                            }
                            PillpopperTime notifyAfterTime = drug.getNotifyAfter();
                            if (notifyAfterTime != null
                                    && drug.getHistoryEventAction().equalsIgnoreCase(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                                if (notifyAfterTime.getGmtSeconds() > 0
                                        && notifyAfterTime.after(PillpopperTime.now())
                                        && notifyAfterTime.before(nextDayAtMidnight)) {
                                    ScheduleMainDrug postponedDrug = new ScheduleMainDrug();
                                    postponedDrug.setPillName(drug.getPillName());
                                    postponedDrug.setPillId(drug.getPillId());
                                    postponedDrug.getUser().setUserId(drug.getUser().getUserId());
                                    postponedDrug.getUser().setFirstName(drug.getUser().getFirstName());

                                    postponedDrug.setDose(getDrugDoseValue(historyCursor));

                                    postponedDrug.setPillTime(String.format("%02d%02d", notifyAfterTime.getLocalHourMinute().getHour(), notifyAfterTime.getLocalHourMinute().getMinute()));

                                    postponedDrug.getUser().setUserType(historyCursor.getString(historyCursor.getColumnIndex(DatabaseConstants.USER_TYPE)));

                                    if (historyHashMap.containsKey(notifyAfterTime.getGmtSeconds())) {
                                        LinkedHashMap<String, List<ScheduleMainDrug>> postponedDrugsByUser = historyHashMap.get(notifyAfterTime.getGmtSeconds());
                                        if (postponedDrugsByUser.containsKey(historyCursor.getString(historyCursor.getColumnIndex(DatabaseConstants.USER_ID)))) {
                                            boolean addToHashmap = true;
                                            List<ScheduleMainDrug> postponedDrugListForUser = postponedDrugsByUser.get(historyCursor.getString(historyCursor.getColumnIndex(DatabaseConstants.USER_ID)));
                                            for (ScheduleMainDrug drugToCheckDuplicate : postponedDrugListForUser) {
                                                if (drugToCheckDuplicate.getPillId().equals(postponedDrug.getPillId())) {
                                                    addToHashmap = false;
                                                    break;
                                                }
                                            }
                                            if (addToHashmap) {
                                                postponedDrugListForUser.add(postponedDrug);
                                            }

                                        } else {
                                            List<ScheduleMainDrug> postponeDrugListForUser = new ArrayList<>();
                                            postponeDrugListForUser.add(postponedDrug);
                                            postponedDrugsByUser.put(historyCursor.getString(historyCursor.getColumnIndex(DatabaseConstants.USER_ID)), postponeDrugListForUser);
                                        }
                                    } else {
                                        LinkedHashMap<String, List<ScheduleMainDrug>> postponedDrugsByUser = new LinkedHashMap<>();
                                        List<ScheduleMainDrug> postponedDrugListForUser = new ArrayList<>();
                                        postponedDrugListForUser.add(postponedDrug);
                                        postponedDrugsByUser.put(historyCursor.getString(historyCursor.getColumnIndex(DatabaseConstants.USER_ID)), postponedDrugListForUser);
                                        historyHashMap.put(notifyAfterTime.getGmtSeconds(), postponedDrugsByUser);
                                    }

                                }
                            }

                            referenceDrug = drug;
                            scheduleDrugListHistory.add(drug);

                        } while (historyCursor.moveToNext());

                        if (!scheduleDrugListHistory.isEmpty()) {
                            ScheduleListItemDataWrapper scheduleListItemDataWrapper = new ScheduleListItemDataWrapper();
                            scheduleListItemDataWrapper.setUserFirstName(scheduleDrugListHistory.get(0).getUser().getFirstName());
                            Collections.sort(scheduleDrugListHistory);
                            scheduleListItemDataWrapper.setDrugList(scheduleDrugListHistory);
                            scheduleListItemDataWrapper.setProxyAvailable(getAllUserIds().size() > 1 ? View.VISIBLE : View.GONE);
                            scheduleListItemDataWrapper.setPillTime(scheduleDrugListHistory.get(0).getPillTime(), focusDayAtMidnight.getLocalDay());
                            scheduleListItemDataWrapper.setUserId(scheduleDrugListHistory.get(0).getUser().getUserId());
                            scheduleListItemDataWrapper.setUserType(scheduleDrugListHistory.get(0).getUser().getUserType());
                            scheduleDrugListWrapperHistory.add(scheduleListItemDataWrapper);

                            if (scheduleListItemDataWrapper.getScheduleMainTimeHeader().getHeaderPillpopperTime().after(PillpopperTime.now())) {
                                historyEventsInFuture.add(scheduleListItemDataWrapper);
                            }
                        }
                    }
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                } finally {
                    try {
                        if (historyCursor != null
                                && !historyCursor.isClosed()) {
                            historyCursor.close();
                        }
                    } catch (Exception e) {
                        PillpopperLog.exception(e.getMessage());
                    }
                }
            }
        });

        Thread getScheduleListPillsInFutureThread = new Thread(() -> {

            Cursor cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ALL_DRUGS_SCHEDULE_MAIN_SCREEN_QUERY,
                    new String[]{"%" + focusDay.getDayOfWeek().getDayNumber() + "%", pillStartDate, pillEndDate});
            List<ScheduleMainDrug> scheduleDrugListFuture = new ArrayList<>();

            try {
                if (cursor.moveToFirst()) {
                    ScheduleMainDrug drug = null;
                    ScheduleMainDrug referenceDrug = null;
                    do {
                        if (!checkDayPeriodValidForSchedule(cursor, focusDay)) {
                            continue;
                        }

                        if (drug != null) {
                            drug = getScheduleMainDrugFromCursor(cursor);
                            if ((!referenceDrug.getPillTime().equals(drug.getPillTime())
                                    || !referenceDrug.getUser().getUserId().equals(drug.getUser().getUserId()))) {
                                ScheduleListItemDataWrapper scheduleListItemDataWrapper = new ScheduleListItemDataWrapper();
                                scheduleListItemDataWrapper.setUserFirstName(scheduleDrugListFuture.get(0).getUser().getFirstName());
                                scheduleListItemDataWrapper.setDrugList(scheduleDrugListFuture);
                                scheduleListItemDataWrapper.setProxyAvailable(getAllUserIds().size() > 1 ? View.VISIBLE : View.GONE);
                                scheduleListItemDataWrapper.setPillTime(scheduleDrugListFuture.get(0).getPillTime(), focusDayAtMidnight.getLocalDay());
                                scheduleListItemDataWrapper.setUserId(scheduleDrugListFuture.get(0).getUser().getUserId());
                                scheduleListItemDataWrapper.setUserType(scheduleDrugListFuture.get(0).getUser().getUserType());

                                ArrayList<String> drugIdsForTakenAction = new ArrayList<>();
                                for (ScheduleMainDrug drugForTaken : scheduleDrugListFuture) {
                                    drugIdsForTakenAction.add(drugForTaken.getPillId());
                                }
                                scheduleListItemDataWrapper.setPillIdsForTakenAction(drugIdsForTakenAction);

                                if (!drugIdsForTakenAction.isEmpty()) {
                                    scheduleListItemDataWrapper.setPossibleNextActiveListItem(true);
                                }

                                if (scheduleListItemDataWrapper.getScheduleMainTimeHeader().getHeaderPillpopperTime().after(PillpopperTime.now())) {
                                    scheduleDrugListWrapperFuture.add(scheduleListItemDataWrapper);
                                }

                                scheduleDrugListFuture = new ArrayList<>();
                            }
                        } else {
                            drug = getScheduleMainDrugFromCursor(cursor);
                        }

                        referenceDrug = getScheduleMainDrugFromCursor(cursor);
                        scheduleDrugListFuture.add(drug);

                    } while (cursor.moveToNext());

                    if (!scheduleDrugListFuture.isEmpty()) {
                        ScheduleListItemDataWrapper scheduleListItemDataWrapper = new ScheduleListItemDataWrapper();
                        scheduleListItemDataWrapper.setUserFirstName(scheduleDrugListFuture.get(0).getUser().getFirstName());
                        scheduleListItemDataWrapper.setDrugList(scheduleDrugListFuture);
                        scheduleListItemDataWrapper.setProxyAvailable(getAllUserIds().size() > 1 ? View.VISIBLE : View.GONE);
                        scheduleListItemDataWrapper.setPillTime(scheduleDrugListFuture.get(0).getPillTime(), focusDayAtMidnight.getLocalDay());
                        scheduleListItemDataWrapper.setUserId(scheduleDrugListFuture.get(0).getUser().getUserId());
                        scheduleListItemDataWrapper.setUserType(scheduleDrugListFuture.get(0).getUser().getUserType());

                        ArrayList<String> drugIdsForTakenAction = new ArrayList<>();
                        for (ScheduleMainDrug drugForTaken : scheduleDrugListFuture) {
                            drugIdsForTakenAction.add(drugForTaken.getPillId());
                        }
                        scheduleListItemDataWrapper.setPillIdsForTakenAction(drugIdsForTakenAction);

                        if (!drugIdsForTakenAction.isEmpty()) {
                            scheduleListItemDataWrapper.setPossibleNextActiveListItem(true);
                        }

                        if (scheduleListItemDataWrapper.getScheduleMainTimeHeader().getHeaderPillpopperTime().after(PillpopperTime.now())) {
                            scheduleDrugListWrapperFuture.add(scheduleListItemDataWrapper);
                        }
                    }

                }
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
            } finally {
                try {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }

        });

        getScheduleListPillsInHistoryThread.start();
        getScheduleListPillsInFutureThread.start();
        try {
            getScheduleListPillsInHistoryThread.join();
            getScheduleListPillsInFutureThread.join();
        } catch (InterruptedException e) {
            PillpopperLog.say("InterruptedException in DatabaseUtils -- getScheduleList -- scheduleHIstoryThread -- " + e.getMessage());
        }

        Collections.sort(scheduleDrugListWrapperHistory);
        Collections.sort(scheduleDrugListWrapperFuture);
        Collections.sort(historyEventsInFuture);

        PillpopperTime referenceTimeForNextActiveItem = null;

        if (focusDay.equals(PillpopperDay.today())) {

            //Removing all events from the future drug list which are scheduled before the current device time.
            for (int index = 0; index < scheduleDrugListWrapperFuture.size(); index++) {
                if (scheduleDrugListWrapperFuture.get(index).getScheduleMainTimeHeader().getHeaderPillpopperTime().before(PillpopperTime.now())
                        || scheduleDrugListWrapperFuture.get(index).getScheduleMainTimeHeader().getHeaderPillpopperTime().equals(PillpopperTime.now())) {
                    scheduleDrugListWrapperFuture.remove(index);
                }
            }


            /* For V3.7.1 This snippet we no longer needed, as we moved to ISO format
             and This is removing the entries for upcoming schedules when we edit the schedules after taken*/

            // Added this snippet because of the DE9874.
            // When ever the notify_after value is tomorrow date also its considering today.
            // So we are removing the indexes of the drugs whose notify_after value is falling tomorrow date.
            /*for (Iterator<ScheduleListItemDataWrapper> iterator = scheduleDrugListWrapperFuture.iterator(); iterator.hasNext(); ) {
                ScheduleListItemDataWrapper scheduleListItemDatawrapper = iterator.next();
                for (ScheduleMainDrug scheduleMainDrug : scheduleListItemDatawrapper.getDrugList()) {
                    if (null != scheduleMainDrug.getNotifyAfter() && scheduleMainDrug.getNotifyAfter().getLocalDay().after(focusDay)) {
                        try {
                            iterator.remove();
                        } catch (Exception exception) {
                            PillpopperLog.say("Exception While removing the drug");
                        }
                    }
                }
            }*/

            for (int index = 0; index < scheduleDrugListWrapperFuture.size(); index++) {
                List<ScheduleMainDrug> drugListToMergeWithHistory = scheduleDrugListWrapperFuture.get(index).getDrugList();

                if (historyHashMap.containsKey(scheduleDrugListWrapperFuture.get(index).getScheduleMainTimeHeader().getHeaderPillpopperTime().getGmtSeconds())) {
                    LinkedHashMap<String, List<ScheduleMainDrug>> postponeHashMap = historyHashMap.get(scheduleDrugListWrapperFuture.get(index).getScheduleMainTimeHeader().getHeaderPillpopperTime().getGmtSeconds());

                    if (postponeHashMap.containsKey(scheduleDrugListWrapperFuture.get(index).getUserId())) {
                        List<ScheduleMainDrug> postponeDrugListFromHistory = postponeHashMap.get(scheduleDrugListWrapperFuture.get(index).getUserId());

                        for (ScheduleMainDrug drugFromHistory : postponeDrugListFromHistory) {
                            boolean addToSchedule = true;
                            for (ScheduleMainDrug drugFromSchedule : drugListToMergeWithHistory) {
                                if (drugFromHistory.getPillId().equals(drugFromSchedule.getPillId())) {
                                    addToSchedule = false;
                                    break;
                                }
                            }

                            if (addToSchedule) {
                                drugListToMergeWithHistory.add(drugFromHistory);
                                scheduleDrugListWrapperFuture.get(index).getPillIdsForTakenAction().add(drugFromHistory.getPillId());
                                scheduleDrugListWrapperFuture.get(index).setPossibleNextActiveListItem(true);
                            }
                        }
                        postponeHashMap.remove(scheduleDrugListWrapperFuture.get(index).getUserId());
                    }
                }

                Collections.sort(drugListToMergeWithHistory);
            }

            for (Map.Entry<Long, LinkedHashMap<String, List<ScheduleMainDrug>>> historyEntry : historyHashMap.entrySet()) {
                for (Map.Entry<String, List<ScheduleMainDrug>> entry : historyEntry.getValue().entrySet()) {
                    ScheduleListItemDataWrapper newScheduleItemForPostpone = new ScheduleListItemDataWrapper();
                    newScheduleItemForPostpone.setPillTime(entry.getValue().get(0).getPillTime(), PillpopperDay.today());
                    newScheduleItemForPostpone.setUserId(entry.getValue().get(0).getUser().getUserId());
                    newScheduleItemForPostpone.setUserFirstName(entry.getValue().get(0).getUser().getFirstName());
                    newScheduleItemForPostpone.setProxyAvailable(getAllUserIds().size() > 1 ? View.VISIBLE : View.GONE);
                    newScheduleItemForPostpone.setUserType(entry.getValue().get(0).getUser().getUserType());
                    ArrayList<String> drugIdsForTakenAction = new ArrayList<>();
                    for (ScheduleMainDrug drugForTaken : entry.getValue()) {
                        drugIdsForTakenAction.add(drugForTaken.getPillId());
                    }
                    newScheduleItemForPostpone.setPillIdsForTakenAction(drugIdsForTakenAction);
                    Collections.sort(entry.getValue());
                    newScheduleItemForPostpone.setDrugList(entry.getValue());
                    newScheduleItemForPostpone.setPossibleNextActiveListItem(true);
                    scheduleDrugListWrapperFuture.add(newScheduleItemForPostpone);
                }
            }

            Collections.sort(scheduleDrugListWrapperFuture);

            int futureIndex;    //Variable to go through the future drug list
            int historyIndex;   //Variable to go through the history drug list

            for (futureIndex = 0; futureIndex < scheduleDrugListWrapperFuture.size(); futureIndex++) {
                String futureUserId = scheduleDrugListWrapperFuture.get(futureIndex).getUserId();
                String futurePillTime = scheduleDrugListWrapperFuture.get(futureIndex).getPillTime();

                boolean historyEventAvailable = false;

                for (historyIndex = scheduleDrugListWrapperHistory.size() - 1; historyIndex >= 0; historyIndex--) {
                    String pastUserId = scheduleDrugListWrapperHistory.get(historyIndex).getUserId();
                    String pastPillTime = scheduleDrugListWrapperHistory.get(historyIndex).getPillTime();

                    if (pastUserId.equals(futureUserId)
                            && pastPillTime.equals(futurePillTime)) {
                        historyEventAvailable = true;
                        break;
                    }
                }

                if (historyEventAvailable) {
                    List<ScheduleMainDrug> historyDrugList = scheduleDrugListWrapperHistory.get(historyIndex).getDrugList();
                    List<ScheduleMainDrug> futureDrugList = scheduleDrugListWrapperFuture.get(futureIndex).getDrugList();

                    int futureDrugListIndex;
                    int historyDrugListIndex;

                    for (historyDrugListIndex = 0; historyDrugListIndex < historyDrugList.size(); historyDrugListIndex++) {
                        for (futureDrugListIndex = 0; futureDrugListIndex < futureDrugList.size(); futureDrugListIndex++) {
                            if (historyDrugList.get(historyDrugListIndex).getPillId().equals(futureDrugList.get(futureDrugListIndex).getPillId())) {
                                futureDrugList.remove(futureDrugListIndex);

                                List<String> takenActionPillIds = scheduleDrugListWrapperFuture.get(futureIndex).getPillIdsForTakenAction();
                                for (int index = 0; index < takenActionPillIds.size(); index++) {
                                    if (takenActionPillIds.get(index).equals(historyDrugList.get(historyDrugListIndex).getPillId())) {
                                        takenActionPillIds.remove(index);
                                    }
                                }

                                scheduleDrugListWrapperFuture.get(futureIndex).setPossibleNextActiveListItem(!takenActionPillIds.isEmpty());

                            }
                        }
                    }

                    futureDrugList.addAll(historyDrugList);
                    Collections.sort(futureDrugList);

                    scheduleDrugListWrapperHistory.remove(historyIndex);

                } else {
                    ArrayList<String> drugListForTakenAction = scheduleDrugListWrapperFuture.get(futureIndex).getPillIdsForTakenAction();
                    List<ScheduleMainDrug> futureDrugList = scheduleDrugListWrapperFuture.get(futureIndex).getDrugList();

                    for (int futureDrugListIndex = 0; futureDrugListIndex < futureDrugList.size(); futureDrugListIndex++) {
                        boolean pillIdExists = false;
                        for (int index = 0; index < drugListForTakenAction.size(); index++) {
                            if (futureDrugList.get(futureDrugListIndex).getPillId().equals(drugListForTakenAction.get(index))) {
                                pillIdExists = true;
                                break;
                            }
                        }

                        if (!pillIdExists) {
                            drugListForTakenAction.add(futureDrugList.get(futureDrugListIndex).getPillId());
                        }
                    }

                    if (!drugListForTakenAction.isEmpty()) {
                        scheduleDrugListWrapperFuture.get(futureIndex).setPossibleNextActiveListItem(true);
                    }
                }
            }


            List<ScheduleListItemDataWrapper> threeDotsScheduleItemList = new ArrayList<>();

            for (int index = 0; index < scheduleDrugListWrapperFuture.size(); index++) {
                if (scheduleDrugListWrapperFuture.get(index).isPossibleNextActiveListItem()) {
                    if (referenceTimeForNextActiveItem == null) {
                        referenceTimeForNextActiveItem = scheduleDrugListWrapperFuture.get(index).getScheduleMainTimeHeader().getHeaderPillpopperTime();
                        scheduleDrugListWrapperFuture.get(index).setShowThreeDotAction(true);
                        RunTimeData.getInstance().setScheduleRecyclerViewScrollPosition(scheduleDrugListWrapperHistory.size() + index);
                        threeDotsScheduleItemList.add(scheduleDrugListWrapperFuture.get(index));
                    } else {
                        if (referenceTimeForNextActiveItem.equals(scheduleDrugListWrapperFuture.get(index).getScheduleMainTimeHeader().getHeaderPillpopperTime())) {
                            scheduleDrugListWrapperFuture.get(index).setShowThreeDotAction(true);
                            threeDotsScheduleItemList.add(scheduleDrugListWrapperFuture.get(index));
                        }
                    }
                }
            }

            listForScheduleScreenAdapter.addAll(removePostponedHistoryEntries(scheduleDrugListWrapperHistory));
            listForScheduleScreenAdapter.addAll(scheduleDrugListWrapperFuture);

            Collections.sort(listForScheduleScreenAdapter);

            if (!threeDotsScheduleItemList.isEmpty()) {
                for (int index = 0; index < listForScheduleScreenAdapter.size(); index++) {
                    if (threeDotsScheduleItemList.get(0).getPillTime().equals(listForScheduleScreenAdapter.get(index).getPillTime())) {
                        RunTimeData.getInstance().setScheduleRecyclerViewScrollPosition(index);
                        break;
                    }
                }
            }

        } else {
            listForScheduleScreenAdapter.addAll(scheduleDrugListWrapperFuture);
            RunTimeData.getInstance().setScheduleRecyclerViewScrollPosition(0);
        }

        return listForScheduleScreenAdapter;

    }


    private List<ScheduleListItemDataWrapper> removePostponedHistoryEntries(List<ScheduleListItemDataWrapper> scheduleDrugListWrapperHistory) {
        List<ScheduleListItemDataWrapper> filteredList = new ArrayList<>();
        for (ScheduleListItemDataWrapper item : scheduleDrugListWrapperHistory) {
            List<ScheduleMainDrug> listCopy = new ArrayList<>(item.getDrugList());
            for (int i = 0; i < item.getDrugList().size(); i++) {
                ScheduleMainDrug drug = item.getDrugList().get(i);
                if (PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY.equalsIgnoreCase(drug.getHistoryEventAction())) {
                    listCopy.remove(drug);
                }
            }
            item.setDrugList(listCopy);
            if (listCopy.size() > 0) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }


    /**
     * Schedule Screen Utility Methods:
     */

    /**
     * Determines whether the pill should be added to the database table.
     * Performs checks which have not been carried out in the database query
     * Checks already perform by the database query:
     * 1. archived = 0
     * 2. AND deleted = 0
     * 3. AND invisible = 0
     * 4. AND (weekdays is null OR weekdays = '' OR weekdays like (for passing the focusDay.getDayNumber) -- EDIT Complete check for weekly drugs.
     * 5. AND DAYPERIOD is not null AND is not ''
     * 6. AND (PillTime is not null or '')
     * 7. AND scheduledType is 'scheduled'
     * 8. AND Start <= FocusDay at midnight
     * 9. AND End = -1 OR End >= FocusDayatMidnight
     * 10. AND User Enabled
     * <p>
     * Results are sorted by the following priority (1 being the highest priority)
     * 1. PILLTIME
     * 2. USERTYPE - primary (higher) / proxy (lower)
     * 3. FIRSTNAME
     * 4. PILLNAME
     */

    public ScheduleMainDrug getScheduleMainDrugFromHistoryCursor(Cursor cursor) {
        ScheduleMainDrug drug = new ScheduleMainDrug();
        drug.setPillName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
        drug.setPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
        drug.setImageGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID)));
        drug.getUser().setUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
        drug.getUser().setFirstName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME)));
        drug.setHistoryEventAction(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_OPERATION)));

        if (cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER) == -1) {
            drug.setNotifyAfter(new PillpopperTime(-1));
        } else {
            drug.setNotifyAfter(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER)))));
        }

        if (cursor.getColumnIndex(DatabaseConstants.USER_TYPE) == -1) {
            drug.getUser().setUserType(null);
        } else {
            drug.getUser().setUserType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_TYPE)));
        }

        drug.setDose(getDrugDoseValue(cursor));

        // Solution 1
        String scheduleDate = Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE)));

        PillpopperTime scheduleDatePillpopperTime = Util.convertStringtoPillpopperTime(scheduleDate);
        drug.setPillTime(String.format("%02d%02d", scheduleDatePillpopperTime.getLocalHourMinute().getHour(), scheduleDatePillpopperTime.getLocalHourMinute().getMinute()));

        return drug;
    }

    private String getDrugDoseValue(Cursor cursor) {
        if (cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE) != -1) {
            if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE)) != null) {
                if (("managed").equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE)))) {
                    if (cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION) == -1) {
                        return null;
                    } else {
                        return cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION));
                    }
                } else {
                    if (cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION) == -1) {
                        return null;
                    } else {
                        return cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION));
                    }

                }
            } else {
                if (cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION) == -1) {
                    return null;
                } else {
                    return cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION));
                }
            }
        } else {
            return null;
        }
    }

    private ScheduleMainDrug getScheduleMainDrugFromCursor(Cursor cursor) {
        ScheduleMainDrug drug = new ScheduleMainDrug();
        if (cursor.getColumnIndex(DatabaseConstants.PILL_ID) == -1) {
            drug.setPillId(null);
        } else {
            drug.setPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.PILL_NAME) == -1) {
            drug.setPillName("");
        } else {
            drug.setPillName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
        }

       /* if (cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE) == -1) {
            drug.setScheduleType(null);
        } else {
            drug.setScheduleType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE)));
        } */

        if (cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD) == -1) {
            drug.setDayPeriod(null);
        } else {
            drug.setDayPeriod(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)));
        }

        drug.setDose(getDrugDoseValue(cursor));

        if (cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY) == -1) {
            drug.setScheduledFrequency(null);
        } else {
            drug.setScheduledFrequency(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.LAST_TAKEN) == -1) {
            drug.setLast((String) null);
        } else {
            drug.setLast(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_TAKEN))));
        }

        if (cursor.getColumnIndex(DatabaseConstants.EFF_LAST_TAKEN) == -1) {
            drug.setEffectiveLastTaken((String) null);
        } else {
            drug.setEffectiveLastTaken(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.EFF_LAST_TAKEN))));
        }

        if (cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER) == -1) {
            drug.setNotifyAfter((PillpopperTime) null);
        } else {
            drug.setNotifyAfter(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER)))));
        }

        if (cursor.getColumnIndex(DatabaseConstants.START) == -1) {
            drug.setStart((String) null);
        } else {
            drug.setStart(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START))));
        }

        if (cursor.getColumnIndex(DatabaseConstants.END) == -1) {
            drug.setEnd((String) null);
        } else {
            drug.setEnd(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.END))));
        }

        drug.setImageGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID)));


        if (cursor.getColumnIndex(DatabaseConstants.PILLTIME) == -1) {
            drug.setPillTime(null);
        } else {
            if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILLTIME)) != null &&
                    !cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILLTIME)).contains("-1")) {
                try {
                    int hours = Util.handleParseInt(Util.convertTimeFormatToHHMM(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILLTIME)))) / 100;
                    int minutes = Util.handleParseInt(Util.convertTimeFormatToHHMM(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILLTIME)))) % 100;
                    HourMinute localHourMinute = new HourMinute(hours, minutes);
                    StringBuilder pillTimeStringBuilder = new StringBuilder(String.format("%02d", localHourMinute.getHour()));
                    pillTimeStringBuilder.append(String.format("%02d", localHourMinute.getMinute()));
                    drug.setPillTime(pillTimeStringBuilder.toString());
                } catch (Exception ex) {
                    drug.setPillTime(null);
                    PillpopperLog.exception("DBUtils - getScheduleMainDrugFromCursor -" + ex.getMessage());
                }
            } else {
                drug.setPillTime(null);
            }
        }


        if (cursor.getColumnIndex(DatabaseConstants.ARCHIVED) == -1) {
            drug.getDrugPreference().setArchived(null);
        } else {
            drug.getDrugPreference().setArchived(cursor.getString(cursor.getColumnIndex(DatabaseConstants.ARCHIVED)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.INVISIBLE) == -1) {
            drug.getDrugPreference().setInvisible(null);
        } else {
            drug.getDrugPreference().setInvisible(cursor.getString(cursor.getColumnIndex(DatabaseConstants.INVISIBLE)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.WEEKDAYS) == -1) {
            drug.getDrugPreference().setWeekdays(null);
        } else {
            drug.getDrugPreference().setWeekdays(cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS)));
        }


        if (cursor.getColumnIndex(DatabaseConstants.DATABASE_MED_FORM_TYPE) == -1) {
            drug.getDrugPreference().setDatabaseMedFormType(null);
        } else {
            drug.getDrugPreference().setDatabaseMedFormType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DATABASE_MED_FORM_TYPE)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.DELETED) == -1) {
            drug.getDrugPreference().setDeleted(null);
        } else {
            drug.getDrugPreference().setDeleted(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DELETED)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE) == -1) {
            drug.getDrugPreference().setDosageType(null);
        } else {
            drug.getDrugPreference().setDosageType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.USER_ID) == -1) {
            drug.getUser().setUserId(null);
        } else {
            drug.getUser().setUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.USER_TYPE) == -1) {
            drug.getUser().setUserType(null);
        } else {
            drug.getUser().setUserType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_TYPE)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.ENABLED) == -1) {
            drug.getUser().setEnabled(null);
        } else {
            drug.getUser().setEnabled(cursor.getString(cursor.getColumnIndex(DatabaseConstants.ENABLED)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.FIRST_NAME) == -1) {
            drug.getUser().setFirstName(null);
        } else {
            drug.getUser().setFirstName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME)));
        }

        if (cursor.getColumnIndex(DatabaseConstants.EARLY_DOSE_WARNING) == -1) {
            drug.getUserPreference().setPreventEarlyDosesWarning(null);
        } else {
            drug.getUserPreference().setPreventEarlyDosesWarning(cursor.getString(cursor.getColumnIndex(DatabaseConstants.EARLY_DOSE_WARNING)));
        }
        return drug;
    }

    private boolean checkDayPeriodValidForSchedule(Cursor cursor, PillpopperDay focusDay) {
        String scheduledFrequency = null;
        if (cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY) != -1) {
            scheduledFrequency = cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY));
        }

        int dayPeriod = -1;
        if (cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD) != -1) {
            dayPeriod = Util.handleParseInt(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)));

        }

        PillpopperTime start = null;
        if (cursor.getColumnIndex(DatabaseConstants.START) != -1) {
            start = Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START))));
        }

        if (scheduledFrequency != null && null != start) {
            if (("D").equalsIgnoreCase(scheduledFrequency)) {
                return focusDay.daysAfter(start.getLocalDay()) % dayPeriod == 0;
            } else if (("W").equalsIgnoreCase(scheduledFrequency)) {
                if (dayPeriod == 7) {
                    return true;
                } else {
                    return focusDay.daysAfter(start.getLocalDay()) % dayPeriod == 0;
                }
            } else if (("M").equalsIgnoreCase(scheduledFrequency)) {
                Calendar cal = Calendar.getInstance();
                cal.set(focusDay.getYear(), focusDay.getMonth(), focusDay.getDay());
                if (start.getLocalDay().getDay() <= 28 && focusDay.getDay() == start.getLocalDay().getDay() && cal.getTime().getTime() > start.getGmtSeconds()) {
                    return true;
                } else if (start.getLocalDay().getDay() == 29 || start.getLocalDay().getDay() == 30) {
                    if (focusDay.getMonth() == 1) {
                        return focusDay.getDay() == cal.getActualMaximum(Calendar.DAY_OF_MONTH) && cal.getTime().getTime() > start.getGmtSeconds();
                    } else {
                        return focusDay.getDay() == start.getLocalDay().getDay() && cal.getTime().getTime() > start.getGmtSeconds();
                    }
                } else if (start.getLocalDay().getDay() == 31) {
                    return focusDay.getDay() == cal.getActualMaximum(Calendar.DAY_OF_MONTH) && cal.getTime().getTime() > start.getGmtSeconds();
                } else
                    return start.getLocalDay().getDay() > 28 && focusDay.daysAfter(start.getLocalDay()) % dayPeriod == 0;
            }
        } else if(null != start){
            if (dayPeriod != 7 && dayPeriod > 0) {
                if (dayPeriod == 30) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(focusDay.getYear(), focusDay.getMonth(), focusDay.getDay());
                    // only for start date <= 28
                    if (start.getLocalDay().getDay() <= 28 && focusDay.getDay() == start.getLocalDay().getDay() && cal.getTime().getTime() > start.getGmtSeconds()) {
                        return true;
                    } else if (start.getLocalDay().getDay() == 29 || start.getLocalDay().getDay() == 30) {
                        if (focusDay.getMonth() == 1) {
                            return focusDay.getDay() == cal.getActualMaximum(Calendar.DAY_OF_MONTH) && cal.getTime().getTime() > start.getGmtSeconds();
                        } else {
                            return focusDay.getDay() == start.getLocalDay().getDay() && cal.getTime().getTime() > start.getGmtSeconds();
                        }
                    } else if (start.getLocalDay().getDay() == 31) {
                        return (focusDay.getDay() == cal.getActualMaximum(Calendar.DAY_OF_MONTH) && focusDay.getMonth() != start.getLocalDay().getMonth())
                                || focusDay.equals(start.getLocalDay());
                    } else
                        return start.getLocalDay().getDay() > 28 && focusDay.daysAfter(start.getLocalDay()) % dayPeriod == 0;
                } else return focusDay.daysAfter(start.getLocalDay()) % dayPeriod == 0;
            } else return dayPeriod == 7;
        }
        return false;
    }

    public User getUserById(String userId) {
        Cursor cursor = null;
        User user = new User();
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_DISPLAYNAME_QUERY, new String[]{userId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    user.setUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                    user.setDisplayName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME)));
                    user.setUserType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_TYPE)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception at getDisplayNameForUSer method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getDisplayNameForUSer method while destroying the cursor object: " + e.getMessage());
                }
            }
        }

        return user;
    }

    public String getUserFirstNameByUserId(String userId) {
        Cursor cursor = null;
        String firstName = "";
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_DISPLAYNAME_QUERY,
                    new String[]{userId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    firstName = cursor.getString(8);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception at getUserFirstNameByUserId method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getUserFirstNameByUserId method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return firstName;
    }

    public void markDrugAsArchive(String pillId) {


        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put("archived", "1");
        String dosageType = getDosageTypeByPillID(pillId);
        dataToInsert.put(DatabaseConstants.DOSAGE_TYPE, !Util.isEmptyString(dosageType) ? dosageType : PillpopperConstants.DOSAGE_TYPE_CUSTOM);


        String where = "PILLID=?";

        try {
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, dataToInsert, where,
                    new String[]{pillId});

        } catch (SQLException e) {
            LoggerUtils.exception("Exception at markDrugsAsArchive method : " + e.getMessage());
        }

    }

    public void removeDrugFromArchive(Drug drug,Context context,String pillId) {
        boolean updatePillTable = false;
        ContentValues dataToUpdate = new ContentValues();
        ContentValues pillDataToUpdate = new ContentValues();
        dataToUpdate.put(DatabaseConstants.ARCHIVED, "0");
        if (FrontController.getInstance(context).isLastActionEventPostpone(pillId) && null != drug.get_notifyAfter()) {
            updatePillTable = true;
            DoseEventCollection doseEventCollection = new DoseEventCollection(context, drug, drug.get_notifyAfter(), 60);
            dataToUpdate.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(String.valueOf(drug.get_notifyAfter().getGmtSeconds())));
            pillDataToUpdate.put(DatabaseConstants.NOTIFY_AFTER, Util.convertDateLongToIso(String.valueOf(doseEventCollection.getNextEvent().getDate().getGmtSeconds())));
        } else {
            dataToUpdate.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));

        }
        String where = "PILLID=?";

        try {
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, dataToUpdate, where, new String[]{pillId});
            if(updatePillTable) {
                databaseHandler.update(DatabaseConstants.PILL_TABLE, pillDataToUpdate, where, new String[]{pillId});
            }
        } catch (SQLException e) {
            PillpopperLog.say("EXCEPTION: DatabaseUtils -- removeDrugFromArchive() -- " + e.toString());
        }
    }

    public void markDrugAsDeleted(String pillId) {


        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(DatabaseConstants.DELETED, "1");

        String where = "PILLID=?";

        try {
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, dataToUpdate, where, new String[]{pillId});
        } catch (SQLException e) {
            PillpopperLog.say("EXCEPTION: DatabaseUtils -- markDrugAsDeleted() -- " + e.toString());
        }
    }


    public List<String> getProxyMemberUserIds() {
        Cursor cursor = null;
        List<String> proxyIds = new ArrayList<>();
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_PROXY_USERS_BY_USERID_QUERY,
                    new String[]{"proxy"});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    proxyIds.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception at getProxyMemberUserId method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getProxyMemberUserId method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return proxyIds;
    }

    public String getPrimaryUserId() {
        Cursor cursor = null;
        String primaryUserID = "";
        try {

            cursor = databaseHandler.executeQuery(
                    DatabaseConstants.GET_PRIMARY_USERID_QUERY_ENABLED);

            if (cursor != null && cursor.moveToFirst()) {
                PillpopperLog.say("--- Primary User UserId Cursor count " + cursor.getCount());
                do {
                    if (cursor.getColumnIndex(DatabaseConstants.USER_ID) != -1) {
                        primaryUserID = cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID));
                    } else {
                        primaryUserID = "";
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception at getPrimaryUserId method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getPrimaryUserId method while destroying the cursor object: " + e.getMessage());
                }
            }
        }

        return primaryUserID;
    }

    public void clearDBTable(String table) {

        databaseHandler.delete(table, null, null);
    }

    public LinkedHashMap<String, Drug> getDrugList() {

        databaseHandler.executeQuery(getDrugListQuery());
        return null;
    }

    private String getDrugListQuery() {
        return "select * from PILL ;";
    }


    public void addMedication(Context context, PillList pillList, String userID) {

        databaseHandler.insert(context, DatabaseConstants.PILL_TABLE, pillList, "", userID);
        insertPillPreferences(context, pillList);

        insertPillSchedule(pillList);
    }

    public void updateMedication(PillList pill) {


        //update the existing pill info in PILL table and PILLPREFERENCE table
        ContentValues pillValues = new ContentValues();
        pillValues.put(DatabaseConstants.PILL_USER_ID, pill.getUserId());
        pillValues.put(DatabaseConstants.PILL_NAME, pill.getName());
        pillValues.put(DatabaseConstants.DOSE, pill.getDose());

        ContentValues pillPrefValues = new ContentValues();
        pillPrefValues.put(DatabaseConstants.NOTES, pill.getPreferences().getNotes());
        pillPrefValues.put(DatabaseConstants.CUSTOM_DESCRIPTION, pill.getPreferences().getCustomDescription());
        pillPrefValues.put(DatabaseConstants.IMAGE_GUID, pill.getPreferences().getImageGUID());

        PillpopperLog.say("MissedDoseCheck --- updateMedication method : " + Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
        //pillPrefValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
        String missedDosesLastChecked = getMissedDoseLastCheckedValue(pill.getPillId());

        if (!Util.isEmptyString(missedDosesLastChecked) && !"-1".equalsIgnoreCase(missedDosesLastChecked)) {
            PillpopperLog.say("MissedDoseCheck --- updateMedication method : " + missedDosesLastChecked);
            pillPrefValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(missedDosesLastChecked));
        } else {
            PillpopperLog.say("MissedDoseCheck --- updateMedication method : " + Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            pillPrefValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
        }

        String where = "PILLID=?";
        try {
            databaseHandler.update(DatabaseConstants.PILL_TABLE, pillValues, where, new String[]{pill.getPillId()});
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, pillPrefValues, where, new String[]{pill.getPillId()});
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateMedication method : " + e.getMessage());
        }
    }

    private void insertPillSchedule(PillList pillList) {

        databaseHandler.insertPillSchedule(DatabaseConstants.PILL_SCHEDULE_TABLE, pillList, "");
    }

    private void insertPillPreferences(Context context, PillList pillList) {

        databaseHandler.insert(context, DatabaseConstants.PILL_PREFERENCE_TABLE, pillList, "", "");
    }

    public void updateMaxDailyDoses(long doses, String pillId) {


        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put("maxNumDailyDoses", String.valueOf(doses));

        String where = "PILLID=?";

        try {
            databaseHandler.update("PILLPREFERENCE", dataToInsert, where,
                    new String[]{pillId});

        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateMaxDailyDoses method : " + e.getMessage());
        }
    }

    public void updateScheduleType(String type, String pillId) {


        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.SCHEDULE_CHOICE, type);

        String where = "PILLID=?";

        try {
            databaseHandler.update("PILLPREFERENCE", dataToInsert, where,
                    new String[]{pillId});

        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateScheduleType method : " + e.getMessage());
        }
    }

    public void updateNotes(String notes, String pillId) {


        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.NOTES, notes);

        String where = "PILLID=?";
        try {
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, dataToInsert, where, new String[]{pillId});
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateNotes method : " + e.getMessage());
        }
    }

    public Drug getDrugByPillId(String pillId) {
        {
            Cursor cursor = null;
            Drug drug = new Drug();
            try {

                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_DRUG_BY_PILL_ID_QUERY, new String[]{pillId});
                if (cursor != null && cursor.moveToFirst()) {
                    do {

                        drug.setId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                        drug.setName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
                        drug.setCreated(Util.convertStringtoPillpopperTime(cursor.getString(cursor.getColumnIndex(DatabaseConstants.CREATED))));
                        drug.setNotes(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES)));
                        drug.setLastTaken(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_TAKEN)))));
                        drug.set_notifyAfter(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER)))));
                        drug.set_effLastTaken(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.EFF_LAST_TAKEN)))));
                        drug.setMemberFirstName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME)));

                        drug.setUserID(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));

                        if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION)) != null
                                && !("").equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.CUSTOM_DESCRIPTION)))
                                && cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION) != -1) {
                            drug.setDose(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.CUSTOM_DESCRIPTION)));
                        } else if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION)) != null
                                && !("").equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.MANAGED_DESCRIPTION)))
                                && cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION) != -1) {
                            drug.setDose(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.MANAGED_DESCRIPTION)));
                        } else {
                            drug.setDose(null);
                        }

                        drug.setPreferecences(getDrugPreferences(cursor));

                        drug.setScheduledFrequency(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY)));

                        Schedule schedule = new Schedule();
                        schedule.setTimeList(new TimeList(getSchdulesByPillId(drug.getGuid())));

                        schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START)))));
                        schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.END)))));


                       // schedule.setSchedType(getScheduleType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE))));
                        schedule.setDayPeriod(Util.parseJSONNonnegativeLong((cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)))));
                        schedule.setDays((cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS))));
                        drug.setSchedule(schedule);

                        //instruction and Rx for only KPHC meds
                        drug.setDirections(cursor.getString(cursor.getColumnIndex(DatabaseConstants.INSTRUCTIONS)));
                        drug.setPrescriptionNum(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PRESCRIPTION_NUM)));
                        drug.setScheduleGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_GUID)));

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while fetching the pill list");
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        LoggerUtils.exception("Exception at getDrugByPillId method : " + e.getMessage());
                    }
                }
            }
            return drug;
        }
    }

    public Drug getDrugDoseByPillId(String pillId) {
        {
            Cursor cursor = null;
            Drug drug = new Drug();
            try {

                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_DRUG_DOSE_BY_PILL_ID_QUERY, new String[]{pillId});

                if (cursor != null && cursor.moveToFirst()) {
                    do {

                        drug.setPreferecences(getDrugDosePreferences(cursor));

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while fetching the pill list");
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        LoggerUtils.exception("Exception at getDrugByPillId method : " + e.getMessage());
                    }
                }
            }
            return drug;
        }
    }

    private JSONObject getDrugDosePreferences(Cursor cursor) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("customDescription", cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION)));
            jsonObject.put("managedDescription", cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION)));
        } catch (JSONException e) {
            LoggerUtils.exception("Exception in getDrugDosePreferences" + e.getMessage());
        }
        return jsonObject;
    }

    private Schedule.SchedType getScheduleType(String scheduleTypeStr) {
        if (null != scheduleTypeStr && ("scheduled").equalsIgnoreCase(scheduleTypeStr)) {
            return Schedule.SchedType.SCHEDULED;
        } else if (null != scheduleTypeStr && ("interval").equalsIgnoreCase(scheduleTypeStr)) {
            return Schedule.SchedType.INTERVAL;
        } else if (null != scheduleTypeStr && ("AS_NEEDED").equalsIgnoreCase(scheduleTypeStr)) {
            return Schedule.SchedType.AS_NEEDED;
        } else {
            return Schedule.SchedType.SCHEDULED;
        }
    }

    public JSONArray getLogEntries(PillpopperActivity _pillpopperActivity) {
        JSONArray logEntriesArray = new JSONArray();
        Cursor cursor = null;

        try {

            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_LOG_ENTRY);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject obj = new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.LOG_ENTRY)));
                    logEntriesArray.put(obj);
                } while (cursor.moveToNext());
            }
            logEntriesArray.put(Util.prepareGetState(_pillpopperActivity));
        } catch (Exception e) {
            LoggerUtils.exception("Exception at getLogEntries method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getLogEntries method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return logEntriesArray;
    }

    public JSONArray getLogEntries(Context context) {
        JSONArray logEntriesArray = new JSONArray();
        Cursor cursor = null;

        try {

            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_LOG_ENTRY);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject obj = new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.LOG_ENTRY)));

                    logEntriesArray.put(obj);
                } while (cursor.moveToNext());
            }
            logEntriesArray.put(Util.prepareGetState(context));
        } catch (Exception e) {
            LoggerUtils.exception("Exception at getLogEntries method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getLogEntries method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return logEntriesArray;
    }

    public JSONArray getNonSecureLogEntries(Context context) {
        JSONArray logEntriesArray = new JSONArray();
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_LOG_ENTRY);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject obj = new JSONObject(cursor.getString(cursor.getColumnIndex(DatabaseConstants.LOG_ENTRY)));
                    if (null != obj) {
                        JSONObject pillpopperRequest = obj.getJSONObject("pillpopperRequest");
                        if (null != pillpopperRequest) {
                            String action = pillpopperRequest.getString("action");
                            if (!PillpopperConstants.ACTION_EDIT_PILL.equalsIgnoreCase(action)
                                    && !PillpopperConstants.ACTION_GET_STATE.equalsIgnoreCase(action)) {
                                logEntriesArray.put(obj);
                            }
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception at getLogEntries method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getLogEntries method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return logEntriesArray;
    }

    public boolean isLogEntryAvailable() {
        Cursor cursor = null;
        boolean isLogEntryAvailable = false;
        try {

            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_LOG_ENTRY);

            if (cursor.getCount() > 0) {
                isLogEntryAvailable = true;
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception at isLogEntryAvailable method: " + e.getMessage());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                LoggerUtils.exception("Exception at isLogEntryAvailable method while destroying the cursor object: " + e.getMessage());
            }
        }

        return isLogEntryAvailable;
    }

    /**
     * Returns TRUE if the provided user is primary member else FALSE.
     *
     * @param userId user id
     * @return user is primary or not
     */
    public boolean isPrimaryUser(String userId) {
        return getPrimaryUserIdIgnoreEnabled().equalsIgnoreCase(userId);
    }

    /**
     * Removes the entry from the log table based on the provided replyId.
     *
     * @param replyId replyid for log table
     */
    public void removeLogEntry(String replyId) {

        try {
            if (null != databaseHandler && null != replyId) {
                databaseHandler.delete(DatabaseConstants.LOG_ENTRY_TABLE,
                        "GUID = ?", new String[]{replyId});
            }
        } catch (Exception e) {
            PillpopperLog.say(e.getMessage());
        }
    }


    /**
     * This method needs to be called for any change if medication
     * * @param entry
     */
    public void addLogEntry(LogEntryModel entry, Context context) {


        try {
            databaseHandler.insert(context, DatabaseConstants.LOG_ENTRY_TABLE, entry, null, null);

        } catch (Exception e) {
            LoggerUtils.exception("Exception at addLogEntry method : " + e.getMessage());
        }
    }

    public JSONArray getSchdulesByPillId(String pillId) {

        JSONArray jsonSchedule = new JSONArray();
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_SCHEDULES_BY_PILL_ID_QUERY, new String[]{pillId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String schedule = Util.convertTimeFormatToHHMM(cursor.getString(0));
                    jsonSchedule.put(schedule);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at getSchdulesByPillId method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getSchdulesByPillId method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        PillpopperLog.say("schedules : " + jsonSchedule.toString());

        return jsonSchedule;
    }

    public JSONArray getSchedulesInTimeFormateByPillId(String pillId) {

        JSONArray jsonSchedule = new JSONArray();
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_SCHEDULES_BY_PILL_ID_QUERY, new String[]{pillId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String schedule = cursor.getString(0);
                    jsonSchedule.put(schedule);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at getSchdulesByPillId method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getSchdulesByPillId method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        PillpopperLog.say("schedules : " + jsonSchedule.toString());

        return jsonSchedule;
    }

    public ArchiveListDataWrapper getArchiveListData(final PillpopperActivity _thisActivity) {
        final ArchiveListDataWrapper archiveListDataWrapper = new ArchiveListDataWrapper();


        Thread getArchivedDrugListThread = new Thread(() -> {

            Cursor cursor = databaseHandler.executeQuery(DatabaseConstants.GET_SORTED_ARCHIVED_DRUG_LIST_QUERY);
            HashMap<String, ArrayList<ArchiveListDrug>> archivedDrugsHashMap = new HashMap<>();
            String referenceUserId = null;
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String pillId = cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID));
                        String pillName = cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME));
                        String pillUserId = cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID));
                        String pillNotes = cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES));
                        String imageGuid = cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID));
                        String pillDosageType = cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE));
                        String pillDose;

                        if (referenceUserId == null || !referenceUserId.equals(pillUserId)) {
                            ArrayList<ArchiveListDrug> drugListForUser = new ArrayList<>();
                            archivedDrugsHashMap.put(pillUserId, drugListForUser);
                            referenceUserId = pillUserId;

                        }

                        ArchiveListDrug archivedDrug = new ArchiveListDrug();
                        archivedDrug.setPillId(pillId);
                        archivedDrug.setPillName(pillName);

                        if (pillDosageType != null) {
                            if (("managed").equalsIgnoreCase(pillDosageType)) {
                                pillDose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION));
                                archivedDrug.setManaged(true);
                            } else {
                                pillDose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION));
                                archivedDrug.setManaged(false);
                            }
                        } else {
                            pillDose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION));
                            archivedDrug.setManaged(false);
                        }

                        archivedDrug.setDose(pillDose);
                        archivedDrug.setUserId(pillUserId);
                        archivedDrug.setNotes(pillNotes);
                        archivedDrug.setImageGuid(imageGuid);

                        archivedDrugsHashMap.get(pillUserId).add(archivedDrug);

                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
            } finally {
                try {
                    if (cursor != null
                            && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }

            archiveListDataWrapper.setArchivedDrugsHashMap(archivedDrugsHashMap);

        });

        Thread getUserListThread = new Thread(() -> {

            Cursor cursor = databaseHandler.executeQuery(DatabaseConstants.GET_USER_LIST_FOR_ARCHIVE_SCREEN_QUERY);
            ArrayList<ArchiveListUserDropDownData> userDropDownList = new ArrayList<>();

            try {
                if (cursor.moveToFirst()) {
                    do {
                        ArchiveListUserDropDownData userDropDownData = new ArchiveListUserDropDownData(
                                cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)),
                                cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME))
                        );
                        userDropDownList.add(userDropDownData);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
            } finally {
                try {
                    if (cursor != null
                            && !cursor.isClosed()) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }

            archiveListDataWrapper.setUserDropDownList(userDropDownList);
        });

        getArchivedDrugListThread.start();
        getUserListThread.start();

        try {
            getArchivedDrugListThread.join();
            getUserListThread.join();
        } catch (InterruptedException e) {
            PillpopperLog.exception(e.getMessage());
        }


        return archiveListDataWrapper;
    }

    public HashMap<String, ArrayList<ArchiveListDrug>> getArchiveListDataHashMap(final PillpopperActivity _thisActivity) {
        final HashMap<String, ArrayList<ArchiveListDrug>> archivedDrugsHashMap = new HashMap<>();

        try {

            Thread getArchivedDrugListThread = new Thread(() -> {

                Cursor cursor = databaseHandler.executeQuery(DatabaseConstants.GET_SORTED_ARCHIVED_DRUG_LIST_QUERY);

                String referenceUserId = null;

                try {

                    if (cursor.moveToFirst()) {
                        do {
                            String pillId = cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID));
                            String pillName = cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME));
                            String pillUserId = cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID));
                            String pillNotes = cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES));
                            String imageGuid = cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID));
                            String pillDosageType = cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE));
                            String pillDose;

                            if (referenceUserId == null || !referenceUserId.equals(pillUserId)) {
                                ArrayList<ArchiveListDrug> drugListForUser = new ArrayList<>();
                                archivedDrugsHashMap.put(pillUserId, drugListForUser);
                                referenceUserId = pillUserId;

                            }

                            ArchiveListDrug archivedDrug = new ArchiveListDrug();
                            archivedDrug.setPillId(pillId);
                            archivedDrug.setPillName(pillName);

                            if (pillDosageType != null) {
                                if (("managed").equalsIgnoreCase(pillDosageType)) {
                                    pillDose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION));
                                    archivedDrug.setManaged(true);
                                } else {
                                    pillDose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION));
                                    archivedDrug.setManaged(false);
                                }
                            } else {
                                pillDose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION));
                                archivedDrug.setManaged(false);
                            }

                            archivedDrug.setDose(pillDose);
                            archivedDrug.setUserId(pillUserId);
                            archivedDrug.setNotes(pillNotes);
                            archivedDrug.setImageGuid(imageGuid);

                            archivedDrugsHashMap.get(pillUserId).add(archivedDrug);


                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                } finally {
                    try {
                        if (cursor != null
                                && !cursor.isClosed()) {
                            cursor.close();
                        }
                    } catch (Exception e) {
                        PillpopperLog.exception(e.getMessage());
                    }
                }

            });

            getArchivedDrugListThread.start();
            getArchivedDrugListThread.join();


        } catch (Exception e) {
            PillpopperLog.say(e.getMessage());
        }

        return archivedDrugsHashMap;
    }


    public ArchiveDetailDrug getArchivedDrugDetails(PillpopperActivity _thisActivity, String pillId) {
        ArchiveDetailDrug archiveDetailDrug = new ArchiveDetailDrug();
        Cursor cursor = null;
        try {


            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ARCHIVED_DRUG_DETAILS_QUERY, new String[]{pillId});

            if (cursor.moveToFirst()) {
                String pillName = cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME));
                String userFirstName = cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME));
                String dosageType = cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE));
                String dose;
                String scheduledFrequency = cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULED_FREQUENCY));
                String dayPeriod = cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD));
                String notes = cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES));
                String instructions = cursor.getString(cursor.getColumnIndex(DatabaseConstants.INSTRUCTIONS));
                String rxNumber = cursor.getString(cursor.getColumnIndex(DatabaseConstants.PRESCRIPTION_NUM));
                String weekdays = cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS));
                String start = Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START)));
                String imageGuid = cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID));

                archiveDetailDrug.setPillName(pillName);
                archiveDetailDrug.setUserFirstName(userFirstName);
                if (dosageType != null) {
                    if (("managed").equalsIgnoreCase(dosageType)) {
                        dose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION));
                        archiveDetailDrug.setManaged(true);
                    } else {
                        dose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION));
                        archiveDetailDrug.setManaged(false);
                    }
                } else {
                    dose = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_DESCRIPTION));
                    archiveDetailDrug.setManaged(false);
                }

                archiveDetailDrug.setDose(dose);
                archiveDetailDrug.setScheduledFrequency(scheduledFrequency);
                archiveDetailDrug.setDayPeriod(dayPeriod);
                archiveDetailDrug.setNotes(notes);
                archiveDetailDrug.setInstructions(instructions);
                archiveDetailDrug.setRxNumber(rxNumber);
                archiveDetailDrug.setWeekdays(weekdays);
                archiveDetailDrug.setStart(start);
                archiveDetailDrug.setImageGuid(imageGuid);

                Cursor scheduleCursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_SCHEDULES_BY_PILL_ID_QUERY, new String[]{pillId});
                ArrayList<String> sortedPillTimesList = new ArrayList();
                if (null != scheduleCursor && scheduleCursor.moveToFirst()) {
                    do {
                        String pillTime = Util.convertTimeFormatToHHMM(scheduleCursor.getString(scheduleCursor.getColumnIndex(DatabaseConstants.PILLTIME)));
                        if (!Util.isEmptyString(pillTime)) {
                            int minute = Util.handleParseInt(pillTime) % 100;
                            int hour = Util.handleParseInt(pillTime) / 100;
                            HourMinute hourMinute = new HourMinute(hour, minute);
                            ///  hourMinute = hourMinute.convertGmtToLocal();

                            //Creating a new string object with the local pill time string
                            //and adding it to the ArrayList which has to be sorted
                            String localPillTime = String.format("%02d", hourMinute.getHour()) + String.format("%02d", hourMinute.getMinute());
                            sortedPillTimesList.add(localPillTime);
                        }
                    } while (scheduleCursor.moveToNext());
                }
                if (scheduleCursor != null) scheduleCursor.close();

                if (!sortedPillTimesList.isEmpty()) {
                    //Sorting the ArrayList with the local pill times
                    Collections.sort(sortedPillTimesList);

                    //Adding the sorted pill times to the
                    for (String pillTime : sortedPillTimesList) {
                        archiveDetailDrug.addScheduleTime(pillTime);
                    }
                }
            }

        } catch (Exception e) {
            LoggerUtils.exception("Exception at getArchivedDrugsDetails method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getArchivedDrugsDetails method while destroying the cursor object: " + e.getMessage());
                }
            }
        }

        return archiveDetailDrug;
    }

    public String isQuickViewEnabled() {

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.QUICKVIEW_FLAG_CHECK_QUERY, new String[]{getPrimaryUserIdIgnoreEnabled()});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    return cursor.getString(cursor.getColumnIndex(DatabaseConstants.QUICKVIEW_OPTINED));
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at isQuickViewEnabled method: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at isQuickViewEnabled method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return State.QUICKVIEW_OPTED_OUT;
    }

    public void updateNewKPHCMed(String pillID, String lastManagedIdNotified) {


        ContentValues values = new ContentValues();

        values.put(DatabaseConstants.LAST_MANAGED_ID_NOTIFIED, lastManagedIdNotified);
        databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, values, DatabaseConstants.PILL_ID + "=?", new String[]{pillID});

    }

    public void performTakeDrug(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context context, boolean isLogEntryRequired) {

        for (Drug drug : overDueDrugList) {
            drug.setIsActionDateRequired(true);
            drug.setRecordDate(Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            if (!AppConstants.updateNotifyAfterValue){
                drug.setActionDate(Util.convertDateLongToIso(String.valueOf(null != drug.getOverdueDate()
                        ? drug.getOverdueDate().getGmtSeconds()
                        : PillpopperTime.now().getGmtSeconds())));
            }
            drug.setOpID(getPillHistoryEventOpId());
            if (isEmptyHistoryEventAvailable(drug.getOverdueDate(), drug.getGuid())) {
                deleteEmptyHistoryEvent(drug.getOverdueDate(), drug.getGuid());
            }
            addHistoryEntry(context, drug, PillpopperConstants.ACTION_TAKE_PILL_HISTORY, dateTaken, 0);
            ContentValues dataToInsert = new ContentValues();
            //ScheduleDate has to be updated for last_take/last field
            if (drug.getOverdueDate() != null) {
                dataToInsert.put(DatabaseConstants.LAST_TAKEN,
                        Util.convertDateLongToIso(String.valueOf(drug.getOverdueDate().getGmtSeconds())));
            }
            dataToInsert.put(DatabaseConstants.OVERDUE, PillpopperConstants.NOT_OVERDUE);
            String where = "PILLID=?";
            try {
                int result = databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{drug.getGuid()});

                // update the last doses missed_doses_last_checked value before adding the log entry
                updateMissedDosesLastCheckedValue(context, drug, false);

                // Adding the Log entry event and the corresponding createHistoryEvent for the take pill action, which will sync with server using intermediate Sync .
                if (isLogEntryRequired) {
                    addLogEntry(Util.prepareLogEntryForAction(PillpopperConstants.ACTION_TAKE_PILL, drug, context), context);
                    addLogEntry(Util.prepareLogEntryForCreateHistoryEvent(PillpopperConstants.ACTION_TAKE_PILL, drug, context), context);
                }
            } catch (SQLException e) {
                PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
            }


        }
    }

    private void updateMissedDosesLastCheckedValue(Context context, Drug drug, boolean isFromLateReminder) {
        boolean updateRequired = false;
        if (isFromLateReminder) {
            if (!havePendingPastReminders(context, drug.getGuid())) {
                updateRequired = true;
            }
        } else {
            if (!isEntryAvailableInPastReminder(drug.getGuid())) {
                updateRequired = true;
            }
        }
        if (updateRequired) {
            String scheduleDate = FrontController.getInstance(context).getScheduleDateFromHistory(drug.getGuid());
            // if the action is taken on a postponed reminder,
            // the schedule date from history will be less than the actual postponed time
            // so we are updating the postponed time to misseddoses last check value.
            if (null != drug.getOverdueDate()) {
                if (drug.getOverdueDate().getGmtSeconds() > Long.valueOf(Util.convertDateIsoToLong(scheduleDate))) {
                    scheduleDate = Util.convertDateLongToIso(String.valueOf(drug.getOverdueDate().getGmtSeconds()));
                }
            }
            if (!Util.isEmptyString(scheduleDate)) {
                updateLastMissedCheck(drug.getGuid(), scheduleDate);
            }
        }
    }

    public void deleteEmptyHistoryEvent(PillpopperTime dateTaken, String pillId) {
        String historyGuid = getHistoryGuid(dateTaken, pillId);
        databaseHandler.delete(DatabaseConstants.HISTORY_TABLE,
                DatabaseConstants.PILL_ID + "=? AND " + DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE + "=?",
                new String[]{pillId, Util.convertDateLongToIso(String.valueOf(dateTaken.getGmtSeconds()))});
        if(!Util.isEmptyString(historyGuid)) {
            databaseHandler.delete(DatabaseConstants.HISTORY_PREFERENCE_TABLE, DatabaseConstants.HISTORY_EVENT_GUID + "=?", new String[]{historyGuid});
        }

    }

    private String getHistoryGuid(PillpopperTime dateTaken, String pillId) {
        Cursor cursor;
        String guid = "";
        cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_HISTORY_GUID, new String[]{pillId, Util.convertDateLongToIso(String.valueOf(dateTaken.getGmtSeconds()))});
        try {
            while (cursor.moveToNext()) {
                guid = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_GUID));
            }
        } catch (SQLException exp) {
            LoggerUtils.exception("Exception while fetching history event GUID" + exp.getMessage());
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return guid;
    }

    /**
     * Perform the Skip/Skip All action over the provided drugs
     *
     * @param overDueDrugList list of drugs
     * @param dateSkipped     date for action
     * @param context         context of the class
     */
    public void performSkipDrug(List<Drug> overDueDrugList, PillpopperTime dateSkipped, Context context, boolean isLogEntryRequired) {

        for (Drug drug : overDueDrugList) {
            drug.setIsActionDateRequired(false);
            drug.setRecordDate(Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            drug.setOpID(getPillHistoryEventOpId());
            if (isEmptyHistoryEventAvailable(drug.getOverdueDate(), drug.getGuid())) {
                deleteEmptyHistoryEvent(drug.getOverdueDate(), drug.getGuid());
            }
            addHistoryEntry(context, drug, PillpopperConstants.ACTION_SKIP_PILL_HISTORY, dateSkipped, 0);
            ContentValues dataToInsert = new ContentValues();
            //ScheduleDate has to be updated for last_take/last field
            dataToInsert.put(DatabaseConstants.LAST_TAKEN,
                    Util.convertDateLongToIso(getScheduleDateFromHistory(drug.getGuid())));
            String where = "PILLID=?";
            try {
                int result = databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{drug.getGuid()});

                // update the last doses missed_doses_last_checked value before adding the log entry
                updateMissedDosesLastCheckedValue(context, drug, false);

                // Adding the Log entry event and the corresponding createHistoryEvent for the take pill action, which will sync with server using intermediate Sync .
                if (isLogEntryRequired) {
                    addLogEntry(Util.prepareLogEntryForAction(PillpopperConstants.ACTION_SKIP_PILL, drug, context), context);
                    addLogEntry(Util.prepareLogEntryForCreateHistoryEvent(PillpopperConstants.ACTION_SKIP_PILL, drug, context), context);
                }
            } catch (SQLException e) {
                PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
            }
        }
    }

    public void performTakeDrug_pastReminders(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context pillpopperActivity, boolean isLogEntryRequired) {

        for (Drug drug : overDueDrugList) {
            drug.setIsActionDateRequired(true);
            drug.setRecordDate(Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            drug.setActionDate(Util.convertDateLongToIso(String.valueOf(drug.getScheduledTime().getGmtSeconds())));
            drug.setOpID(getPillHistoryEventOpId());
            if (isEmptyHistoryEventAvailable(drug.getScheduledTime(), drug.getGuid())) {
                deleteEmptyHistoryEvent(drug.getScheduledTime(), drug.getGuid());
            }
            addHistoryEntry_pastReminders(drug, PillpopperConstants.ACTION_TAKE_PILL_HISTORY, dateTaken, 0,pillpopperActivity);
            ContentValues dataToInsert = new ContentValues();
            //ScheduleDate has to be updated for last_take/last field
            if (drug.getOverdueDate() != null) {
                dataToInsert.put(DatabaseConstants.LAST_TAKEN,
                        Util.convertDateLongToIso(String.valueOf(drug.getOverdueDate().getGmtSeconds())));
            }
            dataToInsert.put(DatabaseConstants.OVERDUE, PillpopperConstants.NOT_OVERDUE);
            String where = "PILLID=?";
            try {
                int result = databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{drug.getGuid()});

                updateMissedDosesLastCheckedValue(pillpopperActivity, drug, true);

                // Adding the Log entry event and the corresponding createHistoryEvent for the take pill action, which will sync with server using intermediate Sync .
                if (isLogEntryRequired) {
                    addLogEntry(Util.prepareLogEntryForAction_pastReminders(PillpopperConstants.ACTION_TAKE_PILL, drug, pillpopperActivity), pillpopperActivity);
                    addLogEntry(Util.prepareLogEntryForCreateHistoryEvent_pastReminders(PillpopperConstants.ACTION_TAKE_PILL, drug, pillpopperActivity), pillpopperActivity);
                }
            } catch (SQLException e) {
                PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
            }


        }
    }

    public void performSkipDrug_pastReminders(List<Drug> overDueDrugList, PillpopperTime dateSkipped, Context pillpopperActivity, boolean isLogEntryRequired) {

        for (Drug drug : overDueDrugList) {
            drug.setIsActionDateRequired(false);
            drug.setRecordDate(Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            drug.setOpID(getPillHistoryEventOpId());
            if (isEmptyHistoryEventAvailable(drug.getScheduledTime(), drug.getGuid())) {
                deleteEmptyHistoryEvent(drug.getScheduledTime(), drug.getGuid());
            }
            addHistoryEntry_pastReminders(drug, PillpopperConstants.ACTION_SKIP_PILL_HISTORY, dateSkipped, 0,pillpopperActivity);
            ContentValues dataToInsert = new ContentValues();
            //ScheduleDate has to be updated for last_take/last field
            dataToInsert.put(DatabaseConstants.LAST_TAKEN,
                    Util.convertDateLongToIso(getScheduleDateFromHistory(drug.getGuid())));
            String where = "PILLID=?";
            try {
                int result = databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{drug.getGuid()});

                updateMissedDosesLastCheckedValue(pillpopperActivity, drug, true);

                // Adding the Log entry event and the corresponding createHistoryEvent for the take pill action, which will sync with server using intermediate Sync .
                if (isLogEntryRequired) {
                    addLogEntry(Util.prepareLogEntryForAction_pastReminders(PillpopperConstants.ACTION_SKIP_PILL, drug, pillpopperActivity), pillpopperActivity);
                    addLogEntry(Util.prepareLogEntryForCreateHistoryEvent_pastReminders(PillpopperConstants.ACTION_SKIP_PILL, drug, pillpopperActivity), pillpopperActivity);
                }
            } catch (SQLException e) {
                PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
            }


        }
    }

    private void addHistoryEntry_pastReminders(Drug drug, String operation, PillpopperTime actedTime, long postPoneByMins,Context context) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_OPERATION, operation);
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_GUID, Util.getRandomGuid());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_TZ_SEC, String.valueOf(Util.getTzOffsetSecs(TimeZone.getDefault())));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_TZ_NAME, TimeZone.getDefault().getDisplayName());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DOSAGE_TYPE, drug.getPreferences().getPreference("dosageType"));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CUSTOM_DESCRIPTION, drug.getPreferences().getPreference("customDescription"));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_MANAGED_DESCRIPTION, drug.getPreferences().getPreference("managedDescription"));

        if (!Util.getScheduleChoice(drug).equalsIgnoreCase(AppConstants.SCHEDULE_CHOICE_SCHEDULED)) {
            dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, PillpopperTime.now().getGmtSeconds());
        } else {
            if (null != drug.getScheduledTime()) {
                if (!operation.equals(PillpopperConstants.ACTION_MISS_PILL)) {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(drug.getScheduledTime().getGmtSeconds())));
                } else {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(actedTime.getGmtSeconds())));
                }
                dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, drug.getScheduledTime().getGmtSeconds());

            } else {
                if (null != drug.getLastTaken()) {
                    if (!operation.equals(PillpopperConstants.ACTION_MISS_PILL)) {
                        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(drug.getLastTaken().getGmtSeconds())));
                    } else {
                        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(actedTime.getGmtSeconds())));
                    }
                } else {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
                }
                if (null != actedTime) {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, actedTime.getGmtSeconds());
                } else {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, PillpopperTime.now().getGmtSeconds());
                }
            }
        }
        dataToInsert.put(DatabaseConstants.PILL_ID, drug.getGuid());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_PERSON_ID, drug.getUserID());
        dataToInsert.put(DatabaseConstants.PILL_NAME, drug.getName());

        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_OPID, drug.getOpID());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DESCRIPTION, drug.getName() + "  " + operation);
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_EDIT_TIME, System.currentTimeMillis());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DELETED, false);
        // look for recent postpone event
        HistoryEvent latestPostponeEvent = getLastPostponedHistoryEventForSpecificTime(drug.getGuid(),String.valueOf(getScheduleDate(drug)));

        //edit the lastPostponeEvent and set isPostponedEventActive to false - local
        //send editHistoryEvent log entry and edit the local History preferences. - to server
        if (null != latestPostponeEvent && null != latestPostponeEvent.getPreferences()
                && latestPostponeEvent.getPreferences().isPostponedEventActive() && (Util.convertDateIsoToLong(latestPostponeEvent.getPreferences().getFinalPostponedDateTime()).equalsIgnoreCase(String.valueOf(drug.getScheduledTime().getGmtSeconds()))
        || latestPostponeEvent.getHeaderTime().equalsIgnoreCase(String.valueOf(drug.getScheduledTime().getGmtSeconds())))) {
            if(drug.get_notifyAfter().getGmtSeconds() <= drug.getScheduledTime().getGmtSeconds()){
                AppConstants.updateNotifyAfterValue = true;
            }else{
                AppConstants.updateNotifyAfterValue = false;
            }
            Util.editPostponeEvent(drug,latestPostponeEvent,context);
            drug.setScheduledTime(new PillpopperTime(Long.valueOf(latestPostponeEvent.getHeaderTime())));
            dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(latestPostponeEvent.getHeaderTime()));
        }
        try {
            databaseHandler.insert(DatabaseConstants.HISTORY_TABLE, dataToInsert);
        } catch (SQLException e) {
            PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
        }
        String eventGuid = dataToInsert.getAsString(DatabaseConstants.HISTORY_EVENT_GUID);
        // insert preferences to the table
        addHistoryPreferences(eventGuid, drug, operation, 0);
    }

    private Long getScheduleDate(Drug drug) {
        if (null != drug.getOverdueDate())
            return drug.getOverdueDate().getGmtSeconds();
        else if (null != drug.getScheduledTime())
            return drug.getScheduledTime().getGmtSeconds();
        else if (null != drug.getLastTaken())
            return drug.getLastTaken().getGmtSeconds();
        else {
            return PillpopperTime.now().getGmtSeconds();
        }
    }


    private void addHistoryEntry(Context context, Drug drug, String operation, PillpopperTime actedTime, long postPoneByMins) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_OPERATION, Util.isNull(operation));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_GUID, Util.getRandomGuid());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_TZ_SEC, String.valueOf(Util.getTzOffsetSecs(TimeZone.getDefault())));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_TZ_NAME, TimeZone.getDefault().getDisplayName());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DOSAGE_TYPE, drug.getPreferences().getPreference("dosageType"));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CUSTOM_DESCRIPTION, drug.getPreferences().getPreference("customDescription"));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_MANAGED_DESCRIPTION, drug.getPreferences().getPreference("managedDescription"));

        if (!Util.getScheduleChoice(drug).equalsIgnoreCase(AppConstants.SCHEDULE_CHOICE_SCHEDULED)) {
            dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, PillpopperTime.now().getGmtSeconds());
        } else {
            if (null != drug.getOverdueDate()) {
                if (!operation.equalsIgnoreCase(PillpopperConstants.ACTION_MISS_PILL)) {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(drug.getOverdueDate().getGmtSeconds())));
                } else {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(actedTime.getGmtSeconds())));
                }
                if (actedTime != null) {
                    PillpopperLog.say("Action might be postpone or else taken earlier.");
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, actedTime.getGmtSeconds());
                } else {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, drug.getOverdueDate().getGmtSeconds());
                }
            } else {
                if (null != drug.getLastTaken()) {
                    if (!operation.equalsIgnoreCase(PillpopperConstants.ACTION_MISS_PILL)) {
                        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(drug.getLastTaken().getGmtSeconds())));
                    } else {
                        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(actedTime.getGmtSeconds())));
                    }
                } else {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
                }
                if (null != actedTime) {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, actedTime.getGmtSeconds());
                } else {
                    dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, PillpopperTime.now().getGmtSeconds());
                }
            }
            if (operation.equalsIgnoreCase(PillpopperConstants.ACTION_SKIP_PILL)) {
                PillpopperLog.say("Action is skip pill ");
                dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, PillpopperTime.now().getGmtSeconds());
            }
        }
        if(AppConstants.updateNotifyAfterValue && null!=drug.getHistoryScheduleDate()){
            dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(drug.getHistoryScheduleDate()));
        }
        // look for recent postpone event
        HistoryEvent latestPostponeEvent = getLastPostponedHistoryEventForSpecificTime(drug.getGuid(),String.valueOf(getScheduleDate(drug)));

        if (null != latestPostponeEvent && null != latestPostponeEvent.getPreferences()
                && null != latestPostponeEvent.getPreferences().getFinalPostponedDateTime()
                && Util.isOverdueAndPostponedEventSame(drug.getOverdueDate(), latestPostponeEvent.getPreferences().isPostponedEventActive(), latestPostponeEvent.getPreferences().getFinalPostponedDateTime())) {
            dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(latestPostponeEvent.getHeaderTime()));
        }

        dataToInsert.put(DatabaseConstants.PILL_ID, drug.getGuid());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_PERSON_ID, Util.isNull(drug.getUserID()));
        dataToInsert.put(DatabaseConstants.PILL_NAME, drug.getName());

        if (drug.isManaged()) {
            dataToInsert.put(DatabaseConstants.MANAGED_DESCRIPTION, drug.getDose());
        } else {
            dataToInsert.put(DatabaseConstants.CUSTOM_DESCRIPTION, drug.getDose());
        }
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_OPID, drug.getOpID());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DESCRIPTION, drug.getName() + "  " + operation);
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_EDIT_TIME, System.currentTimeMillis());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DELETED, false);
        try {
            databaseHandler.insert(DatabaseConstants.HISTORY_TABLE, dataToInsert);
        } catch (SQLException e) {
            PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
        }

        String eventGuid = dataToInsert.getAsString(DatabaseConstants.HISTORY_EVENT_GUID);
        // insert preferences to the table
        addHistoryPreferences(eventGuid, drug, operation, postPoneByMins);

        //edit the lastPostponeEvent and set isPostponedEventActive to false - local
        //send editHistoryEvent log entry and edit the local History preferences. - to server
        if (null != latestPostponeEvent && Util.isOverdueAndPostponedEventSame(drug.getOverdueDate(), latestPostponeEvent.getPreferences().isPostponedEventActive(), latestPostponeEvent.getPreferences().getFinalPostponedDateTime())) {
            AppConstants.updateNotifyAfterValue = true;
            Util.editPostponeEvent(drug,latestPostponeEvent,context);
        }
    }


    public List<HistoryEvent> getActivePostponedEvents(String pillId) {
        Cursor cursor = null;
        List<HistoryEvent> historyEventsList = new ArrayList<>();
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_RECENT_POSTPONE_HISTORY_EVENT_FOR_PILLID,
                    new String[]{PillpopperConstants.ACTION_POST_PONE_PILL, pillId});
            if (cursor != null && cursor.moveToFirst()) {
                LoggerUtils.info("History Count in DB - " + cursor.getCount());
                do {
                    HistoryEvent historyEvent = new HistoryEvent();
                    historyEvent.setPillID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID)));
                    String pillName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_NAME));
                    if (Util.isEmptyString(pillName)) {
                        historyEvent.setPillName(getDrugNameByPillID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID))));
                    } else {
                        historyEvent.setPillName(pillName);
                    }

                    String historyScheduleDate = Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE)));
                    if (historyScheduleDate != null && !("-1").equalsIgnoreCase(historyScheduleDate)) {
                        historyEvent.setHeaderTime(historyScheduleDate);
                    } else if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)) != null && !("-1").equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)))) {
                        historyEvent.setHeaderTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)));
                    }

                    if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID) != -1) {
                        historyEvent.setHistoryEventGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID)));
                    }

                    historyEvent.setDosage(getDrugDoseByPillId(historyEvent.getPillID()).getDose());

                    historyEvent.setOperationStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_OPERATION)));
                    historyEvent.setNotes(getDrugNotesByPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID))));
                    GetHistoryPreferences preference = getHistoryPreferencesForGetHistoryEvent(cursor);
                    historyEvent.setPreferences(preference);
                    if (!Util.isEmptyString(pillName)) {
                        if (null != historyEvent.getPreferences() && historyEvent.getPreferences().isPostponedEventActive()) {
                            historyEventsList.add(historyEvent);
                        }
                    }
                }while (cursor.moveToNext());

            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return historyEventsList;
    }

    public HistoryEvent getLastPostponedHistoryEventForSpecificTime(String pillId, String pillScheduleDate) {
        Cursor cursor = null;
        HistoryEvent historyEvent = null;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_RECENT_POSTPONE_HISTORY_EVENT_FOR_PILLID_AND_TIME,
                    new String[]{PillpopperConstants.ACTION_POST_PONE_PILL, pillId, Util.convertDateLongToIso(pillScheduleDate)});
            if (cursor != null && cursor.moveToFirst()) {
                historyEvent = new HistoryEvent();
                historyEvent.setPillID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID)));
                String pillName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_NAME));
                if (Util.isEmptyString(pillName)) {
                    historyEvent.setPillName(getDrugNameByPillID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID))));
                } else {
                    historyEvent.setPillName(pillName);
                }

                String historyScheduleDate = Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE)));
                if (historyScheduleDate != null && !("-1").equalsIgnoreCase(historyScheduleDate)) {
                    historyEvent.setHeaderTime(historyScheduleDate);
                } else if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)) != null && !("-1").equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)))) {
                    historyEvent.setHeaderTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID) != -1) {
                    historyEvent.setHistoryEventGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID)));
                }

                historyEvent.setDosage(getDrugDoseByPillId(historyEvent.getPillID()).getDose());

                historyEvent.setOperationStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_OPERATION)));
                historyEvent.setNotes(getDrugNotesByPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID))));
                GetHistoryPreferences preference = getHistoryPreferencesForGetHistoryEvent(cursor);
                historyEvent.setPreferences(preference);

            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return historyEvent;
    }

    //setting the scheduleDatePreference value
    // to overDueDate if action is taken from current reminder card
    // to historyScheduleDate if action is taken from historyDetails
    // to ScheduledTime if action is taken from Late reminder card
    private void addHistoryPreferences(String eventGuid, Drug drug, String operation, long postPoneByMins) {
        ContentValues preferencesDataToInsert = new ContentValues();
        preferencesDataToInsert.put(DatabaseConstants.HISTORY_EVENT_GUID, eventGuid);
        preferencesDataToInsert.put(DatabaseConstants.HISTORY_EVENT_DOSAGE_TYPE, drug.getPreferences().getPreference("dosageType"));
        preferencesDataToInsert.put(DatabaseConstants.HISTORY_EVENT_CUSTOM_DESCRIPTION, drug.getPreferences().getPreference("customDescription"));
        preferencesDataToInsert.put(DatabaseConstants.HISTORY_EVENT_MANAGED_DESCRIPTION, drug.getPreferences().getPreference("managedDescription"));
        preferencesDataToInsert.put(DatabaseConstants.HISTORY_RECORD_DATE, drug.getRecordDate());
        preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_SCH_GUID, drug.getScheduleGuid());
        if (null != drug.getActionDate()) {
            preferencesDataToInsert.put(DatabaseConstants.HISTORY_ACTION_DATE, drug.getActionDate());
        }
        try {
            // inserting schedule information to local history preferences db
            if (!AppConstants.SCHEDULE_CHOICE_AS_NEEDED.equalsIgnoreCase(Util.getScheduleChoice(drug))) {
                preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_SCH_DATE,getScheduleDateForPreference(drug));
                preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_SCH_DAY_PERIOD, drug.getSchedule().getDayPeriod());
                preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_SCH_FREQUENCY, drug.getScheduledFrequency());
                preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_WEEKDAYS, drug.getPreferences().getPreference("weekdays"));
            }
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
        }
        try {
            preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_START_DATE,
                    Util.convertDateLongToIso(String.valueOf(PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, Util.getScheduleChoice(drug),drug.getSchedule(), drug.getSchedule().getStart()))));
            preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_END_DATE,
                    Util.convertDateLongToIso(String.valueOf(PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, Util.getScheduleChoice(drug),drug.getSchedule(), drug.getSchedule().getEnd()))));
        } catch (JSONException exception) {
            LoggerUtils.exception(exception.getMessage());
        }
        preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_SCH_TYPE, Util.getScheduleChoice(drug));
        preferencesDataToInsert.put(DatabaseConstants.HISTORY_PREF_SCH_GUID, drug.getScheduleGuid());
        if (operation.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
            preferencesDataToInsert.put(DatabaseConstants.IS_POSTPONED_EVENT_ACTIVE, true);
            preferencesDataToInsert.put(DatabaseConstants.FINAL_POSTPONED_DATE_TIME, Util.convertDateLongToIso(Long.toString(drug._getPostponeTime(postPoneByMins).getGmtSeconds())));
        }

        try {
            databaseHandler.insert(DatabaseConstants.HISTORY_PREFERENCE_TABLE, preferencesDataToInsert);
        } catch (SQLException e) {
            PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
        }
    }
    private Long getScheduleDateForPreference(Drug drug) {
        if (null != drug.getOverdueDate())
            return drug.getOverdueDate().getGmtSeconds();
        else if (null != drug.getHistoryScheduleDate())
            return Long.valueOf(drug.getHistoryScheduleDate());
        else if (null != drug.getScheduledTime())
            return drug.getScheduledTime().getGmtSeconds();
        else {
            return PillpopperTime.now().getGmtSeconds();
        }
    }

    private void addEmptyHistoryEntry(Drug drug, String operation, PillpopperTime dateTaken) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_PILL_NAME, drug.getName());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_GUID, Util.getRandomGuid());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_OPERATION, operation);
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_OPID, getPillHistoryEventOpId());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE, Util.convertDateLongToIso(String.valueOf(dateTaken.getGmtSeconds())));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, dateTaken.getGmtMilliseconds() / 1000);
        dataToInsert.put(DatabaseConstants.PILL_ID, drug.getGuid());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_PERSON_ID, drug.getUserID());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DESCRIPTION, drug.getName() + "  " + operation);
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_EDIT_TIME, System.currentTimeMillis());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DELETED, false);
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_TZ_SEC, String.valueOf(Util.getTzOffsetSecs(TimeZone.getDefault())));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_TZ_NAME, TimeZone.getDefault().getDisplayName());
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_DOSAGE_TYPE, drug.getPreferences().getPreference("dosageType"));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_CUSTOM_DESCRIPTION, drug.getPreferences().getPreference("customDescription"));
        dataToInsert.put(DatabaseConstants.HISTORY_EVENT_MANAGED_DESCRIPTION, drug.getPreferences().getPreference("managedDescription"));

        try {
            databaseHandler.insert(DatabaseConstants.HISTORY_TABLE, dataToInsert);
            addHistoryPreferences(dataToInsert.getAsString(DatabaseConstants.HISTORY_EVENT_GUID), drug, operation, 0);
        } catch (SQLException e) {
            PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
        }
    }

    public void performPostponeDrugs(List<Drug> drugList, long postPoneByMins, Context context, boolean isLogEntryRequired) {
        long finalpostponetime = drugList.get(0).getOverdueDate().getGmtSeconds() + postPoneByMins;
        for (Drug drug : drugList) {
            drug.setIsActionDateRequired(true);
            drug.setRecordDate(Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            drug.setActionDate(Util.convertDateLongToIso(String.valueOf(finalpostponetime)));

            drug.setOpID(getPillHistoryEventOpId());
            if (isEmptyHistoryEventAvailable(drug.getOverdueDate(), drug.getGuid())) {
                deleteEmptyHistoryEvent(drug.getOverdueDate(), drug.getGuid());
            }
            HistoryEvent latestPostponeEvent = getLastPostponedHistoryEventForSpecificTime(drug.getGuid(),String.valueOf(getScheduleDate(drug)));
            if (null != latestPostponeEvent && Util.isOverdueAndPostponedEventSame(drug.getOverdueDate(), latestPostponeEvent.getPreferences().isPostponedEventActive(), latestPostponeEvent.getPreferences().getFinalPostponedDateTime())) {
                updateHistoryEventPreferences(latestPostponeEvent.getHistoryEventGuid(), PillpopperConstants.ACTION_POST_PONE_PILL, drug, postPoneByMins);
                updateNotifyAfterValueForPostPone(postPoneByMins, drug);
                HistoryEditEvent historyEditEvent = getHistoryEditEventDetails(latestPostponeEvent.getHistoryEventGuid());
                addLogEntry(Util.prepareLogEntryForEditHistoryEvent(context, historyEditEvent, PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY), context);
                addLogEntry(Util.prepareLogEntryForAction(PillpopperConstants.ACTION_POST_PONE_PILL, drug, context), context);
            } else {
                addHistoryEntry(context, drug, PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY, PillpopperTime.now(), postPoneByMins);
                try {
                    updateNotifyAfterValueForPostPone(postPoneByMins, drug);
                    // Adding the Log entry event and the corresponding createHistoryEvent for the postpone pill action, which will sync with server using intermediate Sync .
                    if (isLogEntryRequired) {
                        addLogEntry(Util.prepareLogEntryForAction(PillpopperConstants.ACTION_POST_PONE_PILL, drug, context), context);
                        addLogEntry(Util.prepareLogEntryForCreateHistoryEvent(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY, drug, context), context);
                    }
                } catch (SQLException e) {
                    PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
                }
            }
        }
    }

    public void updateNotifyAfterValueForPostPone(long postPoneByMins, Drug drug) {
        drug.setPostponeSeconds(postPoneByMins);
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.NOTIFY_AFTER, Util.convertDateLongToIso(Long.toString(drug._getPostponeTime(postPoneByMins).getGmtSeconds())));
        String where = "PILLID=?";
        try {
            int result = databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{drug.getGuid()});
        } catch (Exception e) {
            PillpopperLog.say("Oops!, Exception while updating the notify after value or preparing the log entry table " + e.getMessage());
        }
    }

    /**
     * Gets the ScheduleDate from the History table
     *
     * @param pillID id of pill
     * @return schedule date
     */
    public String getScheduleDateFromHistory(String pillID) {

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_SCHEDULE_DATE_BY_PILLID_FROM_HISTORY_QUERY, new String[]{pillID});
            if (cursor != null && cursor.moveToLast()) {
                do {
                    return cursor.getString(0);
                } while (cursor.moveToNext());
            }
        } catch (IllegalArgumentException e) {
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Gets the EventDescription value from the History Table
     *
     * @param pillID id of pill
     * @return even description
     */
    public String getEventDescriptionFromHistory(String pillID) {

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_EVENT_DESCRIPTION_BY_PILLID_FROM_HISTORY_QUERY, new String[]{pillID});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    return cursor.getString(0);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Inserts the user information into users table
     *
     * @param user    user object
     * @param context
     */
    public void insertUserData(User user, Context context) {

        databaseHandler.insert(context, DatabaseConstants.USER_TABLE, user, "", "");
        if (!hasUserRemindersPreferenceInDB(user.getUserId())) {
            databaseHandler.insert(context, DatabaseConstants.USER_REMINDERS_TABLE, user, "", "");
        }
    }

    private boolean hasUserRemindersPreferenceInDB(String userId) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.CHECK_USER_REMINDERS_PREF,
                    new String[]{userId});
            count = cursor.getCount();
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return count != 0;
    }

    /**
     * Updates the users enable status as "Y" in user table
     *
     * @param userid provide user id
     */
    public void updateEnableUsersData(String userid) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.ENABLED, "Y");

        String where = "USERID=?";
        try {
            databaseHandler.update(DatabaseConstants.USER_TABLE, dataToInsert, where, new String[]{userid});
        } catch (SQLException e) {
            PillpopperLog.say("Oops!, Exception while updating the selected users as enabled Y" + e.getMessage());
        }

        if (!hasUserRemindersPreferenceInDB(userid)) {
            dataToInsert.clear();
            dataToInsert.put(DatabaseConstants.REMINDERS_ENABLED, "Y");
            try {
                databaseHandler.update(DatabaseConstants.USER_REMINDERS_TABLE, dataToInsert, where, new String[]{userid});
            } catch (SQLException e) {
                PillpopperLog.say("Oops!, Exception while updating the selected users reminders as enabled Y" + e.getMessage());
            }
        }
    }

    /**
     * Gets All the enabled users (Ex : Enabled = "Y")
     *
     * @return list of uesrs
     */
    public List<User> getAllEnabledUsers() {
        List<User> userList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_USERS_QUERY, null);
            addToUserObj(userList, cursor);
        } catch (SQLException e) {
            PillpopperLog.say("Oops!, Exception while getting the users data" + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return userList;
    }

    /**
     * Gets All the users
     *
     * @return list of uesrs
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_MEMBERS_QUERY, null);
            addToUserObj(userList, cursor);
        } catch (SQLException e) {
            PillpopperLog.say("Oops!, Exception while getting the users data" + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return userList;
    }

    private void addToUserObj(List<User> userList, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setUserId(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.USER_ID))));
                user.setUserType(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.USER_TYPE))));
                user.setRelationDesc(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.RELATION_DESC))));
                user.setRelId(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.REL_ID))));
                user.setEnabled(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.ENABLED))));
                user.setNickName(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.NICK_NAME))));
                user.setDisplayName(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.DISPLAY_NAME))));
                user.setFirstName(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.FIRST_NAME))));
                user.setLastName(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.LAST_NAME))));
                user.setMiddleName(Util.isNull(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.MIDDLE_NAME))));
                user.setLastSyncToken(Util.isNull(cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_SYNC_TOKEN))));
                user.setMrn(Util.isNull(cursor.getString(cursor.getColumnIndex(DatabaseConstants.MRN))));
                userList.add(user);
            } while (cursor.moveToNext());
        }
    }

    public List<String> getEnabledUserIds() {
        Cursor cursor = null;
        List<String> userIds = new ArrayList<>();
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_USERS_QUERY, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    userIds.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.USER_ID)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return userIds;
    }

    public List<String> getAllUserIds() {
        Cursor cursor = null;
        List<String> userIds = new ArrayList<>();
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_ALL_USER_IDS_QUERY, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    userIds.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.USER_ID)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return userIds;
    }

    public boolean isEmptyHistoryEventAvailable(PillpopperTime scheduleDate, String pillID) {
        Cursor cursor = null;
        try {
            if (null != scheduleDate) {
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_HISTORY_EVENT_EMPTY_COUNT, new String[]{pillID, Util.convertDateLongToIso(String.valueOf(scheduleDate.getGmtSeconds()))});
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int rowCount = cursor.getInt(0);
                        PillpopperLog.say("Schedule Change : checking history entry :" + PillpopperTime.getDebugString(scheduleDate) + " for PillID : " + pillID + " And count : " + rowCount);
                        return rowCount > 0;
                    } while (cursor.moveToNext());
                }
            }
        } catch (SQLException e) {
            LoggerUtils.exception(e.getMessage());
        } finally {

            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return false;
    }

    public boolean isHistoryEventAvailable(PillpopperTime scheduleDate, String pillID) {

        Cursor cursor = null;
        try {
            if (null != scheduleDate) {
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_HISTORY_EVENT_COUNT, new String[]{pillID, Util.convertDateLongToIso(String.valueOf(scheduleDate.getGmtSeconds()))});
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int rowCount = cursor.getInt(0);
                        return rowCount > 0;
                    } while (cursor.moveToNext());
                }
            }
        } catch (SQLException e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return false;
    }

    public List<HistoryEvent> getHistoryEvents(String userId, String timeStampForComparison) {
        Cursor cursor = null;
        List<HistoryEvent> historyEvents = new ArrayList<>();
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_HISTORY_EVENTS, new String[]{userId});

            if (cursor != null && cursor.moveToFirst()) {
                LoggerUtils.info("History Count in DB - " + cursor.getCount());
                do {
                    HistoryEvent history = new HistoryEvent();
                    history.setPillID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID)));
                    String pillName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_NAME));
                    if (Util.isEmptyString(pillName)) {
                        history.setPillName(getDrugNameByPillID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID))));
                    } else {
                        history.setPillName(pillName);
                    }

                    String historyScheduleDate = Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE)));
                    if (historyScheduleDate != null && !("-1").equalsIgnoreCase(historyScheduleDate)) {
                        history.setHeaderTime(historyScheduleDate);
                    } else if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)) != null && !("-1").equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)))) {
                        historyScheduleDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_CREATIONDATE));
                        history.setHeaderTime(historyScheduleDate);
                    }

                    if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID) != -1) {
                        history.setHistoryEventGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID)));
                    }

                    history.setDosage(getDrugDoseByPillId(history.getPillID()).getDose());

                    history.setOperationStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_OPERATION)));
                    history.setNotes(getDrugNotesByPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID))));
                    GetHistoryPreferences preference = getHistoryPreferencesForGetHistoryEvent(cursor);
                    history.setPreferences(preference);
                    if (!Util.isEmptyString(pillName)) {
                        // to resolve data issue where pillname is recieved as null in getstate and getHistory also
                        if (!cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_OPERATION)).equalsIgnoreCase(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                            if (!cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.HISTORY_EVENT_OPERATION)).equalsIgnoreCase(AppConstants.HISTORY_OPERATION_EMPTY)) {
                                historyEvents.add(history);
                            } else {
                                HistoryEvent latestPostponedHistoryEvent = getLastPostponedHistoryEventForSpecificTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID)),historyScheduleDate);
                                if (null != latestPostponedHistoryEvent) {
                                    if (!Util.isEmptyString(latestPostponedHistoryEvent.getPreferences().getFinalPostponedDateTime()) && !latestPostponedHistoryEvent.getPreferences().getFinalPostponedDateTime().equalsIgnoreCase(Util.convertDateLongToIso(history.getHeaderTime()))) {
                                        historyEvents.add(history);
                                    }
                                } else {
                                    historyEvents.add(history);
                                }
                            }

                        } else {
                            if (null != history.getPreferences() && history.getPreferences().isPostponedEventActive()) {
                                historyEvents.add(history);
                            }
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        LoggerUtils.info("History getHistoryEvents size- " + historyEvents.size());
        return historyEvents;
    }

    private String getDrugNameByPillID(String pillID) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_PILL_NAME_BY_PILL_ID, new String[]{pillID});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    return cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME));
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at isEnabledUser method: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at isEnabledUser method while destroying the cursor object: " + e.getMessage());
                }
            }
        }

        return "";
    }

    public int getDoseHistoryDays() {
        Cursor cursor = null;
        int doseHistoryDays = 30;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_HISTORY_DOSE_DAYS_SETTING,
                    new String[]{getPrimaryUserIdIgnoreEnabled()});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    doseHistoryDays = cursor.getInt(cursor.getColumnIndex(DatabaseConstants.DOSE_HISTORY_DAYS));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return doseHistoryDays == 0 ? 30 : doseHistoryDays; // 0 means something went wrong, so send default value 90
    }

    public HistoryEditEvent getHistoryEditEventDetails(String historyEventGuid) {
        Cursor cursor = null;
        HistoryEditEvent historyEditEvent = null;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_HISTORY_EDIT_SCREEN_DRUG_QUERY,
                    new String[]{historyEventGuid});

            if (cursor != null && cursor.moveToFirst()) {
                historyEditEvent = new HistoryEditEvent();

                if (cursor.getColumnIndex(DatabaseConstants.PILL_ID) != -1) {
                    historyEditEvent.setPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.PILL_NAME) != -1) {
                    historyEditEvent.setPillName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_PERSON_ID) != -1) {
                    historyEditEvent.setPillUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_PERSON_ID)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_OPID) != -1) {
                    historyEditEvent.setPillOperationId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_OPID)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE) != -1) {
                    historyEditEvent.setPillDosageType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DOSAGE_TYPE)));
                    historyEditEvent.setPillDosage(getDrugDoseByPillId(historyEditEvent.getPillId()).getDose());
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_PERSON_ID) != -1) {
                    historyEditEvent.setProxyName(getUserFirstNameByUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_PERSON_ID))));
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID) != -1) {
                    historyEditEvent.setHistoryEventGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID)));
                }

                if (cursor.getColumnIndex((DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE)) != -1) {
                    String historyScheduleDate = Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_SCHEDULE_DATE)));
                    historyEditEvent.setPillScheduleDate(historyScheduleDate);
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_CREATIONDATE) != -1) {
                    historyEditEvent.setPillHistoryCreationDate(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_CREATIONDATE)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_EDIT_TIME) != -1) {
                    historyEditEvent.setPillHistoryEditDate(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_EDIT_TIME)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_OPERATION) != -1) {
                    historyEditEvent.setPillOperation(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_OPERATION)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_DESCRIPTION) != -1) {
                    historyEditEvent.setPillEventDescription(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_DESCRIPTION)));
                }

                if (cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_TZ_SEC) != -1) {
                    historyEditEvent.setTz_secs(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_TZ_SEC)));
                }

                historyEditEvent.setPillImageGuid(getImageGuidByPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID))));

                GetHistoryPreferences preference = getHistoryPreferences(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_GUID)));
                historyEditEvent.setPreferences(preference);

            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }

        return historyEditEvent;
    }

    public void updateHistoryEvent(String guid, String operation, String eventDescription, PillpopperTime creationDate) {
        try {
            ContentValues dataToUpdate = new ContentValues();
            dataToUpdate.put(DatabaseConstants.HISTORY_EVENT_OPERATION, operation);
            dataToUpdate.put(DatabaseConstants.HISTORY_EVENT_DESCRIPTION, eventDescription);
            dataToUpdate.put(DatabaseConstants.HISTORY_EVENT_CREATIONDATE, Long.toString(creationDate.getGmtSeconds()));
            dataToUpdate.put(DatabaseConstants.HISTORY_EVENT_EDIT_TIME, Long.toString(PillpopperTime.now().getGmtSeconds()));
            dataToUpdate.put(DatabaseConstants.HISTORY_EVENT_TZ_SEC, String.valueOf(Util.getTzOffsetSecs(TimeZone.getDefault())));
            dataToUpdate.put(DatabaseConstants.HISTORY_EVENT_TZ_NAME, TimeZone.getDefault().getDisplayName());
            String where = "guid = ?";

            databaseHandler.update(DatabaseConstants.HISTORY_TABLE, dataToUpdate, where, new String[]{guid});

        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        }
    }

    public void updateActionAndRecordDateHistoryPreference(HistoryEditEvent historyEditEvent) {
        try {
            ContentValues dataToUpdate = new ContentValues();
            dataToUpdate.put(DatabaseConstants.HISTORY_ACTION_DATE, historyEditEvent.getPreferences().getActionDate());
            dataToUpdate.put(DatabaseConstants.HISTORY_RECORD_DATE, historyEditEvent.getPreferences().getRecordDate());
            String where = "guid = ?";
            databaseHandler.update(DatabaseConstants.HISTORY_PREFERENCE_TABLE, dataToUpdate, where, new String[]{historyEditEvent.getHistoryEventGuid()});
        } catch (Exception exception) {
            LoggerUtils.exception(exception.getMessage());
        }
    }

    public void updateHistoryEventPreferences(String guid, String currentAction, Drug drug, long postPoneByMins) {
        try {
            ContentValues dataToUpdate = new ContentValues();
            if (null != currentAction && currentAction.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                dataToUpdate.put(DatabaseConstants.IS_POSTPONED_EVENT_ACTIVE, true);
                dataToUpdate.put(DatabaseConstants.FINAL_POSTPONED_DATE_TIME, Util.convertDateLongToIso(Long.toString(drug._getPostponeTime(postPoneByMins).getGmtSeconds())));
            } else {
                dataToUpdate.put(DatabaseConstants.IS_POSTPONED_EVENT_ACTIVE, false);
            }
            String where = "guid = ?";
            databaseHandler.update(DatabaseConstants.HISTORY_PREFERENCE_TABLE, dataToUpdate, where, new String[]{guid});
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        }
    }

    public int getPillHistoryEventCountForToday(String guid) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_HISTORY_EVENT_COUNT_FOR_TODAY,
                    new String[]{guid});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getCount();
            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.error(e.getMessage());
                }
            }
        }
        return 0;
    }

    public void setDoseHistoryDaysForUser(int doseHistoryDays, String userId) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.DOSE_HISTORY_DAYS, doseHistoryDays);
        String where = DatabaseConstants.USER_ID + "=?";
        try {
            int result = databaseHandler.update(DatabaseConstants.USER_PREFERENCE_TABLE, dataToInsert, where, new String[]{userId});
            LoggerUtils.info("Dose History days update result - " + result);
        } catch (Exception ex) {
            LoggerUtils.error("setDoseHistoryDaysForUser" + ex.getMessage());
        }

        //delete the history records
        deleteHistoryRecords(doseHistoryDays, userId);
    }

    private void deleteHistoryRecords(int doseHistoryDays, String userId) {

        Date currentDate = new Date();
        long curDateInDays = TimeUnit.MILLISECONDS.toDays(currentDate.getTime());
        long freqDays = curDateInDays - doseHistoryDays;
        Date dateForComparison = new Date(TimeUnit.DAYS.toMillis(freqDays));
        long mTimeStampForComparison = dateForComparison.getTime() / 1000;
        databaseHandler.delete(DatabaseConstants.HISTORY_TABLE,
                "CAST(" + DatabaseConstants.HISTORY_EVENT_CREATIONDATE + " AS LONG) < ? AND " + DatabaseConstants.USER_ID + "=?",
                new String[]{String.valueOf(mTimeStampForComparison), userId});
    }


    public UserPreferences getUserPreferencesForUser(String userId) {
        Cursor cursor = null;
        UserPreferences userPreferences = null;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_USER_PREFERENCES,
                    new String[]{userId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    userPreferences = new UserPreferences();
                    userPreferences.setUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                    userPreferences.setDoseHistoryStorageDays(cursor.getString(
                            cursor.getColumnIndex(DatabaseConstants.DOSE_HISTORY_DAYS)));
                    if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.EARLY_DOSE_WARNING)) == null) {
                        userPreferences.setPreventEarlyDoseWarningEnabled(true);// default to true
                    } else {
                        if (("1").equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.EARLY_DOSE_WARNING)))) {
                            userPreferences.setPreventEarlyDoseWarningEnabled(true);
                        } else if (("0").equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.EARLY_DOSE_WARNING)))) {
                            userPreferences.setPreventEarlyDoseWarningEnabled(false);
                        }
                    }
                    userPreferences.setNotificationSoundPath(cursor.getString(cursor.getColumnIndex(DatabaseConstants.REMINDER_SOUND_FILENAME)));
                    if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.QUICKVIEW_OPTINED)) == null) {
                        userPreferences.setSignedOutRemindersEnabled(false); // default to false
                    } else {
                        if (("1").equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.QUICKVIEW_OPTINED)))) {
                            userPreferences.setSignedOutRemindersEnabled(true);
                        } else if (("0").equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.QUICKVIEW_OPTINED)))) {
                            userPreferences.setSignedOutRemindersEnabled(false);
                        }
                    }
                    userPreferences.setTz_sec(cursor.getString(cursor.getColumnIndex(DatabaseConstants.TZ_SECS)));
                    userPreferences.setTz_name(cursor.getString(cursor.getColumnIndex(DatabaseConstants.TZ_NAME)));
                    userPreferences.setDstOffset_secs(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DST_OFFSET_SECS)));
                    userPreferences.setRepeatRemindersAfter(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SECONDARY_REMINDER_PERIOD_SECS)));
                    userPreferences.setAndroidReminderSoundFilename(cursor.getString(cursor.getColumnIndex(DatabaseConstants.ANDROID_REMINDER_SOUND_FILENAME)));

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return userPreferences;
    }

    public void setSignedOutReminderEnabled(boolean isChecked, String userId) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.QUICKVIEW_OPTINED, isChecked ? "1" : "0");
        String where = DatabaseConstants.USER_ID + "=?";
        try {
            int result = databaseHandler.update(DatabaseConstants.USER_PREFERENCE_TABLE, dataToInsert, where, new String[]{userId});
            LoggerUtils.info("Signed out reminder update result - " + result);
        } catch (Exception ex) {
            LoggerUtils.error("setSignedOutReminderEnabled" + ex.getMessage());
        }
    }

    public void setNotificationSoundForUser(String reminderSoundName, String userId) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.REMINDER_SOUND_FILENAME, Util.isNull(reminderSoundName));
        dataToInsert.put(DatabaseConstants.ANDROID_REMINDER_SOUND_FILENAME, Util.isNull(reminderSoundName));
        String where = DatabaseConstants.USER_ID + "=?";
        try {
            int result = databaseHandler.update(DatabaseConstants.USER_PREFERENCE_TABLE, dataToInsert, where, new String[]{userId});
            LoggerUtils.info("Reminder Sound update result - " + result);
        } catch (Exception ex) {
            LoggerUtils.error("setNotificationSoundForUser" + ex.getMessage());
        }
    }

    public void setRepeatReminderAfterSecForUser(int repeatRemindersAfter, String userId) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.SECONDARY_REMINDER_PERIOD_SECS, repeatRemindersAfter);
        String where = DatabaseConstants.USER_ID + "=?";
        try {
            int result = databaseHandler.update(DatabaseConstants.USER_PREFERENCE_TABLE, dataToInsert, where, new String[]{userId});
            LoggerUtils.info("Repeat Reminder After update result - " + result);
        } catch (Exception ex) {
            LoggerUtils.error("setRepeatReminderAfterSecForUser" + ex.getMessage());
        }
    }

    public long getSecondaryReminderPeriodSecs(String userId) {
        Cursor cursor = null;
        long remindersInterval = 600;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_SECONDARY_REMINDERS_DATA,
                    new String[]{userId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    remindersInterval = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.SECONDARY_REMINDER_PERIOD_SECS)));
                } while (cursor.moveToNext());
            }
        } catch (NumberFormatException exception) {
            LoggerUtils.exception(exception.getMessage());
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {

            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                LoggerUtils.error(e.getMessage());
            }

        }
        return remindersInterval;

    }

    public void updateQuickviewSelection(String quickviewOptedIn) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.QUICKVIEW_OPTINED, quickviewOptedIn);
        String where = DatabaseConstants.USER_ID + "=?";
        try {
            databaseHandler.update(DatabaseConstants.USER_PREFERENCE_TABLE, dataToInsert, where, new String[]{getPrimaryUserIdIgnoreEnabled()});
        } catch (Exception ex) {
            LoggerUtils.error("updateQuickviewSelection" + ex.getMessage());
        }
    }

    public List<ManageMemberObj> getUsersData() {
        List<ManageMemberObj> members = null;
        ManageMemberObj memberObj = null;
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeQuery(
                    DatabaseConstants.GET_USERS_DATA);
            if (cursor != null && cursor.moveToFirst()) {
                members = new ArrayList<>();
                do {
                    memberObj = new ManageMemberObj();
                    memberObj.setUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                    memberObj.setUserFirstName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME)));
                    memberObj.setMedicationsEnabled(cursor.getString(cursor.getColumnIndex(DatabaseConstants.ENABLED)));
                    memberObj.setRemindersEnabled(cursor.getString(cursor.getColumnIndex(DatabaseConstants.REMINDERS_ENABLED)));
                    memberObj.setUserType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_TYPE)));
                    // if cursor.getString(cursor.getColumnIndex(DatabaseConstants.ISTEEN))) is 1 then the user is teen.
                    memberObj.setTeen("1".equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.ISTEEN))));
                    memberObj.setTeenToggleEnabled("1".equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.ISTEEN_TOGGLE_ENABLED))));
                    members.add(memberObj);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.error(e.getMessage());
                }
            }
        }
        return members;
    }

    public void updateMemberPreferencesToDB(String userId, String medEnabled, String remindersEnabled) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.ENABLED, medEnabled);
        String where = DatabaseConstants.USER_ID + "=?";
        try {
            int result = databaseHandler.update(DatabaseConstants.USER_TABLE, dataToInsert, where, new String[]{userId});
            LoggerUtils.info("updated member info from manage members: userid- " + userId + " medEnabled " + medEnabled);
        } catch (Exception ex) {
            LoggerUtils.error("update member info from manage members " + ex.getMessage());
        }

        dataToInsert.clear();
        dataToInsert.put(DatabaseConstants.REMINDERS_ENABLED, remindersEnabled);
        try {
            int result = databaseHandler.update(DatabaseConstants.USER_REMINDERS_TABLE, dataToInsert, where, new String[]{userId});
            LoggerUtils.info("updated member info from manage members: userid- " + userId + " reminders " + remindersEnabled);
        } catch (Exception ex) {
            LoggerUtils.error("update member info from manage members " + ex.getMessage());
        }
    }


    public String getPrimaryUserIdIgnoreEnabled() {
        Cursor cursor = null;
        String primaryUserID = "";
        try {

            cursor = databaseHandler.executeQuery(
                    DatabaseConstants.GET_PRIMARY_USERID_QUERY);

            if (cursor != null && cursor.moveToFirst()) {
                PillpopperLog.say("--- Primary User UserId Cursor count " + cursor.getCount());
                do {
                    if (cursor.getColumnIndex(DatabaseConstants.USER_ID) != -1) {
                        primaryUserID = cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID));
                    } else {
                        primaryUserID = "";
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getLocalizedMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }

        return primaryUserID;
    }

    public int getDiscontinuedMedicationsCount() {

        Cursor cursor = null;
        int numberOfDiscontinuedMedications = 0;

        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_DISCONTINUED_MEDICATIONS);
            numberOfDiscontinuedMedications = cursor.getCount();
        } catch (Exception e) {
            PillpopperLog.say("Database Util - getDiscontinuedMedications()  - " + e.getMessage());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                PillpopperLog.say("Database Util - getDiscontinuedMedications() - " + e.getMessage());
            }
        }

        return numberOfDiscontinuedMedications;

    }

    public List<DiscontinuedDrug> getDiscontinuedMedications() {

        Cursor cursor = null;
        List<DiscontinuedDrug> discontinuedDrugs = new ArrayList<>();

        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_DISCONTINUED_MEDICATIONS);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DiscontinuedDrug drug = new DiscontinuedDrug();
                    drug.setPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                    drug.setName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
                    if (cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION) != -1) {
                        drug.setDosage(cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION)));
                    } else {
                        drug.setDosage("");
                    }
                    drug.setUserFirstName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME)));
                    drug.setUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                    /*if (cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE) != -1) {
                        drug.setScheduledType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE)));
                    } else {
                        drug.setScheduledType("scheduled");
                    } */
                    discontinuedDrugs.add(drug);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            PillpopperLog.say("Database Util - getDiscontinuedMedications()  - " + e.getMessage());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                PillpopperLog.say("Database Util - getDiscontinuedMedications() - " + e.getMessage());
            }
        }

        return discontinuedDrugs;
    }

    public void acknowledgeDiscontinuedDrugs(List<DiscontinuedDrug> drugList) {

        StringBuilder whereClause = new StringBuilder("PILLID IN (");
        for (int i = 0; i < drugList.size(); i++) {
            if (i > 0) {
                whereClause.append(",");
            }
            whereClause.append("'" + drugList.get(i).getPillId() + "'");
        }
        whereClause.append(")");

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.INVISIBLE, "1");

        databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, contentValues, whereClause.toString(), new String[]{});

    }

    public List<Drug> getAllDrugs(Context context) {
        List<Drug> druglist = new ArrayList<>();
        Cursor cursor = null;
        Drug drug;
        try {

            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_ALL_DRUGS_FOR_KPHC);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    drug = new Drug();
                    drug.setId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID)));
                    drug.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_NAME)));
                    drug.setUserID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.USER_ID)));
                    drug.setPreferecences(getDrugPreferencesForDrugList(cursor));
                    druglist.add(drug);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return druglist;
    }

    public List<User> getAllKPHCUsers(Context context, boolean isNewKPHCNeeded) {
        List<User> userList = null;
        Cursor cursor = null;
        User kphcUser;
        try {
            if (isNewKPHCNeeded)
                cursor = databaseHandler.executeQuery(DatabaseConstants.GET_ALL_NEW_KPHC_DRUGS);
            else
                cursor = databaseHandler.executeQuery(DatabaseConstants.GET_ALL_UPDATED_KPHC_DRUGS);

            if (cursor != null && cursor.moveToFirst()) {
                userList = new ArrayList<>();
                do {
                    kphcUser = new User();
                    kphcUser.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.FIRST_NAME)));
                    kphcUser.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_USER_ID)));
                    userList.add(kphcUser);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return userList;
    }

    public List<Drug> getAllIntervalDrugs(Context context) {
        List<Drug> druglist = new ArrayList<>();
        Cursor cursor = null;
        Drug drug;
        try {

            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_ALL_INTERVAL_DRUGS);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    drug = new Drug();
                    drug.setId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_ID)));
                    drug.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.PILL_NAME)));
                    drug.setUserID(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.USER_ID)));
                    drug.setDose(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.DOSE)));

                    Schedule schedule = new Schedule();
                    schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START)))));
                    schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.END)))));

                    //schedule.setSchedType(getScheduleType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE))));
                    schedule.setDayPeriod(Util.parseJSONNonnegativeLong((cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)))));
                    schedule.setDays((cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS))));

                    drug.setSchedule(schedule);
                    drug.setPreferecences(getDrugPreferencesForDrugList(cursor));

                    drug.setScheduleGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_GUID)));

                    druglist.add(drug);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return druglist;
    }

    public void updateIntervalValueForAsNeededDrug(String guid) {

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.INTERVAL, "0");
        //dataToInsert.put(DatabaseConstants.SCHEDULE_TYPE, "scheduled");
       // ContentValues pillPreferencesData = new ContentValues();
      //  pillPreferencesData.put(DatabaseConstants.SCHEDULE_CHOICE, AppConstants.SCHEDULE_CHOICE_UNDEFINED);
        String where = DatabaseConstants.PILL_ID + "=?";
        try {
         //   databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, pillPreferencesData, where, new String[]{guid});
            int result = databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{guid});
            LoggerUtils.error("updated interval value for drug - " + guid);
        } catch (Exception ex) {
            LoggerUtils.error("failed to update interval value for drug - " + guid + " exception" + ex.getMessage());
        }
    }

    public String getCreationDateFromHistory(String guid) {

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_CREATION_DATE_BY_PILLID_FROM_HISTORY_QUERY, new String[]{guid});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    return cursor.getString(0);
                } while (cursor.moveToNext());
            }
        } catch (IllegalArgumentException e) {
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return null;
    }

    public String getCreationDateByScheduleDateFromHistory(String guid, String scheduleDate) {

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_CREATION_DATE_BY_PILLID_AND_SCHEDULE_DATE_FROM_HISTORY_QUERY, new String[]{guid, Util.convertDateLongToIso(scheduleDate)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    return cursor.getString(0);
                } while (cursor.moveToNext());
            }
        } catch (IllegalArgumentException e) {
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return "";
    }

    public int getEnableUsersMedicationCount() {
        int medicationCount = 0;

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ENABLED_USERS_MEDICATION_COUNT, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    medicationCount = cursor.getInt(cursor.getColumnIndex("count"));
                }
            }
        } catch (IllegalArgumentException e) {
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return medicationCount;
    }

    public String getReminderSoundPathFromDB() {

        Cursor cursor = null;
        String reminderSoundPath = State.REMINDER_SOUND_DEFAULT;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_REMINDERSOUND_PATH,
                    new String[]{getPrimaryUserIdIgnoreEnabled()});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    reminderSoundPath = cursor.getString(cursor.getColumnIndex(DatabaseConstants.REMINDER_SOUND_FILENAME));
                    if (reminderSoundPath == null) {
                        PillpopperLog.say("sound file is not available !");
                        reminderSoundPath = State.REMINDER_SOUND_DEFAULT;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }

        return reminderSoundPath;
    }

    public long updateNotifyAfterValue(String pillId, long notifyAfterValue, long scheduleDate) {

        Cursor cursor = null;
        long dbNotifyAfterValue = -1;

        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_NOTIFY_AFTER_VALUE_BY_PILL_ID_QUERY, new String[]{pillId});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    dbNotifyAfterValue = Long.parseLong(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER))));
                }
            }
        } catch (NumberFormatException exception) {
            LoggerUtils.exception(exception.getMessage());
        } catch (Exception e) {
            PillpopperLog.exception("Exception while updating the notify after value: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }

        if (dbNotifyAfterValue == scheduleDate) {
            updateNotifyAfterValue(pillId, notifyAfterValue);
            return notifyAfterValue;
        } else {
            if (dbNotifyAfterValue > notifyAfterValue) {
                updateNotifyAfterValue(pillId, notifyAfterValue);
                return notifyAfterValue;
            } else {
                if (AppConstants.updateNotifyAfterValue) {
                    updateNotifyAfterValue(pillId, notifyAfterValue);
                    AppConstants.updateNotifyAfterValue = false;
                    return notifyAfterValue;
                }
            }
        }
        return dbNotifyAfterValue;
    }

    public void updateNotifyAfterValue(String pillId, long notifyAfterValue) {

        ContentValues dataToUpdate = new ContentValues();
        String _notifyAfterValue = String.valueOf(notifyAfterValue);
        dataToUpdate.put(DatabaseConstants.NOTIFY_AFTER, Util.convertDateLongToIso(_notifyAfterValue));
        String where = "PILLID=?";
        try {
            databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToUpdate, where, new String[]{pillId});
            PillpopperLog.say("Notify After value is : " + notifyAfterValue);
        } catch (SQLException e) {
            PillpopperLog.exception("Exception While updating the notify after value : " + e.getMessage());
        }
    }

    public void addMissedDoseHistoryEvent(Drug drug, String operation, PillpopperTime operationTime, Context pillpopperActivity) {
        try {
            drug.setScheduledTime(operationTime);
            drug.setOpID(getPillHistoryEventOpId());
            drug.setRecordDate(Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            if (isEmptyHistoryEventAvailable(operationTime, drug.getGuid())) {
                deleteEmptyHistoryEvent(operationTime, drug.getGuid());
            }
            addHistoryEntry_pastReminders(drug, operation, operationTime, 0,pillpopperActivity);
            addLogEntry(Util.prepareLogEntryForCreateHistoryEvent_pastReminders(PillpopperConstants.ACTION_MISS_PILL, drug, pillpopperActivity), pillpopperActivity);
        } catch (Exception e) {
            PillpopperLog.say("Exception While adding the history Events for missed doses");
        }

        if (operationTime != null) {
            updateLastMissedCheck(drug.getGuid(), String.valueOf(operationTime.getGmtSeconds()));
        }
    }

    public boolean isHistoryEventForScheduleAvailable(String timeStamp, String pillId) {
        boolean available = false;
        Cursor cursor = null;
        try {

            String query = String.format(DatabaseConstants.CHECK_HISTORY_EVENT_COUNT_FOR_SCH_TIME, Util.convertDateLongToIso(timeStamp), pillId);
            cursor = databaseHandler.executeQuery(query);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(cursor.getColumnIndex("EventCount"));
                available = count > 0;
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at isHistoryEventForScheduleAvailable method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return available;
    }

    public boolean isHistoryEventForOriginalScheduleRecorded(String timeStamp, String pillId) {
        boolean available = false;
        Cursor cursor = null;
        try {

            String query = String.format(DatabaseConstants.CHECK_HISTORY_EVENT_COUNT_FOR_ORIGINAL_SCH_TIME, Util.convertDateLongToIso(timeStamp), pillId);
            cursor = databaseHandler.executeQuery(query);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(cursor.getColumnIndex("EventCount"));
                available = count > 0;
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at isHistoryEventForScheduleAvailable method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return available;
    }

    public void updateLastMissedCheck(String pillId, String operationTime) {

        ContentValues pillPrefValues = new ContentValues();
        PillpopperLog.say("MissedDoseCheck --- updateLastMissedCheck method : " + Util.convertDateLongToIso(operationTime));
        pillPrefValues.put(DatabaseConstants.MISSED_DOSES_LAST_CHECKED, Util.convertDateLongToIso(operationTime));

        String where = "PILLID=?";
        try {
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, pillPrefValues, where, new String[]{pillId});
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateLastMissedCheck method : " + e.getMessage());
        }
    }

    public void insertPastReminderPillId(String pillID, long time) {

        if (!isEntryAvailableInPastReminder(pillID, new PillpopperTime(time / 1000))) {
            LoggerUtils.info("Debug -- inserting past reminder " + pillID + " pilltime " + time);
            ContentValues dataToInsert = new ContentValues();
            dataToInsert.put(DatabaseConstants.PILL_ID, pillID);
            PillpopperLog.say("inserting DB Utils " + time);
            dataToInsert.put(DatabaseConstants.PILLTIME, time);
            try {
                databaseHandler.insert(DatabaseConstants.PASSED_REMINDERS_TABLE, dataToInsert);
            } catch (Exception e) {
                PillpopperLog.say("Oops!, Exception" + e.getMessage());
            }
//            updateLastMissedCheck(pillID, String.valueOf(time / 1000));
        }
    }

    public List<Drug> getPassedReminderDrugs(Context pillpopperActivity) {
        List<Drug> drugList = new ArrayList<>();
        Cursor cursor = null;
        List<String> pillIdsFromLateReminders = getPillIdFromLateReminders(pillpopperActivity);
        Drug drug;

        for (String pillID : pillIdsFromLateReminders) {
            try {
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_PAST_REMINDERS_QUERY, new String[]{pillID});

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        PillpopperLog.say("Cursor Count " + cursor.getCount());
                        PillpopperLog.say("Cursor Count Inside Do While loop " + cursor.getCount());
                        drug = new Drug();
                        drug.setId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                        drug.setName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
                        drug.setCreated(Util.convertStringtoPillpopperTime(cursor.getString(cursor.getColumnIndex(DatabaseConstants.CREATED))));
                        drug.setNotes(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES)));
                        drug.setLastTaken(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_TAKEN)))));
                        drug.set_effLastTaken(Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.EFF_LAST_TAKEN)))));
                        drug.set_notifyAfter(Util.convertStringtoPillpopperTime(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER))));
                        drug.setPreferecences(getDrugPreferences(cursor));

                        Schedule schedule = new Schedule();
                        schedule.setTimeList(new TimeList(getSchdulesByPillId(drug.getGuid())));

                        schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.START)))));
                        schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.END)))));

                        //schedule.setSchedType(getScheduleType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_TYPE))));
                        schedule.setDayPeriod(Util.parseJSONNonnegativeLong((cursor.getString(cursor.getColumnIndex(DatabaseConstants.DAY_PERIOD)))));
                        schedule.setDays((cursor.getString(cursor.getColumnIndex(DatabaseConstants.WEEKDAYS))));
                        drug.setSchedule(schedule);

                        drug.setUserID(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                        drug.setIsOverdue(cursor.getString(cursor.getColumnIndex(DatabaseConstants.OVERDUE)));
                        drug.setScheduleGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.SCHEDULE_GUID)));
                        drugList.add(drug);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while fetching the pill list" + e.getMessage());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        PillpopperLog.say(e.getMessage());
                    }
                }
            }
        }
        //  PillpopperLog.say("Pending Past Reminders List : " + drugList.size());
        return drugList;
    }

    private List<String> getPillIdFromLateReminders(Context pillpopperActivity) {
        List<String> lateRemindersList = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_PILLID_FROM_LATE_REMINDERS);
            if (cursor != null && cursor.moveToFirst()) {
                lateRemindersList = new ArrayList<>();
                do {
                    lateRemindersList.add(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
//        PillpopperLog.say("PillId from late remidners : " + lateRemindersList.toString());
        return lateRemindersList;
    }

    public boolean isLastActionEventPostpone(String pillId) {
        boolean lastEventPostpone = false;

        Cursor cursor = null;

        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_HISTORY_OPERATION_FOR_NOTIFY_AFTER, new String[]{pillId});

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String historyOperation = cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_OPERATION));
                    if (historyOperation != null) {
                        if (historyOperation.equals(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                            lastEventPostpone = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            PillpopperLog.exception("DatabaseUtils - isLastActionEventPostpone() - " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception("DatabaseUtils - isLastActionEventPostpone(pillId) " + e.getMessage());
                }
            }
        }

        return lastEventPostpone;
    }

    public PillpopperTime getNotifyAfterValue(String pillId) {

        Cursor cursor = null;

        PillpopperTime notifyAfterTime = new PillpopperTime(-1);

        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_NOTIFY_AFTER_VALUE_BY_PILL_ID_QUERY, new String[]{pillId});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        if (cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER) != -1) {
                            long notifyAfterInGmtSeconds = Long.parseLong(Util.convertDateIsoToLong(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER))));
                            notifyAfterTime = new PillpopperTime(notifyAfterInGmtSeconds);
                        }
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            PillpopperLog.exception("DatabaseUtils - getNotifyAfterValue(pillId) " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception("DatabaseUtils - getNotifyAfterValue(pillId) " + e.getMessage());
                }
            }
        }

        return notifyAfterTime;
    }

    public void removeActedPassedReminderFromReminderTable(Context context, String pillID, String pillTime) {

        try {
            databaseHandler.delete(DatabaseConstants.PASSED_REMINDERS_TABLE, "PILLID=? AND PILLTIME=?", new String[]{pillID, pillTime});
            PillpopperLog.say("Removed the pending reminder from the table - " + pillID + " pill time " + pillTime);
        } catch (Exception exception) {
            PillpopperLog.say("Exception while deleting the time from remindet table.");
        }
        updatePendingReminderStatusinSharedPreference(context);
        PillpopperLog.say("At this Moment Past Reminders Count : " + getPassedRemindersCount());
    }

    public void removePillFromPassedReminderTable(Context context, String pillID) {
        try {
            databaseHandler.delete(DatabaseConstants.PASSED_REMINDERS_TABLE, "PILLID=? ", new String[]{pillID});
            PillpopperLog.say("Removed the pending reminder frmo the table");
        } catch (Exception exception) {
            PillpopperLog.say("Exception while deleting the time from remindet table.");
        }
        updatePendingReminderStatusinSharedPreference(context);
        PillpopperLog.say("At this Moment Past Reminders Count : " + getPassedRemindersCount());
    }

    private void updatePendingReminderStatusinSharedPreference(Context context) {
        int pendingReminderCount = getPassedRemindersCount();
        if (pendingReminderCount == 0) {
            PillpopperLog.say("No Pending Passed Reminders");
            SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
            sharedPrefManager.putString("PendingPassedReminders", "0", false);
        } else {
            PillpopperLog.say("Pending Passed Reminders Present");
        }
    }

    private int getPassedRemindersCount() {

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_PAST_REMINDERS_ENTRIES);
            if (cursor.getCount() > 0) {
                return 1;
            }

        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return 0;
    }

    public void removeSchedules(String pillId) {

        Cursor cursor = null;
        try {
            resetDailyDoseLimit(pillId);
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.DELETE_REMINDERS, new String[]{pillId});
            PillpopperLog.say("Removed the pillId reminders " + cursor.getCount());
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        }
    }


    /**
     * This will reset the "maxNumDailyDoses" to -1 when no schedules.
     *
     * @param pillId
     */
    public void resetDailyDoseLimit(String pillId) {
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put("maxNumDailyDoses", "-1");
        dataToInsert.put("missedDosesLastChecked", "-1");
        String where = "PILLID=?";
        try {
            databaseHandler.update("PILLPREFERENCE", dataToInsert, where,
                    new String[]{pillId});

        } catch (SQLException e) {
            LoggerUtils.exception("Exception at resetDailyDoseLimit method : " + e.getMessage());
        }
    }


    public int getPillHistoryEventOpId() {
        int opId = 1;
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeQuery(
                    DatabaseConstants.GET_HISTORY_EVENT_COUNT_FOR_OP_ID);
            if (cursor != null && cursor.moveToFirst()) {
                int nextOpID = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_EVENT_OPID)));
                opId = nextOpID + 1; // next op id
            }
        } catch (Exception e) {
            // If at all any exception occurs the opID will be unique.
            opId = Util.getThreeDigitSecureRandom();
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.error(e.getMessage());
                }
            }
        }
        return opId;
    }

    public void updateUsersLastSyncToken(String userId, String lastSyncToken, boolean hasChanges) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.LAST_SYNC_TOKEN, lastSyncToken);
        contentValues.put(DatabaseConstants.HAS_CHANGES, hasChanges);

        String where = DatabaseConstants.USER_ID + "=?";
        try {
            databaseHandler.update(DatabaseConstants.USER_TABLE, contentValues, where, new String[]{userId});
        } catch (SQLException e) {
            PillpopperLog.exception("Exception at updateLastSyncToken method : " + e.getMessage());
        }
    }

    public String getImageGuidByPillId(String pillId) {

        Cursor cursor = null;
        String imageGuid = null;

        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_IMAGE_GUID_BY_PILL_ID_QUERY, new String[]{pillId});
            if (cursor.moveToFirst()) {
                if (cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID) != -1) {
                    imageGuid = cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID));
                }
            }
        } catch (SQLException e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
        return imageGuid;
    }

    public void setImageGuidByPillId(String pillId, String imageGuid) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.IMAGE_GUID, imageGuid);

        String whereClause = "pillId = ?";

        try {
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, contentValues, whereClause, new String[]{pillId});
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        }
    }

    public boolean isPendingImageRequestAvailable() {

        Cursor cursor = null;

        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_ALL_PENDING_IMAGE_SYNC_REQUESTS_QUERY);
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }

        return false;
    }

    public void insertPendingUploadRequest(String pillId, String imageGuid) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_IMAGE_GUID, imageGuid);
        contentValues.put(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_PILL_ID, pillId);
        contentValues.put(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_NEEDS_UPLOAD, 1);
        contentValues.put(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_NEEDS_DELETE, 0);

        try {
            databaseHandler.insert(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_TABLE, contentValues);
        } catch (SQLException e) {
            LoggerUtils.error(e.getMessage());
        }
    }

    public void insertPendingDeleteRequest(String pillId, String imageGuid) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_IMAGE_GUID, imageGuid);
        contentValues.put(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_PILL_ID, pillId);
        contentValues.put(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_NEEDS_UPLOAD, 0);
        contentValues.put(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_NEEDS_DELETE, 1);

        try {
            databaseHandler.insert(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_TABLE, contentValues);
        } catch (SQLException e) {
            LoggerUtils.error(e.getMessage());
        }
    }

    public List<PendingImageRequest> getAllPendingImageRequests() {

        Cursor cursor = null;
        List<PendingImageRequest> pendingImageRequests = new ArrayList<>();

        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_ALL_PENDING_IMAGE_SYNC_REQUESTS_QUERY);
            if (null != cursor && cursor.moveToFirst()) {
                do {
                    pendingImageRequests.add(PendingImageRequest.getFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }

        return pendingImageRequests;
    }

    public void deletePendingImageRequest(String pillId, String imageGuid, boolean needsUpload, boolean needsDelete) {

        String needsUploadQueryParameter = needsUpload ? "1" : "0";
        String needsDeleteQueryParameter = needsDelete ? "1" : "0";

        String whereClauseForDelete = "_id = ? AND PILLID = ? AND IMAGEGUID = ?";

        int rowId = -1;

        Cursor cursor = null;

        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_FIRST_PENDING_IMAGE_REQUEST_ON_SUCCESS, new String[]{pillId, imageGuid, needsUploadQueryParameter, needsDeleteQueryParameter});
            if (cursor.moveToFirst()) {
                rowId = cursor.getInt(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_ID));
            }

            if (rowId >= 0) {
                databaseHandler.delete(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_TABLE, whereClauseForDelete, new String[]{Integer.toString(rowId), pillId, imageGuid});
            }
        } catch (SQLException e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
    }

    public void performAlreadyTakenDrugs(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context context, boolean isLogEntryRequired) {

        for (Drug drug : overDueDrugList) {
            drug.setIsActionDateRequired(true);
            drug.setRecordDate(Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            drug.setActionDate(Util.convertDateLongToIso(String.valueOf(dateTaken.getGmtSeconds())));
            drug.setOpID(getPillHistoryEventOpId());
            if (isEmptyHistoryEventAvailable(drug.getOverdueDate(), drug.getGuid())) {
                deleteEmptyHistoryEvent(drug.getOverdueDate(), drug.getGuid());
            }
            addHistoryEntry(context, drug, PillpopperConstants.ACTION_TAKE_PILL_HISTORY, dateTaken, 0);
            ContentValues dataToInsert = new ContentValues();
            //ScheduleDate has to be updated for last_take/last field
            if (drug.getOverdueDate() != null) {
                dataToInsert.put(DatabaseConstants.LAST_TAKEN,
                        Util.convertDateLongToIso(String.valueOf(drug.getOverdueDate().getGmtSeconds())));
            }
            dataToInsert.put(DatabaseConstants.OVERDUE, PillpopperConstants.NOT_OVERDUE);
            String where = "PILLID=?";
            try {
                int result = databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{drug.getGuid()});
                // Adding the Log entry event and the corresponding createHistoryEvent for the take pill action, which will sync with server using intermediate Sync .
                if (isLogEntryRequired) {
                    addLogEntry(Util.prepareLogEntryForAction(PillpopperConstants.ACTION_TAKE_PILL, drug, context), context);
                    addLogEntry(Util.prepareLogEntryForCreateHistoryEvent(PillpopperConstants.ACTION_TAKEN_EARLIER, drug, context), context);
                }
            } catch (SQLException e) {
                PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
            }
            if (!isEntryAvailableInPastReminder(drug.getGuid())) {
                updateLastMissedCheck(drug.getGuid(),
                        String.valueOf(drug.getOverdueDate() != null ?
                                drug.getOverdueDate().getGmtSeconds() : drug.getScheduledTime() != null ?
                                drug.getScheduledTime().getGmtSeconds() : PillpopperTime.now().getGmtSeconds()));
            }
        }
    }

    public String isQuickViewEnabledForDBCheck() {

        Cursor cursor = null;
        try {
            if (null != databaseHandler) {
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.QUICKVIEW_FLAG_CHECK_QUERY, new String[]{getPrimaryUserIdIgnoreEnabled()});
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        return cursor.getString(0);
                    } while (cursor.moveToNext());
                }
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at isQuickViewEnabled method: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at isQuickViewEnabled method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return "-2";
    }

    public String getLastSyncTokenForUser(String userId) {
        Cursor cursor = null;
        String lastSyncToken = "";
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_USER_LAST_SYNC_TOKEN,
                    new String[]{userId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    lastSyncToken = cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_SYNC_TOKEN));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception at getLastSyncTokenForUser method : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getLastSyncTokenForUser method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return lastSyncToken;
    }

    public void updateUserdata(Context pillpopperactivity, User user) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.USER_TYPE, user.getUserType());
        contentValues.put(DatabaseConstants.RELATION_DESC, user.getRelationDesc());
        contentValues.put(DatabaseConstants.REL_ID, user.getRelId());
        contentValues.put(DatabaseConstants.ENABLED, user.getEnabled());
        contentValues.put(DatabaseConstants.NICK_NAME, user.getNickName());
        contentValues.put(DatabaseConstants.DISPLAY_NAME, user.getDisplayName());
        contentValues.put(DatabaseConstants.FIRST_NAME, user.getFirstName());
        contentValues.put(DatabaseConstants.LAST_NAME, user.getLastName());
        contentValues.put(DatabaseConstants.MIDDLE_NAME, user.getMiddleName());
        contentValues.put(DatabaseConstants.LAST_SYNC_TOKEN, user.getLastSyncToken());
        contentValues.put(DatabaseConstants.ISTEEN, user.isTeen());
        contentValues.put(DatabaseConstants.GENDER, user.getGenderCode());
        contentValues.put(DatabaseConstants.ISTEEN_TOGGLE_ENABLED, user.isTeenToggleEnabled());

        String where = DatabaseConstants.USER_ID + "=?";
        try {
            databaseHandler.update(DatabaseConstants.USER_TABLE, contentValues, where, new String[]{user.getUserId()});
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateLastSyncToken method : " + e.getMessage());
        }
    }

    public void deleteDataOfUserId(String userId) {
        //delete all records for this userid
        clearDBTableForUser(DatabaseConstants.USER_PREFERENCE_TABLE, userId);
        clearDBTableForUser(DatabaseConstants.PILL_TABLE, userId);
        //pill preferences and pill schedule will be deleted on cascade
    }

    public void clearDBTableForUser(String table, String userId) {

        databaseHandler.delete(table, DatabaseConstants.USER_ID + "=?", new String[]{String.valueOf(userId)});
    }


    public void deleteHistoryEntriesByPillID(String pillId) {

        try {
            databaseHandler.delete(DatabaseConstants.HISTORY_TABLE, DatabaseConstants.PILL_ID + "=?", new String[]{pillId});
            PillpopperLog.say("Deleted History Entries for PillId : " + pillId);
        } catch (SQLException e) {
            PillpopperLog.exception("EXCEPTION: DatabaseUtils -- deletePillFromHistory() -- " + e.toString());
        }
    }

    private String getDrugNotesByPillId(String pillID) {
        Cursor cursor = null;
        String notes = "";
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_NOTES_QUERY,
                    new String[]{pillID});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    notes = cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTES));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception in getting the notes : " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at getting the notes method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return notes;
    }

    public void performNotificationAction(Context context, String tappedAction, List<Drug> overDueDrugsList, PillpopperTime time) {
        for (Drug drug : overDueDrugsList) {
            drug.setRecordDate(Util.convertDateLongToIso(String.valueOf(PillpopperTime.now().getGmtSeconds())));
            drug.setOpID(getPillHistoryEventOpId());
            if (isEmptyHistoryEventAvailable(drug.getScheduledTime(), drug.getGuid())) {
                deleteEmptyHistoryEvent(drug.getScheduledTime(), drug.getGuid());
            }
            if (isHistoryEventAvailable(drug.getScheduledTime(), drug.getGuid())) {
                continue; // DE20678 history already exists for the drug at the same time. continue with the loop
            }
            if (tappedAction.equalsIgnoreCase(NotificationBar.NOTIFICATION_ACTION_TAKE)) {
                LoggerUtils.info("Debug --- adding history for take notification action");
                drug.setIsActionDateRequired(true);
                drug.setActionDate(Util.convertDateLongToIso(String.valueOf(drug.getScheduledTime().getGmtSeconds())));
                addHistoryEntry_pastReminders(drug, PillpopperConstants.ACTION_TAKE_PILL_HISTORY, time, 0,context);
            } else if (tappedAction.equalsIgnoreCase(NotificationBar.NOTIFICATION_ACTION_SKIP)) {
                LoggerUtils.info("Debug --- adding history for skip notification action");
                addHistoryEntry_pastReminders(drug, PillpopperConstants.ACTION_SKIP_PILL_HISTORY, time, 0,context);
            }
            ContentValues dataToInsert = new ContentValues();
            //ScheduleDate has to be updated for last_take/last field
            if (drug.getOverdueDate() != null) {
                dataToInsert.put(DatabaseConstants.LAST_TAKEN,
                        Util.convertDateLongToIso(String.valueOf(drug.getOverdueDate().getGmtSeconds())));
            }
            dataToInsert.put(DatabaseConstants.OVERDUE, PillpopperConstants.NOT_OVERDUE);
            String where = "PILLID=?";
            try {
                databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{drug.getGuid()});
                addLogEntry(Util.prepareLogEntryForAction_pastReminders(tappedAction.equalsIgnoreCase(NotificationBar.NOTIFICATION_ACTION_TAKE) ?
                        PillpopperConstants.ACTION_TAKE_PILL : PillpopperConstants.ACTION_SKIP_PILL, drug, context), context);
                addLogEntry(Util.prepareLogEntryForCreateHistoryEvent_pastReminders(tappedAction.equalsIgnoreCase(NotificationBar.NOTIFICATION_ACTION_TAKE) ?
                        PillpopperConstants.ACTION_TAKE_PILL : PillpopperConstants.ACTION_SKIP_PILL, drug, context), context);

            } catch (SQLException e) {
                PillpopperLog.say("Oops!, Exception while updating the last taken value or preparing the log entry table " + e.getMessage());
            }
        }
        //reset the runtime value of isFromNotificationAction to false
        RunTimeData.getInstance().setFromNotificationAction(false);
    }

    public boolean isUserTimeZoneAvailable(Context context) {
        Cursor cursor = null;
        try {
            // DatabaseHandler handler = DatabaseHandler.getInstance(context);
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_USER_TIME_ZONE);

            if (cursor != null
                    && cursor.moveToFirst()) {
                if (cursor.getString(cursor.getColumnIndex(DatabaseConstants.TZ_NAME)) != null
                        && cursor.getString(cursor.getColumnIndex(DatabaseConstants.TZ_NAME)) != "-1") {
                    return true;
                }
            }

        } catch (Exception e) {
            LoggerUtils.exception("--IsUserTimeZoneAvailable Query--" + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("--Closing the cursor--" + e.getMessage());
                }
            }
        }

        return false;
    }

    public void saveDefaultTimeZoneToDb(Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.TZ_NAME, TimeZone.getDefault().getDisplayName());
        contentValues.put(DatabaseConstants.TZ_SECS, Util.getTzOffsetSecs(TimeZone.getDefault()));

        DatabaseHandler.getInstance(context).insert(DatabaseConstants.USER_PREFERENCE_TABLE, contentValues);
    }

    /**
     * Special Logic
     * Updates last 48 hours events tzSec value with previous timezone, if any history event found tzSec value as null.
     */
    public void updateHistoryOffsetForLast48HourEvents(long tzSecs) {
        //long currentDeviceTimezoneOffset = Util.getTzOffsetSecs(TimeZone.getDefault());
        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.YEAR, PillpopperDay.today().getYear());
        startCal.set(Calendar.MONTH, PillpopperDay.today().getMonth());
        startCal.set(Calendar.DAY_OF_MONTH, PillpopperDay.today().getDay());
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.YEAR, PillpopperDay.getPrevious48HoursLocalDay().getYear());
        endCal.set(Calendar.MONTH, PillpopperDay.getPrevious48HoursLocalDay().getMonth());
        endCal.set(Calendar.DAY_OF_MONTH, PillpopperDay.getPrevious48HoursLocalDay().getDay());
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.HISTORY_EVENT_TZ_SEC, String.valueOf(tzSecs));
        contentValues.put(DatabaseConstants.HISTORY_EVENT_TZ_NAME, TimeZone.getDefault().getDisplayName());
        String where = "scheduledate <=? AND scheduledate >=? AND tz_secs=null";
        try {
            int result = databaseHandler.update(DatabaseConstants.HISTORY_TABLE, contentValues, where, new String[]{String.valueOf(new PillpopperTime(startCal.getTimeInMillis() / 1000).getGmtSeconds()),
                    String.valueOf(new PillpopperTime(endCal.getTimeInMillis() / 1000).getGmtSeconds())});
            PillpopperLog.say("update result : " + result);
        } catch (Exception exception) {
            PillpopperLog.say("Exception while updateHistoryOffsetForLast48HourEvents");
        }

    }

    public Map<String, String> getCreationTimeZoneFromHistory(String guid) {
        Map<String, String> tzParams = null;
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_CREATION_TIME_ZONE_BY_PILLID_FROM_HISTORY_QUERY, new String[]{guid});
            if (cursor != null && cursor.moveToFirst()) {
                tzParams = new HashMap<>();
                do {
                    tzParams.put("tz_secs", cursor.getString(0));
                    tzParams.put("tz_name", cursor.getString(1));
                    return tzParams;
                } while (cursor.moveToNext());
            }
        } catch (IllegalArgumentException e) {
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return null;
    }

    public long getLastHistoryScheduleTimeStamp() {

        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_SCHEDULE_DATE_FROM_HISTORY_QUERY);
            if (cursor != null && cursor.moveToLast()) {
                do {
                    return Long.parseLong(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (IllegalArgumentException e) {
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return -1;
    }


    /**
     * Query to get respective Time Zone offset fields/values for pillid
     *
     * @return
     */
    public HashMap<String, String> getTimeZoneOffsetsFor(String pillId) {
        HashMap<String, String> tzMap = null;
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_TZSECS_BY_PILL_ID_QUERY, new String[]{pillId});
            if (cursor != null && cursor.moveToFirst()) {
                tzMap = new HashMap<>();
                do {
                    tzMap.put(PillpopperConstants.LAST_TAKEN_TZSECS, cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_TAKEN_TZSECS)));
                    tzMap.put(PillpopperConstants.EFF_LAST_TAKEN_TZSECS, cursor.getString(cursor.getColumnIndex(DatabaseConstants.EFF_LAST_TAKEN_TZSECS)));
                    tzMap.put(PillpopperConstants.NOTIFY_AFTER_TZSECS, cursor.getString(cursor.getColumnIndex(DatabaseConstants.NOTIFY_AFTER_TZSECS)));
                    tzMap.put(PillpopperConstants.MISSED_DOSES_LAST_CHECKED_TZSECS, cursor.getString(cursor.getColumnIndex(DatabaseConstants.MISSED_DOSES_LAST_CHECKED_TZSECS)));
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
        return tzMap;
    }

    public void updateScheduleDateChangedTZsecs(String pillid, String tzsecs) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.SCHEDULEDATECHANGED_TZSECS, tzsecs);
        String where = DatabaseConstants.PILL_ID + "=?";
        try {
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, contentValues, where, new String[]{pillid});
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateScheduleDateChangedTZsecs method : " + e.getMessage());
        }
    }
    /*
     * This method will give the drugs based on the KPHC userID

     */

    public List<KphcDrug> getKPHCDrugListByUser(String userId, boolean isNewKPHCNeeded) {
        List<KphcDrug> druglist = new ArrayList<>();
        Cursor cursor = null;
        KphcDrug drug;
        try {
            if (isNewKPHCNeeded)
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ALL_NEW_KPHC_DRUGS_DETAILS, new String[]{userId});
            else
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ALL_UPDATED_KPHC_DRUGS_DETAILS, new String[]{userId});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    drug = new KphcDrug();
                    drug.setDose(cursor.getString(cursor.getColumnIndex(DatabaseConstants.MANAGED_DESCRIPTION)));
                    drug.setPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                    drug.setPillName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_NAME)));
                    drug.setUserName(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME)));
                    drug.setPrescriptionId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PRESCRIPTION_NUM)));
                    drug.setInstruction(cursor.getString(cursor.getColumnIndex(DatabaseConstants.INSTRUCTIONS)));
                    drug.setUserId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                    druglist.add(drug);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("getKPHCDrugListByUser --- >" + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return druglist;
    }

    public void updateKPHCDrugs(List<KphcDrug> drugs) {
        try {
            for (KphcDrug kphcDrug : drugs) {
                String query = String.format(DatabaseConstants.UPDATE_KPHC_DRUG_DETAILS_BY_USER, kphcDrug.getPillId(), kphcDrug.getPillId());
                databaseHandler.executeSQL(query);
            }
        } catch (Exception e) {
            LoggerUtils.exception("getKPHCDrugListByUser --- >" + e.getMessage());
        }
    }

    public boolean isEnabledUser(String userID) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.USER_ENABLED_CHECK_QUERY, new String[]{userID});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    return cursor.getCount() > 0;
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at isEnabledUser method: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at isEnabledUser method while destroying the cursor object: " + e.getMessage());
                }
            }
        }

        return false;
    }


    public boolean isActiveDrug(String pillID, PillpopperTime scheduleTime) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.ACTIVE_DRUG_CHECK_QUERY, new String[]{pillID});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int count = cursor.getInt(cursor.getColumnIndex("EventCount"));
                    return (count > 0 && !isHistoryEventAvailable(scheduleTime, pillID));
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at isEnabledUser method: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at isEnabledUser method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * @param time
     * @return true if an entry for the input time exists in the past reminder table, else false
     */
    public boolean isEntryInPastReminderTable(Long time) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_PAST_REMINDERS_ENTRIES_FOR_TIME, new String[]{String.valueOf(time)});
            return (cursor.getCount() > 0 && isUpdatedReminderOverRecentPastReminder(time));
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return false;
    }

    private boolean isUpdatedReminderOverRecentPastReminder(Long time) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.CHECK_FOR_UPDATED_ENTRY_QUERY);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String pillTime = cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILLTIME));
                    if (null != pillTime) {
                        try {
                            Long updatedPillTime = Long.valueOf(Util.convertDateIsoToLong(pillTime));
                            /*PillpopperLog.say("Existing max time in table: " + PillpopperTime.getDebugString(new PillpopperTime(updatedPillTime/1000)));
                            PillpopperLog.say("Recent time : " + PillpopperTime.getDebugString(new PillpopperTime(time/1000)));*/
                            return time >= updatedPillTime;
                        } catch (Exception e) {
                            PillpopperLog.exception(e.getMessage());
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return false;

    }

    public boolean isEntryAvailableInPastReminder(String pillID, PillpopperTime scheduleTime) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.CHECK_FOR_LATE_REMINDER_ENTRY_QUERY, new String[]{pillID, String.valueOf(scheduleTime.getGmtMilliseconds())});
            return cursor.getCount() > 0;
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return false;
    }


    public void saveFdbImage(Context context, FdbImage fdbImage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.FDB_PILL_ID, fdbImage.getPillId());
        contentValues.put(DatabaseConstants.FDB_IMAGE_ID, fdbImage.getId());
        contentValues.put(DatabaseConstants.FDB_IMAGE_DATA, fdbImage.getImageBytes());
        databaseHandler.insert(DatabaseConstants.FDB_IMAGE_TABLE, contentValues);
    }

    public String getFdbImageByPillId(String pillId) {
        String encodeImage = null;
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_IMAGE_DATA_BY_PILL_ID, new String[]{pillId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    encodeImage = cursor.getString(cursor.getColumnIndex(DatabaseConstants.FDB_IMAGE_DATA));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("getFdbImageByPillId --- >", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return encodeImage;
    }

    public String getCustomImage(String imageId) {
        String encodeImage = null;
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_CUSTOM_IMAGE_DATA_BY_IMAGE_ID, new String[]{imageId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    encodeImage = cursor.getString(cursor.getColumnIndex(DatabaseConstants.CUSTOM_IMAGE_DATA));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("getCustomImageByPillId --- >", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return encodeImage;
    }

    public void saveCustomImage(String imageId, String imageData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.CUSTOM_IMAGE_ID, imageId);
        contentValues.put(DatabaseConstants.CUSTOM_IMAGE_DATA, imageData);
        databaseHandler.insert(DatabaseConstants.CUSTOM_IMAGE_TABLE, contentValues);
    }

    public void updateCustomImage(String imageId, String imageData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.CUSTOM_IMAGE_ID, imageId);
        contentValues.put(DatabaseConstants.CUSTOM_IMAGE_DATA, imageData);
        int result = databaseHandler.update(DatabaseConstants.CUSTOM_IMAGE_TABLE, contentValues, DatabaseConstants.CUSTOM_IMAGE_ID + " = ?", new String[]{imageId});
        if (result != 1) {
            saveCustomImage(imageId, imageData);
        }
    }

    public void deleteCustomImage(String imageId) {
        databaseHandler.delete(DatabaseConstants.CUSTOM_IMAGE_TABLE, DatabaseConstants.CUSTOM_IMAGE_ID, new String[]{imageId});
    }

    public List<KphcDrug> getKPHCDrugsListToFetchFBDImages() {
        List<KphcDrug> drugList = new ArrayList<>();
        Cursor cursor = null;
        KphcDrug drug;
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_ALL_KPHC_DRUGS_TO_FETCH_FDB_IMAGES);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    drug = new KphcDrug();
                    drug.setPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                    drug.setDatabaseNDC(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DATABASE_NDC)));
                    drug.setDefaultImageChoice(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DEFAULT_IMAGE_CHOICE)));
                    drug.setDefaultServiceImageID(cursor.getString(cursor.getColumnIndex(DatabaseConstants.DEFAULT_SERVICE_IMAGE_ID)));
                    drug.setNeedFDBUpdate(cursor.getString(cursor.getColumnIndex(DatabaseConstants.NEED_FDB_UPDATE)));
                    drug.setImageGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_GUID)));
                    drugList.add(drug);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.exception("getKPHCDrugsListToFetchFBDImages --- >", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return drugList;
    }

    public void updatePillImagePreferences(String pillId, String imageChoice, String serviceImageId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.DEFAULT_IMAGE_CHOICE, imageChoice);
        contentValues.put(DatabaseConstants.DEFAULT_SERVICE_IMAGE_ID, serviceImageId);
        String where = "PILLID=?";
        databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, contentValues, where, new String[]{pillId});
    }

    /**
     * check if the FDB image for the given Pill Id is available in the FDB Image table.
     *
     * @param pillId
     * @return true if the FDB image data is available for PillId
     */
    public boolean isFDBImageAvailable(String pillId) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.CHECK_FOR_FDB_IMAGE, new String[]{pillId});
            return cursor.getCount() > 0;
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return false;
    }

    public boolean isCustomImageAvailable(String imageId) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.CHECK_FOR_CUSTOM_IMAGE, new String[]{imageId});
            return cursor.getCount() > 0;
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return false;
    }

    public Drug getDrugForImageLoad(String pillId) {
        {
            Cursor cursor = null;
            Drug drug = new Drug();
            try {
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_DRUG_BY_PILL_ID_QUERY, new String[]{pillId});
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        drug.setId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                        drug.setPreferecences(getDrugPreferences(cursor));
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                PillpopperLog.say("Oops Exception while fetching the pill list");
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        LoggerUtils.exception("Exception at getDrugByPillId method : " + e.getMessage());
                    }
                }
            }
            return drug;
        }
    }

    public void updateNoNeedFDBImageUpdate(String pillId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.NEED_FDB_UPDATE, "false");
        String where = "PILLID=?";
        databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, contentValues, where, new String[]{pillId});
    }

    public void updateMissedDosesLastChecked(List<String> pillIds, String value) {
        if (null != pillIds && !pillIds.isEmpty()) {
            for (String pillId : pillIds) {
                updateLastMissedCheck(pillId, value);
            }
        }
    }

    public boolean isEntryAvailableInPastReminder(String pillID) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.CHECK_FOR_LATE_REMINDER_ENTRY_BY_PILLID_QUERY, new String[]{pillID});
            return cursor.getCount() > 0;
        } catch (SQLException e) {
            PillpopperLog.say(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
            }
        }
        return false;
    }

    public boolean havePendingPastReminders(Context context, String pillID) {

        List<String> pillIdsFromLateReminders = getPillIdFromLateReminders(context);
        return pillIdsFromLateReminders.contains(pillID);

    }

    public void createEntryInHistoryForScheduleTime(List<Drug> drugList, String action, PillpopperTime scheduleTime) {
        for (Drug drug : drugList) {
            if (!isHistoryEventAvailable(scheduleTime, drug.getGuid()) && !isEmptyHistoryEventAvailable(scheduleTime, drug.getGuid())) {
                PillpopperLog.say("Schedule Change : No history entry for :" + PillpopperTime.getDebugString(scheduleTime) + " Time : " + scheduleTime.getGmtSeconds() + " for PillID : " + drug.getGuid());
                addEmptyHistoryEntry(drug, action, scheduleTime);
            }
        }
    }

    public void deleteEmptyHistoryEntriesByPillID(String pillId) {
        try {
            databaseHandler.delete(DatabaseConstants.HISTORY_TABLE, DatabaseConstants.PILL_ID + "=? AND OPERATION == 'EMPTY'", new String[]{pillId});
            PillpopperLog.say("Deleted History Entries for PillId : " + pillId);
        } catch (SQLException e) {
            PillpopperLog.exception("EXCEPTION: DatabaseUtils -- deletePillFromHistory() -- " + e.toString());
        }
    }

    public List<RxRefillUserData> getRxRefillUsersList() {
        List<RxRefillUserData> users = null;
        RxRefillUserData userObj = null;
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeQuery(
                    DatabaseConstants.GET_USERS_DATA);
            if (cursor != null && cursor.moveToFirst()) {
                users = new ArrayList<>();
                do {
                    userObj = new RxRefillUserData(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)),
                            cursor.getString(cursor.getColumnIndex(DatabaseConstants.REL_ID)),
                            cursor.getString(cursor.getColumnIndex(DatabaseConstants.FIRST_NAME)),
                            cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_TYPE)),
                            cursor.getString(cursor.getColumnIndex(DatabaseConstants.LAST_NAME)),
                            cursor.getString(cursor.getColumnIndex(DatabaseConstants.MRN)));
                    users.add(userObj);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.error(e.getMessage());
                }
            }
        }
        return users;
    }

    public boolean isPillEntryAvailableInDB(String pillID) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_PILL_ID_FROM_PILL_TABLE, new String[]{pillID});
            if (cursor != null) {
                return cursor.getCount() > 0;
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception at isPillEntryAvailableInDB method: " + e.getMessage());
        } finally {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                LoggerUtils.exception("Exception at isPillEntryAvailableInDB method while destroying the cursor object: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Insert the failed image entry into database.
     *
     * @param failedImageObj
     */
    public void saveFailedImage(FailedImageObj failedImageObj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstants.PILL_ID, failedImageObj.getPillID());
        contentValues.put(DatabaseConstants.IMAGE_FAILURE_IMAGE_ID, failedImageObj.getImageId());
        contentValues.put(DatabaseConstants.IMAGE_FAILURE_TYPE, failedImageObj.getImageType());
        databaseHandler.insert(DatabaseConstants.IMAGE_FAILURE_ENTRIES_TABLE, contentValues);
    }

    /**
     * Selects all the failed image entry list for retrying
     *
     * @return
     */
    public boolean isTableExists(String tableName) {
        boolean isExist = false;
        try {
            Cursor cursor = databaseHandler.executeQuery("SELECT * FROM " + tableName + " ;");
            if (cursor != null) {
                isExist = true;
                cursor.close();
            }
        } catch (Exception ne) {
            LoggerUtils.exception(ne.getMessage());
        }
        return isExist;
    }

    public List<FailedImageObj> getFailedImageEntryList() {
        List<FailedImageObj> failedImageEntries = new ArrayList<>();
        Cursor cursor = null;
        FailedImageObj failedImageObj;
        try {
            if (null != databaseHandler) {
                cursor = databaseHandler.executeQuery(DatabaseConstants.GET_FAILED_IMAGE_ENTRIES);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        failedImageObj = new FailedImageObj();
                        failedImageObj.setPillID(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID)));
                        failedImageObj.setImageId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_FAILURE_IMAGE_ID)));
                        failedImageObj.setImageType(cursor.getString(cursor.getColumnIndex(DatabaseConstants.IMAGE_FAILURE_TYPE)));
                        failedImageEntries.add(failedImageObj);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            LoggerUtils.exception("getKPHCDrugsListToFetchFBDImages --- >", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return failedImageEntries;
    }

    /**
     * Return True if the entry available with the given PILLID and the serviceID/imageID
     *
     * @param pillID
     * @param imageID
     * @return
     */
    public boolean isEntryAvailableInRetryTable(String pillID, String imageID) {
        Cursor cursor = null;
        try {
            if (null != databaseHandler) {
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ENTRY_FROM_RETRY_TABLE_BY_PILLID_IMAGEID, new String[]{pillID, imageID});
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        return cursor.getCount() > 0;
                    } while (cursor.moveToNext());
                }
            }

        } catch (SQLException e) {
            LoggerUtils.exception("Exception at isEntryAvailableInRetryTable method: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception at isEntryAvailableInRetryTable method while destroying the cursor object: " + e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * Check the entry in the Retry Table and delete the entry.
     *
     * @param pillID
     * @param imageID
     */
    public void deleteEntryFromRetryTable(String pillID, String imageID) {
        try {
            if (null != databaseHandler && isEntryAvailableInRetryTable(pillID, imageID)) {
                String whereClauseForDelete = "PILLID = ? AND IMAGEID = ?";
                databaseHandler.delete(DatabaseConstants.IMAGE_FAILURE_ENTRIES_TABLE, whereClauseForDelete, new String[]{pillID, imageID});
            }
        } catch (SQLException e) {
            PillpopperLog.exception("EXCEPTION: DatabaseUtils -- deleteEntryFromRetryTable() -- " + e.toString());
        }
    }


    /**
     * Returns True if any enabled user has schedules medication.
     *
     * @return
     */
    public boolean isAnyUserEnabledRemindersHasSchedules() {
        Cursor cursor = null;
        List<String> reminderEnabledList = new ArrayList<>();
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_ALL_REMINDERS_ENABLED_USERS);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    reminderEnabledList.add(cursor.getString(cursor.getColumnIndex(DatabaseConstants.USER_ID)));
                } while (cursor.moveToNext());
            }
            return isAnyUserHasScheduledMeds(reminderEnabledList);
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * Returns True if any user has the active medications(i.e Added schedules for atleast one med)
     *
     * @param reminderEnabledList
     * @return
     */
    private boolean isAnyUserHasScheduledMeds(List<String> reminderEnabledList) {
        if (reminderEnabledList.isEmpty()) {
            return false;
        }
        Cursor cursor = null;
        try {
            for (String userID : reminderEnabledList) {
                cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ALL_PILL_IDS_BY_USER, new String[]{userID});
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        if (getScheduleCountByPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID))) >= 1) {
                            return true;
                        }
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
        return false;
    }
    public boolean isAnySchedulesAvailableForUser(String userId) {
        Cursor cursor = null;
        try {
            cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_ALL_PILL_IDS_BY_USER, new String[]{userId});
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        if (getScheduleCountByPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PILL_ID))) >= 1) {
                            return true;
                        }
                    } while (cursor.moveToNext());
                }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
        return false;
    }

    public String getUserGender() {
        Cursor cursor = null;
        String gender = null;
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_USER_GENDER);
            if (null != cursor && cursor.moveToFirst()) {
                gender = cursor.getString(cursor.getColumnIndex(DatabaseConstants.GENDER));
            }

        } catch (SQLException exp) {
            PillpopperLog.exception(exp.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
        return gender;
    }

    public String getUserAge() {
        Cursor cursor = null;
        String age = null;
        try {
            cursor = databaseHandler.executeQuery(DatabaseConstants.GET_USER_AGE);
            if (null != cursor && cursor.moveToFirst()) {
                age = cursor.getString(cursor.getColumnIndex(DatabaseConstants.AGE));
            }

        } catch (SQLException exp) {
            PillpopperLog.exception(exp.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
        return age;
    }

    public void updateScheduleChoice(String scheduleChoice, String pillId) {
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.SCHEDULE_CHOICE, scheduleChoice);

        String where = "PILLID=?";
        try {
            databaseHandler.update(DatabaseConstants.PILL_PREFERENCE_TABLE, dataToInsert, where, new String[]{pillId});
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateScheduleChoice method : " + e.getMessage());
        }
    }

    public void updateScheduleGUID(String scheduleGuid, String pillId) {
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.SCHEDULE_GUID, scheduleGuid);

        String where = "PILLID=?";
        try {
            databaseHandler.update(DatabaseConstants.PILL_TABLE, dataToInsert, where, new String[]{pillId});
        } catch (SQLException e) {
            LoggerUtils.exception("Exception at updateScheduleGUID method : " + e.getMessage());
        }
    }

    public void setTeenUserEnabledFalse(String userId) {
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(DatabaseConstants.ENABLED, "N");

        String where = "USERID=?";
        try {
            databaseHandler.update(DatabaseConstants.USER_TABLE, dataToInsert, where, new String[]{userId});
        } catch (SQLException e) {
            PillpopperLog.say("Oops!, Exception while updating the selected users as enabled N" + e.getMessage());
        }
    }

    public void updatePostponeHistoryEntry(Drug drug) {
        PillpopperTime focusDayAtMidnightForHistory = new PillpopperTime(PillpopperDay.today().atLocalTime(new HourMinute(0, 0)).getGmtSeconds());
        Cursor cursor = databaseHandler.executeRawQuery(DatabaseConstants.GET_HISTORY_EVENTS_SCHEDULE_SCREEN_QUERY,
                new String[]{Util.convertDateLongToIso(Long.toString(focusDayAtMidnightForHistory.getGmtSeconds())), Util.convertDateLongToIso(Long.toString(focusDayAtMidnightForHistory.getGmtSeconds() + 24 * 60 * 60))});
        while (cursor.moveToNext()) {
            ScheduleMainDrug mainDrug = getScheduleMainDrugFromHistoryCursor(cursor);
            if (mainDrug.getHistoryEventAction().equalsIgnoreCase(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                updateNotifyAfterValue(drug.getGuid(), -1);
                updateHistoryEvent(drug.getGuid(), AppConstants.HISTORY_OPERATION_EMPTY, "(" + drug.getDose() + ")", drug.getScheduledTime());
            }
        }
    }
    public GetHistoryPreferences getHistoryPreferencesForGetHistoryEvent(Cursor cursor) {
        GetHistoryPreferences preference = new GetHistoryPreferences();
        try {
                preference.setPostponedEventActive("1".equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.IS_POSTPONED_EVENT_ACTIVE))));
                preference.setFinalPostponedDateTime(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FINAL_POSTPONED_DATE_TIME)));
                preference.setActionDate(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_ACTION_DATE)));
                preference.setRecordDate(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_RECORD_DATE)));
                preference.setDayperiod(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_SCH_DAY_PERIOD)));
                preference.setStart(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_START_DATE)));
                preference.setEnd(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_END_DATE)));
                preference.setScheduleChoice(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_SCH_TYPE)));
                preference.setWeekdays(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_WEEKDAYS)));
                preference.setScheduleFrequency(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_SCH_FREQUENCY)));
                preference.setScheduleGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_SCH_GUID)));
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        }
        return preference;
    }


    public GetHistoryPreferences getHistoryPreferences(String historyGuid) {
        Cursor cursor = null;
        GetHistoryPreferences preference = null;
        try {
            cursor = databaseHandler.executeRawQuery(
                    DatabaseConstants.GET_HISTORY_PREFERENCES,
                    new String[]{historyGuid});
            if (null != cursor && cursor.moveToFirst()) {
                preference = new GetHistoryPreferences();
                preference.setPostponedEventActive("1".equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(DatabaseConstants.IS_POSTPONED_EVENT_ACTIVE))));
                preference.setFinalPostponedDateTime(cursor.getString(cursor.getColumnIndex(DatabaseConstants.FINAL_POSTPONED_DATE_TIME)));
                preference.setActionDate(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_ACTION_DATE)));
                preference.setRecordDate(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_RECORD_DATE)));
                preference.setDayperiod(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_SCH_DAY_PERIOD)));
                preference.setStart(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_START_DATE)));
                preference.setEnd(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_END_DATE)));
                preference.setScheduleChoice(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_SCH_TYPE)));
                preference.setWeekdays(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_WEEKDAYS)));
                preference.setScheduleFrequency(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_SCH_FREQUENCY)));
                preference.setScheduleGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.HISTORY_PREF_SCH_GUID)));
            }
        } catch (Exception e) {
            LoggerUtils.error(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception(e.getMessage());
                }
            }
        }
        return preference;
    }


}