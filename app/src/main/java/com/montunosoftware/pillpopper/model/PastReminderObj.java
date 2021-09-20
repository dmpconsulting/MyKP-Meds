package com.montunosoftware.pillpopper.model;

/**
 * Created by M1023050 on 12/28/2017.
 */

public class PastReminderObj {

    private String pillID;
    private String pillName;
    private String pillDose;
    private String pillTime;
    private String userFirstName;
    private String userID;

    public String getPillID() {
        return pillID;
    }

    public void setPillID(String pillID) {
        this.pillID = pillID;
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        this.pillName = pillName;
    }

    public String getPillDose() {
        return pillDose;
    }

    public void setPillDose(String pillDose) {
        this.pillDose = pillDose;
    }

    public String getPillTime() {
        return pillTime;
    }

    public void setPillTime(String pillTime) {
        this.pillTime = pillTime;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}
