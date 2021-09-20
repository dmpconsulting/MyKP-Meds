package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.Util;

import java.util.ArrayList;

/**
 * @author
 * Created by adhithyaravipati on 6/3/16.
 */
public class ArchiveDetailDrug {

    private String userFirstName;
    private String pillId;
    private String pillName;
    private String dose;
    private String genericName;
    private String brandName;
    private String dayPeriod;
    private String pillNotes;
    private String pillInstructions;
    private String pillRxNumber;
    private PillpopperTime start;
    private ArrayList<String> pillScheduleTimes;
    private String[] weekdays;
    private String scheduledFrequency;
    private boolean managed;
    private String imageGuid;
    private String doseAndProxyName;
    private String scheduleTime;

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getDayPeriodTime() {
        return dayPeriodTime;
    }

    public void setDayPeriodTime(String dayPeriodTime) {
        this.dayPeriodTime = dayPeriodTime;
    }

    private String dayPeriodTime;
    public String getDoseAndProxyName() {
        return doseAndProxyName;
    }

    public void setDoseAndProxyName() {
        StringBuilder mDoseAndProxyNameBuilder = new StringBuilder();
        if(!Util.isEmptyString(getDose())) {
            mDoseAndProxyNameBuilder.append(getDose());
            mDoseAndProxyNameBuilder.append(" ");
        }
        if(!Util.isEmptyString(getUserFirstName())) {
            mDoseAndProxyNameBuilder.append("(for ")
                    .append(getUserFirstName().toUpperCase())
                    .append(")");
        }
        this.doseAndProxyName = mDoseAndProxyNameBuilder.toString();
    }

    public ArchiveDetailDrug() {
        pillScheduleTimes = new ArrayList<>();
    }

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        this.pillName = pillName;
        if (null != pillName) {
            int refPoint = pillName.contains("(") ? pillName.indexOf("(") : -1;

            if (refPoint == -1) {
                this.brandName = pillName;
            } else {
                this.brandName = pillName.substring(0, refPoint);
                String genericName = pillName.substring(refPoint);
                if (!Util.isEmptyString(genericName)) {
                    genericName = genericName.replaceAll("\\(", "");
                    genericName = genericName.replaceAll("\\)", "");
                    this.genericName = genericName;
                }
            }
        }
    }

    public String getGenericName() {
        if(null != genericName && !genericName.equals(""))
        {
            return genericName;
        }
        else {
            return "";
        }

    }

    public String getBrandName() {
        if (null != brandName && !brandName.equals("")) {
            return brandName;
        } else {
            return "";
        }

    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getNotes() {
        return pillNotes;
    }

    public void setNotes(String pillNotes) {
        this.pillNotes = pillNotes;
    }

    public String getInstructions() {
        return pillInstructions;
    }

    public void setInstructions(String pillInstructions) {
        this.pillInstructions = pillInstructions;
    }

    public String getRxNumber() {
        return pillRxNumber;
    }

    public void setRxNumber(String pillRxNumber) {
        this.pillRxNumber = pillRxNumber;
    }

    public String getDayPeriod() {
        return dayPeriod;
    }

    public void setDayPeriod(String dayPeriod) {
        this.dayPeriod = dayPeriod;
    }

    public void addScheduleTime(String hhMM) {
        int minute = Util.handleParseInt(hhMM) % 100;
        int hour = Util.handleParseInt(hhMM) / 100;
        pillScheduleTimes.add(Util.getFormatted12HourTime(hour, minute));
    }

    public ArrayList<String> getPillScheduleTimes() {
        return this.pillScheduleTimes;
    }

    public void setManaged(boolean isManaged) {
        this.managed = isManaged;
    }

    public boolean isManaged() {
        return this.managed;
    }

    public void setWeekdays(String weekdays) {
        if(Util.isEmptyString(weekdays)) {
            this.weekdays =null;
        } else {
            this.weekdays = weekdays.split(",");
        }
    }

    public String[] getWeekdays() {
        return this.weekdays;
    }

    public PillpopperTime getStart() {
        return start;
    }

    public void setStart(PillpopperTime start) {
        this.start = start;
    }

    public void setStart(String start) {
        this.start = Util.convertStringtoPillpopperTime(start);
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getScheduledFrequency() {
        return scheduledFrequency;
    }

    public void setScheduledFrequency(String scheduledFrequency) {
        this.scheduledFrequency = scheduledFrequency;
    }

    public String getImageGuid() {
        return imageGuid;
    }

    public void setImageGuid(String imageGuid) {
        this.imageGuid = imageGuid;
    }
}
