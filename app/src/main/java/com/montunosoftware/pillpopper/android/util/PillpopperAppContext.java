package com.montunosoftware.pillpopper.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.pillpopper.android.PillpopperReplyContext;
import com.montunosoftware.pillpopper.android.Splash;
import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PremiumState;
import com.montunosoftware.pillpopper.model.State;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoginActivity;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class PillpopperAppContext
{
	////////////////////////////////////////////////////////////////////////////
	// handling of singleton class

	private static PillpopperAppContext _globalAppContext;
	public static final String PILLPOPPER_BROADCAST_PERMISSION="org.kp.tpmg.android.mykpmeds.permission.BROADCAST";
	public static final String ACTION_REMINDER_NOTIFICATION_SUPPRESSOR = "com.montunosoftware.pillpopper.SUPPRESS_PENDING_NOTIFICATIONS";

	public static synchronized PillpopperAppContext getGlobalAppContext(Context context)
	{
		if (_globalAppContext == null) {
			_globalAppContext = new PillpopperAppContext(context);
		}

		return _globalAppContext;
	}

	public void kpSignoutForDeepLinking(Context context) {
		ActivationController activationController = ActivationController.getInstance();
		activationController.performSilentSignoff(context);
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	// support for customized editions

	public enum Edition {
		KP
	}

	public Edition getEdition()
	{
		return Edition.KP;
	}

	public String getEditionName()
	{
		return "KP";
	}

	public boolean isPartnerLibrary()
	{
		return true;
	}

	public boolean isUsingDoctorPharmacy()
	{
		return false;
	}


	public boolean isTrackingInventory()
	{
		return false;
	}

	public boolean isTrackingRefillsRemaining()
	{
		return false;
	}

	public boolean isTrackingMultiplePeople()
	{
		return false;
	}


	private State _state;


	///////////////////////////////////////////////////////////////////////////////////////
	// Facility for passing arguments between activities in case where we know the 
	// app context will survive.  The "argument passer" object stores key-value pairs,
	// with arbitrary objects as keys.  The entire ArgumentPasser itself has a key that gets
	// passed through the intent used to call the subactivity.  The subactivity uses that key
	// to retrieve the ArgumentPasser object.
	private HashMap<String, ArgumentPasser> _passedArguments = new HashMap<>();
	private static final String _KEY_ARG_PASSER_ID = "ARG_PASSER_ID";

	// litle helper class
	private static class MetaArguments
	{
		private String argPasserGuid;
		private String actor;
		private String argsAreFor;

		public String getArgPasserGuid() {
			return argPasserGuid;
		}

		public void setArgPasserGuid(String argPasserGuid) {
			this.argPasserGuid = argPasserGuid;
		}

		public String getActor() {
			return actor;
		}

		public void setActor(String actor) {
			this.actor = actor;
		}

		public String getArgsAreFor() {
			return argsAreFor;
		}

		public void setArgsAreFor(String argsAreFor) {
			this.argsAreFor = argsAreFor;
		}
	}

	private ArgumentPasser _putArguments(MetaArguments metaArgs)
	{
		PillpopperLog.say("%s trying to put arguments for %s: %s",
				metaArgs.getActor(),
				metaArgs.getArgsAreFor(),
				metaArgs.getArgPasserGuid()
				);
		ArgumentPasser argPasser = new ArgumentPasser();
		_passedArguments.put(metaArgs.getArgPasserGuid(), argPasser);
		return argPasser;
	}

	private ArgumentPasser _getArguments(MetaArguments metaArgs) throws ArgumentPasser.ArgumentPassException
	{
		PillpopperLog.say("%s trying to get arguments for %s: %s",
				metaArgs.getActor(),
				metaArgs.getArgsAreFor(),
				metaArgs.getArgPasserGuid()
				);				
		ArgumentPasser retval = _passedArguments.get(metaArgs.getArgPasserGuid());

		if (retval == null) {
			PillpopperLog.say("ERROR: ...%s does not exist!  Global app context terminated?", metaArgs.getArgPasserGuid());
			throw new ArgumentPasser.ArgumentPassException();
		}

		return retval;
	}


	private void _killArguments(MetaArguments metaArgs)
	{
		if (metaArgs.getArgPasserGuid() == null)
			return;

		PillpopperLog.say("trying to kill arguments from %s to %s: %s",
				metaArgs.getActor(),
				metaArgs.getArgsAreFor(),
				metaArgs.getArgPasserGuid()
				);				

		if (!_passedArguments.containsKey(metaArgs.getArgPasserGuid())) {
			PillpopperLog.say("ERROR: %s Couldn't kill non-existent argument %s", metaArgs.getActor(), metaArgs.getArgPasserGuid());
		} else {
			_passedArguments.remove(metaArgs.getArgPasserGuid());
		}
	}


	// Make sure we're not leaking any arguments; if so, kill them
	public void ensureNoArguments()
	{
		for (String s: _passedArguments.keySet()) {
			PillpopperLog.say("ERROR: arguments %s were not correctly killed!", s);
		}

		_passedArguments.clear();
	}


	//// intent versions

	private MetaArguments _getMetaArguments(PillpopperReplyContext context, Intent intent)
	{
		MetaArguments retval = new MetaArguments();

		// may get overwritten if we're putting args
		retval.setArgPasserGuid(intent.getStringExtra(_KEY_ARG_PASSER_ID));

		retval.setActor(context.getDebugName());

		if (intent.getComponent() == null) {
			retval.setArgsAreFor("result intent");
		} else {
			retval.setArgsAreFor(intent.getComponent().getClassName());
		}

		return retval;
	}

	public ArgumentPasser putArguments(PillpopperReplyContext context, Intent intent)
	{
		MetaArguments metaArgs = _getMetaArguments(context, intent);
		metaArgs.setArgPasserGuid(Util.getRandomGuid());
		intent.removeExtra(_KEY_ARG_PASSER_ID);
		intent.putExtra(_KEY_ARG_PASSER_ID, metaArgs.getArgPasserGuid());
		return _putArguments(metaArgs);
	}

	public ArgumentPasser getArguments(PillpopperReplyContext context, Intent intent) throws ArgumentPasser.ArgumentPassException
	{
		return _getArguments(_getMetaArguments(context, intent));
	}

	public void killArguments(PillpopperReplyContext context, Intent intent)
	{
		_killArguments(_getMetaArguments(context, intent));
	}

	public ArgumentPasser getAndKillArguments(PillpopperReplyContext context, Intent intent) throws ArgumentPasser.ArgumentPassException
	{
		ArgumentPasser retval = getArguments(context, intent);
		killArguments(context, intent);
		return retval;
	}

	//// bundle versions

	private MetaArguments _getMetaArguments(Activity act, Bundle bundle)
	{
		MetaArguments retval = new MetaArguments();

		// may get overwritten if we're putting args
		retval.setArgPasserGuid(bundle.getString(_KEY_ARG_PASSER_ID));

		retval.setActor(act.getClass().getSimpleName());
		retval.setArgsAreFor("saved instance bundle");

		return retval;
	}

	public ArgumentPasser putArguments(Activity act, Bundle bundle)
	{
		MetaArguments metaArgs = _getMetaArguments(act, bundle);
		metaArgs.setArgPasserGuid(Util.getRandomGuid());
		bundle.remove(_KEY_ARG_PASSER_ID);
		bundle.putString(_KEY_ARG_PASSER_ID, metaArgs.getArgPasserGuid());
		return _putArguments(metaArgs);
	}

	// marked private since it's typically not used - bundle arguments should be get-and-killed
	private ArgumentPasser getArguments(Activity act, Bundle bundle) throws ArgumentPasser.ArgumentPassException
	{
		return _getArguments(_getMetaArguments(act, bundle));
	}

	public void killArguments(Activity act, Bundle bundle)
	{
		_killArguments(_getMetaArguments(act, bundle));
	}

	public ArgumentPasser getAndKillArguments(Activity act, Bundle bundle) throws ArgumentPasser.ArgumentPassException
	{
		ArgumentPasser retval = getArguments(act, bundle);
		killArguments(act, bundle);
		return retval;
	}


	// FDA Drug Database

	private final FDADrugDatabase _fdaDrugDatabase;

	public FDADrugDatabase getFDADrugDatabase()
	{
		return _fdaDrugDatabase;
	}

	////////////////////////////////////////////////////////////////////////////
	//

	public void kpMaybeLaunchLoginScreen(Activity act)
	{
		if(RunTimeData.getInstance().isNotificationGenerated()){
			launchSplash(act);
		}
		ActivationController activationController = ActivationController.getInstance();

		boolean isSessionActive = activationController.isSessionActive(act);
		boolean isTimedOut = activationController.checkForTimeOut(act);
		Notification_Suppressor mNotification_Suppressor = new Notification_Suppressor();

		if (activationController.isDataResetFl(act)) {
			PillpopperLog.say("Activation library indicates user switch. Deleting all state.");
			setState(act,getEmptyState());
			activationController.clearSwitchFlags(act);
			FrontController.getInstance(act).clearDatabase();
			//clearing the hasstatusupdate response on sign out.
        	PillpopperRunTime.getInstance().setHasStatusUpdateResponseObj(null);
			RefillReminderNotificationUtil.getInstance(act).cancelAllPendingRefillReminders(act);
			
			Util.setCreateUserRequestInprogress(false);

			return;
		}

		PillpopperLog.say("Activation library: active=%s, timedOut=%s, username=%s, userid=%s, mrn=%s",
				isSessionActive, isTimedOut,
				activationController.getUserName(act),
				activationController.getUserId(act),
				activationController.getMrn(act));

		if (Util.isActiveInterruptSession() && !isSessionActive ) {
			//ActivationController.getInstance(act).stopTimer();
			//RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
			PillpopperLog.say("Activation library needs login");
			LocalBroadcastManager.getInstance(act).registerReceiver(mNotification_Suppressor, new IntentFilter(ACTION_REMINDER_NOTIFICATION_SUPPRESSOR));
			Intent i = new Intent(act, LoginActivity.class);
			
			if(null!=FrontController.getInstance(act).isQuickViewEnabled() && ("1").equalsIgnoreCase(FrontController.getInstance(act).isQuickViewEnabled())){
				if(null!=RunTimeData.getInstance().getRuntimeSSOSessionID()){
					PillpopperLog.say("user has been logged in and timeout happens");
					i.putExtra("isSessionExpiredRequire", true);
					RunTimeData.getInstance().setUserLogedInAndAppTimeout(true);
				}else{
					PillpopperLog.say("user has killed the app");
					i.putExtra("isSessionExpiredRequire", false);
					RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
				}
				
			}else{
				// Means this is first time login
				PillpopperLog.say("First time login");

				if(null!=RunTimeData.getInstance().getSaveTempUserNameForInterrupt()){
					// Means first time, timeout occured in inturrupt screen
					i.putExtra("isSessionExpiredRequire", true);
					RunTimeData.getInstance().setTimeOutOccuredDuringInturruptBackGround(true);

				}else{
					i.putExtra("isSessionExpiredRequire", false);
					RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
				}
			}
			RunTimeData.getInstance().setInturruptScreenVisible(false);

			Util.performSignout(act, this);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			act.startActivity(i);
		} else {


			if (null != activationController.getSSOSessionId(act)) {
				try {

					String setCookie = new StringBuilder(AppConstants.ConfigParams.getKeepAliveCookieName()+"="+URLEncoder.encode(activationController.getSSOSessionId(act),"utf-8"))
							.append("; domain=").append(AppConstants.ConfigParams.getKeepAliveCookieDomain())
							.append("; path=").append(AppConstants.ConfigParams.getKeepAliveCookiePath())
							.toString();
					//PillpopperServer.clearCookies();
					PillpopperServer.storeCookies(setCookie);
				} catch (UnsupportedEncodingException e) {
					PillpopperLog.say("Opps!, Exception ", e);
				}
				PillpopperLog.say("Activation library gives OK");
			}
		}
	}

	private void launchSplash(Context context) {
		Intent launchApp = new Intent(context, Splash.class);
		launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		FrontController.getInstance(context).hideLateRemindersWhenFromNotifications(context);
		context.startActivity(launchApp);
	}

	public String kpGetUserId(Context context)
	{
		ActivationController activationController = ActivationController.getInstance();

		return activationController.getUserId(context);
	}

	public void kpClearSignon(Context context)
	{
		ActivationController activationController = ActivationController.getInstance();

		activationController.clearSignonFields(context);
		activationController.performSignoff(context);
	}

	public void kpSignout(Context context)
	{
		ActivationController activationController = ActivationController.getInstance();
		activationController.performSignoff(context);
	}

	private void _kpSetLastSyncTime(Context context)
	{
		if (_state == null || context == null)
			return;

		long lastSyncTimeMsec = _state.getLastManagedSyncTimeMsec();

		if (lastSyncTimeMsec > 0) {
			PillpopperLog.say("Last sync time set to: %d", lastSyncTimeMsec);
			ActivationController activationController = ActivationController.getInstance();
			activationController.setLastMemberMedsSyncTime(context,lastSyncTimeMsec);
		}
	}


	private PillpopperAppContext(Context androidContext)
	{
		// Configure billing service
		_configureBillingService(androidContext);
		// Open the FDA drug database
		_fdaDrugDatabase = new FDADrugDatabase(androidContext);
		// Initialize hardware ID
		UniqueDeviceId.init(androidContext);
	}

	public synchronized void setState(final Context context, State newState)
	{
		_state = newState;

		// set an alarm based on the state we just read
		_state.setAlarm(context);

		// Commenting this Snippet for Hot fix release V1.0.1
		// Update managed-drug notification bar
		//NotificationBar_ManagedDrugChange.updateManagedChangeNotification(_globalAppContext);

		// KP ONLY
		_kpSetLastSyncTime(context);
		// END KP ONLY

		// when the state is updated, write the new state out and update the alarm
		_state.registerStateUpdatedListener(() -> {
			_state.syncNeeded(context,_globalAppContext);
			// Update the next-dose-due alarm.
			_state.setAlarm(context);
			// KP ONLY
			_kpSetLastSyncTime(context);
		});

	}

	private State getEmptyState()
	{
		return new State(getEdition(), getFDADrugDatabase());
	}

	public synchronized State getState(Context context)
	{
		if (_state == null) {
				setState(context,getEmptyState());
		}
		return _state;
	}


	////////////// Support for Demo/Premium /////////////////////

	// PremiumState manages its own state -- it doesn't get JSON, but instead records
	// its persistent state with SharedPreferences. (Its state should
	// be local to the device and more resistant to user frobbing.)
	PremiumState _premiumState;


	public boolean isPremium()
	{
		return _premiumState.isPremium();
	}

	private void _configureBillingService(Context context)
	{
		_premiumState = new PremiumState(context);
		_premiumState.setState(PremiumState.SubType.FREE_PREMIUM);
	}

	public void kpClearSSOSessionId(Context context)
	{
		ActivationController activationController = ActivationController.getInstance();
		activationController.clearSSOSessionId(context);
	}

	public void stopKeepAliveService(Context _thisActivity)
	{
		try {
			PillpopperApplication pillPopperApp = (PillpopperApplication) _thisActivity.getApplicationContext();
			pillPopperApp.stopKeepAliveTimer();
		}catch (Exception e){
			PillpopperLog.exception(e.getMessage());
		}
	}
	
	public void resetQuickviewShownFlg(Context context){
		ActivationController activationController = ActivationController.getInstance();
		activationController.resetQuickviewShownFlg(context);
	}

}
