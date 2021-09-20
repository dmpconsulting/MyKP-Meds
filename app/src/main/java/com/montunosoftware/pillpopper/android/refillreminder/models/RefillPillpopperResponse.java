package com.montunosoftware.pillpopper.android.refillreminder.models;

/**
 * Created by M1023050 on 2/21/2018.
 */

public class RefillPillpopperResponse {

    private String replayId;

    private String apiVersion;

    private String dataSyncResult;

    private String userId;

    private ReminderList[] reminderList;

    private String action;

    private String pillpopperVersion;

    private String errorStatus;

    public String getReplayId ()
    {
        return replayId;
    }

    public void setReplayId (String replayId)
    {
        this.replayId = replayId;
    }

    public String getApiVersion ()
    {
        return apiVersion;
    }

    public void setApiVersion (String apiVersion)
    {
        this.apiVersion = apiVersion;
    }

    public String getDataSyncResult ()
    {
        return dataSyncResult;
    }

    public void setDataSyncResult (String dataSyncResult)
    {
        this.dataSyncResult = dataSyncResult;
    }

    public String getUserId ()
    {
        return userId;
    }

    public void setUserId (String userId)
    {
        this.userId = userId;
    }

    public ReminderList[] getReminderList ()
    {
        return reminderList;
    }

    public void setReminderList (ReminderList[] reminderList)
    {
        this.reminderList = reminderList;
    }

    public String getAction ()
    {
        return action;
    }

    public void setAction (String action)
    {
        this.action = action;
    }

    public String getPillpopperVersion ()
    {
        return pillpopperVersion;
    }

    public void setPillpopperVersion (String pillpopperVersion)
    {
        this.pillpopperVersion = pillpopperVersion;
    }

    public String getErrorStatus ()
    {
        return errorStatus;
    }

    public void setErrorStatus (String errorStatus)
    {
        this.errorStatus = errorStatus;
    }

}
