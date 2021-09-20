package com.montunosoftware.pillpopper.model.firebaseMessaging

class PushNotificationAckRequestObj {
    var ackSource: String? = ""
    var ackStatus: String? = ""
    var ack_device_id: String = ""
    var ackStatusReason: String? = ""
    var ackTS: Long? = 0
    var action: String? = ""
    var notificationId: Int? = 0
    var tzName: String? = ""
    var tzSecs: String? = ""
}