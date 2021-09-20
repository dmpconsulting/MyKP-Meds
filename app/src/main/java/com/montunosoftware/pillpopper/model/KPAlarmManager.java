package com.montunosoftware.pillpopper.model;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

public class KPAlarmManager extends BroadcastReceiver {

	@SuppressLint("Wakelock")
	@Override
	public void onReceive(Context context, Intent intent) {

		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, "partialWakeLock:");
		wl.acquire();
		try {
			SessionAliveService.startSessionAliveService(context);
		}catch(Exception e){
			LoggerUtils.exception("Error in calling keep alive : "+ e.getMessage());
		}
		wl.release();
	}

}
