package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.Util;
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
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class PrivacyAndTcTest {

    private ActivityController<PrivacyAndTC> controller;
    private PrivacyAndTC privacyActivity;
    private Context context;
    private String FILE_NAME = "RegionContacts.txt";

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void activityNotNull() {
        mockData("App Support");
        assertNotNull(privacyActivity);
    }

    @Test
    public void testPerformRegionsAPICallRequired() {
        RunTimeData.getInstance().setRegionContactAPICallRequired(true);
        mockData("Appointments and Advice");
        assertNotNull(Util.readFileAsString(context, FILE_NAME));
    }

    @Test
    public void testLoadRegionsFromLocal() {
        RunTimeData.getInstance().setRegionContactAPICallRequired(false);
        mockData("Pharmacy");
        assertNotNull(Util.readFileAsString(context, FILE_NAME));
        LinearLayout apptNAdviceRoot = privacyActivity.findViewById(R.id.appt_n_adv_root);
        assertEquals(View.GONE, apptNAdviceRoot.getVisibility());
    }

    @Test
    public void onClickCloseMenu() {
        mockData("Terms and Conditions");
        MenuItem homeMenu = new RoboMenuItem(R.id.close);
        privacyActivity.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }

    @Test
    public void onClickHomeMenu() {
        mockData("Privacy Statement");
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        privacyActivity.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }

    @Test
    public void onClickHome() {
        mockData("Guide");
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        privacyActivity.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }

    @After
    public void tearDown(){
        controller.pause().stop().destroy();
    }

    private void mockData(String url) {
        Intent intent = new Intent(context, PrivacyAndTC.class);
        intent.putExtra("Guide".equalsIgnoreCase(url) ? "Type" : "url", url);
        Map<String, String> map = new HashMap<>();
        map.put("regionalContactsURL", "http://www.google.com");
        TTGRuntimeData.getInstance().setConfigListParams(map);
        controller = Robolectric.buildActivity(PrivacyAndTC.class, intent);
        privacyActivity = controller.create().start().resume().visible().get();
    }
}
