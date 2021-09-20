package org.kp.tpmg.mykpmeds.activation.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.refillreminder.RefillReminderControllerShadow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.envswitch.EnvironmentSwitchActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponse;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Created by M1030430 on 2/7/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = PillpopperApplicationShadow.class, sdk = TestConfigurationProperties.BUILD_SDK_VERSION, shadows = {RefillReminderControllerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class})
public class LoginActivityTest {

    private ActivityController<LoginActivity> activity;
    private JSONObject userResponse;
    private LoginActivity loginActivity;
    private Intent intent;
    private ShadowActivity shadowActivity;


    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        intent = new Intent();
        intent.putExtra("ForceUpgradeRequire", true);
        intent.putExtra("isSessionExpiredRequire", true);
        activity = Robolectric.buildActivity(LoginActivity.class, intent);
        loginActivity = activity.create().start().resume().get();
        shadowActivity = Shadows.shadowOf(loginActivity);
    }

    @After
    public void tearDown() {
        activity.pause().stop().destroy();
    }

    @Test
    public void testVordelAPIForNCal() {
        try {
            String path = LoginActivityTest.class.getResource("/VordelResponseNcal.txt").toURI().getPath();
            File file = new File(path);
            userResponse = TestUtil.readFromFile(new FileInputStream(file));
            JSONObject json = new JSONObject(userResponse.get("user").toString());
            assertEquals("region name", "MRN", json.get("region").toString());
        } catch (JSONException | IOException | URISyntaxException e) {
            System.err.print(e.getMessage());
        }
    }

    @Test
    public void testVordelAPIForMid() {
        try {
            String path = LoginActivityTest.class.getResource("/VordelResponseMID.txt").toURI().getPath();
            File file = new File(path);
            userResponse = TestUtil.readFromFile(new FileInputStream(file));
            JSONObject json = new JSONObject(userResponse.get("user").toString());
            assertEquals("region name", "MID", json.get("region").toString());
        } catch (JSONException e) {
            LoggerUtils.error(e.getMessage());
            System.err.print(e.getMessage());
        } catch (IOException e) {
            LoggerUtils.exception(e.getMessage());
            System.err.print(e.getMessage());
        } catch (URISyntaxException e) {
            System.err.print(e.getMessage());
        }
    }

    @Test
    public void testNonMemberAccess() {
        try {
            String path = LoginActivityTest.class.getResource("/VordelResponseMID.txt").toURI().getPath();
            File file = new File(path);
            userResponse = TestUtil.readFromFile(new FileInputStream(file));
            JSONObject userJsonObject = (JSONObject) userResponse.get("user");
            JSONArray ebizAccountRolls = userJsonObject.getJSONArray("ebizAccountRoles");
            assertEquals("EbizAccountRolls", "UNM", ebizAccountRolls.getString(0));
        } catch (JSONException e) {
            System.err.print(e.getMessage());
        } catch (IOException e) {
            System.err.print(e.getMessage());
        } catch (URISyntaxException e) {
            System.err.print(e.getMessage());
        }
    }


    @Test
    public void testUserDeviceSwitch() {
        try {
            String path = LoginActivityTest.class.getResource("/VordelResponseNcal.txt").toURI().getPath();
            File file = new File(path);
            userResponse = TestUtil.readFromFile(new FileInputStream(file));
            //JSONObject json = new JSONObject(userResponse.get("user").toString());
            //pass userid as the last parameter

            //Asynctask removed since there is web service call involed which requires to alter main working code
            // creating dependecy and since this scenario calls for mock hence modified

            TestUtil.setRegistrationResponse("/RegisterResponse-New.json");
            SignonResponse signonResponse = RunTimeData.getInstance().getRegistrationResponse();
            assertEquals("Status code for device switch", signonResponse.getResponse().getStatusCode(),
                    String.valueOf(AppConstants.DEVICE_USER_SWITCH_STATUSCODE));
        } catch (FileNotFoundException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void onClickSignInView() {
        TextView signInView = loginActivity.findViewById(R.id.signin_view);
        signInView.performClick();
        Intent intent = shadowActivity.peekNextStartedActivity();
        assertEquals(SigninHelpActivity.class.getCanonicalName(), Objects.requireNonNull(intent.getComponent()).getClassName());

    }

    @Test
    public void onClickSignOnButton() {
        Button signOnButton = loginActivity.findViewById(R.id.sign_on_button);
        signOnButton.performClick();
        assertFalse(RunTimeData.getInstance().isInturruptScreenVisible());
    }

    @Test
    public void onClickCurrentEnvView() {
        TextView currentEnv = loginActivity.findViewById(R.id.current_env_text_view);
        currentEnv.performClick();
        Intent intent = shadowActivity.peekNextStartedActivity();
        assertEquals(EnvironmentSwitchActivity.class.getCanonicalName(), Objects.requireNonNull(intent.getComponent()).getClassName());
    }

    @Test
    public void testOnRequestPermissionsResult() {
        int requestCode = 106;
        int[] grantResult = new int[]{0};
        String[] permissions = new String[]{""};
        loginActivity.onRequestPermissionsResult(requestCode, permissions, grantResult);
        Intent intent = shadowActivity.peekNextStartedActivity();
        assertEquals(TransparentLoadingActivity.class.getCanonicalName(), Objects.requireNonNull(intent.getComponent()).getClassName());

    }

    @Test
    public void testShowInvalidCredentialsAlert() {
        loginActivity.showInvalidCredentialsAlert();
    }

    @Test
    public void testHandleAppProfileComplete() {
        loginActivity.handleAppProfileComplete();
    }
}
