package org.kp.tpmg.mykpmeds.activation.util;

import android.content.Context;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SharedPreferenceManagerOld {

    private SecurePreferences securePreferences;
    private static SharedPreferenceManagerOld preferenceManager;

    private SharedPreferenceManagerOld(Context context, String fileType) {

        securePreferences = new SecurePreferences(context, fileType,
                ActivationUtil.getSecretKey(context), true);
    }

    public static SharedPreferenceManagerOld getInstance(Context context,
                                                         String fileType) {
        if (fileType == null) {
            return null;
        }

        if (preferenceManager == null
                || !fileType
                .equalsIgnoreCase(preferenceManager.securePreferences
                        .getPreferenceName())) {
            preferenceManager = new SharedPreferenceManagerOld(context, fileType);
        }

        return preferenceManager;
    }

    public void putString(String key, String value, boolean isClearable) {
        if (isClearable) {
            registerForUnfixedPreferences(key);
        }
        securePreferences.putString(key, value);
    }

    public void putStrings(Map<String, String> values, boolean isClearable) {
        for (Map.Entry<String, String> mapValue : values.entrySet()) {
            if (isClearable) {
                registerForUnfixedPreferences(mapValue.getKey());
            }
            securePreferences.putString(mapValue.getKey(), mapValue.getValue());
        }

    }

    public String getString(String key, String defaultValue) {
        return securePreferences.getString(key, defaultValue);
    }

    public void clearPreferences() {
        securePreferences.clear();
    }

    public void remove(String key) {
        securePreferences.removeValue(key);
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
        securePreferences.clear();
    }

    public long getLong(String key, long defaultVal) {
        try {
            return Long.parseLong(securePreferences.getString(key,
                    Long.toString(defaultVal)));
        } catch (Exception ex){
            return defaultVal;
        }
    }

    public void putLong(String key, long value, boolean isClearable) {
        if (isClearable) {
            registerForUnfixedPreferences(key);
        }
        securePreferences.putString(key, Long.toString(value));
    }

    public void putBoolean(String key, Boolean bool, boolean isClearable) {
        if (isClearable) {
            registerForUnfixedPreferences(key);
        }
        securePreferences.putString(key, String.valueOf(bool));
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        return Boolean.parseBoolean(securePreferences.getString(key, Boolean
                .toString(defaultVal)));
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return securePreferences.getStringSet(key, defaultValue);
    }

    public void putStringSet(String key, Set<String> values) {
        securePreferences.putStringSet(key, values);
    }

    public String getDecryptedString(String value) {
        return securePreferences.getDecryptedString(value);
    }

    public Map<String, ?> getAllKeys(){
        return securePreferences.getAllKeys();
    }
}
