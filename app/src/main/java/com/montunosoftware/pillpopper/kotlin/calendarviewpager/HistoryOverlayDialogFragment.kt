package com.montunosoftware.pillpopper.kotlin.calendarviewpager

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryOverlayDialogeFragmentBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.util.PillpopperConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.android.util.ViewClickHandler
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.kotlin.history.HistoryBulkActionChangeActivity
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import kotlin.collections.ArrayList


class HistoryOverlayDialogFragment(var historyEvent: ArrayList<HistoryEvent>?, var mSelectedUserName: String?) : DialogFragment() {

    private var binding: HistoryOverlayDialogeFragmentBinding?= null
    private var adapter : HistoryOverlayAdapter?= null
    private lateinit var historyEventList : ArrayList<HistoryEvent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(inflater,R.layout.history_overlay_dialoge_fragment,container,false)
        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(activity, FireBaseConstants.Event.HISTORY_CALENDER_OVERLAY)
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(activity, FireBaseConstants.ScreenEvent.SCREEN_HISTORY_OVERLAY)
        return binding?.root
    }

    override fun onStart() {
        if (RunTimeData.getInstance().isHistoryConfigChanged){
            RunTimeData.getInstance().isHistoryConfigChanged = false
            dismiss()
        }
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setBackgroundDrawable(requireContext().getDrawable(R.drawable.history_overlay_dialog))
        }
    }
    override fun onPause() {
        super.onPause()
        if (!RunTimeData.getInstance().isShouldRetainHistoryOverlay){
            RunTimeData.getInstance().isHistoryOverlayShown = false
            dialog!!.dismiss()
        }
    }

    override fun onStop() {
        RunTimeData.getInstance().isHistoryOverlayShown = false
        super.onStop()
    }

    private fun setValues() {
        val mRobotoMedium = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_MEDIUM)
        val eventDay = Util.getEventDay(historyEvent!![0].headerTime)
        val eventTime = Util.getEventTime(context, historyEvent!![0].headerTime)

        if (eventDay != null) {
            binding!!.tvEventDate.text = eventDay
            binding!!.tvEventDate.typeface = mRobotoMedium
        }

        if (eventTime != null) {
            binding!!.tvEventTime.text = eventTime
            binding!!.tvEventTime.typeface = mRobotoMedium
        }

        binding?.editBtn?.typeface = mRobotoMedium
        ViewClickHandler.preventMultiClick(binding?.editBtn)

        binding!!.imgClose.setOnClickListener {
            RunTimeData.getInstance().isHistoryOverlayShown = false
            ViewClickHandler.preventMultiClick(binding?.imgClose)
            dialog!!.dismiss()
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            historyBroadcastReceiver,
            IntentFilter("HISTORY_MED_CHANGED")
        )
    }

    private fun setAdapter() {
        historyEvent!!.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER , { it.pillName.toString() }))
        adapter = HistoryOverlayAdapter(historyEvent!!,requireContext(),mSelectedUserName)
        binding?.rvHistoryListItem?.adapter = adapter
        if(historyEventList.isNotEmpty() && historyEventList.size>1){
            binding?.editBtn?.text = activity?.resources?.getString(R.string.edit_all_btn)
        }else{
            binding?.editBtn?.text = activity?.resources?.getString(R.string.edit_btn)
        }
        binding?.editBtn?.setOnClickListener {
            onEditClicked()
        }
    }

    private val historyBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            RunTimeData.getInstance().isHistoryOverlayShown = false
            dialog?.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        RunTimeData.getInstance().isHistoryOverlayShown = true
        checkForNonActedMeds()
        setValues()
        setAdapter()
    }
   private fun onEditClicked(){
       RunTimeData.getInstance().isShouldRetainHistoryOverlay = true
       // reset the history med change flag.
       RunTimeData.getInstance().isHistoryMedChanged = false
       val intent = Intent(activity,HistoryBulkActionChangeActivity::class.java)
        intent.putExtra("historyEventsList",historyEvent)
        startActivity(intent)
    }
    private fun checkForNonActedMeds(){
        historyEventList = ArrayList()
        for(historyEvents in historyEvent!!){
            if(historyEvents.operationStatus.equals(PillpopperConstants.ACTION_MISS_PILL_HISTORY, ignoreCase = true)|| historyEvents.operationStatus.equals(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY, ignoreCase = true)
                    || historyEvents.operationStatus.equals(PillpopperConstants.ACTION_REMINDER_PILL_HISTORY,ignoreCase = true)){
                binding?.editBtn?.visibility = View.VISIBLE
                historyEventList.add(historyEvents)
            }

        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(historyBroadcastReceiver)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

}