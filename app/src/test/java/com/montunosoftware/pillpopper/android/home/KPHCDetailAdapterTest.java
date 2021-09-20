package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.KphcDrug;

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

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class KPHCDetailAdapterTest {
    private KPHCDetailAdapter kphcDetailAdapter;
    private Context context;
    private HomeCardDetailActivity homeCardDetailActivity;
    private List<KphcDrug> kphcDrugList;

    @Before
    public void setUp() {
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class).get();
        context = homeCardDetailActivity.getApplicationContext();
        mockData();
        kphcDetailAdapter = new KPHCDetailAdapter(kphcDrugList);
    }

    private void mockData() {
        kphcDrugList = new ArrayList<>();
        KphcDrug kphcDrug = new KphcDrug();
        kphcDrug.setDose("2");
        kphcDrug.setPillId("1223");
        kphcDrug.setPillName("xyz");
        kphcDrug.setInstruction("Take Medicine on time");
        kphcDrug.setPrescriptionId("12134");
        KphcDrug kphcDrug1 = new KphcDrug();
        kphcDrug1.setPillName("abc");
        kphcDrug1.setPillId("121");
        kphcDrug1.setDose("1");
        kphcDrugList.add(kphcDrug);
        kphcDrugList.add(kphcDrug1);
    }

    @Test
    public void adapterShouldNotNull() {
        assertNotNull(kphcDetailAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(2, kphcDetailAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(kphcDetailAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
    }

    @Test
    public void testOnBindViewHolder() {
        LayoutInflater inflater = (LayoutInflater) homeCardDetailActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.new_update_kphc_med_item, null, false);
        KPHCDetailAdapter.ViewHolder viewHolder = new KPHCDetailAdapter.ViewHolder(view);
        kphcDetailAdapter.onBindViewHolder(viewHolder, 0);
        TextView drugDescription = view.findViewById(R.id.kphc_med_item_description);
        assertEquals(View.VISIBLE, drugDescription.getVisibility());
        kphcDetailAdapter.onBindViewHolder(viewHolder, 1);
        assertEquals(View.GONE, drugDescription.getVisibility());
    }
}
