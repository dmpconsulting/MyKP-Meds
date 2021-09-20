package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Context
import android.widget.RelativeLayout
import androidx.lifecycle.ViewModelProvider
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.junit.Assert.assertEquals
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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class CurrentReminderAdapterTest {

    private lateinit var currentReminderActivity: CurrentReminderActivityNew
    private var context: Context? = null
    private var currentReminderViewModel: CurrentReminderActivityViewModel? = null
    private var singleTimeHashMap = LinkedHashMap<String, MutableList<Drug>>()
    private var drugList: List<Drug>? = null
    private var currentReminderAdapter: CurrentReminderAdapter? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        currentReminderActivity = Robolectric.buildActivity(CurrentReminderActivityNew::class.java).create().start().resume().visible().get()
        context = currentReminderActivity.applicationContext
        currentReminderViewModel = ViewModelProvider(currentReminderActivity).get(CurrentReminderActivityViewModel::class.java)
        mockData()
        currentReminderAdapter = CurrentReminderAdapter(context!!, singleTimeHashMap, currentReminderViewModel!!)
    }

    private fun mockData() {
        drugList = ArrayList()
        val drug = Drug()
        drug.userID = "abc"
        drug.isTempHeadr = true
        drug.isNoDrugsFound = false
        drug.isHeader = false
        drug.name = "(xyz)"
        drug.scheduledTime = PillpopperTime(1212)
        drug.setmAction(1)
        (drugList as ArrayList).add(0, drug)
        singleTimeHashMap["dummyName"] = drugList as ArrayList<Drug>

    }

    @Test
    fun adapterShouldNotNull() {
        assertNotNull(currentReminderAdapter)
    }

    @Test
    fun testGetChildView() {
        assertNotNull(currentReminderAdapter?.getChildView(0, 0, true, RelativeLayout(context), RelativeLayout(context)))
    }

    @Test
    fun testGetChildId() {
        val position = 0
        assertEquals(position.toLong(), currentReminderAdapter?.getChildId(0, 0))
    }

    @Test
    fun testGetGroup() {
        assertEquals("dummyName", currentReminderAdapter?.getGroup(0))
    }


}