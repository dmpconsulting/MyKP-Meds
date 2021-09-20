package com.montunosoftware.pillpopper.android.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.webkit.CookieManager;
import android.webkit.URLUtil;

import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.Preferences;
import com.montunosoftware.pillpopper.model.State;

import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.kp.tpmg.ttgmobilelib.utilities.TTGSSLSocketFactory;
import org.kp.tpmg.ttgmobilelib.utilities.TTGUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class PillpopperServer
{
	public static final String IDENTIFY_BY_PREF = "userData";

	private final PillpopperAppContext _globalAppContext;
	private final String _hardwareId;
	private final String _versionName;
	private static final String _JSON_ERROR_TEXT = "errorText";
	private static final String _JSON_DEV_INFO = "devInfo";

	private static PillpopperServer _serverInstance = null;
	Context context;
	TTGSSLSocketFactory socketFactory;
	Preferences _drugPrefs;
	String _appVersion;
	private long _replayId;

	public static class ServerUnavailableException extends Exception
	{
		private static final long serialVersionUID = -9012109482517123021L;

		private String _devInfo;
		private String _errorText;

		public ServerUnavailableException()
		{
			super("ServerUnavailableException");
			_devInfo = null;
			_errorText = null;
		}

		public ServerUnavailableException(JSONObject pillpopperResponse)
		{
			_devInfo = Util.parseJSONStringOrNull(pillpopperResponse, _JSON_DEV_INFO);
			_errorText = Util.parseJSONStringOrNull(pillpopperResponse, _JSON_ERROR_TEXT);

			PillpopperLog.say("Server returned error: userError=%s. devError=%s.",
					_errorText == null ? "<null>" : _errorText,
					_devInfo == null ? "<null>" : _devInfo
			);
		}
	}


	private PillpopperServer(Context ctx,PillpopperAppContext globalAppContext) throws IOException
	{
		_globalAppContext = globalAppContext;
		context = ctx;
		Util.storeEnvironment(context);
		_hardwareId = UniqueDeviceId.getHardwareId(context);

		_drugPrefs =  globalAppContext.getState(context).getPreferences();

		_appVersion = ActivationUtil.getAppVersion(context);

		String versionName;
		try {
			versionName = String.format(Locale.US, "Android-%s-%s",
					_globalAppContext.getEditionName(),
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			versionName = "Android-unknown";
			PillpopperLog.say("NameNotFoundException", e);
		}
		_versionName = versionName;
	}

	public static synchronized PillpopperServer getInstance(Context ctx,PillpopperAppContext globalAppContext) throws ServerUnavailableException
	{

		if (_serverInstance == null) {
			try {
				_serverInstance = new PillpopperServer(ctx,PillpopperAppContext.getGlobalAppContext(ctx));
			} catch (IOException ioe) {
				PillpopperLog.say("can't create http client: %s", ioe);
				throw new ServerUnavailableException();
			}
		}

		return _serverInstance;
	}


	public JSONObject makeQuickviewMultiActionRequest(final String action, final List<Drug> overdueDrugs, Context context, boolean actionTakenFromNotification) throws ServerUnavailableException{
		JSONObject resonse = null;
		try {
			// Check for missed events for this list of drugs
			//checkMissedEventsForMultiDrugs(overdueDrugs);
			//resonse = makeNonSecureRequest(prepareMultiActionReq(action , overdueDrugs), null, null);
			resonse = makeQuickViewCallForMultiResponse(prepareMultiActionRequestObject(action , overdueDrugs,context),actionTakenFromNotification, context);

		} catch (Exception e) {
			PillpopperLog.say("http post: Exception", e);
		}
		return resonse;
	}

	public JSONObject makeRequestInNonSecureMode(JSONObject requestObject, Context context){
		JSONObject response = null;
		try {
			if(null!=requestObject){
				response = makeQuickViewCallForMultiResponse(requestObject, false, context);
			}

		} catch (Exception e) {
			PillpopperLog.say("http post: Exception", e);
		}
		return response;
	}

	public JSONObject makeQuickviewMultiActionRequest(final String action, final List<Drug> overdueDrugs, PillpopperActivity pillpopperActivity,
													  boolean isAlredyTaken, boolean actionTakenFromNotification) throws ServerUnavailableException{
		JSONObject resonse = null;
		try {
			// Check for missed events for this list of drugs
			//checkMissedEventsForMultiDrugs(overdueDrugs);
			//resonse = makeNonSecureRequest(prepareMultiActionReq(action , overdueDrugs), null, null);
			resonse = makeQuickViewCallForMultiResponse(prepareMultiActionRequestObject(action , overdueDrugs, pillpopperActivity, isAlredyTaken), actionTakenFromNotification, context);

		} catch (Exception e) {
			PillpopperLog.say("http post: Exception", e);
		}
		return resonse;
	}

	public JSONObject prepareMultiActionRequestObject(String action, List<Drug> overdueDrugs, PillpopperActivity pillpopperActivity, boolean isAlreadyTaken)
	{
		JSONObject finalRequestObj = new JSONObject();
		JSONObject multipleReqObj = new JSONObject();
		JSONArray requestArray = new JSONArray();
		JSONObject entryJSONObject;

		for(int i=0; i<overdueDrugs.size(); i++ ){
			try {
				JSONArray adjustPillJSONArray = Util.checkForDSTAndPrepareAdjustPillJSONArray(pillpopperActivity);
				if(null!=adjustPillJSONArray){
					for(int index =0; index<adjustPillJSONArray.length(); index++){
						PillpopperLog.say("Adding AdjustPill Object : " + adjustPillJSONArray.getJSONObject(index).toString());
						requestArray.put(adjustPillJSONArray.get(index));
					}
				}

				if(overdueDrugs.get(i).getScheduledTime()!=null){
					PillpopperLog.say("ScheduleTime is not null means it would be past reminder drug");

					if(null!= Util.prepareLogEntryForAction_pastReminders(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject()){
						requestArray.put(Util.prepareLogEntryForAction_pastReminders(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject());
					}

					if(null!= Util.prepareLogEntryForCreateHistoryEvent_pastReminders(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject()){
						requestArray.put(Util.prepareLogEntryForCreateHistoryEvent_pastReminders(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject());
					}

				}else{

					if(null!= Util.prepareLogEntryForAction(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject()){
						requestArray.put(Util.prepareLogEntryForAction(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject());
					}

					if(isAlreadyTaken){
						entryJSONObject = Util.prepareLogEntryForCreateHistoryEvent(PillpopperConstants.ACTION_TAKEN_EARLIER, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject();
					}else{
						entryJSONObject = Util.prepareLogEntryForCreateHistoryEvent(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject();
					}
					if(null!=entryJSONObject){
						requestArray.put(entryJSONObject);
					}
				}


				multipleReqObj.put("requestArray", requestArray);
				multipleReqObj.put("getAllOutput", 1);
				finalRequestObj.put("pillpopperMultiRequest", multipleReqObj);
				Util.processPillRequestObjectFrom(finalRequestObj, context);

			}catch (Exception e) {
				PillpopperLog.say("Opps!, Exception while prepareMultiActionReq", e);
			}
		}
		PillpopperLog.say("Quickview Final Multi action Request : " + finalRequestObj.toString());
		return finalRequestObj;
	}


	public JSONObject prepareMultiActionRequestObject(String action, List<Drug> overdueDrugs, Context pillpopperActivity)
	{
		JSONObject finalRequestObj = new JSONObject();
		JSONObject multipleReqObj = new JSONObject();
		JSONArray requestArray = new JSONArray();

		for(int i=0; i<overdueDrugs.size(); i++ ){
			try {

				JSONArray adjustPillJSONArray = Util.checkForDSTAndPrepareAdjustPillJSONArray(pillpopperActivity);
				if(null!=adjustPillJSONArray){
					for(int index =0; index<adjustPillJSONArray.length(); index++){
						PillpopperLog.say("Adding AdjustPill Object : " + adjustPillJSONArray.getJSONObject(index).toString());
						requestArray.put(adjustPillJSONArray.get(index));
					}
				}

				if(overdueDrugs.get(i).getScheduledTime()!=null){
					PillpopperLog.say("ScheduleTime is not null means it would be past reminder drug");

					if(null!= Util.prepareLogEntryForAction_pastReminders(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject()){
						requestArray.put(Util.prepareLogEntryForAction_pastReminders(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject());
					}

					if(null!= Util.prepareLogEntryForCreateHistoryEvent_pastReminders(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject()){
						requestArray.put(Util.prepareLogEntryForCreateHistoryEvent_pastReminders(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject());
					}

				}else{

					if(null!= Util.prepareLogEntryForAction(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject()){
						requestArray.put(Util.prepareLogEntryForAction(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject());
					}

					if(null!= Util.prepareLogEntryForCreateHistoryEvent(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject()){
						requestArray.put(Util.prepareLogEntryForCreateHistoryEvent(action, overdueDrugs.get(i), pillpopperActivity).getEntryJSONObject());
					}
				}


				multipleReqObj.put("requestArray", requestArray);
				multipleReqObj.put("getAllOutput", 1);
				finalRequestObj.put("pillpopperMultiRequest", multipleReqObj);
				Util.processPillRequestObjectFrom(finalRequestObj, context);

			}catch (Exception e) {
				PillpopperLog.say("Opps!, Exception while prepareMultiActionReq", e);
			}
		}
		PillpopperLog.say("Quickview Final Multi action Request : " + finalRequestObj.toString());
		return finalRequestObj;
	}


	public JSONObject makeQuickViewCallForMultiResponse(JSONObject originalRequest, boolean actionTakenFromNotification, Context context) throws ServerUnavailableException
	{
		JSONObject jsonResponse = null;
		String nonSecureUrl = AppConstants.ConfigParams.getWsPillpopperNonSecuredBaseURL();
		if(actionTakenFromNotification && Util.isEmptyString(nonSecureUrl)) {
			nonSecureUrl = FrontController.getInstance(context).getLocalNonSecureUrl(context);
		}

		if(Util.isEmptyString(nonSecureUrl)){
			return null;
		}
		try {
			initializeSSL();
			URL url = new URL(nonSecureUrl);
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
			PillpopperLog.say("Triggering url : " + nonSecureUrl);
			PillpopperLog.say("QuickView request : " + originalRequest.toString());
			httpsURLConnection.setConnectTimeout(45000);
			httpsURLConnection.setReadTimeout(45000);
			httpsURLConnection.setDoInput(true);
			httpsURLConnection.setDoOutput(true);
			httpsURLConnection.setRequestMethod("POST");
			httpsURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
			httpsURLConnection.setRequestProperty("Content-Type", "application/json");
			httpsURLConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());

			OutputStream outputStream = httpsURLConnection.getOutputStream();
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
			bufferedWriter.write(originalRequest.toString());
			bufferedWriter.flush();
			bufferedWriter.close();
			outputStream.close();

			int status = httpsURLConnection.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				InputStream stream = httpsURLConnection.getInputStream();
				jsonResponse = new JSONObject(TTGUtil.convertResponseTOString(stream));
				//PillpopperLog.say("quickview server response: " + jsonResponse.toString());
				if (null!=jsonResponse && jsonResponse.has(_JSON_ERROR_TEXT)) {
					throw new ServerUnavailableException(jsonResponse);
				}
			}else {
				PillpopperLog.say("http post: got non-200 status %d", status);
				throw new ServerUnavailableException();
			}

		} catch (IOException e) {
			PillpopperLog.say("http post: I/O exception: %s", e);
			throw new ServerUnavailableException();
		} catch (JSONException e) {
			PillpopperLog.say("http post: got non-json response from server", e);
			throw new ServerUnavailableException();
		} catch (Exception e) {
			PillpopperLog.say("http post: Exception :", e);
		}
		return jsonResponse;
	}

	/**
	 * Method to take the log entries from the intermediate log entry table and make the intermediate Sync request.
	 * @param originalRequest Original Request
	 * @param headers headers
	 * @return JSONObject
	 * @throws ServerUnavailableException
	 */
	public JSONObject makeIntermediateSync(JSONObject originalRequest, Map<String, String> headers, String url) throws ServerUnavailableException{
		JSONObject pillpopperResponse = null ;
		PillpopperLog.say("Making server request to %s...", url);
		PillpopperLog.say("Debug server error ------ _serverName : "+ url);
		if(URLUtil.isHttpsUrl(url)){
			try{
				URL _url = new URL(url);
				URLConnection connection = _url.openConnection();
				initializeSSL();
				HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
				httpsConnection.setConnectTimeout(45000);
				httpsConnection.setDoInput(true);
				httpsConnection.setDoOutput(true);
				httpsConnection.setInstanceFollowRedirects(false);
				httpsConnection.setRequestMethod("POST");
				httpsConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());
				if (headers != null && headers.size() > 0) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						httpsConnection.addRequestProperty(header.getKey(), header.getValue());
					}
				}

				httpsConnection.addRequestProperty("Cookie", getLocalCookies());

				httpsConnection.setRequestProperty("Content-Type", "application/json");
				OutputStream out = httpsConnection.getOutputStream();
				if(null!=originalRequest){
					String temp = originalRequest.toString();
					PillpopperLog.say("Server request " + temp);
					out.write(temp.getBytes());
				}
				out.close();
				httpsConnection.connect();
				int status = httpsConnection.getResponseCode();
				//Firebase event
				FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(context,
						status == HttpURLConnection.HTTP_OK ? FireBaseConstants.Event.GET_INTERMEDIATE_SYNC_SUCCESS : FireBaseConstants.Event.GET_INTERMEDIATE_SYNC_FAIL);
				LoggerUtils.info("----Firebase----" + (status == HttpURLConnection.HTTP_OK ? FireBaseConstants.Event.GET_INTERMEDIATE_SYNC_SUCCESS : FireBaseConstants.Event.GET_INTERMEDIATE_SYNC_FAIL));
				if (status == HttpURLConnection.HTTP_OK) {			//	StatusLine status = response.getStatusLine();
					PillpopperLog.say("http post: status code %d", status);
					String resString = TTGUtil.convertResponseTOString(httpsConnection.getInputStream());//Util.convertHttpResponseToString(httpConnection.getInputStream());
					PillpopperLog.say("Server request successful" + resString);
					if (null != resString) {
						try {
							return new JSONObject(resString);
						} catch (Exception e) {
							PillpopperLog.say("Oops, Exception while JSON object from HTTP Response Object", e);
							throw new ServerUnavailableException();
						}
					}
				} else {
					if (status != 200 && status !=302) {
						PillpopperLog.say("http post: got non-200 status %d", status);
						throw new ServerUnavailableException();
					}
				}
				return pillpopperResponse;

			} catch (IOException e) {
				PillpopperLog.say("http post: I/O exception: %s", e);
				throw new ServerUnavailableException();
			} catch (Exception e){
				PillpopperLog.say("http post: Exception from server", e);
				throw new ServerUnavailableException();
			}
		}else{
			try
			{
				URL _url = new URL(url);
				URLConnection connection = _url.openConnection();
				initializeSSL();
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setConnectTimeout(45000);
				httpConnection.setDoInput(true);
				httpConnection.setDoOutput(true);
				httpConnection.setInstanceFollowRedirects(false);
				httpConnection.setRequestMethod("POST");
				if (headers != null && headers.size() > 0) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						httpConnection.addRequestProperty(header.getKey(), header.getValue());
					}
				}

				httpConnection.addRequestProperty("Cookie", getLocalCookies());
				httpConnection.setRequestProperty("Content-Type", "application/json");
				OutputStream out = httpConnection.getOutputStream();
				String temp = originalRequest.toString();
				PillpopperLog.say("Server request " + temp);
				out.write(temp.getBytes());
				out.close();
				httpConnection.connect();
				int status = httpConnection.getResponseCode();
				if (status == HttpURLConnection.HTTP_OK) {			//	StatusLine status = response.getStatusLine();
					PillpopperLog.say("http post: status code %d", status);
					String resString = TTGUtil.convertResponseTOString(httpConnection.getInputStream());//Util.convertHttpResponseToString(httpConnection.getInputStream());
					PillpopperLog.say("Server request successful" + resString);
					//JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response.getEntity()));
					if(null!=resString){
						try {
							return new JSONObject(resString);
						} catch (Exception e) {
							PillpopperLog.say("Oops, Exception while JSON object from HTTP Response Object", e);
							throw new ServerUnavailableException();
						}
					}
				} else{
					if (status != 200 && status !=302) {
						PillpopperLog.say("http post: got non-200 status %d", status);
						throw new ServerUnavailableException();
					}
				}
				return pillpopperResponse;

			} catch (IOException e) {
				PillpopperLog.say("http post: I/O exception: %s", e);
				throw new ServerUnavailableException();
			} catch (Exception e){
				PillpopperLog.say("http post: Exception from server", e);
				throw new ServerUnavailableException();
			}
		}
	}

	public Map<String,String> getHasStatusUpdateHeaders() {
		SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
		Map<String,String> headers = new HashMap<>();
		headers.put("userId", DatabaseUtils.getInstance(context).getPrimaryUserIdIgnoreEnabled());
		headers.put("hardwareId", _hardwareId);
		headers.put("guid", sharedPreferenceManager.getString("kpGuid",null));
		headers.put("os", TTGMobileLibConstants.OS);
		headers.put("appVersion",Util.getAppVersion(context));
		headers.put("osVersion",AppConstants.OS_VERSION);
		return headers;
	}

	public JSONObject makeHasStatusUpdateRequest(String url) throws ServerUnavailableException {
		JSONObject serverResponse = null;

		if(URLUtil.isHttpsUrl(url)){
			try
			{
				URL _url = new URL(url);
				URLConnection connection = _url.openConnection();
				initializeSSL();
				JSONObject pillpopperRequest = null;
				pillpopperRequest = prepareHasStatusUpdateRequestObj(DatabaseUtils.getInstance(context).getPrimaryUserIdIgnoreEnabled(), context);
				// Send it off
				//	httpPost.setEntity(new StringEntity(request.toString(), HTTP.UTF_8));
				HttpsURLConnection httpConnection = (HttpsURLConnection) connection;
				httpConnection.setConnectTimeout(45000);
				httpConnection.setDoInput(true);
				httpConnection.setDoOutput(true);
				httpConnection.setInstanceFollowRedirects(false);
				httpConnection.setRequestMethod("POST");
				httpConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());

				Map<String,String> headers = getHasStatusUpdateHeaders();
				if (headers != null && headers.size() > 0) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						httpConnection.addRequestProperty(header.getKey(), header.getValue());
					}
				}

				httpConnection.setRequestProperty("Content-Type", "application/json");
				OutputStream out = httpConnection.getOutputStream();
				PillpopperLog.say("request for HasStatusUpdate" + pillpopperRequest.toString());
				out.write(pillpopperRequest.toString().getBytes());
				out.close();
				httpConnection.connect();
				int status = httpConnection.getResponseCode();

				if (status != 200 && status !=302) {
					PillpopperLog.say("http post: got non-200 status %d", status);
					throw new ServerUnavailableException();
				}

				if (status == HttpURLConnection.HTTP_OK) {			//	StatusLine status = response.getStatusLine();

					PillpopperLog.say("http post: status code %d", status);
					String resString = TTGUtil.convertResponseTOString(httpConnection.getInputStream());//Util.convertHttpResponseToString(httpConnection.getInputStream());
					PillpopperLog.say("Server request successful for HasStatusUpdate" + resString);
					if (null != resString) {
						try {
							serverResponse = new JSONObject(resString);
						} catch (JSONException e) {
							PillpopperLog.say("Oops, Exception while JSON object from HTTP Response Object", e);
						} catch (Exception e) {
							PillpopperLog.say("Oops, Exception while JSON object from HTTP Response Object", e);
							throw new ServerUnavailableException();
						}
					}
				}
				return serverResponse;

			} catch (IOException e) {
				PillpopperLog.say("http post: I/O exception: %s", e);
				throw new ServerUnavailableException();
			} catch (Exception e){
				PillpopperLog.say("http post: Exception from server", e);
				throw new ServerUnavailableException();
			}
		}else{
			try
			{
				URL _url = new URL(url);
				URLConnection connection = _url.openConnection();
				initializeSSL();
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				JSONObject pillpopperRequest = prepareHasStatusUpdateRequestObj(DatabaseUtils.getInstance(context).getPrimaryUserIdIgnoreEnabled(), context);
				// Send it off
				//	httpPost.setEntity(new StringEntity(request.toString(), HTTP.UTF_8));
				httpConnection.setConnectTimeout(45000);
				httpConnection.setDoInput(true);
				httpConnection.setDoOutput(true);
				httpConnection.setInstanceFollowRedirects(false);
				httpConnection.setRequestMethod("POST");

				Map<String,String> headers = getHasStatusUpdateHeaders();
				if (headers != null && headers.size() > 0) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						httpConnection.addRequestProperty(header.getKey(), header.getValue());
					}
				}

				httpConnection.setRequestProperty("Content-Type", "application/json");
				OutputStream out = httpConnection.getOutputStream();
				out.write(pillpopperRequest.toString().getBytes());
				out.close();
				httpConnection.connect();
				int status = httpConnection.getResponseCode();
				status = httpConnection.getResponseCode();
				if (status == HttpURLConnection.HTTP_OK) {			//	StatusLine status = response.getStatusLine();
					PillpopperLog.say("http post: status code %d", status);
					String resString = TTGUtil.convertResponseTOString(httpConnection.getInputStream());//Util.convertHttpResponseToString(httpConnection.getInputStream());
					PillpopperLog.say("Server request successful");
					if (null != resString) {
						try {
							serverResponse = new JSONObject(resString);

						} catch (JSONException e){
							PillpopperLog.say("JSONException", e);
						} catch (Exception e) {
							PillpopperLog.say("Oops, Exception while JSON object from HTTP Response Object", e);
							throw new ServerUnavailableException();
						}
					}
				} else{
					if (status != 200 && status !=302) {
						PillpopperLog.say("http post: got non-200 status %d", status);
						throw new ServerUnavailableException();
					}
				}
				return serverResponse;

			} catch (IOException e) {
				PillpopperLog.say("http post: I/O exception: %s", e);
				throw new ServerUnavailableException();
			} catch (Exception e){
				PillpopperLog.say("http post: Exception from server", e);
				throw new ServerUnavailableException();
			}
		}
	}

	public JSONObject makeRequest(JSONObject originalRequest, Map<String, String> headers/*, String isInitialDoanload*/) throws ServerUnavailableException
	{
		JSONObject pillpopperResponse = null ;
		String actionForFirebaseTracking;
		String _serverName = AppConstants.ConfigParams.getWsPillpopperSecuredBaseURL();
		PillpopperLog.say("Making server request to %s...", _serverName);
		PillpopperLog.say("Debug server error ------ _serverName : "+ _serverName);
		if(URLUtil.isHttpsUrl(_serverName)){
			try
			{
				URL _url = new URL(_serverName);
				URLConnection connection = _url.openConnection();

				initializeSSL();
				actionForFirebaseTracking = originalRequest.getString(PillpopperConstants.KEY_ACTION);

				HttpsURLConnection httpConnection = (HttpsURLConnection) connection;
				JSONArray requestArray = new JSONArray();
				JSONObject request = new JSONObject();
				originalRequest.put("clientVersion", _versionName);
				originalRequest.put("hardwareId", _hardwareId);
				PillpopperTime.marshal(originalRequest, "currentTime", PillpopperTime.now());
				State currState = _globalAppContext.getState(context);
				JSONObject preferences = new JSONObject();
				preferences.put("deviceName",Util.getDeviceMake());
				preferences.put("userData", SharedPreferenceManager.getInstance(context,AppConstants.AUTH_CODE_PREF_NAME).getString(AppConstants.KP_GUID, null));
				preferences.put("osVersion", Util.getOSVersion());
				originalRequest.put(State.JSON_PREFERENCES, preferences);
				originalRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
				originalRequest.put("clientVersion", Util.getAppVersion(context));
				//originalRequest.put("replayId", Util.getRandomGuid());
				originalRequest.put("deviceId", _hardwareId);
				originalRequest.put("language", Util.getLanguage());
				originalRequest.put("appVersion", Util.getAppVersion(context));


				if (currState.getAccountId() != null ) {
					List<User> enabledUserList = RunTimeData.getInstance().getEnabledUsersList();
					if(null!=enabledUserList && !enabledUserList.isEmpty()){
						PillpopperLog.say("-- Enabled Users : " + enabledUserList.size());
						for (User user : enabledUserList) {
							JSONObject userJsonObject = new JSONObject();
							userJsonObject.put("userGUID", user.getUserId());
							if (!originalRequest.get(PillpopperConstants.KEY_ACTION).equals(PillpopperConstants.ACTION_HISTORY_EVENTS)) {
								userJsonObject.put("lastSyncToken",
										Util.getLastSyncTokenValue(FrontController.getInstance(context).getLastSyncTokenForUser(user.getUserId())));
							}
							requestArray.put(userJsonObject);
						}
					}
					originalRequest.put("userId", currState.getAccountId());
					originalRequest.put("proxyUserList", requestArray);
					originalRequest.put("getAllOutput", 1);
				}else{
					// Now wrap the entire thing in a pillpopperRequest object
				}
				request.put("pillpopperRequest", originalRequest);

				httpConnection.setConnectTimeout(45000);
				httpConnection.setDoInput(true);
				httpConnection.setDoOutput(true);
				httpConnection.setInstanceFollowRedirects(false);
				httpConnection.setRequestMethod("POST");
				httpConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());

				if (headers != null && headers.size() > 0) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						httpConnection.addRequestProperty(header.getKey(), header.getValue());
					}
				}

				httpConnection.addRequestProperty("Cookie", getLocalCookies());
				httpConnection.setRequestProperty("Content-Type", "application/json");
				//httpConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");



				OutputStream out = httpConnection.getOutputStream();
				out.write(request.toString().getBytes());
				out.close();
				httpConnection.connect();
				int status = httpConnection.getResponseCode();
				// Firebase event
				Util.logFirebaseEventForAction(context, actionForFirebaseTracking, status);
				if (status == HttpURLConnection.HTTP_OK) {			//	StatusLine status = response.getStatusLine();
					PillpopperLog.say("http post: status code %d", status);
					String resString = TTGUtil.convertResponseTOString(httpConnection.getInputStream());//Util.convertHttpResponseToString(httpConnection.getInputStream());
					PillpopperLog.say("Server request successful");
					if(null!=resString){
						JSONObject jsonResponse = null;
						try {
							jsonResponse = new JSONObject(resString);
							if(null!=jsonResponse)
								pillpopperResponse = jsonResponse.getJSONObject("pillpopperResponse");
						} catch (JSONException e){
							if(null!=jsonResponse){
								JSONObject pillpopperMultiResponse = jsonResponse.getJSONObject("pillpopperMultiResponse");
								JSONArray pillpopperResponseArray = pillpopperMultiResponse.getJSONArray("responseArray");
								JSONObject responseObj = pillpopperResponseArray.getJSONObject(0);
								pillpopperResponse = responseObj.getJSONObject("pillpopperResponse");
							}
							PillpopperLog.say("Response in catch : " + pillpopperResponse, e);
						} catch (Exception e) {
							PillpopperLog.say("Oops, Exception while JSON object from HTTP Response Object", e);
							throw new ServerUnavailableException();
						}
					}
					// check for an error message that would indicate failure
					if (null!= pillpopperResponse && pillpopperResponse.has(_JSON_ERROR_TEXT)) {
						throw new ServerUnavailableException();
					}
				} else{
					if (status != 200 && status !=302) {
						PillpopperLog.say("http post: got non-200 status %d", status);
						throw new ServerUnavailableException();
					}
				}
				return pillpopperResponse;

			} catch (IOException e) {
				PillpopperLog.say("http post: I/O exception: %s", e);
				throw new ServerUnavailableException();
			} catch (JSONException e) {
				PillpopperLog.say("http post: got non-json response from server", e);
				throw new ServerUnavailableException();
			} catch (Exception e){
				PillpopperLog.say("http post: Exception from server", e);
				throw new ServerUnavailableException();
			}
		}else{
			try
			{
				URL _url = new URL(_serverName);
				URLConnection connection = _url.openConnection();
				initializeSSL();
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				JSONArray requestArray = new JSONArray();
				JSONObject request = new JSONObject();

				// Add the standard fields all requests get
				originalRequest.put("clientVersion", _versionName);
				originalRequest.put("hardwareId", _hardwareId);
				//originalRequest.put("partnerId", _globalAppContext.getEditionName());
				PillpopperTime.marshal(originalRequest, "currentTime", PillpopperTime.now());

				// Add preferences
				State currState = _globalAppContext.getState(context);
				//JSONObject marshalledPreferences = currState.getPreferences().marshal();
				JSONObject preferences = new JSONObject();
				preferences.put("deviceName",Util.getDeviceMake());
				preferences.put("userData",  SharedPreferenceManager.getInstance(context,AppConstants.AUTH_CODE_PREF_NAME).getString(AppConstants.KP_GUID, null));
				preferences.put("osVersion", Util.getOSVersion());
				originalRequest.put(State.JSON_PREFERENCES, preferences);
				originalRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
				originalRequest.put("clientVersion", Util.getAppVersion(context));
				//originalRequest.put("replayId", Util.getRandomGuid());
				originalRequest.put("deviceId", _hardwareId);
				originalRequest.put("language", "en-US_US");
				originalRequest.put("appVersion", Util.getAppVersion(context));


				if (currState.getAccountId() != null ) {
//					List<String> enabledUserIds = RunTimeData.getInstance().getSelectedUsersList();
					List<User> enabledUserList = RunTimeData.getInstance().getEnabledUsersList();
					if (null != enabledUserList && enabledUserList.size() > 0) {
						PillpopperLog.say("-- Enabled Users : " + enabledUserList.size());
						for (User user : enabledUserList) {
							JSONObject userJsonObject = new JSONObject();
							userJsonObject.put("userGUID", user.getUserId());
							if (!originalRequest.get(PillpopperConstants.KEY_ACTION).equals(PillpopperConstants.ACTION_HISTORY_EVENTS)) {
								userJsonObject.put("lastSyncToken",
										Util.getLastSyncTokenValue(FrontController.getInstance(context).getLastSyncTokenForUser(user.getUserId())));
							}
							requestArray.put(userJsonObject);
						}
					}
					originalRequest.put("userId", currState.getAccountId());
					originalRequest.put("proxyUserList", requestArray);
					originalRequest.put("getAllOutput", 1);
				}
				request.put("pillpopperRequest", originalRequest);

				httpConnection.setConnectTimeout(45000);
				httpConnection.setDoInput(true);
				httpConnection.setDoOutput(true);
				httpConnection.setInstanceFollowRedirects(false);
				httpConnection.setRequestMethod("POST");
				if (headers != null && headers.size() > 0) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						httpConnection.addRequestProperty(header.getKey(), header.getValue());
					}
				}

				httpConnection.addRequestProperty("Cookie", getLocalCookies());
				httpConnection.setRequestProperty("Content-Type", "application/json");
				OutputStream out = httpConnection.getOutputStream();
				out.write(request.toString().getBytes());
				out.close();
				httpConnection.connect();
				int status = httpConnection.getResponseCode();
				status = httpConnection.getResponseCode();
				if (status == HttpURLConnection.HTTP_OK) {			//	StatusLine status = response.getStatusLine();
					PillpopperLog.say("http post: status code %d", status);
					String resString = TTGUtil.convertResponseTOString(httpConnection.getInputStream());//Util.convertHttpResponseToString(httpConnection.getInputStream());
					PillpopperLog.say("Server request successful");
					if(null!=resString){
						JSONObject jsonResponse = null;
						try {
							jsonResponse = new JSONObject(resString);
							if(null!=jsonResponse)
								pillpopperResponse = jsonResponse.getJSONObject("pillpopperResponse");
						} catch (JSONException e){
							if(null!=jsonResponse){
								JSONObject pillpopperMultiResponse = jsonResponse.getJSONObject("pillpopperMultiResponse");
								JSONArray pillpopperResponseArray = pillpopperMultiResponse.getJSONArray("responseArray");
								JSONObject responseObj = pillpopperResponseArray.getJSONObject(0);
								pillpopperResponse = responseObj.getJSONObject("pillpopperResponse");
							}
							PillpopperLog.say("Response in catch : " + pillpopperResponse, e);
						} catch (Exception e) {
							PillpopperLog.say("Oops, Exception while JSON object from HTTP Response Object", e);
							throw new ServerUnavailableException();
						}
					}
					// check for an error message that would indicate failure
					if (null!= pillpopperResponse && pillpopperResponse.has(_JSON_ERROR_TEXT)) {
						throw new ServerUnavailableException();
					}
				} else {
					if (status != 200 && status !=302) {
						PillpopperLog.say("http post: got non-200 status %d", status);
						throw new ServerUnavailableException();
					}
				}
				return pillpopperResponse;

			} catch (IOException e) {
				PillpopperLog.say("http post: I/O exception: %s", e);
				throw new ServerUnavailableException();
			} catch (Exception e){
				PillpopperLog.say("http post: Exception from server", e);
				throw new ServerUnavailableException();
			}
		}
	}


	public void initializeSSL(){
		createSocketFactory();
	}


	private SocketFactory createSocketFactory() {
		try {
			KeyStore trustStore = KeyStore.getInstance(TTGMobileLibConstants.CERTIFICATE_TYPE);
			trustStore.load(null, null);
			socketFactory = new TTGSSLSocketFactory(trustStore);
			socketFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

		} catch (KeyManagementException e) {
			LoggerUtils.exception("KeyManagementException", e);
		} catch (UnrecoverableKeyException e) {
			LoggerUtils.exception("UnrecoverableKeyException", e);
		} catch (KeyStoreException e) {
			LoggerUtils.exception("KeyStoreException", e);
		} catch (NoSuchAlgorithmException e) {
			LoggerUtils.exception("NoSuchAlgorithmException", e);
		} catch (CertificateException e) {
			LoggerUtils.exception("CertificateException", e);
		} catch (IOException e) {
			LoggerUtils.exception("IOException", e);
		}
		return socketFactory;
	}


	////////////////////////

	public State getState() throws ServerUnavailableException
	{
		JSONObject request = new JSONObject();

		try {
			request.put("action", "GetState");
			ActivationController activationController = ActivationController.getInstance();
			Map<String,String> headers = new HashMap<>();
			if(null!=activationController.getSSOSessionId(context)){
				headers.put("secureToken", activationController.getSSOSessionId(context));
			}
			headers.put("guid", activationController.getUserId(context));
			headers.put("hardwareId", _hardwareId);
			State currState = _globalAppContext.getState(context);
			if (currState.getAccountId() != null) {
				headers.put("userId", currState.getAccountId());
			}
			JSONObject serverResponse = makeRequest(request, headers/*, Preferences.jsonBooleanString(false)*/);
			//JSONObject serverResponse = prepareRequest(request, headers, "GetState");
			return new State(serverResponse, _globalAppContext.getEdition(), _globalAppContext.getFDADrugDatabase(), null);
		} catch (JSONException e) {
			PillpopperLog.say("got json exception setting up request: %s", e);
			throw new ServerUnavailableException();
		} catch (PillpopperParseException e) {
			PillpopperLog.say("got parse exception trying to parse server state!", e);
			throw new ServerUnavailableException();
		}
	}

	public JSONObject makeRequest(JSONObject originalRequest) throws ServerUnavailableException{
		try
		{
			String _serverName = AppConstants.ConfigParams.getWsPillpopperSecuredBaseURL();
			URL _url = new URL(_serverName);
			URLConnection connection = _url.openConnection();

			initializeSSL();

			HttpsURLConnection httpConnection = (HttpsURLConnection) connection;
			httpConnection.setConnectTimeout(45000);
			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);
			httpConnection.setInstanceFollowRedirects(false);
			httpConnection.setRequestMethod("POST");
			httpConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());

			Map<String, String> headers = prepareHeaders();

			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					httpConnection.addRequestProperty(header.getKey(), header.getValue());
				}
			}

			httpConnection.addRequestProperty("Cookie", getLocalCookies());
			httpConnection.setRequestProperty("Content-Type", "application/json");

			OutputStream out = httpConnection.getOutputStream();
			out.write(originalRequest.toString().getBytes());
			out.close();
			httpConnection.connect();
			int status = httpConnection.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {			//	StatusLine status = response.getStatusLine();
				PillpopperLog.say("http post: status code %d", status);
				String resString = TTGUtil.convertResponseTOString(httpConnection.getInputStream());//Util.convertHttpResponseToString(httpConnection.getInputStream());
				PillpopperLog.say("Server request successful");
				if(null!=resString){
					try {
						PillpopperLog.say("Get All Refill Reminder Response : " + resString);
						if(null!=resString) {
							return new JSONObject(resString);
						}
					} catch (JSONException e){
						PillpopperLog.say("Response in catch : " + resString, e);
					} catch (Exception e) {
						PillpopperLog.say("Oops, Exception while JSON object from HTTP Response Object", e);
						throw new ServerUnavailableException();
					}
				}
			}
			return null;

		} catch (IOException e) {
			PillpopperLog.say("http post: I/O exception: %s", e);
			throw new ServerUnavailableException();
		} catch (Exception e){
			PillpopperLog.say("http post: Exception from server", e);
			throw new ServerUnavailableException();
		}

	}

	private Map<String, String> prepareHeaders() {
		ActivationController activationController = ActivationController.getInstance();
		SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
		State currState = _globalAppContext.getState(context);
		Map<String, String> headers = new HashMap<>();
		if (null != activationController.getSSOSessionId(context)) {
			headers.put("secureToken", activationController.getSSOSessionId(context));
		}
		if(RunTimeData.getInstance().getRegistrationResponse() != null) {
			headers.put("guid", mSharedPrefManager.getString(AppConstants.KP_GUID, ""));
		}
		headers.put("hardwareId", UniqueDeviceId.getHardwareId(context));

		if (currState.getAccountId() != null) {
			headers.put("userId", currState.getAccountId());
		}

		headers.put("os", TTGMobileLibConstants.OS);
		headers.put("appVersion", Util.getAppVersion(context));
		headers.put("osVersion", AppConstants.OS_VERSION);

		return headers;
	}

	public long get_replayId()
	{
		return _replayId;
	}

	public void set_replayId(long _replayId)
	{
		this._replayId = _replayId;
	}

	public JSONObject prepareHasStatusUpdateRequestObj(String userId, Context context) {
		JSONObject jsonObj = new JSONObject();

		JSONObject request = new JSONObject();
		SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

		try {
			jsonObj.put("action", "HasStatusUpdates");
			jsonObj.put("clientVersion", _versionName);
			jsonObj.put("userId", userId);

			// preferences
			JSONObject preferences = new JSONObject();
			preferences.put("language", Util.getLanguage());
			preferences.put("deviceName", _drugPrefs.getPreference("deviceName"));
			preferences.put("osVersion", _drugPrefs.getPreference("osVersion"));
			preferences.put("userData",sharedPrefManager.getString(AppConstants.KP_GUID, null));

			jsonObj.put("preferences", preferences);

			jsonObj.put("partnerId", PillpopperConstants.PARTNER_ID);
			jsonObj.put("language", Util.getLanguage());
			jsonObj.put("hardwareId", _hardwareId);
			jsonObj.put("apiVersion", "Version 6.0.4");
			jsonObj.put("deviceToken", "");

			request.put("pillpopperRequest", jsonObj);

			PillpopperLog.say("Prepared HasStatusUpdate json object  : " + request.toString());

		} catch (JSONException e) {
			PillpopperLog.say("Oops!, JSONException While making the HasStatusUpdate API call :", e);
		} catch (Exception e) {
			PillpopperLog.say("Oops!, Exception while making HasStatusUpdate api call", e);
		}
		return request;
	}

	public JSONObject prepareAcknowledgeStatusRequestObj(String userId, String action) {
		JSONObject jsonObj = new JSONObject();

		JSONObject request = new JSONObject();

		try {
			jsonObj.put("deviceId", _hardwareId);
			jsonObj.put("clientVersion", _versionName);
			jsonObj.put("userId", userId);
			jsonObj.put("appVersion", Util.getAppVersion(context));
			jsonObj.put("action", action);
			jsonObj.put("partnerId", _globalAppContext.getEdition());
			jsonObj.put("language", _drugPrefs.getPreference("language"));
			jsonObj.put("hardwareId", _hardwareId);
			jsonObj.put("apiVersion", "Version 6.0.4");

			request.put("pillpopperRequest", jsonObj);

			PillpopperLog.say("Prepared HasStatusUpdate json object  : " + request.toString());

		} catch (JSONException e) {
			PillpopperLog.say("Oops!, JSONException While making the HasStatusUpdate API call : ", e);
		} catch (Exception e) {
			PillpopperLog.say("Oops!, Exception while making HasStatusUpdate api call", e);
		}
		return request;
	}

	public static void clearCookies() {
			CookieManager.getInstance().removeAllCookies(null);
			CookieManager.getInstance().flush();
	}

	public static String getLocalCookies() {
		return CookieManager.getInstance().getCookie("https://" + AppConstants.ConfigParams.getKeepAliveCookieDomain());
	}

	public static void storeCookies(String cookie) {
		CookieManager.getInstance().setCookie("https://" + AppConstants.ConfigParams.getKeepAliveCookieDomain(),cookie);
	}
}
