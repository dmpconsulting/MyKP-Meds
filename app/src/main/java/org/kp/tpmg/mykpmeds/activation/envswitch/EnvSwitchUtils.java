package org.kp.tpmg.mykpmeds.activation.envswitch;

import android.content.Context;

import com.montunosoftware.mymeds.BuildConfig;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.util.Map;

public class EnvSwitchUtils {

    private static String currentEnvKey;

    private static final String SHARED_PREFS_CURRENT_ENV = "org.kp.tpmg.preventivecare.alpha.envswitch.SHARED_PREFS_CURRENT_ENV";

    public static String getCurrentEnvironmentFromSharedPrefs(Context context) {
        return SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME).getString(SHARED_PREFS_CURRENT_ENV, null);

    }

    public static void setCurrentEnvironmentToSharedPrefs(Context context, String envKey) {
        String currentEnv;
        if(!BuildConfig.ENVIRONMENT_MAP.isEmpty() && (BuildConfig.ENVIRONMENT_MAP.containsKey(envKey))) {
            currentEnv = envKey.toUpperCase();
            LoggerUtils.info("ENV SWITCH - Updating the current environment label in shared preferences - " + currentEnv);
            SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME).putString(SHARED_PREFS_CURRENT_ENV, currentEnv, false);
        }
    }

    public static void initCurrentSelectedEnvironmentEndpoint(Context context) {
        if(!BuildConfig.ENVIRONMENT_MAP.isEmpty()) {
            String currentEnvFromPrefs = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME).getString(SHARED_PREFS_CURRENT_ENV, null);
            LoggerUtils.info("ENV SWITCH - Initializing current selected environment. Pulling set option from shared preferences: " + currentEnvFromPrefs);
            if (!Util.isEmptyString(currentEnvFromPrefs)) {
                String newUrl = BuildConfig.ENVIRONMENT_MAP.get(currentEnvFromPrefs);
                LoggerUtils.info("ENV SWITCH - Found saved environment! Env: " + currentEnvFromPrefs + ", Endpoint: " + newUrl);
                if(!Util.isEmptyString(newUrl)) {
                    currentEnvKey = currentEnvFromPrefs;
                    AppConstants.baseURL = newUrl;
                    setCurrentEnvironmentToSharedPrefs(context, currentEnvKey);
                }
            } else {
                for(Map.Entry<String, String> envMapEntry : BuildConfig.ENVIRONMENT_MAP.entrySet()) {
                    if(AppConstants.baseURL.contentEquals(envMapEntry.getValue())) {
                        currentEnvKey = envMapEntry.getKey();
                        setCurrentEnvironmentToSharedPrefs(context, currentEnvKey);
                        break;
                    }
                }
            }
        }
    }

    public static String getCurrentEnvironmentName() {
        return currentEnvKey;
    }

    public static String getCurrentEnvironmentName(Context context) {
        if(null == currentEnvKey) initCurrentSelectedEnvironmentEndpoint(context);
        return currentEnvKey;
    }
}
