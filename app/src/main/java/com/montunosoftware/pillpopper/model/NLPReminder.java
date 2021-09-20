
package com.montunosoftware.pillpopper.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class NLPReminder {

    @SerializedName("dosage")
    private String mDosage;
    @SerializedName("endDate")
    private String mEndDate;
    @SerializedName("every")
    private String mEvery;
    @SerializedName("frequency")
    private String mFrequency;
    @SerializedName("medicine")
    private String mMedicine;
    @SerializedName("reminderTimes")
    private ArrayList<String> mReminderTimes;
    @SerializedName("sigId")
    private String mSigId;
    @SerializedName("startDate")
    private String mStartDate;

    public String getDosage() {
        return mDosage;
    }

    public void setDosage(String dosage) {
        mDosage = dosage;
    }

    public String getEndDate() {
        return mEndDate;
    }

    public void setEndDate(String endDate) {
        mEndDate = endDate;
    }

    public String getEvery() {
        return mEvery;
    }

    public void setEvery(String every) {
        mEvery = every;
    }

    public String getFrequency() {
        return mFrequency;
    }

    public void setFrequency(String frequency) {
        mFrequency = frequency;
    }

    public String getMedicine() {
        return mMedicine;
    }

    public void setMedicine(String medicine) {
        mMedicine = medicine;
    }

    public ArrayList<String> getReminderTimes() {
        return mReminderTimes;
    }

    public void setReminderTimes(ArrayList<String> reminderTimes) {
        mReminderTimes = reminderTimes;
    }

    public String getSigId() {
        return mSigId;
    }

    public void setSigId(String sigId) {
        mSigId = sigId;
    }

    public String getStartDate() {
        return mStartDate;
    }

    public void setStartDate(String startDate) {
        mStartDate = startDate;
    }

}
