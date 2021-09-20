package com.montunosoftware.pillpopper.android.firebaseMessaging

import android.content.Context
import android.os.AsyncTask
import com.montunosoftware.pillpopper.model.firebaseMessaging.PushNotificationAckRequestObj
import org.json.JSONException
import org.json.JSONObject
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.model.AppData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils

class AckPushNotificationAPIService(private val mContext: Context, private val url: String, requestObject: PushNotificationAckRequestObj) : AsyncTask<Void?, Void?, Void?>() {

    private val requestObject: PushNotificationAckRequestObj = requestObject
    private val sAppData: AppData = AppData.getInstance()

    override fun doInBackground(vararg p0: Void?): Void? {
        try {
            val response = sAppData.getHttpResponse(url,
                    AppConstants.POST_METHOD_NAME, null,
                    ActivationUtil.buildHeaders(mContext), prepareAckRequestObject(requestObject), mContext)
            if (null != response && response != AppConstants.HTTP_DATA_ERROR) {
                LoggerUtils.info("Ack Push Notification Success")
            }
        } catch (ex: Exception) {
            LoggerUtils.exception("Ack Push Notification failed" + ex.localizedMessage)
        }
        return null
    }

    private fun prepareAckRequestObject(requestObject: PushNotificationAckRequestObj): JSONObject {
        val finalRequest = JSONObject()
        val pillpopperRequest = JSONObject()
        try {
            pillpopperRequest.put("action", "pushNotificationAcknowledgement")
            pillpopperRequest.put("notificationId", requestObject.notificationId)
            pillpopperRequest.put("ackTS", requestObject.ackTS)
            pillpopperRequest.put("tz_name", requestObject.tzName)
            pillpopperRequest.put("tz_secs", requestObject.tzSecs)
            pillpopperRequest.put("ack_status", requestObject.ackStatus)
            pillpopperRequest.put("ack_source", requestObject.ackSource)
            pillpopperRequest.put("ack_status_reason", requestObject.ackStatusReason)
            pillpopperRequest.put("ack_device_id", ActivationUtil.getDeviceId(mContext))
            finalRequest.put("pillpopperRequest", pillpopperRequest)
        } catch (e: JSONException) {
            LoggerUtils.exception(e.message)
        }
        return finalRequest
    }

}