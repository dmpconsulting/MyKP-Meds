package com.montunosoftware.pillpopper.model;


import android.content.Context;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.EnumMarshaller;
import com.montunosoftware.pillpopper.android.util.Pair;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperParseException;
import com.montunosoftware.pillpopper.android.util.PillpopperStringBuilder;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.WDHM;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class Schedule implements Cloneable {
    public enum SchedType {
        SCHEDULED, INTERVAL, AS_NEEDED
    }

    public Schedule copy() {
        Schedule clone;
        try {
            clone = (Schedule) super.clone();
        } catch (CloneNotSupportedException e) {
            PillpopperLog.say("Schedule clone failed");
            return null;
        }
        clone.setTimeList(getTimeList().copy());
        clone._daysOfWeek = EnumSet.copyOf(_daysOfWeek);
        return clone;
    }

    public Schedule _copy() {
        Schedule clone;
        try {
            clone = (Schedule) super.clone();
        } catch (CloneNotSupportedException e) {
            PillpopperLog.say("Schedule clone failed");
            return null;
        }
        clone.setTimeList(getTimeList()._copy());
        clone._daysOfWeek = EnumSet.copyOf(_daysOfWeek);
        return clone;
    }

    public Schedule() {
        setSchedType(SchedType.SCHEDULED);
        setTimeList(new TimeList());
    }

    private static final String _JSON_SCHEDTYPE = "type";
    private static final String _JSON_SCHEDTYPE_INTERVAL = "interval";
    private static final String _JSON_SCHEDTYPE_SCHEDULED = "scheduled";
    private static final String _JSON_SCHEDULE = "schedule";
    private static final String _JSON_SCHEDULE_CHOICE = "scheduleChoice";
    private static final String _JSON_SCHEDULE_UNDEFINED = "undefined";

    static Schedule parseJSON(JSONObject jsonDrug, JSONObject jsonDrugPrefs, String _name, Context context) throws PillpopperParseException {
        Schedule schedule = new Schedule();

       // String scheduleTypeString = Util.parseJSONStringOrNull(jsonDrug, _JSON_SCHEDTYPE);
        String scheduleChoiceString = Util.parseJSONStringOrNull(jsonDrugPrefs, _JSON_SCHEDULE_CHOICE);


        // no schedule type string!?  just return a default schedule.
        if (scheduleChoiceString == null) {
            return schedule;
        }


        if (scheduleChoiceString.equalsIgnoreCase(_JSON_SCHEDTYPE_SCHEDULED)) {
            schedule.setSchedType(SchedType.SCHEDULED);

            try {
                JSONArray jsonSchedule = jsonDrug.getJSONArray(_JSON_SCHEDULE);
                schedule.setTimeList(new TimeList(jsonSchedule));
            } catch (JSONException e) {
                PillpopperLog.say("invalid pill: scheduled type, but has no schedule");
                throw new PillpopperParseException("invalid pill: scheduled type, but has no schedule");
            }
        } else if (scheduleChoiceString.equalsIgnoreCase(AppConstants.SCHEDULE_CHOICE_AS_NEEDED) || scheduleChoiceString.equalsIgnoreCase(_JSON_SCHEDULE_UNDEFINED)) {
            schedule.setSchedType(SchedType.INTERVAL);
            long interval = Util.parseJSONNonnegativeLong(jsonDrug, _JSON_INTERVAL);

            if (interval == 0) {
                schedule.setSchedType(SchedType.AS_NEEDED);
            } else if (interval < 0) {
                throw new PillpopperParseException("interval pill has invalid interval");
            }
            schedule.setInterval(interval);

        } else {
            PillpopperLog.say("got invalid schedule type: %s", scheduleChoiceString);
            throw new PillpopperParseException("invalid schedule type");
        }


        PillpopperTime time = PillpopperTime.parseJSON(jsonDrug, _JSON_START);
        String startTime = null;
        if (null != context) {
            startTime = PillpopperTime.getLocalizedStringOnlyTime(time, 0, context);
            PillpopperLog.say("Inspect For start time and Drug Name : " + _name + " And time :" + startTime);
        }

        if (!scheduleChoiceString.equalsIgnoreCase(AppConstants.SCHEDULE_CHOICE_SCHEDULED)) {
            if (time != null) {
                schedule.setStart(time.getLocalDay());
                schedule.getTimeList().clearTimesList();
                if (null != startTime && (startTime.contains("12:00") || startTime.contains("00:00"))) {
                    PillpopperLog.say("Inspect For start time: " + _name + " Replacing with 12:00 AM" + " giving back : " + giveBackFormattedHourMinute(startTime));
                    schedule.getTimeList().addTime(giveBackFormattedHourMinute(startTime));
                } else {
                    schedule.getTimeList().addTime(time.getLocalHourMinute());
                }


            } else {
                schedule.setStart(null);
            }
        } else {

            schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(jsonDrug, _JSON_START));
        }
        schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(jsonDrug, _JSON_END));
        schedule.setDayPeriod(Util.parseJSONNonnegativeLong(jsonDrug, _JSON_DAYPERIOD));

        schedule._daysOfWeek = PillpopperDay.daysOfWeekMarshaller.fromJsonStringList(jsonDrugPrefs, _JSON_DAYS_OF_WEEK);

        try {
            PillpopperLog.say("TAG Week day:" + jsonDrugPrefs.getJSONObject("weekdays"));
        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception" + e.getMessage());
        }

        schedule._dayofWeeks_String = PillpopperDay.daysOfWeekMarshaller.fromJsonStringListWeek(jsonDrugPrefs, _JSON_DAYS_OF_WEEK);
        // daily limit
        schedule._dailyLimitType = _dailyLimitTypeMarshaller.fromJson(jsonDrugPrefs, _JSON_DAILY_LIMIT_TYPE, DailyLimitType.None);
        schedule._dailyLimit = Math.max(0, Util.parseJSONNonnegativeLong(jsonDrugPrefs, _JSON_DAILY_LIMIT));

        return schedule;
    }


    // Helper methods for DB suppoer - start

   /* public static Schedule parseDBSchedule(IntermediateSchedule intermediateSchedule) throws PillpopperParseException {
        Schedule schedule = new Schedule();
        if (intermediateSchedule == null) {
            return schedule;
        }

        String scheduleTypeStr = intermediateSchedule.getType();

        if (null != scheduleTypeStr && scheduleTypeStr.equalsIgnoreCase(_JSON_SCHEDTYPE_SCHEDULED)) {
            schedule.setSchedType(SchedType.SCHEDULED);

            try {
                if (null != intermediateSchedule.getSheduleTimeList()) {
                    schedule.setTimeList(new TimeList(intermediateSchedule.getSheduleTimeList()));

                }
            } catch (Exception e) {
                PillpopperLog.say("invalid pill: scheduled type, but has no schedule");
                throw new PillpopperParseException("invalid pill: scheduled type, but has no schedule");
            }

            schedule.setStart(PillpopperDay.parseGMTTimeAsLocalDay(intermediateSchedule.getStart()));
            schedule.setEnd(PillpopperDay.parseGMTTimeAsLocalDay(intermediateSchedule.getEnd()));
            schedule.setDayPeriod(Util.parseJSONNonnegativeLong(intermediateSchedule.getDayperiod()));
            schedule.setDays(intermediateSchedule.getDays());

        } else if (null != scheduleTypeStr && scheduleTypeStr.equalsIgnoreCase(_JSON_SCHEDTYPE_INTERVAL)) {
            schedule.setSchedType(SchedType.INTERVAL);
        } else if (null != scheduleTypeStr && ("AS_NEEDED").equalsIgnoreCase(scheduleTypeStr)) {
            schedule.setSchedType(SchedType.AS_NEEDED);
        } else {
            schedule.setSchedType(SchedType.SCHEDULED);
        }


        return schedule;
    } */

    // Helper methods for DB suppoer - end
    public static HourMinute giveBackFormattedHourMinute(String scheduletime) {
        return getSelectedHourMinute(2, 30);
    }

    public static HourMinute getSelectedHourMinute(int hour, int min) {
        if (hour < 0 || min < 0) {
            PillpopperLog.say("Selected hourminute: none");
            return null;
        } else {
            HourMinute retval = new HourMinute(hour, min);
            PillpopperLog.say("Selected hourminute: %s", retval.toString());
            return retval;
        }
    }


    private String _intervalStartTime;

    public String get_intervalStartTime() {
        return _intervalStartTime;
    }

    public void set_intervalStartTime(String _intervalStartTime) {
        this._intervalStartTime = _intervalStartTime;
    }

    public void marshal(JSONObject jsonDrug, JSONObject jsonDrugPrefs) throws JSONException {
        String scheduleChoiceString = Util.parseJSONStringOrNull(jsonDrugPrefs, _JSON_SCHEDULE_CHOICE);
        switch (scheduleChoiceString) {
            case AppConstants.SCHEDULE_CHOICE_SCHEDULED:
                jsonDrugPrefs.put(_JSON_SCHEDULE_CHOICE, _JSON_SCHEDTYPE_SCHEDULED);
                jsonDrug.put(_JSON_SCHEDULE, getTimeList().marshal());
                break;
            case AppConstants.SCHEDULE_CHOICE_UNDEFINED:
                jsonDrugPrefs.put(_JSON_SCHEDULE_CHOICE, _JSON_SCHEDULE_UNDEFINED);
                jsonDrug.put(_JSON_INTERVAL, getIntervalSeconds());
                PillpopperLog.say("day and weeks value:---" + getIntervalSeconds());
                break;

            case AppConstants.SCHEDULE_CHOICE_AS_NEEDED:
                jsonDrugPrefs.put(_JSON_SCHEDULE_CHOICE, _JSON_SCHEDULE_UNDEFINED);
                jsonDrug.put(_JSON_INTERVAL, 0);
                break;
        }

        PillpopperDay.marshalLocalDayAsGMTTime(jsonDrug, jsonDrugPrefs,_JSON_START, getStart(), PillpopperDay.PartOfDay.DayStart, this);
        PillpopperDay.marshalLocalDayAsGMTTime(jsonDrug, jsonDrugPrefs,_JSON_END, getEnd(), PillpopperDay.PartOfDay.DayEnd, this);

        jsonDrug.put(_JSON_DAYPERIOD, getDayPeriod());
        //PillpopperDay.daysOfWeekMarshaller.marshal(jsonDrugPrefs, _JSON_DAYS_OF_WEEK, _daysOfWeek);
        PillpopperDay.daysOfWeekMarshaller.marshalWeekString(jsonDrugPrefs, _JSON_DAYS_OF_WEEK, _dayofWeeks_String);
        // daily limit
        _dailyLimitTypeMarshaller.marshal(jsonDrugPrefs, _JSON_DAILY_LIMIT_TYPE, _dailyLimitType);
        Util.putJSONStringFromLong(jsonDrugPrefs, _JSON_DAILY_LIMIT, _dailyLimit);
    }


    // we'll just say it's empty if it's scheduled with no schedule.
    public boolean containsNoData(String scheduleChoice) {
        return (scheduleChoice.equalsIgnoreCase(SchedType.SCHEDULED.toString()) && getTimeList().length() == 0);
    }


    //////////////////////////////////////


    private SchedType _schedType;

    public SchedType getSchedType() {
        return _schedType;
    }

    public void setSchedType(SchedType schedType) {
        _schedType = schedType;

        if (_schedType == SchedType.INTERVAL || _intervalSeconds == 0) {
            if (getIntervalSeconds() == 0) {
                _intervalSeconds = DEFAULT_INTERVAL_SECONDS;
            }
        }
    }

    public static String getSchedTypeString(Context ctx, String schedType, PillpopperAppContext globalAppContext, String maxDailyDoses) {
        //State currState = globalAppContext.getState(ctx);
        Context context = ctx;

        switch (schedType) {
            case AppConstants.SCHEDULE_CHOICE_SCHEDULED:
                return context.getString(R.string.on_a_schedule);
            case AppConstants.SCHEDULE_CHOICE_UNDEFINED:
               /* if (currState.getBedTime().isEnabled()) {
                    return context.getString(R.string.at_intervals_until_bedtime);
                } else {
                    return context.getString(R.string.at_intervals);
                }*/
               /* if (maxDailyDoses != null && !("").equalsIgnoreCase(maxDailyDoses) && !(("-1").equalsIgnoreCase(maxDailyDoses))) {
                    return context.getString(R.string.as_needed);
                } else {*/
                    return "";
//             }
            case AppConstants.SCHEDULE_CHOICE_AS_NEEDED:
                return context.getString(R.string.as_needed);
            default:
                return "getSchedTypeString: bug";
        }
    }

    //////////////////////////////////////////

    //Days

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    private String days;

    // Start date
    private static final String _JSON_START = "start";
    private PillpopperDay _start = PillpopperDay.today();

    public PillpopperDay getStart() {
        return _start;
    }

    public void setStart(PillpopperDay start) {
        if (start != null) {
            _start = start;
        } else {
            _start = PillpopperDay.today();
        }
    }


    // End date
    private static final String _JSON_END = "end";
    private PillpopperDay _end;

    public PillpopperDay getEnd() {
        return _end;
    }

    public void setEnd(PillpopperDay end) {
        _end = end;
    }

    //////////////////////////////////////////

    // List of scheduled times of day
    private TimeList _timeList;

    public void setTimeList(TimeList timeList) {
        _timeList = timeList;
    }

    public TimeList getTimeList() {
        return _timeList;
    }

    public boolean isActiveToday() {
        return isActiveOnDay(PillpopperDay.today());
    }

    public boolean isActiveOnDay(PillpopperDay day) {
        if (_start != null && _start.after(day)) {
            return false;
        }

        return !(_end != null && day.after(_end));

    }

    ///////////////////////////////////////

    // Day period
    private static final String _JSON_DAYPERIOD = "dayperiod";
    private long _dayPeriod = 1;

    public long getDayPeriod() {
        return _dayPeriod;
    }

    public void setDayPeriod(long dayPeriod) {
        _dayPeriod = Math.max(1, dayPeriod);

        if (_dayPeriod != 7) {
            setDaysOfWeek(null);
        }
    }

    public static List<Long> getStandardDayPeriods() {
        return Arrays.asList(1L, 7L, 31L);
    }

    public static String dayperiodToName(Context context, long dayPeriod) {
        if (dayPeriod == 0) {
            return context.getString(R.string._never);
        } else if (dayPeriod == 1) {
            return context.getString(R.string.once_or_more_per_day);
        } else if (dayPeriod == 7) {
            return context.getString(R.string._weekly);
        } else if (dayPeriod == 31) {
            return context.getString(R.string._monthly);
        } else {
            return String.format(
                    context.getString(R.string._every_interval),
                    new WDHM(dayPeriod * WDHM.SecPerDay).toLocalizedString(context, R.string._never));
        }
    }


    // Interval
    private static final String _JSON_INTERVAL = "interval";
    private final static long DEFAULT_INTERVAL_SECONDS = 4 * WDHM.SecPerHour; // default interval: 4 hours
    private long _intervalSeconds;

    public long getIntervalSeconds() {
        return _intervalSeconds;
    }

    public void setInterval(long _interval) {
        this._intervalSeconds = _interval;
    }

    public String intervalAsString(Context context) {
        if (getIntervalSeconds() > 0) {
            return String.format("%s %s",
                    new WDHM(getIntervalSeconds()).toLocalizedString(context, R.string._not_set).toLowerCase(Locale.getDefault()),
                    context.getString(R.string._after_dose));
        } else {
            return context.getString(R.string._not_set);
        }
    }


    ////////////// days of the week ///////////////////////////


    private static final String _JSON_DAYS_OF_WEEK = "weekdays";

    private EnumSet<PillpopperDay.DayOfWeek> _daysOfWeek = EnumSet.noneOf(PillpopperDay.DayOfWeek.class);

    public EnumSet<PillpopperDay.DayOfWeek> getDaysOfWeek() {
        if (_daysOfWeek.isEmpty()) {
            _daysOfWeek.add(PillpopperDay.today().getDayOfWeek());
        }

        return EnumSet.copyOf(_daysOfWeek);
    }

    public void setDaysOfWeek(List<PillpopperDay.DayOfWeek> dayList) {
        if (dayList == null || dayList.isEmpty())
            _daysOfWeek = EnumSet.noneOf(PillpopperDay.DayOfWeek.class);
        else
            _daysOfWeek = EnumSet.copyOf(dayList);
    }


    private List<String> _dayofWeeks_String = new ArrayList<>();

    public List<String> getDaysOfWeekString() {
//		if (_dayofWeeks_String.size() == 0) {
//			_dayofWeeks_String.add(PillpopperDay.today().getDayOfWeekString());
//		}

        return _dayofWeeks_String;
    }

    public void setDaysOfWeekString(List<String> dayList) {
        /*if (dayList == null || dayList.size() == 0)
            _dayofWeeks_String.add("");
		else{
			_da
			_dayofWeeks_String.addAll(dayList);	
		}*/
        _dayofWeeks_String.clear();
        if (!dayList.isEmpty()) {
            _dayofWeeks_String.addAll(dayList);
        }
    }


    ///////////// daily limit //////////////////////////////

    public enum DailyLimitType {
        None,
        PerDay,
        Per24Hours
    }

    @SuppressWarnings("unchecked")
    private static final EnumMarshaller<DailyLimitType> _dailyLimitTypeMarshaller =
            new EnumMarshaller<>(Arrays.asList(
                    new Pair<>(DailyLimitType.None, "0"),
                    new Pair<>(DailyLimitType.PerDay, "1"),
                    new Pair<>(DailyLimitType.Per24Hours, "2")
            ));

    // Daily limit type
    private static final String _JSON_DAILY_LIMIT_TYPE = "limitType";
    private DailyLimitType _dailyLimitType = DailyLimitType.None;

    public DailyLimitType getDailyLimitType() {
        switch (_schedType) {
            case SCHEDULED:
                return DailyLimitType.None;
            case INTERVAL:
            case AS_NEEDED:
                return _dailyLimitType;
        }

        return DailyLimitType.None;
    }

    public void setDailyLimitType(DailyLimitType dailyLimitType) {
        if (dailyLimitType != null)
            _dailyLimitType = dailyLimitType;
    }


    private static final String _JSON_DAILY_LIMIT = "maxNumDailyDoses";
    private long _dailyLimit = 0;

    public long getDailyLimit() {
        return _dailyLimit;
    }

    public void setDailyLimit(long dailyLimit) {
        _dailyLimit = dailyLimit;
    }

    public String getLimitDescription(Context context) {
        // Limit: e.g., None, 4 doses per day, 10 doses per 24 hours
        String _limitText = null;
        switch (getDailyLimitType()) {
            case None:
                _limitText = context.getString(R.string._none);
                break;
            case PerDay:
                _limitText = String.format(Locale.US, "%d %s %s",
                        getDailyLimit(),
                        getDailyLimit() == 1 ? context.getString(R.string._dose) : context.getString(R.string._doses),
                        context.getString(R.string.limit_per_day).toLowerCase(Locale.getDefault()));
                break;
            case Per24Hours:
                _limitText = String.format(Locale.US, "%d %s %s",
                        getDailyLimit(),
                        getDailyLimit() == 1 ? context.getString(R.string._dose) : context.getString(R.string._doses),
                        context.getString(R.string.limit_per_24hours).toLowerCase(Locale.getDefault()));
                break;
        }

        return _limitText;
    }


    ////////////////////////////

    // Output functions


    public static void describeAsHtml(Context ctx, PillpopperStringBuilder sb, String scheduleChoice, Schedule s, String maxDailyDoses) {
        if (s == null)
            sb.appendColumn(R.string.drug_take_drug);
        else if (s.containsNoData(scheduleChoice))
            sb.appendColumn("");
        else
            sb.appendColumn(getSchedTypeString(ctx,scheduleChoice, sb.getGlobalAppContext(), maxDailyDoses));

        if (s == null)
            sb.appendColumn(R.string._starting);
        else if (s.containsNoData(scheduleChoice))
            sb.appendColumn("");
        else {
            if ((AppConstants.SCHEDULE_CHOICE_SCHEDULED).equalsIgnoreCase(scheduleChoice))
                sb.appendColumn(PillpopperDay.getLocalizedString(s.getStart(), true, R.string._not_set, sb.getContext()));
            else
                sb.appendColumn("");
        }

        if (s == null)
            sb.appendColumn(R.string._ending);
        else if (s.containsNoData(scheduleChoice))
            sb.appendColumn("");
        else {
            if ((AppConstants.SCHEDULE_CHOICE_SCHEDULED).equalsIgnoreCase(scheduleChoice))
                sb.appendColumn(PillpopperDay.getLocalizedString(s.getEnd(), true, R.string._never, sb.getContext()));
            else
                sb.appendColumn("");
        }

        if (s == null) {
            sb.appendColumn(R.string._schedule);
        } else if (s.containsNoData(scheduleChoice)) {
            sb.appendColumn("");
        } else {
            switch (scheduleChoice) {
                case AppConstants.SCHEDULE_CHOICE_SCHEDULED: {
                    StringBuilder schedString = new StringBuilder();

                    schedString.append(dayperiodToName(sb.getContext(), s.getDayPeriod()));
                    if (s.getDayPeriod() == 7) {
                        schedString.append(" (");
                        schedString.append(PillpopperDay.getShortDayNameList(s.getDaysOfWeek()));
                        schedString.append(")");
                    }

                    schedString.append(" ");
                    schedString.append(s.getTimeList().getLocalizedLocalTimeString(sb.getContext()));

                    sb.appendColumn(schedString.toString());
                    break;
                }

                case AppConstants.SCHEDULE_CHOICE_UNDEFINED:
//                    sb.appendColumn(s.intervalAsString(sb.getContext()));
                    sb.appendColumn("");
                    break;

                default:
                    sb.appendColumn("");
                    break;
            }
        }

        if (s == null)
            sb.appendColumn(R.string._limit);
        else if (s.containsNoData(scheduleChoice))
            sb.appendColumn("");
        else{
            if (maxDailyDoses != null && !("").equalsIgnoreCase(maxDailyDoses) && !(("-1").equalsIgnoreCase(maxDailyDoses))) {
                String limitText = String.format(Locale.US, "%s %s",
                        maxDailyDoses,
                        Util.handleParseInt(maxDailyDoses) == 1 ? sb.getContext().getString(R.string._dose) : sb.getContext().getString(R.string._doses));
                sb.appendColumn(limitText);
            }else{
                sb.appendColumn(s.getLimitDescription(sb.getContext()));
            }
        }
    }

    /*public void updateInfoView(PillpopperActivity act) {
        ((TextView) act.findViewById(R.id.druginfo_schedtype_value)).setText(getSchedTypeString(act,getSchedType(), act.getGlobalAppContext(), ""));
        ((TextView) act.findViewById(R.id.druginfo_starting_value)).setText(PillpopperDay.getLocalizedString(getStart(), true, R.string._not_set, act));
        ((TextView) act.findViewById(R.id.druginfo_ending_value)).setText(PillpopperDay.getLocalizedString(getEnd(), true, R.string._never, act));

        switch (getSchedType()) {
            case SCHEDULED: {
                act.findViewById(R.id.druginfo_frequency_row).setVisibility(View.VISIBLE);

                StringBuilder sb = new StringBuilder();
                sb.append(dayperiodToName(act, getDayPeriod()));
                if (getDayPeriod() == 7) {
                    sb.append(" (");
                    sb.append(PillpopperDay.getShortDayNameList(getDaysOfWeek()));
                    sb.append(")");
                }
                ((TextView) act.findViewById(R.id.druginfo_frequency_value)).setText(sb.toString());

                act.findViewById(R.id.druginfo_interval_row).setVisibility(View.GONE);

                act.findViewById(R.id.druginfo_timeofday_row).setVisibility(View.VISIBLE);
                ((TextView) act.findViewById(R.id.druginfo_timeofday_value)).setText(getTimeList().getLocalizedLocalTimeString(act));
                break;
            }

            case INTERVAL:
                act.findViewById(R.id.druginfo_frequency_row).setVisibility(View.GONE);

                act.findViewById(R.id.druginfo_interval_row).setVisibility(View.VISIBLE);
                ((TextView) act.findViewById(R.id.druginfo_interval_value)).setText(intervalAsString(act));

                act.findViewById(R.id.druginfo_timeofday_row).setVisibility(View.GONE);
                break;

            case AS_NEEDED:
                act.findViewById(R.id.druginfo_frequency_row).setVisibility(View.GONE);

                act.findViewById(R.id.druginfo_interval_row).setVisibility(View.GONE);

                act.findViewById(R.id.druginfo_timeofday_row).setVisibility(View.GONE);
                break;
        }

        if (getSchedType() == Schedule.SchedType.INTERVAL || getSchedType() == Schedule.SchedType.AS_NEEDED) {
            act.findViewById(R.id.druginfo_limit_row).setVisibility(View.VISIBLE);
            ((TextView) act.findViewById(R.id.druginfo_limit_value)).setText(getLimitDescription(act));
        } else {
            act.findViewById(R.id.druginfo_limit_row).setVisibility(View.GONE);
        }
    }*/
}
