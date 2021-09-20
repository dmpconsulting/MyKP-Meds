package com.montunosoftware.pillpopper.kotlin.quickview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.ReminderAlertBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils

class ReminderAlertActivity : Activity() {

    private var mTakenEarlier: Long? = null
    private var launchMode: String? = null
    private var binding: ReminderAlertBinding? = null
    private var mSlideDownAnim: Animation? = null
    private val slideDownDuration = 300
    private val slideDownStartAnimDelay = 1150
    private val greatJobActFinishDelay = 4300

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        LoggerUtils.info("Alerts - in ReminderAlertActivity 33")
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        binding = DataBindingUtil.setContentView(this, R.layout.reminder_alert)
        binding?.context = this
        initUI()
    }

    private fun initUI() {
        binding?.robotoBold = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_BOLD)
        binding?.robotoMedium = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM)
        if (null != intent) {
            launchMode = intent.getStringExtra(AppConstants.LAUNCH_MODE)
            mTakenEarlier = intent.getLongExtra("TakenEarlier", -1)
            mSlideDownAnim = AnimationUtils.loadAnimation(this,
                    if ("AddToHistory".equals(launchMode, ignoreCase = true)) R.anim.record_dose_slide_down else R.anim.slide_down)
            mSlideDownAnim?.duration = slideDownDuration.toLong()
            binding?.actionTitle?.text = intent.getStringExtra(AppConstants.ACTION_TITLE)
            binding?.actionMessage?.text = intent.getStringExtra(AppConstants.ACTION_MESSAGE)
            if (resources.getString(R.string.skipped).equals(launchMode, ignoreCase = true)) {
                binding?.rootLayout?.visibility = View.VISIBLE
                binding?.rootLayout?.background = ContextCompat.getDrawable(this, R.drawable.skip_alert_background)
                binding?.actionImage?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_skipped_50dp))
            } else if ("passedReminders".equals(launchMode, ignoreCase = true)) {
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(this, FireBaseConstants.ScreenEvent.SCREEN_KEEP_IT_UP_ALERT)
                binding?.rootLayout?.background = ContextCompat.getDrawable(this, R.drawable.taken_alert_background)
                binding?.actionImage?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_icon_alert_taken))
                binding?.cancelButton?.visibility = View.GONE
                startAnimation()
            } else if(resources.getString(R.string.save_title).equals(launchMode, ignoreCase = true)){
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(this, FireBaseConstants.ScreenEvent.SCREEN_SAVED_ALERT)
                LoggerUtils.info("Alerts - launchMode saved 71")
                binding?.rootLayout?.background = ContextCompat.getDrawable(this, R.drawable.greatjobalert)
                binding?.actionImage?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_icon_alert_great_job))
                binding?.okButton?.text = this.getString(R.string.done)
                binding?.cancelButton?.visibility = View.GONE
                startAnimation()
            } else {
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(this, FireBaseConstants.ScreenEvent.SCREEN_GREAT_JOB_ALERT)
                binding?.rootLayout?.background = ContextCompat.getDrawable(this, R.drawable.greatjobalert)
                binding?.actionImage?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_icon_alert_great_job))
                binding?.okButton?.text = this.getString(R.string.done)
                binding?.cancelButton?.visibility = View.GONE
                startAnimation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!resources.getString(R.string.skipped).equals(launchMode, ignoreCase = true)) {
            if (!"passedReminders".equals(launchMode, ignoreCase = true)) {
                overridePendingTransition(R.anim.record_dose_slide_down, android.R.anim.fade_out)
            }
        }
    }

    private fun startAnimation() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding?.rootLayout?.visibility = View.VISIBLE
            binding?.rootLayout?.startAnimation(mSlideDownAnim)
        }, slideDownStartAnimDelay.toLong())
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                val intent = Intent()
                intent.putExtra("TakenEarlierTime", mTakenEarlier)
                setResult(RESULT_OK, intent)
                finish()
            }
        }, greatJobActFinishDelay.toLong())
        AppConstants.SHOW_SAVED_ALERT = false
        AppConstants.MEDS_TAKEN_OR_SKIPPED = false
        AppConstants.MEDS_TAKEN_OR_POSTPONED = false
    }

    fun okClicked() {
        intent.putExtra("TakenEarlierTime", mTakenEarlier)
        setResult(RESULT_OK, intent)
        finish()
    }

    fun cancelClicked() {
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}