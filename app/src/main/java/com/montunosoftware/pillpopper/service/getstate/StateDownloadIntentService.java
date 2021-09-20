package com.montunosoftware.pillpopper.service.getstate;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RequestWrapper;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbConstants;
import com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbHandler;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillPillpopperResponse;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminderRootObject;
import com.montunosoftware.pillpopper.android.refillreminder.services.RefreshRefillRemindersAsyncTask;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperParseException;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.database.model.GetHistoryEvents;
import com.montunosoftware.pillpopper.database.model.IntermittentSyncMultiResponse;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.database.model.PillList;
import com.montunosoftware.pillpopper.database.model.PillpopperResponse;
import com.montunosoftware.pillpopper.database.model.ResponseArray;
import com.montunosoftware.pillpopper.database.model.UserList;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandler;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.State;
import com.montunosoftware.pillpopper.model.UserPreferences;
import com.montunosoftware.pillpopper.service.GetFDBImagesForKPHCDrugsAsyncTask;
import com.montunosoftware.pillpopper.service.TokenService;
import com.montunosoftware.pillpopper.service.images.sync.ImageSyncManager;
import com.montunosoftware.pillpopper.service.images.sync.ImageSynchronizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * An subclass of IntentService for handling asynchronous task requests in
 * a service on a separate handler thread for GetState and GetHistoryEvents Api Calls.
 * <p>
 * helper methods.
 */
public class StateDownloadIntentService extends JobIntentService {
    public static final String ACTION_GET_STATE = "com.montunosoftware.pillpopper.service.getstate.action.GetState";
    public static final String ACTION_GET_HISTORY_EVENTS = "com.montunosoftware.pillpopper.service.getstate.action.GetHistoryEvents";
    public static final String ACTION_INTERMEDIATE_GET_STATE = "com.montunosoftware.pillpopper.service.getstate.action.IntermediateGetState";
    private static final String ACTION_CHECK_DAY_LIGHT_SAVING_STATE = "com.montunosoftware.pillpopper.service.getstate.action.DaylightSavingCheck";
    private static final String ACTION_GET_ALL_REFILL_REMINDERS = "org.kp.tpmg.mykpmeds.action.GetAllRefillReminders";

    public static final String BROADCAST_GET_STATE_COMPLETED = "StateDownloadIntentService.GET_STATE_COMPLETE";
    public static final String BROADCAST_REMOVE_REGISTRATION_POPUP = "com.montunosoftware.pillpopper.REGISTRATION_COMPLETED";
    public static final String BROADCAST_GET_STATE_FAILED = "StateDownloadIntentService.GET_STATE_FAILED";
    public static final String BROADCAST_DAY_LIGHT_SAVING_ADJUSTMENT_DONE = "com.montunosoftware.pillpopper.DAY_LIGHT_SAVING_COMPLETED";
    public static final String BROADCAST_REFRESH_KPHC_FOR_MANAGE_MEMBERS_SELECTION = "REFRESH_KPHC_FOR_MANAGE_MEMBERS_SELECTION";
    public static final String ACTION_NON_SECURE_INTERMEDIATE_GET_STATE = "com.montunosoftware.pillpopper.service.getstate.action.NonSecureIntermediateGetState";
    public static final String BROADCAST_TEEN_PROXY_GET_STATE_COMPLETED = "StateDownloadIntentService.TEEN_PROXY_GET_STATE_COMPLETE";

    /**
     * Unique job ID All the Token Service requests.
     * This should be unique for all the requests assigning to this JobIntentService, otherwise we may have to create another class.
     */
    public static final int STATE_DOWNLOAD_JOB_ID = 1001;


    private static boolean handleHistoryFailure=false;
    private boolean isGetStateFailed;
    // private boolean isNeedstoNotifyWithGetStateCompleted = false;

    /*public StateDownloadIntentService() {
        super("StateDownloadIntentService");
    }*/

    public static void startActionGetState(Context context) {
        try {
            Intent intent = new Intent(context, StateDownloadIntentService.class);
            intent.setAction(ACTION_GET_STATE);
           // context.startService(intent);
            enqueueWork(context, StateDownloadIntentService.class, STATE_DOWNLOAD_JOB_ID, intent);
        } catch (Exception e){
            PillpopperLog.say(e);
        }
    }

    public static void startActionGetAllRefillReminders(Context context) {
        try{
            Intent intent = new Intent(context, StateDownloadIntentService.class);
            intent.setAction(ACTION_GET_ALL_REFILL_REMINDERS);
            enqueueWork(context, StateDownloadIntentService.class, STATE_DOWNLOAD_JOB_ID, intent);
        } catch (Exception e){
            PillpopperLog.say(e);
        }
    }

    public static void startActionGetHistoryEvents(Context context) {
        try{
            Intent intent = new Intent(context, StateDownloadIntentService.class);
            intent.setAction(ACTION_GET_HISTORY_EVENTS);
            enqueueWork(context, StateDownloadIntentService.class, STATE_DOWNLOAD_JOB_ID, intent);
        } catch (Exception e){
            PillpopperLog.say(e);
        }
    }

    public static void startActionIntermediateGetState(Context context) {
        try{
            Intent intent = new Intent(context, StateDownloadIntentService.class);
            intent.setAction(ACTION_INTERMEDIATE_GET_STATE);
            enqueueWork(context, StateDownloadIntentService.class, STATE_DOWNLOAD_JOB_ID, intent);
        }catch (Exception e){
            PillpopperLog.say(e);
        }
    }

    public static void startActionNonSecureIntermediateGetState(Context context) {
        try{
            Intent intent = new Intent(context, StateDownloadIntentService.class);
            intent.setAction(ACTION_NON_SECURE_INTERMEDIATE_GET_STATE);
            enqueueWork(context, StateDownloadIntentService.class, STATE_DOWNLOAD_JOB_ID, intent);
        }catch (Exception e){
            PillpopperLog.say(e);
        }
    }

    public static void startActionForDaylightSavingAdjustmentNeeded(Context context){
        try {
            Intent intent = new Intent(context, StateDownloadIntentService.class);
            intent.setAction(ACTION_CHECK_DAY_LIGHT_SAVING_STATE);
           // context.startService(intent);
            enqueueWork(context, StateDownloadIntentService.class, STATE_DOWNLOAD_JOB_ID, intent);
        } catch (Exception e){
            PillpopperLog.say(e);
        }
    }

    public static void handleHistoryFailure(boolean flag){
        handleHistoryFailure=flag;
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String action = intent.getAction();
        switch(action) {
            case ACTION_GET_STATE:
                handleActionGetState();
                break;
            case ACTION_GET_HISTORY_EVENTS:
                handleActionGetHistoryEvents();
                break;
            case ACTION_INTERMEDIATE_GET_STATE:
                handleActionIntermediateGetState(true);
                break;
            case ACTION_NON_SECURE_INTERMEDIATE_GET_STATE:
                handleActionIntermediateGetState(false);
                break;
            case ACTION_CHECK_DAY_LIGHT_SAVING_STATE:
                handleDaylightSavingState();
                break;
            case ACTION_GET_ALL_REFILL_REMINDERS:
                handleGetAllRefillReminders();
                break;
            default:
                break;
        }
    }


    private void handleGetAllRefillReminders() {
        PillpopperAppContext pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(getApplicationContext());
        PillpopperServer server = null;
        JSONObject serverResponse;

        try {
            server = PillpopperServer.getInstance(getApplicationContext(),pillpopperAppContext);
        } catch (PillpopperServer.ServerUnavailableException e ) {
            PillpopperLog.exception(e.getMessage());
        }

        RequestWrapper requestWrapper = new RequestWrapper(getApplicationContext());
        String primaryUserId = FrontController.getInstance(getApplicationContext()).getPrimaryUserIdIgnoreEnabled();

        if(!Util.isEmptyString(primaryUserId)){ // No userid, Do not invoke request. This leads to API failed.
            JSONObject request = requestWrapper.createGetAllRefillRemindersRequest(primaryUserId);
            try {
                if(null != server) {
                    serverResponse = server.makeRequest(request);
                    if (null != serverResponse) {
                        PillpopperLog.say("Get All Refill Reminders API Response : " + serverResponse.toString());
                        Gson gson = new Gson();
                        RefillReminderDbHandler.getInstance(getApplicationContext()).deleteTableData(RefillReminderDbConstants.TABLE_REFILL_REMINDER);
                        RefillReminderRootObject refillReminderRootObject = gson.fromJson(serverResponse.toString(), RefillReminderRootObject.class);
                        if (isValidGetAllRefillResponse(refillReminderRootObject.getPillpopperResponse())) {
                            FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(this, FireBaseConstants.Event.SIGN_IN_SUCCESS);
                            RefillReminderController.getInstance(getApplicationContext()).insertGetAllRefillReminderData(getApplicationContext(),
                                    refillReminderRootObject.getPillpopperResponse().getReminderList());
                            new RefreshRefillRemindersAsyncTask(this).execute();

                        }
                    }
                }
            } catch (PillpopperServer.ServerUnavailableException e) {
                PillpopperLog.say(e);
                PillpopperLog.say("Get All Refill ServerUnavailableException while deleting the Table ");
            } catch (Exception e){
                PillpopperLog.say(e);
                PillpopperLog.say("Get All Refill Exception while deleting the Table ");
            }
        }
    }

    /**
     * Returns True if the dataSyncResult is Success else False
     * @param refillPillpopperResponse
     * @return
     */
    private boolean isValidGetAllRefillResponse(RefillPillpopperResponse refillPillpopperResponse){
        if(null!=refillPillpopperResponse && null!=refillPillpopperResponse.getDataSyncResult()){
            return RefillReminderConstants.SUCCESS_STRING.equalsIgnoreCase(refillPillpopperResponse.getDataSyncResult());
        }
        return false;
    }

    private void handleNetworkFailureScenario() {
        Intent initialGetStateBroadcastIntent = new Intent();
        initialGetStateBroadcastIntent.setAction("GetStateFailedFilter");
        sendBroadcast(initialGetStateBroadcastIntent);
    }

    private void handleDaylightSavingState() {
        checkForDaylightSavingAdjustment();
    }

    private void checkForDaylightSavingAdjustment() {
        try {
            PillpopperAppContext pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(getApplicationContext());
            List<String> enabledUsers = FrontController.getInstance(getApplicationContext()).getEnabledUserIds();
            SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME);

            JSONObject multiRequest = new JSONObject();
            JSONObject finalRequestObj = new JSONObject();
            JSONArray requestArray=new JSONArray();

            String timezoneSec=null;

            if(!enabledUsers.isEmpty()){
                for (String userID : enabledUsers){
                    if(null!=FrontController.getInstance(getApplicationContext()).getUserPreferencesForUser(userID)){
                        UserPreferences userPref = FrontController.getInstance(getApplicationContext()).getUserPreferencesForUser(userID);
                        timezoneSec = userPref.getTz_sec();
                        String tzName = userPref.getTz_name();
                        String dstOffset = userPref.getDstOffset_secs();

                        if(timezoneSec==null && tzName==null && dstOffset==null){
                            // This proxy is not having the preferences so reading the primary Member preferences
                            String primaryUserID = FrontController.getInstance(getApplicationContext()).getPrimaryUserIdIgnoreEnabled();
                            timezoneSec = FrontController.getInstance(getApplicationContext()).getUserPreferencesForUser(primaryUserID).getTz_sec();
                        }

                        PillpopperLog.say("userID is : " + userID + " And time zone seocs" + timezoneSec);
                        if((null!=timezoneSec && Util.isNeedTOAdjustSchedule(timezoneSec))
                                || ("1").equalsIgnoreCase(FrontController.getInstance(getApplicationContext()).isTimezoneAdjustmentToServerRequired(getApplicationContext()))
                                || PillpopperRunTime.getInstance().isTimeZoneChanged()){
                            PillpopperLog.say("Time Zone got changed. Needs to be called AdjustPillSchedule");


                            TimeZone tz = TimeZone.getDefault();

                            JSONObject setPrefJsonObject = new JSONObject();
                            JSONObject setUserPrefJsonObject = new JSONObject();

                            JSONObject pillpopperRequestForSetPref = new JSONObject();


                            setPrefJsonObject.put("action", "SetPreferences");
                            setPrefJsonObject.put("clientVersion", Util.getAppVersion(getApplicationContext()));
                            setPrefJsonObject.put("userId", FrontController.getInstance(getApplicationContext()).getPrimaryUserIdIgnoreEnabled());
                            setPrefJsonObject.put("partnerId", AppConstants.EDITION);
                            setPrefJsonObject.put("language", Locale.getDefault().toString());
                            setPrefJsonObject.put("hardwareId", UniqueDeviceId.getHardwareId(getApplicationContext()));
                            setPrefJsonObject.put("targetUserId", userID);
                            setPrefJsonObject.put("apiVersion", "Version 6.0.4");
                            setPrefJsonObject.put("deviceToken", "");
                            setPrefJsonObject.put("replayId", Util.getRandomGuid());


                            setUserPrefJsonObject.put("tz_secs", String.valueOf(tz.getOffset(GregorianCalendar.getInstance().getTimeInMillis()) / 1000));
                            setUserPrefJsonObject.put("tz_name", tz.getDisplayName());
                            setUserPrefJsonObject.put("userData", mSharedPrefManager.getString(AppConstants.KP_GUID,""));
                            setUserPrefJsonObject.put("dstOffset_secs", String.valueOf(Util.getDSTOffsetValue()));

                            setPrefJsonObject.put("preferences", setUserPrefJsonObject);

                            pillpopperRequestForSetPref.put("pillpopperRequest", setPrefJsonObject);

                            requestArray.put(pillpopperRequestForSetPref);
                        }else{
                            Intent initialGetStateBroadcastIntent = new Intent();
                            initialGetStateBroadcastIntent.setAction(BROADCAST_DAY_LIGHT_SAVING_ADJUSTMENT_DONE);
                            sendBroadcast(initialGetStateBroadcastIntent);
                            PillpopperRunTime.getInstance().setReminderNeedToShow(true);
                        }

                    }

                }

                if(requestArray.length()>0){
                    requestArray.put(Util.prepareGetState(getApplicationContext()));
                    multiRequest.put("requestArray", requestArray);
                    multiRequest.put("getAllOutput", 1);
                    finalRequestObj.put("pillpopperMultiRequest", multiRequest);
                    UserList[] userList = null;

                    try {
                        JSONObject response = PillpopperServer.getInstance(getApplicationContext(),pillpopperAppContext).makeIntermediateSync(finalRequestObj, Util.buildHeaders(getApplicationContext()), AppConstants.ConfigParams.getWsPillpopperSecuredBaseURL());

                        if(null!=response) {
                            Gson gson = new Gson();
                            IntermittentSyncMultiResponse result = gson.fromJson(response.toString(), IntermittentSyncMultiResponse.class);
                            ResponseArray[] responseArray = result.getPillpopperMultiResponse().getResponseArray();
                            if (null != responseArray) {
                                userList = responseArray[responseArray.length-1].getMultiPillpopperResponse().getUserList();
                            }
                            if(userList!=null){
                                updateUsersLastSyncTokenFromGetState(userList,getApplicationContext());
                            }
                        }

                        Intent initialGetStateBroadcastIntent = new Intent();
                        initialGetStateBroadcastIntent.setAction(BROADCAST_DAY_LIGHT_SAVING_ADJUSTMENT_DONE);
                        sendBroadcast(initialGetStateBroadcastIntent);
                        PillpopperRunTime.getInstance().setReminderNeedToShow(true);
                        PillpopperRunTime.getInstance().setTimeZoneChanged(false);

                    } catch (PillpopperServer.ServerUnavailableException e) {
                        PillpopperLog.exception(e.getMessage());
                    }
                }
            }
        } catch (JSONException e) {
            PillpopperLog.exception(e.getMessage());
        }
    }

    private void updateUsersLastSyncTokenFromGetState(UserList[] result, Context context) {
        for(UserList user : result){
            FrontController.getInstance(context).updateUsersLastSyncToken(user.getUserId(), user.getLastSyncToken(), user.hasChanges());
        }
    }


    private void handleActionGetState() {
        performGetState();
        updateUserTimeZoneIfNotAvailable();
    }

    private void handleActionGetHistoryEvents() {
        performGetHistoryEvents();
    }

    private void handleActionIntermediateGetState(boolean isNeedToInvokeInSecureMode) {
        performIntermediateGetState(isNeedToInvokeInSecureMode);

    }


    private void performGetState() {
        final PillpopperAppContext pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(getApplicationContext());
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME);
        JSONObject request = new JSONObject();
        PillpopperServer server = null;

        StateWrapper stateWrapper = new StateWrapper();

        State currState = pillpopperAppContext.getState(getApplicationContext());
        if (Util.isEmptyString(currState.getAccountId())) {
            return;
        }

        try {
            server = PillpopperServer.getInstance(getApplicationContext(),pillpopperAppContext);
        } catch (PillpopperServer.ServerUnavailableException e ) {
            PillpopperLog.exception(e.getMessage());
        }

        try {
            request.put("action", "GetState");
            request.put("apiVersion", "Version 6.0.4");
            ActivationController activationController = ActivationController.getInstance();
            Map<String, String> headers = new HashMap<>();
            if (null != activationController.getSSOSessionId(getApplicationContext())) {
                headers.put("secureToken", activationController.getSSOSessionId(getApplicationContext()));
            }
            if(RunTimeData.getInstance().getRegistrationResponse() != null) {
                headers.put("guid", mSharedPrefManager.getString(AppConstants.KP_GUID, ""));
            }
            headers.put("hardwareId", UniqueDeviceId.getHardwareId(getApplicationContext()));

            if (currState.getAccountId() != null) {
                headers.put("userId", currState.getAccountId());
            }

            headers.put("os", TTGMobileLibConstants.OS);
            headers.put("appVersion",Util.getAppVersion(getApplicationContext()));
            headers.put("osVersion",AppConstants.OS_VERSION);

            JSONObject serverResponse = null;
            try {
                PillpopperLog.say("-- TAG Request : " + request.toString());
                if(null != server)
                    serverResponse = server.makeRequest(request, headers/*, Preferences.jsonBooleanString(false)*/);
            } catch (PillpopperServer.ServerUnavailableException e) {
                LoggerUtils.exception(e.getMessage());
            }

            if ((null != serverResponse && Util.checkForSessionExpire(serverResponse.toString()) == Util.STATUS_CODE_125
                    || (null != stateWrapper &&
                            (null!= stateWrapper.getStatusCode() && stateWrapper.getStatusCode().equalsIgnoreCase(Util.STATUS_CODE_125_STRING))))) {
                stateWrapper.setState(null);
                stateWrapper.setStatusCode(Util.STATUS_CODE_125_STRING);
            } else {
                if (null != serverResponse) {
                    State serverState = new State(serverResponse, pillpopperAppContext.getEdition(), pillpopperAppContext.getFDADrugDatabase(), getApplicationContext());
                    Gson gson = new Gson();
                    PillpopperResponse result = gson.fromJson(serverResponse.toString(), PillpopperResponse.class);
                    if (null != result && null != result.getUserList()) {
                        for (UserList userList : result.getUserList()) {
                            for (PillList pillList : userList.getPillList()) {
                                final String imageGuid = pillList.getPreferences().getImageGUID();
                                if (!Util.isEmptyString(imageGuid)) {
                                    ImageSynchronizer imageSynchronizer = ImageSyncManager.getInstance(getApplicationContext());
                                    imageSynchronizer.downloadImage(pillList.getPillId(), imageGuid);
                                }
                            }
                        }
                    } else {
                        // GetState Failed.
                        isGetStateFailed = true;
                    }

                    stateWrapper.setState(serverState);
                    stateWrapper.setStatusCode(Util.STATUS_CODE_0_STRING);
                    // check for access token and start the FDB image download task
                    // if not available make access token call.
                    if (!Util.isEmptyString(FrontController.getInstance(getApplicationContext()).getAccessToken(getApplicationContext()))) {
                        new GetFDBImagesForKPHCDrugsAsyncTask(getApplicationContext()).execute(); //initiate getImages by FDB API requests
                    } else {
                        RunTimeData.getInstance().setGetImagesSkippedWhileHandleGetState(true);
                        TokenService.startGetAccessTokenService(getApplicationContext());
                    }
                } else{
                    isGetStateFailed = true;
                }
            }

        } catch (JSONException e) {
            PillpopperLog.say("got json exception setting up request: %s", e.getMessage());
            handleNetworkFailureScenario();
            try {
                throw new PillpopperServer.ServerUnavailableException();
            } catch (PillpopperServer.ServerUnavailableException e1) {
                PillpopperLog.exception(e.getMessage());
            }
        } catch (PillpopperParseException e) {
            PillpopperLog.say("got parse exception trying to parse server state!");
            handleNetworkFailureScenario();
            try {
                throw new PillpopperServer.ServerUnavailableException();
            } catch (PillpopperServer.ServerUnavailableException e1) {
                PillpopperLog.exception(e.getMessage());
            }
        }

        if (null != stateWrapper && stateWrapper.getStatusCode().equals(Util.STATUS_CODE_125_STRING)) {
            PillpopperLog.say("TAG --- :" + getApplicationContext());
            /*if (null != Util.persistentActivity && !Util.persistentActivity.isFinishing()) {
                Util.showSessionexpireAlert(Util.persistentActivity);
            }*/
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent());
        } else {

            PillpopperLog.say("--TAG in PostExecute got state is not null");
            if (null != stateWrapper && stateWrapper.getState() != null && getApplicationContext() != null && pillpopperAppContext.getState(getApplicationContext()) != null) {
                if (/*initialDownload ||*/ pillpopperAppContext.getState(getApplicationContext()).getPreferences().equals(stateWrapper.getState().getPreferences())) {
                    pillpopperAppContext.getState(getApplicationContext()).setPreferences(stateWrapper.getState().getPreferences());
                    PillpopperLog.say("---Quickview flag  quickviewOptIned flag from server :    " + stateWrapper.getState().getPreferences().getPreference("quickviewOptIned"));
                    if (null == stateWrapper.getState().getPreferences().getPreference("quickviewOptIned")) {
                        mSharedPrefManager.putString(AppConstants.sharedPrefquickViewFlagKey, "", true);
                    } else {
                        mSharedPrefManager.putString(AppConstants.sharedPrefquickViewFlagKey, stateWrapper.getState().getPreferences().getPreference("quickviewOptIned"), true);
                    }
                }

            }

            if (null != stateWrapper && stateWrapper.getState() != null) {
                //pillpopperAppContext.getState()._mergeAfterSync(stateWrapper.state);
                pillpopperAppContext.getState(getApplicationContext()).setSyncTimeMSec(System.currentTimeMillis());
                PillpopperRunTime.getInstance().setmLastSyncTime(Calendar.getInstance());
                //pillpopperAppContext.sendBroadcast();
						/*PillpopperLog.say("---Quickview flag  quickviewOptIned flag from server :    " + returnedState.getPreferences().getPreference("quickviewOptIned"));
						if(null==returnedState.getPreferences().getPreference("quickviewOptIned")){
							mSharedPrefManager.putString(AppConstants.sharedPrefquickViewFlagKey, "", true);
						}else{
							mSharedPrefManager.putString(AppConstants.sharedPrefquickViewFlagKey, returnedState.getPreferences().getPreference("quickviewOptIned"), true);
						}*/

                //Util.setQuickViewOptedruntimeFlag(returnedState.getPreferences().getPreference("quickviewOptIned"));
            }

            //getDrugList().setImagesSuccessfullyTransmitted(context.getAndroidContext(), imagesSuccessfullyTransmitted);

            PillpopperLog.say("SYNC: sync complete");
            PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(true);

        }

        doQuickviewReacceptance();
    }

    private void doQuickviewReacceptance() {
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME);
        //from quickview reacceptance screen - As this is required only after upgrade
        if(("1").equalsIgnoreCase(mSharedPrefManager.getString(AppConstants.QV_USER_PREFERENCE,"-1"))){
            PillpopperLog.say("QV Reacceptance value is true");
            FrontController.getInstance(getApplicationContext()).setSignedOutReminderEnabled(true, FrontController.getInstance(getApplicationContext()).getPrimaryUserIdIgnoreEnabled());
        }else if(("0").equalsIgnoreCase(mSharedPrefManager.getString(AppConstants.QV_USER_PREFERENCE,"-1"))){
            PillpopperLog.say("QV Reacceptance value is false");
            FrontController.getInstance(getApplicationContext()).setSignedOutReminderEnabled(false, FrontController.getInstance(getApplicationContext()).getPrimaryUserIdIgnoreEnabled());
        }else{
            return;
        }

        mSharedPrefManager.putString(AppConstants.QV_USER_PREFERENCE, "-1", false); // reset completely

        JSONObject prefrences = new JSONObject();
        try {
            prefrences.put(PillpopperConstants.ACTION_SETTINGS_SIGNOUT_REMINDERS, FrontController.getInstance(getApplicationContext()).isQuickViewEnabled());
            prefrences.put("userData", mSharedPrefManager.getString(AppConstants.KP_GUID, ""));
            createLogEntry(prefrences);
        } catch (JSONException e) {
            PillpopperLog.say("Exception in loadSignedOutReminderInfo method" + e.getMessage());
        }
    }


    private void createLogEntry(JSONObject prefrences) {
        String replyId = Util.getRandomGuid();
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        logEntryModel.setReplyID(replyId);
        JSONObject jsonObj = Util.prepareSettingsAction(prefrences, replyId, ActivationController.getInstance().getUserId(getApplicationContext()), getBaseContext());
        logEntryModel.setEntryJSONObject(jsonObj,getBaseContext());
        FrontController.getInstance(getBaseContext()).addLogEntry(getApplicationContext(), logEntryModel);
    }

    private void performGetHistoryEvents() {
        PillpopperAppContext pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(getApplicationContext());
        JSONObject request = new JSONObject();
        JSONObject serverResponse = null;
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(getApplicationContext(), AppConstants.AUTH_CODE_PREF_NAME);

        try {
            PillpopperServer server = null;

            State currState = pillpopperAppContext.getState(getApplicationContext());
            if (Util.isEmptyString(currState.getAccountId())) {
                return;
            }

            try {
                server = PillpopperServer.getInstance(getApplicationContext(),pillpopperAppContext);
            } catch (PillpopperServer.ServerUnavailableException e) {
                PillpopperLog.exception(e.getMessage());
            }
            request.put(PillpopperConstants.KEY_ACTION, PillpopperConstants.ACTION_HISTORY_EVENTS);
            ActivationController activationController = ActivationController.getInstance();
            Map<String, String> headers = new HashMap<>();
            if (null != activationController.getSSOSessionId(getApplicationContext())) {
                headers.put("secureToken", activationController.getSSOSessionId(getApplicationContext()));
            }
            headers.put("hardwareId", UniqueDeviceId.getHardwareId(getApplicationContext()));
            if (currState.getAccountId() != null) {
                headers.put("userId", currState.getAccountId());
            }

            if(!Util.isEmptyString(mSharedPrefManager.getString(AppConstants.KP_GUID, ""))) {
                headers.put("guid", mSharedPrefManager.getString(AppConstants.KP_GUID, ""));
            }

            headers.put("os", TTGMobileLibConstants.OS);
            headers.put("appVersion",Util.getAppVersion(getApplicationContext()));
            headers.put("osVersion",AppConstants.OS_VERSION);

            if(PillpopperRunTime.getInstance().isLimitedHistorySyncToDo()) {
                request.put("historyDays", PillpopperConstants.LIMIT_HISTORY); //48hr period
                request.put("currentDeviceTime", "" + PillpopperTime.now().getGmtSeconds());
                PillpopperRunTime.getInstance().setLimitedHistorySyncToDo(false);
            }else{
                request.put("historyDays", ""+FrontController.getInstance(getBaseContext()).getDoseHistoryDays()); //days from settings
                request.put("currentDeviceTime", "" + PillpopperTime.now().getGmtSeconds());
            }
            try {
                PillpopperLog.say("-- TAG Request : " + request.toString());
                if(null != server)
                    serverResponse = server.makeRequest(request, headers);
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
//                handleNetworkFailureScenario();
            }
        } catch (JSONException e) {
            PillpopperLog.say(e.getMessage());
        }


        if (null != serverResponse) {
            Gson gson = new Gson();
            try {
                DatabaseHandler.getInstance(getApplicationContext()).deleteTableData(DatabaseConstants.HISTORY_PREFERENCE_TABLE, getApplicationContext());
                DatabaseHandler.getInstance(getApplicationContext()).deleteTableData(DatabaseConstants.HISTORY_TABLE, getApplicationContext());

                Type groupListType = new TypeToken<List<GetHistoryEvents>>() {}.getType();
                List<GetHistoryEvents> eventsList =  gson.fromJson(serverResponse.getJSONArray("historyEvents").toString(), groupListType);

                DatabaseHandler.getInstance(getApplicationContext()).beginTransaction();
                for (GetHistoryEvents g : eventsList) {
                    //     GetHistoryEvents historyEventsObject = gson.fromJson(historyEventsArray.getJSONObject(i).toString(), GetHistoryEvents.class);
                    // Inserting the history related data into tables
                    DatabaseHandler.getInstance(getApplicationContext()).insert(getApplicationContext(), DatabaseConstants.HISTORY_TABLE, g, "", "");
                    DatabaseHandler.getInstance(getApplicationContext()).insert(getApplicationContext(), DatabaseConstants.HISTORY_PREFERENCE_TABLE, g, "", "");
                    // Showing the History Table
                    // DatabaseHandler.getInstance(getApplicationContext()).showTableData(DatabaseConstants.HISTORY_TABLE);
                    PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(true);
                }

                try {
                    DatabaseHandler.getInstance(getApplicationContext()).setTransactionSuccessful();
                    DatabaseHandler.getInstance(getApplicationContext()).endTransaction();
                } catch (Exception ex){
                    PillpopperLog.exception(ex.getMessage());
                }

                checkForDaylightSavingAdjustment();

                PillpopperLog.say("GetStateCompleted Now we can refresh reminders");
                PillpopperRunTime.getInstance().setReminderNeedToShow(true);
                if (!isGetStateFailed) {
                    Intent initialGetStateBroadcastIntent = new Intent();
                    initialGetStateBroadcastIntent.putExtra(PillpopperConstants.KEY_ACTION, PillpopperConstants.ACTION_HISTORY_EVENTS);
                    initialGetStateBroadcastIntent.setAction(BROADCAST_GET_STATE_COMPLETED);
                    sendBroadcast(initialGetStateBroadcastIntent);
                }
                Intent refreshIntent = new Intent();
                refreshIntent.setAction(BROADCAST_REFRESH_KPHC_FOR_MANAGE_MEMBERS_SELECTION);
                sendBroadcast(refreshIntent);

            } catch (JSONException e) {
                PillpopperLog.exception(e.getMessage());
                handleNoResponseFromServer();
            }

            if(isGetStateFailed){
                isGetStateFailed = false;
                Intent initialGetStateBroadcastIntent = new Intent();
                initialGetStateBroadcastIntent.setAction(BROADCAST_GET_STATE_FAILED);
                sendBroadcast(initialGetStateBroadcastIntent);
            }
        }else{
            handleNoResponseFromServer();
        }
    }

    private void handleNoResponseFromServer(){
        if (handleHistoryFailure) {
            Intent initialGetStateBroadcastIntent = new Intent();
            initialGetStateBroadcastIntent.setAction(BROADCAST_GET_STATE_FAILED);
            sendBroadcast(initialGetStateBroadcastIntent);
            handleHistoryFailure(false);
        }

        Intent initialGetFailedBroadcastIntent = new Intent();
        initialGetFailedBroadcastIntent.setAction(BROADCAST_REMOVE_REGISTRATION_POPUP);
        sendBroadcast(initialGetFailedBroadcastIntent);

        Intent refreshIntent = new Intent();
        refreshIntent.setAction(BROADCAST_REFRESH_KPHC_FOR_MANAGE_MEMBERS_SELECTION);
        sendBroadcast(refreshIntent);
    }

    private String convertDate(String key) {
        PillpopperDay pd = PillpopperDay.parseGMTTimeAsLocalDay(key);
        return PillpopperDay.getLocalizedDateString(pd, false, R.string.__blank, getBaseContext());

    }

    private void performIntermediateGetState(boolean isNeedToInvokeInSecureMode) {
        PillpopperAppContext pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(getApplicationContext());
        JSONObject response = null;
        try {
            JSONObject multipleReqObj = new JSONObject();
            JSONObject finalRequestObj = new JSONObject();
            //JSONObject getStateRequest = prepareGetState(pillpopperAppContext);
            JSONArray usersArray = new JSONArray();
            //JSONArray requestArray = FrontController.getInstance(getApplicationContext()).getLogEntries((PillpopperActivity) getApplicationContext());
            JSONArray requestArray = new JSONArray();

            if (getApplicationContext() != null) {
                if (isNeedToInvokeInSecureMode) {
                    requestArray = FrontController.getInstance(getApplicationContext()).getLogEntries(getApplicationContext());
                } else {
                    //Exclude Secure Actions Ex: EditPill
                    requestArray = FrontController.getInstance(getApplicationContext()).getNonSecureLogEntries(getApplicationContext());
                }
            }

            try {
                if (null != RunTimeData.getInstance().getSelectedUsersList()) {
                    for (String userId : RunTimeData.getInstance().getSelectedUsersList()) {
                        JSONObject userJsonObject = new JSONObject();
                        userJsonObject.put("userGUID", userId);
                        usersArray.put(userJsonObject);
                    }
                } else if (null != RunTimeData.getInstance().getRegistrationResponse() && null != RunTimeData.getInstance().getRegistrationResponse().getResponse()) {
                    List<User> userList = RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers();
                    for (User user : userList) {
                        JSONObject userJsonObject = new JSONObject();
                        userJsonObject.put("userGUID", user.getUserId());
                        userJsonObject.put("lastSyncToken",
                                Util.getLastSyncTokenValue(FrontController.getInstance(getApplicationContext()).getLastSyncTokenForUser(user.getUserId())));
                        usersArray.put(userJsonObject);
                    }
                }
                multipleReqObj.put("requestArray", requestArray);
                multipleReqObj.put("getAllOutput", 1);
                finalRequestObj.put("pillpopperMultiRequest", multipleReqObj);
            } catch (JSONException e) {
                PillpopperLog.exception(e.getMessage());
            }
            if (requestArray.length() != 0) {
                response = PillpopperServer.getInstance(getApplicationContext(), pillpopperAppContext).makeIntermediateSync(finalRequestObj, Util.buildHeaders(getApplicationContext())
                        , isNeedToInvokeInSecureMode ? AppConstants.ConfigParams.getWsPillpopperSecuredBaseURL() : FrontController.getInstance(getApplicationContext()).getLocalNonSecureUrl(getApplicationContext()));
            } else{
                PillpopperLog.say("Oops!, Intermediate Sync not initiated -- Request Array Empty");
            }
        } catch (PillpopperServer.ServerUnavailableException e) {
            PillpopperLog.say("Oops!, ServerUnavailableException");
        }

        if (null != response && isNeedToInvokeInSecureMode) {
            PillpopperLog.say("Intermediate API call Response : " + response.toString());
            PillpopperRunTime.getInstance().setmLastSyncTime(Calendar.getInstance());

            try {
                new State(response, getApplicationContext());

                // commenting the below lines to avoid sending broadcast after intermediate sync.
                // the broadcast will be sent after history response handling
                /*if(PillpopperRunTime.getInstance().isFirstTimeSyncDone() && !RunTimeData.getInstance().isManageMembersInProgress()) {
                    LoggerUtils.info("debug --- Sending Broadcast");
                    Intent intermediateGetStateBroadcastIntent = new Intent();
                    intermediateGetStateBroadcastIntent.setAction(BROADCAST_GET_STATE_COMPLETED);
                    sendBroadcast(intermediateGetStateBroadcastIntent);
                }*/
            } catch (PillpopperParseException e) {
                PillpopperLog.exception(e.getMessage());
            }

            pillpopperAppContext.getState(getApplicationContext()).removeLogEntry(getApplicationContext(), response, false);
        } else {
            try {
                if (null != response) {
                    PillpopperLog.say("Intermediate API call Response : " + response.toString());
                    pillpopperAppContext.getState(getApplicationContext()).removeLogEntry(getApplicationContext(), response, false);
                }else{
                    PillpopperLog.say("Intermediate API call Response is NULL");
                }
            } catch (Exception e) {
                PillpopperLog.say(e);
            }
        }
    }

    private JSONObject prepareGetState(PillpopperAppContext pillpopperAppContext) {
        JSONObject getStateJsonObject = new JSONObject();
        JSONObject getStatePrefJsonObject = new JSONObject();

        try {

            getStatePrefJsonObject.put("apiVersion", "Version 6.0.4");
            getStatePrefJsonObject.put("hardwareId", UniqueDeviceId.getHardwareId(getApplicationContext()));
            getStatePrefJsonObject.put("action", "GetState");
            getStateJsonObject.put("pillpopperRequest", getStatePrefJsonObject);
        } catch (JSONException e) {
            PillpopperLog.exception(e.getMessage());
        }
        return getStateJsonObject;
    }

    public void updateUserTimeZoneIfNotAvailable() {
        if (!FrontController.getInstance(getApplicationContext()).isUserTimeZoneAvailable(getApplicationContext())) {
            FrontController.getInstance(getApplicationContext()).saveUserDefaultTimeZone(getApplicationContext());
        }
    }
}
