package com.montunosoftware.pillpopper.android.util;

import android.content.Context;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.UUID;

import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;

/**
 * Created by M1032896 on 7/17/2017.
 */

@Implements(UniqueDeviceId.class)
public class UniqueDeviceIdShadow {

    @Implementation
    public static String getHardwareId(Context context) {
        return UUID.randomUUID().toString();
    }

    @Implementation
    public static String getHardwareId() {
        return UUID.randomUUID().toString();
    }
}
