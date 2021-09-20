package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.android.HomeContainerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.UIUtils
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils
import com.montunosoftware.pillpopper.kotlin.calendarviewValpager.HistoryCalendarWeekFragmentNew
import com.montunosoftware.pillpopper.kotlin.history.HistoryViewModel
import com.montunosoftware.pillpopper.kotlin.history.horizontalRecyclerHistory.HorizontalSectionDataModel
import com.montunosoftware.pillpopper.kotlin.history.horizontalRecyclerHistory.adapter.CalendarRecyclerViewDataAdapterNew
import com.montunosoftware.pillpopper.model.PillpopperDay
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.HistoryCalendarUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


/**
 * Created by D113186 on 09,December,2020
 *
 * This is  the will be responsible for displaying the history calendar [HistoryCalendarFragment] and Calendar pill status
 * which embeds [HistoryCalendarViewPager] for the Calendar
 */
class HistoryCalendarFragment : Fragment() {

    private var isCalendarRefreshed: Boolean = false
    private var needToRefreshFromBackground: Boolean = false
    private var isAdapterSet: Boolean = false
    private var historyEventsList: List<HistoryEvent>? = mutableListOf()

    //viewpager
    private lateinit var mHistoryCalendarViewPager: ViewPager
    private lateinit var mOnPageChangeListener: OnPageChangeListener
    private lateinit var currentDay: PillpopperDay
    private lateinit var startDate: PillpopperDay
    private var historyEvents = mutableListOf<HistoryEvent>()
    private var futureEvents = mutableListOf<HistoryEvent>()
    private var calHistoryEvent = arrayListOf<HistoryEvent>()
    private val sortedHistoryEvent = arrayListOf<HistoryEvent>()
    private lateinit var mContext: Context
    private lateinit var weekStartDay: PillpopperDay
    private lateinit var possibleWeekEndDay: PillpopperDay
    private var mFrontController: FrontController? = null
    private val mBundleUserName = "userName"
    private val mBundleUserId = "userId"
    private var mSelectedUserId: String = ""
    private var mSelectedUserName: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var seeMoreToggleButton: Button
    private var mFontRegular: Typeface? = null
    private var mFontMedium: Typeface? = null
    private var currentPosition = 0
    private var seeMoreToggleButtonState = true
    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private val executors: Executor by lazy { Executors.newSingleThreadExecutor() }
    private lateinit var historyViewModel: HistoryViewModel
    private var needToSortByFrequency = true

    //adapter
    private lateinit var calendarRecyclerViewDataAdapterNew: CalendarRecyclerViewDataAdapterNew
    private lateinit var mHistoryCalendarViewPagerAdapter: HistoryCalendarViewPagerAdapter
    private var mCurrentViewPagerPosition = 0

    // list of calendar
    private lateinit var historyCalendarAdapterList: MutableList<HistoryCalendarWeekFragmentNew>
    private lateinit var dateTextView: TextView
    private lateinit var noHistoryLayout: LinearLayout
    private lateinit var addReminderCalendarLayout: LinearLayout
    private lateinit var addReminderCalendarButton: Button
    private lateinit var noHistoryHeader: TextView
    private lateinit var noHistoryText: TextView

    private var hasFutureEvents: Boolean = false

    init {
        initArguments()
    }

    private fun recyclerViewSetup(view: View) {
        recyclerView = view.findViewById(R.id.horizontal_history_recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.recycledViewPool.clear()
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
    }

    /**
     * Initializing the Views
     */
    private fun initViews(view: View) {
        mHistoryCalendarViewPager = view.findViewById(R.id.history_calendarViewPager)
        dateTextView = view.findViewById(R.id.Calender_date_textView)
        seeMoreToggleButton = view.findViewById(R.id.see_more_toggle_btn) as Button
        noHistoryLayout = view.findViewById(R.id.no_history_calendar) as LinearLayout
        addReminderCalendarLayout = view.findViewById(R.id.add_reminder_calendar_layout) as LinearLayout
        addReminderCalendarButton = view.findViewById(R.id.add_reminder_calendar_button) as Button
        noHistoryHeader = view.findViewById(R.id.no_history_header)
        noHistoryText = view.findViewById(R.id.no_history_txt)
    }

    private fun setUpTypeFace() {
        dateTextView.typeface = mFontRegular
        noHistoryHeader.typeface = mFontRegular
        noHistoryText.typeface = mFontRegular
        addReminderCalendarButton.typeface = mFontMedium
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentPosition = 0
        if (!RunTimeData.getInstance().isHistoryMedChanged ){
            seeMoreToggleButtonState = true
            RunTimeData.getInstance().seeMoreEnabled = !seeMoreToggleButtonState
        }else{
            seeMoreToggleButtonState = !RunTimeData.getInstance().seeMoreEnabled
        }
        //setting typeface
        setUpTypeFace()
        initCalendarViewPagerAdapterList()
        //setting up  Horizontal RecyclerView
        recyclerViewSetup(view)

        initArguments()

        historyCalendarViewOnPageSelected()
    }

    interface FetchingFutureCalendarDataCompletedListener {
        fun onReturnedResult(result: ArrayList<HistoryEvent>)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
    }

    /**
     * This will be responsible for the update of the swipe of the calendar fragment pager
     */
    private fun historyCalendarViewOnPageSelected() {
        // Hiding all the History calendar View for the first time
        historyEvents = mutableListOf()
        mHistoryCalendarViewPagerAdapter = HistoryCalendarViewPagerAdapter(childFragmentManager, historyCalendarAdapterList)
        mHistoryCalendarViewPager.adapter = mHistoryCalendarViewPagerAdapter
        //viewPager Swipe Listener
        mOnPageChangeListener = object : OnPageChangeListener {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // currentPosition = position
            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
              if(!RunTimeData.getInstance().isCalendarPosChanged) {
                    RunTimeData.getInstance().weekSelectedPosition = currentPosition
                }

                // handle [handleSeeMoreStates()] to handle the state
                handleSeeMoreStates()
                //Current Position
                getFirstWeekFutureData(currentPosition)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        }
        mHistoryCalendarViewPager.addOnPageChangeListener(mOnPageChangeListener)
        // trying to get the current week
        if(!RunTimeData.getInstance().isOverlayItemClicked) {
            mHistoryCalendarViewPager.currentItem = historyCalendarAdapterList.size - 1
            mOnPageChangeListener.onPageSelected(mHistoryCalendarViewPager.currentItem)
        }



        // State of SeeMore button
        handleSeeMoreStates()
        // add reminder clicked
        addReminderClickEvent()
//        getFirstWeekFutureData(currentPosition)
    }

    /**
     * Future data updated listener interface
     */
    private val postTaskListener = object : FetchingFutureCalendarDataCompletedListener {
        override fun onReturnedResult(result: ArrayList<HistoryEvent>) {
            if (null != activity) {
                activity!!.runOnUiThread {
                    futureEvents.clear()
                    futureEvents.addAll(result)
                    hasFutureEvents = futureEvents.isNotEmpty()
                    getOneWeekHistoryEvent(weekStartDay, possibleWeekEndDay)
                }
            }
        }
    }


    /**
     *
     */
    private fun getFirstWeekFutureData(position: Int) {
            if (position == (mHistoryCalendarViewPager.adapter?.count?.minus(1))) {
                // get all the upcoming history data from the DB
                runFutureDataInTPool(mContext, postTaskListener)
            }
    }


    private fun runFutureDataInTPool(context: Context, listener: FetchingFutureCalendarDataCompletedListener?) {
        val upComingWeekEndDay = RunTimeData.getInstance().weekEndDay
        val upComingWeekStartDay = RunTimeData.getInstance().weekStartDay
        val futureEvents = ArrayList<HistoryEvent>()
        try {
            executors.execute {
                compileUpcomingDays(upComingWeekStartDay, upComingWeekEndDay).forEach { focusDay ->
                   val historyEventsData = DatabaseUtils.getInstance(context).getHistoryCalendarFutureDays(focusDay, mSelectedUserId)
                   futureEvents.addAll(historyEventsData)
                }
                mainHandler.post {
                    listener?.onReturnedResult(futureEvents)
                }
            }
        } catch (e: Exception) {
            PillpopperLog.exception(e.message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // will kill the handler object from  lingering as ghost and hunt the class and suffer from a crash 👻
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun compileUpcomingDays(startDay: PillpopperDay, endDay: PillpopperDay): ArrayList<PillpopperDay> {
        var focusDay = startDay
        val outBoundEndDay = endDay.addDays(1)
        val listOfUpcomingDays = ArrayList<PillpopperDay>()
        do {
            listOfUpcomingDays.add(focusDay)
            focusDay = focusDay.addDays(1)
        } while (focusDay.before(outBoundEndDay))
        return listOfUpcomingDays
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        LoggerUtils.info("States...$savedInstanceState")
    }

    /**
     * Different states depending on the click of the user
     */
    private fun handleSeeMoreStates() {
        if (null != activity) {
            val seeMore: String = mContext.getString(R.string.see_more)
            val seeLess: String = mContext.getString(R.string.see_less)

            //default - see more
            if (seeMoreToggleButtonState) {
                seeMoreToggleButton.text = seeMore
                seeMoreToggleButton.contentDescription = mContext.getString(R.string.content_description_history_calendar_see_more)
            } else {
                seeMoreToggleButton.text = seeLess
                seeMoreToggleButton.contentDescription = mContext.getString(R.string.content_description_history_calendar_see_less)
            }

            calculateHistoryData(currentPosition)
            getOneWeekHistoryEvent(weekStartDay, possibleWeekEndDay)

            seeMoreToggleButton.setOnClickListener {


                seeMoreToggleButtonState = !seeMoreToggleButtonState
                RunTimeData.getInstance().seeMoreEnabled = !seeMoreToggleButtonState


                if (seeMoreToggleButtonState) {
                    seeMoreToggleButton.text = seeMore
                    seeMoreToggleButton.contentDescription = mContext.getString(R.string.content_description_history_calendar_see_more)
                    historyViewModel.getSeeMoreOrLessClickAction(true)
                } else {
                    seeMoreToggleButton.text = seeLess
                    seeMoreToggleButton.contentDescription = mContext.getString(R.string.content_description_history_calendar_see_less)
                    historyViewModel.getSeeMoreOrLessClickAction(true)
                }
                //  seeMoreToggleButton.text = (if (seeMoreToggleButtonState) seeMore else seeLess)
                calculateHistoryData(currentPosition)
                getOneWeekHistoryEvent(weekStartDay, possibleWeekEndDay)
            }
        }
    }


    /**
     * By finding on which position on the Calendar we are we then calculate the History data by start Day [weekStartDay]and
     * End Day [possibleWeekEndDay] of the week
     */
    private fun calculateHistoryData(position: Int) {
        mCurrentViewPagerPosition = position
        historyCalendarAdapterList[mCurrentViewPagerPosition].setSelectedDayInWeekWithCallback(PillpopperDay.today())
        val historyCalendarFragment = historyCalendarAdapterList[mCurrentViewPagerPosition]
        // Week Start Day
        if (historyCalendarFragment.getWeekStartDay() != null)
            weekStartDay = historyCalendarFragment.getWeekStartDay()!!
        // Possible Week EndDay
        possibleWeekEndDay = historyCalendarFragment.getWeekStartDay()!!.addDays(6)
        // identifying the month from week start days
        if(weekStartDay!=null){
            getCurrentMonthAndYearAsHeader(weekStartDay, possibleWeekEndDay)
        }
        if (possibleWeekEndDay.after(PillpopperDay.today())) {
            val calendar = Calendar.getInstance()
            calendar.set(historyCalendarFragment.getWeekStartDay()!!.year,
                    historyCalendarFragment.getWeekStartDay()!!.month,
                    historyCalendarFragment.getWeekStartDay()!!.day)
            val difference = kotlin.math.abs(Calendar.getInstance().timeInMillis - calendar.timeInMillis)
            possibleWeekEndDay = historyCalendarFragment.getWeekStartDay()!!.addDays(TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS))
        }
        RunTimeData.getInstance().setCalenderStartDate(historyCalendarFragment.getWeekStartDay())
        RunTimeData.getInstance().setCalenderEndDate(possibleWeekEndDay)
        recyclerView.removeAllViewsInLayout()
        // prepare the start and end day of the first week
        val weekStartDayForUpComing = possibleWeekEndDay
        RunTimeData.getInstance().weekStartDay = weekStartDayForUpComing
        RunTimeData.getInstance().weekEndDay = historyCalendarFragment.getWeekStartDay()!!.addDays(6)
        // if the current page is not current week, load the history events, else the history events will be loaded after the "FutureAsync" Task
        if (currentPosition != (mHistoryCalendarViewPager.adapter?.count?.minus(1))) {
            futureEvents.clear()
            getOneWeekHistoryEvent(weekStartDay, possibleWeekEndDay)
        }
    }

    /**
     * Will show the current month and year of the week depending on which day it falls
     * for example start day [weekStartDay]  may fall on February and end day [weekEndDay] may fall on March
     */
    private fun getCurrentMonthAndYearAsHeader(weekStartDay: PillpopperDay, weekEndDay: PillpopperDay) {
        val endMonthsYear = weekEndDay.year
        val startMonthsYear = weekStartDay.year
        if (weekEndDay.month == weekStartDay.month) {
            val months = weekStartDay.headerMonthText
            ("$months $startMonthsYear").also { this.dateTextView.text = it }
        } else {
            if (startMonthsYear == endMonthsYear) {
                weekStartDay.headerMonthText.plus("- ${weekEndDay.headerMonthText}").plus(weekStartDay.year.toString())
                        .also { dateTextView.text = it }
            } else {
                weekStartDay.headerMonthText.plus(startMonthsYear.toString()).plus(" - ${weekEndDay.headerMonthText.plus(endMonthsYear.toString())}")
                        .also { dateTextView.text = it }
            }
        }
    }

    private fun addReminderClickEvent() {
        //Opening a reminder Fragment
        addReminderCalendarButton.setOnClickListener {
            if (null != activity) {
                (activity as HomeContainerActivity?)!!.onSetUpReminderQuickActionClicked()
            }
        }
    }

    /**
     * Based on the week data this ,method will send the data to the
     * [CalendarRecyclerViewDataAdapterNew] adapter
     */
    private fun getOneWeekHistoryEvent(startDay: PillpopperDay, endDay: PillpopperDay) {
        sortedHistoryEvent.clear()
        // Based on the data from the calendar Swipe we can filter the
        // History event list using the  startDay and endDay
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        startDate.set(startDay.year, startDay.month, startDay.day, 0, 0)
        endDate.set(endDay.year, endDay.month, endDay.day + 1, 0, 0)
        if (!calHistoryEvent.isNullOrEmpty()) {
            for (historyEventDays in calHistoryEvent) {
                val eventDate = Date(historyEventDays.headerTime.toLong() * 1000)
                //so this is where we are restricting the data from adding the future days
                if (eventDate.after(startDate.time) && eventDate.before(endDate.time)) {
                    sortedHistoryEvent.add(historyEventDays)
                }
            }

        }
        /*
        Collecting the future Calendar data for showing scheduled pills for the future
        */
       if (!futureEvents.isNullOrEmpty()) {
            sortedHistoryEvent.addAll(futureEvents)
        }
        sortedHistoryEvent.reverse()
        manageSortingGrouping(sortedHistoryEvent)
    }

    private fun manageSortingGrouping(data: ArrayList<HistoryEvent>) {
        val handler = Handler(Looper.getMainLooper())
        val sectionalHistoryEvent = groupingByHeaderTime(data)
        val size = sectionalHistoryEvent.size
        if (size > 0) {
            if (size in 1..3) {
                seeMoreToggleButton.visibility = View.GONE
            } else {
                seeMoreToggleButton.visibility = View.VISIBLE
            }
            noHistoryLayout.visibility = View.GONE
            addReminderCalendarLayout.visibility = View.GONE
            isAdapterSet = true
            //do the heavy lifting here 💪
            thread(true) {
                calendarRecyclerViewDataAdapterNew = CalendarRecyclerViewDataAdapterNew(sectionalHistoryEvent, historyViewModel, needToSortByFrequency, requireActivity().supportFragmentManager,mSelectedUserName)
                handler.post {
                    recyclerView.adapter = calendarRecyclerViewDataAdapterNew
                    calendarRecyclerViewDataAdapterNew.notifyDataSetChanged()
                    visibilityVisible()
                }
            }
        } else if (size <= 0) {
            if (size <= 0 && (!historyEventsList.isNullOrEmpty() || hasFutureEvents || FrontController.getInstance(mContext).isAnySchedulesAvailableForUser(mSelectedUserId))) {
                zeroOneWeekHistoryData()
            } else {
                zeroHistoryData()
            }
        }
    }

    private fun zeroHistoryData() {
        //If we don't have any history data at all we have to hide or show the views below
        seeMoreToggleButton.visibility = View.GONE
        noHistoryLayout.visibility = View.VISIBLE
        noHistoryHeader.visibility = View.VISIBLE
        noHistoryText.visibility = View.VISIBLE
        addReminderCalendarLayout.visibility = View.VISIBLE
        addReminderCalendarButton.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun zeroOneWeekHistoryData() {
        // if we don not have one week data at that particular week
        // when we swipe the viewpager we have to show or hide the views below
        seeMoreToggleButton.visibility = View.GONE
        recyclerView.visibility = View.GONE
        dateTextView.visibility = View.VISIBLE
        addReminderCalendarLayout.visibility = View.INVISIBLE
        // Just in-case the data is empty on the first run...
        mHistoryCalendarViewPager.visibility = View.VISIBLE
        addReminderCalendarButton.visibility = View.INVISIBLE
        noHistoryLayout.visibility = View.VISIBLE
        noHistoryText.visibility = View.VISIBLE
        noHistoryHeader.visibility = View.GONE
        noHistoryText.text = resources.getString(R.string.history_one_week_empty_message)
    }

    private fun visibilityVisible() {
        recyclerView.visibility = View.VISIBLE
        dateTextView.visibility = View.VISIBLE
        mHistoryCalendarViewPager.visibility = View.VISIBLE
    }

    /**
     * grouping the data by the header time
     */
    private fun groupingByHeaderTime(historyEvents: List<HistoryEvent>): ArrayList<HorizontalSectionDataModel> {
        // first sort using a frequency schedule
        // and then if that is null use timeSort and grouping
        needToSortByFrequency = true
        for (event in historyEvents) {
            if (null == event.preferences || null == event.preferences.scheduleFrequency) {
                needToSortByFrequency = false
                break
            }
        }
        //checks if the historyEventList Have a preferences that is not null
        return when (needToSortByFrequency) {
            true -> {
                // group by frequencies
                val groupByFrequencyMap = prepareHistoryEvents(historyEvents).groupBy { it.preferences.scheduleFrequency }
                val sortedFrequencyMap = mutableMapOf<String, List<HistoryEvent>>()
                //sort the frequencies and add it to map in sorted frequency order ie., D,W,M,CD(custom daily),CW(custom weekly)
                sortFrequencyComparator(groupByFrequencyMap.keys).forEach {
                    sortedFrequencyMap[it] = groupByFrequencyMap[it]!!
                }
                val groupBySortedTimeMap = mutableMapOf<String, List<HistoryEvent>>()
                for ((key, value) in sortedFrequencyMap) {
                    val groupTime = groupingByTime(value)
                    when {
                        key.equals("W", ignoreCase = true) -> {
                            for ((k, groupByTimeValue) in groupTime) {
                                val groupByWeekDay = groupByTimeValue.groupBy { it.preferences.weekdays }
                                for ((timeKey, timeValue) in groupByWeekDay)
                                    groupBySortedTimeMap["$timeKey~$k"] = timeValue
                            }
                        }
                        key.equals("M", ignoreCase = true) -> {
                            for ((k, groupByTimeValue) in groupTime) {
                                val groupByWeekDay = groupByTimeValue.groupBy { it.preferences.start }
                                for ((timeKey, timeValue) in groupByWeekDay)
                                    groupBySortedTimeMap["$timeKey~$k"] = timeValue
                            }
                        }
                        key.equals("CW", ignoreCase = true) || key.equals("CD", ignoreCase = true) -> {
                            for ((k, groupByTimeValue) in groupTime) {
                                val groupByCustom = (groupByTimeValue.groupBy { it.preferences.dayperiod }).toSortedMap(compareBy { it.toInt() })
                                for ((customKey, customValue) in groupByCustom) {
                                    val groupByCustomDayAndWeek = customValue.groupBy { it.preferences.start }
                                    for ((timeKey, timeValue) in groupByCustomDayAndWeek)
                                        groupBySortedTimeMap["$timeKey~$k~$customKey"] = timeValue
                                }
                            }
                        }
                        else -> {
                            for ((timeKey, timeValue) in groupTime)
                                groupBySortedTimeMap["$key~$timeKey"] = timeValue
                        }
                    }

                }
                // now finally we have to send the finalHistoryEvent from here
                returnFinalSortedGroupedHistoryEvent(groupBySortedTimeMap)
            }
            false -> {
                val sortedByTimeMap = groupingByTime(historyEvents)
                // now finally we have to send the finalHistoryEvent from here
                returnFinalSortedGroupedHistoryEvent(sortedByTimeMap)
            }
        }
    }

    private fun prepareHistoryEvents(historyList: List<HistoryEvent>): List<HistoryEvent> {
        for (event in historyList) {
            if (event.preferences.scheduleFrequency.equals("w", ignoreCase = true) && event.preferences.dayperiod.toInt() / 7 > 1) {
                event.preferences.scheduleFrequency = "CW"
            } else if (event.preferences.scheduleFrequency.equals("D", ignoreCase = true) && event.preferences.dayperiod.toInt() > 1) {
                event.preferences.scheduleFrequency = "CD"
            }
        }
        return historyList
    }

    private fun returnFinalSortedGroupedHistoryEvent(sortedByTimeMap: Map<String, List<HistoryEvent>>): ArrayList<HorizontalSectionDataModel> {
        var finalHistoryEvent = arrayListOf<HorizontalSectionDataModel>()
        finalHistoryEvent.clear()
        finalHistoryEvent = ArrayList()
        for (timeSorted in sortedByTimeMap) {
            val historyEventsInSection = HorizontalSectionDataModel()
            val singleHistoryEvent = ArrayList<HistoryEvent>()
            for (data in timeSorted.value) {
                singleHistoryEvent.add(data)
                historyEventsInSection.headerTime = timeSorted.key
            }
            historyEventsInSection.horizontalHistoryEventList = singleHistoryEvent
            finalHistoryEvent.add(historyEventsInSection)
        }
        return finalHistoryEvent
    }

    /**
     * Grouping based on Time
     */
    private fun groupingByTime(historyEvents: List<HistoryEvent>?): Map<String, List<HistoryEvent>> {
        var sortedByTimeMap: Map<String, List<HistoryEvent>> = emptyMap()
        historyEvents?.let { historyEventsList ->
            val groupingByTime = historyEventsList.groupBy { Util.getEventTime(mContext, it.headerTime) }
            val sdf = SimpleDateFormat(if (DateFormat.is24HourFormat(mContext)) "HH:mm" else "h:mm a", Locale.US)
            sortedByTimeMap = groupingByTime.toSortedMap(compareBy { sdf.parse(it) })
        }
        return sortedByTimeMap
    }

    private fun sortFrequencyComparator(preferences: Set<String>): ArrayList<String> {
        /**
         *   This  will be helpful to sort the MEDS by frequency
         *   "D" stands for Daily
         *   "W" stands for Weekly
         *   "M" stands for Monthly
         *   "C" stands for Custom
         *    Numbers indicate the order
         */
        val scheduleFrequency: HashMap<String, Int> = hashMapOf("D" to 0, "W" to 1, "M" to 2, "CD" to 3, "CW" to 5)
        val comparator = Comparator { pref1: String, pref2: String ->
            LoggerUtils.info("--History-- pref1- $pref1 pref2- $pref2")
            return@Comparator scheduleFrequency[pref1]!! - scheduleFrequency[pref2]!!
        }
        val copy = arrayListOf<String>().apply { addAll(preferences) }
        copy.sortWith(comparator)
        return copy
    }

    /**
     * initializing the calendar View decide how many days are the to divide into weeks
     */
    @SuppressLint("SimpleDateFormat")
    private fun initCalendarViewPagerAdapterList() {
        historyCalendarAdapterList = ArrayList()
        // Will Calculate the number of weeks in a given doseHistoryDays it could be two weeks or  a month or a year or two year
        mFrontController = FrontController.getInstance(mContext)
        val doseHistoryDays = mFrontController!!.doseHistoryDays
        currentDay = PillpopperDay.today()
        startDate = currentDay.addDays(-doseHistoryDays.toLong())
        val currentDate = System.currentTimeMillis()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val today = Date(currentDate)

        val endDate = getEndDay(formatter.format(currentDate), -doseHistoryDays)
        // Because we are dealing with history date we have to give the startDate an EndDate and the EndDate vice versa
        val numberOfWeeks = HistoryCalendarUtil.getWeeksBetween(endDate, today)
        for (weekCounter in 0 until numberOfWeeks) {
            val weekFragment = HistoryCalendarWeekFragmentNew.newInstance(getWeekStartDayFromToday(startDate).addDays((weekCounter * 7).toLong()),
                    activity,
                    historyCalendarAdapterList,
                    weekCounter)
            historyCalendarAdapterList.add(weekFragment)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getEndDay(startDay: String, num: Int): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        try {
            sdf.parse(startDay)?.let { calendar.time = it }
            calendar.add(Calendar.DAY_OF_MONTH, num)
        } catch (e: Exception) {
            PillpopperLog.exception(e.message)
        }
        return calendar.time
    }

    /**
     * Will calculate the start day in the week
     */
    private fun getWeekStartDayFromToday(startDate: PillpopperDay): PillpopperDay {
        //number of the day mean sunday being the first day monday is 2
        var pillPopperToday = startDate
        if (pillPopperToday.dayOfWeek.dayNumber > 1) {
            pillPopperToday = pillPopperToday.addDays((-1 * (pillPopperToday.dayOfWeek.dayNumber - 1)).toLong())
        }
        return pillPopperToday
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggerUtils.info("---History_Calendar--  onCreateView ")
        val view = inflater.inflate(R.layout.history_base_calendar_layout, container, false)
        historyViewModel = ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java)
        initViews(view)
        mFontRegular = ActivationUtil.setFontStyle(activity, AppConstants.FONT_ROBOTO_REGULAR)
        mFontMedium = ActivationUtil.setFontStyle(activity, AppConstants.FONT_ROBOTO_MEDIUM)
        setObserver()
        return view
    }

    /**
     * We will be notified if the day changes in here
     */
    private val dayChangedBroadcastReceiver = object : DateChangedBroadcastReceiver() {
        override fun onDateChanged(previousDate: Calendar, newDate: Calendar) {
            LoggerUtils.info("____________TIMEDAY_changed____________")
            initCalendarViewPagerAdapterList()
            historyCalendarViewOnPageSelected()
            //reFreshFragment()
        }
    }

    override fun onStart() {
        super.onStart()
        val cal = Calendar.getInstance()
        dayChangedBroadcastReceiver.registerOnStart(requireActivity(), cal)
    }

    private fun setObserver() {
        historyViewModel.historyEventsList.observe(viewLifecycleOwner) { items ->
            historyEventsList = items
            getHistoryData(historyEventsList as MutableList<HistoryEvent>)
        }
    }

    override fun onResume() {
        super.onResume()
        // DE22066
        if (needToRefreshFromBackground) {
            needToSortByFrequency = false
            if (this::weekStartDay.isInitialized) {
                getOneWeekHistoryEvent(weekStartDay, possibleWeekEndDay)
            }
        }
        //refresh overlay medication
        if(RunTimeData.getInstance().isOverlayItemClicked ){
            if(!isCalendarRefreshed) {
                isCalendarRefreshed = true
                mHistoryCalendarViewPager.postDelayed(Runnable {
                    mHistoryCalendarViewPager.currentItem = RunTimeData.getInstance().weekSelectedPosition
                    mOnPageChangeListener.onPageSelected(RunTimeData.getInstance().weekSelectedPosition)
                }, 100)
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        LoggerUtils.info("---History_Calendar-- onAttach() ")
    }

    override fun onDestroy() {
        super.onDestroy()
        UIUtils.dismissProgressDialog()
        historyViewModel.isSeeMoreOrLessClicked.postValue(false)
    }

    private fun initArguments() {
        if (null != arguments) {
            mSelectedUserId = requireArguments()[mBundleUserId] as String
            mSelectedUserName = requireArguments()[mBundleUserName] as String
        }
    }

    /**
     *
     */
    private fun getHistoryData(historyEvents: List<HistoryEvent>?) {
        calHistoryEvent.clear()
        for (historyEvent in historyEvents!!) {
            //removing the takeAsNeeded history events as those events are not supposed to be visible in calendar.
            if (null == historyEvent.preferences || null == historyEvent.preferences.scheduleChoice) {
                calHistoryEvent.add(historyEvent)
            } else {
                if (null != historyEvent.preferences.scheduleChoice && !historyEvent.preferences.scheduleChoice.equals(AppConstants.SCHEDULE_CHOICE_AS_NEEDED, ignoreCase = true)) {
                    calHistoryEvent.add(historyEvent)
                }
            }
        }
    }

    /**
     * This is the Adapter Class that ties the data to the Calendar View
     */
    @SuppressLint("WrongConstant")
    class HistoryCalendarViewPagerAdapter(fm: FragmentManager?, private val fragments: MutableList<HistoryCalendarWeekFragmentNew>)
        : FragmentStatePagerAdapter(fm!!, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }

    override fun onStop() {
        super.onStop()
        LoggerUtils.info("--History Calendar-- onStop")
        needToRefreshFromBackground = true
    }
}