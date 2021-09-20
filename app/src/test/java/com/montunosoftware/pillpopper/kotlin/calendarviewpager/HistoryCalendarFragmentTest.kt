package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import android.view.View
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.SupportFragmentTestUtil
import com.montunosoftware.pillpopper.android.HomeContainerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import kotlinx.android.synthetic.main.history_base_calendar_layout.view.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class HistoryCalendarFragmentTest {
    private var historyCalendarFragment: HistoryCalendarFragment? = null
    private var view: View? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        historyCalendarFragment = HistoryCalendarFragment()
        SupportFragmentTestUtil.startFragment(historyCalendarFragment, HomeContainerActivity::class.java)
        view = historyCalendarFragment!!.view
    }

    @Test
    fun fragmentShouldNotNull() {
        assertNotNull(historyCalendarFragment)
    }

    @Test
    fun testOnClickSeeMoreToggleButton() {
        val seeMoreToggleButton = view!!.see_more_toggle_btn
        seeMoreToggleButton.performClick()
        assertEquals("See Less", seeMoreToggleButton.text)
    }

    @After
    fun tearDown() {
        TestUtil.resetDatabase()
        historyCalendarFragment?.onDestroyView()
        historyCalendarFragment?.onDestroy()
        historyCalendarFragment?.onStop()
    }
}