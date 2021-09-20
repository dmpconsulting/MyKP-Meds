package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 5/16/2016.
 */
import java.util.Arrays;
public class PillpopperMultiResponse {

    private ResponseArray[] responseArray;

    public ResponseArray[] getResponseArray ()
    {
        return responseArray;
    }

    public void setResponseArray (ResponseArray[] responseArray)
    {
        this.responseArray = responseArray;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [responseArray = "+ Arrays.toString(responseArray) +"]";
    }
}
