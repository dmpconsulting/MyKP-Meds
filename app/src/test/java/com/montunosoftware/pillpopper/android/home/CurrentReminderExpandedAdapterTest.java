package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class CurrentReminderExpandedAdapterTest {
    private CurrentReminderExpandedAdapter currentReminderExpandedAdapter;
    private Context context;
    private HomeCardDetailActivity homeCardDetailActivity;
    private TreeMap<User, LinkedList<Drug>> currentReminderByUserName;
    private Long reminderTime;

    @Before
    public void setUp() {
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class).get();
        context = homeCardDetailActivity.getApplicationContext();
        mockData();
        currentReminderExpandedAdapter = new CurrentReminderExpandedAdapter(context, currentReminderByUserName, reminderTime, 1);
    }

    private void mockData() {
        UniqueDeviceId.init(context);
        reminderTime = Long.valueOf(TestConfigurationProperties.MOCK_REMINDER_TIME);
        LinkedList<Drug> drugLinkedList = new LinkedList<>();
        Drug drug = new Drug();
        drug.setName("Med Name1");
        drug.setUserID("");
        drug.setDose("1");
        drug.setmAction(PillpopperConstants.NO_ACTION_TAKEN);
        Drug drug2 = new Drug();
        drug2.setName("Med Name2");
        drug2.setUserID("");
        drug2.setDose("2");
        Drug drug3 = new Drug();
        drug3.setName("Med Name3");
        drug3.setUserID("");
        drug3.setDose("3");
        drugLinkedList.add(drug);
        drugLinkedList.add(drug2);
        drugLinkedList.add(drug3);
        User user = new User();
        user.setFirstName("john");
        user.setDisplayName("Abc");
        user.setAge("12");
        user.setEnabled("T");
        user.setUserType("male");
        user.setUserId("123");
        currentReminderByUserName = new TreeMap<>();
        if (!drugLinkedList.isEmpty()) {
            for (Drug d : drugLinkedList) {
                if (!currentReminderByUserName.containsKey(user)) {
                    currentReminderByUserName.put(user, new LinkedList<Drug>());
                }
                currentReminderByUserName.get(user).add(d);
            }
        }

    }

    @Test
    public void adapterShouldNotNull() {
        assertNotNull(currentReminderExpandedAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(1, currentReminderExpandedAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(currentReminderExpandedAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
    }

    @Test
    public void testOnBindViewHolder() {
        LayoutInflater inflater = (LayoutInflater) homeCardDetailActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.current_reminder_expanded_item, null, false);
        CurrentReminderExpandedAdapter.ViewHolder viewHolder = new  CurrentReminderExpandedAdapter.ViewHolder(view);
        currentReminderExpandedAdapter.onBindViewHolder(viewHolder, 0);
        ImageView userLevelOverflowIcon = view.findViewById(R.id.drug_username_action);
        assertEquals(View.VISIBLE, userLevelOverflowIcon.getVisibility());
        assertThat(userLevelOverflowIcon.performClick()).isTrue();
    }

    @Test
    public void testGetCurrentReminderDrugsCount() {
        assertEquals(3, currentReminderExpandedAdapter.getCurrentReminderDrugsCount());
    }
}
