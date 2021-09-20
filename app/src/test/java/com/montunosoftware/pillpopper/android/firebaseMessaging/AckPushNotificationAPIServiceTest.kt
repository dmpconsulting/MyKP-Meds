package com.montunosoftware.pillpopper.android.firebaseMessaging

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.model.firebaseMessaging.PushNotificationAckRequestObj
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION],
        application = PillpopperApplicationShadow::class,
        shadows = [DatabaseHandlerShadow::class,
            SecurePreferencesShadow::class,
            PillpopperAppContextShadow::class])
class AckPushNotificationAPIServiceTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        TestUtil.setupTestEnvironment()
        context = ApplicationProvider.getApplicationContext()
        Robolectric.getBackgroundThreadScheduler().pause()
    }


    @Test
    fun `testing the async task implementation`() {

        val ackRequestObj = PushNotificationAckRequestObj().apply {
            ackSource = "ackSource"
            ackStatus = "true"
            ackStatusReason = "ackStatusReason"
            ackTS = 32138589949
            action = "action"
            ack_device_id = "ack_device_id"
            tzName = "tzName"
            tzSecs = "tzSecs"
        }

        val ackPushNotificationAPIService = AckPushNotificationAPIService(context, "https://tpmg", ackRequestObj)
        ackPushNotificationAPIService.execute()
        Robolectric.flushBackgroundThreadScheduler()
        Assert.assertEquals(null,ackPushNotificationAPIService.get())

    }


}