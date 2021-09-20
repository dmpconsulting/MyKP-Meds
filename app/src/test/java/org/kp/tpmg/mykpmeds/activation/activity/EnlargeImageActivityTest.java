package org.kp.tpmg.mykpmeds.activation.activity;

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

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

import android.widget.Button;
import android.widget.ImageView;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class EnlargeImageActivityTest {
    private EnlargeImageActivity enlargeImageActivity;
    private ActivityController<EnlargeImageActivity> controller;
    @Before
    public  void setup() {
        TestUtil.setupTestEnvironment();
        controller = Robolectric.buildActivity(EnlargeImageActivity.class);
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        enlargeImageActivity =  controller.create().start().resume().visible().get();
    }

    @After
    public void tearDown(){
        TestUtil.resetDatabase();
        controller.destroy();
    }

    @Test
    public void checkActivityNotNull(){
        assertNotNull(enlargeImageActivity);
    }

    @Test
    public void testOnClick() {
        Button changeButton = enlargeImageActivity.findViewById(R.id.change_btn);
        changeButton.performClick();

        ImageView cancel = enlargeImageActivity.findViewById(R.id.close);
        cancel.performClick();
        assertTrue(enlargeImageActivity.isFinishing());
    }

    @Test
    public void testOnBackPressed() {
        enlargeImageActivity.onBackPressed();
    }

}
