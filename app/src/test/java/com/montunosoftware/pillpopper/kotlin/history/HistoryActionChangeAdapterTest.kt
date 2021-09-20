package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryBulkActionChangeListBinding
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
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


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class HistoryActionChangeAdapterTest {
    private var historyActionChangeAdapter: HistoryActionChangeAdapter? = null
    private var historyEventsList: ArrayList<HistoryEvent>? = null
    private var activity: PillpopperActivity? = null
    private var context: Context? = null


    @Before
    fun setUp() {
        historyEventsList = TestUtil.historyEventMockData() as ArrayList<HistoryEvent>?
        activity = Robolectric.buildActivity(PillpopperActivity::class.java).create().get()
        context = activity!!.applicationContext
        historyActionChangeAdapter = HistoryActionChangeAdapter(historyEventsList!!, activity!!)
        historyActionChangeAdapter?.onCreateViewHolder(RelativeLayout(context), 0)
    }

    @Test
    fun adapterShouldNotNull() {
        assertNotNull(historyActionChangeAdapter)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(2, historyActionChangeAdapter?.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        assertNotNull(historyActionChangeAdapter?.onCreateViewHolder(RelativeLayout(context), 0))
    }

    @Test
    fun testOnBindViewHolder() {
        val binding: HistoryBulkActionChangeListBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.history_bulk_action_change_list, null, false)
        historyActionChangeAdapter?.onBindViewHolder(historyActionChangeAdapter!!.ViewHolder(binding), 0)
    }
}