package com.montunosoftware.pillpopper.android.firebaseMessaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.android.Splash
import com.montunosoftware.pillpopper.model.firebaseMessaging.FirebaseMessageModel
import com.montunosoftware.pillpopper.model.firebaseMessaging.PushNotificationAckRequestObj
import com.montunosoftware.pillpopper.android.util.PillpopperLog
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.controller.FrontController
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils
import java.util.*


class FCMService : FirebaseMessagingService() {
    private val gson = Gson()

    companion object {
        private const val CHANNEL_NAME = "KP_FCM"
        private const val CHANNEL_DESC = "Firebase Cloud Messaging"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        //Logging the new Token
        PillpopperLog.say(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options

        LoggerUtils.info("FireBAse Cloud messaging is allowed..... ")
        LoggerUtils.info("FireBAse Cloud messaging is allowed..... ")
        LoggerUtils.info("FCM --data - " + remoteMessage.data)
        LoggerUtils.info("FCM -- message " + remoteMessage.data["message"])
        LoggerUtils.info("FCM -- notification - " + remoteMessage.notification)

        if (AppConstants.IS_FIREBASE_CLOUD_MESSAGING_ENABLED) {
            // this will parse the message coming to the app
            if (!RunTimeData.getInstance().isLoadingInProgress) {
                Util.dismissForegroundAlertDialogIfAny()
                handleMessageData(remoteMessage)
                parseJsonMessage(remoteMessage)?.let { sendPushNotificationAck(it) }
            }
        }
    }

    /**
     * It will handle the data part of the message
     *  This is just a test before the actual data payload comes from the backend server
     */
    private fun handleMessageData(remoteMessage: RemoteMessage) {
        try {
            parseJsonMessage(remoteMessage)?.let { dataPayload ->
                if (!AppConstants.isByPassLogin() && ActivationController.getInstance().isSessionActive(applicationContext)
                        && RunTimeData.getInstance().isAppVisible) {
                    showInAppDialog(dataPayload)
                } else {
                    sendNotification(dataPayload)
                }
            }
        } catch (e: Exception) {
            LoggerUtils.exception(e.message)
        }
    }

    /**
     * It will return a message String  Json
     */
    private fun jsonToMessageString(remoteMessage: RemoteMessage): String? {
        return remoteMessage.data["message"]
    }

    private fun showInAppDialog(payload: FirebaseMessageModel?) {
        payload?.let {
            val intent = Intent(applicationContext, FCMNotificationReceiverActivity::class.java)
            intent.putExtra("isFromFCM", true)
            intent.putExtra("title", payload.alert.title)
            intent.putExtra("body", payload.alert.body)
            LoggerUtils.info("FCM -- Sending Intent to Activity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    /**
     * will parse the message data that is coming from the server
     */
    private fun parseJsonMessage(remoteMessage: RemoteMessage): FirebaseMessageModel? {
        var firebaseMessageModel: FirebaseMessageModel? = null
        try {
            firebaseMessageModel = jsonToMessageString(remoteMessage)?.let { jsonString ->
                gson.fromJson(jsonString, FirebaseMessageModel::class.java)
            }
        } catch (e: java.lang.Exception) {
            LoggerUtils.exception(e.message)
        }
        return firebaseMessageModel
    }

    /**
     * Sending the Notiifcation from here instead of directly from the firebase cloud Server
     */
    private fun sendNotification(firebaseMessageModel: FirebaseMessageModel?) {
        firebaseMessageModel?.let {
            val intent = Intent(this, Splash::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val title = firebaseMessageModel.alert.title
            val body = firebaseMessageModel.alert.body
            val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.FCM_CHANNEL_ID))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_app_icon))
                    .setColor(getColor(R.color.colorAccent))
                    .setLights(Color.RED, 1000, 300)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.drawable.icon_notification)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(getString(R.string.FCM_CHANNEL_ID),
                        CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                channel.description = CHANNEL_DESC
                channel.setShowBadge(true)
                channel.canShowBadge()
                channel.enableLights(true)
                channel.lightColor = Color.GREEN
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(0, notificationBuilder.build())
        }
    }


    /**
     * Will send an acknowledgment to the backend server
     */
    private fun sendPushNotificationAck(payload: FirebaseMessageModel?) {
        try {
            payload?.let {
                var notiId = 0
                val ackRequestObj = PushNotificationAckRequestObj()
                // setting the notificationId
                if (it.md5.notificationId == null) {
                    LoggerUtils.info("payload.md5.notificationId.toInt()$notiId")
                    ackRequestObj.notificationId = notiId
                } else {
                    ackRequestObj.notificationId = it.md5.notificationId
                }
                if (RunTimeData.getInstance().isAppVisible) {
                    ackRequestObj.ackSource = "App Foreground"
                    LoggerUtils.info("FCM -- App Foreground")
                } else {
                    ackRequestObj.ackSource = "App Background"
                    LoggerUtils.info("FCM -- App Background")
                }
                ackRequestObj.ack_device_id = ActivationUtil.getDeviceId(applicationContext)
                ackRequestObj.tzSecs = Util.getTzOffsetSecs(TimeZone.getDefault()).toString()
                ackRequestObj.tzName = TimeZone.getDefault().displayName
                ackRequestObj.ackTS = PillpopperTime.now().gmtMilliseconds
                ackRequestObj.ackStatus = "success"
                ackRequestObj.ackStatusReason = ""
                val ackService = AckPushNotificationAPIService(applicationContext, FrontController.getInstance(applicationContext).getLocalNonSecureUrl(applicationContext), ackRequestObj)
                ackService.execute()
            }
        } catch (e: java.lang.Exception) {
            LoggerUtils.exception(e.message)
        }
    }
}