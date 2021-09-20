package org.kp.tpmg.mykpmeds.activation.controller;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, SecurePreferencesShadow.class})
public class ActivationControllerTest {
    private ActivationController activationController;
    private Context context;
    @Before
    public void setup(){
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        context = ApplicationProvider.getApplicationContext().getApplicationContext();
        activationController = ActivationController.getInstance();
    }
   /* @Test
    public void testcheckForTimeOut(){
        System.out.println(activationController.checkForTimeOut(context));
        assertTrue(activationController.checkForTimeOut(context));
    }*/
    @Test
    public void testgetUserName(){
        assertNotEquals("avncb",activationController.getUserName(context));
    }
    /*@Test
    public void testisSessionActive(){
        System.out.println(activationController.isSessionActive(context));
        assertFalse(activationController.isSessionActive(context));
    }*/
    @Test
    public void testgetUserId(){
        assertNotEquals("12223",activationController.getUserId(context));
    }
    @Test
    public void testgetSSOSessionId(){
        assertNotEquals("3445r",activationController.getSSOSessionId(context));
    }
    @Test
    public void testgetMrn(){
        assertNotEquals("8267353",activationController.getMrn(context));
    }
  /*  @Test
    public void testisNewUser(){
        assertTrue(activationController.isNewUser(context));
    }*/
    @Test
    public void testisDeviceSwitched(){
        assertSame(false,activationController.isDeviceSwitched(context));
    }
   /* @Test
    public void testisDataResetFl(){
        assertTrue(activationController.isDataResetFl(context));
    }
   */ @Test
    public void testisAppVisible(){
        assertSame(false,activationController.isAppVisible());
    }
    @Test
    public void testgetRefillScreenChoice(){
        assertSame(true,activationController.getRefillScreenChoice(context));
    }
    @Test
    public void testgetSetupCompleteFlag(){
        assertNotEquals("huuywdd",activationController.getSetupCompleteFlag(context));
    }
    @Test
    public void testfetchUserRegion(){
        assertNotEquals("qwer",activationController.fetchUserRegion(context));
    }
    @Test
    public void testgetAccessToken(){
        assertNotEquals("gswfuu",activationController.getAccessToken(context));
    }
    @Test
    public void testgetRefreshToken(){
        assertNotEquals("gghggyu",activationController.getRefreshToken(context));
    }
    @Test
    public void testgetTokenType(){
        assertNotEquals("akiwbss",activationController.getTokenType(context));
    }
    @Test
    public void testgetUserAge(){
        assertNotEquals("23",activationController.getUserAge(context));
    }
    @Test
    public void testgetUserEmail(){
        assertNotEquals("abc@gmail.com",activationController.getUserEmail(context));
    }
    @Test
    public void testgetIntroCompleteFlag(){
        assertNotEquals("ndmbmb",activationController.getIntroCompleteFlag(context));
    }
}
