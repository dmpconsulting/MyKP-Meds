@file:JvmName("FCMHandler")

package com.montunosoftware.pillpopper.android.firebaseMessaging

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.montunosoftware.pillpopper.android.util.PillpopperLog

class FCMHandler private constructor(var context: Context) {

    class Builder {
        private lateinit var context: Context
        fun init(context: Context) = apply {
            this.context = context
            FCMHandler.context = context
            initFirebaseMessaging()
        }

        /**
         * we have to make sure if the user installed google play services
         */
        private fun checkGooglePlayServices(context: Context): Boolean {
            val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            return if (status != ConnectionResult.SUCCESS) {
                PillpopperLog.exception("Error  $status")
                // ask user to update google play services and manage the error.
                false
            } else {
                PillpopperLog.say("Google play services updated")
                true
            }
        }

        fun build() = FCMHandler(context)
        private fun initFirebaseMessaging() {
            FirebaseApp.initializeApp(context)
            //checking to see if the device have a google play service
            checkGooglePlayServices(context)
        }
    }


    companion object {
        private var token: String? = null
        lateinit var context: Context

        @JvmStatic
        fun getFirebaseToken(tokenReceived: TokenReceived) {
            if (token == null)
                FirebaseInstallations.getInstance().id
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                return@OnCompleteListener
                            }
                            // Get new Instance ID token
                            val token = task.result
                            if (token != null) {
                                this.token = token
                                tokenReceived.onReceivedToken(token)
                            }
                        })
            else
                tokenReceived.onReceivedToken(token!!)
        }
    }

    interface TokenReceived {
        fun onReceivedToken(token: String)
    }
}