package com.montunosoftware.pillpopper.android.util;

import android.app.AlarmManager;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by M1032896 on 7/23/2018.
 */


@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, UniqueDeviceIdShadow.class})
public class UtilTest {

    private Context context;
    private PillpopperAppContext pillpopperAppContext;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(context);
    }

    @Test
    public void testGetAppVersion() {
        assertNotNull(Util.getAppVersion(context));
    }

    @Test
    public void testGetAppVersionCode() {
        assertNotNull(Util.getAppVersionCode(context));
    }

    @Test
    public void testParseNonnegativeLong() {
        assertEquals(1, Util.parseNonnegativeLong("1"));
        assertEquals(-1, Util.parseNonnegativeLong(null));
    }

    @Test
    public void testParseNonnegativeDouble() {
        assertEquals(1.0, Util.parseNonnegativeDouble("1.0"));
        assertEquals(-1, Util.parseNonnegativeLong(null));
    }

    @Test
    public void testParseJSONStringOrNull() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("data", "info");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        assertEquals(null, Util.parseJSONStringOrNull(null, "data"));
        assertEquals("info", Util.parseJSONStringOrNull(jsonObject, "data"));
    }

    @Test
    public void testCleanString() {
        assertEquals("data ".trim(), Util.cleanString("data "));
    }

    @Test
    public void testGetTextFromDouble() {
        assertEquals("1", Util.getTextFromDouble(1.0));
        assertNull(Util.getTextFromDouble(-1));
    }

    @Test
    public void testGetTextFromLong() {
        assertEquals("1", Util.getTextFromLong(1));
        assertNull(Util.getTextFromLong(0));
    }

    @Test
    public void testMaybeAppendString() {
        assertNull(Util.maybeAppendString(null, "data"));
        assertEquals("data data", Util.maybeAppendString("data", "data"));

    }

    @Test
    public void testGetHomeDate() {
        assertNotNull(Util.getHomeDate());
    }


    @Test
    public void testFriendlyGuid() {
        assertNotNull(Util.friendlyGuid("test"));
        assertEquals("", Util.friendlyGuid(null));
    }

    @Test
    public void testGetRandomGuid() {
        assertNotNull(Util.getRandomGuid());
    }

    @Test
    public void testGetRandomGuidWithNumber() {
        assertNotNull(Util.getRandomGuid(2));
    }

    @Test
    public void testIsNetworkAvailable() {
        assertTrue(Util.isNetworkAvailable(context));
    }


    /*@Test
    public void testShowGenericStatusAlert() {
        Util.showGenericStatusAlert(context, "Test message");
    }*/

    @Test
    public void testHandleParseInt() {
        assertNotNull(Util.handleParseInt("3"));
        assertEquals(0, Util.handleParseInt("3.0"));
    }

    @Test
    public void testCheckForSessionExpire() {
        assertEquals(-1, Util.checkForSessionExpire(null));
    }

    /*@Test
    public void testShowSessionexpireAlert() {
        Util.showSessionexpireAlert(context, PillpopperAppContext.getGlobalAppContext(context));
    }*/

    @Test
    public void testIsCreateUserRequestInProgress() {
        assertFalse(Util.isCreateUserRequestInprogress());
    }

    @Test
    public void testBuildHeaders() {
        assertNotNull(Util.buildHeaders(context));
    }

    @Test
    public void testPrepareAcknowledgeDiscontinuedDrugsLogEntry() {
        assertNotNull(Util.prepareAcknowledgeDiscontinuedDrugsLogEntry(new DiscontinuedDrug(), context));
    }


    /*@Test
    public void testPrepareLogEntryForActions() {
        Drug drug = FrontController.getInstance(context).getDrugByPillId(TestConfigurationProperties.MOCK_UTIL_PILL_ID);
        if (drug != null) {
            Util.prepareLogEntryForAction("takePill", drug, context);
        }
    }*/

    @Test
    public void testGetClientInfo() {
        assertNotNull(Util.getClientInfo(context));
    }

    /*@Test
    public void testPrepareLogEntryForCreateHistoryEvent() {
        Drug drug = FrontController.getInstance(context).getDrugByPillId("54536dbb2ee6fdb26ac34042d1884fe1");
        if (drug != null) {
            Util.prepareLogEntryForCreateHistoryEvent("takePill", drug, context);
        }
    }*/

    @Test
    public void testPrepareLogEntryForCreateHistoryEvent_pastReminders() {
        Drug drug = FrontController.getInstance(context).getDrugByPillId(TestConfigurationProperties.MOCK_UTIL_PILL_ID);
        drug.setScheduledTime(PillpopperTime.now());
        if (drug != null) {
            Util.prepareLogEntryForCreateHistoryEvent_pastReminders("takePill", drug, context);
        }
    }

    @Test
    public void testDefaultLogEntry() {
        assertNotNull(Util.defaultLogEntry());
    }

    @Test
    public void testPrepareGetState() {
        assertNotNull(Util.prepareGetState(context));
    }

    @Test
    public void testIsValidOverDue() {
        assertFalse(Util.isValidDrugForReminders(new Drug()));
    }


    @Test
    public void testGetDeviceMake() {
        assertNotNull(Util.getDeviceMake());
    }

    @Test
    public void testIsEmptyString() {
        assertTrue(Util.isEmptyString(null));
        assertTrue(Util.isEmptyString(""));
        assertFalse(Util.isEmptyString("test"));
    }

    @Test
    public void testGetSystemPMFormat() {
        assertNotNull(Util.getSystemPMFormat());
    }

    @Test
    public void testFormatPhoneNumber() {
        assertNotNull(Util.formatPhoneNumber("11234567890"));
    }

    /*@Test
    public void testPrepareLogEntryForDelete(){
        Drug drug = FrontController.getInstance(context).getDrugByPillId(TestConfigurationProperties.MOCK_UTIL_PILL_ID);
        if (drug != null) {
            assertNotNull(Util.prepareLogEntryForDelete(drug, pillpopperActivity));
        }
    }*/

    @Test
    public void testPrepareSettingsAction(){
        JSONObject prefrences = new JSONObject();
        try {
            prefrences.put(PillpopperConstants.ACTION_SETTINGS_SIGNOUT_REMINDERS, "0");
            prefrences.put("userData", TestConfigurationProperties.MOCK_USER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertNotNull(Util.prepareSettingsAction(prefrences, "12345", TestConfigurationProperties.MOCK_USER_ID, context));
    }

    @Test
    public void testFormatted12HourTime(){
       assertNotNull(Util.getFormatted12HourTime(11,30));
    }

    @Test
    public void testPrepareRemindersMapData(){
        List<Drug> list = FrontController.getInstance(context).getDrugListForOverDue(context);
        if(list.size()>0){
            Util.getInstance().prepareRemindersMapData(list, context);
        }
    }

    @Test
    public void testGetSystemAMFormat(){
        assertNotNull(Util.getSystemAMFormat());
    }

    @Test
    public void testCalculateUpdatedSchedule(){
        assertNotNull(Util.calculateUpdatedSchedule(TestConfigurationProperties.MOCK_LONG_TIME));
    }

   /* @Test
    public void testGetNotificationUri(){
        assertNotNull(Util.getNotificationUri(context, "default"));
    }*/

    @Test
    public void testupdateOptinSelection(){
        Util.updateOptinSelection("1", context);
    }

    @Test
    public void testColorWrapper(){
        Util.getColorWrapper(context, R.color.text_content);
    }

    @Test
    public void testCloneDrugObject(){
        Drug drug = FrontController.getInstance(context).getDrugByPillId(TestConfigurationProperties.MOCK_UTIL_PILL_ID);
        if (drug != null) {
            Util.cloneDrugObject(context, drug);
        }
    }

    @Test
    public void testGetLastSyncTokenValue(){
        assertNotNull(Util.getLastSyncTokenValue("1531263174"));
    }

    /*@Test
    public void testGetOpIdByAction(){
        assertNotNull(Util.getOpIdByAction("TakePill"));
    }*/

    @Test
    public void testCanShowLateReminder(){
        assertEquals(false,Util.canShowLateReminder(context));
    }

    /*@Test
    public void testGetActivationUrl(){
        assertNotNull(Util.getActivationUrl(context));
    }*/

    @Test
    public void testIsEmulator(){
        assertEquals(false, Util.isEmulator());
    }

    @Test
    public void testcheckForDSTAndPrepareAdjustPillLogEntryObject(){
        Util.checkForDSTAndPrepareAdjustPillLogEntryObject(context);
    }

    @Test
    public void testcheckForDSTAndPrepareAdjustPillJSONArray(){
        Util.checkForDSTAndPrepareAdjustPillJSONArray(context);
    }

    @Test
    public void testgetDSTOffsetValue(){
        assertEquals(0,Util.getDSTOffsetValue());
    }


    @Test
    public void testDescribeDrugAsHtml(){
        Drug drug = FrontController.getInstance(context).getDrugByPillId(TestConfigurationProperties.MOCK_UTIL_PILL_ID);
        if (drug != null) {
            Util.describeDrugAsHtml(context, pillpopperAppContext ,drug);
        }
    }

    @Test
    public void testDrugFirstName(){
        assertNotNull(Util.getFirstName("Halobetasol (ULTRAVATE)"));
    }

    @Test
    public void testCardIndex(){
        Util.saveCardIndex(context, 1);
    }

    @Test
    public void testGetKeyValueFromAppProfileRuntimeData(){
        assertEquals(null, Util.getKeyValueFromAppProfileRuntimeData("pillpopperSecureBaseURL"));
    }

    @Test
    public void testisAppProfileDownloadTimeMoreThan15Min(){
        assertEquals(true, Util.isAppProfileDownloadTimeMoreThan15Min(context));
    }

    @Test
    public void testisAppProfileCallRequired(){
        assertEquals(true, Util.isAppProfileCallRequired(context));
    }

    @Test
    public void testresetHomeScreenCardsFlags(){
        Util.getInstance().resetHomeScreenCardsFlags();
    }

    @Test
    public void testremovePillSchedulesFromReminders(){
        Util.getInstance().removePillSchedulesFromReminders(context, TestConfigurationProperties.MOCK_USER_ID);
    }

    @Test
    public void testcheckForJavaScriptEnablingOption(){
        assertEquals(true, Util.checkForJavaScriptEnablingOption("guide"));
    }

    @Test
    public void testgetRemindersMapDataForNotificationAction(){
        List<Drug> list = FrontController.getInstance(context).getDrugListForOverDue(context);
        if(list.size()>0) {
            Util.getInstance().getRemindersMapDataForNotificationAction(list, 1531263174 ,context);
        }
    }

    @Test
    public void testcleanAttachments(){
        try {
            Util.cleanAttachments(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testcheck24hourformat(){
        assertNotNull(Util.check24hourformat("12:30", null));
    }

    @Test
    public void testsetAlarm(){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager == null) {
            Util.setAlarm(context, alarmManager);
        }
    }

    @Test
    public void testCancelAlarm(){
        Util.cancelAlarm(context);
    }

    @Test
    public void testGetTimeZoneName(){
        assertNotNull(Util.getTimeZoneName());
    }

    @Test
    public void testBatteryOptimizationAlertRequired(){
        assertTrue(Util.isBatteryOptimizationAlertRequired(context));
    }

    @Test
    public void testisBatteryOptimizationCardRequired(){
        assertTrue(Util.isBatteryOptimizationCardRequired(context));
    }

    @Test
    public void teststoreEnvironment(){
        Util.storeEnvironment(context);
    }

    @Test
    public void testLogFireBaseEventForDeviceFontScale(){
        Util.logFireBaseEventForDeviceFontScale(context);
    }

    @Test
    public void testLoadValuesForRxRefillActivity(){
        Util.getInstance().loadValuesForRxRefillActivity(context);
    }

    @Test
    public void testcloseNavigationDrawerIfOpen(){
        Util.NavDrawerUtils.closeNavigationDrawerIfOpen();
    }

    @Test
    public void testsetNavigationDrawerLayout(){
        Util.NavDrawerUtils.setNavigationDrawerLayout(null);
    }

    @Test
    public void testsetClientIdAndClientSecret(){
        Util.getInstance().setClientIdAndClientSecret();
    }

    @Test
    public void testgetMappedSSOEnvironment(){
        assertNotNull(Util.getMappedSSOEnvironment("hint2"));
    }

    /*@Test
    public void testprocessPillRequestObjectFrom(){
        Util.processPillRequestObjectFrom(new JSONObject(), context);
    }*/

    @Test
    public void testisNonSecureAppProfileCallRequired(){
        Util.isNonSecureAppProfileCallRequired(context);
    }

    @Test
    public void testisHasStatusUpdateCallRequired(){
        Util.isHasStatusUpdateCallRequired(context);
    }

    @Test
    public void testhasPendingAlertsNeedForceSignIn(){
        Util.hasPendingAlertsNeedForceSignIn(context);
    }

    @Test
    public void testclearHasStatusUpdateValues(){
        Util.clearHasStatusUpdateValues(context);
    }

    @Test
    public void testgetWeekdayName(){
        assertNotNull(Util.getWeekdayName(context, 1));
    }

    @Test
    public void testDeleteRegionContactFile(){
        Util.deleteRegionContactFile(context);
    }

    @Test
    public void testHandleParseLong() {
        assertNotNull(Util.handleParseLong("3.00456"));
    }

    @Test
    public void testconvertHHMMtoTimeFormat() {
        assertNotNull(Util.convertHHMMtoTimeFormat("1245"));
    }

    @Test
    public void testgetOSVersion() {
        assertNotNull(Util.getOSVersion());
    }


    @Test
    public void testgetInstance() {
        assertNotNull(Util.getInstance());
    }

    @Test
    public void testParseJSONNonnegativeLong() {
       Assert.assertEquals(124324455,Util.parseJSONNonnegativeLong("124324455"));
    }
    @Test
    public void testgetTime() {
        assertNotNull(Util.getTime(830));
    }

    @Test
    public void convertFloattoPillpopperTime() {
        assertEquals(new PillpopperTime(Util.handleParseLong("830")),Util.convertFloattoPillpopperTime("830"));
    }

    @Test
    public void testisActiveInterruptSession() {
        assertTrue(Util.isActiveInterruptSession());
    }

    @Test
    public void testGetSuffix() {
        assertEquals("st", Util.getSuffix(1));
        assertEquals("nd", Util.getSuffix(2));
        assertEquals("rd", Util.getSuffix(3));
        assertEquals("th", Util.getSuffix(12));
    }

    @Test
    public void testSetOnWeekdays()
    {
        String weekdays = "1";
        assertEquals("Sun", Util.setOnWeekdays(context, weekdays));
        weekdays = "1234567";
        assertEquals("Sun, Mon, Tue, Wed, Thu, Fri, Sat", Util.setOnWeekdays(context, weekdays));
    }
    @Test
    public void  testGetGenericCardsList()
    {
        RunTimeData.getInstance().setAnnouncements(TestUtil.getAnnouncementsResponse());
        assertNotNull(Util.getGenericCardsList(context));
    }

    @Test
    public void testGetGenericBannerList()
    {
        RunTimeData.getInstance().setAnnouncements(TestUtil.getAnnouncementsResponse());
        assertNotNull(Util.getGenericBannerList(context, "Home screen"));
        assertEquals(1, Util.getGenericBannerList(context, "Home screen").size());
    }


    @Test
    public void testGet24FormatTimeFromHrMin() {
        assertEquals("09:16", Util.get24FormatTimeFromHrMin(9, 16));
        assertEquals("12:10", Util.get24FormatTimeFromHrMin(12, 10));
        assertNotEquals("9:10", Util.get24FormatTimeFromHrMin(9, 10));
    }

    @Test
    public void testGetAmPmTimeFromHrMin() {
        assertNotEquals("09:16" + " " + Util.getSystemAMFormat(), Util.getAmPmTimeFromHrMin(9, 16));
        assertEquals("9:16" + " " + Util.getSystemAMFormat(), Util.getAmPmTimeFromHrMin(9, 16));
        assertEquals("9:16" + " " + Util.getSystemPMFormat(), Util.getAmPmTimeFromHrMin(21, 16));
        assertNotEquals("12:00" + " " + Util.getSystemPMFormat(), Util.getAmPmTimeFromHrMin(0, 0));
        assertEquals("12:00" + " " + Util.getSystemAMFormat(), Util.getAmPmTimeFromHrMin(0, 0));
    }

    @Test
    public void testConvertTimeTo24HrFormat() {
        assertEquals("09:00", Util.convertTimeTo24HrFormat(900));
        assertNotEquals("9:00", Util.convertTimeTo24HrFormat(900));
        assertEquals("21:30", Util.convertTimeTo24HrFormat(2130));
    }

    @Test
    public void testConvertTimeTo12HrFormat() {
        assertNotEquals("09:00" + " " + Util.getSystemAMFormat(), Util.convertTimeTo12HrFormat(900));
        assertEquals("9:00" + " " + Util.getSystemAMFormat(), Util.convertTimeTo12HrFormat(900));
        assertEquals("9:16" + " " + Util.getSystemPMFormat(), Util.convertTimeTo12HrFormat(2116));
        assertEquals("12:00" + " " + Util.getSystemAMFormat(), Util.convertTimeTo12HrFormat(0));
    }
}
