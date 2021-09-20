package com.montunosoftware.pillpopper.android.refillreminder;

import android.content.Context;

import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.controller.FrontController;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by M1024581 on 2/14/2018.
 */

public class RequestWrapper {

    private final Context mContext;
    private String _hardwareId;

    public RequestWrapper (Context context){
        mContext = context;
        //need to make a copy of UniqueDeviceId.Java when refill reminder is made as a module
        _hardwareId = UniqueDeviceId.getHardwareId(context);
    }

    public JSONObject createRefillReminderRequestObject(RefillReminder refillReminder){
        JSONObject requestObject = new JSONObject();

        try {
            String deviceDefaultTzSecs = String.valueOf(RefillReminderUtils.getTzOffsetSecs());
            JSONObject pillPopperRequestObj = new JSONObject();
            pillPopperRequestObj.put("reminderGuid", refillReminder.getReminderGuid());
            pillPopperRequestObj.put("userId", refillReminder.getUserId());
            pillPopperRequestObj.put("recurring",refillReminder.isRecurring());
            pillPopperRequestObj.put("frequency",refillReminder.getFrequency());
            pillPopperRequestObj.put("reminder_end_date",
                    RefillReminderUtils.convertDateLongToIso(refillReminder.getReminderEndDate()));
            pillPopperRequestObj.put("reminder_end_tz_secs",deviceDefaultTzSecs);
            pillPopperRequestObj.put("reminderNote",refillReminder.getReminderNote());
            pillPopperRequestObj.put("next_reminder_date",
                    RefillReminderUtils.convertDateLongToIso(refillReminder.getNextReminderDate()));
            pillPopperRequestObj.put("next_reminder_tz_secs",refillReminder.getNextReminderTzSecs());
            pillPopperRequestObj.put("overdue_reminder_date",
                        RefillReminderUtils.convertDateLongToIso(refillReminder.getOverdueReminderDate()));
            pillPopperRequestObj.put("overdue_reminder_tz_secs",deviceDefaultTzSecs);
            pillPopperRequestObj.put("last_acknowledge_date",
                    RefillReminderUtils.convertDateLongToIso(refillReminder.getLastAcknowledgeDate()));
            pillPopperRequestObj.put("last_acknowledge_date_tz_secs",deviceDefaultTzSecs);
            pillPopperRequestObj.put("action", RefillReminderConstants.ACTION_UPDATE_REFILL_REMINDERS);
            pillPopperRequestObj.put("language", RefillReminderUtils.getLanguage());
            pillPopperRequestObj.put("clientVersion", RefillReminderUtils.getAppVersion(mContext));
            pillPopperRequestObj.put("partnerId",RefillReminderConstants.PARTNER_ID);
            pillPopperRequestObj.put("apiVersion",RefillReminderUtils.getAppVersion(mContext));
            pillPopperRequestObj.put("hardwareId",_hardwareId);
            pillPopperRequestObj.put("replayId", RefillReminderUtils.getReplyId());

            requestObject.put("pillpopperRequest", pillPopperRequestObj);
        } catch (JSONException e) {
            RefillReminderLog.say("Exception createRefillReminderRequestObject", e);
        }

        return requestObject;
    }

    /**
     * Prepare the JSON request object for "ListRefillReminder" API.
     * @param userID
     * @return
     */
    public JSONObject createGetAllRefillRemindersRequest(String userID){
        JSONObject requestObject = new JSONObject();
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("userId", userID);
            jsonRequest.put("action", RefillReminderConstants.ACTION_LIST_REFILL_REMINDERS);
            jsonRequest.put("language", RefillReminderUtils.getLanguage());
            jsonRequest.put("clientVersion", RefillReminderUtils.getAppVersion(mContext));
            jsonRequest.put("partnerId", RefillReminderConstants.PARTNER_ID);
            jsonRequest.put("apiVersion", RefillReminderUtils.getAppVersion(mContext));
            jsonRequest.put("hardwareId", _hardwareId);

            requestObject.put("pillpopperRequest", jsonRequest);


        } catch (Exception e) {
            RefillReminderLog.say(e);
        }
        RefillReminderLog.say("createGetAllRefillRemindersRequest : " + requestObject.toString());
        return requestObject;
    }


    /**
     * Creates the DeleteRefillReminder API request JSONObject
     * @param reminderGUID
     * @return
     */
    public JSONObject createDeleteRefillReminderRequest(String reminderGUID){
        JSONObject reqJsonObject = new JSONObject();
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(RefillReminderConstants.JSON_KEY_REMINDER_GUID, reminderGUID);
            jsonRequest.put(RefillReminderConstants.JSON_KEY_USER_ID, FrontController.getInstance(mContext).getPrimaryUserIdIgnoreEnabled());
            jsonRequest.put(RefillReminderConstants.JSON_KEY_ACTION, RefillReminderConstants.ACTION_DELETE_REFILL_REMINDER);
            reqJsonObject.put(RefillReminderConstants.JSON_KEY_PILLPOPPER_REQUEST, prepareDefaultKeys(jsonRequest));
        } catch (Exception e) {
            RefillReminderLog.say(e);
        }
        RefillReminderLog.say("createDeleteRefillReminderRequest : " + reqJsonObject.toString());
        return reqJsonObject;
    }

    /**
     * Creates the AcknowledgeRefillReminder API JSON Request Object
     * @param refillReminder
     * @return
     */
    public JSONObject createAcknowledgeRefillRequest(RefillReminder refillReminder){
        JSONObject reqJsonObject = new JSONObject();
        JSONObject jsonRequest = new JSONObject();
        try {
            String tzSec = String.valueOf(RefillReminderUtils.getTzOffsetSecs());
            jsonRequest.put(RefillReminderConstants.JSON_KEY_REMINDER_GUID, refillReminder.getReminderGuid());
            jsonRequest.put(RefillReminderConstants.JSON_KEY_USER_ID, FrontController.getInstance(mContext).getPrimaryUserIdIgnoreEnabled());
            jsonRequest.put(RefillReminderConstants.JSON_KEY_NEXT_REMINDER_DATE_REQUEST,  RefillReminderUtils.convertDateLongToIso(refillReminder.getNextReminderDate()));
            jsonRequest.put(RefillReminderConstants.JSON_KEY_NEXT_REMINDER_TZ_SEC_REQUEST, tzSec);
            jsonRequest.put(RefillReminderConstants.JSON_KEY_OVERDUE_REMINDER_DATE_REQUEST,  "null");
            jsonRequest.put(RefillReminderConstants.JSON_KEY_OVERDUE_REMINDER_TZ_SEC_REQUEST, tzSec);
            jsonRequest.put(RefillReminderConstants.JSON_KEY_LAST_ACK_DATE_REQUEST,  RefillReminderUtils.convertDateLongToIso(refillReminder.getLastAcknowledgeDate()));
            jsonRequest.put(RefillReminderConstants.JSON_KEY_LAST_ACK_TZ_SEC_REQUEST, tzSec);
            jsonRequest.put(RefillReminderConstants.JSON_KEY_ACTION, RefillReminderConstants.ACTION_ACKNOWLEDGE_REFILL_REMINDER);
            reqJsonObject.put(RefillReminderConstants.JSON_KEY_PILLPOPPER_REQUEST, prepareDefaultKeys(jsonRequest));
        } catch (Exception e) {
            RefillReminderLog.say(e);
        }
        RefillReminderLog.say("Ack Req : " + reqJsonObject.toString());
        return reqJsonObject;
    }


    /**
     * Fills the request JSONObject with the default keys.
     * @param requestObject
     * @return
     */
    public JSONObject prepareDefaultKeys(JSONObject requestObject){
        try {
            requestObject.put(RefillReminderConstants.JSON_KEY_LANGUAGE, RefillReminderUtils.getLanguage());
            requestObject.put(RefillReminderConstants.JSON_KEY_CLIENT_VERSION, RefillReminderUtils.getAppVersion(mContext));
            requestObject.put(RefillReminderConstants.JSON_KEY_PARTNER_ID, RefillReminderConstants.PARTNER_ID);
            requestObject.put(RefillReminderConstants.JSON_KEY_API_VERSION, RefillReminderUtils.getAppVersion(mContext));
            requestObject.put(RefillReminderConstants.JSON_KEY_HARDWARE_ID, _hardwareId);
            requestObject.put(RefillReminderConstants.JSON_KEY_REPLAY_ID, RefillReminderUtils.getReplyId());
        } catch (Exception e){
            RefillReminderLog.say(e);
        }
        return  requestObject;
    }
}
