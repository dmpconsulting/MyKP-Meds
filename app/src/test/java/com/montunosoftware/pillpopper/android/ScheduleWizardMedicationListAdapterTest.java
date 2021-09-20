package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.widget.RelativeLayout;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;

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
public class ScheduleWizardMedicationListAdapterTest {
    private ScheduleWizardMedicationListAdapter scheduleWizardMedicationListAdapter;
    private HomeContainerActivity homeContainerActivity;
    private Context context;
    private List<Drug> drugList;

    @Before
    public void setUp() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        context = homeContainerActivity.getAndroidContext();
        mockData();
        scheduleWizardMedicationListAdapter = new ScheduleWizardMedicationListAdapter();
        scheduleWizardMedicationListAdapter.setData(drugList, homeContainerActivity);
    }

    private void mockData() {
        UniqueDeviceId.init(context);
        Drug drug = new Drug();
        drug.setName("Med Name1");
        drug.setUserID("");
        drug.setDose("2");
        drug.setIsTempHeadr(true);
        Drug drug2 = new Drug();
        drug2.setDose("7");
        drug2.setName("Med Name2");
        drug2.setUserID("");
        drug2.setNotes("abc");
        Drug drug3 = new Drug();
        drug3.setName("Med Name3");
        drug3.setUserID("");
        drug3.setDose("4");
        drugList = new ArrayList<>();
        drugList.add(drug);
        drugList.add(drug2);
        drugList.add(drug3);
    }

    @Test
    public void adapterNotNull() {
        assertNotNull(scheduleWizardMedicationListAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(drugList.size(), scheduleWizardMedicationListAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(scheduleWizardMedicationListAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
    }


}
