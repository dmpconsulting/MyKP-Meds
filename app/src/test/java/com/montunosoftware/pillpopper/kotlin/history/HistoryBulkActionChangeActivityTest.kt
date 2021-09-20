package com.montunosoftware.pillpopper.kotlin.history

import android.content.Intent
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class HistoryBulkActionChangeActivityTest {
    private var historyEventsList: ArrayList<HistoryEvent>? = null
    private var intent: Intent? = null
    private var historyBulkActionChangeActivity: HistoryBulkActionChangeActivity? = null
    private var controller: ActivityController<HistoryBulkActionChangeActivity>? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        historyEventsList = TestUtil.historyEventMockData() as ArrayList<HistoryEvent>?
        intent = Intent()
        intent!!.putExtra("historyEventsList", historyEventsList)
        controller = Robolectric.buildActivity(HistoryBulkActionChangeActivity::class.java, intent)
        historyBulkActionChangeActivity = controller!!.create().start().resume().get()
    }

    @Test
    fun testShouldNotNull() {
        assertNotNull(historyBulkActionChangeActivity)
    }

    @Test
    fun testOnSaveClicked() {
        historyBulkActionChangeActivity?.onSaveClicked()
    }

    @Test
    fun testOnBackPressed() {
        historyBulkActionChangeActivity?.onBackPressed()
        val alert = ShadowAlertDialog.getLatestAlertDialog()
        if (alert != null) assertTrue(alert.isShowing)
    }


    @After
    fun tearDown() {
        TestUtil.resetDatabase()
        controller!!.pause().stop().destroy()
    }
}