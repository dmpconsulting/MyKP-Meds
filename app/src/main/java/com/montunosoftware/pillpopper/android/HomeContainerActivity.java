package com.montunosoftware.pillpopper.android;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.LateRemindersActionInterface;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.home.CurrentReminderCard;
import com.montunosoftware.pillpopper.android.home.HomeFragment;
import com.montunosoftware.pillpopper.android.home.LateRemindersHomeCard;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.refillreminder.views.RefillRemindersHomeContainerActivity;
import com.montunosoftware.pillpopper.android.refillreminder.views.RefillRemindersListFragment;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.CalendarWeekFragmentNew;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.EditScheduleRunTimeData;
import com.montunosoftware.pillpopper.android.view.NavDrawerListAdapter;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.kotlin.calendarviewpager.HistoryCalendarFragment;
import com.montunosoftware.pillpopper.kotlin.calendarviewpager.HistoryOverlayDialogFragment;
import com.montunosoftware.pillpopper.kotlin.history.HistoryBaseScreen;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.NavDrawerItem;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.SessionAliveService;
import com.montunosoftware.pillpopper.model.State;
import com.montunosoftware.pillpopper.model.StateUpdatedListener;
import com.montunosoftware.pillpopper.model.TTGSecureWebViewModel;
import com.montunosoftware.pillpopper.model.TTGWebviewModel;
import com.montunosoftware.pillpopper.service.LogEntryUpdateAsyncTask;
import com.montunosoftware.pillpopper.service.TokenService;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;
import com.montunosoftware.pillpopper.service.images.sync.ImageSynchronizerService;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.RefillReminderInterface;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.activity.LoginActivity;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.AppData;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.service.SetUpProxyEnableService;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.RxRefillConstants;
import org.kp.tpmg.ttg.controller.RxFrontController;
import org.kp.tpmg.ttg.database.RxRefillDBHandler;
import org.kp.tpmg.ttg.database.SupportRefillDatabaseHelper;
import org.kp.tpmg.ttg.presenter.RxKpLocationPresentor;
import org.kp.tpmg.ttg.presenter.RxLocationPresentorCallback;
import org.kp.tpmg.ttg.utils.RxRefillUtils;
import org.kp.tpmg.ttg.views.RxRefillPrescriptionsListFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeContainerActivity extends StateListenerActivity implements OnClickListener, StateUpdatedListener,
        CalendarWeekFragmentNew.OnDateSelectedListener,
        DrugListRecyclerDBFragment.RefillLauncherInterface,
        HomeFragment.ChangeNavigation,
        RxLocationPresentorCallback,
        HomeFragment.onQuickActionButtonClickListener, PharmacyLocatorFragment.onPharmacyListItemClickListener,
        RefillReminderInterface, RefillRemindersListFragment.CreateRefillListenerInterface, ScheduleWizardFragment.OnAddMedicationClicked, ReminderTimeFragment.SaveScheduleListener, SetUpProxyEnableService.SetUpProxyEnableResponseListener,
        ScheduleWizardFragment.SaveReminderTimeFragmentInterface{

    private CharSequence mDrawerTitle;

    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<NavDrawerItem> mDrawerItems;
    private SharedPreferenceManager mSharedPrefManager;
    private AlertDialog dialogRefill;
    private int previousNavigationDrawerItem = -1;

    //timestamp for bulk reminders setup.
    private long lastExecuted = System.currentTimeMillis();

    private ScheduleFragmentNew mScheduleFragmentInstance;
    private final List<Drug> drugListForQuickview = new ArrayList<>();
    private boolean isRefillFragmentLoaded;
    private LateRemindersActionInterface mLateRemindersActionInterface;
    private RxFrontController mFrontController;
    private boolean isOnPause;
    private boolean isTransactionPending;
    private boolean isNavigationPending;
    private EditScheduleRunTimeData scheduleData = new EditScheduleRunTimeData();
    private final BroadcastReceiver refreshKPHCCardsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //refreshHomeCards();
            if (getSupportFragmentManager().getFragments().size() > 0) {
                Fragment fragment = getSupportFragmentManager().getFragments().get(0);
                if (fragment instanceof HomeFragment) {
                    ((HomeFragment) fragment).refreshHomeCards();
                }
            }
        }

    };
    private final BroadcastReceiver openNewTab = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String destination = intent.getStringExtra("destination_name");
                if (null != destination) {
                    destinationTab = destination;
                    new Handler().post(() -> {
                        if (!isOnPause) {
                            screenNavigation(destination);
                            destinationTab = null;
                        } else {
                            isNavigationPending = true;
                        }
                    });
                }
            }


        }

    };

    private final BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // when the teen user has enabled true we need to make a SetUpProxyEnableService API.
            if (mSharedPrefManager.getBoolean("showTeenCard", false) && !isSetupProxyEnabledAPICalled) {
                isSetupProxyEnabledAPICalled = true;
                Intent loadingIntent = new Intent(HomeContainerActivity.this, LoadingActivity.class);
                SetUpProxyEnableService setUpProxyEnableService = new SetUpProxyEnableService(context, getEnabledUserId(), HomeContainerActivity.this);
                setUpProxyEnableService.execute(AppConstants.getPillSetProxyEnableURL());
                startActivityForResult(loadingIntent, 0);
            }
            handleGetStateBroadcast();
        }
    };

    private final boolean scheduleTakeNowPopUpShown = false;
    private PopupMenu schedulePopupMenu;
    private ArrayList<Integer> requestCodes;
    private boolean loadingFromConfigChanges = false;
    private DrawerLayout drawerLayout;
    private PillpopperActivity mContext;
    private static final String TAG_FRAGMENT = "TAG_FRAGMENT";
    private boolean isSetupProxyEnabledAPICalled = false;
    private String destinationTab;
    private ReminderTimeFragment mReminderTimeFragment;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_out) {
            callSignout();
        }
    }

    @Override
    public void changeNavigation(int pos) {
        setRegularToolbar();
        selectItem(pos);
    }

    @Override
    public void onDbUpdated() {
        runOnUiThread(() -> {
            dismissLoadingActivity();
            Util.getInstance().loadValuesForRxRefillActivity(getActivity());
            launchRxRefillLocatorFragment();
        });
    }

    @Override
    public void onFindAPharmacyQuickActionClicked() {
        previousNavigationDrawerItem = NavigationHome.FIND_PHARMACY.getPosition();
        PillpopperRunTime.getInstance().setSelectedHomeFragment(NavigationHome.FIND_PHARMACY);
        launchFindPharmacyLocator();
        isRefillFragmentLoaded = false;
    }

    @Override
    public void onGuideQuickActionClicked() {
        setRegularToolbar();
        selectItem(NavigationHome.GUIDE.getPosition());
        isRefillFragmentLoaded = false;
    }

    @Override
    public void onCreateARefillQuickActionClicked() {
        Intent intent = new Intent(this, RefillRemindersHomeContainerActivity.class);
        // getActivity() is required for its working, otherwise the request code will be changed in onActivityResult
        getActivity().startActivityForResult(intent, PillpopperConstants.REQUEST_QUICK_ACCESS_CREATE_REFILL_REMINDER);
    }

    @Override
    public void onSetUpReminderQuickActionClicked() {
        setRegularToolbar();
        selectItem(NavigationHome.MEDICATION_REMINDERS.getPosition());
        getSupportActionBar().setTitle(getResources().getString(R.string.create_schedule));
    //    RunTimeData.getInstance().setSpinnerPosition(0);
     //   installFragment(new ScheduleWizardFragment());
    }

    @Override
    public void onRxRefillMedicationsQuickActionClicked() {
        loadRxRefill();
    }

    @Override
    public void onPharmacyItemClicked(Bundle extras) {
        Intent intent = new Intent(getActivity(), RxRefillHomeContainerActivity.class);
        intent.putExtra("launchPharmacyLocatorDetails", true);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public void onCreateRefillReminderClicked() {
        startActivity(new Intent(this, RefillRemindersHomeContainerActivity.class));
    }

    private void callKeepAlive() {
        try {
            LoggerUtils.info("Debug ---- calling keep alive -- ");
            SessionAliveService.startSessionAliveService(getApplicationContext());
        } catch (Exception e) {
            LoggerUtils.exception("Error in calling keep alive : " + e.getMessage());
        }
    }

    @Override
    public void onRefillReminderItemClicked(RefillReminder mSelectedRefillReminder) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedRefillReminder", mSelectedRefillReminder);
        Intent intent = new Intent(this, RefillRemindersHomeContainerActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void addLogEntryForRefillReminderUpdate(JSONObject obj) {
        prepareAndAddLogEntryModel(obj);
        selectItem(NavigationHome.REFILL_REMINDER.getPosition());
    }

    @Override
    public void addLogEntryForRefillReminderDelete(JSONObject obj) {
        prepareAndAddLogEntryModel(obj);
        selectItem(NavigationHome.REFILL_REMINDER.getPosition());
    }

    private void prepareAndAddLogEntryModel(JSONObject obj) {
        String replayId = obj.optJSONObject("pillpopperRequest").optString("replayId");
        if (null != replayId && replayId.length() > 0) {
            LogEntryModel logEntryModel = new LogEntryModel();
            logEntryModel.setDateAdded(System.currentTimeMillis());
            logEntryModel.setReplyID(replayId);
            logEntryModel.setEntryJSONObject(obj, this);
            FrontController.getInstance(this).addLogEntry(this, logEntryModel);
        }
    }

    @Override
    public void onAddMedicationClicked(Bundle data) {
        Intent intent = new Intent(this, AddMedicationsForScheduleActivity.class);
        intent.putExtras(data);
        getActivity().startActivity(intent); // called within fragment
    }

    @Override
    public void onSaveScheduleClicked() {
        if(!isDuplicateExecution()) {
            LoggerUtils.info("Bulk reminders  onSaveScheduleClicked");
            lastExecuted = System.currentTimeMillis();
            if (RunTimeData.getInstance().isRestored()) {
                setRegularToolbar();
                selectItem(NavigationHome.MEDICATIONS.getPosition());
                return;
            }
            if (RunTimeData.getInstance().isUserSelected()) {
                setRegularToolbar();
                previousNavigationDrawerItem = RunTimeData.getInstance().getHomeNavPosition();
                selectItem(NavigationHome.MEDICATION_REMINDERS.getPosition());
            } else {
                if (!RunTimeData.getInstance().isUserSelected()
                        && !RunTimeData.getInstance().isScheduleEdited()) {
                    int position = RunTimeData.getInstance().getLastSelectedFragmentPosition();
                    if(position != NavigationHome.MEDICATION_REMINDERS.getPosition()) {
                        final NavigationHome selectedHomeFragment = NavigationHome.values()[position];
                        handleNavigationItemClick(selectedHomeFragment, position);
                    } else{
                        setHomeScreenToolbar();
                        selectItem(NavigationHome.HOME.getPosition());
                    }
                }
            }
        }
    }

    private boolean isDuplicateExecution() {
        return !(System.currentTimeMillis() - lastExecuted > 3000);
    }

    public enum NavigationHome {
        HOME(0, R.string.home),
        MEDICATIONS(1, R.string.medications),
        DAILY_SCHEDULE(2, R.string.daily_schedule),
        MEDICATION_REMINDERS(3, R.string.medication_reminders),
        REFILL_REMINDER(4, R.string.refill_reminder),
        PRESCRIPTION_REFILLS(5, R.string.prescription_refills),
        HISTORY(6, R.string.history),
        EMPTY(7, R.string.empty),
        FIND_PHARMACY(8, R.string.button_find_pharmacy),
        SETTINGS(9, R.string.settings),
        ARCHIVE(10, R.string.archive_),
        GUIDE(11, R.string.guide),
        SUPPORT(12, R.string.support);
        private final int _position;
        private final int _stringResId;

        NavigationHome(int position, int resourceId) {
            this._position = position;
            this._stringResId = resourceId;
        }

        public int getPosition() {
            return _position;
        }

    }

    private Toolbar mToolbar;
    private Toolbar mToolbarHome;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (AutoSignInSplashActivity.isAutoSignInInProgress) {
            LoggerUtils.info("Debug ---  reached Home container activity before auto sigh in complete");
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_container_new);
        mFrontController = RxFrontController.getInstance(getApplicationContext());
        mContext = (PillpopperActivity) getActivity();
        Button mSignout = findViewById(R.id.sign_out);
        mSignout.setOnClickListener(this);

        mSharedPrefManager = SharedPreferenceManager.getInstance(this, AppConstants.AUTH_CODE_PREF_NAME);

        Util.storeEnvironment(HomeContainerActivity.this);
        initNavDrawerUI();
        initActionBar();
        buildRequestCode();

        clearDBOnUserSwitch(this);
        mSharedPrefManager = SharedPreferenceManager.getInstance(this, AppConstants.AUTH_CODE_PREF_NAME);
        setActionBarTitleAndNavDrawerSelectedItem(PillpopperRunTime.getInstance().getSelectedHomeFragment()._position);

        getOverDueDrugCountFromDB();

        getState().setScheduleViewDay(PillpopperDay.today());
        if (null != RunTimeData.getInstance().getPillIdList() && !RunTimeData.getInstance().getPillIdList().isEmpty()) {
            for (String pillId : RunTimeData.getInstance().getPillIdList()) {
                addLogEntryForEdit(FrontController.getInstance(_thisActivity).getDrugByPillId(pillId), _thisActivity);
            }
        }
        ActivationController.getInstance().setRefillScreenChoice(this, true);
        selectItem(NavigationHome.HOME.getPosition());

        LocalBroadcastManager.getInstance(this).registerReceiver(refreshKPHCCardsReceiver,
                new IntentFilter("REFRESH_KPHC_CARDS"));
        /*LocalBroadcastManager.getInstance(this).registerReceiver(refreshLateReminderCardsReceiver,
                new IntentFilter(PillpopperConstants.LATE_REMINDER_FIRST_TIME_REFRESH));*/

        if (RunTimeData.getInstance().getLastSelectedFragmentPosition() != -1) {
            loadingFromConfigChanges = true;
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(openNewTab,
                new IntentFilter("TO_OPEN_TAB"));

        // calling keep alive to fix the rx refill cookie issue.
        callKeepAlive();
    }

    private void addLogEntryForEdit(Drug drug, PillpopperActivity pillpopperActivity) {
        try {
            LogEntryUpdateAsyncTask logEntryUpdateAsyncTask = new LogEntryUpdateAsyncTask(pillpopperActivity, "EditPill", drug);
            logEntryUpdateAsyncTask.execute();
        } catch (Exception e) {
            PillpopperLog.say("Exception while adding log entry for edit ", e);
        }
    }


    private void buildRequestCode() {
        requestCodes = new ArrayList<>();
        requestCodes.add(-1);
        requestCodes.add(100);
        requestCodes.add(0);
        requestCodes.add(101);
        requestCodes.add(1);
        requestCodes.add(PillpopperConstants.REQUEST_REFILL_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.REQUEST_SETUP_REMINDER_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.REQUEST_SETUP_VIEW_MEDS_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.REQUEST_SETUP_MANAGE_MEMBERS_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.REQUEST_NEW_KPHC_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.REQUEST_UPDATED_KPHC_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.REQUEST_LATE_REMINDER_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.REQUEST_VIEW_REMINDER_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.REQUEST_QUICK_ACCESS_MENU_SETUP_REMINDERS);
        requestCodes.add(PillpopperConstants.REQUEST_QUICK_ACCESS_CREATE_REFILL_REMINDER);
        requestCodes.add(PillpopperConstants.REQUEST_SAVE_SCHEDULE);
        requestCodes.add(PillpopperConstants.TEEN_PROXY_HOME_CARD_DETAIL);
        requestCodes.add(PillpopperConstants.GENERIC_HOME_CARD_DETAIL);
    }

    /**
     * Clear All the tables data from the database, when ever user switch detects.
     */
    private void clearDBOnUserSwitch(Context context) {
        ActivationController activationController = new ActivationController();
        if (activationController.isDataResetFl(context)) {
            FrontController.getInstance(_thisActivity).clearDatabase();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter getStateIntentFilter = new IntentFilter();
        getStateIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        try {
            registerReceiver(mGetStateBroadcastReceiver, getStateIntentFilter);
        } catch (Exception e) {
            PillpopperLog.say(e);
        }
        IntentFilter registerDaylightSavingChangeListener = new IntentFilter();
        registerDaylightSavingChangeListener.addAction(StateDownloadIntentService.BROADCAST_DAY_LIGHT_SAVING_ADJUSTMENT_DONE);
        try {
            registerReceiver(registrationRemoveDaylightReceiver, registerDaylightSavingChangeListener);
        } catch (Exception e) {
            PillpopperLog.say(e);
        }
        RunTimeData.getInstance().setHomeContainerActivityLaunched(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        getState().unregisterStateUpdatedListener(this);
        unregisterReceiver(mGetStateBroadcastReceiver);
        unregisterReceiver(registrationRemoveDaylightReceiver);
        /*LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshLateReminderCardsReceiver);*/
    }


    BroadcastReceiver registrationRemoveDaylightReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PillpopperLog.say("---------Day light Saving Adjustment Completed---------");
            // mScheduleFragmentInstance._updateView();
            new Handler().postDelayed(() -> finishActivity(0), 500);

            refreshReminder();

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    try {
                        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    } catch (Exception e) {
                        LoggerUtils.exception("exception while fragment onRequestPermissionsResult");
                    }
                }
            }
        }
    }


    public void onDateSelected(PillpopperDay selectedDay) {
        if (null != RunTimeData.getInstance().getFocusDay() && !selectedDay.equals(RunTimeData.getInstance().getFocusDay())) {
            getState().setScheduleViewDay(selectedDay);
            mScheduleFragmentInstance.updateView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int color = Color.parseColor("#FFFFFF");
        final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        for (int i = 0; i < mToolbar.getChildCount(); i++) {
            final View v = mToolbar.getChildAt(i);

            if (v instanceof ImageButton) {
                ((ImageButton) v).setColorFilter(colorFilter);
            }
        }

        int colorHome = Color.parseColor("#0569A5");
        final PorterDuffColorFilter colorFilterHome = new PorterDuffColorFilter(colorHome, PorterDuff.Mode.SRC_ATOP);

        for (int i = 0; i < mToolbarHome.getChildCount(); i++) {
            final View v = mToolbarHome.getChildAt(i);

            if (v instanceof ImageButton) {
                ((ImageButton) v).setColorFilter(colorFilterHome);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void initActionBar() {
        mToolbar = findViewById(R.id.app_bar);
        mToolbarHome = findViewById(R.id.app_bar_home);
        setSupportActionBar(mToolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Util.hideKeyboard(HomeContainerActivity.this, drawerLayout);
                if (!getActivity().getResources().getString(R.string.schedule_wizard).equalsIgnoreCase(mDrawerTitle.toString()) && getResources().getString(R.string.prescription_refills) != mDrawerTitle) {
                    getSupportActionBar().setTitle(mDrawerTitle);
                }
                mDrawerToggle.syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!getActivity().getResources().getString(R.string.schedule_wizard).equalsIgnoreCase(mDrawerTitle.toString()) && getResources().getString(R.string.prescription_refills) != mDrawerTitle) {
                    getSupportActionBar().setTitle(mDrawerTitle);
                }
                mDrawerToggle.syncState();
            }
        };
        drawerLayout.addDrawerListener(mDrawerToggle);
        setHomeScreenToolbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.NavDrawerUtils.closeNavigationDrawerIfOpen();
        switch (item.getItemId()) {
            case android.R.id.home:
                RxRefillUtils.hideSoftKeyboard(this);
                handleBackNavigation();
                break;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleBackNavigation() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mDrawerTitle = title;
        if (!getActivity().getResources().getString(R.string.refill_reminder).equalsIgnoreCase(mDrawerTitle.toString()) &&
                !getActivity().getResources().getString(R.string.button_find_pharmacy).equalsIgnoreCase(mDrawerTitle.toString()) &&
                !getActivity().getResources().getString(R.string.schedule_wizard).equalsIgnoreCase(mDrawerTitle.toString()) &&
                getResources().getString(R.string.prescription_refills) != mDrawerTitle)
            getSupportActionBar().setTitle(title);
    }

    private void initNavDrawerUI() {
        mDrawerList = findViewById(R.id.lstdrawer);
        mDrawerList.setAdapter(new NavDrawerListAdapter(this, getNavDrawerItems()));
        mDrawerList.setOnItemClickListener(new NavDrawerItemClickListener());
        mDrawerList.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                selectItem(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }


    private ArrayList<NavDrawerItem> getNavDrawerItems() {
        mDrawerItems = new ArrayList<>();
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.HOME.getPosition()),
                getString(NavigationHome.HOME._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.MEDICATIONS.getPosition()),
                getString(NavigationHome.MEDICATIONS._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.DAILY_SCHEDULE.getPosition()),
                getString(NavigationHome.DAILY_SCHEDULE._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.MEDICATION_REMINDERS.getPosition()),
                getString(NavigationHome.MEDICATION_REMINDERS._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.REFILL_REMINDER.getPosition()),
                getString(NavigationHome.REFILL_REMINDER._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.PRESCRIPTION_REFILLS.getPosition()),
                getString(NavigationHome.PRESCRIPTION_REFILLS._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.HISTORY.getPosition()),
                getString(NavigationHome.HISTORY._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.EMPTY.getPosition()),
                getString(NavigationHome.EMPTY._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.FIND_PHARMACY.getPosition()),
                getString(NavigationHome.FIND_PHARMACY._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.SETTINGS.getPosition()),
                getString(NavigationHome.SETTINGS._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.ARCHIVE.getPosition()),
                getString(NavigationHome.ARCHIVE._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.GUIDE.getPosition()),
                getString(NavigationHome.GUIDE._stringResId)));
        mDrawerItems.add(new NavDrawerItem(getNavIcon(NavigationHome.SUPPORT.getPosition()),
                getString(NavigationHome.SUPPORT._stringResId)));
        return mDrawerItems;
    }


    private int getNavIcon(int position) {
        NavigationHome selectedHomeFragment = NavigationHome.values()[position];
        int drawableIcon = R.drawable.drug_reminder_on;
        switch (selectedHomeFragment) {
            case HOME:
                drawableIcon = R.drawable.navigation_home;
                break;
            case MEDICATIONS:
                drawableIcon = R.drawable.navigation_medication;
                break;

            case DAILY_SCHEDULE:
                drawableIcon = R.drawable.navigation_calendar;
                break;

            case REFILL_REMINDER:
                drawableIcon = R.drawable.navigation_checkmark;
                break;

            case MEDICATION_REMINDERS:
                drawableIcon = R.drawable.navigation_schedule;
                break;

            case HISTORY:
                drawableIcon = R.drawable.navigation_history;
                break;

            case PRESCRIPTION_REFILLS:
                drawableIcon = R.drawable.navigation_refill;
                break;

            case SETTINGS:
                drawableIcon = R.drawable.white_color_drawable;
                break;

            case ARCHIVE:
                drawableIcon = R.drawable.white_color_drawable;
                break;

            case GUIDE:
                drawableIcon = R.drawable.white_color_drawable;
                break;

            case FIND_PHARMACY:
                drawableIcon = R.drawable.white_color_drawable;
                break;

            case SUPPORT:
                drawableIcon = R.drawable.white_color_drawable;
                break;


            case EMPTY:
                drawableIcon = R.drawable.transparent_color_drawable;
                break;
        }

        return drawableIcon;
    }

    public void selectItem(int position) {

        PillpopperConstants.setCanShowMedicationList(false);
        if (null != RunTimeData.getInstance().getScheduleData()) {
            RunTimeData.getInstance().setScheduleData(null);
        }
        if(NavigationHome.MEDICATION_REMINDERS._position != position){
            RunTimeData.getInstance().setHomeNavPosition(position);
        }
        if (previousNavigationDrawerItem == -1 ||
                position != previousNavigationDrawerItem) {

            previousNavigationDrawerItem = position;
            if (position != NavigationHome.FIND_PHARMACY.getPosition()) {
                setActionBarTitleAndNavDrawerSelectedItem(position);
            }
            NavigationHome selectedHomeFragment = NavigationHome.values()[position];
            switch (selectedHomeFragment) {
                case MEDICATIONS:
                    installFragment(new DrugListRecyclerDBFragment());
                    break;

                case DAILY_SCHEDULE:
                    mScheduleFragmentInstance = new ScheduleFragmentNew();
                    PillpopperConstants.setCanShowMedicationList(false);
                    installFragment(mScheduleFragmentInstance, new ScheduleCalendarFragmentNew());
                    break;

                case REFILL_REMINDER:
                    launchRefillReminder();
                    break;

                case MEDICATION_REMINDERS:
                    // if user was changed during bulk schedule flow,
                    // don't reset the position to primary member
                    if (!RunTimeData.getInstance().isUserSelected()) {
                        RunTimeData.getInstance().setSpinnerPosition(0);
                    }
                    RunTimeData.getInstance().setLaunchSource(AppConstants.BULK_MEDICATION);
                    prepareScheduleData();
                    installFragment(new ScheduleWizardFragment());
                    break;

                case HISTORY:
                    installFragment(new HistoryBaseScreen());
                    break;

                case ARCHIVE:
                    installFragment(new ArchiveFragmentRedesign());
                    break;

                case PRESCRIPTION_REFILLS:
                    break;

                case SETTINGS:
                    installFragment(new SettingsBaseScreenFragment());
                    break;

                case GUIDE:
                    launchGuide();
                    break;

                case FIND_PHARMACY:
                    launchFindPharmacyLocator();
                    break;

                case SUPPORT:
                    installFragment(new SupportFragmenBaseScreen());
                    break;

                case HOME:
                    if (!isOnPause) {
                        installFragment(new HomeFragment());
                    } else {
                        isTransactionPending = true;
                    }
                    break;

                case EMPTY:
                    break;
            }
            // _thisActivity.getState().setSelectedHomeFragment(selectedHomeFragment);
            /*if (selectedHomeFragment != NavigationHome.PRESCRIPTION_REFILLS) {
                PillpopperRunTime.getInstance().setWebViewInstance(null);
            }*/
            PillpopperRunTime.getInstance().setSelectedHomeFragment(selectedHomeFragment);
        }
    }

    private void handleNavigationItemClick(NavigationHome selectedHomeFragment, int position) {
        if (selectedHomeFragment == NavigationHome.EMPTY) {
            setActionBarTitleAndNavDrawerSelectedItem(PillpopperRunTime.getInstance().getSelectedHomeFragment()._position);
            isRefillFragmentLoaded = false;
            RunTimeData.getInstance().setLastSelectedFragmentPosition(NavigationHome.EMPTY.getPosition());
        } else if (selectedHomeFragment == NavigationHome.PRESCRIPTION_REFILLS) {
            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                    FireBaseConstants.Event.REFILL_MEDS,
                    FireBaseConstants.ParamName.SOURCE,
                    FireBaseConstants.ParamValue.NAVIGATION_MENU);
            loadRxRefill();
        } else if (selectedHomeFragment == NavigationHome.HOME) {
            setActionBarTitleAndNavDrawerSelectedItem(PillpopperRunTime.getInstance().getSelectedHomeFragment()._position);
            setHomeScreenToolbar();
            selectItem(position);
        } else if (selectedHomeFragment == NavigationHome.MEDICATION_REMINDERS) {
            FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                    FireBaseConstants.Event.BULK_REMINDER_ADD,
                    FireBaseConstants.ParamName.SOURCE,
                    FireBaseConstants.ParamValue.NAVIGATION_MENU);
            setRegularToolbar();
            selectItem(NavigationHome.MEDICATION_REMINDERS.getPosition());
        } else {
            if (selectedHomeFragment == NavigationHome.FIND_PHARMACY) {
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.FIND_PHARMACY,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.NAVIGATION_MENU);
            }

            if (selectedHomeFragment != NavigationHome.FIND_PHARMACY) {
                setRegularToolbar();
            }
            selectItem(position);
            isRefillFragmentLoaded = false;
        }
    }

    private void loadRxRefill() {
        boolean externalBrowserAEMSwitch = Boolean.parseBoolean(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.EXTERNAL_AEM_BROWSER_SWITCH));
        if (!ActivationController.getInstance().getRefillScreenChoice(HomeContainerActivity.this)) {
            setActionBarTitleAndNavDrawerSelectedItem(getState().getSelectedHomeFragment()._position);
            if (!AppConstants.IS_NATIVE_RX_REFILL_REQUIRED) {
                if (!externalBrowserAEMSwitch) {
                    refillFragment();
                } else {
                    setActionBarTitleAndNavDrawerSelectedItem(previousNavigationDrawerItem);
                    loadRxRefillInBrowser();
                }
            } else {
                launchRxRefillPrescriptionListFragment();
            }
            selectItem(NavigationHome.PRESCRIPTION_REFILLS.getPosition());
        } else {
            if (previousNavigationDrawerItem == -1 ||
                    NavigationHome.PRESCRIPTION_REFILLS.getPosition() != previousNavigationDrawerItem) {
                if (!AppConstants.IS_NATIVE_RX_REFILL_REQUIRED) { // for prescription refill
                    setActionBarTitleAndNavDrawerSelectedItem(getState().getSelectedHomeFragment()._position);
                } else {
                    setActionBarTitleAndNavDrawerSelectedItem(previousNavigationDrawerItem);
                }
                if (!AppConstants.IS_NATIVE_RX_REFILL_REQUIRED) {
                    if (!externalBrowserAEMSwitch) {
                        refillAlertDailog(_thisActivity);
                        isRefillFragmentLoaded = false;
                    } else {
                        setActionBarTitleAndNavDrawerSelectedItem(previousNavigationDrawerItem);
                        loadRxRefillInBrowser();
                    }
                } else {
                    launchRxRefillPrescriptionListFragment();
                }
            }
        }
    }

    private void loadRxRefillInBrowser() {
        String aemRefillPrescriptionUrl = Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_AEM_REFILL_PRESCRIPTION_URL_KEY);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(aemRefillPrescriptionUrl));
        startActivity(browserIntent);
    }

    private List<User> getEnabledUserId() {
        List<User> enabledUsers = FrontController.getInstance(this).getAllEnabledUsers();
        return enabledUsers;
    }

    private void launchRxRefillPrescriptionListFragment() {
        isRefillFragmentLoaded = true;
        mToolbar.setVisibility(View.GONE);
        mToolbarHome.setVisibility(View.GONE);
        RefillRuntimeData.getInstance().setDrawerLayout(drawerLayout);
        Util.getInstance().loadValuesForRxRefillActivity(this);
        RefillRuntimeData.getInstance().setRxRefillUsersList(FrontController.getInstance(this).getRxRefillUsersList());
        installFragment(new RxRefillPrescriptionsListFragment());
        selectItem(NavigationHome.PRESCRIPTION_REFILLS.getPosition());
    }

    private void setRegularToolbar() {
        if (null != RefillRuntimeData.getInstance().getmRxToolBar()) {
            RefillRuntimeData.getInstance().getmRxToolBar().setVisibility(View.GONE);
            RefillRuntimeData.getInstance().setmRxToolBar(null);
        }
        mToolbar.setVisibility(View.VISIBLE);
        mToolbarHome.setVisibility(View.GONE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawable_hamburger_vector_white);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }


    private void setActionBarTitleAndNavDrawerSelectedItem(int position) {
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        mDrawerTitle = mDrawerItems.get(position).getTitle();
        if (getActivity().getResources().getString(R.string.medication_reminders).equalsIgnoreCase(mDrawerTitle.toString())) {
            mDrawerTitle = getActivity().getResources().getString(R.string.create_schedule);
        }
        getSupportActionBar().setTitle(mDrawerTitle);
        RunTimeData.getInstance().setRxRefillOnAEMPages(getActivity().getResources().getString(R.string.prescription_refills).equalsIgnoreCase(mDrawerTitle.toString()));

    }

    private void prepareScheduleData() {
        scheduleData.setScheduleTime(new ArrayList<String>());
        scheduleData.setDurationType("");
        scheduleData.setDuration(0);
        scheduleData.setMeditationDuration("Set Reminder");
        scheduleData.setStartDate(null);
        scheduleData.setEndDate(null);
        scheduleData.setSelectedDays(null);
        scheduleData.setEditMedicationClicked(true);
    }

    private void navigateHomeOrCallSignOut() {
        if (mToolbar.getVisibility() == View.VISIBLE) {
            drawerLayout.closeDrawers();
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
                if (null != fragment && fragment instanceof ScheduleWizardFragment && ((ScheduleWizardFragment) fragment).isScheduleDiscardAlertRequired()) {
                    // show confirmation Alert first.
                    if(!RunTimeData.getInstance().isSaveButtonEnabled()) {
                        showDiscardAlert();
                    } else {
                        showSaveAlert();
                    }
                } else {
                    setActionBarTitleAndNavDrawerSelectedItem(NavigationHome.HOME._position);
                    setHomeScreenToolbar();
                    selectItem(NavigationHome.HOME._position);
                }

            }
        } else {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                if (PillpopperRunTime.getInstance().getSelectedHomeFragment().getPosition() != NavigationHome.PRESCRIPTION_REFILLS.getPosition()) {
                    callSignout();
                } else {
                    setActionBarTitleAndNavDrawerSelectedItem(NavigationHome.HOME._position);
                    setHomeScreenToolbar();
                    selectItem(NavigationHome.HOME._position);
                }
            }
        }
    }


    private void launchRefillReminder() {
        if (Util.isEmptyString(FrontController.getInstance(this).getAccessToken(this))) {
            RunTimeData.getInstance().setIsNeedToSetValuesForRefill(true);
            TokenService.startGetAccessTokenService(this);
        }
        installFragment(new RefillRemindersListFragment());
    }

    private void launchFindPharmacyLocator() {
        String dbName = mFrontController.getLocationDbName(getApplicationContext(), "");
        boolean isDbExist = SupportRefillDatabaseHelper.checkIfDbExist(getApplicationContext(), dbName);
        if (isDbExist) {
            Util.getInstance().loadValuesForRxRefillActivity(getActivity());
            launchRxRefillLocatorFragment();
        } else {
            String locationsBaseUrl = Util.getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_LOCATIONS_DB_URL);
            if (!Util.isEmptyString(locationsBaseUrl)) {
                //download location zip db from server
                if (Util.isNetworkAvailable(mContext)) {
                    invokePharmacyDBDownloadAPI();
                } else {
                    loadLocalPharmacyDB();
                }
            } else {
                loadLocalPharmacyDB();
            }
        }
    }

    private void launchGuide() {
        Bundle bundle = new Bundle();
        bundle.putString("Type", "guide");
        bundle.putString("url", AppConstants.ConfigParams.getFaqURL());
        Fragment fragment = new PrivacyAndTCFragment();
        fragment.setArguments(bundle);
        installFragment(fragment);
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra("needHomeButtonEvent", true);
        intent.putExtra("type", "simple");
        startActivityForResult(intent, 0);
    }

    private void launchRxRefillLocatorFragment() {
        setRegularToolbar();
        setActionBarTitleAndNavDrawerSelectedItem(NavigationHome.FIND_PHARMACY.getPosition());
        RefillRuntimeData.setIsPharmacyFromHomeFragment(true);
        //RefillRuntimeData.getInstance().setContext(getApplicationContext());
        installFragment(new PharmacyLocatorFragment());
    }

    private void invokePharmacyDBDownloadAPI() {
        startLoadingActivity();
        RxKpLocationPresentor presentor = new RxKpLocationPresentor();
        AppData.getInstance().initilizeCertificateKeysForPharmacyDB();
        presentor.makekpLocationApiCall(getApplicationContext(), this, Util.getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_LOCATIONS_DB_URL));
    }

    private void startLoadingActivity() {
        if (null != getActivity()) {
            getActivity().startActivityForResult(new Intent(getActivity(), LoadingActivity.class), 0);
        }
    }

    private void setHomeScreenToolbar() {
        mToolbar.setVisibility(View.GONE);
        mToolbarHome.setVisibility(View.VISIBLE);
        setSupportActionBar(mToolbarHome);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawable_hamburger_vector);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void installFragment(Fragment f) {
        if (!isFinishing()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, f, TAG_FRAGMENT);
            fragmentTransaction.replace(R.id.schedule_fragment_calendar_container, new ScheduleCalendarEmptyFragment());
            fragmentTransaction.commit();
        }
    }

    private void installFragment(Fragment f, ScheduleCalendarFragmentNew f2) {
        if (!isFinishing()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.schedule_fragment_calendar_container, f2, TAG_FRAGMENT);
            fragmentTransaction.replace(R.id.fragment_container, f);
            fragmentTransaction.commit();
        }
    }

    private void checkAndMakegetSystemAPI(String quickViewFlag) {
        Intent intent = new Intent(HomeContainerActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (!mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, false) && quickViewFlag.equalsIgnoreCase(State.QUICKVIEW_OPTED_IN)) {
            if (null != FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity)
                    && (("0").equalsIgnoreCase(FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity)) || ("1").equalsIgnoreCase(FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity)))) {
                launchQuickview();
            } else {
                _thisActivity.startActivity(intent);
            }
        } else {
            finishActivity(0);
            _thisActivity.startActivity(intent);
        }
    }

    private void launchQuickview() {
        if (!mSharedPrefManager.getBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, false) && checkForQuickviewReminders() &&
                (null != FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity) &&
                        ("0").equalsIgnoreCase(FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity)) ||
                        ("1").equalsIgnoreCase(FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity)))) {
            AppConstants.setByPassLogin(true);
            FrontController.getInstance(_thisActivity).hideLateRemindersWhenFromNotifications(_thisActivity);
            Intent quickViewIntent = new Intent(_thisActivity, QuickViewOverDueReminderScreen.class);
            quickViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            quickViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PillpopperRunTime.getInstance().setQuickViewReminderDrugs(drugListForQuickview);
            startActivity(quickViewIntent);
            finish();
        } else {
            SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(_thisActivity, AppConstants.AUTH_CODE_PREF_NAME);
            mSharedPrefManager.putBoolean(AppConstants.SIGNED_STATE_REMOVAL, false, false);
            mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false, false);
            mSharedPrefManager.putString(AppConstants.TIME_STAMP, "0", false);
            Intent intent = new Intent(HomeContainerActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private boolean checkForQuickviewReminders() {
        boolean launchQuickView = false;
        if (null != drugListForQuickview && !drugListForQuickview.isEmpty()) {

            Util.getInstance().prepareRemindersMapData(drugListForQuickview, _thisActivity);

            LinkedHashMap<Long, List<Drug>> currentRemindersMap = PillpopperRunTime.getInstance().getmCurrentRemindersMap();
            LinkedHashMap<Long, List<Drug>> passedRemindersMap = PillpopperRunTime.getInstance().getmPassedRemindersMap();

            if (null != currentRemindersMap && currentRemindersMap.size() > 0) {
                launchQuickView = true;
            } else if (null != passedRemindersMap && passedRemindersMap.size() > 0) {
                SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this, AppConstants.AUTH_CODE_PREF_NAME);
                String lateRemindersStatusFromNotifications = sharedPreferenceManager.getString(AppConstants.LATE_REMINDERS_STATUS_FROM_NOTIFICATION, "0");
                if (("1").equalsIgnoreCase(lateRemindersStatusFromNotifications)) {
                    LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedReminderersHashMapByUserId = PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId();
                    if (null != passedReminderersHashMapByUserId && !passedReminderersHashMapByUserId.isEmpty()) {
                        insertPastRemindersPillIdsIntoDB(passedReminderersHashMapByUserId);
                    }
                    AppConstants.setByPassLogin(true);
                }
                if (Util.canShowLateReminder(_thisActivity)) {
                    launchQuickView = true;
                }
            }
        }
        return launchQuickView;
    }

    private void insertPastRemindersPillIdsIntoDB(LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> masterHashMap) {
        for (Map.Entry<String, LinkedHashMap<Long, List<Drug>>> _entry : masterHashMap.entrySet()) {
            LinkedHashMap<Long, List<Drug>> list = _entry.getValue();
            for (Map.Entry<Long, List<Drug>> entry : list.entrySet()) {
                List<Drug> drugs = entry.getValue();
                long time = entry.getKey();
                for (Drug drug : drugs) {
                    PillpopperLog.say("Past Reminder - Inserting pill id : " + drug.getGuid() + " : Time is : " + time);
                    FrontController.getInstance(this).insertPastReminderPillId(drug.getGuid(), time);
                }
            }
        }
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        lookForQuickViewLaunch();
    }

    @Override
    public void onResume() {
        super.onResume();
        isOnPause = false;
        get_globalAppContext().kpMaybeLaunchLoginScreen(this);
        lookForQuickViewLaunch();
        if (isTransactionPending) {
            installFragment(new HomeFragment());
            isTransactionPending = false;
        }

        if(isNavigationPending && null != destinationTab){
            isNavigationPending = false;
            screenNavigation(destinationTab);
            destinationTab = null;
        }

        if (scheduleTakeNowPopUpShown) {
            if (schedulePopupMenu != null) {
                schedulePopupMenu.dismiss();
            }
        }

        if (RunTimeData.getInstance().isFromMDO()
                && !RunTimeData.getInstance().isDiscontinuedAlertShown()
                && FrontController.getInstance(this).getDiscontinuedMedicationsCount() > 0) {
            RunTimeData.getInstance().setIsFromMDO(false);
        }
        if (PillpopperConstants.isCanShowMedicationList()) {
            PillpopperConstants.setCanShowMedicationList(false);
            setRegularToolbar();
            selectItem(1);
        }

        reloadingFromConfigChanges();
    }

    private void reloadingFromConfigChanges(){
        if (loadingFromConfigChanges && RunTimeData.getInstance().getLastSelectedFragmentPosition() != -1) {
            if (RunTimeData.getInstance().getLastSelectedFragmentPosition() == NavigationHome.HOME._position) {
                setActionBarTitleAndNavDrawerSelectedItem(PillpopperRunTime.getInstance().getSelectedHomeFragment()._position);
                setHomeScreenToolbar();
                LocalBroadcastManager.getInstance(this).registerReceiver(refreshKPHCCardsReceiver,
                        new IntentFilter("REFRESH_KPHC_CARDS"));
                selectItem(RunTimeData.getInstance().getLastSelectedFragmentPosition());
            } else if (RunTimeData.getInstance().getLastSelectedFragmentPosition() == NavigationHome.PRESCRIPTION_REFILLS._position) {
                while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                }
                loadRxRefill();
            } else {
                setRegularToolbar();
                isRefillFragmentLoaded = false;
                selectItem(RunTimeData.getInstance().getLastSelectedFragmentPosition());
            }
            loadingFromConfigChanges = false;
            finishActivity(0);
        }
    }

    private void checkForIntervalMedsPostUpgradeToV3() {
        new Handler(Looper.getMainLooper()).post(() -> convertIntervalMedstoAsNeeded());
    }

    private void convertIntervalMedstoAsNeeded() {
        final List<Drug> pillList = FrontController.getInstance(_thisActivity).getAllIntervalDrugs(_thisActivity);
        if (!pillList.isEmpty()) {
            mSharedPrefManager = SharedPreferenceManager.getInstance(this, AppConstants.AUTH_CODE_PREF_NAME);
            FrontController frontController = FrontController.getInstance(_thisActivity);
            for (Drug drug : pillList) {
                frontController.updateIntervalValueForAsNeededDrug(drug.getGuid());
                frontController.addLogEntry(_thisActivity, Util.prepareLogEntryForAction("EditPill", drug, _thisActivity));
            }
            if (!mSharedPrefManager.getBoolean("discontinuedIntervalMedsAlertShown", false))
                showDiscontinuedIntervalMedsAlert();
        }
    }

    private void showDiscontinuedIntervalMedsAlert() {
        mSharedPrefManager.putBoolean("discontinuedIntervalMedsAlertShown", true, false);
        mSharedPrefManager.putBoolean("updateFlag", false, false);
        final Dialog dialog = new Dialog(_thisActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.interval_meds_discontinued_alert);
        Button btnOk = dialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            mSharedPrefManager.putBoolean("discontinuedIntervalMedsAlertShown", true, false);
            updateReminderScreen();
            PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
            LocalBroadcastManager.getInstance(HomeContainerActivity.this).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
        });
        dialog.show();
    }

    private void lookForQuickViewLaunch() {
        String quickViewFlag = FrontController.getInstance(_thisActivity).isQuickViewEnabled();
        if (((null != quickViewFlag
                && quickViewFlag.equalsIgnoreCase(State.QUICKVIEW_OPTED_IN)
                && null == ActivationController.getInstance().getSSOSessionId(this)))) {

            RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
            checkAndMakegetSystemAPI(quickViewFlag);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isOnPause = true;
        if (PillpopperRunTime.getInstance().getSelectedHomeFragment()._position != NavigationHome.EMPTY.getPosition()) {
            RunTimeData.getInstance().setLastSelectedFragmentPosition(PillpopperRunTime.getInstance().getSelectedHomeFragment().getPosition());
        }
    }

    @Override
    public void startActivityForResult(Intent i, int requestCode) {
        /* If resultCode is 100, it means that the device is Android 5.0 and above,
		   and this has been done to handle Android 5.0 and above not allowing
		   attachments from internal storage due to security issue.
		 */
        try {
            if (requestCodes.contains(requestCode)) {
                // this is just super.startActivity() calling through here; the -1 means "I don't need a reply."
                super.startActivityForResult(i, requestCode);
            } else {
                throw new Error("Saaay, you're trying to start an activity from a fragment that would return to the activity. Use PillpopperReplyContext instead.");
            }
        } catch (Exception ne) {
            PillpopperLog.say("requestCodes", ne.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == -1 || resultCode == 100 || resultCode == 0) {
            super.onActivityResult(requestCode, resultCode, intent);
            if (requestCode == PillpopperConstants.REQUEST_REFILL_CARD_DETAIL && resultCode == RESULT_OK) {
                loadRxRefill();
            } else if ((requestCode == PillpopperConstants.REQUEST_NEW_KPHC_CARD_DETAIL && resultCode == RESULT_OK) || (requestCode == PillpopperConstants.REQUEST_SETUP_REMINDER_CARD_DETAIL && resultCode == RESULT_CANCELED)
                    || (requestCode == PillpopperConstants.REQUEST_QUICK_ACCESS_MENU_SETUP_REMINDERS && resultCode == RESULT_OK)) {
                setRegularToolbar();
                if (RunTimeData.getInstance().getIsNewScheduleRequired()) {
                    selectItem(NavigationHome.MEDICATION_REMINDERS.getPosition());
                } else {
                    selectItem(NavigationHome.DAILY_SCHEDULE.getPosition());
                }
            } else if (requestCode == PillpopperConstants.REQUEST_QUICK_ACCESS_CREATE_REFILL_REMINDER) {
                setRegularToolbar();
                selectItem(NavigationHome.REFILL_REMINDER.getPosition());
            } else if (requestCode == PillpopperConstants.REQUEST_SETUP_VIEW_MEDS_CARD_DETAIL && resultCode == RESULT_OK) {
                if (!isFinishing()) {
                    setRegularToolbar();
                    selectItem(NavigationHome.MEDICATIONS.getPosition());
                }
            } else if (requestCode == PillpopperConstants.REQUEST_SAVE_SCHEDULE) {
                setHomeScreenToolbar();
                selectItem(NavigationHome.HOME.getPosition());
            }
        } else if (resultCode == AppConstants.LATE_REMINDERS_CONTRACTED_CARD_REFRESH_RESULT_CODE) {
            String userID = intent.getStringExtra("Id");
            mLateRemindersActionInterface = new LateRemindersHomeCard();
            mLateRemindersActionInterface.doButtonTextRefresh(this, userID);
        } else if (resultCode == AppConstants.CURRENT_REMINDERS_CONTRACTED_CARD_REFRESH_RESULT_CODE) {
            CurrentReminderCard mCurrentReminderRefreshInterface = new CurrentReminderCard();
            mCurrentReminderRefreshInterface.doRefresh();
        } else {
            // Neutered startActivityForResult should keep us from ever landing here.
            throw new Error("Saaay, you started an activity from a fragment but via the activity. See PillpopperReplyContext.");
        }

        if (PillpopperConstants.isDiscontinuedKPHCMedAlertShown()) {
            PillpopperConstants.setIsDiscontinuedKPHCMedAlertShown(false);
            updateReminderScreen();
        }
    }

    private void showSaveAlert(){
            DialogHelpers.showAlertWithSaveCancelListeners(getActivity(), R.string.save_updates, R.string.save_changes_on_exit_message,
                    new DialogHelpers.Confirm_CancelListener() {
                        @Override
                        public void onConfirmed() {
                            if(null != mReminderTimeFragment){
                                mReminderTimeFragment.saveScheduleOnBackPress(_thisActivity);
                            }
                        }

                        @Override
                        public void onCanceled() {
                            setHomeScreenToolbar();
                            selectItem(NavigationHome.HOME.getPosition());
                        }
                    });
    }

    private void showDiscardAlert() {
        DialogHelpers.showAlertWithConfirmDiscardListeners(this, R.string.discard_schedule_title, R.string.discard_schedule_on_exit_message,
                new DialogHelpers.Confirm_CancelListener() {
                    @Override
                    public void onConfirmed() {
                        // DE22785
                        setHomeScreenToolbar();
                        selectItem(NavigationHome.HOME.getPosition());
                    }

                    @Override
                    public void onCanceled() {
                        //do nothing
                    }
                });
    }

    @Override
    public void saveReminderTimeFragmentInstance(ReminderTimeFragment reminderTimeFragment) {
        mReminderTimeFragment = reminderTimeFragment;
    }

    @Override
    public void onBackPressed() {
        if (null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {

            RunTimeData.getInstance().getScheduleData().setEditMedicationClicked(false);
            RunTimeData.getInstance().getScheduleData().setIsFromScheduleMed(true);
            installFragment(new DrugListRecyclerDBFragment());
            return;
        }
        if (PillpopperRunTime.getInstance().getSelectedHomeFragment().getPosition() != NavigationHome.PRESCRIPTION_REFILLS.getPosition()) {
            navigateHomeOrCallSignOut();
        } else {
            if (AppConstants.IS_NATIVE_RX_REFILL_REQUIRED) {
                int count = getSupportFragmentManager().getBackStackEntryCount();
                if (count == 0) {
                    navigateHomeOrCallSignOut();
                } else {
                    if (!RefillRuntimeData.getInstance().isAppInOrderConfirmationScreen()) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            } else {
                if (!RunTimeData.getInstance().isRxRefillOnAEMPages()) {
                    handleWebViewBackNavigation();
                } else {
                    if (RunTimeData.getInstance().isRxRefillAEMErrorPageLoaded) {
                        RunTimeData.getInstance().isRxRefillAEMErrorPageLoaded = false;
                        PillpopperRunTime.getInstance().setWebViewInstance(null);
                        refillFragment();
                    } else {
                        handleWebViewBackNavigation();
                    }
                }
            }
        }
    }

    private void handleWebViewBackNavigation() {
        if (null != PillpopperRunTime.getInstance().getWebViewInstance()) {
            if (PillpopperRunTime.getInstance().getWebViewInstance().canGoBack()) {
                PillpopperRunTime.getInstance().getWebViewInstance().goBack();
            } else {
                navigateHomeOrCallSignOut();
                PillpopperRunTime.getInstance().setWebViewInstance(null);
            }
        } else {
            navigateHomeOrCallSignOut();
        }
    }

    private void callSignout() {
        if (FrontController.getInstance(_thisActivity).isLogEntryAvailable()) {
            //Performing an intermediate get state if log entries are available since there is a chance the user might
            //sign out of the application before the next time the GetState service is triggered.
            StateDownloadIntentService.startActionIntermediateGetState(_thisActivity);
        }

        ImageSynchronizerService.startImageSynchronization(_thisActivity);

        DialogHelpers.showConfirm_CancelDialog(_thisActivity, R.string.setting_signout_confirm, new DialogHelpers.Confirm_CancelListener() {
            @Override
            public void onConfirmed() {
                try {
                    TokenService.startRevokeTokenService(_thisActivity, FrontController.getInstance(_thisActivity).getRefreshToken(_thisActivity));
                } catch (Exception e) {
                    PillpopperLog.say(e.getMessage());
                }
                RunTimeData.getInstance().setShowFingerprintDialog(false);
                Util.performSignout(_thisActivity, get_globalAppContext());
                PillpopperRunTime.getInstance().setSelectedHomeFragment(NavigationHome.HOME);
                getState().setScheduleViewDay(PillpopperDay.today());
                FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(_thisActivity, FireBaseConstants.Event.SIGN_OUT);
            }

            @Override
            public void onCanceled() {
            }
        });
    }

    private int getOverDueDrugCountFromDB() {
        ArrayList<Drug> overduedrugs = new ArrayList<>();
        for (final Drug d : FrontController.getInstance(_thisActivity).getDrugListForOverDue(_thisActivity)) {
            d.computeDBDoseEvents(_thisActivity, d, PillpopperTime.now(), 60);
            if (d.isoverDUE() && (null == d.getSchedule().getEnd()
                    || (d.getSchedule().getEnd().equals(PillpopperDay.today()) ||
                    d.getSchedule().getEnd().after(PillpopperDay.today())))) {
                if (((!PillpopperRunTime.getInstance().isLauchingFromPast() && isEligibleEvent(d))
                        || (null != d.getPassedReminderTimes() && d.getPassedReminderTimes().size() > 0))) {
                    if ((PillpopperTime.now().getGmtMilliseconds() - d.getOverdueDate().getGmtMilliseconds()) < 24 * 60 * 60 * 1000) {
                        overduedrugs.add(d);
                    }
                }
            }
        }
        drugListForQuickview.addAll(overduedrugs);
        if (!overduedrugs.isEmpty()) {
            AppConstants.setIsOptedInAndPendingNotificationIsThere(true);
            return overduedrugs.size();
        } else {
            AppConstants.setIsOptedInAndPendingNotificationIsThere(false);
            return 0;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void refillBannerClicked(boolean isBannerCicked) {
        loadRxRefill();
    }

    public void refillFragment() {
        TTGWebviewModel mWebModel = Util.getRefillWebViewModel(_thisActivity);
        if (mWebModel == null) {
            _thisActivity.finishActivity(0);
        } else {
            setRegularToolbar();
            selectItem(NavigationHome.PRESCRIPTION_REFILLS.getPosition());

            //trigger screen event
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_PRESCRIPTION_REFILL_AEM);

            FragmentTransaction mFragmenTransaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = null;
            Bundle bundle = new Bundle();
            if (mWebModel instanceof TTGSecureWebViewModel) {
                fragment = new SecureWebviewFragment();
            } else {
                fragment = new NonSecureWebviewFragment();
            }
            bundle.putSerializable("webViewModel", mWebModel);
            fragment.setArguments(bundle);
            mFragmenTransaction.replace(R.id.schedule_fragment_calendar_container, new ScheduleCalendarEmptyFragment());
            mFragmenTransaction.replace(R.id.fragment_container, fragment, TAG_FRAGMENT);
            isRefillFragmentLoaded = true;
            RunTimeData.getInstance().setRxRefillOnAEMPages(true);
            RunTimeData.getInstance().setLastSelectedFragmentPosition(NavigationHome.PRESCRIPTION_REFILLS.getPosition());
            PillpopperRunTime.getInstance().setSelectedHomeFragment(NavigationHome.PRESCRIPTION_REFILLS);
            mFragmenTransaction.commit();
        }
    }


    public void refillAlertDailog(final PillpopperActivity context) {
        setActionBarTitleAndNavDrawerSelectedItem(PillpopperRunTime.getInstance().getSelectedHomeFragment()._position);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View customLayout = getLayoutInflater().inflate(R.layout.refill_dialog, null);
        builder.setView(customLayout);
        Button dialogButton = customLayout.findViewById(R.id.btn_refill_alert);
        dialogRefill = builder.create();
        dialogRefill.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogButton.setOnClickListener(v -> {
            refillFragment();
            if (null != dialogRefill && dialogRefill.isShowing()) {
                ActivationController.getInstance().setRefillScreenChoice(HomeContainerActivity.this, true);
                dialogRefill.dismiss();
            }
        });
        if (null != dialogRefill && !dialogRefill.isShowing()) {
            RunTimeData.getInstance().setAlertDialogInstance(dialogRefill);
            dialogRefill.show();
            dialogRefill.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    public void onStateUpdated() {
        super.onStateUpdated();
    }

    public void handleGetStateBroadcast() {
        checkForIntervalMedsPostUpgradeToV3();
        if (null != RunTimeData.getInstance().getPillIdList() && !RunTimeData.getInstance().getPillIdList().isEmpty()) {
            for (String pillId : RunTimeData.getInstance().getPillIdList()) {
                addLogEntryForEdit(FrontController.getInstance(_thisActivity).getDrugByPillId(pillId), _thisActivity);
            }
            RunTimeData.getInstance().setPillIdList(null);
        }
    }

    @Override
    public void onDbDownloadFail() {
        runOnUiThread(this::loadLocalPharmacyDB);
    }

    private void loadLocalPharmacyDB() {
        boolean isDbExist = SupportRefillDatabaseHelper.checkIfDbExist(getActivity(), mFrontController.getLocationDbName(getActivity(), ""));
        if (!isDbExist) {
            FireBaseAnalyticsTracker.getInstance().logEvent(getActivity(),
                    FireBaseConstants.Event.PHARMACY_BUNDLED_DB_USED,
                    FireBaseConstants.ParamName.REASON,
                    FireBaseConstants.ParamValue.NO_DB_DOWNLOADED);
            RxRefillDBHandler.getInstance(getActivity()).copyLocalDB();
            Util.getInstance().loadValuesForRxRefillActivity(getActivity());
        }
        dismissLoadingActivity();
        launchRxRefillLocatorFragment();
    }

    private void dismissLoadingActivity() {
        if (null != getActivity()) {
            getActivity().finishActivity(0);
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshKPHCCardsReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(openNewTab);
        super.onDestroy();
    }
    public void screenNavigation(String destinationTab) {
        if(null != destinationTab) {
            switch (destinationTab) {
                case FireBaseConstants.ScreenEvent.SCREEN_HOME:
                    setHomeScreenToolbar();
                    selectItem(NavigationHome.HOME.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_MED_LIST:
                    setRegularToolbar();
                    selectItem(NavigationHome.MEDICATIONS.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_FIND_PHARMACY:
                    selectItem(NavigationHome.FIND_PHARMACY.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_SCHEDULE_LIST:
                    setRegularToolbar();
                    selectItem(NavigationHome.DAILY_SCHEDULE.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_BULK_REMINDER_SETUP:
                    setRegularToolbar();
                    selectItem(NavigationHome.MEDICATION_REMINDERS.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_HISTORY_LIST:
                    setRegularToolbar();
                    selectItem(NavigationHome.HISTORY.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_ARCHIVE_LIST:
                    setRegularToolbar();
                    selectItem(NavigationHome.ARCHIVE.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_GUIDE:
                    setRegularToolbar();
                    selectItem(NavigationHome.GUIDE.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_REFILL_REMINDER_LIST:
                    setRegularToolbar();
                    selectItem(NavigationHome.REFILL_REMINDER.getPosition());
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_PRESCRIPTION_REFILL_AEM:
                    loadRxRefill();
                    break;
                case FireBaseConstants.ScreenEvent.SCREEN_SETTINGS:
                    setRegularToolbar();
                    selectItem(NavigationHome.SETTINGS.getPosition());
                    break;
            }
        }
    }

    /*
     * On Success of SetUpProxyEnabledAPI call
     */
    @Override
    public void onSetUpProxyResponseReceived(int result) {
        if (0 == result) {
            RunTimeData.getInstance().setEnabledUsersList(FrontController.getInstance(this).getAllEnabledUsers());
            RunTimeData.getInstance().setSelectedUsersList(FrontController.getInstance(this).getEnabledUserIds());
        }
    }

    private class NavDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            final NavigationHome selectedHomeFragment = NavigationHome.values()[position];
            new Handler().postDelayed(() -> {
                if (selectedHomeFragment != NavigationHome.MEDICATION_REMINDERS) {
                    handleBulkRemindersNavigationAlerts(selectedHomeFragment, position);
                } else {
                    handleNavigationItemClick(selectedHomeFragment, position);
                }
            }, 250);
            if (selectedHomeFragment != NavigationHome.EMPTY) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        }
    }


    private void handleBulkRemindersNavigationAlerts(NavigationHome selectedHomeFragment, int position) {
        try {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
            if (null != fragment && fragment instanceof ScheduleWizardFragment && ((ScheduleWizardFragment) fragment).isScheduleDiscardAlertRequired()) {
                Util.hideSoftKeyboard(getActivity());
                if (!RunTimeData.getInstance().isSaveButtonEnabled()) {
                    DialogHelpers.showAlertWithConfirmDiscardListeners(HomeContainerActivity.this, R.string.discard_schedule_title, R.string.discard_schedule_on_exit_message,
                            new DialogHelpers.Confirm_CancelListener() {
                                @Override
                                public void onConfirmed() {
                                    RunTimeData.getInstance().setLastSelectedFragmentPosition(position);
                                    handleNavigationItemClick(selectedHomeFragment, position);
                                }

                                @Override
                                public void onCanceled() {
                                    setActionBarTitleAndNavDrawerSelectedItem(3);
                                }
                            });
                } else {
                    DialogHelpers.showAlertWithSaveCancelListeners(getActivity(), R.string.save_updates, R.string.save_changes_on_exit_message,
                            new DialogHelpers.Confirm_CancelListener() {
                                @Override
                                public void onConfirmed() {
                                    RunTimeData.getInstance().setLastSelectedFragmentPosition(position);
                                    if (null != mReminderTimeFragment) {
                                        mReminderTimeFragment.saveScheduleOnBackPress(_thisActivity);
                                    }
                                }

                                @Override
                                public void onCanceled() {
                                    handleNavigationItemClick(selectedHomeFragment, position);
                                }
                            });
                }
            } else {
                handleNavigationItemClick(selectedHomeFragment, position);
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
            handleNavigationItemClick(selectedHomeFragment, position);
        }
    }
}
