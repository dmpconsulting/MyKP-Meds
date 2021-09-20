package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.SupportDatabaseHelper;
import com.montunosoftware.pillpopper.service.GetAppProfileUrlsService;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManagerOld;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class AppUpdateListener extends BroadcastReceiver
{
	private static final String APP_PROFILE_CONFIG_FILE_PATH = "configList/config.properties";

	private SharedPreferenceManagerOld mSharedPrefManager;
	private SharedPreferenceManager mSharedPreferenceManger;
	@Override
	public void onReceive(Context context, Intent intent) {
		mSharedPrefManager = SharedPreferenceManagerOld.getInstance(context, AppConstants.AUTH_CODE_PREF_OLD);
		mSharedPreferenceManger = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

		if (null != intent && Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {

			try {
				RunTimeData.getInstance().setmContext(context);
				migrateToNewSharedPreference();

				/*this flag FORCE_SIGN_IN_SHARED_PREF_KEY is introduced in 5.3.
				* if the app is being upgraded from 5.2 or below to current version, the below key will not be there in the preferences.
				* in that case, the app must show the Force sign in alert screen.
				* otherwise, it will set the value decided for the particular release taken from AppConstants IS_FORCE_SIGN_IN_REQUIRED*/
				if (null != mSharedPreferenceManger.getAllKeys()
						&& checkForIsForceSignInRequiredKey(mSharedPreferenceManger)) {
					LoggerUtils.info("IS_FORCE_SIGN_IN_REQUIRED  - " + AppConstants.IS_FORCE_SIGN_IN_REQUIRED);
					mSharedPreferenceManger.putBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, AppConstants.IS_FORCE_SIGN_IN_REQUIRED, false);
				} else {
					// means the oldversion is 5.2 or lower. a force sign in is required
					mSharedPreferenceManger.putBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, true, false);
				}

				SupportDatabaseHelper openHelper = new SupportDatabaseHelper();
				if (null != intent.getData().getSchemeSpecificPart() &&
						intent.getData().getSchemeSpecificPart().equalsIgnoreCase(context.getPackageName())) {
					PillpopperLog.say("Application version upgraded");
					if (ActivationUtil.isNetworkAvailable(context)) {
						PillpopperLog.say("Application version upgraded Calling GetAppProfile API");
						new GetAppProfileUrlsService(context).execute();
					}

					mSharedPreferenceManger.putBoolean("updateFlag", true, false);

					String versionUpgradedStr = mSharedPreferenceManger.getString("versionUpgraded", "-1");

					String quickviewEnabledForDBCheck = FrontController.getInstance(context).isQuickViewEnabledForDBCheck();

					PillpopperLog.say("Quickview Enabled CHeck : " + quickviewEnabledForDBCheck);
					mSharedPreferenceManger.putString("quickviewOptedInDBFlag", quickviewEnabledForDBCheck, false);

					// if versionUpgraded flag is -1, it means that the App is being upgraded to 3.0 from 2.x versions
					// in that case, we save the current running version to "versionUpgraded".
					// This value could be used to distinguish versions past 3.0
					// and also allows to keep the specific business logic for version 3.0 or higher
					// also set a flag "forceSignInRequired", to be used to force sign in the user.

					if (versionUpgradedStr.equals("-1")) {
						mSharedPreferenceManger.putString("versionUpgraded", "1", false);
						mSharedPreferenceManger.putBoolean("forceSignInRequired", true, false);
					}

					PillpopperLog.say("Application Got Upgraded After upgrade SharedPref value is : " + mSharedPreferenceManger.getString("versionUpgraded", "-1")
							+ " And forceSignInRequired flag is " + mSharedPreferenceManger.getBoolean("forceSignInRequired", false));

					//deleting the config properties file from internal storage,
					//Since new keys are added in 3.0, the existing file should be deleted
					File file = new File(context.getFilesDir() + File.separator
							+ APP_PROFILE_CONFIG_FILE_PATH);
					if (file.exists()) {
						boolean deleted = file.delete();
						LoggerUtils.info("Config Properties file deleted : " + deleted + " " + context.getFilesDir() + File.separator
								+ APP_PROFILE_CONFIG_FILE_PATH);
					} else {
						LoggerUtils.info("Config Properties file could not be deleted : " + context.getFilesDir() + File.separator
								+ APP_PROFILE_CONFIG_FILE_PATH);
					}

					//As we are using sign out url from config of internal storage. After sign out we will delete file.
					performSignoffAfterUpgrade(context);
				}
			} catch (Exception e) {
				PillpopperLog.say(e.getMessage());
			}
		}
	}

	/**
	 * returns true if the key is found in the shared pref
	 * @param mSharedPreferenceManger
	 * @return
	 */
	private boolean checkForIsForceSignInRequiredKey(SharedPreferenceManager mSharedPreferenceManger) {
		for (String key : mSharedPreferenceManger.getAllKeys().keySet()) {
			if (mSharedPreferenceManger.getDecryptedString(key).equalsIgnoreCase(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY)) {
				LoggerUtils.info("UPGRADE - FORCE_SIGN_IN_SHARED_PREF_KEY Found");
				return true;
			}
		}
		return false;
	}

	private void migrateToNewSharedPreference() {
		Map<String, ?> keySet = mSharedPrefManager.getAllKeys();
		PillpopperLog.say("--SharedPreference-- migration " + keySet.size());
		if (!keySet.isEmpty() && keySet.size() > 0) {
			for (Map.Entry<String, ?> entry : keySet.entrySet()) {
				if(null != entry  && null != entry.getValue()) {
					if (entry.getValue() instanceof Set) {
						String decryptedKey = mSharedPrefManager.getDecryptedString(entry.getKey());
						Set<String> setValue = mSharedPrefManager.getStringSet(decryptedKey, null);
						for (String value : setValue) {
							PillpopperLog.say("--SharedPreference-- key-- ", decryptedKey + ": value " + value);
						}
						mSharedPreferenceManger.putStringSet(decryptedKey, setValue);
					} else {
						String decryptedKey = mSharedPrefManager.getDecryptedString(entry.getKey());
						String value = mSharedPrefManager.getString(decryptedKey, null);
						PillpopperLog.say("--SharedPreference-- key " + decryptedKey + " value " + value);
						mSharedPreferenceManger.putString(decryptedKey, value, false);
						PillpopperLog.say("--SharedPreference-- retreival key--" + decryptedKey + " value " + mSharedPreferenceManger.getString(decryptedKey, null));
					}
				}
			}
		}
		mSharedPrefManager.clearPreferences();
	}

	public void performSignoffAfterUpgrade(Context context){
		ActivationController activationController =  ActivationController.getInstance();
		activationController.clearUserLoginFlg();
		activationController.resetQuickviewShownFlg(context);
		activationController.stopTimer(context);
		activationController.resetCookiesInfo();
		mSharedPrefManager.remove(AppConstants.SSO_SESSION_ID);
	}
}
