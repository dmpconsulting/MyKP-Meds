package com.montunosoftware.pillpopper.android;

import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.HourMinute;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperTime;

/**
 * @author
 * Created by adhithyaravipati on 5/20/16.
 */
public class ScheduleMainTimeHeader {

    private int hours;
    private int minutes;
    private String amPm;

    private PillpopperTime headerPillpopperTime;

    public ScheduleMainTimeHeader(String pillTime, PillpopperDay scheduleTimeHeaderFocusDay) {
        this.hours = Util.handleParseInt(pillTime) / 100;
        this.minutes = Util.handleParseInt(pillTime) % 100;

        headerPillpopperTime = scheduleTimeHeaderFocusDay.atLocalTime(new HourMinute(this.hours, this.minutes));

        this.hours = headerPillpopperTime.getLocalHourMinute().getHour();
        this.minutes = headerPillpopperTime.getLocalHourMinute().getMinute();

        if(this.hours == 0) {
            this.hours = 12;
            this.amPm = Util.getSystemAMFormat();
        }else if(this.hours > 0 && this.hours < 12) {
            this.amPm = Util.getSystemAMFormat();
        } else if(this.hours > 12 && this.hours <= 23) {
            this.hours = this.hours - 12;
            this.amPm = Util.getSystemPMFormat();
        } else if(this.hours == 12) {
            this.amPm = Util.getSystemPMFormat();
        }
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getAmPm() {
        return amPm;
    }

    public PillpopperTime getHeaderPillpopperTime() {
        return headerPillpopperTime;
    }

    public String toString() {
        return String.format("%d",this.hours)
                + ":"
                + String.format("%02d",this.minutes)
                + " "
                + amPm;
    }
}
