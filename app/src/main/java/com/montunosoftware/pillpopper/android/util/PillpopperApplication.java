/**
 * Copyright (c) 2008-2020 Zetetic LLC
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the ZETETIC LLC nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY ZETETIC LLC ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ZETETIC LLC BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.montunosoftware.pillpopper.android.util;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.montunosoftware.pillpopper.AlarmResetWorkManager;
import com.montunosoftware.pillpopper.android.Splash;
import com.montunosoftware.pillpopper.android.firebaseMessaging.FCMHandler;
import com.montunosoftware.pillpopper.android.refillreminder.database.RefillReminderDbConstants;
import com.montunosoftware.pillpopper.android.refillreminder.database.SupportRefillReminderDbHelper;
import com.montunosoftware.pillpopper.database.persistence.SupportDatabaseHelper;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.squareup.leakcanary.LeakCanary;

import net.sqlcipher.database.SQLiteDatabase;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ClearAllStoreDataController;
import org.kp.tpmg.mykpmeds.activation.controller.CookieResetController;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttgmobilelib.controller.TTGCallBackInterfaces;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.kp.tpmg.mykpmeds.activation.AppConstants.IS_FROM_PILL_POPPER_APPLICATION;

public class PillpopperApplication extends Application implements TTGCallBackInterfaces.KeepAlive {
    private AlarmManager alarmManager;
    public static final String DATABASE_NAME = "mykpmeds.db";
    private int DATABASE_VERSION;
    private SupportDatabaseHelper openHelper;
    private SupportRefillReminderDbHelper refillReminderDbHelper;
    private boolean inBackground = false;
    private Context ctx;
    private static final int MINUTES_IN_A_DAY = 1440;

    /**
     * added work manager to reset alarm in every 24 hour starting at 12am
     */
    public void alarmResetHandler() {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int initialDelay = MINUTES_IN_A_DAY - (hours * 60 + minutes);

        final PeriodicWorkRequest periodicWorkRequest1 = new PeriodicWorkRequest.Builder(AlarmResetWorkManager.class, AppConstants.ALARM_RESET_TIME, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .build();
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(periodicWorkRequest1);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //end of Firebase init
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        RefillRuntimeData.getInstance().setContext(getApplicationContext());

        try {
            SQLiteDatabase.loadLibs(this);
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        }

        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(
                this, AppConstants.AUTH_CODE_PREF_NAME);
        if (!mSharedPrefManager.getBoolean("RecurringWorkManagerSet", false)) {
            mSharedPrefManager.putBoolean("RecurringWorkManagerSet", true, false);
            alarmResetHandler();
        }

        DATABASE_VERSION = Util.getAppVersionCode(this);
        RunTimeData.getInstance().setmContext(this);
        openHelper = new SupportDatabaseHelper();
        refillReminderDbHelper = new SupportRefillReminderDbHelper(this, RefillReminderDbConstants.MYMEDS_REFILL_DATABASE, null,
                DATABASE_VERSION);
        ctx = getApplicationContext();
        //RxRefillAppDynamicsController.getInstance(ctx).initAppDynamics();
        CookieResetController.registerApp(() -> {

            PillpopperLog.say("-----Calling form library stating Timeout detected ---- So have to reset the cookies..");
            stopKeepAliveTimer();
            Util.autoSignoutResetCookies(ctx);
        });

        ClearAllStoreDataController.registerForClearData(() -> {
            PillpopperLog.say("ClearAllStoreDataController Received Call Back");
            launchSplash();
        });
    }

    // When user clicks on reset/Clear stored data in settings screen, the following methods gets executed.
    // Otherwise when user kills and launches the app we don't have to launch Splash explicitly.
    private void launchSplash() {
        PillpopperLog.say("ClearAllStoreDataController Invoking Splash ");
        Intent intent = new Intent(ctx, Splash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IS_FROM_PILL_POPPER_APPLICATION, true);
        startActivity(intent);
    }


    public SupportDatabaseHelper getOpenHelper() {
        return openHelper;
    }

    public SupportRefillReminderDbHelper getRefillReminderDbHelper() {
        return refillReminderDbHelper;
    }

    public boolean isInBackground() {
        return inBackground;
    }

    public void setAppIsInBackground(boolean flag) {
        inBackground = flag;
    }

    static StringBuilder _logEntries = new StringBuilder();

    public static void log(String s) {
        _logEntries.append(PillpopperTime.getDebugString(PillpopperTime.now()));
        _logEntries.append(" ");
        _logEntries.append(s);
        _logEntries.append("\n");
    }

    public void startKeepAliveTimer() {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
            Util.setAlarm(this, alarmManager);
        }

    }

    public void stopKeepAliveTimer() {
        Util.cancelAlarm(this);
        alarmManager = null;
    }

    @Override
    public void onKeepAliveSuccess(String ssoSession) {
        AppData.getInstance().setSSOSessionId(getApplicationContext(), ssoSession);
        PillpopperLog.say(" PillpopperApplication : Session Alive update to vordel success.\nNew Sessionid=" + ssoSession);

    }

    @Override
    public void onKeepAliveError(int i) {
        PillpopperLog.say("PillpopperApplication : Session Alive update to vordel Failed.");
    }

}
