package com.montunosoftware.pillpopper.model;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import com.montunosoftware.pillpopper.model.PillpopperRunTime;

/**
 * Created by M1028309 on 5/23/2017.
 */
@Implements(PillpopperRunTime.class)
public class PillpopperRunTimeShadow {

    @Implementation
    public boolean isHistorySyncDone() {
        return true;
    }

    @Implementation
    public boolean isTimeZoneChanged() {
        return false;
    }
}
