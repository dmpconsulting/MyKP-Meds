package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by m1032896 on 11/15/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class HomeCardAdapterTest {

    private HomeCardAdapter homeCardAdapter;
    private HomeContainerActivity homeContainerActivity;
    private Context context;
    private DisplayMetrics displayMetrics;
    private List<HomeCard> homeCardList;


    @Before
    public void setup() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        context = homeContainerActivity.getAndroidContext();
        mockData();
        displayMetrics = new DisplayMetrics();
        homeCardAdapter = new HomeCardAdapter(displayMetrics, homeCardList, context, 1);

    }

    private void mockData() {
        homeCardList = new ArrayList<>();
        HomeCard refillCard = new RefillCard();
        User user = new User();
        HomeCard kphcCards = new KPHCCards(user, true);
        HomeCard kphcCards1 = new KPHCCards(user, false);
        HomeCard manageMemberCard = new ManageMemberCard();
        List<DiscontinuedDrug> discontinuedDrugList = new ArrayList<>();
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
        discontinuedDrugList.add(discontinuedDrug);
        discontinuedDrugList.add(discontinuedDrug1);
        HomeCard kphcDiscontinueCard = new KPHCDiscontinueCard(discontinuedDrugList, context);
        HomeCard refillReminderOverdueCard = new RefillReminderOverdueCard(TestUtil.prepareMockOverDueRefillReminder(), 1);
        HomeCard teenProxyHomeCard = new TeenProxyHomeCard(context);
        List<Drug> drugList = new ArrayList<>();
        Drug drug = new Drug();
        drug.setName("Med Name1");
        drug.setUserID("123");
        Drug drug2 = new Drug();
        drug2.setName("Med Name2");
        drug2.setUserID("1234");
        drugList.add(drug);
        drugList.add(drug2);
        HomeCard lateRemindersHomeCard = new LateRemindersHomeCard(TestConfigurationProperties.MOCK_USER_ID,
                TestConfigurationProperties.MOCK_LATE_REMINDER_USER_NAME, drugList, 1, 1);
        Long reminderTime = Long.valueOf(TestConfigurationProperties.MOCK_REMINDER_TIME);
        LinkedHashMap<Long, List<Drug>> dummyMap = new LinkedHashMap<>();
        dummyMap.put(reminderTime, FrontController.getInstance(RuntimeEnvironment.systemContext).getAllDrugs(context));
        PillpopperRunTime.getInstance().setCurrentRemindersByUserIdForCard(dummyMap);
        HomeCard currentReminderCard = new CurrentReminderCard(context, reminderTime, 1);
        HomeCard batteryOptimizerInfoCard = new BatteryOptimizerInfoCard(context);
        homeCardList.add(kphcCards);
        homeCardList.add(kphcCards1);
        homeCardList.add(refillCard);
        homeCardList.add(manageMemberCard);
        homeCardList.add(kphcDiscontinueCard);
        homeCardList.add(refillReminderOverdueCard);
        homeCardList.add(teenProxyHomeCard);
        homeCardList.add(lateRemindersHomeCard);
        homeCardList.add(currentReminderCard);
        homeCardList.add(batteryOptimizerInfoCard);
    }

    @Test
    public void testAdapter() {
        assertNotNull(homeCardAdapter);
    }

    @Test
    public void testGetItemViewType() {
        assertEquals(1, homeCardAdapter.getItemViewType(0));
        assertEquals(2, homeCardAdapter.getItemViewType(1));
        assertEquals(0, homeCardAdapter.getItemViewType(2));
        assertEquals(0, homeCardAdapter.getItemViewType(3));
        assertEquals(6, homeCardAdapter.getItemViewType(4));
        assertEquals(4, homeCardAdapter.getItemViewType(5));
        assertEquals(8, homeCardAdapter.getItemViewType(6));
        assertEquals(3, homeCardAdapter.getItemViewType(7));
        assertEquals(5, homeCardAdapter.getItemViewType(8));
        assertEquals(7, homeCardAdapter.getItemViewType(9));
    }

    @Test
    public void testGetItemCount() {
        assertEquals(homeCardList.size(), homeCardAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 1));
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 2));
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 3));
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 4));
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 5));
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 6));
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 7));
        assertNotNull(homeCardAdapter.onCreateViewHolder(new RelativeLayout(context), 8));
    }

 /*   @Test
    public void testOnBindViewHolder() {
        LayoutInflater inflater = (LayoutInflater) homeContainerActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        view = inflater.inflate(R.layout.home_card, null, false);
        HomeCardAdapter.ViewHolder viewHolder = homeCardAdapter.new ViewHolder(null, view);
        homeCardAdapter.onBindViewHolder(viewHolder, 2);
    }*/
}
