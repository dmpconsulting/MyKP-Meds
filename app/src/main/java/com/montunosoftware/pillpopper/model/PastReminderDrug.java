package com.montunosoftware.pillpopper.model;

/**
 * Created by M1023050 on 8/20/2017.
 */

public class PastReminderDrug {

    private String pillTime;
    private String pillID;

    public String getPillID() {
        return pillID;
    }

    public void setPillID(String pillID) {
        this.pillID = pillID;
    }

    public String getPillTime() {
        return pillTime;
    }

    public void setPillTime(String pillTime) {
        this.pillTime = pillTime;
    }

}
