package org.kp.tpmg.mykpmeds.activation.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;

import androidx.appcompat.app.AlertDialog;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.fingerprint.FingerprintUtils;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.State;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.kp.tpmg.mykpmeds.activation.AppConstants.ANDROID_DEVICE_MAKE;

public class ActivationUtil {

	public static boolean isNetworkAvailable(Context ctx) {
		return NetworkManager.isDataConnectionAvailable(ctx);
	}

	public static boolean checkNetworkAvailablity(Context ctx) {
		boolean isNetworkPresent = false;
		if (isNetworkPresent = isNetworkAvailable(ctx)) {
			return isNetworkPresent;
		} else {
			GenericAlertDialog alertDialog = new GenericAlertDialog(ctx,
					ctx.getString(R.string.data_unavailable_title),
					ctx.getString(R.string.alert_network_error),
					ctx.getString(R.string.ok_text), (dialog, which) -> {
						RunTimeData.getInstance().setClickFlg(false);
						RunTimeData.getInstance().setAlertDisplayedFlg(
								false);
						dialog.dismiss();

					}, null, null);

			alertDialog.showDialog();
			return isNetworkPresent;
		}
	}

	public static void hideKeyboard(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static String getSecretKey(Context context) {
		String androidID = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		String model = Build.MODEL;
		String manufacturer = Build.MANUFACTURER;
		if (androidID == null) {
			androidID = "";
		}
		String combinedId = manufacturer + model + androidID;


		// Apply SHA Encryption
		//String modifiedKey = decideSecretkey(SECRET_KEY, context);
		return applyHashing(combinedId);
	}

	/*private static String decideSecretkey(String secretKey, Context context) {
		String key;
		if(null!=secretKey){
			key = secretKey;
		}else{
			SharedPreferenceManager sharedPref =  SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
			key = sharedPref.getString(secretKey, ""); 
		}
		return key;
	}*/

	@SuppressLint("DefaultLocale")
	public static String applyHashing(String combinedId) {
		LoggerUtils.info("TAG Key Applying : " + combinedId);
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			if (messageDigest != null) {
				messageDigest.update(combinedId.getBytes(), 0,
						combinedId.length());
				byte shaData[] = messageDigest.digest();
				// create a hex string
				StringBuilder uniqueID = new StringBuilder();
				for (int i = 0; i < shaData.length; i++) {
					int bitData = (0xFF & shaData[i]);
					// if it is a single digit, make sure it have 0 in front
					// (proper padding)
					if (bitData <= 0xF) {
						uniqueID.append("0");
					}
					// add number to string
					uniqueID.append(Integer.toHexString(bitData));
					// hex string to uppercase
				}
				LoggerUtils.info("TAG Key Returning  : " + uniqueID.toString().toUpperCase());
				return uniqueID.toString().toUpperCase();
			}
		} catch (NoSuchAlgorithmException e) {
			LoggerUtils.exception(e.getMessage());
		}
		return null;
	}

	public static String getDeviceId(Context context) {
		String finalId = null;
		try{
			if(Util.isEmulator()){
				finalId = Build.BRAND;
				LoggerUtils.info("Application Running on Emulator");
			}else{
				String androidID = getAndroidID(context);
				String wifiMac = getWifiMacAddress(context);
				String bluetoothMac = getBluetoothMac(context);
				String combinedId = ((androidID == null ? "" : androidID)
						+ (wifiMac == null ? "" : wifiMac) + (bluetoothMac == null ? ""
						: bluetoothMac));

				// Apply SHA Encryption
				finalId = applyHashing(combinedId);

				if (finalId == null) {
					finalId = combinedId.length() > 0 ? combinedId : null;
				}
			}
		}catch(Exception e){
			LoggerUtils.info("No DeviceId found");
		}
		return finalId;
	}

	public static String getAndroidID(Context context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	public static String getBluetoothMac(Context context) {
		BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
		m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		return m_BluetoothAdapter.getAddress();

	}

	public static String getWifiMacAddress(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wm.getConnectionInfo().getMacAddress();
	}

	/**
	 * @param context Application context
	 * @return TRUE if the device is having call option else FALSE.
	 */
	public static boolean isCallOptionAvailable(Context context) {
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
			String alertMsg = "This Feature is not supported in your device.";
			alertDialog.setMessage(alertMsg);
			alertDialog.setCancelable(false);
			alertDialog.setPositiveButton(context.getString(R.string.ok_text),
					(dialog, which) -> {
						// cancel the dialog
						RunTimeData.getInstance().setAlertDisplayedFlg(
								false);
						dialog.dismiss();
						RunTimeData.getInstance().setClickFlg(false);
					});
			RunTimeData.getInstance().setAlertDisplayedFlg(true);
			AlertDialog dialog = alertDialog.create();
			RunTimeData.getInstance().setAlertDialogInstance(dialog);
			dialog.show();
		} else {
			return true;
		}
		return false;
	}

	public static String getAppVersion(Context context) {
		String version = "";
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			LoggerUtils.exception(e.getMessage());
		}
		return version;

	}

	public static Intent callMailClient(String recipientMailId, String subject,
			String body) {

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc822");
		emailIntent.setAction(Intent.ACTION_SENDTO);
		emailIntent.setData(Uri.parse("mailto:" + ""));
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { recipientMailId });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				Html.fromHtml(body));
		return emailIntent;
	}
	public static String handleStrNull(String resultStr) {
		if (null != resultStr && resultStr.length() > 0
				&& !("null").equalsIgnoreCase(resultStr))
			return resultStr;
		else
			return "";
	}
	public static Map<String,String> getBaseParams(Context context){
		String[] params = AppConstants.URL_BASE_PARAMETERS.split("&");
		Map<String,String> paramMap = new HashMap<>();
		for (String param : params) {
			String[] split = param.split("=");
			paramMap.put(split[0], split[1]);
		}
		paramMap.put("appVersion", getAppVersion(context));
		return paramMap;
	}

	public static Map<String,String> getGuideHeaders(Context context){
		Map<String,String> headersMap = getBaseParams(context);
		headersMap.put("deviceId", UniqueDeviceId.getHardwareId(context));
		return headersMap;
	}


	public static void createObssoCookie(String ssoSession, Context ctx) {
		try {
			final CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookies(null);
			String cookieDomain = "";
			String cookieValue ="";
			if (!(AppConstants.ConfigParams.getKeepAliveCookieDomain().startsWith("https://") || AppConstants.ConfigParams.getKeepAliveCookieDomain().startsWith("http://"))) {
				cookieDomain = "https://" +AppConstants.ConfigParams.getKeepAliveCookieDomain();
			}
			cookieValue = AppConstants.ConfigParams.getKeepAliveCookieName() + "=" + URLEncoder.encode(ssoSession,"UTF-8");
			cookieManager.setAcceptCookie(true);


			if(cookieDomain.length()>0 && cookieValue.length()>0){
				cookieManager.setCookie(cookieDomain, cookieValue);
				cookieManager.flush();
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					LoggerUtils.info("Exception in delay....");
				}
			}
			LoggerUtils.info("createObssoCookie is done..........");

		} catch (Exception e) {
			LoggerUtils.info("Exception While creating the OBSSOCookie");
		}
	}

	public static void syncCookieManagerCookies(String ssoSession, Context ctx) {
		/*CookieStore cookieStore = GenericHttpClient.getCookieStore();
		try {

			String cookieString = CookieManager.getInstance().getCookie("https://"+AppConstants.ConfigParams.getKeepAliveCookieDomain());

			if(cookieString!=null){
				String[] keyValueSets = cookieString.split(";");
				if(keyValueSets.length>0){
					for(String cookie : keyValueSets){
						String[] keyValue = cookie.split("=");
						if(keyValue!=null && keyValue.length>0 &&  AppConstants.ConfigParams.getKeepAliveCookieName().equalsIgnoreCase(keyValue[0].trim())){
							String value = "";
							if(keyValue.length>1){
								value = keyValue[1];
							}
							BasicClientCookie c = new BasicClientCookie(keyValue[0], URLEncoder.encode(value,"utf-8"));
							c.setPath(AppConstants.ConfigParams.getKeepAliveCookiePath());
							c.setDomain(AppConstants.ConfigParams.getKeepAliveCookieDomain());
							c.setSecure(true);
							cookieStore.addCookie(c);
						}
					}
				}else{
					ActivationUtil.createObssoCookie(ssoSession, ctx);
					BasicClientCookie c = new BasicClientCookie(AppConstants.ConfigParams.getKeepAliveCookieName(), URLEncoder.encode(ssoSession,"utf-8"));
					c.setPath(AppConstants.ConfigParams.getKeepAliveCookiePath());
					c.setDomain(AppConstants.ConfigParams.getKeepAliveCookieDomain());
					c.setSecure(true);
					cookieStore.addCookie(c);
				}
			}else{
				ActivationUtil.createObssoCookie(ssoSession, ctx);
				BasicClientCookie c = new BasicClientCookie(AppConstants.ConfigParams.getKeepAliveCookieName(), URLEncoder.encode(ssoSession,"utf-8"));
				c.setPath(AppConstants.ConfigParams.getKeepAliveCookiePath());
				c.setDomain(AppConstants.ConfigParams.getKeepAliveCookieDomain());
				c.setSecure(true);
				cookieStore.addCookie(c);
			}
		} catch (UnsupportedEncodingException e) {
			LoggerUtils.exception(e.getMessage());
		}*/
	}
	
	public static Map<String,String> buildHeaders(Context context){
		Map<String,String> headers = new HashMap<>();
		if(null!= ActivationController.getInstance().getSSOSessionId(context)){
			headers.put("ssoSessionId", ActivationController.getInstance().getSSOSessionId(context));
		}
		headers.put("guid", ActivationController.getInstance().getUserId(context));
		headers.put("os", TTGMobileLibConstants.OS);
		headers.put("appVersion", Util.getAppVersion(context));
		headers.put("osVersion", AppConstants.OS_VERSION);
		return headers;
		
	}
	
	public static long generateUUID(){
		return UUID.randomUUID().getMostSignificantBits();
	}

	public static Typeface setFontStyle(Context mContext, String textStyle) {
		return Typeface.createFromAsset(mContext.getAssets(), "fonts/" + textStyle);
	}

	public static int getColorWrapper(Context context, int id) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return context.getColor(id);
		} else {
			//noinspection deprecation
			return context.getResources().getColor(id);
		}
	}

	public static void resetDevice(Context context) {
		FingerprintUtils.resetAndPurgeKeyStore(context);
		ActivationController.getInstance().clearWelcomeScreenDisplayCounter(context);
		SharedPreferenceManager preferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
		preferenceManager.putLong(AppConstants.WELCOME_SCREEN_DISPLAY_COUNTER, 0l, false);
		State.deletePersistentState(context);
		FrontController.getInstance(context).clearDatabase();
		RefillReminderController.getInstance(context).clearDBTable();
		RefillReminderNotificationUtil.getInstance(context).cancelAllPendingRefillReminders(context);


		try {
			if (Util.deleteDirectory(Util.getImageCacheDir(context, false))) {
				PillpopperLog.say("Deleted the image directory after app reset.");
			} else {
				PillpopperLog.say("ERROR: App Reset: Unable to delete the image directory.");
			}
		} catch (IOException e) {
			PillpopperLog.say("Util -- deleteDirectory -- Unable to delete directory -- " , e);
		}
	}
}