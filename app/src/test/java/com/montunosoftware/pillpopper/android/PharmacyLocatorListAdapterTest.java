package com.montunosoftware.pillpopper.android;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

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
import org.kp.tpmg.ttg.views.pharmacylocator.model.PharmacyLocatorObj;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class PharmacyLocatorListAdapterTest {
    private HomeContainerActivity homeContainerActivity;
    private Context context;
    private PharmacyLocatorListAdapter pharmacyLocatorListAdapter;
    private List<PharmacyLocatorObj> pharmacyList;
    private PharmacyLocatorListAdapter.OnItemClickListener listener;
    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        context = homeContainerActivity.getApplicationContext();
        pharmacyList = TestUtil.pharmacyListMockData();
        pharmacyLocatorListAdapter = new PharmacyLocatorListAdapter(homeContainerActivity, pharmacyList, listener);
    }

    @Test
    public void adapterNotNull() {
        assertNotNull(pharmacyLocatorListAdapter);
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(pharmacyLocatorListAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
    }

    @Test
    public void testGetItemCount() {
        assertEquals(pharmacyList.size(), pharmacyLocatorListAdapter.getItemCount());
    }

    @Test
    public void testOnBindViewHolder() {
        LayoutInflater inflater = (LayoutInflater) homeContainerActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pharmacy_locator_row, null, false);
        PharmacyLocatorListAdapter.ViewHolder viewHolder = pharmacyLocatorListAdapter.new ViewHolder(view);
        pharmacyLocatorListAdapter.onBindViewHolder(viewHolder, 0);
        pharmacyLocatorListAdapter.onBindViewHolder(viewHolder, 1);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
    }
}