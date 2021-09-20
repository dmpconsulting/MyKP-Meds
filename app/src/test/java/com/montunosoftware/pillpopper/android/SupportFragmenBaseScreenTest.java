package com.montunosoftware.pillpopper.android;

import android.widget.LinearLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class SupportFragmenBaseScreenTest {
   private SupportFragmenBaseScreen supportFragmenBaseScreen = new SupportFragmenBaseScreen();

    @Before
    public void setup(){
        TestUtil.setupTestEnvironment();
        SupportFragmentTestUtil.startFragment(supportFragmenBaseScreen,HomeContainerActivity.class);

    }
    @Test
    public void fragmentShouldNotNull() {
        assertThat(supportFragmenBaseScreen != null);
    }

    @Test
    public void fragmentViewShouldNotBeNull() {
        assertThat(supportFragmenBaseScreen.getView()).isNotNull();
    }

    @Test
    public void shouldLaunchNewActivity()
    {
      LinearLayout mLayoutAppSupport = supportFragmenBaseScreen.getView().findViewById(R.id.app_support_row);
      LinearLayout  mLayoutAppointments = supportFragmenBaseScreen.getView().findViewById(R.id.appointments_row);
      LinearLayout  mLayoutMedication = supportFragmenBaseScreen.getView().findViewById(R.id.medications_row);
      assertThat(mLayoutAppointments.performClick()).isTrue();
      assertThat(mLayoutAppSupport.performClick()).isTrue();
      assertThat(mLayoutMedication.performClick()).isTrue();

    }

}
