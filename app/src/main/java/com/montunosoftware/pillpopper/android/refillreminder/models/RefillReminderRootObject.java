package com.montunosoftware.pillpopper.android.refillreminder.models;

/**
 * Created by M1023050 on 2/21/2018.
 */

public class RefillReminderRootObject {

    private RefillPillpopperResponse pillpopperResponse;

    public RefillPillpopperResponse getPillpopperResponse ()
    {
        return pillpopperResponse;
    }

    public void setPillpopperResponse (RefillPillpopperResponse pillpopperResponse)
    {
        this.pillpopperResponse = pillpopperResponse;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [pillpopperResponse = "+pillpopperResponse+"]";
    }
}
