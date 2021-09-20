package com.montunosoftware.pillpopper.database.model;

import java.io.Serializable;

/**
 * @author
 * Created by M1023050 on 3/15/2016.
 */
public class GetHistoryPreferences implements Serializable {

    private String dosageType;
    private String managedDescription;
    private String customDescription;
    private String scheduleFrequency;
    private String finalPostponedDateTime;
    private boolean isPostponedEventActive;
    private String actionDate;
    private String recordDate;
    private String start;
    private String end;
    private String scheduleChoice;
    private String dayperiod;
    private String weekdays;
    private String scheduleDate;
    private String scheduleGuid;

    public String getActionDate() {
        return actionDate;
    }

    public void setActionDate(String actionDate) {
        this.actionDate = actionDate;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public String getDosageType ()
    {
        return dosageType;
    }

    public void setDosageType (String dosageType)
    {
        this.dosageType = dosageType;
    }

    public String getManagedDescription ()
    {
        return managedDescription;
    }

    public void setManagedDescription (String managedDescription)
    {
        this.managedDescription = managedDescription;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [dosageType = "+dosageType+", managedDescription = "+managedDescription+",customDescription="+customDescription+"]";
    }

    public String getCustomDescription() {
        return customDescription;
    }

    public void setCustomDescription(String customDescription) {
        this.customDescription = customDescription;
    }

    public String getScheduleFrequency() {
        return scheduleFrequency;
    }

    public void setScheduleFrequency(String scheduleFrequency) {
        this.scheduleFrequency = scheduleFrequency;
    }

    public String getFinalPostponedDateTime() {
        return finalPostponedDateTime;
    }

    public void setFinalPostponedDateTime(String finalPostponedDateTime) {
        this.finalPostponedDateTime = finalPostponedDateTime;
    }

    public boolean isPostponedEventActive() {
        return isPostponedEventActive;
    }

    public void setPostponedEventActive(boolean postponedEventActive) {
        isPostponedEventActive = postponedEventActive;
    }

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

    public String getScheduleChoice() {
        return scheduleChoice;
    }

    public void setScheduleChoice(String scheduleChoice) {
        this.scheduleChoice = scheduleChoice;
    }

    public String getDayperiod() {
        return dayperiod;
    }

    public void setDayperiod(String dayperiod) {
        this.dayperiod = dayperiod;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays;
    }

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getScheduleGuid() {
        return scheduleGuid;
    }

    public void setScheduleGuid(String scheduleGuid) {
        this.scheduleGuid = scheduleGuid;
    }
}
