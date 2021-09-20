package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.montunosoftware.pillpopper.android.util.NotificationBar_OverdueDose;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.HasStatusUpdateResponseObj;

import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

/**
 * @author
 * Created by M1024581 on 7/26/2016.
 */
public class HasStatusUpdateAsyncTask extends AsyncTask<Void, Void, JSONObject> {

    private final Context mContext;
    private final AppData sAppData;
    private final PillpopperAppContext appContext;

    private HasStatusUpdateResponseListener mCallback;

    public interface HasStatusUpdateResponseListener{
        void onHasStatusUpdateResponseReceived(HasStatusUpdateResponseObj response);
    }

    public HasStatusUpdateAsyncTask(Context context, HasStatusUpdateResponseListener callback){
        mContext = context;
        appContext = PillpopperAppContext.getGlobalAppContext(context);
        sAppData = AppData.getInstance();
        mCallback = callback;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {

        RunTimeData.getInstance().setHasStatusCallInProgress(true);

        JSONObject response = null;
        PillpopperServer server =  null;
        try {
            server = PillpopperServer.getInstance(mContext,appContext);
        } catch (PillpopperServer.ServerUnavailableException e) {
            LoggerUtils.exception(e.getMessage());
        }
        try {
            if (null != server) {
                response = server.makeHasStatusUpdateRequest(FrontController.getInstance(mContext).getLocalNonSecureUrl(mContext));
            }
        } catch (PillpopperServer.ServerUnavailableException e) {
            LoggerUtils.error("Server Unavailable Exception in HasStatus Update AsyncTask - " + e.getMessage());
        } catch (Exception e){
            LoggerUtils.error("Exception in HasStatus Update AsyncTask - " + e.getMessage());
        }

        return response;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        super.onPostExecute(response);
        RunTimeData.getInstance().setHasStatusCallInProgress(false);
        if (null != response) {
            Gson gson = new Gson();
            HasStatusUpdateResponseObj result = gson.fromJson(response.toString(),
                    HasStatusUpdateResponseObj.class);
            saveMainAlertElementsAndTimeStamp(result);
            RunTimeData.getInstance().setHasStatusCallInProgress(false);
            if(null != mCallback) {
                mCallback.onHasStatusUpdateResponseReceived(result);
            } else{
                if(Util.hasPendingAlertsNeedForceSignIn(mContext)){
                    NotificationBar_OverdueDose.createSignInRequiredNotification(mContext);
                }
            }
        }else{
            RunTimeData.getInstance().setHasStatusCallInProgress(false);
            if(null != mCallback) {
                mCallback.onHasStatusUpdateResponseReceived(null);
            }
        }
    }

    private void saveMainAlertElementsAndTimeStamp(HasStatusUpdateResponseObj result) {
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(
                mContext, AppConstants.AUTH_CODE_PREF_NAME);

        mSharedPrefManager.putString(AppConstants.KPHC_MEDS_STATUS_CHANGED, result.getPillpopperResponse().getKphcMedsStatusChanged(), false);
        mSharedPrefManager.putString(AppConstants.MED_ARCHIVED_OR_REMOVED, result.getPillpopperResponse().getMedArchivedOrRemoved(),false);
        mSharedPrefManager.putString(AppConstants.PROXY_STATUS_CODE, result.getPillpopperResponse().getProxyStatusCode(),false);
        mSharedPrefManager.putString(AppConstants.MEDICATION_SCHEDULE_CHANGED, result.getPillpopperResponse().getMedicationScheduleChanged(),false);
        mSharedPrefManager.putLong(AppConstants.HAS_STATUS_UPDATE_TIMESTAMP, System.currentTimeMillis(), false);

    }
}
