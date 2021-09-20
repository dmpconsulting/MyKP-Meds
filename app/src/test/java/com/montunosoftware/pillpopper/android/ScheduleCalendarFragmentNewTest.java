package com.montunosoftware.pillpopper.android;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class,shadows = {StateDownloadIntentServiceShadow.class, PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperRunTimeShadow.class})

public class ScheduleCalendarFragmentNewTest {
    private ScheduleCalendarFragmentNew scheduleCalendarFragmentNew = new ScheduleCalendarFragmentNew();

    @Before
    public  void setup(){
        TestUtil.setupTestEnvironment();
        SupportFragmentTestUtil.startFragment(scheduleCalendarFragmentNew, StateListenerActivity.class);
    }
    @After
    public void tearDown(){
        TestUtil.resetDatabase();
    }

    @Test
    public void weeklyScheduleNotNull(){
        assertThat(scheduleCalendarFragmentNew!=null);
    }

    @Test
    public void checkInitModeValue(){

    }
}
