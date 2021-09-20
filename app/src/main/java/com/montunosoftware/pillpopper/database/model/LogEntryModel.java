package com.montunosoftware.pillpopper.database.model;

import android.content.Context;
import android.text.TextUtils;

import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONObject;

/**
 * @author
 * Created by M1023050 on 5/8/2016.
 */
public class LogEntryModel {

    private long dateAdded;
    private String replyID;
    private JSONObject entryJSONObject;
    private long lastUploadAttempt;
    private long lastUploadResponse;
    private String action;


    public String getReplyID() {
        return replyID;
    }

    public void setReplyID(String replyID) {
        this.replyID = replyID;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public JSONObject getEntryJSONObject() {
        return entryJSONObject;
    }

    public void setEntryJSONObject(JSONObject entryJSONObject, Context context) {
        this.entryJSONObject = Util.processPillRequestObjectFrom(entryJSONObject,context);
    }

    public long getLastUploadAttempt() {
        return lastUploadAttempt;
    }

    public void setLastUploadAttempt(long lastUploadAttempt) {
        this.lastUploadAttempt = lastUploadAttempt;
    }

    public long getLastUploadResponse() {
        return lastUploadResponse;
    }

    public void setLastUploadResponse(long lastUploadResponse) {
        this.lastUploadResponse = lastUploadResponse;
    }


    public int getAction() {
        int actionType = 0;
        if(!TextUtils.isEmpty(action)) {
            if (action.equalsIgnoreCase(PillpopperConstants.ACTION_CREATE_PILL)) {
                actionType = 1;
            } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_TAKE_PILL)) {
                actionType = 2;
            } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_SKIP_PILL)) {
                actionType = 3;
            } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                actionType = 4;
            }
        }
        return actionType;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
