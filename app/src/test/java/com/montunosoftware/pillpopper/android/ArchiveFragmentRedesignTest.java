package com.montunosoftware.pillpopper.android;

import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandler;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;
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
import static org.junit.Assert.assertNotNull;

/**
 * Created by M1028309 on 5/25/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class,shadows = {RxRefillDBHandlerShadow.class, StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class,SecurePreferencesShadow.class, PillpopperAppContextShadow.class, PillpopperRunTimeShadow.class})
public class ArchiveFragmentRedesignTest {

    private ArchiveFragmentRedesign archiveFragment;
    private View view;

    @Before
    public  void setup(){
        TestUtil.setupTestEnvironment();
        TestUtil.setRegistrationResponse("/RegisterResponse-Existing.json");
        archiveFragment = new ArchiveFragmentRedesign();
        SupportFragmentTestUtil.startFragment(archiveFragment,StateListenerActivity.class);
        view = archiveFragment.getView();
    }

    @After
    public void tearDown(){
        TestUtil.resetDatabase();
        archiveFragment.onStop();
    }


    @Test
    public void ArchiveFragmentViewShouldNotBeNull(){
        assertNotNull(view);
    }

    @Test
    public void checkNoArchivesMessageDisplay(){
        removeArchiveForTesting();
        assertThat(view.findViewById(R.id.archive_list_no_archived_medications_textview).getVisibility()== View.VISIBLE);
    }

    private void removeArchiveForTesting() {
        DatabaseHandler db=DatabaseHandler.getInstance(ApplicationProvider.getApplicationContext());
        db.executeRawQuery("UPDATE PILL SET USERID='abc' where USERID='074f6ea2b1481051fbaea5cf6d819108ab'",null);
    }


}
