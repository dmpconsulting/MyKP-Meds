package com.montunosoftware.pillpopper.model;

/**
 * @author
 * Created by adhithyaravipati on 5/18/16.
 */
public class ScheduleMainUser {
    private String userId;
    private String userType;
    private String enabled;
    private String firstName;

    public ScheduleMainUser() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
