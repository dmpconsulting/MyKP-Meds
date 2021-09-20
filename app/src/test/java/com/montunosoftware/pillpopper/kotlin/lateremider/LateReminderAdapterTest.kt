package com.montunosoftware.pillpopper.kotlin.lateremider

import android.content.Context
import android.widget.RelativeLayout
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class LateReminderAdapterTest {
    private var lateReminderAdapter: LateReminderAdapter? = null
    private var context: Context? = null
    private var lateReminderViewModel: LateReminderViewModel? = null
    private var lateRemindersActivity: LateRemindersActivity? = null
    private var singleUserHashMap = LinkedHashMap<Long, MutableList<Drug>>()
    private var groupActionDrugTimes: MutableList<Long> = ArrayList()
    private var reminderTime: Long? = null
    private var drugList: List<Drug>? = null
    private var userID: String? = null


    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockData()
        lateRemindersActivity = Robolectric.buildActivity(LateRemindersActivity::class.java).create().start().resume().get()
        lateReminderViewModel = ViewModelProvider(lateRemindersActivity!!).get(LateReminderViewModel::class.java)
        lateReminderAdapter = LateReminderAdapter(singleUserHashMap, groupActionDrugTimes, lateReminderViewModel!!)

    }

    private fun mockData() {
        UniqueDeviceId.init(context)
        reminderTime = (TestConfigurationProperties.MOCK_LATE_REMINDER_TIME).toLong()
        userID = TestConfigurationProperties.MOCK_USER_ID
        drugList = ArrayList()
        val drug = Drug()
        drug.userID = "abc"
        drug.isTempHeadr = true
        drug.isNoDrugsFound = false
        drug.isHeader = false
        drug.name = "xyz"
        drug.scheduledTime = PillpopperTime(1212)
        drug.setmAction(1)
        (drugList as ArrayList).add(0, drug)
        singleUserHashMap[reminderTime!!] = drugList as ArrayList<Drug>
        groupActionDrugTimes.add(0, reminderTime!!)
        val dummyMap = LinkedHashMap<Long, List<Drug>>()
        dummyMap[reminderTime!!] = FrontController.getInstance(context).getAllDrugs(context)
        val passedDrugMap = LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>>()
        passedDrugMap[userID!!] = dummyMap
        PillpopperRunTime.getInstance().setmPassedRemindersMap(dummyMap)
        PillpopperRunTime.getInstance().passedReminderersHashMapByUserId = passedDrugMap

    }

    @Test
    fun adapterShouldNotNull() {
        assertNotNull(lateReminderAdapter)
    }

    @Test
    fun testGetGroup() {
        assertEquals(groupActionDrugTimes.size, lateReminderAdapter?.getGroup(0))
    }

    @Test
    fun testGetChildView() {
        assertNotNull(lateReminderAdapter?.getChildView(0, 0, true, RelativeLayout(context), RelativeLayout(context)))
    }

    @Test
    fun testGetChildId() {
        val position = 0
        assertEquals(position.toLong(), lateReminderAdapter?.getChildId(0, 0))
    }

    @Test
    fun testGetGroupCount() {
        assertEquals(groupActionDrugTimes.size, lateReminderAdapter?.groupCount)
    }

    @Test
    fun testGetGroupId() {
        val position = 0
        assertEquals(position.toLong(), lateReminderAdapter?.getGroupId(0))
    }
}