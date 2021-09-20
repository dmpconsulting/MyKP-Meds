package com.montunosoftware.pillpopper.model.firebaseMessaging

import com.google.gson.annotations.SerializedName

data class Md5(
        @SerializedName("notification_id") val notificationId: Int = 0,
        @SerializedName("campaign") val campaign: Campaign
)