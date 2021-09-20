package com.montunosoftware.pillpopper.model;

/**
 * Created by M1028309 on 12/14/2017.
 */

public class KphcDrug {

    private String pillId;
    private String pillName;
    private String dose;
    private String prescriptionId;
    private String instruction;
    private String userId;
    private String userName;
    private String imageGuid;
    private String databaseNDC;
    private String defaultImageChoice;
    private String defaultServiceImageID;
    private String needFDBUpdate;

    public KphcDrug() {

    }

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }

    public String getPillName() {
        return pillName;
    }

    public void setPillName(String name) {
        this.pillName = name;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageGuid() {
        return imageGuid;
    }

    public void setImageGuid(String imageGuid) {
        this.imageGuid = imageGuid;
    }

    public String getDatabaseNDC() {
        return databaseNDC;
    }

    public void setDatabaseNDC(String databaseNDC) {
        this.databaseNDC = databaseNDC;
    }

    public String getDefaultImageChoice() {
        return defaultImageChoice;
    }

    public void setDefaultImageChoice(String defaultImageChoice) {
        this.defaultImageChoice = defaultImageChoice;
    }

    public String getDefaultServiceImageID() {
        return defaultServiceImageID;
    }

    public void setDefaultServiceImageID(String defaultServiceImageID) {
        this.defaultServiceImageID = defaultServiceImageID;
    }

    public String getNeedFDBUpdate() {
        return needFDBUpdate;
    }

    public void setNeedFDBUpdate(String needFDBUpdate) {
        this.needFDBUpdate = needFDBUpdate;
    }
}

