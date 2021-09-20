package com.montunosoftware.pillpopper.android.util;

/**
 * Created by M1028309 on 10/12/2017.
 */

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import androidx.test.core.app.ApplicationProvider;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class)
public class DSTUtilityAPITest {

    private  final String START = "start";
    private  final String END = "end";
    private  final String LAST_TAKEN = "last_taken";
    private  final String EFF_LAST_TAKEN = "eff_last_taken";
    private  final String NOTIFY_AFTER = "notify_after";
    private  final String SCHEDULE_DATE = "scheduleDate";
    private  final String PILLPOPPER_REQUEST = "pillpopperRequest";
    private  final String PILLPOPPER_MULTI_REQUEST = "pillpopperMultiRequest";
    private  final String REQUEST_ARRAY = "requestArray";
    private  final String PREFERENCES = "preferences";
    private  final String MISSED_DOSES_LAST_CHECKED = "missedDosesLastChecked";
    private final String isScheduleAddedOrUpdated = "isScheduleAddedOrUpdated";
    private final String tz_secs = "_tz_secs";
    private final String pillId = "pillId";


    public static final String LAST_TAKEN_TZSECS = "last_taken";
    public static final String EFF_LAST_TAKEN_TZSECS = "eff_last_taken";
    public static final String NOTIFY_AFTER_TZSECS = "notify_after";
    public static final String MISSED_DOSES_LAST_CHECKED_TZSECS = "missedDosesLastChecked";
    public static final String SCHEDULECHANGED_TZSECS = "scheduleChanged_tz_secs";
    private ArrayList<String> requestDSTParams, requestTZParams;


    /*@Test
    public void testDateConvertionISOToLong(){
        String isoDate="2017-10-12T15:49:01";
        String longDate= Util.convertDateIsoToLong(isoDate);
        Assert.assertEquals("1507803541",longDate);
    }

    @Test
    public void testDateConvertionLongTOIso(){
        String longDate="1507803541";
        String isoDate=Util.convertDateLongToIso(longDate);
        Assert.assertEquals("2017-10-12T15:49:01",isoDate);
    }*/

    @Test
    public void testConvertHHMMToTime(){
        String hhmm="830";
        String time=Util.convertHHMMtoTimeFormat(hhmm);
        Assert.assertEquals("08:30:00",time);
    }

    @Test
    public void testConvertTimeToHHMM(){
        String time="08:30:00";
        String hhmm=Util.convertTimeFormatToHHMM(time);
        Assert.assertEquals("830",hhmm);
        System.out.println("Time ="+TimeZone.getDefault().getID()+"===" );
        System.out.println("Time ="+TimeZone.getDefault().getRawOffset());
    }


    @Test
    public void testAdditionOFTZsecParamsInRequest(){
        LogEntryModel logEntryModel =new LogEntryModel();
        try {
            logEntryModel.setEntryJSONObject(TestUtil.getJsonObject("/DST_Request"), ApplicationProvider.getApplicationContext());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        checkPresenceOfTzSecsParams(logEntryModel.getEntryJSONObject());

    }

    private void initTZParams() {
        requestTZParams =null;
        requestTZParams = new ArrayList<>();
        requestTZParams.add(LAST_TAKEN_TZSECS);
        requestTZParams.add(EFF_LAST_TAKEN_TZSECS);
        requestTZParams.add(NOTIFY_AFTER_TZSECS);
        requestTZParams.add(MISSED_DOSES_LAST_CHECKED_TZSECS);
    }

    private void initDSTParams() {
        requestDSTParams =null;
        requestDSTParams = new ArrayList<>();
        requestDSTParams.add(START);
        requestDSTParams.add(END);
        requestDSTParams.add(LAST_TAKEN);
        requestDSTParams.add(EFF_LAST_TAKEN);
        requestDSTParams.add(NOTIFY_AFTER);
        requestDSTParams.add(SCHEDULE_DATE);
    }

    private JSONObject checkPresenceOfTzSecsParams(JSONObject entryJSONObject) {
        JSONObject pillRequest = null;
        JSONArray jsonArray = null;
        try {
            if (entryJSONObject.optJSONObject(PILLPOPPER_REQUEST) != null) {
                pillRequest = entryJSONObject.optJSONObject(PILLPOPPER_REQUEST);
                applyISOFormat(pillRequest);
            } else if (entryJSONObject.optJSONObject(PILLPOPPER_MULTI_REQUEST) != null) {
                jsonArray = entryJSONObject.optJSONObject(PILLPOPPER_MULTI_REQUEST).optJSONArray(REQUEST_ARRAY);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.opt(i);
                    applyISOFormat(obj);
                }
            }

        } catch (JSONException je) {
            PillpopperLog.exception(je.getMessage());
        }catch (Exception e){
            PillpopperLog.exception(e.getMessage());
        }
        return entryJSONObject;
    }

    private void applyISOFormat(JSONObject pillRequest) throws JSONException {
        final String pillId = String.valueOf(pillRequest.get(this.pillId));
        HashMap<String,String> tzMap= DatabaseUtils.getInstance(ApplicationProvider.getApplicationContext()).getTimeZoneOffsetsFor(pillId);

        for (String param : requestDSTParams) {
            if (pillRequest.opt(param) != null) {
                Assert.assertTrue(pillRequest.get(param).toString().contains("T"));

                if (requestTZParams.contains(param) && tzMap!=null){
                    Assert.assertTrue( pillRequest.get(param).toString().contains((tz_secs))
                    );
                }
            }
        }

        JSONObject preferences = pillRequest.optJSONObject(PREFERENCES);

        if (pillRequest.opt(isScheduleAddedOrUpdated)!=null && pillRequest.optBoolean(isScheduleAddedOrUpdated)==true && preferences!=null){
            Assert.assertTrue(preferences.get(SCHEDULECHANGED_TZSECS)!=null);
           // DatabaseUtils.getInstance(ApplicationProvider.getApplicationContext()).updateScheduleDateChangedTZsecs(pillId,String.valueOf(Util.getTzOffsetSecs(TimeZone.getDefault())));
        }

        if(preferences!=null && preferences.has(MISSED_DOSES_LAST_CHECKED)){
            Assert.assertTrue(preferences.getString(MISSED_DOSES_LAST_CHECKED).contains("T"));

            if (tzMap!=null ){
                Assert.assertTrue(preferences.opt(MISSED_DOSES_LAST_CHECKED.concat(tz_secs))!=null);
            }

        }
    }

}
