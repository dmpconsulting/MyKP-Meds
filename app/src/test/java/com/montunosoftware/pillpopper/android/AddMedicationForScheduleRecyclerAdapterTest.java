package com.montunosoftware.pillpopper.android;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class AddMedicationForScheduleRecyclerAdapterTest {
    private AddMedicationForScheduleRecyclerAdapter addMedicationForScheduleRecyclerAdapter;
    private List<Drug> drugList;
    private Context context;
    private View view;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        AddMedicationsForScheduleActivity addMedicationsForScheduleActivity = Robolectric.buildActivity(AddMedicationsForScheduleActivity.class).get();
        context = addMedicationsForScheduleActivity.getAndroidContext();
        mockData();
        addMedicationForScheduleRecyclerAdapter = new AddMedicationForScheduleRecyclerAdapter(addMedicationsForScheduleActivity, drugList, addMedicationsForScheduleActivity);
        LayoutInflater inflater = (LayoutInflater) addMedicationsForScheduleActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.recycler_list_item, null, false);
    }

    private void mockData() {
        UniqueDeviceId.init(context);
        Drug drug = new Drug();
        drug.setName("Med Name1");
        drug.setUserID("");
        Drug drug2 = new Drug();
        drug2.setName("Med Name2");
        drug2.setUserID("");
        Drug drug3 = new Drug();
        drug3.setName("Med Name3");
        drug3.setUserID("");
        drugList = new ArrayList<>();
        drugList.add(drug);
        drugList.add(drug2);
        drugList.add(drug3);
    }

    @Test
    public void adapterNotNull() {
        assertNotNull(addMedicationForScheduleRecyclerAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(drugList.size(), addMedicationForScheduleRecyclerAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(addMedicationForScheduleRecyclerAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
    }

    @Test
    public void testOnBindViewHolder() {

        AddMedicationForScheduleRecyclerAdapter.MedicationHolder holder = addMedicationForScheduleRecyclerAdapter.new MedicationHolder(view);
        addMedicationForScheduleRecyclerAdapter.onBindViewHolder(holder, 0);
        TextView drugGenericNameDosage = view.findViewById(R.id.drug_generic_name_dosage);
        assertEquals(View.GONE, drugGenericNameDosage.getVisibility());
    }
}
