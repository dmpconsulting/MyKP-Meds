package com.montunosoftware.pillpopper.kotlin.lateremider

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.kotlin.quickview.ReminderAlertActivity
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity
import java.util.*
import kotlin.collections.LinkedHashMap

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class LateRemindersActivityTest {
    private var lateReminderActivity: LateRemindersActivity? = null
    private var controller: ActivityController<LateRemindersActivity>? = null
    private var context: Context? = null
    private var reminderTime: Long? = null
    private var userID: String? = null
    private var shadowActivity: ShadowActivity? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
    }

    @Test
    fun activityShouldNotNull() {
        mockData(false)
        assertNotNull(lateReminderActivity)
    }

    @Test
    fun testOnTakenAllClicked() {
        mockData(false)
        lateReminderActivity?.onTakenAllClicked()
        val shadowActivity: ShadowActivity = Shadows.shadowOf(lateReminderActivity)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(ReminderAlertActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun testOnSkipAllClicked() {
        mockData(true)
        lateReminderActivity?.onSkippedAllClicked()
        val intent: Intent? = shadowActivity?.nextStartedActivity
        val shadowIntent = Shadows.shadowOf(intent)
        assertEquals(ReminderAlertActivity::class.java, shadowIntent.intentClass)
    }

    private fun mockData(flag: Boolean) {
        context = ApplicationProvider.getApplicationContext()
        var intent = Intent(context, LateRemindersActivity::class.java)
        intent.putExtra("launchingMode", "LaunchPendingPassedReminders")
        controller = Robolectric.buildActivity(LateRemindersActivity::class.java, intent)
        lateReminderActivity = controller?.create()?.start()?.resume()?.visible()?.get()
        shadowActivity = Shadows.shadowOf(lateReminderActivity)
        userID = TestConfigurationProperties.MOCK_USER_ID
        reminderTime = (TestConfigurationProperties.MOCK_LATE_REMINDER_TIME).toLong()
        val drug = Drug()
        drug.userID = ""
        drug.name = "Eldoper"
        drug.setmAction(PillpopperConstants.TAKEN)
        val drug2 = Drug()
        drug2.userID = "1234"
        drug2.name = "Eldoper"
        drug2.setmAction(PillpopperConstants.TAKEN)
        val dummyMap = LinkedHashMap<Long, List<Drug>>()
        dummyMap[reminderTime!!] = listOf(drug, drug2)
        PillpopperRunTime.getInstance().setmPassedRemindersMap(dummyMap)
        var userData: LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> = LinkedHashMap()
        userData["1234"] = dummyMap
        PillpopperRunTime.getInstance().passedReminderersHashMapByUserId = userData
        if (flag) {
            PillpopperRunTime.getInstance().removalTime = (TestConfigurationProperties.MOCK_LATE_REMINDER_TIME).toLong()
        }
    }
}