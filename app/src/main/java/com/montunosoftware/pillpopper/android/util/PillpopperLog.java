package com.montunosoftware.pillpopper.android.util;

import android.util.Log;

import java.util.Locale;

public class PillpopperLog
{

	public static void exception(String message) {
		if(PillpopperConstants.IS_LOGGING){
			Log.e("My KP Meds", message);
		}
	}

	public static void warning(String message) {
		if(PillpopperConstants.IS_LOGGING){
			Log.w("My KP Meds", message);
		}
	}

	public static void say(String s)
	{
		if(PillpopperConstants.IS_LOGGING){
			Log.v("My KP Meds", s);
		}
	}

	public static void say(String format, Object... args)
	{
		if(PillpopperConstants.IS_LOGGING) {
			say(String.format(Locale.US, format, args));
		}
	}

	public static void say(Exception exception){
		if(PillpopperConstants.IS_LOGGING) {
			Log.e("My KP Meds", " Exception - " + exception.getMessage());
		}
	}

	public static void say(String message, Exception exception){
		if(PillpopperConstants.IS_LOGGING) {
			Log.e("My KP Meds", "Message - " + message + " Exception - " + exception.getMessage());
		}
	}
}