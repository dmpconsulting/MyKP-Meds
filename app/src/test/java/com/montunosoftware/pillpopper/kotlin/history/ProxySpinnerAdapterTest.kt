package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.widget.RelativeLayout
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.HomeContainerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.model.User
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class ProxySpinnerAdapterTest {
    private var proxySpinnerAdapter: ProxySpinnerAdapter? = null
    private var userList: List<User>? = null
    private var context: Context? = null
    private var homeContainerActivity: HomeContainerActivity? = null

    @Before
    fun setUp() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity::class.java).create().start().resume().get()
        context = homeContainerActivity?.androidContext
        userList = ArrayList()
        (userList as ArrayList).add(0, TestUtil.prepareUserObject())
        proxySpinnerAdapter = ProxySpinnerAdapter(homeContainerActivity!!, userList!!)

    }

    @Test
    fun testGetItem() {
        assertNotNull(proxySpinnerAdapter?.getItem(0))
    }

    @Test
    fun testGetItemId() {
        val position = 0
        assertEquals(position.toLong(), proxySpinnerAdapter?.getItemId(0))
    }

    @Test
    fun testGetCount() {
        assertEquals(1, proxySpinnerAdapter?.count)
    }

    @Test
    fun testGetView() {
        assertNotNull(proxySpinnerAdapter?.getView(0, null, RelativeLayout(context)))
    }

}