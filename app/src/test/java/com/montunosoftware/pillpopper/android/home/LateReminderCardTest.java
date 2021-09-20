package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config( sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperRunTimeShadow.class})
public class LateReminderCardTest {
    private Long reminderTime;
    private LateRemindersHomeCard reminderCard;
    private final String userID = TestConfigurationProperties.MOCK_USER_ID;
    private final String userName = TestConfigurationProperties.MOCK_LATE_REMINDER_USER_NAME;
    private HomeContainerActivity homeContainerActivity;
    private Context context;
    private List<Drug> drugList;
    private ShadowActivity shadowActivity;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().start().resume().get();
        context = homeContainerActivity.getAndroidContext();
        mockData();
        reminderCard = new LateRemindersHomeCard(userID, userName,
                drugList, 1, 1);
        reminderCard.setContext(context);
        shadowActivity = shadowOf(homeContainerActivity);
    }

    private void mockData() {
        ReflectionHelpers.setStaticField(UniqueDeviceId.class, "_cachedId", UUID.randomUUID().toString());
        UniqueDeviceId.init(context);
        drugList = new ArrayList<>();
        Drug drug = new Drug();
        drug.setName("Med Name1");
        drug.setUserID("123");
        Drug drug2 = new Drug();
        drug2.setName("Med Name2");
        drug2.setUserID("1234");
        drugList.add(drug);
        drugList.add(drug2);
        reminderTime = Long.valueOf(TestConfigurationProperties.MOCK_LATE_REMINDER_TIME);
        LinkedHashMap<Long, List<Drug>> dummyMap = new LinkedHashMap<>();
        dummyMap.put(reminderTime,drugList);
        LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedDrugMap = new LinkedHashMap<>();
        passedDrugMap.put(userID, dummyMap);
        PillpopperRunTime.getInstance().setPassedReminderersHashMapByUserId(passedDrugMap);
    }

    @Test
    public void lateReminderShouldNotBeNull() {
        assertNotNull(PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId());
    }

    @Test
    public void userNameShouldNotBeNull() {
        assertNotNull(reminderCard.getUserName());
    }

    @Test
    public void userIdNotNull() {
        assertNotNull(reminderCard.getUserID());
    }

    @Test
    public void checkReminderTakenButton() {
        assertEquals(context.getString(R.string.taken_all), reminderCard.getTakenButtonText());
    }

    @Test
    public void checkReminderSkipButton() {
        assertEquals(context.getString(R.string.skipped_all), reminderCard.getSkippedButtonText());
    }

    @Test
    public void testDoRefresh(){
        reminderCard.doRefresh(userID, true, homeContainerActivity);
    }

    @Test
    public void testOnTakenAllCard()
    {
        reminderCard.onTakenAllClick(new RelativeLayout(context));
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(LoadingActivity.class, shadowIntent.getIntentClass());
    }

    @Test
    public void testOnTakenAllInDetailCard()
    {
        reminderCard.onTakenAllInDetailCard(new RelativeLayout(context));
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(LoadingActivity.class, shadowIntent.getIntentClass());
    }
    @Test
    public void testGetResourceString()
    {
        assertEquals("Skipped",reminderCard.getResourceString(R.string.drug_action_skipped));
    }

    @Test
    public void testInitDetailView()
    {
        LayoutInflater layoutInflater = (LayoutInflater) homeContainerActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(reminderCard.getDetailView(),null,false);
        RecyclerView lateReminderRecyclerView = view.findViewById(R.id.recycler_late_reminder_list);
        Button takeButton = view.findViewById(R.id.card_footer_taken_all);
        Button skipButton = view.findViewById(R.id.card_footer_skip_all);
        reminderCard.initDetailView(lateReminderRecyclerView,takeButton,skipButton,userID,1);
        assertEquals("Taken All",takeButton.getText());
        assertEquals("Skipped All",skipButton.getText());
        assertFalse(reminderCard.isButtonRefreshRequired(userID));
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
    }
}
