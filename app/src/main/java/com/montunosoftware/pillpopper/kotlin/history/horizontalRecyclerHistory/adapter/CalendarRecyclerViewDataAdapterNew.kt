package com.montunosoftware.pillpopper.kotlin.history.horizontalRecyclerHistory.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.montunosoftware.mymeds.BR
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.CalendarHorizontalListBinding
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.android.util.ViewClickHandler
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.kotlin.calendarviewpager.HistoryOverlayDialogFragment
import com.montunosoftware.pillpopper.kotlin.history.HistoryViewModel
import com.montunosoftware.pillpopper.kotlin.history.horizontalRecyclerHistory.HorizontalSectionDataModel
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil


class CalendarRecyclerViewDataAdapterNew(
        private val historyItem: List<HorizontalSectionDataModel>,
        private val historyViewModel: HistoryViewModel,
        private val isSortBasedOnFrequency: Boolean,
        private val supportFragmentManager: FragmentManager,
        private val mSelectedUserName: String?
) : RecyclerView.Adapter<CalendarRecyclerViewDataAdapterNew.ViewHolder>() {

    private lateinit var horizontalSingleSectionItem: ArrayList<HistoryEvent>
    private lateinit var mContext: Context
    private lateinit var binding: CalendarHorizontalListBinding

    companion object {
        const val HISTORY_ITEM_COUNT = 3
        const val MONTHLY = "M"
        const val WEEKLY = "W"
        const val DAILY = "D"
        const val CUSTOM_DAILY = "CD"
        const val CUSTOM_WEEKLY = "CW"
    }

    inner class ViewHolder(viewbinding: ViewDataBinding) : RecyclerView.ViewHolder(viewbinding.root) {
        fun bind(headerTime: String, position: Int) {
            binding.setVariable(BR.headerTime, headerTime)
            binding.position = position
            binding.context = this@CalendarRecyclerViewDataAdapterNew
            binding.robotoRegular = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_REGULAR)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.calendar_horizontal_list, parent, false)
        mContext = parent.context
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        val historyItemSize = this.historyItem.size
        var runTimeSeeMoreState = RunTimeData.getInstance().seeMoreEnabled
        if (historyItem.isEmpty()) {
            runTimeSeeMoreState = true
        }
        return when {
            runTimeSeeMoreState -> {
                historyItemSize
            }
            historyItemSize in 1..3 -> {
                historyItemSize
            }
            else -> {
                HISTORY_ITEM_COUNT
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val headerTime = if(historyItem[position].headerTime.contains("~")) historyItem[position].headerTime.split("~")[1] else historyItem[position].headerTime
        val preferences = historyItem[position].horizontalHistoryEventList
        horizontalSingleSectionItem = historyItem[position].horizontalHistoryEventList
        //Adding grouping by Frequency
        if (isSortBasedOnFrequency) {
            val frequency = getFrequencyByScheduleFrequencyType(preferences)
            holder.bind(headerTime.plus(" ").plus(frequency), position)
        } else {
            holder.bind(headerTime, position)
        }
        val resultsOfHistoryEventItems = historyViewModel.groupSimilarOperationStatus(horizontalSingleSectionItem)
        val drawableList = resultsOfHistoryEventItems.let { historyViewModel.getDrawableList(it, mContext) }
        val imgList: ArrayList<ImageView> = arrayListOf(
                binding.historyCalendarDrugActionImageSunday,
                binding.historyCalendarDrugActionImageMonday,
                binding.historyCalendarDrugActionImageTuesday,
                binding.historyCalendarDrugActionImageWednesday,
                binding.historyCalendarDrugActionImageThursday,
                binding.historyCalendarDrugActionImageFriday,
                binding.historyCalendarDrugActionImageSaturday
        )
        setImgBackground(imgList, drawableList)
      if(RunTimeData.getInstance().isOverlayItemClicked){

            ViewClickHandler.preventMultiClick(binding.historyCalendarDrugActionWeeksHolder)
                horizontalSingleSectionItem = historyItem[RunTimeData.getInstance().listPosition].horizontalHistoryEventList
                val historyEventForCurrentDate: ArrayList<HistoryEvent> = ArrayList()
                for (historyData in horizontalSingleSectionItem) {
                    val startDate = RunTimeData.getInstance().calenderStartDate
                    val date = Util.getEventDay(historyData.headerTime)
                    //future events are not suppose to be clickable
                    try {
                        if (date.split("/")[1].toInt() == (startDate.addDays(RunTimeData.getInstance().listCalendarPosition.toLong())).day
                                && historyData.headerTime.toLong() < PillpopperTime.now().gmtSeconds) {
                            historyEventForCurrentDate.add(historyData)
                        }
                    } catch (ex: Exception) {
                        PillpopperLog.say(ex.message)
                    }
                }
          if (historyEventForCurrentDate.size > 0 && RunTimeData.getInstance().isHistoryItemUpdated) {
              showOverlay(historyEventForCurrentDate, mSelectedUserName)
          }
          RunTimeData.getInstance().isCalendarPosChanged = false
          RunTimeData.getInstance().isOverlayItemClicked = false
        }

    }

    /**
     * depending on the schedule frequency Initial name we decide the type and DayPeriod
     */
    private fun getFrequencyByScheduleFrequencyType(prefs: List<HistoryEvent>?): String {
        var fullName = ""
        prefs?.onEach { historyEvent ->
            historyEvent.preferences?.let { prefsValues ->
                val days = if (null != prefsValues.dayperiod) prefsValues.dayperiod.toInt() else 1
                when (prefsValues.scheduleFrequency) {
                    CUSTOM_DAILY -> {
                        fullName = "Every $days days"
                    }
                    CUSTOM_WEEKLY -> {
                        fullName = "Every " + days / 7 + " weeks"
                    }
                    MONTHLY -> {
                        fullName = "Monthly"
                    }
                    WEEKLY -> {
                        fullName = "Weekly"
                    }
                    DAILY -> {
                        fullName = ""
                    }
                }
            }
        }
        return fullName
    }

    private fun setImgBackground(imgList: ArrayList<ImageView>, drawableList: ArrayList<Drawable>) {
        for (x in 0..6) {
            imgList[x].background = drawableList[x]
            val drawableUpcoming: Drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_history_upcoming)!!
            val emptyPillHistory: Drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_empty_pill_history)!!
            if(drawableList[x].constantState == drawableUpcoming.constantState || drawableList[x].constantState == emptyPillHistory.constantState){
                imgList[x].importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        }
    }

    fun onEventImageClick(position: Int, listPosition: Int) {
        ViewClickHandler.preventMultiClick(binding.historyCalendarDrugActionWeeksHolder)
        if(!RunTimeData.getInstance().isHistoryOverlayShown) {
            horizontalSingleSectionItem = historyItem[listPosition].horizontalHistoryEventList
            val historyEventForCurrentDate: ArrayList<HistoryEvent> = ArrayList()
            for (historyData in horizontalSingleSectionItem) {
                val startDate = RunTimeData.getInstance().calenderStartDate
                val date = Util.getEventDay(historyData.headerTime)
                //future events are not suppose to be clickable
                try {
                    if (date.split("/")[1].toInt() == (startDate.addDays(position.toLong())).day
                            && historyData.headerTime.toLong() < PillpopperTime.now().gmtSeconds) {
                      historyEventForCurrentDate.add(historyData)
                    }
                } catch (ex: Exception) {
                    PillpopperLog.say(ex.message)
                }
            }
            if (historyEventForCurrentDate.size > 0) {
                RunTimeData.getInstance().listPosition = listPosition
                RunTimeData.getInstance().listCalendarPosition = position
                showOverlay(historyEventForCurrentDate,mSelectedUserName)
            }
        }
    }

    /**
     * Classes that handles the Pill status operations
     * [Taken] if all the pills are taken and
     * [Missed] if we miss one or more of the pills
     * [Skipped]  if we skipped one or more of the pils
     * [Upcoming] For the Future days
     * [Empty]  if nothing is assigned for that day
     */
    class PillAction(
            var operationAction: ArrayList<String>,
            var taken: Boolean = false,
            var missed: Boolean = false,
            var skipped: Boolean = false,
            var postponed: Boolean = false,
            var reminder: Boolean = false,
            var upcoming: Boolean = false,
    )

    private fun showOverlay(historyEvent:ArrayList<HistoryEvent>,selectedName:String?){
        val historyDialogFragment = HistoryOverlayDialogFragment(historyEvent,selectedName)
        historyDialogFragment.show(supportFragmentManager, "history_overlay_fragment")
    }

}