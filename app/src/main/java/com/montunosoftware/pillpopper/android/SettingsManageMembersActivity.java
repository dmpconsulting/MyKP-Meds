package com.montunosoftware.pillpopper.android;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.ManageMemberObj;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.service.SetUpProxyEnableService;
import org.kp.tpmg.mykpmeds.activation.service.SetUpProxyEnableService.SetUpProxyEnableResponseListener;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by M1024581 on 7/12/2016.
 */
public class SettingsManageMembersActivity extends StateListenerActivity implements SetUpProxyEnableResponseListener {

    private TextView mTxtMemberName;

    public static final String BUNDLE_CONSTANT_MEMBER_OBJ = "MemberObj";
    private Switch mMedEnabledSwitch;
    private Switch mRemEnabledSwitch;
    private RelativeLayout mRemindersLayout;
    private FrontController mFrontController;
    private ManageMemberObj mMemberObjFromSettings;
    private int mEnabledUsersCount;
    private TextView mLimitedAccess;
    private LinearLayout mSwitchContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_manage_members_layout);
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_MANAGE_MEMBERS);
        mFrontController = FrontController.getInstance(this);
        initActionBar();
        initUI();
        Bundle mBundle = getIntent().getExtras();
        mMemberObjFromSettings = (ManageMemberObj) mBundle.getSerializable(BUNDLE_CONSTANT_MEMBER_OBJ);
        loadData();
        setCheckChangeListenerForSwitch();
        List enabledUserList = mFrontController.getAllEnabledUsers();
        if (enabledUserList != null && !enabledUserList.isEmpty()) {
            mEnabledUsersCount = enabledUserList.size();
        } else {
            mEnabledUsersCount = 0;
        }
    }

    private void initActionBar() {
        Toolbar mToolbar = findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.manage_members));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (!(mMemberObjFromSettings.getTeen() && !Util.getTeenToggleEnabled())) {
            inflater.inflate(R.menu.menu_save, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_menu_item) {
            if (("N").equalsIgnoreCase(mMemberObjFromSettings.getMedicationsEnabled()) && !mMedEnabledSwitch.isChecked()) {
                finish();
            } else if (("Y").equalsIgnoreCase(mMemberObjFromSettings.getMedicationsEnabled()) && mMedEnabledSwitch.isChecked()) {
                saveDataToDB("Y", mRemEnabledSwitch.isChecked() ? "Y" : "N");
                finish();
            } else {
                saveDataToDB(mMedEnabledSwitch.isChecked() ? "Y" : "N",
                        mRemEnabledSwitch.isChecked() ? "Y" : "N");
                Intent intent = new Intent(this, LoadingActivity.class);
                SetUpProxyEnableService setUpProxyEnableService = new SetUpProxyEnableService(this, getEnabledUserId(), this);
                setUpProxyEnableService.execute(AppConstants.getPillSetProxyEnableURL());
                startActivityForResult(intent, 0);
            }
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<User> getEnabledUserId() {
        List<User> enabledUsers = mFrontController.getAllEnabledUsers();
        return enabledUsers;
    }

    private void saveDataToDB(String medicationsEnabled, String remindersEnabled) {
        mFrontController.updateMemberPreferencesToDB(
                mMemberObjFromSettings.getUserId(),
                medicationsEnabled != null ? medicationsEnabled : "N",
                remindersEnabled == null || ("Y").equalsIgnoreCase(remindersEnabled) ? "Y" : "N");
    }

    private void initUI() {
        mTxtMemberName = findViewById(R.id.txt_member_name);
        mMedEnabledSwitch = findViewById(R.id.switch_medications);
        mRemEnabledSwitch = findViewById(R.id.switch_reminders);
        mRemindersLayout = findViewById(R.id.rl_manage_reminders);
        mLimitedAccess = findViewById(R.id.limited_access);
        mSwitchContainer = findViewById(R.id.switchContainer);
    }

    private void setCheckChangeListenerForSwitch() {
        mMedEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mEnabledUsersCount++;
                mRemindersLayout.setVisibility(View.VISIBLE);
                mRemEnabledSwitch.setChecked(true);
                mRemEnabledSwitch.setOnCheckedChangeListener(((button, checked) -> {
                    FireBaseAnalyticsTracker.getInstance().logEvent(SettingsManageMembersActivity.this,
                            FireBaseConstants.Event.MANAGE_MEMBER_REMINDER_TOGGLE,
                            FireBaseConstants.ParamName.TOGGLE,
                            checked ? FireBaseConstants.ParamValue.ON : FireBaseConstants.ParamValue.OFF);
                }));
                FireBaseAnalyticsTracker.getInstance().logEvent(SettingsManageMembersActivity.this,
                        FireBaseConstants.Event.MANAGE_MEMBER_MED_TOGGLE,
                        FireBaseConstants.ParamName.TOGGLE,
                        FireBaseConstants.ParamValue.ON);
            } else {
                mEnabledUsersCount--;
                FireBaseAnalyticsTracker.getInstance().logEvent(SettingsManageMembersActivity.this,
                        FireBaseConstants.Event.MANAGE_MEMBER_MED_TOGGLE,
                        FireBaseConstants.ParamName.TOGGLE,
                        FireBaseConstants.ParamValue.OFF);

                if (mEnabledUsersCount != 0) {
                    mRemindersLayout.setVisibility(View.GONE);
                    mMedEnabledSwitch.setChecked(false);
                    mRemEnabledSwitch.setOnCheckedChangeListener(null);

                } else {
                    showLastEnabledUserErrorAlert();
                }
            }
        });
    }

    private boolean isLastEnabledUser() {
        List<User> enabledUsers = mFrontController.getAllEnabledUsers();
        return enabledUsers.size() <= 1;
    }

    private void showLastEnabledUserErrorAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.manage_med_error_alert_title));
        builder.setMessage(getString(R.string.manage_med_error_alert_message));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.ok_text), (dialog, which) -> {
            dialog.dismiss();
            revertMedicationAndReminderToggles();
        });

        AlertDialog alert = builder.create();
        if (!isFinishing()) {
            RunTimeData.getInstance().setAlertDialogInstance(alert);
            alert.show();
            // setting the dismiss listener on dialog instance.
            // to revert the toggle on in case of dismissing the Alert by Medication Alert/InApp Push Notification Alert.
            alert.setOnDismissListener(dialogInterface -> revertMedicationAndReminderToggles());
        }

        Button btnPositive = alert.findViewById(android.R.id.button1);
        Button btnNegative = alert.findViewById(android.R.id.button2);

        btnPositive.setTextColor(Util.getColorWrapper(this, R.color.kp_theme_blue));
        btnNegative.setTextColor(Util.getColorWrapper(this, R.color.kp_theme_blue));
    }

    private void revertMedicationAndReminderToggles() {
        boolean remindersChecked = false;
        if (mRemEnabledSwitch.isChecked()) {
            remindersChecked = true;
        }
        mMedEnabledSwitch.setChecked(true);
        if (!remindersChecked) {
            mRemEnabledSwitch.setChecked(false);
        }
    }

    private void loadData() {
        mTxtMemberName.setText(mMemberObjFromSettings.getUserFirstName());

        if (!"proxy".equalsIgnoreCase(mMemberObjFromSettings.getUserType())) {
            mLimitedAccess.setVisibility(View.GONE);
            showLayouts();
        } else {
            if (mMemberObjFromSettings.getTeen() && !Util.getTeenToggleEnabled()) {
                mLimitedAccess.setVisibility(View.VISIBLE);
                mSwitchContainer.setVisibility(View.GONE);
            } else {
                mSwitchContainer.setVisibility(View.VISIBLE);
                mLimitedAccess.setVisibility(View.GONE);
                showLayouts();
            }
        }

    }

    private void showLayouts() {
        if (("Y").equalsIgnoreCase(mMemberObjFromSettings.getMedicationsEnabled())) {
            mMedEnabledSwitch.setChecked(true);
            mRemindersLayout.setVisibility(View.VISIBLE);
            if (mMemberObjFromSettings.getRemindersEnabled() == null || ("Y").equalsIgnoreCase(mMemberObjFromSettings.getRemindersEnabled())) {
                mRemEnabledSwitch.setChecked(true);
            } else if (("N").equalsIgnoreCase(mMemberObjFromSettings.getRemindersEnabled())) {
                mRemEnabledSwitch.setChecked(false);
            }
        } else {
            mMedEnabledSwitch.setChecked(false);
            mRemindersLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSetUpProxyResponseReceived(int statusCode) {

        switch (statusCode) {
            case 0:
                RunTimeData.getInstance().setEnabledUsersList(mFrontController.getAllEnabledUsers());
                RunTimeData.getInstance().setSelectedUsersList(mFrontController.getEnabledUserIds());
                checkLogEntryAndInitiateAPICalls();
                break;
            case -1:
                finishActivity(0);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.unable_to_update));
                builder.setMessage(getString(R.string.no_internet_connection));
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.ok_text), (dialog, which) -> {
                    dialog.dismiss();
                    saveDataToDB(mMemberObjFromSettings.getMedicationsEnabled(), mMemberObjFromSettings.getRemindersEnabled());
                    loadData();
                });

                AlertDialog alert = builder.create();
                if (!isFinishing()) {
                    RunTimeData.getInstance().setAlertDialogInstance(alert);
                    alert.show();
                }

                Button btnPositive = alert.findViewById(android.R.id.button1);
                btnPositive.setTextColor(ActivationUtil.getColorWrapper(_thisActivity, R.color.kp_theme_blue));

                break;
        }
    }

    private void checkLogEntryAndInitiateAPICalls() {
        //registering broadcast receiver
        IntentFilter initialGetStateIntentFilter = new IntentFilter();
        initialGetStateIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        try {
            registerReceiver(mGetStateBroadcastReceiver, initialGetStateIntentFilter);
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        }
        //check for pending log entries
        StateDownloadIntentService.handleHistoryFailure(true);

        if (mFrontController.isLogEntryAvailable()) {
            PillpopperLog.say("Starting Intermediate Sync, ");
            StateDownloadIntentService.startActionIntermediateGetState(getBaseContext());
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(showDialogReveiver, new IntentFilter());
        PillpopperLog.say("Starting Get State and Get History Events");
        StateDownloadIntentService.startActionGetState(getBaseContext());
        StateDownloadIntentService.startActionGetHistoryEvents(getBaseContext());
        StateDownloadIntentService.startActionForDaylightSavingAdjustmentNeeded(getBaseContext());

    }

    private BroadcastReceiver showDialogReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != getActivity() && !getActivity().isFinishing()) {
                Util.showSessionexpireAlert(getActivity(), PillpopperAppContext.getGlobalAppContext(getActivity()));
            }
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(showDialogReveiver);
        }
    };

    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleServicesComplete();
        }
    };

    private void handleServicesComplete() {

        PillpopperLog.say("SettingsManageMembersActivity --- Services Completed..");
        unregisterReceiver(mGetStateBroadcastReceiver);

        insertEligiblePastRemindersToDB();

        PillpopperRunTime.getInstance().setHistorySyncDone(true);
        finishActivity(0);
        new Handler().post(() -> finish());
    }

    private void insertEligiblePastRemindersToDB() {
        LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersHashMapByUserId = PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId();

        List<Drug> allUsersDrugList = new ArrayList<>();
        if (null != passedRemindersHashMapByUserId && !passedRemindersHashMapByUserId.isEmpty()) {

            List<String> removedUsersID = new ArrayList<>();
            for (Map.Entry<String, LinkedHashMap<Long, List<Drug>>> _entry : passedRemindersHashMapByUserId.entrySet()) {
                LinkedHashMap<Long, List<Drug>> list = _entry.getValue();
                String userID = _entry.getKey();

                if (FrontController.getInstance(getActivity()).isEnabledUser(userID)) {
                    for (Map.Entry<Long, List<Drug>> entry : list.entrySet()) {
                        for (Drug d : entry.getValue()) {
                            if (FrontController.getInstance(getActivity()).isActiveDrug(d.getGuid(), new PillpopperTime(entry.getKey() / 1000))) {
                                allUsersDrugList.add(d);
                            }
                        }
                    }
                } else {
                    removedUsersID.add(userID);
                }
            }

            if (!removedUsersID.isEmpty()) {
                for (String userId : removedUsersID) {
                    passedRemindersHashMapByUserId.remove(userId);
                }
            }

            if (null != allUsersDrugList && !allUsersDrugList.isEmpty()) {
                Util.getInstance().insertPastRemindersPillIdsIntoDB(this, passedRemindersHashMapByUserId);
            }
        }
    }
}
