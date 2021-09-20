package com.montunosoftware.pillpopper.android.home;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.NewKphcCardBinding;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.AddOrEditMedicationActivity;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


/**
 * Created by m1032896 on 11/14/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class, PillpopperRunTimeShadow.class})
public class HomeFragmentTest {

    private HomeFragment homeFragment;
    private HomeContainerActivity homeContainerActivity;
    private Context mContext;
    private View view;

    @Before
    public void setup() {
       homeContainerActivity= Robolectric.buildActivity(HomeContainerActivity.class).create().get();
       mContext=homeContainerActivity.getApplicationContext();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        TestUtil.setupTestEnvironment();
        RunTimeData.getInstance().setHomeCardsShown(true);
        PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(true);
        initFragment(true);
    }

    private void initFragment(boolean isWelcomeScreenTobeShown) {
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        AppConstants.setWelcomeScreensDisplayResult(isWelcomeScreenTobeShown?"1":"0");
        homeFragment = new HomeFragment();
        SupportFragmentTestUtil.startFragment(homeFragment, HomeContainerActivity.class);
        view = homeFragment.getView();
        view.findViewById(R.id.card_list).measure(0, 0);
        view.findViewById(R.id.card_list).layout(0, 0, 100, 10000);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        homeFragment.onStop();
        homeFragment.onDestroy();
    }

    @Test
    public void testFragmentViews() {
        assertNotNull(view);
    }

    @Test
    public void greetShouldNotBeNull(){
        TextView greeting = (TextView)view.findViewById(R.id.txtHomeMessage);
        assertNotNull(greeting.getText().toString());
        assertEquals(Util.getHomeGreeting(ApplicationProvider.getApplicationContext()),greeting.getText().toString());
    }

    @Test
    public void homeDateShouldNotBeNull(){
        TextView greeting = (TextView)view.findViewById(R.id.txtHomeDate);
        assertNotNull(greeting.getText().toString());
        assertEquals(Util.getHomeDate(),greeting.getText().toString());
    }

    @Test
    public void testWelcomeCardsDisplay(){
        if(((RecyclerView)view.findViewById(R.id.card_list)).getAdapter()!=null)
        assert(((RecyclerView)view.findViewById(R.id.card_list)).getAdapter().getItemCount()>=4);
    }

    @Test
    public void testKPHCContractedCardsDisplay(){
        initFragment(false); // excluding welcome cards
        RecyclerView  recyclerView = ((RecyclerView)view.findViewById(R.id.card_list));
        if(recyclerView!=null && recyclerView.getAdapter()!=null) {
            int count = recyclerView.getAdapter().getItemCount();
            assert (count >= 1); //Kphc card shown

            if (count > 0) {
                if (((HomeCardAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0)).getBinding() instanceof NewKphcCardBinding) {
                    int reqCode = ((NewKphcCardBinding) ((HomeCardAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0)).getBinding()).getHandler().getRequestCode();
                    assertEquals(PillpopperConstants.REQUEST_NEW_KPHC_CARD_DETAIL, reqCode); //NEW KPHC card is shown
                }
            }
        }
    }
   @Test
   public void testSetUpRemaindersButtonClick()
   {
       TextView reminderText = (TextView)view.findViewById(R.id.textView_setup_reminder);
       assertNotNull(reminderText.getText().toString());
       Button setUpRemainderButton=view.findViewById(R.id.btn_setup_reminders);
       setUpRemainderButton.performClick();
       /*Toolbar homeToolbar=(Toolbar)homeContainerActivity.getActivity().findViewById(R.id.app_bar_home);
       Toolbar toolbar=(Toolbar)homeContainerActivity.getActivity().findViewById(R.id.app_bar);
       assertEquals(View.VISIBLE,homeToolbar.getVisibility());*/

   }
   @Test
    public void testCreateRefillRemainderButtonClick()
   {
       TextView reminderText = (TextView)view.findViewById(R.id.textView_create_refill_remind);
       assertNotNull(reminderText.getText().toString());
       Button setUpRemainderButton=view.findViewById(R.id.btn_create_refill_remind);
       setUpRemainderButton.performClick();
   }

   @Test
    public void testRefillMedicationsButtonClick()
   {
       TextView reminderText = (TextView)view.findViewById(R.id.textView_refill_med);
       assertNotNull(reminderText.getText().toString());
       Button setUpRemainderButton=view.findViewById(R.id.btn_refill_medications);
       setUpRemainderButton.performClick();
   }

   @Test
    public void testAddMedicationsButtonClick()
   {
       TextView reminderText = (TextView)view.findViewById(R.id.textView_add_med);
       assertNotNull(reminderText.getText().toString());
       Button setUpRemainderButton=view.findViewById(R.id.btn_add_med);
       setUpRemainderButton.performClick();
       Intent expIntent = new Intent(mContext, AddOrEditMedicationActivity.class);
       ShadowApplication shadowApplication = Shadows.shadowOf((Application) mContext);
       Intent actualIntent = shadowApplication.getNextStartedActivity();
       Assert.assertEquals(expIntent.getComponent(), actualIntent.getComponent());
   }
  /* @Test
    public void testFindPharmacyButtonClick()
   {
       TextView reminderText = (TextView)homeFragment.getView().findViewById(R.id.locate_pharmacy);
       assertNotNull(reminderText.getText().toString());
       Button setUpRemainderButton=homeFragment.getView().findViewById(R.id.btn_find_pharmacy);
       setUpRemainderButton.performClick();
   }*/

   @Test
    public void testGuideTextClick()
   {
       LinearLayout layoutContainer = view.findViewById(R.id.guide_container);
       assertThat(layoutContainer.performClick()).isTrue();
   }
}
