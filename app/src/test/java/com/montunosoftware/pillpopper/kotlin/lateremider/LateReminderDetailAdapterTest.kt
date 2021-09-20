package com.montunosoftware.pillpopper.kotlin.lateremider

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.LateReminderDetailListBinding
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class LateReminderDetailAdapterTest {
    private var context: Context? = null
    private var activity: PillpopperActivity? = null
    private var lateReminderDetailAdapter: LateReminderDetailAdapter? = null
    private var drugList: List<Drug>? = null
    private var viewModelProvider: LateReminderDetailViewModel? = null
    private var lateReminderDetail: LateReminderDetail? = null
    private var intent: Intent? = null

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(PillpopperActivity::class.java).create().get()
        context = ApplicationProvider.getApplicationContext()
        intent = Intent()
        intent!!.putExtra("isLastGroup", true)
        mockData()
        lateReminderDetail = Robolectric.buildActivity(LateReminderDetail::class.java, intent).create().start().resume().get()
        viewModelProvider = ViewModelProvider(lateReminderDetail!!).get(LateReminderDetailViewModel::class.java)
        lateReminderDetailAdapter = LateReminderDetailAdapter(drugList!!, viewModelProvider!!, activity!!)
        lateReminderDetailAdapter?.onCreateViewHolder(RelativeLayout(context), 0)
    }

    private fun mockData() {
        drugList = ArrayList()
        val drug = Drug()
        drug.userID = "abc"
        drug.isTempHeadr = true
        drug.isNoDrugsFound = false
        drug.isHeader = true
        drug.setmAction(PillpopperConstants.SKIPPED)
        drug.scheduledTime = PillpopperTime(1212)
        drug.setmAction(1)
        (drugList as ArrayList).add(0, drug)
        PillpopperRunTime.getInstance().setmOverdueDrugs(drugList)
        PillpopperRunTime.getInstance().proxyDrugs = drugList
        PillpopperRunTime.getInstance().headerTime = 1221
        AppConstants.setByPassLogin(true)
    }

    @Test
    fun adapterShouldNotNull() {
        assertNotNull(lateReminderDetailAdapter)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(1, lateReminderDetailAdapter?.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        assertNotNull(lateReminderDetailAdapter?.onCreateViewHolder(RelativeLayout(context), 0))
    }

    @Test
    fun testOnBindViewHolder() {
        val binding: LateReminderDetailListBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.late_reminder_detail_list, null, false)
        lateReminderDetailAdapter?.onBindViewHolder(lateReminderDetailAdapter!!.ViewHolder(binding), 0)
        assertEquals(View.VISIBLE, lateReminderDetailAdapter?.binding?.view?.visibility)
        assertEquals(View.GONE, lateReminderDetailAdapter?.binding?.seeNotesButton?.visibility)
    }

    @Test
    fun testGetItemViewType() {
        assertEquals(0, lateReminderDetailAdapter?.getItemViewType(0))
    }

    @Test
    fun testGetItemId() {
        val id = 0
        assertNotEquals(id, lateReminderDetailAdapter?.getItemId(0))
        assertEquals(id.toLong(), lateReminderDetailAdapter?.getItemId(0))
    }


}