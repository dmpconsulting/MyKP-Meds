package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.os.AsyncTask;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.GenericHttpResponse;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.GenericHttpClient;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttgmobilelib.utilities.TTGUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author
 * Created by M1024581 on 7/26/2016.
 */
public class AcknowledgeStatusAsyncTask extends AsyncTask<String, Void, GenericHttpResponse> {

    private final Context mContext;
    private final PillpopperAppContext appContext;

//    private AcknowledgeStatusResponseListener mCallback;
//
//    public interface AcknowledgeStatusResponseListener {
//        void onAcknowledgeStatusResponseReceived(HasStatusUpdateResponseObj response);
//    }

    public AcknowledgeStatusAsyncTask(Context context/*, AcknowledgeStatusResponseListener callback*/){
        mContext = context;
        appContext = PillpopperAppContext.getGlobalAppContext(context);
    }

    /**
     *
     * @param params
     * [0] - Url
     * [1] - userId
     * [2] - action
     * @return GenericHttpResponse
     */
    @Override
    protected GenericHttpResponse doInBackground(String... params) {
        GenericHttpResponse httpResponse = new GenericHttpResponse();
        String response = null;
        InputStream stream = null;
        try {
            //params[0] - Url
            //params[1] - userId
            //params[2] - action
            RunTimeData.getInstance().setRuntimeSSOSessionID(ActivationController.getInstance().getSSOSessionId(mContext));
            GenericHttpClient httpClient = GenericHttpClient.getInstance();
            stream = httpClient.makeRequest(params[0], AppConstants.POST_METHOD_NAME, null,ActivationUtil.buildHeaders(mContext), PillpopperServer.getInstance(mContext,appContext).prepareAcknowledgeStatusRequestObj(params[1],params[2]));
            if (null != stream ) {
                response = TTGUtil.convertResponseTOString(stream);
                LoggerUtils.info("Response : " + response);
                httpResponse.setStatus(true);
                httpResponse.setData(response);
            } else {
                httpResponse.setStatus(false);
                httpResponse.setData(AppConstants.HTTP_DATA_ERROR);
            }
        } catch (IOException e) {
            LoggerUtils.error("IO Exception in Acknowledge AsyncTask - " + e.getMessage());
        } catch (PillpopperServer.ServerUnavailableException e) {
            LoggerUtils.error("Server Unavailable Exception in Acknowledge AsyncTask - " + e.getMessage());
        } catch (Exception e){
            LoggerUtils.error("Exception in Acknowledge Update AsyncTask - " + e.getMessage());
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                LoggerUtils.error("Exception in Acknowledge Update AsyncTask - " + e.getMessage());
            }
        }

        if(!httpResponse.getStatus()){
            httpResponse.setStatus(false);
            httpResponse.setData(AppConstants.HTTP_DATA_ERROR);
        }

        return httpResponse;
    }

    @Override
    protected void onPostExecute(GenericHttpResponse response) {
        super.onPostExecute(response);
        if (null != response
                && !response.getData().equalsIgnoreCase(AppConstants.HTTP_DATA_ERROR)) {
            if (!AppConstants.isByPassLogin()) {
                // invoked from Splash.. So clear only the Proxy Status Code
                SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(
                        mContext, AppConstants.AUTH_CODE_PREF_NAME);
                mSharedPrefManager.remove(AppConstants.PROXY_STATUS_CODE);
            } else{
                Util.clearHasStatusUpdateValues(mContext);
            }
        }
    }
}
