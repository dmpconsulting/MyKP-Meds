package com.montunosoftware.pillpopper.model.firebaseMessaging
import com.google.gson.annotations.SerializedName
data class FirebaseMessageModel (
		@SerializedName("alert") val alert : Alert,
		@SerializedName("md-5") val md5 : Md5
)