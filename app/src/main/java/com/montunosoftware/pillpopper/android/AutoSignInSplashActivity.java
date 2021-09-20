package com.montunosoftware.pillpopper.android;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.fingerprint.FingerprintOptInContainerActivity;
import com.montunosoftware.pillpopper.android.fingerprint.FingerprintUtils;
import com.montunosoftware.pillpopper.android.interrupts.InterruptsActivity;
import com.montunosoftware.pillpopper.android.util.FirebaseEventsUtil;
import com.montunosoftware.pillpopper.android.util.NotificationBar_OverdueDose;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.HasStatusUpdateResponseObj;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.service.GetAppProfileUrlsService;
import com.montunosoftware.pillpopper.service.HasStatusUpdateAsyncTask;
import com.montunosoftware.pillpopper.service.TokenService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.kpsecurity.KPSecurity;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.activity.LoginActivity;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.envswitch.EnvSwitchUtils;
import org.kp.tpmg.mykpmeds.activation.handler.ActivationHandler;
import org.kp.tpmg.mykpmeds.activation.model.ActivationError;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponse;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponseCompat;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.ErrorMessageUtil;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManagerOld;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.controller.RxRefillController;
import org.kp.tpmg.ttg.database.RxRefillDBUtil;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.kp.tpmg.ttgmobilelib.controller.TTGCallBackInterfaces;
import org.kp.tpmg.ttgmobilelib.controller.TTGSignonController;
import org.kp.tpmg.ttgmobilelib.model.TTGBaseResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGCarePathKeepAliveResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGHttpResponseObject;
import org.kp.tpmg.ttgmobilelib.model.TTGInterruptObject;
import org.kp.tpmg.ttgmobilelib.model.TTGInteruptAPIResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGKeepAliveRequestObj;
import org.kp.tpmg.ttgmobilelib.model.TTGPortalAPIResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGSignonRequestDataObj;
import org.kp.tpmg.ttgmobilelib.model.TTGUserResponse;
import org.kp.tpmg.ttgmobilelib.service.TTGResponseHandler;
import org.kp.tpmg.ttgmobilelib.service.TTGServiceDelegate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.kp.tpmg.mykpmeds.activation.AppConstants.DELAY_LOGO;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.DELAY_SPLASH;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.FADE_IN_DURATION;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.FADE_OUT_DURATION;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.RESPONSE_COUNT;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.TIME_BETWEEN;

public class AutoSignInSplashActivity extends FragmentActivity
        implements HasStatusUpdateAsyncTask.HasStatusUpdateResponseListener,
        TTGCallBackInterfaces.Signon, TTGCallBackInterfaces.KeepAlive, GetAppProfileUrlsService.AppProfileWSComplete,
        TTGCallBackInterfaces.Interrupts, TTGCallBackInterfaces.CareKeepAlive,
        TTGCallBackInterfaces.InterruptAPI, TTGCallBackInterfaces.PortalAPI,TTGCallBackInterfaces.GoogleAnalyticsCallBack {

    private static HashMap<String, String> mAutoSignInContent;
    private String packageName;

    private TTGHttpResponseObject mServerStatusRespObj;

    private static StringBuilder primaryUserIdFromMDO = new StringBuilder();

    private HasStatusUpdateResponseObj mHasStatusUpdateRespObj;

    private SharedPreferenceManager mSharedPrefManager;

    private static Handler handler;

    private int statusCode;

    private TTGUserResponse userResponse;

    private ActivationHandler activationHandler;
    private AppData sAppData;
    private Context mContext;
    private ActivationController activationController;

    private AlertDialog dialog;
    private String setUpcompleteFlg;
    private String introCompleteFl;
    private GenericAlertDialog mAltDlgSignOnError;
    private boolean isActivateMemberDeviceCallRequired;

    private int splashScreenRespCount = 0;
    private boolean isUserSwitched;

    private static int INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_USER_DEVICE_SWITCH = 1;
    private static int INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_SUCCESS_LOGIN = 2;
    private GenericAlertDialog mNetworkErrorAlertDialog;
    private String loggedInPrimaryUserId;

    private String mSignOnInterruptType;

    private TTGUserResponse mUserResponse;
    private TTGInteruptAPIResponse interruptResponse;

    public static boolean isAutoSignInInProgress = false;

    private ImageView animImageView;
    private RelativeLayout splashAnimationLayout;
    private Animation fadeOutAnimation;
    private Animation fadeInAnimation;
    private int[] imagesToLoop = {R.drawable.alarm, R.drawable.pill};
    private RelativeLayout kpLogoLayout;
    private LinearLayout kpLogoLoading;
    private boolean animationLoopCompleted;
    private boolean afterLoginSuccessCalled;
    private boolean needSplashAnimation = true;
    private boolean isBiometricPromptShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Util.applyStatusbarColor(this, ContextCompat.getColor(this,R.color.linear_gradient_top));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.splash_activity_new);

        mSharedPrefManager = SharedPreferenceManager.getInstance(AutoSignInSplashActivity.this, AppConstants.AUTH_CODE_PREF_NAME);
        clearObjects();
        primaryUserIdFromMDO = new StringBuilder();
        mAutoSignInContent = new HashMap<>();
        NotificationBar_OverdueDose.cancelNotificationBar(this);

        if (getIntent() != null) {
            mAutoSignInContent = new HashMap<>();
            packageName = getIntent().getStringExtra(AppConstants.MDO_PACKAGE_NAME);
            readCursor(packageName + ".AppSwitchContentProvider");

            FireBaseAnalyticsTracker.getInstance().logEvent(AutoSignInSplashActivity.this,
                    FireBaseConstants.Event.APP_LAUNCH,
                    FireBaseConstants.ParamName.SOURCE,
                    FireBaseConstants.ParamValue.MDO);

            if (mAutoSignInContent.size() == 0) {
                goToSplashActivity();
                return;
            }
        } else {
            goToSplashActivity();
            return;
        }

        EnvSwitchUtils.initCurrentSelectedEnvironmentEndpoint(this);
        Util.storeEnvironment(this);
        Util.resetRuntimeInturruptFlags();
        Util.initAPIKeyAsPerEnvironment();

        mNetworkErrorAlertDialog = new GenericAlertDialog(this,
                getString(R.string.data_unavailable_title),
                getString(R.string.alert_network_error_for_sign_in),
                getString(R.string.ok_text), alertListener, null, null);

        mContext = getApplicationContext();
        activationHandler = new ActivationHandler();
        sAppData = AppData.getInstance();
        activationController = ActivationController.getInstance();

        loggedInPrimaryUserId = activationController.getUserName(this);

        primaryUserIdFromMDO.append(mAutoSignInContent.get(AppConstants.URL_USER_NAME_STRING));

        if (RunTimeData.getInstance().isNotificationGenerated()) {
            RunTimeData.getInstance().setNotificationGenerated(false);
        }
        TTGRuntimeData.getInstance().setAppName(AppConstants.APPNAME+" "+ Util.getAppVersion(this));
        initUI();

        if (!AppConstants.isByPassLogin() && loggedInPrimaryUserId != null
                && !Util.isEmptyString(loggedInPrimaryUserId)
                && !Util.isEmptyString(primaryUserIdFromMDO.toString())) {
            if (!loggedInPrimaryUserId.equalsIgnoreCase(primaryUserIdFromMDO.toString())) {
                //Sign out the other logged in user
                Util.performSignoutForDeepLinking(this, PillpopperAppContext.getGlobalAppContext(this));
            } else if (loggedInPrimaryUserId.equalsIgnoreCase(primaryUserIdFromMDO.toString())
                    && activationController.isSessionActive(this)
                    && activationController.getSSOSessionId(this) != null
                    && activationController.getSSOSessionId(this).length() > 0) {
                finishActivity(0);
                if (ActivationUtil.isNetworkAvailable(this)) {
                    isAutoSignInInProgress = false;
                    Intent intent = new Intent(this, HomeContainerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    LoggerUtils.info("Deeplinking : Same User logged in. bringing Home Container Activity to front...");
                    startActivity(intent);
                    finish();
                    return;
                } else {
                    // if there is no network, check and bring home container activity to front if it is already started.
                    // else show network error alert
                    launchHome();
                }
            }
        }

        mAltDlgSignOnError = new GenericAlertDialog(AutoSignInSplashActivity.this,
                AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING,
                getString(R.string.ok_text), alertListener, null, null);

        if (ActivationUtil.isNetworkAvailable(getApplicationContext())) {
            if (Util.isAppProfileCallRequired(this)) {
                ActivationController.initializeLoggersInSignOnLib();
                ActivationController.initilizeCertificateKeys(AutoSignInSplashActivity.this);
                new GetAppProfileUrlsService(AutoSignInSplashActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                handleAppProfileComplete();
            }
        } else {
            finishActivity(0);
            showNetworkErrorAlert();
        }

        setHandler(new Handler() {

            @Override
            public void handleMessage(Message msg) {
                int status = 0;
                if (null != msg) {
                    status = msg.arg1;
                }
                if (status == 1) {
                    if (!ActivationController.getInstance().isSessionActive(AutoSignInSplashActivity.this) || AppConstants.isByPassLogin()) {
                        if(null != msg && null != msg.obj) {
                            TTGUserResponse userResponse = (TTGUserResponse) msg.obj;
                            startInitSessionTask(userResponse.getGuid(), userResponse.getSsoSession(), getPrimaryUserIdFromMDO());
                        }
                    } else {
                        //Since the user already logged into the app, we just have to launch the app.
                        //   FrontController.getInstance(mContext).updateAsNoPendingReminders(mContext);
                        PillpopperRunTime.getInstance().setFromMDO(true);
                        PillpopperLog.say("User already logged into the app.");
                        isAutoSignInInProgress = false;
                        launchHome(true);
                    }
                }
            }
        });

        startSplashAnimation();
    }

    private void initUI() {
        kpLogoLoading = findViewById(R.id.logo_for_no_animation);
        kpLogoLayout = findViewById(R.id.kp_logo_layout);
        splashAnimationLayout = findViewById(R.id.splash_animation_layout);
        animImageView = findViewById(R.id.splash_anim_image);

        if (needSplashAnimation) {
            kpLogoLoading.setVisibility(View.GONE);
            kpLogoLayout.setVisibility(View.VISIBLE);
        }else {
            kpLogoLoading.setVisibility(View.VISIBLE);
            kpLogoLayout.setVisibility(View.GONE);
        }

        fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        if(needSplashAnimation) {
            startSplashAnimation();
        } else{
            animationLoopCompleted = true;
            Intent intent = new Intent(AutoSignInSplashActivity.this, LoadingActivity.class);
            startActivityForResult(intent, 0);
        }
    }*/

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
                    animateFinalImageFadeOut(imageViewNew);
                } else {
                    animationLoopCompleted = true;
                    Intent intent = new Intent(AutoSignInSplashActivity.this, LoadingActivity.class);
                    startActivityForResult(intent, 0);
                    continueAppLoadingProcess();
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

    private String getPrimaryUserIdFromMDO(){
        return null != primaryUserIdFromMDO ? primaryUserIdFromMDO.toString() : "";
    }

    private void showNetworkErrorAlert() {
        try {
            if (mNetworkErrorAlertDialog != null && !mNetworkErrorAlertDialog.isShowing()) {
                if (!isFinishing()) {
                    mNetworkErrorAlertDialog.showDialog();
                }
            }
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
        }
    }

    private void launchHome() {
        if (RunTimeData.getInstance().isHomeContainerActivityLaunched()) {
            isAutoSignInInProgress = false;
            Intent intent = new Intent(this, HomeContainerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            LoggerUtils.info("Deeplinking : Same User logged in. bringing Home Container Activity to front...");
            startActivity(intent);
            finish();
            return;
        } else {
            showNetworkErrorAlert();
        }
    }

    private void goToSplashActivity() {
        isAutoSignInInProgress = false;
        Intent splashIntent = new Intent(AutoSignInSplashActivity.this, Splash.class);
        splashIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(splashIntent);
        finish();
    }

    public AsyncTask<String, Void, SignonResponse> startInitSessionTask(String guid, String ssoSession, String mUserIdStr) {
        return new VerifyLoginTask().execute(guid, ssoSession, mUserIdStr);
    }

    @Override
    public void handleAppProfileComplete() {
        if (Util.isAppProfileCallRequired(this)) {
            finishActivity(0);
            //show error
            mAltDlgSignOnError.setDialogTitle(AppConstants.EMPTY_STRING);
            mAltDlgSignOnError.setMessage(getString(R.string.app_profile_generic_error_msg));
            mAltDlgSignOnError.showDialog();
        } else {
            // continue after AppProfile Complete.. signOn will continue after hasStatusUpdate call
            if(!Util.isEmptyString(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_GET_SYSTEM_URL))) {
                new ServerStatusAsyncTask().execute();
            } else {
                splashScreenRespCount++;
            }

            ActivationController.initializeLoggersInSignOnLib();
            ActivationController.initilizeCertificateKeys(AutoSignInSplashActivity.this);
            String primaryUserId = FrontController.getInstance(this).getPrimaryUserIdIgnoreEnabled();

            PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
            RunTimeData.getInstance().resetRefreshCardsFlags();

            if (loggedInPrimaryUserId.equalsIgnoreCase(primaryUserIdFromMDO.toString()) && !Util.isEmptyString(primaryUserId)) {
                if (!Util.hasPendingAlertsNeedForceSignIn(this) && Util.isHasStatusUpdateCallRequired(this)) {
                    HasStatusUpdateAsyncTask statusUpdateAsyncTask = new HasStatusUpdateAsyncTask(this, this);
                    statusUpdateAsyncTask.execute();
                } else{
                    splashScreenRespCount++;
                }
            } else {
                splashScreenRespCount++;
            }

            if (splashScreenRespCount == RESPONSE_COUNT) {
                RunTimeData.getInstance().setHasStatusCallInProgress(false);
                continueAppLoadingProcess();
            }
        }
    }

    @Override
    public void sendTTGGoogleAnalyticsEvent(String category, String action, String label) {

    }

    @Override
    public void sendTTGGoogleAnalyticsEventWithBundle(String s, Bundle bundle) {

    }

    private class VerifyLoginTask extends
            AsyncTask<String, Void, SignonResponse> {

        @Override
        protected SignonResponse doInBackground(String... params) {
            return activationHandler.initSession(params[0], getPrimaryUserIdFromMDO(),
                    sAppData, mContext, params[1], params[2]);
        }

        @Override
        protected void onPostExecute(SignonResponse baseResponse) {
            super.onPostExecute(baseResponse);

            LoggerUtils.info("-- Setting the response " + baseResponse);

            RunTimeData.getInstance().setRegistrationResponse(baseResponse);

            if (null != baseResponse) {
                int loginStatus = Integer.parseInt(baseResponse
                        .getResponse().getStatusCode());
                setUpcompleteFlg = baseResponse.getResponse().getSetUpCompleteFl();
                if (!(RunTimeData.getInstance().getHomeButtonPressed() == 2 && !activationController.isSessionActive(AutoSignInSplashActivity.this))) {
                    mAltDlgSignOnError.setDialogTitle(AppConstants.EMPTY_STRING);
                    introCompleteFl = activationController.getIntroCompleteFlag(AutoSignInSplashActivity.this);
//							finishActivity(0);
                    if (loginStatus == AppConstants.DEVICE_USER_SWITCH_STATUSCODE) {
                        finishActivity(0);
                        showSwitchUser_DeviceAlert(
                                getString(R.string.acti_lib_alert_user_switch_title),
                                getString(R.string.acti_lib_alert_user_switch_msg),
                                true, true);
                        startTimer();
                    } else if (loginStatus == AppConstants.DEVICE_SWITCH_STATUSCODE) {
                        finishActivity(0);// Device
                        String versionUpgraedString;

                        versionUpgraedString = mSharedPrefManager.getString("versionUpgraded", "0");
                        if ("0".equals(versionUpgraedString)  || ("0").equalsIgnoreCase(versionUpgraedString)
                                || ("0").equalsIgnoreCase(versionUpgraedString.trim())) {
                            LoggerUtils.info("versionUpgraded This is not first time login after upgraded to 2.0 OR user might be fresh installation");
                            showSwitchUser_DeviceAlert(
                                    getString(R.string.acti_lib_alert_device_switch_title),
                                    getString(R.string.acti_lib_alert_device_switch_msg_multiuser),
                                    false, false);
                            isActivateMemberDeviceCallRequired = true;
                        } else {
                            finishActivity(0);
                            LoggerUtils.info("versionUpgraded Since this is first time we should not show the device switch alert");
                            mSharedPrefManager.putString("versionUpgraded", "0", false);
                            if (ActivationUtil.checkNetworkAvailablity(AutoSignInSplashActivity.this)) {
                                activationController.setUserLoginFlg(true);
                                Intent intent = new Intent(AutoSignInSplashActivity.this, LoadingActivity.class);
                                UpdateActivationTask updateIntroTask = new UpdateActivationTask();
                                updateIntroTask.execute(prepareUpdateActivationUrl());
                                startActivityForResult(intent, 0);
                            }
                        }
                        startTimer();
                    } else if (loginStatus == AppConstants.USER_SWITCH_STATUSCODE) { // User
                        // switched
                        finishActivity(0);
                        showSwitchUser_DeviceAlert(
                                getString(R.string.acti_lib_alert_user_switch_title),
                                getString(R.string.acti_lib_alert_user_switch_msg),
                                false, true);
                        LoggerUtils
                                .info("Updated Member Device, And storing the response");
                        startTimer();
                    } else if (loginStatus == 0) { // Login Success
                        mSharedPrefManager.putString("kpGuid", baseResponse.getResponse().getKpGUID(), false);
                        continueOnLoginSuccess();
                    } else if (loginStatus == AppConstants.INVALIDCREDENTIALS) { // Invalid credentials
                        finishActivity(0);
                        showInvalidCredentialsAlert();
                    } else if (loginStatus == AppConstants.ACCOUNT_LOCKEDOUT_CODE) {
                        showErrorAlert(getString(R.string.acti_lib_lockout_title),
                                getString(R.string.acti_lib_online_lockout_msg));
                    }  else if ( loginStatus == AppConstants.ACCOUNT_TEEN_PRIMARY) {
                        finishActivity(0);
                        ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                        ActivationError errorDetails = errorMessageHandler.getErrorDetails(loginStatus, baseResponse.getResponse().getMessage());
                        if (!TextUtils.isEmpty(baseResponse.getResponse().getTitle())) {
                            errorDetails.setTitle(baseResponse.getResponse().getTitle());
                           Util.getInstance(). showTeenAccountErrorAlert(errorDetails.getTitle(), errorDetails.getMessage(),AutoSignInSplashActivity.this);
                        } else {
                            showErrorAlert(errorDetails.getTitle(), errorDetails.getMessage());
                        }
                    }

                    else {
                        finishActivity(0);
                        ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                        ActivationError errorDetails = errorMessageHandler.getErrorDetails(loginStatus, baseResponse.getResponse().getMessage());
                        showErrorAlert(errorDetails.getTitle(), errorDetails.getMessage());
                    }

                }
            } else {
                mAltDlgSignOnError.setDialogTitle(AppConstants.EMPTY_STRING);
                finishActivity(0);
                ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                ActivationError errorDetails = errorMessageHandler.getErrorDetails(20, "");
                showErrorAlert(errorDetails.getTitle(), errorDetails.getMessage());
            }
            RunTimeData.getInstance().setClickFlg(false);
        }
    }

    private void continueOnLoginSuccess() {
        startTimer();

        boolean rememberUserId = mSharedPrefManager.getBoolean(
                AppConstants.REMEMBER_USER_ID, true);
        mSharedPrefManager.putBoolean(AppConstants.REMEMBER_USER_ID,
                rememberUserId, true);
        if (rememberUserId) {
            mSharedPrefManager.putString(AppConstants.USER_NAME, getPrimaryUserIdFromMDO(),
                    false);
        } else {
            mSharedPrefManager.putString(AppConstants.USER_NAME,
                    AppConstants.EMPTY_STRING, false);
        }
        AppConstants.setWrongLoginAttepmts(0);
        mSharedPrefManager.putString(AppConstants.APPLOCKEDOUT, "0", false);
        activationController.setUserLoginFlg(true);

        mSharedPrefManager.putBoolean("timeOut", false, true);

        // launching home on every successful login
        PillpopperRunTime.getInstance().setSelectedHomeFragment(HomeContainerActivity.NavigationHome.HOME);

        if (!startBiometricOptInFlow(INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_SUCCESS_LOGIN)) {
            continueWithTutorialOrNormalFlowAfterLoginSuccess();
        }
    }

    protected String prepareUpdateActivationUrl() {
        String deviceId = ActivationUtil.getDeviceId(mContext);

        return AppConstants.getActivateMemberStatusURL()
                + AppConstants.URL_DEVICE_ID_STRING + deviceId;
    }

    private void startTimer() {
        activationController.startTimer(AutoSignInSplashActivity.this);
    }

    /**
     * Making update member activation web service call, and handling the
     * response.
     */
    private class UpdateActivationTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... url) {
            int status = -1;

            Map<String, String> params = new HashMap<>(ActivationUtil.getBaseParams(AutoSignInSplashActivity.this));

            Map<String, String> headers = new HashMap<>();
            if (null != activationController.getSSOSessionId(AutoSignInSplashActivity.this)) {
                headers.put("ssoSessionId",
                        activationController.getSSOSessionId(AutoSignInSplashActivity.this));
            }

            headers.put("guid", null != RunTimeData.getInstance()
                    .getSigninRespObj() ? RunTimeData.getInstance()
                    .getSigninRespObj().getGuid() : null);

            String response = sAppData.getHttpResponse(url[0],
                    AppConstants.POST_METHOD_NAME, params, headers, null, mContext);

            LoggerUtils.info("Update Activation Response: " + response);

            if (null != response
                    && !response.equals(AppConstants.HTTP_DATA_ERROR)) {
                Gson gson = new Gson();
                SignonResponseCompat result = gson.fromJson(response,
                        SignonResponseCompat.class);
                if (result != null && result.getResponse() != null) {
                    String statusCode = result.getResponse().getStatusCode();
                    if (!Strings.isNullOrEmpty(statusCode)
                            && !("").equalsIgnoreCase(statusCode)
                            && !("null").equalsIgnoreCase(statusCode)) {
                        try {
                            status = Integer.parseInt(statusCode);
                        } catch (Exception e) {
                            LoggerUtils
                                    .info("ERROR: Could not parse the status code");
                        }
                    } else {
                        status = -2;
                    }
                }
            } else if (null != response
                    && response.equals(AppConstants.HTTP_DATA_ERROR)) {
                status = -3;
            }
            return status;
        }


        @Override
        protected void onPostExecute(Integer statusCode) {
            super.onPostExecute(statusCode);
            switch (statusCode) {
                case 0: // update member success
                    LoggerUtils
                            .info("Updated Member Device, And storing the response");
                    activationHandler.storeInitResponse(getPrimaryUserIdFromMDO(), sAppData,
                            mContext);

                    introCompleteFl = activationController.getIntroCompleteFlag(AutoSignInSplashActivity.this);

                    boolean rememberUserId = mSharedPrefManager.getBoolean(
                            AppConstants.REMEMBER_USER_ID, true);
                    mSharedPrefManager.putBoolean(AppConstants.REMEMBER_USER_ID,
                            rememberUserId, true);
                    if (rememberUserId) {
                        mSharedPrefManager.putString(AppConstants.USER_NAME, getPrimaryUserIdFromMDO(),
                                false);
                    } else {
                        mSharedPrefManager.putString(AppConstants.USER_NAME,
                                AppConstants.EMPTY_STRING, false);
                    }
                    activationController.setUserLoginFlg(true);
                    mSharedPrefManager.putBoolean("timeOut", false, true);

                    //boolean updateFlag = mSharedPrefManager.getBoolean("updateFlag", false);

                    if (!startBiometricOptInFlow(INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_USER_DEVICE_SWITCH)) {
                        continueWithTutorialOrNormalFlowAfterDeviceSwitch();
                    }
                    break;
                case 20:
                case -3:
                case -2:
                case -1:
                default:
                    ActivationController.getInstance()
                            .resetCookiesInfo();
                    finishActivity(0);
                    activationController.stopTimer(AutoSignInSplashActivity.this);
                    break;
            }
        }
    }

    private void continueWithTutorialOrNormalFlowAfterLoginSuccess() {
        isAutoSignInInProgress = false;
        AppConstants.setByPassLogin(false);
        Util.initializeRefillNativeFl(AutoSignInSplashActivity.this);
        RxRefillController.getInstance(AutoSignInSplashActivity.this).clearContentAndConfigAPIFileData(AutoSignInSplashActivity.this);
        afterLoginSuccessCalled = true;
        launchHome(true);
    }

    private void continueWithTutorialOrNormalFlowAfterDeviceSwitch() {
        isAutoSignInInProgress = false;
        AppConstants.setByPassLogin(false);
        Util.initializeRefillNativeFl(AutoSignInSplashActivity.this);
        RxRefillController.getInstance(AutoSignInSplashActivity.this).clearContentAndConfigAPIFileData(AutoSignInSplashActivity.this);
        launchHome(false);
    }

    private void launchHome(boolean isAfterLoginSuccess){
        if(animationLoopCompleted) {
            Intent intent = new Intent(AutoSignInSplashActivity.this, HomeContainerActivity.class);
            if (isAfterLoginSuccess && !isUserSwitched) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                isUserSwitched = false;
            }
            startActivity(intent);
            RunTimeData.getInstance().setNeedToAnimateLogin(false);
            finishActivityInHandler();
        }
    }

    private void finishActivityInHandler() {
        finishActivity(0);
        new Handler().post(() -> finish());
    }

    /**
     * Showing Alert Dialog in case of Device Switch OR User Switch
     *
     * @param title                   String
     * @param msg                     String
     * @param isDeviceAndUserSwitched boolean
     */
    private void showSwitchUser_DeviceAlert(String title, String msg,
                                            final boolean isDeviceAndUserSwitched, final boolean isUserSwitchAlert) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                AutoSignInSplashActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setIcon(R.drawable.actlib_device_user_switch_alert_icon);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(
                getString(R.string.act_lib_usethis_device_text),
                (dialog, which) -> {
                    dialog.dismiss();
                    RunTimeData.getInstance().setAlertDisplayedFlg(false);

                    // clearing the rx refill data on successful user/device switch
                    RxRefillController.getInstance(AutoSignInSplashActivity.this).clearRxRefillData(AutoSignInSplashActivity.this);
                    RxRefillDBUtil.getInstance(AutoSignInSplashActivity.this).resetPreferredPharmacyValue();

                    if (isDeviceAndUserSwitched) {
                        isActivateMemberDeviceCallRequired = true;
                        showSwitchUser_DeviceAlert(
                                getString(R.string.acti_lib_alert_device_switch_title),
                                getString(R.string.acti_lib_alert_device_switch_msg_multiuser),
                                false, isUserSwitchAlert);
                    } else {
                        if (isUserSwitchAlert) {
                            mSharedPrefManager.putBoolean("showTeenCard", false, false);
                            mSharedPrefManager.remove("CardIdSet");
                            activationController.clearWelcomeScreenDisplayCounter(AutoSignInSplashActivity.this);
                            FingerprintUtils.resetAndPurgeKeyStore(AutoSignInSplashActivity.this);
                        }
                        //activateMemberDevice API call will be made only if device switch is made,
                        //else user will be taken to home screen or tutorial
                        if (isActivateMemberDeviceCallRequired) {
                            if (ActivationUtil
                                    .checkNetworkAvailablity(AutoSignInSplashActivity.this)) {
                                activationController.setUserLoginFlg(true);
                                Intent intent = new Intent(AutoSignInSplashActivity.this,
                                        LoadingActivity.class);
                                UpdateActivationTask updateIntroTask = new UpdateActivationTask();
                                updateIntroTask
                                        .execute(prepareUpdateActivationUrl());
                                startActivityForResult(intent, 0);
                            }
                            isActivateMemberDeviceCallRequired = false;
                        } else {
                            activationHandler.storeInitResponse(primaryUserIdFromMDO.toString(), sAppData,
                                    mContext);
                            isUserSwitched = true;
                            continueOnLoginSuccess();
                        }
                    }
                });
        alertDialog.setNegativeButton(getString(R.string.cancel_text),
                (dialog, which) -> {
                    RunTimeData.getInstance().setRegistrationResponse(null);
                    if (null != RunTimeData.getInstance()
                            .getRuntimeSSOSessionID()) {
                        RunTimeData.getInstance().setRuntimeSSOSessionID(
                                null);
                    }
                    dialog.dismiss();
                    activationController.stopTimer(AutoSignInSplashActivity.this);
                    navigateToLogin();
                });
        RunTimeData.getInstance().setAlertDisplayedFlg(true);
        dialog = alertDialog.create();
        if (!isFinishing()) {
            dialog.show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, AutoSignInSplashActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onHasStatusUpdateResponseReceived(HasStatusUpdateResponseObj response) {
        mHasStatusUpdateRespObj = response;
        splashScreenRespCount++;
        continueAppLoadingProcess();
    }

    private void performSignOnAction() {

        RunTimeData.getInstance().setInturruptScreenVisible(false);
        RunTimeData.getInstance().setInterruptScreenBackButtonClicked(false);
        RefillRuntimeData.getInstance().clearAllPrescriptionByRx();
        RefillRuntimeData.getInstance().clearRuntimePrescriptionData();
        RunTimeData.getInstance().setRegionContactAPICallRequired(true);
        RunTimeData.getInstance().setLastSelectedFragmentPosition(-1);
        RefillRuntimeData.getInstance().setIsAPICalledForCurrentSession(new HashMap<>()); // reset
        // resetting the below flag, if it was set to true in HomeContainerActivity's launchQuickView
        AppConstants.setByPassLogin(false);

        Util.getInstance().resetHomeScreenCardsFlags();

        mSharedPrefManager.putBoolean(AppConstants.SIGNED_STATE_REMOVAL, false, false);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false, false);
        mSharedPrefManager.putString(AppConstants.TIME_STAMP, "0", false);

        mSharedPrefManager.putBoolean("timeOut", false, true);

        AppConstants.setWelcomeScreensDisplayResult("-1"); // reset after signout

        RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(false);
        RunTimeData.getInstance().setInitialGetStateCompleted(false);
        RunTimeData.getInstance().setInturruptScreenVisible(false);

        // clear stored Rx Refill FDB Image Data and reset runtime fields
        Util.clearRxRefillRelatedData(this);

        // reset the flags based based on whether late reminder was dismissed in signed in/signed out state
        boolean signedOutStateRemoval = mSharedPrefManager.getBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false);
        boolean signedStateRemoval = mSharedPrefManager.getBoolean(AppConstants.SIGNED_STATE_REMOVAL, false);
        boolean signedOutStateRemovalAndSignInOnce = mSharedPrefManager.getBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL_LOGIN_ONCE, false);

        if (signedOutStateRemoval) {
            mSharedPrefManager.putBoolean(AppConstants.SIGNED_STATE_REMOVAL, false, false);
            if (signedOutStateRemovalAndSignInOnce) {
                resetAllLateReminderFlags();
            }
        }
        if (signedStateRemoval) {
            resetAllLateReminderFlags();
        }

        if (ActivationUtil
                .isNetworkAvailable(AutoSignInSplashActivity.this)) {
            continueOnlineLogin();
        } else {
            finishActivity(0);
            showNetworkErrorAlert();
        }
    }

    private void resetAllLateReminderFlags() {
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_STATE_REMOVAL, false, false);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false, false);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL_LOGIN_ONCE, false, false);
        mSharedPrefManager.putString(AppConstants.TIME_STAMP, "0", false);
    }

    public void continueOnlineLogin() {

        Util.getInstance().initializeFCM(this);

        isAutoSignInInProgress = true;
        Util.resetRuntimeInturruptFlags();
        final TTGSignonController signonController = TTGSignonController
                .getInstance();
        new Thread(() -> {
            TTGSignonRequestDataObj signOnRequestDataobj = new TTGSignonRequestDataObj();
            signOnRequestDataobj.setUsername(new String(
                    org.apache.commons.codec.binary.Base64.encodeBase64(primaryUserIdFromMDO.toString().getBytes())));
            signOnRequestDataobj.setAppVersion(Util.getAppVersion(AutoSignInSplashActivity.this));
            signOnRequestDataobj.setMemberRegion(Util.getRegionsFromAppProfileData());
            signOnRequestDataobj.setApiKey(AppConstants.getAPIKEY());
            signOnRequestDataobj.setAppId(AppConstants.APP_ID);
            signOnRequestDataobj.setAppName(AppConstants.APPNAME+" "+ Util.getAppVersion(AutoSignInSplashActivity.this));
            signOnRequestDataobj
                    .setUserAgentCategory(AppConstants.USER_AGENT_CATEGORY);
            signOnRequestDataobj.setAuthorization(
                    "Basic " + new String(
                            org.apache.commons.codec.binary.Base64.encodeBase64(
                                    (primaryUserIdFromMDO.toString() + ":" + mAutoSignInContent.get(AppConstants.URL_PASSWORD_STRING)).getBytes())));

            signonController.performSignon(signOnRequestDataobj,
                    AutoSignInSplashActivity.this, AutoSignInSplashActivity.this, AutoSignInSplashActivity.this);
            LocalBroadcastManager
                    .getInstance(this)
                    .sendBroadcast(
                            new Intent(
                                    "com.montunosoftware.pillpopper.SUPPRESS_PENDING_NOTIFICATIONS"));
        }).start();
    }

    private class ServerStatusAsyncTask extends AsyncTask<Void, Void, TTGHttpResponseObject> {

        @Override
        protected TTGHttpResponseObject doInBackground(Void... params) {
            final TTGServiceDelegate ttgServiceDelegate = TTGServiceDelegate
                    .getInstance();
            mServerStatusRespObj = ttgServiceDelegate.performSystemStatusCheck(ActivationUtil.getAppVersion(AutoSignInSplashActivity.this), AppConstants.APP_ID, null);
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

    private void readCursor(String pAuthority) {
        Cursor c = null;
        try {
            c = getContentResolver().query(Uri.parse("content://" + pAuthority + "/query"), null, null, null, null);
            if (c != null && c.moveToFirst()) {

                while (!c.isAfterLast()) {
                    String key = c.getString(0);
                    String value = c.getString(1);

                    if(null != mAutoSignInContent.get(AppConstants.APP_VERSION)) {
                        LoggerUtils.info("Result key" + key);
                        LoggerUtils.info("Result value" + mSharedPrefManager.getDecryptedString(value));
                        mAutoSignInContent.put(key, mSharedPrefManager.getDecryptedString(value));
                    } else {
                        SharedPreferenceManagerOld managerOld = SharedPreferenceManagerOld.getInstance(AutoSignInSplashActivity.this, AppConstants.AUTH_CODE_PREF_OLD);
                        mAutoSignInContent.put(key, managerOld.getDecryptedString(value));
                    }
                    c.moveToNext();
                }
            }
        } catch (Exception e) {
            LoggerUtils.info("Query Result Cannot access provider");
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void setHandler(Handler handler2) {
        handler = handler2;
    }

    @Override
    public void onKeepAliveSuccess(String ssoSession) {
        LoggerUtils
                .info(" Session Alive update to vordel success.\nNew Sessionid="
                        + ssoSession);
        FirebaseEventsUtil.invokeKeepAliveSuccessFirebaseEvent(AutoSignInSplashActivity.this);
        mSharedPrefManager.putString(AppConstants.SSO_SESSION_ID, ssoSession,
                true);
        ActivationUtil.createObssoCookie(ssoSession, AutoSignInSplashActivity.this);
        userResponse.setSsoSession(ssoSession);
        // Added for STAY_IN_TOUCH login in issue
        RunTimeData.getInstance().setRuntimeSSOSessionID(ssoSession);
        Message message = new Message();
        message.arg1 = 1;
        message.obj = userResponse;
        handler.sendMessage(message);

    }

    @Override
    public void onKeepAliveError(int i) {
        LoggerUtils.info("   onKeepAliveError   ");
        FirebaseEventsUtil.invokeKeepAliveFailureFirebaseEvent(AutoSignInSplashActivity.this);
        Message message = new Message();
        message.arg1 = 1;
        message.obj = userResponse;
        handler.sendMessage(message);

        // in case of keep alive error, get the SSOSession id from Sign on Object and Save the ObSSOCookie
        ActivationUtil.createObssoCookie(RunTimeData.getInstance().getSigninRespObj().getSsoSession(), this);

        // Added for STAY_IN_TOUCH login in issue
        RunTimeData.getInstance().setRuntimeSSOSessionID(userResponse.getSsoSession());
    }


    @Override
    public void onSignOnSuccess(TTGUserResponse ttgUserResponse) {
        userResponse = ttgUserResponse;

        Util.getInstance().resetHomeScreenCardsFlags();
        if (null != ttgUserResponse) {
            ActivationController.getInstance().setSSOSessionId(this, ttgUserResponse.getSsoSession());
            ActivationController.getInstance().saveUserRegion(this, ttgUserResponse.getRegion());
            ActivationController.getInstance().saveUserEmail(this, ttgUserResponse.getEmail());
            RunTimeData.getInstance().setRuntimeSSOSessionID(ttgUserResponse.getSsoSession());
            RunTimeData.getInstance().setServiceArea(ttgUserResponse.getServiceArea());
            AppData.getInstance().clearAccessAndRefreshTokens(this);
            RunTimeData.getInstance().setDrugsNLPRemindersList(new HashMap<>());//reset
            TokenService.startGetAccessTokenService(this);
            if (null != ttgUserResponse.getEbizAccountRoles()
                    && ("UNM").equalsIgnoreCase(ttgUserResponse.getEbizAccountRoles())) {
                handler.post(() -> {
                    finishActivity(0);
                    ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                    ActivationError errorDetails = errorMessageHandler
                            .getErrorDetails(11);
                    String message = errorDetails.getMessage();
                    FirebaseEventsUtil.invokeSignOnFailureFirebaseEvent(AutoSignInSplashActivity.this, 11, message);
                    Util.showNMAAlert(AutoSignInSplashActivity.this, message);
                });
            } else if(null != ttgUserResponse.getEbizAccountRoles()
                    && ("CAFH").equalsIgnoreCase(ttgUserResponse.getEbizAccountRoles())) {
                handler.post(() -> {
                    finishActivity(0);
                   showCAFHAlert();
                });
            }
            if(!"CAFH".equalsIgnoreCase(ttgUserResponse.getEbizAccountRoles()) && !"UNM".equalsIgnoreCase(ttgUserResponse.getEbizAccountRoles())) {
                continueWithKeepAliveWSCall(userResponse,
                        AppConstants.ConfigParams.getKeepAliveCookieName(),
                        AppConstants.ConfigParams.getKeepAliveCookieDomain(),
                        AppConstants.ConfigParams.getKeepAliveCookiePath());
            }
        }else {
            Util.showGenericStatusAlert(AutoSignInSplashActivity.this, getString(R.string.alert_error_status_20));
        }
    }

    @Override
    public void onSignOnError(TTGBaseResponse baseResponse) {
        statusCode = baseResponse.getStatusCode();
        final String statusMsg = baseResponse.getStatusMsg();

        handler.post(() -> {
            finishActivity(0);
            LoggerUtils.info("Statuscode : " + statusCode + "\n\n"
                    + "Message : " + statusMsg);

            if(statusMsg!=null){
                FirebaseEventsUtil.invokeSignOnFailureFirebaseEvent(AutoSignInSplashActivity.this, statusCode, statusMsg);
            }

            if (statusCode == 5) {
                LoggerUtils.info("Vordel authorization failed");
                showInvalidCredentialsAlert();
            } else if (statusCode == 9) {
                LoggerUtils.info("Terminated Member error");
                finishActivity(0);
                showTerminatedMemberErrorAlert(getString(R.string._error), getString(R.string.alert_error_status_9));
            } else if (statusCode == 12) {
                LoggerUtils.error("Business error : Pending OTP");
                showPendingOTPAlert(getString(R.string.alert_eror_status_12));
            } else if (statusCode == AppConstants.StatusCodeConstants.FORCE_UPGRADE_CODE_3
                    || statusCode == AppConstants.StatusCodeConstants.FORCE_UPGRADE_CODE_101) {
                Util.showForceUpgradeAlert(AutoSignInSplashActivity.this, statusMsg);
            } else if (statusCode == AppConstants.StatusCodeConstants.APP_MAINTENANCE_STATUS_CODE
                    || statusCode == AppConstants.StatusCodeConstants.SECURITY_BREACH_STATUS_CODE) {
                if (null != statusMsg && !statusMsg.isEmpty()) {
                    Util.showGenericStatusAlert(AutoSignInSplashActivity.this, statusMsg);
                } else {
                    Util.showGenericStatusAlert(AutoSignInSplashActivity.this, getString(R.string.alert_error_status_20));
                }
            } else {
                String message = "";
                String title = "";
                if (statusCode == 6) {
                    message = getString(R.string.acti_lib_online_lockout_msg);
                    title = getString(R.string.acti_lib_lockout_title);

                } else {
                    ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                    ActivationError errorDetails = errorMessageHandler
                            .getErrorDetails(statusCode, statusMsg);
                    message = errorDetails.getMessage();
                    title = errorDetails.getTitle();
                }
                if(!title.equalsIgnoreCase("App Unavailable")){
                    showErrorAlert(title, message);
                } else{
                    showAppUnavailableErrorDialog(title, message);
                }
            }
        });
    }

    private void showAppUnavailableErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.ok_text),alertListener);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        RunTimeData.getInstance().setAlertDisplayedFlg(true);
    }

    private void showPendingOTPAlert(String message) {
        GenericAlertDialog mAlertDialog = new GenericAlertDialog(AutoSignInSplashActivity.this, "", message,
                null, null, getString(R.string.activate_account),
                (dialog, which) -> {
                    dialog.dismiss();
                    Intent openBrowser = new Intent(Intent.ACTION_VIEW);
                    openBrowser.setData(Uri.parse(Util.getActivationUrl(AutoSignInSplashActivity.this)));
                    startActivity(openBrowser);
                });
        mAlertDialog.showDialogWithoutBtnPadding();
    }

    private void showTerminatedMemberErrorAlert(String title, String message) {
        GenericAlertDialog mAltDlgSignOnError = new GenericAlertDialog(AutoSignInSplashActivity.this, title, message, getString(R.string.ok_text), (dialog, which) -> dialog.dismiss(), null, null);

        mAltDlgSignOnError.showDialogWithoutBtnPadding();
    }

    public void showInvalidCredentialsAlert() {
        GenericAlertDialog mAltDlgSignOnError = new GenericAlertDialog(AutoSignInSplashActivity.this,
                AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING,
                getString(R.string.ok_text), alertListener, null, null);
        mAltDlgSignOnError.setDialogTitle(null);
        mAltDlgSignOnError.setMessage(getString(R.string.alert_error_status_5));
        if (null != mAltDlgSignOnError && !mAltDlgSignOnError.isShowing()
                && !isFinishing()) {
            mAltDlgSignOnError.showDialog();
        }
    }

    private void showErrorAlert(String title, String msg) {
        GenericAlertDialog alertDialog = new GenericAlertDialog(
                AutoSignInSplashActivity.this, title, msg, getString(R.string.ok_text),
                alertListener, null, null);
        if (null != alertDialog && !alertDialog.isShowing() && !isFinishing()) {
            alertDialog.showDialog();
        }
        RunTimeData.getInstance().setAlertDisplayedFlg(true);
    }

    private final android.content.DialogInterface.OnClickListener alertListener = (dialog, which) -> {
        RunTimeData.getInstance().setClickFlg(false);
        dialog.dismiss();
        RunTimeData.getInstance().setAlertDisplayedFlg(false);
        RunTimeData.getInstance().setRuntimeSSOSessionID(null);
        RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
        finish();
    };

    private void continueAppLoadingProcess() {

        if (splashScreenRespCount == RESPONSE_COUNT && animationLoopCompleted) {
            if (mServerStatusRespObj != null) {
                Map systemStatusResponseMap = TTGResponseHandler.getInstance().handleHttpUrlConnectionSystemStatusResponse(mServerStatusRespObj);
                if (systemStatusResponseMap.get("statusCode") == null
                        && !String.valueOf(TTGMobileLibConstants.TTGSatusCodeConstants.SUCCESS_STATUS_CODE_75).equalsIgnoreCase((String) systemStatusResponseMap.get("statusCode"))) {
                    //continue with has status update response
                    handleHasStatusUpdateResponse();
                } else if (String.valueOf(TTGMobileLibConstants.TTGSatusCodeConstants.SUCCESS_STATUS_CODE_75).equalsIgnoreCase((String) systemStatusResponseMap.get("statusCode"))) {
                    finishActivity(0);
                    //show upgrade alert
                    GenericAlertDialog upgradeAlert = new GenericAlertDialog(mContext, null, systemStatusResponseMap.get("message").toString(), getString(R.string.upgrade_now_alert_btn_text), (dialog, which) -> {
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
                        dialog.dismiss();
                        startActivityForResult(new Intent(AutoSignInSplashActivity.this, LoadingActivity.class), 0);
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
                        finishActivity(0);
                        Util.showForceUpgradeAlert(mContext, systemStatusResponseMap.get("message").toString());
                    } else {
                        // show force upgrade alert with generic error message with OK button//
                        GenericAlertDialog forceUpgradeAlert = new GenericAlertDialog(mContext, null, getString(R.string.alert_error_status_20), getString(R.string.ok_text), (dialog, which) -> {
                            dialog.dismiss();
                            startActivityForResult(new Intent(AutoSignInSplashActivity.this, LoadingActivity.class), 0);
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
        if (mHasStatusUpdateRespObj != null) {
            PillpopperRunTime.getInstance().setHasStatusUpdateResponseObj(mHasStatusUpdateRespObj);
        }
        //Vordel API Requests
        performSignOnAction();
    }

    private boolean startBiometricOptInFlow(final int requestCode) {

        if (KPSecurity.isBiometricPromptReadyToUse(AutoSignInSplashActivity.this)) {
            FingerprintUtils.encryptAndStorePassword(AutoSignInSplashActivity.this, mAutoSignInContent.get(AppConstants.URL_PASSWORD_STRING));
        }

        boolean isFingerprintDecisionTaken =
                mSharedPrefManager.getBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_DECISION_TAKEN, false);

        if (!isFingerprintDecisionTaken
                && FingerprintUtils.isDeviceEligibleForFingerprintOptIn(this)) {
            handler.post(() -> {
                finishActivity(0);
                RunTimeData.getInstance().setmFingerPrintTCInProgress(true);
                FingerprintOptInContainerActivity.startOptInFlow(AutoSignInSplashActivity.this, requestCode);
            });

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_USER_DEVICE_SWITCH || requestCode == INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_SUCCESS_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                startBiometricEnroll(requestCode);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                continueWithHomeContainerActivity(requestCode);
            }
        }
    }

    private void startBiometricEnroll(int requestCode) {

        if (KPSecurity.isBiometricPromptReadyToUse(AutoSignInSplashActivity.this)) {

            KPSecurity.initBiometric(true, AutoSignInSplashActivity.this, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    PillpopperLog.say("--Biometric-- authentication error");
                    FingerprintUtils.setFingerprintSignInForUser(AutoSignInSplashActivity.this, false);
                    if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                        FingerprintUtils.showGlobalThresholdReachedMessage(AutoSignInSplashActivity.this, (dialog, which) -> continueWithHomeContainerActivity(requestCode));
                    }else {
                        continueWithHomeContainerActivity(requestCode);
                    }
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    KPSecurity.biometricEncryptPassword("", mAutoSignInContent.get(AppConstants.URL_PASSWORD_STRING), AutoSignInSplashActivity.this, result);
                    FingerprintUtils.setFingerprintSignInForUser(AutoSignInSplashActivity.this, true);
                    continueWithHomeContainerActivity(requestCode);
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    PillpopperLog.say("--Biometric-- authentication failed");
                }
            });
            try {
                KPSecurity.biometricAuthenticate(SharedPreferenceManager.getInstance(
                        this, AppConstants.AUTH_CODE_PREF_NAME).getString(AppConstants.USER_NAME, null));
            } catch (Exception ex) {
                PillpopperLog.say("Biometric Exception " + ex.getMessage());
                continueWithTutorialOrNormalFlowAfterLoginSuccess();
            }
        }
    }

    private void continueWithHomeContainerActivity(int requestCode) {
        if (requestCode == INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_USER_DEVICE_SWITCH) {
            continueWithTutorialOrNormalFlowAfterDeviceSwitch();
        } else {
            continueWithTutorialOrNormalFlowAfterLoginSuccess();
        }
    }

    public static void clearObjects() {
        if (primaryUserIdFromMDO != null) {
            primaryUserIdFromMDO.delete(0, primaryUserIdFromMDO.length());
            primaryUserIdFromMDO = null;
        }
        if (mAutoSignInContent != null) {
            mAutoSignInContent.clear();
            mAutoSignInContent = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAutoSignInInProgress = false;
//        clearObjects();
    }

    @Override
    public void onNonIgnorableInterruptRecieved(TTGInterruptObject interrupt) {
        LoggerUtils.info("Result - Non Ignorable interrupts recieved");
        mSignOnInterruptType = interrupt.getInterruptType();
        RunTimeData.getInstance().setInterruptType(mSignOnInterruptType);
        mUserResponse = interrupt.getUserResponse();
        RunTimeData.getInstance().setUserResponse(mUserResponse);
        if (null != Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_SIGNON_HARD_INTERRUPT_URL_KEY)) {
            LoggerUtils.info("Result - Non Ignorable interrupts received");

            if (null != mUserResponse) {
                if (!TextUtils.isEmpty(mUserResponse.getRegion())) {
                    ActivationController.getInstance().saveUserRegion(this, mUserResponse.getRegion());
                }
                if (!TextUtils.isEmpty(mUserResponse.getAge())) {
                    ActivationController.getInstance().saveUserAge(this, mUserResponse.getAge());
                }
                if (!TextUtils.isEmpty(mUserResponse.getEmail())) {
                    ActivationController.getInstance().saveUserEmail(this, mUserResponse.getEmail());
                }
                if (!TextUtils.isEmpty(mUserResponse.getSsoSession())) {
                    ActivationController.getInstance().setSSOSessionId(this, mUserResponse.getSsoSession());
                }
            }

            if (null != mSignOnInterruptType && (mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_365)
                    || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_MUST_ACCEPT_NEW_VERSION)
                    || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_NOT_ACCEPTED))) {
                mSharedPrefManager.putBoolean(AppConstants.TC_BANNER_TO_BE_SHOWN, true, false);
                TTGSignonController.getInstance().performInterruptAPICall(TTGMobileLibConstants.HTTP_METHOD_PUT,
                        interruptResponse, AutoSignInSplashActivity.this, prepareJSONForTCPut());
            } else {
                Intent intent = new Intent(AutoSignInSplashActivity.this, InterruptsActivity.class);
                intent.putExtra("mSignOnInterruptType", mSignOnInterruptType);
                startActivity(intent);
            }
        } else {
            finishActivity(0);
            handler.post(() -> showErrorAlert("", getResources().getString(R.string.app_profile_generic_error_msg)));
        }
        //GA Events not added
    }

    @Override
    public void onNonIgnorableInterruptFailed() {
        LoggerUtils.info("Result - non ignorable interrupt failed");
        handleInterruptFailureScenario();
    }

    @Override
    public void onPortalAPISuccess(TTGPortalAPIResponse portalAPIResponse) {
        LoggerUtils.info("Result - Portal API call success");
        finishActivity(0); //For time being.

        try {
            TTGSignonController.getInstance().performCareKeepAliveAPICall(portalAPIResponse, AutoSignInSplashActivity.this);
        } catch (IOException e) {
            LoggerUtils.info(e.getMessage());
        }
    }

    @Override
    public void onPortalAPIFailure() {
        LoggerUtils.info("Result - Portal API call failed");
        handleInterruptFailureScenario();
    }

    @Override
    public void onCareKeepAliveSuccess(TTGCarePathKeepAliveResponse carePathResponse) {
        LoggerUtils.info("Result - Care Path keep alive success");
        TTGSignonController.getInstance().performInterruptAPICall(TTGMobileLibConstants.HTTP_METHOD_GET, carePathResponse, AutoSignInSplashActivity.this, null);
    }

    @Override
    public void onCareKeepAliveFailure() {
        LoggerUtils.info("Result - Care Path keep alive failed");
        handleInterruptFailureScenario();
    }

    @Override
    public void onInterruptGETAPISuccess(TTGInteruptAPIResponse interuptAPIResponse) {
        interruptResponse = interuptAPIResponse;
        LoggerUtils.info("Result - Interrupt API call success");
        RunTimeData.getInstance().setInterruptGetAPIResponse(interuptAPIResponse);
        FireBaseAnalyticsTracker.getInstance().logEvent(AutoSignInSplashActivity.this,
                FireBaseConstants.Event.EVENT_SSO_INTERRUPTS_GET_SUCCESS,
                FireBaseConstants.ParamName.PARAMETER_API_SUCCESS,
                FireBaseConstants.ParamValue.PARAMETER_VALUE_SUCCESS_WITH_STATUS0);
        if (null != mSignOnInterruptType && (mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_365)
                || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_MUST_ACCEPT_NEW_VERSION)
                || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_NOT_ACCEPTED))) {
            mSharedPrefManager.putBoolean(AppConstants.TC_BANNER_TO_BE_SHOWN, true, false);
            TTGSignonController.getInstance().performInterruptAPICall(TTGMobileLibConstants.HTTP_METHOD_PUT,
                    interuptAPIResponse, AutoSignInSplashActivity.this, prepareJSONForTCPut());
        } else {
//            avoidShowingFPAlertDuringStartSessioncall = false;
//            mRetainedUserName = loggedInPrimaryUserId;
            Intent intent = new Intent(AutoSignInSplashActivity.this, InterruptsActivity.class);
            intent.putExtra("mSignOnInterruptType", mSignOnInterruptType);
            startActivity(intent);
        }
    }

    @Override
    public void onInterruptAPIFailure() {
        LoggerUtils.info("Result - Interrupt API call failed");
        FirebaseEventsUtil.invokeInterruptFailureAPIEvent(AutoSignInSplashActivity.this);
        handleInterruptFailureScenario();
    }

    @Override
    public void onInterruptPUTAPISuccess(TTGInteruptAPIResponse interuptAPIResponse) {
        LoggerUtils.info("Result - Interrupt PUT API call success");
        if (null != interuptAPIResponse && null!=interuptAPIResponse.getInterruptType() &&
                (TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH.equalsIgnoreCase(interuptAPIResponse.getInterruptType()) ||
                        TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS.equalsIgnoreCase(interuptAPIResponse.getInterruptType()) ||
                        TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD.equalsIgnoreCase(interuptAPIResponse.getInterruptType()))) {
//            avoidShowingFPAlertDuringStartSessioncall = false;
//            mRetainedUserName = primaryUserIdFromMDO.toString();
            Intent intent = new Intent(AutoSignInSplashActivity.this, InterruptsActivity.class);
            intent.putExtra("mSignOnInterruptType", interuptAPIResponse.getInterruptType());
            startActivity(intent);
        } else {
            if (null != mSignOnInterruptType && (mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_365)
                    || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_MUST_ACCEPT_NEW_VERSION)
                    || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_NOT_ACCEPTED))) {
                String keepAliveCookieName = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_NAME_KEY);
                String keepAliveCookieDomain = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY);
                String keepAliveCookiePath = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVE_COOKIE_PATH_KEY);
                if (null != interuptAPIResponse &&  null != interuptAPIResponse.getSsosession()) {
                    mUserResponse.setSsoSession(interuptAPIResponse.getSsosession());
                }
                continueWithKeepAliveWSCall(mUserResponse, keepAliveCookieName, keepAliveCookieDomain, keepAliveCookiePath);
            }
        }
    }

    private void continueWithKeepAliveWSCall(TTGUserResponse userResponse, String keepAliveCookieName, String keepAliveCookieDomain, String keepAliveCookiePath) {
        this.userResponse = userResponse;
        RunTimeData.getInstance().setSigninRespObj(this.userResponse);
        ActivationUtil.createObssoCookie(this.userResponse.getSsoSession(),
                this);
        TTGKeepAliveRequestObj keepAliveRequestObj = new TTGKeepAliveRequestObj(
                keepAliveCookieName,
                keepAliveCookieDomain,
                this.userResponse.getSsoSession(),
                keepAliveCookiePath);

        try {
            TTGSignonController.getInstance()
                    .performKeepAlive(keepAliveRequestObj,
                            AutoSignInSplashActivity.this);
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        }
    }

    private void handleInterruptFailureScenario() {
        finishActivity(0);
        handler.post(() -> {
            // show some error alert.. confirmation required
        });
    }

    private String prepareJSONForTCPut() {
        JSONObject headerObject = new JSONObject();
        JSONArray paramArray = new JSONArray();
        JSONObject params = new JSONObject();
        try {
            params.put("fname", "tnc");
            params.put("tnc", "1.8");
            paramArray.put(params);
            headerObject.put("requestMetaData", paramArray);
        } catch (JSONException e) {
            LoggerUtils.exception(e.getMessage());
        }
        return headerObject.toString();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (RunTimeData.getInstance().isFromInterruptScreen() && null != mSignOnInterruptType && null != RunTimeData.getInstance().getmTTtgInteruptAPIResponse()) {
            RunTimeData.getInstance().setFromInterruptScreen(false);
            TTGInteruptAPIResponse response = RunTimeData.getInstance().getmTTtgInteruptAPIResponse();
            String keepAliveCookieName = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_NAME_KEY);
            String keepAliveCookieDomain = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY);
            String keepAliveCookiePath = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVE_COOKIE_PATH_KEY);
            if (null != response.getSsosession()) {
                mUserResponse.setSsoSession(response.getSsosession());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String userId = mSharedPrefManager.getString(AppConstants.USER_NAME, primaryUserIdFromMDO.toString());
                String passWord = FingerprintUtils.getDecryptedPassword(AutoSignInSplashActivity.this, userId);

                //TODO - password for fingerprint
                /*if (!(mEdtPwd.getText().toString().equalsIgnoreCase(passWord)) ||
                        !(mUserIdStr.toString().equalsIgnoreCase(userId))) {
                    FingerprintUtils.resetAndPurgeKeyStore(AutoSignInSplashActivity.this);
                }*/
            }
            mSharedPrefManager.putString(AppConstants.USER_NAME, primaryUserIdFromMDO.toString(), false);
            continueWithKeepAliveWSCall(mUserResponse, keepAliveCookieName, keepAliveCookieDomain, keepAliveCookiePath);
        }
    }
    private void showCAFHAlert(){
        GenericAlertDialog cafhAlert = new GenericAlertDialog(AutoSignInSplashActivity.this,getString(R.string.app_restricted_title),getString(R.string.cafh_alert_message),"view",viewListener,"Cancel",null);
        cafhAlert.setDialogTitle(getString(R.string.app_restricted_title));
        if (null != cafhAlert && !cafhAlert.isShowing() && !isFinishing()) {
            cafhAlert.showDialog();
        }
    }

    private final DialogInterface.OnClickListener viewListener = (dialogInterface, i) -> {
        dialogInterface.dismiss();
        Uri uri = Uri.parse(AppConstants.KP_ORG_URL); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    };
}
