package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.SupportFragmentTestUtil
import com.montunosoftware.pillpopper.android.HomeContainerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.kotlin.calendarviewValpager.HistoryCalendarWeekFragmentNew
import com.montunosoftware.pillpopper.model.PillpopperDay
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class HistoryCalendarWeekFragmentNewTest {
    private var historyCalendarWeekFragmentNew: HistoryCalendarWeekFragmentNew? = null
    private var homeContainerActivity: HomeContainerActivity? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity::class.java).create().start().resume().get()
        historyCalendarWeekFragmentNew = HistoryCalendarWeekFragmentNew()
        SupportFragmentTestUtil.startFragment(historyCalendarWeekFragmentNew, HomeContainerActivity::class.java)
    }

    @Test
    fun fragmentShouldNotNull() {
        assertNotNull(historyCalendarWeekFragmentNew)
    }


    @Test
    fun testGetContext() {
        assertNotNull(historyCalendarWeekFragmentNew?.getmContext())
    }

    @Test
    fun testInstanceNotNull() {
        assertNotNull(HistoryCalendarWeekFragmentNew.newInstance(PillpopperDay.today(), homeContainerActivity, ArrayList(), 0))
    }

    @After
    fun tearDown() {
        TestUtil.resetDatabase()
        historyCalendarWeekFragmentNew?.onDestroyView()
        historyCalendarWeekFragmentNew?.onDestroy()
        historyCalendarWeekFragmentNew?.onStop()
    }

}