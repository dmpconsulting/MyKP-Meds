package com.montunosoftware.pillpopper.android.interrupts.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by M1024762 on 8/1/2018.
 */

public class InterruptFailureResponseRoot implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("output")
    private InterruptErrors output;
    @SerializedName("interruptList")
    private String interruptList;
    @SerializedName("userInfo")
    private String userInfo;

    public InterruptErrors getOutput() {
        return output;
    }

    public void setOutput(InterruptErrors output) {
        this.output = output;
    }

    public String getInterruptList() {
        return interruptList;
    }

    public void setInterruptList(String interruptList) {
        this.interruptList = interruptList;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

}
