package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperRunTimeShadow.class})
public class LateReminderDetailAdapterTest {
    private HomeCardDetailActivity homeCardDetailActivity;
    private LateReminderDetailAdapter lateReminderDetailAdapter;
    private Context context;
    private List<Drug> drugList;
    private LayoutInflater inflater;

    @Before
    public void setUp() {
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class).get();
        context = homeCardDetailActivity.getApplicationContext();
        mockData();
        lateReminderDetailAdapter = new LateReminderDetailAdapter(drugList, context, TestConfigurationProperties.MOCK_USER_ID, 1);
        inflater = (LayoutInflater) homeCardDetailActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private void mockData() {
        UniqueDeviceId.init(context);
        Drug drug = new Drug();
        drug.setName("Med Name1");
        drug.setUserID("");
        drug.setDose("1");
        PillpopperTime time = new PillpopperTime(1214);
        drug.setScheduledTime(time);
        Drug drug2 = new Drug();
        drug2.setName("Med Name2");
        drug2.setUserID("");
        drug2.setDose("2");
        PillpopperTime time2 = new PillpopperTime(1214);
        drug2.setScheduledTime(time2);
        Drug drug3 = new Drug();
        drug3.setName("Med Name3");
        drug3.setUserID("");
        drug3.setDose("3");
        PillpopperTime time3 = new PillpopperTime(1212);
        drug3.setScheduledTime(time3);
        drugList = new ArrayList<>();
        drugList.add(drug);
        drugList.add(drug2);
        drugList.add(drug3);
    }

    @Test
    public void adapterShouldNotNull() {
        assertNotNull(lateReminderDetailAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(3, lateReminderDetailAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(lateReminderDetailAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
        assertNotNull(lateReminderDetailAdapter.onCreateViewHolder(new RelativeLayout(context), 1));
    }

    @Test
    public void testGetItemViewType() {
        assertEquals(0, lateReminderDetailAdapter.getItemViewType(0));
        assertEquals(1, lateReminderDetailAdapter.getItemViewType(1));
        assertEquals(0, lateReminderDetailAdapter.getItemViewType(2));
    }

    @Test
    public void testOnBindViewHolder() {
        View view;
        view = inflater.inflate(R.layout.late_reminder_item_with_header, null, false);
        LateReminderDetailAdapter.LateReminderItemWithTimeHeaderViewHolder lateReminderItemWithTimeHeaderViewHolder =
                lateReminderDetailAdapter.new LateReminderItemWithTimeHeaderViewHolder(view);
        lateReminderDetailAdapter.onBindViewHolder(lateReminderItemWithTimeHeaderViewHolder, 0);
        ImageView overFlowActionsImageView = view.findViewById(R.id.lr_overflow_actions_icon);
        assertThat(overFlowActionsImageView.performClick()).isTrue();
    }
}
