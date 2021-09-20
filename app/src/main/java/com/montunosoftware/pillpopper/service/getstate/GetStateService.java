package com.montunosoftware.pillpopper.service.getstate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.service.images.sync.ImageSynchronizerService;

public class GetStateService extends Service {

    private Context mContext;

    /**
     * Period of timer which starts a task to check the Log Entry Table
     * to determine where the GetState Api call has to be initiated.
     */
    private final int LOG_ENTRY_CHECK_TIMER_PERIOD = 30;

    /**
     * The thread which contains the Runnable interface to handle the
     * GetState and GetHistoryEvents Api calls."
     */
    private Thread mGetStateThread;
    /**
     * The Runnable Interface which contains the tasks for
     * GetState and GetHistoryEvents Api Calls
     */
    private Runnable mGetStateRunnable;

    /**
     * Handler to run the GetStateThread based on a Periodic Timer Event.
     */
    private Handler mGetStateHandler;

    //Default constructor
    public GetStateService() {

    }

    @Override
    public void onCreate() {
        mGetStateHandler = new Handler();
        mContext = this;
        mGetStateRunnable = () -> {
            //Sending all images before downloading the images using the initial get State
            if(FrontController.getInstance(mContext).isPendingImageRequestAvailable()) {
                ImageSynchronizerService.startImageSynchronization(mContext);
            }

            if(!PillpopperRunTime.getInstance().isFirstTimeSyncDone()) {

                if(!FrontController.getInstance(mContext).isLogEntryAvailable()) {
                    PillpopperLog.say("GetStateService: onCreate(): Starting thread to get initial user state. APICall: 'GetState'");
                    StateDownloadIntentService.startActionGetState(getBaseContext());
                } else {
                    PillpopperLog.say("GetStateService: onCreate(): Starting thread to get state after posting log entries. APICall: 'GetState'");
                    StateDownloadIntentService.startActionIntermediateGetState(getBaseContext());
                    StateDownloadIntentService.startActionGetState(getBaseContext());
                }

                //TODO This needs to enable for GetAllRefill Reminders API Request.
                //PillpopperLog.say(("GetAllRefillReminders: invoking the req"));
                StateDownloadIntentService.startActionGetAllRefillReminders(getBaseContext());

                PillpopperLog.say(("GetStateService: onCreate(): Starting thread for action GetHistoryEvents."));
                StateDownloadIntentService.startActionGetHistoryEvents(getBaseContext());
                PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(true);
                /*Intent initialGetStateBroadcastIntent = new Intent();
                initialGetStateBroadcastIntent.setAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
                sendBroadcast(initialGetStateBroadcastIntent);*/
            } else {
                if(FrontController.getInstance(mContext).isLogEntryAvailable()) {
                    PillpopperLog.say("** Starting action IntermediateGetState API Call **");
                    StateDownloadIntentService.startActionIntermediateGetState(getBaseContext());
                    PillpopperLog.say("** End of action IntermediateStateCall API Call **");
                   /* Intent intermediateGetStateBroadcastIntent = new Intent();
                    intermediateGetStateBroadcastIntent.setAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
                    sendBroadcast(intermediateGetStateBroadcastIntent);*/
                } else {
                    PillpopperLog.say("** Skipping action IntermediateGetState **");
                    PillpopperLog.say("Info: No User changes detected.");
                    Intent intermediateGetStateBroadcastIntent = new Intent();
                    intermediateGetStateBroadcastIntent.setAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
                    sendBroadcast(intermediateGetStateBroadcastIntent);

                    Intent registrationPopupRemoverBroadcastIntent = new Intent();
                    registrationPopupRemoverBroadcastIntent.setAction(StateDownloadIntentService.BROADCAST_REMOVE_REGISTRATION_POPUP);
                    sendBroadcast(registrationPopupRemoverBroadcastIntent);

                }
            }

            mGetStateHandler.postDelayed(mGetStateRunnable, LOG_ENTRY_CHECK_TIMER_PERIOD * 1000);
        };

        mGetStateThread = new Thread(mGetStateRunnable);
        mGetStateThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        PillpopperLog.say("GetStateService: onDestroy(): User is exiting the application. Stopping GetStateService.class");
        mGetStateHandler.removeCallbacks(mGetStateRunnable);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        PillpopperLog.say("Application being killed. Stopping the GetStateService.");
        stopSelf();
    }
}
