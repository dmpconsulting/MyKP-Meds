package com.montunosoftware.pillpopper.kotlin.lateremider

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.PassedReminderListRedesignBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.ReminderContainerActivity
import com.montunosoftware.pillpopper.android.RunTimeConstants
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.android.view.DialogHelpers
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.kotlin.quickview.ReminderAlertActivity
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
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager
import java.util.*
import kotlin.collections.ArrayList

class LateRemindersActivity : ReminderContainerActivity() , PopUpListener {


    private var mPassedRemindersHashMapByUserId: LinkedHashMap<String, LinkedHashMap<Long, MutableList<Drug>>>? = null
    private var isRequiredSave: Boolean = true
    private lateinit var mLateRemindersAdapter: LateReminderAdapter
    private lateinit var lateReminderViewModel: LateReminderViewModel
    private lateinit var binding: PassedReminderListRedesignBinding
    private lateinit var mFrontController: FrontController
    private var mSharedPrefManager: SharedPreferenceManager? = null
    private var isDataLoaded = false
    private var mGroupActionDrugTimes: MutableList<Long> = ArrayList()

    private var currentUserId: String = ""
    private val drugsToBeActed: MutableList<Drug> = ArrayList()
    private var mSingleUserHashMap = LinkedHashMap<Long, MutableList<Drug>>()
    private var reminderPopUpShown = false
    private var reminderPopupMenu: PopupMenu? = null
    private var allDrugs: MutableList<Drug> = ArrayList()
    private var passedReminderMap = LinkedHashMap<Long, List<Drug>>()
    private val mProxyList: MutableList<String>? = null
    private val allUsersHashMap = LinkedHashMap<String, List<Drug>>()
    private var users: MutableList<String> = ArrayList()
    private lateinit var listOfTimes: MutableList<Long>
    private val userNames: MutableList<String> = ArrayList()
    private val mLateDrugsMap = LinkedHashMap<Long, List<Drug>>()
    var tempListOfDrugs: MutableList<Drug> = ArrayList()
    private var setOfUsers: MutableSet<String>? = null
    private lateinit var finalDrugs: ArrayList<Drug>
    private val skipRequestCode = 100
    private var currentRemindersActionType: String = ""

    override fun onCreate(bundle: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.passed_reminder_list_redesign)
        lateReminderViewModel = ViewModelProvider(this).get(LateReminderViewModel::class.java)
        initValues()
        setObserver()
        lateReminderViewModel.masterHashMap.size
        super.onCreate(bundle)
    }

    private fun setObserver() {
        lateReminderViewModel.insertPastReminder.observe(this, Observer {
            insertPastRemindersPillIdsIntoDB(it)
        })
        lateReminderViewModel.onDrugImageClicked.observe(this, Observer {
            if(null != it) {
                val expandImageIntent = Intent(this, EnlargeImageActivity::class.java)
                expandImageIntent.putExtra("pillId", it.guid)
                expandImageIntent.putExtra("imageId", it.imageGuid)
                expandImageIntent.putExtra("isFromReminderDrugDetailActivity", true)
                startActivity(expandImageIntent)
                lateReminderViewModel.onDrugImageClicked.postValue(null)
            }
        })
    }

    private fun initValues() {
        if (null != PillpopperRunTime.getInstance().getmPassedRemindersMap()) {
            passedReminderMap = PillpopperRunTime.getInstance().getmPassedRemindersMap()
        }
        val mFontMedium = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_MEDIUM)
        val mFontRegular = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_REGULAR)
        val mFontBold = ActivationUtil.setFontStyle(_thisActivity, AppConstants.FONT_ROBOTO_BOLD)

        binding.robotoBold  = mFontBold
        binding.robotoMedium  = mFontMedium
        binding.robotoRegular  = mFontRegular
        binding.context = this
        mPassedRemindersHashMapByUserId = PillpopperRunTime.getInstance().passedReminderersHashMapByUserId
        mFrontController = FrontController.getInstance(this)
        val intent = intent
        if (intent != null) {
            val mPassedRemindersHashMapByUserId = PillpopperRunTime.getInstance().passedReminderersHashMapByUserId
            val pendingPassedReminders = intent.getStringExtra("launchingMode")
            if (null != mPassedRemindersHashMapByUserId) {
                lateReminderViewModel.createMasterHashMap(pendingPassedReminders, mPassedRemindersHashMapByUserId)
            }
            currentRemindersActionType = intent.getStringExtra("currentRemindersActionType").toString()
        } else {
            PillpopperLog.say("Past Reminder - No Previous past reminders are present needs to be shown with regular past reminders else")
            if (null != mPassedRemindersHashMapByUserId) {
                lateReminderViewModel.masterHashMap.putAll(mPassedRemindersHashMapByUserId!!)
            }
            insertPastRemindersPillIdsIntoDB(lateReminderViewModel.masterHashMap)
        }
        mSharedPrefManager = SharedPreferenceManager.getInstance(applicationContext, AppConstants.AUTH_CODE_PREF_NAME)
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_QUICKVIEW_LATE_REMINDER)

    }
    private fun insertPastRemindersPillIdsIntoDB(masterHashMap: LinkedHashMap<String, LinkedHashMap<Long, MutableList<Drug>>>) {
        for ((_, list) in masterHashMap) {
            for ((time, drugs) in list) {
                for (drug in drugs) {
                    PillpopperLog.say("Past Reminder - Inserting pill id : " + drug.guid + " : Time is :" + time)
                    mFrontController.insertPastReminderPillId(drug.guid, time)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isDataLoaded || PillpopperRunTime.getInstance().removalTime != 0L) {
           // lateReminderViewModel.removeGrpHdr(mGroupActionDrugTimes, PillPopperRunTime.getInstance().removalTime)
            if(PillpopperRunTime.getInstance().removalTime.toInt() != 0) {
                lateReminderViewModel.mRemovalTimeList.add(PillpopperRunTime.getInstance().removalTime)
                PillpopperRunTime.getInstance().removalTime = 0
            }

            if (lateReminderViewModel.mRemovalTimeList.size >= mGroupActionDrugTimes.size -1 && null != lateReminderViewModel.masterHashMap) {

                lateReminderViewModel.mRemovalTimeList.clear()
                lateReminderViewModel.masterHashMap.remove(currentUserId)
                PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.remove(currentUserId)
                if (null != lateReminderViewModel.masterHashMap && lateReminderViewModel.masterHashMap.size > 0) {
                    currentUserId = lateReminderViewModel.masterHashMap.keys.iterator().next()
                    refreshMemberData(currentUserId)
                } else {
                    if (AppConstants.isByPassLogin()) {
                        ActivationController.getInstance().performSignoff(_thisActivity)
                    } else {
                        _thisActivity.finish()
                        FrontController.getInstance(_thisActivity).updateAsNoPendingReminders(_thisActivity)
                    }
                    PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                }
            }
            mLateRemindersAdapter.notifyDataSetChanged()
        }
    }

    private fun refreshMemberData(userID: String) {
        if (drugsToBeActed.isNotEmpty()) {
            drugsToBeActed.clear()
        }
        if (mGroupActionDrugTimes.isNotEmpty()) {
            mGroupActionDrugTimes.clear()
        }
        PillpopperLog.say("Past Reminder - First time/completed past reminders individually -- ")
        FrontController.getInstance(_thisActivity).updateAsPendingRemindersPresent(_thisActivity)
        mSharedPrefManager!!.putBoolean(AppConstants.IS_LAUNCHING_LATE_AFTER_CURRENT, true, false)
        PillpopperRunTime.getInstance().lateRemindersMap = lateReminderViewModel.masterHashMap
        mSingleUserHashMap = lateReminderViewModel.masterHashMap[userID]!!
        if (lateReminderViewModel.masterHashMap.size > 1) {
            val tempMasterHashMap = LinkedHashMap<String, LinkedHashMap<Long, MutableList<Drug>>>()
            tempMasterHashMap.putAll(lateReminderViewModel.masterHashMap)
            tempMasterHashMap.remove(userID)
            PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.remove(userID)
            binding.tvProxyCount.text = tempMasterHashMap.size.toString()
            if (tempMasterHashMap.size > 1) {
                binding.tvReminderText.text = " more late reminders"
                binding.tvProxy.text = "There are "
            } else if (tempMasterHashMap.size == 1) {
                binding.tvReminderText.text = " more late reminder"
                binding.tvProxy.text = "There is "
            }
        } else {
          binding.lrProxyContainer.visibility = View.GONE
        }
        for ((_, value) in mSingleUserHashMap.entries) {
            drugsToBeActed.addAll(value)
        }
        PillpopperRunTime.getInstance().proxyDrugs = drugsToBeActed
        binding.passedReminderScreenToolbar.tvProxyName.setVisibility(View.VISIBLE)
        val userNameText = FrontController.getInstance(_thisActivity).getUserFirstNameByUserId(currentUserId)
        binding.passedReminderScreenToolbar.tvProxyName.setText(HtmlCompat.fromHtml(userNameText, HtmlCompat.FROM_HTML_MODE_COMPACT))
        if (mSingleUserHashMap.isNotEmpty() && mSingleUserHashMap.keys.isNotEmpty()) {
            if (null == mGroupActionDrugTimes) {
                mGroupActionDrugTimes = ArrayList()
            }
            mGroupActionDrugTimes.addAll(mSingleUserHashMap.keys)
        }

        binding.expandablePastReminders.setChildDivider(Util.getDrawableWrapper(this, R.drawable.transparent_color_drawable))

        val mUserMap = mSingleUserHashMap
        val drug = Drug()

        val drugList : ArrayList<Drug> = ArrayList()
        drugList.add(drug)

        mUserMap[1] = drugList

        var  mGroupList : MutableList<Long> = ArrayList()
        if (null != mUserMap && mUserMap.keys.isNotEmpty()) {
            if (null == mGroupList) {
                mGroupList = ArrayList()
            }
            mGroupList.addAll(mUserMap.keys)
        }

        mGroupActionDrugTimes = mGroupList
        mLateRemindersAdapter = LateReminderAdapter(mSingleUserHashMap,mGroupActionDrugTimes,lateReminderViewModel)
        binding.adapter = mLateRemindersAdapter
        binding.expandablePastReminders.setOnGroupClickListener { _, _, groupPosition, _ ->
            val mGroupHeaderTime = mGroupActionDrugTimes[groupPosition]
            val mChildList: MutableList<Drug?> = ArrayList()
            var isAllActionTaken = false
            for(x in lateReminderViewModel.mRemovalTimeList.indices){
                if(mGroupHeaderTime == lateReminderViewModel.mRemovalTimeList[x]){
                    isAllActionTaken = true
                    break
                }
            }
            if(!isAllActionTaken) {
                mChildList.addAll(mSingleUserHashMap[mGroupHeaderTime]!!)
                PillpopperRunTime.getInstance().headerTime = mGroupHeaderTime
                PillpopperRunTime.getInstance().setmOverdueDrugs(mChildList)
                val intent = Intent(this, LateReminderDetail::class.java)
                intent.putExtra("currentRemindersActionType",currentRemindersActionType)
                if (lateReminderViewModel.masterHashMap.size == 1 && lateReminderViewModel.mRemovalTimeList.size >= mGroupActionDrugTimes.size - 2) {
                    intent.putExtra("isLastGroup", true)
                }
                if (null != PillpopperRunTime.getInstance().proxyDrugs && PillpopperRunTime.getInstance().proxyDrugs.isNotEmpty()) {
                    startActivityForResult(intent, 1)
                }
            }
            true
        }
        binding.expandablePastReminders.setOnChildClickListener { _, _, groupPosition, _, _ ->
            val mGroupHeaderTime = mGroupActionDrugTimes[groupPosition]
            var isAllActionTaken = false
            for(x in lateReminderViewModel.mRemovalTimeList.indices){
                if(mGroupHeaderTime == lateReminderViewModel.mRemovalTimeList[x]){
                    isAllActionTaken = true
                    break
                }
            }
            if(!isAllActionTaken) {
                val mChildList: MutableList<Drug?> = ArrayList()
                mChildList.addAll(mSingleUserHashMap[mGroupHeaderTime]!!)
                PillpopperRunTime.getInstance().headerTime = mGroupHeaderTime
                PillpopperRunTime.getInstance().setmOverdueDrugs(mChildList)
                val intent = Intent(this, LateReminderDetail::class.java)
                intent.putExtra("currentRemindersActionType",currentRemindersActionType)
                if (lateReminderViewModel.masterHashMap.size == 1 && lateReminderViewModel.mRemovalTimeList.size >= mGroupActionDrugTimes.size -2) {
                    intent.putExtra("isLastGroup", true)
                }
                if (null != PillpopperRunTime.getInstance().proxyDrugs && PillpopperRunTime.getInstance().proxyDrugs.isNotEmpty()) {
                    startActivityForResult(intent, 1)
                }
            }
            true
        }
        isDataLoaded = true
    }

    override fun onResume() {
        super.onResume()
        ActivationController.getInstance().stopTimer(this)
        if (reminderPopUpShown) {
            if (reminderPopupMenu != null) {
                reminderPopupMenu?.dismiss()
                reminderPopUpShown = false
            }
        }

        RunTimeConstants.getInstance().isNotificationSuppressor = false
        if (allDrugs.isNotEmpty()) {
            allDrugs.clear()
        }
        allDrugs = ArrayList()
        for ((_, value) in passedReminderMap.entries) {
            allDrugs.addAll(value)
        }

        listOfTimes = ArrayList(passedReminderMap.keys)
        buildAllUserDrugs(allDrugs)
        doPreparePassedRmData(allUsersHashMap)

        if (!isDataLoaded) {
            if (null != lateReminderViewModel.masterHashMap && lateReminderViewModel.masterHashMap.size > 0) {
                currentUserId = lateReminderViewModel.masterHashMap.keys.iterator().next()
                refreshMemberData(currentUserId)
                if (!PillpopperConstants.isAlertActedOn()) {
                    DialogHelpers.showPostSignInAlert(_thisActivity)
                }
            }
        }
        setFooterButtons()
    }
    override fun onPopUpDismissed() {
        reminderPopUpShown = false
        reminderPopupMenu = null
    }

    private fun doPreparePassedRmData(mPassedReminderDrugs: LinkedHashMap<String, List<Drug>>) {
        for (user in users) {
            if (user == FrontController.getInstance(_thisActivity).primaryUserId) {
                mPassedReminderDrugs[user]?.let { buildPrimaryUserData(user, it) }
                userNames.add(user)
            }
        }
        if (null != users) {
            users.clear()
        }
        users = getSortedProxyMembers()
        userNames.addAll(users)
        for (user in users) {
            if (user != FrontController.getInstance(_thisActivity).primaryUserId) {
                buildProxyUserData(user, mPassedReminderDrugs[user])
            }
        }
    }
    private fun getSortedProxyMembers(): MutableList<String> {
        return FrontController.getInstance(_thisActivity).proxyMemberUserIds
    }

    private fun buildAllUserDrugs(allDrugs: List<Drug>) {
        for (d in allDrugs) {
            mProxyList?.add(d.userID)
        }
        mProxyList?.let { getUniqueMembers(it) }
        for (user in users) {
            for (d in allDrugs) {
                if (user == d.userID) {
                    tempListOfDrugs.add(d)
                }
            }
            allUsersHashMap[user] = tempListOfDrugs
        }
    }

    private fun getUniqueMembers(mProxyList: MutableList<String>) {
        setOfUsers = TreeSet(mProxyList)
        users.clear()
        users.addAll(setOfUsers as TreeSet<String>)
    }

    private fun buildProxyUserData(user: String, drugs: List<Drug>?) {
        val temp: MutableList<Drug> = ArrayList()
        if (null != drugs && drugs.isNotEmpty()) {
            for (time in listOfTimes) {
                mLateDrugsMap.clear()
                for (i in drugs.indices) {
                    if (time == drugs[i].scheduledTime.gmtMilliseconds) {
                        temp.add(drugs[i])
                    }
                }
                if (temp.isNotEmpty()) {
                    mLateDrugsMap[time] = temp
                }
            }
        }
    }

    override fun onPopUpShown(popupMenu: PopupMenu?) {
        reminderPopUpShown = true
        reminderPopupMenu = popupMenu
    }
    private fun setFooterButtons() {
        if (isMultipleDrugs()) {
            if (isActionTakenDrugAvailable()) {
                binding.btnSkipAll.text = resources.getString(R.string.skip_the_rest_btn)
                binding.btnTakenAll.text = resources.getString(R.string.take_the_rest_btn)
            } else {
                binding.btnTakenAll.text = resources.getString(R.string.reminder_taken_all)
                binding.btnSkipAll.text = resources.getString(R.string.reminder_skip_all)
            }
        } else {
            binding.btnTakenAll.text = resources.getString(R.string.take)
            binding.btnSkipAll.text = resources.getString(R.string.skipped)
        }
    }
    fun isActionTakenDrugAvailable():Boolean {
        if (allDrugs.isNotEmpty()) {
            for (d in allDrugs) {
                if (null != d.guid && d.getmAction() != PillpopperConstants.NO_ACTION_TAKEN && currentUserId.equals(d.userID!!,ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    private fun buildPrimaryUserData(primaryUserId: String, allDrugs: List<Drug>) {
        val temp: MutableList<Drug> = ArrayList()
        for (time in listOfTimes) {
            for (i in allDrugs.indices) {
                if (time == allDrugs[i].scheduledTime.gmtMilliseconds) {
                    if (isValid(time, temp)) {
                        temp.add(allDrugs[i])
                    }
                }
            }
            if (temp.isNotEmpty()) {
                mLateDrugsMap[time] = temp
            }
        }
    }

    private fun isValid(time: Long, temp: List<Drug>): Boolean {
        for (d in temp) {
            if (d.scheduledTime.gmtSeconds == time) {
                return false
            }
        }
        return true
    }

    private fun isMultipleDrugs(): Boolean {
        if (lateReminderViewModel.masterHashMap.isNotEmpty()) {
            //       list will hold the keySet of outer hashmap with string as key and hashmap objects as values.
            val list: List<String> = ArrayList<String>(lateReminderViewModel.masterHashMap.keys)
            //        longList will hold the keySet of inner hashmap with key as long(times in gmtSeconds) and list of drugs as values.
            var longList: List<Long?>? = null
            if (null != lateReminderViewModel.masterHashMap[list[0]]) {
                longList = ArrayList<Long>(lateReminderViewModel.masterHashMap[list[0]]!!.keys)
            }
            if (lateReminderViewModel.masterHashMap.isNotEmpty()) {
//                if he has more than 1 medication for a single time we return true to show Taken/ skipped all actions
//                else if there are more than one time instances we need to show Taken/ skipped all actions returns true.
                if (null != lateReminderViewModel.masterHashMap[list[0]] &&
                        lateReminderViewModel.masterHashMap[list[0]]!![longList!![0]]!!.size > 1) {
                    return true
                } else if (null != longList && longList.size > 2) {
                    //checking for size > 2 because of the empty layout added at the end
                    return true
                }
            }
        }
        return false
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                skipRequestCode -> {
                    lateReminderViewModel.mRemovalTimeList.clear()
                    AppConstants.SHOW_SAVED_ALERT = true
                    if (finalDrugs.isNotEmpty()) {
                        updateDrugsAsSkipped(finalDrugs)
                        updatePassedReminderTable(finalDrugs)
                        if (AppConstants.isByPassLogin()) {
                            FrontController.getInstance(_thisActivity).performSkipDrug_pastRemindersForQuickView(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.btnSkipAll.text.toString())
                            StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
                        } else {
                            FrontController.getInstance(_thisActivity).performSkipDrug_pastRemindersForQuickView(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.btnSkipAll.text.toString())
                        }
                    }
                    if (lateReminderViewModel.masterHashMap.size > 0) {
                        val topUserId: String = lateReminderViewModel.masterHashMap.keys.iterator().next()
                        lateReminderViewModel.masterHashMap.remove(topUserId)
                        PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.remove(topUserId)
                        if (lateReminderViewModel.masterHashMap.size > 0) {
                            currentUserId = lateReminderViewModel.masterHashMap.keys.iterator().next()
                            refreshMemberData(currentUserId)
                        } else {
                            FrontController.getInstance(_thisActivity).updateAsNoPendingReminders(_thisActivity)
                            if(AppConstants.MEDS_TAKEN_OR_POSTPONED) {
                                showSavedResponseAlert()
                            }else {
                                ActivationController.getInstance().performSignoff(_thisActivity)
                            }
                        }
                    } else {
                        FrontController.getInstance(_thisActivity).updateAsNoPendingReminders(_thisActivity)
                        if (AppConstants.isByPassLogin()) {
                            if(AppConstants.MEDS_TAKEN_OR_POSTPONED) {
                                showSavedResponseAlert()
                            }else{
                                ActivationController.getInstance().performSignoff(_thisActivity)
                            }
                        } else {
                            PillpopperRunTime.getInstance().isLauchingFromPast = true
                            checkForEmptyRemindersMasterHashMap()
                            if(AppConstants.MEDS_TAKEN_OR_POSTPONED) {
                                showSavedResponseAlert()
                            }else {
                                _thisActivity.finish()
                            }
                        }
                    }
                }
                SAVED_ALERT_REQUEST_CODE -> {
                    LoggerUtils.info("Alerts - calling performSignOff 555")
                    ActivationController.getInstance().performSignoff(_thisActivity)
                }
                else -> {
                    doTakeDrugs()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            isRequiredSave = false
        }
    }
    private fun doTakeDrugs() {
        if (finalDrugs.isNotEmpty()) {
            updatePassedReminderTable(finalDrugs)
            if (AppConstants.isByPassLogin()) {
                FrontController.getInstance(_thisActivity).performTakeDrug_pastRemindersForQuickView(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.btnTakenAll.text.toString())
                StateDownloadIntentService.startActionNonSecureIntermediateGetState(_thisActivity)
            } else {
                FrontController.getInstance(_thisActivity).performTakeDrug_pastRemindersForQuickView(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.QUICKVIEW,binding.btnTakenAll.text.toString())
            }
            if (lateReminderViewModel.masterHashMap.size > 0) {
                val topUserId: String = lateReminderViewModel.masterHashMap.keys.iterator().next()
                lateReminderViewModel.masterHashMap.remove(topUserId)
                PillpopperRunTime.getInstance().passedReminderersHashMapByUserId.remove(topUserId)
                if (lateReminderViewModel.masterHashMap.size > 0) {
                    currentUserId = lateReminderViewModel.masterHashMap.keys.iterator().next()
                    refreshMemberData(currentUserId)
                    setFooterButtons()
                }
            } else {
                if (AppConstants.isByPassLogin()) {
                    ActivationController.getInstance().performSignoff(_thisActivity)
                } else {
                    PillpopperRunTime.getInstance().isLauchingFromPast = true
                    _thisActivity.finish()
                }
                PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
                FrontController.getInstance(_thisActivity).updateAsNoPendingReminders(_thisActivity)
            }
        }
        checkForEmptyRemindersMasterHashMap()
    }

    private fun updatePassedReminderTable(finalDrugs: List<Drug>) {
        for (drug in finalDrugs) {
            FrontController.getInstance(_thisActivity).removeActedPassedReminderFromReminderTable(drug.guid, drug.scheduledTime.gmtMilliseconds.toString(), _thisActivity)
        }
    }
    private fun checkForEmptyRemindersMasterHashMap() {
        if (lateReminderViewModel.masterHashMap.isEmpty()) {
            if (AppConstants.isByPassLogin()) {
                ActivationController.getInstance().performSignoff(_thisActivity)
            } else {
                _thisActivity.finish()
            }
            PillpopperConstants.setIsCurrentReminderRefreshRequired(true)
            FrontController.getInstance(_thisActivity).updateAsNoPendingReminders(_thisActivity)
        }
    }

    fun signInClicked(){
        ActivationController.getInstance().performSignoff(_thisActivity)
    }

    fun onTakenAllClicked(){
        AppConstants.MEDS_TAKEN_OR_POSTPONED = true
        lateReminderViewModel.mRemovalTimeList.clear()
        finalDrugs = ArrayList()
        for (d in drugsToBeActed) {
            // If Not acted upon this drug then add it to final drugs list
            if (null != d.guid && d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                d.setmAction(PillpopperConstants.TAKEN)
                finalDrugs.add(d)
            }
        }

        /*if (finalDrugs.isNotEmpty()) {
            updatePassedReminderTable(finalDrugs)
        } */

        if (lateReminderViewModel.masterHashMap.size == 1) {
            if(AppConstants.SHOW_SAVED_ALERT){
                val intent = Intent(this, ReminderAlertActivity::class.java)
                intent.putExtra(AppConstants.LAUNCH_MODE, resources.getString(R.string.save_title))
                intent.putExtra(AppConstants.ACTION_TITLE, resources.getString(R.string.save_title))
                intent.putExtra(AppConstants.ACTION_MESSAGE, resources.getString(R.string.save_alert_msg))
                startActivityForResult(intent, 0)
            }else {
                showResponseAlert()
            }
        } else {
            doTakeDrugs()
        }
    }
    private fun showResponseAlert(){
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
    fun outsideLayoutClicked(){
        // To prevent background item click with gradient effect
    }

    fun onSkippedAllClicked(){
        finalDrugs = ArrayList()
        for (d in drugsToBeActed) {
            // If Not acted upon this drug then add it to final drugs list
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
    private fun updateDrugsAsSkipped(drugList: List<Drug>) {
        for (d in drugList) {
            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                d.setmAction(PillpopperConstants.SKIPPED)
            }
        }
    }
}