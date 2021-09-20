package com.montunosoftware.pillpopper.android.fingerprint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

public class FingerprintOptInStateListenerContainerActivity extends StateListenerActivity {

    private boolean mOptInFlowCompleted;

    private int INTENT_REQUEST_CODE_FINGERPRINT_TERMS_AND_CONDITIONS = 1;

    private ReminderListenerInterfaces mReminderShowListener;

    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mReminderShowListener.showReminder(true);

        }
    };

    public static void startOptInFlow(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, FingerprintOptInStateListenerContainerActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startOptInFlow(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), FingerprintOptInStateListenerContainerActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_opt_in_container);
        mOptInFlowCompleted = false;
        mReminderShowListener = this;
        startFingerprintOptInFlow();

    }

    @Override
    protected void onStart() {
        super.onStart();

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!mOptInFlowCompleted) {
            setResult(Activity.RESULT_CANCELED);
            if (getParent() != null) {
                setResult(Activity.RESULT_CANCELED);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_CODE_FINGERPRINT_TERMS_AND_CONDITIONS) {
            if (resultCode == Activity.RESULT_OK) {
                boolean isTermsDecisionMade = data.getBooleanExtra(AppConstants.INTENT_RESULT_IS_FINGERPRINT_TERMS_DECISION_MADE, false);
                boolean isTermsAccepted = data.getBooleanExtra(AppConstants.INTENT_RESULT_IS_FINGERPRINT_TERMS_ACCEPTED, false);

                if (isTermsDecisionMade) {
                    handleTermsAndConditionsResult(isTermsAccepted);
                } else {
                    sendResultCanceledToCallingActivity();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                sendResultCanceledToCallingActivity();
            }
        }

    }

    private void startFingerprintOptInFlow() {
        if(RunTimeData.getInstance().ismFingerPrintTCInProgress()) {
            Intent fingerprintTermsIntent = new Intent(this, FingerprintTermsAndConditionsStateListenerActivity.class);
            startActivityForResult(fingerprintTermsIntent, INTENT_REQUEST_CODE_FINGERPRINT_TERMS_AND_CONDITIONS);
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void handleTermsAndConditionsResult(boolean isTermsAccepted) {
        mOptInFlowCompleted = true;
        sendResultOkToCallingActivity(isTermsAccepted);
    }

    private void sendResultOkToCallingActivity(boolean isFingerprintOptedIn) {
        if(mOptInFlowCompleted) {
            Intent resultData = new Intent();
            resultData.putExtra(AppConstants.INTENT_RESULT_IS_FINGERPRINT_OPT_IN_SETUP_COMPLETE, mOptInFlowCompleted);
            resultData.putExtra(AppConstants.INTENT_RESULT_IS_FINGERPRINT_OPTED_IN, isFingerprintOptedIn);
            setResult(Activity.RESULT_OK, resultData);
            if (getParent() != null) {
                getParent().setResult(Activity.RESULT_OK, resultData);
            }
            finish();
        } else {
            sendResultCanceledToCallingActivity();
        }
    }

    private void sendResultCanceledToCallingActivity() {
        setResult(Activity.RESULT_CANCELED);
        if(getParent() != null) {
            getParent().setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }
}
