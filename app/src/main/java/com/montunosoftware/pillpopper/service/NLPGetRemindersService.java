package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.NLPUtils;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.NLPReminder;
import com.montunosoftware.pillpopper.model.NLPRemindersRequestObject;
import com.montunosoftware.pillpopper.model.NLPRemindersResponseObj;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttgmobilelib.utilities.TTGUtil;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class NLPGetRemindersService extends AsyncTask<String, Void, NLPReminder> {

    private final Context mContext;
    private final NLPRemindersRequestObject remindersRequestObject;

    private NLPRemindersResponseListener mCallback;

    public interface NLPRemindersResponseListener{
        void onNLPRemindersResponseReceived(NLPReminder nlpReminder);
    }

    public NLPGetRemindersService(Context context, NLPRemindersRequestObject remindersRequestObject, NLPRemindersResponseListener callback){
        mContext = context;
        this.remindersRequestObject = remindersRequestObject;
        mCallback = callback;
    }

    @Override
    protected NLPReminder doInBackground(String... url) {
        /*String env = EnvSwitchUtils.getCurrentEnvironmentName(mContext);
        if(Util.isEmptyString(env)){
            return null;
        }*/
        try {
            HttpURLConnection httpURLConnection = NLPUtils.makeRequest(AppConstants.ConfigParams.getNLPRemindersAPIURL(),
                    AppConstants.POST_METHOD_NAME,
                    buildHeaders(mContext), prepareNLPRequestObject(remindersRequestObject));
            String response = null;

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response = TTGUtil.convertResponseTOString(httpURLConnection.getInputStream());
                FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext, FireBaseConstants.Event.NLP_EXTRACT_REMINDER_SUCCESS);
            } else {
                String failureInfo = httpURLConnection.getResponseCode() + "~" + (!Util.isEmptyString(httpURLConnection.getResponseMessage()) ? httpURLConnection.getResponseMessage() : "N/A");
                Bundle bundle = new Bundle();
                if (failureInfo.length() < 100) {
                    bundle.putString(FireBaseConstants.ParamName.FAILURE_INFO, failureInfo);
                } else {
                    bundle.putString(FireBaseConstants.ParamName.FAILURE_INFO, failureInfo.substring(0, 99));
                }
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.NLP_EXTRACT_REMINDER_FAIL, bundle);
            }

            if (null != response
                    && !response.equals(AppConstants.HTTP_DATA_ERROR)) {
                LoggerUtils.info("NLP Reminders Response: " + response);
                Gson gson = new Gson();
                NLPRemindersResponseObj result = gson.fromJson(response, NLPRemindersResponseObj.class);
                if (null != result && null != result.getNLPReminders() && result.getNLPReminders().size() > 0) {
                    return result.getNLPReminders().get(0);//returning first reminder
                }
            }
        } catch (Exception ex){
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

    private JSONObject prepareNLPRequestObject(NLPRemindersRequestObject remindersRequestObject) {
        JSONObject request = new JSONObject();
        try {
            request.put("instructions", remindersRequestObject.getInstructions());
            request.put("dosage", remindersRequestObject.getDosage());
            request.put("medicine", remindersRequestObject.getMedicineName());
            request.put("startDate",remindersRequestObject.getStartDate());
            request.put("endDate", remindersRequestObject.getEndDate());
            request.put("pillId",remindersRequestObject.getPillId());
        } catch (JSONException e) {
            LoggerUtils.exception(e.getMessage());
        }
        return request;
    }

    @Override
    protected void onPostExecute(NLPReminder result) {
        super.onPostExecute(result);
        if (null != result) {
            RunTimeData.getInstance().getDrugsNLPRemindersList().put(remindersRequestObject.getPillId(), result);
        }
        if (null != mCallback) {
            mCallback.onNLPRemindersResponseReceived(result);
        }
    }
}

