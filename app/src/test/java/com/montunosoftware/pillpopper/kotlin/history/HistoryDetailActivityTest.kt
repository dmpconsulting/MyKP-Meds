package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import kotlinx.android.synthetic.main.history_detail.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class HistoryDetailActivityTest {
    private var historyDetailActivity: HistoryDetailActivity? = null
    private var context: Context? = null
    private var controller: ActivityController<HistoryDetailActivity>? = null
    private var intent: Intent? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
    }

    private fun mockData(operationStatus: String) {
        intent = Intent()
        val historyEvent = HistoryEvent()
        historyEvent.drugId = ""
        historyEvent.dosage = "abc"
        historyEvent.notes = "Take Medicine"
        historyEvent.headerTime = "1531215000"
        historyEvent.historyEventGuid = "aa537479dd3ac4d113418a3a5e4102ffc"
        historyEvent.operationStatus = operationStatus
        val bundle = Bundle()
        bundle.putSerializable("historyEvent", historyEvent)
        intent!!.putExtras(bundle)
        controller = Robolectric.buildActivity(HistoryDetailActivity::class.java, intent)
        historyDetailActivity = controller!!.create().start().resume().get()
        context = historyDetailActivity!!.applicationContext
    }

    @Test
    fun testShouldNotNull() {
        mockData("")
        assertNotNull(historyDetailActivity)
    }


    @Test
    fun testOnClickDrugImage() {
        mockData("skipPill")
        historyDetailActivity!!.drugImage.performClick()
        val shadowActivity: ShadowActivity = Shadows.shadowOf(historyDetailActivity)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(EnlargeImageActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun onClickSaveMenu() {
        mockData("takePill")
        val addMenuItem: MenuItem = RoboMenuItem(R.id.save_menu_item)
        assertNotNull(addMenuItem)
        historyDetailActivity!!.onOptionsItemSelected(addMenuItem)
        assertSame(true, addMenuItem.isVisible)
    }

    @Test
    fun onClickHomeMenu() {
        mockData("takePill")
        val homeMenu: MenuItem = RoboMenuItem(android.R.id.home)
        historyDetailActivity!!.onOptionsItemSelected(homeMenu)
        assertSame(true, homeMenu.isVisible)
    }

    @After
    fun tearDown() {
        TestUtil.resetDatabase()
        controller!!.pause().stop().destroy()
    }
}