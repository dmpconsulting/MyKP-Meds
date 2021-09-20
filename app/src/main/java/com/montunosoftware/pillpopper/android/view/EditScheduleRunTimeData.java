package com.montunosoftware.pillpopper.android.view;

import com.montunosoftware.pillpopper.model.Drug;

import java.util.ArrayList;

public class EditScheduleRunTimeData {

    //Medication Duration
    private String meditationDuration;
    private boolean isFromScheduleMed;//on back press form edit medication
    private boolean isReminderAdded;// reminder visibility when changed edit mode
    private boolean asNeededSwitch;
    private boolean isMedSavedClicked;
    private boolean isNLPReminder;

    public boolean isMedSavedClicked() { return isMedSavedClicked; }
    public void setMedSavedClicked(boolean medSavedClicked) { isMedSavedClicked = medSavedClicked; }

    public boolean isReminderAdded() { return isReminderAdded; }
    public void setReminderAdded(boolean reminderAdded) { isReminderAdded = reminderAdded; }

    public boolean isMedicationSaved() { return isMedicationSaved; }
    public void setMedicationSaved(boolean medicationSaved) { isMedicationSaved = medicationSaved; }

    private boolean isMedicationSaved;//handle navigation on back press in edit mode

    public boolean getIsFromScheduleMed() { return isFromScheduleMed; }
    public void setIsFromScheduleMed(boolean isFromScheduleMed) { this.isFromScheduleMed = isFromScheduleMed; }

    public String getMeditationDuration() { return meditationDuration; }
    public void setMeditationDuration(String meditationDuration) { this.meditationDuration = meditationDuration; }


    //Daily Schedule Value
    private ArrayList<String> scheduleTime;
    private Drug selectedDrug;
    private boolean isEditMedicationClicked;
    private String  startDate;
    private String  EndDate;

    public ArrayList<String> getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(ArrayList<String> scheduleTime) { this.scheduleTime = scheduleTime; }

    public Drug getSelectedDrug() { return selectedDrug; }
    public void setSelectedDrug(Drug selectedDrug) { this.selectedDrug = selectedDrug; }

    public boolean isEditMedicationClicked() { return isEditMedicationClicked; }
    public void setEditMedicationClicked(boolean editMedicationClicked) { isEditMedicationClicked = editMedicationClicked; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return EndDate; }
    public void setEndDate(String endDate) { EndDate = endDate; }

    //Weekly Schedule Value
    private String selectedDays;

    public String getSelectedDays() { return selectedDays; }
    public void setSelectedDays(String selectedDays) { this.selectedDays = selectedDays; }

    //Custom Schedule Value
    private String durationType;
    private long duration;

    public String getDurationType() { return durationType; }
    public void setDurationType(String noOfDays) { this.durationType = noOfDays; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public void setAsNeededSwitch(boolean asNeededSwitch) {
        this.asNeededSwitch = asNeededSwitch;
    }

    public boolean getAsNeededSwitch() {
        return asNeededSwitch;
    }

    public boolean isNLPReminder() {
        return isNLPReminder;
    }

    public void setNLPReminder(boolean NLPReminder) {
        isNLPReminder = NLPReminder;
    }
}
