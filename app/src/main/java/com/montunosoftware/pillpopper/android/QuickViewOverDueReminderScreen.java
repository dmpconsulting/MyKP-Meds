package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.pillpopper.android.util.NotificationBar_OverdueDose;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.State;
import com.montunosoftware.pillpopper.model.StateUpdatedListener;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoginActivity;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class QuickViewOverDueReminderScreen extends StateListenerActivity implements StateUpdatedListener
{
	public static final int _REQ_EDIT_SCHEDULE = 0;
	List<Drug> overduedrugs;
	@SuppressWarnings("unused")
	private State _currState;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		RunTimeData.getInstance().setNeedToAnimateLogin(false);
		_updateView();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		getState().unregisterStateUpdatedListener(this);
	}

	Util util;
	LinkedHashMap<Long, List<Drug>> currentRemindersMap = new LinkedHashMap<>();
	LinkedHashMap<Long, List<Drug>> passedRemindersMap = new LinkedHashMap<>();
	private void _updateView() {
		overduedrugs = new ArrayList<>();
		overduedrugs = PillpopperRunTime.getInstance().getQuickViewReminderDrugs();
		if (null != overduedrugs && !overduedrugs.isEmpty()) {
			util = Util.getInstance();
			util.prepareRemindersMapData(overduedrugs, _thisActivity);
			currentRemindersMap = PillpopperRunTime.getInstance().getmCurrentRemindersMap();
			passedRemindersMap = PillpopperRunTime.getInstance().getmPassedRemindersMap();
			Intent launchIntent = new Intent(_thisActivity, ReminderContainerActivity.class);
			if (null != currentRemindersMap && currentRemindersMap.size() > 0) {
				PillpopperLog.say("-- Current Reminders Not Null... Have to call Current Reminders " + currentRemindersMap.size());
				FrontController.getInstance(_thisActivity).showLateRemindersWhenFromNotifications(_thisActivity);
				launchIntent.putExtra("launch", "CurrentReminderActivity");
				_thisActivity.startActivity(launchIntent);
			} else if (null != passedRemindersMap && passedRemindersMap.size() > 0) {
				PillpopperLog.say("-- Passed Reminders Not Null... Have to call Passed Reminders " + passedRemindersMap.size());
				if (Util.canShowLateReminder(_thisActivity)) {
					launchIntent.putExtra("launch", "PastReminder");
					_thisActivity.startActivity(launchIntent);
				} else {
					Intent intent = new Intent(QuickViewOverDueReminderScreen.this, LoginActivity.class);
					_thisActivity.startActivity(intent);
				}

			} else {
				Intent intent = new Intent(QuickViewOverDueReminderScreen.this, LoginActivity.class);
				_thisActivity.startActivity(intent);
			}
		} else {
			// Cancel the notification once there are no drugs overdue
			NotificationBar_OverdueDose.cancelNotificationBar(getPillpopperActivity());
			AppConstants.setByPassLogin(false);
		}
	}

	@Override
	public void onBackPressed()
	{
		AppConstants.setByPassLogin(false);
		finish();
		super.onBackPressed();
	}

	@Override
	public void onStateUpdated()
	{
		_updateView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		ActivationController.getInstance().stopTimer(this);
		NotificationBar_OverdueDose.cancelNotificationBar(getPillpopperActivity());
		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("com.montunosoftware.pillpopper.SUPPRESS_PENDING_NOTIFICATIONS"));
	}

	public void onActivityResult(int requestCode, int resultCode, Intent resultIntent)
	{
		super.onActivityResult(requestCode, resultCode, resultIntent);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case _REQ_EDIT_SCHEDULE:
				break;
			}
		}
		_updateView();
	}

}
