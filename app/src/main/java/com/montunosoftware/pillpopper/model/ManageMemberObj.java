package com.montunosoftware.pillpopper.model;


import java.io.Serializable;

/**
 * @author
 * Created by M1024581 on 7/5/2016.
 */
public class ManageMemberObj implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String userFirstName;
    private String userType;
    private String medicationsEnabled;
    private String remindersEnabled;
    private Boolean isTeen;
    private Boolean isTeenToggleEnabled;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getMedicationsEnabled() {
        return medicationsEnabled;
    }

    public void setMedicationsEnabled(String medicationsEnabled) {
        this.medicationsEnabled = medicationsEnabled;
    }

    public String getRemindersEnabled() {
        return remindersEnabled;
    }

    public void setRemindersEnabled(String remindersEnabled) {
        this.remindersEnabled = remindersEnabled;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Boolean getTeen() {
        return isTeen;
    }

    public void setTeen(Boolean teen) {
        isTeen = teen;
    }
    public Boolean getTeenToggleEnabled(){
        return isTeenToggleEnabled;
    }
    public void setTeenToggleEnabled(Boolean teenToggleEnabled){
        this.isTeenToggleEnabled = teenToggleEnabled;
    }
}
