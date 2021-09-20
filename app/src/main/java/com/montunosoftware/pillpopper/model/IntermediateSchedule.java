package com.montunosoftware.pillpopper.model;

import org.json.JSONArray;

/**
 * Created by M1023050 on 4/25/2016.
 *
 * This is the intermediate Schedule model object for passing the data from the DB layer
 */
public class IntermediateSchedule {

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    private String days;

    private String type;

    public String getDayperiod() {
        return dayperiod;
    }

    public void setDayperiod(String dayperiod) {
        this.dayperiod = dayperiod;
    }

    private String dayperiod;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    private String start;
    private JSONArray sheduleTimeList;
    private String end;

    public JSONArray getSheduleTimeList() {
        return sheduleTimeList;
    }

    public void setSheduleTimeList(JSONArray sheduleTimeList) {
        this.sheduleTimeList = sheduleTimeList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }




}
