package org.kp.tpmg.mykpmeds.activation.util;

/**
 * Created by M1023050 on 8/2/2018.
 * This is to store all the configuration properties OR mock data which we are using in all unit test cases in test folder.
 * This data needs to be uptodate with the mocking data base which we are using inside the test/resources folder.
 * As and when the database is changed, this configuration file needs to be updated with the corresponding mock data.
 * Ex: pillID, userID, HistoryEntryID etc.
 */

public class TestConfigurationProperties {


    public static final int BUILD_SDK_VERSION = 28;


    //CurrentReminderCardTest.java test file mock data.
    public static final String MOCK_REMINDER_TIME = "1501900200";
    public static final String MOCK_HARDWARE_ID = "D08D3139B631D89840EA2496D1B06981659A04F819B59EB736187618B6D6123A";


    //LateReminderCardTest.java test file mock data.
    public static final String MOCK_USER_ID = "7a8e7f1974cfd85b810f319886d1042f1c";
    public static final String MOCK_LATE_REMINDER_USER_NAME = "Wppmrnjhjjgddacgbfn";
    public static final String MOCK_LATE_REMINDER_TIME = "1532761200";

    // mock KPHC PIll
    public static final String MOCK_KPHC_PILL_ID = "54d15deab911c7529381b5eb22dbeed10";

    // mock history event schedule time
    public static final String MOCK_HISTORY_EVENT_SCHEDULE_TIME = "2018-07-10T16:00:00";



    //ArchiveDetailActivityTest.java test file mock data.
    public static final String MOCK_ARCHIVE_DETAIL_INTENT_PILL_ID = "54536dbb2ee6fdb26ac34042d1884fe1";
    public static final String MOCK_ARCHIVE_DETAIL_INTENT_PILL_IMAGE_GUID = "2e7ce5016184f9c4efde110e20bcaa38b";


    //DrugDetailActivityTest.java test file mock data.
    public static final String MOCK_DRUG_DETAILS_OTC_PILL_ID = "EB2984EC-B1B4-4DFD-812B-DCEE537A75C9";
    public static final String MOCK_DRUG_DETAILS_KPHC_PILL_ID = "727a99b9b918d7867a75f49b2a9c3065";
    public static final String MOCK_DRUG_DETAILS_SCHEDULED_PILL_ID = "c05a104c7b602823eeceb9ba622ab695e";


    //HistoryEditActivityTest.java test file mock data.
    public static final String MOCK_HISTORY_HISTORY_EVENT_GUID = "aa537479dd3ac4d113418a3a5e4102ffc";


    //ReminderRedesignTest.java test file mock data.
    public static final String MOCK_REMINDER_REDESIGN_USERID = "9ce37661b59e89727a0c11bd6474b127";
    public static final String MOCK_REMINDER_REDESIGN_PILLID = "e07c5b98b8b810c3e71dd5c8351c56419";
    public static final String MOCK_REMINDER_REDESIGN_PROXY_NAME = "Subscriber";
    public static final String MOCK_REMINDER_REDESIGN_PROXY_ID = "397bc6438ff4233986f7fb29a195b67d";

    //UtilTest.java
    public static final String MOCK_UTIL_PILL_ID = "81DE681E-F1B8-4EAB-8956-B2C5C6DA2003";
    public static final long MOCK_LONG_TIME = 1532784600;


    //ImageSynUtil.java
    public static final String MOCK_IMAGE_GUID = "2e7ce5016184f9c4efde110e20bcaa38b";
    public static final String MOCK_PILL_ID = "54536dbb2ee6fdb26ac34042d1884fe1";


    //RefillReminderDBUtils.java
    public static final String MOCK_REFILL_REMINDER_GUID = "472E8DF2-2C94-4A0B-9E6D-D05220F01A5A";
    public static final String MOCK_REFILL_REMINDER_TABLE_NAME = "MMRefillReminder";
    public static final String MOCK_REFILL_REMINDER_NEXT_REMINDER_TIME = "1532782800";

    // Firebase Analytics mock data
    public static final String MOCK_EVENT = "battery_optimization_settings";
    public static final String MOCK_EVENT_NAME  = "view_card_home";
    public static final String MOCK_PARAM_NAME  = "card_type";
    public static final String MOCK_PARAM_VALUE  = "Current reminder";
    public static final String MOCK_SCREEN_NAME  = "Sign In";

    //RefillHomeContainerActivity mock data
    public static final boolean launchLocatorFragment = true;

    //SettingManageMenberActivity Mock Data
    public static final String BUNDLE_CONSTANT_MEMBER_OBJ = "MemberObj";

    //DrugDetailVIewActivity
    public static final String ARCHIVE_DETAIL_INTENT_PILL_ID = "1234";
}
