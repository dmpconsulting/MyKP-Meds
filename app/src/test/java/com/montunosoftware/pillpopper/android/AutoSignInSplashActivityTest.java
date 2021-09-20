package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.io.BufferedReader;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class AutoSignInSplashActivityTest {
    private ActivityController<AutoSignInSplashActivity> activity;
    private Context context;
    private BufferedReader reader = null;
    private StringBuilder sb = new StringBuilder();
    private JSONObject userResponse;
    private AutoSignInSplashActivity autoSignInSplashActivity;
    private ShadowActivity autoSignInSplashActivityShadow;


    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        activity = Robolectric.buildActivity(AutoSignInSplashActivity.class);
        context = ApplicationProvider.getApplicationContext();
        autoSignInSplashActivity = activity.create().start().resume().visible().get();
        autoSignInSplashActivityShadow = Shadows.shadowOf(autoSignInSplashActivity);
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(autoSignInSplashActivity);
    }

    @Test
    public void shouldGoToSplashActivity() {
        Intent startedIntent = autoSignInSplashActivityShadow.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        assertThat(shadowIntent, is(notNullValue()));
    }

    @After
    public void tearDown(){
        TestUtil.resetDatabase();
        activity.pause().stop().destroy();
    }
}
