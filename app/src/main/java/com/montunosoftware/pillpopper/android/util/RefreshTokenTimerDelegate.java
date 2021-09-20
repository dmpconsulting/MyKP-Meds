package com.montunosoftware.pillpopper.android.util;

import android.content.Context;

import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.service.TokenService;

import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.Timer;
import java.util.TimerTask;

public class RefreshTokenTimerDelegate {
    private static RefreshTokenTimerDelegate timerDelegate;
    private Timer timer;
    private static TimerTask task;

    public static synchronized RefreshTokenTimerDelegate getInstance() {
        if (timerDelegate == null)
            timerDelegate = new RefreshTokenTimerDelegate();
        return timerDelegate;
    }

    private RefreshTokenTimerDelegate() {
        timer = new Timer();
    }

    public void startTimerTask(Context context) {
        resetTimerTask(false);
        task = new RefreshTokenTimerDelegate.MyTask(context);
        int duration = (Integer.parseInt(FrontController.getInstance(context).getTokenExpiryTime(context)) - 300) * 1000; // milliseconds
        LoggerUtils.info("--API manager-- Refresh task scheduled--" + duration + "milliseconds");
        timer.schedule(task, duration > 0 ? duration : 0);
    }

    public void resetTimerTask(boolean nullify) {
        stopTimer(nullify);
    }

    private void stopTimer(boolean nullify) {
        if (task != null) {
            LoggerUtils.info("--API manager-- Refresh task cancelled--" + task.cancel());
            task.cancel();
            ((RefreshTokenTimerDelegate.MyTask) task).closeContext();
        }
        task = null;
        if (nullify)
            timerDelegate = null;
    }


    static class MyTask extends TimerTask implements TokenService.TokenRefreshAPIListener {
        private Context context;

        public MyTask(Context context) {
            this.context = context;
        }

        public void closeContext() {
            context = null;
        }

        @Override
        public void run() {
            task.cancel();
            task = null;
            LoggerUtils.info("--API manager-- Refresh task executing--");
            TokenService.startRefreshAccessTokenService(context, this);
        }

        @Override
        public void tokenRefreshSuccess() {
            LoggerUtils.info("--API manager-- Refresh task success--");
            //on success schedule a timer task for refresh.
            AppData.getInstance().startRefreshTokenTimerTask(context);
        }

        @Override
        public void tokenRefreshError() {

        }
    }

}
