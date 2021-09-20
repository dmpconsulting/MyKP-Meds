package com.montunosoftware.pillpopper.kotlin.quickview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.CurrentReminderDetailViewRedesignBinding
import com.montunosoftware.mymeds.databinding.EmptyLayoutBinding
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.GreatJobAlertForTakenAllActivity
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog
import com.montunosoftware.pillpopper.android.view.DialogHelpers
import com.montunosoftware.pillpopper.android.view.ReminderSnoozePicker
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.kotlin.lateremider.LateRemindersActivity
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.model.PillpopperTime
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import java.util.*

class ReminderOverDueAdapter(drugList: List<Drug>, context: PillpopperActivity?, ctx: SingleCurrentReminder?, allDrugs: List<Drug>, val currentReminderViewModel: CurrentReminderActivityViewModel) : RecyclerView.Adapter<ReminderOverDueAdapter.ViewHolder>() {

    private  var reminderSnoozePicker: ReminderSnoozePicker? = null
    private lateinit var binding: CurrentReminderDetailViewRedesignBinding
    private var context: PillpopperActivity? = null
    private var mListFromMap: List<Drug>? = ArrayList()
    private val singleActionList: MutableList<Drug> = ArrayList()
    private var allDrugs: List<Drug> = ArrayList()
    private var mSingleCurrentReminderContext: SingleCurrentReminder? = null
    private var mDrugsWithNoActionPerformed: MutableList<Drug>? = ArrayList()
    private val drug = 1
    private val view = 0
    private var drugPosition = 0

    private var mFontMedium: Typeface
    private var mFontRegular: Typeface

    fun dismissDialog(){
        if(null != reminderSnoozePicker && reminderSnoozePicker!!.isVisible){
            reminderSnoozePicker!!.dismiss()
        }
    }
    init {
        this.context = context
        singleActionList.clear()
        mListFromMap = drugList
        for (drug in drugList) {
            if (drug.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                singleActionList.add(drug)
            }
        }
        this.allDrugs = allDrugs
        mSingleCurrentReminderContext = ctx
        mDrugsWithNoActionPerformed!!.clear()
        mDrugsWithNoActionPerformed = PillpopperRunTime.getInstance().drugsToBeTaken

        // initialize fonts
        mFontMedium = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_MEDIUM)
        mFontRegular = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_REGULAR)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == drug) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.current_reminder_detail_view_redesign, parent, false)
            binding.currentReminderViewModel = currentReminderViewModel
            binding.robotoMedium = mFontMedium
            binding.robotoRegular = mFontRegular
            ViewHolder(binding.root)
        } else {
            val emptyLayoutBinding: EmptyLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.empty_layout, parent, false)
            ViewHolder(emptyLayoutBinding.root)
        }
    }

    fun getItem(position: Int): Any {
        return mListFromMap!![position]
    }

    override fun getItemCount(): Int {
        return mListFromMap!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mListFromMap!![position].guid != null) drug else view
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (null != mListFromMap && position != mListFromMap!!.lastIndex) {
            val drug = mListFromMap!![position]
            binding.drugNameText.text = drug.firstName
            binding.childDrug = drug
            if (!Util.isEmptyString(drug.dose)) {
                binding.doseStrengthAndBrandnameText.visibility = View.VISIBLE
                binding.doseStrengthAndBrandnameText.text = if (Util.isEmptyString(drug.genericName)) drug.dose else drug.genericName + "  " + drug.dose
            } else {
                if (!Util.isEmptyString(drug.genericName)) {
                    binding.doseStrengthAndBrandnameText.visibility = View.VISIBLE
                    binding.doseStrengthAndBrandnameText.text = drug.genericName
                } else {
                    binding.doseStrengthAndBrandnameText.visibility = View.GONE
                }
            }

            // causes blink issue, if written in xml file
            binding.seeNotesButton.visibility = if (Util.isEmptyString(drug.notes)) View.GONE else View.VISIBLE

            ImageUILoaderManager.getInstance().loadDrugImage(context,drug.imageGuid, drug.guid, binding.pillImage, context?.let { ContextCompat.getDrawable(it, R.drawable.pill_default) })
            holder.setIsRecyclable(false)
            binding.drugDetailsLayout.setOnClickListener {
                //launch read only screen
                val intent = Intent(context, ReminderSingleMedDetailActivity::class.java)
                intent.putExtra("pill_id", drug.guid)
                intent.putExtra("drug_guid", drug.imageGuid)
                context?.startActivity(intent)
            }

            when (drug.getmAction()) {

                PillpopperConstants.TAKEN, PillpopperConstants.TAKE_EARLIER -> context?.getString(R.string.taken)?.let { addActionImage(context, R.drawable.ic_taken, it) }
                PillpopperConstants.SKIPPED -> context?.getString(R.string.skipped)?.let { addActionImage(context, R.drawable.ic_skipped, it) }
                PillpopperConstants.TAKE_LATER -> context?.getString(R.string.remind_later)?.let { addActionImage(context, R.drawable.ic_postpone, it) }
                else -> {
                    if(context!=null){
                        ImageUILoaderManager.getInstance().loadDrugImage(context, drug.imageGuid, drug.guid, binding.pillImage, Util.getDrawableWrapper(context, R.drawable.pill_default))
                    }
                    if (null != drug.imageGuid) {
                        binding.pillImage.contentDescription = context?.getString(R.string.content_description_current_and_Late_Reminder_image)
                    } else {
                        binding.pillImage.contentDescription = context?.getString(R.string.tap_to_enlarge_default_pill_image)
                    }
                    binding.actionPillImage.visibility = View.GONE
                }
            }
        }
    }

    private fun addActionImage(context: Context?, actionImage: Int, action: String) {
        binding.actionPillImage.visibility = View.VISIBLE
        binding.actionPillImage.setImageDrawable(context, ContextCompat.getDrawable(context!!, actionImage))
        binding.actionPillImage.contentDescription = action +", "+ context.getString(R.string.content_description_current_and_Late_Reminder_image)
        binding.actionImage.visibility = View.GONE
        binding.toBeTakenCheckmark.visibility = View.GONE
    }

    val singleActionDrugList: List<Drug>
        get() = singleActionList

    private val takeDrugs: MutableList<Drug> = ArrayList()

    inner class ViewHolder(private val parentView: View) : RecyclerView.ViewHolder(parentView) {
        init {
            binding.toBeTakenCheckmark.setOnClickListener {
                val drug = getItem(if (adapterPosition == mListFromMap!!.lastIndex) adapterPosition - 1 else adapterPosition) as Drug
                takeDrugs.clear()
                takeDrugs.add(drug)
                AppConstants.MEDS_TAKEN_OR_SKIPPED = true
                AppConstants.MEDS_TAKEN_OR_POSTPONED = true
                if (AppConstants.isByPassLogin()) {
                    FrontController.getInstance(context).performTakeDrug(takeDrugs, null, context, true, FireBaseConstants.ParamValue.QUICKVIEW)
                    StateDownloadIntentService.startActionNonSecureIntermediateGetState(context)
                } else {
                    FrontController.getInstance(context).performTakeDrug(takeDrugs, null, context, true, FireBaseConstants.ParamValue.QUICKVIEW)
                }
                drug.setmAction(PillpopperConstants.TAKEN)
                singleActionList.remove(drug)
                mDrugsWithNoActionPerformed!!.remove(drug)
                mSingleCurrentReminderContext!!.refreshAdapter(isExitRequired(singleActionList))
                // checking whether all the drugs listed in the current reminder screen has been acted.
                // if all the drugs are acted then isExitRequired returns true else false.
                /*if (isExitRequired(singleActionList)) {
                    if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId &&
                            PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()
                            && checkForNonActedDrugs(allDrugs)) {
                        showLateRemindersOrRefreshCurrent()
                    } else if (AppConstants.isByPassLogin()) {
                        refreshReminder()
                    } else {
                        PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                        val intent = Intent()
                        context!!.setResult(Activity.RESULT_CANCELED, intent)
                        context!!.finish()
                    }
                } */
            }
            binding.actionImage.setOnClickListener { parentView ->
                val drug = getItem(if (adapterPosition == mListFromMap!!.lastIndex) adapterPosition - 1 else adapterPosition) as Drug
                drugPosition = adapterPosition
                showBottomSheet(drug)

            }
        }

    }

    private fun showLateRemindersOrRefreshCurrent() {
        if (Util.canShowLateReminder(context)) {
            context!!.startActivity(Intent(context, LateRemindersActivity::class.java))
            val intent = Intent()
            context!!.setResult(Activity.RESULT_CANCELED, intent)
            context!!.finish()
        } else if (!AppConstants.isByPassLogin()) {
            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            FrontController.getInstance(context).updateAsPendingRemindersPresent(context)
            context!!.finish()
        } else {
            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            context!!.finish()
        }
    }

    private fun checkForNonActedDrugs(allDrugs: List<Drug>): Boolean {
        for (d in allDrugs) {
            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                return false
            }
        }
        return true
    }

    private val drugsCountWithNoActionTaken: Int
        get() = if (null != mDrugsWithNoActionPerformed && mDrugsWithNoActionPerformed!!.size == 1) {
            1
        } else 0

    fun isExitRequired(allDrugs: List<Drug>): Boolean {
        return allDrugs.isEmpty()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun refreshReminder() {
        PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
        mSingleCurrentReminderContext!!.finish()
    }

    private fun showBottomSheet(drug: Drug) {
        var lateCurrentReminder = false
        if (Calendar.getInstance().timeInMillis - PillpopperRunTime.getInstance().headerTime > PillpopperConstants.LATE_REMINDER_INTERVAL) {
            //the value is changed to 10minutes for regression later will be reverted to 1hr
            lateCurrentReminder = true
        }
        val args = Bundle()
        args.putString("mPillId", drug.guid)
        args.putBoolean("isLateCurrentReminder", lateCurrentReminder)
        val bottomSheet = ActionBottomDialogFragment.newInstance()
        bottomSheet.arguments = args
        bottomSheet.show((context as FragmentActivity).supportFragmentManager, bottomSheet.tag)

    }

    fun onSkip() {
        val intent = Intent(context, ReminderAlertActivity::class.java)
        intent.putExtra(AppConstants.LAUNCH_MODE, context?.resources?.getString(R.string.skipped))
        intent.putExtra(AppConstants.ACTION_TITLE, context?.resources?.getString(R.string.skipped))
        intent.putExtra(AppConstants.ACTION_MESSAGE, context?.resources?.getString(R.string.skip_alert_message))
        context?.startActivityForResult(intent, 200)
    }

    fun onReminderLater() {
        val drug = getItem(if (drugPosition == mListFromMap!!.lastIndex) drugPosition - 1 else drugPosition) as Drug
        val manager = context!!.supportFragmentManager
        val frag = manager.findFragmentByTag("fragment_name")
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit()
        }
        reminderSnoozePicker = ReminderSnoozePicker()
        reminderSnoozePicker!!.setHourMinutePickedListener(ReminderSnoozePicker.HourMinutePickedListener { hm ->
            if (hm == null) {
                return@HourMinutePickedListener
            }
            val postponeTimeSeconds = (hm[0] * 60 + hm[1]) * 60
            val listForValidation: MutableList<Drug> = ArrayList()
            listForValidation.add(drug)
            val postponeError = Drug.validatePostpones(listForValidation, postponeTimeSeconds.toLong(), context)
            if (postponeError != null) {
                DialogHelpers.showPostponeErrorAlert(context)
            } else {
                singleActionList.remove(drug)
                takeDrugs.add(drug)
                AppConstants.SHOW_SAVED_ALERT = true
                AppConstants.MEDS_TAKEN_OR_POSTPONED = true
                drug.setmAction(PillpopperConstants.TAKE_LATER)
                if (AppConstants.isByPassLogin()) {
                    FrontController.getInstance(context).performPostponeDrugs(listForValidation, postponeTimeSeconds.toLong(), context, true, FireBaseConstants.ParamValue.QUICKVIEW)
                    StateDownloadIntentService.startActionNonSecureIntermediateGetState(context)
                } else {
                    FrontController.getInstance(context).performPostponeDrugs(listForValidation, postponeTimeSeconds.toLong(), context, true, FireBaseConstants.ParamValue.QUICKVIEW)
                }
                mSingleCurrentReminderContext!!.refreshAdapter(isExitRequired(singleActionList))
               /* if (isExitRequired(singleActionList)) {
                    if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId &&
                            PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()
                            && checkForNonActedDrugs(allDrugs)) {
                        showLateRemindersOrRefreshCurrent()
                    } else if (AppConstants.isByPassLogin()) {
                        refreshReminder()
                    } else {
                        PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                        val intent = Intent()
                        context!!.setResult(Activity.RESULT_CANCELED, intent)
                        context!!.finish()
                    }
                } */
            }
        })
        reminderSnoozePicker!!.show(manager, "fragment_name")
    }

    fun onTakenEarlier() {
        val drug = getItem(if (drugPosition == mListFromMap!!.lastIndex) drugPosition - 1 else drugPosition) as Drug
        val dateAndTimePickerDialog = DateAndTimePickerDialog(
                context,
                DateAndTimePickerDialog.OnDateAndTimeSetListener { pillPopperTime ->
                    takeDrugs.clear()
                    takeDrugs.add(drug)
                    singleActionList.remove(drug)
                    FrontController.getInstance(context).performAlreadyTakenDrugs(takeDrugs, pillPopperTime, context, true, FireBaseConstants.ParamValue.QUICKVIEW)
                    drug.setmAction(PillpopperConstants.TAKE_EARLIER)
                    AppConstants.MEDS_TAKEN_OR_SKIPPED = true
                    AppConstants.MEDS_TAKEN_OR_POSTPONED = true
                    if (drugsCountWithNoActionTaken == 1) {
                        mSingleCurrentReminderContext!!.startActivityForResult(Intent(mSingleCurrentReminderContext, GreatJobAlertForTakenAllActivity::class.java), 0)
                        mSingleCurrentReminderContext!!.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        if (AppConstants.isByPassLogin()) {
                            StateDownloadIntentService.startActionNonSecureIntermediateGetState(context)
                        }
                        mSingleCurrentReminderContext!!.refreshAdapter(isExitRequired(singleActionList))
                       /* if (singleActionList.isEmpty()) {
                            context!!.finish()
                        } */
                      /*  if (isExitRequired(singleActionList)) {
                            if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId &&
                                    PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()
                                    && checkForNonActedDrugs(allDrugs)) {
                                showLateRemindersOrRefreshCurrent()
                            } else if (AppConstants.isByPassLogin()) {
                                refreshReminder()
                            } else {
                                PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                                val intent = Intent()
                                context!!.setResult(Activity.RESULT_CANCELED, intent)
                                context!!.finish()
                            }
                        }*/
                    }
                },
                false,
                PillpopperTime.now(),
                15, "Taken", true
        )
        dateAndTimePickerDialog.show(context!!.supportFragmentManager, "taken_earlier")
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        PillpopperLog.say("MyAdapter", "onActivityResult")
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                200 -> {
                    val drug = getItem(if (drugPosition == mListFromMap!!.lastIndex) drugPosition - 1 else drugPosition) as Drug
                    takeDrugs.clear()
                    takeDrugs.add(drug)
                    if (AppConstants.isByPassLogin()) {
                        FrontController.getInstance(context).performSkipDrug(takeDrugs, PillpopperTime.now(), context, true, FireBaseConstants.ParamValue.QUICKVIEW)
                        StateDownloadIntentService.startActionNonSecureIntermediateGetState(context)
                    } else {
                        FrontController.getInstance(context).performSkipDrug(takeDrugs, PillpopperTime.now(), context, true, FireBaseConstants.ParamValue.QUICKVIEW)
                    }
                    drug.setmAction(PillpopperConstants.SKIPPED)
                    singleActionList.remove(drug)
                    mDrugsWithNoActionPerformed!!.remove(drug)
                    mSingleCurrentReminderContext!!.refreshAdapter(isExitRequired(singleActionList))
                    /*if (isExitRequired(singleActionList)) {
                        if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId &&
                                PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()
                                && checkForNonActedDrugs(allDrugs)) {
                            showLateRemindersOrRefreshCurrent()
                        } else if (AppConstants.isByPassLogin()) {
                            refreshReminder()
                        } else {
                            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                            val intent = Intent()
                            context!!.setResult(Activity.RESULT_CANCELED, intent)
                            context!!.finish()
                        }
                    } */
                }
            }
        }

    }
}
