package com.montunosoftware.pillpopper.android.interrupts.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by M1024762 on 8/1/2018.
 */

public class InterruptErrorCode implements Serializable{
    private static final long serialVersionUID = 1L;

    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;


    public String getCode() {
        return code;
    }

    public void setCode(String code)     {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
