package com.montunosoftware.pillpopper.model;

public class NLPSigValidationRequestObj {

    private String pillId;
    private String mobileResponse;
    private String changeInSchedule;

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }

    public String getMobileResponse() {
        return mobileResponse;
    }

    public void setMobileResponse(String mobileResponse) {
        this.mobileResponse = mobileResponse;
    }

    public String getChangeInSchedule() {
        return changeInSchedule;
    }

    public void setChangeInSchedule(String changeInSchedule) {
        this.changeInSchedule = changeInSchedule;
    }
}
