package com.montunosoftware.pillpopper.android.view;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.montunosoftware.pillpopper.model.BulkSchedule;
import com.montunosoftware.pillpopper.model.Drug;

import java.util.ArrayList;
import java.util.List;

public class ScheduleViewModel extends ViewModel {

    private MutableLiveData<Integer> frequencySelector = new MutableLiveData<>();
    private MutableLiveData<List<Drug>> medicationsList = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> weeklySelectedDays = new MutableLiveData<>();
    private String customFrequency;
    private MutableLiveData<String> customFrequencyNumber = new MutableLiveData<>();
    public MutableLiveData<ArrayList<String>> getWeeklySelectedDays() {
        return weeklySelectedDays;
    }
    private MutableLiveData<ArrayList<String>> customSelectedOptions = new MutableLiveData<>();
    private MutableLiveData<Integer> startDateSelected = new MutableLiveData<>();
    public MutableLiveData<BulkSchedule> bulkScheduleMutableLiveData = new MutableLiveData<>();

    public void getBulkSchedule(BulkSchedule bulkSchedule) {
        bulkScheduleMutableLiveData.postValue(bulkSchedule);
    }

    public MutableLiveData<Integer> getFrequencySelector() {
        return frequencySelector;
    }

    public MutableLiveData<List<Drug>> getMedicationsList() {
        return medicationsList;
    }

    public MutableLiveData<String> getCustomFrequencyNumber() {
        return customFrequencyNumber;
    }

    public String getCustomFrequency() {
        return customFrequency;
    }

    public void setCustomFrequency(String customFrequency) {
        this.customFrequency = customFrequency;
    }

    public MutableLiveData<Integer> getStartDateSelector() {
        return startDateSelected;
    }
}
