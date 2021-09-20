package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryListItemBinding
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.SupportFragmentTestUtil
import com.montunosoftware.pillpopper.android.HomeContainerActivity
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class HistoryListAdapterTest {
    private var historyListAdapter: HistoryListAdapter? = null
    private var historyEventsList: List<HistoryEvent>? = null
    private var historySpanSelected: String? = "1 Month"
    private val doseHistoryFromSettingsForFooter: String? = "You’re viewing 1 month of medication history.\\nChange timeframe in settings."
    private var historyBaseScreen: HistoryBaseScreen? = null
    private var context: Context? = null

    @Before
    fun setUp() {
        historyBaseScreen = HistoryBaseScreen()
        SupportFragmentTestUtil.startFragment(historyBaseScreen, HomeContainerActivity::class.java)
        context = ApplicationProvider.getApplicationContext()
        historyEventsList = TestUtil.historyEventMockData()
        historyListAdapter = HistoryListAdapter(context!!, historyEventsList!!, doseHistoryFromSettingsForFooter, historyBaseScreen!!, historySpanSelected)
        historyListAdapter?.onCreateViewHolder(RelativeLayout(context), 0)
    }


    @Test
    fun adapterShouldNotNull() {
        assertNotNull(historyListAdapter)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(4, historyListAdapter?.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        assertNotNull(historyListAdapter?.onCreateViewHolder(RelativeLayout(context), 0))
    }

    @Test
    fun testGetItemViewType() {
        assertEquals(0, historyListAdapter?.getItemViewType(0))
    }

    @Test
    fun testOnBindViewHolder() {
        val binding: HistoryListItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.history_list_item, null, false)
        historyListAdapter?.onBindViewHolder(historyListAdapter!!.ItemViewHolder(binding.root), 0)
        assertEquals(View.VISIBLE, binding.historySettingsLayout.visibility)
        historyListAdapter?.onBindViewHolder(historyListAdapter!!.ItemViewHolder(binding.root), 1)
        assertEquals(View.VISIBLE, binding.historyDisclaimer.visibility)
        historyListAdapter?.onBindViewHolder(historyListAdapter!!.ItemViewHolder(binding.root), 2)
    }

}