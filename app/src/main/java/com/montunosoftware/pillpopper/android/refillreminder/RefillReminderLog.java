package com.montunosoftware.pillpopper.android.refillreminder;

import android.util.Log;

public class RefillReminderLog
{

	public static void exception(String message) {
		if(RefillReminderConstants.IS_LOGGING){
			Log.e("My KP Meds", message);
		}
	}

	public static void warning(String message) {
		if(RefillReminderConstants.IS_LOGGING){
			Log.w("My KP Meds", message);
		}
	}

	public static void say(String s)
	{
		if(RefillReminderConstants.IS_LOGGING){
			Log.v("My KP Meds", s);
		}
	}

	public static void say(Exception exception){
		if(RefillReminderConstants.IS_LOGGING) {
			Log.e("My KP Meds", " Exception - " + exception.getMessage());
		}
	}

	public static void say(String message, Exception exception){
		if(RefillReminderConstants.IS_LOGGING) {
			Log.e("My KP Meds", "Message - " + message + " Exception - " + exception.getMessage());
		}
	}
}