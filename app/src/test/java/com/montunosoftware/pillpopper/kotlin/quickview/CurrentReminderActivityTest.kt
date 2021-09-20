package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowIntent


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class CurrentReminderActivityTest {

    private lateinit var controller: ActivityController<CurrentReminderActivityNew>
    private lateinit var currentReminderActivity: CurrentReminderActivityNew
    private lateinit var shadowActivity: ShadowActivity
    private var context: Context? = null
    private var reminderTime: Long? = null
    private var intent: Intent? = null
    private var shadowIntent: ShadowIntent? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
    }

    @Test
    fun activityNotNull() {
        mockData()
        assertNotNull(currentReminderActivity)
    }

    @Test
    fun testOnTakenAllClicked() {
        mockData()
        currentReminderActivity.onTakenAllClicked()
        intent = shadowActivity.nextStartedActivity
        shadowIntent = Shadows.shadowOf(intent)
        assertEquals(ReminderAlertActivity::class.java, shadowIntent!!.intentClass)
    }

    @Test
    fun testOnTakeLaterClicked() {
        mockData()
        currentReminderActivity.onTakeLaterClicked()
    }

    @Test
    fun testOnSkippedAllClicked() {
        mockData()
        currentReminderActivity.onSkippedAllClicked()
        intent = shadowActivity.nextStartedActivity
        shadowIntent = Shadows.shadowOf(intent);
        assertEquals(ReminderAlertActivity::class.java, shadowIntent!!.intentClass)
    }

    @Test
    fun testOnTakenEarlierClicked() {
        mockData()
        currentReminderActivity.onTakenEarlierClicked()
    }

    @After
    fun tearDown() {
        TestUtil.resetDatabase()
        controller.pause().stop().destroy()
    }

    private fun mockData() {
        AppConstants.setByPassLogin(true)
        controller = Robolectric.buildActivity(CurrentReminderActivityNew::class.java)
        currentReminderActivity = controller.create().start().resume().visible().get()
        shadowActivity = Shadows.shadowOf(currentReminderActivity)
        context = ApplicationProvider.getApplicationContext()
        PillpopperRunTime.getInstance().headerTime = (TestConfigurationProperties.MOCK_LATE_REMINDER_TIME).toLong()
        reminderTime = (TestConfigurationProperties.MOCK_LATE_REMINDER_TIME).toLong()

        val dummyMap = LinkedHashMap<Long, List<Drug>>()
        val drug = Drug()
        drug.name = "Med Name1"
        drug.userID = ""
        val drug2 = Drug()
        drug2.name = "Med Name2"
        drug2.userID = ""
        val drug3 = Drug()
        drug3.name = "Med Name3"
        drug3.userID = ""
        val drugList: MutableList<Drug> = ArrayList()
        drugList.add(drug)
        drugList.add(drug2)
        drugList.add(drug3)
        dummyMap[reminderTime!!] = drugList
        PillpopperRunTime.getInstance().setmCurrentRemindersMap(dummyMap)
        PillpopperRunTime.getInstance().passedReminderersHashMapByUserId = null
    }

}