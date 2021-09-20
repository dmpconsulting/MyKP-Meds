package com.montunosoftware.pillpopper.kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.HasStatusAlertBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.HomeContainerActivity
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.service.AcknowledgeStatusAsyncTask
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil

class HasStatusAlert : AppCompatActivity() {

    private var primaryUserId: String? = null
    private var proxyStatusCodeValue: String? = null
    private var binding: HasStatusAlertBinding? = null
    private var isForceSignIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        binding = DataBindingUtil.setContentView(this, R.layout.has_status_alert)
        binding?.handler = this
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(this,FireBaseConstants.ScreenEvent.SCREEN_SIGN_IN_ALERTS)
        initFontStyle()
        loadData()
    }

    private fun loadData() {
        isForceSignIn = intent.getBooleanExtra("isForceSignIn", false)
        proxyStatusCodeValue = intent?.getStringExtra("proxyStatusCodeValue")
        primaryUserId = intent?.getStringExtra("primaryUserId")
        binding?.alertMessage?.text = intent?.getStringExtra("message")
        binding?.alertTitle?.text = intent?.getStringExtra("title")
    }

    private fun initFontStyle() {
        binding?.robotoBold = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_BOLD)
        binding?.robotoMedium = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM)
        binding?.robotoRegular = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_REGULAR)
    }

    fun onOkClicked() {
        RunTimeData.getInstance().isNeedToAnimateLogin = false
        finish()
        if (isForceSignIn) {
            PillpopperRunTime.getInstance().isReminderNeedToShow = false
            //force signIn
            ActivationController.getInstance().performSignoff(this)
        } else {
            if ("N".equals(proxyStatusCodeValue, ignoreCase = true)
                    && !Util.isEmptyString(AppConstants.getAcknowledgeStatusAPIUrl())) {
                // will be called when proxyStatusCodeValue is N
                val acknowledgeStatusAsyncTask = AcknowledgeStatusAsyncTask(this)
                acknowledgeStatusAsyncTask.execute(AppConstants.getUpdateAcknowledgeUserFlUrl(), primaryUserId, "UpdateAcknowledgeUserFl")
            }
            val mainIntent = Intent(this, HomeContainerActivity::class.java)
            startActivity(mainIntent)
        }
    }

    override fun onBackPressed() {
        // not allowing back press
    }
}

