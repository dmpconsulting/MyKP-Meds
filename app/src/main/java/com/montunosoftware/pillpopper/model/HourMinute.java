package com.montunosoftware.pillpopper.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.WDHM;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

// Class which holds an hour and minute.
@SuppressLint("Assert")
public class HourMinute
{
	private int _hour = 0;
	private int _minute = 0;
	private int _second = 0;

	private void _ParseHHMM(int HHMM)
	{
		_minute = HHMM % 100;
		_hour = HHMM / 100;
	}

	public HourMinute(int HHMM)
	{
		assert(HHMM >= 0);
		_ParseHHMM(HHMM);
	}
	
	public HourMinute(int hour, int minute)
	{
		_hour = hour;
		_minute = minute;
	}
	
	public HourMinute(int hour, int minute, int second)
	{
		_hour = hour;
		_minute = minute;
		_second = second;
	}
	
	public HourMinute(WDHM wdhm)
	{
		_hour = (int) wdhm.get(WDHM.HOURS);
		_minute = (int) wdhm.get(WDHM.MINUTES);
	}
	
	
	public static HourMinute parseJSON(JSONObject jsonState, String key)
	{
		long longVal = Util.parseJSONNonnegativeLong(jsonState,  key);
		
		if (longVal < 0)
			return null;
		else
			return new HourMinute((int) longVal);
	}
	
	public static long getLocalTimezoneSecs()
	{
		TimeZone tz = TimeZone.getDefault();
		return tz.getOffset(GregorianCalendar.getInstance().getTimeInMillis()) / 1000;
	}
	
	public HourMinute addSeconds(long secondsToAdd)
	{
		long seconds = new WDHM(0, 0, _hour, _minute).toSeconds();
		
		seconds += secondsToAdd + WDHM.SecPerDay;
		seconds %= WDHM.SecPerDay;
		
		return new HourMinute(new WDHM(seconds));
	}
	
	public HourMinute convertGmtToLocal()
	{
		return addSeconds(getLocalTimezoneSecs());
	}
	
	public HourMinute convertLocalToGmt()
	{
		return addSeconds(-getLocalTimezoneSecs());
	}
	
	///////////////////////////////
	
	public int marshal()
	{
		return _hour * 100 + _minute;
	}
	
	public String toString()
	{
		return String.format(Locale.US,"%02d:%02d", _hour, _minute);
	}
	
	public String to12HourString(){
		
		boolean pmFlag = false;
		
		StringBuilder builder = new StringBuilder();
		if (_hour<13) {
			if (_hour==0) {
				builder.append(String.format("%02d", 12));
			}else {
				builder.append(String.format("%02d", _hour));
			}
			if (_hour>=12) {
				pmFlag = true;
			}
		}else {
			builder.append(String.format("%02d", (_hour-12)));
			pmFlag=true;
		}
		builder.append(":");
		builder.append(String.format("%02d", _minute));
		builder.append(" ").append( pmFlag ? Util.getSystemPMFormat() : Util.getSystemAMFormat());

		
		return builder.toString();
	}

	public int getHour()
	{
		return _hour;
	}
	
	public int getMinute()
	{
		return _minute;
	}

	public int getSecond()
	{
		return _second;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	private String _getLocalizedString(Context context)
	{
		Date d = new Date();
		d.setHours(_hour);
		d.setMinutes(_minute);
		
		SimpleDateFormat simpleDateFormat;
		if(DateFormat.is24HourFormat(context)){
			simpleDateFormat = new SimpleDateFormat("HH:mm");
		}else{
			simpleDateFormat = new SimpleDateFormat("h:mm a");
		}
		return simpleDateFormat.format(d);
		//return DateFormat.getTimeFormat(context).format(d);
	}
	
	public boolean isEqual(HourMinute rhs)
	{
		return _hour == rhs._hour && _minute == rhs._minute;
	}
	
	// Similar to the above, but a static method that can handle nulls
	public static String getLocalizedString(HourMinute hourMinute, Context context)
	{
		if (hourMinute == null) {
			return context.getString(R.string._not_set);
		} else {
			return hourMinute._getLocalizedString(context);
		}
	}
	
	public static class ConvertGmtToLocalComparator implements Comparator<HourMinute>
	{
		@Override
		public int compare(HourMinute lhsGmt, HourMinute rhsGmt)
		{

			if (lhsGmt.getHour() != rhsGmt.getHour())
				return lhsGmt.getHour() - rhsGmt.getHour();
			
			return lhsGmt.getMinute() - rhsGmt.getMinute();
		}
	}
	
	public boolean isBefore(HourMinute hourMinute){
		if (_hour<hourMinute._hour) {
			return true;
		}else if (_hour==hourMinute._hour) {
			return _minute<hourMinute._minute;
		}else
			return false;
	}
	
	public boolean isAfter(HourMinute hourMinute){
		if (_hour>hourMinute._hour) {
			return true;
		}else if (_hour==hourMinute._hour) {
			return _minute>hourMinute._minute;
		}else
			return false;
	}
}
