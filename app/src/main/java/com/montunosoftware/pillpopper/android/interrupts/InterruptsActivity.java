package com.montunosoftware.pillpopper.android.interrupts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.StateListenerActivity;
import com.montunosoftware.pillpopper.android.interrupts.email.EmailMismatchFragment;
import com.montunosoftware.pillpopper.android.interrupts.model.InterruptErrorCode;
import com.montunosoftware.pillpopper.android.interrupts.model.InterruptFailureResponseRoot;
import com.montunosoftware.pillpopper.android.interrupts.secretquestions.SecretQuestionsFragment;
import com.montunosoftware.pillpopper.android.interrupts.temporaryPassword.TemporaryPasswordFragment;
import com.montunosoftware.pillpopper.android.util.FirebaseEventsUtil;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.TransparentLoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.controller.TTGCallBackInterfaces;
import org.kp.tpmg.ttgmobilelib.controller.TTGSignonController;
import org.kp.tpmg.ttgmobilelib.model.TTGInteruptAPIResponse;
import org.kp.tpmg.ttgmobilelib.service.TTGResponseHandler;

import java.net.HttpURLConnection;


public class InterruptsActivity extends StateListenerActivity implements TTGCallBackInterfaces.InterruptAPI, OnConfirmClickListenerInterface {

    private Toolbar mToolbar;
    private GenericAlertDialog mAlertDialog;
    private Handler handler;
    private TTGInteruptAPIResponse mInterruptResposne = null;
    private final int ON_INTERRUPT_RECIEVED_REQUEST_CODE = 1;
    private String emailRequestParams;
    private boolean isAlertDialogShown = false;
    private String mInterruptTypeForEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_intruptions);
        mToolbar = findViewById(R.id.loading_screen_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");

        if (null != getIntent() && getIntent().getStringExtra("mSignOnInterruptType").equalsIgnoreCase(AppConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH) ||
                getIntent().getStringExtra("mSignOnInterruptType").equalsIgnoreCase(AppConstants.SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH)) {
            mInterruptTypeForEmail = getIntent().getStringExtra("mSignOnInterruptType");
            installFragment(new EmailMismatchFragment());
        } else if (null != getIntent() && getIntent().getStringExtra("mSignOnInterruptType").equalsIgnoreCase(AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS)) {
            installFragment(new SecretQuestionsFragment());
        } else if (null != getIntent() && getIntent().getStringExtra("mSignOnInterruptType").equalsIgnoreCase(AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD)) {
            installFragment(new TemporaryPasswordFragment());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        });
        RunTimeData.getInstance().setInturruptScreenEnteredTimeStamp(System.currentTimeMillis());
    }

    private void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void installFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        if (fragment instanceof EmailMismatchFragment){
            bundle.putString("mSignOnInterruptType", mInterruptTypeForEmail);
            fragment.setArguments(bundle);
        }
        FragmentTransaction fragment_transaction = getSupportFragmentManager().beginTransaction();

        fragment_transaction.replace(R.id.fragment_container, fragment, fragment.getTag()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragment_transaction.commit();
    }


    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = () -> syncActionBarArrowState();

    private void syncActionBarArrowState() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        RunTimeData.getInstance().setInturruptScreenVisible(true);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        RunTimeData.getInstance().setInturruptScreenEnteredTimeStamp(System.currentTimeMillis());
    }

    @Override
    public void onInterruptGETAPISuccess(TTGInteruptAPIResponse interuptAPIResponse) {
        //Nothing needed to be handled here.
        FireBaseAnalyticsTracker.getInstance().logEvent(InterruptsActivity.this,
                FireBaseConstants.Event.EVENT_SSO_INTERRUPTS_GET_SUCCESS,
                FireBaseConstants.ParamName.PARAMETER_API_SUCCESS,
                FireBaseConstants.ParamValue.PARAMETER_VALUE_SUCCESS_WITH_STATUS0);
    }

    @Override
    public void onInterruptAPIFailure() {
        handler.post(() -> {
            finishActivity(0);
            FirebaseEventsUtil.invokeInterruptFailureAPIEvent(InterruptsActivity.this);
            showErrorAlert("Sorry couldn't process your request");
        });
    }

    @Override
    public void onInterruptPUTAPISuccess(TTGInteruptAPIResponse ttgInteruptAPIResponse) {
        FireBaseAnalyticsTracker.getInstance().logEvent(InterruptsActivity.this,
                FireBaseConstants.Event.EVENT_SSO_INTERRUPTS_PUT_SUCCESS,
                FireBaseConstants.ParamName.PARAMETER_API_SUCCESS,
                FireBaseConstants.ParamValue.PARAMETER_VALUE_SUCCESS_WITH_STATUS0);
    }

    @Override
    public void onConfirmClick(final String requestParams, String signonResponseInterruptType) {
        if (AppConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH.equalsIgnoreCase(signonResponseInterruptType) ||
                AppConstants.SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH.equalsIgnoreCase(signonResponseInterruptType)) {
            String mismatchType = null;
            if (null != RunTimeData.getInstance().getUserResponse()
                    && !Util.isEmptyString(RunTimeData.getInstance().getUserResponse().getEmail())
                    && RunTimeData.getInstance().getUserResponse().getEmail().equalsIgnoreCase(requestParams)) {
                if (AppConstants.SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH.equalsIgnoreCase(signonResponseInterruptType)){
                    mismatchType = "newEmail";
                }else{
                    mismatchType = "epicEmail";
                }
            } else if (null != RunTimeData.getInstance().getUserResponse()
                    && !Util.isEmptyString(RunTimeData.getInstance().getUserResponse().getEpicEmail())
                    && RunTimeData.getInstance().getUserResponse().getEpicEmail().equalsIgnoreCase(requestParams)) {
                mismatchType = "kpEmail";
            } else {
                mismatchType = "newEmail";
            }
            final String finalMismatchType = mismatchType;
            emailRequestParams = getRequestJson(mismatchType, requestParams);

            if (Util.isNetworkAvailable(InterruptsActivity.this)) {
                new PutRequestAsyncTask().execute(finalMismatchType, requestParams);
            } else {
                showNetworkAlert();
            }


        } else if (AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS.equalsIgnoreCase(signonResponseInterruptType)
                || AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD.equalsIgnoreCase(signonResponseInterruptType)) {
            if (Util.isNetworkAvailable(InterruptsActivity.this)) {
                emailRequestParams = requestParams;
                new PutRequestAsyncTask().execute(null, requestParams);
            } else {
                showNetworkAlert();
            }
        }
    }

    private void showNetworkAlert() {
        mAlertDialog = new GenericAlertDialog(
                InterruptsActivity.this,
                "Network Error",
                getString(R.string.alert_network_error),
                getString(R.string.ok_text),
                (dialog, which) -> dialog.dismiss(),
                null,
                null);
        mAlertDialog.showDialog();
    }

    private void showLoadingScreen() {
        startActivityForResult(new Intent(this, TransparentLoadingActivity.class).putExtra("type", "simple"), 1);
    }

    private String getRequestJson(String mismatchType, String userInput) {
        JSONObject headerObject = new JSONObject();
        JSONArray paramArray = new JSONArray();
        JSONObject params = new JSONObject();
        try {
            params.put("fname", mismatchType);
            params.put("emailAddress", userInput);
            paramArray.put(params);
            headerObject.put("requestMetaData", paramArray);
        } catch (JSONException e) {
            LoggerUtils.exception(e.getMessage());
        }
        return headerObject.toString();
    }

    private class PutRequestAsyncTask extends AsyncTask<String, Void, TTGInteruptAPIResponse> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingScreen();
        }

        @Override
        protected TTGInteruptAPIResponse doInBackground(String... strings) {
            TTGInteruptAPIResponse interuptAPIResponse = null;
            //Check if the interrupt is soft interrupt or hard interrupt.
            //If soft interrupt call the method inside if condition. Otherwise else condition will be executed.
            if (null != mInterruptTypeForEmail && mInterruptTypeForEmail.equalsIgnoreCase(AppConstants.SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH)){
                interuptAPIResponse = TTGSignonController.getInstance().performSoftInterruptInterruptAPICallResponse(TTGMobileLibConstants.HTTP_METHOD_PUT,
                        RunTimeData.getInstance().getmTTtgInteruptAPIResponse(), InterruptsActivity.this, emailRequestParams);
            }else{
                interuptAPIResponse = TTGSignonController.getInstance().performInterruptAPICallResponse(TTGMobileLibConstants.HTTP_METHOD_PUT,
                        RunTimeData.getInstance().getmTTtgInteruptAPIResponse(), InterruptsActivity.this, emailRequestParams);
            }
            return interuptAPIResponse;
        }

        @Override
        protected void onPostExecute(final TTGInteruptAPIResponse interuptAPIResponse) {
            mInterruptResposne = interuptAPIResponse;
            finishActivity(1);
            if (interuptAPIResponse != null) {
                //keepAliveResponse.getStatusCode();
                if (interuptAPIResponse.getStatusCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                    parseInterruptFailureResp(interuptAPIResponse);
                } else {
                    TTGResponseHandler.getInstance().handleInterruptAPIResponse(
                            interuptAPIResponse, InterruptsActivity.this, TTGMobileLibConstants.HTTP_METHOD_PUT);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                handleBackNavigation();
                break;
        }
        return true;
    }

    private void handleBackNavigation() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            RunTimeData.getInstance().setInterruptScreenBackButtonClicked(true);
            RunTimeData.getInstance().setInturruptScreenVisible(false);
//            Util.performSignout(this, get_globalAppContext());
            Util.hideSoftKeyboard(this);
            finish();
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LoggerUtils.info("onActivityResult");

        if (requestCode == ON_INTERRUPT_RECIEVED_REQUEST_CODE) {
            if (null != mInterruptResposne) {
                RunTimeData.getInstance().setInterruptGetAPIResponse(mInterruptResposne);
            }
            if (null != mInterruptResposne && null != mInterruptResposne.getInterruptType() && mInterruptResposne.getStatusCode() != HttpURLConnection.HTTP_UNAVAILABLE && (TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH.equalsIgnoreCase(mInterruptResposne.getInterruptType()) ||
                    TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS.equalsIgnoreCase(mInterruptResposne.getInterruptType()) ||
                    TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD.equalsIgnoreCase(mInterruptResposne.getInterruptType()) ||
                    TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH.equalsIgnoreCase(mInterruptResposne.getInterruptType()))) {
                if (AppConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH.equalsIgnoreCase(mInterruptResposne.getInterruptType()) ||
                        AppConstants.SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH.equalsIgnoreCase(mInterruptResposne.getInterruptType())) {
                    mInterruptTypeForEmail = mInterruptResposne.getInterruptType();
                    installFragment(new EmailMismatchFragment());
                } else if (AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS.equalsIgnoreCase(mInterruptResposne.getInterruptType())) {
                    installFragment(new SecretQuestionsFragment());
                } else if (AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD.equalsIgnoreCase(mInterruptResposne.getInterruptType())) {
                    installFragment(new TemporaryPasswordFragment());
                } else {
                    RunTimeData.getInstance().setFromInterruptScreen(true);
                    finish();
                }
            } else {
                if (!isAlertDialogShown) {
                    RunTimeData.getInstance().setFromInterruptScreen(true);
                    this.finish();
                }
            }
        }
    }

    private void parseInterruptFailureResp(TTGInteruptAPIResponse interuptAPIResponse) {
        String data = interuptAPIResponse.getData();
        InterruptFailureResponseRoot parseInterruptResponse = null;
        if (data != null) {
            parseInterruptResponse = parseInterruptPUTFailureResponse(data);
            if (null != parseInterruptResponse) {
                boolean isErrorCodeMatching = false;
                if (null != parseInterruptResponse.getOutput() &&
                        null != parseInterruptResponse.getOutput().getErrors() &&
                        null != parseInterruptResponse.getOutput().getErrors().getError()) {
                    for (InterruptErrorCode errorCode : parseInterruptResponse.getOutput().getErrors().getError()) {
                        if (null != errorCode.getCode() && (errorCode.getCode().contains(getString(R.string.password_easytoguess)) ||
                                errorCode.getCode().contains(getString(R.string.password_lengthfailure)) ||
                                errorCode.getCode().contains(getString(R.string.password_contain_repeatedchars)) ||
                                errorCode.getCode().contains(getString(R.string.password_contain_sequence)) ||
                                errorCode.getCode().contains(getString(R.string.password_invalidchars)))) {
                            showErrorAlert(getString(R.string.password_reset_error_alert_message));
                            isErrorCodeMatching = true;
                            break;
                        } else if (null != errorCode.getCode() && (errorCode.getCode().contains(getString(R.string.email_invalid_chars)))) {
                            showErrorAlert(getResources().getString(R.string.enter_valid_email_text));
                            isErrorCodeMatching = true;
                            break;
                        }
                    }
                }
                if (!isErrorCodeMatching) {
                    showErrorAlert("Sorry couldn't process your request");
                }
            } else {
                showErrorAlert("Sorry couldn't process your request");
            }
        }

    }

    private InterruptFailureResponseRoot parseInterruptPUTFailureResponse(String data) {
        return FrontController.getInstance(InterruptsActivity.this).getJsonParserUtilityInstance(this).parseJson(data,
                InterruptFailureResponseRoot.class);
    }

    private void showErrorAlert(String message) {
        mAlertDialog = new GenericAlertDialog(InterruptsActivity.this, null, message, InterruptsActivity.this.getString(R.string.ok_text), (dialog, which) -> {
            dialog.dismiss();
            isAlertDialogShown = false;
        }, null, null);

        if (mAlertDialog != null && !mAlertDialog.isShowing()) {
            mAlertDialog.showDialog();
            isAlertDialogShown = true;
        }
    }
}

