package com.montunosoftware.pillpopper.android;

import android.view.View;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.view.EditScheduleRunTimeData;
import com.montunosoftware.pillpopper.android.view.WeeklyScheduleWizardFragment;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class,shadows = {StateDownloadIntentServiceShadow.class, PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperRunTimeShadow.class})

public class WeeklyScheduleWizardFragmentTest {

    private WeeklyScheduleWizardFragment weeklyScheduleWizardFragment;
    private View view;

    @Before
    public  void setup(){
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        mockData();
        weeklyScheduleWizardFragment = new WeeklyScheduleWizardFragment();
        SupportFragmentTestUtil.startFragment(weeklyScheduleWizardFragment, StateListenerActivity.class);
        view = weeklyScheduleWizardFragment.getView();
    }

    private void mockData() {
        EditScheduleRunTimeData editScheduleRunTimeData = new EditScheduleRunTimeData();
        editScheduleRunTimeData.setEditMedicationClicked(true);
        editScheduleRunTimeData.setReminderAdded(true);
        editScheduleRunTimeData.setEndDate("12032021");
        editScheduleRunTimeData.setMeditationDuration("1");
        editScheduleRunTimeData.setSelectedDays("1234567");
        RunTimeData.getInstance().setScheduleData(editScheduleRunTimeData);
    }

    @After
    public void tearDown(){
        TestUtil.resetDatabase();
    }

    @Test
    public void weeklyScheduleNotNull(){
        assertThat(weeklyScheduleWizardFragment!=null);
    }
    @Test
    public void viewShouldNotNull()
    {
        assertNotNull(view);
    }

}
