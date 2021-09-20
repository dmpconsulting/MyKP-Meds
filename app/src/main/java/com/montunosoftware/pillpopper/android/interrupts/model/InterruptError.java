package com.montunosoftware.pillpopper.android.interrupts.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by M1024762 on 8/1/2018.
 */
public class InterruptError implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("error")
    private List<InterruptErrorCode> error;

    public List<InterruptErrorCode> getError() {
        return error;
    }

    public void setError(List<InterruptErrorCode> error) {
        this.error = error;
    }
}
