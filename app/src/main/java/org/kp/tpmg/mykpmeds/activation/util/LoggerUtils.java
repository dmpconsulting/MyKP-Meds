package org.kp.tpmg.mykpmeds.activation.util;

import android.util.Log;

import org.kp.tpmg.mykpmeds.activation.AppConstants;

public class LoggerUtils {
	/*protected static final boolean LOGGING_EXCEPTION = true;
	protected static final boolean LOGGING_WARNING = true;
	protected static final boolean LOGGING_INFO = true;
	protected static final boolean LOGGING_ERROR = true;*/

	public static void warning(String message) {
		if (AppConstants.isLogging()) {
			Log.v("Warning", ""+message);
		}
	}

	public static void info(String message) {
		if (AppConstants.isLogging()) {
			Log.i("Info", ""+message);
		}
	}

	public static void error(String message) {
		if(AppConstants.isLogging()) {
			Log.e("Error", "" + message);
		}
	}

	public static void exception(String message) {
		if(AppConstants.isLogging()) {
			Log.e("Exception", "" + message);
		}
	}

	public static void exception(String message, Exception ex){
		if(AppConstants.isLogging()) {
			Log.e("My KP Meds", " Message - " + message + " Exception - " + ex.getMessage());
		}
	}
}
