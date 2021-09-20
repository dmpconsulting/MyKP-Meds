package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

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
public class RegionsAdapterTest {
    private SigninHelpActivity signinHelpActivity;
    private Context context;
    private RegionsAdapter regionsAdapter;
    private String[] regionList;

    @Before
    public void setUp() {
        signinHelpActivity = Robolectric.buildActivity(SigninHelpActivity.class).create().get();
        context = ApplicationProvider.getApplicationContext();
        regionList = new String[]{"India", "America", "Uk", "NewYork"};
        regionsAdapter = new RegionsAdapter(context, regionList);
    }


    @Test
    public void adapterShouldNotNull() {
        assertNotNull(regionsAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(regionList.length, regionsAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(regionsAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
    }

    @Test
    public void testOnBindViewHolder() {
        LayoutInflater inflater = (LayoutInflater) signinHelpActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.sign_in_help_region_child, null, false);
        RegionsAdapter.RegionHolder regionHolder = regionsAdapter.new RegionHolder(view);
        TextView regionText = view.findViewById(R.id.region);
        regionsAdapter.onBindViewHolder(regionHolder, 1);
        assertEquals("Uk", regionText.getText());
        regionsAdapter.onBindViewHolder(regionHolder, 0);
        assertEquals("India", regionText.getText());
        regionsAdapter.onBindViewHolder(regionHolder, 3);
        assertEquals("NewYork", regionText.getText());

    }
}
