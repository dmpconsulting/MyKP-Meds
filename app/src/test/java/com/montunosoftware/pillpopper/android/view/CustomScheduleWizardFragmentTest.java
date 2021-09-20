package com.montunosoftware.pillpopper.android.view;

import android.view.View;
import android.widget.ImageView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class CustomScheduleWizardFragmentTest {

    private CustomScheduleWizardFragment customScheduleWizardFragment;
    private View view;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        customScheduleWizardFragment = new CustomScheduleWizardFragment();
        SupportFragmentTestUtil.startFragment(customScheduleWizardFragment, HomeContainerActivity.class);
        view = customScheduleWizardFragment.getView();
    }

    @Test
    public void fragmentShouldNotNull() {
        assertNotNull(customScheduleWizardFragment);
    }

    @Test
    public void viewNotNull() {
        assertNotNull(view);
    }

    @Test
    public void testClearDaysText() {
        customScheduleWizardFragment.clearDaysText();
        ImageView imageView = view.findViewById(R.id.btn_clear);
        assertEquals(View.GONE, imageView.getVisibility());
    }

    @After
    public void resetDatabase() {
        TestUtil.resetDatabase();
    }
}
