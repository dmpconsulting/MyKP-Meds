package com.montunosoftware.pillpopper.model.firebaseMessaging

import com.google.gson.annotations.SerializedName
data class Campaign (
		@SerializedName("id") val id : String,
		@SerializedName("name") val name : String
)