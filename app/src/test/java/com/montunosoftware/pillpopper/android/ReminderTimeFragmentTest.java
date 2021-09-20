package com.montunosoftware.pillpopper.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.RelativeLayout;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.controller.FrontControllerShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandler;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, FrontControllerShadow.class, PillpopperRunTimeShadow.class})
public class ReminderTimeFragmentTest {
    private ReminderTimeFragment reminderTimeFragment;
    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        reminderTimeFragment = new ReminderTimeFragment();
        SupportFragmentTestUtil.startFragment(reminderTimeFragment, HomeContainerActivity.class);
    }

    @After
    public void resetDatabase() {
        TestUtil.resetDatabase();
        reminderTimeFragment.onPause();
        reminderTimeFragment.onDestroyView();
    }

    @Test
    public void ScheduleFragmentShouldNotBeNull() {
        assertNotNull(reminderTimeFragment);
    }
    @Test
    public void  fragmentViewShouldNotBeNull() {

        assertThat(reminderTimeFragment.getView()).isNotNull();
    }
    @Test
    public void shouldShowDuplicateRemindersAlert() {
        RelativeLayout layout = reminderTimeFragment.getView().findViewById(R.id.add_reminder_layout);
        layout.performClick();
        if (layout.performClick()) {
            AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
            if (alert != null)
                assertTrue(alert.isShowing());
        }
    }

    @Test
    public void testResetStartDate() {
        reminderTimeFragment.resetStartDate();
        assertTrue(RunTimeData.getInstance().isScheduleEdited());
    }

    @Test
    public void testResetEndDate() {
        reminderTimeFragment.resetEndDate();
        assertTrue(RunTimeData.getInstance().isScheduleEdited());
    }

    @Test
    public void testAddNewReminder() {
        String[] reminder = {"7:00 AM", "8:00PM"};
        for (String s : reminder) {
            reminderTimeFragment.addNewReminder(s);
        }
    }

    @Test
    public void testOnStartDateSelected()
    {
        reminderTimeFragment.onStartDateSelected(2);
        assertTrue(RunTimeData.getInstance().isScheduleEdited());
    }

    @Test
    public void testOnActivityResult() {
        Intent intent = new Intent();
        reminderTimeFragment.onActivityResult(1, Activity.RESULT_OK, intent);
    }

}
