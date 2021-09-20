package com.montunosoftware.pillpopper.service.getstate;

import com.montunosoftware.pillpopper.model.State;

/**
 * @author
 * Created by adhithyaravipati on 8/8/16.
 */
public class StateWrapper {
    private State state;
    private String statusCode;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
