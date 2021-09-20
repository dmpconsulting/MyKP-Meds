package com.montunosoftware.pillpopper.database.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @author
 * Created by M1024581 on 6/14/2016.
 */
public class HistoryEvent implements Serializable {

    private String pillName;
    private String historyEventGuid;
    private String headerTime;
    private String dosage;
    private String operationStatus;
    private String drugId;
    private String drugEventTime;
    private String drugAction;
    private String notes;
    private String customDescription;
    private String managedDescription;
    private String dosageType;
    private String actionType;

    public GetHistoryPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(GetHistoryPreferences preferences) {
        this.preferences = preferences;
    }

    private GetHistoryPreferences preferences;
    public String getPillID() {
        return pillID;
    }

    public void setPillID(String pillID) {
        this.pillID = pillID;
    }

    private String pillID;

    public String getTzSecs() {
        return tzSecs;
    }

    public void setTzSecs(String tzSecs) {
        this.tzSecs = tzSecs;
    }

    private String tzSecs;

    public String getHistoryEventGuid() {
        return historyEventGuid;
    }

    public void setHistoryEventGuid(String historyEventGuid) {
        this.historyEventGuid = historyEventGuid;
    }

    public String getHeaderTime() {
        return headerTime;
    }

    public void setHeaderTime(String headerTime) {
        this.headerTime = headerTime;
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        if (null != pillName) {
            int refPoint = pillName.contains("(") ? pillName.indexOf("(") : -1;
            this.pillName =  refPoint == -1 ? pillName : pillName.substring(0, refPoint);
        }
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public String getDrugEventTime() {
        return drugEventTime;
    }

    public void setDrugEventTime(String drugEventTime) {
        this.drugEventTime = drugEventTime;
    }

    public String getDrugAction() {
        return drugAction;
    }

    public void setDrugAction(String drugAction) {
        this.drugAction = drugAction;
    }

public String getNotes() { return notes; }

public void setNotes(String notes) {this.notes = notes;}


    public String getCustomDescription() {
        return customDescription;
    }

    public void setCustomDescription(String customDescription) {
        this.customDescription = customDescription;
    }

    public String getManagedDescription() {
        return managedDescription;
    }

    public void setManagedDescription(String managedDescription) {
        this.managedDescription = managedDescription;
    }

    public String getDosageType() {
        return dosageType;
    }

    public void setDosageType(String dosageType) {
        this.dosageType = dosageType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
