package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.kotlin.lateremider.LateReminderDetail
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.model.PillpopperTime
import kotlinx.android.synthetic.main.threedot_bottom_action_sheet.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class ActionBottomDialogFragmentTest {

    private var actionBottomDialogFragment: ActionBottomDialogFragment? = null
    private val pillId: String = TestConfigurationProperties.MOCK_PILL_ID
    private var lateReminderDetail: LateReminderDetail? = null
    private var controller: ActivityController<LateReminderDetail>? = null
    private var intent: Intent? = null
    private var drugList: List<Drug>? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        intent = Intent()
        intent!!.putExtra("isLastGroup", true)
        mockData()
        controller = Robolectric.buildActivity(LateReminderDetail::class.java, intent)
        lateReminderDetail = controller!!.create().start().resume().get()
    }

    private fun mockData() {
        drugList = ArrayList()
        val drug = Drug()
        drug.userID = "abc"
        drug.isTempHeadr = true
        drug.isNoDrugsFound = false
        drug.isHeader = true
        drug.scheduledTime = PillpopperTime(1212)
        drug.setmAction(0)
        (drugList as ArrayList).add(0, drug)
        PillpopperRunTime.getInstance().setmOverdueDrugs(drugList)
        PillpopperRunTime.getInstance().proxyDrugs = drugList
        PillpopperRunTime.getInstance().headerTime = 1221
        AppConstants.setByPassLogin(true)
    }

    private fun startBottomDialogFragment(reminder: String) {
        val args = Bundle()
        args.putString("mPillId", pillId)
        args.putBoolean(reminder, true)
        actionBottomDialogFragment = ActionBottomDialogFragment.newInstance()
        actionBottomDialogFragment!!.arguments = args
        lateReminderDetail!!.supportFragmentManager.beginTransaction().add(0, actionBottomDialogFragment!!).commit()
    }

    @Test
    fun fragmentShouldNotNull() {
        startBottomDialogFragment("isLateReminder")
        assertNotNull(actionBottomDialogFragment)
    }

    @Test
    fun testOnClickSkippedLayout() {
        startBottomDialogFragment("isLateCurrentReminder")
        actionBottomDialogFragment!!.skipped_layout.performClick()
        assertEquals(View.GONE, actionBottomDialogFragment!!.iv_icon_skipped_24dp.visibility)
        assertEquals(View.VISIBLE, actionBottomDialogFragment!!.iv_icon_skipped_24dp_blue.visibility)
    }

    @Test
    fun testOnClickTakenEarlierLayout() {
        startBottomDialogFragment("isLateReminder")
        actionBottomDialogFragment!!.taken_earlier_layout.performClick()
        assertEquals(View.GONE, actionBottomDialogFragment!!.iv_ic_checkmark.visibility)
        assertEquals(View.VISIBLE, actionBottomDialogFragment!!.iv_ic_checkmark_blue.visibility)
    }

    @Test
    fun testOnClickCancelLayout() {
        startBottomDialogFragment("")
        actionBottomDialogFragment!!.cancel_layout.performClick()
        assertEquals(View.GONE, actionBottomDialogFragment!!.iv_ic_baseline_remove_circle_outline_24.visibility)
        assertEquals(View.VISIBLE, actionBottomDialogFragment!!.iv_ic_baseline_remove_circle_outline_24_blue.visibility)
    }

    @Test
    fun testOnClickReminderLaterLayout() {
        startBottomDialogFragment("isLateCurrentReminder")
        actionBottomDialogFragment!!.reminder_later_layout.performClick()
        assertEquals(View.GONE, actionBottomDialogFragment!!.iv_ic_icon_remind_me_later.visibility)
        assertEquals(View.VISIBLE, actionBottomDialogFragment!!.iv_ic_icon_remind_me_later_blue.visibility)
    }
}