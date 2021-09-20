package com.montunosoftware.pillpopper.model;

/**
 * @author
 * Created by M1024581 on 7/25/2016.
 */
public class HasStatusUpdatePillpopperResponse {
    private String replayId;

    private String medArchivedOrRemoved;

    private String medicationScheduleChanged;

    private String action;

    private String proxyStatusCode;

    private String pillpopperVersion;

    private HasStatusUpdateUserList[] userList;

    private String kphcMedsStatusChanged;

    public String getReplayId() {
        return replayId;
    }

    public void setReplayId(String replayId) {
        this.replayId = replayId;
    }

    public String getMedArchivedOrRemoved() {
        return medArchivedOrRemoved;
    }

    public void setMedArchivedOrRemoved(String medArchivedOrRemoved) {
        this.medArchivedOrRemoved = medArchivedOrRemoved;
    }

    public String getMedicationScheduleChanged() {
        return medicationScheduleChanged;
    }

    public void setMedicationScheduleChanged(String medicationScheduleChanged) {
        this.medicationScheduleChanged = medicationScheduleChanged;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getProxyStatusCode() {
        return proxyStatusCode;
    }

    public void setProxyStatusCode(String proxyStatusCode) {
        this.proxyStatusCode = proxyStatusCode;
    }

    public String getPillpopperVersion() {
        return pillpopperVersion;
    }

    public void setPillpopperVersion(String pillpopperVersion) {
        this.pillpopperVersion = pillpopperVersion;
    }

    public HasStatusUpdateUserList[] getUserList() {
        return userList;
    }

    public void setUserList(HasStatusUpdateUserList[] userList) {
        this.userList = userList;
    }

    public String getKphcMedsStatusChanged() {
        return kphcMedsStatusChanged;
    }

    public void setKphcMedsStatusChanged(String kphcMedsStatusChanged) {
        this.kphcMedsStatusChanged = kphcMedsStatusChanged;
    }
}
