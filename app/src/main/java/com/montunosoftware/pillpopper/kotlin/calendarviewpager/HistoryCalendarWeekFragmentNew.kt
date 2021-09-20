package com.montunosoftware.pillpopper.kotlin.calendarviewValpager

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.model.PillpopperDay
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil

/**
 * Created by D113186 on 14,December,2020
 */
class HistoryCalendarWeekFragmentNew : Fragment() {

    private var mInflatedView: View? = null
    private var weekStartDay: PillpopperDay? = null
    private var mSelectedDayInWeek: PillpopperDay? = null
    private var mContext: Context? = null
    private var mParentViewPagerAdapter: List<HistoryCalendarWeekFragmentNew>? = null
    var positionInParentViewPager = 0
    private var mFontRegular: Typeface? = null
    private var mFontBold: Typeface? = null
    private val mDayLinearLayout = arrayOfNulls<LinearLayout>(7)
    private val mDateTextView = arrayOfNulls<TextView>(7)
    private val mDayTextView = arrayOfNulls<TextView>(7)
    val futureDays = arrayOfNulls<TextView>(7)
    private val mCurrentDayIndicatorView = arrayOfNulls<View>(7)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mInflatedView = inflater.inflate(R.layout.history_schedule_calender_week_layout, container, false)
        mFontBold = ActivationUtil.setFontStyle(activity, AppConstants.FONT_ROBOTO_BOLD)
        mFontRegular = ActivationUtil.setFontStyle(activity, AppConstants.FONT_ROBOTO_REGULAR)
        return mInflatedView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mContext = context
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnCalendarDateSelectedListener")
        }
    }

    override fun onStart() {
        super.onStart()
        initUi(mInflatedView)
        populateUi()
        val days: PillpopperDay? = if (mContext != null) {
            PillpopperDay.today()
        } else {
            weekStartDay?.addDays(PillpopperDay.today().dayOfWeek.dayNumber.toLong())
        }
        if (days != null) {
            setSelectedDayInWeek(days)
        }
    }

    private fun initUi(view: View?) {
        view?.let { viewVal ->
            mDayLinearLayout[0] = viewVal.findViewById(R.id.schedule_calendar_sunday_holder)
            mDayLinearLayout[1] = viewVal.findViewById(R.id.schedule_calendar_monday_holder)
            mDayLinearLayout[2] = viewVal.findViewById(R.id.schedule_calendar_tuesday_holder)
            mDayLinearLayout[3] = viewVal.findViewById(R.id.schedule_calendar_wednesday_holder)
            mDayLinearLayout[4] = viewVal.findViewById(R.id.schedule_calendar_thursday_holder)
            mDayLinearLayout[5] = viewVal.findViewById(R.id.schedule_calendar_friday_holder)
            mDayLinearLayout[6] = viewVal.findViewById(R.id.schedule_calendar_saturday_holder)
            for (index in 0..6) {
                mDateTextView[index] = mDayLinearLayout[index]?.getChildAt(1) as TextView
                mDayTextView[index] = mDayLinearLayout[index]?.getChildAt(0) as TextView
                mCurrentDayIndicatorView[index] = mDayLinearLayout[index]?.getChildAt(2)
            }
        }
        initUiColorsAndListeners()
    }

    private fun initUiColorsAndListeners() {
        for (index in 0..6) {
            val pDay = weekStartDay?.addDays(index.toLong())
            pDay?.let { pillpopperDay ->
                mDateTextView[index]!!.typeface = mFontRegular
                mDayTextView[index]!!.typeface = mFontRegular
                if (pillpopperDay.after(PillpopperDay.today())) {
                    mDateTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.history_calendar_future_day_color))
                    mDayTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.history_calendar_future_day_color))
                } else {
                    mDateTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.black))
                    mDayTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.black))
                    mDayLinearLayout[index]!!.setOnClickListener {
                        setSelectedDayInWeekWithCallback(pDay)
                        if (positionInParentViewPager > 0) {
                            mParentViewPagerAdapter!![positionInParentViewPager - 1].setSelectedDayInWeek(pDay)
                        } else {
                            mParentViewPagerAdapter!![positionInParentViewPager + 1].setSelectedDayInWeek(pDay)
                        }
                    }
                }
            }
        }
    }


    private fun populateUi() {
        for (index in 0..6) {
            weekStartDay?.addDays(index.toLong())?.let { weekDay ->
                weekDay.day.toString().also {
                    mDateTextView[index]!!.text = it
                }
                if (weekDay == PillpopperDay.today()) {
                    mDateTextView[index]!!.typeface = mFontBold
                    mDayTextView[index]!!.typeface = mFontBold
                    // we have to change the color to white and the bg to  a drawable
                    mDateTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.white))
                    mDayTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.white))
                    // mDayLinearLayout[index]!!.setBackgroundColor(Util.getColorWrapper(mContext, R.color.history_calendar_overlay_color))
                    mDayLinearLayout[index]!!.setBackgroundResource(R.drawable.history_calendar_overlay)
                    mDateTextView[index]!!.setBackgroundColor(Util.getColorWrapper(mContext, R.color.transparent_color))
                } else if (weekDay.before(PillpopperDay.today())) {
                    mDayTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.black))
                    mDateTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.black))
                }
            }
        }
    }

    private fun setSelectedDayInWeek(selectedDayInWeek: PillpopperDay) {
        var selectedDayInWeek = selectedDayInWeek
        weekStartDay?.addDays((selectedDayInWeek.dayOfWeek.dayNumber - 1).toLong())?.let { pillpopperDay ->
            if (pillpopperDay.after(PillpopperDay.today())) {
                selectedDayInWeek = PillpopperDay.today()
            }
        }

        setSelectedDayInWeekUi(selectedDayInWeek)
    }

    private fun setSelectedDayInWeekUi(selectedDayInWeek: PillpopperDay) {
        if (mInflatedView != null) {
            populateUi()
            val index = selectedDayInWeek.dayOfWeek.dayNumber - 1
            if (mSelectedDayInWeek != null) {
                val oldIndexSelected = mSelectedDayInWeek!!.dayOfWeek.dayNumber - 1
                mDateTextView[oldIndexSelected]!!.setBackgroundResource(R.color.white)
                mDateTextView[oldIndexSelected]!!.setTextColor(Util.getColorWrapper(mContext, R.color.black))
                mDayTextView[oldIndexSelected]!!.setTextColor(Util.getColorWrapper(mContext, R.color.create_rem_txt_color))
                populateUi()
            }
            mSelectedDayInWeek = selectedDayInWeek
            if (mSelectedDayInWeek != PillpopperDay.today()) {
                //  mDateTextView[index]!!.setBackgroundResource(R.drawable.select_date_background_circle_blue)
                mDateTextView[index]!!.setTextColor(Util.getColorWrapper(mContext, R.color.black))
                populateUi()
            }
        }
    }

    fun setSelectedDayInWeekWithCallback(selectedDayInWeek: PillpopperDay) {
        setSelectedDayInWeek(selectedDayInWeek)
        var callbackPillpopperDay =
            weekStartDay?.addDays((selectedDayInWeek.dayOfWeek.dayNumber - 1).toLong())
        if (callbackPillpopperDay!!.after(PillpopperDay.today())) {
            callbackPillpopperDay = PillpopperDay.today()
        }
    }

    fun getmContext(): Context? {
        return mContext
    }

    fun setmContext(mContext: Context?) {
        this.mContext = mContext
    }

    fun getWeekStartDay(): PillpopperDay? {
        return weekStartDay
    }

    fun getSelectedDayInWeek(): PillpopperDay? {
        return mSelectedDayInWeek
    }

    fun getmParentViewPagerAdapter(): List<HistoryCalendarWeekFragmentNew>? {
        return mParentViewPagerAdapter
    }

    fun setmParentViewPagerAdapter(mParentViewPagerAdapter: List<HistoryCalendarWeekFragmentNew>?) {
        this.mParentViewPagerAdapter = mParentViewPagerAdapter
    }

    companion object {
        fun newInstance(weekStartDay: PillpopperDay, context: Context?, parentViewPagerAdapter: List<HistoryCalendarWeekFragmentNew>?, positionInViewPager: Int): HistoryCalendarWeekFragmentNew {
            val fragment = HistoryCalendarWeekFragmentNew()
            fragment.weekStartDay = weekStartDay
            fragment.setmContext(context)
            fragment.setmParentViewPagerAdapter(parentViewPagerAdapter)
            fragment.positionInParentViewPager = positionInViewPager
            return fragment
        }
    }
}