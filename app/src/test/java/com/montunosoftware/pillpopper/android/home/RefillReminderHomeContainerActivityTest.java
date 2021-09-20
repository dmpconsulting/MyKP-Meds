package com.montunosoftware.pillpopper.android.home;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;

import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.views.CreateOrUpdateRefillReminderFragment;
import com.montunosoftware.pillpopper.android.refillreminder.views.RefillRemindersHomeContainerActivity;
import com.montunosoftware.pillpopper.android.refillreminder.views.RefillRemindersListFragment;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RefillReminderDBHandlerShadow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.List;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by M1024581 on 7/18/2018.
 */

@RunWith(RobolectricTestRunner.class)
@Config( sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, RefillReminderDBHandlerShadow.class, SecurePreferencesShadow.class})
public class RefillReminderHomeContainerActivityTest {

    private RefillRemindersHomeContainerActivity refillReminderActivity;
    private ActivityController<RefillRemindersHomeContainerActivity> controller;

    @Before
    public void setUp(){
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), RefillRemindersHomeContainerActivity.class);
        controller = Robolectric.buildActivity(RefillRemindersHomeContainerActivity.class,intent);
        refillReminderActivity =  controller.create().start().resume().visible().get();
    }

    @Test
    public void testRefillRemindersCount(){
        assertNotNull(RefillReminderController.getInstance(ApplicationProvider.getApplicationContext()).getRefillRemindersCount());
    }

    @Test
    public void testLaunchCreateRefillOrShowList(){
        int refillCount = RefillReminderController.getInstance(ApplicationProvider.getApplicationContext()).getRefillRemindersCount();
        List<Fragment> fragmentList =  refillReminderActivity.getSupportFragmentManager().getFragments();
        if(refillCount > 0){
            Assert.assertTrue(fragmentList.get(0) instanceof RefillRemindersListFragment);
        } else if(refillCount == 0){
            Assert.assertTrue(fragmentList.get(0) instanceof CreateOrUpdateRefillReminderFragment);
        }
    }
}
