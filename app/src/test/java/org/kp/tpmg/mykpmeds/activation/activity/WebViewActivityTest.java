package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Intent;
import android.view.MenuItem;

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
import org.robolectric.fakes.RoboMenuItem;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class WebViewActivityTest {
    private WebViewActivity webViewActivity;
    private ActivityController<WebViewActivity> controller;

    @Before
    public  void setup() {
        TestUtil.setupTestEnvironment();
    }

    private void startActivity(String type) {
        Intent intent = new Intent();
        intent.putExtra("Type", type);
        intent.putExtra("Source", "SignInHelp");
        controller = Robolectric.buildActivity(WebViewActivity.class, intent);
        webViewActivity =  controller.create().start().resume().visible().get();
    }

    @After
    public void tearDown(){
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
    }

    @Test
    public void checkActivityNotNull(){
        startActivity("terms");
        assertNotNull(webViewActivity);
    }

    @Test
    public void onClickHomeMenu() {
        startActivity("privacy");
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        webViewActivity.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
        assertEquals(0, RunTimeData.getInstance().getHomeButtonPressed());
    }

    @Test
    public void onClickCloseMenu() {
        startActivity("privacy");
        MenuItem closeMenu = new RoboMenuItem(R.id.close);
        webViewActivity.onOptionsItemSelected(closeMenu);
        assertSame(true, closeMenu.isVisible());
    }

    @Test
    public void testOnRequestPermissionsResult() {
        startActivity("guide");
        int requestCode;
        int[] grantResults;
        grantResults = new int[]{0};
        String[] permissions = {" "};
        requestCode = AppConstants.PERMISSION_PHONE_CALL_PHONE;
        webViewActivity.onRequestPermissionsResult(requestCode,permissions,grantResults);
        grantResults = new int[]{1};
        webViewActivity.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

}
