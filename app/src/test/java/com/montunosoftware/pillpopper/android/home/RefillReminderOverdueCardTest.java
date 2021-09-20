package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.view.View;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RefillReminderDBHandlerShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by M1023050 on 02-Jul-19.
 */


@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, RefillReminderDBHandlerShadow.class})

public class RefillReminderOverdueCardTest {

    private RefillReminderOverdueCard refillReminderOverdueCard;
    private ActivityController<HomeContainerActivity> activityActivityController;
    private Context context;


    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        RunTimeData.getInstance().setHomeCardsShown(true);
        activityActivityController = Robolectric.buildActivity(HomeContainerActivity.class);
        refillReminderOverdueCard = new RefillReminderOverdueCard(TestUtil.prepareMockOverDueRefillReminder(), 1);
        context = ApplicationProvider.getApplicationContext();
        refillReminderOverdueCard.setContext(activityActivityController.get());

    }

    @Test
    public void testRefillOverdueDate(){
        assertNotNull(refillReminderOverdueCard.getRefillOverdueDate());
    }

    @Test
    public void testRefillOverdueTime(){
        assertNotNull(refillReminderOverdueCard.getRefillOverdueTime());
    }

    @Test
    public void testInitDetailView(){
        refillReminderOverdueCard.initDetailView();
    }

    @Test
    public void testGetRefillReminder(){
        assertNotNull(refillReminderOverdueCard.getRefillReminder());
    }

    @Test
    public void testOnDismissInDetailCard(){
        RefillReminderController.getInstance(activityActivityController.get());
        refillReminderOverdueCard.onDismissInDetailCard(new View(context));
    }

    @Test
    public void testOnRefillNowInDetailCard(){
        RefillReminderController.getInstance(activityActivityController.get());
        refillReminderOverdueCard.onRefillNowInDetailCard(new View(context));
    }
}
