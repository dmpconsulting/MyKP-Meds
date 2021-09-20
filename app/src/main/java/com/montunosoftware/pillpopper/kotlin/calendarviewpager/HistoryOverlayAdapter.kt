package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.montunosoftware.mymeds.BR
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryOverlayDialogeAdapterBinding
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.kotlin.history.HistoryDetailActivity
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil


class
HistoryOverlayAdapter(var historyEvent: ArrayList<HistoryEvent>, var context: Context, var mSelectedUserName: String?) : RecyclerView.Adapter<HistoryOverlayAdapter.ViewHolder>() {
    private lateinit var actCntxt: Context
    private lateinit var binding: HistoryOverlayDialogeAdapterBinding
    private val drawableTaken: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_taken)!!
    private val drawableSkipped: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_skipped)!!
    private val drawableMissed: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_history_missed)!!
    private val drawableMixed: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_history_mixed)!!
    private val emptyPillHistory: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_empty_pill_history)!!

    var mRobotoRegular: Typeface? = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_REGULAR)

    inner class ViewHolder(viewDataBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewDataBinding.root) {
        fun bind(data: HistoryEvent) {
            binding.setVariable(BR.historyEvent, data)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.history_overlay_dialoge_adapter, parent, false)
        actCntxt = parent.context
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return historyEvent.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
    companion object {
        private const val NONE = "NONE"
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyList = historyEvent[position]
        holder.bind(historyList)
        binding.tvPillName.text = getTruncatedPillName(historyList.pillName)
        binding.tvPillName.typeface = mRobotoRegular
        when (historyEvent[position].operationStatus) {
            PillpopperConstants.ACTION_TAKE_PILL_HISTORY -> {
                binding.historyPillOperationStatus.background = drawableTaken
            }
            PillpopperConstants.ACTION_SKIP_PILL_HISTORY -> {
                binding.historyPillOperationStatus.background = drawableSkipped
            }
            PillpopperConstants.ACTION_MIXED_PILL_HISTORY -> {
                binding.historyPillOperationStatus.background = drawableMixed
            }
            PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY -> {
                binding.historyPillOperationStatus.background = drawableMissed
                binding.tvPostponedTime.visibility = View.VISIBLE
                val finalPostponedTime = historyEvent[position].preferences.finalPostponedDateTime
                if (null != finalPostponedTime) {
                    binding.tvPostponedTime.text = context.getString(R.string.postponed_to) + Util.getEventTime(context, Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(finalPostponedTime)).gmtSeconds.toString())
                }
            }
            NONE -> {
                binding.historyPillOperationStatus.background = emptyPillHistory
            }
            else -> {
                binding.historyPillOperationStatus.background = drawableMissed
                //holder.historyPillOperationStatus.background = drawableMissed
            }
        }

        binding.lrMain.setOnClickListener(View.OnClickListener {
            RunTimeData.getInstance().isOverlayItemClicked = true
            RunTimeData.getInstance().isCalendarPosChanged = true
            RunTimeData.getInstance().isShouldRetainHistoryOverlay = true
            val intent = Intent(actCntxt, HistoryDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("historyEvent", historyEvent[position])
            bundle.putString("mBundleUserName", mSelectedUserName)
            bundle.putBoolean("fromCalendarOverlayScreen",true)
            intent.putExtras(bundle)
            // reset the history med change flag.
            RunTimeData.getInstance().isHistoryMedChanged = false
            actCntxt?.startActivity(intent)
        })

    }

    /**
     * max char limit for the pill name is 100
     * return substring with ellipses
     */
    private fun getTruncatedPillName(pillName: String?): CharSequence? {
        return if (pillName?.length!! > 100) {
            pillName.substring(0, 100).plus("...")
        } else {
            pillName
        }
    }
}