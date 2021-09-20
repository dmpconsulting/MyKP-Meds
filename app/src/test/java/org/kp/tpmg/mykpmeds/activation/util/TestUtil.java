package org.kp.tpmg.mykpmeds.activation.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import com.google.gson.Gson;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionAnswerRequestModel;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.util.JsonParserUtility;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.database.model.GetHistoryPreferences;
import com.montunosoftware.pillpopper.database.model.HistoryEvent;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.database.model.PillList;
import com.montunosoftware.pillpopper.database.model.PillPreferences;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandler;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponse;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.ttg.views.pharmacylocator.model.PharmacyLocatorObj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by M1032896 on 2/10/2017.
 * Mindtree Ltd
 * Raghavendra.dg@mindtree.com
 */
public class TestUtil {


    //LoadFragment method removed instead use "SupportFragmentTestUtil.startFragment()" as used in other test cases

    public static void resetDatabase(){
        try {
            Field instance = DatabaseHandler.class.getDeclaredField("databaseHandler");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            System.err.print(e.getMessage());
        }
    }

    public static void setupTestEnvironment(){
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = "com.montunosoftware.dosecast";
        packageInfo.versionName = "2.0";
        packageInfo.versionCode=2;
        packageInfo.applicationInfo = new ApplicationInfo();
        packageInfo.applicationInfo.packageName = "com.montunosoftware.dosecast";
        Bundle bundle = new Bundle();
        bundle.putString("server_url","https://dv1.mydoctor.kaiserpermanente.org/pillpopper");
        bundle.putBoolean("com.montunosoftware.pillpopper.loggingFlg",false);
        bundle.putString("server_url","https://dv1.mydoctor.kaiserpermanente.org/pillpopper");
        packageInfo.applicationInfo.metaData = bundle;
       // ShadowApplicationPackageManager shadowPackageManager = shadowOf(ApplicationProvider.getApplicationContext().getPackageManager());
       // shadowPackageManager.addPackage(packageInfo);
       // RuntimeEnvironment.setRobolectricPackageManager(RuntimeEnvironment.getRobolectricPackageManager());
        SharedPreferenceManager.getInstance(ApplicationProvider.getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME).putString("drugcount","1",false);
        SharedPreferenceManager.getInstance(ApplicationProvider.getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME).putString(AppConstants.ISNEW_USER,"false",false);
        SharedPreferenceManager.getInstance(ApplicationProvider.getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME).putString(AppConstants.DEVICE_SWITCH_FLAG,"false",false);
        SharedPreferenceManager.getInstance(ApplicationProvider.getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME).putBoolean("isLaunchingAfterTutorials", true, false);
        SharedPreferenceManager.getInstance(ApplicationProvider.getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME).putString("QuickviewToBeShown", "1", true);
        SharedPreferenceManager.getInstance(ApplicationProvider.getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME).putBoolean("NeedToInvokeCreateSchedule", true, true);

    }

    public static JSONObject readFromFile(InputStream inputStream) {
        JSONObject jsonResponse = null;
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                sb.append(mLine);
            }
        } catch (IOException e) {
            System.err.print(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.print(e.getMessage());
                }
            }
        }
        try {
            jsonResponse = new JSONObject(sb.toString());
        } catch (JSONException e) {
            System.err.print(e.getMessage());
        }
        return jsonResponse;
    }

    public static String readFromResource(String fileName){
        StringBuilder response = new StringBuilder();
        try{
            String path = TestUtil.class.getResource("/"+fileName).toURI().getPath();
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String data = null;
            while ((data = br.readLine())!=null) {
                response.append(data);
            }
            br.close();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return  response.toString();
    }

    public static boolean isDeviceSwitch() {
        return false;
    }

    public static JSONObject getJsonObject(String filename) throws URISyntaxException, FileNotFoundException {
        String path = TestUtil.class.getResource(filename).toURI().getPath();
        File file = new File(path);
        return readFromFile(new FileInputStream(file));
    }

    public  static void setRegistrationResponse(String filename){
        try {
            JSONObject response = getJsonObject(filename);
            Gson gson = new Gson();
            SignonResponse result = gson.fromJson(response.toString(), SignonResponse.class);
            RunTimeData.getInstance().setRegistrationResponse(result);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


    public static RefillReminder prepareMockOverDueRefillReminder(){
        RefillReminder refillReminder = new RefillReminder();
        refillReminder.setReminderGuid("19B77846-E85D-4689-9577-D95089C55433");
        refillReminder.setUserId("");
        refillReminder.setRecurring(true);
        refillReminder.setFrequency(1);
        refillReminder.setReminderEndDate("");
        refillReminder.setReminderEndTzSecs(null);
        refillReminder.setNextReminderDate("1532871000");
        refillReminder.setOverdueReminderDate("1532784600");
        refillReminder.setOverdueReminderTzSecs(null);
        refillReminder.setLastAcknowledgeDate(null);
        refillReminder.setLastAcknowledgeTzSecs(null);
        refillReminder.setReminderNote("recuuring refill reminder for every 1 day at 9 PM");
       return refillReminder;
    }


    public static LogEntryModel prepareCreatePillEntry(PillList addPill, Context context) {
        String replyId = Util.getRandomGuid();
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        logEntryModel.setReplyID(replyId);
        PillPreferences pref = new PillPreferences();
        pref.setImageGUID("12f23cdde4ddfg3");
        pref.setNotes("sample Notes");
        addPill.setPreferences(pref);
        JSONObject jsonObj = prepareEntryObject("CreatePill", replyId, addPill);
        logEntryModel.setEntryJSONObject(jsonObj,context);
        return logEntryModel;
    }

    public static JSONObject prepareEntryObject(String action, String replyId, PillList pill) {
        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        try {
            pillRequest.put("action", action);
            pillRequest.put("replayId", replyId);
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("deviceToken", "");
            pillRequest.put("dose", pill.getDose());
            pillRequest.put("name", pill.getName());
            pillRequest.put("type", "interval");
            pillRequest.put("clientVersion", "4.2");
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillRequest.put("pillId", pill.getPillId());
            pillRequest.put("interval", 0);
            pillRequest.put("hardwareId", TestConfigurationProperties.MOCK_HARDWARE_ID);
            pillRequest.put("userId", TestConfigurationProperties.MOCK_USER_ID);
            pillRequest.put("targetUserId", pill.getUserId());
            pillRequest.put("created", PillpopperTime.now().getGmtSeconds());
            pillRequest.put("isScheduleAddedOrUpdated", false);
            pillPrefRequest.put("limitType", "0");
            pillPrefRequest.put("customDosageID", "");
            pillPrefRequest.put("refillsRemaining", "-1");
            pillPrefRequest.put("personId", pill.getUserId());
            pillPrefRequest.put("invisible", "0");
            pillPrefRequest.put("archived", "0");
            pillPrefRequest.put("refillAlertDoses", "");
            pillPrefRequest.put("notes", pill.getPreferences().getNotes());
            pillPrefRequest.put("refillQuantity", "");
            pillPrefRequest.put("customDescription", pill.getDose());
            pillPrefRequest.put("noPush", "0");
            pillPrefRequest.put("remainingQuantity", "");
            pillPrefRequest.put("secondaryReminders", "1");
            pillPrefRequest.put("maxNumDailyDoses","-1");
            pillPrefRequest.put("dosageType", PillpopperConstants.DOSAGE_TYPE_CUSTOM);
            pillPrefRequest.put("imageGUID",pill.getPreferences().getImageGUID());
            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);
        } catch (JSONException e) {
            PillpopperLog.say("Oops! Exception while preparing the request object while creating the logentry for : " + action, e);
        }
        return pillpopperRequest;
    }

    public static User prepareUserObject(){
        User user = new User();
        user.setDisplayName("Wppmrnjhjjgddacgbfn  Wppmrnidhfefffdieln");
        user.setEnabled("Y");
        user.setFirstName("WPPMRNJHJJGDDACGBFN");
        user.setGenderCode("M");
        user.setHasSyncChanges(true);
        user.setLastName("Wppmrnidhfefffdieln");
        user.setLastSyncToken("1532781660448");
        user.setMiddleName("");
        user.setMrn("");
        user.setNickName("");
        user.setRelationDesc("");
        user.setRelId("");
        user.setUserId("7a8e7f1974cfd85b810f319886d1042f1c");
        user.setUserType("Primary");
        user.setSelected(false);
        return user;
    }

    public static String ANNOUNCEMENTS_RESPONSE = "{\n" +
            "  \"announcements\": [\n" +
            "    {\n" +
            "      \"type\": \"banner_full\",\n" +
            "      \"retention\": \"hard\",\n" +
            "      \"id\": 101,\n" +
            "      \"priority\": 1,\n" +
            "      \"baseScreen\": \"Home screen\",\n" +
            "      \"iconName\": \"banner_alert\",\n" +
            "      \"regions\": [\n" +
            "        \"NCAL\",\n" +
            "        \"MAS\"\n" +
            "      ],\n" +
            "      \"title\": \"Important Information\",\n" +
            "      \"subTitle\": \"from Kaiser Permanente\",\n" +
            "      \"message\": \"This is an important announcement\",\n" +
            "      \"buttons\": [\n" +
            "        {\n" +
            "          \"label\": \"Find your Pharmacy\",\n" +
            "          \"order\": 1,\n" +
            "          \"button_color\": \"FFFFFF\",\n" +
            "          \"font_color\": \"006BA6\",\n" +
            "          \"action\": \"tab\",\n" +
            "          \"destination\": \"Find a Pharmacy\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"label\": \"OK\",\n" +
            "          \"order\": 2,\n" +
            "          \"button_color\": \"006BA6\",\n" +
            "          \"font_color\": \"FFFFFF\",\n" +
            "          \"action\": \"ack\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"home_card\",\n" +
            "      \"retention\": \"soft\",\n" +
            "      \"id\": 102,\n" +
            "      \"priority\": 2,\n" +
            "      \"regions\": [\n" +
            "        \"NCAL\",\n" +
            "        \"MAS\"\n" +
            "      ],\n" +
            "      \"title\": \"More important information\",\n" +
            "      \"subTitle\": \"from Kaiser Permanente\",\n" +
            "      \"message\": \"You don't have permission to use this feature at this time. For more information, contact Member Services at <tel>1-800-556-7677</tel>.\",\n" +
            "      \"buttons\": [\n" +
            "        {\n" +
            "          \"label\": \"Visit kp.org\",\n" +
            "          \"order\": 1,\n" +
            "          \"button_color\": \"FFFFFF\",\n" +
            "          \"font_color\": \"006BA6\",\n" +
            "          \"action\": \"link\",\n" +
            "          \"url\": \"https://kp.org\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"label\": \"OK\",\n" +
            "          \"order\": 2,\n" +
            "          \"button_color\": \"006BA6\",\n" +
            "          \"font_color\": \"FFFFFF\",\n" +
            "          \"action\": \"ack\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static AnnouncementsResponse getAnnouncementsResponse()
    {
        return JsonParserUtility.getInstance().parseJson(ANNOUNCEMENTS_RESPONSE,
                AnnouncementsResponse.class);
    }

    public static List<HistoryEvent> historyEventMockData() {

        List<HistoryEvent> historyEventList = new ArrayList<>();

        HistoryEvent historyEvent = new HistoryEvent();
        historyEvent.setDrugId("");
        historyEvent.setDosage("abc");
        historyEvent.setNotes("Take Medicine");
        historyEvent.setHeaderTime("12376849");
        historyEvent.setHistoryEventGuid("aa537479dd3ac4d113418a3a5e4102ffc");
        historyEvent.setOperationStatus("takePill");
        historyEvent.setPillName("medic");
        historyEvent.setActionType(PillpopperConstants.ACTION_TAKE_PILL_HISTORY);
        GetHistoryPreferences preferences = new GetHistoryPreferences();
        preferences.setDayperiod("3");
        preferences.setScheduleFrequency("CD");
        historyEvent.setPreferences(preferences);
        HistoryEvent historyEvent2 = new HistoryEvent();
        historyEvent2.setDrugId("");
        historyEvent2.setDosage("xyz");
        historyEvent2.setNotes("Take Medicine");
        historyEvent2.setHeaderTime("12376860");
        historyEvent2.setActionType(PillpopperConstants.ACTION_SKIP_PILL_HISTORY);
        historyEvent2.setHistoryEventGuid("aa537479dd3ac4d113418a3a5e4102ffc");
        historyEvent2.setOperationStatus("skipPill");
        historyEvent2.setPillName("medicine");
        GetHistoryPreferences preferences2 = new GetHistoryPreferences();
        preferences2.setDayperiod("21");
        preferences2.setScheduleFrequency("CW");
        historyEvent2.setPreferences(preferences);
        historyEventList.add(0, historyEvent);
        historyEventList.add(1, historyEvent2);

        return historyEventList;
    }
    public static List<PharmacyLocatorObj> pharmacyListMockData() {
        List<PharmacyLocatorObj> pharmacyList = new ArrayList<>();
        PharmacyLocatorObj pharmacyLocatorObj = new PharmacyLocatorObj();
        pharmacyLocatorObj.setDeptId("100001");
        pharmacyLocatorObj.setDepartmentName("Inland Valley Medical Center");
        pharmacyLocatorObj.setState("CA");
        pharmacyLocatorObj.setStreet("36485 Inland Valley Drive");
        pharmacyLocatorObj.setCityStateAndZip("92595");
        pharmacyLocatorObj.setLatitude(33.590892);
        pharmacyLocatorObj.setLongitude(-117.236934);
        pharmacyLocatorObj.setFormattedHours("open 24 hours");
        pharmacyLocatorObj.setIsPreferredFacility(1);
        pharmacyLocatorObj.setRefillableOnline("NCAL");
        pharmacyLocatorObj.setCity("Wildomar");
        PharmacyLocatorObj pharmacyLocatorObj1 = new PharmacyLocatorObj();
        pharmacyLocatorObj1.setDeptId("100012");
        pharmacyLocatorObj1.setDepartmentName("Valley Medical Center");
        pharmacyLocatorObj1.setState("CA");
        pharmacyLocatorObj1.setStreet("36485 Inland Valley Drive");
        pharmacyLocatorObj1.setCityStateAndZip("92595");
        pharmacyLocatorObj1.setLatitude(33.590892);
        pharmacyLocatorObj1.setLongitude(-117.236934);
        pharmacyLocatorObj1.setFormattedHours("open 24 hours");
        pharmacyLocatorObj1.setIsPreferredFacility(1);
        pharmacyLocatorObj1.setRefillableOnline("NCAL");
        pharmacyLocatorObj1.setCity("Wildomar");
        pharmacyList.add(pharmacyLocatorObj);
        pharmacyList.add(pharmacyLocatorObj1);
        return pharmacyList;
    }

    public static List<SecretQuestionAnswerRequestModel> prepareSecretQuestionAnswerMockData() {
        SecretQuestionAnswerRequestModel model1 = new SecretQuestionAnswerRequestModel();
        model1.setGroupId("101");
        model1.setQuestionId("1");
        model1.setAnswerText("Answer1");
        model1.setQuestionText("Question1");
        SecretQuestionAnswerRequestModel model2 = new SecretQuestionAnswerRequestModel();
        model2.setGroupId("102");
        model2.setQuestionId("2");
        model2.setAnswerText("Answer2");
        model2.setQuestionText("Question2");
        SecretQuestionAnswerRequestModel model3 = new SecretQuestionAnswerRequestModel();
        model3.setGroupId("103");
        model3.setQuestionId("3");
        model3.setAnswerText("Answer3");
        model3.setQuestionText("Question3");
        List<SecretQuestionAnswerRequestModel> list = new ArrayList<>();
        list.add(model1);
        list.add(model2);
        list.add(model3);
        return list;
    }

}
