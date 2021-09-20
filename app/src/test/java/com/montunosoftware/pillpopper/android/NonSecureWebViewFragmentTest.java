package com.montunosoftware.pillpopper.android;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;
import com.montunosoftware.pillpopper.model.TTGWebviewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class NonSecureWebViewFragmentTest {
    private NonSecureWebviewFragment nonSecureWebviewFragment;
    private View view;
    private TTGWebviewModel ttgWebviewModel;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        mockData();
        Bundle bundle = new Bundle();
        bundle.putSerializable("webViewModel", ttgWebviewModel);
        nonSecureWebviewFragment = new NonSecureWebviewFragment();
        nonSecureWebviewFragment.setArguments(bundle);
        SupportFragmentTestUtil.startFragment(nonSecureWebviewFragment, HomeContainerActivity.class);
        view = nonSecureWebviewFragment.getView();
    }

    private void mockData() {
        ttgWebviewModel = new TTGWebviewModel();
        ttgWebviewModel.setTitle("abc");
        ttgWebviewModel.setRefreshImageViewId(1);
        ttgWebviewModel.setCloseImageViewId(0);
        ttgWebviewModel.setUrl("https://abc");
    }

    @Test
    public void viewShouldNotNull() {
        assertNotNull(view);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        nonSecureWebviewFragment.onDestroy();
    }

    @Test
    public void onClickHomeMenu() {
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        nonSecureWebviewFragment.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }

    @Test
    public void onClickRefreshMenu() {
        MenuItem refreshMenu = new RoboMenuItem(R.id.refresh_icon);
        nonSecureWebviewFragment.onOptionsItemSelected(refreshMenu);
        assertSame(true, refreshMenu.isVisible());
    }

    @Test
    public void onClickCloseMenu() {
        MenuItem closeMenu = new RoboMenuItem(R.menu.refill_close);
        nonSecureWebviewFragment.onOptionsItemSelected(closeMenu);
        assertSame(true, closeMenu.isVisible());
    }
}
