package org.kp.tpmg.mykpmeds.activation.activity;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class TransparentLoadingActivityTest {
    private TransparentLoadingActivity transparentLoadingActivity;
    private ActivityController<TransparentLoadingActivity> controller;
    @Before
    public  void setup() {
        TestUtil.setupTestEnvironment();
        controller = Robolectric.buildActivity(TransparentLoadingActivity.class);
        transparentLoadingActivity =  controller.create().start().resume().pause().stop().destroy().visible().get();
    }

    @After
    public void tearDown(){
        TestUtil.resetDatabase();
    }

    @Test
    public void checkActivityNotNull(){
        assertNotNull(transparentLoadingActivity);
    }

}
