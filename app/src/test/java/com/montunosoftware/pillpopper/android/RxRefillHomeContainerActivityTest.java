package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class RxRefillHomeContainerActivityTest {
    private RxRefillHomeContainerActivity rxRefillHomeContainerActivity;
    private ActivityController<RxRefillHomeContainerActivity> controller;
    private ShadowActivity rxRefillHomeContainerActivityShadow;
    private Context context;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, RxRefillHomeContainerActivity.class);
        intent.putExtra("launchPharmacyLocatorDetails", true);
        RefillRuntimeData.getInstance().setRxRefillUsersList(FrontController.getInstance(context).getRxRefillUsersList());
        RunTimeConstants.getInstance().setNotificationSuppressor(true);
        controller = Robolectric.buildActivity(RxRefillHomeContainerActivity.class, intent);
        rxRefillHomeContainerActivity = controller.create().destroy().visible().get();
        rxRefillHomeContainerActivityShadow = Shadows.shadowOf(rxRefillHomeContainerActivity);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(rxRefillHomeContainerActivity);
    }

    @Test
    public void testInitActionBar() {
        Toolbar toolbar = rxRefillHomeContainerActivity.findViewById(R.id.loading_screen_tool_bar);
        String title = toolbar.getTitle().toString();
        assertEquals("Pharmacy", title);
    }

    @Test
    public void onClickHomeMenu() {
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        rxRefillHomeContainerActivity.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }
}
