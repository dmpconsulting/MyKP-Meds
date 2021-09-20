package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
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
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class LoginErrorActivityTest {
    private LoginErrorActivity loginErrorActivity;
    private ActivityController<LoginErrorActivity> controller;
    private ShadowActivity loginErrorActivityShadow;
    @Before
    public  void setup() {
        TestUtil.setupTestEnvironment();
        controller = Robolectric.buildActivity(LoginErrorActivity.class);
        loginErrorActivity =  controller.create().start().resume().pause().stop().destroy().visible().get();
        loginErrorActivityShadow = Shadows.shadowOf(loginErrorActivity);
    }

    @After
    public void tearDown(){
        TestUtil.resetDatabase();
    }

    @Test
    public void checkActivityNotNull(){
        assertNotNull(loginErrorActivity);
    }

    @Test
    public void checkInitUI()
    {
        TextView alertMsgTxtVw = loginErrorActivity.findViewById(R.id.textview_msg);
        TextView alertTitleTxtVw = loginErrorActivity.findViewById(R.id.textview_title);
        assertNotNull(alertMsgTxtVw.getText().toString());
        assertNotNull(alertTitleTxtVw.getText().toString());

    }
    @Test
    public void shouldLaunchLoginScreen()
    {
        Button btnSignIn = loginErrorActivity.findViewById(R.id.cancel_button);
        btnSignIn.performClick();
        Intent startedIntent = loginErrorActivityShadow.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        assertThat(shadowIntent, is(notNullValue()));
    }

}
