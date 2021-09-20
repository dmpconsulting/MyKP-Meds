package com.montunosoftware.pillpopper.android.interrupts.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by M1024762 on 8/1/2018.
 */
public class InterruptErrors implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("errors")
    private InterruptError errors;

    public InterruptError getErrors() {
        return errors;
    }

    public void setErrors(InterruptError errors) {
        this.errors = errors;
    }

}
