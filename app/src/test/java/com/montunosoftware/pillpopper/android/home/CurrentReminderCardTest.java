package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import androidx.test.core.app.ApplicationProvider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by M1032896 on 4/23/2018.
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class})
public class CurrentReminderCardTest {
    private Long reminderTime;
    CurrentReminderCard reminderCard;
    private Context context;
    private ActivityController<HomeContainerActivity> activityActivityController;
    private View mTestActivityView;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        ReflectionHelpers.setStaticField(UniqueDeviceId.class, "_cachedId", UUID.randomUUID().toString());
        reminderTime = Long.valueOf(TestConfigurationProperties.MOCK_REMINDER_TIME);
        LinkedHashMap<Long,List<Drug>> dummyMap = new LinkedHashMap<>();
        dummyMap.put(reminderTime, FrontController.getInstance(ApplicationProvider.getApplicationContext()).getAllDrugs(ApplicationProvider.getApplicationContext()));
        PillpopperRunTime.getInstance().setCurrentRemindersByUserIdForCard(dummyMap);
        activityActivityController = Robolectric.buildActivity(HomeContainerActivity.class);
        reminderCard = new CurrentReminderCard(activityActivityController.get(),reminderTime,1);
        context = ApplicationProvider.getApplicationContext().getApplicationContext();
        mTestActivityView = LayoutInflater.from(activityActivityController.get()).inflate(R.layout.current_reminder_home_card, null);
    }

    @Test
    public void currentReminderShouldNotBeNull(){
        assertNotNull(PillpopperRunTime.getInstance().getCurrentRemindersByUserIdForCard());
    }

    /*@Test
    public void  userNameShouldNotBeNull(){
        assertNotNull(reminderCard.getUserNames());
    }*/


    @Test
    public void  reminderTimeShouldNotBeNull(){
        assertNotNull(reminderCard.getReminderTime());
    }

    @Test
    public void  reminderDateShouldNotBeNull(){
        assertNotNull(reminderCard.getReminderDate());
    }

    @Test
    public void checkReminderTakenButton(){
        List<Drug> drugs = FrontController.getInstance(ApplicationProvider.getApplicationContext()).getAllDrugs(ApplicationProvider.getApplicationContext());
        if(drugs.size()>0){
            assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.taken_all),reminderCard.getTakenText());
        }else {
            assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.taken),reminderCard.getTakenText());
        }
    }

    @Test
    public void checkReminderSkipButton(){
        List<Drug> drugs = FrontController.getInstance(ApplicationProvider.getApplicationContext()).getAllDrugs(ApplicationProvider.getApplicationContext());
        if(drugs.size()>0){
            assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.skipped_all),reminderCard.getSkipText());
        }else {
            assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.skipped),reminderCard.getSkipText());
        }
    }

    @Test
    public void testgetCurrentReminderDrugsCount(){
        assertNotNull(reminderCard.getCurrentReminderDrugsCount());
    }

    @Test
    public void testTaken(){
        View view = mTestActivityView.findViewById(R.id.current_reminder_taken);
        view.setTag("contract");
        reminderCard.onTaken(view);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
    }
}
