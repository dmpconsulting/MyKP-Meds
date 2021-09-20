package com.montunosoftware.pillpopper.android.inAppReminders;

import static org.junit.Assert.assertNotNull;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

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

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class,
        shadows = {DatabaseHandlerShadow.class, SecurePreferencesShadow.class})

public class CurrentReminderDetailActivityTest {
    private CurrentReminderDetailActivity currentReminderDetailActivity;
    private ActivityController<CurrentReminderDetailActivity> controller;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        controller = Robolectric.buildActivity(CurrentReminderDetailActivity.class);
        currentReminderDetailActivity = controller.create().resume().visible().get();
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(currentReminderDetailActivity);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
    }

}
