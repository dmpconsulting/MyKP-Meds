package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 3/14/2016.
 */
public class PillPreferences {

    private String language;
    private String lastManagedIdNotified;
    private String managedDropped;
    private String databaseNDC;
    private String prescriptionNum;
    private String invisible = "0";
    private String managedMedicationId;
    private String lastManagedIdNeedingNotify;
    private String dosageType;
    private String managedDescription;
    private String refillAlertDoses;
    private String refillsRemaining;
    private String limitType;
    private String refillQuantity;
    private String remainingQuantity;
    private String personId;
    private String databaseMedFormType;
    private String customDosageID;
    private String maxNumDailyDoses;
    private String deleted = "0";
    private String customDescription;
    private String archived  = "0";
    private String secondaryReminders;
    private String weekdays;
    private String logMissedDoses;
    private String noPush;
    private String imageGUID;
    private String defaultImageChoice;
    private String defaultServiceImageID;
    private String needFDBUpdate;
    private String notes;
    private String missedDosesLastChecked;
    private String missedDosesLastChecked_tz_secs;
    private String scheduleChanged_tz_secs;
    private String scheduleChoice;
    private String scheduleFrequency;

    public String getPharmacyCount() {
        return pharmacyCount;
    }

    public void setPharmacyCount(String pharmacyCount) {
        this.pharmacyCount = pharmacyCount;
    }

    private String pharmacyCount;

    public String getDoctorCount() {
        return doctorCount;
    }

    public void setDoctorCount(String doctorCount) {
        this.doctorCount = doctorCount;
    }

    private String doctorCount;


    public String getSecondaryReminders() {
        return secondaryReminders;
    }

    public void setSecondaryReminders(String secondaryReminders) {
        this.secondaryReminders = secondaryReminders;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays;
    }

    public String getLogMissedDoses() {
        return logMissedDoses;
    }

    public void setLogMissedDoses(String logMissedDoses) {
        this.logMissedDoses = logMissedDoses;
    }

    public String getNoPush() {
        return noPush;
    }

    public void setNoPush(String noPush) {
        this.noPush = noPush;
    }

    public String getImageGUID() {
        return imageGUID;
    }

    public void setImageGUID(String imageGUID) {
        this.imageGUID = imageGUID;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMissedDosesLastChecked() {
        return missedDosesLastChecked;
    }

    public void setMissedDosesLastChecked(String missedDosesLastChecked) {
        this.missedDosesLastChecked = missedDosesLastChecked;
    }

    public String getArchived() {
        return archived;
    }

    public void setArchived(String archived) {
        this.archived = archived;
    }

    public String getRefillAlertDoses() {
        return refillAlertDoses;
    }

    public void setRefillAlertDoses(String refillAlertDoses) {
        this.refillAlertDoses = refillAlertDoses;
    }

    public String getRefillsRemaining() {
        return refillsRemaining;
    }

    public void setRefillsRemaining(String refillsRemaining) {
        this.refillsRemaining = refillsRemaining;
    }

    public String getLimitType() {
        return limitType;
    }

    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }

    public String getRefillQuantity() {
        return refillQuantity;
    }

    public void setRefillQuantity(String refillQuantity) {
        this.refillQuantity = refillQuantity;
    }

    public String getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(String remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getDatabaseMedFormType() {
        return databaseMedFormType;
    }

    public void setDatabaseMedFormType(String databaseMedFormType) {
        this.databaseMedFormType = databaseMedFormType;
    }

    public String getCustomDosageID() {
        return customDosageID;
    }

    public void setCustomDosageID(String customDosageID) {
        this.customDosageID = customDosageID;
    }

    public String getMaxNumDailyDoses() {
        return maxNumDailyDoses;
    }

    public void setMaxNumDailyDoses(String maxNumDailyDoses) {
        this.maxNumDailyDoses = maxNumDailyDoses;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getCustomDescription() {
        return customDescription;
    }

    public void setCustomDescription(String customDescription) {
        this.customDescription = customDescription;
    }

    public String getLastManagedIdNotified() {
        return lastManagedIdNotified;
    }

    public void setLastManagedIdNotified(String lastManagedIdNotified) {
        this.lastManagedIdNotified = lastManagedIdNotified;
    }

    public String getManagedDropped() {
        return managedDropped;
    }

    public void setManagedDropped(String managedDropped) {
        this.managedDropped = managedDropped;
    }

    public String getDatabaseNDC() {
        return databaseNDC;
    }

    public void setDatabaseNDC(String databaseNDC) {
        this.databaseNDC = databaseNDC;
    }

    public String getPrescriptionNum() {
        return prescriptionNum;
    }

    public void setPrescriptionNum(String prescriptionNum) {
        this.prescriptionNum = prescriptionNum;
    }

    public String getInvisible() {
        return invisible;
    }

    public void setInvisible(String invisible) {
        this.invisible = invisible;
    }

    public String getManagedMedicationId() {
        return managedMedicationId;
    }

    public void setManagedMedicationId(String managedMedicationId) {
        this.managedMedicationId = managedMedicationId;
    }

    public String getLastManagedIdNeedingNotify() {
        return lastManagedIdNeedingNotify;
    }

    public void setLastManagedIdNeedingNotify(String lastManagedIdNeedingNotify) {
        this.lastManagedIdNeedingNotify = lastManagedIdNeedingNotify;
    }

    public String getDosageType() {
        return dosageType;
    }

    public void setDosageType(String dosageType) {
        this.dosageType = dosageType;
    }

    public String getManagedDescription() {
        return managedDescription;
    }

    public void setManagedDescription(String managedDescription) {
        this.managedDescription = managedDescription;
    }

    public String getLanguage ()
    {
        return language;
    }

    public void setLanguage (String language)
    {
        this.language = language;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [language = "+language+"]";
    }

    public String getMissedDosesLastCheckedTZsecs() {
        return missedDosesLastChecked_tz_secs;
    }

    public void setMissedDosesLastCheckedTZsecs(String missedDosesLastChecked_tz_secs) {
        this.missedDosesLastChecked_tz_secs = missedDosesLastChecked_tz_secs;
    }

    public String getScheduleDateChnagedTZsecs() {
        return scheduleChanged_tz_secs;
    }

    public void setScheduleDateChnagedTZsecs(String scheduleChanged_tz_secs) {
        this.scheduleChanged_tz_secs = scheduleChanged_tz_secs;
    }

    public String getDefaultImageChoice() {
        return defaultImageChoice;
    }

    public void setDefaultImageChoice(String defaultImageChoice) {
        this.defaultImageChoice = defaultImageChoice;
    }

    public String getDefaultServiceImageID() {
        return defaultServiceImageID;
    }

    public void setDefaultServiceImageID(String defaultServiceImageID) {
        this.defaultServiceImageID = defaultServiceImageID;
    }

    public String getNeedFDBUpdate() {
        return needFDBUpdate;
    }

    public void setNeedFDBUpdate(String needFDBUpdate) {
        this.needFDBUpdate = needFDBUpdate;
    }

    public String getScheduleChoice() {
        return scheduleChoice;
    }

    public void setScheduleChoice(String scheduleChoice) {
        this.scheduleChoice = scheduleChoice;
    }

    public String getScheduleFrequency() {
        return scheduleFrequency;
    }

    public void setScheduleFrequency(String scheduleFrequency) {
        this.scheduleFrequency = scheduleFrequency;
    }
}
