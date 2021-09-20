package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.SupportFragmentTestUtil
import com.montunosoftware.pillpopper.android.StateListenerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import org.junit.After
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
class HistoryOverlayDialogFragmentTest {
    private var historyOverlayDialogFragment: HistoryOverlayDialogFragment? = null
    private var historyEventsList: ArrayList<HistoryEvent>? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        historyEventsList = TestUtil.historyEventMockData() as ArrayList<HistoryEvent>?
        historyOverlayDialogFragment = HistoryOverlayDialogFragment(historyEventsList,"")
        SupportFragmentTestUtil.startFragment(historyOverlayDialogFragment, StateListenerActivity::class.java)
    }

    @Test
    fun fragmentShouldNotNull() {
        assertNotNull(historyOverlayDialogFragment)
    }

    @After
    fun tearDown() {
        TestUtil.resetDatabase()
        historyOverlayDialogFragment?.onPause()
    }


}