package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.controller.FrontControllerShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, FrontControllerShadow.class, PillpopperRunTimeShadow.class})
public class ScheduleWizardFragmentTest {
    private ScheduleWizardFragment scheduleFragment;
    private Context context;
    private HomeContainerActivity homeContainerActivity;
    private ShadowActivity shadowActivity;
    private View view;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        TestUtil.setRegistrationResponse("/RegisterResponse-Existing.json");
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        context = homeContainerActivity.getAndroidContext();
        scheduleFragment = new ScheduleWizardFragment();
        SupportFragmentTestUtil.startFragment(scheduleFragment, HomeContainerActivity.class);
        view = scheduleFragment.getView();
        shadowActivity = Shadows.shadowOf(homeContainerActivity);
    }

    @After
    public void resetDatabase() {
        TestUtil.resetDatabase();
        scheduleFragment.onStop();
        scheduleFragment.onDestroy();
        scheduleFragment.onDestroyView();

    }

    @Test
    public void ScheduleFragmentShouldNotBeNull() {
        assertNotNull(scheduleFragment);
    }

    @Test
    public void testVisibility() {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assertEquals(View.GONE, toolbar.getVisibility());
    }

    @Test
    public void testShowDrugDetails() {
        Drug drug = new Drug();
        drug.setName("abc");
        scheduleFragment.showDrugDetails(drug, context, view);
        Intent intent = shadowActivity.peekNextStartedActivity();
        assertEquals(MedicationDetailActivity.class.getCanonicalName(), intent.getComponent().getClassName());
    }

    @Test
    public void testOnScheduleMedication() {
        scheduleFragment.onScheduleMedication(1);
        scheduleFragment.onScheduleMedication(2);
        scheduleFragment.onScheduleMedication(3);
        scheduleFragment.onScheduleMedication(4);
        TextView edit = view.findViewById(R.id.edit);
        assertFalse(edit.isClickable());
    }

    @Test
    public void testOnEditClicked() {
        scheduleFragment.onEditClicked();
        View summaryDivider = view.findViewById(R.id.summaryDivider);
        assertEquals(View.VISIBLE, summaryDivider.getVisibility());
    }

    @Test
    public void testOnSpinnerClick() {
        scheduleFragment.onSpinnerClick();
    }


}
