
package com.montunosoftware.pillpopper.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NLPRemindersResponseObj {

    @SerializedName("pillId")
    private String pillId;

    @SerializedName("reminders")
    private List<NLPReminder> mNLPReminders;

    public List<NLPReminder> getNLPReminders() {
        return mNLPReminders;
    }

    public void setNLPReminders(List<NLPReminder> NLPReminders) {
        mNLPReminders = NLPReminders;
    }

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }
}
