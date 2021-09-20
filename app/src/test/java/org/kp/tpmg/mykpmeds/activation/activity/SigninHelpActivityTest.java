package org.kp.tpmg.mykpmeds.activation.activity;


import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.model.AppDataShadow;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtilShadow;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TTGSignonControllerShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by M1032896 on 7/12/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = PillpopperApplicationShadow.class, sdk = TestConfigurationProperties.BUILD_SDK_VERSION, shadows = {ActivationUtilShadow.class,PillpopperAppContextShadow.class,
        DatabaseHandlerShadow.class, AppDataShadow.class, SecurePreferencesShadow.class, TTGSignonControllerShadow.class})
public class SigninHelpActivityTest {


    private SigninHelpActivity signinHelpActivity;
    private ShadowActivity signinHelpShadowActivity;
    private ActivityController<SigninHelpActivity> controller;


    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        controller = Robolectric.buildActivity(SigninHelpActivity.class);
        signinHelpActivity = controller.create().start().resume().visible().get();
        signinHelpShadowActivity = Shadows.shadowOf(signinHelpActivity);
    }


    @Test
    public void activityShouldBeNull() {
        assertNotNull(signinHelpActivity);
    }

    @Test
    public void textShouldNotBeNull() {
        assertNotNull(((TextView) signinHelpActivity.findViewById(R.id.tv_question_one)).getText().toString());
        assertNotNull(((TextView) signinHelpActivity.findViewById(R.id.tv_answer_one)).getText().toString());
        assertNotNull(((TextView) signinHelpActivity.findViewById(R.id.tv_question_two)).getText().toString());
        assertNotNull(((TextView) signinHelpActivity.findViewById(R.id.tv_answer_two)).getText().toString());
    }


    @Test
    //Permission shadow is need to run this test
    public void shouldMakeCall() {
        TextView callMemberServiceTextView = signinHelpActivity.findViewById(R.id.tv_member_Services);
        callMemberServiceTextView.performClick();
        assertNotNull(callMemberServiceTextView.getText().toString());
    }

    @Test
    public void shouldStartBrowserWithForgotUserLink() {
        TextView forgotUser = signinHelpActivity.findViewById(R.id.tv_forgot_user);
        forgotUser.performClick();
        assertEquals(View.VISIBLE,forgotUser.getVisibility());
        assertFalse(RunTimeData.getInstance().isClickFlg());
    }


    @Test
    public void shouldStartBrowserWithForgotPasswordLink() {
        TextView forgotPassword = signinHelpActivity.findViewById(R.id.tv_forgot_pwd);
        forgotPassword.performClick();
        assertEquals(View.VISIBLE,forgotPassword.getVisibility());
        assertFalse(RunTimeData.getInstance().isClickFlg());
    }

    @Test
    public void shouldStartBrowserWithRegisterLink() {
        TextView register = signinHelpActivity.findViewById(R.id.tv_register);
        register.performClick();
        assertEquals(View.VISIBLE,register.getVisibility());
        assertFalse(RunTimeData.getInstance().isClickFlg());
    }

    @Test
    public void testTermsAndConditions() {
        TextView termsAndConditions = signinHelpActivity.findViewById(R.id.tv_terms_conditions);
        termsAndConditions.performClick();
        assertEquals(View.VISIBLE,termsAndConditions.getVisibility());
        assertFalse(RunTimeData.getInstance().isClickFlg());
    }

    @Test
    public void testPrivacyStatement() {
        TextView privacyStatement = signinHelpActivity.findViewById(R.id.tv_privacy_policy);
        privacyStatement.performClick();
        assertEquals(View.VISIBLE,privacyStatement.getVisibility());
        assertFalse(RunTimeData.getInstance().isClickFlg());
    }

    @Test
    public void testOptionItemSelected(){
        MenuItem item = new RoboMenuItem(android.R.id.home);
        signinHelpActivity.onOptionsItemSelected(item);
        assertEquals(0,RunTimeData.getInstance().getHomeButtonPressed());
    }

    @After
    public void tearDown(){
        controller.pause().stop().destroy();
    }
}
