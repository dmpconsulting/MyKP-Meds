package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.os.AsyncTask;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponseCompat;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by M1024581 on 12/4/2017.
 */

public class UpdateSetupIntroCompleteTask extends AsyncTask<String, Void, Integer> {

    private final Context mContext;
    private final SharedPreferenceManager mSharedPrefManager;

    public UpdateSetupIntroCompleteTask(Context context){
        mContext = context;
        mSharedPrefManager = SharedPreferenceManager.getInstance(
                mContext, AppConstants.AUTH_CODE_PREF_NAME);
    }

    @Override
    protected Integer doInBackground(String... params) {
            int status = -1;
            AppData appData = AppData.getInstance();

            Map<String, String> baseParams = new HashMap<>(ActivationUtil.getBaseParams(mContext));

            Map<String,String> headers = new HashMap<>();
            if(null!= ActivationController.getInstance().getSSOSessionId(mContext)){
                headers.put("ssoSessionId", ActivationController.getInstance().getSSOSessionId(mContext));
            }

            headers.put("guid", mSharedPrefManager.getString(AppConstants.KP_GUID, ""));

            String response = appData.getHttpResponse(params[0], AppConstants.POST_METHOD_NAME, baseParams, headers,null, mContext);

            if (null != response && !response.equals(AppConstants.HTTP_DATA_ERROR)) {
                Gson gson = new Gson();
                SignonResponseCompat result = gson.fromJson(response, SignonResponseCompat.class);
                LoggerUtils.info("UpdateIntroCompleteStatusResponse: " + response);
                if (result != null && result.getResponse() != null) {
                    String statusCode = result.getResponse().getStatusCode();
                    if(!Strings.isNullOrEmpty(statusCode) && !("").equalsIgnoreCase(statusCode) && !("null").equalsIgnoreCase(statusCode)){
                        try{
                            status = Integer.parseInt(statusCode);
                            // Update IntroFlag Service returns Success
                            //Update shared preferences.
                            mSharedPrefManager.putBoolean(AppConstants.IS_FRESHINSTALL_FLG, false, false);
                        }catch(Exception e){
                            LoggerUtils.info("ERROR: Could not parse the status code");
                        }
                    }
                }
            }
            return status;
    }

    @Override
    protected void onPostExecute(Integer statusCode) {
        super.onPostExecute(statusCode);

        LoggerUtils.info("UpdateSetupIntroCompleteTask Status Code - " + statusCode);
        if(statusCode == 0){
            LoggerUtils.info("UpdateSetupIntroCompleteTask Success");
        }else{
            LoggerUtils.info("UpdateSetupIntroCompleteTask failed");
        }
    }
}
