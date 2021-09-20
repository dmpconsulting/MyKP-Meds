package com.montunosoftware.pillpopper.android;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.kp.tpmg.ttg.views.pharmacylocator.model.PharmacyLocatorObj;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class PharmacyLocatorFragmentTest {

    private PharmacyLocatorFragment pharmacyLocatorFragment;
    private List<PharmacyLocatorObj> list;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        pharmacyLocatorFragment = new PharmacyLocatorFragment();
        list = TestUtil.pharmacyListMockData();
        SupportFragmentTestUtil.startFragment(pharmacyLocatorFragment, HomeContainerActivity.class);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        pharmacyLocatorFragment.onDestroy();
    }

    @Test
    public void fragmentNotNull() {
        assertThat(pharmacyLocatorFragment != null);
    }

    @Test
    public void viewShouldNotNull() {
        assertThat(pharmacyLocatorFragment.getView()).isNotNull();
    }

    @Test
    public void testListItem() {
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    public void testRecyclerViewContent() {

        RecyclerView mRecyclerView = pharmacyLocatorFragment.getView().findViewById(R.id.locator_recycler_view);
        assertNotNull(list);
        pharmacyLocatorFragment.getPharmacyLocatorList(list);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.measure(0, 0);
        mRecyclerView.layout(0, 0, 100, 10000);
        assertTrue(mRecyclerView.getAdapter().getItemCount() > 0);
    }


}