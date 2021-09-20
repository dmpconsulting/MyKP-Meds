package com.montunosoftware.pillpopper.refillreminder;

import android.content.Context;

import com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbUtils;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceIdShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import androidx.test.core.app.ApplicationProvider;

/**
 * Created by M1023050 on 8/7/2018.
 */

@RunWith(RobolectricTestRunner.class)
@Config( sdk = TestConfigurationProperties.BUILD_SDK_VERSION, manifest="AndroidManifest.xml", application = TestPillpopperApplication.class, shadows = {PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, UniqueDeviceIdShadow.class})
public class RefillReminderDBUtilsTest {


    private Context context;
    private RefillReminderDbUtils mRefillReminderDbUtils;


    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext().getApplicationContext();
        mRefillReminderDbUtils = RefillReminderDbUtils.getInstance(context);
    }

    @Test
    public void testGetRefillReminders(){
        List<RefillReminder> list = mRefillReminderDbUtils.getRefillReminders();
    }

    @Test
    public void testGetNextRefillReminders(){
        List<RefillReminder> list = mRefillReminderDbUtils.getNextRefillReminders();
    }

    @Test
    public void testGetFutureRefillReminders(){
        List<RefillReminder> list = mRefillReminderDbUtils.getFutureRefillReminders();
    }

    @Test
    public void testgetRefillRemindersCount(){
        mRefillReminderDbUtils.getRefillRemindersCount();
    }

    @Test
    public void testGetRefillRemindersByNextReminderTime(){
        mRefillReminderDbUtils.getRefillRemindersByNextReminderTime(TestConfigurationProperties.MOCK_REFILL_REMINDER_NEXT_REMINDER_TIME);
    }

    @Test
    public void testgetOverdueRefillRemindersForRefresh(){
        mRefillReminderDbUtils.getOverdueRefillRemindersForRefresh();
    }

    @Test
    public void testdeleteRefillReminderByReminderGUID(){
        mRefillReminderDbUtils.deleteRefillReminderByReminderGUID(TestConfigurationProperties.MOCK_REFILL_REMINDER_GUID);
    }

    @Test
    public void testgetOverdueRefillRemindersForCards(){
        mRefillReminderDbUtils.getOverdueRefillRemindersForCards();
    }

    @Test
    public void testClearTable(){
        mRefillReminderDbUtils.clearDBTable(TestConfigurationProperties.MOCK_REFILL_REMINDER_TABLE_NAME);
    }



}
