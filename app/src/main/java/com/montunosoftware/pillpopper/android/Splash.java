package com.montunosoftware.pillpopper.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.refillreminder.services.RefreshRefillRemindersAsyncTask;
import com.montunosoftware.pillpopper.android.util.NotificationBar_OverdueDose;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.kotlin.HasStatusAlert;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.HasStatusUpdateResponseObj;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.service.GetAppProfileUrlsService;
import com.montunosoftware.pillpopper.service.HasStatusUpdateAsyncTask;

import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.envswitch.EnvSwitchUtils;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.RxRefillConstants;
import org.kp.tpmg.ttg.presenter.RxKpLocationPresentor;
import org.kp.tpmg.ttg.presenter.RxLocationPresentorCallback;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.model.TTGHttpResponseObject;
import org.kp.tpmg.ttgmobilelib.service.TTGResponseHandler;
import org.kp.tpmg.ttgmobilelib.service.TTGServiceDelegate;

import java.util.Map;
import java.util.TimeZone;

import static org.kp.tpmg.mykpmeds.activation.AppConstants.DELAY_LOGO;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.DELAY_SPLASH;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.FADE_IN_DURATION;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.FADE_OUT_DURATION;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.RESPONSE_COUNT;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.TIME_BETWEEN;

public class Splash extends Activity implements HasStatusUpdateAsyncTask.HasStatusUpdateResponseListener, GetAppProfileUrlsService.AppProfileWSComplete, RxLocationPresentorCallback {

	private final String TIME_STAMP_KEY = "hasStatusResponseTimeStamp";
	boolean kphcMedicationChanged = false;
	boolean proxyStatusChanged = false;
	boolean medicationScheduleChanged = false;

	private Context mContext;
	private HasStatusUpdateAlert hasStatusUpdateAlert;
	private HasStatusAlert hasStatusAlert;
	private String kphcMedChangedValue;
	private String proxyStatusCodeValue;
	private String primaryUserId;
	private SharedPreferenceManager mSharedPrefManager;
	private TTGHttpResponseObject mServerStatusRespObj;
	private int splashScreenRespCount = 0;

	private ImageView animImageView;
	private RelativeLayout splashAnimationLayout;
	private Animation fadeOutAnimation;
	private Animation fadeInAnimation;
	private boolean animationLoopCompleted;
	private int[] imagesToLoop = {R.drawable.alarm, R.drawable.pill};
	private RelativeLayout kpLogoLayout;
	private LinearLayout kpLogoLoading;
	private boolean continueWithLoginCalled;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Util.applyStatusbarColor(this, ContextCompat.getColor(this,R.color.linear_gradient_top));

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.splash_activity_new);
		mContext = this;

		if (null != getIntent() && getIntent().getBooleanExtra(AppConstants.IS_FROM_PILL_POPPER_APPLICATION, false)) {
			RunTimeData.getInstance().setShowSplashAnimation(false);
		}

		initUI();

		RunTimeData.getInstance().setShowFingerprintDialog(true);

		EnvSwitchUtils.initCurrentSelectedEnvironmentEndpoint(this);

		mSharedPrefManager = SharedPreferenceManager.getInstance(
				Splash.this, AppConstants.AUTH_CODE_PREF_NAME);

		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0 && !RunTimeData.getInstance().isNotificationGenerated()) {
			finish();
			return;
		}

		if (RunTimeData.getInstance().isNotificationGenerated()) {
			RunTimeData.getInstance().setNotificationGenerated(false);
			if (!AppConstants.isByPassLogin()) {
				sendRefreshCardsBroadcastWithDelay();
			}
		}
		Util.resetRuntimeInturruptFlags();
		Util.storeEnvironment(Splash.this);
		Util.initAPIKeyAsPerEnvironment();
		cancelAllPendingNotifications();
		unRegisterReceivers(Splash.this);

		if (ActivationUtil.isNetworkAvailable(getApplicationContext()) && Util.isAppProfileCallRequired(this)) {
			new GetAppProfileUrlsService(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			handleAppProfileComplete(); // continue in case of no network.
		}

		if ("-1".equalsIgnoreCase(mSharedPrefManager.getString("IsResetClicked", "-1"))) {
			mSharedPrefManager.putString("IsResetClicked", "1", false);
		}

		if (!RunTimeData.getInstance().isShowSplashAnimation()) {
			Intent intent = new Intent(Splash.this, LoadingActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivityForResult(intent, 0);
		}

		ActivationController.initializeLoggersInSignOnLib();
		ActivationController.initilizeCertificateKeys(Splash.this);

		new RefreshRefillRemindersAsyncTask(this).execute();
		RunTimeData.getInstance().setFromSplashScreen(true);

	}

	private void initUI() {
		kpLogoLoading = findViewById(R.id.logo_for_no_animation);
		kpLogoLayout = findViewById(R.id.kp_logo_layout);
		splashAnimationLayout = findViewById(R.id.splash_animation_layout);
		animImageView = findViewById(R.id.splash_anim_image);

		if (!RunTimeData.getInstance().isShowSplashAnimation()) {
			kpLogoLoading.setVisibility(View.VISIBLE);
			kpLogoLayout.setVisibility(View.GONE);
		}else {
			kpLogoLoading.setVisibility(View.GONE);
			kpLogoLayout.setVisibility(View.VISIBLE);
		}

		fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (RunTimeData.getInstance().isShowSplashAnimation()) {
			startSplashAnimation();
			FireBaseAnalyticsTracker.getInstance().logEvent(Splash.this,
					FireBaseConstants.Event.APP_LAUNCH,
					FireBaseConstants.ParamName.SOURCE,
					FireBaseConstants.ParamValue.FRESH_LAUNCH);
		} else {
			FireBaseAnalyticsTracker.getInstance().logEvent(Splash.this,
					FireBaseConstants.Event.APP_LAUNCH,
					FireBaseConstants.ParamName.SOURCE,
					FireBaseConstants.ParamValue.BACKGROUND_FOREGROUND);
			animationLoopCompleted = true;
		}
	}

	private void startSplashAnimation() {
		new Handler().postDelayed(() -> {
			finishActivity(0);
			kpLogoLayout.startAnimation(fadeOutAnimation);
			kpLogoLayout.setVisibility(View.GONE);
		}, DELAY_LOGO);

		new Handler().postDelayed(() -> {
			splashAnimationLayout.startAnimation(fadeInAnimation);
			splashAnimationLayout.setVisibility(View.VISIBLE);
			animate(animImageView, imagesToLoop, 0);
		}, DELAY_SPLASH);
	}

	private void animate(final ImageView imageView, final int images[], final int imageIndex) {


		imageView.setImageResource(images[imageIndex]);

		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator());
		fadeIn.setDuration(FADE_IN_DURATION);

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setStartOffset(FADE_IN_DURATION + TIME_BETWEEN);
		fadeOut.setDuration(FADE_OUT_DURATION);

		AnimationSet animation = new AnimationSet(false);
		animation.addAnimation(fadeIn);
		animation.addAnimation(fadeOut);
		animation.setRepeatCount(1);
		imageView.startAnimation(animation);

		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				//Default implementation Ignored
			}

			public void onAnimationEnd(Animation animation) {
				if (images.length - 1 > imageIndex) {
					animate(imageView, images, imageIndex + 1);
				} else {
					imageView.setVisibility(View.INVISIBLE);
					animateFinalImageFadeIn(imageView);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				//Default implementation Ignored
			}
		});
	}

	private void animateFinalImageFadeIn(final ImageView imageViewNew) {

		imageViewNew.setImageResource(R.drawable.kpm);

		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator());
		fadeIn.setDuration(FADE_IN_DURATION);
		fadeIn.setFillAfter(true);
		fadeIn.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				//Default implementation Ignored
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				imageViewNew.setVisibility(View.VISIBLE);

				if (splashScreenRespCount < RESPONSE_COUNT) {
					if(continueWithLoginCalled){
						animationLoopCompleted = true;
						continueWithLogin();
					}else {
						animateFinalImageFadeOut(imageViewNew);
					}

				} else {
					animationLoopCompleted = true;
					if(continueWithLoginCalled){
						continueWithLogin();
					} else {
						continueAppLoadingProcess();
					}
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				//Default implementation Ignored
			}
		});

		imageViewNew.setAnimation(fadeIn);

	}

	private void animateFinalImageFadeOut(final ImageView imageViewNew) {


		//imageViewNew.setImageResource(R.drawable.kpm);

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setStartOffset(FADE_IN_DURATION + TIME_BETWEEN);
		fadeOut.setDuration(FADE_OUT_DURATION);

		fadeOut.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				//Default implementation Ignored
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				imageViewNew.setVisibility(View.VISIBLE);
				animate(animImageView, imagesToLoop, 0);

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				//Default implementation Ignored
			}
		});
		imageViewNew.startAnimation(fadeOut);
	}

	private void sendRefreshCardsBroadcastWithDelay() {
		new Handler().postDelayed(() -> {
			RunTimeData.getInstance().resetRefreshCardsFlags();
			try {
				LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
				if (null != localBroadcastManager) {
					localBroadcastManager.sendBroadcast(new Intent("REFRESH_CURRENT_REMINDERS"));
				}
			} catch (Exception ex) {
				PillpopperLog.say(ex);
			}
		}, 5000);
	}

	private void cancelAllPendingNotifications() {
		NotificationBar_OverdueDose.cancelNotificationBar(Splash.this);
	}

	private void unRegisterReceivers(Context context) {
		try {
			context.getApplicationContext().unregisterReceiver(RunTimeData.getInstance().getNotificationBarOrderedBroadcastHandler());
		} catch (Exception e) {
			PillpopperLog.exception(e.getMessage());
		}
	}

	private void checkAndUpdateAsPendingReminderPresent(Context context) {
		String isPendingPastRemindersAvailable = FrontController.getInstance(context).getPendingRemindersStatus(context);
		PillpopperLog.say("NotifiactionISSUE Splash isPendingReminderValue " + isPendingPastRemindersAvailable);
		boolean launchingLateRemindersAfterCurrent = mSharedPrefManager.getBoolean("launchingLateRemindersAfterCurrent", false);
		if (("1").equalsIgnoreCase(isPendingPastRemindersAvailable) && !launchingLateRemindersAfterCurrent) {
			long lastHistoryScheduleTimeStamp = FrontController.getInstance(context).getLastHistoryScheduleTimeStamp();
			boolean updateAsNoPendingReminders = false;
			if (lastHistoryScheduleTimeStamp != -1) {
				UniqueDeviceId.getHardwareId(context); // initialization the cacheId to get Hardware Id.
				for (final Drug drug : FrontController.getInstance(mContext).getDrugListForOverDue(mContext)) {
					drug.computeDBDoseEvents(mContext, drug, PillpopperTime.now(), 60);
					if (drug.isoverDUE() && drug.getOverdueDate() != null) {
						if (drug.getOverdueDate().getGmtSeconds() > lastHistoryScheduleTimeStamp) {
							PillpopperLog.say("NotifiactionISSUE Splash inside all if " + drug.getOverdueDate().getGmtSeconds()
									+ " lastHistoryScheduleTimeStamp  is : " + lastHistoryScheduleTimeStamp);
							updateAsNoPendingReminders = true;
							break;
						}
					}
				}
			}

			if (lastHistoryScheduleTimeStamp != -1 && !updateAsNoPendingReminders) {
				PillpopperLog.say("NotifiactionISSUE Splash updateAsPendingRemindersPresent");
				FrontController.getInstance(context).updateAsPendingRemindersPresent(context);
			} else {
				PillpopperLog.say("NotifiactionISSUE Splash updateAsNoPendingReminders");
				FrontController.getInstance(context).updateAsNoPendingReminders(context);
			}
		}
	}

	private void continueWithLogin() {
		if(!RunTimeData.getInstance().isShowSplashAnimation()){
			new Handler().postDelayed(() -> startHomeContainerActivity(), 3000);
		} else if(animationLoopCompleted) {
			new Handler().post(() -> startHomeContainerActivity());
		}
	}

	private void startHomeContainerActivity() {
		finishActivity(0);
		Intent mainIntent = new Intent(Splash.this, HomeContainerActivity.class);
		startActivity(mainIntent);
		finish();
	}

	private void continueAppLoadingProcess() {

		if (splashScreenRespCount == RESPONSE_COUNT && animationLoopCompleted) {
			finishActivity(0);
			if (mServerStatusRespObj != null) {
				Map systemStatusResponseMap = TTGResponseHandler.getInstance().handleHttpUrlConnectionSystemStatusResponse(mServerStatusRespObj);
				if (systemStatusResponseMap.get("statusCode") == null
						&& !String.valueOf(TTGMobileLibConstants.TTGSatusCodeConstants.SUCCESS_STATUS_CODE_75).equalsIgnoreCase((String) systemStatusResponseMap.get("statusCode"))) {
					//continue with has status update response
					handleHasStatusUpdateResponse();
				} else if (String.valueOf(TTGMobileLibConstants.TTGSatusCodeConstants.SUCCESS_STATUS_CODE_75).equalsIgnoreCase((String) systemStatusResponseMap.get("statusCode"))) {
					//show upgrade alert
					GenericAlertDialog upgradeAlert = new GenericAlertDialog(Splash.this, null, systemStatusResponseMap.get("message").toString(), getString(R.string.upgrade_now_alert_btn_text), (dialog, which) -> {
						// take user to playstore
						dialog.dismiss();
						final String appPackageName = "org.kp.tpmg.android.mykpmeds";// production package name.
						// as Dev and QA package names will not give result in playstore.
						try {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
						} catch (android.content.ActivityNotFoundException anfe) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
						}
						finish();
					}, getString(R.string.remind_later_alert_btn_text), (dialog, which) -> {
						RunTimeData.getInstance().setNeedToAnimateLogin(false);
						dialog.dismiss();
						//continue with has status update response
						handleHasStatusUpdateResponse();
					});
					//saving the upgrade alert shown timestamp, irrespective of the actoin taken by the user
					mSharedPrefManager.putLong(AppConstants.APP_UPGRADE_ALERT_TIMESTAMP, PillpopperTime.now().getGmtSeconds(), false);
					// as the dialog buttons where being truncated and there was no padding between two, this below method is added in Generic alertDialog
					upgradeAlert.showDialogWithoutBtnPadding();
				} else if (String.valueOf(AppConstants.StatusCodeConstants.FORCE_UPGRADE_CODE_3).equalsIgnoreCase((String) systemStatusResponseMap.get("statusCode"))
						|| String.valueOf(AppConstants.StatusCodeConstants.FORCE_UPGRADE_CODE_101).equalsIgnoreCase((String) systemStatusResponseMap.get("statusCode"))) {
					if (null != systemStatusResponseMap.get("message")) {
						Util.showForceUpgradeAlert(Splash.this, systemStatusResponseMap.get("message").toString());
					} else {
						// show force upgrade alert with generic error message with OK button//
						GenericAlertDialog forceUpgradeAlert = new GenericAlertDialog(Splash.this, null, getString(R.string.alert_error_status_20), getString(R.string.ok_text), (dialog, which) -> {
							RunTimeData.getInstance().setNeedToAnimateLogin(false);
							dialog.dismiss();
							handleHasStatusUpdateResponse();
						}, null, null);

						forceUpgradeAlert.showDialogWithoutBtnPadding();
					}
				} else {
					//continue with has status update response
					handleHasStatusUpdateResponse();
				}
			} else {
				//continue with has status update response
				handleHasStatusUpdateResponse();
			}
		}
	}


	private void handleHasStatusUpdateResponse() {
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(
                this, AppConstants.AUTH_CODE_PREF_NAME);

        proxyStatusCodeValue = mSharedPrefManager.getString(AppConstants.PROXY_STATUS_CODE,"");
        kphcMedChangedValue = mSharedPrefManager.getString(AppConstants.KPHC_MEDS_STATUS_CHANGED,"");
        String medicationScheduleChangedValue = mSharedPrefManager.getString(AppConstants.MEDICATION_SCHEDULE_CHANGED,"");

		if (!mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, false) &&
				checkForStatusChange(proxyStatusCodeValue, kphcMedChangedValue, medicationScheduleChangedValue)) {
            finishActivity(0);
            PillpopperLog.say("Alerts are present, No need to refresh the missed doses events.");
            prepareStatusAlert();
        } else {
            continueToLaunch();
        }
	}

	private boolean checkUpgradeAlertShownForToday() {
		long upgradeAlertTimeStamp = mSharedPrefManager.getLong(AppConstants.APP_UPGRADE_ALERT_TIMESTAMP, -1);
		if (upgradeAlertTimeStamp == -1) {
			return false;
		} else {
			PillpopperTime upgradeAlertTime = new PillpopperTime(upgradeAlertTimeStamp);
			if (upgradeAlertTime.getLocalDay().before(PillpopperTime.now().getLocalDay())) {
				return false;
			}
		}
		return true;
	}

	private void continueToLaunch() {
		invokePharmacyDBDownLoad();

		RunTimeData.getInstance().setFCMInitialized(false);// reset
		Util.getInstance().initializeFCM(this);

		new AdjustPill_HomeScreenLauncherAsyncTask().execute();

		if (!RunTimeData.getInstance().isHasStatusCallInProgress()) {
			Intent mainIntent = new Intent(Splash.this, HomeContainerActivity.class);
			startActivity(mainIntent);
			RunTimeData.getInstance().setHasStatusCallInProgress(false);
			new Handler().postDelayed(() -> {
				finishActivity(0);
				finish();
			}, 2000);
		}

	}

	@Override
	public void onHasStatusUpdateResponseReceived(HasStatusUpdateResponseObj response) {
		mSharedPrefManager.putLong(TIME_STAMP_KEY, PillpopperTime.now().getGmtMilliseconds(), false);
		HasStatusUpdateResponseObj mHasStatusUpdateRespObj = response;
		splashScreenRespCount++;
		RunTimeData.getInstance().setHasStatusCallInProgress(false);
		continueAppLoadingProcess();
	}

	//to prepare msges and btns for alert
	private void prepareStatusAlert() {
		String message = "";
		String title="";
		boolean isForceSignIn = false;

		if ((kphcMedicationChanged && proxyStatusChanged)
				|| (proxyStatusChanged && medicationScheduleChanged)
				|| (kphcMedicationChanged && medicationScheduleChanged)) {    //Checking if the status of two or more things changed
			title = getString(R.string.my_kp_meds_update);
			message = getString(R.string.generic_message_alert);
			isForceSignIn = true;

		} else if (proxyStatusChanged) {
			title = getString(R.string.caregiver_access);
			message = getProxyAlertMsg(proxyStatusCodeValue);
			if (!("N").equalsIgnoreCase(proxyStatusCodeValue)) {
				isForceSignIn = true;
			}
		} else if (kphcMedicationChanged) {
			title = getString(R.string.medication_updates);
			message = getKphcAlertMsg(kphcMedChangedValue);
			isForceSignIn = true;
		} else if (medicationScheduleChanged) {
			title = getString(R.string.medication_updates);
			message = getString(R.string.medication_schedule_changed);
			isForceSignIn = true;
			FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.SIGN_IN_ALERTS, FireBaseConstants.ParamName.ALERT_TYPE, FireBaseConstants.ParamValue.SCHEDULE_CHANGE);
		}
		if(!mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY,false)) {
			showStatusAlert(title, message, isForceSignIn);
		}
	}

    private boolean checkForStatusChange(String proxyStatusCodeValue, String kphcMedChangedValue, String medicationSchChanged) {

        if (!Util.isEmptyString(proxyStatusCodeValue)) {
            if (!("P").equalsIgnoreCase(proxyStatusCodeValue)) {
                proxyStatusChanged = true;
            }
        }

        if (!Util.isEmptyString(kphcMedChangedValue)) {
            if (!("P").equalsIgnoreCase(kphcMedChangedValue)) {
                kphcMedicationChanged = true;
            }
        }

        if (!Util.isEmptyString(medicationSchChanged)) {
            if (!("N").equalsIgnoreCase(medicationSchChanged)) {
                medicationScheduleChanged = true;
            }
        }

        return proxyStatusChanged | kphcMedicationChanged | medicationScheduleChanged;
    }

	public String getProxyAlertMsg(String proxyStatusCode) {

		String message = "";

		switch (proxyStatusCode) {

			case PillpopperConstants.PROXY_NEW:
				message = getString(R.string.add_proxy_msg);
				FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.SIGN_IN_ALERTS, FireBaseConstants.ParamName.ALERT_TYPE, FireBaseConstants.ParamValue.PROXY_ADDED);
				break;

			case PillpopperConstants.PROXY_REMOVED:
				message = getString(R.string.drop_proxy_msg);
				FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.SIGN_IN_ALERTS, FireBaseConstants.ParamName.ALERT_TYPE, FireBaseConstants.ParamValue.PROXY_REMOVED);
				break;

			case PillpopperConstants.PROXY_ADD_REMOVED:
				message = getString(R.string.add_drop_proxy_msg);
				FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.SIGN_IN_ALERTS, FireBaseConstants.ParamName.ALERT_TYPE, FireBaseConstants.ParamValue.PROXY_ADDED_AND_REMOVED_ANOTHER_PROXY);
				break;

			default:
				message = getString(R.string.proxy_status_error);
				break;
		}

		return message;
	}

	public String getKphcAlertMsg(String kphcStatusCode) {

		String message = "";

		switch (kphcStatusCode) {

			case PillpopperConstants.KPHC_NEW:
				message = getString(R.string.kphc_med_new_remove);
				FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.SIGN_IN_ALERTS, FireBaseConstants.ParamName.ALERT_TYPE, FireBaseConstants.ParamValue.NEW_MEDICATION);
				break;
			case PillpopperConstants.KPHC_REMOVED:
				message = getString(R.string.kphc_med_new_remove);
				FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.SIGN_IN_ALERTS, FireBaseConstants.ParamName.ALERT_TYPE, FireBaseConstants.ParamValue.DISCONTINUED_MEDICATION);
				break;

			case PillpopperConstants.KPHC_UPDATED:
				message = getString(R.string.kphc_med_updated);
				FireBaseAnalyticsTracker.getInstance().logEvent(mContext, FireBaseConstants.Event.SIGN_IN_ALERTS, FireBaseConstants.ParamName.ALERT_TYPE, FireBaseConstants.ParamValue.UPDATED_MEDICATION);
				break;

			default:
				message = getString(R.string.proxy_status_error);
				break;
		}

		return message;
	}

	//to show alert based on status
	public void showStatusAlert(String title, String message, final boolean isForceSignIn) {
		RunTimeData.getInstance().setHasStatusCallInProgress(false);
		Intent intent = new Intent(this, HasStatusAlert.class);
		intent.putExtra("title", title);
		intent.putExtra("message", message);
		intent.putExtra("isForceSignIn",isForceSignIn);
		intent.putExtra("proxyStatusCodeValue", proxyStatusCodeValue);
		intent.putExtra("primaryUserId", primaryUserId);
		startActivity(intent);
	}


	@Override
	public void handleAppProfileComplete() {

		if (Util.isNetworkAvailable(this) && !AppConstants.isByPassLogin()) {

			//below condition is to check whether upgrade alert is shown already.
			// If already shown, ServerStatus API request will not be made as it will not be used in splash
			if (!checkUpgradeAlertShownForToday()
					&& !Util.isEmptyString(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_GET_SYSTEM_URL))) {
				new ServerStatusAsyncTask().execute();
			} else {
				splashScreenRespCount++;
			}

			primaryUserId = FrontController.getInstance(this).getPrimaryUserIdIgnoreEnabled();
			if (!Util.isEmptyString(primaryUserId)) {
				/**
				 * HasStatusUpdate API is called for every 15 minutes interval.
				 */
//				if (null == PillpopperRunTime.getInstance().getHasStatusUpdateResponseObj() ||
//						PillpopperTime.now().getGmtMilliseconds() - mSharedPrefManager.getLong(TIME_STAMP_KEY, 0L) > AppConstants.TIMEOUT_PERIOD) {
				if (!Util.hasPendingAlertsNeedForceSignIn(this) && Util.isHasStatusUpdateCallRequired(this)) {
					RunTimeData.getInstance().setHasStatusCallInProgress(true);
					HasStatusUpdateAsyncTask statusUpdateAsyncTask = new HasStatusUpdateAsyncTask(this, this);
					statusUpdateAsyncTask.execute();
				} else {
					splashScreenRespCount++;
				}
			} else {
				splashScreenRespCount++;
			}

			//checkAndLogMissedDoseEvents();
			// neither Server status nor hasStatusUpdate call has been placed. // not a likely scenario
			if (splashScreenRespCount == RESPONSE_COUNT) {
//				finishActivity(0);
				//continueToLaunchWithDelay();
                if(Util.isEmptyString(primaryUserId)) {
                    continueWithLoginCalled = true;
                    continueWithLogin();
                }else{
                    RunTimeData.getInstance().setHasStatusCallInProgress(false);
                    continueAppLoadingProcess();
                }
			}
		} else {
			//checkAndLogMissedDoseEvents();
			//continueToLaunchWithDelay();
			continueWithLoginCalled = true;
			continueWithLogin();
		}
	}

	@Override
	public void onDbUpdated() {
		PillpopperLog.say("PharmacyDB Download Success");
	}

	@Override
	public void onDbDownloadFail() {
		PillpopperLog.say("PharmacyDB Download Failure");
	}

	private class ServerStatusAsyncTask extends AsyncTask<Void, Void, TTGHttpResponseObject> {

		@Override
		protected TTGHttpResponseObject doInBackground(Void... params) {
			final TTGServiceDelegate ttgServiceDelegate = TTGServiceDelegate
					.getInstance();
			mServerStatusRespObj = ttgServiceDelegate.performSystemStatusCheck(Util.getAppVersion(Splash.this), AppConstants.APP_ID, null);
			return mServerStatusRespObj;
		}

		@Override
		protected void onPostExecute(TTGHttpResponseObject serverStatusRespObj) {
			super.onPostExecute(mServerStatusRespObj);
			mServerStatusRespObj = serverStatusRespObj;
			splashScreenRespCount++;
			continueAppLoadingProcess();
		}
	}

	private class AdjustPill_HomeScreenLauncherAsyncTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			JSONObject request = Util.checkForDSTAndPrepareAdjustPillLogEntryObject(Splash.this);
			if (null != request) {
				try {
					JSONObject response = PillpopperServer.getInstance(Splash.this, PillpopperAppContext.getGlobalAppContext(Splash.this)).
							makeRequestInNonSecureMode(request, Splash.this);
				} catch (PillpopperServer.ServerUnavailableException e) {
					PillpopperLog.say("--- Exception while calling Adjust Pill in non secure mode");
				}
			} else {
				/**
				 *Special Logic
				 * Updates last 48 hours events tzSec value with current device timezone, if any history event found tzSec value as null.
				 */
				FrontController.getInstance(Splash.this).updateHistoryOffsetForLast48HourEvents(Util.getTzOffsetSecs(TimeZone.getDefault()));
			}

			Util.getInstance().checkAndLogMissedDoseEvents(mContext);

			checkAndUpdateAsPendingReminderPresent(Splash.this);

			return true;
		}

		public void onPostExecute(Boolean isNeedToLaunchHomScreen) {

		}
	}

	@Override
	protected void onDestroy() {
		RxKpLocationPresentor presentor = new RxKpLocationPresentor();
		presentor.clearContextMemory();
		super.onDestroy();
	}

	private void invokePharmacyDBDownLoad() {
		//download location zip db from server
		String locationsBaseUrl = Util.getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_LOCATIONS_DB_URL);
		if (!Util.isEmptyString(locationsBaseUrl)) {
			if (!RefillRuntimeData.getInstance().isDbDownloadStarted()) {
				RefillRuntimeData.getInstance().setDbDownloadStarted(true);
				RxKpLocationPresentor presentor = new RxKpLocationPresentor();
				AppData.getInstance().initilizeCertificateKeysForPharmacyDB();
				presentor.makekpLocationApiCall(getApplicationContext(), this, Util.getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_LOCATIONS_DB_URL));
			}
		}
	}

}
