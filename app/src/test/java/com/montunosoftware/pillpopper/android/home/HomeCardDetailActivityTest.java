package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.util.ReflectionHelpers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import androidx.test.core.app.ApplicationProvider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.TestCase.assertNotSame;
import static org.robolectric.Shadows.shadowOf;

/**
 * Created by M1032896 on 11/29/2017.
 */


@RunWith(RobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class,
        shadows = {DatabaseHandlerShadow.class, SecurePreferencesShadow.class})
public class HomeCardDetailActivityTest {

    private HomeCardDetailActivity homeCardDetailActivity;
    private ActivityController<HomeContainerActivity> activityActivityController;
    private LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedReminderMap = new LinkedHashMap<>();
    private Context context;


    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        ReflectionHelpers.setStaticField(UniqueDeviceId.class, "_cachedId", UUID.randomUUID().toString());
        TestUtil.setRegistrationResponse("/RegisterResponse-Existing.json");
        context = RuntimeEnvironment.systemContext;
        activityActivityController = Robolectric.buildActivity(HomeContainerActivity.class);
    }

    @Test
    public void testRefillCard() {
        HomeCard parcel = new RefillCard();
        Intent intent = new Intent();
        intent.putExtra("card", (RefillCard) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        Assert.assertNotNull(homeCardDetailActivity);
        assertEquals(parcel.getTitle(), ApplicationProvider.getApplicationContext().getString(R.string.card_title_refill_from_phone));
    }

    @Test
    public void testRefillDescription() {
        HomeCard parcel = new RefillCard();
        Intent intent = new Intent();
        intent.putExtra("card", (RefillCard) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        Assert.assertNotNull(homeCardDetailActivity);
        assertEquals(parcel.getDescription(), ApplicationProvider.getApplicationContext().getString(R.string.card_refill_description));
    }

    @Test
    public void testSetupReminderCard() {
        HomeCard parcel = new SetupReminderCard();
        Intent intent = new Intent();
        intent.putExtra("card", (SetupReminderCard) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        Assert.assertNotNull(homeCardDetailActivity);
        assertEquals(parcel.getTitle(), ApplicationProvider.getApplicationContext().getString(R.string.card_title_setup_reminder));
        assertEquals(parcel.getDescription(), ApplicationProvider.getApplicationContext().getString(R.string.card_setup_reminder_description));
    }

    @Test
    public void testCreateButton() {
        HomeCard parcel = new SetupReminderCard();
        Intent intent = new Intent();
        intent.putExtra("card", (SetupReminderCard) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        View layout = LayoutInflater.from(activityActivityController.get()).inflate(R.layout.card_detail_layout_setup_reminder, null);
        layout.findViewById(R.id.card_btn_view_meds).performClick();
        assertEquals(0, shadowOf(homeCardDetailActivity).getResultCode());
    }


    @Test
    public void testRefillButton() {
        HomeCard parcel = new RefillCard();
        Intent intent = new Intent();
        intent.putExtra("card", (RefillCard) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        View layout = LayoutInflater.from(activityActivityController.get()).inflate(R.layout.card_detail_layout_refill, null);
        layout.findViewById(R.id.card_btn_view_meds).performClick();
        assertEquals(0, shadowOf(homeCardDetailActivity).getResultCode());
    }

    @Test
    public void testManageMembersCard() {
        shadowOf(Looper.getMainLooper()).idle();
        HomeCard parcel = new ManageMemberCard();
        Intent intent = new Intent();
        intent.putExtra("card", (ManageMemberCard) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        homeCardDetailActivity.findViewById(R.id.membersRecyclerView).measure(0, 0);
        homeCardDetailActivity.findViewById(R.id.membersRecyclerView).layout(0, 0, 100, 10000);
        Assert.assertNotNull(homeCardDetailActivity);
        assertEquals(parcel.getTitle(), ApplicationProvider.getApplicationContext().getString(R.string.card_title_manage_from_phone));

        ///((MembersAdapter.ViewHolder)((RecyclerView)homeCardDetailActivity.findViewById(R.id.membersRecyclerView)).findViewHolderForAdapterPosition(1)).checkbox.isChecked()

        assertEquals(homeCardDetailActivity.findViewById(R.id.card_detail_close).getVisibility(), View.VISIBLE);

        assertNotSame(homeCardDetailActivity.findViewById(R.id.card_btn_manage_members).getAlpha(), 1F);  //Save button is visible as by default one of member is selected

        homeCardDetailActivity.findViewById(R.id.card_detail_close).performClick();
        assertEquals(0, shadowOf(homeCardDetailActivity).getResultCode());


        /*homeCardDetailActivity.findViewById(R.id.card_btn_manage_members).performClick();
        Assert.assertEquals(-1,Shadows.shadowOf(homeCardDetailActivity).getResultCode());*/
    }

    @Test
    public void testViewMedicationCard() {
        HomeCard parcel = new ViewMedicationCard();
        Intent intent = new Intent();
        intent.putExtra("card", (ViewMedicationCard) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        Assert.assertNotNull(homeCardDetailActivity);
        assertEquals(parcel.getTitle(), ApplicationProvider.getApplicationContext().getString(R.string.card_title_view_medication));
        assertEquals(homeCardDetailActivity.findViewById(R.id.card_detail_close).getVisibility(), View.VISIBLE);
        View layout = LayoutInflater.from(activityActivityController.get()).inflate(R.layout.card_detail_layout_view_medications, null);
        assertEquals(0, shadowOf(homeCardDetailActivity).getResultCode());
        assertEquals(homeCardDetailActivity.findViewById(R.id.card_btn_view_meds).getVisibility(), View.VISIBLE);
        layout.findViewById(R.id.card_btn_view_meds).performClick();
        assertEquals(0, shadowOf(homeCardDetailActivity).getResultCode());
    }


    @Test
    public void testNewKphcMedicationDetail() {
        User user = new User();
        HomeCard parcel = new KPHCCards(user, true);
        Intent intent = new Intent();
        intent.putExtra("card", (KPHCCards) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        Assert.assertNotNull(homeCardDetailActivity);
    }

    @Test
    public void testNewKphcDismiss() {
        User user = new User();
        HomeCard parcel = new KPHCCards(user, true);
        Intent intent = new Intent();
        intent.putExtra("card", (KPHCCards) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        homeCardDetailActivity.findViewById(R.id.card_footer_new_kphc_dismiss).performClick();
        assertEquals(0, shadowOf(homeCardDetailActivity).getResultCode());
    }

    @Test
    public void testManageMembersCardNoProxy() {
        shadowOf(Looper.getMainLooper()).idle();
        HomeCard parcel = new ManageMemberCard();
        Intent intent = new Intent();
        intent.putExtra("card", (ManageMemberCard) parcel);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
        homeCardDetailActivity.findViewById(R.id.membersRecyclerView).measure(0, 0);
        homeCardDetailActivity.findViewById(R.id.membersRecyclerView).layout(0, 0, 100, 10000);
        Assert.assertNotNull(homeCardDetailActivity);
        assertNotNull(parcel.getDescription());
    }


    @Test
    public void testTakenButtonClick() {
        List<Drug> list = FrontController.getInstance(context).getDrugListForOverDue(context);
        if (list.size() > 0) {
            Util.getInstance().prepareRemindersMapData(list, context);
            HomeCard parcel = new LateRemindersHomeCard(TestConfigurationProperties.MOCK_USER_ID, TestConfigurationProperties.MOCK_LATE_REMINDER_USER_NAME, list, 1, 3);
            Intent intent = new Intent();
            intent.putExtra("card", (LateRemindersHomeCard) parcel);
            // homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
            // homeCardDetailActivity.findViewById(R.id.membersRecyclerView).measure(0, 0);
            //homeCardDetailActivity.findViewById(R.id.membersRecyclerView).layout(0, 0, 100, 10000);
            //Assert.assertNotNull(homeCardDetailActivity);
            View view = LayoutInflater.from(activityActivityController.get()).inflate(R.layout.card_detail_layout_late_reminders, null);
            assertEquals(view.findViewById(R.id.card_detail_close).getVisibility(), View.VISIBLE);
            view.findViewById(R.id.card_detail_close).performClick();
            assertEquals(0, shadowOf(activityActivityController.get()).getResultCode());
            assertEquals(view.findViewById(R.id.card_footer_taken_all).getVisibility(), View.VISIBLE);
            view.findViewById(R.id.card_footer_taken_all).performClick();
            //assertEquals(-1, Shadows.shadowOf(activityActivityController.get()).getResultCode());
        }
    }

    @Test
    public void testskipButtonClick() {
        //preparePassedReminderMap();
        List<Drug> list = FrontController.getInstance(context).getDrugListForOverDue(context);
        if (list.size() > 0) {
            Util.getInstance().prepareRemindersMapData(list, context);
            HomeCard parcel = new LateRemindersHomeCard(TestConfigurationProperties.MOCK_USER_ID, TestConfigurationProperties.MOCK_LATE_REMINDER_USER_NAME, list, 1, 3);
            Intent intent = new Intent();
            intent.putExtra("card", (LateRemindersHomeCard) parcel);
            homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
            homeCardDetailActivity.findViewById(R.id.membersRecyclerView).measure(0, 0);
            homeCardDetailActivity.findViewById(R.id.membersRecyclerView).layout(0, 0, 100, 10000);
            Assert.assertNotNull(homeCardDetailActivity);
            assertEquals(homeCardDetailActivity.findViewById(R.id.card_detail_close).getVisibility(), View.VISIBLE);
            homeCardDetailActivity.findViewById(R.id.card_detail_close).performClick();
            assertEquals(0, shadowOf(homeCardDetailActivity).getResultCode());
            assertEquals(homeCardDetailActivity.findViewById(R.id.card_footer_skip_all).getVisibility(), View.VISIBLE);
            homeCardDetailActivity.findViewById(R.id.card_footer_skip_all).performClick();
            assertEquals(-1, shadowOf(homeCardDetailActivity).getResultCode());
        }
    }



    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        if (homeCardDetailActivity != null)
            homeCardDetailActivity.finish();
    }

}
