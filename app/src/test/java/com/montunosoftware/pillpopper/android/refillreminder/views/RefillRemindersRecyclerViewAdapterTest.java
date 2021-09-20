package com.montunosoftware.pillpopper.android.refillreminder.views;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;

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
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class RefillRemindersRecyclerViewAdapterTest {
    private HomeContainerActivity homeContainerActivity;
    private RefillRemindersRecyclerViewAdapter refillRemindersRecyclerViewAdapter;
    private List<RefillReminder> result;

    @Before
    public void setUp() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        mockData();
        refillRemindersRecyclerViewAdapter = new RefillRemindersRecyclerViewAdapter(homeContainerActivity, result, false);

    }

    private void mockData() {
        result = new ArrayList<>();
        RefillReminder refillReminder = new RefillReminder();
        refillReminder.setLastAcknowledgeDate("");
        refillReminder.setRecurring(true);
        refillReminder.setReminderGuid("");
        refillReminder.setNextReminderDate("1234567");
        refillReminder.setRecurring(true);
        result.add(0, refillReminder);
    }

    @Test
    public void adapterShouldNotNull() {
        assertNotNull(refillRemindersRecyclerViewAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(result.size(), refillRemindersRecyclerViewAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(refillRemindersRecyclerViewAdapter.onCreateViewHolder(new RelativeLayout(homeContainerActivity), 0));
    }

    @Test
    public void testOnBindViewHolder() {
        refillRemindersRecyclerViewAdapter.setChangeDeleteButtonListener(new RefillRemindersListFragment());
        LayoutInflater inflater = (LayoutInflater) homeContainerActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listItemView;
        listItemView = inflater.inflate(R.layout.refill_reminder_recycler_item, null, false);
        RefillRemindersRecyclerViewAdapter.ViewHolder viewHolder = refillRemindersRecyclerViewAdapter.new ViewHolder(listItemView);
        refillRemindersRecyclerViewAdapter.onBindViewHolder(viewHolder, 0);
        ImageView repeatImage = listItemView.findViewById(R.id.refill_repeat);
        assertEquals(View.VISIBLE, repeatImage.getVisibility());
    }
}
