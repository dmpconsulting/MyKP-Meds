package com.montunosoftware.pillpopper.controller;

import android.content.Context;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.database.model.PillList;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import com.montunosoftware.pillpopper.model.DiscontinuedDrug;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.HourMinute;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.test.core.app.ApplicationProvider;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by m1032896 on 2/13/2017.
 * Mindtree Ltd
 * Raghavendra.dg@mindtree.com
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class,shadows = {DatabaseHandlerShadow.class, SecurePreferencesShadow.class})
public class FrontControllerTest {

    private Context context;
    private FrontController frontController;
    private PillpopperActivity pillpopperActivity;
    private String USER_ID;

    @Before
    public void setup(){
        TestUtil.setupTestEnvironment();
        pillpopperActivity = Robolectric.buildActivity(PillpopperActivity.class).get();
        context =  ApplicationProvider.getApplicationContext().getApplicationContext();
        frontController = FrontController.getInstance(context);
        UniqueDeviceId.getHardwareId(context);
    }

    @Test
    public void frontControllerShouldNotBeNull(){
        assertNotNull(frontController);
    }

    @Test
    public void getProxyMemberUserIdsShouldNotBeNull(){
        USER_ID = frontController.getPrimaryUserId();
        System.out.println(USER_ID);
        assertNotNull(USER_ID);
    }

   /* @Test
    public void getDrugListByUserIdTest(){
        LinkedHashMap<String, List<Drug>> drugListByUserId =frontController.getDrugListByUserId(USER_ID);
        assertNotNull(drugListByUserId);
        System.out.println(drugListByUserId);
    }*/

    @Test
    public void testgetDrugByPillId(){
        assertNotNull(frontController.getDrugByPillId(TestConfigurationProperties.MOCK_PILL_ID));
    }

    @Test
    public void testgetFdbImageByPillId(){
        frontController.getFdbImageByPillId(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testUpdateMaxDailyDoses(){
        frontController.updateMaxDailyDoses(3, TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testUpdateScheduleType(){
        frontController.updateScheduleType("Scheduled", TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testupdateNotes(){
        frontController.updateNotes("Sample Notes", TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testgetPrimaryUserId(){
        assertNotNull(frontController.getPrimaryUserId());
    }

    @Test
    public void testgetPrimaryUserIdIgnoreEnabled(){
        assertNotNull(frontController.getPrimaryUserIdIgnoreEnabled());
    }

    @Test
    public void testgetDrugListByUserId(){
        assertNotNull(frontController.getDrugListByUserId(null, TestConfigurationProperties.MOCK_USER_ID));
    }

    @Test
    public void testgetProxyMemberUserIds(){
        assertNotNull(frontController.getProxyMemberUserIds());
    }

    @Test
    public void testgetUserFirstNameByUserId(){
        assertNotNull(frontController.getUserFirstNameByUserId(TestConfigurationProperties.MOCK_USER_ID));
    }

    @Test
    public void testgetUserById(){
        assertNotNull(frontController.getUserById(TestConfigurationProperties.MOCK_USER_ID));
    }

    @Test
    public void testgetEnabledUserIdSize(){
        assertNotNull(frontController.getEnabledUserIds());
    }

    @Test
    public void testgetDrugsListByUserId(){
        assertNotNull(frontController.getDrugsListByUserId(TestConfigurationProperties.MOCK_USER_ID));
    }

    @Test
    public void testmarkDrugAsArchive(){
        frontController.markDrugAsArchive(TestConfigurationProperties.MOCK_PILL_ID);
    }

    /*@Test
    public void testremoveDrugFromArchive(){
        Drug drug = FrontController.getInstance(pillpopperActivity).getDrugByPillId(TestConfigurationProperties.MOCK_PILL_ID);
        if(null!=drug) {
            frontController.removeDrugFromArchive(drug, pillpopperActivity, TestConfigurationProperties.MOCK_PILL_ID);
        }
    }*/

    @Test
    public void testmarkDrugAsDeleted(){
        frontController.markDrugAsDeleted(TestConfigurationProperties.MOCK_PILL_ID);
    }

   /* @Test
    public void testgetMedicationForUser(){
        assertNotNull(frontController.getMedicationForUser(TestConfigurationProperties.MOCK_USER_ID));
    }*/


    @Test
    public void updateSchedule(){
        HashMap<String, String> data = new HashMap<>();
        data.put("user_id", TestConfigurationProperties.MOCK_USER_ID);
        data.put("start_date", Long.toString(PillpopperDay.today().atLocalTime(new HourMinute(0, 0)).getGmtSeconds()));
        data.put("end_date", Long.toString(-1));
        data.put("scheduledFrequency", "D");
        List<String> drugIds = new ArrayList<>();
        drugIds.add(TestConfigurationProperties.MOCK_PILL_ID);
        List<String> pilltimes = new ArrayList<>();
        pilltimes.add("06:30:00");
        frontController.updateSchedule(null,data,drugIds,pilltimes);
    }


    @Test
    public void testAddLogEntry(){
        frontController.addLogEntry(context, TestUtil.prepareCreatePillEntry(new PillList(),context));
    }

    @Test
    public void testLogEntries(){
        assertNotNull(frontController.getLogEntries(context));
    }

    @Test
    public void testIsLogEntryAvailable(){
        frontController.isLogEntryAvailable();
    }

    @Test
    public void testRemoveLogEntry(){
        frontController.removeLogEntry(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testgetSchdulesByPillId(){
        assertNotNull(frontController.getSchdulesByPillId(TestConfigurationProperties.MOCK_PILL_ID));
    }

   /* @Test
    public void testgetLastActionTimeForSchedule(){
        frontController.getLastActionTimeForSchedule(null);
    }

    @Test
    public void testgetAllArchivedDrugs(){
        assertNotNull(frontController.getAllArchivedDrugs(null));
    }*/

    @Test
    public void testgetArchiveListData(){
        assertNotNull(frontController.getArchiveListData(null));
    }

    @Test
    public void testgetArchiveListDataHashMap(){
        assertNotNull(frontController.getArchiveListDataHashMap(null));
    }

    @Test
    public void testgetArchivedDrugDetails(){
        assertNotNull(frontController.getArchivedDrugDetails(null, TestConfigurationProperties.MOCK_PILL_ID));
    }

    @Test
    public void testgetDrugListForOverDue(){
        assertNotNull(frontController.getDrugListForOverDue(context));
    }

    @Test
    public void testgetDrugListForDue(){
        assertNotNull(frontController.getDrugListForDue(context));
    }

    @Test
    public void testisQuickViewEnabled(){
        assertNotNull(frontController.isQuickViewEnabled());
    }

    @Test
    public void testupdateNewKPHCMed(){
        frontController.updateNewKPHCMed(TestConfigurationProperties.MOCK_KPHC_PILL_ID, null);
    }


    @Test
    public void testgetEventDescriptionFromHistory(){
        frontController.getEventDescriptionFromHistory(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testupdateEnableUsersData(){
        frontController.updateEnableUsersData(TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testgetAllEnabledUsers(){
        frontController.getAllEnabledUsers();
    }

    @Test
    public void testisHistoryEventAvailable(){
        frontController.isHistoryEventAvailable(new PillpopperTime(Long.valueOf(Util.convertDateIsoToLong(TestConfigurationProperties.MOCK_HISTORY_EVENT_SCHEDULE_TIME))), TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testgetHistoryEvents(){
        frontController.getHistoryEvents(TestConfigurationProperties.MOCK_USER_ID, "30");
    }

    @Test
    public void testgetHistoryEditEventDetails(){
        frontController.getHistoryEditEventDetails("aa537479dd3ac4d113418a3a5e4102ffc");
    }

    @Test
    public void testupdateHistoryEvent(){
        frontController.updateHistoryEvent("aa537479dd3ac4d113418a3a5e4102ffc", "takePill", "Azithromycin med Skipped", new PillpopperTime(1531215000));
    }

    @Test
    public void testgetDoseHistoryDays(){
        frontController.getDoseHistoryDays();
    }


    @Test
    public void testgetPillHistoryEventCountForToday(){
        frontController.getPillHistoryEventCountForToday("aa537479dd3ac4d113418a3a5e4102ffc");
    }

    @Test
    public void testgetUserPreferencesForUser(){
        frontController.getUserPreferencesForUser(TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testsetDoseHistoryDaysForUser(){
        frontController.setDoseHistoryDaysForUser(30, TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testsetSignedOutReminderEnabled(){
        frontController.setSignedOutReminderEnabled(false, TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testsetNotificationSoundForUser(){
        frontController.setNotificationSoundForUser("default", TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testsetRepeatReminderAfterSecForUser(){
        frontController.setRepeatReminderAfterSecForUser(300, TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testgetSecondaryReminderPeriodSecs(){
        frontController.getSecondaryReminderPeriodSecs(TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testupdateQuickviewSelection() {
        frontController.updateQuickviewSelection(PillpopperConstants.QUICKVIEW_OPTED_OUT);
    }

    @Test
    public void testgetUsersData() {
        frontController.getUsersData();
    }

    @Test
    public void testupdateAsPendingRemindersPresent() {
        frontController.updateAsPendingRemindersPresent(context);
    }

    @Test
    public void testupdateAsNoPendingReminders() {
        frontController.updateAsNoPendingReminders(context);
    }

    @Test
    public void testgetPendingRemindersStatus() {
        frontController.getPendingRemindersStatus(context);
    }

    @Test
    public void testgetDiscontinuedMedicationsCount() {
        frontController.getDiscontinuedMedicationsCount();
    }

    @Test
    public void testgetDiscontinuedMedications() {
        frontController.getDiscontinuedMedications();
    }


    @Test
    public void testacknowledgeDiscontinuedDrugs() {
        List<DiscontinuedDrug> drugList = new ArrayList<>();
        DiscontinuedDrug d = new DiscontinuedDrug();
        d.setPillId(TestConfigurationProperties.MOCK_KPHC_PILL_ID);
        frontController.acknowledgeDiscontinuedDrugs(drugList);
    }

    @Test
    public void testgetAllDrugs() {
        frontController.getAllDrugs(context);
    }

    @Test
    public void testgetAllIntervalDrugs() {
        frontController.getAllIntervalDrugs(context);
    }

    @Test
    public void testupdateIntervalValueForAsNeededDrug() {
        frontController.updateIntervalValueForAsNeededDrug(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testgetCreationDateFromHistory() {
        frontController.getCreationDateFromHistory(TestConfigurationProperties.MOCK_HISTORY_HISTORY_EVENT_GUID);
    }

    @Test
    public void testgetEnableUsersMedicationCount() {
        frontController.getEnableUsersMedicationCount();
    }

    @Test
    public void testgetReminderSoundPathFromDB() {
        frontController.getReminderSoundPathFromDB();
    }

    @Test
    public void testgetPassedReminderDrugs() {
        frontController.getPassedReminderDrugs(context);
    }

    @Test
    public void testisLastActionEventPostpone() {
        frontController.isLastActionEventPostpone(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testgetNotifyAfterValue() {
        frontController.getNotifyAfterValue(TestConfigurationProperties.MOCK_KPHC_PILL_ID);
    }

    @Test
    public void testremoveSchedules() {
        frontController.removeSchedules(TestConfigurationProperties.MOCK_KPHC_PILL_ID);
    }

    @Test
    public void testisPendingImageRequestAvailable() {
        frontController.isPendingImageRequestAvailable();
    }

    @Test
    public void testisQuickViewEnabledForDBCheck() {
        frontController.isQuickViewEnabledForDBCheck();
    }

    @Test
    public void testisPillEntryAvailableInDB() {
        frontController.isPillEntryAvailableInDB(TestConfigurationProperties.MOCK_PILL_ID);
    }
    @Test
    public void testgetAllPendingImageRequests() {
        frontController.getAllPendingImageRequests();
    }

    @Test
    public void testgetLastSyncTokenForUser() {
        frontController.getLastSyncTokenForUser(TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testgetAllUserIds() {
        frontController.getAllUserIds();
    }

    @Test
    public void testdeleteDataOfUserId() {
        frontController.deleteDataOfUserId(TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testdeleteHistoryEntriesByPillID() {
        frontController.deleteHistoryEntriesByPillID(TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testisTimezoneAdjustmentToServerRequired() {
        frontController.isTimezoneAdjustmentToServerRequired(context);
    }

    @Test
    public void testisUserTimeZoneAvailable() {
        frontController.isUserTimeZoneAvailable(context);
    }

    @Test
    public void testsaveUserDefaultTimeZone() {
        frontController.saveUserDefaultTimeZone(context);
    }

    @Test
    public void testhideLateRemindersWhenFromNotifications() {
        frontController.hideLateRemindersWhenFromNotifications(context);
    }

    @Test
    public void testshowLateRemindersWhenFromNotifications() {
        frontController.showLateRemindersWhenFromNotifications(context);
    }

    @Test
    public void testgetCreationTimeZoneFromHistory() {
        frontController.getCreationTimeZoneFromHistory("aa537479dd3ac4d113418a3a5e4102ffc");
    }

    @Test
    public void testgetLastHistoryScheduleTimeStamp() {
        frontController.getLastHistoryScheduleTimeStamp();
    }

    @Test
    public void testgetSchedulesInTimeFormateByPillId() {
        frontController.getSchedulesInTimeFormateByPillId(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testisEnabledUser() {
        frontController.isEnabledUser(TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testgetUserGender() {
        frontController.getUserGender();
    }

    @Test
    public void testdisableBatteryOptimizationCard() {
        frontController.disableBatteryOptimizationCard(context);
    }

    @Test
    public void testshowBatteryOptimizationCard() {
        frontController.showBatteryOptimizationCard(context);
    }

    @Test
    public void testisAnyUserEnabledRemindersHasSchedules() {
        frontController.isAnyUserEnabledRemindersHasSchedules();
    }

    @Test
    public void testdisableBatteryOptimizationAlert() {
        frontController.disableBatteryOptimizationAlert(context);
    }

    @Test
    public void testisBatteryOptmizationDecisionNotOpted() {
        frontController.isBatteryOptmizationDecisionNotOpted(context);
    }

    @Test
    public void testgetDosageTypeByPillID() {
        frontController.getDosageTypeByPillID(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testgetNonSecureLogEntries() {
        frontController.getNonSecureLogEntries(context);
    }

    @Test
    public void testgetUniqueRandomIdForGA() {
        frontController.getUniqueRandomIdForGA(context);
    }

    @Test
    public void testgetFailedImageEntryList() {
        frontController.getFailedImageEntryList();
    }

    @Test
    public void testgetMissedDosesLastCheckedValue() {
        frontController.getMissedDosesLastCheckedValue(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testdeleteEmptyHistoryEntriesByPillID() {
        frontController.deleteEmptyHistoryEntriesByPillID(TestConfigurationProperties.MOCK_PILL_ID);
    }

    @Test
    public void testgetJsonParserUtilityInstance() {
        frontController.getJsonParserUtilityInstance(context);
    }


    @Test
    public void testupdateMissedDosesLastChecked() {
        List<String> pillIds = new ArrayList<>();
        pillIds.add(TestConfigurationProperties.MOCK_PILL_ID);
        frontController.updateMissedDosesLastChecked(pillIds, "1532784600");
    }

    @Test
    public void testperformTakeDrug_pastReminders(){
        List<Drug> drugs = frontController.getAllDrugs(context);
        List<Drug> modifiedDrugs = new ArrayList<>();
        for(Drug d : drugs){
            if(null!=d.getScheduledTime()){
                d.setScheduledTime(Util.convertStringtoPillpopperTime(TestConfigurationProperties.MOCK_REMINDER_TIME));
                modifiedDrugs.add(d);
            }
        }
        frontController.performTakeDrug_pastReminders(modifiedDrugs, Util.convertStringtoPillpopperTime(TestConfigurationProperties.MOCK_REMINDER_TIME),
                context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
    }

    @Test
    public void testPerformTakeDrug(){
        List<Drug> drugs = frontController.getAllDrugs(context);
        List<Drug> modifiedDrugs = new ArrayList<>();
        for(Drug d : drugs){
            if(null!=d.getScheduledTime()){
                d.setScheduledTime(Util.convertStringtoPillpopperTime(TestConfigurationProperties.MOCK_REMINDER_TIME));
                modifiedDrugs.add(d);
            }
        }
        frontController.performTakeDrug(modifiedDrugs, Util.convertStringtoPillpopperTime(TestConfigurationProperties.MOCK_REMINDER_TIME),
                context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
    }

    @Test
    public void testPerformSkipDrug(){
        List<Drug> drugs = frontController.getAllDrugs(context);
        List<Drug> modifiedDrugs = new ArrayList<>();
        for(Drug d : drugs){
            if(null!=d.getScheduledTime()){
                d.setScheduledTime(Util.convertStringtoPillpopperTime(TestConfigurationProperties.MOCK_REMINDER_TIME));
                modifiedDrugs.add(d);
            }
        }
        frontController.performSkipDrug(modifiedDrugs, Util.convertStringtoPillpopperTime(TestConfigurationProperties.MOCK_REMINDER_TIME),
                context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
    }

    @Test
    public void testInsertUserData(){
        frontController.insertUserData(context, TestUtil.prepareUserObject());
    }

    @Test
    public void testUpdateUserData(){
       // frontController.updateUserData(TestUtil.prepareUserObject());
    }


}
