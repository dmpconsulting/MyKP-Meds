package org.kp.tpmg.mykpmeds.activation.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.montunosoftware.mymeds.BuildConfig;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.PreEffectiveMemberActivity;
import com.montunosoftware.pillpopper.android.fingerprint.FingerprintOptInContainerActivity;
import com.montunosoftware.pillpopper.android.fingerprint.FingerprintUtils;
import com.montunosoftware.pillpopper.android.interrupts.InterruptsActivity;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.util.FirebaseEventsUtil;
import com.montunosoftware.pillpopper.android.util.NotificationBar_OverdueDose;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperApplication;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.CustomScrollView;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.service.GetAppProfileUrlsService;
import com.montunosoftware.pillpopper.service.TokenService;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.kpsecurity.KPSecurity;
import org.kp.kpsecurity.security.UnableToRetreivePasswordException;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.envswitch.EnvSwitchUtils;
import org.kp.tpmg.mykpmeds.activation.envswitch.EnvironmentSwitchActivity;
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
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.controller.RxRefillController;
import org.kp.tpmg.ttg.database.RxRefillDBUtil;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.kp.tpmg.ttgmobilelib.controller.TTGCallBackInterfaces;
import org.kp.tpmg.ttgmobilelib.controller.TTGSignonController;
import org.kp.tpmg.ttgmobilelib.model.TTGBaseResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGCarePathKeepAliveResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGInterruptObject;
import org.kp.tpmg.ttgmobilelib.model.TTGInteruptAPIResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGKeepAliveRequestObj;
import org.kp.tpmg.ttgmobilelib.model.TTGPortalAPIResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGSignonRequestDataObj;
import org.kp.tpmg.ttgmobilelib.model.TTGUserResponse;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class LoginActivity extends FragmentActivity implements OnClickListener,
        TTGCallBackInterfaces.Signon, TTGCallBackInterfaces.KeepAlive, AutofillEnabledEditText.ActionEDitListener, GetAppProfileUrlsService.AppProfileWSComplete,
        TTGCallBackInterfaces.Interrupts, TTGCallBackInterfaces.CareKeepAlive,
        TTGCallBackInterfaces.InterruptAPI, TTGCallBackInterfaces.PortalAPI, TTGCallBackInterfaces.GoogleAnalyticsCallBack {

    private StringBuilder mUserIdStr;
    private StringBuilder pwd;
    private AutofillEnabledEditText mEdtUserId;
    private AutofillEnabledEditText mEdtPwd;
    private Button mImgSignOn;
    private SwitchCompat rememberSwitch;
    private StringBuilder userName;
    private boolean retainFields;
    private boolean clearFields;
    private ActivationController activationController;
    private SharedPreferenceManager mSharedPrefManager;
    private GenericAlertDialog mAltDlgSignOnError;
    private AlertDialog dialog;
    private static AppData sAppData;
    private ActivationHandler activationHandler;
    private Handler handler;
    private ImageView user_id_clear_img;
    private ImageView pwd_clear_img;
    private CustomScrollView scrollPage;
    private boolean isForceUpgradeRequire;
    private boolean isSessionExpire;
    private RelativeLayout contentView, members_msg_tv;
    private boolean isLoading;

    private boolean mIsSignInFromFingerprintSignIn = false;
    private boolean mSessionTimeOutShowingDontShowFPSignIn = false;
    private boolean mIsInSignInProcess = false;

    private static int INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_USER_DEVICE_SWITCH = 1;
    private static int INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_SUCCESS_LOGIN = 2;

    //    private FingerprintDialogUiHandler mFingerprintDialogUiHandler;
    private boolean isActivateMemberDeviceCallRequired;
    private boolean isInBackground = false;
    private boolean launchAgain = false;

    private CardView mRefillBannerLayout;

    private TextView mCurrentEnvTextView;
    private String mSignOnInterruptType;

    private TTGUserResponse mUserResponse;
    private boolean avoidShowingFPAlertDuringStartSessioncall = false;
    private String mRetainedUserName;
    FragmentManager manager;
    private ImageView mFingerPrintImage;
    private FrontController mFrontController;
    private boolean mIsAnyUserEnabledRemindersHasSchedules;
    private String mPrimaryUserID;
    private boolean isNeededToCheckKeyboard = false;
    private boolean isDeviceEligibleForFingerprintSignIn;
    private RelativeLayout translateLayout;
    private RelativeLayout loginAnimationLayout;
    private RelativeLayout loginLayout;

    private boolean animationComplete;

    private RelativeLayout mRootLayout;
    private FrameLayout mSigninHelpLayout;
    private boolean isBiometricPromptShown;
    private TTGInteruptAPIResponse interruptResponse;
    private GenericAlertDialog forceUpgradeAlertDialog;
    private boolean isInLoginScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RefillRuntimeData.getInstance().setDbDownloadStarted(false);
        mIsInSignInProcess = false;
        Util.applyStatusbarColor(this, ContextCompat.getColor(this, R.color.linear_gradient_top));

        setHandler(new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (mUserIdStr != null) {
                    int status = 0;
                    if (null != msg) {
                        status = msg.arg1;
                    }
                    if (status == 1) {
                        TTGUserResponse userResponse = (TTGUserResponse) msg.obj;
                        if (null != userResponse) {
                            startInitSessionTask(userResponse.getGuid(), userResponse.getSsoSession(), mUserIdStr.toString());
                        }

                    }
                }
            }
        });

        if (RunTimeData.getInstance().isShowSplashAnimation()) {
            overridePendingTransition(0, 0);
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        setContentView(R.layout.signin_redesign);
        mFrontController = FrontController.getInstance(LoginActivity.this);
        mIsAnyUserEnabledRemindersHasSchedules = mFrontController.isAnyUserEnabledRemindersHasSchedules();
        mPrimaryUserID = FrontController.getInstance(this).getPrimaryUserIdIgnoreEnabled();

        Intent intent = getIntent();
        if (null != intent) {
            isForceUpgradeRequire = intent.getBooleanExtra("ForceUpgradeRequire", false);
            isSessionExpire = intent.getBooleanExtra("isSessionExpiredRequire", false);
        }

        if ((isSessionExpire || RunTimeData.getInstance().isUserLogedInAndAppTimeout() || RunTimeData.getInstance().isTimeOutOccuredDuringInturruptBackGround())
                && !RunTimeData.getInstance().isFromTutorialScreen()) {
            RunTimeData.getInstance().setShowFingerprintDialog(false);
            TokenService.startRevokeTokenService(this, FrontController.getInstance(this).getRefreshToken(this));
            showTimeOutDialog();
        }

        if (isForceUpgradeRequire) {
            showForceUpgradeAlert();
        }
        RunTimeData.getInstance().setFromTutorialScreen(false);
        TTGRuntimeData.getInstance().setAppName(AppConstants.APPNAME+" "+ Util.getAppVersion(this));
        scrollPage = findViewById(R.id.scrollPage);
        /*scrollPage.setSmoothScrollingEnabled(true);
        scrollPage.scrollTo(0,0);*/
        activationController = ActivationController.getInstance();
        sAppData = AppData.getInstance();
        RunTimeData.getInstance().setClickFlg(false);
        activationHandler = new ActivationHandler();
        initUI();
        retainFields = false;
        // ActivationController.initializeActivationServiceUrl(LoginActivity.this);
        ActivationController.initializeLoggersInSignOnLib();
        ActivationController.initilizeCertificateKeys(LoginActivity.this);

        Util.storeEnvironment(LoginActivity.this);

        if (AppConstants.isSecureFlg()) {
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
        // Stopping the timer,since timer has been started.
        activationController.stopTimer(this);
        PillpopperRunTime.getInstance().setLimitedHistorySyncToDo(true);
        PillpopperRunTime.getInstance().setHistorySyncDone(false);
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(LoginActivity.this, FireBaseConstants.ScreenEvent.SCREEN_SIGN_IN);
        AppConstants.SHOW_SAVED_ALERT = false;
        AppConstants.MEDS_TAKEN_OR_SKIPPED = false;
        AppConstants.MEDS_TAKEN_OR_POSTPONED = false;
    }

    private void setHandler(Handler handler2) {
        handler = null;
        handler = handler2;
    }

    private void showForceUpgradeAlert() {
        GenericAlertDialog forceUpgradeAlert = new GenericAlertDialog(
                LoginActivity.this, getString(R.string.time_to_upgrade_title),
                getString(R.string.time_to_upgrade_msg), Html.fromHtml(
                "<b>" + getString(R.string.upgrade_button_text)
                        + "</b>").toString(),
                (dialog, which) -> {
                    dialog.dismiss();
                    Uri uri = Uri.parse(AppConstants.PLAY_STORE_URL);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }, null, null);
        forceUpgradeAlert.showDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!animationComplete
                && RunTimeData.getInstance().isNeedToAnimateLogin()
                && RunTimeData.getInstance().isShowSplashAnimation()) {
            startLoginAnimation();
        } else {
            loginAnimationLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }

        if (!retainFields) {
            resetFields();
        }
        if (clearFields && !retainFields) {
            resetFields();
            clearFields = false;
        }

        refreshEnvironmentButtonText();

        if (null != RunTimeData.getInstance().getSaveTempUserNameForInterrupt()) {
            mEdtUserId.setText("");
            mEdtUserId.append(RunTimeData.getInstance().getSaveTempUserNameForInterrupt());
            mEdtPwd.requestFocus();
        }
    }

    private void startLoginAnimation() {

        final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
//        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.logo_fade_out);
        translateLayout.startAnimation(slideUp);
        new Handler().postDelayed(() -> {
//                loginLayout.startAnimation(fadeIn);
            loginLayout.setVisibility(View.VISIBLE);
            loginAnimationLayout.startAnimation(fadeOut);
            loginAnimationLayout.setVisibility(View.GONE);
            animationComplete = true;
        }, 600);
    }

    private void resetFields() {
        if (!rememberSwitch.isChecked()) {
            mEdtUserId.setText(AppConstants.EMPTY_STRING);
        }
        mEdtPwd.setText(AppConstants.EMPTY_STRING);
        if (null != userName && userName.length() > 0 || (null != RunTimeData.getInstance().getSaveTempUserNameForInterrupt())) {
            mEdtPwd.requestFocus();
        } else {
            mEdtUserId.requestFocus();
        }
    }

    private void initUI() {
        mSharedPrefManager = SharedPreferenceManager.getInstance(
                LoginActivity.this, AppConstants.AUTH_CODE_PREF_NAME);
        isDeviceEligibleForFingerprintSignIn = FingerprintUtils.isDeviceEligibleForFingerprintSignIn(LoginActivity.this);
        if (getIntent() != null
                && getIntent().getStringExtra("userName") != null) {
            this.userName = new StringBuilder();
            this.userName.append(getIntent().getStringExtra("userName"));
        } else {
            this.userName = new StringBuilder();
            this.userName.append(activationController.getUserName(this));
        }
        //Util.disableAutofill(this, (LinearLayout) findViewById(R.id.ll_uid_pwd));
        user_id_clear_img = findViewById(R.id.user_id_clear_icon);
        pwd_clear_img = findViewById(R.id.pwd_clear_icon);
        mFingerPrintImage = findViewById(R.id.finger_print);
        mEdtUserId = findViewById(R.id.userid_edittext);
        mEdtUserId.setActionEDitListener(this);
        mEdtUserId.setTypeface(Typeface.DEFAULT);
        mImgSignOn = findViewById(R.id.sign_on_button);
        mRefillBannerLayout = findViewById(R.id.refill_banner_layout);
        mRootLayout = findViewById(R.id.login_rootLayout);
        mSigninHelpLayout = findViewById(R.id.help_sign_in_layout);

        showRefillReminderBanner();
        mEdtUserId.setOnTouchListener((v, event) -> {
            mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                Rect r = new Rect();
                mRootLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = mRootLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    //  the keyboard is up...
                    mSigninHelpLayout.setVisibility(View.GONE);
                    scrollPage.setEnableScrolling(false);
                } else {
                    scrollPage.setEnableScrolling(true);
                    new Handler().postDelayed(() -> mSigninHelpLayout.setVisibility(View.VISIBLE), 50);
                }
            });
            ObjectAnimator anims = ObjectAnimator.ofInt(scrollPage,
                    "scrollY", mImgSignOn.getTop() - Util.convertToDp(52, LoginActivity.this));
            isNeededToCheckKeyboard = true;
            anims.setDuration(800);
            anims.start();
            return false;
        });
        mEdtUserId.addTextChangedListener(new UserIdEditTextWatcher());
        mEdtPwd = findViewById(R.id.password_edittext);
        mEdtPwd.setActionEDitListener(this);
        mEdtPwd.setTypeface(Typeface.DEFAULT);
        mEdtPwd.setOnTouchListener((v, event) -> {
            mRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                Rect r = new Rect();
                mRootLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = mRootLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    //  the keyboard is up...
                    mSigninHelpLayout.setVisibility(View.GONE);
                    scrollPage.setEnableScrolling(false);
                } else {
                    scrollPage.setEnableScrolling(true);
                    new Handler().postDelayed(() -> mSigninHelpLayout.setVisibility(View.VISIBLE), 50);
                }
            });
            ObjectAnimator anims = ObjectAnimator.ofInt(scrollPage,
                    "scrollY", mImgSignOn.getTop());
            isNeededToCheckKeyboard = true;
            anims.setDuration(800);
            anims.start();
            return false;
        });

        mEdtPwd.addTextChangedListener(new PwdEditTextWatcher());
        TextView signinHelpTxtVw = findViewById(R.id.signin_view);
        mImgSignOn.setOnClickListener(this);
        signinHelpTxtVw.setOnClickListener(this);
        rememberSwitch = findViewById(R.id.remember_switch);
        rememberSwitch.setChecked(mSharedPrefManager.getBoolean(
                AppConstants.REMEMBER_USER_ID, true));
        if (rememberSwitch.isChecked()) {
            rememberSwitch.setContentDescription(getString(R.string.remember_userid_ON));
            mEdtUserId.setText(userName);
            retainFields = true;
        } else {
            rememberSwitch.setContentDescription(getString(R.string.remember_userid_OFF));
            mFingerPrintImage.setVisibility(View.GONE);
        }
        rememberSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            mSharedPrefManager.putBoolean(AppConstants.REMEMBER_USER_ID, isChecked, false);
            if (!isChecked) {
                rememberSwitch.setContentDescription(getString(R.string.remember_userid_OFF));
                mFingerPrintImage.setVisibility(View.GONE);
            } else {
                rememberSwitch.setContentDescription(getString(R.string.remember_userid_ON));
                isDeviceEligibleForFingerprintSignIn = FingerprintUtils.isDeviceEligibleForFingerprintSignIn(LoginActivity.this);
                if (isDeviceEligibleForFingerprintSignIn && (pwd_clear_img.getVisibility() == View.GONE || TextUtils.isEmpty(mEdtPwd.getText().toString()))) {
                    mFingerPrintImage.setVisibility(View.VISIBLE);
                }
            }
        });
        mAltDlgSignOnError = new GenericAlertDialog(LoginActivity.this,
                AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING,
                getString(R.string.ok_text), alertListener, null, null);


        disableCopyPaste_EditText(mEdtPwd);

        user_id_clear_img.setOnClickListener(v -> mEdtUserId.setText(AppConstants.EMPTY_STRING));

        pwd_clear_img.setOnClickListener(v -> mEdtPwd.setText(AppConstants.EMPTY_STRING));


        mEdtPwd.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                scrollPage.scrollTo(0, -240);
                ActivationUtil.hideKeyboard(LoginActivity.this, mEdtUserId);
                mUserIdStr = new StringBuilder();
                mUserIdStr.append(mEdtUserId.getText().toString());
                pwd = new StringBuilder();
                pwd.append(mEdtPwd.getText());

                if (null != mAltDlgSignOnError)
                    mAltDlgSignOnError
                            .setDialogTitle(AppConstants.EMPTY_STRING);
                if (Strings.isNullOrEmpty(mUserIdStr.toString())) {
                    if (null != mAltDlgSignOnError)
                        mAltDlgSignOnError.setMessage("Please enter username");
                    if (null != mAltDlgSignOnError
                            && !mAltDlgSignOnError.isShowing())
                        mAltDlgSignOnError.showDialog();
                } else if (Strings.isNullOrEmpty(pwd.toString())) {

                    if (null != mAltDlgSignOnError
                            && !mAltDlgSignOnError.isShowing()) {
                        mAltDlgSignOnError.setMessage("Please enter password");
                        mAltDlgSignOnError.showDialog();
                    }
                } else {
                    mSharedPrefManager.putBoolean("timeOut", false, true);
                    if (ActivationUtil
                            .checkNetworkAvailablity(LoginActivity.this)) {
                        continueOnlineLogin();
                    } else {
                        resetFieldsOnError();
                    }
                }
                return false;
            }

            return false;
        });

        mCurrentEnvTextView = findViewById(R.id.current_env_text_view);
        if (BuildConfig.ENVIRONMENT_MAP.isEmpty()) {
            mCurrentEnvTextView.setVisibility(View.GONE);
        } else {
            mCurrentEnvTextView.setOnClickListener(this);
        }

        if (!isDeviceEligibleForFingerprintSignIn && Util.isBatteryOptimizationAlertRequired(LoginActivity.this)
                && mIsAnyUserEnabledRemindersHasSchedules && !mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, false)) {
            LoggerUtils.info("--Force SignIn-- Battery Optimization "+ mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, false));
            new Handler().postDelayed(this::showBatteryOptimizationAlert, 500);
        }

        translateLayout = findViewById(R.id.translate_layout);
        loginAnimationLayout = findViewById(R.id.login_animation_layout);
        loginLayout = findViewById(R.id.login_layout);

        Typeface mFontMedium = ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM);

        TextView refillReminderTitle = findViewById(R.id.refill_remainder);
        refillReminderTitle.setTypeface(mFontMedium);

        if (isDeviceEligibleForFingerprintSignIn) {
            mFingerPrintImage.setVisibility(TextUtils.isEmpty(userName) ? View.GONE : View.VISIBLE);
            mFingerPrintImage.setOnClickListener(view -> {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                initBiometricAuthentication();
                            }
                        });
                    }
                }, 500);
            });
        } else {
            mFingerPrintImage.setVisibility(View.GONE);
            if (mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, false)
                    && RunTimeData.getInstance().isFromSplashScreen()) {
                showForceSignInRequiredDialog();
            }
        }
    }

    private void showForceSignInRequiredDialog() {
        if (null == forceUpgradeAlertDialog) {
            forceUpgradeAlertDialog = new GenericAlertDialog(this, getString(R.string.force_signin_required_title), getString(R.string.force_signin_required_message), "Sign in", (dialog, which) -> {
                RunTimeData.getInstance().setFromSplashScreen(false);
                dialog.dismiss();
                initBiometricAuthentication();
            });
        }
        if (!forceUpgradeAlertDialog.isShowing()) {
            new Handler().postDelayed(() -> forceUpgradeAlertDialog.showDialogWithoutPadding(), 1000);
        }
    }

    private void showBatteryOptimizationAlert() {
        runOnUiThread(() -> DialogHelpers.showAlertDialogWithHeader(LoginActivity.this,
                R.string.battery_optimization_alert_title,
                R.string.battery_optimization_alert_message,
                () -> {
                    mFrontController.disableBatteryOptimizationAlert(LoginActivity.this);
                    if (FingerprintUtils.isDeviceEligibleForFingerprintSignIn(LoginActivity.this)) {
                        new Handler().postDelayed(
                                () -> runOnUiThread(new Runnable() {
                                    public void run() {
                                        initBiometricAuthentication();
                                    }
                                }), 300);
                    }
                }));
    }

    private void initBiometricAuthentication(){
        try {
            if (!Util.isEmptyString(mPrimaryUserID)) {
                if(mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY,false ) && RunTimeData.getInstance().isFromSplashScreen()){
                  showForceSignInRequiredDialog();
                }
                // Second Time Launch
                else if (Util.isBatteryOptimizationAlertRequired(LoginActivity.this) && mIsAnyUserEnabledRemindersHasSchedules
                        && RunTimeData.getInstance().isShowFingerprintDialog()) {
                    LoggerUtils.info("--Force SignIn-- Battery Optimization initBiometric "+ mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, false));
                    showBatteryOptimizationAlert();
                }
                else {
                    startBiometricAuthentication();
                }
            } else {

                if(mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY,false ) && RunTimeData.getInstance().isFromSplashScreen()){
                    showForceSignInRequiredDialog();
                } else {
                    startBiometricAuthentication();
                }
            }
        } catch (UnrecoverableKeyException e) {
            PillpopperLog.exception("Biometric - No Password Saved - " +e.getMessage());
            FingerprintUtils.resetAndPurgeKeyStore(this);
            mFingerPrintImage.setVisibility(View.GONE);
            DialogHelpers.showAlertDialog(this, "Please login with username and password");
        } catch (KeyPermanentlyInvalidatedException e) {
            PillpopperLog.exception("Biometric - No Password Saved - " +e.getMessage());
            FingerprintUtils.resetAndPurgeKeyStore(this);
            DialogHelpers.showAlertDialog(this, "Please login with username and password");
            mFingerPrintImage.setVisibility(View.GONE);
        } catch (InvalidAlgorithmParameterException e) {
            PillpopperLog.exception("Biometric - Biometrics Recently changed. Please login with username and password - " +e.getMessage());
            FingerprintUtils.resetAndPurgeKeyStore(this);
            DialogHelpers.showAlertDialog(this, "Biometrics Recently changed. Please login with username and password");
            mFingerPrintImage.setVisibility(View.GONE);
        } catch(UnableToRetreivePasswordException e) {
            PillpopperLog.exception("Biometric - Please add fingerprint biometrics to your device - " +e.getMessage());
            FingerprintUtils.resetAndPurgeKeyStore(this);
            mFingerPrintImage.setVisibility(View.GONE);
            DialogHelpers.showAlertDialog(this, "Please login with username and password");
        }
    }

    private void disableCopyPaste_EditText(EditText editText) {
        editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode actionMode,
                                               MenuItem item) {
                return false;
            }

            public void onDestroyActionMode(ActionMode actionMode) {
            }
        });
    }

    @Override
    protected void onPause() {
        mEdtUserId.setEnabled(false);
        mEdtPwd.setEnabled(false);
        super.onPause();
    }

    @Override
    protected void onStop() {
        isInLoginScreen = false;
        super.onStop();
        if (!isLoading) {
            ActivityCompat.finishAffinity(this);
        }
        RunTimeData.getInstance().setShowFingerprintDialog(true);
        RunTimeData.getInstance().setShowSplashAnimation(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (launchAgain) {
            launchAgain = false;
            isInBackground = false;
            ((PillpopperApplication) getApplication()).setAppIsInBackground(false);
            continueToHomeContainerActivity();
        }
        RunTimeData.getInstance().setAppVisibleFlg(true);
        RunTimeData.getInstance().setClickFlg(false);
        mEdtUserId.setEnabled(true);
        mEdtPwd.setEnabled(true);
        resetFields();
        if ((null != dialog && dialog.isShowing())
                && activationController.checkForTimeOut(this)) {
            closeDialogAndStopTimer(true);
        }
        if (mEdtPwd.hasFocus()) {
            scrollPage.postDelayed(() -> scrollPage.scrollTo(0, 240), 600);
        }
        if (RunTimeData.getInstance().isAppProfileKeyOrValueMissing()) {
            RunTimeData.getInstance().setAppProfileKeyOrValueMissing(false);
            showErrorAlert("", getResources().getString(R.string.app_profile_generic_error_msg));
        }
        isInLoginScreen = true;
    }

    private void showTimeOutDialog() {
        RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
        Util.hideKeyboard(LoginActivity.this, findViewById(R.id.user_id_clear_icon));
        // This is fallback mechanism if the view is null by any chance
        Util.hideSoftKeyboard(LoginActivity.this);
        PillpopperAppContext pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(this);
        pillpopperAppContext.getState(this).setAccountId(null);
        GenericAlertDialog sessionTimeOutDialogue = new GenericAlertDialog(
                LoginActivity.this, getString(R.string.session_expiry_title),
                getString(R.string.session_expiry_message),
                getString(R.string.ok_text), alertListener, null, null);
        sessionTimeOutDialogue.showDialog();
        mSessionTimeOutShowingDontShowFPSignIn = true;
    }

    private void closeDialogAndStopTimer(boolean isDialogNotNull) {
        if (isDialogNotNull) {
            dialog.dismiss();
            dialog = null;
        }
        resetFieldsOnError();
        activationController.stopTimer(this);
        mSharedPrefManager.putBoolean("timeOut", true, true);
        RunTimeData.getInstance().setOldLockTime(Long.MAX_VALUE);
        RunTimeData.getInstance().setNewLockTime(Long.MAX_VALUE);
        RunTimeData.getInstance().setAlertDisplayedFlg(false);
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onClick(View view) {
        if (!RunTimeData.getInstance().isClickFlg()) {
            RunTimeData.getInstance().setClickFlg(true);
            int id = view.getId();
            Intent intent = null;
            if (id == R.id.signin_view) {
                intent = new Intent(LoginActivity.this, SigninHelpActivity.class);
                startActivity(intent);

            } else if (id == R.id.sign_on_button) {
                isNeededToCheckKeyboard = false;
                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(false);
                RunTimeData.getInstance().setInitialGetStateCompleted(false);
                RunTimeData.getInstance().setInturruptScreenVisible(false);
                //clear the schedule runtime data
                if(null != RunTimeData.getInstance().getScheduleData()) {
                    RunTimeData.getInstance().setScheduleData(null);
                }
                mUserIdStr = new StringBuilder();
                mUserIdStr.append(mEdtUserId.getText().toString());
                pwd = new StringBuilder();
                pwd.append(mEdtPwd.getText());
                ActivationUtil.hideKeyboard(this, mEdtUserId);
                if (Strings.isNullOrEmpty(mUserIdStr.toString())) {
                    if (null != mAltDlgSignOnError)
                        mAltDlgSignOnError.setMessage("Please enter username");
                    if (null != mAltDlgSignOnError
                            && !mAltDlgSignOnError.isShowing())
                        mAltDlgSignOnError.showDialog();

                } else if (Strings.isNullOrEmpty(pwd.toString())) {
                    if (null != mAltDlgSignOnError
                            && !mAltDlgSignOnError.isShowing()) {
                        mAltDlgSignOnError.setMessage("Please enter password");
                        mAltDlgSignOnError.showDialog();
                    }
                } else {
                    // Check if the runtime data of AppProfile, If no runtime data OR the stamp is more than 15 from now to last AppProfile success call.
                    if (Util.isNetworkAvailable(LoginActivity.this) && Util.isAppProfileCallRequired(LoginActivity.this)) {
                        startActivityForResult(new Intent(this, TransparentLoadingActivity.class), 0);
                        new GetAppProfileUrlsService(LoginActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        NotificationBar_OverdueDose.cancelNotificationBar(this);
                        PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
                        RunTimeData.getInstance().resetRefreshCardsFlags();
                        performSignOnAction();
                    }
                }
            } else if (id == R.id.current_env_text_view) {
                Intent intentEnvSwitch = new Intent(this, EnvironmentSwitchActivity.class);
                startActivity(intentEnvSwitch);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //if the user allow the permission
            onPermissionGranted(requestCode);
        } else {
            if (permissions.length > 0) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    //if the user denies for the permission when the never ask again check box is not ticked
                    onPermissionDenied(requestCode);
                } else {
                    //if the user denies for the permission when the never ask again check box is ticked
                    onPermissionDeniedNeverAskAgain(requestCode);
                }
            }
        }
    }

    private void onPermissionGranted(int requestCode) {
        if (requestCode == AppConstants.PERMISSION_PHONE_CALL_PHONE) {
            //Telephony permission for getDeviceId()
            continueOnlineLogin();
        }
    }

    private void onPermissionDeniedNeverAskAgain(int requestCode) {
        String message = PermissionUtils.permissionDeniedMessage(requestCode, this);
        PermissionUtils.permissionDeniedDailogueForNeverAskAgain(this, message);
    }


    public void onPermissionDenied(int requestCode) {
        String message = PermissionUtils.permissionDeniedMessage(requestCode, this);
        PermissionUtils.permissionDeniedDailogue(this, message);
    }

    /**
     * Continue the login functionality in online Mode
     */
    public void continueOnlineLogin() {

        Util.getInstance().initializeFCM(this);

        Util.resetRuntimeInturruptFlags();
        Intent intent = new Intent(LoginActivity.this, TransparentLoadingActivity.class);
        intent.putExtra("type", "simple");
        intent.putExtra("needHomeButtonEvent", true);
        intent.putExtra("homeButtonEvent", 2);
        startActivityForResult(intent, 0);
        isLoading = true;
        retainFields = true;
        final TTGSignonController signonController = TTGSignonController
                .getInstance();
        new Thread(() -> {
            TTGSignonRequestDataObj signOnRequestDataobj = new TTGSignonRequestDataObj();

            signOnRequestDataobj.setUsername(mUserIdStr.toString().trim());

            signOnRequestDataobj.setAppVersion(ActivationUtil.getAppVersion(LoginActivity.this));
            signOnRequestDataobj.setMemberRegion(Util.getRegionsFromAppProfileData());
            signOnRequestDataobj.setApiKey(AppConstants.APIKEY);
            signOnRequestDataobj.setAppId(AppConstants.APP_ID);
            signOnRequestDataobj.setAppName(AppConstants.APPNAME+" "+ Util.getAppVersion(LoginActivity.this));
            signOnRequestDataobj
                    .setUserAgentCategory(AppConstants.USER_AGENT_CATEGORY);

            signOnRequestDataobj.setAuthorization(
                    "Basic " + new String(
                            Base64.encodeBase64(
                                    (mUserIdStr.toString().trim() + ":" + pwd.toString()).getBytes())));

            signonController.performSignon(signOnRequestDataobj,
                    LoginActivity.this, LoginActivity.this, LoginActivity.this);

        }).start();
        LocalBroadcastManager
                .getInstance(this)
                .sendBroadcast(
                        new Intent(
                                "com.montunosoftware.pillpopper.SUPPRESS_PENDING_NOTIFICATIONS"));
    }


    /**
     * Showing Alert Dialog in case of Invalid Credentials OR Account LockedOut
     *
     * @param title title for dialog
     * @param msg   msg fo rdialog
     */

    private void showErrorAlert(String title, String msg) {
        GenericAlertDialog alertDialog = new GenericAlertDialog(
                LoginActivity.this, title, msg, getString(R.string.ok_text),
                alertListener, null, null);
        if (null != alertDialog && !alertDialog.isShowing() && !isFinishing()) {
            alertDialog.showDialog();
        }
        resetFieldsOnError();
        RunTimeData.getInstance().setAlertDisplayedFlg(true);
    }

    /**
     * resetting the userId and Pwd fields.
     */
    private void resetFieldsOnError() {
        mEdtPwd.setText(AppConstants.EMPTY_STRING);
        mEdtPwd.clearFocus();
        mEdtUserId.clearFocus();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (activationController.getUserLoginFlg()) {
            activationController.restartTimer(this);
        }
    }

    public void showInvalidCredentialsAlert() {

        if (mIsSignInFromFingerprintSignIn) {
            FingerprintUtils.resetAndPurgeKeyStore(LoginActivity.this);
            mIsSignInFromFingerprintSignIn = false;
        }

        retainFields = false;
        mAltDlgSignOnError.setDialogTitle(null);
        mAltDlgSignOnError.setMessage(getString(R.string.alert_error_status_5));
        if (null != mAltDlgSignOnError && !mAltDlgSignOnError.isShowing()
                && !isFinishing()) {
            mAltDlgSignOnError.showDialog();
        }
        resetFieldsOnError();
    }

    /**
     * Starting the timer
     */
    private void startTimer() {
        activationController.startTimer(this);
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
                LoginActivity.this);
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
                    RxRefillController.getInstance(LoginActivity.this).clearRxRefillData(LoginActivity.this);
                    RxRefillDBUtil.getInstance(LoginActivity.this).resetPreferredPharmacyValue(); // writing this outside clearRxRefillData for MDO

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
                            activationController.clearWelcomeScreenDisplayCounter(LoginActivity.this);
                            FingerprintUtils.resetAndPurgeKeyStore(LoginActivity.this);
                        }
                        //activateMemberDevice API call will be made only if device switch is made,
                        //else user will be taken to home screen or tutorial
                        if (isActivateMemberDeviceCallRequired) {
                            if (ActivationUtil
                                    .checkNetworkAvailablity(LoginActivity.this)) {
                                activationController.setUserLoginFlg(true);
                                activationController.clearWelcomeScreenDisplayCounter(LoginActivity.this);
                                Util.deleteRegionContactFile(LoginActivity.this);
                                Intent intent = new Intent(LoginActivity.this,
                                        TransparentLoadingActivity.class);
                                UpdateActivationTask updateIntroTask = new UpdateActivationTask();
                                updateIntroTask
                                        .execute(prepareUpdateActivationUrl());
                                startActivityForResult(intent, 0);
                                isLoading = true;
                            }
                            isActivateMemberDeviceCallRequired = false;
                        } else {
                            activationHandler.storeInitResponse(mUserIdStr.toString(), sAppData,
                                    LoginActivity.this);
                            isInBackground = false;
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
                    resetUser();
                    mIsInSignInProcess = false;
                    activationController.stopTimer(LoginActivity.this);
//                    checkConditionAndStartFingerprintSignInFlow(100l);
                });
        RunTimeData.getInstance().setAlertDisplayedFlg(true);
        dialog = alertDialog.create();
        dialog.show();
    }

    private void resetUser() {
        if (!rememberSwitch.isChecked()) {
            mEdtUserId.setText(AppConstants.EMPTY_STRING);
        }
        mEdtPwd.setText(AppConstants.EMPTY_STRING);
        if (null != userName && userName.length() > 0) {
            mEdtPwd.requestFocus();
        } else {
            mEdtUserId.requestFocus();
        }
        mEdtUserId.setText(userName);
    }

    /**
     * Prepares the update member activation Url
     *
     * @return activateMemberDevice URL
     */
    protected String prepareUpdateActivationUrl() {
        String deviceId = ActivationUtil.getDeviceId(this);
        String url = AppConstants.getActivateMemberStatusURL()
                + AppConstants.URL_DEVICE_ID_STRING + deviceId;
        return url;
    }

    @Override
    public void onKeyPreIME(int keyCode, KeyEvent event) {
        scrollPage.scrollTo(0, -240);
    }

    @Override
    public void handleAppProfileComplete() {
        finishActivity(0);
        if (Util.isAppProfileCallRequired(this)) {
            //show error
            mAltDlgSignOnError.setDialogTitle(AppConstants.EMPTY_STRING);
            mAltDlgSignOnError.setMessage(getString(R.string.app_profile_generic_error_msg));
            mAltDlgSignOnError.showDialog();
        } else {
            NotificationBar_OverdueDose.cancelNotificationBar(this);
            PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
            RunTimeData.getInstance().resetRefreshCardsFlags();
            performSignOnAction();
        }
    }

    @Override
    public void sendTTGGoogleAnalyticsEvent(String category, String action, String label) {

    }

    @Override
    public void sendTTGGoogleAnalyticsEventWithBundle(String s, Bundle bundle) {

    }

    /**
     * Making update member activation web service call, and handling the
     * response.
     */
    private class UpdateActivationTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... url) {
            int status = -1;

            Map<String, String> params = new HashMap<>(ActivationUtil.getBaseParams(LoginActivity.this));

            Map<String, String> headers = new HashMap<>();
            if (null != activationController.getSSOSessionId(LoginActivity.this)) {
                headers.put("ssoSessionId",
                        activationController.getSSOSessionId(LoginActivity.this));
            }

            headers.put("guid", null != RunTimeData.getInstance()
                    .getSigninRespObj() ? RunTimeData.getInstance()
                    .getSigninRespObj().getGuid() : null);

            String response = sAppData.getHttpResponse(url[0],
                    AppConstants.POST_METHOD_NAME, params, headers, null, LoginActivity.this);

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
                    activationHandler.storeInitResponse(mUserIdStr.toString(), sAppData,
                            LoginActivity.this);

                    boolean rememberUserId = rememberSwitch.isChecked();
                    mSharedPrefManager.putBoolean(AppConstants.REMEMBER_USER_ID,
                            rememberUserId, false);

                    if (rememberUserId) {
                        mSharedPrefManager.putString(AppConstants.USER_NAME,
                                mUserIdStr.toString().trim(), false);
                    } else {
                        mSharedPrefManager.putString(AppConstants.USER_NAME,
                                AppConstants.EMPTY_STRING, false);
                    }

                    if (!startBiometricOptinFlow(INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_USER_DEVICE_SWITCH)) {
                        continueToHomeContainerActivity();
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
                    retainFields = false;
                    activationController.stopTimer(LoginActivity.this);
                    resetFieldsOnError();
                    break;
            }
        }
    }

    private final DialogInterface.OnClickListener alertListener = (dialog, which) -> {
        RunTimeData.getInstance().setClickFlg(false);
        dialog.dismiss();
        RunTimeData.getInstance().setAlertDisplayedFlg(false);
        RunTimeData.getInstance().setRuntimeSSOSessionID(null);
        RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
        Util.resetRuntimeInturruptFlags();
        initBiometricAuthentication();
    };

    @Override
    protected void onDestroy() {
        clearObjects();
        super.onDestroy();
        if (mSharedPrefManager.getBoolean("timeOut", true) && null != dialog) {
            dialog.dismiss();
            resetFieldsOnError();
        }
        RunTimeData.getInstance().setClickFlg(false);
        //resetRuntimeInturruptFlags();
        if (mRetainedUserName != null) {
            mRetainedUserName = null;
        }
    }

    private void clearObjects() {
        if (mUserIdStr != null) {
            mUserIdStr.delete(0, mUserIdStr.length());
            mUserIdStr = null;
        }
        if (pwd != null) {
            pwd.delete(0, pwd.length());
            pwd = null;
        }
        if (userName != null) {
            userName.delete(0, userName.length());
            userName = null;
        }

        if (mRetainedUserName != null) {
            mRetainedUserName = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private TTGUserResponse userResponse;

    @Override
    public void onSignOnSuccess(final TTGUserResponse userResponse) {
        LoggerUtils.info("----Firebase----" + FireBaseConstants.Event.AXWAY_TOKEN_SUCCESS);
        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(this, FireBaseConstants.Event.AXWAY_TOKEN_SUCCESS);
        this.userResponse = userResponse;
        Util.getInstance().resetHomeScreenCardsFlags();
        if (null != userResponse) {
            ActivationController.getInstance().setSSOSessionId(this, userResponse.getSsoSession());
            ActivationController.getInstance().saveUserRegion(this, userResponse.getRegion());
            ActivationController.getInstance().saveUserEmail(this, userResponse.getEmail());
            ActivationController.getInstance().saveUserAge(this, userResponse.getAge());
            RunTimeData.getInstance().setRuntimeSSOSessionID(userResponse.getSsoSession());
            RunTimeData.getInstance().setServiceArea(userResponse.getServiceArea());
            AppData.getInstance().clearAccessAndRefreshTokens(this);
            RunTimeData.getInstance().setDrugsNLPRemindersList(new HashMap<>());//reset
            TokenService.startGetAccessTokenService(this);

            if (null != userResponse.getEbizAccountRoles() && ("UNM").equalsIgnoreCase(userResponse.getEbizAccountRoles())) {
                handler.post(() -> {
                    finishActivity(0);
                    resetFieldsOnError();
                    retainFields = false;
                    ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                    ActivationError errorDetails = errorMessageHandler
                            .getErrorDetails(11);
                    String message = errorDetails.getMessage();
                    FirebaseEventsUtil.invokeSignOnFailureFirebaseEvent(LoginActivity.this, 11, message);
                    Util.showNMAAlert(LoginActivity.this, message);
                });
            } else if (null != userResponse.getEbizAccountRoles()
                    && "PEM".equalsIgnoreCase(userResponse.getEbizAccountRoles())) {
                handler.post(() -> {
                    retainFields = false;
                    finishActivity(0);
                    resetFieldsOnError();
//                    saveUserInfoInPreference();
                    Util.performSignout(this, PillpopperAppContext.getGlobalAppContext(this));
                    FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(this, FireBaseConstants.Event.PEM_MEMBER_ACCESS);
                    showPreEffectiveScreen();
                });

            } else if (null != userResponse.getEbizAccountRoles() && "CAFH".equalsIgnoreCase(userResponse.getEbizAccountRoles())) {
                handler.post(() -> {
                    retainFields = false;
                    finishActivity(0);
                    resetFieldsOnError();
                    FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(this, FireBaseConstants.Event.CAFH_MEMBER_ACCESS);
                    showCAFHAlert();
                    //  Util.performSignout(this, PillpopperAppContext.getGlobalAppContext(this));
                });
            } else if (null != userResponse.getEbizAccountRoles() && ("NMP").equalsIgnoreCase(userResponse.getEbizAccountRoles())) {
                handler.post(() -> {
                    finishActivity(0);
                    resetFieldsOnError();
                    retainFields = false;
                    GenericAlertDialog nmpAlert = new GenericAlertDialog(LoginActivity.this, "", getString(R.string.nmp_message), getString(R.string.ok_text), (dialog, which) -> dialog.dismiss());
                    if (!nmpAlert.isShowing() && !isFinishing()) {
                        nmpAlert.showDialog();
                    }
                });
            }
            if (null != userResponse.getEbizAccountRoles()
                    && !"PEM".equalsIgnoreCase(userResponse.getEbizAccountRoles()) && !"CAFH".equalsIgnoreCase(userResponse.getEbizAccountRoles())
                    && !"NMP".equalsIgnoreCase(userResponse.getEbizAccountRoles())) {
                continueWithKeepAliveWSCall(userResponse,
                        AppConstants.ConfigParams.getKeepAliveCookieName(),
                        AppConstants.ConfigParams.getKeepAliveCookieDomain(),
                        AppConstants.ConfigParams.getKeepAliveCookiePath());
            }
        } else {
            Util.showGenericStatusAlert(LoginActivity.this, getString(R.string.alert_error_status_20));
        }
    }

    int statusCode;

    @Override
    public void onSignOnError(TTGBaseResponse baseResponse) {
        LoggerUtils.info("----Firebase----" + FireBaseConstants.Event.AXWAY_TOKEN_FAIL);
        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(this, FireBaseConstants.Event.AXWAY_TOKEN_FAIL);
        statusCode = baseResponse.getStatusCode();
        final String statusMsg = baseResponse.getStatusMsg();

        handler.post(() -> {
            finishActivity(0);
            LoggerUtils.info("Statuscode : " + statusCode + "\n\n"
                    + "Message : " + statusMsg);

            if(statusMsg!=null){
                FirebaseEventsUtil.invokeSignOnFailureFirebaseEvent(LoginActivity.this, statusCode, statusMsg);
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
                resetFieldsOnError();
            } else if (statusCode == AppConstants.StatusCodeConstants.FORCE_UPGRADE_CODE_3
                    || statusCode == AppConstants.StatusCodeConstants.FORCE_UPGRADE_CODE_101) {
                Util.showForceUpgradeAlert(LoginActivity.this, statusMsg);
            } else if (statusCode == AppConstants.StatusCodeConstants.APP_MAINTENANCE_STATUS_CODE
                    || statusCode == AppConstants.StatusCodeConstants.SECURITY_BREACH_STATUS_CODE) {
                if (null != statusMsg && !statusMsg.isEmpty()) {
                    Util.showGenericStatusAlert(LoginActivity.this, statusMsg);
                } else {
                    Util.showGenericStatusAlert(LoginActivity.this, getString(R.string.alert_error_status_20));
                }
            } else {
                retainFields = false;
                String message = "";
                String title = "";
                if (statusCode == 6) {
                    message = getString(R.string.acti_lib_online_lockout_msg);
                    title = getString(R.string.acti_lib_lockout_title);
                } else {
                    FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(LoginActivity.this, FireBaseConstants.Event.SIGN_IN_FAIL);

                    ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                    ActivationError errorDetails = errorMessageHandler
                            .getErrorDetails(statusCode, statusMsg);
                    message = errorDetails.getMessage();
                    title = errorDetails.getTitle();
                }

                if (!("App Unavailable").equalsIgnoreCase(title)) {
                    showErrorAlert(title, message);
                } else {
                    showAppUnavailableErrorDialog(title, message);
                }
                resetFieldsOnError();
            }
        });
    }

    private void showAppUnavailableErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.ok_text), alertListener);
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        resetFieldsOnError();
        RunTimeData.getInstance().setAlertDisplayedFlg(true);
    }

    private void continueOnLoginSuccess() {

        startTimer();

        saveUserInfoInPreference();
        AppConstants.setWrongLoginAttepmts(0);
        mSharedPrefManager.putString(AppConstants.APPLOCKEDOUT, "0", false);
        activationController.setUserLoginFlg(true);
        // launching home on every successful login
        PillpopperRunTime.getInstance().setSelectedHomeFragment(HomeContainerActivity.NavigationHome.HOME);
        mSharedPrefManager.putBoolean("timeOut", false, true);

        if (!startBiometricOptinFlow(INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_SUCCESS_LOGIN)) {
            continueToHomeContainerActivity();
        }
    }

    private void saveUserInfoInPreference() {
        boolean rememberUserId = rememberSwitch.isChecked();
        mSharedPrefManager.putBoolean(AppConstants.REMEMBER_USER_ID,
                rememberUserId, true);
        if (rememberUserId && null != mUserIdStr) {
            mSharedPrefManager.putString(AppConstants.USER_NAME, mUserIdStr.toString().trim(),
                    false);
        } else {
            mSharedPrefManager.putString(AppConstants.USER_NAME,
                    AppConstants.EMPTY_STRING, false);
            FingerprintUtils.resetAndPurgeKeyStore(this);
        }
    }

    private class UserIdEditTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                      int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1,
                                  int i2) {

        }

        public void afterTextChanged(Editable editable) {
            String str = editable.toString();
            if (null != str && str.length() > 0) {
                user_id_clear_img.setVisibility(View.VISIBLE);
            } else {
                user_id_clear_img.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class PwdEditTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                                      int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1,
                                  int i2) {
        }

        public void afterTextChanged(Editable editable) {
            String str = editable.toString();
            if (null != str && str.length() > 0) {
                pwd_clear_img.setVisibility(View.VISIBLE);
                mFingerPrintImage.setVisibility(View.GONE);
            } else {
                pwd_clear_img.setVisibility(View.INVISIBLE);
                if (isDeviceEligibleForFingerprintSignIn && rememberSwitch.isChecked()) {
                    mFingerPrintImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onKeepAliveError(int i) {
        LoggerUtils.info("   onKeepAliveError   ");

        FirebaseEventsUtil.invokeKeepAliveFailureFirebaseEvent(LoginActivity.this);

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
    public void onKeepAliveSuccess(String ssoSession) {

        LoggerUtils
                .info(" Session Alive update to vordel success.\nNew Sessionid="
                        + ssoSession);
        FirebaseEventsUtil.invokeKeepAliveSuccessFirebaseEvent(this);
        mSharedPrefManager.putString(AppConstants.SSO_SESSION_ID, ssoSession,
                true);
        ActivationUtil.createObssoCookie(ssoSession, this);
        RunTimeData.getInstance().getSigninRespObj().setSsoSession(ssoSession);
        // Added for STAY_IN_TOUCH login in issue
        RunTimeData.getInstance().setRuntimeSSOSessionID(ssoSession);

        Message message = new Message();
        message.arg1 = 1;
        message.obj = RunTimeData.getInstance().getSigninRespObj();
        handler.sendMessage(message);
    }

    /**
     * Finishes the current Activity inside the handler to avoid the black screen,
     * while navigating from Login Screen to HomeScreen.
     */
    private void finishActivityInHandler() {
        finishActivity(0);
        new Handler().post(() -> finish());
    }

    public AsyncTask<String, Void, SignonResponse> startInitSessionTask(String guid, String ssoSession, String mUserIdStr) {
        return new VerifyLoginTask().execute(guid, ssoSession, mUserIdStr);

    }

    private class VerifyLoginTask extends
            AsyncTask<String, Void, SignonResponse> {

        @Override
        protected SignonResponse doInBackground(String... params) {

            return activationHandler.initSession(params[0], mUserIdStr.toString(),
                    sAppData, LoginActivity.this, params[1], params[2]);
        }

        @Override
        protected void onPostExecute(SignonResponse baseResponse) {
            super.onPostExecute(baseResponse);

            LoggerUtils.info("-- Setting the response " + baseResponse);

            RunTimeData.getInstance().setRegistrationResponse(baseResponse);

            if (null != baseResponse && null != baseResponse.getResponse()) {
                int loginStatus = Integer.parseInt(baseResponse
                        .getResponse().getStatusCode());
                String setUpcompleteFlg = baseResponse.getResponse().getSetUpCompleteFl();
                if (!(RunTimeData.getInstance().getHomeButtonPressed() == 2 && !activationController.isSessionActive(LoginActivity.this))) {
                    mAltDlgSignOnError.setDialogTitle(AppConstants.EMPTY_STRING);
                    mImgSignOn.setClickable(true);
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
                        if (versionUpgraedString.equals("0")  || versionUpgraedString.equalsIgnoreCase("0")
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
                            if (ActivationUtil.checkNetworkAvailablity(LoginActivity.this)) {
                                activationController.setUserLoginFlg(true);
                                Intent intent = new Intent(LoginActivity.this, TransparentLoadingActivity.class);
                                UpdateActivationTask updateIntroTask = new UpdateActivationTask();
                                updateIntroTask.execute(prepareUpdateActivationUrl());
                                startActivityForResult(intent, 0);
                                isLoading = true;
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
                        resetFieldsOnError();
                    } else if ( loginStatus == AppConstants.ACCOUNT_TEEN_PRIMARY){
                        finishActivity(0);
                        retainFields = false;
                        ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                        ActivationError errorDetails = errorMessageHandler.getErrorDetails(loginStatus, baseResponse.getResponse().getMessage());
                        if (!TextUtils.isEmpty(baseResponse.getResponse().getTitle())) {
                            errorDetails.setTitle(baseResponse.getResponse().getTitle());
                           Util.getInstance().showTeenAccountErrorAlert(errorDetails.getTitle(), errorDetails.getMessage(),LoginActivity.this);
                        } else {
                            showErrorAlert(errorDetails.getTitle(), errorDetails.getMessage());
                        }
                        resetFieldsOnError();
                    } else {
                        finishActivity(0);
                        retainFields = false;
                        ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                        ActivationError errorDetails = errorMessageHandler.getErrorDetails(loginStatus, baseResponse.getResponse().getMessage());
                        showErrorAlert(errorDetails.getTitle(), errorDetails.getMessage());
                        resetFieldsOnError();
                    }
                }
            } else {
                mAltDlgSignOnError.setDialogTitle(AppConstants.EMPTY_STRING);
                mImgSignOn.setClickable(true);
                finishActivity(0);
                retainFields = false;
                ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
                ActivationError errorDetails = errorMessageHandler.getErrorDetails(20, "");
                showErrorAlert(errorDetails.getTitle(), errorDetails.getMessage());
                resetFieldsOnError();
            }
            RunTimeData.getInstance().setClickFlg(false);
        }
    }

    private void showPendingOTPAlert(String message) {
        GenericAlertDialog mAlertDialog = new GenericAlertDialog(LoginActivity.this, "", message,
                null, null, getString(R.string.activate_account),
                (dialog, which) -> {
                    dialog.dismiss();
                    Intent openBrowser = new Intent(Intent.ACTION_VIEW);
                    openBrowser.setData(Uri.parse(Util.getActivationUrl(LoginActivity.this)));
                    startActivity(openBrowser);
                });
        mAlertDialog.showDialogWithoutBtnPadding();
    }

    private void showTerminatedMemberErrorAlert(String title, String message) {
        GenericAlertDialog mAltDlgSignOnError = new GenericAlertDialog(LoginActivity.this, title, message, getString(R.string.ok_text), (dialog, which) -> dialog.dismiss(), null, null);

        mAltDlgSignOnError.showDialogWithoutBtnPadding();
    }


    private boolean startBiometricOptinFlow(final int requestCode) {

        if (KPSecurity.isBiometricPromptReadyToUse(LoginActivity.this)) {
            FingerprintUtils.encryptAndStorePassword(LoginActivity.this, pwd.toString());
        }

        boolean isFingerprintDecisionTaken =
                mSharedPrefManager.getBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_DECISION_TAKEN, false);

        if (!isFingerprintDecisionTaken
                && FingerprintUtils.isDeviceEligibleForFingerprintOptIn(this)) {
            handler.post(() -> {
                finishActivity(0);
                RunTimeData.getInstance().setmFingerPrintTCInProgress(true);
                RunTimeData.getInstance().setBiomerticFinished(false);
                LoggerUtils.info("--FingerPrint--Login Activity-- "+RunTimeData.getInstance().ismFingerPrintTCInProgress());
                FingerprintOptInContainerActivity.startOptInFlow(LoginActivity.this, requestCode);
            });

            return true;
        } else {
            return false;
        }
    }

    private void checkConditionAndStartFingerprintSignInFlow(long delayInMilliseconds) {
        runOnUiThread(() -> {
            if (!mSessionTimeOutShowingDontShowFPSignIn
                    && !mIsInSignInProcess
                    && FingerprintUtils.isDeviceEligibleForFingerprintSignIn(LoginActivity.this)
                    && isInLoginScreen) {
                startFingerprintSignInFlowWithDelay();
            }
        });
    }

    private void startFingerprintSignInFlowWithDelay() {
        if (RunTimeData.getInstance().isShowFingerprintDialog()) {
            initBiometricAuthentication();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_USER_DEVICE_SWITCH
                || requestCode == INTENT_REQUEST_CODE_FINGERPRINT_OPT_IN_AFTER_SUCCESS_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                pwd = new StringBuilder(mEdtPwd.getText().toString());
                startBiometricEnroll();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                continueToHomeContainerActivity();
            }
        }
    }

    private void startBiometricEnroll() {

        if (KPSecurity.isBiometricPromptReadyToUse(LoginActivity.this)) {
            KPSecurity.initBiometric(true, this, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    PillpopperLog.say("--Biometric-- authentication error");
                    FingerprintUtils.setFingerprintSignInForUser(LoginActivity.this, false);
                    isInBackground = false;
                    if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                        FingerprintUtils.showGlobalThresholdReachedMessage(LoginActivity.this, okClickListener);
                    }else {
                        continueToHomeContainerActivity();
                    }
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    KPSecurity.biometricEncryptPassword("", pwd.toString().trim(), LoginActivity.this, result);
                    FingerprintUtils.setFingerprintSignInForUser(LoginActivity.this, true);
                    isInBackground = false;
                    continueToHomeContainerActivity();
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
                continueToHomeContainerActivity();
            }
        }
    }

    private final DialogInterface.OnClickListener okClickListener =
            (dialog, which) -> {
                dialog.dismiss();
                continueToHomeContainerActivity();
            };

    private void startBiometricAuthentication() throws UnrecoverableKeyException, KeyPermanentlyInvalidatedException, InvalidAlgorithmParameterException, UnableToRetreivePasswordException {
        if (FingerprintUtils.isDeviceEligibleForFingerprintSignIn(LoginActivity.this) && isInLoginScreen) {
            if (null == mUserIdStr) {
                mUserIdStr = new StringBuilder(mEdtUserId.getText().toString().trim());
            }
            KPSecurity.initBiometric(false, this, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    isBiometricPromptShown = false;
                    if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                        new Handler().postDelayed(() -> {
                            mFingerPrintImage.setVisibility(View.GONE);
                            FingerprintUtils.showGlobalThresholdReachedMessage(LoginActivity.this, (dialogInterface, i) -> dialogInterface.dismiss());
                        }, 500);
                    }

                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    boolean isExceptionCaused = false;
                    try {
                        isBiometricPromptShown = false;
                        pwd = new StringBuilder(KPSecurity.biometricDecryptPassword(mEdtUserId.getText().toString(), getBaseContext(), result));
                        isInBackground = false;
                    } catch (Exception ex){
                        PillpopperLog.exception("Biometric - Biometrics Recently changed. Please login with username and password - " + ex.getMessage());
                        FingerprintUtils.resetAndPurgeKeyStore(LoginActivity.this);
                        Toast.makeText(LoginActivity.this, "System settings modified", Toast.LENGTH_SHORT).show();
                        mFingerPrintImage.setVisibility(View.GONE);
                        isExceptionCaused = true;
                    }
                    if (!isExceptionCaused) {
                        performFingerprintSignIn(LoginActivity.this, pwd.toString());
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    isBiometricPromptShown = false;
                    super.onAuthenticationFailed();
                }
            });
            if(!isBiometricPromptShown && isInLoginScreen && !isFinishing()) {
                isBiometricPromptShown = true;
                KPSecurity.biometricAuthenticate(mSharedPrefManager.getString(AppConstants.USER_NAME, null));
            }
        }
    }

    private void performFingerprintSignIn(Context context, String decryptedPassword) {
        mIsSignInFromFingerprintSignIn = true;
        String savedUsername = mSharedPrefManager.getString(AppConstants.USER_NAME, null);

        if (!Util.isEmptyString(savedUsername)
                && !Util.isEmptyString(decryptedPassword)) {
            mUserIdStr = new StringBuilder(savedUsername);
            pwd = new StringBuilder(decryptedPassword);

            mEdtUserId.getText().clear();
            mEdtUserId.setText(mUserIdStr.toString());
            mEdtPwd.getText().clear();
            mEdtPwd.setText(pwd.toString());

            //performSignOnAction();
            if (Util.isAppProfileCallRequired(LoginActivity.this)) {
                startActivityForResult(new Intent(this, TransparentLoadingActivity.class), 0);
                new GetAppProfileUrlsService(LoginActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                NotificationBar_OverdueDose.cancelNotificationBar(this);
                PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
                RunTimeData.getInstance().resetRefreshCardsFlags();
                performSignOnAction();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isInBackground = !hasFocus;
    }

    private void continueToHomeContainerActivity() {

        Util.initializeRefillNativeFl(LoginActivity.this);
        if (isInBackground && ((PillpopperApplication) getApplication()).isInBackground()) {
            launchAgain = true;
            finishActivity(0);
            return;
        }
        PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
        Intent intent = new Intent(LoginActivity.this, HomeContainerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishActivityInHandler();

    }

    private void performFingerprintSignIn() {
        mIsSignInFromFingerprintSignIn = true;
        String savedUsername = mSharedPrefManager.getString(AppConstants.USER_NAME, null);
        String decryptedPassword = FingerprintUtils.getDecryptedPassword(LoginActivity.this, savedUsername);

        if (!Util.isEmptyString(savedUsername)
                && !Util.isEmptyString(decryptedPassword)) {
            mUserIdStr = new StringBuilder(savedUsername);
            pwd = new StringBuilder(decryptedPassword);

            mEdtUserId.getText().clear();
            mEdtUserId.setText(mUserIdStr.toString());
            mEdtPwd.getText().clear();
            mEdtPwd.setText(pwd.toString());

            //performSignOnAction();
            if (Util.isAppProfileCallRequired(LoginActivity.this)) {
                startActivityForResult(new Intent(this, TransparentLoadingActivity.class), 0);
                new GetAppProfileUrlsService(LoginActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                NotificationBar_OverdueDose.cancelNotificationBar(this);
                PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
                RunTimeData.getInstance().resetRefreshCardsFlags();
                performSignOnAction();
            }

        }

    }

    private void performSignOnAction() {
        mIsInSignInProcess = true;
        RunTimeData.getInstance().setInturruptScreenVisible(false);
        RunTimeData.getInstance().setInterruptScreenBackButtonClicked(false);
        RefillRuntimeData.getInstance().clearAllPrescriptionByRx();
        RefillRuntimeData.getInstance().clearRuntimePrescriptionData();
        RefillRuntimeData.getInstance().setDbDownloadStarted(false);
        RefillRuntimeData.getInstance().setIsAPICalledForCurrentSession(new HashMap<>()); // reset
        RunTimeData.getInstance().setRegionContactAPICallRequired(true);
        RunTimeData.getInstance().setLastSelectedFragmentPosition(-1);
        PillpopperConstants.setIsAlertActedOn(false);
        // resetting the below flag, if it was set to true in HomeContainerActivity's launchQuickView
        AppConstants.setByPassLogin(false);

        Util.getInstance().resetHomeScreenCardsFlags();

        scrollPage.scrollTo(0, -240);
        ActivationUtil.hideKeyboard(this, mEdtUserId);
        mUserIdStr = new StringBuilder();
        mUserIdStr.append(mEdtUserId.getText().toString());
        pwd = new StringBuilder();
        pwd.append(mEdtPwd.getText());

        mAltDlgSignOnError.setDialogTitle(AppConstants.EMPTY_STRING);
        //reset sso-interrupts terms and conditions flag
        mSharedPrefManager.putBoolean(AppConstants.TC_BANNER_TO_BE_SHOWN, false, false);
        mSharedPrefManager = SharedPreferenceManager.getInstance(LoginActivity.this, AppConstants.AUTH_CODE_PREF_NAME);

        AppConstants.setWelcomeScreensDisplayResult("-1"); // reset after signout

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

        if (Strings.isNullOrEmpty(mUserIdStr.toString())) {
            if (null != mAltDlgSignOnError)
                mAltDlgSignOnError.setMessage("Please enter username");
            if (null != mAltDlgSignOnError
                    && !mAltDlgSignOnError.isShowing())

                mAltDlgSignOnError.showDialog();

        } else if (Strings.isNullOrEmpty(pwd.toString())) {
            if (null != mAltDlgSignOnError
                    && !mAltDlgSignOnError.isShowing()) {
                mAltDlgSignOnError.setMessage("Please enter password");
                mAltDlgSignOnError.showDialog();
            }

        } else {
            mSharedPrefManager.putBoolean("timeOut", false, true);
            if (ActivationUtil
                    .isNetworkAvailable(LoginActivity.this)) {
                continueOnlineLogin();
            } else {
                GenericAlertDialog alertDialog = new GenericAlertDialog(this,
                        getString(R.string.data_unavailable_title),
                        getString(R.string.alert_network_error_for_sign_in),
                        getString(R.string.ok_text), (dialog, which) -> {
                            RunTimeData.getInstance().setClickFlg(false);
                            RunTimeData.getInstance().setAlertDisplayedFlg(
                                    false);
                            dialog.dismiss();
                            resetFieldsOnError();
                        }, null, null);
                alertDialog.showDialog();
            }
        }
    }


    private void resetAllLateReminderFlags() {
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_STATE_REMOVAL, false, false);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false, false);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL_LOGIN_ONCE, false, false);
        mSharedPrefManager.putString(AppConstants.TIME_STAMP, "0", false);
    }

    private class CheckRefillReminder extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            return RefillReminderController.getInstance(LoginActivity.this).getOverdueRefillRemindersForCards().size();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result > 0) {
                new Handler().postDelayed(() -> {
                    mRefillBannerLayout.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    final int targtetHeight = mRefillBannerLayout.getMeasuredHeight();
                    mRefillBannerLayout.setVisibility(View.VISIBLE);
                    mRefillBannerLayout.getLayoutParams().height = 0;
                    Animation a = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            mRefillBannerLayout.getLayoutParams().height = interpolatedTime == 1
                                    ? ViewGroup.LayoutParams.WRAP_CONTENT
                                    : (int) (targtetHeight * interpolatedTime);
                            mRefillBannerLayout.requestLayout();
                        }

                        @Override
                        public boolean willChangeBounds() {
                            return true;
                        }
                    };
                    a.setDuration(500);
                    mRefillBannerLayout.startAnimation(a);
                    checkConditionAndStartFingerprintSignInFlow(700);
                }, 500);
            } else {
                if (RunTimeData.getInstance().isShowFingerprintDialog() && isDeviceEligibleForFingerprintSignIn && isInLoginScreen) {
                    if (!isFinishing()) {
                        initBiometricAuthentication();
                    }
                }
            }
        }
    }

    private void showRefillReminderBanner() {
        new CheckRefillReminder().execute();
    }

    private void refreshEnvironmentButtonText() {
        String envButtonText = "Env: " + EnvSwitchUtils.getCurrentEnvironmentName(this);
        mCurrentEnvTextView.setText(envButtonText);
    }

    @Override
    public void onNonIgnorableInterruptRecieved(TTGInterruptObject interrupt) {
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
                        interruptResponse,LoginActivity.this, prepareJSONForTCPut());
            } else {
                avoidShowingFPAlertDuringStartSessioncall = false;
                if (rememberSwitch.isChecked()) {
                    mRetainedUserName = mEdtUserId.getText().toString();
                    RunTimeData.getInstance().setSaveTempUserNameForInterrupt(mRetainedUserName);
                }
                Intent intent = new Intent(LoginActivity.this, InterruptsActivity.class);
                intent.putExtra("mSignOnInterruptType", mSignOnInterruptType);
                startActivity(intent);
            }
        } else {
            finishActivity(0);
            handler.post(() -> showErrorAlert("", getResources().getString(R.string.app_profile_generic_error_msg)));
        }
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
            TTGSignonController.getInstance().performCareKeepAliveAPICall(portalAPIResponse, LoginActivity.this);
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
        TTGSignonController.getInstance().performInterruptAPICall(TTGMobileLibConstants.HTTP_METHOD_GET, carePathResponse, LoginActivity.this, null);
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
        RunTimeData.getInstance().setInterruptGetAPIResponse(interruptResponse);
        FireBaseAnalyticsTracker.getInstance().logEvent(LoginActivity.this,
                FireBaseConstants.Event.EVENT_SSO_INTERRUPTS_GET_SUCCESS,
                FireBaseConstants.ParamName.PARAMETER_API_SUCCESS,
                FireBaseConstants.ParamValue.PARAMETER_VALUE_SUCCESS_WITH_STATUS0);
        if (null != mSignOnInterruptType && (mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_365)
                || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_MUST_ACCEPT_NEW_VERSION)
                || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_NOT_ACCEPTED))) {
            mSharedPrefManager.putBoolean(AppConstants.TC_BANNER_TO_BE_SHOWN, true, false);
            TTGSignonController.getInstance().performInterruptAPICall(TTGMobileLibConstants.HTTP_METHOD_PUT,
                    interruptResponse, LoginActivity.this, prepareJSONForTCPut());
        } else {
            avoidShowingFPAlertDuringStartSessioncall = false;
            if (rememberSwitch.isChecked()) {
                mRetainedUserName = mEdtUserId.getText().toString();
                RunTimeData.getInstance().setSaveTempUserNameForInterrupt(mRetainedUserName);
            }
            Intent intent = new Intent(LoginActivity.this, InterruptsActivity.class);
            intent.putExtra("mSignOnInterruptType", mSignOnInterruptType);
            startActivity(intent);
        }
    }

    @Override
    public void onInterruptAPIFailure() {
        LoggerUtils.info("Result - Interrupt API call failed");
        FirebaseEventsUtil.invokeInterruptFailureAPIEvent(LoginActivity.this);
        handleInterruptFailureScenario();
    }

    @Override
    public void onInterruptPUTAPISuccess(TTGInteruptAPIResponse interuptAPIResponse) {
        LoggerUtils.info("Result - Interrupt PUT API call success");
        if (null != interuptAPIResponse && null != interuptAPIResponse.getInterruptType() &&
                (TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH.equalsIgnoreCase(interuptAPIResponse.getInterruptType()) ||
                        TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS.equalsIgnoreCase(interuptAPIResponse.getInterruptType()) ||
                        TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD.equalsIgnoreCase(interuptAPIResponse.getInterruptType()))) {
            avoidShowingFPAlertDuringStartSessioncall = false;
            if (rememberSwitch.isChecked()) {
                RunTimeData.getInstance().setSaveTempUserNameForInterrupt(mRetainedUserName);
            }
            Intent intent = new Intent(LoginActivity.this, InterruptsActivity.class);
            intent.putExtra("mSignOnInterruptType", interuptAPIResponse.getInterruptType());
            startActivity(intent);
        } else {
            if (null != mSignOnInterruptType && (mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_365)
                    || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_MUST_ACCEPT_NEW_VERSION)
                    || mSignOnInterruptType.equalsIgnoreCase(TTGMobileLibConstants.SIGNON_RESPONSE_INTERRUPT_TNC_NOT_ACCEPTED))) {
                String keepAliveCookieName = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_NAME_KEY);
                String keepAliveCookieDomain = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY);
                String keepAliveCookiePath = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVE_COOKIE_PATH_KEY);
                if (null != interuptAPIResponse && null != interuptAPIResponse.getSsosession()) {
                    mUserResponse.setSsoSession(interuptAPIResponse.getSsosession());
                }
                continueWithKeepAliveWSCall(mUserResponse, keepAliveCookieName, keepAliveCookieDomain, keepAliveCookiePath);
            }
        }
    }

    private void continueWithKeepAliveWSCall(TTGUserResponse userResponse, String keepAliveCookieName, String keepAliveCookieDomain, String keepAliveCookiePath) {
        this.userResponse = userResponse;
        RunTimeData.getInstance().setSigninRespObj(userResponse);

        ActivationUtil.createObssoCookie(userResponse.getSsoSession(),
                this);

        TTGKeepAliveRequestObj keepAliveRequestObj = new TTGKeepAliveRequestObj(
                keepAliveCookieName,
                keepAliveCookieDomain,
                userResponse.getSsoSession(),
                keepAliveCookiePath);

        try {
            TTGSignonController.getInstance()
                    .performKeepAlive(keepAliveRequestObj,
                            LoginActivity.this);
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
        LoggerUtils.info("Trans LoginActivity : onRestart");
        if (RunTimeData.getInstance().isFromInterruptScreen() && null != mSignOnInterruptType && null != RunTimeData.getInstance().getmTTtgInteruptAPIResponse()) {
            RunTimeData.getInstance().setFromInterruptScreen(false);
            TTGInteruptAPIResponse response = RunTimeData.getInstance().getmTTtgInteruptAPIResponse();
            String keepAliveCookieName = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_NAME_KEY);
            String keepAliveCookieDomain = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY);
            String keepAliveCookiePath = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVE_COOKIE_PATH_KEY);
            if (null != response.getSsosession()) {
                mUserResponse.setSsoSession(response.getSsosession());
            }
            //Commenting for DE21668: MM 5.2: Getting finger print T&C after clearing the interrupt, but it should not.
           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String userId = mSharedPrefManager.getString(AppConstants.USER_NAME, mUserIdStr.toString());
                String decryptedPwd = FingerprintUtils.getDecryptedPassword(LoginActivity.this, userId);

                if (!(null != decryptedPwd && decryptedPwd.equalsIgnoreCase(mEdtPwd.getText().toString())) ||
                        !(userId.equalsIgnoreCase(mUserIdStr.toString()))) {
                    FingerprintUtils.resetAndPurgeKeyStore(LoginActivity.this);
                }
            }*/
            mSharedPrefManager.putString(AppConstants.USER_NAME, mUserIdStr.toString().trim(), false);
            continueWithKeepAliveWSCall(mUserResponse, keepAliveCookieName, keepAliveCookieDomain, keepAliveCookiePath);
        }

        //To handle back button navigation from interrupt screen.
        if (RunTimeData.getInstance().isInterruptScreenBackButtonClicked()) {
            finishActivity(0);
            resetFieldsOnError();
            mEdtUserId.setText(RunTimeData.getInstance().getSaveTempUserNameForInterrupt()); //Setting previously user user id after coming from interrupt screen
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // do not make the call to .super() as due to some reason its causing "" Exception when invoking the fingerprintFlow Dialogue during the activity is moved to background.
        // Hence we are stopping the super call to avoid these instances.
        //https://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa/10261438#10261438
    }

    private void showPreEffectiveScreen() {
        FireBaseAnalyticsTracker.getInstance().logEvent(this,
                FireBaseConstants.Event.EVENT_SIGN_IN_STATE,
                FireBaseConstants.ParamName.PARAMETER_NAME_TYPE,
                FireBaseConstants.ParamValue.PARAMETER_VALUE_PRE_EFFECTIVE);
        Intent launchPEMIntent = new Intent(LoginActivity.this, PreEffectiveMemberActivity.class);
        startActivity(launchPEMIntent);
    }

    private void showCAFHAlert(){
        GenericAlertDialog cafhAlert = new GenericAlertDialog(LoginActivity.this,getString(R.string.app_restricted_title),getString(R.string.cafh_alert_message),"view",viewListener,"Cancel",null);
        cafhAlert.setDialogTitle(getString(R.string.app_restricted_title));
        if (null != cafhAlert && !cafhAlert.isShowing() && !isFinishing()) {
            cafhAlert.showDialog();
        }
    }

    private final DialogInterface.OnClickListener viewListener = (dialogInterface, i) -> {
        dialogInterface.dismiss();
        Uri uri = Uri.parse(AppConstants.KP_ORG_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    };

}

