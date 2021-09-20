package com.montunosoftware.pillpopper.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;

import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.envswitch.EnvSwitchUtils;
import org.kp.tpmg.ttgmobilelib.utilities.TTGUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shiva NageshwarRao on 10-Jun-19.
 */

public class FireBaseAnalyticsTracker {

    private static final String AGE_65_PLUS = "65+";
    private static final String AGE_55_TO_64 = "55-64";
    private static final String AGE_45_TO_54 = "45-54";
    private static final String AGE_35_TO_44 = "35-44";
    private static final String AGE_25_TO_34 = "25-34";
    private static final String AGE_18_TO_24 = "18-24";
    private static final String AGE_13_TO_17 = "13-17";

    private static FireBaseAnalyticsTracker mFireBaseAnalyticsTracker;

    private FireBaseAnalyticsTracker()
    {

    }

    public static synchronized FireBaseAnalyticsTracker getInstance()
    {
        if (null == mFireBaseAnalyticsTracker) {
            mFireBaseAnalyticsTracker = new FireBaseAnalyticsTracker();
        }
        return mFireBaseAnalyticsTracker;
    }

    /**
     * Logs the event with given eventName and the bundle which contains the parameterName and parameterValue information
     * @param eventName
     * @param bundle
     */
    public void logEvent(Context context, String eventName, Bundle bundle){
        if (null != context) {
            FirebaseAnalytics fireBaseAnalytics = FirebaseAnalytics.getInstance(context);
            fireBaseAnalytics.logEvent(eventName, bundle);
            logUserProperties(context);
        }
    }


    /**
     * Logs the event with given eventName, parameterName and parameterValue.
     * @param eventName
     * @param paramName
     * @param paramValue
     */
    public void logEvent(Context context, String eventName, String paramName, String paramValue){
        if (null != context && !Util.isEmptyString(paramName) && !Util.isEmptyString(paramValue)) {
                Bundle bundle = new Bundle();
                bundle.putString(paramName, paramValue);
                logEvent(context, eventName, bundle);
        }
    }

    /**
     * Logs the screen Event with the given screen Name.
     * @param context
     * @param screenName
     */
    public void logScreenEvent(Context context, String screenName) {
        if (null != context && !Util.isEmptyString(screenName)) {
            Bundle bundle = new Bundle();
            bundle.putString(FireBaseConstants.ScreenEvent.SCREEN_NAME, screenName);
            logEvent(context, FireBaseConstants.ScreenEvent.OPEN_SCREEN, bundle);
        }
    }

    /**
     * Logs Only Event with empty bundle.
     * @param context
     * @param eventName
     */
    public void logEventWithoutParams(Context context, String eventName){
        if (null != context && !Util.isEmptyString(eventName)) {
            logEvent(context, eventName, new Bundle());
        }
    }

    /**
     * Sets user property with the given name and values
     */
    private void logUserProperties(Context context){
        if (null != context) {
            HashMap<String, String> userPropertiesMap = getUserProperties(context);
            if(!userPropertiesMap.isEmpty()){
                for (Map.Entry<String,String> entry : userPropertiesMap.entrySet()){
                    FirebaseAnalytics.getInstance(context).setUserProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Prepares the User Properties List data. It would not collect the key, if the value is empty or null.
     * @return UserProperties List data
     */
    private HashMap<String, String> getUserProperties(Context context){
        HashMap<String, String> userPropertiesMap = new HashMap<>();

        // This values will be taken from shared preferences/database based on the App logic.
        String userAge = getRangeAge(FrontController.getInstance(context).getUserAge());
        // Gender Logic needs to added in API front. Once API is working needs to be enabled this logic.
        String userGender = FrontController.getInstance(context).getUserGender();
        String userRegion = Util.getUserRegionValue(ActivationController.getInstance().fetchUserRegion(context));
        String userEnvironment = EnvSwitchUtils.getCurrentEnvironmentName();
        String env = TTGUtil.getEnvironment(context);

        if(!Util.isEmptyString(userAge)){
            userPropertiesMap.put(FireBaseConstants.UserProperties.USER_PROPS_AGE, userAge);
        }
        if(!Util.isEmptyString(userGender)){
            userPropertiesMap.put(FireBaseConstants.UserProperties.USER_PROPS_GENDER, userGender);
        }
        if(!Util.isEmptyString(userRegion)){
            userPropertiesMap.put(FireBaseConstants.UserProperties.USER_PROPS_REGION, userRegion);
        }

        if(!Util.isEmptyString(env)) {
            if (!Util.isEmptyString(userEnvironment)) { // Through environment switcher
                if (FireBaseConstants.ENVIRONMENT_BETA.equalsIgnoreCase(userEnvironment) || FireBaseConstants.ENVIRONMENT_PP.equalsIgnoreCase(userEnvironment)) {
                    userEnvironment = FireBaseConstants.ENVIRONMENT_TRACK_PP;
                } else if (FireBaseConstants.ENVIRONMENT_PR.equalsIgnoreCase(env)) {
                    userEnvironment = FireBaseConstants.ENVIRONMENT_PROD;
                }
            } else {// Through Specific Beta Or Production builds
                if (FireBaseConstants.ENVIRONMENT_PP.equalsIgnoreCase(env)) {
                    userEnvironment = FireBaseConstants.ENVIRONMENT_TRACK_PP;
                } else if (FireBaseConstants.ENVIRONMENT_PR.equalsIgnoreCase(env)) {
                    userEnvironment = FireBaseConstants.ENVIRONMENT_PROD;
                }
            }
        }
        userPropertiesMap.put(FireBaseConstants.UserProperties.USER_PROPS_ENVIRONMENT, userEnvironment);
        return userPropertiesMap;
    }


    /**
     * Gets the user Age range value.
     * @param ageString
     * @return
     */
    private String getRangeAge(String ageString){
        int age = 0;
        try{
            if(null!=ageString && ageString.contains(".")){  // Since the age value comes are double.
                age = (int) Double.parseDouble(ageString);
            }else{
                age = Integer.parseInt(ageString); // By Any Chance if it comes as Integer.
            }
        } catch (Exception e){
            PillpopperLog.exception(e.getMessage());
        }
        int age13 = 13;
        int age17 = 17;
        int age18 = 18;
        int age24 = 24;
        int age25 = 25;
        int age34 = 34;
        int age35 = 35;
        int age44 = 44;
        int age45 = 45;
        int age54 = 54;
        int age55 = 55;
        int age64 = 64;
        int age65 = 65;
        if(between(age, age13, age17)){
            return AGE_13_TO_17;
        }else if(between(age, age18, age24)){
            return AGE_18_TO_24;
        }else if(between(age, age25, age34)){
            return AGE_25_TO_34;
        }else if(between(age, age35, age44)){
            return AGE_35_TO_44;
        }else if(between(age, age45, age54)){
            return AGE_45_TO_54;
        }else if(between(age, age55, age64)){
            return AGE_55_TO_64;
        }else if(age>=age65){
            return AGE_65_PLUS;
        }
        return null;
    }

    /**
     *
     * @param age
     * @param minRange
     * @param maxRange
     * @return
     */
    private boolean between(int age, int minRange, int maxRange) {
        return (age>= minRange && age<= maxRange);
    }

}
