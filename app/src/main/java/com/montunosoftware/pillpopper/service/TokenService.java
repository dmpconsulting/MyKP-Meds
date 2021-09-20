package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.network.NetworkAPI;
import com.montunosoftware.pillpopper.network.NetworkClient;
import com.montunosoftware.pillpopper.network.model.FailedImageObj;
import com.montunosoftware.pillpopper.network.model.GetTokenResponseObj;
import com.montunosoftware.pillpopper.service.images.sync.ImageSyncManager;
import com.montunosoftware.pillpopper.service.images.sync.ImageSynchronizer;

import org.apache.commons.codec.binary.Base64;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by M1023050 on 16-Nov-18.
 */

public class TokenService extends JobIntentService {

    public static final String TOKEN_URL = AppConstants.ConfigParams.getAPIManagerTokenBaseURL();
    private String CLIENT_ID  = AppConstants.CLIENT_ID;
    private String CLIENT_SECRET = AppConstants.CLIENT_SECRET;

    /**
     * Unique job ID All the Token Service requests.
     * This should be unique for all the requests assigning to this JobIntentService, otherwise we may have to create another class.
     */
    public static final int REVOKE_TOKEN_JOB_ID = 1000;

    /**
     * Actions
     */
    private static final String ACTION_REVOKE_TOKEN = "action.REVOKE_TOKEN";
    private static final String ACTION_GET_ACCESS_TOKEN = "action.GET_ACCESS_TOKEN";
    private static final String ACTION_REFRESH_ACCESS_TOKEN = "action.REFRESH_ACCESS_TOKEN";
    private AppData sAppData;
    private static Context mContext;
    private static TokenRefreshAPIListener mTokenRefreshAPIListener;

    //Convenience methods for enqueuing work in to this service.
    /**
     * To revoke the Token on user Signout from the application
     * @param context
     */
    public static void startRevokeTokenService(Context context, String refreshToken){
        mContext = context;
        //stop timer task
        AppData.getInstance().cancelRefreshTimerTask(context);
        Intent intent = new Intent(context, TokenService.class);
        intent.setAction(ACTION_REVOKE_TOKEN);
        intent.putExtra("refresh_token", refreshToken);
        enqueueWork(context, TokenService.class, REVOKE_TOKEN_JOB_ID, intent);
    }

    /**
     * To Get the Fresh Token
     * @param context
     */
    public static void startGetAccessTokenService(Context context){
        Util.getInstance().setClientIdAndClientSecret();
        mContext = context;
        Intent intent = new Intent(context, TokenService.class);
        intent.setAction(ACTION_GET_ACCESS_TOKEN);
        enqueueWork(context, TokenService.class, REVOKE_TOKEN_JOB_ID, intent);
    }

    /**
     * To refresh the Old Token
     * @param context
     * @param callback
     */
    public static void startRefreshAccessTokenService(Context context, TokenRefreshAPIListener callback){
        mContext = context;
        mTokenRefreshAPIListener = callback;
        Intent intent = new Intent(context, TokenService.class);
        intent.setAction(ACTION_REFRESH_ACCESS_TOKEN);
        enqueueWork(context, TokenService.class, REVOKE_TOKEN_JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sAppData = AppData.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mContext = null;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {

                case ACTION_GET_ACCESS_TOKEN:
                case ACTION_REFRESH_ACCESS_TOKEN:
                    if(!Util.isEmptyString(TOKEN_URL)) {
                        try {
                            NetworkAPI refreshTokenService = NetworkClient.getInstance().prepareClient(TOKEN_URL).create(NetworkAPI.class);
                            Call<GetTokenResponseObj> refreshTokenCall = refreshTokenService.getAccessTokenService(prepareHeaders(), prepareParams(intent.getAction()));
                            refreshTokenCall.enqueue(new Callback<GetTokenResponseObj>() {
                                @Override
                                public void onResponse(Call<GetTokenResponseObj> call, Response<GetTokenResponseObj> response) {
                                    if (response.isSuccessful()) {
                                        LoggerUtils.info("----Firebase----"+FireBaseConstants.Event.API_TOKEN_SUCCESS);
                                        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext, FireBaseConstants.Event.API_TOKEN_SUCCESS);
                                        GetTokenResponseObj getTokenResponseObj = response.body();
                                        if (null != getTokenResponseObj) {
                                            if (!TextUtils.isEmpty(getTokenResponseObj.toString())) {
                                                PillpopperLog.say("Access token response is : " + getTokenResponseObj.toString());
                                            }
                                            if (!TextUtils.isEmpty(getTokenResponseObj.getAccess_token())) {
                                                sAppData.saveAccessToken(mContext, getTokenResponseObj.getAccess_token());
                                            }
                                            if (!TextUtils.isEmpty(getTokenResponseObj.getRefresh_token())) {
                                                sAppData.saveRefreshToken(mContext, getTokenResponseObj.getRefresh_token());
                                            }
                                            if (!TextUtils.isEmpty(getTokenResponseObj.getToken_type())) {
                                                sAppData.saveTokenType(mContext, getTokenResponseObj.getToken_type());
                                            }
                                            if (!TextUtils.isEmpty(getTokenResponseObj.getExpires_in())) {
                                                sAppData.saveTokenExpiryTime(mContext, getTokenResponseObj.getExpires_in());
                                                LoggerUtils.info("--API manager-- Expires in " + getTokenResponseObj.getExpires_in());
                                            }

                                            // set token values for RxRefill.
                                            if (RunTimeData.getInstance().isNeedToSetValuesForRefill()) {
                                                RunTimeData.getInstance().setIsNeedToSetValuesForRefill(false);
                                                Util.setTokenValuesForRxRefill(getTokenResponseObj);
                                            }

                                            //start timer task
                                            AppData.getInstance().startRefreshTokenTimerTask(mContext);

                                            //Download only the failed images
                                            if(RunTimeData.getInstance().getAccessTokenCalledForFailedFDBImages()) {
                                                LoggerUtils.info("--API manager-- get access token success for failed FDB images--");
                                                RunTimeData.getInstance().setAccessTokenCalledForFailedFDBImages(false);
                                                List<FailedImageObj> mFailedImageObjList = FrontController.getInstance(mContext).getFailedImageEntryList();
                                                if (null != mFailedImageObjList && !mFailedImageObjList.isEmpty()) {
                                                    RunTimeData.getInstance().setImageAPIDownloadCounter(mFailedImageObjList.size());
                                                    ImageSynchronizer imageSynchronizer = ImageSyncManager.getInstance(mContext);
                                                    for (FailedImageObj failedImageObj : mFailedImageObjList) {
                                                        if (PillpopperConstants.IMAGE_TYPE_NDC.equalsIgnoreCase(failedImageObj.getImageType())) {
                                                            imageSynchronizer.downloadFdbImageByNdcCode(failedImageObj.getPillID(), failedImageObj.getImageId());
                                                        } else {
                                                            imageSynchronizer.downloadFdbImageById(failedImageObj.getPillID(), failedImageObj.getImageId());
                                                        }
                                                    }
                                                }
                                            } else if(RunTimeData.getInstance().isGetImagesSkippedWhileHandleGetState()){
                                                // this will be executed only one when we call AccessToken for the first time
                                                RunTimeData.getInstance().setGetImagesSkippedWhileHandleGetState(false); //reset
                                                new GetFDBImagesForKPHCDrugsAsyncTask(mContext).execute();
                                            }
                                        }

                                        if (null != mTokenRefreshAPIListener && ACTION_REFRESH_ACCESS_TOKEN.equalsIgnoreCase(intent.getAction())) {
                                            mTokenRefreshAPIListener.tokenRefreshSuccess();
                                        }
                                    } else {
                                        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext, FireBaseConstants.Event.API_TOKEN_FAIL);
                                        LoggerUtils.info("----Firebase----"+FireBaseConstants.Event.API_TOKEN_FAIL);
                                        LoggerUtils.error("Debug --- Token API failed -- " + response.message());
                                    }
                                }

                                @Override
                                public void onFailure(Call<GetTokenResponseObj> call, Throwable t) {
                                    PillpopperLog.say("Debug --- Access token Failed ");
                                    LoggerUtils.info("----Firebase----"+FireBaseConstants.Event.API_TOKEN_FAIL);
                                    FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext, FireBaseConstants.Event.API_TOKEN_FAIL);
                                    if (null != mTokenRefreshAPIListener && ACTION_REFRESH_ACCESS_TOKEN.equalsIgnoreCase(intent.getAction())) {
                                        mTokenRefreshAPIListener.tokenRefreshError();
                                    }
                                }
                            });
                        } catch (Exception e){
                            LoggerUtils.error("Exception" + e.getMessage());
                        }
                    } else{
                        LoggerUtils.error("Debug --- The Token_URL is empty");
                    }
                    break;

                case ACTION_REVOKE_TOKEN:
                    if(!Util.isEmptyString(TOKEN_URL)) {
                        try {
                            NetworkAPI service = NetworkClient.getInstance().prepareClient(TOKEN_URL).create(NetworkAPI.class);
                            Call<ResponseBody> call = service.getRevokeTokenService(prepareHeaders(), prepareParamsForRevokeToken(intent.getStringExtra("refresh_token")));
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        PillpopperLog.say("revoke token response is : " + response.message());
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    PillpopperLog.say("revoke token Failed ");
                                }
                            });
                        }catch (Exception e){
                            LoggerUtils.exception(e.getMessage());
                        }
                    } else{
                        LoggerUtils.error("The Token_URL is empty");
                    }
                    break;
            }
        }
    }

    private Map<String, String> prepareParams(String action) {
        Map<String, String> paramsForRequest = new HashMap<>();
        try {
            if(action.equalsIgnoreCase(ACTION_GET_ACCESS_TOKEN)){
                paramsForRequest.put("grant_type", "ssotoken");
                if(null!=ActivationController.getInstance().getSSOSessionId(this))
                    paramsForRequest.put("ssosession", URLEncoder.encode(ActivationController.getInstance().getSSOSessionId(this), "utf-8"));

                String ssoEnvironmentMap = Util.getMappedSSOEnvironment(Util.getKeyValueFromAppProfileRuntimeData(TTGMobileLibConstants.mConfigSignonUrl));
                //if(!Util.isEmptyString(ssoEnvironmentMap)) {
                paramsForRequest.put("ssoenvironment", ssoEnvironmentMap);
                //}
                String guid = RunTimeData.getInstance().getSigninRespObj().getGuid();
                if (!Util.isEmptyString(guid)) {
                    paramsForRequest.put("guid",guid);
                }
            }else if(action.equalsIgnoreCase(ACTION_REFRESH_ACCESS_TOKEN)){
                paramsForRequest.put("grant_type", "refresh_token");
                if(!Util.isEmptyString(FrontController.getInstance(this).getRefreshToken(this))) {
                    paramsForRequest.put("refresh_token", FrontController.getInstance(this).getRefreshToken(this));
                }
            }
        } catch (Exception ex){
            LoggerUtils.info("Unable to prepare params");
        }
        return paramsForRequest;
    }

    private Map<String, String> prepareHeaders(){
        Map<String, String> headers = new HashMap();
        headers.put("Authorization",
                "Basic " + new String(Base64.encodeBase64((CLIENT_ID + ":" + CLIENT_SECRET).getBytes())));
        if(null!=getLocalCookies()) {
            headers.put("Cookie", getLocalCookies());
        }

        return headers;
    }

    private Map<String, String> prepareParamsForRevokeToken(String refresh_token) {
        Map<String, String> paramsForRequest = new HashMap<>();
        try {
            if (!Util.isEmptyString(refresh_token))
                paramsForRequest.put("token", refresh_token);
        } catch (Exception ex) {
            LoggerUtils.info("Unable to prepare params for GetAccessToken");
        }
        return paramsForRequest;
    }

    public String getLocalCookies() {
        return CookieManager.getInstance().getCookie("https://" + AppConstants.ConfigParams.getKeepAliveCookieDomain());
    }


    public interface TokenRefreshAPIListener {
        void tokenRefreshSuccess();
        void tokenRefreshError();
    }

}
