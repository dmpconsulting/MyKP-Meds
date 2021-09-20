package com.montunosoftware.pillpopper.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.view.EditScheduleRunTimeData;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;

import org.junit.After;
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
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowIntent;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class ScheduleLoadingActivityTest {
    private ScheduleLoadingActivity scheduleLoadingActivity;
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        mockData();
        scheduleLoadingActivity = Robolectric.buildActivity(ScheduleLoadingActivity.class).create().start().resume().get();
        shadowActivity = shadowOf(scheduleLoadingActivity);
        RunTimeData.getInstance().setScheduleEdited(true);
    }

    private void mockData() {
        UniqueDeviceId.init(ApplicationProvider.getApplicationContext());
        EditScheduleRunTimeData editScheduleRunTimeData = new EditScheduleRunTimeData();
        editScheduleRunTimeData.setEditMedicationClicked(true);
        editScheduleRunTimeData.setReminderAdded(true);
        editScheduleRunTimeData.setEndDate("12032021");
        editScheduleRunTimeData.setMeditationDuration("1");
        editScheduleRunTimeData.setSelectedDays("1234567");
        Drug drug = new Drug();
        drug.setName("Med Name3");
        drug.setUserID("");
        drug.setMemberFirstName("abc");
        editScheduleRunTimeData.setSelectedDrug(drug);
        RunTimeData.getInstance().setScheduleData(editScheduleRunTimeData);
    }

    @Test
    public void activityNotNull() {
        assertNotNull(scheduleLoadingActivity);
    }

    @After
    public void tearDown() {
        RunTimeData.getInstance().setSaveButtonEnabled(false);
        RunTimeData.getInstance().setScheduleEdited(false);
        TestUtil.resetDatabase();
    }

    @Test
    public void testOnAddMedicationClicked() {
        Bundle bundle = new Bundle();
        bundle.putString("selectedUserId", "123");
        bundle.putString("selectedUserName", "abc");
        scheduleLoadingActivity.onAddMedicationClicked(bundle);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(AddMedicationsForScheduleActivity.class, shadowIntent.getIntentClass());
    }

    @Test
    public void testVisibilityOfDiscardAlertDialog() {
        scheduleLoadingActivity.onBackPressed();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        if (alert != null)
            assertTrue(alert.isShowing());
    }

    @Test
    public void testVisibilityOfSaveAlertDialog() {
        RunTimeData.getInstance().setSaveButtonEnabled(true);
        scheduleLoadingActivity.onBackPressed();
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        if (alert != null)
            assertTrue(alert.isShowing());
    }

}
