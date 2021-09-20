package com.montunosoftware.pillpopper.android

import android.content.Intent
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId
import com.montunosoftware.pillpopper.android.view.EditScheduleRunTimeData
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.BulkSchedule
import com.montunosoftware.pillpopper.model.Drug
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])

class MedicationOverlayConfirmationScheduleActivityTest {
    private var medicationOverlayConfirmationScheduleActivity: MedicationOverlayConfirmationScheduleActivity? = null
    private var controller: ActivityController<MedicationOverlayConfirmationScheduleActivity>? = null
    private var intent: Intent? = null
    private var bulkSchedule: BulkSchedule? = null
    private var drug: Drug? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
    }

    private fun mockData(scheduleFrequency: String, dayPeriod: Int) {
        bulkSchedule = BulkSchedule()
        bulkSchedule!!.scheduledFrequency = scheduleFrequency
        bulkSchedule!!.dayPeriod = dayPeriod.toString()
        bulkSchedule!!.scheduledStartDate = "1081157732"
        bulkSchedule!!.scheduledEndDate = "1081157732"
        bulkSchedule!!.scheduledTimeList = listOf(900, 500)
        UniqueDeviceId.init(ApplicationProvider.getApplicationContext())

        val editScheduleRunTimeData = EditScheduleRunTimeData()
        editScheduleRunTimeData.isEditMedicationClicked = true
        editScheduleRunTimeData.isReminderAdded = true
        editScheduleRunTimeData.endDate = "12032021"
        editScheduleRunTimeData.meditationDuration = "1"
        editScheduleRunTimeData.selectedDays = "1234567"
        drug = Drug()
        drug!!.name = "Med Name3"
        drug!!.userID = ""
        drug!!.memberFirstName = "abc"
        editScheduleRunTimeData.selectedDrug = drug
        RunTimeData.getInstance().scheduleData = editScheduleRunTimeData

        intent = Intent()
        intent!!.putExtra("BULK_SCHEDULE", bulkSchedule)
        controller = Robolectric.buildActivity(MedicationOverlayConfirmationScheduleActivity::class.java ,intent)
        medicationOverlayConfirmationScheduleActivity = controller!!.create().start().resume().get()
    }

    @Test
    fun testShouldNotNull() {
        mockData("D", 1)
        assertNotNull(medicationOverlayConfirmationScheduleActivity)
    }

    @Test
    fun testExpandImage() {
        mockData("M", 30)
        medicationOverlayConfirmationScheduleActivity?.expandImage(drug!!)
        val shadowActivity: ShadowActivity = Shadows.shadowOf(medicationOverlayConfirmationScheduleActivity)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(EnlargeImageActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun testOnClickOverlayCancelButton() {
        mockData("W", 7)
        val cancelOverlayButton = medicationOverlayConfirmationScheduleActivity!!.findViewById<Button>(R.id.bt_overlay_cancel)
        cancelOverlayButton.performClick()
        assertTrue(medicationOverlayConfirmationScheduleActivity!!.isFinishing)
    }

    @Test
    fun testOnClickOverlayScheduleButton() {
        mockData("W", 21)
        val scheduleOverlayButton = medicationOverlayConfirmationScheduleActivity!!.findViewById<Button>(R.id.bt_overlay_schedule)
        scheduleOverlayButton.performClick()
        assertTrue(medicationOverlayConfirmationScheduleActivity!!.isFinishing)
    }

    @After
    fun tearDown() {
        TestUtil.resetDatabase()
        controller!!.pause().stop().destroy()
        RunTimeData.getInstance().scheduleData = null
    }
}