package org.kp.tpmg.mykpmeds.activation.controller;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Strings;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoginActivity;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.utilities.TTGLoggerUtil;

import java.util.HashMap;

public class ActivationController {
	private static ActivationController activationController;

	private static AppData sAppData;
	/*private static String mConfigUserIdHelpUrl = "useridhelpurl";
	private static String mConfigPwdhelpUrl = "passwordhelpurl";
	private static String mConfigRegisterUrl = "registerurl";
	private static String mConfigAppidUrl = "appid";
	private static String mConfigMemberRegionUrl = "memberregion";
	private static String mConfigApikeyUrl = "apikey";
	private static String mConfigMonitorAppKey="monitorAppKey";
	private static String mConfigMonitorURL="monitorURL";
	public static final String mWsSecuredBaseURL="wsSecuredBaseURL";
	public static final String mWsNonSecuredBaseURL="wsNonSecuredBaseURL";
	public static final String mUsablenetCookeName = "usablenetCookeName";
	public static final String mCookieName = "cookieName";
	public static final String mPillpopperBaseUrl = "wsPillpopperBaseUrl";
	public static final String mPillpopperNonSecureBaseUrl = "wsPillpopperNonSecuredBaseURL";
	public static final String mKpPharmacyURL = "kpPharmacyURL";*/

	public static ActivationController getInstance() {
		if(activationController==null)
			activationController = new ActivationController();

		sAppData = AppData.getInstance();
		return activationController;
	}

	/**
	 * @return Return TRUE if time out happens. Return FALSE if time out not
	 *         detects.
	 */
	public boolean checkForTimeOut(Context context) {
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(
				context, AppConstants.AUTH_CODE_PREF_NAME);
		return RunTimeData.getInstance().getOldLockTime() == Long.MAX_VALUE
				|| manager.getBoolean("timeOut", false)
				|| (System.currentTimeMillis() - AppConstants.TIMEOUT_PERIOD > RunTimeData
				.getInstance().getOldLockTime());
	}

	/**
	 * @return Returns UserName
	 * @param context
	 */
	public String getUserName(Context context) {
		String userName = sAppData.getUserName(context);
		if (!Strings.isNullOrEmpty(userName)) {
			return userName;
		}
		return AppConstants.EMPTY_STRING;
	}

	/**
	 * 
	 * @param context application context
	 * @return TRUE if the user signed in and Time out not occurred
	 * or FALSE is the user not signed in or Timeout occurred.
	 */
	public boolean isSessionActive(Context context) {
		boolean isUserLoggedIn = getUserLoginFlg();  // includes both offline and online login

		if(AppConstants.isByPassLogin()){
			return true;
		}
		return !(!isUserLoggedIn || checkForTimeOut(context));
	}

	/**
	 * @return Returns UserId associated with the userName.
	 * @param context
	 */
	public String getUserId(Context context) {
		String userId = sAppData.getUserId(context);
		if (null == userId || ("").equals(userId)) {
			userId = null != RunTimeData.getInstance()
					.getSigninRespObj() ? RunTimeData.getInstance()
					.getSigninRespObj().getGuid():null;
		}
		return userId;
	}

	public String getSSOSessionId(Context context) {
		return sAppData.getSSOSessionId(context);
	}

	/**
	 * @return Returns User tutorial Completion flag.
	 */
	public String getIntroCompleteFlag(Context context) {
		return sAppData.getIntroCompleteFlag(context);
	}

	/**
	 * @return Returns the user MRN
	 * @param context
	 */
	public String getMrn(Context context) {
		return sAppData.getMRN(context);
	}

	/**
	 * @return Returns TRUE if the logged in user is different than last Logged in user. otherwise FALSE
	 */
	public boolean isNewUser(Context context) {
		return sAppData.isNewUser(context);
	}

	/**
	 * @return Returns TRUE in case of switch device
	 */
	public boolean isDeviceSwitched(Context context) {
		return sAppData.isDeviceSwitched(context);
	}

	/**
	 * @return Returns TRUE in case of switch user and/or switch device.
	 */
	public boolean isDataResetFl(Context context) {
		return sAppData.isNewUser(context) || sAppData.isDeviceSwitched(context);
	}

	/**
	 * Clears device and user switch flags.
	 */
	public void clearSwitchFlags(Context context) {
		if (isDataResetFl(context)) {
			sAppData.clearSwitchFlags(context);
		}
	}

	/**
	 * To wipe out stored data during login and device/user switch flags
	 */
	public void clearSignonFields(Context context) {
		sAppData.clearSignonFields(context);
		clearSwitchFlags(context);
	}

	public void clearUserLoginFlg() {
		setUserLoginFlg(false);
	}

	/**
	 * Calls LoginActivity
	 * @param context application context.
	 */
	public void performSignoff(Context context){

		TTGLoggerUtil.info("--- Doing Login perform from Activation Controller---");
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context,AppConstants.AUTH_CODE_PREF_NAME);


		Intent intent = new Intent(context,
				LoginActivity.class);
		clearUserLoginFlg();
		stopTimer(context);
		resetCookiesInfo();
		resetQuickviewShownFlg(context);
		AppConstants.setByPassLogin(false);
		manager.remove(AppConstants.SSO_SESSION_ID);
		manager.putBoolean("timeOut", true, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * Calls LoginActivity
	 * @param context application context.
	 */
	public void performSilentSignoff(Context context){
		TTGLoggerUtil.info("--- Doing Login perform from Activation Controller---");
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context,AppConstants.AUTH_CODE_PREF_NAME);
		clearUserLoginFlg();
		stopTimer(context);
		resetCookiesInfo();
		resetQuickviewShownFlg(context);
		AppConstants.setByPassLogin(false);
		manager.remove(AppConstants.SSO_SESSION_ID);
		manager.putBoolean("timeOut", true, true);
	}

	public void performSignoffAndShowAlert(Context context){
		TTGLoggerUtil.info("--- Doing Login performSignoffAndShowAlert---");
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context,AppConstants.AUTH_CODE_PREF_NAME);
		Intent intent = new Intent(context,
				LoginActivity.class);
		PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
		clearUserLoginFlg();
		resetQuickviewShownFlg(context);
		stopTimer(context);
		resetCookiesInfo();
		manager.remove(AppConstants.SSO_SESSION_ID);
		manager.putBoolean("timeOut", true, true);
		intent.putExtra("isSessionExpiredRequire", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public void resetCookiesInfo() {
		if(CookieResetController.myapp !=null){
			CookieResetController.myapp.resetCoockies();
		}
	}

	/**
	 * To Start Timer
	 **/
	public void startTimer(Context context) {
		sAppData.startTimerTask(context);
	}

	/**
	 * To Restart Timer
	 **/
	public void restartTimer(Context context) {
		sAppData.restartTimerTask(context);
	}

	/**
	 * To Reset Timer
	 **/
	public void stopTimer(Context context) {
		sAppData.resetTimerTask(context);
	}

	/**
	 * This is the method which needs to be called in My Med application to set
	 * LastsyncTime
	 */
	public void setLastMemberMedsSyncTime(Context context,long lastSyncTime) {
		sAppData.setLastMemberMedsSyncTime(context,lastSyncTime);
	}

	/**
	 * Returns App visibility
	 * @return boolean
	 */
	public boolean isAppVisible() {
		return RunTimeData.getInstance().isAppVisible();
	}

	public void setUserLoginFlg(boolean b) {
		RunTimeData.getInstance().setUserLoginFlg(b);
	}

	public boolean getUserLoginFlg() {
		return RunTimeData.getInstance().getUserLoginFlg();
	}

	public void initializeUrlsWithAppProfileResponse(HashMap<String, String> configListMap) {
		LoggerUtils.info("Initializing Activation library server urls");

		if(configListMap.isEmpty()){
			return;
		}

		TTGMobileLibConstants.sKPMobileAppId = AppConstants.APP_ID;

	}
	
	
	public static void initializeLoggersInSignOnLib() {
		TTGLoggerUtil.LOGGING_ERROR = AppConstants.isLogging();
		TTGLoggerUtil.LOGGING_INFO = AppConstants.isLogging();
		TTGLoggerUtil.LOGGING_EXCEPTION = AppConstants.isLogging();
		TTGLoggerUtil.LOGGING_WARNING = AppConstants.isLogging();
	}

	public void clearSSOSessionId(Context context){
		sAppData.clearSSOSessionId(context);
	}

	public static void initilizeCertificateKeys(Context ctx) {
		AppData.getInstance().initilizeCertificateKeys(ctx);
	}
	public void resetQuickviewShownFlg(Context context){
		sAppData.resetQuickviewShownFlg(context);
	}
	public void setRefillScreenChoice(Context context,boolean decision){
		sAppData.setRefillScreenChoice(context,decision);
	}
	
	public boolean getRefillScreenChoice(Context context){
		return sAppData.getRefillScreenChoice(context);
	}

	public String getSetupCompleteFlag(Context context){
		return sAppData.getSetupCompleteFlag(context);
	}

	public void clearWelcomeScreenDisplayCounter(Context context){
		sAppData.clearWelcomeScreenDisplayCounter(context);
	}

	public void saveAppProfileInvokedTimeStamp(Context context){
		SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(
				context, AppConstants.AUTH_CODE_PREF_NAME);
		sharedPreferenceManager.putLong(AppConstants.APP_PROFILE_INVOKED_TIMESTAMP, System.currentTimeMillis(), true);

	}

	public void saveUserRegion(Context context, String region){
		SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(
				context, AppConstants.AUTH_CODE_PREF_NAME);
		sharedPreferenceManager.putString(AppConstants.USER_REGION_KET, region, true);
	}

	public String fetchUserRegion(Context context){
		SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(
				context, AppConstants.AUTH_CODE_PREF_NAME);
		return sharedPreferenceManager.getString(AppConstants.USER_REGION_KET, "MRN");
	}

	public String getAccessToken(Context context){
		return sAppData.getAccessToken(context);
	}

	public String getRefreshToken(Context context){
		return sAppData.getRefreshToken(context);
	}

	public String getTokenType(Context context){
		return sAppData.getTokenType(context);
	}

	public void setSSOSessionId(Context context, String ssoSession) {
		sAppData.setSSOSessionId(context, ssoSession);
	}

	public void saveUserEmail(Context context, String email){
		SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(
				context, AppConstants.AUTH_CODE_PREF_NAME);
		sharedPreferenceManager.putString(AppConstants.USER_EMAIL, email, true);
	}

	public void saveUserAge(Context context, String age){
		SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(
				context, AppConstants.AUTH_CODE_PREF_NAME);
		sharedPreferenceManager.putString(AppConstants.USER_AGE, age, true);
	}

	public String getUserAge(Context context){
		SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(
				context, AppConstants.AUTH_CODE_PREF_NAME);
		return sharedPreferenceManager.getString(AppConstants.USER_AGE, "");
	}

	public String getUserEmail(Context context) {
		return sAppData.getUserEmail(context);
	}

}