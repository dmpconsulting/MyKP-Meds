package com.montunosoftware.pillpopper.android.view;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.service.getstate.GetStateService;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

public class RegistrationPopup
{
	private ProgressDialog _progressDialog = null;

	private PillpopperActivity mPillpopperActivity;

	BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			handleInitialGetStateComplete();
		}
	};

	public interface RegistrationCallbacks
	{
		void onRegistrationSuccess();
		void onRegistrationFailure();
	}

	public void abandonPopup()
	{
		_progressDialog = null;
	}

	public void dismiss()
	{
		if(null!=_progressDialog && _progressDialog.isShowing()){
			_progressDialog.dismiss();
		}
	}

	public boolean isShowing(){
		return null != _progressDialog && _progressDialog.isShowing();
	}

	public void register(final PillpopperActivity act, final RegistrationCallbacks registrationCallbacks)
	{
		IntentFilter removeDiscontinuedMedsFilter = new IntentFilter();
		removeDiscontinuedMedsFilter.addAction(StateDownloadIntentService.BROADCAST_REMOVE_REGISTRATION_POPUP);
		act.registerReceiver(mGetStateBroadcastReceiver,removeDiscontinuedMedsFilter);
		mPillpopperActivity = act;

		final class ServerTask extends AsyncTask<Void, Void, String>
		{

			@Override
			protected String doInBackground(Void... arg0)
			{
				try {
					Util.setCreateUserRequestInprogress(true);
						if(null!=RunTimeData.getInstance().getRegistrationResponse()){
							return RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers().get(0).getUserId();
						}
						return null;
				} catch (Exception e) {
					PillpopperLog.say(e);
					return null;
				}
			}

			private BroadcastReceiver showDialogReveiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (null != act && !act.isFinishing()) {
						Util.showSessionexpireAlert(act, PillpopperAppContext.getGlobalAppContext(act));
						LocalBroadcastManager.getInstance(act).unregisterReceiver(showDialogReveiver);
					}
				}
			};

			@Override
			protected void onPostExecute(String accountId)
			{
				PillpopperLog.say("Register call got the response and the accountID is : "+ accountId);
				if (accountId == null) {
					// registration failed. Take off the spinner and show the error.
					if (_progressDialog != null) {
						_progressDialog.cancel();
						_progressDialog = null;
					}
                    RunTimeData.getInstance().setLoadingInProgress(false);
					if(!act.isFinishing()){
						PillpopperLog.say("Debug server error got the register call response and found that accountID is empty, so showing internal server error alert");
						DialogHelpers.showAlertDialog(act, R.string.server_failure, () -> registrationCallbacks.onRegistrationFailure());
					}
				} else {
					if(accountId.equalsIgnoreCase(Util.STATUS_CODE_125_STRING)){
						if (_progressDialog != null) {
							_progressDialog.cancel();
							_progressDialog = null;
						}
                        RunTimeData.getInstance().setLoadingInProgress(false);
                        if(!act.isFinishing()){
                            Util.showSessionexpireAlert(act.getPillpopperActivity(),act.getGlobalAppContext());
						}
					}else{
						// Registration succeeded. Keep the spinner up for now and strt
						// a sync to download initial state.
						Util.setCreateUserRequestInprogress(false);
						act.getState().setAccountId(accountId);

						PillpopperLog.say("Registration popup log");
						LocalBroadcastManager.getInstance(act).registerReceiver(showDialogReveiver, new IntentFilter());
						Intent intent = new Intent(act, GetStateService.class);
						act.startService(intent);
					}
				}
			}
		}

		if (_progressDialog == null) {
			try {
				_progressDialog = ProgressDialog.show(act.getAndroidContext(), "", act.getString(R.string.server_progress));
				_progressDialog.show();
				RunTimeData.getInstance().setLoadingInProgress(false);
			} catch (Exception e) {
				PillpopperLog.say("Oops, Exception when trying to show the progressbar and caused window leaked exception");
			}
			new ServerTask().execute();
		}
	}

	public void handleInitialGetStateComplete() {

		PillpopperLog.say("Working sync");
		try {
			// finally, take down the spinner and call the completion callback
			if (_progressDialog != null && _progressDialog.isShowing() /*&& !act.isFinishing()*/) {
				PillpopperLog.say("-- Get History Events call is got over and needs to be kill the dialogue");
				_progressDialog.cancel();
				_progressDialog = null;
			}
		}catch (Exception e){
			PillpopperLog.exception(e.getMessage());
		}
        RunTimeData.getInstance().setLoadingInProgress(false);
		if(!RunTimeData.getInstance().isDiscontinuedAlertShown() && FrontController.getInstance(mPillpopperActivity.getAndroidContext()).getDiscontinuedMedicationsCount() > 0){
			RunTimeData.getInstance().setDiscontinuedAlertShown(true);
		}
		mPillpopperActivity.unregisterReceiver(mGetStateBroadcastReceiver);

	}
}