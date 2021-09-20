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
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class FingerprintTermsAndConditionsActivityTest {
    private ActivityController<FingerprintTermsAndConditionsActivity> activity;
    private FingerprintTermsAndConditionsActivity fingerprintTermsAndConditionsActivity;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        activity = Robolectric.buildActivity(FingerprintTermsAndConditionsActivity.class);
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        fingerprintTermsAndConditionsActivity = activity.create().start().resume().visible().get();
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(fingerprintTermsAndConditionsActivity);
    }

    @Test
    public void agreeButtonClick() {
        Button accept = fingerprintTermsAndConditionsActivity.findViewById(R.id.t_and_c_accept_button);
        assertNotNull(accept);
        accept.performClick();
    }

    @Test
    public void cancelButtonClick() {
        Button cancel = fingerprintTermsAndConditionsActivity.findViewById(R.id.t_and_c_cancel_button);
        assertNotNull(cancel);
        cancel.performClick();
    }

    @After
    public void finishActivity() {
        activity.stop();
    }
}
