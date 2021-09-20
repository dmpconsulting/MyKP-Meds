package org.kp.tpmg.mykpmeds.activation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import com.montunosoftware.mymeds.BuildConfig;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.io.File;

@SuppressLint("SdCardPath")
public class AppConstants {
    public static final String EMPTY_STRING = "";
    public static final String ANDROID_OS_VERSION = android.os.Build.VERSION.RELEASE;
    public static final String ANDROID_DEVICE_MAKE = android.os.Build.MODEL;
    public static final String ANDROID_DEVICE_MANUFACTURER = android.os.Build.MANUFACTURER;
    public static final String PHONE_OS = "android";
    public static final String PHONE_ANDROID = "Android ";
    public static final String APP_PROFILE_INVOKED_TIMESTAMP = "appProfileInvokedTimeStamp";
    public static final int APP_PROFILE_TIMER = 15;
    public static final int DOWNLOAD_EXPIRY_LIMIT = 15;

    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";
    public static final String TOKEN_TYPE_KEY = "token_type";
    public static final String TOKEN_EXPIRES_IN = "expiry_in";

    // Use This Config flag for enabling or disabling the Native RxRefill Prescription Implementation.
    // Make this flag as True if require native implementation, False if require existing refill webview implementation
    public static boolean IS_NATIVE_RX_REFILL_REQUIRED = true;
    public static boolean IS_FIREBASE_CLOUD_MESSAGING_ENABLED = true;

    public static boolean IS_IN_EXPANDED_HOME_CARD = false;
    public static boolean isFromInAppAlerts= false;

    public static final String APP_ID = "MD-5";
    public static final String AUTH_CODE_PREF_NAME = "authcodePrefNew";
    public static final String AUTH_CODE_PREF_OLD = "authcodePref";
    public static final String MRN = "mrn";
    public static final String USERID = "userId";
    public static final String KP_GUID = "kpGUID";

    public static final String USER_NAME = "userName";
    public static final String INTRO_COMPLETE_FL = "introCompleteFl";
    public static final String DEVICE_SWITCH_FLAG = "switchDeviceFlag";
    public static final String SSO_SESSION_ID = "ssoSessionId";
    public static final String FLAG_DISABLE_REFILL_ALERT = "flgDisableRefillAlert";
    public static final String LAST_MEMBERMEDS_SYNC_TIME = "lastMemberMedsSyncTime";
    public static final String ISNEW_USER = "isNewuser";
    public static final String URL_DEVICE_ID_STRING = "&deviceId=";
    public static final String HTTP_DATA_ERROR = "ERROR";
    public static final String REMEMBER_USER_ID = "rememberUserId";
    public static final int TIMEOUT_PERIOD = 15 * 60 * 1000;
    public static final int SESSION_DURATION = 3 * 60 * 1000;
    public static final boolean ERROR = true;
    public static final boolean NOT_ERROR = false;
    public static final String TUTORIALS_COMPLETE_STATUS_YES = "Y";
    public static final int DEVICE_SWITCH_STATUSCODE = 101;
    public static final int USER_SWITCH_STATUSCODE = 102;
    public static final int DEVICE_USER_SWITCH_STATUSCODE = 103;
    public static final String APPLOCKEDOUT = "Lockedout";
    private static int WRONG_LOGIN_ATTEPMTS = 0;
    public static final int ACCOUNT_LOCKEDOUT_CODE = 6;
    public static final int INVALIDCREDENTIALS = 5;
    public static final int ACCOUNT_TEEN_PRIMARY = 9;
    private static final boolean logging = false;
    public static final String MID_NIGHT = "0:00";
    public static final String MID_DAY = "12:00";
    public static final String URL_BASE_PARAMETERS = "os=" + PHONE_OS
            + "&useragentType=" + ANDROID_DEVICE_MAKE + "&osVersion="
            + ANDROID_OS_VERSION + "&appId=" + APP_ID;


    // App Profile Keys
    public static final String KEY_SIGN_IN_URL = "signon";
    public static final String KEY_KEEP_ALIVE_URL = "keepalive";
    public static final String KEY_KP_SSO_COOKIE_DOMAIN = "kpSSOCookieDomain";
    public static final String KEY_KP_SSO_COOKIE_PATH = "kpSSOCookiePath";
    public static final String KEY_KP_SSO_COOKIE_NAME = "kpSSOCookieName";
    private static final String KEY_USABLE_NET_PHARMACY_URL = "usablenetPharmacyURL";
    public static final String KEY_GET_SYSTEM_URL = "systemstatus";
    public static final String KEY_WS_SECURED_BASE_URL = "wsSecuredBaseURL";
    public static final String KEY_WS_NON_SECURED_BASE_URL = "wsNonSecuredBaseURL";
    public static final String KEY_PILL_POPPER_SECURED_BASE_URL = "pillpopperSecureBaseURL";
    public static final String KEY_PILL_POPPER_NON_SECURED_BASE_URL = "pillpopperNonSecureBaseURL";
    public static final String KEY_KP_SSO_COOKIE_STORE_IS_SECURE = "kpSSOCookieSecure";
    private static final String KEY_IMAGE_BASE_URL = "getImageBaseURL";
    private static final String KEY_API_MANAGER_TOKEN_BASE_URL = "apiManagerTokenBaseURL";
    private static final String KEY_NLP_SERVICE_BASE_URL = "NLPServiceBaseURL";
    public static final String KEY_SHOULD_PERFORM_NLP = "shouldPerformNLP";
    public static final String KEY_FCM_ENABLED = "EnablePushNotification";
    public static final boolean SHOULD_SHOW_NLP_UI = true; // local flag

    //App Profile Constants
    public static final String APP_PROFILE_KEEP_ALIVECOOKIE_NAME_KEY = "kpSSOCookieName";
    public static final String APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY = "kpSSOCookieDomain";
    public static final String APP_PROFILE_KEEP_ALIVE_COOKIE_PATH_KEY = "kpSSOCookiePath";
    private static final String APP_PROFILE_KEEP_ALIVE_COOKIE_IS_SECURE_KEY = "kpSSOCookieSecure";
    private static final String APP_PROFILE_USABLE_NET_COOKIE_NAME_KEY = "usablenetCookieName";
    private static final String APP_PROFILE_USABLE_NET_COOKIE_DOMAIN_KEY = "usablenetCookieDomain";
    private static final String APP_PROFILE_USABLE_NET_COOKIE_PATH_KEY = "usablenetCookiePath";
    private static final String APP_PROFILE_USABLE_NET_COOKIE_SECURE_KEY = "usablenetCookieSecure";
    private static final String APP_PROFILE_USABLE_NET_COOKIE_PHARMACY_URL_KEY = "usablenetPharmacyURL";
    private static final String APP_PROFILE_USABLE_NET_FAQ_URL_KEY = "faqURL";

    public static final String APP_PROFILE_REFILL_PRESCRIPTION_URL_KEY = "usablenetPharmacyURL";
    public static final String APP_PROFILE_AEM_REFILL_PRESCRIPTION_URL_KEY = "aemPharmacyURL";
    public static final String USABLE_NET_AEM_SWITCH_KEY = "usablenetAEMSwitch";
    public static final String EXTERNAL_AEM_BROWSER_SWITCH = "aemExtBrowserSwitch";

    public static String baseURL = BuildConfig.BASE_URL; //need to keep this as it will be needed in App profile call

    private static final boolean secureFlg = false;
    public static final String EDITION = "KP";
    public static final String KP_MANAGE_MEMBER_URL = "https://healthy.kaiserpermanente.org/health/mycare/consumer/myprofilehome/myprofile/act-for-family-members";

    // Signon Constants
    public static final String USER_AGENT_CATEGORY = "A";
    public static final String APPNAME = "MyKPMeds";
    public static final String OS_VERSION = android.os.Build.VERSION.RELEASE;
    public static String APIKEY = BuildConfig.API_KEY;
    public static String APIKEY_PR = "edadf58a-0fa7-48c3-8be9-b4b6530b6483";
    private static final String MEMBER_REGION_MRN = "MRN";

    // certificate CN and ORG Names
    public static final String CN_NAME = "kaiserpermanente.org";
    public static final String CN_PERMANENTE = "permanente.net";
    public static final String CN_NAME_PHARMACY_DB = "kplocator.kp.org";
    public static final String ORG_TPMG_NAME = "The Permanente Medical Group";
    public static final String ORG_FOUNDATION_HEALTH_PLAN_NAME = "Foundation Health Plan";
    public static final String ORG_KAISER_PERMANENTE_NAME = "Kaiser Permanente";

    //Opt_in Constants
    public static final String FLAG_OPT_IN_FROM_NOTIFICATION = "optinnotificationscreen";
    public static final String FLAG_OPT_IN_FIRST_TIME = "optinfirsttime";
    private static boolean byPassLogin = false;
    private static boolean isTappedMedication = false;
    private static boolean IS_OPTED_IN_AND_PENDING_NOTIFICATION_IS_THERE = false;
    public static final String IS_FRESHINSTALL_FLG = "isfreshinstall";
    public static final String QV_USER_PREFERENCE = "qv_user_preference";
    public static final String APP_UPGRADE_ALERT_TIMESTAMP = "upgradeAlertTimeStamp";

    public static final boolean INCLUDE_NOTIFICATION_ACTIONS_FEATURE = true;

    public static final String ACTION_REFRESH = "com.montunosoftware.pillpopper.android.REFRESH";

    public static final String WELCOME_SCREEN_DISPLAY_COUNTER = "WelcomeScreenDisplayCounter";
    public static final String FDB_IMAGE_CARD_DISPLAY_COUNTER = "FdbCardDisplayCounter";
    public static final String FDB_IMAGE_CARD_DISPLAY_CHOICE = "FdbCardDisplayChoice";
    public static final long WELCOME_SCREEN_DISPLAY_MAX_LIMIT = 2;
    public static final String SETUP_COMPLETE_FL = "setupCompleteFl";
    private static String WELCOME_SCREENS_DISPLAY_RESULT = "-1";

    public static void setFdbScreenDisplayResult(String fdbScreenDisplayResult) {
        FDB_SCREEN_DISPLAY_RESULT = fdbScreenDisplayResult;
    }

    private static String FDB_SCREEN_DISPLAY_RESULT = "-1";

    public static final String EXPANDED_CARD_INDEX_KEY = "expandedCardIndex";

    public static final String HISTORY_OPERATION_EMPTY = "EMPTY";

    //KPHC FBD Images constants
    public static final String IMAGE_CHOICE_UNDEFINED = "UNDEFINED";
    public static final String IMAGE_CHOICE_FDB = "FDB";
    public static final String IMAGE_CHOICE_CUSTOM = "CUSTOM";
    public static final String IMAGE_CHOICE_NO_IMAGE = "NOIMAGE";
    public static final String IMAGE_NOT_FOUND = "NOTFOUND";
    public static final String IMAGE_NEED_FDB_UPDATE = "NEEDFDBUPDATE";

    public static final String APP_PROFILE_SIGNON_HARD_INTERRUPT_URL_KEY = "kpHardInterruptUrl";
    public static final String APP_PROFILE_SIGNON_SECRECT_QUESTION_URL_KEY = "kpSecretQuestionsUrl";

    public static void setWrongLoginAttepmts(int wrongLoginAttepmts) {
        WRONG_LOGIN_ATTEPMTS = wrongLoginAttepmts;
    }

    public static boolean isLogging() {
        return logging;
    }

    public static boolean isSecureFlg() {
        return secureFlg;
    }

    public static String getAPIKEY() {
        return APIKEY;
    }

    public static boolean isByPassLogin() {
        return byPassLogin;
    }

    public static void setByPassLogin(boolean byPassLogin) {
        AppConstants.byPassLogin = byPassLogin;
    }

    public static void setIsTappedMedication(boolean isTappedMedication) {
        AppConstants.isTappedMedication = isTappedMedication;
    }

    public static void setIsOptedInAndPendingNotificationIsThere(boolean isOptedInAndPendingNotificationIsThere) {
        IS_OPTED_IN_AND_PENDING_NOTIFICATION_IS_THERE = isOptedInAndPendingNotificationIsThere;
    }

    public static String getWelcomeScreensDisplayResult() {
        return WELCOME_SCREENS_DISPLAY_RESULT;
    }

    public static void setWelcomeScreensDisplayResult(String welcomeScreensDisplayResult) {
        WELCOME_SCREENS_DISPLAY_RESULT = welcomeScreensDisplayResult;
    }

    public static class ConfigParams {

        public static final String productionActivationUrl = "https://m.kp.org/mt/healthy.kaiserpermanente.org/sign-on.html";
        public static final String nonProductionActivationUrl = "https://kp3-nat-qa.usdk.net/mt/hpp.kaiserpermanente.org/sign-on.html";

        public static String getRefillCookieName() {
            return Util.getKeyValueFromAppProfileRuntimeData(APP_PROFILE_USABLE_NET_COOKIE_NAME_KEY);
        }

        public static String getRefillCookieDomain() {
            return Util.getKeyValueFromAppProfileRuntimeData(APP_PROFILE_USABLE_NET_COOKIE_DOMAIN_KEY);
        }

        public static String getRefillCookiePath() {
            return Util.getKeyValueFromAppProfileRuntimeData(APP_PROFILE_USABLE_NET_COOKIE_PATH_KEY);
        }

        public static String getRefillCookieSecure() {
            return Util.getKeyValueFromAppProfileRuntimeData(APP_PROFILE_USABLE_NET_COOKIE_SECURE_KEY);
        }

        public static String getKeepAliveCookieName() {
            return Util.getKeyValueFromAppProfileRuntimeData(APP_PROFILE_KEEP_ALIVECOOKIE_NAME_KEY);
        }

        public static String getKeepAliveCookieDomain() {
            return Util.getKeyValueFromAppProfileRuntimeData(APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY);
        }

        public static String getKeepAliveCookiePath() {
            return Util.getKeyValueFromAppProfileRuntimeData(APP_PROFILE_KEEP_ALIVE_COOKIE_PATH_KEY);
        }

        public static String getKeepAliveCookieIsSecure() {
            return Util.getKeyValueFromAppProfileRuntimeData(APP_PROFILE_KEEP_ALIVE_COOKIE_IS_SECURE_KEY);
        }

        public static String getWsSecuredBaseURL() {
            String wsSecuredBaseURL = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_WS_SECURED_BASE_URL);
            if (!Util.isEmptyString(wsSecuredBaseURL)) {
                return wsSecuredBaseURL;
            }
            return "";
        }

        public static String getWsNonSecuredBaseURL() {
            String wsNonSecuredBaseURL = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_WS_NON_SECURED_BASE_URL);
            if (!Util.isEmptyString(wsNonSecuredBaseURL)) {
                return wsNonSecuredBaseURL;
            }
            return "";
        }

        public static String getWsPillpopperSecuredBaseURL() {
            String pillpopperSecuredBaseURL = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_PILL_POPPER_SECURED_BASE_URL);
            if (!Util.isEmptyString(pillpopperSecuredBaseURL)) {
                return pillpopperSecuredBaseURL;
            }
            return "";
        }

        public static String getWsPillpopperNonSecuredBaseURL() {
            String pillpopperNonSecuredBaseURL = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_PILL_POPPER_NON_SECURED_BASE_URL);
            if (!Util.isEmptyString(pillpopperNonSecuredBaseURL)) {
                return pillpopperNonSecuredBaseURL;
            }
            return "";
        }

        public static String getKpPharmacyURL() {
            String kpPharmacyURL = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_USABLE_NET_PHARMACY_URL);
            if (!Util.isEmptyString(kpPharmacyURL)) {
                return kpPharmacyURL;
            }
            return "";
        }

        public static String getFdbImageURL() {
            String imageBaseURL = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_IMAGE_BASE_URL);
            if (!Util.isEmptyString(imageBaseURL)) {
                return imageBaseURL;
            }
            return "";
        }

        public static String getFaqURL() {
            String faqURL = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_USABLE_NET_FAQ_URL_KEY);
            if (!Util.isEmptyString(faqURL)) {
                return faqURL;
            }
            return "";
        }

        public static String getAPIManagerTokenBaseURL() {
            String apiManagerTokenBaseUrl = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_API_MANAGER_TOKEN_BASE_URL);
            if (!Util.isEmptyString(apiManagerTokenBaseUrl)) {
                return apiManagerTokenBaseUrl;
            }
            return "";
        }

        public static String getNLPRemindersAPIURL() {
            String apiManagerTokenBaseUrl = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_NLP_SERVICE_BASE_URL);
            if (!Util.isEmptyString(apiManagerTokenBaseUrl)) {
                return apiManagerTokenBaseUrl.concat("/extractreminder");
            }
            return "";
        }

        public static String getNLPSigValidationAPIURL() {
            String apiManagerTokenBaseUrl = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_NLP_SERVICE_BASE_URL);
            if (!Util.isEmptyString(apiManagerTokenBaseUrl)) {
                return apiManagerTokenBaseUrl.concat("/schedulevalidation");
            }
            return "";
        }
    }

    public static class StatusCodeConstants {

        //force upgrade error status codes
        public static final int FORCE_UPGRADE_CODE_3 = 3;
        public static final int FORCE_UPGRADE_CODE_101 = 101;
        public static final int APP_MAINTENANCE_STATUS_CODE = 100;
        public static final int SECURITY_BREACH_STATUS_CODE = 110;

    }

    public static final String PRODUCTION_PACKAGE_NAME = "org.kp.tpmg.android.mykpmeds";
    public static final String MARKET_URL = "market://details?id=" + PRODUCTION_PACKAGE_NAME;
    public static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=" + PRODUCTION_PACKAGE_NAME;

    private static final String MONITOR_APP_KEY = "EUM-AAB-AUB";
    private static final String MONITOR_APP_URL = "https://stats.permanente.net";
    public static final String KP_ORG_URL = "https://healthy.kaiserpermanente.org";


    public static final String sharedPrefquickViewFlagKey = "sharedPrefquickViewFlag";

    public static final String POST_METHOD_NAME = "POST";
    public static final String HTTP_METHOD_GET = "GET";

    public static final int LATE_REMINDERS_CONTRACTED_CARD_REFRESH_RESULT_CODE = 999;
    public static final int CURRENT_REMINDERS_CONTRACTED_CARD_REFRESH_RESULT_CODE = 998;

    public static String getAppProfileUrl() {
//        if(ConfigParams.getWsNonSecuredBaseURL()!="") {
//            return new StringBuilder(ConfigParams.getWsNonSecuredBaseURL()).append("/getAppProfile").toString();
//        } else {
        return new StringBuilder(AppConstants.baseURL).append("/getAppProfile").toString();
//        }
    }

    public static String getTermsNConditionsURL(Context context) {
        return new StringBuilder(ConfigParams.getWsNonSecuredBaseURL()).append("/getTermsAndConditions" + "?").append(AppConstants.URL_BASE_PARAMETERS).append("&appVersion=").append(ActivationUtil.getAppVersion(context)).toString();
    }

    public static String getActivationInitSessionRegisterURL() {
        return ConfigParams.getWsSecuredBaseURL() + "/register";
    }

    public static String getActivateMemberStatusURL() {
        return new StringBuilder(ConfigParams.getWsSecuredBaseURL()).append("/activateMemberDevice?").toString();
    }

    public static String getPrivacyPracticeURL(Context context) {
        return new StringBuilder(ConfigParams.getWsNonSecuredBaseURL()).append("/getPrivacyContent" + "?").append(AppConstants.URL_BASE_PARAMETERS).append("&appVersion=").append(ActivationUtil.getAppVersion(context)).toString();
    }

    public static String getSetupCompleteURL() {
        return new StringBuilder(ConfigParams.getWsSecuredBaseURL()).append("/updateSetupCompletionStatus").toString();
    }

    public static String getPillSetProxyEnableURL() {
        return new StringBuilder(ConfigParams.getWsPillpopperSecuredBaseURL()).toString();
    }

    public static String getRegisterURL() {
        return new StringBuilder(ConfigParams.getWsPillpopperSecuredBaseURL()).toString();
    }

    public static String getAppSupportURL(Context context) {
        return new StringBuilder(ConfigParams.getWsNonSecuredBaseURL()).append("/getAppSupport?").append(AppConstants.URL_BASE_PARAMETERS).append("&appVersion=").append(ActivationUtil.getAppVersion(context)).toString();
    }

    public static String getAcknowledgeStatusAPIUrl() {
        return new StringBuilder(ConfigParams.getWsPillpopperSecuredBaseURL()).toString();
    }

    public static String getUpdateAcknowledgeUserFlUrl() {
        return new StringBuilder(ConfigParams.getWsPillpopperNonSecuredBaseURL()).toString();
    }

    public static String getRegisterUrl() {
        String KEY_REGISTER_HELP_URL = "registerurl";
        return Util.getKeyValueFromAppProfileRuntimeData(KEY_REGISTER_HELP_URL);
    }

    public static String getUserIDHelpUrl() {
        String KEY_USER_ID_HELP_URL = "useridhelpurl";
        return Util.getKeyValueFromAppProfileRuntimeData(KEY_USER_ID_HELP_URL);
    }

    public static String getPasswordHelpUrl() {
        String KEY_PASSWORD_HELP_URL = "passwordhelpurl";
        return Util.getKeyValueFromAppProfileRuntimeData(KEY_PASSWORD_HELP_URL);
    }

    public static String getMemberServicesUrl() {
        String KEY_MEMBER_SERVICE_URL = "memberServiceURL";
        return Util.getKeyValueFromAppProfileRuntimeData(KEY_MEMBER_SERVICE_URL);
    }

    public static boolean shouldPerformNLP() {
        try {
            if (!Util.isEmptyString(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_SHOULD_PERFORM_NLP))) {
                return Boolean.parseBoolean(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_SHOULD_PERFORM_NLP));
            }
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
        }
        return false;
    }

    public static boolean shouldEnableFCMPushNotification() {
        try {
            if (!Util.isEmptyString(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_FCM_ENABLED))) {
                return Boolean.parseBoolean(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_FCM_ENABLED));
            }
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
        }
        return false;
    }

    public static final int PERMISSION_CAMERA = 101;
    public static final int PERMISSION_CONTACTS_READ = 102;
    public static final int PERMISSION_CONTACTS_WRITE = 103;
    public static final int PERMISSION_PHONE_CALL_PHONE = 106;
    public static final int PERMISSION_READ_EXTERNAL_STORAGE = 107;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 108;

    public static final String FONT_ROBOTO_BOLD = "Roboto-Bold.ttf";
    public static final String FONT_ROBOTO_REGULAR = "Roboto-Regular.ttf";
    public static final String FONT_ROBOTO_MEDIUM = "Roboto-Medium.ttf";
    public static final String FONT_ROBOTO_LIGHT = "Roboto-Light.ttf";
    public static final String FONT_ROBOTO_ITALIC = "Roboto-Italic.ttf";

    public static final String SIGNED_STATE_REMOVAL = "SignedStateRemoval";
    public static final String SIGNED_OUT_STATE_REMOVAL = "SignedoutStateRemoval";
    public static final String SIGNED_OUT_STATE_REMOVAL_LOGIN_ONCE = "SignedoutStateRemovalLoggedInOnce";
    public static final String LATE_REMINDERS_STATUS_FROM_NOTIFICATION = "PendingPassedReminderStatusFromNotification";
    public static final String IS_LAUNCHING_LATE_AFTER_CURRENT = "launchingLateRemindersAfterCurrent";
    public static final String TIME_STAMP = "Timestamp";
    public static final String BATTERY_OPTIMIZATION_DECISION = "BatteryOptmizationDecision";
    public static final String SHOW_BATTERY_OPTIMIZATION_CARD = "ShowBatteryOptimizationCard";

    /* Fingerprint Constants Start */
    public static final String KEY_SHARED_PREFS_FINGERPRINT_DECISION_TAKEN = "FINGERPRINT_DECISION_TAKEN";
    public static final String KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN = "FINGERPRINT_OPTED_IN";

    public static final String INTENT_RESULT_IS_FINGERPRINT_TERMS_ACCEPTED = "INTENT_RESULT_IS_FINGERPRINT_TERMS_ACCEPTED";
    public static final String INTENT_RESULT_IS_FINGERPRINT_TERMS_DECISION_MADE = "INTENT_RESULT_IS_FINGERPRINT_TERMS_ACCEPTED";
    public static final String INTENT_RESULT_IS_FINGERPRINT_OPTED_IN = "INTENT_RESULT_IS_FINGERPRINT_OPTED_IN";
    public static final String INTENT_RESULT_IS_FINGERPRINT_OPT_IN_SETUP_COMPLETE = "INTENT_RESULT_IS_FINGERPRINT_OPT_IN_SETUP_COMPLETE";
    /* Fingerprint Constants End */



    public static final String MESSAGE_DETAIL_IS_QA_URL= "hreg2.kaiserpermanente.org";
    public static final String MESSAGE_DETAIL_IS_DEV_URL = "hpp.kaiserpermanente.org";
    public static final String MESSAGE_DETAIL_IS_PP_OR_PROD_URL = "healthy.kaiserpermanente.org";
    public static final String MESSAGE_DETAIL_KP_ORG_URL = "kp.org";
    public static final String MESSAGE_DETAIL_KP_DOC_ORG_URL = "kpdoc.org";
    public static final String MESSAGE_DETAIL_KAISER_PERMANENTE_ORG_URL = "kaiserpermanente.org";


    /*Deep Linking*/
    public static final String MDO_PACKAGE_NAME = "MDO_PACKAGE_NAME";
    public static final String URL_USER_NAME_STRING = "username";
    public static final String URL_PASSWORD_STRING = "password";
    public static final String APP_VERSION = "AppVersion";

    private static boolean isFromNotification = false;

    public static void setIsFromNotification(boolean isFromNotification) {
        AppConstants.isFromNotification = isFromNotification;
    }

    public static final String SIGNON_RESPONSE_INTERRUPT_TEMP_PWD = "TEMP PWD";
    public static final String SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH = "EMAIL MISMATCH";
    public static final String SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS = "SECRET QUESTIONS";
    public static final String SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH = "STAY IN TOUCH";

    public static final String TC_BANNER_TO_BE_SHOWN = "isTCBAnnerToBeShown";

    public static final String USER_REGION_KET = "region";
    public static final String USER_EMAIL = "email";
    public static final String USER_AGE = "age";

    public static String CLIENT_ID = BuildConfig.CLIENT_ID;
    public static String CLIENT_SECRET = BuildConfig.CLIENT_SECRET;

    public static final String DEV_CLIENT_ID = "968qkovEYoVsFxDO1zXvPqCMsfoa";
    public static final String DEV_CLIENT_SECRET = "ze8qFEat8rfcb70DnmcPgE2M4xMa";

    public static final String QI_CLIENT_ID = "XTYhOxEVSqLCTXVyMvEIdcNeHdIa";
    public static final String QI_CLIENT_SECRET = "_gTCeNVR6mNWrtDZHHzhNUNI9zUa";

    public static final String QA_CLIENT_ID = "ZuTFQeFGUQHGs07HNckkiZIur0sa";
    public static final String QA_CLIENT_SECRET = "2AuEf4xZ5HhHKEdH6Rp1VG_fwQka";

    public static final String PP_CLIENT_ID = "YT_wN6DqNPdU0D58YoZgxxwrAawa";
    public static final String PP_CLIENT_SECRET = "wHP2LogytlkSwWgOdkI1hklf6LIa";

    public static final String PR_CLIENT_ID = "e9RVr2GXcc9JiwP2aPVd84iO_HUa";
    public static final String PR_CLIENT_SECRET = "KAHyGDxqsS9q9c_sQDli6efFPWIa";

    //key for shared preferences
    public static final String KEY_GA_UNIQUE_RANDOM_VALUE = "GA_UniqueRandomId";
    public static final String LOG_GA_EVENT_FOR_DYNAMIC_FONT_SCALE = "GAEventForFontScaleState";
    public static final float FONT_SCALE_VALUE_DEFAULT = 1;

    // Splash Animation constants
    public static final int FADE_IN_DURATION = 500;
    public static final int TIME_BETWEEN = 500;
    public static final int FADE_OUT_DURATION = 500;
    public static final int RESPONSE_COUNT = 2;
    public static final int DELAY_LOGO = 1400;
    public static final int DELAY_SPLASH = 1600;

    public static String IS_FROM_PILL_POPPER_APPLICATION = "FromPillpopperApplication";

    // has Status update constants
    public static final String KPHC_MEDS_STATUS_CHANGED = "kphcMedsStatusChanged";
    public static final String MED_ARCHIVED_OR_REMOVED = "medArchivedOrRemoved";
    public static final String PROXY_STATUS_CODE = "proxyStatusCode";
    public static final String MEDICATION_SCHEDULE_CHANGED = "medicationScheduleChanged";
    public static final String HAS_STATUS_UPDATE_TIMESTAMP = "hasStatusUpdateTimeStamp";
    public static final String PRE_EFFECTIVE_MEMBER_URL = "https://healthy.kaiserpermanente.org/health/mycare/consumer/my-health-manager/my-plan-and-coverage/eligibility-and-benefits";

    // broadcast constants
    public static final String BROADCAST_REFRESH_FOR_MED_IMAGES = "refreshForImageUpdates";
    public static final String BUNDLE_EXTRA_DRUG_TO_REFRESH = "DrugToRefresh";
    public static final String SCHEDULE_CHOICE_UNDEFINED = "undefined";
    public static final String SCHEDULE_CHOICE_AS_NEEDED = "takeAsNeeded";
    public static final String SCHEDULE_CHOICE_SCHEDULED = "scheduled";

    public static final String GOOD_MORNING = "Good Morning";
    public static final String GOOD_AFTERNOON = "Good Afternoon";
    public static final String GOOD_EVENING = "Good Evening";
    public static String SINGLE_MEDICATION = "single medication";
    public static String BULK_MEDICATION = "bulk medication";

    public static final String X_APP_NAME_KEY = "x-appName";
    public static final String X_APP_NAME_VALUE = "MyKPMedsApp";
    public static final int ALARM_RESET_TIME = 24;// in hour

    public static final String LAUNCH_MODE = "LaunchMode";
    public static final String ACTION_TITLE = "actionTitle";
    public static final String ACTION_MESSAGE = "actionMessage";

    public static Uri contentUri = null;
    public static File photoFile = null;
    public static boolean showCalendarView = true;

    //used to update the notifyAfter value after taking action on the postponed med
    public static boolean updateNotifyAfterValue = false;

    public static final Boolean IS_FORCE_SIGN_IN_REQUIRED = false;
    public static String FORCE_SIGN_IN_SHARED_PREF_KEY = "forceSignIn";

    public static final int SAVED_ALERT_REQUEST_CODE = 999;
    public static boolean SHOW_SAVED_ALERT = false;
    public static boolean MEDS_TAKEN_OR_SKIPPED = false;
    public static boolean MEDS_TAKEN_OR_POSTPONED = false;

}
