package com.montunosoftware.pillpopper.android.firebaseMessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.RunTimeConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

public class FCMNotificationReceiverActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppConstants.isSecureFlg()) {
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
        showAlert(getIntent());
    }

    private void showAlert(Intent intent) {
        if (null != intent && null != intent.getExtras()) {
            String title = intent.getStringExtra("title");
            String body = intent.getStringExtra("body");
            if (!Util.isEmptyString(title) && !Util.isEmptyString(body)) {
                LoggerUtils.info("FCM -- registerPushNotificationReceiver show dialog");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final View customLayout = getLayoutInflater().inflate(R.layout.inapp_fcm_alert_layout, null);
                builder.setView(customLayout);
                TextView alertTitle = customLayout.findViewById(R.id.alert_title);
                alertTitle.setText(title);
                TextView alertMessage = customLayout.findViewById(R.id.alert_message);
                alertMessage.setText(body);
                TextView okBtn = customLayout.findViewById(R.id.ok_btn);
                okBtn.setOnClickListener(view -> {
                    /*if (RunTimeData.getInstance().isFingerPrintOptInProgress()
                            && null != RunTimeData.getInstance().getFingerprintOptInDialog()) {
                        RunTimeData.getInstance().getFingerprintOptInDialog().onStart();
                    }*/
                    finish();
                });
                builder.setCancelable(false);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                LoggerUtils.info("FCM -- Push notification missing required information");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        RunTimeConstants.getInstance().setNotificationSuppressor(true);
        RunTimeData.getInstance().setAppVisibleFlg(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        RunTimeConstants.getInstance().setNotificationSuppressor(false);
        RunTimeData.getInstance().setAppVisibleFlg(true);
        LoggerUtils.info("Starting the Timer ");
        ActivationController.getInstance().startTimer(this);
        // launch the login screen if needed, or timed out
        PillpopperAppContext.getGlobalAppContext(this).kpMaybeLaunchLoginScreen(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showAlert(intent);
    }
}