package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.widget.Button;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;
import com.montunosoftware.pillpopper.model.ScheduleMainDrug;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class ScheduleFragmentNewTest
{
    private ScheduleFragmentNew  scheduleFragmentNew;
    private HomeContainerActivity homeContainerActivity;
    private ActivityController<HomeContainerActivity> controller;
    private ShadowActivity shadowActivity;
    @Before
    public void setup(){
        TestUtil.setupTestEnvironment();
        scheduleFragmentNew = new ScheduleFragmentNew();
        SupportFragmentTestUtil.startFragment(scheduleFragmentNew,HomeContainerActivity.class);
        Intent intent=new Intent();
        intent.putExtra("NeedMyMedTab", true);
        controller = Robolectric.buildActivity(HomeContainerActivity.class,intent);
        homeContainerActivity=controller.create().start().resume().get();
        shadowActivity = shadowOf(homeContainerActivity);
    }
    @Test
    public void fragmentShouldNotNull() {

        assertThat(scheduleFragmentNew != null);
    }

    @Test
    public void fragmentViewShouldNotBeNull() {
        assertThat(scheduleFragmentNew.getView()).isNotNull();
    }
    @After
    public void tearDown()
    {
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
        scheduleFragmentNew.onStop();
    }

    @Test
    public void testOnDrugClicked()
    {
        ScheduleMainDrug scheduleMainDrug = new ScheduleMainDrug();
        scheduleMainDrug.setDose("1");
        scheduleMainDrug.setPillName("abc");
        scheduleMainDrug.setDayPeriod("2");
        scheduleFragmentNew.onDrugClicked(scheduleMainDrug);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(MedicationDetailActivity.class, shadowIntent.getIntentClass());
    }

    @Test
    public void testOnAddMedSchedule()
    {
        scheduleFragmentNew.onAddMedSchedule();
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(AddOrEditMedicationActivity.class, shadowIntent.getIntentClass());
    }

}
