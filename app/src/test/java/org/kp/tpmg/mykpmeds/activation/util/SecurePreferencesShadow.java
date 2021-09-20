package org.kp.tpmg.mykpmeds.activation.util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferences;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Implements(SecurePreferencesNew.class)
public class SecurePreferencesShadow {
    private Map<String,String> shadowMap = new HashMap<>();
    private Map<String,Set<String>> stringSet = new HashMap<>();

    public SecurePreferencesShadow(){
        shadowMap.put("timeOut",String.valueOf(false));
        shadowMap.put(AppConstants.SSO_SESSION_ID,"unknown");
        stringSet.put("fixedPreferences",new HashSet<String>());
        if(!TestUtil.isDeviceSwitch()) {
            shadowMap.put(AppConstants.ISNEW_USER, String.valueOf(true));
            shadowMap.put(AppConstants.KP_GUID, null);
        }
    }

    @Implementation
    public synchronized String getString(String key,String defaultVal){
        if(shadowMap.containsKey(key)){
            return shadowMap.get(key);
        }
        return defaultVal;
    }

    @Implementation
    public void putString(String key, String defaultVal, boolean isClearable) {
        shadowMap.put(key,defaultVal);
    }

    @Implementation
    public void removeValue(String key) {
        shadowMap.remove(key);
    }


    @Implementation
    public  void putStringSet(String key, Set<String> values) {
        stringSet.put(key,values);
    }

    @Implementation
    public  Set<String> getStringSet(String key, Set<String> defaultValue) {
        return stringSet.get(key);
    }

    @Implementation
    public void putValue(String key, String value){
        shadowMap.put(key,value);

    }
}
