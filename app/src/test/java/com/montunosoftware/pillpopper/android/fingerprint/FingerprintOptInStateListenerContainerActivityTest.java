package com.montunosoftware.pillpopper.android.fingerprint;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
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
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class FingerprintOptInStateListenerContainerActivityTest {
    private ActivityController<FingerprintOptInStateListenerContainerActivity> controller;
    private FingerprintOptInStateListenerContainerActivity fingerprintOptInStateListenerContainerActivity;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        controller = Robolectric.buildActivity(FingerprintOptInStateListenerContainerActivity.class);
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        fingerprintOptInStateListenerContainerActivity = controller.create().start().resume().get();
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(fingerprintOptInStateListenerContainerActivity);
    }

    @After
    public void finishActivity() {
        controller.pause().stop().destroy();
    }

}
