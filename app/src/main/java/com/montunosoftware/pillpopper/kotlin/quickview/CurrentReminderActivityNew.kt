package com.montunosoftware.pillpopper.kotlin.quickview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.ExpandableListView.OnGroupClickListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.CurrentReminderListActivityBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.ReminderContainerActivity
import com.montunosoftware.pillpopper.android.RunTimeConstants
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog.OnDateAndTimeSetListener
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
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager
import java.util.*
import kotlin.collections.ArrayList

class CurrentReminderActivityNew : ReminderContainerActivity() {

    private var takenEarlierTime: Long? = 0
    private lateinit var currentReminderViewModel: CurrentReminderActivityViewModel
    private lateinit var binding: CurrentReminderListActivityBinding
    private lateinit var mProxyAdapter: CurrentReminderAdapter
    private lateinit var userNames: MutableList<String>
    private var mCurrentReminderDrugs = LinkedHashMap<Long, MutableList<Drug>>()
    private lateinit var mProxyList: MutableList<String>
    private var users: MutableList<String> = ArrayList()
    private var setOfUsers: MutableSet<String>? = null
    private val masterHashMap = LinkedHashMap<Long, LinkedHashMap<String, MutableList<Drug>>>()
    private val mTempList: MutableList<Drug> = ArrayList()
    private var singleMed = false
    private val skipRequestCode = 100
    private val reminderSnoozePicker = ReminderSnoozePicker()
    private var isFromOnCreate = false

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding = DataBindingUtil.setContentView(this, R.layout.current_reminder_list_activity)
        currentReminderViewModel = ViewModelProvider(this).get(CurrentReminderActivityViewModel::class.java)
        binding.activityContext = this
        isFromOnCreate = true
        initValues()
        setObserver()
    }

    private fun setObserver() {
        currentReminderViewModel.onDrugImageClicked.observe(this, {
            if(null != it) {
                val expandImageIntent = Intent(this, EnlargeImageActivity::class.java)
                expandImageIntent.putExtra("pillId", it.guid)
                expandImageIntent.putExtra("imageId", it.imageGuid)
                expandImageIntent.putExtra("isFromReminderDrugDetailActivity", true)
                startActivity(expandImageIntent)
                currentReminderViewModel.onDrugImageClicked.postValue(null)
            }
        })

        currentReminderViewModel.isByPassLogin.observe(this, {
            if(it) Util.performSignout(_thisActivity, _globalAppContext) else _thisActivity.finish()
        })
    }

    private fun initValues() {

        val mFontMedium = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_MEDIUM)
        val mFontRegular = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_REGULAR)
        val mFontBold = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_BOLD)

        binding.expandableProxyReminders.setChildDivider(Util.getDrawableWrapper(this, R.drawable.transparent_color_drawable))
        currentReminderViewModel.mFrontController = FrontController.getInstance(_thisActivity)
        mProxyList = ArrayList()
        if(null != PillpopperRunTime.getInstance().getmCurrentRemindersMap()) {
            mCurrentReminderDrugs = PillpopperRunTime.getInstance().getmCurrentRemindersMap()
        }

        binding.reminderScreenToolbar.medicationRemainder.typeface = mFontRegular
        binding.reminderScreenToolbar.signInBtn.typeface = mFontRegular
        binding.reminderScreenToolbar.reminderOverDueDateTime.typeface = mFontBold
        binding.reminderScreenToolbar.backNavigation.visibility = View.GONE
        binding.robotoMedium = mFontMedium
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_QUICKVIEW_CURRENT_REMINDER)

    }

    override fun onResume() {
        super.onResume()
        LoggerUtils.info("Alerts - in onResume 109")
        ActivationController.getInstance().stopTimer(this)
        binding.reminderScreenToolbar.signIn.setOnClickListener {
            signInClicked()
        }

        currentReminderViewModel.isCurrentReminderRefreshRequired()

        currentReminderViewModel.isCurrentReminderRefreshRequired.observe(this, {
            if (it && !isFromOnCreate) {
                PillpopperLog.say("-- We are done with current and past reminders .. so just needs to kill this activity and refresh")
                if (currentReminderViewModel.getDrugsCountWithNoActionTaken() > 0 && null != mProxyAdapter ) {
                    mProxyAdapter.notifyDataSetChanged()
                }
            } else {
                isFromOnCreate = false
                PillpopperLog.say("-- We are done with current and past reminders .. Regular flow")
                RunTimeConstants.getInstance().isNotificationSuppressor = false
                PillpopperConstants.setIsRemindersBeingShown(true)
                currentReminderViewModel.listOfDrugsToBeTaken = ArrayList()
                userNames = ArrayList()
                if (AppConstants.isByPassLogin()) {
                    binding.reminderScreenToolbar.signIn.visibility = View.VISIBLE
                } else {
                    binding.reminderScreenToolbar.signIn.visibility = View.GONE
                    if (!PillpopperConstants.isAlertActedOn()) {
                        DialogHelpers.showPostSignInAlert(_thisActivity)
                    }
                }
                if (mCurrentReminderDrugs.isNotEmpty()) {
                    buildMapUsers(mCurrentReminderDrugs)
                    userNames.addAll(currentReminderViewModel.mOverdueHashMap.keys)
                }
                for (i in userNames.indices) {
                    if (null != currentReminderViewModel.mSingleTimeHashMap[userNames[i]]) {
                        currentReminderViewModel.mSingleTimeHashMap[userNames[i]]?.let { list -> currentReminderViewModel.listOfDrugsToBeTaken.addAll(list) }
                    }
                }
                PillpopperRunTime.getInstance().drugsToBeTaken = currentReminderViewModel.listOfDrugsToBeTaken
                setFooterButtons()
                val map = currentReminderViewModel.mSingleTimeHashMap
                map["dummyName"] = ArrayList()
                mProxyAdapter = CurrentReminderAdapter(this, map, currentReminderViewModel)
                binding.adapter = mProxyAdapter

                binding.expandableProxyReminders.setOnChildClickListener(OnChildClickListener { _, _, groupPosition, _, _ ->
                    val memberName: String = currentReminderViewModel.mFrontController.getUserFirstNameByUserId(userNames[groupPosition])
                    val memberDrugs: List<Drug>? = currentReminderViewModel.mOverdueHashMap[userNames[groupPosition]]
                    if (checkForNonPerformedDrugs(memberDrugs)) {
                        PillpopperRunTime.getInstance().proxyName = memberName
                        PillpopperRunTime.getInstance().proxyDrugs = memberDrugs
                        val intent = Intent(_thisActivity, SingleCurrentReminder::class.java)
                        intent.putExtra("isLastCurrentUser", isLastUser(currentReminderViewModel.mOverdueHashMap))
                        if (null != PillpopperRunTime.getInstance().proxyDrugs && PillpopperRunTime.getInstance().proxyDrugs.isNotEmpty()) {
                            startActivity(intent)
                        }
                    } else {
                        // User has performed an action on each drug.
                        // do Nothing
                    }
                    return@OnChildClickListener true
                })
                binding.expandableProxyReminders.setOnGroupClickListener(OnGroupClickListener { parent, v, groupPosition, id ->
                    if(groupPosition != userNames.size){
                        val memberName: String = currentReminderViewModel.mFrontController.getUserFirstNameByUserId(userNames[groupPosition])
                        val memberDrugs: List<Drug> = currentReminderViewModel.mOverdueHashMap[userNames[groupPosition]]!!
                        if (checkForNonPerformedDrugs(memberDrugs)) {
                            PillpopperRunTime.getInstance().proxyName = memberName
                            PillpopperRunTime.getInstance().proxyDrugs = memberDrugs
                            val intent = Intent(_thisActivity, SingleCurrentReminder::class.java)
                            intent.putExtra("isLastCurrentUser", isLastUser(currentReminderViewModel.mOverdueHashMap))
                            if (null != PillpopperRunTime.getInstance().proxyDrugs && PillpopperRunTime.getInstance().proxyDrugs.isNotEmpty()) {
                                startActivity(intent)
                            }
                        } else {
                            // User has performed an action on each drug.
                            // do Nothing
                        }
                    } else {
                        return@OnGroupClickListener false
                    }
                    true
                })
            }
        })
    }

    private fun isLastUser(overDueUserHashMap: LinkedHashMap<String, MutableList<Drug>>?): Boolean {

        if (null != overDueUserHashMap && overDueUserHashMap.isNotEmpty()) {
            val usersCount: MutableList<String> = LinkedList()
            for ((_, value) in overDueUserHashMap) {
                for (d in value) {
                    if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN
                            && !usersCount.contains(d.userID)) {
                        usersCount.add(d.userID)
                    }
                }
            }
            return usersCount.size <= 1
        }
        return true
    }

    private fun checkForNonPerformedDrugs(memberDrugs: List<Drug>?): Boolean {
        if (null != memberDrugs && memberDrugs.isNotEmpty()) {
            for (drug in memberDrugs) {
                if (drug.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                    return true
                }
            }
        }
        return false
    }

    private fun buildMapUsers(mCurrentReminderDrugs: LinkedHashMap<Long, MutableList<Drug>>) {
        for ((key, value) in mCurrentReminderDrugs) {
            val tempListOfDrugs: List<Drug> = ArrayList(value)
            for (d in tempListOfDrugs) {
                mProxyList.add(d.userID)
            }
            getUniqueMembers(mProxyList)
            for (userId in users) {
                if (userId == currentReminderViewModel.mFrontController.getPrimaryUserId()) {
                    currentReminderViewModel.getPrimaryUserData(userId, tempListOfDrugs)
                }
            }
            if (null != users) {
                users.clear()
            }
            users = getSortedProxyMembers()
            for (userId in users) {
                if (userId != currentReminderViewModel.mFrontController.getPrimaryUserId()) {
                    currentReminderViewModel.buildProxyUsersData(userId, tempListOfDrugs)
                }
            }
            masterHashMap[key] = currentReminderViewModel.mOverdueHashMap
            binding.reminderScreenToolbar.reminderOverDueDateTime.text = Util.getDate(key)+ ", " + Util.getTime(key)
            mTempList.addAll(value)
            PillpopperRunTime.getInstance().headerTime = key
            PillpopperRunTime.getInstance().headerDate = key
            currentReminderViewModel.mSingleTimeHashMap = masterHashMap[key]!!
            PillpopperRunTime.getInstance().drugsHashMap = currentReminderViewModel.mSingleTimeHashMap
        }
    }

    private fun getUniqueMembers(mProxyList: MutableList<String>) {
        setOfUsers = TreeSet(mProxyList)
        users.clear()
        users.addAll(setOfUsers as TreeSet<String>)
    }

    private fun getSortedProxyMembers(): MutableList<String> {
        return FrontController.getInstance(_thisActivity).proxyMemberUserIds
    }

    private fun setFooterButtons() {
        try {
            if (PillpopperRunTime.getInstance().headerTime != null) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = PillpopperRunTime.getInstance().headerTime
                if (Calendar.getInstance().time.after(cal.time)) {
                    if (Calendar.getInstance().timeInMillis - PillpopperRunTime.getInstance().headerTime > PillpopperConstants.LATE_REMINDER_INTERVAL) {
                        //the value is changed to 10minutes for regression later will be reverted to 1hr
                        binding.takeAllLater.visibility = View.GONE
                        binding.takenAllEarlier.visibility = View.VISIBLE
                        val keys: List<Long> = ArrayList(mCurrentReminderDrugs.keys)
                        if (keys.isNotEmpty() && keys.size == 1) {
                            if (mCurrentReminderDrugs[keys[0]]!!.size == 1) {
                                singleMed = true
                                binding.takenAllEarlierBtn.text = resources.getString(R.string.reminder_taken_earlier)
                            } else {
                                binding.takenAllEarlierBtn.text = resources.getString(R.string.reminder_taken_all_earlier)
                            }
                        } else {
                            singleMed = false
                            binding.takenAllEarlierBtn.text = resources.getString(R.string.reminder_taken_all_earlier)
                        }
                    } else {
                        val keys: List<Long> = ArrayList(mCurrentReminderDrugs.keys)
                        singleMed = keys.isNotEmpty() && keys.size == 1 && mCurrentReminderDrugs[keys[0]]!!.size == 1
                        binding.takenAllEarlier.visibility = View.GONE
                        binding.takeAllLater.visibility = View.VISIBLE
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
            if(currentReminderViewModel.isActionTakenDrugAvailable()){
                binding.skipAllBtn.text = resources.getString(R.string.skip_the_rest_btn)
                binding.takenAllBtn.text = resources.getString(R.string.take_the_rest_btn)
                binding.takenAllEarlierBtn.text = resources.getString(R.string.taken_the_rest_earlier_btn)
            }else{
                binding.takenAllBtn.text = resources.getString(R.string.reminder_taken_all)
                binding.skipAllBtn.text = resources.getString(R.string.reminder_skip_all)
            }
        }
    }
    fun getActionType():String {
        if (currentReminderViewModel.listOfDrugsToBeTaken.isNotEmpty()) {
            return Util.getActionType(currentReminderViewModel.listOfDrugsToBeTaken as ArrayList<Drug>?,this)
        }
        return ""
    }

    fun outsideLayoutClicked(){
        // To prevent background item click with gradient effect
    }
    fun onSkippedAllClicked() {
        currentReminderViewModel.createFinalDrugList()
        intent = Intent(this, ReminderAlertActivity::class.java)
        intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.skipped))
        intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.skipped))
        val skipMessage: String = if (currentReminderViewModel.getDrugsCountWithNoActionTaken() == 1) {
            resources.getString(R.string.skip_alert_message)
        } else {
            resources.getString(R.string.skipall_alert_message)
        }
        intent.putExtra(AppConstants.ACTION_MESSAGE,  skipMessage)
        startActivityForResult(intent, skipRequestCode)
    }

    fun onTakenAllClicked() {
        AppConstants.MEDS_TAKEN_OR_SKIPPED = true
        AppConstants.MEDS_TAKEN_OR_POSTPONED = true
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
    }

    private fun showSavedResponseAlert(){
        LoggerUtils.info("Alerts - in showSavedResponseAlert 352")
        if ((null == PillpopperRunTime.getInstance().passedReminderersHashMapByUserId || PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isEmpty())) {
            val intent = Intent(this, ReminderAlertActivity::class.java)
            intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
            intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
            intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
            startActivityForResult(intent, SAVED_ALERT_REQUEST_CODE)
        }
    }

    fun onTakenEarlierClicked() {
        currentReminderViewModel.finalDrugs = ArrayList()
        currentReminderViewModel.createFinalDrugList()

        val dateAndTimePickerDialog = DateAndTimePickerDialog(
                this,
                OnDateAndTimeSetListener { pillPopperTime ->
                    takenEarlierTime = pillPopperTime.gmtSeconds
                    AppConstants.MEDS_TAKEN_OR_SKIPPED = true
                    AppConstants.MEDS_TAKEN_OR_POSTPONED = true
                    if (null == PillpopperRunTime.getInstance().passedReminderersHashMapByUserId ||
                            PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isEmpty()) {
                        val intent = Intent(this, ReminderAlertActivity::class.java)
                        intent.putExtra("TakenEarlier", pillPopperTime.gmtSeconds)
                        if (!AppConstants.SHOW_SAVED_ALERT) {
                            intent.putExtra(AppConstants.LAUNCH_MODE, "LateReminders")
                            intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.great_job_title))
                            intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.action_taken_msg))
                        } else {
                            intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
                            intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
                            intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
                        }
                        startActivityForResult(intent, 101)
                    } else {
                        continueTakeDrugProcess(101)
                    }
                },
                false,
                PillpopperTime.now(),
                15, _thisActivity.resources.getString(R.string.taken_text), true
        )

        dateAndTimePickerDialog.show(supportFragmentManager, "taken_earlier_time")
    }

    fun onTakeLaterClicked() {
        currentReminderViewModel.finalDrugs = ArrayList()
        currentReminderViewModel.createFinalDrugList()
        val manager = supportFragmentManager
        val frag = manager.findFragmentByTag("fragment_edit_name")
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit()
        }

        reminderSnoozePicker.setHourMinutePickedListener(ReminderSnoozePicker.HourMinutePickedListener { hm ->
            if (hm == null) {
                return@HourMinutePickedListener
            }
            val postponeTimeSeconds = (hm[0] * 60 + hm[1]) * 60.toLong()
            val postponeError = Drug.validatePostpones(currentReminderViewModel.finalDrugs, postponeTimeSeconds,
                    _thisActivity)
            if (postponeError != null) {
                DialogHelpers.showPostponeErrorAlert(_thisActivity)
            } else {
                if (AppConstants.isByPassLogin()) {
                    FrontController.getInstance(_thisActivity).performPostponeDrugs(currentReminderViewModel.finalDrugs, postponeTimeSeconds, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW)
                    StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
                } else {
                    FrontController.getInstance(_thisActivity).performPostponeDrugs(currentReminderViewModel.finalDrugs, postponeTimeSeconds, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW)
                }
                resetShowLateReminderFlags()
                AppConstants.SHOW_SAVED_ALERT = true
                AppConstants.MEDS_TAKEN_OR_POSTPONED = true
                updateDrugsAsPostponed(currentReminderViewModel.finalDrugs)
                if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId && PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()) {
                    showLateRemindersOrRefreshCurrent()
                } else if (AppConstants.isByPassLogin()) {
                    PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                    if (AppConstants.MEDS_TAKEN_OR_SKIPPED) {
                        showSavedResponseAlert()
                    }else{
                        ActivationController.getInstance().performSignoff(_thisActivity)
                    }
                } else {
                    PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                    if (AppConstants.MEDS_TAKEN_OR_SKIPPED) {
                        showSavedResponseAlert()
                    }else{
                        _thisActivity.finish ()
                    }
                }
            }
            PillpopperConstants.setIsRemindersDisplying(false)
        })
        reminderSnoozePicker.show(manager, "fragment_edit_name")
    }

    private fun continueTakeDrugProcess(requestCode: Int) {
        currentReminderViewModel.finalDrugs = ArrayList()
        currentReminderViewModel.createFinalDrugList()
        if (currentReminderViewModel.finalDrugs.isNotEmpty()) {
            updateDrugsAsTaken(currentReminderViewModel.finalDrugs)
            if (AppConstants.isByPassLogin()) {
                if (requestCode == 101) {
                    FrontController.getInstance(_thisActivity).performAlreadyTakenDrugsForQuickView(currentReminderViewModel.finalDrugs, takenEarlierTime?.let { PillpopperTime(it) }, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllEarlierBtn.text.toString())
                } else {
                    FrontController.getInstance(_thisActivity).performTakeDrugForQuickView(currentReminderViewModel.finalDrugs, null, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllBtn.text.toString())
                }
                StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
            } else {
                if (requestCode == 101) {
                    FrontController.getInstance(_thisActivity).performAlreadyTakenDrugsForQuickView(currentReminderViewModel.finalDrugs, takenEarlierTime?.let { PillpopperTime(it) }, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllEarlierBtn.text.toString())
                } else {
                    FrontController.getInstance(_thisActivity).performTakeDrugForQuickView(currentReminderViewModel.finalDrugs, null, _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.takenAllBtn.text.toString())
                }
            }
        }
        resetShowLateReminderFlags()
        if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId && PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()) {
            showLateRemindersOrRefreshCurrent()
        } else if (AppConstants.isByPassLogin()) {
            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            ActivationController.getInstance().performSignoff(_thisActivity)
        } else {
            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            _thisActivity.finish()
        }
    }

    private fun resetShowLateReminderFlags() {
        // in signed out state, reset the flags irrespective of when/ where the late reminder is dismissed.
        if (AppConstants.isByPassLogin()) {
            val sharedPreferenceManager = SharedPreferenceManager.getInstance(_thisActivity, AppConstants.AUTH_CODE_PREF_NAME)
            sharedPreferenceManager.putBoolean(AppConstants.SIGNED_STATE_REMOVAL, false, false)
            sharedPreferenceManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false, false)
            sharedPreferenceManager.putString(AppConstants.TIME_STAMP, "0", false)
            LoggerUtils.info("resetShowLateReminderFlags done")

            // if there are late reminders, then updateAsPendingRemindersPresent, so the late reminders will be shown outside
            if (FrontController.getInstance(_thisActivity).getPassedReminderDrugs(_thisActivity).size > 0) {
                FrontController.getInstance(_thisActivity).updateAsPendingRemindersPresent(_thisActivity)
            }
        }
    }
    private fun updateDrugsAsSkipped(drugList: List<Drug>) {
        for (d in drugList) {
            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                d.setmAction(PillpopperConstants.SKIPPED)
            }
        }
    }
    private fun updateDrugsAsPostponed(drugList: List<Drug>) {
        for (d in drugList) {
            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                d.setmAction(PillpopperConstants.TAKE_LATER)
            }
        }
    }
    private fun updateDrugsAsTaken(drugList: List<Drug>) {
        for (d in drugList) {
            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                d.setmAction(PillpopperConstants.TAKEN)
            }
        }
    }

    private fun showLateRemindersOrRefreshCurrent() {
        if (Util.canShowLateReminder(_thisActivity)) {
            intent = Intent(this, LateRemindersActivity::class.java)
            intent.putExtra("currentRemindersActionType", getActionType())
            startActivity(intent)
            _thisActivity.finish()
        } else if (!AppConstants.isByPassLogin()) {
            FrontController.getInstance(_thisActivity).updateAsPendingRemindersPresent(_thisActivity)
            _thisActivity.finish()
        } else {
            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            _thisActivity.finish()
        }
    }

    private fun signInClicked() {
        ActivationController.getInstance().performSignoff(_thisActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode){
                skipRequestCode -> {
                    currentReminderViewModel.finalDrugs = ArrayList()
                    currentReminderViewModel.createFinalDrugList()
                    if (currentReminderViewModel.finalDrugs.isNotEmpty()) {
                        updateDrugsAsSkipped(currentReminderViewModel.finalDrugs)
                        AppConstants.SHOW_SAVED_ALERT = true
                        AppConstants.MEDS_TAKEN_OR_SKIPPED = true
                        if (AppConstants.isByPassLogin()) {
                            FrontController.getInstance(_thisActivity).performSkipDrugForQuickView(currentReminderViewModel.finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.skipAllBtn.text.toString())
                            StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
                        } else {
                            FrontController.getInstance(_thisActivity).performSkipDrugForQuickView(currentReminderViewModel.finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.skipAllBtn.text.toString())
                        }
                    }
                    resetShowLateReminderFlags()
                    if (null != PillpopperRunTime.getInstance().passedReminderersHashMapByUserId && PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.isNotEmpty()) {
                        showLateRemindersOrRefreshCurrent()
                    } else if (AppConstants.isByPassLogin()) {
                        LoggerUtils.info("Alerts - calling showSavedResponseAlert 541")
                        PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                        if(AppConstants.MEDS_TAKEN_OR_POSTPONED){
                            showSavedResponseAlert()
                        }else{
                            ActivationController.getInstance().performSignoff(_thisActivity)
                        }
                    } else {
                        PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                        if(AppConstants.MEDS_TAKEN_OR_POSTPONED){
                            showSavedResponseAlert()
                        }else{
                            _thisActivity.finish()
                        }
                    }
                }
                SAVED_ALERT_REQUEST_CODE -> {
                    LoggerUtils.info("Alerts - calling performSignOff 555")
                    ActivationController.getInstance().performSignoff(_thisActivity)
                }
                else -> {
                    takenEarlierTime = data?.getLongExtra("TakenEarlierTime", -1)
                    continueTakeDrugProcess(requestCode)
                }
            }
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if(reminderSnoozePicker.isVisible){
            reminderSnoozePicker.dismiss()
        }
    }
}
