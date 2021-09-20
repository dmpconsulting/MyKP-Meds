package com.montunosoftware.pillpopper.kotlin.lateremider

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.montunosoftware.mymeds.BR
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.LateReminderDetailListBinding
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.kotlin.quickview.ActionBottomDialogFragment
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil

class LateReminderDetailAdapter(var drugList: List<Drug>, var viewModelProvider: LateReminderDetailViewModel, var _thisActivity: PillpopperActivity) : RecyclerView.Adapter<LateReminderDetailAdapter.ViewHolder>() {

    lateinit var binding: LateReminderDetailListBinding
    var context: Context? = null
    lateinit var robotoMedium : Typeface
    lateinit var robotoRegular : Typeface
    var drugPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.context = _thisActivity
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.late_reminder_detail_list, parent, false)
        robotoMedium = ActivationUtil.setFontStyle(parent.context, AppConstants.FONT_ROBOTO_MEDIUM)
        robotoRegular = ActivationUtil.setFontStyle(parent.context, AppConstants.FONT_ROBOTO_REGULAR)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return drugList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drug = drugList[position]
        holder.bind(drug)
        binding.view.visibility = if (position == drugList.size - 1)
            View.VISIBLE
        else View.GONE

        holder.setIsRecyclable(false)
        if(context!=null){
            ImageUILoaderManager.getInstance().loadDrugImage(context, drugList[position].imageGuid, drugList[position].guid, binding.pillImage, Util.getDrawableWrapper(context, R.drawable.pill_default))
        }
        when (drug.getmAction()) {
            PillpopperConstants.SKIPPED -> context?.getString(R.string.skipped)?.let { setVisibility(R.drawable.ic_skipped, it) }
            PillpopperConstants.TAKEN -> context?.getString(R.string.taken)?.let { setVisibility(R.drawable.ic_taken, it) }
            else -> {
                ImageUILoaderManager.getInstance().loadDrugImage(context, drugList[position].imageGuid, drugList[position].guid, binding.pillImage, Util.getDrawableWrapper(context, R.drawable.pill_default))
                if (null != drug.imageGuid) {
                    binding.pillImage.contentDescription = context?.getString(R.string.content_description_current_and_Late_Reminder_image)
                } else {
                    binding.pillImage.contentDescription = context?.getString(R.string.tap_to_enlarge_default_pill_image)
                }
                binding.actionPillImage.visibility = View.GONE
            }
        }

        // causes blink issue, if written in xml file
        binding.seeNotesButton.visibility = if (Util.isEmptyString(drug.notes)) View.GONE else View.VISIBLE

        binding.actionImage.setOnClickListener{ parentView ->
            drugPosition = position
            showBottomSheet(drug)
        }
        binding.toBeTakenCheckmark.setOnClickListener {
            viewModelProvider.onMedTakenClicked(drugList[position], position)
        }
    }

    private fun setVisibility(icon: Int, action: String) {
        binding.actionPillImage.visibility = View.VISIBLE
        binding.actionPillImage.setImageDrawable(context, ContextCompat.getDrawable(context!!, icon))
        binding.actionPillImage.contentDescription = action +", "+ context?.getString(R.string.content_description_current_and_Late_Reminder_image)
        binding.actionImage.visibility = View.GONE
        binding.toBeTakenCheckmark.visibility = View.GONE
    }
    private fun showBottomSheet(drug: Drug) {
        val args = Bundle()
        args.putString("mPillId", drug.guid)
        args.putBoolean("isLateReminder", true)
        val bottomSheet = ActionBottomDialogFragment.newInstance()
        bottomSheet.arguments = args
        bottomSheet.show((context as FragmentActivity).supportFragmentManager, bottomSheet.tag)

    }

    inner class ViewHolder(viewBinding: ViewDataBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(data: Drug) {
            binding.setVariable(BR.drug, data)
            binding.robotoMedium = robotoMedium
            binding.robotoRegular = robotoRegular
            binding.setVariable(BR.viewModel, viewModelProvider)
            binding.executePendingBindings()
        }
    }

     fun onSkip() {
        viewModelProvider.onMedSkippedClicked(drugList[drugPosition],drugPosition)    }

}