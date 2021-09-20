package com.montunosoftware.pillpopper.model;

import android.content.Context;
import android.text.format.DateFormat;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.WDHM;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class PillpopperTime implements Comparable<PillpopperTime>
{
	private long _gmtSeconds = 0;
	
	
	//// constructors 
	
	public PillpopperTime(long gmtSeconds)
	{
		_gmtSeconds = gmtSeconds;
	}
	
	public PillpopperTime(PillpopperTime pillpopperTime)
	{
		_gmtSeconds = pillpopperTime._gmtSeconds;
	}
	
	public PillpopperTime(PillpopperTime startingTime, long addedSeconds)
	{
		_gmtSeconds = startingTime._gmtSeconds + addedSeconds;
	}
	
	public PillpopperTime(Calendar calendar)
	{
		_gmtSeconds = calendar.getTimeInMillis() / 1000;
	}
	
	public static PillpopperTime now()
	{
		Calendar _now = GregorianCalendar.getInstance();
		return new PillpopperTime(_now.getTimeInMillis() / 1000);
	}
	
	public static PillpopperTime epoch()
	{
		return new PillpopperTime(0);
	}

	
	//// marshalling / unmarshalling
	
	public static PillpopperTime parseJSON(JSONObject jsonObject, String key)
	{
		long longVal = Util.parseJSONNonnegativeLong(jsonObject, key);

		if (longVal < 0) {
			return null;
		} else {
			return new PillpopperTime(longVal);
		}
	}

	public static PillpopperTime parseJSON(String key)
	{
		long longVal = Util.parseJSONNonnegativeLong(key);

		if (longVal < 0) {
			return null;
		} else {
			return new PillpopperTime(longVal);
		}
	}
	
	public static PillpopperTime parseJSONBackCompat(JSONObject jsonObject, String oldKey, String currKey)
	{
		PillpopperTime retval = parseJSON(jsonObject, oldKey);
		
		if (retval == null)
			return parseJSON(jsonObject, currKey);
		else
			return retval;
	}

	
	public static void marshal(JSONObject jsonObject, String key, PillpopperTime pillpopperTime) throws JSONException
	{
		if (pillpopperTime == null) {
			jsonObject.put(key, -1);
		} else {
			jsonObject.put(key, pillpopperTime._gmtSeconds);
		}
	}


	public static long marshal(PillpopperTime pillpopperTime) throws JSONException
	{
		if (pillpopperTime == null) {
			return -1;
		} else {
			return pillpopperTime._gmtSeconds;
		}
	}

	public static List<PillpopperTime> parseStringArray(String s)
	{
		List<PillpopperTime> retval = new ArrayList<>();
		
		if (s != null) {
			String[] tokens = s.split(",");
			
			for (String token: tokens) {
				try {
					long l = Long.parseLong(token);
					
					if (l > 0) {
						retval.add(new PillpopperTime(l));
					}
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
		return retval;
	}

	
	
	//// accessors

	public long getGmtSeconds()
	{
		return _gmtSeconds;
	}
	
	public long getGmtMilliseconds()
	{
		return _gmtSeconds * 1000;
	}
	
	private Calendar _getLocalCalendar()
	{
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(_gmtSeconds * 1000);
		return c;
	}
	
	
	
	////  formatters

	// Returns:
	//   The nullDateStringId (e.g. "never") if null
	//   "4:30pm" if the date is today
	//   "tomorrow 4pm" if it's tomorrow
	//   "yesterday 2:30pm" if it's yesterday
	//   "Mon 2:30pm" if the date is within 6 days of today
	//   "Feb 12, 2012" if the date is more than 6 days away
	public static String getLocalizedAdaptiveString(PillpopperTime pillpopperTime, int nullDateStringId, Context context)
	{
		if (pillpopperTime == null) {
			return context.getString(nullDateStringId);
		}
		
		// Find the number of days away the target date is
		PillpopperDay today = PillpopperDay.today();
		long daysAway = pillpopperTime.getLocalDay().daysAfter(today);
		
		long millis = pillpopperTime.getGmtMilliseconds();

		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a",Locale.US);
		
		if (daysAway == 0) {
			// If the date is today, just print the time
			//return HourMinute.getLocalizedString(pillpopperTime.getLocalHourMinute(), context);
			return sdf.format(millis);
		} else if (daysAway == 1) {
			// If tomorrow, say tomorrow
			return String.format("%s %s",
					context.getString(R.string._tomorrow).toLowerCase(Locale.getDefault()),
					//DateFormat.getTimeFormat(context).format(millis));
					sdf.format(millis));
		} else if (daysAway == -1) {
			// If yesterday, say yesterday
			return String.format("%s %s",
					context.getString(R.string._yesterday).toLowerCase(Locale.getDefault()),
					//DateFormat.getTimeFormat(context).format(millis));
					sdf.format(millis));
		} else if (Math.abs(daysAway) <= 6) {
			// If the date is this week, print just the name of the day and the time
			return String.format("%s %s",
					DateFormat.format("E", millis),
					//DateFormat.getTimeFormat(context).format(millis));
					sdf.format(millis));
		} else {
			// It's more than a week away: print date only, not the time
			return DateFormat.getMediumDateFormat(context).format(millis);
		}
	}


	public static String getLocalizedString(PillpopperTime pillpopperTime, int nullDateStringId, Context context)
	{
		if (pillpopperTime == null) {
			return context.getString(nullDateStringId);
		}
		
		long millis = pillpopperTime._gmtSeconds * 1000;

		return String.format("%s %s",
				DateFormat.getMediumDateFormat(context).format(millis),
				HourMinute.getLocalizedString(pillpopperTime.getLocalHourMinute(), context)
				);
	}

	public static String getLocalizedStringOnlyTime(PillpopperTime pillpopperTime, int nullDateStringId, PillpopperActivity context)
	{
		if (pillpopperTime == null) {
			try {
				if(nullDateStringId == 0){
					return context.getResources().getString(R.string.__blank);
				}else{
					return context.getResources().getString(nullDateStringId);
				}
			} catch (Exception e) {
				PillpopperLog.say("Oops!, Exception while getting the resource. So sending blank");
				return "";
			}
		}
		
		//long millis = pillpopperTime._gmtSeconds * 1000;

		return String.format("%s",
								HourMinute.getLocalizedString(pillpopperTime.getLocalHourMinute(), context)
				);
	}
	
	public static String getLocalizedStringOnlyTime(PillpopperTime pillpopperTime, int nullDateStringId, Context context )
	{
		if (pillpopperTime == null) {
			try {
				if(nullDateStringId == 0){
					return context.getResources().getString(R.string.__blank);
				}else{
					return context.getResources().getString(nullDateStringId);
				}
			} catch (Exception e) {
				PillpopperLog.say("Oops!, Exception while getting the resource. So sending blank");
				return "";
			}
		}
		
		//long millis = pillpopperTime._gmtSeconds * 1000;

		return String.format("%s",
								HourMinute.getLocalizedString(pillpopperTime.getLocalHourMinute(), context)
				);
	}
	
	public static String getDebugString(PillpopperTime pillpopperTime)
	{
		if (pillpopperTime == null) {
			return "<null date>";
		} else {
			long millis = pillpopperTime._gmtSeconds * 1000;
			return String.format(Locale.US,"%s (%d)", DateFormat.format("E MMM dd, yyyy h:mmaa", millis), pillpopperTime._gmtSeconds);
		}
	}



	//////////
	
	



	// Get the year/month/date portion of the PillpopperDate, ignoring
	// hour, minute and second.
	public PillpopperDay getLocalDay()
	{
		Calendar c = _getLocalCalendar();
		return new PillpopperDay(
				c.get(Calendar.YEAR),
				c.get(Calendar.MONTH),
				c.get(Calendar.DATE)
				);
	}

	public PillpopperDay getPrevious48HoursLocalDay()
	{
		Calendar c = _getLocalCalendar();
		return new PillpopperDay(
				c.get(Calendar.YEAR),
				c.get(Calendar.MONTH),
				c.get(Calendar.DATE)-2
		);
	}

	/**
	 * Gives the 31 days back PilPopperDay
	 * @return
	 */
	public PillpopperDay get31DaysOldLocalDay()
	{
		Calendar c = _getLocalCalendar();
		return new PillpopperDay(
				c.get(Calendar.YEAR),
				c.get(Calendar.MONTH),
				c.get(Calendar.DATE) - PillpopperConstants.MISSING_DOSES_MAXIMUM_DAYS_CHECK
		);
	}
	
	public HourMinute getLocalHourMinute()
	{
		Calendar c = _getLocalCalendar();
		return new HourMinute(
				c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE));
	}


	public PillpopperTime getContainingMinute()
	{
		return getLocalDay().atLocalTime(getLocalHourMinute());
	}
	
	
	//// comparisons/computations
	
	public boolean before(PillpopperTime pillpopperTime)
	{
		return _gmtSeconds < pillpopperTime._gmtSeconds;
	}

	public boolean after(PillpopperTime pillpopperTime)
	{
		return _gmtSeconds > pillpopperTime._gmtSeconds;
	}

	@Override
	public int compareTo(PillpopperTime other)
	{
		return (int) (_gmtSeconds - other._gmtSeconds);
	}

	@Override
	public boolean equals(Object o)
	{
		// Return true if the objects are identical.
		// (This is just an optimization, not required for correctness.)
		if (this == o) {
			return true;
		}

		// Return false if the other object has the wrong type.
		// This type may be an interface depending on the interface's specification.
		if (!(o instanceof PillpopperTime)) {
			return false;
		}

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access private fields.
		PillpopperTime rhs = (PillpopperTime) o;
		
		return rhs.compareTo(this) == 0;
	}
	
	public int hashCode()
	{
		return (int) _gmtSeconds;
	}
	
	public boolean fallsBetweenTimes(HourMinute startHourMinute, HourMinute endHourMinute)
	{
		if (startHourMinute == null || endHourMinute == null) {
			return false;
		}
		
		// Convert start and end (specified in the local timezone) to UTC
		PillpopperDay pastDay = PillpopperDay.today();
		PillpopperTime start = pastDay.atLocalTime(startHourMinute);
		PillpopperTime end =   pastDay.atLocalTime(endHourMinute);
		
		long spanLenSecs = (WDHM.SecPerDay + end.getGmtSeconds() - start.getGmtSeconds()) % WDHM.SecPerDay;

		/*
		PillpopperLog.Say("asking whether %s is between %s and %s",
				getDebugString(this),
				startHourMinute.toString(),
				endHourMinute.toString());
		PillpopperLog.Say("converted to local time, span runs %s to %s",
				getDebugString(start),
				getDebugString(end));
		
		PillpopperLog.Say("Span is %d milliseconds long", spanLenMillis);
		PillpopperLog.Say("time of interest is %d msec after start.  result: %s",
				(this.getTimeInMillis() - start.getTimeInMillis()) % millisPerDay, retval ? "yes" : "no");
				*/
		
		return (this.getGmtSeconds() - start.getGmtSeconds()) % WDHM.SecPerDay >= 0 &&
		(this.getGmtSeconds() - start.getGmtSeconds()) % WDHM.SecPerDay <= spanLenSecs;
	}
	
	public static  String getTimeThenDayOfWeek(long timeOfDay){
		Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeOfDay);
       // String timeAndDay = cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+" "+cal.get(Calendar.AM_PM)+" "+cal.get(Calendar.DAY_OF_WEEK);      


        Date date = new Date(timeOfDay);
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a EEE",Locale.US);


		//String.format("%s (%d)", DateFormat.format("h:mmaa", timeOfDay));
        return formatter.format(date);
  }
  
public String getTimeStringRelToToday(){
	String dateHeader;
	if (this.before(PillpopperDay.today().atLocalTime(new HourMinute(0, 0)))) {
		dateHeader = PillpopperTime.getTimeThenDayOfWeek(this.getGmtMilliseconds());
	}else {
		dateHeader = PillpopperTime.getTimeThenDayOfWeekWithAMPM(this.getGmtMilliseconds());
	}
	return dateHeader;
}
	
  public static  String getTimeThenDayOfWeekWithAMPM(long timeOfDay){
        long timestamp = timeOfDay;
        Calendar cal = Calendar.getInstance();  
        cal.setTimeInMillis(timestamp);
       // String timeAndDay = cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+" "+cal.get(Calendar.AM_PM);    


        Date date = new Date(timeOfDay);
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a",Locale.US);
        String dateFormatted = formatter.format(date);

        //String.format("%s (%d)", DateFormat.format("h:mmaa", timeOfDay));
        return dateFormatted;
  }

}
