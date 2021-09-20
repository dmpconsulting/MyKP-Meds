package com.montunosoftware.pillpopper.kotlin.history

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryBaseNewBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.util.*
import com.montunosoftware.pillpopper.android.view.DialogHelpers
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.model.LogEntryModel
import com.montunosoftware.pillpopper.kotlin.calendarviewpager.HistoryCalendarFragment
import com.montunosoftware.pillpopper.model.*
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService
import org.json.JSONException
import org.json.JSONObject
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.model.User
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class HistoryBaseScreen : Fragment(), HistoryListAdapter.OnItemClicked {

    private var historyEventsList: List<HistoryEvent> = arrayListOf<HistoryEvent>()
    private lateinit var historyViewModel: HistoryViewModel
    private var isSettingsChanged: Boolean = false
    private var isHistoryTaskInProgress = false
    private var dialog: AlertDialog? = null
    private var lastVisiblePosition: Int = -1
    private var mBinding: HistoryBaseNewBinding? = null
    private val mBundleUserName = "userName"
    private val mBundleUserId = "userId"
    private val tagFragment = "TAG_FRAGMENT"
    private var mSelectedUserId: String = ""
    private var mSelectedUserName: String? = null
    private var mFrontController: FrontController? = null
    private lateinit var mContext: Context
    private var userList: List<User>? = null
    private var mEmailHistoryEntriesMenuItem: MenuItem? = null
    private var mHistorySelection: Int = 1
    private var historyItems: Array<String>? = null
    private var historyPeriod: Array<String>? = null
    private var dateForComparison: Date = Date()

    //private var historyEvents = arrayListOf<HistoryEvent>()
    private var lastSelectedHistoryPosition = -1
    private var mUserPreferences: UserPreferences? = null
    var historyCalendarFragment = HistoryCalendarFragment()
    private var fromOnResume = false
    private var isItemChanged = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggerUtils.info("History -- onCreateView")
        mContext = requireActivity()
        mFrontController = FrontController.getInstance(mContext)
        mBinding = DataBindingUtil.inflate(inflater, R.layout.history_base_new, container, false)
        historyViewModel = ViewModelProvider(requireActivity()).get(HistoryViewModel::class.java)
        mBinding?.handler = this
        mBinding?.robotoMedium = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_MEDIUM)
        mBinding?.robotoRegular = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_REGULAR)
        historyItems = context?.resources?.getStringArray(R.array.new_history_reminder_window)
        historyPeriod = context?.resources?.getStringArray(R.array.history_period)
        if (mFrontController!=null && !mFrontController?.allEnabledUsers.isNullOrEmpty()) {
            userList = mFrontController?.allEnabledUsers
            mUserPreferences = mFrontController!!.getUserPreferencesForUser(mFrontController?.allEnabledUsers?.get(0)?.userId)
        }
        // mUserPreferences = mFrontController!!.getUserPreferencesForUser(mFrontController?.allEnabledUsers?.get(0)?.userId)
        initSpinnerData(userList)
        registerReceivers()
        setHasOptionsMenu(true)
        setObserver()
        return mBinding?.root
    }

    private fun setObserver() {
        LoggerUtils.info("History -- setObserver ")
        historyViewModel.historyData.observe(viewLifecycleOwner, {
            LoggerUtils.info("History -- setObserver observe")
            /*the observer is being called twice on launching history module,
            * which is resulting in a glitch and empty layout being shown for some time.
            * inorder to avoid the initial empty state, using the below fromOnResume field */
            if (fromOnResume) {
                historyEventsList = it as List<HistoryEvent>
                val sortedList = arrayListOf<HistoryEvent>()
                for (historyEvent in historyEventsList) {
                    //dateForComparison
                    if (null != historyEvent && null != historyEvent.headerTime) {
                        val eventDate = Date(historyEvent.headerTime.toLong() * 1000)
                        // filtering the history events in between current time to past history settings days
                        if (eventDate.after(dateForComparison) && eventDate.before(Date())) {
                            sortedList.add(historyEvent)
                        }
                    }
                }
                sortedList.sortWith { historyEvent1: HistoryEvent, historyEvent2: HistoryEvent ->
                    Date(TimeUnit.DAYS.toMillis(historyEvent2.headerTime.toLong()))
                            .compareTo(Date(TimeUnit.DAYS.toMillis(historyEvent1.headerTime.toLong())))
                }
                historyEventsList = sortedList
                if(isSettingsChanged){
                    isSettingsChanged = false
                    mBinding?.appbar?.setExpanded(true, true)
                }
                // load the calendar first and then list to avoid glitch
                if (AppConstants.showCalendarView) {
                    installFragment()
                }

                loadHistoryData(historyEventsList)

                historyViewModel.setHistoryList(historyEventsList)
                isHistoryTaskInProgress = false
            }
        })

        historyViewModel.isSeeMoreOrLessClicked.observe(viewLifecycleOwner, {
            LoggerUtils.info("History -- setObserver seeMoreOrLessAction $it")
            if(it)
            {
                mBinding?.appbar?.setExpanded(true, true)
            }
        })
    }

    private val mGetStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            LoggerUtils.info("---History--- GetState success")
            if (!PillpopperRunTime.getInstance().isHistorySyncDone) {
                if (intent!!.hasExtra(PillpopperConstants.KEY_ACTION) && intent.getStringExtra(PillpopperConstants.KEY_ACTION) == PillpopperConstants.ACTION_HISTORY_EVENTS) {
                    LoggerUtils.info("---History--- GetHistory success")
                    PillpopperRunTime.getInstance().isHistorySyncDone = true
                    mHistorySelection = getHistoryDaysSelection()
                    if (isSettingsChanged) {
                        createLogEntryForSetPreferences()
                        if (FrontController.getInstance(context).isLogEntryAvailable) {
                            StateDownloadIntentService.startActionIntermediateGetState(context)
                        }
                        //isSettingsChanged = false
                    }
                    isHistoryTaskInProgress = false
                    //calculate reminders and add current and late reminders back to historyTable
                    Util.getInstance().prepareRemindersMapData(Util.getOverdueDrugList(mContext), mContext)

                    historyViewModel.getHistoryEvents(context, mSelectedUserId, getDoseHistoryDays().toString())
                }
            }
        }
    }

    private fun getOverdueDrugList(): List<Drug>? {
        val drugLists: MutableList<Drug> = ArrayList()
        for (d in FrontController.getInstance(mContext).getDrugListForOverDue(mContext)) {
            d.computeDBDoseEvents(mContext, d, PillpopperTime.now(), 60)
            if (d.isoverDUE() && (null == d.schedule.end
                            || d.schedule.end == PillpopperDay.today() ||
                            d.schedule.end.after(PillpopperDay.today()))) {
                if (PillpopperTime.now().gmtMilliseconds - d.overdueDate.gmtMilliseconds < 24 * 60 * 60 * 1000) {
                    drugLists.add(d)
                }
            }
        }
        return drugLists
    }

    private val mGetStateFailedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            LoggerUtils.info("---History--- Get State/Get History failure")
            isSettingsChanged = false
            mEmailHistoryEntriesMenuItem!!.isVisible = false
            mHistorySelection = lastSelectedHistoryPosition
            updatePreferences()
            if (historyEventsList.isEmpty()) {
                if (-1 == mHistorySelection) {
                    mHistorySelection = getHistoryDaysSelection()
                }
            }
            UIUtils.dismissProgressDialog()
            isHistoryTaskInProgress = false
            DialogHelpers.showAlertDialog(activity, R.string.alert_error_status_20)
        }
    }

    private fun initSpinnerData(list: List<User>?) {
        list?.let { listOfUsers ->
            mSelectedUserId = listOfUsers[0].userId.toString()
            mSelectedUserName = listOfUsers[0].firstName
            mBinding?.usersSpinner?.isEnabled = listOfUsers.size > 1
            mBinding?.imgSpinnerDownArrow?.visibility = if (listOfUsers.size > 1) View.VISIBLE else View.GONE
            mBinding?.usersSpinner?.adapter = activity?.let { ProxySpinnerAdapter(it, listOfUsers) }
            mBinding?.usersSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    mSelectedUserId = listOfUsers[position].userId
                    mSelectedUserName = listOfUsers[position].firstName
                    if (!isHistoryTaskInProgress) {
                        // HistoryTask().execute()
                        if (!isHistoryTaskInProgress) {
                            isHistoryTaskInProgress = true
                            historyViewModel.getHistoryEvents(context, mSelectedUserId, getDoseHistoryDays().toString())
                            isItemChanged = true
                        }
                    }
                }
            }
        }
    }

    private fun installFragment() {
        val fragmentTransaction: FragmentTransaction = activity?.supportFragmentManager?.beginTransaction()!!
        historyCalendarFragment = HistoryCalendarFragment()
        val bundle = Bundle()
        bundle.putString(mBundleUserId, mSelectedUserId)
        bundle.putString(mBundleUserName, mSelectedUserName)
        historyCalendarFragment.arguments = bundle
        fragmentTransaction.replace(R.id.historyCalendar, historyCalendarFragment, tagFragment)
        fragmentTransaction.commit()
    }

    private fun loadHistoryData(historyList: List<HistoryEvent>) {
        LoggerUtils.info("History -- loadHistoryData")
        mBinding?.mainContent?.visibility = View.GONE
        val mRelMain: RelativeLayout? = mBinding?.relMain
        val params: AppBarLayout.LayoutParams = mRelMain?.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
        mRelMain.layoutParams = params
        if (historyList.isNotEmpty()) {
            setAdapter(historyList, getDoseHistoryFromSettingsForFooter())
        } else {
            setAdapter(historyList, "")
        }
        mHistorySelection = getHistoryDaysSelection()
        mEmailHistoryEntriesMenuItem?.isVisible = historyList.isNotEmpty()
        isHistoryTaskInProgress = false
    }

    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    private fun setAdapter(historyEvents: List<HistoryEvent>, doseHistoryFromSettingsForFooter: String?) {
        LoggerUtils.info("---History--- setAdapter")
        try {
            thread(true) {
                val adapter = HistoryListAdapter(mContext, historyEvents, doseHistoryFromSettingsForFooter, this, historyPeriod?.get(mHistorySelection))
                LoggerUtils.info("---HistoryAdapter--- $adapter")
                mainHandler.post {
                    mBinding?.historyRecyclerView?.setHasFixedSize(true)
                    mBinding?.historyRecyclerView?.adapter = adapter
                    mBinding?.historyRecyclerView?.visibility = View.VISIBLE

                    if(isItemChanged) {
                        mBinding?.appbar?.setExpanded(true, true)
                        isItemChanged = false
                    }

                    if (-1 != lastVisiblePosition) {
                        (mBinding?.historyRecyclerView?.layoutManager as LinearLayoutManager).scrollToPosition(lastVisiblePosition)
                        lastVisiblePosition = -1
                    }
                    mBinding?.historyRecyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
                }
                requireActivity().runOnUiThread {
                    UIUtils.dismissProgressDialog()
                    mBinding?.mainContent?.let { fadeIn(it) }
                }
            }
        } catch (e: Exception) {
            PillpopperLog.exception(e.message)
            PillpopperLog.say("Exception from setAdapter ${e.message}")
        } finally {
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun fadeIn(contentView: View) {
        var animFadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        animFadeIn.duration = 300 // 1sec
        animFadeIn.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation) {
                contentView.visibility = View.VISIBLE
                RunTimeData.getInstance().isHistoryMedChanged = false
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
        view?.startAnimation(animFadeIn)
    }

    override fun onResume() {
        super.onResume()
        LoggerUtils.info("History -- onResume")
        fromOnResume = true
        mHistorySelection = getHistoryDaysSelection()
        mBinding?.mainContent?.visibility = View.GONE
        if (Util.isNetworkAvailable(mContext)) {
            if (historyEventsList.isEmpty() || RunTimeData.getInstance().isHistoryMedChanged) {
                if (!isHistoryTaskInProgress) {
                    isHistoryTaskInProgress = true
                    historyViewModel.getHistoryEvents(context, mSelectedUserId, getDoseHistoryDays().toString())
                }
            } else {
                setAdapter(historyEventsList, getDoseHistoryFromSettingsForFooter())
            }
        } else {
            showDataNotAvailableAlert()
        }
    }

    private fun showDataNotAvailableAlert() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setTitle(getString(R.string.data_unavailable))
        builder.setMessage(getString(R.string.network_connection_error))
        builder.setPositiveButton("Ok") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        dialog = builder.create()
        if (!requireActivity().isFinishing) {
            dialog?.show()
            RunTimeData.getInstance().alertDialogInstance = dialog
        }
    }

    private fun registerReceivers() {

        LoggerUtils.info("---History--- History List screen register receivers")
        val getStateIntentFilter = IntentFilter()
        getStateIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED)
        requireActivity().registerReceiver(mGetStateReceiver, getStateIntentFilter)

        val getHistoryIntentFilter = IntentFilter()
        getHistoryIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_FAILED)
        requireActivity().registerReceiver(mGetStateFailedReceiver, getHistoryIntentFilter)
    }

    private fun getDoseHistoryDays(): Long {
        val curDateInDays = TimeUnit.MILLISECONDS.toDays(Date().time)
        val freqDays: Long = curDateInDays - mFrontController?.doseHistoryDays!!
        dateForComparison = Date(TimeUnit.DAYS.toMillis(freqDays))
        return dateForComparison.time / 1000
    }


    private fun updatePreferences() {
        val currentSelection: Int = getHistorySelection(mHistorySelection)
        LoggerUtils.info("---History--- History selected value $mHistorySelection")
        mFrontController!!.setDoseHistoryDaysForUser(currentSelection, mFrontController!!.primaryUserIdIgnoreEnabled)
    }

    private fun getDoseHistoryFromSettingsForFooter(): String? {
        var footerText = ""
        val doseHistoryDays = FrontController.getInstance(activity).doseHistoryDays
        if (doseHistoryDays == 1) { // upgrade scenario
            footerText = "14 days"
        } else if (doseHistoryDays >= 365 && doseHistoryDays % 365 == 0) {
            footerText = if (doseHistoryDays == 365) {
                "1 year"
            } else {
                (doseHistoryDays / 365).toString() + " years"
            }
        } else if (doseHistoryDays in 30..364 && doseHistoryDays % 30 == 0) {
            footerText = if (doseHistoryDays == 30) {
                "1 month"
            } else {
                (doseHistoryDays / 30).toString() + " months"
            }
        } else if (doseHistoryDays < 30) {
            footerText = "$doseHistoryDays days"
        }
        return footerText
    }

    private fun getHistoryDaysSelection(): Int {
        return when (mFrontController?.doseHistoryDays) {
            1, 14 -> 0
            30 -> 1
            90 -> 2
            365 -> 3
            730 -> 4
            else -> {
                mHistorySelection = 1
                1
            }
        }
    }

    private fun getHistorySelection(selectedItem: Int): Int {
        val historySpan: Int
        when (selectedItem) {
            0 -> {
                historySpan = 14
            }
            1 -> {
                historySpan = 30
            }
            2 -> {
                historySpan = 90
            }
            3 -> {
                historySpan = 365
            }
            4 -> {
                historySpan = 730
            }
            else -> {
                historySpan = 14
            }
        }
        return historySpan
    }

    /**
     * Utility method to create the log entry with action = "SetPreferences"
     *
     * @param preferenceJSONObject JSONObject
     */
    private fun createLogEntryForSetPreferences() {
        try {
            val preferenceJSONObject = JSONObject()
            preferenceJSONObject.put("userData", SharedPreferenceManager.getInstance(activity, AppConstants.AUTH_CODE_PREF_NAME).getString(AppConstants.KP_GUID, null))
            preferenceJSONObject.put(PillpopperConstants.ACTION_SETTINGS_HISTORY_DAYS, getHistorySelection(mHistorySelection))
            createLogEntry(preferenceJSONObject)
        } catch (e: JSONException) {
            PillpopperLog.say("Exception in create log entry method", e)
        }
    }

    private fun createLogEntry(preferences: JSONObject) {
        val replyId = Util.getRandomGuid()
        val logEntryModel = LogEntryModel()
        logEntryModel.dateAdded = System.currentTimeMillis()
        logEntryModel.replyID = replyId
        val jsonObj = if (mUserPreferences != null) {
            Util.prepareSettingsAction(preferences, replyId, mUserPreferences?.userId, activity)
        } else {
            Util.prepareSettingsAction(preferences, replyId, mFrontController?.primaryUserIdIgnoreEnabled, activity)
        }
        logEntryModel.setEntryJSONObject(jsonObj, activity)
        FrontController.getInstance(activity).addLogEntry(activity, logEntryModel)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.history_main_menu, menu)
        mEmailHistoryEntriesMenuItem = menu.findItem(R.id.history_menu_email)
        mEmailHistoryEntriesMenuItem!!.isVisible = historyEventsList.isNotEmpty()
        menu.findItem(R.id.history_menu_email).setOnMenuItemClickListener {

            FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(activity, FireBaseConstants.Event.HISTORY_LIST_SHARE)
            Util.NavDrawerUtils.closeNavigationDrawerIfOpen()
            onEmailButtonClick()
            return@setOnMenuItemClickListener true
        }
    }

    private fun onEmailButtonClick() {
        if (PermissionUtils.checkVersionCode()) {
            if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, activity as PillpopperActivity?)) {
                HistoryEmailHelper.emailHistoryOrComplain(activity as PillpopperActivity?, historyEventsList, mSelectedUserName, getDoseHistoryFromSettingsForFooter())
            }
        } else {
            HistoryEmailHelper.emailHistoryOrComplain(activity as PillpopperActivity?, historyEventsList, mSelectedUserName, getDoseHistoryFromSettingsForFooter())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted,
            if (requestCode == AppConstants.PERMISSION_WRITE_EXTERNAL_STORAGE) {
                HistoryEmailHelper.emailHistoryOrComplain(mContext as PillpopperActivity, historyEventsList, mSelectedUserName, getDoseHistoryFromSettingsForFooter())
            }
        } else {
            //permission Denied
            if (permissions.isNotEmpty()) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mContext as PillpopperActivity, permissions[0])) {
                    if (requestCode == AppConstants.PERMISSION_WRITE_EXTERNAL_STORAGE) {
                        onPermissionDenied(requestCode)
                    }
                } else {
                    if (requestCode == AppConstants.PERMISSION_WRITE_EXTERNAL_STORAGE) {
                        onPermissionDeniedNeverAskAgain(requestCode)
                    }
                }
            }
        }
    }

    private fun onPermissionDenied(requestCode: Int) {
        val message = PermissionUtils.permissionDeniedMessage(requestCode, mContext as PillpopperActivity)
        PermissionUtils.permissionDeniedDailogue(mContext as PillpopperActivity, message)
    }

    private fun onPermissionDeniedNeverAskAgain(requestCode: Int) {
        val message = PermissionUtils.permissionDeniedMessage(requestCode, mContext as PillpopperActivity)
        PermissionUtils.permissionDeniedDailogueForNeverAskAgain(mContext as PillpopperActivity, message)
    }

    override fun onDestroy() {
        UIUtils.dismissProgressDialog()
        if(!requireActivity().isFinishing) {
            requireActivity().supportFragmentManager.beginTransaction().remove(historyCalendarFragment).commitAllowingStateLoss();
        }
        requireActivity().unregisterReceiver(mGetStateReceiver)
        requireActivity().unregisterReceiver(mGetStateFailedReceiver)
        super.onDestroy()
    }

    override fun onItemClick(historyEvent: HistoryEvent) {
        val intent = Intent(activity, HistoryDetailActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("historyEvent", historyEvent)
        bundle.putString(mBundleUserName, mSelectedUserName)
        intent.putExtras(bundle)
        // reset the history med change flag.
        RunTimeData.getInstance().isHistoryMedChanged = false
        activity?.startActivity(intent)
    }

    override fun onSettingClicked() {
        val builder = AlertDialog.Builder(mContext)
        lastSelectedHistoryPosition = mHistorySelection
        builder.setTitle(mContext.getString(R.string.history))
                .setSingleChoiceItems(historyItems, getHistoryDaysSelection()) { _, which: Int -> mHistorySelection = which }
                .setPositiveButton(mContext.resources.getString(R.string.ok_text)) { dialog: DialogInterface?, _: Int ->
                    val previousSelection = mFrontController?.doseHistoryDays
                    if (previousSelection != getHistorySelection(mHistorySelection)) {
                        Util.firebaseEventForShowHistory(mHistorySelection,context,FireBaseConstants.ParamValue.HISTORY_LIST_SCREEN)
                        try {
                            isSettingsChanged = true
                            PillpopperRunTime.getInstance().isHistorySyncDone = false
                            updatePreferences()
                            //  HistoryTask().execute()
                            historyViewModel.getHistoryEvents(context, mSelectedUserId, getDoseHistoryDays().toString())
                        } catch (e: JSONException) {
                            PillpopperLog.exception("JSONException in showHistorySelectionDialog method")
                        } catch (e: Exception) {
                            PillpopperLog.exception("Exception in showHistorySelectionDialog method")
                        }
                    }
                    dialog?.dismiss()
                }.setNegativeButton(mContext.resources.getString(R.string.cancel_text)) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    mHistorySelection = lastSelectedHistoryPosition
                }
        builder.create()

        val alert = builder.create()
        RunTimeData.getInstance().alertDialogInstance = alert
        alert.show()

        val btnPositive = alert.findViewById<Button>(android.R.id.button1)
        val btnNegative = alert.findViewById<Button>(android.R.id.button2)

        btnPositive!!.setTextColor(Util.getColorWrapper(mContext, R.color.kp_theme_blue))
        btnNegative!!.setTextColor(Util.getColorWrapper(mContext, R.color.kp_theme_blue))
    }

    override fun onPause() {
        super.onPause()
        lastVisiblePosition = (mBinding?.historyRecyclerView?.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (null != dialog && dialog?.isShowing!!) {
            dialog?.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lastVisiblePosition = -1
    }
}