package com.montunosoftware.pillpopper.android.util;

import android.content.Context;
import android.os.Bundle;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.home.RefillReminderOverdueCard;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RefillReminderDBHandlerShadow;
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

/**
 * Created by M1023050 on 02-Jul-19.
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, RefillReminderDBHandlerShadow.class})
public class FireBaseAnalyticsTrackerTest {
    private ActivityController<HomeContainerActivity> activityActivityController;
    private Context context;
    private FireBaseAnalyticsTracker mFireBaseAnalyticsTracker;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        RunTimeData.getInstance().setHomeCardsShown(true);
        activityActivityController = Robolectric.buildActivity(HomeContainerActivity.class);
        context = ApplicationProvider.getApplicationContext();
        mFireBaseAnalyticsTracker = FireBaseAnalyticsTracker.getInstance();
    }

    @Test
    public void testLogEventWithBundle(){
        mFireBaseAnalyticsTracker.logEvent(context, TestConfigurationProperties.MOCK_EVENT, new Bundle());
    }

    @Test
    public void testLogEvent(){
        mFireBaseAnalyticsTracker.logEvent(context,
                TestConfigurationProperties.MOCK_EVENT_NAME,
                TestConfigurationProperties.MOCK_PARAM_NAME,
                TestConfigurationProperties.MOCK_PARAM_VALUE);
    }

    @Test
    public void testScreenEvent(){
        mFireBaseAnalyticsTracker.logScreenEvent(context,
                TestConfigurationProperties.MOCK_SCREEN_NAME);
    }

    @Test
    public void testlogEventWithoutParams(){
        mFireBaseAnalyticsTracker.logEventWithoutParams(context,
                TestConfigurationProperties.MOCK_EVENT_NAME);
    }

}
