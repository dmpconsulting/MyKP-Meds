package com.montunosoftware.pillpopper.kotlin.lateremider

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
import com.montunosoftware.mymeds.databinding.LateReminderDetailsBinding
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.ReminderContainerActivity
import com.montunosoftware.pillpopper.android.RunTimeConstants
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.kotlin.quickview.ActionBottomDialogFragment
import com.montunosoftware.pillpopper.kotlin.quickview.ReminderAlertActivity
import com.montunosoftware.pillpopper.kotlin.quickview.ReminderSingleMedDetailActivity
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.model.PillpopperTime
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.AppConstants.SAVED_ALERT_REQUEST_CODE
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class LateReminderDetail : ReminderContainerActivity(),ActionBottomDialogFragment.ItemClickListener {

    private lateinit var listAdapter: LateReminderDetailAdapter
    private var reminderTime: Long? = null
    private var isLastGroup: Boolean = false
    private lateinit var binding: LateReminderDetailsBinding
    private lateinit var viewModelProvider : LateReminderDetailViewModel
    private var drugList: MutableList<Drug> = ArrayList()
    private var singleMed: MutableList<Drug> = ArrayList()
    private  var popUpMenu : PopupMenu? = null
    private val skipRequestCode = 100
    private var currentRemindersActionType: String = ""

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding = DataBindingUtil.setContentView(this,R.layout.late_reminder_details)
        viewModelProvider = ViewModelProvider(this).get(LateReminderDetailViewModel::class.java)
        initValue()
        setObserver()
    }

    private fun updatePassedReminderTable(finalDrugs: List<Drug>) {
        for (drug in finalDrugs) {
            FrontController.getInstance(_thisActivity).removeActedPassedReminderFromReminderTable(drug.guid, drug.scheduledTime.gmtMilliseconds.toString(), _thisActivity)
        }
    }

    private fun setObserver() {

        viewModelProvider.onItemClicked.observe(this, Observer {
            if(null != it) {
                val intent = Intent(this, ReminderSingleMedDetailActivity::class.java)
                intent.putExtra("pill_id", it.guid)
                intent.putExtra("drug_guid", it.imageGuid)
                intent.putExtra("launchMode", resources.getString(R.string.late_reminder))
                this.startActivity(intent)
                viewModelProvider.onItemClicked.postValue(null)
            }
        })
        viewModelProvider.onImageClicked.observe(this, Observer {
            if(null != it){
                val expandImageIntent = Intent(this, EnlargeImageActivity::class.java)
                expandImageIntent.putExtra("pillId", it.guid)
                expandImageIntent.putExtra("imageId", it.imageGuid)
                expandImageIntent.putExtra("isFromReminderDrugDetailActivity", true)
                startActivity(expandImageIntent)
                viewModelProvider.onImageClicked.postValue(null)
            }
        })

        viewModelProvider.onMedTakenClicked.observe(this, androidx.lifecycle.Observer {
            if(null != it) {
                val drug = it
                val takeDrugs: MutableList<Drug> = ArrayList()
                takeDrugs.clear()
                takeDrugs.add(drug.first)
                for (i in drugList.indices) {
                    if (drugList[i].equals(drug.first)) {
                        drugList[i].setmAction(PillpopperConstants.TAKEN)
                    }
                }
                // updatePassedReminderTable(takeDrugs)
                when (AppConstants.isByPassLogin()) {
                    true -> {
                        FrontController.getInstance(_thisActivity).performTakeDrug_pastReminders(takeDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW)
                        StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
                    }
                    false -> FrontController.getInstance(_thisActivity).performTakeDrug_pastReminders(takeDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW)
                }

                listAdapter.notifyDataSetChanged()
                AppConstants.MEDS_TAKEN_OR_POSTPONED = true
                if (isExitRequired) {
                    PillpopperRunTime.getInstance().removalTime = drug.first.scheduledTime.gmtMilliseconds
                    if(isLastGroup){
                        if(AppConstants.SHOW_SAVED_ALERT){
                            val intent = Intent(this, ReminderAlertActivity::class.java)
                            intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
                            intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
                            intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
                            startActivityForResult(intent, SAVED_ALERT_REQUEST_CODE)
                        }else {
                            showResponseAlertForTakenAction()
                        }
                    }else{
                        this.finish()
                    }
                }
                changeBtnTextIfAnyActionTaken()
                viewModelProvider.onMedTakenClicked.postValue(null)
            }
        })
        viewModelProvider.onMedSkippedClicked.observe(this, androidx.lifecycle.Observer {

            if (null != it) {
                val drug = it
                val takeDrugs: MutableList<Drug> = ArrayList()
                takeDrugs.clear()
                takeDrugs.add(drug.first)
                for (i in drugList.indices) {
                    if (drugList[i].equals(drug.first)) {
                        drugList[i].setmAction(PillpopperConstants.SKIPPED)
                    }
                }
                AppConstants.SHOW_SAVED_ALERT = true
                listAdapter.notifyDataSetChanged()
                when (AppConstants.isByPassLogin()) {
                    true -> {
                        FrontController.getInstance(_thisActivity).performSkipDrug_pastReminders(takeDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW)
                        StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
                    }
                    false -> FrontController.getInstance(_thisActivity).performSkipDrug_pastReminders(takeDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW)
                }

                if (isExitRequired) {
                    PillpopperRunTime.getInstance().removalTime = drug.first.scheduledTime.gmtMilliseconds
                    if (isLastGroup) {
                        if(AppConstants.MEDS_TAKEN_OR_POSTPONED) {
                            showSavedResponseAlert()
                        }else{
                            ActivationController.getInstance().performSignoff(_thisActivity)
                        }
                    } else {
                        this.finish()
                    }
                }
                changeBtnTextIfAnyActionTaken()
                viewModelProvider.onMedSkippedClicked.postValue(null)
            }
        })
    }
    val isExitRequired: Boolean
        get() {
            for (d in drugList) {
                if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                    return false
                }
            }
            return true
        }

    fun outsideLayoutClicked(){
        // To prevent background item click with gradient effect
    }
    fun onSkippedAllClicked(){

        val finalDrugs = ArrayList<Drug>()
        for (d in drugList) {
            if (null != d.guid && d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode){
                skipRequestCode -> {
                    val finalDrugs = ArrayList<Drug>()
                    for (d in drugList) {
                        if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                             d.setmAction(PillpopperConstants.SKIPPED)
                            finalDrugs.add(d)
                        }
                    }
                    AppConstants.SHOW_SAVED_ALERT = true
                    if (finalDrugs.isNotEmpty()) {
                        updatePassedReminderTable(finalDrugs)
                        if (!AppConstants.isByPassLogin()) {
                            FrontController.getInstance(_thisActivity).performSkipDrug_pastRemindersForQuickView(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.skipAllBtn.text.toString())
                        }
                        if (AppConstants.isByPassLogin()) {
                            FrontController.getInstance(_thisActivity).performSkipDrug_pastRemindersForQuickView(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.skipAllBtn.text.toString())
                            StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
                        }
                    }
                    PillpopperRunTime.getInstance().removalTime = reminderTime!!
                    if (isLastGroup && isExitRequired) {
                        if(AppConstants.MEDS_TAKEN_OR_POSTPONED) {
                            showSavedResponseAlert()
                        }else{
                            ActivationController.getInstance().performSignoff(_thisActivity)
                        }
                    }else{
                        _thisActivity.finish()
                    }
                }
                SAVED_ALERT_REQUEST_CODE -> {
                    LoggerUtils.info("Alerts - calling performSignOff 555")
                    ActivationController.getInstance().performSignoff(_thisActivity)
                }
                0 -> {
                    doTakeDrugs()
                    PillpopperRunTime.getInstance().removalTime = reminderTime!!
                }
                200 -> {
                    listAdapter.onSkip()
                }
                else -> finish()
            }
        }
    }

    fun onTakenAllClicked(){
        AppConstants.MEDS_TAKEN_OR_POSTPONED = true
        if (!isLastGroup) {
            doTakeDrugs()
            PillpopperRunTime.getInstance().removalTime = reminderTime!!
        } else {
            if(AppConstants.SHOW_SAVED_ALERT){
                val intent = Intent(this, ReminderAlertActivity::class.java)
                intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
                intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
                intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
                startActivityForResult(intent, 0)
            }else {
                showResponseAlertForTakenAction()
            }
        }
    }
    private fun showResponseAlertForTakenAction(){
        LoggerUtils.info("Alerts - in showSavedResponseAlert 352")
        if (currentRemindersActionType.equals(resources.getString(R.string.taken_action), ignoreCase = true)) {
            val intent = Intent(this, ReminderAlertActivity::class.java)
            intent.putExtra(AppConstants.LAUNCH_MODE, "passedReminders")
            intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.taken))
            intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.keep_it_up_message))
            startActivityForResult(intent, 0)
        } else {
            val intent = Intent(this, ReminderAlertActivity::class.java)
            intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
            intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
            intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
            startActivityForResult(intent, 0)
        }
    }
    private fun showSavedResponseAlert(){
        val intent = Intent(this, ReminderAlertActivity::class.java)
        intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
        intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
        intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
        startActivityForResult(intent, SAVED_ALERT_REQUEST_CODE)
    }

    private fun initValue() {
        val mFontBold = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_BOLD)
        val mFontMedium = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM)
        binding.activityContext = this
        binding.robotoBold = mFontBold
        binding.robotoMedium = mFontMedium
        val intent = intent
        currentRemindersActionType = intent.getStringExtra("currentRemindersActionType").toString()
        isLastGroup = intent.getBooleanExtra("isLastGroup", false)
        val mDrugsToBeActed = ArrayList(PillpopperRunTime.getInstance().proxyDrugs)
        if (mDrugsToBeActed.isNotEmpty()) {
            PillpopperRunTime.getInstance().proxyName = FrontController.getInstance(_thisActivity).getUserFirstNameByUserId(mDrugsToBeActed[0].userID)
            binding.lateReminderToolbar.tvPatientName.text = FrontController.getInstance(_thisActivity).getUserFirstNameByUserId(mDrugsToBeActed[0].userID)
            binding.lateReminderToolbar.tvPatientName.typeface = mFontBold
        }
        reminderTime = PillpopperRunTime.getInstance().headerTime
        drugList = PillpopperRunTime.getInstance().getmOverdueDrugs()
        singleMed = PillpopperRunTime.getInstance().getmOverdueDrugs()
        binding.lateReminderToolbar.signIn.setOnClickListener { ActivationController.getInstance().performSignoff(_thisActivity) }
        binding.lateReminderToolbar.backNavigation.setOnClickListener { finish() }
    }

    override fun onPause() {
        if (popUpMenu != null) {
            popUpMenu!!.dismiss()
        }
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        ActivationController.getInstance().stopTimer(this)

        RunTimeConstants.getInstance().isNotificationSuppressor = false
        when(AppConstants.isByPassLogin()){
            true -> binding.lateReminderToolbar.signIn.visibility = View.VISIBLE
            false -> binding.lateReminderToolbar.signIn.visibility = View.GONE
        }

        try {
            val sdf = SimpleDateFormat("dd/MMM/yyyy")
            val compareDate = sdf.format(Date(reminderTime!!))
            val mGroupDate = sdf.parse(compareDate)
            binding.tvDatetime.text = if (mGroupDate!!.before(sdf.parse(sdf.format(Calendar.getInstance().timeInMillis)))) Util.getTime(reminderTime!!) + " " + resources.getString(R.string._yesterday)
            else Util.getTime(reminderTime!!) + " " + resources.getString(R.string._today)
        } catch (exp: ParseException) {
            PillpopperLog.say("---Error while parsing the date---" + exp.message)
        }
        for(drug in drugList){
            viewModelProvider.setBindingData(drug)
        }
        listAdapter = LateReminderDetailAdapter(drugList,viewModelProvider,_thisActivity)
        binding.adapter = listAdapter
        setFooterButtons()
    }

    private fun setFooterButtons() {
        if(drugList.size>1){
            binding.skipAllBtn.text = resources.getString(R.string.skipped_all)
            binding.takenAllBtn.text = resources.getString(R.string.reminder_taken_all)
            changeBtnTextIfAnyActionTaken()
        }else{
            binding.skipAllBtn.text = resources.getString(R.string.skipped)
            binding.takenAllBtn.text = resources.getString(R.string.taken_text)
        }
    }
    fun changeBtnTextIfAnyActionTaken() {
        if (drugList.isNotEmpty()) {
            for (d in drugList) {
                if (null != d.guid && d.getmAction() != PillpopperConstants.NO_ACTION_TAKEN) {
                    changeButtonText()
                }
            }
        }
    }
    private fun changeButtonText(){
        if(drugList.size>1){
            binding.skipAllBtn.text = resources.getString(R.string.skip_the_rest_btn)
            binding.takenAllBtn.text = resources.getString(R.string.take_the_rest_btn)
        }
    }


    private fun doTakeDrugs() {
        val drugListToAct = ArrayList<Drug>()
        for (d in drugList) {
//              If Not acted upon this drug then add it to final drugs list
            if (null != d.guid && d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                d.setmAction(PillpopperConstants.TAKEN)
                drugListToAct.add(d)
            }
        }
        if (drugListToAct.isNotEmpty()) {
            updatePassedReminderTable(drugListToAct)
            if (!AppConstants.isByPassLogin()) {
                FrontController.getInstance(_thisActivity).performTakeDrug_pastRemindersForQuickView(drugListToAct, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllBtn.text.toString())
            }
            if (AppConstants.isByPassLogin()) {
                FrontController.getInstance(_thisActivity).performTakeDrug_pastRemindersForQuickView(drugListToAct, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllBtn.text.toString())
                StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
            }
        } else {
            updatePassedReminderTable(drugList)
            if (!AppConstants.isByPassLogin()) {
                FrontController.getInstance(_thisActivity).performTakeDrug_pastRemindersForQuickView(drugList, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllBtn.text.toString())
            }
            if (AppConstants.isByPassLogin()) {
                FrontController.getInstance(_thisActivity).performTakeDrug_pastRemindersForQuickView(drugList, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllBtn.text.toString())
                StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
            }
        }
        finish()
    }

    override fun onSkip() {
        val intent = Intent(this, ReminderAlertActivity::class.java)
        intent.putExtra(AppConstants.LAUNCH_MODE, resources?.getString(R.string.skipped))
        intent.putExtra(AppConstants.ACTION_TITLE, resources?.getString(R.string.skipped))
        intent.putExtra(AppConstants.ACTION_MESSAGE, resources?.getString(R.string.skip_alert_message))
        startActivityForResult(intent, 200)
    }

    override fun onReminderLater() {
        // no remind later option for late Reminders
    }

    override fun onTakenEarlier() {
        // no taken earlier option for late Reminders
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish()
    }
}