package org.kp.tpmg.mykpmeds.activation.handler;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.CookieManager;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.firebaseMessaging.FCMHandler;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponse;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponseCompat;
import org.kp.tpmg.mykpmeds.activation.model.SignonResult;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ActivationHandler {

    private SignonResult signOnObj;
    private String ssoSessionid;

    /**
     * Initiates the signin operation and returns the status code
     *
     * @param userName provided username
     * @param appData  AppData instance
     * @param context  context
     * @return loginStatus code
     */
    public SignonResponse initSession(String guId, String userName, AppData appData, Context context, String ssoSession, String ebizaccountRoles) {

        int loginStatus = -1;
        ssoSessionid = ssoSession;
        SignonResponse response = null;
        List<String> enabledUsers = new ArrayList<>();
        if (ActivationUtil.isNetworkAvailable(context)) {
            response = getInitSessionWSResponse(userName, guId, appData, context, ssoSession, ebizaccountRoles);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                LoggerUtils.exception(e.getMessage());
            }
            if (null != response && null != response.getResponse()) {
                loginStatus = Integer.parseInt(response.getResponse().getStatusCode());
            }
            if (loginStatus == 0 && null != response && null != response.getResponse()) {

                ActivationController activationController = ActivationController.getInstance();
                // Storing ssosessionId
                appData.setSSOSessionId(context, ssoSessionid);

                RunTimeData.getInstance().setRuntimeSSOSessionID(ssoSessionid);
                String switchDeviceFl = signOnObj.getSwitchDeviceFlag();

                appData.checkForNewUser(context, signOnObj.getKpGUID()); // Changing from signOnObj.getPrimaryUserId() Since the checkForNewUser() method logic has been implmented based on the guid Not based on the registerId

                List<User> enabledUsersList = new ArrayList<>();
                for (User user : response.getResponse().getUsers()) {
                    LoggerUtils.info("User info : " + user.getFirstName() + " Enabled Status : " + user.getEnabled());
                    if (("Y").equalsIgnoreCase(user.getEnabled())) {
                        enabledUsers.add(user.getUserId());
                        enabledUsersList.add(user);
                    }
                }
                RunTimeData.getInstance().setSelectedUsersList(enabledUsers);
                RunTimeData.getInstance().setEnabledUsersList(enabledUsersList);
//				}

                if (null != switchDeviceFl && ("true").equalsIgnoreCase(switchDeviceFl) && activationController.isNewUser(context)) {
                    loginStatus = AppConstants.DEVICE_USER_SWITCH_STATUSCODE; // User and Device switch detects
                } else if (null != switchDeviceFl && ("true").equalsIgnoreCase(switchDeviceFl)) {
                    loginStatus = AppConstants.DEVICE_SWITCH_STATUSCODE;
                } else if (activationController.isNewUser(context)) {
                    loginStatus = AppConstants.USER_SWITCH_STATUSCODE; // User switch
                } else { //  Stores the user info after successfully authenticated.
                    appData.storeSessionResponse(context, signOnObj, userName.toString());
                }
                response.getResponse().setStatusCode(String.valueOf(loginStatus));
                storeCookieInfo(ssoSessionid);
            } else {
                ActivationController.getInstance().resetCookiesInfo();
            }
        }
        return response;
    }

    private void storeCookieInfo(String ssoSessionid) {
        try {
            final CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookies(null);
            String cookieDomain = "";
            String cookieValue = "";
            if (!(AppConstants.ConfigParams.getRefillCookieDomain().startsWith("https://") || AppConstants.ConfigParams.getRefillCookieDomain().startsWith("http://"))) {
                cookieDomain = "https://" + AppConstants.ConfigParams.getRefillCookieDomain();
            }
            cookieValue = AppConstants.ConfigParams.getRefillCookieName() + "=" + URLEncoder.encode(ssoSessionid, "UTF-8");
            cookieManager.setAcceptCookie(true);


            if (cookieDomain.length() > 0 && cookieValue.length() > 0) {
                cookieManager.setCookie(cookieDomain, cookieValue);
                cookieManager.flush();
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    LoggerUtils.info("Exception in delay....");
                }
            }
            LoggerUtils.info("createObssoCookie is done..........");

        } catch (Exception e) {
            LoggerUtils.info("Exception While creating the OBSSOCookie");
        }
    }

    /**
     * Performs the signin operation and get the status code
     *
     * @param userName provided user name
     * @param guid     provided guid
     * @param appData  AppData instance
     * @param context  context
     * @return The status code for initSession web service call.
     */
    public SignonResponse getInitSessionWSResponse(String userName, String guid, AppData appData, Context context, String ssoSession, String ebizaccountRoles) {
        int status = -1;
        SignonResponse result = null;

        Map<String, String> params = new HashMap<>();
        params.putAll(ActivationUtil.getBaseParams(context));
        params.put("username", userName);
        Map<String, String> headers = new HashMap<>();
        headers.put("ssoSessionId", ssoSession);
        headers.put("guid", guid);
        headers.put("ebizaccountRoles", ebizaccountRoles);
        headers.put("os", TTGMobileLibConstants.OS);
        headers.put("appVersion",Util.getAppVersion(context));
        headers.put("osVersion",AppConstants.OS_VERSION);

        JSONObject requestJsonString;
        String activationInitUrl;
        requestJsonString = prepareRequestJson(context, guid, userName);
        activationInitUrl = AppConstants.getRegisterURL();

        String response = appData.getHttpResponse(activationInitUrl, AppConstants.POST_METHOD_NAME, null, headers, requestJsonString, context);
        LoggerUtils.info("-- Regsiter Response : " + response);
        //Firebase event
        String event = !Util.isEmptyString(response) && !response.equals(AppConstants.HTTP_DATA_ERROR)
                ? FireBaseConstants.Event.REGISTER_CALL_SUCCESS
                : FireBaseConstants.Event.REGISTER_CALL_FAIL;
        LoggerUtils.info("----Firebase----" + event);
        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(context, event);
        if (response != null && !response.equals(AppConstants.HTTP_DATA_ERROR)) {
            Gson gson = new Gson();
            try {
                result = gson.fromJson(response, SignonResponse.class);
                result.setResponse(result.getResponse());
            } catch (Exception ie) {
                PillpopperLog.exception(ie.getMessage());
            }
            if (result != null && result.getResponse() != null) {
                signOnObj = result.getResponse();
                String statusCode = result.getResponse().getStatusCode();
                signOnObj.setSetUpCompleteFl(result.getResponse().getSetUpCompleteFl());
                if (!Strings.isNullOrEmpty(statusCode) && !("").equalsIgnoreCase(statusCode) && !("null").equalsIgnoreCase(statusCode)) {
                    try {
                        status = Integer.parseInt(statusCode);
                    } catch (Exception e) {
                        LoggerUtils.info("ERROR: Could not parse the status code");
                    }
                } else {
                    status = -2;
                    result.getResponse().setStatusCode(String.valueOf(status));

                }
            }
        } else if (null != response && response.equals(AppConstants.HTTP_DATA_ERROR)) {
            result = new SignonResponse();
            SignonResult obj = new SignonResult();

            status = -3;

            obj.setStatusCode(String.valueOf(status));
            result.setResponse(obj);
        }
        return result;
    }

    /**
     * Stores the user info after successfully authenticated and after device/user switch.
     *
     * @param userName user name
     * @param appData  AppData instance
     * @param ctx      context
     */
    public void storeInitResponse(String userName, AppData appData, Context ctx) {
        appData.storeSessionResponse(ctx, signOnObj, userName);
        //Storing ssosessionId
        appData.setSSOSessionId(ctx, ssoSessionid);
    }

    //Preferences that are sent along with the register request has to be taken from the current state which is empty state.
    // This is the drawback of having activation library seperate from main application
    // as the register and many others are merged with main applications data.
    private JSONObject prepareRequestJson(Context context, String guid, String username) {

        JSONObject pillpopperRequest = new JSONObject();
        JSONObject preferences = new JSONObject();
        JSONObject finalObj = new JSONObject();

        try {
            pillpopperRequest.put("action", "Register");
            pillpopperRequest.put("tosAgreed", 1);
            pillpopperRequest.put("identifyByPref", "username");
            String versionName;
            try {
                versionName = String.format(Locale.US, "Android-KP-%s",
                        context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                versionName = "Android-unknown";
            }
            pillpopperRequest.put("clientVersion", versionName);
            pillpopperRequest.put("hardwareId", ActivationUtil.getDeviceId(context));
            pillpopperRequest.put("partnerId", "KP");
            pillpopperRequest.put("currentTime", GregorianCalendar.getInstance().getTimeInMillis() / 1000);
            pillpopperRequest.put("username", username);
            pillpopperRequest.put("os", AppConstants.PHONE_OS);
            pillpopperRequest.put("osVersion", AppConstants.OS_VERSION);
            pillpopperRequest.put("appId", "MD-5");
            pillpopperRequest.put("appVersion", ActivationUtil.getAppVersion(context));
            pillpopperRequest.put("deviceName", AppConstants.ANDROID_DEVICE_MANUFACTURER);
            //we have to add the deviceNotificationId
            if (AppConstants.IS_FIREBASE_CLOUD_MESSAGING_ENABLED) {
                FCMHandler.getFirebaseToken(token -> {
                    try {
                        pillpopperRequest.put("deviceNotificationId", token);
                    } catch (JSONException e) {
                        LoggerUtils.exception(e.getMessage());
                    }
                });
            }
            // end of the FCM deviceNotificationId Json
            pillpopperRequest.put("deviceMake", AppConstants.ANDROID_DEVICE_MAKE);
            pillpopperRequest.put("deviceModel", "1");
            pillpopperRequest.put("useragentType", AppConstants.PHONE_OS);
            pillpopperRequest.put("deviceId", ActivationUtil.getDeviceId(context));

            preferences.put("language", Locale.getDefault().toString());
            preferences.put("osVersion", String.valueOf(Build.VERSION.SDK_INT));
            preferences.put("lastManagedUpdate", "-1");
            preferences.put("userData", guid);

            pillpopperRequest.put("preferences", preferences);
            finalObj.put("pillpopperRequest", pillpopperRequest);

            LoggerUtils.info("request json" + finalObj.toString());

        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        }

        return finalObj;
    }

}