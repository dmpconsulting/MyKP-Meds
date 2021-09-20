package com.montunosoftware.pillpopper.android

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.ActivityMedicationOverlayConfirmationBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.android.view.ScheduleViewModel
import com.montunosoftware.pillpopper.model.BulkSchedule
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil


class MedicationOverlayConfirmationScheduleActivity :
    StateListenerActivity() {
    private var binding: ActivityMedicationOverlayConfirmationBinding? = null
    private var scheduleViewModel: ScheduleViewModel? = null
    private lateinit var bulkSchedule: BulkSchedule

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_medication_overlay_confirmation);
        scheduleViewModel = RunTimeData.getInstance().getScheduleViewModel(this)
        bulkSchedule = intent.extras?.get("BULK_SCHEDULE") as BulkSchedule

        FireBaseAnalyticsTracker.getInstance().logScreenEvent(
            activity,
            FireBaseConstants.ScreenEvent.SCREEN_SAVE_SCHEDULE_CONFIRMATION
        )

        // to suppress the inApp in confirm screen
        RunTimeData.getInstance().isLoadingInProgress = true

    }

    private fun setValues() {
        binding?.robotoRegular = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_REGULAR)
        binding?.robotoMedium = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM)
        binding?.robotoBold = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_BOLD)

        if (null != RunTimeData.getInstance().scheduleData.selectedDrug) {
            val drug = RunTimeData.getInstance().scheduleData.selectedDrug
            binding?.medication = drug
            binding?.isManagedSIG = drug.isManaged && !Util.isEmptyString(drug.directions)

            if(binding?.isManagedSIG==true){
                binding?.llConfirmTitle?.visibility = View.VISIBLE
                binding?.headerDividerView?.visibility = View.GONE
            }else{
                binding?.llConfirmTitle?.visibility = View.GONE
                binding?.headerDividerView?.visibility = View.VISIBLE
            }

            binding?.medDetailsLayout?.medication = drug
            binding?.medDetailsLayout?.confirmationActivity = this
            binding?.medDetailsLayout?.robotoMedium = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM)
            ImageUILoaderManager.getInstance().loadDrugImage(
                this,
                drug.imageGuid,
                drug.guid,
                binding?.medDetailsLayout?.drugImage,
                Util.getDrawableWrapper(this, R.drawable.pill_default)
            )
        }
        getScheduleFrequency(bulkSchedule.dayPeriod, bulkSchedule.scheduledFrequency)
        binding!!.btOverlayCancel.setOnClickListener {
            FireBaseAnalyticsTracker.getInstance()
                .logEventWithoutParams(activity, FireBaseConstants.Event.EVENT_CANCEL_SCHEDULE)
            finish()
        }
        binding!!.btOverlaySchedule.setOnClickListener {
            FireBaseAnalyticsTracker.getInstance()
                .logEventWithoutParams(activity, FireBaseConstants.Event.EVENT_CONFIRM_SCHEDULE)

            if (null != RunTimeData.getInstance().scheduleData && RunTimeData.getInstance().scheduleData.isNLPReminder) {
                RunTimeData.getInstance().scheduleData.isNLPReminder = false
            }
            scheduleViewModel?.getBulkSchedule(bulkSchedule)
            finish()
        }
    }

    fun getScheduleFrequency(dayPeriod: String, scheduleFrequency: String) {
        val selectedDayPeriod: String

        if ("M".equals(scheduleFrequency, ignoreCase = true)) {
            selectedDayPeriod = resources.getString(R.string._monthly)
            binding?.tvFrequency?.text = selectedDayPeriod
            binding?.tvDays?.text = convertToDateValue()
            setScheduleDateAndTime()
        } else if ("D".equals(scheduleFrequency, ignoreCase = true)) {
            if (dayPeriod == "1") {
                selectedDayPeriod = resources.getString(R.string.txt_remindter_set_time_daily)
                binding?.tvFrequency?.text = selectedDayPeriod
                binding?.tvDays?.visibility = View.GONE
                binding?.tvLastDateOfMonth?.visibility = View.GONE
                setScheduleDateAndTime()
            } else {
                selectedDayPeriod = resources.getString(R.string.custom)
                binding?.tvFrequency?.text = selectedDayPeriod
                binding?.tvDays?.text = convertToDateValue()
                binding?.tvLastDateOfMonth?.visibility = View.GONE
                setScheduleDateAndTime()
            }
        } else if ("W".equals(scheduleFrequency, ignoreCase = true)) {
            if (dayPeriod.toInt() / 7 != 1) {
                selectedDayPeriod = resources.getString(R.string.custom)
                binding?.tvFrequency?.text = selectedDayPeriod
                binding?.tvDays?.text = convertToDateValue()
                binding?.tvLastDateOfMonth?.visibility = View.GONE
                setScheduleDateAndTime()
            } else {
                selectedDayPeriod = resources.getString(R.string.txt_remindter_set_time_weekly)
                binding?.tvFrequency?.text = selectedDayPeriod
                binding?.tvDays?.text =
                    Util.setOnWeekdays(activity, bulkSchedule.daysSelectedForWeekly)
                binding?.tvLastDateOfMonth?.visibility = View.GONE
                setScheduleDateAndTime()
            }
        }

    }

    fun setScheduleDateAndTime() {
        binding?.tvStartDate?.text = Util.getScheduleFormattedDate(bulkSchedule.scheduledStartDate)
        binding?.tvEndDate?.text = Util.getScheduleFormattedDate(bulkSchedule.scheduledEndDate)
        binding?.tvTime?.text = covertToScheduledTimeList(bulkSchedule.scheduledTimeList)
    }

    /**
    Here Mutable list is  used
     */
    fun covertToScheduledTimeList(scheduledTimeList: MutableList<Int>): String {
        var timeSchedule = ""
        return when {
            scheduledTimeList.size > 1 -> {
                for (time in scheduledTimeList) {
                    timeSchedule = "$timeSchedule, ${getTimeFormat12HrOr24HrFormat(time)}"
                }
                timeSchedule.removePrefix(", ")
            }
            else -> {
                timeSchedule = getTimeFormat12HrOr24HrFormat(scheduledTimeList[0])
                timeSchedule
            }
        }
    }

    fun getTimeFormat12HrOr24HrFormat(time: Int): String {
        return if (DateFormat.is24HourFormat(activity)) {
            Util.convertTimeTo24HrFormat(time)
        } else {
            Util.convertTimeTo12HrFormat(time)
        }
    }

    fun convertToDateValue(): String {
        return when {
            "M".equals(bulkSchedule.scheduledFrequency, ignoreCase = true) -> {
                val date = Util.getScheduleFormattedDate(bulkSchedule.scheduledStartDate)
                    .split(" ")[1].removeSuffix(",")
                binding?.tvLastDateOfMonth?.visibility =
                    if (date == "31") View.VISIBLE else View.GONE
                resources.getString(R.string.every) + " " + date + Util.getSuffix(date.toInt())
            }
            "D".equals(bulkSchedule.scheduledFrequency, ignoreCase = true) -> {
                resources.getString(R.string.every) + " " + bulkSchedule.dayPeriod + " " + resources.getString(
                    R.string._days
                )
            }
            else -> {
                resources.getString(R.string.every) + " " + bulkSchedule.dayPeriod.toInt() / 7 + " " + resources.getString(
                    R.string._weeks
                )
            }
        }
    }

    override fun onDestroy() {
        RunTimeData.getInstance().isLoadingInProgress = false
        super.onDestroy()

    }

    override fun onResume() {
        super.onResume()
        setValues()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish()
    }

    fun expandImage(drug: Drug) {
        val expandImageIntent = Intent(this, EnlargeImageActivity::class.java)
        expandImageIntent.putExtra("pillId", drug.guid)
        expandImageIntent.putExtra("imageId", drug.imageGuid)
        expandImageIntent.putExtra("isFromReminderDrugDetailActivity", true)
        startActivity(expandImageIntent)
    }
}