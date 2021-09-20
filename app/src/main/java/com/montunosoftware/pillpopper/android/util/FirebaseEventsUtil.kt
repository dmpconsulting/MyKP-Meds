package com.montunosoftware.pillpopper.android.util

import android.content.Context
import android.os.Bundle
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData
import org.kp.tpmg.ttgmobilelib.utilities.TTGUtil
import java.util.*

object FirebaseEventsUtil {

    @JvmStatic
    fun invokeInterruptFailureAPIEvent(context: Context?) {
        if (!Util.isEmptyString(TTGRuntimeData.getInstance().correlationId) && null != TTGRuntimeData.getInstance().requestMethod) {
            if (TTGMobileLibConstants.HTTP_METHOD_GET.equals(TTGRuntimeData.getInstance().requestMethod, ignoreCase = true)) {
                logFailureFirebaseEvent(TTGRuntimeData.getInstance().correlationId,
                        TTGRuntimeData.getInstance().httpStatusCode, context,
                        FireBaseConstants.Event.EVENT_SSO_INTERRUPTS_GET_FAILURE, "", "")
            } else if (TTGMobileLibConstants.HTTP_METHOD_PUT.equals(TTGRuntimeData.getInstance().requestMethod, ignoreCase = true)) {
                logFailureFirebaseEvent(TTGRuntimeData.getInstance().correlationId,
                        TTGRuntimeData.getInstance().httpStatusCode, context,
                        FireBaseConstants.Event.EVENT_SSO_INTERRUPTS_PUT_FAILURE, "", "")
            }
        }
    }

    @JvmStatic
    fun invokeKeepAliveSuccessFirebaseEvent(context: Context?) {
        try {
            FireBaseAnalyticsTracker.getInstance().logEvent(context,
                    FireBaseConstants.Event.EVENT_KEEP_ALIVE_SERVICE_SUCCESS,
                    FireBaseConstants.ParamName.PARAMETER_API_SUCCESS, FireBaseConstants.ParamValue.PARAMETER_VALUE_SUCCESS)
        } catch (ex: Exception) {
            LoggerUtils.exception(ex.message)
        }
    }

    @JvmStatic
    fun invokeKeepAliveFailureFirebaseEvent(context: Context?) {
        try {
            logFailureFirebaseEvent(TTGRuntimeData.getInstance().correlationId,
                    TTGRuntimeData.getInstance().httpStatusCode, context,
                    FireBaseConstants.Event.EVENT_KEEP_ALIVE_SERVICE_FAILURE, "", "")
        } catch (ex: Exception) {
            LoggerUtils.exception(ex.message)
        }
    }

    private fun logFirebaseTracking(context: Context, eventName: String, parmaName: String, paramValue: String) {
        var parmValue = paramValue
        try {
            if (parmValue != null && parmValue.length > 99) {
                parmValue = parmValue.substring(0, Math.min(parmValue.length, 99))
                FireBaseAnalyticsTracker.getInstance().logEvent(context, eventName, parmaName, parmValue.trim { it <= ' ' })
            }
        } catch (ex: Exception) {
            LoggerUtils.exception(ex.message)
        }
    }

    private fun logFailureFirebaseEvent(correlationID: String?, responseCode: Int, context: Context?,
                                        eventName: String?, paramName: String?, responseMessage: String?) {
        val paramValue = StringBuilder()
        paramValue.append(responseCode)
        paramValue.append(" - ")
        paramValue.append(if (TTGUtil.isEmpty(correlationID)) "N/A" else correlationID)
        val bundle = Bundle()
        // failure info
        if (paramValue.length > 99) {
            // split the param value into substrings of 99 or less and add to failure_info 1,2,3 ...
            val correlationIdList = splitParamValue(paramValue.toString())
            bundle.putString(FireBaseConstants.ParamName.PARAMETER_FAILED_CORRELATION_ID, correlationIdList[0])
            for (i in 1 until correlationIdList.size) {
                bundle.putString(FireBaseConstants.ParamName.PARAMETER_FAILED_CORRELATION_ID + i, correlationIdList[i])
            }
        } else {
            bundle.putString(FireBaseConstants.ParamName.PARAMETER_FAILED_CORRELATION_ID, paramValue.toString())
        }
        FireBaseAnalyticsTracker.getInstance().logEvent(context, eventName, bundle)
    }

    private fun splitParamValue(paramValue: String): List<String> {
        val size = 99
        val ret: MutableList<String> = ArrayList((paramValue.length + size - 1) / size)
        var start = 0
        while (start < paramValue.length) {
            ret.add(paramValue.substring(start, Math.min(paramValue.length, start + size)))
            start += size
        }
        return ret
    }

    @JvmStatic
    fun invokeSignOnFailureFirebaseEvent(context: Context, statusCode: Int, statusMessage: String) {
        var event: String?
        var others = false
        when (statusCode) {
            5 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_AUTH_FAILED_5
            6 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_ACCT_LOCKED_6
            7 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_BUSINESS_ERROR_7
            8 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_REG_NO_SUPPORT_8
            9 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_TERMINATED_MEMBER_9
            11 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_NMA_11
            12 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_PENDING_OTP_12
            20 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_SYSTEM_ERROR
            1000 -> event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_D1000
            else -> {
                // for any other status code, append the status code to SignOn_Failure_
                event = FireBaseConstants.Event.EVENT_SIGNON_FAILURE_OTHERS + statusCode
                others = true
            }
        }
        if (!Util.isEmptyString(event) && !Util.isEmptyString(statusMessage) && !others) {
            logFirebaseTracking(context, event,
                    FireBaseConstants.ParamName.PARAMETER_API_FAILURE,
                    "respcode:$statusCode msg:$statusMessage")
        }

        // for any other Sign on failure, call the below firebase event
        if (others) {
            logFailureFirebaseEvent(TTGRuntimeData.getInstance().correlationId,
                    TTGRuntimeData.getInstance().httpStatusCode, context,
                    event, "", "")
        }
    }
}