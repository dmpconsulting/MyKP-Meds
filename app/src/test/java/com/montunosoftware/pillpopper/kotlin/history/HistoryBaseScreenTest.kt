package com.montunosoftware.pillpopper.kotlin.history

import android.content.Intent
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.SupportFragmentTestUtil
import com.montunosoftware.pillpopper.android.HomeContainerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.AppConstants.PERMISSION_CAMERA
import org.kp.tpmg.mykpmeds.activation.AppConstants.PERMISSION_WRITE_EXTERNAL_STORAGE
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class HistoryBaseScreenTest {
    private var historyBaseScreen: HistoryBaseScreen? = null
    private var shadowActivity: ShadowActivity? = null
    private var homeContainerActivity: HomeContainerActivity? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity::class.java).create().start().resume().get()
        historyBaseScreen = HistoryBaseScreen()
        SupportFragmentTestUtil.startFragment(historyBaseScreen, HomeContainerActivity::class.java)
        shadowActivity = Shadows.shadowOf(homeContainerActivity)
    }

    @Test
    fun fragmentShouldNotNull() {
        assertNotNull(historyBaseScreen)
    }

    @After
    fun tearDown() {
        TestUtil.resetDatabase()
        historyBaseScreen?.onPause()
        historyBaseScreen?.onDestroyView()
        historyBaseScreen?.onDestroy()
    }


    @Test
    fun testOnItemClick() {
        historyBaseScreen?.onItemClick(TestUtil.historyEventMockData()[0])
        val intent: Intent? = shadowActivity?.nextStartedActivity
        val shadowIntent = Shadows.shadowOf(intent)
        assertEquals(HistoryDetailActivity::class.java, shadowIntent.intentClass)

    }

    @Test
    fun testOnSettingClicked() {
        historyBaseScreen?.onSettingClicked()
    }


    @Test
    fun testPermissionsGranted() {
        val grantResults = intArrayOf(0)
        val permissions = arrayOf("abc")
        historyBaseScreen!!.onRequestPermissionsResult(PERMISSION_CAMERA, permissions, grantResults)
    }

    @Test
    fun testPermissionsDenied() {
        val grantResults = intArrayOf()
        val permissions = arrayOf("abc")
        historyBaseScreen!!.onRequestPermissionsResult(PERMISSION_WRITE_EXTERNAL_STORAGE, permissions, grantResults)
    }


}