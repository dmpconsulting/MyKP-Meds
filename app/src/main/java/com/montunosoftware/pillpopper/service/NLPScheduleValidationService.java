package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.NLPUtils;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.NLPSigValidationRequestObj;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttgmobilelib.utilities.TTGUtil;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class NLPScheduleValidationService extends AsyncTask<String, Void, String> {

    private final Context mContext;
    private final NLPSigValidationRequestObj requestObject;

    public NLPScheduleValidationService(Context context, NLPSigValidationRequestObj requestObject){
        mContext = context;
        this.requestObject = requestObject;
    }

    @Override
    protected String doInBackground(String... url) {
        /*String env = EnvSwitchUtils.getCurrentEnvironmentName(mContext);
        if (Util.isEmptyString(env)) {
            return null;
        }*/
        try {
            HttpURLConnection httpURLConnection = NLPUtils.makeRequest(AppConstants.ConfigParams.getNLPSigValidationAPIURL(),
                    AppConstants.POST_METHOD_NAME,
                    buildHeaders(mContext), prepareNLPRequestObject(requestObject));

            String response = null;

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response = TTGUtil.convertResponseTOString(httpURLConnection.getInputStream());
                FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext, FireBaseConstants.Event.NLP_SCHEDULE_VALIDATION_SUCCESS);
            } else {
                String failureInfo = httpURLConnection.getResponseCode() + "~" + (!Util.isEmptyString(httpURLConnection.getResponseMessage()) ? httpURLConnection.getResponseMessage() : "N/A");
                Bundle bundle = new Bundle();
                if (failureInfo.length() < 100) {
                    bundle.putString(FireBaseConstants.ParamName.FAILURE_INFO, failureInfo);
                } else {
                    bundle.putString(FireBaseConstants.ParamName.FAILURE_INFO, failureInfo.substring(0, 99));
                }
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.NLP_SCHEDULE_VALIDATION_FAIL, bundle);
            }

            if (null != response
                    && !response.equals(AppConstants.HTTP_DATA_ERROR)) {
                return response;
            }
        } catch (Exception ex) {
            LoggerUtils.exception("NLP Reminders exception: " + ex.getMessage());
        }

        return null;
    }

    public Map<String,String> buildHeaders(Context context){
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization",getAuthorization(context));
        headers.put("Content-Type","application/json");
        return headers;
    }

    private static String getAuthorization(Context context) {
        StringBuilder authorization = new StringBuilder();
        ActivationController activationController = ActivationController.getInstance();
        LoggerUtils.info("Access token  -- " + activationController.getAccessToken(context));
        if (null != activationController.getAccessToken(context)) {
            authorization = new StringBuilder();
            authorization.append(activationController.getTokenType(context));
            authorization.append(" ");
            authorization.append(activationController.getAccessToken(context));
        }
        return authorization.toString();
    }

    private JSONObject prepareNLPRequestObject(NLPSigValidationRequestObj requestObject) {
        JSONObject request = new JSONObject();
        try {
            request.put("pillId", requestObject.getPillId());
            request.put("userResponse", requestObject.getMobileResponse());
            request.put("changeInSchedule", requestObject.getChangeInSchedule());
        } catch (JSONException e) {
            LoggerUtils.exception(e.getMessage());
        }
        return request;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        LoggerUtils.info("Sig Validation response -- " + result);
    }
}

