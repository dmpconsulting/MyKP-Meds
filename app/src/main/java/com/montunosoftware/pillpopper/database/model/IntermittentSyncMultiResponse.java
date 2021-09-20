package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 5/16/2016.
 */
public class IntermittentSyncMultiResponse {

    private PillpopperMultiResponse pillpopperMultiResponse;

    public PillpopperMultiResponse getPillpopperMultiResponse ()
    {
        return pillpopperMultiResponse;
    }

    public void setPillpopperMultiResponse (PillpopperMultiResponse pillpopperMultiResponse)
    {
        this.pillpopperMultiResponse = pillpopperMultiResponse;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [pillpopperMultiResponse = "+pillpopperMultiResponse+"]";
    }
}
