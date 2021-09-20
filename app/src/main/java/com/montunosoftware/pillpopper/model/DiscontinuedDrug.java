package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.Util;

/**
 * @author
 * Created by adhithyaravipati on 7/25/16.
 */
public class DiscontinuedDrug {

    private String pillId;

    private String name;
    private String brandName;
    private String genericName;

    private String dosage;
    private String userFirstName;
    private String userId;

    private String scheduledType;

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }

    public void setName(String name) {
        this.name = name;
        if (null != name) {
            int refPoint = name.contains("(") ? name.indexOf("(") : -1;

            if (refPoint == -1) {
                this.brandName = name;
            } else {
                this.brandName = name.substring(0, refPoint);
                String genericName = name.substring(refPoint);
                if (!Util.isEmptyString(genericName)) {
                    genericName = genericName.replaceAll("\\(", "");
                    genericName = genericName.replaceAll("\\)", "");
                    this.genericName = genericName;
                }
            }
        }
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getName() {
        return name;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScheduledType() {
        return scheduledType;
    }

    public void setScheduledType(String scheduledType) {
        this.scheduledType = scheduledType;
    }
}
