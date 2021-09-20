package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.Schedule;

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
public class DrugListRecyclerDBAdapterTest {
    private DrugListRecyclerDBAdapter drugListRecyclerDBAdapter;
    private Context context;
    private List<Drug> drugList;
    private LayoutInflater inflater;

    @Before
    public void setUp() {
        StateListenerActivity stateListenerActivity = Robolectric.buildActivity(StateListenerActivity.class).create().get();
        context = stateListenerActivity.getApplicationContext();
        mockData();
        drugListRecyclerDBAdapter = new DrugListRecyclerDBAdapter(stateListenerActivity, drugList, true, stateListenerActivity.getState());
        inflater = (LayoutInflater) stateListenerActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void mockData() {
        UniqueDeviceId.init(context);
        Drug drug = new Drug();
        drug.setName("Med Name1");
        drug.setUserID("");
        drug.setIsTempHeadr(true);
        Drug drug2 = new Drug();
        Schedule schedule = new Schedule();
        schedule.setSchedType(Schedule.SchedType.INTERVAL);
        schedule.setSchedType(Schedule.SchedType.AS_NEEDED);
        drug2.setSchedule(schedule);
        drug2.setName("Med Name2");
        drug2.setUserID("");
        drug2.setNotes("abc");
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
        assertNotNull(drugListRecyclerDBAdapter);
    }

    @Test
    public void testGetItemViewType() {
        assertEquals(0, drugListRecyclerDBAdapter.getItemViewType(0));
        assertEquals(1, drugListRecyclerDBAdapter.getItemViewType(1));
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(drugListRecyclerDBAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
        assertNotNull(drugListRecyclerDBAdapter.onCreateViewHolder(new RelativeLayout(context), 1));
    }

    @Test
    public void testGetItemCount() {
        assertEquals(drugList.size(), drugListRecyclerDBAdapter.getItemCount());
    }

    @Test
    public void testGetDrugsCount() {
        assertEquals(2, drugListRecyclerDBAdapter.getDrugsCount());
    }

    @Test
    public void testOnBindViewHolder() {
        View view;
        view = inflater.inflate(R.layout.drug_recycler_member_name, null, false);
        DrugListRecyclerDBAdapter.ViewHolderHeader holderHeader = drugListRecyclerDBAdapter.new ViewHolderHeader(view);
        drugListRecyclerDBAdapter.onBindViewHolder(holderHeader, 0);
        ImageView shareImage = view.findViewById(R.id.share_med_list);
        shareImage.performClick();
        view = inflater.inflate(R.layout.drug_recycler_item, null, false);
        DrugListRecyclerDBAdapter.ViewHolder holder = drugListRecyclerDBAdapter.new ViewHolder(view);
        drugListRecyclerDBAdapter.onBindViewHolder(holder, 1);
        CheckBox drugSelectionCheckbox = view.findViewById(R.id.drug_select_img_btn);
        drugSelectionCheckbox.performClick();
        ImageView medNotes = view.findViewById(R.id.med_notes);
        assertEquals(View.VISIBLE, medNotes.getVisibility());
        TextView emptyView = view.findViewById(R.id.empty_view);
        assertEquals(View.VISIBLE, emptyView.getVisibility());
    }

}
