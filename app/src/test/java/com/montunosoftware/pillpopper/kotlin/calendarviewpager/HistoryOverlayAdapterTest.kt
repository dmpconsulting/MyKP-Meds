package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryOverlayDialogeAdapterBinding
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class HistoryOverlayAdapterTest {
    private var historyOverlayAdapter: HistoryOverlayAdapter? = null
    private var context: Context? = null
    private var historyEventsList: ArrayList<HistoryEvent>? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        historyEventsList = TestUtil.historyEventMockData() as ArrayList<HistoryEvent>?
        historyOverlayAdapter = HistoryOverlayAdapter(historyEventsList!!, context!!,"")
        historyOverlayAdapter?.onCreateViewHolder(RelativeLayout(context), 1)
    }

    @Test
    fun adapterShouldNotNull() {
        assertNotNull(historyOverlayAdapter)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(2, historyOverlayAdapter?.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        assertNotNull(historyOverlayAdapter?.onCreateViewHolder(RelativeLayout(context), 0))
    }

    @Test
    fun testGetItemId() {
        val position = 0
        assertEquals(position.toLong(), historyOverlayAdapter?.getItemId(0))
    }

    @Test
    fun testGetItemViewType() {
        val position = 0
        assertEquals(position, historyOverlayAdapter?.getItemViewType(0))
    }

    @Test
    fun testOnBindViewHolder() {
        val binding: HistoryOverlayDialogeAdapterBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.history_overlay_dialoge_adapter, null, false)
        historyOverlayAdapter?.onBindViewHolder(historyOverlayAdapter!!.ViewHolder(binding), 0)
        assertEquals(View.GONE, binding.tvPostponedTime.visibility)
        historyOverlayAdapter?.onBindViewHolder(historyOverlayAdapter!!.ViewHolder(binding), 1)
    }

}