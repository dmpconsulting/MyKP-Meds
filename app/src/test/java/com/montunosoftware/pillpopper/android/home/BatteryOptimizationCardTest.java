package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
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
import static junit.framework.Assert.assertFalse;

/**
 * Created by M1023050 on 02-Jul-19.
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class})
public class BatteryOptimizationCardTest{

    private BatteryOptimizerInfoCard batteryOptimizerInfoCard;
    private ActivityController<HomeContainerActivity> activityActivityController;
    private Context context;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        RunTimeData.getInstance().setHomeCardsShown(true);
        activityActivityController = Robolectric.buildActivity(HomeContainerActivity.class);
        batteryOptimizerInfoCard = new BatteryOptimizerInfoCard(activityActivityController.get());
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testIsSamSungDevice(){
        Log.v("testIsSamSungDevice", "" + context.getString(R.string.samsung));
        assertFalse(context.getString(R.string.samsung), batteryOptimizerInfoCard.isSamSungDevice());
    }

    @Test
    public void testIsGoogleDevice(){
        Log.v("testIsGoogleDevice", "" + context.getString(R.string.google));
        System.out.println("testIsGoogleDevice : " + context.getString(R.string.google) + "And Is Google Device : " + batteryOptimizerInfoCard.isGoogleDevice());
        assertFalse(context.getString(R.string.google),batteryOptimizerInfoCard.isGoogleDevice());
    }

    @Test
    public void testIsUnKnownDevice(){
        assertFalse("unknown",batteryOptimizerInfoCard.isGoogleDevice());
    }

    @Test
    public void testOnSettingsButtonClick(){
        batteryOptimizerInfoCard.onSettingsButtonClick(new View(context));
    }


    @Test
    public void testOnDismissButtonClick(){
        batteryOptimizerInfoCard.onDismissButtonClick(new View(context));
    }

   /* @Test
    public void testInitDetailView(){
        batteryOptimizerInfoCard.initDetailView();
    }*/

}
