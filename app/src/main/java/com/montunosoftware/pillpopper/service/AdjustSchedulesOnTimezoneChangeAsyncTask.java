package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.os.AsyncTask;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;

import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.TimeZone;

/**
 * Created by M1024581 on 8/23/2017.
 */

public class AdjustSchedulesOnTimezoneChangeAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private Context context;

    public AdjustSchedulesOnTimezoneChangeAsyncTask(Context context){
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        LoggerUtils.info("--- TimeZone Change Listener -- ");
        JSONObject request = Util.checkForDSTAndPrepareAdjustPillLogEntryObject(context);
        if(null!=request){
            try {
                JSONObject response = PillpopperServer.getInstance(context, PillpopperAppContext.getGlobalAppContext(context)).
                        makeRequestInNonSecureMode(request, context);
            } catch (PillpopperServer.ServerUnavailableException e) {
                PillpopperLog.say("--- Exception while calling Adjust Pill in non secure mode");
            }
        }else{
            FrontController.getInstance(context).updateHistoryOffsetForLast48HourEvents(Util.getTzOffsetSecs(TimeZone.getDefault()));
        }

        LoggerUtils.info("--- TimeZone Change Listener -- Schedule adjustment done");
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        // some flag to indicate the timezone adjustment is success.
        // if failed, it can again be started in splash
    }
}
