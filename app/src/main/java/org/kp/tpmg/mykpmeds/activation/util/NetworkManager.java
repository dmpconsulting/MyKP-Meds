package org.kp.tpmg.mykpmeds.activation.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkManager {

	/**
	 * This method is use to check WiFi enable/ Disable functionality
	 * uses-permission required : ACCESS_NETWORK_STATE
	 * 
	 * @param context
	 * @return true if WiFi enabled else false
	 */
//	public static boolean isWiFiEnabled(Context context) {
//		ConnectivityManager connectivityManager = (ConnectivityManager) context
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo networkInfo = null;
//		if (connectivityManager != null) {
//			networkInfo = connectivityManager
//					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//		}
//		return networkInfo == null ? false : networkInfo.isConnected();
//	}

	

	/**
	 * This method is use to check WiMAX enable/ Disable functionality
	 * uses-permission required : ACCESS_NETWORK_STATE
	 * 
	 * @param context
	 * @return true if WiMAX enabled else false
	 */
//	public static boolean isWiMAXEnabled(Context context) {
//		ConnectivityManager connectivityManager = (ConnectivityManager) context
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo networkInfo = null;
//		if (connectivityManager != null) {
//			networkInfo = connectivityManager
//					.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
//		}
//		return networkInfo == null ? false : networkInfo.isConnected();
//	}

	/**
	 * This method is use to check 3G enable/ Disable functionality
	 * uses-permission required : ACCESS_NETWORK_STATE
	 * 
	 * @param context
	 * @return true if 3G enabled else false
	 */
//	public static boolean is3GEnabled(Context context) {
//		ConnectivityManager connectivityManager = (ConnectivityManager) context
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo networkInfo = null;
//		if (connectivityManager != null) {
//			networkInfo = connectivityManager
//					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//		}
//		return networkInfo == null ? false : networkInfo.isConnected();
//	}

	/**
	 * This method is use to check GPRS enable/ Disable functionality
	 * uses-permission required : ACCESS_NETWORK_STATE
	 * 
	 * @param context application context
	 * @return true if GPRS enabled else false
	 */
	public static boolean isGPRSEnabled(Context context) {
		TelephonyManager mgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = mgr.getNetworkType();

		return networkType == TelephonyManager.NETWORK_TYPE_GPRS;

	}

	
	/**
	 * 
	 * Check for any type of data connection
	 * @param context application context
	 * @return true if any of 3G, GPRS, WiFi or WiMAX is available else false
	 */

	public static boolean isDataConnectionAvailable(Context context) {
		if (null != context) {
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		        // if no network is available networkInfo will be null
		        // otherwise check if we are connected
		        if (networkInfo != null && networkInfo.isConnected()) {
		            return true;
		        }
		}
		return false;
	}
	
}
