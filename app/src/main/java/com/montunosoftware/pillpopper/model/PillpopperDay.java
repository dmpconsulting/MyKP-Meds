package com.montunosoftware.pillpopper.model;

import android.content.Context;
import android.text.format.DateFormat;

import com.montunosoftware.pillpopper.android.util.EnumMarshaller;
import com.montunosoftware.pillpopper.android.util.Pair;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


// A class that holds a day: Year, Month and Day of Month.
public class PillpopperDay {
    private int _year;
    private int _month;
    private int _day;

    private static final TimeZone _gmtTimeZone = TimeZone.getTimeZone("GMT");

    //// constructors

    public PillpopperDay(int year, int month, int day) {
        _year = year;
        _month = month;
        _day = day;
    }

    public PillpopperDay(Calendar calendar) {
        _setFromCalendar(calendar);
    }

    public static PillpopperDay today() {
        return PillpopperTime.now().getLocalDay();
    }

    public static PillpopperDay getPrevious48HoursLocalDay() {
        return PillpopperTime.now().getPrevious48HoursLocalDay();
    }


    // Return a PillpopperTime: our day plus the passed HourMinute, yielding a PillpopperTime.
    public PillpopperTime atLocalTime(HourMinute hourMinute) {
        Calendar c = new GregorianCalendar();
        c.set(Calendar.YEAR, _year);
        c.set(Calendar.MONTH, _month);
        c.set(Calendar.DATE, _day);
        c.set(Calendar.HOUR_OF_DAY, hourMinute.getHour());
        c.set(Calendar.MINUTE, hourMinute.getMinute());
        c.set(Calendar.SECOND, hourMinute.getSecond());
        c.set(Calendar.MILLISECOND, 0);

        return new PillpopperTime(c);
    }

    public PillpopperTime atGmtTime(HourMinute hourMinute) {
        Calendar gmtCalendar = new GregorianCalendar(_gmtTimeZone);
        gmtCalendar.set(Calendar.HOUR_OF_DAY, hourMinute.getHour());
        gmtCalendar.set(Calendar.MINUTE, hourMinute.getMinute());

        Calendar localHourMinuteTranslator = new GregorianCalendar();
        localHourMinuteTranslator.setTimeInMillis(gmtCalendar.getTimeInMillis());

        Calendar localCalendar = _getLocalCalendar();
        localCalendar.set(Calendar.HOUR_OF_DAY, localHourMinuteTranslator.get(Calendar.HOUR_OF_DAY));
        localCalendar.set(Calendar.MINUTE, localHourMinuteTranslator.get(Calendar.MINUTE));

        return new PillpopperTime(localCalendar);
    }

    //// marshalling/unmarshalling


    public enum PartOfDay {
        DayStart,
        DayEnd
    }

    // Put the local day, which we represent as the number of GMT seconds of the start or end of the GMT day.
    public static void marshalLocalDayAsGMTTime(JSONObject jsonObject, String key, PillpopperDay pillpopperDay, PartOfDay partOfDay, int hour, int min) throws JSONException {
        if (pillpopperDay == null) {
            jsonObject.put(key, -1);
        } else {
            HourMinute hm;

            if (partOfDay == PartOfDay.DayStart) {
                hm = new HourMinute(hour, min, 0);
            } else {
                hm = new HourMinute(23, 59, 59);
            }

            PillpopperTime.marshal(jsonObject, key, pillpopperDay.atLocalTime(hm));
        }
    }


    // Factory used during JSON parsing.  Expects to find an integer representing GMT seconds of the
    // start of a day. Convert to the day in the local timezone.
    public static PillpopperDay parseGMTTimeAsLocalDay(JSONObject jsonObject, String key) {
        PillpopperTime time = PillpopperTime.parseJSON(jsonObject, key);
        if (time == null) {
            return null;
        } else {
            return time.getLocalDay();
        }
    }


    public static PillpopperDay parseGMTTimeAsLocalDay(String key) {
        PillpopperTime time = PillpopperTime.parseJSON(key);

        if (time == null) {
            return null;
        } else {
            return time.getLocalDay();
        }
    }


    //// accessors

    private void _setFromCalendar(Calendar calendar) {
        _year = calendar.get(Calendar.YEAR);
        _month = calendar.get(Calendar.MONTH);
        _day = calendar.get(Calendar.DATE);
    }

    private Calendar _getGmtCalendar() {
        Calendar gmtCalendar = new GregorianCalendar(_gmtTimeZone);
        gmtCalendar.set(Calendar.YEAR, _year);
        gmtCalendar.set(Calendar.MONTH, _month);
        gmtCalendar.set(Calendar.DATE, _day);
        gmtCalendar.set(Calendar.HOUR_OF_DAY, 0);
        gmtCalendar.set(Calendar.MINUTE, 0);
        gmtCalendar.set(Calendar.SECOND, 0);
        gmtCalendar.set(Calendar.MILLISECOND, 0);
        return gmtCalendar;
    }

    private Calendar _getLocalCalendar() {
        Calendar localCalendar = new GregorianCalendar();
        localCalendar.set(Calendar.YEAR, _year);
        localCalendar.set(Calendar.MONTH, _month);
        localCalendar.set(Calendar.DATE, _day);
        localCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);

        return localCalendar;
    }

    public String getHeaderDateText() {
        Calendar cal = _getLocalCalendar();
        DateFormatSymbols dateFormats = new DateFormatSymbols();
        StringBuilder builder = new StringBuilder();
        builder.append(dateFormats.getWeekdays()[cal.get(Calendar.DAY_OF_WEEK)]);
        builder.append(", ");
        builder.append(dateFormats.getMonths()[cal.get(Calendar.MONTH)]);
        builder.append(" ");
        builder.append(cal.get(Calendar.DAY_OF_MONTH));

        return builder.toString();
    }

    public String getHeaderMonthText() {
        Calendar cal = _getLocalCalendar();
        DateFormatSymbols dateFormats = new DateFormatSymbols();
        String builder;
        builder = dateFormats.getMonths()[cal.get(Calendar.MONTH)] + " ";
        return builder;
    }

    public int getYear() {
        return _year;
    }

    public int getMonth() {
        return _month;
    }

    public int getDay() {
        return _day;
    }


    //// formatters

    // Returns a full date string like "Mon Feb 12, 2012"
    private String _getLocalizedString(boolean withDayOfWeek, Context context) {
        long millis = atLocalTime(new HourMinute(0, 0)).getGmtSeconds() * 1000;
        StringBuilder sb = new StringBuilder();

        if (withDayOfWeek) {
            sb.append(DateFormat.format("E", millis));
            sb.append(" ");
        }

        sb.append(DateFormat.getMediumDateFormat(context).format(millis));
        return sb.toString();
    }

    // Similar to the above, but can handle nulls
    public static String getLocalizedString(PillpopperDay pillpopperDay, boolean withDayOfWeek, int nullDateStringId, Context context) {
        if (pillpopperDay == null) {
            return context.getString(nullDateStringId);
        } else {
            return pillpopperDay._getLocalizedString(withDayOfWeek, context);
        }
    }

    // New Method Which serve the different date formate.
    public static String getLocalizedDateString(PillpopperDay pillpopperDay, boolean withDayOfWeek, int nullDateStringId, Context context) {
        if (pillpopperDay == null) {
            return context.getString(nullDateStringId);
        } else {
            return pillpopperDay._getLocalizedDateString(withDayOfWeek, context);
        }
    }

    // New method which returns new

    private String _getLocalizedDateString(boolean withDayOfWeek, Context context) {
        long millis = atLocalTime(new HourMinute(0, 0)).getGmtSeconds() * 1000;
        StringBuilder sb = new StringBuilder();

        if (withDayOfWeek) {
            //sb.append(DateFormat.format("E", millis));
            //sb.append(" ");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMMM d yyyy", Locale.US);
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy",Locale.US);
        //sb.append(DateFormat.getDateFormat(context).format(millis));
        sb.append(simpleDateFormat.format(millis));
        return sb.toString();
    }


    public static String getDateString(PillpopperDay pillpopperDay, boolean withDayOfWeek, int nullDateStringId, Context context) {
        if (pillpopperDay == null) {
            return context.getString(nullDateStringId);
        } else {
            return pillpopperDay.getFormattedString(withDayOfWeek, context);
        }
    }

    private String getFormattedString(boolean withDayOfWeek, Context context) {
        long millis = atLocalTime(new HourMinute(0, 0)).getGmtSeconds() * 1000;
        StringBuilder sb = new StringBuilder();

        if (withDayOfWeek) {
            //sb.append(DateFormat.format("E", millis));
            //sb.append(" ");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy",Locale.US);
        //sb.append(DateFormat.getDateFormat(context).format(millis));
        sb.append(simpleDateFormat.format(millis));
        return sb.toString();
    }

    public CharSequence getDayName() {
        Calendar c = _getGmtCalendar();
        return DateFormat.format("EEEE", c);
    }

    public static String getLocalizedDateStr(PillpopperDay pillpopperDay, boolean withDayOfWeek, int nullDateStringId, Context context) {
        if (pillpopperDay == null) {
            return context.getString(nullDateStringId);
        } else {
            return pillpopperDay._getLocalizedDateStr(withDayOfWeek);
        }
    }

    private String _getLocalizedDateStr(boolean withDayOfWeek) {
        long millis = atLocalTime(new HourMinute(0, 0)).getGmtSeconds() * 1000;
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        sb.append(simpleDateFormat.format(millis));
        return sb.toString();
    }

    //// comparators

    @Override
    public boolean equals(Object o) {
        // Return true if the objects are identical.
        // (This is just an optimization, not required for correctness.)
        if (this == o) {
            return true;
        }

        // Return false if the other object has the wrong type.
        // This type may be an interface depending on the interface's specification.
        if (!(o instanceof PillpopperDay)) {
            return false;
        }

        // Cast to the appropriate type.
        // This will succeed because of the instanceof, and lets us access private fields.
        PillpopperDay lhs = (PillpopperDay) o;

        // Check each field. Primitive fields, reference fields, and nullable reference
        // fields are all treated differently.
        return
                (_year == lhs._year) &&
                        (_month == lhs._month) &&
                        (_day == lhs._day)
                ;
    }


    /*FindBugs defects
     *
     * implementing hashcode method and keeping it exact for FindBugs fixes*/
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    public boolean after(PillpopperDay today) {
        return _getGmtCalendar().after(today._getGmtCalendar());
    }

    public boolean before(PillpopperDay today) {
        return _getGmtCalendar().before(today._getGmtCalendar());
    }

    public long daysAfter(PillpopperDay other) {
        long millisDiff = _getGmtCalendar().getTimeInMillis() - other._getGmtCalendar().getTimeInMillis();
        return millisDiff / (24 * 60 * 60 * 1000);
    }

    public long daysAfterWithoutMilliSeconds(PillpopperDay other) {
        long millisDiff = _getGmtCalendar().getTimeInMillis() - other._getGmtCalendar().getTimeInMillis();
        return millisDiff / (24 * 60 * 60);
    }


    //// math

    public PillpopperDay addDays(long l) {
        Calendar c = _getGmtCalendar();
        c.add(Calendar.DATE, (int) l);
        return new PillpopperDay(c);
    }


    ////// names of days of the week


    public enum DayOfWeek {
        Sunday(1),

        Monday(2),

        Tuesday(3),

        Wednesday(4),

        Thursday(5),

        Friday(6),

        Saturday(7);

        private final int _dayNumber;

        DayOfWeek(int dayNumber) {
            _dayNumber = dayNumber;
        }

        public int getDayNumber() {
            return _dayNumber;
        }
    }

    public DayOfWeek getDayOfWeek() {
        Calendar cal = _getGmtCalendar();
        int day = cal.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                return DayOfWeek.Sunday;
            case Calendar.MONDAY:
                return DayOfWeek.Monday;
            case Calendar.TUESDAY:
                return DayOfWeek.Tuesday;
            case Calendar.WEDNESDAY:
                return DayOfWeek.Wednesday;
            case Calendar.THURSDAY:
                return DayOfWeek.Thursday;
            case Calendar.FRIDAY:
                return DayOfWeek.Friday;
            case Calendar.SATURDAY:
                return DayOfWeek.Saturday;
            default:
                return DayOfWeek.Sunday;
        }
    }

    public String getDayOfWeekString() {
        Calendar cal = _getGmtCalendar();
        int day = cal.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return "Sunday";
        }
    }

    @SuppressWarnings("unchecked")
    public static final EnumMarshaller<DayOfWeek> daysOfWeekMarshaller =
            new EnumMarshaller<>(Arrays.asList(
                    new Pair<>(DayOfWeek.Sunday, "1"),

                    new Pair<>(DayOfWeek.Monday, "2"),

                    new Pair<>(DayOfWeek.Tuesday, "3"),

                    new Pair<>(DayOfWeek.Wednesday, "4"),

                    new Pair<>(DayOfWeek.Thursday, "5"),

                    new Pair<>(DayOfWeek.Friday, "6"),

                    new Pair<>(DayOfWeek.Saturday, "7")

            ));

    private static final String[] _weekdayNames = new DateFormatSymbols().getWeekdays();
    private static final String[] _shortWeekdayNames = new DateFormatSymbols().getShortWeekdays();

    private static int _dayOfWeekToCalendarConstant(DayOfWeek day) {
        switch (day) {
            case Sunday:
                return Calendar.SUNDAY;
            case Monday:
                return Calendar.MONDAY;
            case Tuesday:
                return Calendar.TUESDAY;
            case Wednesday:
                return Calendar.WEDNESDAY;
            case Thursday:
                return Calendar.THURSDAY;
            case Friday:
                return Calendar.FRIDAY;
            case Saturday:
                return Calendar.SATURDAY;

        }

        return 0;
    }

    public static String getDayName(DayOfWeek day) {
        return _weekdayNames[_dayOfWeekToCalendarConstant(day)];
    }

    public static String getShortDayName(DayOfWeek day) {
        return _shortWeekdayNames[_dayOfWeekToCalendarConstant(day)];
    }

    public static String getShortDayNameList(EnumSet<DayOfWeek> daysOfWeek) {
        StringBuilder sb = new StringBuilder();
        for (DayOfWeek d : daysOfWeek) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(getShortDayName(d));
        }
        return Util.cleanString(sb.toString());
    }

    public static void marshalLocalDayAsGMTTime(JSONObject jsonObject, JSONObject preferenceObject,String key, PillpopperDay pillpopperDay, PartOfDay partOfDay, Schedule schedule) throws JSONException {
        if (pillpopperDay == null) {
            jsonObject.put(key, -1);
        } else {
            HourMinute hm;

            if (partOfDay == PartOfDay.DayStart) {
                String scheduleChoiceString = Util.parseJSONStringOrNull(preferenceObject, "scheduleChoice");

                if (null !=scheduleChoiceString && !scheduleChoiceString.equalsIgnoreCase(AppConstants.SCHEDULE_CHOICE_SCHEDULED) && schedule.getTimeList() != null && schedule.getTimeList().length() != 0) {

                    hm = schedule.getTimeList().getTime(0);

                } else {
                    hm = new HourMinute(0, 0, 0);
                }

            } else {
                hm = new HourMinute(23, 59, 59);
            }

            PillpopperTime.marshal(jsonObject, key, pillpopperDay.atLocalTime(hm));
        }
    }


    // DB Suppert methods


    public static long marshalLocalDayAsGMTTime(PartOfDay partOfDay, String scheduleChoice,Schedule schedule, PillpopperDay pillpopperDay) throws JSONException {

        if (pillpopperDay == null) {
            return -1;
        } else {
            HourMinute hm;

            if (partOfDay == PartOfDay.DayStart) {

                if (null!=scheduleChoice && !scheduleChoice.equalsIgnoreCase(AppConstants.SCHEDULE_CHOICE_SCHEDULED) && schedule.getTimeList() != null && schedule.getTimeList().length() != 0) {

                    hm = schedule.getTimeList().getTime(0);

                } else {
                    hm = new HourMinute(0, 0, 0);
                }

            } else {
                hm = new HourMinute(23, 59, 59);
            }

            return PillpopperTime.marshal(pillpopperDay.atLocalTime(hm));
        }
    }
}
