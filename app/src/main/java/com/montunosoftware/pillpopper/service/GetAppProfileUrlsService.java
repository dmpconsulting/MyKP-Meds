package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.os.AsyncTask;
import com.google.gson.Gson;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.AutoSignInSplashActivity;
import com.montunosoftware.pillpopper.android.Splash;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoginActivity;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.GenericHttpResponse;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.GenericHttpClient;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GetAppProfileUrlsService extends AsyncTask<Void, Void, GenericHttpResponse> {


	private Context mContext;

	private static String CONFIG_LIST_ARRAY = "configList";
	private static String DISABLED_FEATURES_ARRAY = "disabledFeatures";
	private static String JSON_RESPONSE = "response";
	private static String MONITOR_APP_KEY = "monitorAppKey";
	public static final String APP_PROFILE_CONFIG_FILE_PATH = "configList/config.properties";
	public static final String DISABLED_FEATURES_FILE = "disabledFeatures/disabledfeatures.properties";
	private static String APP_REGION_LIST_ARRAY = "appRegionsList";
	private SharedPreferenceManager mSharedPrefManager;
	private String mNonSecureLocalUrl = "";
	public static final String ANNOUNCEMENTS = "announcements";

	private boolean hasStatusUpdateCallRequired;

	public interface AppProfileWSComplete {
		void handleAppProfileComplete();
	}

	public GetAppProfileUrlsService(Context context) {
		mContext = context;
		mSharedPrefManager = SharedPreferenceManager.getInstance(mContext, AppConstants.AUTH_CODE_PREF_NAME);
	}

	public GetAppProfileUrlsService(Context context, String url, boolean hasStatusUpdateCallRequired) {
		this.mContext = context;
		this.mNonSecureLocalUrl = url;
		this.hasStatusUpdateCallRequired = hasStatusUpdateCallRequired;
		mSharedPrefManager = SharedPreferenceManager.getInstance(mContext, AppConstants.AUTH_CODE_PREF_NAME);
	}

	@Override
	protected GenericHttpResponse doInBackground(Void... sparams) {

		RunTimeData.getInstance().setAppProfileInProgress(true);
		GenericHttpClient gHttpClient = GenericHttpClient.getInstance();
		GenericHttpResponse genericHttpResponse = null;

		Map<String, String> params = new HashMap<>(ActivationUtil.getBaseParams(mContext));
		params.put("deviceId", ActivationUtil.getDeviceId(mContext));
		try {
			if(!Util.isEmptyString(mNonSecureLocalUrl)){
				genericHttpResponse = gHttpClient.executeUrlRequest(mNonSecureLocalUrl.concat("/services/getAppProfile"), AppConstants.POST_METHOD_NAME, params, null, null, null);//Request(httpRequest);
			}else{
				genericHttpResponse = gHttpClient.executeUrlRequest(AppConstants.getAppProfileUrl(), AppConstants.POST_METHOD_NAME, params, null, null, null);//Request(httpRequest);
			}
			if (genericHttpResponse != null) {
				LoggerUtils.info("---APP Profile API--- Response --- " + genericHttpResponse.getData());
			}
		} catch (Exception e) {
			if (e.getMessage() != null) {
				LoggerUtils.exception(e.getMessage());
				LoggerUtils.info("---APP Profile API--- Exception--- In Catch--- " + e.getMessage());
			}
		}

		return genericHttpResponse;
	}

	@Override
	protected void onPostExecute(GenericHttpResponse result) {
		super.onPostExecute(result);

		if (result != null && result.getStatus()) {
			processResponse(result);
			LoggerUtils.info("----Firebase----" + FireBaseConstants.Event.APP_PROFILE_SUCCESS);
			FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext, FireBaseConstants.Event.APP_PROFILE_SUCCESS);
		} else if (null == result) {
			LoggerUtils.error("App profile API response is null");
			trackAppProfileFailGAEvent();
		} else if (null != result
				&& AppConstants.HTTP_DATA_ERROR.equalsIgnoreCase(result
				.getData())) {
			LoggerUtils.error("ERROR in API Profile API Service ");
			trackAppProfileFailGAEvent();
		} else {
			trackAppProfileFailGAEvent();
			LoggerUtils.error("App profile API response failure " + result.getData());
		}
		RunTimeData.getInstance().setAppProfileInProgress(false);
		try {
			if (mContext instanceof Splash) {
				((Splash) mContext).handleAppProfileComplete();
			} else if (mContext instanceof LoginActivity) {
				((LoginActivity) mContext).handleAppProfileComplete();
			} else if (mContext instanceof AutoSignInSplashActivity) {
				((AutoSignInSplashActivity) mContext).handleAppProfileComplete();
			}
		} catch (Exception e) {
			PillpopperLog.say(e);
		}
	}

	public void trackAppProfileFailGAEvent() {
		LoggerUtils.info("----Firebase----" + FireBaseConstants.Event.APP_PROFILE_FAIL);
		FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext, FireBaseConstants.Event.APP_PROFILE_FAIL);
	}

	private void processResponse(GenericHttpResponse genericHttpResponse) {

		JSONObject appProfileResponse = null;
		try {
			JSONObject json = new JSONObject(genericHttpResponse.getData());
			appProfileResponse = json.getJSONObject(JSON_RESPONSE);
		} catch (JSONException e) {
			LoggerUtils.error(e.getLocalizedMessage());
		}

		if (appProfileResponse == null) {
			return;
		}

		parseDisabledFeatures(appProfileResponse);

		// Generic Banner and Card
		if (null != appProfileResponse) {
			parseAnnouncements(appProfileResponse);
		}

		JSONArray configUrls = appProfileResponse.optJSONArray(CONFIG_LIST_ARRAY);

		JSONArray regionListUrls = appProfileResponse.optJSONArray(APP_REGION_LIST_ARRAY);

		if (configUrls == null || regionListUrls == null) {
			return;
		}

		HashMap<String, String> configListMap = new HashMap<>();
		for (int i = 0; i < configUrls.length(); i++) {
			try {
				JSONObject config = (JSONObject) configUrls.get(i);
				String key = config.getString("name");
				String value = config.getString("value");
				configListMap.put(key, value);
				if(AppConstants.KEY_PILL_POPPER_NON_SECURED_BASE_URL.equalsIgnoreCase(key)) {
					mSharedPrefManager.putString(AppConstants.KEY_PILL_POPPER_NON_SECURED_BASE_URL, value, true);
					// check for non secure base url and then
					// check for 15 min and make has status update response call
					if (hasStatusUpdateCallRequired && Util.isHasStatusUpdateCallRequired(mContext) && !RunTimeData.getInstance().isHasStatusCallInProgress()) {
						// have to call the HasStatusUpdateAsyncTask with callback expected,
						// Since once after the call back we have to scan the alerts again and post the notification if required.
						HasStatusUpdateAsyncTask statusUpdateAsyncTask = new HasStatusUpdateAsyncTask(mContext, null);
						statusUpdateAsyncTask.execute();
					}
				}
			} catch (JSONException e) {
				LoggerUtils.error(e.getLocalizedMessage());
			} catch (Exception e) {
				LoggerUtils.exception(e.getMessage());
			}
		}


		HashMap<String, Boolean> regionListMap = new HashMap<>();

		if (regionListUrls.length() > 0) {
			for (int i = 0; i < regionListUrls.length(); i++) {
				try {
					JSONObject config = (JSONObject) regionListUrls.get(i);
					regionListMap.put(config.getString("regionCode"), config.getBoolean("refillNativeFl"));
				} catch (JSONException e) {
					LoggerUtils.error(e.getLocalizedMessage());
				} catch (Exception e) {
					LoggerUtils.exception(e.getMessage());
				}
			}
		}

		if (!regionListMap.isEmpty()) {
			RunTimeData.getInstance().setRegionListParams(regionListMap);
		}

		if (!configListMap.isEmpty()) {

			ActivationController.getInstance().saveAppProfileInvokedTimeStamp(mContext);
			TTGRuntimeData.getInstance().setConfigListParams(configListMap);

			//temporary fix for the keepAlive key changes
			if (TTGRuntimeData.getInstance().getConfigListParams().get(TTGMobileLibConstants.mConfigKeepAliveCookieName) == null) {
				TTGRuntimeData.getInstance().getConfigListParams().put(TTGMobileLibConstants.mConfigKeepAliveCookieName, AppConstants.ConfigParams.getKeepAliveCookieName());
				TTGRuntimeData.getInstance().getConfigListParams().put(TTGMobileLibConstants.mConfigKeepAliveCookieDomain, AppConstants.ConfigParams.getKeepAliveCookieDomain());
				TTGRuntimeData.getInstance().getConfigListParams().put(TTGMobileLibConstants.mConfigKeepAliveCookiePath, AppConstants.ConfigParams.getKeepAliveCookiePath());
				TTGRuntimeData.getInstance().getConfigListParams().put(TTGMobileLibConstants.mConfigKeepAliveIsCookieSecure, AppConstants.ConfigParams.getKeepAliveCookieIsSecure());
			}

			ActivationController.getInstance().initializeUrlsWithAppProfileResponse(configListMap);

			//initOrChangeAppDynamicsKey();

		}
	}

	private JSONObject readMockData() {
		String announcementsFileData = Util.loadJSONContent(mContext, "sampleCardBanner.json");
		try {
			JSONObject jsonObject = new JSONObject(announcementsFileData);
			return jsonObject;
		} catch (JSONException e) {
			LoggerUtils.exception("Generic Card and Banner - Invalid Json");
			LoggerUtils.exception(e.getMessage());
		}
		return null;
	}

	private void parseAnnouncements(JSONObject announcements) {
		try {
			Gson gson = new Gson();
			AnnouncementsResponse announcementsResponse = (AnnouncementsResponse) gson.fromJson(String.valueOf(announcements), AnnouncementsResponse.class);
			RunTimeData.getInstance().setAnnouncements(announcementsResponse);
		} catch (Exception ex){
			LoggerUtils.exception(ex.getMessage());
		}
	}

	private void parseDisabledFeatures(JSONObject appProfileResponse) {

		JSONArray disabledFeatures = appProfileResponse.optJSONArray(DISABLED_FEATURES_ARRAY);
		if(disabledFeatures == null){
			return;
		}

		HashMap<String,String> disabledFeaturesMap = new HashMap<>();
		for (int i = 0; i < disabledFeatures.length(); i++) {
			try {
				JSONObject disabledFeature = (JSONObject) disabledFeatures.get(i);
				disabledFeaturesMap.put(disabledFeature.getString("code"), disabledFeature.getString("reason"));
			} catch (JSONException e) {
				LoggerUtils.error(e.getLocalizedMessage());
			}
		}

		File file = new File(mContext.getFilesDir() + File.separator
				+ DISABLED_FEATURES_FILE);

		try {
			if (file.exists()) {
				if(!file.delete()){
					PillpopperLog.say("Oops, GetAppProfileUrlsService -  processResponse - failed to delete file");
				}
			} else {
				File configDir = new File(mContext.getFilesDir() + File.separator
						+ DISABLED_FEATURES_ARRAY);
				configDir.mkdir();
				if(!file.createNewFile()){
					PillpopperLog.say("Oops, GetAppProfileUrlsService -  processResponse - failed to create file");
				}
			}

			Properties properties = new Properties();
			for (Map.Entry<String, String> entry : disabledFeaturesMap
					.entrySet()) {
				properties.put(entry.getKey().toString(), entry.getValue()
						.toString());
			}

			writeToFile(properties, file);

		} catch (IOException e) {
			LoggerUtils.exception(e.getMessage());
		}
	}

	private void writeToFile(Properties properties, File file) {

		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(file);
			properties.store(fileOut, "App Profile URLs");
			fileOut.close();
		} catch (IOException e) {
			LoggerUtils.error(e.getLocalizedMessage());
		}
	}
}
