package com.montunosoftware.pillpopper.android

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.ActivityImportantInformationBinding
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem
import com.montunosoftware.pillpopper.model.genericCardAndBanner.ButtonsItem
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils

class ExpandedGenericBannerActivity : StateListenerActivity() {
    private var binding: ActivityImportantInformationBinding? = null
    private var announcementsItem: AnnouncementsItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_important_information)
        val intent: Intent? = intent
        announcementsItem = intent?.getSerializableExtra("announcement") as AnnouncementsItem?
        binding?.announcement = announcementsItem
        binding?.handler = this
        initUI()
    }


    private fun setMessageBody(body: String): String {
        return ("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en-US\">" +
                "<pre style=\"word-wrap: break-word; white-space: normal;background-color: transparent; font-family: Avenir-Light; font-size: 12pt; \">" +
                "<head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
                "<title>Kaiser Permanente</title>" +
                "<style type=\"text/css\">" +
                "* {  background-color: transparent; font-family: Avenir-Light; font-size: 12pt; }" +
                " </style> </head> <body> "
                + body.trim { it <= ' ' } +
                "</body></pre> </html>")
    }

    private fun convertMessageBody(msg: String): String {
        var msg = msg
        msg = msg.replace("\n".toRegex(), "<br/>")
        msg += "<br/>"
        return msg
    }

    private fun addLinks(body: String): String? {
        var data: String = convertMessageBody(body)
        val dataBuffer = StringBuilder("")
        data = data.replace("<br>", "\n")
        data = data.replace("<br/>", "\n")
        data = data.replace("epichttp", "http")
        var word = ""
        for (c in data.toCharArray()) {
            word = when (c) {
                ' ' -> {
                    if ((word.contains("http") || word.contains("www")) && !word.contains("href")) {
                        if (!dataBuffer.toString().endsWith("href=\" ")) dataBuffer.append("<a href=\"$word\">$word</a>") else dataBuffer.append(word)
                    } else {
                        dataBuffer.append(word)
                    }
                    dataBuffer.append(" ")
                    ""
                }
                '\n' -> {
                    if ((word.contains("http") || word.contains("www")) && !word.contains("href")) {
                        if (!dataBuffer.toString().endsWith("href=\" ")) dataBuffer.append("<a href=\"$word\">$word</a>") else dataBuffer.append(word)
                    } else {
                        dataBuffer.append(word)
                    }
                    dataBuffer.append("<br>")
                    ""
                }
                '\t' -> {
                    if ((word.contains("http") || word.contains("www")) && !word.contains("href")) {
                        if (!dataBuffer.toString().endsWith("href=\" ")) dataBuffer.append("<a href=\"$word\">$word</a>") else dataBuffer.append(word)
                    } else {
                        dataBuffer.append(word)
                    }
                    dataBuffer.append("&nbsp;")
                    ""
                }
                else -> word + c
            }
        }
        data = dataBuffer.toString()
        data = convertMessageBody(data)
        data = data.replace("&amp;", "&")
        data = data.replace("&lt;", "<")
        data = data.replace("&gt;", ">")
        if (data.contains("<a href=\"www")) {
            data = data.replace("<a href=\"www", "<a href=\"https://www")
        }
        return data
    }

    private fun initUI() {
        var message: String? = announcementsItem?.message

        if (message!!.contains("<tel>")) {
            var number = message.substring(message.indexOf("<tel>"), message.indexOf("</tel>"))
            number = number.replace("<tel>", "")

            val oldText = "<tel>" + number + "</tel>"
            val newText = "<a href='tel:" + number + "'>" + number + "</a>"
            message = message.replace(oldText, newText)
        }

        binding?.descriptionText?.setWebViewClient(KpWebViewClient())
        binding?.descriptionText?.getSettings()!!.setAllowContentAccess(true)

        binding?.descriptionText?.setClickable(true)

        message = addLinks(message)
        binding?.descriptionText?.loadDataWithBaseURL(null, setMessageBody(message!!), "text/html; charset=UTF-8", null, null)
        binding?.descriptionText?.setBackgroundColor(Color.TRANSPARENT)
        if (announcementsItem?.getButtons()?.size == 1) {
            binding?.kpButton?.let { showButton(announcementsItem!!.getButtons().get(0), it) }
        } else if (announcementsItem?.getButtons()?.size == 2) {
            binding?.kpButton?.let { showButton(announcementsItem!!.getButtons().get(0), it) }
            binding?.acknowledgeButton?.let { showButton(announcementsItem!!.getButtons().get(1), it) }
        }
    }

    private fun showButton(buttonsItem: ButtonsItem,button: Button) {
        if (GenericCardAndBannerUtility.showButtonIfEligible(buttonsItem)) {
            button.visibility = View.VISIBLE
            button.text = buttonsItem.label
            GenericCardAndBannerUtility.setButtonColor(button, buttonsItem)
            button.setOnClickListener {
                buttonAction(announcementsItem!!, buttonsItem.action, buttonsItem.url, buttonsItem.destination) }
        }
    }

    fun closeActivity() {
        this.finish()
    }

    fun buttonAction(announcementsItem: AnnouncementsItem, action: String, link: String?, destination: String?) {
        GenericCardAndBannerUtility.buttonAction(announcementsItem, action, link, destination, this)
        finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        GenericCardAndBannerUtility.dismissDialog();
    }

    inner class KpWebViewClient : WebViewClient() {
        private var isPending = false
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            view.visibility = View.VISIBLE
            isPending = false
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("tel:") || url.startsWith("mobilecare:phone:")) {
                val mPhoneNoStr = url.substring(url.lastIndexOf('=') + 1)
                val phoneNoStr = mPhoneNoStr.replace("tel:", "")

             //   var intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNoStr.replace("-", "")))
                RunTimeData.getInstance().runTimePhoneNumber = phoneNoStr
                if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_PHONE_CALL_PHONE, Manifest.permission.CALL_PHONE, activity)) {
                   // startActivity(intent)

                    // context.startActivity(intent);
                    GenericCardAndBannerUtility.makeCall(phoneNoStr, _thisActivity, "")
                }
                return true
            } else if (url.startsWith("http")) {

                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            } else if (url.startsWith("mailto:")) {
                val email = ActivationUtil.callMailClient(url.replace("mailto:", ""), getSubjectLine(getString(R.string.subject_line)), "")
                startActivity(email)
            }
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
        }
    }

    private fun getSubjectLine(message: String): String? {
        val subjectLine = java.lang.StringBuilder()
        subjectLine.append(message).append(" ").append(Util.getUserRegionValue(ActivationController.getInstance().fetchUserRegion(_thisActivity))).append(":")
        subjectLine.append(" v").append(Util.getAppVersion(_thisActivity))
        subjectLine.append(", ").append(AppConstants.PHONE_ANDROID)
        subjectLine.append(Util.getOSVersion()).append(", ").append(AppConstants.ANDROID_DEVICE_MAKE)
        return subjectLine.toString()
    }

    class RedirectWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
        }
    }
}