package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

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
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class AddMedicationsForScheduleActivityTest {
    private AddMedicationsForScheduleActivity addMedicationsForScheduleActivity;
    private ActivityController<AddMedicationsForScheduleActivity> controller;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        Intent intent = new Intent();
        intent.putExtra("selectedUserId", "123");
        intent.putExtra("selectedUserName", "abc");
        controller = Robolectric.buildActivity(AddMedicationsForScheduleActivity.class, intent);
        addMedicationsForScheduleActivity = controller.create().start().resume().get();
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(addMedicationsForScheduleActivity);
    }

    @Test
    public void testOnClickHomeMenu() {
        MenuItem menuItem = new RoboMenuItem(android.R.id.home);
        addMedicationsForScheduleActivity.onOptionsItemSelected(menuItem);
        assertSame(true, menuItem.isVisible());
    }

    @Test
    public void testOnClickSaveMedicationButton() {
        Button saveMedicationButton = addMedicationsForScheduleActivity.findViewById(R.id.btn_save_medications);
        saveMedicationButton.performClick();
        TextView memberName = addMedicationsForScheduleActivity.findViewById(R.id.member_name);
        assertEquals(View.VISIBLE, memberName.getVisibility());
    }


}

