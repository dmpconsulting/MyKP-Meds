package com.montunosoftware.pillpopper.analytics;


/**
 * Created by Shiva NageshwarRao on 10-Jun-19.
 */

public class FireBaseConstants {

    public static final String UTILITY_CLASS = "Utility Class";
    public static final String ENVIRONMENT_BETA = "BETA";
    public static final String ENVIRONMENT_PP = "PP";
    public static final String ENVIRONMENT_TRACK_PP = "PP";
    public static final String ENVIRONMENT_PR = "PR";
    public static final String ENVIRONMENT_PROD = "PROD";

    private FireBaseConstants() {
        throw new IllegalStateException(UTILITY_CLASS);
    }

    public static class ParamName {
        public static final String CARD_TYPE = "card_type";
        public static final String SOURCE = "source";
        public static final String ACTION_TYPE = "action_type";
        public static final String AUTH_TYPE = "auth_type";
        public static final String VALUE = "value";
        public static final String TIME_PERIOD = "timePeriod";
        public static final String REASON = "reason";
        public static final String SIZE = "size";
        public static final String PARAMETER_NAME_TYPE = "Type";
        public static final String FAILURE_INFO = "failure_info";
        public static final String ALERT_TYPE = "alert_type";
        public static final String TOGGLE = "toggle";

        public static final String PARAMETER_FAILED_CORRELATION_ID = "Failure_CorrelationID";
        public static final String PARAMETER_API_FAILURE = "Failure";
        public static final String PARAMETER_API_SUCCESS = "Success";

        public static final String PARAMETER_NAME_LOWERCASE_TYPE = "type";

        private ParamName() {
            throw new IllegalStateException(UTILITY_CLASS);
        }
    }

    public static class ParamValue {

        public static final String CURRENT_REMINDER = "Current reminder";
        public static final String LATE_REMINDER = "Late reminder";
        public static final String NEW_MEDS = "New meds";
        public static final String UPDATED_MEDS = "Updated meds";
        public static final String REFILL_REMINDER = "Refill reminder";
        public static final String DISCONTINUED_MEDS = "Discontinued Meds";
        public static final String VIEW_YOUR_MEDICATIONS_CARD = "View Your Medications Card";
        public static final String REFILL_FROM_YOUR_PHONE_CARD = "Refill from Your Phone card";
        public static final String MANAGE_YOUR_FAMILY_MEDICATION_CARD = "Manage Your Family's Medication Card";
        public static final String SET_UP_REMINDERS_CARD = "Set Up Reminders Card";
        public static final String VIEW_REMINDERS_CARD = "View Reminder Card";
        public static final String CURRENT_REMINDER_QUICKVIEW = "Current reminder quickview";
        public static final String LATE_REMINDER_QUICKVIEW = "Late reminder quickview";
        public static final String BATTERY_OPTIMIZATION = "Battery optimization";
        public static final String SCHEDULE_SCREEN_EMPTY_STATE = "Schedule screen empty state";
        public static final String QUICK_ACCESS = "Quick access";
        public static final String NEW_MEDICATION_CARD = "New medication card";

        public static final String REFILL_REMINDER_CARD = "Refill reminder card";
        public static final String NAVIGATION_MENU = "Navigation menu";
        public static final String MED_LIST = "Med list";

        public static final String REFILL_REMINDER_LIST = "Refill reminder list";

        public static final String SKIPPED = "Skipped";
        public static final String TAKEN = "Taken";
        public static final String SKIPPED_ALL = "Skipped all";
        public static final String TAKEN_ALL = "Taken all";
        public static final String REMIND_ME_LATER = "Remind me later";
        public static final String TAKEN_EARLIER = "Taken earlier";
        public static final String QUICKVIEW = "Quickview";
        public static final String RICH_NOTIFICATION = "rich notification";
        public static final String FOCUS_CARD = "focus card";
        public static final String RECORD_DOSE_BUTTON = "Record Dose Button";

        public static final String TOUCH_ID = "Touch ID";

        public static final String MED_DETAILS = "Med details";
        public static final String VIEW_IMAGE_SCREEN = "View image screen";

        public static final String AACC = "aacc";
        public static final String PHARMACY_CALL_CENTER = "pharmacy call center";

        public static final String ON = "on";
        public static final String OFF = "off";

        public static final String DAY_1 = "1 day";
        public static final String DAYS_14 = "14 days";
        public static final String MONTH_1 = "1 month";
        public static final String MONTHS_3 = "3 months";
        public static final String YEAR_1 = "1 year";
        public static final String YEARS_2 = " 2 years";

        public static final String NO_DB_DOWNLOADED = "no DB downloaded";
        public static final String BACKGROUND_FOREGROUND = "background foreground";
        public static final String FRESH_LAUNCH = "fresh launch";
        public static final String MDO = "MDO";
        public static final String PARAMETER_VALUE_PRE_EFFECTIVE = "Pre-effective Member";
        public static final String TEEN_PROXY_CARD = "Teen Proxy card";
        public static final String PROXY_ADDED = "Proxy added";
        public static final String PROXY_REMOVED = "Proxy Removed";
        public static final String NEW_MEDICATION = "New medication";
        public static final String UPDATED_MEDICATION = "Updated medication";
        public static final String DISCONTINUED_MEDICATION = "Discontinued Medications";
        public static final String SCHEDULE_CHANGE = "Schedule Change (Archived/Removed Meds)";
        public static final String VIEW = "View";
        public static final String DISMISS = "Dismiss";
        public static final String PROXY_ADDED_AND_REMOVED_ANOTHER_PROXY = "Proxy added and removed another proxy";

        public static final String PARAMETER_VALUE_SUCCESS_WITH_STATUS0 = "Successful response with status code 0";
        public static final String PARAMETER_VALUE_SUCCESS = "Success";
        public static final String DB_HAS_NO_RECORD = "DB has no record";
        public static final String GENERIC_BANNER = "Generic Banner";
        public static final String GENERIC_CARD = "Generic Card";
        public static final String ADD_MEDICATIONS = "Add Medications";
        public static final String EDIT_MEDICATIONS = "Edit Medications";
        public static final String ADD_NOTES = "Add Notes";
        public static final String EDIT_NOTES = "Edit Notes";
        public static final String SETTINGS = "Settings";
        public static final String HISTORY_LIST_SCREEN = "History List Screen";
        public static final String TAKE_ACTON = "take action";
        public static final String UPDATE_EXISTING_HISTORY = "update existing history";
        public static final String SCHEDULE_LIST = "Schedule List" ;
        public static final String HISTORY_DETAIL_SCREEN = "History Detail Screen";
        public static final String HISTORY_CALENDAR_OVERLAY = "History Calendar Overlay";
        public static final String TAKE_THE_REST = "Take the Rest";
        public static final String SKIP_THE_REST = "Skip the Rest";
        public static final String TAKEN_THE_REST_EARLIER = "Taken the Rest Earlier";
        public static final String HISTORY_EDIT_SCREEN = "History Edit Screen";


        private ParamValue() {
            throw new IllegalStateException(UTILITY_CLASS);
        }

    }

    public static class Event {

        public static final String VIEW_CARD_HOME = "view_card_home";
        public static final String BULK_REMINDER_ADD = "bulk_reminder_add";
        public static final String REFILL_MEDS = "refill_meds";
        public static final String ADD_MEDS = "add_meds";
        public static final String ADD_MEDS_SAVE = "add_meds_save";
        public static final String FIND_PHARMACY = "find_pharmacy";
        public static final String REFILL_REMINDER_CREATE = "refill_reminder_create";
        public static final String REFILL_REMINDER_SAVE = "refill_reminder_save";
        public static final String REMINDER_ACTIONS = "reminder_actions";
        public static final String ENABLED_BIOMETRICS = "enabled_biometrics";
        public static final String MED_ARCHIVE = "med_archive";
        public static final String MED_LIST_SHARE = "med_list_share";
        public static final String CHANGE_IMAGE = "change_image";
        public static final String BULK_REMINDER_SAVE = "bulk_reminder_save";
        public static final String MED_REMINDER_ADD = "med_reminder_add";
        public static final String MED_REMINDER_SAVE = "med_reminder_save";
        public static final String HISTORY_LIST_SHARE = "history_list_share";
        public static final String CALL = "call";
        public static final String CLEAR_ALL_STORED_DATA = "clear_all_stored_data";
        public static final String SIGN_IN_SUCCESS = "sign_in_success";
        public static final String SIGN_OUT = "sign_out";
        public static final String MED_ARCHIVE_RESTORE = "med_archive_restore";
        public static final String MED_ARCHIVE_DELETE = "med_archive_delete";
        public static final String MANAGE_MEMBER_MED_TOGGLE = "manage_member_med_toggle";
        public static final String MANAGE_MEMBER_REMINDER_TOGGLE = "manage_member_reminder_toggle";
        public static final String QUICKVIEW_TOGGLE = "quickview_toggle";
        public static final String SHOW_HISTORY = "show_history";
        public static final String BATTERY_OPTIMIZATION_SETTINGS = "battery_optimization_settings";
        public static final String GET_STATE_SUCCESS = "getstate_success";
        public static final String GET_STATE_FAIL = "getstate_fail";
        public static final String GET_HISTORY_EVENT_FAIL = "gethistoryevent_fail";
        public static final String GET_HISTORY_EVENT_SUCCESS = "gethistoryevent_success";
        public static final String GET_INTERMEDIATE_SYNC_SUCCESS = "getintermediate_sync_success";
        public static final String GET_INTERMEDIATE_SYNC_FAIL = "getintermediate_sync_fail";
        public static final String SIGN_IN_FAIL = "sign_in_fail";
        public static final String REGISTER_CALL_SUCCESS = "register_call_success";
        public static final String REGISTER_CALL_FAIL = "register_call_fail";
        public static final String APP_PROFILE_SUCCESS = "app_profile_success";
        public static final String APP_PROFILE_FAIL = "app_profile_fail";
        public static final String API_TOKEN_SUCCESS = "api_token_success";
        public static final String API_TOKEN_FAIL = "api_token_fail";
        public static final String AXWAY_TOKEN_SUCCESS = "axway_token_success";
        public static final String AXWAY_TOKEN_FAIL = "axway_token_fail";
        public static final String PHARMACY_BUNDLED_DB_USED = "pharmacy_bundled_DB_used";
        public static final String APP_LAUNCH = "app_launch";
        public static final String DYNAMIC_FONT = "dynamic_font";
        public static final String EVENT_SIGN_IN_STATE = "Sign_in_state";
        public static final String NLP_EXTRACT_REMINDER_SUCCESS = "NLP_extractreminder_success";
        public static final String NLP_EXTRACT_REMINDER_FAIL = "NLP_extractreminder_fail";
        public static final String NLP_SCHEDULE_VALIDATION_SUCCESS = "NLP_schedulevalidation_success";
        public static final String NLP_SCHEDULE_VALIDATION_FAIL = "NLP_schedulevalidation_fail";
        public static final String SIGN_IN_ALERTS = "Sign_In_Alerts";
        public static final String IN_APP_NOTIFICATIONS_ACTIONS = "In_App_Notifications_actions";

        public static final String EVENT_SIGNON_FAILURE_AUTH_FAILED_5 = "SignOn_Failure_Auth_Failed";
        public static final String EVENT_SIGNON_FAILURE_ACCT_LOCKED_6 = "SignOn_Failure_Acct_Locked";
        public static final String EVENT_SIGNON_FAILURE_BUSINESS_ERROR_7 = "SignOn_Failure_Buisnesserror";
        public static final String EVENT_SIGNON_FAILURE_D1000 = "SignOn_Failure_D1000";
        public static final String EVENT_SIGNON_FAILURE_NMA_11 = "SignOn_Failure_NMA";
        public static final String EVENT_SIGNON_FAILURE_PENDING_OTP_12 = "SignOn_Failure_Pending_Otp";
        public static final String EVENT_SIGNON_FAILURE_REG_NO_SUPPORT_8 = "SignOn_Failure_Reg_NoSupport";
        public static final String EVENT_SIGNON_FAILURE_SYSTEM_ERROR = "SignOn_Failure_Sys_Error";
        public static final String EVENT_SIGNON_FAILURE_TERMINATED_MEMBER_9 = "SignOn_Failure_Terminated_Member";
        public static final String EVENT_SIGNON_FAILURE_OTHERS = "SignOn_Failure_";

        public static final String EVENT_KEEP_ALIVE_SERVICE_FAILURE = "Keep_Alive_Service_Faliure";
        public static final String EVENT_KEEP_ALIVE_SERVICE_SUCCESS = "Keep_Alive_Service_Success";

        public static final String EVENT_SSO_INTERRUPTS_PUT_FAILURE = "SSO_Interrupts_PUT_Interrupt_Faliure";
        public static final String EVENT_SSO_INTERRUPTS_PUT_SUCCESS = "SSO_Interrupts_PUT_Interrupt_Success";

        public static final String EVENT_SSO_INTERRUPTS_GET_FAILURE = "SSO_Interrupts_GET_Interrupt_Faliure";
        public static final String EVENT_SSO_INTERRUPTS_GET_SUCCESS = "SSO_Interrupts_GET_Interrupt_Success";
        public static final String CAFH_MEMBER_ACCESS = "CAFH_member_access";
        public static final String PEM_MEMBER_ACCESS = "PEM_member_access";
        public static final String NOTES_SAVE = "notes_save";
        public static final String GENERIC_CARD_CLICK = "generic_card_click";
        public static final String GENERIC_BANNER_CLICK ="generic_banner_click";
        public static final String HISTORY_DETAILS_UPDATE = "history_details_update";
        public static final String HISTORY_CALENDER_OVERLAY = "history_calendar_overlay";
        public static final String EVENT_CONFIRM_SCHEDULE ="med_reminder_confirm";
        public static final String EVENT_CANCEL_SCHEDULE="med_reminder_cancel";
        public static final String EVENT_HISTORY_EDIT_SAVE = "history_edit_save";


        private Event() {
            throw new IllegalStateException(UTILITY_CLASS);
        }

    }


    public static class UserProperties {
        public static final String USER_PROPS_AGE = "user_age";
        public static final String USER_PROPS_GENDER = "user_gender";
        public static final String USER_PROPS_REGION = "region";
        public static final String USER_PROPS_ENVIRONMENT = "environment";
    }

    public static class ScreenEvent {
        public static final String OPEN_SCREEN = "open_screen";
        public static final String SCREEN_NAME = "screen_name";
        public static final String SCREEN_SIGN_IN = "Sign In";
        public static final String SCREEN_SIGN_IN_HELP = "Sign In Help";
        public static final String SCREEN_TOUCH_TERMS_CONDITIONS = "Touch ID Terms & Conditions";
        public static final String SCREEN_MED_LIST = "Medication List";
        public static final String SCREEN_ADD_MEDICATION = "Add Medication ";
        public static final String SCREEN_MED_DETAILS = "Medication Detail";
        public static final String SCREEN_MED_DETAILS_UNEDITABLE = "Medication Detail Uneditable";
        public static final String SCREEN_SCHEDULE_LIST = "Schedule List";
        public static final String SCREEN_HISTORY_LIST = "History List";
        public static final String SCREEN_HISTORY_DETAILS = "History details";
        public static final String SCREEN_APP_SUPPORT = "App Support";
        public static final String SCREEN_TERMS_CONDITIONS = "Terms and Conditions";
        public static final String SCREEN_PRIVACY_STATEMENT = "Privacy Statement";
        public static final String SCREEN_APPOINTMENT_ADVICE = "Appointment and Advice";
        public static final String SCREEN_PHARMACY_CALL_CENTER = "Pharmacy Call Center";
        public static final String SCREEN_GUIDE = "Guide";
        public static final String SCREEN_MANAGE_MEMBERS = "Manage Members";
        public static final String SCREEN_SETTINGS = "Settings";
        public static final String SCREEN_ARCHIVE_LIST = "Archive List";
        public static final String SCREEN_CURRENT_REMINDER = "Current reminder";
        public static final String SCREEN_LATE_REMINDER = "Late reminder";
        public static final String SCREEN_NEW_MEDS = "New meds";
        public static final String SCREEN_UPDATED_MEDS = "Updated meds";
        public static final String SCREEN_REFILL_REMINDER = "Refill reminder";
        public static final String SCREEN_DISCONTINUED_MEDS = "Discontinued Meds";
        public static final String SCREEN_VIEW_YOUR_MEDICATIONS = "View Your Medications";
        public static final String SCREEN_REFILL_FROM_YOUR_PHONE = "Refill from Your Phone";
        public static final String SCREEN_MANAGE_YOUR_FAMILY_MEMBERS = "Manage Your Family's Medication";
        public static final String SCREEN_SET_UP_REMINDERS = "Set Up Reminders";
        public static final String SCREEN_VIEW_REMINDERS = "View Reminder Card";
        public static final String SCREEN_BATTERY_OPTIMIZATION = "Battery optimization";
        public static final String SCREEN_REFILL_REMINDER_LIST = "Refill Reminder List";
        public static final String SCREEN_REFILL_SETUP = "Refill Setup";
        public static final String SCREEN_QUICKVIEW_CURRENT_REMINDER = "Quickview Current Reminder";
        public static final String SCREEN_QUICKVIEW_LATE_REMINDER = "Quickview Late reminder";
        public static final String SCREEN_EXPAND_IMAGE = "Expanded image";
        public static final String SCREEN_INTERRUPT_STAY_IN_TOUCH = "SSO Interrupt - Stay in touch";
        public static final String SCREEN_INTERRUPT_SECRET_QUESTIONS = "SSO Interrupt - Secret questions";
        public static final String SCREEN_INTERRUPT_TEMP_PWD = "SSO Interrupt - Temporary password";
        public static final String SCREEN_INTERRUPT_EMAIL_MISMATCH = "SSO Interrupt - Email mismatch";
        public static final String SCREEN_BULK_REMINDER_SETUP ="Bulk reminder setup";
        public static final String SCREEN_BULK_REMINDER_ADD_MEDICATIONS = "Bulk reminder add medications";
        public static final String SCREEN_PRE_EFFECTIVE_MEMBER_PROMPT = "Pre-effective Member Prompt";
        public static final String SCREEN_ADD_NOTES = "Add Notes";
        public static final String SCREEN_EDIT_NOTES = "Edit Notes";
        public static final String SCREEN_EDIT_OTC_MEDICATION = "Edit OTC Medication";
        public static final String SCREEN_TEEN_PROXY_CARD = "Teen Proxy card";
        public static final String SCREEN_CREATE_OR_EDIT_SCHEDULE = "Create/Edit Schedule";
        public static final String SCREEN_SIGN_IN_ALERTS = "Sign In Alerts";
        public static final String SCREEN_HOME = "Home screen";
        public static final String SCREEN_PRESCRIPTION_REFILL_AEM = "Prescription Refills";
        public static final String SCREEN_FIND_PHARMACY = "Find a Pharmacy";
        public static final String SCREEN_ARCHIVE_DETAILS ="Archive Details";
        public static final String SCREEN_SAVE_SCHEDULE_CONFIRMATION = "Save Schedule Confirmation Screen";
        public static final String SCREEN_GREAT_JOB_ALERT = "Great Job Alert";
        public static final String SCREEN_KEEP_IT_UP_ALERT = "Keep It Up Alert";
        public static final String SCREEN_SAVED_ALERT = "Saved Alert";
        public static final String SCREEN_HISTORY_OVERLAY = "History Overlay Screen";
        public static final String SCREEN_HISTORY_EDIT = "History Edit Screen";


    }

    //GA Constants for font scale
    public static final String LABEL_FONT_SETTING_SMALL = "small";
    public static final String LABEL_FONT_SETTING_DEFAULT = "medium";
    public static final String LABEL_FONT_SETTING_LARGE = "large";
}
