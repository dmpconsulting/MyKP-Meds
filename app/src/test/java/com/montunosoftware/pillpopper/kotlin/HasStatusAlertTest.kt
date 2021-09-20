package com.montunosoftware.pillpopper.kotlin

import android.content.Context
import android.content.Intent
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.HomeContainerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class HasStatusAlertTest {
    private var hasStatusAlert: HasStatusAlert? = null
    private var shadowActivity: ShadowActivity? = null
    private var controller: ActivityController<HasStatusAlert>? = null
    private var context: Context? = null

    @Before
    fun setUp() {

    }

    @Test
    fun activityShouldNotNull() {
        prepareData(true)
        assertNotNull(hasStatusAlert)
    }

    @Test
    fun testOnOkClicked() {
        prepareData(false)
        hasStatusAlert?.onOkClicked()
        val startedIntent: Intent? = shadowActivity?.nextStartedActivity
        val shadowIntent = Shadows.shadowOf(startedIntent)
        assertEquals(HomeContainerActivity::class.java, shadowIntent.intentClass)
    }

    @Test
    fun testOnOkClickedForForceSignIn() {
        prepareData(true)
        hasStatusAlert?.onOkClicked()
        assertFalse(PillpopperRunTime.getInstance().isReminderNeedToShow)
    }

    @After
    fun tearDown(){
        controller?.pause()?.stop()?.destroy()
    }

    private fun prepareData(flag:Boolean){
        val intent = Intent()
        intent.putExtra("title", "")
        intent.putExtra("message", "")
        intent.putExtra("isForceSignIn", flag)
        intent.putExtra("proxyStatusCodeValue", "N")
        intent.putExtra("primaryUserId", "")
        controller = Robolectric.buildActivity(HasStatusAlert::class.java, intent);
        hasStatusAlert = controller?.create()?.start()?.resume()?.visible()?.get()
        shadowActivity = Shadows.shadowOf(hasStatusAlert)
        context = hasStatusAlert!!.application
    }

}