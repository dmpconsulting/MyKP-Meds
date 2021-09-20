package com.montunosoftware.pillpopper.model;

import java.io.Serializable;
import java.util.List;

public class BulkSchedule implements Serializable {

    private String userId;
    private String scheduledStartDate;
    private String scheduledEndDate;
    private List<String> pillIdList;
    private String scheduledFrequency;
    private String scheduledType;
    private int scheduledForEvery;
    private String daysSelectedForWeekly;
    private List<Integer> scheduledTimeList;
    private String dayPeriod;
    private String scheduleGUID;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScheduledStartDate() {
        return scheduledStartDate;
    }

    public void setScheduledStartDate(String scheduledStartDate) {
        this.scheduledStartDate = scheduledStartDate;
    }

    public String getScheduledEndDate() {
        return scheduledEndDate;
    }

    public void setScheduledEndDate(String scheduledEndDate) {
        this.scheduledEndDate = scheduledEndDate;
    }

    public List<String> getPillIdList() {
        return pillIdList;
    }

    public void setPillIdList(List<String> pillIdList) {
        this.pillIdList = pillIdList;
    }

    public String getScheduledFrequency() {
        return scheduledFrequency;
    }

    public void setScheduledFrequency(String scheduledFrequency) {
        this.scheduledFrequency = scheduledFrequency;
    }

    public String getScheduledType() {
        return scheduledType;
    }

    public void setScheduledType(String scheduledType) {
        this.scheduledType = scheduledType;
    }

    public int getScheduledForEvery() {
        return scheduledForEvery;
    }

    public void setScheduledForEvery(int scheduledForEvery) {
        this.scheduledForEvery = scheduledForEvery;
    }

    public String getDaysSelectedForWeekly() {
        return daysSelectedForWeekly;
    }

    public void setDaysSelectedForWeekly(String daysSelectedForWeekly) {
        this.daysSelectedForWeekly = daysSelectedForWeekly;
    }

    public List<Integer> getScheduledTimeList() {
        return scheduledTimeList;
    }

    public void setScheduledTimeList(List<Integer> scheduledTimeList) {
        this.scheduledTimeList = scheduledTimeList;
    }

    public String getDayPeriod() {
        return dayPeriod;
    }

    public void setDayPeriod(String dayPeriod) {
        this.dayPeriod = dayPeriod;
    }

    public String getScheduleGUID() {
        return scheduleGUID;
    }

    public void setScheduleGUID(String scheduleGUID) {
        this.scheduleGUID = scheduleGUID;
    }
}
