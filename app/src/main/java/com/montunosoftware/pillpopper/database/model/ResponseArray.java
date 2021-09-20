package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 5/16/2016.
 */
public class ResponseArray {

    public IntermittentMultiPillpopperResponse getMultiPillpopperResponse() {
        return pillpopperResponse;
    }

    public void setMultiPillpopperResponse(IntermittentMultiPillpopperResponse multiPillpopperResponse) {
        this.pillpopperResponse = multiPillpopperResponse;
    }

    private IntermittentMultiPillpopperResponse pillpopperResponse;


}
