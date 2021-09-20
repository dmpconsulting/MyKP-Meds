package com.montunosoftware.pillpopper.model;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.montunosoftware.pillpopper.android.util.PillpopperApplication;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.ttgmobilelib.controller.TTGSignonController;
import org.kp.tpmg.ttgmobilelib.model.TTGKeepAliveRequestObj;

public class SessionAliveService extends JobIntentService {

	/**
	 * Unique job ID Session Alive Service request.
	 * This should be unique for all the requests assigning to this JobIntentService, otherwise we may have to create another class.
	 */
	public static final int SESSION_ALIVE_SERVICE_JOB_ID = 1002;
	public static final String ACTION_SESSION_ALIVE = "com.service.ACTION_KEEP_ALIVE_SERVICE";

	public static void startSessionAliveService(Context context) {
		try {
			Intent intent = new Intent(context, SessionAliveService.class);
			intent.setAction(ACTION_SESSION_ALIVE);
			enqueueWork(context, SessionAliveService.class, SESSION_ALIVE_SERVICE_JOB_ID, intent);
		} catch (Exception e){
			PillpopperLog.say(e);
		}
	}

	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		String action = intent.getAction();
		switch(action) {
			case ACTION_SESSION_ALIVE:
				if (ActivationController.getInstance().isSessionActive(this) && null != ActivationController.getInstance().getSSOSessionId(this)) {
					if (ActivationUtil.isNetworkAvailable(getApplicationContext())) {
						String ssoSession = ActivationController.getInstance().getSSOSessionId(this);
						new Thread(() -> {
                            PillpopperLog.say("Calling Keep Alive from SessionAliveHandler...");
                            try {
                                TTGKeepAliveRequestObj keepAliveRequestObj = new TTGKeepAliveRequestObj(AppConstants.ConfigParams.getKeepAliveCookieName(), AppConstants.ConfigParams.getKeepAliveCookieDomain(), ssoSession, AppConstants.ConfigParams.getKeepAliveCookiePath());
                                TTGSignonController.getInstance().performKeepAlive(keepAliveRequestObj, (PillpopperApplication) getApplication());
                            } catch (Exception e) {
                                PillpopperLog.say("Oops!, Exception while calling the keep alive service");
                            }
                        }).start();
					}
				} else {
					Util.cancelAlarm(this);
				}
				break;
			default:
				break;
		}


	}

}
