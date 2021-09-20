package com.montunosoftware.pillpopper.refillreminder;

import com.montunosoftware.pillpopper.android.util.PillpopperApplication;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

/**
 * Created by M1023050 on 8/7/2018.
 */

public class TestPillpopperApplication extends PillpopperApplication {

    @Override
    public void onCreate() {
        //super.onCreate();
        try {
            //AppDynamicsController.getInstance(getApplicationContext()).initAppDynamics();
        } catch (Exception e){
            LoggerUtils.info(e.getMessage());
        }
    }
}
