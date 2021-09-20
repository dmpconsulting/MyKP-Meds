package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.UIUtils
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.kotlin.history.horizontalRecyclerHistory.adapter.CalendarRecyclerViewDataAdapterNew
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.model.User

class HistoryViewModel : ViewModel() {

    var historyData = MutableLiveData<List<HistoryEvent>>()
    var userData = MutableLiveData<List<User>>()
    var historyDetail = MutableLiveData<HistoryEvent>()
    var historyEventsList = MutableLiveData<List<HistoryEvent>>()
    var isSeeMoreOrLessClicked = MutableLiveData(false)

    // Calling the AsyncTask for the HistoryEvent
    fun getHistoryEvents(context: Context?, userId: String, doseHistoryDays: String) {
        HistoryTask(historyData, context!!, userId, doseHistoryDays).execute()
    }

    fun getUsersList(context: Context?) {
        userData.postValue(FrontController.getInstance(context).allEnabledUsers)
    }

    fun onItemClicked(historyEvent: HistoryEvent?) {
        historyDetail.postValue(historyEvent!!)
    }

    fun setHistoryList(historyEvents: List<HistoryEvent>) {
        historyEventsList.postValue(historyEvents)
    }
    // focus on click of see more or less only
    fun getSeeMoreOrLessClickAction(isClicked: Boolean?)
    {
        isSeeMoreOrLessClicked.postValue(isClicked!!)
    }

    private class HistoryTask(var historyData: MutableLiveData<List<HistoryEvent>>, var context: Context, var userId: String, var historyDays: String) : AsyncTask<String?, String?, String>() {
        var historyEvents = ArrayList<HistoryEvent>()
        override fun doInBackground(vararg params: String?): String? {
            var isHistorySyncDone = "false"
            if (PillpopperRunTime.getInstance().isHistorySyncDone) {
                isHistorySyncDone = "true"
                historyEvents = FrontController.getInstance(context).getHistoryEvents(userId, historyDays) as ArrayList<HistoryEvent>
                RunTimeData.getInstance().isHistoryMedChanged = false
            }
            return isHistorySyncDone
        }

        override fun onPostExecute(result: String) {
            if (result.equals("false", ignoreCase = true)) {
                if (FrontController.getInstance(context).isLogEntryAvailable) {
                    PillpopperLog.say("Starting Intermediate Sync, Get State Events")
                    StateDownloadIntentService.startActionIntermediateGetState(context)
                }
                StateDownloadIntentService.startActionGetHistoryEvents(context)
                StateDownloadIntentService.handleHistoryFailure(true)
            } else {
                historyData.postValue(historyEvents)
            }

        }


        override fun onPreExecute() {
            if(!RunTimeData.getInstance().isHistoryOverlayShown)
                UIUtils.showProgressDialog(context, context.resources.getString(R.string.progress_msg))
        }
    }

    fun groupSimilarOperationStatus(historyEvent: ArrayList<HistoryEvent>): ArrayList<String> {
        val orderedArr = ArrayList<String>()
        val filteredHistoryEvent = filteringOperationStatus(historyEvent)
        for (i in 0..6) {
            orderedArr.add("NONE")
        }
        for ((key, operationStatus) in filteredHistoryEvent) {
            val day = Util.getEventDay(key)
            when (day.substring(0, 3)) {
                "Sun" -> {
                    orderedArr[0] = operationStatus
                }
                "Mon" -> {
                    orderedArr[1] = operationStatus
                }
                "Tue" -> {
                    orderedArr[2] = operationStatus
                }
                "Wed" -> {
                    orderedArr[3] = operationStatus
                }
                "Thu" -> {
                    orderedArr[4] = operationStatus
                }
                "Fri" -> {
                    orderedArr[5] = operationStatus
                }
                "Sat" -> {
                    orderedArr[6] = operationStatus
                }
            }
        }
        return orderedArr
    }

    private fun filteringOperationStatus(historyEventList: List<HistoryEvent>): HashMap<String, String> {
        /**
         * if all the pills are taken [Taken]
         * if all the pills are skipped then [Skipped]
         * if all are postponed or missed then [Missed]
         * if some of the pills are taken or some are skipped or some are missed then [Mixed]
         * if the day is after the current day we have to assign an upcoming Drawable [Upcoming] or [future]
         */
        val distinctHistoryEvent = historyEventList.distinctBy { Pair(it.headerTime, it.operationStatus) }
        val pillActionMap = HashMap<String, CalendarRecyclerViewDataAdapterNew.PillAction>()
        for (events in distinctHistoryEvent) {
            if (pillActionMap.containsKey(events.headerTime)) {
                pillActionMap.getValue(events.headerTime).operationAction.add(events.operationStatus)
                when (events.operationStatus) {
                    PillpopperConstants.ACTION_TAKE_PILL_HISTORY -> {
                        pillActionMap.getValue(events.headerTime).taken = true
                    }
                    PillpopperConstants.ACTION_MISS_PILL_HISTORY -> {
                        pillActionMap.getValue(events.headerTime).missed = true
                    }
                    PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY -> {
                        pillActionMap.getValue(events.headerTime).postponed = true
                    }
                    PillpopperConstants.ACTION_SKIP_PILL_HISTORY -> {
                        pillActionMap.getValue(events.headerTime).skipped = true
                    }
                    PillpopperConstants.ACTION_REMINDER_PILL_HISTORY -> {
                        pillActionMap.getValue(events.headerTime).reminder = true
                    }
                    PillpopperConstants.ACTION_UPCOMING_PILL_HISTORY -> {
                        pillActionMap.getValue(events.headerTime).upcoming = true
                    }
                    // I Think this one is causing some issues
                    AppConstants.HISTORY_OPERATION_EMPTY -> {
                        pillActionMap.getValue(events.headerTime).missed = true
                    }
                }
            } else {
                val pillOperationArray = ArrayList<String>()
                pillOperationArray.add(events.operationStatus)
                val pillAction = CalendarRecyclerViewDataAdapterNew.PillAction(pillOperationArray, taken = false, missed = false, skipped = false, postponed = false, upcoming = false)
                when (events.operationStatus) {
                    PillpopperConstants.ACTION_TAKE_PILL_HISTORY -> {
                        pillAction.taken = true
                    }
                    PillpopperConstants.ACTION_MISS_PILL_HISTORY -> {
                        pillAction.missed = true
                    }
                    PillpopperConstants.ACTION_SKIP_PILL_HISTORY -> {
                        pillAction.skipped = true
                    }
                    PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY -> {
                        pillAction.postponed = true
                    }
                    PillpopperConstants.ACTION_REMINDER_PILL_HISTORY -> {
                        pillAction.reminder = true
                    }
                    PillpopperConstants.ACTION_UPCOMING_PILL_HISTORY -> {
                        pillAction.upcoming = true
                    }
                }
                pillActionMap[events.headerTime] = pillAction
            }
        }
        return operationStatusFinal(pillActionMap)
    }

    private fun operationStatusFinal(hash: HashMap<String, CalendarRecyclerViewDataAdapterNew.PillAction>): HashMap<String, String> {
        val hashOfDayPillHistory = HashMap<String, String>()
        for ((keys, values) in hash) {
            if (values.taken && !values.skipped && !values.missed && !values.postponed && !values.reminder) {
                hashOfDayPillHistory[keys] = PillpopperConstants.ACTION_TAKE_PILL_HISTORY
            } else if (values.skipped && !values.taken && !values.missed &&!values.postponed && !values.reminder) {
                hashOfDayPillHistory[keys] = PillpopperConstants.ACTION_SKIP_PILL_HISTORY
            } else if (!values.skipped && !values.taken && values.missed && !values.postponed && !values.reminder) {
                hashOfDayPillHistory[keys] = PillpopperConstants.ACTION_MISS_PILL_HISTORY
            } else if (values.postponed &&!values.skipped && !values.taken && !values.missed && !values.reminder ) {
                hashOfDayPillHistory[keys] = PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY
            } else if (values.reminder && !values.postponed &&!values.skipped && !values.taken && !values.missed ) {
                hashOfDayPillHistory[keys] = PillpopperConstants.ACTION_REMINDER_PILL_HISTORY
            }  else if (values.upcoming ) {
                // this has to be
                hashOfDayPillHistory[keys] = PillpopperConstants.ACTION_UPCOMING_PILL_HISTORY
            } else {
                hashOfDayPillHistory[keys] = PillpopperConstants.ACTION_MIXED_PILL_HISTORY
            }
        }
        return hashOfDayPillHistory
    }

    /**
     * get a List o
     */
    fun getDrawableList(resultsOfHistoryEventItems: ArrayList<String>, mContext: Context): ArrayList<Drawable> {
// we have two icons that are similar which is [missed] and [empty] so  we have to have separated Icon for each
        val drawableTaken: Drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_taken)!!
        val drawableSkipped: Drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_skipped)!!
        val drawableMissed: Drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_history_missed)!!
        val emptyPillHistory: Drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_empty_pill_history)!!
        val drawableMixed: Drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_history_mixed)!!
        val drawableUpcoming: Drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_history_upcoming)!!

        val drawableList = ArrayList<Drawable>()
        for (i in 0..6) {
            when (resultsOfHistoryEventItems[i]) {
                PillpopperConstants.ACTION_TAKE_PILL_HISTORY -> {
                    drawableList.add(drawableTaken)
                }
                PillpopperConstants.ACTION_SKIP_PILL_HISTORY -> {
                    drawableList.add(drawableSkipped)
                }
                PillpopperConstants.ACTION_MISS_PILL_HISTORY -> {
                    drawableList.add(drawableMissed)
                }
                PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY -> {
                    drawableList.add(drawableMissed)
                }
                PillpopperConstants.ACTION_MIXED_PILL_HISTORY -> {
                    drawableList.add(drawableMixed)
                }
                PillpopperConstants.ACTION_REMINDER_PILL_HISTORY -> {
                    drawableList.add(drawableMissed)
                }
                PillpopperConstants.ACTION_UPCOMING_PILL_HISTORY -> {
                    drawableList.add(drawableUpcoming)
                }
                "NONE" -> {
                    drawableList.add(emptyPillHistory)
                }
                else -> {
                    drawableList.add(drawableUpcoming)
                }
            }
        }
        return drawableList
    }
}