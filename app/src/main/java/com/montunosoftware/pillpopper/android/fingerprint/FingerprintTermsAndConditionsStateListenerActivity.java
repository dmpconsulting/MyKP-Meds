package com.montunosoftware.pillpopper.android.fingerprint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.ReminderListenerInterfaces;
import com.montunosoftware.pillpopper.android.StateListenerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

/**
 * Created by adhithyaravipati on 4/17/17.
 */

public class FingerprintTermsAndConditionsStateListenerActivity extends StateListenerActivity {
    private boolean mTermsDecisionTaken;

    private Button mAcceptButton;
    private Button mCancelButton;

    private ReminderListenerInterfaces mReminderShowListener;

    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mReminderShowListener.showReminder(true);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_terms_and_conditions_dialog);
        if(RunTimeData.getInstance().isBiomerticFinished()){
            finish();
        }
        mTermsDecisionTaken = false;
        mReminderShowListener = this;
        initUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        mReminderShowListener.showReminder(true);

        IntentFilter mGetStateReceiverIntentFilter = new IntentFilter();
        mGetStateReceiverIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        registerReceiver(mGetStateBroadcastReceiver,mGetStateReceiverIntentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            unregisterReceiver(mGetStateBroadcastReceiver);
        } catch (Exception e) {
            PillpopperLog.exception("ScheduleFragment.java: Unable to unregister receiver. " + e.getMessage()
            );
        }
        RunTimeData.getInstance().setmFingerPrintTCInProgress(false);
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        //To prevent the back button from closing the fingerprint terms and conditions dialog.
    }

    private void initUi() {
        mAcceptButton = findViewById(R.id.t_and_c_accept_button);
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
        mCancelButton = findViewById(R.id.t_and_c_cancel_button);
        mCancelButton.setOnClickListener(v -> {
            RunTimeData.getInstance().setBiomerticChecked(false);
            RunTimeData.getInstance().setBiomerticFinished(true);
            FingerprintUtils.setFingerprintSignInForUser(FingerprintTermsAndConditionsStateListenerActivity.this, false);
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

}
