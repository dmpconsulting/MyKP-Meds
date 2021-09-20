package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 3/15/2016.
 */
public class GetHistoryEvents {
    private String opId;

    private String deleted;

    private String guid;

    private String updateUserId;

    private String creationDate;

    private String operation;

    private String pillId;

    private String eventDescription;

    private String scheduleDate;

    private String scheduleDate_tz_secs;

    private String editTime;

    private String personId;

    private String operationData;

    private String createUserId;

    private String pillName;

    private String tz_secs;

    private String tz_name;

    public String getTz_secs() {
        return tz_secs;
    }

    public void setTz_secs(String tz_secs) {
        this.tz_secs = tz_secs;
    }

    private GetHistoryPreferences preferences;

    public String getOpId ()
    {
        return opId;
    }

    public void setOpId (String opId)
    {
        this.opId = opId;
    }

    public String getDeleted ()
    {
        return deleted;
    }

    public void setDeleted (String deleted)
    {
        this.deleted = deleted;
    }

    public String getGuid ()
    {
        return guid;
    }

    public void setGuid (String guid)
    {
        this.guid = guid;
    }

    public String getUpdateUserId ()
    {
        return updateUserId;
    }

    public void setUpdateUserId (String updateUserId)
    {
        this.updateUserId = updateUserId;
    }

    public String getCreationDate ()
    {
        return creationDate;
    }

    public void setCreationDate (String creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getOperation ()
    {
        return operation;
    }

    public void setOperation (String operation)
    {
        this.operation = operation;
    }

    public String getPillId ()
    {
        return pillId;
    }

    public void setPillId (String pillId)
    {
        this.pillId = pillId;
    }

    public String getEventDescription ()
    {
        return eventDescription;
    }

    public void setEventDescription (String eventDescription)
    {
        this.eventDescription = eventDescription;
    }

    public String getScheduleDate ()
    {
        return scheduleDate;
    }

    public void setScheduleDate (String scheduleDate)
    {
        this.scheduleDate = scheduleDate;
    }

    public String getEditTime ()
    {
        return editTime;
    }

    public void setEditTime (String editTime)
    {
        this.editTime = editTime;
    }

    public String getPersonId ()
    {
        return personId;
    }

    public void setPersonId (String personId)
    {
        this.personId = personId;
    }

    public String getOperationData ()
    {
        return operationData;
    }

    public void setOperationData (String operationData)
    {
        this.operationData = operationData;
    }

    public String getCreateUserId ()
    {
        return createUserId;
    }

    public void setCreateUserId (String createUserId)
    {
        this.createUserId = createUserId;
    }

    public GetHistoryPreferences getPreferences ()
    {
        return preferences;
    }

    public void setPreferences (GetHistoryPreferences preferences)
    {
        this.preferences = preferences;
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        this.pillName = pillName;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [opId = "+opId+", deleted = "+deleted+", guid = "+guid+", updateUserId = "+updateUserId+", creationDate = "+creationDate+", operation = "+operation+", pillId = "+pillId+", eventDescription = "+eventDescription+", scheduleDate = "+scheduleDate+", editTime = "+editTime+", personId = "+personId+", pillName = "+pillName+", operationData = "+operationData+", createUserId = "+createUserId+", preferences = "+preferences+"]";
    }

    public String getTz_name() {
        return tz_name;
    }

    public void setTz_name(String tz_name) {
        this.tz_name = tz_name;
    }

    public String getScheduleDateTZsecs() {
        return scheduleDate_tz_secs;
    }

    public void setScheduleDateTZsecs(String scheduleDate_tz_secs) {
        this.scheduleDate_tz_secs = scheduleDate_tz_secs;
    }

}
