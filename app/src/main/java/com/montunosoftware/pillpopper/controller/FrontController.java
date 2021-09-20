package com.montunosoftware.pillpopper.controller;

import android.content.Context;
import android.os.Bundle;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionsResponse;
import com.montunosoftware.pillpopper.android.util.JsonParserUtility;
import com.montunosoftware.pillpopper.android.util.NotificationBar;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.database.model.ArchiveListDataWrapper;
import com.montunosoftware.pillpopper.database.model.HistoryEditEvent;
import com.montunosoftware.pillpopper.database.model.HistoryEvent;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.database.model.PillList;
import com.montunosoftware.pillpopper.model.ArchiveDetailDrug;
import com.montunosoftware.pillpopper.model.ArchiveListDrug;
import com.montunosoftware.pillpopper.model.BulkSchedule;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.KphcDrug;
import com.montunosoftware.pillpopper.model.ManageMemberObj;
import com.montunosoftware.pillpopper.model.PendingImageRequest;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.ScheduleListItemDataWrapper;
import com.montunosoftware.pillpopper.model.UserPreferences;
import com.montunosoftware.pillpopper.network.model.FailedImageObj;
import com.montunosoftware.pillpopper.service.images.sync.model.FdbImage;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.model.RxRefillUserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author M1023050
 * This class controls the all interactions between UI persistence
 * modules.
 */
public class FrontController {

    private static final FrontController sFrontController = new FrontController();
    private static MyMedAppData sAppData;
    private static AppData appData;

    public static FrontController getInstance(Context context) {
        if (sAppData == null)
            sAppData = MyMedAppData.getInstance(context);
        appData = AppData.getInstance();
        return sFrontController;
    }

    // This will inserts pill/Drug information into database.This needs be called for OTC medication for primary/proxy members.
    public void addMedication(Context context, PillList pillList, String userid) {
        sAppData.addMedication(pillList, userid, context);
    }

    public void updateMedication(PillList pill) {
        sAppData.updateMedication(pill);
    }


    public Drug getDrugByPillId(String pillID) {

        Drug drug = sAppData.getDrugByPillId(pillID);

        return drug;
    }

    public String getFdbImageByPillId(String pillId) {
        return sAppData.getFdbImageByPillId(pillId);
    }

    public void updateMaxDailyDoses(long doses, String pillId) {

        sAppData.updateMaxDailyDoses(doses, pillId);
    }

    public void updateScheduleType(String type, String pillId) {

        sAppData.updateScheduleType(type, pillId);
    }

    public void updateNotes(String notes, String pillId) {

        sAppData.updateNotes(notes, pillId);
    }

    public String getPrimaryUserId() {
        return sAppData.getPrimaryUserId();
    }

    public String getPrimaryUserIdIgnoreEnabled() {
        return sAppData.getPrimaryUserIdIgnoreEnabled();
    }


    public LinkedHashMap<String, List<Drug>> getDrugListByUserId(PillpopperActivity thisActivity, String userId) {
        return sAppData.getDrugListByUserId(thisActivity, userId);
    }

    public List<String> getProxyMemberUserIds() {
        return sAppData.getProxyMemberUserIds();
    }

    public String getUserFirstNameByUserId(String userId) {
        return sAppData.getUserFirstNameByUserId(userId);
    }

    public User getUserById(String userId) {
        return sAppData.getUserById(userId);
    }

    public List<String> getEnabledUserIds() {
        return sAppData.getEnabledUserIds();
    }


    public List<Drug> getDrugsListByUserId(String userID) {
        return sAppData.getDrugsListByUserId(userID);
    }

    public void markDrugAsArchive(String pillId) {
        sAppData.markDrugAsArchive(pillId);
    }

    public void removeDrugFromArchive(Drug drug,Context context,String pillId) {
        sAppData.removeDrugFromArchive(drug,context,pillId);
    }

    public void markDrugAsDeleted(String pillId) {
        sAppData.markDrugAsDeleted(pillId);
    }

    public void updateSchedule(PillpopperActivity _thisActivity, HashMap<String, String> data, List<String> drugIds, List<String> pillTimes) {

        sAppData.updateSchedule(_thisActivity, data, drugIds, pillTimes);
    }

    public int updateSchedule(BulkSchedule data, List<String> drugIds, List<String> pillTimes) {

        return sAppData.updateSchedule(data, drugIds, pillTimes);
    }


    /**
     * Add one log entry in the log entry table.
     *
     * @param context
     * @param entry   log entry object
     */
    public void addLogEntry(Context context, LogEntryModel entry) {
        sAppData.addLogEntry(entry, context);
    }

    public JSONArray getLogEntries(Context context) {
        return sAppData.getLogEntries(context);
    }

    public boolean isLogEntryAvailable() {
        return sAppData.isLogEntryAvailable();
    }

    /**
     * Removes the log entry from the intermediate log entry table based on the replyId
     *
     * @param replyId reply id
     */
    public void removeLogEntry(String replyId) {
        sAppData.removeLogEntry(replyId);
    }


    public JSONArray getSchdulesByPillId(String pillId) {
        return sAppData.getSchdulesByPillId(pillId);
    }

    public ArchiveListDataWrapper getArchiveListData(PillpopperActivity _thisActivity) {
        return sAppData.getArchiveListData(_thisActivity);
    }

    public HashMap<String, ArrayList<ArchiveListDrug>> getArchiveListDataHashMap(final PillpopperActivity _thisActivity) {
        return sAppData.getArchiveListDataHashMap(_thisActivity);
    }

    public ArchiveDetailDrug getArchivedDrugDetails(PillpopperActivity _thisActivity, String pillId) {
        return sAppData.getArchivedDrugDetails(_thisActivity, pillId);
    }


    public List<Drug> getDrugListForOverDue(Context _thisActivity) {
        return sAppData.getDrugListForOverDue(_thisActivity);
    }


    public List<Drug> getDrugListForDue(Context _thisActivity) {
        return sAppData.getDrugListForDue(_thisActivity);
    }

    public String isQuickViewEnabled() {
        return sAppData.isQuickViewEnabled();
    }


    public void updateNewKPHCMed(String pillID, String lastManagedIdNotified) {
        sAppData.updateNewKPHCMed(pillID, lastManagedIdNotified);
    }

    public void performTakeDrug(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context pillpopperActivity, boolean isLogEntryRequired, String source) {
        sAppData.performTakeDrug(overDueDrugList, dateTaken, pillpopperActivity, isLogEntryRequired);
        invokeFireBaseEvent(pillpopperActivity, PillpopperConstants.ACTION_TAKE_PILL, overDueDrugList.size() > 1, source);
    }

    public void performTakeDrug_pastReminders(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context context, boolean isLogEntryRequired, String source) {
        sAppData.performTakeDrug_pastReminders(overDueDrugList, dateTaken, context, isLogEntryRequired);
        invokeFireBaseEvent(context, PillpopperConstants.ACTION_TAKE_PILL, overDueDrugList.size() > 1, source);
    }

    /**
     * Perform the Skip/Skipp All action over the provided drugs
     *
     * @param overDueDrugList    list of drugs
     * @param dateSkipped        date for action
     * @param pillpopperActivity context
     */
    public void performSkipDrug(List<Drug> overDueDrugList, PillpopperTime dateSkipped, Context pillpopperActivity, boolean isLogEntryRequired, String source) {
        sAppData.performSkipDrug(overDueDrugList, dateSkipped, pillpopperActivity, isLogEntryRequired);
        invokeFireBaseEvent(pillpopperActivity, PillpopperConstants.ACTION_SKIP_PILL, overDueDrugList.size() > 1, source);
    }

    public void performSkipDrug_pastReminders(List<Drug> overDueDrugList, PillpopperTime dateSkipped, Context context, boolean isLogEntryRequired, String source) {
        sAppData.performSkipDrug_pastReminders(overDueDrugList, dateSkipped, context, isLogEntryRequired);
        invokeFireBaseEvent(context, PillpopperConstants.ACTION_SKIP_PILL, overDueDrugList.size() > 1, source);
    }

    /**
     * Gets the ScheduleDate from the History table
     *
     * @param pillID pill id
     * @return schedule date
     */
    public String getScheduleDateFromHistory(String pillID) {
        return sAppData.getScheduleDateFromHistory(pillID);
    }

    /**
     * Gets the EventDescription value from the History Table
     *
     * @param pillID pill id
     * @return history event description
     */
    public String getEventDescriptionFromHistory(String pillID) {
        return sAppData.getEventDescriptionFromHistory(pillID);
    }

    /**
     * Inserts the user data into User Table
     *
     * @param context
     * @param user    user object
     */
    public void insertUserData(Context context, User user) {
        sAppData.insertUserData(user, context);
    }

    /**
     * Updates the enabled flag in Users Table.
     *
     * @param userid user id
     */
    public void updateEnableUsersData(String userid) {
        sAppData.updateEnableUsersData(userid);
    }

    /**
     * Gets All the enabled userNames (Ex : Enabled = "Y")
     *
     * @return list of enabled userNames
     */
    public List<User> getAllEnabledUsers() {
        return sAppData.getAllEnabledUsers();
    }

    public boolean isHistoryEventAvailable(PillpopperTime scheduleDate, String pillId) {
        return sAppData.isHistoryEventAvailable(scheduleDate, pillId);
    }

    public List<HistoryEvent> getHistoryEvents(String selectedUserId, String doseHistorydays) {
        return sAppData.getHistoryEvents(selectedUserId, doseHistorydays);
    }

    public HistoryEditEvent getHistoryEditEventDetails(String historyEventGuid) {
        return sAppData.getHistoryEditEventDetails(historyEventGuid);
    }

    public void updateHistoryEvent(String guid, String operation, String eventDescription, PillpopperTime creationDate) {
        sAppData.updateHistoryEvent(guid, operation, eventDescription, creationDate);
    }

    public int getDoseHistoryDays() {
        return sAppData.getDoseHistoryDays();
    }

    /**
     * Perform the Postpone action over the provided drugs
     *
     * @param drugList       list of drugs
     * @param postPoneByMins time to postpone
     * @param context        context
     */
    public void performPostponeDrugs(List<Drug> drugList, long postPoneByMins, Context context, boolean isLogEntryRequired, String source) {
        sAppData.performPostponeDrugs(drugList, postPoneByMins, context, isLogEntryRequired);
        invokeFireBaseEvent(context, PillpopperConstants.ACTION_POST_PONE_PILL, drugList.size() > 1, source);
    }

    public List<ScheduleListItemDataWrapper> getMedicationScheduleForDay(PillpopperActivity _thisActivity, PillpopperDay focusDay) {
        return sAppData.getMedicationScheduleForDay(_thisActivity, focusDay);
    }

    public int getPillHistoryEventCountForToday(String guid) {
        return sAppData.getPillHistoryEventCountForToday(guid);
    }

    public UserPreferences getUserPreferencesForUser(String userId) {
        return sAppData.getUserPreferencesForUser(userId);
    }

    public void setDoseHistoryDaysForUser(int doseHistoryDays, String userId) {
        sAppData.setDoseHistoryDaysForUser(doseHistoryDays, userId);
    }

    public void setSignedOutReminderEnabled(boolean isChecked, String userId) {
        sAppData.setSignedOutReminderEnabled(isChecked, userId);
    }

    public void setNotificationSoundForUser(String reminderSoundName, String userId) {
        sAppData.setNotificationSoundForUser(reminderSoundName, userId);
    }

    public void setRepeatReminderAfterSecForUser(int repeatRemindersAfter, String userId) {
        sAppData.setRepeatReminderAfterSecForUser(repeatRemindersAfter, userId);
    }

    public long getSecondaryReminderPeriodSecs(String userId) {
        return sAppData.getSecondaryReminderPeriodSecs(userId);
    }

    /**
     * Updates the quickview flag in database
     *
     * @param quickviewOptedIn flag for quik view opted
     */

    public void updateQuickviewSelection(String quickviewOptedIn) {
        sAppData.updateQuickviewSelection(quickviewOptedIn);
    }

    public List<ManageMemberObj> getUsersData() {
        return sAppData.getUsersData();
    }

    public void updateMemberPreferencesToDB(String userId, String medEnabled, String remindersEnabled) {
        sAppData.updateMemberPreferencesToDB(userId, medEnabled, remindersEnabled);
    }

    /**
     * This will clear all the tables data.
     */
    public void clearDatabase() {
        sAppData.clearDatabase();
    }

    public void updateAsPendingRemindersPresent(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        sharedPrefManager.putString("PendingPassedReminders", "1", false);
        sharedPrefManager.putBoolean(AppConstants.IS_LAUNCHING_LATE_AFTER_CURRENT, true, false);
    }

    public void updateAsNoPendingReminders(Context context) {

        PillpopperLog.say("NotifiactionISSUE - inside updateAsNoPendingReminders updating as 0");

        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        sharedPrefManager.putString("PendingPassedReminders", "0", false);

        //
        sharedPrefManager.putBoolean(AppConstants.IS_LAUNCHING_LATE_AFTER_CURRENT, false, false);
    }

    public String getPendingRemindersStatus(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        return sharedPrefManager.getString("PendingPassedReminders", "0");
    }

    public int getDiscontinuedMedicationsCount() {
        return sAppData.getDiscontinuedMedicationsCount();
    }

    public List<DiscontinuedDrug> getDiscontinuedMedications() {
        return sAppData.getDiscontinuedMedications();
    }

    public void acknowledgeDiscontinuedDrugs(List<DiscontinuedDrug> drugList) {
        sAppData.acknowledgeDiscontinuedDrugs(drugList);
    }

    public List<Drug> getAllDrugs(Context context) {
        return sAppData.getAllDrugs(context);
    }

    public List<Drug> getAllIntervalDrugs(Context context) {
        return sAppData.getAllIntervalDrugs(context);
    }

    public void updateIntervalValueForAsNeededDrug(String guid) {
        sAppData.updateIntervalValueForAsNeededDrug(guid);
    }

    public String getCreationDateFromHistory(String guid) {
        return sAppData.getCreationDateFromHistory(guid);
    }

    public int getEnableUsersMedicationCount() {
        return sAppData.getEnableUsersMedicationCount();
    }

    public String getReminderSoundPathFromDB() {
        return sAppData.getReminderSoundPathFromDB();
    }

    public long updateNotifyAfterValue(String pillId, long notifyAfterValue, long scheduleDate) {
        return sAppData.updateNotifyAfterValue(pillId, notifyAfterValue, scheduleDate);
    }

    public void updateNotifyAfterValue(String pillId, long notifyAfterValue) {
        sAppData.updateNotifyAfterValue(pillId, notifyAfterValue);
    }

    public void addMissedDoseHistoryEvent(Drug drug, String missPill, PillpopperTime date, Context thisActivity) {
        sAppData.addMissedDoseHistoryEvent(drug, missPill, date, thisActivity);
    }

    public void updateLastMissedCheck(String pillID, String pillTime) {
        sAppData.updateLastMissedCheck(pillID, pillTime);
    }

    public boolean isHistoryEventForScheduleAvailable(String timeStamp, String pillId) {
        return sAppData.isHistoryEventForScheduleAvailable(timeStamp, pillId);
    }

    public void insertPastReminderPillId(String pillID, long time) {
        sAppData.insertPastReminderPillId(pillID, time);
    }

    public List<Drug> getPassedReminderDrugs(Context pillpopperActivity) {
        return sAppData.getPassedReminderDrugs(pillpopperActivity);
    }

    public boolean isLastActionEventPostpone(String pillId) {
        return sAppData.isLastActionEventPostpone(pillId);
    }

    public PillpopperTime getNotifyAfterValue(String pillId) {
        return sAppData.getNotifyAfterValue(pillId);
    }

    public void removeActedPassedReminderFromReminderTable(String pillID, String pillTime, Context _pillPillpopperActivity) {
        sAppData.removeActedPassedReminderFromReminderTable(_pillPillpopperActivity, pillID, pillTime);
    }

    public void removeSchedules(String pillId) {
        sAppData.removeSchedules(pillId);
    }

    public void updateUsersLastSyncToken(String userId, String lastSyncToken, boolean hasChanges) {
        sAppData.updateUsersLastSyncToken(userId, lastSyncToken, hasChanges);
    }

    public void performAlreadyTakenDrugs(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context context, boolean isLogEntryRequired, String source) {
        sAppData.performAlreadyTakenDrugs(overDueDrugList, dateTaken, context, isLogEntryRequired);
        invokeFireBaseEvent(context, PillpopperConstants.ACTION_TAKEN_EARLIER, overDueDrugList.size() > 1,source);
    }

    private void invokeFireBaseEvent(Context context, String action, boolean isMultiDrugAction, String source) {
        if (null != context) {
            String triggeringAction = "";
            if (PillpopperConstants.ACTION_TAKE_PILL.equalsIgnoreCase(action)) {
                triggeringAction = isMultiDrugAction ? FireBaseConstants.ParamValue.TAKEN_ALL : FireBaseConstants.ParamValue.TAKEN;
            } else if (PillpopperConstants.ACTION_SKIP_PILL.equalsIgnoreCase(action)) {
                triggeringAction = isMultiDrugAction ? FireBaseConstants.ParamValue.SKIPPED_ALL : FireBaseConstants.ParamValue.SKIPPED;
            } else if (PillpopperConstants.ACTION_POST_PONE_PILL.equalsIgnoreCase(action)) {
                triggeringAction = FireBaseConstants.ParamValue.REMIND_ME_LATER;
            } else if (PillpopperConstants.ACTION_TAKEN_EARLIER.equalsIgnoreCase(action)) {
                triggeringAction = FireBaseConstants.ParamValue.TAKEN_EARLIER;
            }
            Bundle bundle = new Bundle();
            bundle.putString(FireBaseConstants.ParamName.ACTION_TYPE, triggeringAction);
            if(!Util.isEmptyString(source)) {
                bundle.putString(FireBaseConstants.ParamName.SOURCE, source);
            }
            FireBaseAnalyticsTracker.getInstance().logEvent(context,FireBaseConstants.Event.REMINDER_ACTIONS,bundle);
        }
    }

    public String isQuickViewEnabledForDBCheck() {
        return sAppData.isQuickViewEnabledForDBCheck();
    }

    public void setImageGuidByPillId(String pillId, String imageGuid) {
        sAppData.setImageGuidByPillId(pillId, imageGuid);
    }

    public boolean isPendingImageRequestAvailable() {
        return sAppData.isPendingImageRequestAvailable();
    }

    public void updatePillImage(String pillId, String imageGuid) {
        //Add the request into upload request only if it has the Proper pillID created.
        //Otherwise it leads to the situation where putBlob API call would initiate before createPill API Call.
        if (isPillEntryAvailableInDB(pillId)) {
            sAppData.insertPendingUploadRequest(pillId, imageGuid);
        }
        setImageGuidByPillId(pillId, imageGuid);
    }

    /**
     * Checks the entry Available in pill table or not.
     *
     * @param pillID
     * @return true if entry available else False
     */
    public boolean isPillEntryAvailableInDB(String pillID) {
        return sAppData.isPillEntryAvailableInDB(pillID);
    }

    public void deletePillImage(String pillId, String imageGuid) {
        sAppData.insertPendingDeleteRequest(pillId, imageGuid);
        setImageGuidByPillId(pillId, null);
    }

    public List<PendingImageRequest> getAllPendingImageRequests() {
        return sAppData.getAllPendingImageRequests();
    }

    public void deletePendingImageRequest(String pillId, String imageGuid, boolean needsUpload, boolean needsDelete) {
        sAppData.deletePendingImageRequest(pillId, imageGuid, needsUpload, needsDelete);
    }

    public String getLastSyncTokenForUser(String userId) {
        return sAppData.getLastSyncTokenForUser(userId);
    }

    public void updateUserData(Context pillpopperactivity, User user) {
        sAppData.updateUserData(pillpopperactivity , user);
    }

    public List<String> getAllUserIds() {
        return sAppData.getAllUserIds();
    }

    public void deleteDataOfUserId(String userId) {
        sAppData.deleteDataOfUserId(userId);
    }

    public void deleteHistoryEntriesByPillID(String pillID) {
        sAppData.deleteHistoryEntriesByPillID(pillID);
    }

    public void performNotificationAction(Context context, String tappedAction, List<Drug> overDueDrugsList, PillpopperTime time, String source) {
        sAppData.performNotificationAction(context, tappedAction, overDueDrugsList, time);
        if(!AppConstants.isFromInAppAlerts) {
            invokeFireBaseEvent(context,
                    NotificationBar.NOTIFICATION_ACTION_TAKE.equalsIgnoreCase(tappedAction) ? PillpopperConstants.ACTION_TAKE_PILL : PillpopperConstants.ACTION_SKIP_PILL,
                    false, source);
        }
    }

    public String isTimezoneAdjustmentToServerRequired(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        return sharedPrefManager.getString("TimeZoneAdjustmentToServerRequired", "0");
    }

    public boolean isUserTimeZoneAvailable(Context context) {
        return sAppData.isUserTimeZoneAvailable(context);
    }

    public void saveUserDefaultTimeZone(Context context) {
        sAppData.saveUserDefaultTimeZone(context);
    }


    public void hideLateRemindersWhenFromNotifications(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        sharedPrefManager.putString(AppConstants.LATE_REMINDERS_STATUS_FROM_NOTIFICATION, "1", false);
        LoggerUtils.info("PassedRemindersStatusFromNotifications flag is set to 1 - Hide");
    }

    public void showLateRemindersWhenFromNotifications(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        sharedPrefManager.putString(AppConstants.LATE_REMINDERS_STATUS_FROM_NOTIFICATION, "0", false);
        LoggerUtils.info("PassedRemindersStatusFromNotifications flag is reset to 0 - Show");
    }

    public Map<String, String> getCreationTimeZoneFromHistory(String guid) {
        return sAppData.getCreationTimeZoneFromHistory(guid);
    }

    /**
     * Special Logic
     * Updates last 48 hours events tzSec value with previous timezone, if any history event found tzSec value as null.
     */
    public void updateHistoryOffsetForLast48HourEvents(long tzSecs) {
        sAppData.updateHistoryOffsetForLast48HourEvents(tzSecs);
    }

    public long getLastHistoryScheduleTimeStamp() {
        return sAppData.getLastHistoryScheduleTimeStamp();
    }

    public JSONArray getSchedulesInTimeFormateByPillId(String pillID) {
        return sAppData.getSchedulesInTimeFormateByPillId(pillID);
    }

    public boolean isEnabledUser(String userID) {
        return sAppData.isEnabledUser(userID);
    }

    public boolean isActiveDrug(String pillID, PillpopperTime scheduleTime) {
        return sAppData.isActiveDrug(pillID, scheduleTime);
    }

    public boolean isEntryInPastReminderTable(Long time) {
        return sAppData.isEntryInPastReminderTable(time);
    }

    public boolean isEntryAvailableInPastReminder(String pillID, PillpopperTime scheduleTime) {
        return sAppData.isEntryAvailableInPastReminder(pillID, scheduleTime);
    }

    public void removePillFromPassedReminderTable(Context context, String guid) {
        sAppData.removePillFromPassedReminderTable(context, guid);
    }

    public void saveFdbImage(Context context, FdbImage fdbImage) {
        sAppData.saveFdbImage(context, fdbImage);
    }

    public List<KphcDrug> getKPHCDrugsListToFetchFBDImages() {
        return sAppData.getKPHCDrugsListToFetchFBDImages();
    }

    public void updatePillImagePreferences(String pillId, String imageChoice, String serviceImageId) {
        sAppData.updatePillImagePreferences(pillId, imageChoice, serviceImageId);
    }

    public boolean isFDBImageAvailable(String pillId) {
        return sAppData.isFDBImageAvailable(pillId);
    }

    public void saveCustomImage(String imageId, String imageData) {
        sAppData.saveCustomImage(imageId, imageData);
    }

    public void updateCustomImage(String pillId, String imageGuid, String imageData) {
        sAppData.updateCustomImage(imageGuid, imageData);
        updatePillImage(pillId, imageGuid);
    }

    public String getCustomImage(String imageId) {
        return sAppData.getCustomImage(imageId);
    }

    public void deleteCustomImage(String pillId, String imageId) {
        sAppData.deleteCustomImage(imageId);
        deletePillImage(pillId, imageId);
    }

    public Drug getDrugForImageLoad(String pillID) {
        return sAppData.getDrugForImageLoad(pillID);
    }

    public void updateNoNeedFDBImageUpdate(String pillId) {
        sAppData.updateNoNeedFDBImageUpdate(pillId);
    }

    public void updateMissedDosesLastChecked(List<String> pillIds, String value) {
        sAppData.updateMissedDosesLastChecked(pillIds, value);
    }

    public SecretQuestionsResponse parseSecretQuestionsResponse(Context context, String jsonInput) {
        return sAppData.parseSecretQuestionsResponse(context, jsonInput);
    }

    public JsonParserUtility getJsonParserUtilityInstance(Context context) {
        return sAppData.getJsonParserUtilityInstance(context);
    }

    public void createEntryInHistoryForScheduleTime(List<Drug> drugList, String action, PillpopperTime scheduleTime) {
        sAppData.createEntryInHistoryForScheduleTime(drugList, action, scheduleTime);
    }

    public void deleteEmptyHistoryEntriesByPillID(String pillId) {
        sAppData.deleteEmptyHistoryEntriesByPillID(pillId);
    }

    public List<RxRefillUserData> getRxRefillUsersList() {
        return sAppData.getRxRefillUsersList();
    }

    public String getMissedDosesLastCheckedValue(String pillId) {
        return sAppData.getMissedDosesLastCheckedValue(pillId);
    }

    public String getAccessToken(Context context) {
        return appData.getAccessToken(context);
    }

    public String getRefreshToken(Context context) {
        return appData.getRefreshToken(context);
    }

    public String getTokenExpiryTime(Context context){
        return appData.getTokenExpiryTime(context);
    }

    public void saveFailedImage(FailedImageObj failedImageObj) {
        sAppData.saveFailedImage(failedImageObj);
    }

    public List<FailedImageObj> getFailedImageEntryList() {
        return sAppData.getFailedImageEntryList();
    }

    public void deleteEntryFromRetryTable(String pillID, String imageID) {
        sAppData.deleteEntryFromRetryTable(pillID, imageID);
    }

    public String getUniqueRandomIdForGA(Context context) {
        return sAppData.getUniqueRandomIdForGA(context);
    }

    public JSONArray getNonSecureLogEntries(Context context) {
        return sAppData.getNonSecureLogEntries(context);
    }

    public String getDosageTypeByPillID(String pillID) {
        return sAppData.getDosageTypeByPillID(pillID);
    }

    public String getCreationDateByScheduleDateFromHistory(String guid, String scheduleDate) {
        return sAppData.getCreationDateByScheduleDateFromHistory(guid, scheduleDate);
    }

    public void disableBatteryOptimizationAlert(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        sharedPrefManager.putBoolean(AppConstants.BATTERY_OPTIMIZATION_DECISION, false, false);
    }

    public boolean isBatteryOptmizationDecisionNotOpted(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        return sharedPrefManager.getBoolean(AppConstants.BATTERY_OPTIMIZATION_DECISION, true);
    }

    public boolean isAnyUserEnabledRemindersHasSchedules() {
        return sAppData.isAnyUserEnabledRemindersHasSchedules();
    }

    public void disableBatteryOptimizationCard(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        sharedPrefManager.putBoolean(AppConstants.SHOW_BATTERY_OPTIMIZATION_CARD, false, false);
    }

    public boolean showBatteryOptimizationCard(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        return sharedPrefManager.getBoolean(AppConstants.SHOW_BATTERY_OPTIMIZATION_CARD, true);
    }

    public String getUserGender() {
        return sAppData.getUserGender();
    }

    public String getUserAge() {
        return sAppData.getUserAge();
    }

    public String getLocalNonSecureUrl(Context context) {
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);
        return sharedPrefManager.getString(AppConstants.KEY_PILL_POPPER_NON_SECURED_BASE_URL, "");
    }

    public void updateScheduleChoice(String scheduleChoice, String pillId) {
        sAppData.updateScheduleChoice(scheduleChoice,pillId);
    }

    public void updatePostponeHistoryAvailable(Drug drug){
        sAppData.updatePostponeHistoryAvailable(drug);
    }
    public boolean isTableExist(String tableName) {
        return sAppData.isTableExist(tableName);
    }

    public void updateActionAndRecordDateHistoryPreference(HistoryEditEvent mHistoryEditEvent) {
        sAppData.updateActionAndRecordDateHistoryPreference(mHistoryEditEvent);
    }

    public void updateHistoryEventPreferences(String historyEventGuid, String pillOperation, Drug drug, long postponeSeconds) {
        sAppData.updateHistoryEventPreferences(historyEventGuid,pillOperation,drug,postponeSeconds);
    }

    public boolean isAnySchedulesAvailableForUser(String mSelectedUserId) {
       return sAppData.isAnySchedulesAvailableForUser(mSelectedUserId);
    }

    public void updateScheduleGUID(String scheduleGuid, String pillId) {
        sAppData.updateScheduleGUID(scheduleGuid,pillId);
    }

    public List<HistoryEvent> getActivePostponedEvents(String pillId) {
        return sAppData.getActivePostponedEvents(pillId);
    }

    public void performSkipDrugForQuickView(List<Drug> overDueDrugList, PillpopperTime dateSkipped, Context pillpopperActivity, boolean isLogEntryRequired, String source,String text) {
        sAppData.performSkipDrug(overDueDrugList, dateSkipped, pillpopperActivity, isLogEntryRequired);
        invokeFireBaseEventForQuickView(pillpopperActivity, PillpopperConstants.ACTION_SKIP_PILL, overDueDrugList.size() > 1, source,text);
    }

    public void performTakeDrugForQuickView(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context pillpopperActivity, boolean isLogEntryRequired, String source,String text) {
        sAppData.performTakeDrug(overDueDrugList, dateTaken, pillpopperActivity, isLogEntryRequired);
        invokeFireBaseEventForQuickView(pillpopperActivity, PillpopperConstants.ACTION_TAKE_PILL, overDueDrugList.size() > 1, source,text);
    }

    public void performAlreadyTakenDrugsForQuickView(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context context, boolean isLogEntryRequired, String source,String text) {
        sAppData.performAlreadyTakenDrugs(overDueDrugList, dateTaken, context, isLogEntryRequired);
        invokeFireBaseEventForQuickView(context, PillpopperConstants.ACTION_TAKEN_EARLIER, overDueDrugList.size() > 1,source,text);
    }

    public void performSkipDrug_pastRemindersForQuickView(List<Drug> overDueDrugList, PillpopperTime dateSkipped, Context context, boolean isLogEntryRequired, String source,String text) {
        sAppData.performSkipDrug_pastReminders(overDueDrugList, dateSkipped, context, isLogEntryRequired);
        invokeFireBaseEventForQuickView(context, PillpopperConstants.ACTION_SKIP_PILL, overDueDrugList.size() > 1, source,text);
    }

    public void performTakeDrug_pastRemindersForQuickView(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context context, boolean isLogEntryRequired, String source,String text) {
        sAppData.performTakeDrug_pastReminders(overDueDrugList, dateTaken, context, isLogEntryRequired);
        invokeFireBaseEventForQuickView(context, PillpopperConstants.ACTION_TAKE_PILL, overDueDrugList.size() > 1, source,text);
    }

    private void invokeFireBaseEventForQuickView(Context context, String action, boolean isMultiDrugAction, String source,String text) {
        try {
            if (null != context && !Util.isEmptyString(text)) {
                String triggeringAction = "";
                if (PillpopperConstants.ACTION_TAKE_PILL.equalsIgnoreCase(action)) {
                    if (text.equalsIgnoreCase(context.getResources().getString(R.string.take))) {
                        triggeringAction = FireBaseConstants.ParamValue.TAKEN;
                    } else if (text.equalsIgnoreCase(context.getResources().getString(R.string.reminder_taken_all))) {
                        triggeringAction = FireBaseConstants.ParamValue.TAKEN_ALL;
                    } else if (text.equalsIgnoreCase(context.getResources().getString(R.string.take_the_rest_btn))) {
                        triggeringAction = FireBaseConstants.ParamValue.TAKE_THE_REST;
                    } else {
                        triggeringAction = isMultiDrugAction ? FireBaseConstants.ParamValue.TAKEN_ALL : FireBaseConstants.ParamValue.TAKEN;
                    }
                } else if (PillpopperConstants.ACTION_SKIP_PILL.equalsIgnoreCase(action)) {
                    if (text.equalsIgnoreCase(context.getResources().getString(R.string.skipped))) {
                        triggeringAction = FireBaseConstants.ParamValue.SKIPPED;
                    } else if (text.equalsIgnoreCase(context.getResources().getString(R.string.reminder_skip_all))) {
                        triggeringAction = FireBaseConstants.ParamValue.SKIPPED_ALL;
                    } else if (text.equalsIgnoreCase(context.getResources().getString(R.string.skip_the_rest_btn))) {
                        triggeringAction = FireBaseConstants.ParamValue.SKIP_THE_REST;
                    } else {
                        triggeringAction = isMultiDrugAction ? FireBaseConstants.ParamValue.SKIPPED_ALL : FireBaseConstants.ParamValue.SKIPPED;
                    }
                } else if (PillpopperConstants.ACTION_POST_PONE_PILL.equalsIgnoreCase(action)) {
                    triggeringAction = FireBaseConstants.ParamValue.REMIND_ME_LATER;
                } else if (PillpopperConstants.ACTION_TAKEN_EARLIER.equalsIgnoreCase(action)) {
                    if (text.equalsIgnoreCase(context.getResources().getString(R.string.taken_the_rest_earlier_btn))) {
                        triggeringAction = FireBaseConstants.ParamValue.TAKEN_THE_REST_EARLIER;
                    } else {
                        triggeringAction = FireBaseConstants.ParamValue.TAKEN_EARLIER;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putString(FireBaseConstants.ParamName.ACTION_TYPE, triggeringAction);
                if (!Util.isEmptyString(source)) {
                    bundle.putString(FireBaseConstants.ParamName.SOURCE, source);
                }
                FireBaseAnalyticsTracker.getInstance().logEvent(context, FireBaseConstants.Event.REMINDER_ACTIONS, bundle);
            }
        }catch (Exception e){
            LoggerUtils.exception("Exception while triggering firebase event: "+e.getMessage());
        }
    }
}