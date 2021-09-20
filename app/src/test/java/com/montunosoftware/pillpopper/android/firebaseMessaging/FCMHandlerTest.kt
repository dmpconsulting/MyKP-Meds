/*

package com.montunosoftware.pillpopper.android.firebaseMessaging

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(application = PillpopperApplicationShadow::class, sdk = [TestConfigurationProperties.BUILD_SDK_VERSION])
class FCMHandlerTest {


    private var context: Context = ApplicationProvider.getApplicationContext()


    // Initialize the test
    init {
        // initializing Firebase Messaging
        if (AppConstants.IS_FIREBASE_CLOUD_MESSAGING_ENABLED) {
            //initializing the Firebase App
            FirebaseApp.initializeApp(context)
            //initializing our app
            FCMHandler.Builder().init(context).build()
        }
    }


    @Test
    fun `test if the cloud messaging disabled`() {
        AppConstants.IS_FIREBASE_CLOUD_MESSAGING_ENABLED = true
        FirebaseApp.initializeApp(context)
        FCMHandler.Builder().init(context).build()
        Assert.assertEquals(true, AppConstants.IS_FIREBASE_CLOUD_MESSAGING_ENABLED)

    }

   */
/* @Test
    fun `test if we received the FCM Token`() {
        FirebaseInstallations.getInstance().getToken(true)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    val token = task.result!!.token
                    Assert.assertEquals(token.isNotEmpty(), token)
                    PillpopperLog.say(token)
                })
    } *//*

}
*/
