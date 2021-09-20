package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.montunosoftware.mymeds.BR
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryBulkActionChangeListBinding
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils

class HistoryActionChangeAdapter(var historyEventList: ArrayList<HistoryEvent>, var _thisActivity: PillpopperActivity) : RecyclerView.Adapter<HistoryActionChangeAdapter.ViewHolder>() {
    lateinit var binding: HistoryBulkActionChangeListBinding
    var context: Context? = null
    lateinit var robotoMedium: Typeface
    lateinit var robotoRegular: Typeface


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryActionChangeAdapter.ViewHolder {
        this.context = _thisActivity
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.history_bulk_action_change_list, parent, false)
        robotoMedium = ActivationUtil.setFontStyle(parent.context, AppConstants.FONT_ROBOTO_MEDIUM)
        robotoRegular = ActivationUtil.setFontStyle(parent.context, AppConstants.FONT_ROBOTO_REGULAR)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryActionChangeAdapter.ViewHolder, position: Int) {
        val historyEvent = historyEventList[position]
        holder.bind(historyEvent)
        binding.view.visibility = if (position == historyEventList.size - 1) View.VISIBLE else View.GONE
        holder.setIsRecyclable(false)
        refreshImage(historyEvent)
        when (historyEvent.operationStatus) {
            PillpopperConstants.ACTION_TAKE_PILL_HISTORY -> context?.getString(R.string.taken)?.let { addActionImage(context, R.drawable.ic_taken, it) }
            PillpopperConstants.ACTION_SKIP_PILL_HISTORY -> context?.getString(R.string.skipped)?.let { addActionImage(context, R.drawable.ic_skipped, it) }
            else -> {
                if (null != historyEvent.actionType) {
                    if (historyEvent.actionType.equals(PillpopperConstants.ACTION_SKIP_PILL_HISTORY, ignoreCase = true)) {
                        context?.getString(R.string.skipped)?.let { addActionImage(context, R.drawable.ic_skipped, it) }
                    } else if (historyEvent.actionType.equals(PillpopperConstants.ACTION_TAKE_PILL_HISTORY, ignoreCase = true)) {
                        context?.getString(R.string.taken)?.let { addActionImage(context, R.drawable.ic_taken, it) }
                    }
                }
            }
        }

        binding.skipImage.setOnClickListener(View.OnClickListener {
            historyEvent.actionType = PillpopperConstants.ACTION_SKIP_PILL_HISTORY
            notifyDataSetChanged()
        })
        binding.toBeTakenCheckmark.setOnClickListener {
            historyEvent.actionType = PillpopperConstants.ACTION_TAKE_PILL_HISTORY
            historyEvent.preferences.actionDate =  Util.convertDateIsoToLong(historyEvent.headerTime)
            notifyDataSetChanged()
        }
        binding.pillImage.setOnClickListener {
            onImageClicked(historyEvent)
        }
        binding.actionPillImage.setOnClickListener {
            onImageClicked(historyEvent)
        }
    }

    override fun getItemCount(): Int {
        return historyEventList.size
    }

    private fun addActionImage(context: Context?, actionImage: Int, action: String) {
        binding.actionPillImage.visibility = View.VISIBLE
        binding.actionPillImage.setImageDrawable(context, ContextCompat.getDrawable(context!!, actionImage))
        binding.actionPillImage.contentDescription = action +", "+ context.getString(R.string.content_description_current_and_Late_Reminder_image)
        binding.pillImage.visibility = View.GONE
        binding.actionButtonsLayout.visibility = View.GONE
    }

    private fun refreshImage(historyEvent: HistoryEvent) {
        try {
            Handler(Looper.getMainLooper()).run {
                val refreshedHistoryEvent = FrontController.getInstance(context).getHistoryEditEventDetails(historyEvent.historyEventGuid.toString())
                ImageUILoaderManager.getInstance().loadDrugImage(context, refreshedHistoryEvent?.pillImageGuid, refreshedHistoryEvent?.pillId, binding.pillImage, Util.getDrawableWrapper(context, R.drawable.pill_default))
            }
        } catch (ex: Exception) {
            LoggerUtils.info(ex.message)
        }
    }

    private fun onImageClicked(historyEvent: HistoryEvent) {
        val refreshedHistoryEvent = FrontController.getInstance(context).getHistoryEditEventDetails(historyEvent.historyEventGuid.toString())
        val expandImageIntent = Intent(context, EnlargeImageActivity::class.java)
        expandImageIntent.putExtra("pillId", refreshedHistoryEvent.pillId)
        expandImageIntent.putExtra("imageId", refreshedHistoryEvent.pillImageGuid)
        expandImageIntent.putExtra("pillName", refreshedHistoryEvent.pillBrandName)
        expandImageIntent.putExtra("isFromReminderDrugDetailActivity", true)
        context?.startActivity(expandImageIntent)
        RunTimeData.getInstance().setIsFromHistory(true)
    }

    inner class ViewHolder(viewBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(data: HistoryEvent) {
            binding.setVariable(BR.historyEvent, data)
            binding.robotoMedium = robotoMedium
            binding.robotoRegular = robotoRegular
            binding.executePendingBindings()
        }
    }
}