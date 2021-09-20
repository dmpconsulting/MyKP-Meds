package com.montunosoftware.pillpopper.android;

import android.content.Intent;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
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
public class ReminderContainerActivityTest {
    private ReminderContainerActivity reminderContainerActivity;
    private ActivityController<ReminderContainerActivity> controller;
    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        RunTimeData.getInstance().setNotificationGenerated(true);
        Intent intent = new Intent();
        intent.putExtra("launch","CurrentReminderActivity");
        controller = Robolectric.buildActivity(ReminderContainerActivity.class,intent);
        reminderContainerActivity =  controller.create().resume().visible().get();
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.destroy();
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(reminderContainerActivity);
    }
}
