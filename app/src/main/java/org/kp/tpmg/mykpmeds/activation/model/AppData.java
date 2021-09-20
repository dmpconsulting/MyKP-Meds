package org.kp.tpmg.mykpmeds.activation.model;

import android.content.Context;

import com.montunosoftware.pillpopper.android.util.RefreshTokenTimerDelegate;

import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.TimerDelegate;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.GenericHttpClient;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttgmobilelib.controller.TTGSignonController;
import org.kp.tpmg.ttgmobilelib.model.CertificateValidaterObj;
import org.kp.tpmg.ttgmobilelib.utilities.TTGLoggerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppData {

	private static AppData appData;

	public static AppData getInstance() {
		if (appData==null)
			appData=new AppData();
		return appData;
	}

	public void checkForNewUser(Context context,String userid) {
		String userId = getSharedPreferenceManager(context).getString(AppConstants.KP_GUID, null);
		if(null!=userId && !userId.equalsIgnoreCase(userid)){
			getSharedPreferenceManager(context).putBoolean(AppConstants.ISNEW_USER, true, false);
		}else{
			getSharedPreferenceManager(context).putBoolean(AppConstants.ISNEW_USER, false, false);
		}
	}

	private SharedPreferenceManager getSharedPreferenceManager(Context context) {
		return SharedPreferenceManager.getInstance(context,
                    AppConstants.AUTH_CODE_PREF_NAME);
	}

	public boolean isNewUser(Context context) {

		return getSharedPreferenceManager(context).getBoolean(AppConstants.ISNEW_USER, false);
	}

	public boolean isDeviceSwitched(Context context) {
		return getSharedPreferenceManager(context).getBoolean(AppConstants.DEVICE_SWITCH_FLAG, false);
	}

	/**
	 *
	 * @param url URL
	 * @param requestType Request type
	 * @param params Parameters
	 * @param headers Headers
	 * @param requestObj Request
     * @param cntext Application context
     * @return String
     */
	public String getHttpResponse(String url, String requestType,
			Map<String, String> params, Map<String, String> headers,JSONObject requestObj, Context cntext) {
		GenericHttpResponse genericHttpResponseDataObject = null;
		TTGLoggerUtil.info("TAG going to check the network status  and context: " + cntext);
		TTGLoggerUtil.info("TAG going to check the network status : " + ActivationUtil.isNetworkAvailable(cntext));
		if (ActivationUtil.isNetworkAvailable(cntext)) {
			//try {
				//HttpUriRequest httpRequest = GenericHttpClient.getInstance().createHttpRequest(url, requestType, params, headers);
				genericHttpResponseDataObject = GenericHttpClient.getInstance().executeUrlRequest(url,requestType,params,headers, requestObj,null);
		//	} catch (UnsupportedEncodingException e) {
		//		 LoggerUtils.exception(e.getMessage());
		//	}
			if (genericHttpResponseDataObject != null) {
				return genericHttpResponseDataObject.getData();
			}
		}
		return null;
	
	
	}
	
	public void storeSessionResponse(Context context,SignonResult signOnObj, String userName) {
		LoggerUtils.info(signOnObj.getMrn() + ":"
				+ signOnObj.getPrimaryUserId() + ":" + signOnObj.getIntroCompleteFl()
				+ ":" + signOnObj.getSwitchDeviceFlag());

		storeSessionValues(context,signOnObj.getMrn(), userName, signOnObj.getPrimaryUserId(),
				signOnObj.getIntroCompleteFl(), signOnObj.getSwitchDeviceFlag(),signOnObj.getKpGUID(), signOnObj.getSetUpCompleteFl());
	}

	public void storeSessionValues(Context context,String... sessionValues) {
		Map<String, String> sessionValuesMap = new HashMap<>();
		sessionValuesMap.put(AppConstants.MRN, sessionValues[0]);
		sessionValuesMap.put(AppConstants.USER_NAME, sessionValues[1]);
		sessionValuesMap.put(AppConstants.USERID, sessionValues[2]);
		sessionValuesMap.put(AppConstants.INTRO_COMPLETE_FL, sessionValues[3]);
		sessionValuesMap.put(AppConstants.DEVICE_SWITCH_FLAG, sessionValues[4]);
		sessionValuesMap.put(AppConstants.KP_GUID, sessionValues[5]);
		sessionValuesMap.put(AppConstants.SETUP_COMPLETE_FL,sessionValues[6]);
		getSharedPreferenceManager(context).putStrings(sessionValuesMap, true);

	}

	public String getUserName(Context context) {
		return getSharedPreferenceManager(context).getString(AppConstants.USER_NAME, null);
	}

	public String getMRN(Context context) {
		return getSharedPreferenceManager(context).getString(AppConstants.MRN, null);
	}

	public String getUserId(Context context) {
		return getSharedPreferenceManager(context).getString(AppConstants.USERID, null);
	}
	
	public void setSSOSessionId(Context context,String ssosessionid) {
		getSharedPreferenceManager(context).putString(AppConstants.SSO_SESSION_ID, ssosessionid, true);
		// Saving this because if the user stays in pharmacy morethan 15 min and make any webservice call, which needs to make with updated Token.
		RefillRuntimeData.getInstance().setSSOSessionId(ssosessionid);
	}
	
	public String getSSOSessionId(Context context) {
		return getSharedPreferenceManager(context).getString(AppConstants.SSO_SESSION_ID, null);
	}

	public String getIntroCompleteFlag(Context context) {
		return getSharedPreferenceManager(context).getString(
				AppConstants.INTRO_COMPLETE_FL, null);
	}

	public String getSetupCompleteFlag(Context context) {
		return getSharedPreferenceManager(context).getString(
				AppConstants.SETUP_COMPLETE_FL, null);
	}

	public void clearSwitchFlags(Context context) {
		SharedPreferenceManager sSharedPreferenceManager=getSharedPreferenceManager(context);
		sSharedPreferenceManager.remove(AppConstants.ISNEW_USER);
		sSharedPreferenceManager.remove(AppConstants.DEVICE_SWITCH_FLAG);
		sSharedPreferenceManager.remove(AppConstants.FLAG_DISABLE_REFILL_ALERT);
		sSharedPreferenceManager.remove(AppConstants.BATTERY_OPTIMIZATION_DECISION);
		sSharedPreferenceManager.remove(AppConstants.SHOW_BATTERY_OPTIMIZATION_CARD);
	}

	public void clearSignonFields(Context context) {
		SharedPreferenceManager sSharedPreferenceManager=getSharedPreferenceManager(context);
		sSharedPreferenceManager.clearPreferences();
		sSharedPreferenceManager.remove(AppConstants.MRN);
		sSharedPreferenceManager.remove(AppConstants.USER_NAME);
		sSharedPreferenceManager.remove(AppConstants.USERID);
		sSharedPreferenceManager.remove(AppConstants.REMEMBER_USER_ID);
		//sSharedPreferenceManager.remove(AppConstants.DEVICE_SWITCH_FLAG);
		sSharedPreferenceManager.remove(AppConstants.ISNEW_USER);
		sSharedPreferenceManager.remove(AppConstants.KP_GUID);
		sSharedPreferenceManager.remove(AppConstants.INTRO_COMPLETE_FL);
		sSharedPreferenceManager.remove(AppConstants.FLAG_DISABLE_REFILL_ALERT);
		RunTimeData.getInstance().setUserLoginFlg(false);
		sSharedPreferenceManager.remove(AppConstants.IS_FRESHINSTALL_FLG);
		sSharedPreferenceManager.remove(AppConstants.SETUP_COMPLETE_FL);
		sSharedPreferenceManager.remove(AppConstants.FDB_IMAGE_CARD_DISPLAY_CHOICE);
		sSharedPreferenceManager.remove(AppConstants.FDB_IMAGE_CARD_DISPLAY_COUNTER);
		sSharedPreferenceManager.remove(AppConstants.BATTERY_OPTIMIZATION_DECISION);
		sSharedPreferenceManager.remove(AppConstants.SHOW_BATTERY_OPTIMIZATION_CARD);
	}

	
	public void startTimerTask(Context context) {
		TimerDelegate timerDelegate = TimerDelegate.getInstance();
		timerDelegate.startTimerTask(context);
	}
	
	public void restartTimerTask(Context context) {
		TimerDelegate timerDelegate = TimerDelegate.getInstance();
		timerDelegate.restartTimerTask(context);
	}

	public void resetTimerTask(Context context) {
		TimerDelegate timerDelegate = TimerDelegate.getInstance();
		timerDelegate.resetTimerTask(true);
	}

	public void setLastMemberMedsSyncTime(Context context,long lastSyncTime) {
		getSharedPreferenceManager(context).putLong(AppConstants.LAST_MEMBERMEDS_SYNC_TIME, lastSyncTime, true);
	}
	
	public void clearSSOSessionId(Context context) {
		getSharedPreferenceManager(context).remove(AppConstants.SSO_SESSION_ID);
	}

	public void setGuid(Context context,String guid) {
		getSharedPreferenceManager(context).putString(AppConstants.USERID, guid, true);
	}

	public void initilizeCertificateKeys(Context ctx) {
		CertificateValidaterObj obj = new CertificateValidaterObj();
		obj.setCnValues(prepareCNList());
		obj.setOrgValues(prepareOrgList());
		TTGSignonController.storeCertificateInfo(obj);
	}

	public void initilizeCertificateKeysForPharmacyDB() {
		CertificateValidaterObj obj = new CertificateValidaterObj();
		obj.setCnValues(prepareCNListForPharmacyDB());
		obj.setOrgValues(prepareOrgList());
		TTGSignonController.storeCertificateInfo(obj);
	}

	private ArrayList<String> prepareOrgList() {
		ArrayList<String> orgList = new ArrayList<>();
		orgList.add(AppConstants.ORG_TPMG_NAME);
		orgList.add(AppConstants.ORG_FOUNDATION_HEALTH_PLAN_NAME);
		orgList.add(AppConstants.ORG_KAISER_PERMANENTE_NAME);
		return orgList;
	}

	private ArrayList<String> prepareCNList() {
		ArrayList<String> orgList = new ArrayList<>();
		orgList.add(AppConstants.CN_NAME);
		orgList.add(AppConstants.CN_PERMANENTE);
		return orgList;
	}

	private ArrayList<String> prepareCNListForPharmacyDB() {
		ArrayList<String> orgList = new ArrayList<>();
		orgList.add(AppConstants.CN_NAME_PHARMACY_DB);
		return orgList;
	}
	
	public void resetQuickviewShownFlg(Context context){
		SharedPreferenceManager sSharedPreferenceManager=getSharedPreferenceManager(context);
		String quickviewShownorNot = sSharedPreferenceManager.getString("QuickviewToBeShown", "0");
		String tutorialCompleteFlg = sSharedPreferenceManager.getString(AppConstants.INTRO_COMPLETE_FL, AppConstants.TUTORIALS_COMPLETE_STATUS_YES);
		if(null!=quickviewShownorNot && (("1").equalsIgnoreCase(quickviewShownorNot) || tutorialCompleteFlg.equalsIgnoreCase(AppConstants.TUTORIALS_COMPLETE_STATUS_YES))){
			sSharedPreferenceManager.putString("QuickviewToBeShown", "0", true);
		}
	}

	public void setRefillScreenChoice(Context context,boolean decision) {
		getSharedPreferenceManager(context).putBoolean(AppConstants.FLAG_DISABLE_REFILL_ALERT, decision, false);
		
	}
	public boolean getRefillScreenChoice(Context context){
		return getSharedPreferenceManager(context).getBoolean(AppConstants.FLAG_DISABLE_REFILL_ALERT, true);
	}


	public void clearWelcomeScreenDisplayCounter(Context context) {
		getSharedPreferenceManager(context).remove(AppConstants.WELCOME_SCREEN_DISPLAY_COUNTER);
		getSharedPreferenceManager(context).remove(AppConstants.FDB_IMAGE_CARD_DISPLAY_COUNTER);
	}

	public void saveAccessToken(Context context, String accessToken) {
		getSharedPreferenceManager(context).putString(AppConstants.ACCESS_TOKEN_KEY, accessToken, false);
	}

	public void saveRefreshToken(Context context, String refreshToken) {
		getSharedPreferenceManager(context).putString(AppConstants.REFRESH_TOKEN_KEY, refreshToken, false);
	}

	public void saveTokenType(Context mContext, String token_type) {
		getSharedPreferenceManager(mContext).putString(AppConstants.TOKEN_TYPE_KEY, token_type, false);
	}

	public void saveTokenExpiryTime(Context mContext, String expiresIn) {
		getSharedPreferenceManager(mContext).putString(AppConstants.TOKEN_EXPIRES_IN, expiresIn, false);
	}

	public void clearAccessAndRefreshTokens(Context context){
		getSharedPreferenceManager(context).remove(AppConstants.ACCESS_TOKEN_KEY);
		getSharedPreferenceManager(context).remove(AppConstants.REFRESH_TOKEN_KEY);
		getSharedPreferenceManager(context).remove(AppConstants.TOKEN_TYPE_KEY);
		getSharedPreferenceManager(context).remove(AppConstants.TOKEN_EXPIRES_IN);
	}

	public String getAccessToken(Context context){
		return getSharedPreferenceManager(context).getString(AppConstants.ACCESS_TOKEN_KEY, AppConstants.EMPTY_STRING);
	}

	public String getRefreshToken(Context context){
		return getSharedPreferenceManager(context).getString(AppConstants.REFRESH_TOKEN_KEY, AppConstants.EMPTY_STRING);
	}

	public String getTokenType(Context context){
		return getSharedPreferenceManager(context).getString(AppConstants.TOKEN_TYPE_KEY, AppConstants.EMPTY_STRING);
	}

	public String getTokenExpiryTime(Context context){
		return getSharedPreferenceManager(context).getString(AppConstants.TOKEN_EXPIRES_IN, AppConstants.EMPTY_STRING);
	}

	public String getUserEmail(Context context) {
		return getSharedPreferenceManager(context).getString(AppConstants.USER_EMAIL, null);
	}

	public void startRefreshTokenTimerTask(Context context) {
		RefreshTokenTimerDelegate timerDelegate = RefreshTokenTimerDelegate.getInstance();
		timerDelegate.startTimerTask(context);
	}

	public void cancelRefreshTimerTask(Context context) {
		RefreshTokenTimerDelegate timerDelegate = RefreshTokenTimerDelegate.getInstance();
		timerDelegate.resetTimerTask(true);
	}

}