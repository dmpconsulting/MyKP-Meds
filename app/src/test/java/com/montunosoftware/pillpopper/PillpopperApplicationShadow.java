package com.montunosoftware.pillpopper;

import android.app.Application;

import com.montunosoftware.pillpopper.android.util.PillpopperApplication;
import org.kp.tpmg.ttgmobilelib.controller.TTGCallBackInterfaces;
import org.robolectric.annotation.Implements;

/**
 * Created by M1028309 on 5/3/2017.
 */
@Implements(PillpopperApplication.class)
public class PillpopperApplicationShadow extends Application implements TTGCallBackInterfaces.KeepAlive{


    @Override
    public void onKeepAliveSuccess(String s) {

    }

    @Override
    public void onKeepAliveError(int statusCode) {

    }
}
