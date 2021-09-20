package org.kp.tpmg.mykpmeds.activation.util;

import android.content.Context;

import org.kp.tpmg.mykpmeds.activation.AppConstants;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SharedPreferenceManager {

	private SecurePreferencesNew securePreferencesNew;
	private static SharedPreferenceManager preferenceManager;

	private SharedPreferenceManager(Context context, String fileType) {

		securePreferencesNew = new SecurePreferencesNew(context, AppConstants.AUTH_CODE_PREF_NAME, ActivationUtil.getSecretKey(context), true);
	}

	public static SharedPreferenceManager getInstance(Context context,
			String fileType) {
		if (fileType == null) {
			return null;
		}

		if (preferenceManager == null
				|| !fileType
						.equalsIgnoreCase(preferenceManager.securePreferencesNew
								.getPreferenceName())) {
			preferenceManager = new SharedPreferenceManager(context, fileType);
		}
		
		return preferenceManager;
	}

	public void putString(String key, String value, boolean isClearable) {
		if (isClearable) {
			registerForUnfixedPreferences(key);
		}
		securePreferencesNew.putString(key, value);
	}

	public void putStrings(Map<String, String> values, boolean isClearable) {
		for (Map.Entry<String, String> mapValue : values.entrySet()) {
			if (isClearable) {
				registerForUnfixedPreferences(mapValue.getKey());
			}
			securePreferencesNew.putString(mapValue.getKey(), mapValue.getValue());
		}

	}

	public String getString(String key, String defaultValue) {
		return securePreferencesNew.getString(key, defaultValue);
	}

	public void clearPreferences() {
		securePreferencesNew.clear();
	}

	public void remove(String key) {
		securePreferencesNew.removeValue(key);
	}

	private void registerForUnfixedPreferences(String key) {

		Set<String> userPrefSet = getStringSet("fixedPreferences",
                new LinkedHashSet<>());
		userPrefSet.add(key);
		putStringSet("fixedPreferences", userPrefSet);

	}

	public void removePrefs() {
		Set<String> userPrefSet = getStringSet("fixedPreferences", null);
		if (userPrefSet != null) {
			for (String key : userPrefSet) {
				remove(key);
			}
		}
	}

	public void resetPrefs() {
		securePreferencesNew.clear();
	}

	public long getLong(String key, long defaultVal) {
		try {
			return Long.parseLong(securePreferencesNew.getString(key,
					Long.toString(defaultVal)));
		} catch (Exception ex){
			return defaultVal;
		}
	}

	public void putLong(String key, long value, boolean isClearable) {
		if (isClearable) {
			registerForUnfixedPreferences(key);
		}
		securePreferencesNew.putString(key, Long.toString(value));
	}

	public void putBoolean(String key, Boolean bool, boolean isClearable) {
		if (isClearable) {
			registerForUnfixedPreferences(key);
		}
		securePreferencesNew.putString(key, String.valueOf(bool));
	}

	public boolean getBoolean(String key, boolean defaultVal) {
		return Boolean.parseBoolean(securePreferencesNew.getString(key, Boolean
				.toString(defaultVal)));
	}

	public Set<String> getStringSet(String key, Set<String> defaultValue) {
		return securePreferencesNew.getStringSet(key, defaultValue);
	}

	public void putStringSet(String key, Set<String> values) {
		securePreferencesNew.putStringSet(key, values);
	}

	public String getDecryptedString(String value) {
		return securePreferencesNew.getDecryptedString(value);
	}

	public Map<String, ?> getAllKeys(){
		return securePreferencesNew.getAllKeys();
	}
}
