package com.montunosoftware.pillpopper.service;

import android.os.AsyncTask;

import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;


/**
 * Utility AsyncTask toupdate the log entry table with the provided action
 */
public class LogEntryUpdateAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private final PillpopperActivity mPillpopperActivity;
    private final FrontController mFrontController;
    private final PillpopperAppContext appContext;
    private String mAction;
    private Drug mDrug;

    public LogEntryUpdateAsyncTask(PillpopperActivity context, String action, Drug drug){
        mPillpopperActivity = context;
        appContext = PillpopperAppContext.getGlobalAppContext(context);
        mFrontController = FrontController.getInstance(mPillpopperActivity);
        this.mAction = action;
        this.mDrug = drug;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        mFrontController.addLogEntry(mPillpopperActivity, Util.prepareLogEntryForAction(mAction, mDrug, mPillpopperActivity));
        return true;
    }

    @Override
    protected void onPostExecute(Boolean logEntryUpdateStatus) {
        super.onPostExecute(logEntryUpdateStatus);
        if (logEntryUpdateStatus) {
            PillpopperLog.say("LogEntry Has been added to DB log entry table");
        }else{
            PillpopperLog.say("LogEntry not added to DB log entry table");
        }
    }
}
