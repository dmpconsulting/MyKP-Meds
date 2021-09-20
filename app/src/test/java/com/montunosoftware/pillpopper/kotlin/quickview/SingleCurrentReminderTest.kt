package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
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
class SingleCurrentReminderTest {
    private var singleCurrentReminder: SingleCurrentReminder? = null
    private var context: Context? = null
    private var controller: ActivityController<SingleCurrentReminder>? = null
    private var intent: Intent? = null
    private var drugList: List<Drug>? = null
    private lateinit var shadowActivity: ShadowActivity
    private lateinit var viewModelprovider: CurrentReminderActivityViewModel

    @Before
    fun setUp() {
        intent = Intent()
        intent!!.putExtra("isLastGroup", true)
        mockData()
        controller = Robolectric.buildActivity(SingleCurrentReminder::class.java, intent)
        singleCurrentReminder = controller!!.create().start().resume().get()
        shadowActivity = Shadows.shadowOf(singleCurrentReminder)
        context = singleCurrentReminder!!.application
        viewModelprovider = ViewModelProvider(singleCurrentReminder!!).get(CurrentReminderActivityViewModel::class.java)
    }

    private fun mockData() {
        drugList = ArrayList()
        val drug = Drug()
        drug.userID = "abc"
        drug.isTempHeadr = true
        drug.isNoDrugsFound = false
        drug.isHeader = true
        drug.scheduledTime = PillpopperTime(1212)
        drug.setmAction(0)

        (drugList as ArrayList).add(0, drug)
        PillpopperRunTime.getInstance().setmOverdueDrugs(drugList)
        PillpopperRunTime.getInstance().proxyDrugs = drugList
        PillpopperRunTime.getInstance().headerTime = 1221
        AppConstants.setByPassLogin(true)
    }

    @Test
    fun testShouldNotNull() {
        Assert.assertNotNull(singleCurrentReminder)
    }

    @Test
    fun testOnSkippedAllClicked() {
        singleCurrentReminder?.onSkippedAllClicked()
        shadowActivity = Shadows.shadowOf(singleCurrentReminder)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        Assert.assertEquals(ReminderAlertActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun testOnSkip() {
        singleCurrentReminder?.onSkip()
        shadowActivity = Shadows.shadowOf(singleCurrentReminder)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        Assert.assertEquals(ReminderAlertActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun testImageClick(){
        viewModelprovider.onImgClicked(Drug())
        val startedIntent = shadowActivity.nextStartedActivity
        val shadowIntent = Shadows.shadowOf(startedIntent)
        Assert.assertEquals(EnlargeImageActivity::class.java, shadowIntent.intentClass)
    }

    @After
    fun tearDown() {
        controller!!.pause().stop().destroy()
    }
}