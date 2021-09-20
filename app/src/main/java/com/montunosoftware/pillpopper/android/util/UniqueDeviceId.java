package com.montunosoftware.pillpopper.android.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


public class UniqueDeviceId
{
	//
	// http://www.pocketmagic.net/?p=1662
	// http://stackoverflow.com/questions/2322234/how-to-find-serial-number-of-android-device
	//
	// Secure.ANDROID_ID is supposedly the API provided for a device ID, but for some devices
	// it returns null.  And a buggy build of Android 2.2 returned the same ID for all devices.
	// The guy at pocketmagic suggested that, in practice, a combination of all the Build.*
	// constants should be unique.  He also suggested using a hash, but only on the Build.*
	// constants after most of their entropy had been removed.
	// My (Jeremy's) solution is to use a hash and seed it with the complete text of
	// all the Build.* constants *and* the android ID if available.
	// This seems a lot more likely to be unique.
	private static class _Digest
	{
		MessageDigest _m;
		
		public _Digest() throws NoSuchAlgorithmException
		{
			_m = MessageDigest.getInstance("SHA-256");
		}
		
		public void add(String s)
		{
			if (s != null) {
				_m.update(s.getBytes());
			}
		}
		
		public String getDigest()
		{
			StringBuilder sb = new StringBuilder();
			byte data[] = _m.digest();

			for (byte datum : data) {
				sb.append(String.format("%02x", datum));
			}
			
			return sb.toString();
		}
	}
	
	public static String _generateHardwareId(Context context)
	{
		String retval;
		
		try {
			_Digest d = new _Digest();
			d.add(Build.BOARD);
			d.add(Build.BRAND);
			d.add(Build.CPU_ABI);
			d.add(Build.DEVICE);
			d.add(Build.DISPLAY);
			d.add(Build.FINGERPRINT);
			d.add(Build.HOST);
			d.add(Build.ID);
			d.add(Build.MANUFACTURER);
			d.add(Build.MODEL);
			d.add(Build.PRODUCT);
			d.add(Build.TAGS);
			d.add(Build.TYPE);
			d.add(Build.USER);
			d.add(Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
			retval = d.getDigest();
		} catch (NoSuchAlgorithmException e) {
			PillpopperLog.say("warning: could not generate a good device id");
			retval = "00000000";
		}
		
		PillpopperLog.say("Generated device ID %s", retval);
		return retval;
	}
	
	private static String _cachedId = null;

	public synchronized static void init(Context context)
	{
		if (_cachedId == null) {
			if(Util.isEmulator()) {
				_cachedId = UUID.randomUUID().toString();
			}else{
				_cachedId = _generateHardwareId(context);
			}
		}
	}

	public static String getHardwareId(Context context)
	{
		init(context);
		return _cachedId;
	}
	
	// This feels like a sin. Hiding a global here.
	public static String getHardwareId()
	{
		if (_cachedId == null) {
			throw new Error("Trying to get hardware id before initialization");
		}
		
		return _cachedId;
	}
}
