package com.montunosoftware.pillpopper.kotlin.quickview

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.CurrentReminderDetailRedesignBinding
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.ReminderContainerActivity
import com.montunosoftware.pillpopper.android.RunTimeConstants
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
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.AppConstants.SAVED_ALERT_REQUEST_CODE
import org.kp.tpmg.mykpmeds.activation.PopUpListener
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils
import java.util.*
import kotlin.collections.ArrayList

class SingleCurrentReminder : ReminderContainerActivity(), PopUpListener,ActionBottomDialogFragment.ItemClickListener {
    private  var overDueAdapter: ReminderOverDueAdapter? = null
    private lateinit var currentReminderViewModel: CurrentReminderActivityViewModel
    private lateinit var binding: CurrentReminderDetailRedesignBinding
    private var drugList: MutableList<Drug>? = ArrayList()
    private var mSingleTimeHashMap: LinkedHashMap<String, List<Drug>>? = LinkedHashMap()
    private var takenEarlierTime: Long = 0
    private val allDrugs: MutableList<Drug> = ArrayList()
    private var finalDrugs: MutableList<Drug>? = ArrayList()
    private var isLastCurrentUser = false
    private var reminderPopUpShown = false
    private var reminderPopupMenu: PopupMenu? = null
    private var singleMed = false
    private val skipRequestCode = 100
    private val reminderSnoozePicker = ReminderSnoozePicker()

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding = DataBindingUtil.setContentView(this, R.layout.current_reminder_detail_redesign)
        currentReminderViewModel = ViewModelProvider(this).get(CurrentReminderActivityViewModel::class.java)
        binding.activityContext = this
        binding.patientName.text = PillpopperRunTime.getInstance().proxyName
        binding.reminderScreenToolbarForDetailScreen.backNavigation.visibility = View.VISIBLE
        mSingleTimeHashMap = PillpopperRunTime.getInstance().drugsHashMap
        if (null != mSingleTimeHashMap && mSingleTimeHashMap!!.isNotEmpty()) {
            val keySet: List<String> = java.util.ArrayList(mSingleTimeHashMap!!.keys)
            for (s in keySet) {
                mSingleTimeHashMap!![s]?.let { allDrugs.addAll(it) }
            }
        }
        val intent = intent
        if (null != intent) {
            isLastCurrentUser = intent.getBooleanExtra("isLastCurrentUser", false)
        }
        drugList?.addAll( PillpopperRunTime.getInstance().proxyDrugs)
        val drug = Drug()
        drug.setmAction(-1)
        drugList!!.add(drug)
        initValues()
        setObserver()
    }

    private fun setObserver() {
        currentReminderViewModel.onDrugImageClicked.observe(this, Observer {
            if(null != it) {
                val expandImageIntent = Intent(_thisActivity, EnlargeImageActivity::class.java)
                expandImageIntent.putExtra("pillId", it.guid)
                expandImageIntent.putExtra("imageId", it.imageGuid)
                expandImageIntent.putExtra("isFromReminderDrugDetailActivity", true)
                startActivity(expandImageIntent)
                currentReminderViewModel.onDrugImageClicked.postValue(null)
            }
        })
        currentReminderViewModel.onPersonalNotesClicked.observe(this, Observer {
            //launch read only screen
            intent = Intent(this, ReminderSingleMedDetailActivity::class.java)
            intent.putExtra("pill_id", it.guid)
            intent.putExtra("drug_guid", it.imageGuid)
            intent.putExtra("launchMode", "CurrentReminder")
            startActivity(intent)
        })
    }

    fun outsideLayoutClicked(){
        // To prevent background item click with gradient effect
    }

    private fun initValues() {
        val mFontMedium = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_MEDIUM)
        val mFontRegular = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_REGULAR)
        val mFontBold = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_BOLD)

        binding.reminderScreenToolbarForDetailScreen.medicationRemainder.typeface = mFontRegular
        binding.reminderScreenToolbarForDetailScreen.signInBtn.typeface = mFontRegular
        binding.reminderScreenToolbarForDetailScreen.reminderOverDueDateTime.typeface = mFontBold
        binding.robotoMedium = mFontMedium
        binding.robotoBold = mFontBold

        binding.reminderScreenToolbarForDetailScreen.backNavigation.setOnClickListener { onBackNavigationClicked() }
        binding.reminderScreenToolbarForDetailScreen.signIn.setOnClickListener { onSingInClicked() }
    }

    override fun onResume() {
        super.onResume()
        ActivationController.getInstance().stopTimer(this)
        if (reminderPopUpShown && reminderPopupMenu != null) {
            reminderPopupMenu!!.dismiss()
            reminderPopUpShown = false
        }
        RunTimeConstants.getInstance().isNotificationSuppressor = false
        PillpopperConstants.setIsRemindersBeingShown(true)
        binding.reminderScreenToolbarForDetailScreen.signIn.visibility = if (AppConstants.isByPassLogin()) View.VISIBLE else View.GONE
        overDueAdapter = ReminderOverDueAdapter(drugList!!, _thisActivity, this, allDrugs, currentReminderViewModel)
        binding.adapter = overDueAdapter
        setFooterButtons()
       /* if (isExitRequired) {
            _thisActivity!!.finish()
        } */
    }

    private fun setFooterButtons() {
        try {
            if (drugList!![0].overdueDate != null) {
                binding.reminderScreenToolbarForDetailScreen.reminderOverDueDateTime.text = Util.getFormattedString(drugList!![0])
                if (PillpopperRunTime.getInstance().headerTime != null) {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = PillpopperRunTime.getInstance().headerTime
                    if (Calendar.getInstance().time.after(cal.time)) {
                        if (Calendar.getInstance().timeInMillis - PillpopperRunTime.getInstance().headerTime > PillpopperConstants.LATE_REMINDER_INTERVAL) {
                            //the value is changed to 10minutes for regression later will be reverted to 1hr
                            binding.takeAllLater.visibility = View.GONE
                            binding.takenAllEarlier.visibility = View.VISIBLE
                            //Checking for size 2 because we are adding a empty drug object
                            if (drugList!!.size == 2) {
                                singleMed = true
                                binding.takenAllEarlierBtn.text = resources.getString(R.string.reminder_taken_earlier)
                            } else {
                                binding.takenAllEarlierBtn.text = resources.getString(R.string.reminder_taken_all_earlier)
                            }
                        } else {
                            singleMed = drugList!!.isNotEmpty() && drugList!!.size == 2
                            binding.takenAllEarlier.visibility = View.GONE
                            binding.takeAllLater.visibility = View.VISIBLE
                        }
                    }
                }
            }
        } catch (e: Exception) {
            PillpopperLog.say("Exception Occurs", e)
        }
        if (singleMed) {
            binding.takenAllBtn.text = resources.getString(R.string.take)
            binding.skipAllBtn.text = resources.getString(R.string.skipped)
        } else {
            binding.takenAllBtn.text = resources.getString(R.string.reminder_taken_all)
            binding.skipAllBtn.text = resources.getString(R.string.reminder_skip_all)
            changeBtnTextIfAnyActionTaken()
        }
    }
    fun changeBtnTextIfAnyActionTaken() {
        if (isActionTakenDrugAvailable()) {
            binding.skipAllBtn.text = resources.getString(R.string.skip_the_rest_btn)
            binding.takenAllBtn.text = resources.getString(R.string.take_the_rest_btn)
            binding.takenAllEarlierBtn.text = resources.getString(R.string.taken_the_rest_earlier_btn)
        }
    }
   private fun isActionTakenDrugAvailable():Boolean {
        if (drugList?.isNotEmpty() == true) {
            for (d in drugList!!) {
                if (null != d.guid && d.getmAction() != PillpopperConstants.NO_ACTION_TAKEN) {
                    return true
                }
            }
        }
        return false
    }
    private fun getActionType():String {
        if (drugList?.isNotEmpty() == true) {
            return Util.getActionType(drugList as ArrayList<Drug>?,this)
        }
        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                skipRequestCode -> {
                    finalDrugs = ArrayList()
                    for (d in overDueAdapter!!.singleActionDrugList) {
                        if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                            finalDrugs?.add(d)
                            allDrugs.remove(d)
                        }
                    }
                    finalDrugs?.let { updateDrugsAsSkipped(it) }
                    AppConstants.SHOW_SAVED_ALERT = true
                    AppConstants.MEDS_TAKEN_OR_SKIPPED = true
                    if (AppConstants.isByPassLogin()) {
                        FrontController.getInstance(_thisActivity).performSkipDrugForQuickView(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.skipAllBtn.text.toString())
                        StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
                    } else {
                        FrontController.getInstance(_thisActivity).performSkipDrugForQuickView(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.skipAllBtn.text.toString())
                    }
                    if (isExitRequired) {
                        if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId &&
                                PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()) {
                            showLateRemindersOrRefreshCurrent()
                        } else if (AppConstants.isByPassLogin()) {
                            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                            if(AppConstants.MEDS_TAKEN_OR_POSTPONED) {
                                showSavedResponseAlert()
                            }else{
                                ActivationController.getInstance().performSignoff(_thisActivity)
                            }
                        }  else {
                            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                            if(AppConstants.MEDS_TAKEN_OR_POSTPONED) {
                                showSavedResponseAlert()
                            }else{
                                _thisActivity.finish()
                            }
                        }
                    } else {
                        _thisActivity.finish()
                    }
                }
                SAVED_ALERT_REQUEST_CODE -> {
                    LoggerUtils.info("Alerts - calling performSignOff 555")
                    ActivationController.getInstance().performSignoff(_thisActivity)
                }
                200 -> {
                    AppConstants.SHOW_SAVED_ALERT = true
                    AppConstants.MEDS_TAKEN_OR_SKIPPED = true
                    overDueAdapter!!.onActivityResult(requestCode, resultCode, data)
                }
                else -> {
                    takenEarlierTime = data!!.getLongExtra("TakenEarlierTime", -1)
                    continueTakeDrugProcess(requestCode)
                }
            }
        }
    }

    private fun continueTakeDrugProcess(requestCode: Int) {
        val finalDrugs: MutableList<Drug> = java.util.ArrayList()
        //          after taking action on the list of drugs, updating the drug flag as taken
        for (d in overDueAdapter!!.singleActionDrugList) {
            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                d.setmAction(PillpopperConstants.TAKEN)
                finalDrugs.add(d)
            }
        }
        if (finalDrugs.isNotEmpty()) {
            if (AppConstants.isByPassLogin()) {
                if (requestCode == 101) {
                    FrontController.getInstance(_thisActivity).performAlreadyTakenDrugsForQuickView(finalDrugs, PillpopperTime(takenEarlierTime), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllEarlierBtn.text.toString())
                } else {
                    FrontController.getInstance(_thisActivity).performTakeDrugForQuickView(finalDrugs, null, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllBtn.text.toString())
                }
                StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
            } else {
                if (requestCode == 101) {
                    FrontController.getInstance(_thisActivity).performAlreadyTakenDrugsForQuickView(finalDrugs, PillpopperTime(takenEarlierTime), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllEarlierBtn.text.toString())
                } else {
                    FrontController.getInstance(_thisActivity).performTakeDrugForQuickView(finalDrugs, null, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllBtn.text.toString())
                }
            }
        }
        if (isExitRequired) {
            if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId &&
                    PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()) {
                showLateRemindersOrRefreshCurrent()
            } else if (AppConstants.isByPassLogin()) {
                ActivationController.getInstance().performSignoff(_thisActivity)
                PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            } else {
                PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                _thisActivity.finish()
            }
        } else {
            _thisActivity.finish()
        }
    }

    private fun showLateRemindersOrRefreshCurrent() {
        if (Util.canShowLateReminder(_thisActivity)) {
            intent = Intent(_thisActivity, LateRemindersActivity::class.java)
            intent.putExtra("currentRemindersActionType", getActionType())
            startActivity(intent)
            _thisActivity.finish()
        } else if (!AppConstants.isByPassLogin()) {
            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            FrontController.getInstance(_thisActivity).updateAsPendingRemindersPresent(_thisActivity)
            _thisActivity.finish()
        } else {
            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            _thisActivity.finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if(null != overDueAdapter) {
            overDueAdapter!!.dismissDialog()
        }
        RunTimeConstants.getInstance().isNotificationSuppressor = true
        PillpopperConstants.setIsRemindersBeingShown(false)
        binding.reminderScreenToolbarForDetailScreen.signIn.visibility = if (AppConstants.isByPassLogin()) View.VISIBLE else View.GONE
        if(reminderSnoozePicker.isVisible){
            reminderSnoozePicker.dismiss()
        }
    }

    override fun onPopUpShown(popupMenu: PopupMenu) {
        reminderPopUpShown = true
        reminderPopupMenu = popupMenu
    }

    override fun onPopUpDismissed() {
        reminderPopUpShown = false
        reminderPopupMenu = null
    }

    fun onSkippedAllClicked() {
        val finalDrugs = ArrayList<Drug>()
        for (d in overDueAdapter!!.singleActionDrugList) {
            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                finalDrugs.add(d)
            }
        }
        intent = Intent(this, ReminderAlertActivity::class.java)
        intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.skipped))
        intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.skipped))
        val skipMessage: String = if (finalDrugs.size == 1) {
            resources.getString(R.string.skip_alert_message)
        } else {
            resources.getString(R.string.skipall_alert_message)
        }
        intent.putExtra(AppConstants.ACTION_MESSAGE,  skipMessage)
        startActivityForResult(intent, skipRequestCode)
    }

    private fun updateDrugsAsSkipped(drugList: List<Drug>) {
        for (d in drugList) {
            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                d.setmAction(PillpopperConstants.SKIPPED)
                allDrugs.remove(d)
            }
        }
    }

    fun onTakenAllClicked() {
        AppConstants.MEDS_TAKEN_OR_SKIPPED = true
        AppConstants.MEDS_TAKEN_OR_POSTPONED = true
        if (isLastCurrentUser) {
            if (null == PillpopperRunTime.getInstance().passedReminderersHashMapByUserId ||
                    PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isEmpty()) {
                val intent = Intent(this, ReminderAlertActivity::class.java)
                if (AppConstants.SHOW_SAVED_ALERT) {
                    intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
                    intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
                    intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
                } else {
                    intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.great_job_title))
                    intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.action_taken_msg))
                }
                startActivityForResult(intent, 0)
            } else {
                continueTakeDrugProcess(102)
            }
        } else {
            continueTakeDrugProcess(102)
        }
    }

    fun onTakeAllLaterClicked() {
        val manager = supportFragmentManager
        val frag = manager.findFragmentByTag("fragment_edit_name")
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit()
        }

        reminderSnoozePicker.setHourMinutePickedListener(ReminderSnoozePicker.HourMinutePickedListener { hm ->
            if (hm == null) {
                return@HourMinutePickedListener
            }
            finalDrugs = ArrayList()
            val postponeTimeSeconds = (hm[0] * 60 + hm[1]) * 60.toLong()
            val drugsForPostponeValidation: MutableList<Drug> = java.util.ArrayList()
            for (d in overDueAdapter!!.singleActionDrugList) {
                if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                    drugsForPostponeValidation.add(d)
                }
            }
            val postponeError = Drug.validatePostpones(drugsForPostponeValidation, postponeTimeSeconds,
                    _thisActivity)
            if (postponeError != null) {
                DialogHelpers.showPostponeErrorAlert(_thisActivity)
            } else {
                if (drugsForPostponeValidation.isNotEmpty()) {
                    for (drug in drugsForPostponeValidation) {
                        drug.setmAction(PillpopperConstants.TAKE_LATER)
                        finalDrugs?.add(drug)
                        allDrugs.remove(drug)
                    }
                }
                if (AppConstants.isByPassLogin()) {
                    FrontController.getInstance(_thisActivity).performPostponeDrugs(finalDrugs, postponeTimeSeconds, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW)
                    StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
                } else {
                    FrontController.getInstance(_thisActivity).performPostponeDrugs(finalDrugs, postponeTimeSeconds, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW)
                }
                AppConstants.SHOW_SAVED_ALERT = true
                AppConstants.MEDS_TAKEN_OR_POSTPONED = true
                if (isExitRequired) {
                    if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId &&
                            PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()) {
                        showLateRemindersOrRefreshCurrent()
                    } else if (AppConstants.isByPassLogin()) {
                        PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                        if (AppConstants.MEDS_TAKEN_OR_SKIPPED) {
                            showSavedResponseAlert()
                        } else {
                            ActivationController.getInstance().performSignoff(_thisActivity)
                        }
                    } else {
                        PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                        if (AppConstants.MEDS_TAKEN_OR_SKIPPED) {
                            showSavedResponseAlert()
                        }else{
                            _thisActivity.finish()
                        }
                    }
                } else {
                    _thisActivity.finish()
                }
            }
            PillpopperConstants.setIsRemindersDisplying(false)
        })
        reminderSnoozePicker.show(manager, "fragment_edit_name")
    }

    fun onTakenEarlierClicked() {
        val dateAndTimePickerDialog = DateAndTimePickerDialog(
                this,
                DateAndTimePickerDialog.OnDateAndTimeSetListener { pillPopperTime ->
                    for (d in drugList!!) {
                        if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                            finalDrugs?.add(d)
                            allDrugs.remove(d)
                        }
                    }
                    AppConstants.MEDS_TAKEN_OR_SKIPPED = true
                    AppConstants.MEDS_TAKEN_OR_POSTPONED = true
                    takenEarlierTime = pillPopperTime.gmtSeconds
                    if (isLastCurrentUser) {
                        if (null == PillpopperRunTime.getInstance().passedReminderersHashMapByUserId ||
                                PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isEmpty()) {
                            val intent = Intent(this, ReminderAlertActivity::class.java)
                            intent.putExtra("TakenEarlier", pillPopperTime.gmtSeconds)
                            if(AppConstants.SHOW_SAVED_ALERT) {
                                intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
                                intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
                                intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
                            }else{
                                intent.putExtra(AppConstants.LAUNCH_MODE, "LateReminders")
                                intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.great_job_title))
                                intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.action_taken_msg))
                            }
                            startActivityForResult(intent, 101)
                        } else {
                            continueTakeDrugProcess(101)
                        }
                    } else {
                        continueTakeDrugProcess(101)
                    }
                },
                false,
                PillpopperTime.now(),
                15, "Taken", true
        )
        dateAndTimePickerDialog.show(supportFragmentManager, "taken_at_dialog")
    }

    fun onBackNavigationClicked() {
        _thisActivity.finish()
    }

    fun onSingInClicked() {
        ActivationController.getInstance().performSignoff(_thisActivity)
    }

    fun refreshAdapter(exitRequired : Boolean) {
        overDueAdapter!!.notifyDataSetChanged()
        changeBtnTextIfAnyActionTaken()
        if(exitRequired) {
            if (isExitRequired) {
                if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId &&
                        PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()) {
                    showLateRemindersOrRefreshCurrent()
                } else if (AppConstants.isByPassLogin()) {
                    PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                    if (AppConstants.SHOW_SAVED_ALERT) {
                        if (AppConstants.MEDS_TAKEN_OR_SKIPPED && AppConstants.MEDS_TAKEN_OR_POSTPONED) {
                            showSavedResponseAlert()
                        } else {
                            ActivationController.getInstance().performSignoff(_thisActivity)
                        }
                    } else {
                        val intent = Intent(this, ReminderAlertActivity::class.java)
                        intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.great_job_title))
                        intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.action_taken_msg))
                        startActivityForResult(intent, SAVED_ALERT_REQUEST_CODE)
                    }
                } else {
                    PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                    _thisActivity.finish()
                }
            }else{
                PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                _thisActivity.finish()
            }
        }
    }
    private fun showSavedResponseAlert(){
        LoggerUtils.info("Alerts - in showSavedResponseAlert 352")
        if ((isLastCurrentUser) && (null == PillpopperRunTime.getInstance().passedReminderersHashMapByUserId || PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isEmpty())){
            val intent = Intent(this, ReminderAlertActivity::class.java)
            intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
            intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
            intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
            startActivityForResult(intent, SAVED_ALERT_REQUEST_CODE)
        }
    }

    val isExitRequired: Boolean
        get() {
            for (d in allDrugs) {
                if (null != d.guid && d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                    return false
                }
            }
            return true
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        currentReminderViewModel.onDrugImageClicked.postValue(null)
        if(reminderSnoozePicker.isVisible){
            reminderSnoozePicker.dismiss()
        }
    }

    override fun onSkip() {
        overDueAdapter!!.onSkip()
    }

    override fun onReminderLater() {
        overDueAdapter!!.onReminderLater()
    }

    override fun onTakenEarlier() {
        overDueAdapter!!.onTakenEarlier()
    }

}