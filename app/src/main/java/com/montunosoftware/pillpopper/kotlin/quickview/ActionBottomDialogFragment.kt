package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.ThreedotBottomActionSheetBinding
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.model.ArchiveDetailDrug
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil


class ActionBottomDialogFragment : BottomSheetDialogFragment() {
    private var mListener: ItemClickListener? = null
    private lateinit var binding: ThreedotBottomActionSheetBinding
    private lateinit var drug: ArchiveDetailDrug
    private lateinit var mContext: Context


    @Nullable
    override fun onCreateView(@NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
                              @Nullable savedInstanceState: Bundle?): View? {
        binding = ThreedotBottomActionSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mArgs = arguments
        val pillId = mArgs!!.getString("mPillId")
        val isPassedReminder = mArgs.getBoolean("isLateCurrentReminder")
        val isLateReminder = mArgs.getBoolean("isLateReminder")
        drug = FrontController.getInstance(activity).getArchivedDrugDetails(activity as PillpopperActivity?, pillId)
        binding.childDrug = drug
        val mFontMedium = ActivationUtil.setFontStyle(activity, AppConstants.FONT_ROBOTO_MEDIUM)
        val mFontRegular = ActivationUtil.setFontStyle(activity, AppConstants.FONT_ROBOTO_REGULAR)
        binding.robotoMedium = mFontMedium
        binding.robotoRegular = mFontRegular
        var genericName = ""
        if (null != drug.pillName) {
            var name = drug.brandName
            genericName = drug.genericName
            binding.drugNameText.text = if (name.length > 100)  name.substring(0, 100) + "..." else name
        }
        var dosageAndGenericNameText = ""
        if (!Util.isEmptyString(drug.dose)) {
            dosageAndGenericNameText = if (Util.isEmptyString(genericName)) {
                drug.dose
            } else {
                genericName + "  " + drug.dose
            }
        } else {
            if (!Util.isEmptyString(genericName)) {
                dosageAndGenericNameText = genericName
            }
        }
        if (dosageAndGenericNameText != null && dosageAndGenericNameText.length > 100) {
            dosageAndGenericNameText = dosageAndGenericNameText.substring(0, 100) + "..."
        }
        if(!Util.isEmptyString(dosageAndGenericNameText)){
            binding.doseStrengthAndBrandnameText.visibility = View.VISIBLE
            binding.doseStrengthAndBrandnameText.text = dosageAndGenericNameText
        }else{
            binding.doseStrengthAndBrandnameText.visibility = View.GONE
        }
        ImageUILoaderManager.getInstance().loadDrugImage(activity, drug.imageGuid, pillId, binding.pillDefault, activity?.getDrawable(R.drawable.pill_default))
        if (isPassedReminder) {
            binding.takenEarlierLayout.visibility = View.VISIBLE
            binding.reminderLaterLayout.visibility = View.GONE
        } else if (isLateReminder) {
            binding.takenEarlierLayout.visibility = View.GONE
            binding.reminderLaterLayout.visibility = View.GONE
        } else {
            binding.takenEarlierLayout.visibility = View.GONE
            binding.reminderLaterLayout.visibility = View.VISIBLE
        }
        initListeners()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as ItemClickListener
            mContext = context
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement ItemClickListener")
        }
    }

    private fun initListeners() {
        binding.skippedLayout.setOnClickListener {
            binding.skippedLayout.setBackgroundResource(R.drawable.cancel_background)
            ContextCompat.getColor(mContext ,R.color.kp_theme_blue).let { it1 -> binding.tvSkipped.setTextColor(it1) }
            binding.ivIconSkipped24dp.visibility = View.GONE
            binding.ivIconSkipped24dpBlue.visibility = View.VISIBLE
            mListener?.onSkip()
            Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
            }, 200)        }
        binding.takenEarlierLayout.setOnClickListener {
            binding.takenEarlierLayout.setBackgroundResource(R.drawable.cancel_background)
            ContextCompat.getColor(mContext, R.color.kp_theme_blue).let { it1 -> binding.tvTakenEarlier.setTextColor(it1) }
            binding.ivIcCheckmark.visibility = View.GONE
            binding.ivIcCheckmarkBlue.visibility = View.VISIBLE
            mListener?.onTakenEarlier()
            Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
            }, 200)        }
        binding.cancelLayout.setOnClickListener {
            binding.cancelLayout.setBackgroundResource(R.drawable.cancel_background)
            ContextCompat.getColor(mContext ,R.color.kp_theme_blue).let { it1 -> binding.tvCancel.setTextColor(it1) }
            binding.ivIcBaselineRemoveCircleOutline24.visibility = View.GONE
            binding.ivIcBaselineRemoveCircleOutline24Blue.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
            }, 200)        }
        binding.reminderLaterLayout.setOnClickListener {
            binding.reminderLaterLayout.setBackgroundResource(R.drawable.cancel_background)
            ContextCompat.getColor(mContext, R.color.kp_theme_blue).let { it1 -> binding.tvRemindLater.setTextColor(it1) }
            binding.ivIcIconRemindMeLater.visibility = View.GONE
            binding.ivIcIconRemindMeLaterBlue.visibility = View.VISIBLE
            mListener?.onReminderLater()
            Handler(Looper.getMainLooper()).postDelayed({
                dismiss()
            }, 200)
        }
    }

    interface ItemClickListener {
        fun onSkip()
        fun onReminderLater()
        fun onTakenEarlier()
    }

    companion object {
        fun newInstance(): ActionBottomDialogFragment {
            return ActionBottomDialogFragment()
        }
    }
}