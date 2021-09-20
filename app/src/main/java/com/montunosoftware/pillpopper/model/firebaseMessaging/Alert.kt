package com.montunosoftware.pillpopper.model.firebaseMessaging

import com.google.gson.annotations.SerializedName

data class Alert(
        @SerializedName("title") val title: String,
        @SerializedName("body") val body: String
)