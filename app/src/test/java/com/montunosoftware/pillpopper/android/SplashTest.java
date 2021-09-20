package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.IS_FROM_PILL_POPPER_APPLICATION;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class SplashTest {
    private Splash splash;
    private ActivityController<Splash> controller;


    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        RunTimeData.getInstance().setNotificationGenerated(true);
        AppConstants.setByPassLogin(false);
    }

    @Test
    public void activityShouldNotNull() {
        startActivity(true);
        assertNotNull(splash);
    }

    private void startActivity(Boolean value) {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, Splash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IS_FROM_PILL_POPPER_APPLICATION, value);
        controller = Robolectric.buildActivity(Splash.class, intent);
        splash = controller.create().start().resume().get();
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
    }

    @Test
    public void testKpLogoLayoutVisibilityGone() {
        startActivity(true);
        RelativeLayout kpLogoLayout = splash.findViewById(R.id.kp_logo_layout);
        assertEquals(View.GONE, kpLogoLayout.getVisibility());
    }

    @Test
    public void testKpLogoLayoutVisibilityVisible() {
        startActivity(false);
        RelativeLayout kpLogoLayout = splash.findViewById(R.id.kp_logo_layout);
        assertEquals(View.VISIBLE, kpLogoLayout.getVisibility());
    }


}
