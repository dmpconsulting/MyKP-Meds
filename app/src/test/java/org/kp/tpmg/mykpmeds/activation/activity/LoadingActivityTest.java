package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Intent;

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
public class LoadingActivityTest {
    private LoadingActivity loadingActivity;
    private ActivityController<LoadingActivity> controller;
    @Before
    public  void setup() {
        TestUtil.setupTestEnvironment();
        Intent intent = new Intent();
        intent.putExtra("needHomeButtonEvent", true);
        intent.putExtra("type", "simple");
        controller = Robolectric.buildActivity(LoadingActivity.class, intent);
        loadingActivity =  controller.create().start().resume().visible().get();
    }

    @After
    public void tearDown(){
        controller.pause().stop().destroy();
        TestUtil.resetDatabase();
    }

    @Test
    public void checkActivityNotNull(){
        assertNotNull(loadingActivity);
    }

}
