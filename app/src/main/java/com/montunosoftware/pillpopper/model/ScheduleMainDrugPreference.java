package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.Util;

import java.util.Arrays;

/**
 * @author
 * Created by adhithyaravipati on 5/18/16.
 */
public class ScheduleMainDrugPreference {

    private String archived;
    private String invisible;
    private String deleted;

    private String dosageType;

    private String weekdays;
    private boolean[] activeOnWeekday = new boolean[8];

    private String databaseMedFormType;

    public ScheduleMainDrugPreference() {

    }

    public String getArchived() {
        return archived;
    }

    public void setArchived(String archived) {
        this.archived = archived;
    }

    public String getInvisible() {
        return invisible;
    }

    public void setInvisible(String invisible) {
        this.invisible = invisible;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getDosageType() {
        return dosageType;
    }

    public void setDosageType(String dosageType) {
        this.dosageType = dosageType;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public boolean[] getActiveOnWeekday() {
        return activeOnWeekday;
    }

    public void setWeekdays(String weekdays) {

        this.weekdays = weekdays;

        if(weekdays == null || ("").equalsIgnoreCase(weekdays) || ("null").equalsIgnoreCase(weekdays)) {
            Arrays.fill(activeOnWeekday,true);
        } else {
            String[] weekdayArray = weekdays.split(",");
            Arrays.fill(activeOnWeekday,false);
            for (String s : weekdayArray) {
                activeOnWeekday[Util.handleParseInt(s)] = true;
            }
        }
    }

    public String getDatabaseMedFormType() {
        return databaseMedFormType;
    }

    public void setDatabaseMedFormType(String databaseMedFormType) {
        this.databaseMedFormType = databaseMedFormType;
    }
}
