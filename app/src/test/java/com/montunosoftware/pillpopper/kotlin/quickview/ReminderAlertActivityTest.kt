package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class ReminderAlertActivityTest {
    private var reminderAlertActivity: ReminderAlertActivity? = null
    private var context: Context? = null
    private var intent: Intent? = null


    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
    }

    private fun startReminderActivity(launchMode :String) {
        intent = Intent()
        intent!!.putExtra(AppConstants.LAUNCH_MODE, launchMode)
        reminderAlertActivity = Robolectric.buildActivity(ReminderAlertActivity::class.java, intent).create().resume().get()
        context = reminderAlertActivity!!.application
    }

    @Test
    fun testShouldNotNull() {
        startReminderActivity("Skipped")
        assertNotNull(reminderAlertActivity)
    }

    @Test
    fun testCancelButtonVisibility() {
        startReminderActivity("Saved")
        val cancelButton: Button = reminderAlertActivity!!.findViewById(R.id.cancelButton)
        assertEquals(View.GONE, cancelButton.visibility)
    }

    @Test
    fun testOkClicked() {
        startReminderActivity("passedReminders")
        reminderAlertActivity?.okClicked()
        reminderAlertActivity?.isFinishing?.let { assertTrue(it) }
    }

    @Test
    fun testCancelClicked() {
        startReminderActivity("TakenEarlierTime")
        reminderAlertActivity?.cancelClicked()
        reminderAlertActivity?.isFinishing?.let { assertTrue(it) }
    }

    @After
    fun tearDown() {
        TestUtil.resetDatabase()
    }
}