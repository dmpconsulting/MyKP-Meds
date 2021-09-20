package com.montunosoftware.pillpopper.android.firebaseMessaging;

import android.content.Intent;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class FCMNotificationReceiverActivityTest {


    private FCMNotificationReceiverActivity fCMNotificationReceiverActivity;


    @Before
    @LooperMode(LooperMode.Mode.PAUSED)
    public void setup() {
        Intent intent = new Intent();
        intent.putExtra("title", "payload.alert.title");
        intent.putExtra("body", "payload.alert.body");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fCMNotificationReceiverActivity = Robolectric
                .buildActivity(FCMNotificationReceiverActivity.class, intent)
                .create()
                .start()
                .resume()
                .visible()
                .get();
        shadowOf(getMainLooper()).idle();
    }


    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void checkActivityNull() {
        Assert.assertNotNull(fCMNotificationReceiverActivity);
        shadowOf(getMainLooper()).idle();
    }
}