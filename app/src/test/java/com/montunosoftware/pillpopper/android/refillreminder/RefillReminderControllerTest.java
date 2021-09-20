package com.montunosoftware.pillpopper.android.refillreminder;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, SecurePreferencesShadow.class})
public class RefillReminderControllerTest {
    private RefillReminderController refillReminderController;
    private Context context;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        context = ApplicationProvider.getApplicationContext().getApplicationContext();
        refillReminderController = RefillReminderController.getInstance(context);
    }

    @Test
    public void testgetRefillReminders() {
        assertNotNull(refillReminderController.getRefillReminders());
    }

    @Test
    public void testgetFutureRefillReminders() {
        assertNotNull(refillReminderController.getFutureRefillReminders());
    }

    @Test
    public void testgetRefillRemindersCount() {
        assertNotEquals(12, refillReminderController.getRefillRemindersCount());
    }

    @Test
    public void testgetNextRefillReminders() {
        assertNotNull(refillReminderController.getNextRefillReminders());
    }

    @Test
    public void testgetRefillRemindersByNextReminderTime() {
        assertNotNull(refillReminderController.getRefillRemindersByNextReminderTime("120587828"));
    }

    @Test
    public void getOverdueRefillRemindersForRefresh() {
        assertNotNull(refillReminderController.getOverdueRefillRemindersForRefresh());
    }

    @Test
    public void testgetOverdueRefillRemindersForCards() {
        assertNotNull(refillReminderController.getOverdueRefillRemindersForCards());
    }
}
