package com.montunosoftware.pillpopper.android;


import android.content.Context;
import android.widget.RelativeLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class FrequencySpinnerAdapterTest {
    private FrequencySpinnerAdapter frequencySpinnerAdapter;
    private Context context;
    private HomeContainerActivity homeContainerActivity;

    @Before
    public void setUp() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        context = homeContainerActivity.getAndroidContext();
        String[] customOptions = new String[]{"Days", "Weeks"};
        frequencySpinnerAdapter = new FrequencySpinnerAdapter(context, R.layout.custom_frequency_spinner_layout, customOptions);
    }

    @Test
    public void adapterNotNull() {
        assertNotNull(homeContainerActivity);
        assertNotNull(frequencySpinnerAdapter);
    }

    @Test
    public void testGetCount() {
        assertEquals(2, frequencySpinnerAdapter.getCount());
    }

    @Test
    public void testGetDropDownView() {
        assertNotNull(frequencySpinnerAdapter.getDropDownView(0, null, new RelativeLayout(context)));
    }

    @Test
    public void testGetView() {
        assertNotNull(frequencySpinnerAdapter.getView(0, null, new RelativeLayout(context)));
    }

}
