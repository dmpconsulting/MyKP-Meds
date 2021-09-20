package com.montunosoftware.pillpopper.android.fingerprint;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.refillreminder.RefillReminderControllerShadow;

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

import static junit.framework.TestCase.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(application = PillpopperApplicationShadow.class,sdk = TestConfigurationProperties.BUILD_SDK_VERSION, shadows = {RefillReminderControllerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class})
public class FingerprintOptInContainerActivityTest {
    private FingerprintOptInContainerActivity fingerprintOptInContainerActivity;
    private ActivityController<FingerprintOptInContainerActivity> controller;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        controller = Robolectric.buildActivity(FingerprintOptInContainerActivity.class);
        fingerprintOptInContainerActivity = controller.create().start().resume().visible().get();
        FingerprintOptInContainerActivity.startOptInFlow(fingerprintOptInContainerActivity, 1);
    }
    @Test
    public void checkActivityNotNull(){
        assertNotNull(fingerprintOptInContainerActivity);
    }

    @After
    public void finishActivity(){
        controller.pause().stop().destroy();
    }
}
