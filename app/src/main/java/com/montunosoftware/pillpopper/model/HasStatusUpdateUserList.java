package com.montunosoftware.pillpopper.model;

/**
 * @author
 * Created by M1024581 on 7/25/2016.
 */
public class HasStatusUpdateUserList {
    private String isPrimary;

    private String medicationScheduleChanged;

    private String userId;

    private String proxyStatusCode;

    private String kphcMedsStatusChanged;

    public String getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(String isPrimary) {
        this.isPrimary = isPrimary;
    }

    public String getMedicationScheduleChanged() {
        return medicationScheduleChanged;
    }

    public void setMedicationScheduleChanged(String medicationScheduleChanged) {
        this.medicationScheduleChanged = medicationScheduleChanged;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProxyStatusCode() {
        return proxyStatusCode;
    }

    public void setProxyStatusCode(String proxyStatusCode) {
        this.proxyStatusCode = proxyStatusCode;
    }

    public String getKphcMedsStatusChanged() {
        return kphcMedsStatusChanged;
    }

    public void setKphcMedsStatusChanged(String kphcMedsStatusChanged) {
        this.kphcMedsStatusChanged = kphcMedsStatusChanged;
    }
}
