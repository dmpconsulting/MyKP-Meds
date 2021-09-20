package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class KPHCDiscontinueCardExpandedAdapterTest {
    private KPHCDiscontinueCardExpandedAdapter kphcDiscontinueCardExpandedAdapter;
    private Context context;
    private HomeCardDetailActivity homeCardDetailActivity;
    private List<DiscontinuedDrug> discontinuedDrugList;
    private LayoutInflater inflater;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        mockData();
        KPHCDiscontinueCard parcel = new KPHCDiscontinueCard(discontinuedDrugList, context);
        Intent intent = new Intent();
        intent.putExtra("card", parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().get();
        kphcDiscontinueCardExpandedAdapter = new KPHCDiscontinueCardExpandedAdapter(homeCardDetailActivity, discontinuedDrugList);
        inflater = (LayoutInflater) homeCardDetailActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void mockData() {
        discontinuedDrugList = new ArrayList<>();
        DiscontinuedDrug discontinuedDrug = new DiscontinuedDrug();
        discontinuedDrug.setDosage("1");
        discontinuedDrug.setName("abc");
        discontinuedDrug.setPillId("212");
        discontinuedDrug.setUserFirstName("john");
        discontinuedDrug.setUserId("1231");
        DiscontinuedDrug discontinuedDrug1 = new DiscontinuedDrug();
        discontinuedDrug1.setDosage("2");
        discontinuedDrug1.setName("xyz");
        discontinuedDrug1.setPillId("2122");
        discontinuedDrug1.setUserFirstName("Roy");
        discontinuedDrug1.setUserId("1231");
        DiscontinuedDrug discontinuedDrug2 = new DiscontinuedDrug();
        discontinuedDrug2.setDosage("3");
        discontinuedDrug2.setName("BC");
        discontinuedDrug2.setPillId("2022");
        discontinuedDrug2.setUserFirstName("ABC");
        discontinuedDrug2.setUserId("1230");
        discontinuedDrugList.add(discontinuedDrug);
        discontinuedDrugList.add(discontinuedDrug1);
        discontinuedDrugList.add(discontinuedDrug2);
    }

    @Test
    public void adapterNotNull() {
        assertNotNull(kphcDiscontinueCardExpandedAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(discontinuedDrugList.size(), kphcDiscontinueCardExpandedAdapter.getItemCount());
    }

    @Test
    public void testGetItemViewType() {
        assertSame(1, kphcDiscontinueCardExpandedAdapter.getItemViewType(0));
        assertEquals(2, kphcDiscontinueCardExpandedAdapter.getItemViewType(1));
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(kphcDiscontinueCardExpandedAdapter.onCreateViewHolder(new RelativeLayout(context), 2));
        assertNotNull(kphcDiscontinueCardExpandedAdapter.onCreateViewHolder(new RelativeLayout(context), 1));
    }

    @Test
    public void testOnBindViewHolder() {
        View view;
        view = inflater.inflate(R.layout.discontinued_medication_alert_with_proxy_header_holder, null, false);
        KPHCDiscontinueCardExpandedAdapter.DiscontinuedDrugWithProxyHeaderViewHolder proxyHeaderViewHolder = kphcDiscontinueCardExpandedAdapter.new DiscontinuedDrugWithProxyHeaderViewHolder(view);
        kphcDiscontinueCardExpandedAdapter.onBindViewHolder(proxyHeaderViewHolder, 0);
        TextView proxyNameTextView = view.findViewById(R.id.discontinued_medication_proxy_name_textview);
        assertEquals("john", proxyNameTextView.getText());
        view = inflater.inflate(R.layout.discontinued_medication_alert_medication_holder, null, false);
        KPHCDiscontinueCardExpandedAdapter.DiscontinuedDrugViewHolder viewHolder = kphcDiscontinueCardExpandedAdapter.new DiscontinuedDrugViewHolder(view);
        kphcDiscontinueCardExpandedAdapter.onBindViewHolder(viewHolder, 1);
        TextView medicationDosageTextView = view.findViewById(R.id.discontinued_medication_dosage_textview);
        assertEquals("2", medicationDosageTextView.getText());
    }
}
