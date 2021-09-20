package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author
 * Created by adhithyaravipati on 5/18/16.
 */
public class ScheduleMainDrug implements Comparable<ScheduleMainDrug> {

    private String pillId;
    private String pillName;
    private String imageGuid;

    private String genericName;
    private String scientificName;

    private String scheduleType;
    private String scheduledFrequency;

    private String dayPeriod;
    private String dose;

    private PillpopperTime last;
    private PillpopperTime notifyAfter;
    private PillpopperTime effectiveLastTaken;
    private PillpopperTime start;
    private PillpopperTime end;

    private String pillTime;

    private String historyEventAction;

    private ScheduleMainDrugPreference drugPreference;
    private ScheduleMainUser user;
    private ScheduleMainUserPreference userPreference;

    public ScheduleMainDrug() {
        this.drugPreference = new ScheduleMainDrugPreference();
        this.user = new ScheduleMainUser();
        this.userPreference = new ScheduleMainUserPreference();
    }

    public int compareTo(ScheduleMainDrug drug) {
        return genericName.toLowerCase().compareTo(drug.getPillName().toLowerCase());
    }

    public String toString() {
        return "Pill ID: " + pillId + "\n" +
                "Pill Name: " + pillName + "\n" +
                "Generic Name: " + genericName + "\n" +
                "Scientific Name: " + scientificName + "\n" +
                "Dosage: " + dose + "\n" +
                "User ID: " + getUser().getUserId() + "\n" +
                "User Name: " + getUser().getFirstName() + "\n" +
                "Times: " + pillTime + "\n" +
                "DayPeriod: " + dayPeriod + "\n" +
                "Weekdays: " + getDrugPreference().getWeekdays() + "\n" +
                "Archived: " + getDrugPreference().getArchived() + "\n" +
                "Deleted: " + getDrugPreference().getDeleted() + "\n" +
                "Invisible: " + getDrugPreference().getInvisible() + "\n";
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
        int refPoint = null!=pillName?pillName.indexOf("("):-1;
        if(refPoint==-1){
            this.pillName = pillName;
        }else{
            try {
                if(null != pillName) {
                    String drugFirstName = pillName.substring(0,refPoint);
                    if (drugFirstName!=null && !drugFirstName.equals("")) {
                        this.pillName = drugFirstName;
                    }
                }
            }catch (Exception e){
                PillpopperLog.exception(e.getMessage());
            }
        }

        Pattern scientificNamePattern = Pattern.compile("\\((.*?)\\)");
        Matcher scientificNameMatcher = scientificNamePattern.matcher(pillName);

        while (scientificNameMatcher.find()) {
            this.scientificName = scientificNameMatcher.group(1);
        }

        try {
            if (null != pillName) {
                int parenthesisIndex = pillName.indexOf("(");
                if (parenthesisIndex == -1) {
                    this.genericName = pillName;
                } else {
                    this.genericName = pillName.substring(0, parenthesisIndex - 1);
                }
            }
        } catch (Exception ex){
            this.genericName = pillName;
            LoggerUtils.info(ex.getMessage());
        }
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getDayPeriod() {
        return dayPeriod;
    }

    public void setDayPeriod(String dayPeriod) {
        this.dayPeriod = dayPeriod;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public PillpopperTime getLast() {
        return last;
    }

    public void setLast(PillpopperTime last) {
        this.last = last;
    }

    public void setLast(String last) {
        this.last = Util.convertStringtoPillpopperTime(last);
    }

    public PillpopperTime getEffectiveLastTaken() {
        return effectiveLastTaken;
    }

    public void setEffectiveLastTaken(PillpopperTime effectiveLastTaken) {
        this.effectiveLastTaken = effectiveLastTaken;
    }

    public void setEffectiveLastTaken(String effectiveLastTaken) {
        this.effectiveLastTaken = Util.convertStringtoPillpopperTime(effectiveLastTaken);
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

    public PillpopperTime getEnd() {
        return end;
    }

    public void setEnd(PillpopperTime end) {
        this.end = end;
    }

    public void setEnd(String end) {
        this.end = Util.convertStringtoPillpopperTime(end);
    }

    public String getPillTime() {
        return pillTime;
    }

    public void setPillTime(String pillTime) {
        this.pillTime = pillTime;
    }

    public ScheduleMainDrugPreference getDrugPreference() {
        return drugPreference;
    }

    public void setDrugPreference(ScheduleMainDrugPreference drugPreference) {
        this.drugPreference = drugPreference;
    }

    public ScheduleMainUser getUser() {
        return user;
    }

    public void setUser(ScheduleMainUser user) {
        this.user = user;
    }

    public ScheduleMainUserPreference getUserPreference() {
        return userPreference;
    }

    public void setUserPreference(ScheduleMainUserPreference userPreference) {
        this.userPreference = userPreference;
    }

    public String getScheduledFrequency() {
        return scheduledFrequency;
    }

    public void setScheduledFrequency(String scheduledFrequency) {
        this.scheduledFrequency = scheduledFrequency;
    }

    public PillpopperTime getNotifyAfter() {
        return this.notifyAfter;
    }

    public void setNotifyAfter(String notifyAfter) {
        this.notifyAfter = Util.convertStringtoPillpopperTime(notifyAfter);
    }

    public void setNotifyAfter(PillpopperTime notifyAfter) {
        this.notifyAfter = notifyAfter;
    }


    public String getHistoryEventAction() {
        return historyEventAction;
    }

    public void setHistoryEventAction(String historyEventAction) {
        this.historyEventAction = historyEventAction;
    }

    public String getImageGuid() {
        return imageGuid;
    }

    public void setImageGuid(String imageGuid) {
        this.imageGuid = imageGuid;
    }
}
