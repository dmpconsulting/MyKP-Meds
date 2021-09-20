package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryListItemBinding
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.android.util.ViewClickHandler
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil

class HistoryListAdapter(val context: Context, private val historyEventsList: List<HistoryEvent>, private val doseHistoryFromSettingsForFooter: String?, listener: OnItemClicked, var historySpanSelected: String?) : RecyclerView.Adapter<HistoryListAdapter.ItemViewHolder>() {

    lateinit var mBinding: HistoryListItemBinding
    var listener: OnItemClicked? = null
    private val robotoBold: Typeface
    private val robotoMedium: Typeface
    private val robotoRegular: Typeface
    private val drawableTaken: Drawable
    private val drawableSkipped: Drawable
    private val drawableMissed: Drawable

    init {
        this.listener = listener
        drawableTaken = ContextCompat.getDrawable(context, R.drawable.ic_taken)!!
        drawableSkipped = ContextCompat.getDrawable(context, R.drawable.ic_skipped)!!
        drawableMissed = ContextCompat.getDrawable(context, R.drawable.ic_history_missed)!!
        robotoBold = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_BOLD)
        robotoMedium = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_MEDIUM)
        robotoRegular = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_REGULAR)
    }

    interface OnItemClicked {
        fun onItemClick(historyEvent: HistoryEvent)
        fun onSettingClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.history_list_item, parent, false)
        mBinding.robotoBold = robotoBold
        mBinding.robotoMedium = robotoMedium
        mBinding.robotoRegular = robotoRegular
        return ItemViewHolder(mBinding.root)
    }

    override fun getItemCount(): Int {
        return if(historyEventsList.isEmpty()) 2 else historyEventsList.size + 2
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        if(historyEventsList.isEmpty())
        {
            when(position)
            {
                0 ->
                {
                    showHistorySettingsLayout()

                }
                else ->
                {
                    mBinding.historyDoseEventDay.visibility = View.GONE
                    mBinding.historyDoseEventTime.visibility = View.GONE
                    mBinding.drugDetailsLayout.visibility = View.GONE
                    mBinding.historyDisclaimer.visibility = View.GONE
                    mBinding.noHistoryRecords.visibility = View.VISIBLE
                    mBinding.historySettingsLayout.visibility = View.GONE
                    mBinding.postponedStatus.visibility = View.GONE
                    mBinding.historyDisclaimer.visibility = View.GONE
                }
            }
        }
        else {
            when (position) {
                0 -> {
                    showHistorySettingsLayout()
                }
                itemCount - 1 -> {
                    showHistoryDisclaimer()
                }
                else -> {
                    val historyItem = historyEventsList[position - 1]
                    val eventDay = Util.getEventDay(historyItem.headerTime)
                    val eventTime = Util.getEventTime(context, historyItem.headerTime)
                    mBinding.historySettingsLayout.visibility = View.GONE
                    mBinding.postponedStatus.visibility = View.GONE
                    mBinding.historyDisclaimer.visibility = View.GONE
                    if (position == 1) {
                        mBinding.historyDoseEventDay.visibility = View.VISIBLE
                        mBinding.historyDoseEventTime.visibility = View.VISIBLE
                        mBinding.historyDoseEventDay.text = eventDay
                        mBinding.historyDoseEventTime.text = eventTime
                    } else {
                        mBinding.drugDetailsLayout.visibility = View.VISIBLE
                        if (eventDay.equals(Util.getEventDay(historyEventsList[position - 2].headerTime))) {
                            mBinding.historyDoseEventDay.visibility = View.GONE
                            if (eventTime.equals(Util.getEventTime(context, historyEventsList[position - 2].headerTime))) {
                                mBinding.historyDoseEventTime.visibility = View.GONE
                            } else {
                                mBinding.historyDoseEventTime.visibility = View.VISIBLE
                                mBinding.historyDoseEventTime.text = eventTime
                                val params: ViewGroup.MarginLayoutParams = mBinding.historyDoseEventTime.layoutParams as ViewGroup.MarginLayoutParams
                                params.topMargin = Util.convertToDp(12, context)

                            }
                        } else {
                            mBinding.historyDoseEventDay.visibility = View.VISIBLE
                            mBinding.historyDoseEventDay.text = eventDay
                            mBinding.historyDoseEventTime.visibility = View.VISIBLE
                            mBinding.historyDoseEventTime.text = eventTime
                        }
                    }

                    if (historyItem.operationStatus.equals(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                        mBinding.postponedStatus.visibility = View.VISIBLE
                        val finalPostponedTime = historyItem.preferences!!.finalPostponedDateTime
                        if (null != finalPostponedTime) {
                            mBinding.postponedStatus.text = context.getString(R.string.postponed_to) + Util.getEventTime(context, Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(finalPostponedTime)).gmtSeconds.toString())
                        }
                    }

                    var drugName = ""
                    if (null != historyItem.pillName) {
                        drugName = if (historyItem.pillName?.contains("(")!! && historyItem.pillName?.contains(")")!!) {
                            val index = historyItem.pillName.indexOf("(")
                            historyItem.pillName.substring(0, index)
                        } else {
                            historyItem.pillName
                        }
                    }

                    mBinding.drugName.text = if (drugName != null && drugName.length > 100)
                        drugName.substring(0, 100).plus("...")
                    else
                        drugName

                    when (historyItem.operationStatus) {
                        PillpopperConstants.ACTION_TAKE_PILL_HISTORY -> {
                            mBinding.drugActionImage.contentDescription = context.getString(R.string.taken)
                            mBinding.drugActionImage.background = drawableTaken
                        }
                        PillpopperConstants.ACTION_SKIP_PILL_HISTORY -> {
                            mBinding.drugActionImage.contentDescription = context.getString(R.string.skipped)
                            mBinding.drugActionImage.background = drawableSkipped
                        }
                        else -> {
                            mBinding.drugActionImage.contentDescription = PillpopperConstants.ACTION_MISS_PILL
                            mBinding.drugActionImage.background = drawableMissed
                        }
                    }
                    mBinding.drugDetailsLayout.setOnClickListener { listener?.onItemClick(historyItem) }
                }
            }
        }
    }

    private fun showHistorySettingsLayout() {
        mBinding.historySpan.text = historySpanSelected.plus(" ").plus(context.resources.getString(R.string.history))
        mBinding.historySettingsLayout.visibility = View.VISIBLE
        mBinding.historyDoseEventDay.visibility = View.GONE
        mBinding.historyDoseEventTime.visibility = View.GONE
        mBinding.drugDetailsLayout.visibility = View.GONE
        mBinding.historyDisclaimer.visibility = View.GONE
        mBinding.settingsLayout.setOnClickListener {
            ViewClickHandler.preventMultiClick(it)
            listener?.onSettingClicked()
        }
    }

    private fun showHistoryDisclaimer() {
        mBinding.historySettingsLayout.visibility = View.GONE
        mBinding.historyDoseEventDay.visibility = View.GONE
        mBinding.historyDoseEventTime.visibility = View.GONE
        mBinding.drugDetailsLayout.visibility = View.GONE
        mBinding.historyDisclaimer.visibility = View.VISIBLE
        mBinding.historyDisclaimer.text = doseHistoryFromSettingsForFooter?.let { String.format(context.resources.getString(R.string.history_base_screen_bottom_text), it) }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}