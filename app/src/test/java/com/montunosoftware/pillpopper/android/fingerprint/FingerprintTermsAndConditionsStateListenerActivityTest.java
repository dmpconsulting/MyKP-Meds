package com.montunosoftware.pillpopper.android.fingerprint;

import android.widget.Button;

import com.montunosoftware.mymeds.R;
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
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class FingerprintTermsAndConditionsStateListenerActivityTest {
    private ActivityController<FingerprintTermsAndConditionsStateListenerActivity> controller;
    private FingerprintTermsAndConditionsStateListenerActivity fingerprintTermsAndConditionsStateListenerActivity;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        controller = Robolectric.buildActivity(FingerprintTermsAndConditionsStateListenerActivity.class);
        fingerprintTermsAndConditionsStateListenerActivity = controller.create().start().resume().visible().get();
    }
    @Test
    public void checkActivityNotNull(){
        assertNotNull(fingerprintTermsAndConditionsStateListenerActivity);
    }

    @Test
    public void acceptButton(){
        Button accept = fingerprintTermsAndConditionsStateListenerActivity.findViewById(R.id.t_and_c_accept_button);
        assertNotNull(accept);
        accept.performClick();
    }

    @Test
    public void cancelButton(){
        Button cancel = fingerprintTermsAndConditionsStateListenerActivity.findViewById(R.id.t_and_c_accept_button);
        assertNotNull(cancel);
        cancel.performClick();
    }

    @After
    public void finishActivity(){
        controller.pause().stop().destroy();
    }
}
