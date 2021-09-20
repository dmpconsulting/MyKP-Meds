package com.montunosoftware.pillpopper.model;

import android.content.Context;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class TimeList implements Iterable<HourMinute>
{
	private List<HourMinute> _doseTimes;
	private List<HourMinute> _doseTimes_copy;

	public TimeList copy()
	{
		TimeList clone = new TimeList();
		for (HourMinute hm : _doseTimes) {
			clone.addTime(hm);
		}
		return clone;
	}

	public TimeList _copy()
	{
		TimeList clone = new TimeList();
		for (HourMinute hm : _doseTimes_copy) {
			clone.addTime(hm);
		}
		return clone;
	}

	private void _init()
	{
		_doseTimes = new ArrayList<>();
		_doseTimes_copy = new ArrayList<>();
	}

	public TimeList()
	{
		_init();
	}

	private void _sort()
	{
		Collections.sort(_doseTimes, new HourMinute.ConvertGmtToLocalComparator());
	}

	public TimeList(JSONArray jsonSchedule)
	{
		_init();
		int n = jsonSchedule.length();
		for (int i = 0; i < n; i++) {
			long HHMM = -1;
			try {
				HHMM = jsonSchedule.getLong(i);
			} catch (JSONException e) {
				PillpopperLog.say("got exception parsing schedule time %d", i);
			}

			if (HHMM >= 0) {
				_doseTimes.add(new HourMinute((int) HHMM));
				_doseTimes_copy.add(new HourMinute((int) HHMM));
			}
		}
		_sort();
	}

	public TimeList(String interval)
	{
		_init();
		SimpleDateFormat formatter = new SimpleDateFormat("h:mm a",Locale.US);
		Date date;
		try {
			date = formatter.parse(interval);
			if (date.getTime() >= 0) {
				_doseTimes.add(new HourMinute((int) date.getTime()));
				_doseTimes_copy.add(new HourMinute((int) date.getTime()));
			}
		} catch (ParseException e) {
			PillpopperLog.say("Oops!, ParseException" + e.getMessage());
		} catch(Exception e){
			  PillpopperLog.say("Oops!, Exception" + e.getMessage());
		}
	}

	public JSONArray marshal()
	{
		JSONArray jsonTimes = new JSONArray();

		for (HourMinute hm : _doseTimes) {
			jsonTimes.put(hm.marshal());
		}

		return jsonTimes;
	}

	public HourMinute getTime(int index)
	{
		return _doseTimes.get(index);
	}

	public void modifyOrDeleteTime(int index, HourMinute newTime)
	{
		_doseTimes.clear();
		/*
		 * _doseTimes.remove(index); if (newTime != null) {
		 * _doseTimes.add(index, newTime); _sort(); }
		 */

	}

	public void addTime(HourMinute newTime)
	{
		_doseTimes.add(newTime);
		_doseTimes_copy.add(newTime);
		_sort();
	}

	public int length()
	{
		return _doseTimes.size();
	}

	public String toString()
	{
		StringBuilder retval = new StringBuilder();

		int n = _doseTimes.size();
		for (int i = 0; i < n; i++) {
			retval.append(_doseTimes.get(i).toString());
			if (i < n - 1) {
				retval.append(",");
			}
		}

		return retval.toString();
	}

	public String getLocalizedLocalTimeString(Context context)
	{
		StringBuilder retval = new StringBuilder();

		int n = _doseTimes.size();

		if (n == 0) {
			return context.getString(R.string._none);
		} else {
			for (int i = 0; i < n; i++) {
				if (i > 0) {
					retval.append(", ");
				}

				retval.append(HourMinute.getLocalizedString(_doseTimes.get(i), context));
			}
		}

		return retval.toString();
	}

	@Override
	public Iterator<HourMinute> iterator()
	{
		return _doseTimes.iterator();
	}

	public void clearTimesList()
	{
		_doseTimes_copy.clear();
		_doseTimes.clear();
	}

	public List<HourMinute> get_doseTimes_copy()
	{
		return _doseTimes_copy;
	}

}
