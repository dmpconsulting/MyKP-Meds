package com.montunosoftware.pillpopper.android.fingerprint;

import android.content.Context;
import android.content.DialogInterface;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.kpsecurity.KPSecurity;
import org.kp.kpsecurity.security.KeyStoreManager;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.util.List;

/**
 * Created by adhithyaravipati on 4/17/17.
 */

public final class FingerprintUtils {

    public static final int MAX_LOCAL_INCORRECT_ATTEMPTS = 3;

    private static SharedPreferenceManager mSharedPreferenceManager;

    public static boolean isDeviceEligibleForFingerprintOptIn(Context context) {
        mSharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

        boolean rememberUserId = mSharedPreferenceManager.getBoolean(AppConstants.REMEMBER_USER_ID, false);

        return KPSecurity.isBiometricPromptReadyToUse(context)
               /* && KPSecurity.isFingerprintEnrolled(context)
                && KPSecurity.isFingerprintAvailable(context)*/
                && rememberUserId;
                /*&& isCredentialsSaved(context);*/
    }

    public static boolean isDeviceEligibleForFingerprintSignIn(Context context) {
        mSharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

        boolean rememberUserId = mSharedPreferenceManager.getBoolean(AppConstants.REMEMBER_USER_ID, false);
        boolean isFingerprintDecisionTaken = mSharedPreferenceManager.getBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_DECISION_TAKEN, false);
        boolean isFingerprintOptedIn = mSharedPreferenceManager.getBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN, false);

        return KPSecurity.isBiometricPromptReadyToUse(context)
                && rememberUserId
                && isFingerprintDecisionTaken
                && isFingerprintOptedIn;
                /*&& isCredentialsSaved(context);*/
    }

    public static boolean encryptAndStorePassword(Context context, String password) {
        mSharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

        boolean rememberUserId = mSharedPreferenceManager.getBoolean(AppConstants.REMEMBER_USER_ID, false);
        String savedUsername = mSharedPreferenceManager.getString(AppConstants.USER_NAME, null);

        if(!rememberUserId
                || Util.isEmptyString(savedUsername)) {
            return false;
        }

        List keystoreList = KeyStoreManager.listKeys();

        if(keystoreList == null
                || keystoreList.isEmpty()) {
            return KeyStoreManager.initKeyStore(savedUsername, password, context);
        }

        return false;
    }

    public static String getDecryptedPassword(Context context, String alias) {
        mSharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
        String decryptedPassword = null;

        if(alias != null) {
            decryptedPassword = KPSecurity.decryptCredentials(alias, context);
        }


        if (null != decryptedPassword && decryptedPassword.length() > 15) {
            decryptedPassword = null;
        }

        return decryptedPassword;

    }

    public static boolean isCredentialsSaved(Context context) {
        mSharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

        String savedUsername = mSharedPreferenceManager.getString(AppConstants.USER_NAME, null);
        String decryptedPassword = null;

        List keystoreList = KeyStoreManager.listKeys();

        if(!Util.isEmptyString(savedUsername)
                && (keystoreList != null && !keystoreList.isEmpty())) {
            decryptedPassword = getDecryptedPassword(context, savedUsername);
        }

        return !Util.isEmptyString(savedUsername)
                && !Util.isEmptyString(decryptedPassword);


    }

    public static void setFingerprintSignInForUser(Context context, boolean fingerprintEnabled) {
        PillpopperLog.say("Fingerprintutils -- Enabling fingerprint sign in for user: " + fingerprintEnabled);
        mSharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
        mSharedPreferenceManager.putBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_DECISION_TAKEN, true, true);
        mSharedPreferenceManager.putBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN, fingerprintEnabled, true);
        if(fingerprintEnabled){
            FireBaseAnalyticsTracker.getInstance().logEvent(context,
                    FireBaseConstants.Event.ENABLED_BIOMETRICS,
                    FireBaseConstants.ParamName.AUTH_TYPE,
                    FireBaseConstants.ParamValue.TOUCH_ID);
        }
    }

    public static void disableFingerprintFromSettings(Context context) {
        PillpopperLog.say("FingerprintUtils -- Disabling fingerprint sign in for user");
        mSharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
        mSharedPreferenceManager.putBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN, false, true);
    }

    public static void resetAndPurgeKeyStore(Context context) {
        PillpopperLog.say("FingerprintUtils -- Resetting fingerprint preferences and purging keystore.");
        KPSecurity.purgeKeyStore();
        mSharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
        mSharedPreferenceManager.remove(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_DECISION_TAKEN);
        mSharedPreferenceManager.remove(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN);

    }

    public static void showLocalThresholdReachedMessage(Context context, DialogInterface.OnClickListener okButtonListener) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.MyAlertDialog);
        builder.setTitle(context.getResources().getString(R.string.dialog_fingerprint_local_threshold_title));
        builder.setMessage(context.getResources().getString(R.string.dialog_fingerprint_local_threshold_message));
        builder.setPositiveButton(context.getResources().getString(R.string._ok), okButtonListener);
        builder.setCancelable(false);

        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        RunTimeData.getInstance().setAlertDialogInstance(alertDialog);
        alertDialog.show();
    }

    public static void showGlobalThresholdReachedMessage(Context context, DialogInterface.OnClickListener okButtonListener) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.MyAlertDialog);
        builder.setTitle(context.getResources().getString(R.string.dialog_fingerprint_global_threshold_title));
        builder.setMessage(context.getResources().getString(R.string.dialog_fingerprint_global_threshold_message));
        builder.setPositiveButton(context.getResources().getString(R.string._ok), okButtonListener);
        builder.setCancelable(false);

        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        RunTimeData.getInstance().setAlertDialogInstance(alertDialog);
        alertDialog.show();
    }

}

