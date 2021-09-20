package com.montunosoftware.pillpopper.android.home;

import android.content.Intent;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;

import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;

import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import androidx.test.core.app.ApplicationProvider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by M1032896 on 4/23/2018.
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperRunTimeShadow.class})
public class KPHCDiscontinuedCardTest {
    KPHCDiscontinueCard discontinueCard;
    List<DiscontinuedDrug> discontinuedDrugList;
    private HomeCardDetailActivity homeCardDetailActivity;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        discontinuedDrugList = FrontController.getInstance(ApplicationProvider.getApplicationContext()).getDiscontinuedMedications();
        discontinueCard = new KPHCDiscontinueCard(discontinuedDrugList, ApplicationProvider.getApplicationContext());
        Intent intent = new Intent();
        intent.putExtra("card", discontinueCard);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().visible().get();
    }

    @Test
    public void checkDiscontinuedMedsSize(){
        if(discontinuedDrugList.size()>0){
            assertNotNull(discontinuedDrugList);
        }
    }

    @Test
    public void userNamesShouldNotBeNull(){
        assertNotNull(discontinueCard.getUserNames());
    }


    @Test
    public void okButtonClick(){
        if(discontinuedDrugList.size()>0){
            homeCardDetailActivity.findViewById(R.id.archive_details_delete_medication_button).performClick();
        }
    }

   /* @Test
    public void  userNameShouldNotBeNull(){
        assertNotNull(reminderCard.getUserNames());
    }


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
    }*/

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
    }
}
