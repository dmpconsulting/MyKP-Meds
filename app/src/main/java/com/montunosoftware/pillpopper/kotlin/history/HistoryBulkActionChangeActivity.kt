package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryBulkChangesScreenBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.StateListenerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.android.view.DialogHelpers
import com.montunosoftware.pillpopper.android.view.DialogHelpers.Confirm_CancelListener
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.database.model.HistoryEditEvent
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil

class HistoryBulkActionChangeActivity : StateListenerActivity() {
    private var historyBulkChangesScreenBinding: HistoryBulkChangesScreenBinding? = null
    private var historyEventList: ArrayList<HistoryEvent>? = null
    private lateinit var historyEvents: ArrayList<HistoryEvent>
    private lateinit var listAdapter: HistoryActionChangeAdapter
    private var mHistoryEditEvent: HistoryEditEvent? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        historyBulkChangesScreenBinding = DataBindingUtil.setContentView(this, R.layout.history_bulk_changes_screen)
        // to suppress the inApp in confirm screen
        RunTimeData.getInstance().isLoadingInProgress = true
        initValues()
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(this, FireBaseConstants.ScreenEvent.SCREEN_HISTORY_EDIT)
    }

    private fun initValues() {
        historyBulkChangesScreenBinding?.robotoBold = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_BOLD)
        historyBulkChangesScreenBinding?.robotoMedium = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM)
        historyBulkChangesScreenBinding?.activityContext = this
        val intent = intent
        historyEventList = intent.getSerializableExtra("historyEventsList") as ArrayList<HistoryEvent>?

    }

    override fun onResume() {
        super.onResume()
        val eventDay = Util.getEventDay(historyEventList!![0].headerTime)
        val eventTime = Util.getEventTime(this, historyEventList!![0].headerTime)
        historyBulkChangesScreenBinding?.tvDate?.text = eventDay
        historyBulkChangesScreenBinding?.tvTime?.text = eventTime
        if(historyEventList!=null){
            listAdapter = HistoryActionChangeAdapter(historyEventList!!, _thisActivity)
            historyBulkChangesScreenBinding?.adapter = listAdapter
        }

    }

    private fun checkForNewlyActionTakenMeds() {
        historyEvents = ArrayList()
        for (historyEvent in historyEventList!!) {
            if (!Util.isEmptyString(historyEvent.actionType)) {
                historyEvents.add(historyEvent)
            }
        }
    }

    private fun setActionTypeToNull() {
        for (historyEvent in historyEvents) {
            historyEvent.actionType = null
        }
    }

    fun onSaveClicked() {
        checkForNewlyActionTakenMeds()
        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(this, FireBaseConstants.Event.EVENT_HISTORY_EDIT_SAVE)
        if (historyEvents.isNotEmpty()) {
            try {
                for (historyEvent in historyEvents) {
                    mHistoryEditEvent = FrontController.getInstance(this).getHistoryEditEventDetails(historyEvent.historyEventGuid.toString())
                    mHistoryEditEvent?.pillOperation = historyEvent.actionType
                    mHistoryEditEvent?.preferences?.recordDate = Util.convertDateLongToIso(PillpopperTime.now().gmtSeconds.toString())
                    if (historyEvent.actionType == PillpopperConstants.ACTION_TAKE_PILL_HISTORY) {
                        mHistoryEditEvent?.preferences?.actionDate = historyEvent.preferences.actionDate
                    }
                    if (historyEvent.operationStatus.equals(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY, ignoreCase = true) || historyEvent.operationStatus.equals(PillpopperConstants.ACTION_REMINDER_PILL_HISTORY, ignoreCase = true)) {
                        val drug = FrontController.getInstance(this).getDrugByPillId(historyEvent.pillID)
                        if (historyEvent.operationStatus.equals(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY, ignoreCase = true)) {
                            FrontController.getInstance(this).updateHistoryEventPreferences(mHistoryEditEvent?.historyEventGuid, historyEvent.actionType, drug, drug.postponeSeconds)
                            FrontController.getInstance(this).addLogEntry(this, Util.prepareLogEntryForEditHistoryEvent(this, mHistoryEditEvent, historyEvent.actionType))
                        } else {
                            DatabaseUtils.getInstance(this).deleteEmptyHistoryEvent(mHistoryEditEvent?.pillHistoryCreationDate, drug.guid)
                        }
                        if (null != drug.scheduledTime && !Util.isEmptyString(drug.scheduledTime.gmtMilliseconds.toString())) {
                            FrontController.getInstance(this).removeActedPassedReminderFromReminderTable(drug.guid, drug.scheduledTime.gmtMilliseconds.toString(), this)
                        }
                        drug.historyScheduleDate = historyEvent.headerTime
                        AppConstants.updateNotifyAfterValue = true
                        if (historyEvent.actionType.equals(PillpopperConstants.ACTION_TAKE_PILL, ignoreCase = true)) {
                            if (null != mHistoryEditEvent!!.preferences.finalPostponedDateTime && Util.convertDateIsoToLong(mHistoryEditEvent!!.preferences.finalPostponedDateTime) < (PillpopperTime.now().gmtSeconds.toString())) {
                                // set action date to finalPostponedDateTime if the time has exceeded the finalPostponedDateTime
                                drug.actionDate = mHistoryEditEvent!!.preferences.finalPostponedDateTime
                            } else {
                                drug.actionDate = Util.convertDateLongToIso(historyEvent.headerTime)
                            }
                            drug.isActionDateRequired = true
                            FrontController.getInstance(this).performTakeDrug(arrayListOf<Drug>(drug), PillpopperTime.now(), this, true, FireBaseConstants.ParamValue.HISTORY_EDIT_SCREEN)
                        } else {
                            FrontController.getInstance(this).performSkipDrug(arrayListOf<Drug>(drug), PillpopperTime.now(), this, true, FireBaseConstants.ParamValue.HISTORY_EDIT_SCREEN)
                        }
                    } else {
                        FrontController.getInstance(this).updateHistoryEvent(mHistoryEditEvent?.historyEventGuid,
                                historyEvent.actionType,
                                mHistoryEditEvent?.pillEventDescription,
                                mHistoryEditEvent?.pillHistoryCreationDate)
                        FrontController.getInstance(this).updateActionAndRecordDateHistoryPreference(mHistoryEditEvent)
                        if (historyEvent.actionType.equals(PillpopperConstants.ACTION_TAKE_PILL, ignoreCase = true)) {
                            mHistoryEditEvent!!.isActionDateRequired = true
                        }
                        invokeFireBaseEvent(this,historyEvent.actionType)
                        FrontController.getInstance(this).addLogEntry(this, Util.prepareLogEntryForEditHistoryEvent(this, mHistoryEditEvent, null))
                    }
                }
                RunTimeData.getInstance().isOverlayItemClicked = true
                RunTimeData.getInstance().isHistoryMedChanged = true
                RunTimeData.getInstance().isHistoryItemUpdated = true
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("HISTORY_MED_CHANGED"))
            } catch (e: Exception) {
                PillpopperLog.exception(e.message)
            }

        }
        finish()
    }
    private fun invokeFireBaseEvent(context: Context?, action: String) {
        if (null != context) {
            var triggeringAction = ""
            if (PillpopperConstants.ACTION_TAKE_PILL.equals(action, ignoreCase = true)) {
                triggeringAction = FireBaseConstants.ParamValue.TAKEN
            } else if (PillpopperConstants.ACTION_SKIP_PILL.equals(action, ignoreCase = true)) {
                triggeringAction = FireBaseConstants.ParamValue.SKIPPED
            }
            val bundle = Bundle()
            bundle.putString(FireBaseConstants.ParamName.ACTION_TYPE, triggeringAction)
            bundle.putString(FireBaseConstants.ParamName.SOURCE, FireBaseConstants.ParamValue.HISTORY_EDIT_SCREEN)
            FireBaseAnalyticsTracker.getInstance().logEvent(context, FireBaseConstants.Event.REMINDER_ACTIONS, bundle)
        }
    }

    fun onCancelClicked() {
        checkForNewlyActionTakenMeds()
        if (historyEvents.isNotEmpty()) {
            showAlertDialogOnBackPress()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        onCancelClicked()
    }

    private fun showAlertDialogOnBackPress() {
        DialogHelpers.showAlertWithSaveCancelListeners(this, R.string.save_updates, R.string.save_changes_on_exit_message,
                object : Confirm_CancelListener {
                    override fun onConfirmed() {
                        onSaveClicked()
                    }

                    override fun onCanceled() {
                        setActionTypeToNull()
                        finish()
                    }
                })
    }

    override fun onDestroy() {
        RunTimeData.getInstance().isLoadingInProgress = false
        RunTimeData.getInstance().isShouldRetainHistoryOverlay = false
        super.onDestroy()
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkForNewlyActionTakenMeds()
        if (historyEvents.isNotEmpty()) {
            setActionTypeToNull()
        }
        RunTimeData.getInstance().isHistoryConfigChanged = true
        finish()
    }
}