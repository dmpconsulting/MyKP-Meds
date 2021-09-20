package com.montunosoftware.pillpopper.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.fingerprint.FingerprintOptInStateListenerContainerActivity;
import com.montunosoftware.pillpopper.android.fingerprint.FingerprintUtils;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;
import com.montunosoftware.pillpopper.android.util.NotificationBar_OverdueDose;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.UIUtils;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.model.ManageMemberObj;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.Preferences;
import com.montunosoftware.pillpopper.model.State;
import com.montunosoftware.pillpopper.model.UserPreferences;
import com.montunosoftware.pillpopper.service.TokenService;
import com.montunosoftware.pillpopper.service.getstate.GetStateService;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.kpsecurity.KPSecurity;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.controller.ClearAllStoreDataController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.controller.RxRefillController;
import org.kp.tpmg.ttg.database.RxRefillDBUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author Created by M1024581 on 6/30/2016.
 */
public class SettingsBaseScreenFragment extends Fragment implements View.OnClickListener {

    private PillpopperAppContext _globalAppContext;
    private PillpopperActivity mPillpopperActivity;
    private FrontController mFrontController;
    private Activity mContext;

    private List<ManageMemberObj> mMembersList;

    private Preferences _preferences = new Preferences();
    // Reminder sound
    private Uri mDefaultNotificationSoundUri;

    private static final int _REQ_SET_REMINDER_SOUND = 101;
    private static final int _REQ_SET_REMINDER_SOUND_0 = 102;

    private View mView;
    private RelativeLayout mMembersRootLayout;

    private LinearLayout mFingerprintSignInContainerLinearLayout;
    private SwitchCompat mFingerprintSignInSwitch;
    private boolean mIsAutomaticFingerprintSwitchToggle = false;
    private boolean mIsSettingsScreenStarting = false;
    private TextView mTxtNotificationSoundSelect;
    private TextView mRepeatReminders;
    private TextView mTxtLastSyncTime;
    private TextView mTxtVersion;
    private TextView mLblJenkins_build_no;
    private SwitchCompat mSignedOutRemindersSwitch;
    private TextView mSignedOutCopyEdit;
    private TextView mShowHistoryCopyEdit;
    private int selectedPosition = -1;
    private String[] mRepeatReminderItems;
    private String[] mShowHistoryItems;

    private static final int MEMBER_LAYOUT_ID_BASE = 1000;
    private int mMemberLayoutId = MEMBER_LAYOUT_ID_BASE;
    private UserPreferences mUserPreferences;
    ReminderListenerInterfaces mReminderShowListener;

    public static final int INTENT_REQUEST_CODE_OPT_IN_FROM_SETTINGS = 1;


    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PillpopperLog.say("Debug -- GetState or Get History Success" );
            UIUtils.dismissProgressDialog();
            if (!PillpopperRunTime.getInstance().isHistorySyncDone() && (intent.hasExtra(PillpopperConstants.KEY_ACTION) &&
                    intent.getStringExtra(PillpopperConstants.KEY_ACTION).equals(PillpopperConstants.ACTION_HISTORY_EVENTS))) {
                PillpopperLog.say("$$$History : setting historySyncDone ");
                PillpopperRunTime.getInstance().setHistorySyncDone(true);
                createLogEntryForSetPref(selectedPosition);
                updateUserPreferences();
            }
            updateLastSyncTime();
        }
    };

    BroadcastReceiver mGetFailedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PillpopperLog.say("Debug -- GetState or Get History Failed");
            UIUtils.dismissProgressDialog();
            PillpopperRunTime.getInstance().setHistorySyncDone(false);
            DialogHelpers.showAlertDialogWithHeader(getActivity(),
                    R.string.unable_to_retrieve_data_title,
                    R.string.unable_to_retrieve_data_message, null);
            selectedPosition = lastHistorySelectedPosition;
            mFrontController.setDoseHistoryDaysForUser(selectedPosition, mUserPreferences.getUserId());
            loadShowHistorySpinnerData();
        }
    };
    private boolean fingerPrintOptInFlowInProgress;
    private int lastHistorySelectedPosition;
    private boolean isActivityRestartCalled;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.settings_base_layout, container, false);

        mPillpopperActivity = (PillpopperActivity) getActivity();
        mFrontController = FrontController.getInstance(mPillpopperActivity);
        _globalAppContext = PillpopperAppContext.getGlobalAppContext(mPillpopperActivity);
        mContext = getActivity();
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(mPillpopperActivity, FireBaseConstants.ScreenEvent.SCREEN_SETTINGS);
        initUI();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mIsSettingsScreenStarting = true;

        initFingerprintSignInSwitch(mView);

        IntentFilter getStateIntentFilter = new IntentFilter();
        getStateIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        getActivity().registerReceiver(mGetStateBroadcastReceiver, getStateIntentFilter);

        IntentFilter getHistoryIntentFilter = new IntentFilter();
        getHistoryIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_FAILED);
        getActivity().registerReceiver(mGetFailedBroadcastReceiver, getHistoryIntentFilter);

        mIsSettingsScreenStarting = false;
    }

    private void syncData() {
        if (mFrontController.isLogEntryAvailable()) {
            LoggerUtils.info("Log Entry available. size  " + mFrontController.getLogEntries(getActivity()).length());
            StateDownloadIntentService.startActionIntermediateGetState(getContext());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mReminderShowListener.showReminder(true);
        mMembersRootLayout.removeAllViews();
        loadData(); // to update manage members section on return
        RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mReminderShowListener = (ReminderListenerInterfaces) context;
        } catch (ClassCastException e) {
            LoggerUtils.exception(context.toString() + " must implement ReminderListenerInterfaces", e);
            throw new ClassCastException(context.toString() + " must implement ReminderListenerInterfaces");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mGetStateBroadcastReceiver);
        getActivity().unregisterReceiver(mGetFailedBroadcastReceiver);
    }

    private void initUI() {
        mMembersRootLayout = mView.findViewById(R.id.members_root_view);
        LinearLayout clear_data_layout = mView.findViewById(R.id.clear_data_layout);
        mTxtNotificationSoundSelect = mView.findViewById(R.id.txt_notification_select);
        mRepeatReminders = mView.findViewById(R.id.reminder_select);
        mTxtLastSyncTime = mView.findViewById(R.id.last_sync_time_text);
        mTxtVersion = mView.findViewById(R.id.version_text);
        TextView mTxtJenkins_build_no = mView.findViewById(R.id.jenkins_build_no_text);
        mLblJenkins_build_no = mView.findViewById(R.id.jenkins_build_no);
        mTxtJenkins_build_no.setText(getString(R.string.jenkins_build_number));

        if (Util.isProductionBuild()) {
            mTxtJenkins_build_no.setVisibility(View.GONE);
            mLblJenkins_build_no.setVisibility(View.GONE);
        } else {
            mTxtJenkins_build_no.setVisibility(View.VISIBLE);
            mLblJenkins_build_no.setVisibility(View.VISIBLE);
        }
        mSignedOutRemindersSwitch = mView.findViewById(R.id.toggleButton_singedOut);
        mSignedOutCopyEdit = mView.findViewById(R.id.singed_out_subtext);
        mRepeatReminderItems = mPillpopperActivity.getResources().getStringArray(R.array.reminder_nag_frequency);
        mShowHistoryCopyEdit = mView.findViewById(R.id.tv_history_select);
        mShowHistoryItems = mPillpopperActivity.getResources().getStringArray(/*!AppConstants.newHistorySwitch ? R.array.history_reminder_window : */R.array.new_history_reminder_window);

        clear_data_layout.setOnClickListener(this);
        mTxtNotificationSoundSelect.setOnClickListener(this);
        mRepeatReminders.setOnClickListener(this);
        mShowHistoryCopyEdit.setOnClickListener(this);

        initFingerprintSignInSwitch(mView);
    }

    private void loadData() {
        loadMembersInfo();
        if (mMembersList != null && !mMembersList.isEmpty()) {
            mUserPreferences = mFrontController.getUserPreferencesForUser(mMembersList.get(0).getUserId());
        }
        loadNotificationSoundInfo();
        updateLastSyncTime();
        loadVersionInfo();
        loadShowHistorySpinnerData();
        loadRepeatRemindersSpinnerData();
        loadSignedOutReminderInfo();
    }

    private void loadSignedOutReminderInfo() {
        if (mUserPreferences != null) {
            mSignedOutRemindersSwitch.setChecked(mUserPreferences.isSignedOutRemindersEnabled()); // default set to true
        } else {
            mSignedOutRemindersSwitch.setChecked(false); // default set to false
        }
        if (mSignedOutRemindersSwitch.isChecked()) {
            mSignedOutRemindersSwitch.setContentDescription(getString(R.string.settings_reminder_without_sigin_ON));
            mSignedOutCopyEdit.setText(R.string.signed_out_reminder_on_text);
        } else {
            mSignedOutRemindersSwitch.setContentDescription(getString(R.string.settings_reminders_without_sigin_OFF));
            mSignedOutCopyEdit.setText(R.string.signed_out_reminder_off_text);
        }
        mSignedOutRemindersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.QUICKVIEW_TOGGLE,
                        FireBaseConstants.ParamName.TOGGLE,
                        FireBaseConstants.ParamValue.ON);
                mSignedOutRemindersSwitch.setContentDescription(getString(R.string.settings_reminder_without_sigin_ON));
                mSignedOutCopyEdit.setText(R.string.signed_out_reminder_on_text);
            } else {
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.QUICKVIEW_TOGGLE,
                        FireBaseConstants.ParamName.TOGGLE,
                        FireBaseConstants.ParamValue.OFF);
                mSignedOutRemindersSwitch.setContentDescription(getString(R.string.settings_reminders_without_sigin_OFF));
                mSignedOutCopyEdit.setText(R.string.signed_out_reminder_off_text);
            }
            if (mUserPreferences != null) {
                mFrontController.setSignedOutReminderEnabled(isChecked, mUserPreferences.getUserId());
            } else {
                mFrontController.setSignedOutReminderEnabled(isChecked, mFrontController.getPrimaryUserIdIgnoreEnabled());
            }
            JSONObject prefrences = new JSONObject();
            try {
                prefrences.put(PillpopperConstants.ACTION_SETTINGS_SIGNOUT_REMINDERS, isChecked ? "1" : "0");
                createLogEntryForSetPreferences(prefrences);
            } catch (JSONException e) {
                PillpopperLog.say("Exception in loadSignedOutReminderInfo method", e);
            }
        });
    }

    public void updateLastSyncTime() {
        SimpleDateFormat df = new SimpleDateFormat("MM/d/yy h:mm aaa", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        if (PillpopperRunTime.getInstance() != null) {
            if (PillpopperRunTime.getInstance().getmLastSyncTime() != null) {
                cal.setTimeInMillis(PillpopperRunTime.getInstance().getmLastSyncTime().getTimeInMillis());
                mTxtLastSyncTime.setText(df.format(cal.getTime()));
            }
        }
    }

    private void loadVersionInfo() {
        try {
            mTxtVersion.setText(mPillpopperActivity.getPackageManager().getPackageInfo(mPillpopperActivity.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            PillpopperLog.say("PackageManager.NameNotFoundException", e);
        }
    }

    private void loadNotificationSoundInfo() {
        mDefaultNotificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        updateReminderSoundInfo(getReminderSound(), false);
    }

    private void loadShowHistorySpinnerData() {
        checkAndUpdateDoseHistoryDays();
        mShowHistoryCopyEdit.setText(mShowHistoryItems[getNewHistoryPosition()]);
    }

    JSONObject jsonObj;

    private void createLogEntry(JSONObject preferences) {

        String replyId = Util.getRandomGuid();
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        logEntryModel.setReplyID(replyId);
        if (mUserPreferences != null) {
            jsonObj = Util.prepareSettingsAction(preferences, replyId, mUserPreferences.getUserId(), getActivity());
        } else {
            jsonObj = Util.prepareSettingsAction(preferences, replyId, mFrontController.getPrimaryUserIdIgnoreEnabled(), getActivity());
        }
        logEntryModel.setEntryJSONObject(jsonObj,getActivity());
        FrontController.getInstance(getActivity()).addLogEntry(getActivity(), logEntryModel);

    }

    private void loadRepeatRemindersSpinnerData() {
        if (mUserPreferences != null && mUserPreferences.getRepeatRemindersAfter() != null && !mUserPreferences.getRepeatRemindersAfter().equalsIgnoreCase("")) {
            int repeatRemindersAfter = Util.handleParseInt(mUserPreferences.getRepeatRemindersAfter());

            PillpopperLog.say("Loading RepeatReminder value : " + repeatRemindersAfter);

            if (repeatRemindersAfter != -1) {
                mRepeatReminders.setText("" + (repeatRemindersAfter / 60) + " " + mPillpopperActivity.getResources().getString(R.string._minutes_small));
            } else {
                mRepeatReminders.setText(mPillpopperActivity.getResources().getString(R.string.never));
            }
        } else {
            PillpopperLog.say("Loading RepeatReminder value Not found in V3 hence setting as default. : " + mRepeatReminderItems[2]);
            mRepeatReminders.setText(mRepeatReminderItems[2]);   // default to 10 minutes
        }
    }

    private boolean isNotValidSecondaryReminderTime(int repeatRemindersAfter) {
        if (repeatRemindersAfter == 3600) {
            PillpopperLog.say("Repeat Reminder Notification is 1 hour ");
            return true;
        }
        return false;
    }

    private void loadMembersInfo() {

        mMembersList = mFrontController.getUsersData();

        if (mMembersList != null) {
            for (int i = 0; i < mMembersList.size(); i++) {
                addMemberLayout(mMembersList.get(i));
            }
        }
    }


    private void addMemberLayout(final ManageMemberObj memberObj) {
        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.manage_members_list_item, null);

        TextView txtUserName = view.findViewById(R.id.txt_member_name);
        TextView txtIsMedicationEnabled = view.findViewById(R.id.txt_medication_enabled);
        TextView txtIsRemindersEnabled = view.findViewById(R.id.txt_notification_enabled);

        if (memberObj.getUserFirstName() != null) {
            txtUserName.setText(memberObj.getUserFirstName());
        }
        if (memberObj.getMedicationsEnabled().equalsIgnoreCase("Y") && !(memberObj.getTeen()&& !Util.getTeenToggleEnabled())){
            txtIsMedicationEnabled.setVisibility(View.VISIBLE);
            if (memberObj.getRemindersEnabled() == null || memberObj.getRemindersEnabled().equalsIgnoreCase("Y")) {
                txtIsRemindersEnabled.setVisibility(View.VISIBLE);
            } else if (memberObj.getRemindersEnabled().equalsIgnoreCase("N")) {
                txtIsRemindersEnabled.setVisibility(View.INVISIBLE);
            }
        } else {
            txtIsMedicationEnabled.setVisibility(View.GONE);
            txtIsRemindersEnabled.setVisibility(View.GONE);
        }

        if (txtIsRemindersEnabled.getVisibility() == View.VISIBLE) {
            txtIsMedicationEnabled.setText(mContext.getString(R.string.medications).toString().concat(","));
        }

        view.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(SettingsManageMembersActivity.BUNDLE_CONSTANT_MEMBER_OBJ, memberObj);
            Intent intent = new Intent(getContext(), SettingsManageMembersActivity.class);
            intent.putExtras(bundle);
            mPillpopperActivity.startActivity(intent);
        });

        if (mMemberLayoutId != MEMBER_LAYOUT_ID_BASE) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, mMemberLayoutId);
            view.setLayoutParams(params);
        }
        mMemberLayoutId++;
        view.setId(mMemberLayoutId);
        mMembersRootLayout.addView(view);

    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.clear_data_layout) {
            syncData();
            DialogHelpers.showConfirmationDialog(mPillpopperActivity, R.string.clear_data_message,
                    () -> {
                        startActivityForResult(new Intent(getActivity(), LoadingActivity.class), 0);
                        TokenService.startRevokeTokenService(mPillpopperActivity, FrontController.getInstance(mPillpopperActivity).getRefreshToken(mPillpopperActivity));
                        new Handler().postDelayed(() -> {
                            getActivity().finishActivity(0);
                            clearAllStoredData();
                        },5000);
                    });
        } else if (viewId == R.id.txt_notification_select) {
            String currentReminderSound = getReminderSound();
            Uri menuUri;
            if (State.REMINDER_SOUND_DEFAULT.equals(currentReminderSound)) {
                menuUri = mDefaultNotificationSoundUri;
            } else if (State.REMINDER_SOUND_NONE.equals(currentReminderSound)) {
                menuUri = null;
            } else {
                menuUri = Uri.parse(currentReminderSound);
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationBar_OverdueDose.getChannelId());
                startActivityForResult(intent,_REQ_SET_REMINDER_SOUND_0);
            }
            else {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        Settings.System.DEFAULT_NOTIFICATION_URI);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                        getString(R.string.setting_reminder_sound_help));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, menuUri);
                startActivityForResult(intent, _REQ_SET_REMINDER_SOUND);
            }

        } else if (viewId == R.id.reminder_select) {
            showRepeatRemindersSelectionDialog();
        } else if (viewId == R.id.tv_history_select) {
            lastHistorySelectedPosition = selectedPosition;
            showHistorySelectionDialog();
        }
    }

    private void clearAllStoredData() {

        mPillpopperActivity.stopService(new Intent(mPillpopperActivity, GetStateService.class));

        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mPillpopperActivity, FireBaseConstants.Event.CLEAR_ALL_STORED_DATA);

        //retaining the UniqueRandomIdForGA for notification GA
        String uniqueIdForNotificationGA = FrontController.getInstance(mContext).getUniqueRandomIdForGA(mContext);
        // KP only start
        _globalAppContext.kpClearSignon(getActivity());
        // KP only end
        // Clearing the ssoSessionid value
        _globalAppContext.kpClearSSOSessionId(getActivity());
        _globalAppContext.resetQuickviewShownFlg(getActivity());
        FingerprintUtils.resetAndPurgeKeyStore(getActivity());
        ActivationController.getInstance().clearWelcomeScreenDisplayCounter(getActivity());
        SharedPreferenceManager preferenceManager = SharedPreferenceManager.getInstance(getActivity(), AppConstants.AUTH_CODE_PREF_NAME);
        preferenceManager.putLong(AppConstants.WELCOME_SCREEN_DISPLAY_COUNTER, 0l, false);
        preferenceManager.putString(AppConstants.FDB_IMAGE_CARD_DISPLAY_CHOICE, "", true);
        preferenceManager.putLong(AppConstants.FDB_IMAGE_CARD_DISPLAY_COUNTER, 0l, true);
        preferenceManager.putBoolean("showTeenCard", false, false);
        preferenceManager.remove("CardIdSet");
        //restoring the UniqueRandomIdForGA
        preferenceManager.putString(AppConstants.KEY_GA_UNIQUE_RANDOM_VALUE, uniqueIdForNotificationGA, false);
        State.deletePersistentState(mPillpopperActivity);
        FrontController.getInstance(mPillpopperActivity).clearDatabase();
        RefillReminderController.getInstance(mPillpopperActivity).clearDBTable();
        RefillReminderNotificationUtil.getInstance(mPillpopperActivity).cancelAllPendingRefillReminders(mPillpopperActivity);
        RxRefillController.getInstance(mPillpopperActivity).clearContentAndConfigAPIFileData(mPillpopperActivity);
        Util.deleteRegionContactFile(getActivity());
        PillpopperRunTime.getInstance().setSelectedHomeFragment(HomeContainerActivity.NavigationHome.HOME);

        try {
            if (Util.deleteDirectory(Util.getImageCacheDir(mPillpopperActivity, false))) {
                PillpopperLog.say("Deleted the image directory after app reset.");
            } else {
                PillpopperLog.say("ERROR: App Reset: Unable to delete the image directory.");
            }
        } catch (IOException e) {
            PillpopperLog.say("Util -- deleteDirectory -- Unable to delete directory -- " , e);
        }

        // Not clearing AppProfile runtime data as per discussion

        //clearing the hasstatusupdate response on sign out.
        PillpopperRunTime.getInstance().setHasStatusUpdateResponseObj(null);

        //Clearing RX FDB Image data
        RxRefillController.getInstance(mPillpopperActivity).clearRxRefillData(mPillpopperActivity);
        RxRefillDBUtil.getInstance(mPillpopperActivity).resetPreferredPharmacyValue();

        if(ClearAllStoreDataController.clearAllDataInterface!=null){
            ClearAllStoreDataController.clearAllDataInterface.resetData();
        }
        LoggerUtils.info("CSD - clear stored data completed");
        System.exit(0);
    }

    private void showHistorySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mPillpopperActivity);
        builder.setTitle(mPillpopperActivity.getString(R.string.history)).setSingleChoiceItems(mShowHistoryItems, getNewHistoryPosition(), (dialog, which) -> selectedPosition = which).setPositiveButton(mPillpopperActivity.getResources().getString(R.string.ok_text), (dialog, which) -> {
            int previousSelectionDays = mFrontController.getDoseHistoryDays();
            int currentSelection = getNewShowHistorySelectedValue(selectedPosition);
            mShowHistoryCopyEdit.setText(mShowHistoryItems[selectedPosition]);

            try {
                if (previousSelectionDays != currentSelection) {
                    Util.firebaseEventForShowHistory(selectedPosition,mContext,FireBaseConstants.ParamValue.SETTINGS);
                    PillpopperRunTime.getInstance().setHistorySyncDone(false);
                    UIUtils.showProgressDialog(getActivity(), "Please wait...");
                    mFrontController.setDoseHistoryDaysForUser(currentSelection, mUserPreferences.getUserId());
                    if (mFrontController.isLogEntryAvailable()) {
                        PillpopperLog.say("Starting Intermediate Sync, Get State Events");
                        StateDownloadIntentService.startActionIntermediateGetState(getActivity());
                    }
                    StateDownloadIntentService.startActionGetHistoryEvents(getActivity());
                    StateDownloadIntentService.handleHistoryFailure(true);
                }
            } catch (Exception e) {
                PillpopperLog.exception("Exception in showHistorySelectionDialog method");
            }
        }).setNegativeButton(mPillpopperActivity.getResources().getString(R.string.cancel_text), (dialog, which) -> dialog.dismiss());
        builder.create();

        AlertDialog alert = builder.create();
        RunTimeData.getInstance().setAlertDialogInstance(alert);
        alert.show();

        Button btnPositive = alert.findViewById(android.R.id.button1);
        Button btnNegative = alert.findViewById(android.R.id.button2);

        btnPositive.setTextColor(Util.getColorWrapper(mContext, R.color.kp_theme_blue));
        btnNegative.setTextColor(Util.getColorWrapper(mContext, R.color.kp_theme_blue));
    }

    private void createLogEntryForSetPref(int currentSelection){
        JSONObject preferences = new JSONObject();
        try {
            preferences.put(PillpopperConstants.ACTION_SETTINGS_HISTORY_DAYS, getNewShowHistorySelectedValue(currentSelection));
            createLogEntryForSetPreferences(preferences);
        } catch (JSONException e) {
            LoggerUtils.exception(e.getMessage());
        }
    }

    private void updateUserPreferences(){
        if (selectedPosition != -1) {
            int currentSelection = getNewShowHistorySelectedValue(selectedPosition);
            if (mUserPreferences != null) {
                mShowHistoryCopyEdit.setText(mShowHistoryItems[selectedPosition]);
                mFrontController.setDoseHistoryDaysForUser(currentSelection, mUserPreferences.getUserId());
            } else {
                mFrontController.setDoseHistoryDaysForUser(currentSelection, mFrontController.getPrimaryUserIdIgnoreEnabled());
            }
            if (mFrontController.isLogEntryAvailable()) {
                StateDownloadIntentService.startActionIntermediateGetState(getActivity());
            }
        }
    }

    private void showRepeatRemindersSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mPillpopperActivity);
        builder.setTitle(mPillpopperActivity.getString(R.string.repeat_reminder_dialog_header)).setSingleChoiceItems(mRepeatReminderItems, getRemindersPosition(), (dialog, which) -> selectedPosition = which).setPositiveButton(mPillpopperActivity.getResources().getString(R.string.ok_text), (dialog, which) -> {
            if (mUserPreferences != null) {
                if (selectedPosition != 0) {
                    mRepeatReminders.setText("" + getRepeatReminderSelectedValue(selectedPosition) + " " + mPillpopperActivity.getResources().getString(R.string._minutes_small));
                    mFrontController.setRepeatReminderAfterSecForUser(getRepeatReminderSelectedValue(selectedPosition) * 60, mUserPreferences.getUserId());
                } else {
                    mRepeatReminders.setText(mPillpopperActivity.getResources().getString(R.string.never));
                    mFrontController.setRepeatReminderAfterSecForUser(-1, mUserPreferences.getUserId());
                }
            } /*else {
                mFrontController.setRepeatReminderAfterSecForUser(getRepeatReminderSelectedValue(selectedPosition) * 60, mFrontController.getPrimaryUserIdIgnoreEnabled());
            }*/

            JSONObject prefrences = new JSONObject();
            try {
                prefrences.put(PillpopperConstants.ACTION_SETTINGS_REPEAT_REMINDERS, getRepeatReminderSelectedValue(selectedPosition) != -1 ? getRepeatReminderSelectedValue(selectedPosition) * 60 : -1);
                createLogEntryForSetPreferences(prefrences);
            } catch (JSONException e) {
                PillpopperLog.say("Exception in shoeRepeatRemindersSelectionDialog method", e);
            }
        }).setNegativeButton(mPillpopperActivity.getResources().getString(R.string.cancel_text), (dialog, which) -> dialog.dismiss());
        builder.create();
        AlertDialog alert = builder.create();
        RunTimeData.getInstance().setAlertDialogInstance(alert);
        alert.show();

        Button btnPositive = alert.findViewById(android.R.id.button1);
        Button btnNegative = alert.findViewById(android.R.id.button2);

        btnPositive.setTextColor(Util.getColorWrapper(mContext, R.color.kp_theme_blue));
        btnNegative.setTextColor(Util.getColorWrapper(mContext, R.color.kp_theme_blue));
    }

    private int getNewShowHistorySelectedValue(int selectedPosition) {
        int selectedValue;
        switch (selectedPosition) {
            case 1:
                selectedValue = 30;
                break;
            case 2:
                selectedValue = 90;
                break;
            case 3:
                selectedValue = 365;
                break;
            case 4:
                selectedValue = 730;
                break;
            default:
                selectedValue = 14;
        }
        return selectedValue;
    }

    private int getHistoryPosition() {
        int doseHistoryDays = -1;
        if (mMembersList != null && !mMembersList.isEmpty()) {
            doseHistoryDays = mFrontController.getDoseHistoryDays();
            switch (doseHistoryDays) {
                case 1:
                    doseHistoryDays = 0;
                    break;
                case 14:
                    doseHistoryDays = 1;
                    break;
                case 30:
                    doseHistoryDays = 2;
                    break;
                case 90:
                    doseHistoryDays = 3;
                    break;
                case 365:
                    doseHistoryDays = 4;
                    break;
                case 730:
                    doseHistoryDays = 5;
                    break;
                default:
                    doseHistoryDays = 3;
            }
        } else {
            doseHistoryDays = 3;
        }
        selectedPosition = doseHistoryDays;
        return doseHistoryDays;
    }

    private int getNewHistoryPosition() {
        int doseHistoryDays = -1;
        if (mMembersList != null && !mMembersList.isEmpty()) {
            doseHistoryDays = mFrontController.getDoseHistoryDays();
            switch (doseHistoryDays) {
                case 30:
                    doseHistoryDays = 1;
                    break;
                case 90:
                    doseHistoryDays = 2;
                    break;
                case 365:
                    doseHistoryDays = 3;
                    break;
                case 730:
                    doseHistoryDays = 4;
                    break;
                default:
                    doseHistoryDays = 0;
            }
        } else {
            doseHistoryDays = 0;
        }
        selectedPosition = doseHistoryDays;
        return doseHistoryDays;
    }

    // if the history setting is 1 from 5.1 or below version, change the setting to 14 days
    private void checkAndUpdateDoseHistoryDays() {
        int doseHistoryDays = FrontController.getInstance(getActivity()).getDoseHistoryDays();
        if (doseHistoryDays == 1) {
            FrontController.getInstance(getActivity()).setDoseHistoryDaysForUser(14, FrontController.getInstance(getActivity()).getPrimaryUserIdIgnoreEnabled());
            createLogEntryForSetPref(0);
        }
    }

    private int getRepeatReminderSelectedValue(int position) {
        int selectedValue = -1;
        switch (position) {
            case 0:
                selectedValue = -1;
                break;
            case 1:
                selectedValue = 5;
                break;
            case 2:
                selectedValue = 10;
                break;
            case 3:
                selectedValue = 15;
                break;
            case 4:
                selectedValue = 30;
                break;
        }
        return selectedValue;
    }

    private int getRemindersPosition() {
        int position = -1;
        mUserPreferences = mFrontController.getUserPreferencesForUser(mMembersList.get(0).getUserId());
        if (mUserPreferences != null && mUserPreferences.getRepeatRemindersAfter() != null && !mUserPreferences.getRepeatRemindersAfter().equalsIgnoreCase("")) {
            switch (Integer.parseInt(mUserPreferences.getRepeatRemindersAfter())) {
                case 1800:
                    position = 4;
                    break;
                case 900:
                    position = 3;
                    break;
                case 600:
                    position = 2;
                    break;
                case 300:
                    position = 1;
                    break;
                case -1:
                    position = 0;
                    break;
                default:
                    position = 2; //default to 10 mins postion 2
            }
        } else {
            position = 2;
        }
        selectedPosition = position;
        return position;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case _REQ_SET_REMINDER_SOUND:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri == null) {
                        setReminderSound(State.REMINDER_SOUND_NONE);
                    } else if (uri.toString().equals(mDefaultNotificationSoundUri.toString())) {
                        setReminderSound(State.REMINDER_SOUND_DEFAULT);
                    } else {
                        setReminderSound(uri.toString());
                   /* if (PermissionUtils.checkVersionCode()) {
                        if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_WRITE_SETTINGS, Manifest.permission.WRITE_SETTINGS, getActivity())) {
                            RingtoneManager.setActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_NOTIFICATION, uri);
                        }
                    } else {
                        RingtoneManager.setActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_NOTIFICATION, uri);
                    }*/
                    }
                }
                break;
            case _REQ_SET_REMINDER_SOUND_0:
                Uri uri = NotificationBar_OverdueDose.getNotificationSound(getActivity());
                if (uri == null) {
                    setReminderSound(State.REMINDER_SOUND_NONE);
                } else if (uri.toString().equals(mDefaultNotificationSoundUri.toString())) {
                    setReminderSound(State.REMINDER_SOUND_DEFAULT);
                } else {
                    setReminderSound(uri.toString());
                }
                break;
            case INTENT_REQUEST_CODE_OPT_IN_FROM_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    startBiometricEnroll();
                }
                break;

        }
    }

    private void startBiometricEnroll() {
        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(getActivity(), AppConstants.AUTH_CODE_PREF_NAME);

        //init biometric
        KPSecurity.initBiometric(true, this, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                FingerprintUtils.setFingerprintSignInForUser(getActivity(), false);
                if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                    FingerprintUtils.showGlobalThresholdReachedMessage(getActivity(), okClickListener);
                }
                mFingerprintSignInSwitch.setChecked(false);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                FingerprintUtils.setFingerprintSignInForUser(getActivity(), true);
                boolean isFingerprintOptedIn = SharedPreferenceManager.getInstance(getActivity(), AppConstants.AUTH_CODE_PREF_NAME).getBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN, false);
                String password = KPSecurity.biometricDecryptPassword(sharedPreferenceManager.getString(AppConstants.USER_NAME, null),getActivity(), result);
                if(null != password) {
                    KPSecurity.biometricEncryptPassword("", password, getActivity(), result);
                }
                mIsAutomaticFingerprintSwitchToggle = true;
                mFingerprintSignInSwitch.setChecked(isFingerprintOptedIn);
                mIsAutomaticFingerprintSwitchToggle = false;
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

            try {
                KPSecurity.biometricAuthenticate(SharedPreferenceManager.getInstance(
                        mContext, AppConstants.AUTH_CODE_PREF_NAME).getString(AppConstants.USER_NAME, null));
            } catch (Exception ex) {
                FingerprintUtils.resetAndPurgeKeyStore(getActivity());
                Toast.makeText(getActivity(),"System settings modified", Toast.LENGTH_SHORT).show();
                PillpopperLog.say("Biometric Exception " + ex.getMessage());
                SharedPreferenceManager.getInstance(getActivity(), AppConstants.AUTH_CODE_PREF_NAME).
                        putBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN, false, true);
                mFingerprintSignInContainerLinearLayout.setVisibility(View.GONE);
            }
    }

    private final DialogInterface.OnClickListener okClickListener =
            (dialog, which) -> {
                dialog.dismiss();
                mFingerprintSignInSwitch.setChecked(false);
            };

    public String getReminderSound() {
        String reminderSoundName = null;
        if (mUserPreferences != null) {
            reminderSoundName = mUserPreferences.getAndroidReminderSoundFilename();
        }
        if (reminderSoundName == null) {
            return State.REMINDER_SOUND_DEFAULT;
        } else {
            return reminderSoundName;
        }
    }

    public void setReminderSound(String reminderSoundName) {
        if (reminderSoundName == null) {
            reminderSoundName = State.REMINDER_SOUND_DEFAULT;
        }
        PillpopperLog.say("Setting reminder sound: %s", reminderSoundName);
        updateReminderSoundInfo(reminderSoundName, true);
        if (mUserPreferences != null) {
            mFrontController.setNotificationSoundForUser(reminderSoundName, mUserPreferences.getUserId());
        } else {
            mFrontController.setNotificationSoundForUser(reminderSoundName, mFrontController.getPrimaryUserIdIgnoreEnabled());
        }
        RefillReminderNotificationUtil.getInstance(getActivity()).setNotificationUri(Uri.parse(reminderSoundName));
    }

    private void updateReminderSoundInfo(String reminderSoundName, boolean isRequireToUpdatetoServer) {
        //update Notification Sound Text
        String ringtoneFileName;
        if (State.REMINDER_SOUND_DEFAULT.equals(reminderSoundName) || reminderSoundName == null || reminderSoundName.equalsIgnoreCase("")) {
            mTxtNotificationSoundSelect.setText(getResources().getString(R.string.phone_default));
            ringtoneFileName = getResources().getString(R.string.phone_default);
        } else if (State.REMINDER_SOUND_NONE.equals(reminderSoundName)) {
            mTxtNotificationSoundSelect.setText(getResources().getString(R.string._none));
            ringtoneFileName = getResources().getString(R.string._none);
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, Uri.parse(reminderSoundName));
            if (ringtone == null) {
                mTxtNotificationSoundSelect.setText(getResources().getString(R.string._none));
                ringtoneFileName = getResources().getString(R.string._none);
            } else {
                if (null != ringtone.getTitle(mContext) /*&& android.text.TextUtils.isDigitsOnly(ringtone.getTitle(mContext))*/) {
                    mTxtNotificationSoundSelect.setText(ringtone.getTitle(mContext));
                    ringtoneFileName = ringtone.getTitle(mContext);
                } else {
                    mTxtNotificationSoundSelect.setText(getResources().getString(R.string.phone_default));
                    ringtoneFileName = getResources().getString(R.string.phone_default);
                }
            }
        }

        if (isRequireToUpdatetoServer) {
            JSONObject prefrences = new JSONObject();
            try {
                prefrences.put(PillpopperConstants.ACTION_SETTINGS_NOTIFICATION_FILE, ringtoneFileName);
                PillpopperLog.say("Notification path : name " + reminderSoundName + " Uri path : " + Uri.parse(reminderSoundName));
                prefrences.put(PillpopperConstants.ACTION_SETTINGS_ANDROID_REMINDER_FILE_NAME, Uri.parse(reminderSoundName));
                createLogEntryForSetPreferences(prefrences);
            } catch (JSONException e) {
                PillpopperLog.say("Exception in updateReminderSoundInfo method", e);
            }
        }
    }

    /**
     * Utility method to create the log entry with action = "SetPreferences"
     *
     * @param preferenceJSONObject JSONObject
     */
    private void createLogEntryForSetPreferences(JSONObject preferenceJSONObject) {
        try {
            preferenceJSONObject.put("userData", SharedPreferenceManager.getInstance(getActivity(),AppConstants.AUTH_CODE_PREF_NAME).getString(AppConstants.KP_GUID, null));
            createLogEntry(preferenceJSONObject);
        } catch (JSONException e) {
            PillpopperLog.say("Exception in updateReminderSoundInfo method", e);
        }
    }

    private String getActiveUserId() {
        String userId = mMembersList.get(0).getUserId();
        for (ManageMemberObj memberObj : mMembersList) {
            if (memberObj.getMedicationsEnabled().equalsIgnoreCase("Y")) {
                userId = memberObj.getUserId();
                break;
            }
        }
        return userId;
    }

    @Override
    public void onPause() {
        isActivityRestartCalled = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UIUtils.dismissProgressDialog();
    }

    private void initFingerprintSignInSwitch(View view) {

        if(RunTimeData.getInstance().isBiomerticChecked())
        {
            isActivityRestartCalled = true;
            RunTimeData.getInstance().setmFingerPrintTCInProgress(true);
            fingerPrintOptInFlowInProgress = true;
            FingerprintOptInStateListenerContainerActivity.startOptInFlow(SettingsBaseScreenFragment.this, INTENT_REQUEST_CODE_OPT_IN_FROM_SETTINGS);
        }

        mFingerprintSignInContainerLinearLayout = view.findViewById(R.id.settings_fingerprint_sign_in_container);
        if (FingerprintUtils.isDeviceEligibleForFingerprintOptIn(getActivity())) {
            mFingerprintSignInContainerLinearLayout.setVisibility(View.VISIBLE);

            boolean isFingerprintOptedIn = SharedPreferenceManager.getInstance(getActivity(), AppConstants.AUTH_CODE_PREF_NAME).getBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN, false);

            mFingerprintSignInSwitch = mFingerprintSignInContainerLinearLayout.findViewById(R.id.settings_fingerprint_sign_in_switch);
            mFingerprintSignInSwitch.setChecked(isFingerprintOptedIn);
            mFingerprintSignInSwitch.setOnClickListener(view1 -> {
                if(!mIsAutomaticFingerprintSwitchToggle
                        && !mIsSettingsScreenStarting) {
                    if (mFingerprintSignInSwitch.isChecked()) {
                        if(!isActivityRestartCalled) {
                            RunTimeData.getInstance().setBiomerticFinished(false);
                            RunTimeData.getInstance().setBiomerticChecked(true);
                            isActivityRestartCalled = true;
                        }

                        RunTimeData.getInstance().setmFingerPrintTCInProgress(true);
                        fingerPrintOptInFlowInProgress = true;
                        FingerprintOptInStateListenerContainerActivity.startOptInFlow(SettingsBaseScreenFragment.this, INTENT_REQUEST_CODE_OPT_IN_FROM_SETTINGS);
                    } else {
                        RunTimeData.getInstance().setBiomerticChecked(false);
                        showFingerprintSignInOffAlert();
                        FingerprintUtils.disableFingerprintFromSettings(getActivity());
                    }
                }
            });
        } else {
            SharedPreferenceManager.getInstance(getActivity(), AppConstants.AUTH_CODE_PREF_NAME).
                    putBoolean(AppConstants.KEY_SHARED_PREFS_FINGERPRINT_OPTED_IN, false, true);
            mFingerprintSignInContainerLinearLayout.setVisibility(View.GONE);
        }
    }

    private void showFingerprintSignInOffAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialog);
        builder.setMessage(getActivity().getResources().getString(R.string.dialog_fingerprint_sign_in_turned_off_message));
        builder.setPositiveButton(getActivity().getResources().getString(R.string._ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
