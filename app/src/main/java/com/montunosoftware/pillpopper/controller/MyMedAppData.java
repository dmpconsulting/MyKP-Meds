package com.montunosoftware.pillpopper.controller;

import android.content.Context;

import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionsResponse;
import com.montunosoftware.pillpopper.android.util.JsonParserUtility;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.database.model.ArchiveListDataWrapper;
import com.montunosoftware.pillpopper.database.model.HistoryEditEvent;
import com.montunosoftware.pillpopper.database.model.HistoryEvent;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.database.model.PillList;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.model.RxRefillUserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;


/**
 * @author M1023050
 * This class maintains the data related to application and provides
 * methods to populate/access the data.
 */
public class MyMedAppData {

    private static DatabaseUtils dataBaseUtil;
    private static final MyMedAppData appData = new MyMedAppData();

    private MyMedAppData() {
    }

    /**
     * @param context context
     * @return instance of MyMedAppData
     */
    public static MyMedAppData getInstance(Context context) {
        if(dataBaseUtil==null)
        dataBaseUtil = DatabaseUtils.getInstance(context);
        return appData;
    }

    public LinkedHashMap<String, Drug> getDrugList() {
        return dataBaseUtil.getDrugList();
    }


    public void addMedication(PillList pillList, String userid, Context context) {
        dataBaseUtil.addMedication(context, pillList, userid);
    }

    public void updateMedication(PillList pill) {
        dataBaseUtil.updateMedication(pill);
    }


    public Drug getDrugByPillId(String pillID) {

        return dataBaseUtil.getDrugByPillId(pillID);
    }

    public String getFdbImageByPillId(String pillId){
        return dataBaseUtil.getFdbImageByPillId(pillId);
    }

    public void updateMaxDailyDoses(long doses, String pillId) {
        dataBaseUtil.updateMaxDailyDoses(doses, pillId);
    }

    public void updateScheduleType(String type, String pillId) {

        dataBaseUtil.updateScheduleType(type, pillId);
    }

    public void updateNotes(String notes, String pillId) {

        dataBaseUtil.updateNotes(notes, pillId);
    }

    public String getPrimaryUserId() {
        return dataBaseUtil.getPrimaryUserId();
    }

    public String getPrimaryUserIdIgnoreEnabled() {
        return dataBaseUtil.getPrimaryUserIdIgnoreEnabled();
    }

    public LinkedHashMap<String, List<Drug>> getDrugListByUserId(PillpopperActivity thisActivity, String userId) {
        return dataBaseUtil.getDrugListByUser(thisActivity, userId);
    }

    public List<String> getEnabledUserIds() {
        return dataBaseUtil.getEnabledUserIds();

    }

    public List<String> getProxyMemberUserIds() {
        return dataBaseUtil.getProxyMemberUserIds();
    }

    public String getUserFirstNameByUserId(String userId) {
        return dataBaseUtil.getUserFirstNameByUserId(userId);
    }

    public User getUserById(String userId) {
        return dataBaseUtil.getUserById(userId);
    }

    public void markDrugAsArchive(String pillId) {
        dataBaseUtil.markDrugAsArchive(pillId);
    }

    public void removeDrugFromArchive(Drug drug,Context context,String pillId) {
        dataBaseUtil.removeDrugFromArchive(drug,context,pillId);
    }

    public void markDrugAsDeleted(String pillId) {
        dataBaseUtil.markDrugAsDeleted(pillId);
    }

    public List<Drug> getDrugsListByUserId(String userID) {
        return dataBaseUtil.getDrugsListByUserId(userID);
    }

    public void updateSchedule(PillpopperActivity _thisActivity, HashMap<String, String> data, List<String> drugIds, List<String> pillTimes) {

        dataBaseUtil.updateSchedule(_thisActivity, data, drugIds, pillTimes);
    }

    public int updateSchedule(BulkSchedule data, List<String> drugIds, List<String> pillTimes) {

        return dataBaseUtil.updateSchedule(data, drugIds, pillTimes);
    }

    public void addLogEntry(LogEntryModel entry, Context context) {
        dataBaseUtil.addLogEntry(entry, context);
    }

    /**
     * Returns the list of objects which are needs to be synced to the server as a part of the intermediate Sync.
     *
     * @return list of objects
     */
    public JSONArray getLogEntries(PillpopperActivity _pillpopperActivity) {
        return dataBaseUtil.getLogEntries(_pillpopperActivity);
    }

    public JSONArray getLogEntries(Context context) {
        return dataBaseUtil.getLogEntries(context);
    }

    public JSONArray getNonSecureLogEntries(Context context) {
        return dataBaseUtil.getNonSecureLogEntries(context);
    }

    public boolean isLogEntryAvailable() {

        return dataBaseUtil.isLogEntryAvailable();
    }

    /**
     * Returns TRUE if the provided user is primary member else FALSE.
     *
     * @param userId user id
     * @return       user is primary or not
     */
    public boolean isPrimaryUser(String userId) {
        return dataBaseUtil.isPrimaryUser(userId);
    }

    public void removeLogEntry(String replyId) {
        dataBaseUtil.removeLogEntry(replyId);
    }

    public JSONArray getSchdulesByPillId(String pillId) {
        return dataBaseUtil.getSchdulesByPillId(pillId);
    }

    public ArchiveListDataWrapper getArchiveListData(PillpopperActivity _thisActivity) {
        return dataBaseUtil.getArchiveListData(_thisActivity);
    }

    public HashMap<String, ArrayList<ArchiveListDrug>> getArchiveListDataHashMap(final PillpopperActivity _thisActivity) {
        return dataBaseUtil.getArchiveListDataHashMap(_thisActivity);
    }

    public ArchiveDetailDrug getArchivedDrugDetails(PillpopperActivity _thisActivity, String pillId) {
        return dataBaseUtil.getArchivedDrugDetails(_thisActivity, pillId);
    }

    public List<Drug> getDrugListForOverDue(Context thisActivity) {
        return dataBaseUtil.getDrugListForOverDue(thisActivity);
    }

    public String isQuickViewEnabled() {
        return dataBaseUtil.isQuickViewEnabled();
    }

    public void updateNewKPHCMed(String pillID, String lastManagedIdNotified) {
        dataBaseUtil.updateNewKPHCMed(pillID, lastManagedIdNotified);
    }

    public void performTakeDrug(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context pillpopperActivity, boolean isLogEntryRequired) {
        dataBaseUtil.performTakeDrug(overDueDrugList, dateTaken, pillpopperActivity, isLogEntryRequired);
    }

    public void performTakeDrug_pastReminders(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context context, boolean isLogEntryRequired) {
        dataBaseUtil.performTakeDrug_pastReminders(overDueDrugList, dateTaken, context,isLogEntryRequired);
    }


    /**
     * Perform the Skip/Skipp All action over the provided drugs
     * @param overDueDrugList list of drugs
     * @param dateSkipped      date for action
     * @param pillpopperActivity pillpopperActivity context
     */
    public void performSkipDrug(List<Drug> overDueDrugList, PillpopperTime dateSkipped, Context pillpopperActivity, boolean isLogEntryRequired) {
        dataBaseUtil.performSkipDrug(overDueDrugList, dateSkipped, pillpopperActivity, isLogEntryRequired);
    }

    public void performSkipDrug_pastReminders(List<Drug> overDueDrugList, PillpopperTime dateSkipped, Context context, boolean isLogEntryRequired) {
        dataBaseUtil.performSkipDrug_pastReminders(overDueDrugList, dateSkipped, context, isLogEntryRequired);
    }
    /**
     * Perform the Take/Take All action over the provided drugs
     * @param drugList list of drugs
     * @param postPoneByMins time to postpone
     * @param pillpopperActivity pillpopperActivity context
     */
    public void performPostponeDrugs(List<Drug> drugList, long postPoneByMins, Context pillpopperActivity, boolean isLogEntryRequired) {
        dataBaseUtil.performPostponeDrugs(drugList, postPoneByMins, pillpopperActivity, isLogEntryRequired);
    }

    /**
     * Gets the ScheduleDate from the History table
     * @param pillID pill id
     * @return schedule date
     */
    public String getScheduleDateFromHistory(String pillID) {
        return dataBaseUtil.getScheduleDateFromHistory(pillID);
    }

    /**
     * Gets the EventDescription value from the History Table
     * @param pillID pill id
     * @return history event description
     */
    public String getEventDescriptionFromHistory(String pillID) {
        return dataBaseUtil.getEventDescriptionFromHistory(pillID);
    }

    /**
     * Inserts the user data into User Table
     * @param user user object
     * @param context
     */
    public void insertUserData(User user, Context context) {
        dataBaseUtil.insertUserData(user, context);
    }

    /**
     * Updates the enabled flag in Users Table.
     * @param userid user id
     */
    public void updateEnableUsersData(String userid) {
        dataBaseUtil.updateEnableUsersData(userid);
    }

    /**
     * Gets All the enabled userNames (Ex : Enabled = "Y")
     * @return list of userNames
     */
    public List<User> getAllEnabledUsers() {
        return dataBaseUtil.getAllEnabledUsers();
    }

    public boolean isHistoryEventAvailable(PillpopperTime scheduleDate, String pillID) {
        return dataBaseUtil.isHistoryEventAvailable(scheduleDate, pillID);
    }

    public List<HistoryEvent> getHistoryEvents(String selectedUserId, String doseHistorydays) {
        return dataBaseUtil.getHistoryEvents(selectedUserId, doseHistorydays);
    }

    public HistoryEditEvent getHistoryEditEventDetails(String historyEventGuid) {
        return dataBaseUtil.getHistoryEditEventDetails(historyEventGuid);
    }

    public void updateHistoryEvent(String guid, String operation, String eventDescription, PillpopperTime creationDate) {
        dataBaseUtil.updateHistoryEvent(guid, operation, eventDescription, creationDate);
    }

    public int getDoseHistoryDays() {
        return dataBaseUtil.getDoseHistoryDays();
    }


    public List<ScheduleListItemDataWrapper> getMedicationScheduleForDay(PillpopperActivity _thisActivity, PillpopperDay focusDay) {
        return dataBaseUtil.getMedicationScheduleForDay(_thisActivity, focusDay);
    }

    public int getPillHistoryEventCountForToday(String guid) {
        return dataBaseUtil.getPillHistoryEventCountForToday(guid);
    }

    public UserPreferences getUserPreferencesForUser(String userId) {
        return dataBaseUtil.getUserPreferencesForUser(userId);
    }

    public void setSignedOutReminderEnabled(boolean isChecked, String userId) {
        dataBaseUtil.setSignedOutReminderEnabled(isChecked, userId);
    }

    public void setDoseHistoryDaysForUser(int doseHistoryDays, String userId) {
        dataBaseUtil.setDoseHistoryDaysForUser(doseHistoryDays, userId);
    }

    public void setNotificationSoundForUser(String reminderSoundName, String userId) {
        dataBaseUtil.setNotificationSoundForUser(reminderSoundName, userId);
    }

    public void setRepeatReminderAfterSecForUser(int repeatRemindersAfter, String userId) {
        dataBaseUtil.setRepeatReminderAfterSecForUser(repeatRemindersAfter, userId);
    }

    public long getSecondaryReminderPeriodSecs(String userId) {
        return dataBaseUtil.getSecondaryReminderPeriodSecs(userId);
    }

    public void updateQuickviewSelection(String quickviewOptedIn) {
        dataBaseUtil.updateQuickviewSelection(quickviewOptedIn);
    }

    public List<ManageMemberObj> getUsersData() {
        return dataBaseUtil.getUsersData();
    }

    public void updateMemberPreferencesToDB(String userId, String medEnabled, String remindersEnabled) {
        dataBaseUtil.updateMemberPreferencesToDB(userId, medEnabled, remindersEnabled);
    }


    /**
     * This will clear all the tables data.
     */
    public void clearDatabase() {
        dataBaseUtil.clearDBTable(DatabaseConstants.USER_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.PILL_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.PILL_SCHEDULE_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.PILL_PREFERENCE_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.USER_PREFERENCE_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.HISTORY_PREFERENCE_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.HISTORY_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.LOG_ENTRY_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.USER_REMINDERS_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.FDB_IMAGE_TABLE);
        dataBaseUtil.clearDBTable(DatabaseConstants.CUSTOM_IMAGE_TABLE);
    }

    public List<Drug> getDrugListForDue(Context context) {
        return dataBaseUtil.getDrugListForDue(context);
    }

    public int getDiscontinuedMedicationsCount() {
        return dataBaseUtil.getDiscontinuedMedicationsCount();
    }

    public List<DiscontinuedDrug> getDiscontinuedMedications() {
        return dataBaseUtil.getDiscontinuedMedications();
    }

    public void acknowledgeDiscontinuedDrugs(List<DiscontinuedDrug> drugList) {
        dataBaseUtil.acknowledgeDiscontinuedDrugs(drugList);
    }

    public List<Drug> getAllDrugs(Context context) {
        return dataBaseUtil.getAllDrugs(context);
    }

    public List<Drug> getAllIntervalDrugs(Context context) {
        return dataBaseUtil.getAllIntervalDrugs(context);
    }

    public void updateIntervalValueForAsNeededDrug(String guid) {
        dataBaseUtil.updateIntervalValueForAsNeededDrug(guid);
    }

    public String getCreationDateFromHistory(String guid) {
        return dataBaseUtil.getCreationDateFromHistory(guid);
    }

    public int getEnableUsersMedicationCount() {
        return dataBaseUtil.getEnableUsersMedicationCount();
    }

    public String getReminderSoundPathFromDB() {
        return dataBaseUtil.getReminderSoundPathFromDB();
    }

    public void updateNotifyAfterValue(String pillId, long notifyAfterValue) {
        dataBaseUtil.updateNotifyAfterValue(pillId, notifyAfterValue);
    }

    public long updateNotifyAfterValue(String pillId, long notifyAfterValue, long scheduleDate) {
        return dataBaseUtil.updateNotifyAfterValue(pillId, notifyAfterValue, scheduleDate);
    }

    public void addMissedDoseHistoryEvent(Drug drug, String missPill, PillpopperTime date, Context thisActivity) {
        dataBaseUtil.addMissedDoseHistoryEvent(drug, missPill, date, thisActivity);
    }
    public void updateLastMissedCheck(String pillID, String pilltime){
        dataBaseUtil.updateLastMissedCheck(pillID, pilltime);
    }

    public boolean isHistoryEventForScheduleAvailable(String timeStamp, String pillId){
        return dataBaseUtil.isHistoryEventForScheduleAvailable(timeStamp,pillId);
    }

    public void insertPastReminderPillId(String pillID, long time) {
        dataBaseUtil.insertPastReminderPillId(pillID, time);
    }

    public List<Drug> getPassedReminderDrugs(Context pillpopperActivity) {
        return dataBaseUtil.getPassedReminderDrugs(pillpopperActivity);
    }

    public boolean isLastActionEventPostpone(String pillId) {
        return dataBaseUtil.isLastActionEventPostpone(pillId);
    }

    public PillpopperTime getNotifyAfterValue(String pillId) {
        return dataBaseUtil.getNotifyAfterValue(pillId);
    }

    public void removeActedPassedReminderFromReminderTable(Context context, String pillID, String pillTime) {
        dataBaseUtil.removeActedPassedReminderFromReminderTable(context, pillID, pillTime);
    }

    public void removeSchedules(String pillId){
        dataBaseUtil.removeSchedules(pillId);
    }


    public void updateUsersLastSyncToken(String userId, String lastSyncToken, boolean hasChanges) {
        dataBaseUtil.updateUsersLastSyncToken(userId, lastSyncToken, hasChanges);
    }

    public String getImageGuidByPillId(String pillId) {
        return dataBaseUtil.getImageGuidByPillId(pillId);
    }

    public void setImageGuidByPillId(String pillId, String imageGuid) {
        if(Util.isEmptyString(imageGuid)) {
            dataBaseUtil.setImageGuidByPillId(pillId, "");
        } else {
            dataBaseUtil.setImageGuidByPillId(pillId, imageGuid);
        }
    }

    public boolean isPendingImageRequestAvailable() {
        return dataBaseUtil.isPendingImageRequestAvailable();
    }

    public void insertPendingUploadRequest(String pillId, String imageGuid) {
        dataBaseUtil.insertPendingUploadRequest(pillId, imageGuid);
    }

    public void insertPendingDeleteRequest(String pillId, String imageGuid) {
        dataBaseUtil.insertPendingDeleteRequest(pillId, imageGuid);
    }

    public List<PendingImageRequest> getAllPendingImageRequests() {
        return dataBaseUtil.getAllPendingImageRequests();
    }

    public void deletePendingImageRequest(String pillId, String imageGuid, boolean needsUpload, boolean needsDelete) {
        dataBaseUtil.deletePendingImageRequest(pillId, imageGuid, needsUpload, needsDelete);
    }

    public void performAlreadyTakenDrugs(List<Drug> overDueDrugList, PillpopperTime dateTaken, Context context, boolean isLogEntryRequired) {
        dataBaseUtil.performAlreadyTakenDrugs(overDueDrugList, dateTaken, context, isLogEntryRequired);
    }

    public String isQuickViewEnabledForDBCheck() {
        return dataBaseUtil.isQuickViewEnabledForDBCheck();
    }

    public String getLastSyncTokenForUser(String userId) {
        return dataBaseUtil.getLastSyncTokenForUser(userId);
    }

    public void updateUserData(Context pillpopperactivity, User user) {
        dataBaseUtil.updateUserdata(pillpopperactivity, user);
    }

    public List<String> getAllUserIds() {
        return dataBaseUtil.getAllUserIds();
    }

    public void deleteDataOfUserId(String userId) {
        dataBaseUtil.deleteDataOfUserId(userId);
    }

    public void deleteHistoryEntriesByPillID(String pillID) {
        dataBaseUtil.deleteHistoryEntriesByPillID(pillID);
    }

    public void performNotificationAction(Context context, String tappedAction, List<Drug> overDueDrugsList, PillpopperTime time) {
        dataBaseUtil.performNotificationAction(context, tappedAction, overDueDrugsList, time);
    }

    public boolean isUserTimeZoneAvailable(Context context) {
        return dataBaseUtil.isUserTimeZoneAvailable(context);
    }

    public void saveUserDefaultTimeZone(Context context) {
        dataBaseUtil.saveDefaultTimeZoneToDb(context);

        JSONObject preferences = new JSONObject();

        try {
            preferences.put("tz_name", TimeZone.getDefault().getDisplayName());
            preferences.put("tz_secs", Util.getTzOffsetSecs(TimeZone.getDefault()));
        } catch (JSONException e) {
            PillpopperLog.exception(e.getMessage());
        }

        createTimeZoneLogEntry(preferences,context);

    }

    private void createTimeZoneLogEntry(JSONObject preferences, Context mContext) {

        String replyId = Util.getRandomGuid();
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        logEntryModel.setReplyID(replyId);

        JSONObject jsonObj = Util.prepareSettingsAction(preferences, replyId, getPrimaryUserId(), mContext);

        logEntryModel.setEntryJSONObject(jsonObj,mContext);
        FrontController.getInstance(mContext).addLogEntry(mContext,logEntryModel);

    }

    /**
     * Special Logic
     * Updates last 48 hours events tzSec value with previous timezone, if any history event found tzSec value as null.
     */
    public void updateHistoryOffsetForLast48HourEvents(long tzSecs) {
        dataBaseUtil.updateHistoryOffsetForLast48HourEvents(tzSecs);
    }


    public Map<String, String> getCreationTimeZoneFromHistory(String guid) {
        return dataBaseUtil.getCreationTimeZoneFromHistory(guid);
    }

    public long getLastHistoryScheduleTimeStamp() {
        return dataBaseUtil.getLastHistoryScheduleTimeStamp();
    }

    public JSONArray getSchedulesInTimeFormateByPillId(String pillID){
        return dataBaseUtil.getSchedulesInTimeFormateByPillId(pillID);
    }

    public boolean isEnabledUser(String userID) {
        return dataBaseUtil.isEnabledUser(userID);
    }

    public boolean isActiveDrug(String pillID, PillpopperTime scheduleTime) {
        return dataBaseUtil.isActiveDrug(pillID, scheduleTime);
    }

    public boolean isEntryInPastReminderTable(Long time) {
        return dataBaseUtil.isEntryInPastReminderTable(time);
    }

    public boolean isEntryAvailableInPastReminder(String pillID, PillpopperTime scheduleTime) {
        return dataBaseUtil.isEntryAvailableInPastReminder(pillID, scheduleTime);
    }

    public void removePillFromPassedReminderTable(Context context, String guid) {
        dataBaseUtil.removePillFromPassedReminderTable(context, guid);
    }

    public void saveFdbImage(Context context, FdbImage fdbImage){
        dataBaseUtil.saveFdbImage(context,fdbImage);
    }

    public List<KphcDrug> getKPHCDrugsListToFetchFBDImages() {
        return dataBaseUtil.getKPHCDrugsListToFetchFBDImages();
    }

    public void updatePillImagePreferences(String pillId, String imageChoice, String serviceImageId) {
        dataBaseUtil.updatePillImagePreferences(pillId, imageChoice, serviceImageId);
    }

    public boolean isFDBImageAvailable(String pillId) {
        return dataBaseUtil.isFDBImageAvailable(pillId);
    }

    public boolean isCustomImageAvailable(String imageId) {
        return dataBaseUtil.isCustomImageAvailable(imageId);
    }

    public void saveCustomImage(String imageId,String imageData) {
        dataBaseUtil.saveCustomImage(imageId,imageData);
    }

    public void updateCustomImage(String pillId,String imageData) {
        dataBaseUtil.updateCustomImage(pillId,imageData);
    }

    public String getCustomImage(String imageId){
        return dataBaseUtil.getCustomImage(imageId);
    }

    public void deleteCustomImage(String imageId) {
        dataBaseUtil.deleteCustomImage(imageId);
    }

    public Drug getDrugForImageLoad(String pillID) {
        return dataBaseUtil.getDrugForImageLoad(pillID);
    }

    public void updateNoNeedFDBImageUpdate(String pillId) {
        dataBaseUtil.updateNoNeedFDBImageUpdate(pillId);
    }

    public void updateMissedDosesLastChecked(List<String> pillIds, String value) {
        dataBaseUtil.updateMissedDosesLastChecked(pillIds, value);
    }

    public JsonParserUtility getJsonParserUtilityInstance(Context context){
        return JsonParserUtility.getInstance();
    }

    public SecretQuestionsResponse parseSecretQuestionsResponse(Context context, String jsonInput) {
        SecretQuestionsResponse resp = getJsonParserUtilityInstance(context).parseJsonForSecretQuestions(jsonInput);
        return resp;
    }

    public void createEntryInHistoryForScheduleTime(List<Drug> drugList, String action, PillpopperTime scheduleTime) {
        dataBaseUtil.createEntryInHistoryForScheduleTime(drugList, action, scheduleTime);
    }

    public void deleteEmptyHistoryEntriesByPillID(String pillId) {
        dataBaseUtil.deleteEmptyHistoryEntriesByPillID(pillId);
    }

    public List<RxRefillUserData> getRxRefillUsersList() {
        return dataBaseUtil.getRxRefillUsersList();
    }

    public String getMissedDosesLastCheckedValue(String pillId) {
        return dataBaseUtil.getMissedDoseLastCheckedValue(pillId);
    }

    public void saveFailedImage(FailedImageObj failedImageObj) {
        dataBaseUtil.saveFailedImage(failedImageObj);
    }

    public List<FailedImageObj> getFailedImageEntryList() {
        return dataBaseUtil.getFailedImageEntryList();
    }

    public boolean isEntryAvailableInRetryTable(String pillID, String imageID) {
        return dataBaseUtil.isEntryAvailableInRetryTable(pillID, imageID);
    }

    public void deleteEntryFromRetryTable(String pillID, String imageID) {
        dataBaseUtil.deleteEntryFromRetryTable(pillID, imageID);
    }

    public boolean isPillEntryAvailableInDB(String pillID) {
        return dataBaseUtil.isPillEntryAvailableInDB(pillID);
    }

    public String getUniqueRandomIdForGA(Context context) {
        try {
            SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
            String uniqueId = mSharedPrefManager.getString(AppConstants.KEY_GA_UNIQUE_RANDOM_VALUE, "");
            if (Util.isEmptyString(uniqueId)) {
                // save it for the first time
                uniqueId = UUID.randomUUID().toString();
                mSharedPrefManager.putString(AppConstants.KEY_GA_UNIQUE_RANDOM_VALUE, uniqueId, false);
            }
            return uniqueId;
        } catch (Exception ex) {
            PillpopperLog.say("exception while getUniqueRandomIdForGA");
            return "";
        }
    }

    public String getDosageTypeByPillID(String pilllID){
        return dataBaseUtil.getDosageTypeByPillID(pilllID);
    }

    public String getCreationDateByScheduleDateFromHistory(String guid, String scheduleDate) {
        return dataBaseUtil.getCreationDateByScheduleDateFromHistory(guid,scheduleDate);
    }

    public boolean isAnyUserEnabledRemindersHasSchedules() {
        return dataBaseUtil.isAnyUserEnabledRemindersHasSchedules();
    }

    public String getUserGender(){
        return dataBaseUtil.getUserGender();
    }

    public String getUserAge(){
        return dataBaseUtil.getUserAge();
    }

    public void updateScheduleChoice(String scheduleChoice, String pillId) {
        dataBaseUtil.updateScheduleChoice(scheduleChoice,pillId);
    }

    public void updatePostponeHistoryAvailable(Drug drug) {
        dataBaseUtil.updatePostponeHistoryEntry(drug);
    }
    public boolean isTableExist(String tableName) {
        return  dataBaseUtil.isTableExists(tableName);
    }

    public void updateActionAndRecordDateHistoryPreference(HistoryEditEvent mHistoryEditEvent) {
        dataBaseUtil.updateActionAndRecordDateHistoryPreference(mHistoryEditEvent);
    }

    public void updateHistoryEventPreferences(String historyEventGuid, String pillOperation, Drug drug, long postponeSeconds) {
        dataBaseUtil.updateHistoryEventPreferences(historyEventGuid,pillOperation,drug,postponeSeconds);
    }

    public boolean isAnySchedulesAvailableForUser(String mSelectedUserId) {
       return dataBaseUtil.isAnySchedulesAvailableForUser(mSelectedUserId);
    }

    public void updateScheduleGUID(String scheduleGuid, String pillId) {
        dataBaseUtil.updateScheduleGUID(scheduleGuid, pillId);
    }

    public List<HistoryEvent> getActivePostponedEvents(String pillId) {
        return dataBaseUtil.getActivePostponedEvents(pillId);
    }
}