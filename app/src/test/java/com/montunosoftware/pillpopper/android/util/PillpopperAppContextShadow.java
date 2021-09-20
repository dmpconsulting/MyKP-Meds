package com.montunosoftware.pillpopper.android.util;

import android.content.Context;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;

/**
 * Created by M1032896 on 6/6/2018.
 */

@Implements(PillpopperAppContext.class)
public class PillpopperAppContextShadow {

    @Implementation
    public void stopKeepAliveService(Context _thisActivity) {
    }
}
