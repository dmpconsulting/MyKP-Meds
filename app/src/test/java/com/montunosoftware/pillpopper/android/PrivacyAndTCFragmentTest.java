package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontControllerShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.PageList;
import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.model.Region;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, FrontControllerShadow.class, PillpopperRunTimeShadow.class})
public class PrivacyAndTCFragmentTest {
    private PrivacyAndTCFragment privacyAndTCFragment = new PrivacyAndTCFragment();
    private View view;
    private Context context;
    private String FILE_NAME = "RegionContacts.txt";

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        context = RuntimeEnvironment.systemContext;
    }

    @Test
    public void privacyAndTCFragmentShouldNotNull() {
        mockData("App Support");
        assertNotNull(privacyAndTCFragment);
    }

    @Test
    public void testPerformRegionsAPICallRequired() {
        RunTimeData.getInstance().setRegionContactAPICallRequired(true);
        mockData("Appointments and Advice");
        assertNotNull(Util.readFileAsString(context, FILE_NAME));
        SupportFragmentTestUtil.startFragment(privacyAndTCFragment, HomeContainerActivity.class);
    }

    @Test
    public void testLoadRegionsFromLocal() {
        RunTimeData.getInstance().setRegionContactAPICallRequired(false);
        mockData("Pharmacy");
        assertNotNull(Util.readFileAsString(context, FILE_NAME));
        LinearLayout apptNAdviceRoot = view.findViewById(R.id.appt_n_adv_root);
        assertEquals(apptNAdviceRoot.getVisibility(), View.GONE);
    }

    @Test
    public void onClickCloseMenu() {
        mockData("Terms and Conditions");
        MenuItem homeMenu = new RoboMenuItem(R.id.close);
        privacyAndTCFragment.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
        Objects.requireNonNull(privacyAndTCFragment.getActivity()).finish();
    }

    @Test
    public void onClickHomeMenu() {
        mockData("Privacy Statement");
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        privacyAndTCFragment.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }

    private void mockData(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        privacyAndTCFragment.setArguments(bundle);
        Map<String, String> map = new HashMap<>();
        map.put("regionalContactsURL", "http://www.google.com");
        TTGRuntimeData.getInstance().setConfigListParams(map);
        SupportFragmentTestUtil.startFragment(privacyAndTCFragment, HomeContainerActivity.class);
        view = privacyAndTCFragment.getView();
    }

}
