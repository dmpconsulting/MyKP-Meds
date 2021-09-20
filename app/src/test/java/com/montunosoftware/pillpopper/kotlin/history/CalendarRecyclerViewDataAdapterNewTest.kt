package com.montunosoftware.pillpopper.kotlin.history


import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.CalendarHorizontalListBinding
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.HomeContainerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.controller.FrontControllerShadow
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.kotlin.history.horizontalRecyclerHistory.HorizontalSectionDataModel
import com.montunosoftware.pillpopper.kotlin.history.horizontalRecyclerHistory.adapter.CalendarRecyclerViewDataAdapterNew
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class, FrontControllerShadow::class])
class CalendarRecyclerViewDataAdapterNewTest {
    private var calendarRecyclerViewDataAdapterNew: CalendarRecyclerViewDataAdapterNew? = null
    private var context: Context? = null
    private var historyItem: List<HorizontalSectionDataModel>? = null
    private var historyViewModel: HistoryViewModel? = null
    private var historyEventsList: List<HistoryEvent>? = null
    private var homeContainerActivity: HomeContainerActivity? = null


    @Before
    fun setUp() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity::class.java).create().start().resume().get()
        context = homeContainerActivity?.androidContext
        historyViewModel = ViewModelProvider(homeContainerActivity!!).get(HistoryViewModel::class.java)
        mockData()
        calendarRecyclerViewDataAdapterNew = CalendarRecyclerViewDataAdapterNew(historyItem!!, historyViewModel!!, true, homeContainerActivity!!.supportFragmentManager,"")
        calendarRecyclerViewDataAdapterNew?.onCreateViewHolder(RelativeLayout(context), 0)
    }

    private fun mockData() {
        historyItem = ArrayList()
        historyEventsList = TestUtil.historyEventMockData()
        val horizontalSectionDataModel = HorizontalSectionDataModel()
        horizontalSectionDataModel.headerTime = "1234567"
        horizontalSectionDataModel.horizontalHistoryEventList = (historyEventsList as ArrayList<HistoryEvent>)
        (historyItem as ArrayList).add(0, horizontalSectionDataModel)
    }

    @Test
    fun adapterShouldNotNull() {
        assertNotNull(calendarRecyclerViewDataAdapterNew)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(1, calendarRecyclerViewDataAdapterNew?.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        assertNotNull(calendarRecyclerViewDataAdapterNew?.onCreateViewHolder(RelativeLayout(context), 0))
    }

    @Test
    fun testGetItemViewType() {
        assertEquals(0, calendarRecyclerViewDataAdapterNew?.getItemViewType(0))
    }

    @Test
    fun testOnBindViewHolder() {
        val binding: CalendarHorizontalListBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.calendar_horizontal_list, null, false)
        calendarRecyclerViewDataAdapterNew?.onBindViewHolder(calendarRecyclerViewDataAdapterNew!!.ViewHolder(binding), 0)
    }

    @Test
    fun testOnEventImageClick() {
        calendarRecyclerViewDataAdapterNew!!.onEventImageClick(2, 0)
        assertFalse(RunTimeData.getInstance().isHistoryOverlayShown)
    }
}