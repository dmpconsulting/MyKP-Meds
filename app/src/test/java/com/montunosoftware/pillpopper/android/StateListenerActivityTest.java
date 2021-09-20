package com.montunosoftware.pillpopper.android;


import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class StateListenerActivityTest {

    private ActivityController<StateListenerActivity> controller;
    private StateListenerActivity stateListenerActivity;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        AppConstants.setByPassLogin(true);
        PillpopperRunTime.getInstance().setReminderNeedToShow(true);
        PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(true);
        controller = Robolectric.buildActivity(StateListenerActivity.class);
        stateListenerActivity = controller.create().start().resume().get();
    }

    @Test
    public void activityNotNull() {
        assertNotNull(stateListenerActivity);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.stop().pause().destroy();
    }

}
