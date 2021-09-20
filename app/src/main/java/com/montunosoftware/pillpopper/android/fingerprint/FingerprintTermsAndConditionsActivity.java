package com.montunosoftware.pillpopper.android.fingerprint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.KpBaseActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

/**
 * Created by adhithyaravipati on 4/17/17.
 */

public class FingerprintTermsAndConditionsActivity extends KpBaseActivity {
    private boolean mTermsDecisionTaken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_terms_and_conditions_dialog);
        if(RunTimeData.getInstance().isBiomerticFinished()){
            finish();
        }
        mTermsDecisionTaken = false;
        initUi();
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(FingerprintTermsAndConditionsActivity.this, FireBaseConstants.ScreenEvent.SCREEN_TOUCH_TERMS_CONDITIONS);
    }

    @Override
    public void onBackPressed() {
        //To prevent the back button from closing the fingerprint terms and conditions dialog.
    }

    private void initUi() {
        Button mAcceptButton = findViewById(R.id.t_and_c_accept_button);
        mAcceptButton.setOnClickListener(v -> {
            RunTimeData.getInstance().setBiomerticChecked(false);
            RunTimeData.getInstance().setBiomerticFinished(true);
            if(FingerprintUtils.isDeviceEligibleForFingerprintOptIn(getBaseContext())) {
                mTermsDecisionTaken = true;
                sendResult(true);
            } else  {
                sendResultCanceledToCallingActivity();
            }
        });
        Button mCancelButton = findViewById(R.id.t_and_c_cancel_button);
        mCancelButton.setOnClickListener(v -> {
            RunTimeData.getInstance().setBiomerticChecked(false);
            RunTimeData.getInstance().setBiomerticFinished(true);
            FingerprintUtils.setFingerprintSignInForUser(FingerprintTermsAndConditionsActivity.this, false);
            sendResultCanceledToCallingActivity();
        });
    }

    private void sendResult(boolean isTermsAccepted) {
        if(mTermsDecisionTaken) {
            sendResultOkToCallingActivity(isTermsAccepted);
        } else {
            sendResultCanceledToCallingActivity();
        }
    }

    private void sendResultOkToCallingActivity(boolean isTermsAccepted) {
        Intent resultData = new Intent();
        resultData.putExtra(AppConstants.INTENT_RESULT_IS_FINGERPRINT_TERMS_DECISION_MADE, mTermsDecisionTaken);
        resultData.putExtra(AppConstants.INTENT_RESULT_IS_FINGERPRINT_TERMS_ACCEPTED, isTermsAccepted);
        setResult(Activity.RESULT_OK, resultData);
        if(getParent() != null) {
            setResult(Activity.RESULT_OK, resultData);
        }
        finish();
    }

    private void sendResultCanceledToCallingActivity() {
        setResult(Activity.RESULT_CANCELED);
        if(getParent() != null) {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        RunTimeData.getInstance().setmFingerPrintTCInProgress(false);
        if(AppConstants.isByPassLogin()) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}
