package com.montunosoftware.pillpopper.android.util;

import android.content.Context;

import com.montunosoftware.mymeds.R;

public class WDHM
{
	public static final int WEEKS = 1;
	public static final int DAYS = 2;
	public static final int HOURS = 3;
	public static final int MINUTES = 4;

	public static final long MilliPerSec = 1000;
	public static final long SecPerMinute = 60;
	public static final long SecPerHour = SecPerMinute * 60;
	public static final long SecPerDay = SecPerHour * 24;
	public static final long SecPerWeek = SecPerDay * 7;

	private long _weeks = 0;
	private long _days = 0;
	private long _hours = 0;
	private long _minutes = 0;

	public WDHM(long seconds)
	{
		_setSeconds(seconds);
	}

	public WDHM(long weeks, long days, long hours, long minutes)
	{
		_weeks = weeks;
		_days = days;
		_hours = hours;
		_minutes = minutes;
	}

	private void _setSeconds(long seconds)
	{
		_weeks = seconds / SecPerWeek;
		seconds -= _weeks * SecPerWeek;

		_days = seconds / SecPerDay;
		seconds -= _days * SecPerDay;

		_hours = seconds / SecPerHour;
		seconds -= _hours * SecPerHour;

		_minutes = seconds / SecPerMinute;
	}


	public void set(int field, long value)
	{
		switch (field) {
		case WEEKS:
			_weeks = value;
			break;
		case DAYS:
			_days = value;
			break;
		case HOURS:
			_hours = value;
			break;
		case MINUTES:
			_minutes = value;
			break;
		}
	}

	public long get(int field)
	{
		switch (field) {
		case WEEKS:
			return _weeks;
		case DAYS:
			return _days;
		case HOURS:
			return _hours;
		case MINUTES:
			return _minutes;
		default:
			return 0;
		}
	}

	public long toSeconds()
	{
		return
				SecPerWeek* _weeks +
				SecPerDay * _days +
				SecPerHour * _hours +
				SecPerMinute * _minutes;
	}

	public long toMillies(){

		return (SecPerWeek * _weeks +
				SecPerDay * _days +
				SecPerHour * _hours +
				SecPerMinute * _minutes) *MilliPerSec;
	}

	// Returns a normalized string
	public String toLocalizedString(Context context, int zeroStringId)
	{
		// Normalize, then generate string
		return new WDHM(toSeconds()).toUnNormalizedLocalizedString(context, zeroStringId);
	}

	private String toUnNormalizedLocalizedString(Context context, int zeroStringId)
	{
		StringBuilder s = new StringBuilder();

		if (_weeks == 0 && _days == 0 && _hours == 0 && _minutes == 0) {
			return context.getString(zeroStringId);
		}

		if (_weeks == 1) {
			s.append(String.format("1 %s", context.getString(R.string._week)));
		} else if (_weeks > 1) {
			s.append(String.format("%d %s", _weeks, context.getString(R.string._weeks)));
		}

		if (_days > 0) {
			if (s.length() > 0) {
				s.append(", ");
			}

			if (_days == 1) {
				s.append(String.format("1 %s", context.getString(R.string._day)));
			} else {
				s.append(String.format("%d %s", _days, context.getString(R.string._days)));
			}
		}

		if (_hours > 0) {
			if (s.length() > 0) {
				s.append(", ");
			}

			if (_hours == 1) {
				s.append(String.format("1 %s", context.getString(R.string._hour)));
			} else {
				s.append(String.format("%d %s", _hours, context.getString(R.string._hours)));
			}
		}

		if (_minutes > 0) {
			if (s.length() > 0) {
				s.append(", ");
			}

			if (_minutes == 1) {
				s.append(String.format("1 %s", context.getString(R.string._minute)));
			} else {
				s.append(String.format("%d %s", _minutes, context.getString(R.string._minutes)));
			}
		}

		return s.toString();
	}

}
