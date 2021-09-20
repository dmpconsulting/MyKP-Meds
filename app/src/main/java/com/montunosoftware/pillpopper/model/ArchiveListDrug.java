package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.Util;

/**
 * @author
 * Created by adhithyaravipati on 6/1/16.
 */
public class ArchiveListDrug implements Comparable<ArchiveListDrug> {

    private String pillId;
    private String pillName;
    private String dose;

    private String genericName;
    private String brandName;

    private String userId;
    private String notes;

    private boolean managed;

    private String imageGuid;

    public ArchiveListDrug() {

    }

    public int compareTo(ArchiveListDrug drug) {
        return brandName.toLowerCase().compareTo(drug.getBrandName().toLowerCase());
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

    public void setPillName(String drugName) {

        if (null != drugName) {
            int refPoint = drugName.indexOf("(");

            if (refPoint == -1) {
                this.brandName = drugName;
            } else {
                this.brandName = drugName.substring(0, refPoint);
                String genericName = drugName.substring(refPoint);
                if (!Util.isEmptyString(genericName)) {
                    genericName = genericName.replaceAll("\\(", "");
                    genericName = genericName.replaceAll("\\)", "");
                    this.genericName = genericName;
                }
            }
        }
    }

    public String getGenericName() {
        return genericName;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    public String getImageGuid() {
        return imageGuid;
    }

    public void setImageGuid(String imageGuid) {
        this.imageGuid = imageGuid;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(boolean managed) {
        this.managed = managed;
    }
}
