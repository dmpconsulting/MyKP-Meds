package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by adhithyaravipati on 9/27/16.
 */
public class ArchiveListUserDropDownData {

    private String userId;
    private String userFirstName;

    public ArchiveListUserDropDownData(String userId, String userFirstName) {
        this.setUserId(userId);
        this.setUserFirstName(userFirstName);
    }

    public String toString() {
        return this.getUserId() + " -- " + this.getUserFirstName();
    }


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
}
