package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HistoryDetailBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.MedicationDetailActivity
import com.montunosoftware.pillpopper.android.StateListenerActivity
import com.montunosoftware.pillpopper.android.util.*
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog.OnDateAndTimeSetListener
import com.montunosoftware.pillpopper.android.view.DialogHelpers
import com.montunosoftware.pillpopper.android.view.DialogHelpers.Confirm_CancelListener
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.database.model.GetHistoryPreferences
import com.montunosoftware.pillpopper.database.model.HistoryEditEvent
import com.montunosoftware.pillpopper.database.model.HistoryEvent
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperTime
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils
import java.text.SimpleDateFormat
import java.util.*

class HistoryDetailActivity : StateListenerActivity(), View.OnClickListener {

    private var mHistoryTimeStamp: PillpopperTime? = null
    private var mHistoryEditEvent: HistoryEditEvent? = null
    private var mDetailBinding: HistoryDetailBinding? = null
    private var historyEvent: HistoryEvent? = null
    private var initialHistoryOperation = ""
    private var initialHistoryTimeStamp: PillpopperTime? = null
    private var currentOperation: String? = null
    private var saveMenuItem: MenuItem? = null
    private var isFromCalendarOverlay: Boolean = false

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.history_detail)
        mDetailBinding?.robotoBold = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_BOLD)
        mDetailBinding?.robotoMedium = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM)
        mDetailBinding?.robotoRegular = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_REGULAR)
        mDetailBinding?.clickHandler = this
        initToolBar()
        initBundleData()
        loadData()
    }

    private fun initBundleData() {
        val bundle = intent.extras
        historyEvent = bundle?.getSerializable("historyEvent") as HistoryEvent
        isFromCalendarOverlay = bundle?.getBoolean("fromCalendarOverlayScreen")
    }

    private fun initToolBar() {
        setSupportActionBar(mDetailBinding?.appBar as Toolbar?)
        supportActionBar!!.title = getString(R.string.history) + " " + getString(R.string.details)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        RunTimeData.getInstance().isHistoryItemUpdated = false
        // refresh the headertime
        if (null != historyEvent && null != historyEvent?.headerTime) {
            historyEvent?.headerTime?.let {
                mDetailBinding?.historyTime?.text = Util.getEventTime(this, it)
                mDetailBinding?.historyDate?.text = Util.getEventDay(it)
            }
        }

        // refresh record and action Date
        if (null != mHistoryEditEvent && null != mHistoryEditEvent?.pillOperation && null != mHistoryEditEvent?.preferences) {
            if (mHistoryEditEvent?.pillOperation.equals(PillpopperConstants.ACTION_SKIP_PILL_HISTORY, ignoreCase = true)) {
                mHistoryEditEvent?.preferences?.recordDate?.let { setActionMessageText(null, it) }
            } else if (mHistoryEditEvent?.pillOperation.equals(PillpopperConstants.ACTION_TAKE_PILL_HISTORY, ignoreCase = true)) {
                mHistoryEditEvent?.preferences?.recordDate?.let { setActionMessageText(Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.actionDate).toString(), it) }
            }
        }

        refreshImage()
    }

    /**
     * refresh med image, if it is not skipped or taken.
     * get the updated Image Guid, from the DB.
     * The image could be set in Med read only screen, or expanded image screen, and on return the image needs to be refreshed.
     * DE22197
     */
    private fun refreshImage() {

        try {
            if (null != historyEvent) {
                if (!mHistoryEditEvent?.pillOperation.equals(PillpopperConstants.ACTION_SKIP_PILL_HISTORY, true)
                        && !mHistoryEditEvent?.pillOperation.equals(PillpopperConstants.ACTION_TAKE_PILL_HISTORY, true)) {
                    Handler(Looper.getMainLooper()).post {
                        var refreshedHistoryEvent = FrontController.getInstance(this).getHistoryEditEventDetails(historyEvent?.historyEventGuid.toString())
                        runOnUiThread { ImageUILoaderManager.getInstance().loadDrugImage(activity, refreshedHistoryEvent?.pillImageGuid, refreshedHistoryEvent?.pillId, mDetailBinding?.drugImage, Util.getDrawableWrapper(activity, R.drawable.pill_default)) }
                    }
                }
            }
        } catch (ex: Exception) {
            LoggerUtils.info(ex.message)
        }
    }

    private fun loadData() {
        mHistoryEditEvent = FrontController.getInstance(this).getHistoryEditEventDetails(historyEvent?.historyEventGuid.toString())
        if(!isRecordDateAvailable()) {
            mHistoryEditEvent?.pillHistoryCreationDate = Util.convertStringtoPillpopperTime(mHistoryEditEvent?.tz_secs?.let { getAdjustedEventTime(it, mHistoryEditEvent?.pillHistoryCreationDate?.gmtSeconds.toString()) })
        }
        mHistoryTimeStamp = mHistoryEditEvent?.pillScheduleDate
        initialHistoryOperation = mHistoryEditEvent?.pillOperation.toString()
        currentOperation  = initialHistoryOperation;
        initialHistoryTimeStamp = mHistoryTimeStamp
        historyEvent?.headerTime?.let {
            mDetailBinding?.historyTime?.text = Util.getEventTime(this, it)
            mDetailBinding?.historyDate?.text = Util.getEventDay(it)
        }
        mDetailBinding?.proxyName?.text = mHistoryEditEvent?.proxyName
        if (null != mHistoryEditEvent?.pillBrandName) {
            mDetailBinding?.drugName?.text = if (mHistoryEditEvent?.pillBrandName?.length!! > 100) {
                mHistoryEditEvent?.pillBrandName?.substring(0, 100).plus("...")
            } else {
                mHistoryEditEvent?.pillBrandName
            }
        }

        if (!Util.isEmptyString(mHistoryEditEvent?.pillGenericName)) {
            mDetailBinding?.genericName?.visibility = View.VISIBLE
            mDetailBinding?.genericName?.text = mHistoryEditEvent?.pillGenericName
        } else {
            mDetailBinding?.genericName?.visibility = View.GONE
        }

        if (!Util.isEmptyString(mHistoryEditEvent?.pillDosage)) {
            mDetailBinding?.drugDosage?.visibility = View.VISIBLE
            mDetailBinding?.drugDosage?.text = mHistoryEditEvent?.pillDosage
        } else {
            mDetailBinding?.drugDosage?.visibility = View.GONE
        }

        when (historyEvent?.operationStatus) {
            PillpopperConstants.ACTION_SKIP_PILL_HISTORY -> {
                mDetailBinding?.drugImage?.setDrawable(Util.getDrawableWrapper(this, R.drawable.ic_skipped))
                if(isRecordDateAvailable()) {
                    setActionMessageText(null, Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.recordDate))
                    // set record date
                    mHistoryEditEvent?.preferences?.recordDate = Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.recordDate)
                }else{
                    setActionMessageText(null, Util.convertDateIsoToLong(mHistoryEditEvent?.pillHistoryCreationDate?.gmtSeconds.toString()))
                    // set record date
                    mHistoryEditEvent?.preferences?.recordDate = Util.convertDateIsoToLong(mHistoryEditEvent?.pillHistoryCreationDate?.gmtSeconds.toString())
                }
                enableButtons(mDetailBinding?.skippedButton, mDetailBinding?.takenButton)
            }
            PillpopperConstants.ACTION_TAKE_PILL_HISTORY -> {
                //enableSaveMenu()
                mDetailBinding?.drugImage?.setDrawable(Util.getDrawableWrapper(this, R.drawable.ic_taken))
                if (isRecordDateAvailable()) {
                    setActionMessageText(Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.actionDate), Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.recordDate))
                    // set action date
                    mHistoryEditEvent?.preferences?.actionDate = Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.actionDate)
                    // set record date
                    mHistoryEditEvent?.preferences?.recordDate = Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.recordDate)
                } else {
                    enableSaveMenu()
                    setActionMessageText(Util.convertDateIsoToLong(mHistoryEditEvent?.pillHistoryCreationDate?.gmtSeconds.toString()), Util.convertDateIsoToLong(mHistoryEditEvent?.pillHistoryCreationDate?.gmtSeconds.toString()))
                    // set action date
                    mHistoryEditEvent?.preferences?.actionDate = Util.convertDateIsoToLong(historyEvent!!.headerTime)
                    // set record date
                    mHistoryEditEvent?.preferences?.recordDate = Util.convertDateIsoToLong(mHistoryEditEvent?.pillHistoryCreationDate?.gmtSeconds.toString())
                }
                mDetailBinding?.actionMessage?.contentDescription  = resources.getString(R.string.content_description_history_time_change)
                enableButtons(mDetailBinding?.takenButton, mDetailBinding?.skippedButton)
            }
            else -> {
                disableSaveMenu()
                ImageUILoaderManager.getInstance().loadDrugImage(activity, mHistoryEditEvent?.pillImageGuid, mHistoryEditEvent?.pillId, mDetailBinding?.drugImage, Util.getDrawableWrapper(activity, R.drawable.pill_default))
                mDetailBinding?.skippedButton?.setBackgroundColor(getColor(R.color.white))
                mDetailBinding?.takenButton?.setBackgroundColor(getColor(R.color.white))
                mDetailBinding?.skippedButton?.setTextColor(getColor(R.color.kp_theme_blue))
                mDetailBinding?.takenButton?.setTextColor(getColor(R.color.kp_theme_blue))
                mDetailBinding?.actionMessage?.visibility = View.GONE
            }
        }

        mDetailBinding?.drugDetails?.setOnClickListener(this)
        mDetailBinding?.drugImage?.setOnClickListener(this)
    }

    private fun setActionMessageText(actionDate: String?, recordDate: String) {
        var operation: String
        var builder = StringBuilder()
        mDetailBinding?.actionMessage?.visibility = View.VISIBLE
        var spannable: SpannableString
        if (null != mHistoryEditEvent) {
            when (mHistoryEditEvent!!.pillOperation) {
                PillpopperConstants.ACTION_SKIP_PILL_HISTORY -> {
                    operation = resources.getString(R.string.drug_action_skipped)
                    builder.append(operation).append(",")
                    mDetailBinding?.actionMessage?.text = builder.toString()
                    builder = StringBuilder()
                    builder.append("recorded at ").append(getEventTimeAndDate(recordDate, this))
                    mDetailBinding?.recordMessage?.text = builder.toString()
                }
                PillpopperConstants.ACTION_TAKE_PILL_HISTORY -> {
                    operation = resources.getString(R.string.drug_action_taken)
                    builder.append(operation).append(" at ")
                    builder.append(getEventTimeAndDate(actionDate!!, this))
                    builder.append(",")
                    val mClickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = ContextCompat.getColor(this@HistoryDetailActivity, R.color.kp_theme_blue)
                            ds.typeface = Typeface.DEFAULT_BOLD
                            ds.isUnderlineText = false
                        }

                        override fun onClick(view: View) {
                            showTimePicker()
                        }
                    }
                    spannable = SpannableString(builder.toString())
                    spannable.setSpan(mClickableSpan,
                            9,
                            9 + Util.getEventTime(this, actionDate).length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    mDetailBinding?.actionMessage?.text = spannable
                    mDetailBinding?.actionMessage?.movementMethod = LinkMovementMethod.getInstance()
                    builder = StringBuilder()
                    builder.append("recorded at ").append(getEventTimeAndDate(Util.convertDateIsoToLong(recordDate), this))
                    mDetailBinding?.recordMessage?.text = builder.toString()
                }
            }
        }
    }

    private fun getEventTimeAndDate(timeStamp: String, context: Context): String {
        var timeStampToReturn = timeStamp
        // record date sent from onResume will be in ISO, convert to long
        if (timeStamp.contains("T")) {
            timeStampToReturn = Util.convertDateIsoToLong(timeStamp);
        }
        val eventTime = Date(timeStampToReturn.toLong() * 1000)
        val sdf = SimpleDateFormat(if (DateFormat.is24HourFormat(context)) "HH:mm MM/dd/yyyy" else "h:mm a MM/dd/yyyy", Locale.US)
        return sdf.format(eventTime)
    }

    private fun enableButtons(blueButton: TextView?, whiteButton: TextView?) {
        whiteButton?.setBackgroundColor(getColor(R.color.white))
        whiteButton?.setTextColor(getColor(R.color.kp_theme_blue))
        whiteButton?.isSelected = false
        blueButton?.setBackgroundColor(getColor(R.color.kp_theme_blue))
        blueButton?.setTextColor(getColor(R.color.white))
        blueButton?.isSelected = true
    }

    fun onSkippedClick() {
        enableButtons(mDetailBinding?.skippedButton, mDetailBinding?.takenButton)
        mDetailBinding?.drugImage?.setDrawable(Util.getDrawableWrapper(this, R.drawable.ic_skipped))
        mDetailBinding?.drugImage?.contentDescription = getString(R.string.skipped)
        mHistoryEditEvent?.pillOperation = PillpopperConstants.ACTION_SKIP_PILL_HISTORY
        mHistoryEditEvent?.isActionDateRequired = false
        setEventDescription()
        if(!currentOperation.equals(PillpopperConstants.ACTION_SKIP_PILL_HISTORY, ignoreCase = true)) {
            currentOperation = PillpopperConstants.ACTION_SKIP_PILL_HISTORY
            enableSaveMenu()
            mHistoryEditEvent?.preferences?.recordDate = Util.convertDateLongToIso(PillpopperTime.now().gmtSeconds.toString())
            setActionMessageText(null, PillpopperTime.now().gmtSeconds.toString())
        }
    }

    fun onTakenClick() {
        enableButtons(mDetailBinding?.takenButton, mDetailBinding?.skippedButton)
        mDetailBinding?.drugImage?.setDrawable(Util.getDrawableWrapper(this, R.drawable.ic_taken))
        mDetailBinding?.drugImage?.contentDescription = getString(R.string.taken)
        mHistoryEditEvent?.pillOperation = PillpopperConstants.ACTION_TAKE_PILL_HISTORY
        mHistoryEditEvent?.isActionDateRequired = true
        setEventDescription()
        if(!currentOperation.equals(PillpopperConstants.ACTION_TAKE_PILL_HISTORY, ignoreCase = true)) {
            currentOperation = PillpopperConstants.ACTION_TAKE_PILL_HISTORY
            enableSaveMenu()

            // temporary fix for crash
            if (null == mHistoryEditEvent?.preferences) {
                mHistoryEditEvent?.preferences = GetHistoryPreferences()
            }

            if(mHistoryEditEvent!=null && initialHistoryOperation.equals(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY, ignoreCase = true) && (Util.convertDateIsoToLong(mHistoryEditEvent!!.preferences.finalPostponedDateTime) < (PillpopperTime.now().gmtSeconds.toString()))) {
                // set action date to finalPostponedDateTime if the time has exceeded the finalPostponedDateTime
                mHistoryEditEvent?.preferences?.actionDate = mHistoryEditEvent!!.preferences.finalPostponedDateTime
            }else{
                mHistoryEditEvent?.preferences?.actionDate = Util.convertDateLongToIso(historyEvent?.headerTime)

            }
            mHistoryEditEvent?.preferences?.recordDate = Util.convertDateLongToIso(PillpopperTime.now().gmtSeconds.toString())
            setActionMessageText(Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.actionDate).toString(), PillpopperTime.now().gmtSeconds.toString())
        }
    }

    fun showTimePicker() {
        val dateAndTimePickerDialog = DateAndTimePickerDialog(
                this,
                OnDateAndTimeSetListener { pillpopperTime: PillpopperTime ->
                    mHistoryTimeStamp = pillpopperTime
                    enableSaveMenu()
                    mHistoryEditEvent?.preferences?.actionDate = Util.convertDateLongToIso(mHistoryTimeStamp?.gmtSeconds.toString())
                    mHistoryEditEvent?.preferences?.recordDate = Util.convertDateIsoToLong(PillpopperTime.now().gmtSeconds.toString())
                    setActionMessageText(mHistoryTimeStamp?.gmtSeconds.toString(), PillpopperTime.now().gmtSeconds.toString())
                },
                false,
                PillpopperTime(Util.convertDateIsoToLong(mHistoryEditEvent?.preferences?.actionDate).toLong()) ?: mHistoryTimeStamp,
                true,  // to indicate from history,
                1,
                resources.getString(R.string._set),
                true
        )

        dateAndTimePickerDialog.show(supportFragmentManager, "history_creation_date")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.refill_reminder_save_menu, menu)
        saveMenuItem = menu?.findItem(R.id.save_menu_item)
        disableSaveMenu()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                handleBackPress()
            }
            R.id.save_menu_item -> {
                saveHistoryDetails()
                finish()
            }
        }
        return true
    }

    private fun showDiscardAlert() {
        // show confirmation Alert first.
        DialogHelpers.showAlertWithSaveCancelListeners(activity, R.string.save_updates, R.string.save_changes_on_exit_message,
                object : Confirm_CancelListener {
                    override fun onConfirmed() {
                        saveHistoryDetails()
                        finish()
                    }

                    override fun onCanceled() {
                        // no changes be saved
                        finish()
                    }
                })
    }

    private fun saveHistoryDetails() {
        try {
            mHistoryEditEvent?.pillHistoryCreationDate = mHistoryTimeStamp
            val bundle = Bundle()
            if(isFromCalendarOverlay){
                bundle.putString(FireBaseConstants.ParamName.SOURCE, FireBaseConstants.ParamValue.HISTORY_CALENDAR_OVERLAY)
            }else{
                bundle.putString(FireBaseConstants.ParamName.SOURCE, FireBaseConstants.ParamValue.HISTORY_LIST_SCREEN)
            }
            if (initialHistoryOperation.equals(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY, ignoreCase = true) || initialHistoryOperation.equals(PillpopperConstants.ACTION_REMINDER_PILL_HISTORY, ignoreCase = true)) {
                // track when user take action in history details
                bundle.putString(FireBaseConstants.ParamName.PARAMETER_NAME_LOWERCASE_TYPE, FireBaseConstants.ParamValue.TAKE_ACTON)
                FireBaseAnalyticsTracker.getInstance().logEvent(this, FireBaseConstants.Event.HISTORY_DETAILS_UPDATE, bundle)
                val drug = FrontController.getInstance(activity).getDrugByPillId(mHistoryEditEvent!!.pillId)
                if(initialHistoryOperation.equals(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY, ignoreCase = true)) {
                    FrontController.getInstance(this).updateHistoryEventPreferences(mHistoryEditEvent?.historyEventGuid, mHistoryEditEvent?.pillOperation, drug, drug.postponeSeconds)
                    FrontController.getInstance(this).addLogEntry(this, Util.prepareLogEntryForEditHistoryEvent(this, mHistoryEditEvent, mHistoryEditEvent?.pillOperation))
                }else{
                    DatabaseUtils.getInstance(this).deleteEmptyHistoryEvent(mHistoryEditEvent?.pillHistoryCreationDate,drug.guid)
                }
                if(null!=drug.scheduledTime && !Util.isEmptyString(drug.scheduledTime.gmtMilliseconds.toString())) {
                    FrontController.getInstance(this).removeActedPassedReminderFromReminderTable(drug.guid, drug.scheduledTime.gmtMilliseconds.toString(), this)
                }
                drug.historyScheduleDate = historyEvent?.headerTime
                AppConstants.updateNotifyAfterValue = true
                if (mHistoryEditEvent?.pillOperation.equals(PillpopperConstants.ACTION_TAKE_PILL, ignoreCase = true)) {
                    if( null!=mHistoryEditEvent!!.preferences.finalPostponedDateTime && Util.convertDateIsoToLong(mHistoryEditEvent!!.preferences.finalPostponedDateTime) < (PillpopperTime.now().gmtSeconds.toString())) {
                        // set action date to finalPostponedDateTime if the time has exceeded the finalPostponedDateTime
                        drug.actionDate = mHistoryEditEvent!!.preferences.finalPostponedDateTime
                    }else{
                        drug.actionDate = Util.convertDateLongToIso(historyEvent?.headerTime)
                    }
                    drug.isActionDateRequired = true
                    FrontController.getInstance(this).performTakeDrug(arrayListOf<Drug>(drug), PillpopperTime.now(), this, true, FireBaseConstants.ParamValue.HISTORY_DETAIL_SCREEN)
                } else {
                    FrontController.getInstance(this).performSkipDrug(arrayListOf<Drug>(drug), PillpopperTime.now(), this, true, FireBaseConstants.ParamValue.HISTORY_DETAIL_SCREEN)
                }
            } else {
                //track updating existing history
                bundle.putString(FireBaseConstants.ParamName.PARAMETER_NAME_LOWERCASE_TYPE, FireBaseConstants.ParamValue.UPDATE_EXISTING_HISTORY)
                FireBaseAnalyticsTracker.getInstance().logEvent(this, FireBaseConstants.Event.HISTORY_DETAILS_UPDATE, bundle)

                FrontController.getInstance(this).updateHistoryEvent(mHistoryEditEvent?.historyEventGuid,
                        mHistoryEditEvent?.pillOperation,
                        mHistoryEditEvent?.pillEventDescription,
                        mHistoryEditEvent?.pillHistoryCreationDate)
                FrontController.getInstance(this).updateActionAndRecordDateHistoryPreference(mHistoryEditEvent)
                if (mHistoryEditEvent?.pillOperation.equals(PillpopperConstants.ACTION_TAKE_PILL, ignoreCase = true)) {
                    mHistoryEditEvent!!.isActionDateRequired = true
                }
                FrontController.getInstance(this).addLogEntry(this, Util.prepareLogEntryForEditHistoryEvent(this, mHistoryEditEvent, null))
            }
            RunTimeData.getInstance().isHistoryMedChanged = true
            RunTimeData.getInstance().isHistoryItemUpdated = true
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("HISTORY_MED_CHANGED"));
        } catch (e: Exception) {
            PillpopperLog.exception(e.message)
        }
    }

    private fun setEventDescription() {
        val eventDescriptionStringBuilder = StringBuilder()
        eventDescriptionStringBuilder.append(mHistoryEditEvent!!.pillBrandName)
        if (!Util.isEmptyString(mHistoryEditEvent!!.pillGenericName)) {
            eventDescriptionStringBuilder.append(" (")
            eventDescriptionStringBuilder.append(mHistoryEditEvent!!.pillGenericName)
            eventDescriptionStringBuilder.append(") ")
        }
        if (!Util.isEmptyString(mHistoryEditEvent!!.pillDosage)) {
            eventDescriptionStringBuilder.append(" (")
            eventDescriptionStringBuilder.append(mHistoryEditEvent!!.pillDosage)
            eventDescriptionStringBuilder.append(") ")
        }
        eventDescriptionStringBuilder.append(mHistoryEditEvent!!.pillOperation)
        mHistoryEditEvent!!.pillEventDescription = eventDescriptionStringBuilder.toString()
    }

    private fun getAdjustedEventTime(existingTZSecs: String, existingCreationDate: String): String {
        if (Util.isEmptyString(existingTZSecs)) {
            PillpopperLog.say("History_CreationDate : existing tz_sec is empty so considering the existing creationDate")
            return existingCreationDate
        }

        val eventTZSecs: Long = existingTZSecs.toLong()
        val localTZSecs = Util.getTzOffsetSecs(TimeZone.getDefault())
        if (eventTZSecs != localTZSecs) {
            val timeZoneInterval = eventTZSecs - localTZSecs
            val adjustedCreationDate: Long = existingCreationDate.toLong() + timeZoneInterval
            return adjustedCreationDate.toString()
        }
        return existingCreationDate
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.drugImage) {
            // enlarge image view
            ViewClickHandler.preventMultiClick(mDetailBinding?.drugImage)
            intent = Intent(this, EnlargeImageActivity::class.java)
            intent.putExtra("pillId", FrontController.getInstance(this).getDrugByPillId(mHistoryEditEvent?.pillId).guid)
            intent.putExtra("imageId", mHistoryEditEvent?.pillImageGuid)
            intent.putExtra("pillName", mHistoryEditEvent?.pillBrandName)
            startActivity(intent)
            RunTimeData.getInstance().setIsFromHistory(true)
        } else if (view?.id == R.id.drugDetails) {
            ViewClickHandler.preventMultiClick(mDetailBinding?.drugDetails)
            if (null != FrontController.getInstance(this).getDrugByPillId(mHistoryEditEvent?.pillId).guid) {
                RunTimeData.getInstance().isMedDetailView = false
                RunTimeData.getInstance().isFromArchive = false
                val intent = Intent(this, MedicationDetailActivity::class.java)
                intent.putExtra(PillpopperConstants.PILL_ID, mHistoryEditEvent?.pillId)
                intent.putExtra("isFromHistory", true)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RunTimeData.getInstance().isShouldRetainHistoryOverlay = false
    }
    override fun onBackPressed() {
        handleBackPress()
    }

    private fun handleBackPress() {
        if (!initialHistoryOperation.equals(mHistoryEditEvent?.pillOperation, ignoreCase = true)
                || !initialHistoryTimeStamp?.equals(mHistoryTimeStamp)!!
                || (initialHistoryOperation.equals(PillpopperConstants.ACTION_TAKE_PILL_HISTORY,ignoreCase = true) || initialHistoryOperation.equals(PillpopperConstants.ACTION_SKIP_PILL_HISTORY,ignoreCase = true)) &&
                (isRecordDateChanged())) {
            showDiscardAlert()
        } else {
            finish()
        }
    }
    private fun isRecordDateAvailable(): Boolean {
        // checking only recordDate because the actionDate will be available automatically if there are recordDate
        if (null != mHistoryEditEvent?.preferences && !Util.isEmptyString(mHistoryEditEvent!!.preferences!!.recordDate)) {
            return true
        }
        return false
    }
    private fun isRecordDateChanged(): Boolean {
        var recordDateChanged = false
        if (isRecordDateAvailable() && null != historyEvent?.preferences?.recordDate) {
            if (!Util.convertDateIsoToLong(historyEvent?.preferences?.recordDate).equals(mHistoryEditEvent?.preferences?.recordDate)) {
                recordDateChanged = true
            }
        } else {
            if (!mHistoryEditEvent?.pillHistoryCreationDate?.gmtSeconds.toString().equals(mHistoryEditEvent?.preferences?.recordDate)) {
                recordDateChanged = true
            }
        }
        return recordDateChanged
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        RunTimeData.getInstance().isHistoryConfigChanged = true
        finish()
    }

    /*
     * Applying the alpha white color to the save menu item.
     */
    private fun disableSaveMenu(){
        saveMenuItem?.isEnabled = false
        val spannableString = SpannableString(resources.getString(R.string.save))
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.alpha_white)), 0, spannableString.length, 0)
        saveMenuItem?.title = spannableString
    }

    /*
    * Applying the white color to the save menu item.
    */
    private fun enableSaveMenu() {
        saveMenuItem?.isEnabled = true
         val spannableString = SpannableString(resources.getString(R.string.save))
         spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.white)), 0, spannableString.length, 0)
         saveMenuItem?.title = spannableString
    }
}