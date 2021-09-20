package com.montunosoftware.pillpopper.android.interrupts.temporaryPassword;

import static org.junit.Assert.assertNotNull;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.interrupts.InterruptsActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class TemporaryPasswordFragmentTest {
    private ActivityController<InterruptsActivity> controller;
    private View view;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        Intent intent = new Intent(RuntimeEnvironment.systemContext, InterruptsActivity.class);
        intent.putExtra("mSignOnInterruptType", AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD);
        controller = Robolectric.buildActivity(InterruptsActivity.class, intent);
        InterruptsActivity interruptsActivity = controller.create().start().resume().visible().get();
        view = interruptsActivity.getSupportFragmentManager().getFragments().get(0).getView();
    }

    @Test
    public void fragmentViewShouldNotBeNull() {
        assertNotNull(view);
    }

    @Test
    public void testConFirmButtonClick() {
        Button confirmButton = view.findViewById(R.id.confirm_btn);
        confirmButton.performClick();
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
    }
}
