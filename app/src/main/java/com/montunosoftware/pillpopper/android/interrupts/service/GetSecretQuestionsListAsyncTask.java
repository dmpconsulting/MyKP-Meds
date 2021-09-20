package com.montunosoftware.pillpopper.android.interrupts.service;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionsResponse;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.TransparentLoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.GenericHttpResponse;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.GenericHttpClient;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

public class GetSecretQuestionsListAsyncTask extends AsyncTask<Void, Void, SecretQuestionsResponse> {

    private Context mContext;
    private GetQuestionsListener listener;


    public GetSecretQuestionsListAsyncTask(Context context, GetQuestionsListener listener) {
        this.mContext = context;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        ((Activity)mContext).startActivityForResult(new Intent(mContext,
                TransparentLoadingActivity.class), 0);
    }

    @Override
    protected SecretQuestionsResponse doInBackground(Void... params) {
        String responseStr = null;
        SecretQuestionsResponse secretQuestionsResponse = null;
        GenericHttpClient gHttpClient = GenericHttpClient.getInstance();
        GenericHttpResponse genericHttpResponseDataObject = null;
        FrontController mFrontController = FrontController.getInstance(mContext);
        try {
            genericHttpResponseDataObject = gHttpClient.executeHttpUrlRequest(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_SIGNON_SECRECT_QUESTION_URL_KEY), AppConstants.HTTP_METHOD_GET,
                    null, Util.buildHeadersForSecretQuestions(AppConstants.USER_AGENT_CATEGORY, AppConstants.ANDROID_OS_VERSION,
                            AppConstants.ANDROID_DEVICE_MAKE, AppConstants.APIKEY, AppConstants.APPNAME + " " + Util.getAppVersion(mContext)));
            if (genericHttpResponseDataObject != null) {
                responseStr = genericHttpResponseDataObject.getData();
                secretQuestionsResponse = mFrontController
                        .parseSecretQuestionsResponse(mContext, responseStr);
                LoggerUtils.info("---Secret questions--- Response --- " + genericHttpResponseDataObject.getData());
            }
        } catch (Exception e) {
            LoggerUtils.info(e.getMessage());
        }

        return secretQuestionsResponse;
    }

    @Override
    protected void onPostExecute(SecretQuestionsResponse secretQuestionsResponse) {
        super.onPostExecute(secretQuestionsResponse);
        ((Activity)mContext).finishActivity(0);
        if(null != secretQuestionsResponse && null != secretQuestionsResponse.getSecretQuestionsMap()
                && !secretQuestionsResponse.getSecretQuestionsMap().isEmpty()) {
            listener.getSecretQuestions(secretQuestionsResponse);
        }else{
            GenericAlertDialog mErrorAlert = new GenericAlertDialog(mContext, null,
                    mContext.getResources().getString(R.string.alert_error_status_20),
                    mContext.getResources().getString(R.string.ok_text), (dialog, which) -> {
                        dialog.dismiss();
                        RunTimeData.getInstance().setInterruptScreenBackButtonClicked(true);
                        ((Activity) mContext).finish();
                    }, null, null);
            if(!mErrorAlert.isShowing()) {
                mErrorAlert.showDialog();
            }
        }

    }

    public interface GetQuestionsListener{
        void getSecretQuestions(SecretQuestionsResponse secretQuestionsResponse);
    }
}
