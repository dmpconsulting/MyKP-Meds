package com.montunosoftware.pillpopper.android.home;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.AddOrEditMedicationActivity;
import com.montunosoftware.pillpopper.android.AutoSignInSplashActivity;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.PrivacyAndTC;
import com.montunosoftware.pillpopper.android.RxRefillHomeContainerActivity;
import com.montunosoftware.pillpopper.android.ScheduleCalendarEmptyFragment;
import com.montunosoftware.pillpopper.android.ScheduleWizardFragment;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.refillreminder.views.RefillRemindersHomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;
import com.montunosoftware.pillpopper.kotlin.bannerCard.GenericBannerFragment;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem;
import com.montunosoftware.pillpopper.service.UpdateSetupIntroCompleteTask;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.controller.RxFrontController;
import org.kp.tpmg.ttg.database.RxRefillDBHandler;
import org.kp.tpmg.ttg.database.SupportRefillDatabaseHelper;
import org.kp.tpmg.ttg.presenter.RxLocationPresentorCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService.BROADCAST_REMOVE_REGISTRATION_POPUP;

/**
 * Created by m1032896 on 11/14/2017.
 */

public class HomeFragment extends Fragment implements View.OnClickListener, RxLocationPresentorCallback {

    private RecyclerView mCardList;
    private HomeCardAdapter mHomeCardAdapter;
    private List<HomeCard> mHomeCards = new ArrayList<>();
    private Handler mBannerHandler = new Handler();
    private Runnable mBannerRunnable;
    private Button mBtnSetupReminders;
    private Button mBtnCreateRefillReminders;
    private Button mBtnRefillRemind;
    private Button mBtnAddMedication;
    private Button mBtnFindAPharmacy;
    private LinearLayout mGuideContainer;
    private TextView txtHomeDate, getTxtHomeGreeting;
    private DisplayMetrics displayMetrics;
    private IntentFilter mGetStateReceiverIntentFilter;
    private int lateReminderUsersCount;
    private PillpopperActivity mContext;
    private Dialog refillDialog;
    private ChangeNavigation mChangeNavigation;
    private SharedPreferenceManager mSharedPrefManager;
    private LoadCardsTask loadCardsTask;
    private CardView mTermsAndConditionsBanner;
    private TextView mView;
    private TextView mDismiss;
    private boolean launchLocatorFragment = false;
    private boolean mIsAnyUserEnabledRemindersHasSchedules;
    private boolean mBatteryOptimizationFlag;
    private boolean mCurrentStateOfBatteryOptimization;
    private RxFrontController mRxFrontController;

    private BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null) return;
            try {
                getActivity().unregisterReceiver(mGetStateBroadcastReceiver);
            } catch (Exception e) {
                PillpopperLog.say("HomeFragment.java: Unable to unregister receiver. ", e);
            }
            FrontController frontController = FrontController.getInstance(getContext());
            mIsAnyUserEnabledRemindersHasSchedules = frontController.isAnyUserEnabledRemindersHasSchedules();
            setAdapter();
            RunTimeData.getInstance().setHomeCardsShown(true);
            Util.logFireBaseEventForDeviceFontScale(getActivity());
        }
    };


    private BroadcastReceiver timeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent && intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                if (minute == 0) {
                    updateGreeting();
                }
            }
        }
    };
    private static FragmentManager fragmentManager;
    private Context context;
    private View view;

    public interface onQuickActionButtonClickListener{
        void onFindAPharmacyQuickActionClicked();
        void onGuideQuickActionClicked();
        void onCreateARefillQuickActionClicked();
        void onSetUpReminderQuickActionClicked();
        void onRxRefillMedicationsQuickActionClicked();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //Register the broadcast receiver to receive TIME_TICK
        context.registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        mSharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
        mChangeNavigation = (ChangeNavigation) context;
        mContext = (PillpopperActivity) context;

        FrontController frontController = FrontController.getInstance(getContext());
        mRxFrontController = RxFrontController.getInstance(getContext());
        mIsAnyUserEnabledRemindersHasSchedules = frontController.isAnyUserEnabledRemindersHasSchedules();

        loadCardsTask = new LoadCardsTask();

        FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_HOME);

        LocalBroadcastManager.getInstance(context).registerReceiver(refreshRefillCards,
                new IntentFilter("REFRESH_REFILL_REMINDERS"));
        LocalBroadcastManager.getInstance(context).registerReceiver(refreshCurrentReminderCards,
                new IntentFilter("REFRESH_CURRENT_REMINDERS"));
        LocalBroadcastManager.getInstance(context).registerReceiver(refreshReminderCards,
                new IntentFilter("REFRESH_REMINDERS_CARDS_AFTER_ACTION"));
        LocalBroadcastManager.getInstance(context).registerReceiver(teenProxyHomeCard,
                new IntentFilter("REFRESH_TEEN_PROXY_HOME_CARD"));
        LocalBroadcastManager.getInstance(context).registerReceiver(refreshGenericHomeCard,
                new IntentFilter("REFRESH_GENERIC_HOME_CARD"));
        LocalBroadcastManager.getInstance(context).registerReceiver(refreshKPHCCardsReceiver,
                new IntentFilter("REFRESH_KPHC_CARDS"));
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (RunTimeData.getInstance().isHomeCardsShown())
            setAdapter();
        else
            setStateDownloadCompleteReceiver();
    }

    private void initHomeCards() {
        if (null != loadCardsTask && loadCardsTask.getStatus() != AsyncTask.Status.RUNNING) {
            loadCardsTask = null;
            loadCardsTask = new LoadCardsTask();
            loadCardsTask.execute();
        }
    }

    @Override
    public void onDbUpdated() {
        dismissLoadingActivity();
        Util.getInstance().loadValuesForRxRefillActivity(context);
        launchRxRefillLocatorFragment();
    }

    @Override
    public void onDbDownloadFail() {
        boolean isDbExist = SupportRefillDatabaseHelper.checkIfDbExist(getContext(), mRxFrontController.getLocationDbName(getContext(), ""));
        if(!isDbExist) {
            FireBaseAnalyticsTracker.getInstance().logEvent(getContext(),
                    FireBaseConstants.Event.PHARMACY_BUNDLED_DB_USED,
                    FireBaseConstants.ParamName.REASON,
                    FireBaseConstants.ParamValue.NO_DB_DOWNLOADED);
            RxRefillDBHandler.getInstance(getContext()).copyLocalDB();
            Util.getInstance().loadValuesForRxRefillActivity(context);
        }
        dismissLoadingActivity();
        launchRxRefillLocatorFragment();
    }

    public class LoadCardsTask extends AsyncTask<Void, Void, List<HomeCard>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (RunTimeData.getInstance().isFirstTimeLandingOnHomeScreen()) {
                LoggerUtils.info("show loading cards progress");
                getActivity().startActivityForResult(new Intent(getActivity(), LoadingActivity.class), 0);
            }
        }

        @Override
        protected List<HomeCard> doInBackground(Void... voids) {
            Thread.currentThread().setName(getClass().getName());
            if (mHomeCards != null) {
                mHomeCards.clear();
            }

            if((Util.isBatteryOptimizationCardRequired(getActivity()) && mIsAnyUserEnabledRemindersHasSchedules)){
                mBatteryOptimizationFlag = true;
                mHomeCards.add(new BatteryOptimizerInfoCard(getActivity()));
            }

            if (isWelcomeScreensToBeShown()) {
                mHomeCards.add(new ViewMedicationCard());
                mHomeCards.add(new RefillCard());
                mHomeCards.add(new ManageMemberCard());
                mHomeCards.add(new SetupReminderCard());
            }
            initGenericAnnouncementsCards();

            if (mSharedPrefManager.getBoolean("showTeenCard", false)) {
                mHomeCards.add(new TeenProxyHomeCard(getActivity()));
            }
            initNewKPHCCards();
            initUpdatedKPHCCards();
            initKPHCDiscontinueCards();
            initCurrentReminderCards();
            initRefillReminderCards();
            initLateReminderCards();
            if (isQuickViewCardNeedsToShow()) {
                mHomeCards.add(new ViewReminderCard());
            }
            return mHomeCards;
        }

        @Override
        protected void onPostExecute(List<HomeCard> homeCards) {
            super.onPostExecute(homeCards);
            new Handler().postDelayed(() -> {
                try {
                    getActivity().finishActivity(0);
                } catch (Exception e) {
                    PillpopperLog.say(e);
                }
            }, 5);

            mHomeCardAdapter = null;
            mHomeCardAdapter = new HomeCardAdapter(displayMetrics, mHomeCards, getActivity(), lateReminderUsersCount);
            if(!mHomeCards.isEmpty()) {
                mCardList.setVisibility(View.VISIBLE);
                mCardList.setAdapter(mHomeCardAdapter);
            }else{
                mCardList.setVisibility(View.GONE);
            }

            String cardIndex = mSharedPrefManager.getString(AppConstants.EXPANDED_CARD_INDEX_KEY, "-1");
            int index = Integer.parseInt(cardIndex);

            if (index != -1 && PillpopperRunTime.getInstance().isCardAdjustmentRequired()
                    && index > 0) {
                if (mHomeCards.size() > index) {
                    scrollToPosition(index);
                    PillpopperRunTime.getInstance().setCardAdjustmentRequired(false);
                    mSharedPrefManager.remove(AppConstants.EXPANDED_CARD_INDEX_KEY);
                } else {
                    scrollToPosition(mHomeCards.size() - 1);
                    PillpopperRunTime.getInstance().setCardAdjustmentRequired(false);
                    mSharedPrefManager.remove(AppConstants.EXPANDED_CARD_INDEX_KEY);
                }
            } else {
                if (!mHomeCards.isEmpty()) {
                    scrollToPosition(0);
                    PillpopperRunTime.getInstance().setCardAdjustmentRequired(false);
                    mSharedPrefManager.remove(AppConstants.EXPANDED_CARD_INDEX_KEY);
                }
            }

            // to take the focus to first card
            if (RunTimeData.getInstance().isRefreshHomeCardsPending()
                    && RunTimeData.getInstance().isBackFromExpandedCard()) {
                new Handler().postDelayed(() -> {
                    scrollToPosition(0);
                    PillpopperRunTime.getInstance().setCardAdjustmentRequired(false);
                    RunTimeData.getInstance().resetRefreshCardsFlags();
                }, 500);
            }

            if (RunTimeData.getInstance().isNavigateToRefillScreen()) {
                new Handler().post(() -> {
                    RunTimeData.getInstance().setNavigateToRefillScreen(false);
                    if (null != getActivity()) {
                        ((HomeContainerActivity) getActivity()).onRxRefillMedicationsQuickActionClicked();
                    }
                });
            }

            if (!RunTimeData.getInstance().isFirstTimeLandingOnHomeScreen()) {
                AutoSignInSplashActivity.clearObjects();
                removeRegistrationPopup();
                LoggerUtils.info("removeRegistrationPopup");
            }
        }
    }

    /**
     * displays Announcements cards in home screen.
     * received in AppProfile response.
     */
    private void initGenericAnnouncementsCards() {
        List<AnnouncementsItem> announcementsList = Util.getGenericCardsList(getActivity());
        if (null != announcementsList && !announcementsList.isEmpty()) {
            for (AnnouncementsItem announcementsItem : announcementsList) {
                if(Util.isEmptyString(announcementsItem.getRetention())|| announcementsItem.getRetention().equalsIgnoreCase("hard")) {
                    mHomeCards.add(new GenericHomeCard(announcementsItem, mHomeCards.size()));
                }else{
                    Set<String> addedIdSet = mSharedPrefManager.getStringSet("CardIdSet",new HashSet<>());
                    if (null==addedIdSet || !addedIdSet.contains(announcementsItem.getId()+"")) {
                        mHomeCards.add(new GenericHomeCard(announcementsItem, mHomeCards.size()));
                    }
                }
            }
        }
    }


    // TODO needs to revisit for optimization.
    /**
     * Invokes the bulk schedule screen.
     * @param context
     * @param isFinishActivityRequired
     */
    public static void invokeBulkSchedule(Context context, boolean isFinishActivityRequired)
    {
        RunTimeData.getInstance().setIsNewScheduleRequired(true);
        if(!isFinishActivityRequired){
            ((AppCompatActivity) context).setResult(RESULT_OK);
        }
        ((AppCompatActivity) context).finish();
        FragmentTransaction fragment_transaction = fragmentManager.beginTransaction();
        fragment_transaction.replace(R.id.fragment_container,new ScheduleWizardFragment());
        fragment_transaction.replace(R.id.schedule_fragment_calendar_container, new ScheduleCalendarEmptyFragment());
    }

    private void initKPHCDiscontinueCards() {
        if (mHomeCards != null) {
            List<DiscontinuedDrug> mDiscontinuedDrugList = FrontController.getInstance(mContext).getDiscontinuedMedications();
            if (!mDiscontinuedDrugList.isEmpty()) {
                mHomeCards.add(new KPHCDiscontinueCard(mDiscontinuedDrugList, mContext));
            }
        }
    }

    private void initRefillReminderCards() {
        List<RefillReminder> refillReminderList = RefillReminderController.getInstance(getContext()).getOverdueRefillRemindersForCards();
        for (RefillReminder refillReminder : refillReminderList) {
            mHomeCards.add(new RefillReminderOverdueCard(refillReminder, mHomeCards.size()));
        }
    }

    private void initCurrentReminderCards() {
        try {
            List<Drug> drugList = getOverDueDrugList();
            if (!drugList.isEmpty()) {

                insertEligiblePastRemindersToDB();

                Util.getInstance().prepareRemindersMapData(drugList, mContext);

                if (FrontController.getInstance(mContext).getPassedReminderDrugs(mContext).size() > 0) {
                    FrontController.getInstance(mContext).updateAsPendingRemindersPresent(mContext);
                } else {
                    FrontController.getInstance(mContext).updateAsNoPendingReminders(mContext);
                }

                PillpopperRunTime.getInstance().setCurrentRemindersByUserIdForCard(PillpopperRunTime.getInstance().getmCurrentRemindersMap());
                for (Map.Entry<Long, List<Drug>> entry : PillpopperRunTime.getInstance().
                        getCurrentRemindersByUserIdForCard().entrySet()) {
                    mHomeCards.add(new CurrentReminderCard(mContext, entry.getKey(), mHomeCards.size()));
                }
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception in initCurrentReminderCards ", e);
        }
    }

    private void insertEligiblePastRemindersToDB() {
        LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersHashMapByUserId = PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId();
        List<Drug> allUsersDrugList = new ArrayList<>();
        if (null != passedRemindersHashMapByUserId && !passedRemindersHashMapByUserId.isEmpty()) {
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
                }
            }
            if (null != allUsersDrugList && !allUsersDrugList.isEmpty()) {
                Util.getInstance().insertPastRemindersPillIdsIntoDB(mContext, passedRemindersHashMapByUserId);
            }
        }
    }

    private List<Drug> getOverDueDrugList() {
        List<Drug> overDrugList = new ArrayList<>();
        for (final Drug d : FrontController.getInstance(mContext).getDrugListForOverDue(mContext)) {
            d.computeDBDoseEvents(mContext, d, PillpopperTime.now(), 60);
            if (d.isoverDUE() && (null == d.getSchedule().getEnd()
                    || (d.getSchedule().getEnd().equals(PillpopperDay.today()) ||
                    d.getSchedule().getEnd().after(PillpopperDay.today())))) {
                if (((!PillpopperRunTime.getInstance().isLauchingFromPast() && isEligibleEvent(d)))) {
                    if ((PillpopperTime.now().getGmtMilliseconds() - d.getOverdueDate().getGmtMilliseconds()) < 24 * 60 * 60 * 1000) {
                        overDrugList.add(d);
                    }
                }
            }
        }
        return overDrugList;
    }


    public boolean isEligibleEvent(Drug drug) {
        if (null != drug.getPreferences() && (null != drug.getPreferences().getPreference("missedDosesLastChecked")
                && (drug.getOverdueDate().after(Util.convertStringtoPillpopperTime(drug.getPreferences().getPreference("missedDosesLastChecked"))))
                || String.valueOf(drug.getOverdueDate().getGmtSeconds()).equalsIgnoreCase(drug.getPreferences().getPreference("missedDosesLastChecked")))
                || (FrontController.getInstance(mContext).isEntryAvailableInPastReminder(drug.getGuid(), drug.getOverdueDate()))
                && (null != drug.getOverdueDate() && null != drug.getCreated() && !drug.getOverdueDate().before(drug.getCreated()))) {
            return true;
        }
        return false;
    }

    private void initLateReminderCards() {

        try {
            PillpopperRunTime.getInstance().setPassedRemindersByUserIdForCards(PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId());

            LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersHashMapByUserId = PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards();

            List<Drug> allUsersDrugList = new ArrayList<>();
            List<String> usersToBeRemoved = new ArrayList<>();
            if (null != passedRemindersHashMapByUserId && !passedRemindersHashMapByUserId.isEmpty()) {
                lateReminderUsersCount = passedRemindersHashMapByUserId.size();
                for (Map.Entry<String, LinkedHashMap<Long, List<Drug>>> _entry : passedRemindersHashMapByUserId.entrySet()) {
                    LinkedHashMap<Long, List<Drug>> list = _entry.getValue();
                    String userID = _entry.getKey();

                    if (FrontController.getInstance(getActivity()).isEnabledUser(userID)) {
                        List<Drug> drugList = new ArrayList<>();
                        for (Map.Entry<Long, List<Drug>> entry : list.entrySet()) {
                            for (Drug d : entry.getValue()) {
                                if (FrontController.getInstance(getActivity()).isActiveDrug(d.getGuid(), new PillpopperTime(entry.getKey() / 1000))) {
                                    if (FrontController.getInstance(context).isEntryAvailableInPastReminder(d.getGuid(), new PillpopperTime(entry.getKey() / 1000))) {
                                        drugList.add(d);
                                        allUsersDrugList.add(d);
                                    }
                                }
                            }
                        }

                        if (!drugList.isEmpty()) {
                            mHomeCards.add(new LateRemindersHomeCard(userID, FrontController.getInstance(getActivity()).getUserFirstNameByUserId(userID), drugList, isLastUserCheck(passedRemindersHashMapByUserId, userID), mHomeCards.size()));
                        } else if (drugList.isEmpty()) {
                            usersToBeRemoved.add(userID);
                        }
                    }
                }
                for (String userId : usersToBeRemoved) {
                    passedRemindersHashMapByUserId.remove(userId);
                }
                PillpopperRunTime.getInstance().setPassedRemindersByUserIdForCards(passedRemindersHashMapByUserId);
                if (null != allUsersDrugList && !allUsersDrugList.isEmpty()) {
                    Util.getInstance().insertPastRemindersPillIdsIntoDB(getContext(), passedRemindersHashMapByUserId);
                }
            }
        } catch (Exception ex) {
            LoggerUtils.exception("Exception in initLateReminderCards ", ex);
        }

    }

    private int isLastUserCheck(LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> list, String userID) {
        List<String> usersList = new LinkedList<>(list.keySet());
        PillpopperLog.say(" isLastUserCheck : " + usersList.indexOf(userID) + " Userid : " + userID + " : Size : " + usersList.size());
        return (usersList.indexOf(userID) == usersList.size() - 1) ? 1 : 0;
    }


    private boolean isQuickViewCardNeedsToShow() {
        String quickViewFlag = FrontController.getInstance(getActivity()).isQuickViewEnabled();
        if ((null == quickViewFlag || ("-1").equalsIgnoreCase(quickViewFlag))) {
            return true;
        }
        return false; // Could be optedIn/OptedOut For QuickView
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (timeTickReceiver != null) {
                getActivity().unregisterReceiver(timeTickReceiver);
            }
        } catch (Exception e) {
            PillpopperLog.say(e);
        }
    }

    private void initNewKPHCCards() {
        if (mHomeCards != null) {
            List<User> newKphcUsers = DatabaseUtils.getInstance(getActivity()).getAllKPHCUsers(getActivity(), true);
            processKPHCCards(newKphcUsers, true);
        }

    }

    private void initUpdatedKPHCCards() {
        if (mHomeCards != null) {
            List<User> updatedKphcUsers = DatabaseUtils.getInstance(getActivity()).getAllKPHCUsers(getActivity(), false);
            processKPHCCards(updatedKphcUsers, false);

        }
    }

    private void processKPHCCards(List<User> kphcUsers, boolean isNewKPHCMeds) {
        if (kphcUsers != null && !kphcUsers.isEmpty()) {
            for (User user : kphcUsers) {
                mHomeCards.add(new KPHCCards(user, isNewKPHCMeds));
            }
        } else {
            if (mGetStateReceiverIntentFilter != null)
                try {
                    getActivity().unregisterReceiver(mGetStateBroadcastReceiver);
                } catch (Exception e) {
                    PillpopperLog.say("HomeFragment.java: Unable to unregister receiver. ", e);
                }
        }
    }

    private boolean isWelcomeScreensToBeShown() {
        // to avoid calculations every time the user visits the home screen, AppConstants.WELCOME_SCREENS_DISPLAY_RESULT is used
        if (("-1").equalsIgnoreCase(AppConstants.getWelcomeScreensDisplayResult())
                && ActivationController.getInstance().isSessionActive(getActivity())) {
            // indicates the home screen is visited for the first time.
            String setUpCompleteFlag = ActivationController.getInstance().getSetupCompleteFlag(getActivity());
            SharedPreferenceManager preferenceManager = SharedPreferenceManager.getInstance(
                    getActivity(), AppConstants.AUTH_CODE_PREF_NAME);
            long welcomeScreenDisplayCounter = preferenceManager.getLong(AppConstants.WELCOME_SCREEN_DISPLAY_COUNTER, 0l);

            if (Util.isEmptyString(setUpCompleteFlag)) {
                AppConstants.setWelcomeScreensDisplayResult("0");
                return false;
            }

            if (setUpCompleteFlag != null && setUpCompleteFlag.equalsIgnoreCase(AppConstants.TUTORIALS_COMPLETE_STATUS_YES)) {
                AppConstants.setWelcomeScreensDisplayResult("0"); // hide the welcome screens
                return false;
            } else if (welcomeScreenDisplayCounter < AppConstants.WELCOME_SCREEN_DISPLAY_MAX_LIMIT) {
                welcomeScreenDisplayCounter++;
                preferenceManager.putLong(AppConstants.WELCOME_SCREEN_DISPLAY_COUNTER, welcomeScreenDisplayCounter, false);
                if (welcomeScreenDisplayCounter == AppConstants.WELCOME_SCREEN_DISPLAY_MAX_LIMIT) {
                    UpdateSetupIntroCompleteTask updateSetupIntroCompleteTask = new UpdateSetupIntroCompleteTask(getActivity());
                    updateSetupIntroCompleteTask.execute(AppConstants.getSetupCompleteURL());
                }
                AppConstants.setWelcomeScreensDisplayResult("1"); // display the welcome screens
                return true;
            } else {
                AppConstants.setWelcomeScreensDisplayResult("0"); // hide the welcome screens
                return false;
            }
        }
        return ("1").equalsIgnoreCase(AppConstants.getWelcomeScreensDisplayResult());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home, container, false);
        mCardList = view.findViewById(R.id.card_list);
        txtHomeDate = view.findViewById(R.id.txtHomeDate);
        fragmentManager = getActivity().getSupportFragmentManager();
        getTxtHomeGreeting = view.findViewById(R.id.txtHomeMessage);
        mGuideContainer = view.findViewById(R.id.guide_container);
        mGuideContainer.setOnClickListener(this);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mCardList);
        mCardList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        initQuickActionViews(view);
        setBannerLayout();
        initTermsAndConditionBanner(view);
        if(null != getArguments()) {
            if (getArguments().getBoolean("isForRefill")) {

                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
                startActivity(new Intent(getActivity(), RefillRemindersHomeContainerActivity.class));
            }
        }
        return view;
    }

    private void setBannerLayout() {
        List<AnnouncementsItem> bannerToShow = Util.getGenericBannerList(getContext(),FireBaseConstants.ScreenEvent.SCREEN_HOME);
        if (null != bannerToShow && !bannerToShow.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bannerData", (Serializable) bannerToShow);
            Fragment fragment = new GenericBannerFragment();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(com.montunosoftware.mymeds.R.id.banner_container, fragment);
            fragmentTransaction.commit();
            view.findViewById(com.montunosoftware.mymeds.R.id.banner_container).setVisibility(View.VISIBLE);
        }
    }

    private void initQuickActionViews(View view) {
        mBtnAddMedication = view.findViewById(R.id.btn_add_med);
        mBtnRefillRemind = view.findViewById(R.id.btn_refill_medications);
        mBtnSetupReminders = view.findViewById(R.id.btn_setup_reminders);
        mBtnCreateRefillReminders = view.findViewById(R.id.btn_create_refill_remind);
        mBtnFindAPharmacy = view.findViewById(R.id.btn_find_pharmacy);

        mBtnCreateRefillReminders.setOnClickListener(this);
        mBtnAddMedication.setOnClickListener(this);
        mBtnRefillRemind.setOnClickListener(this);
        mBtnSetupReminders.setOnClickListener(this);
        mBtnFindAPharmacy.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PillpopperConstants.isCanShowMedicationList()) {
            PillpopperConstants.setCanShowMedicationList(false);
            // Medication is the first item in the navigation drawer
            mChangeNavigation.changeNavigation(HomeContainerActivity.NavigationHome.MEDICATIONS.getPosition());
        } else if (PillpopperConstants.isCanShowScheduleScreen()) {
            PillpopperConstants.setCanShowScheduleScreen(false);
            mChangeNavigation.changeNavigation(HomeContainerActivity.NavigationHome.DAILY_SCHEDULE.getPosition());
        }
        mCurrentStateOfBatteryOptimization = Util.isBatteryOptimizationCardRequired(getActivity());
        if(mBatteryOptimizationFlag != mCurrentStateOfBatteryOptimization){
            //There is some change occured for Battery Optimization. Hence refreshing the Adapter for BatteryOptimization Card.
            setAdapter();
            mBatteryOptimizationFlag=mCurrentStateOfBatteryOptimization;
        }
        setHasOptionsMenu(true);
        updateGreeting();
    }

    private void setStateDownloadCompleteReceiver() {
        mGetStateReceiverIntentFilter = new IntentFilter();
        mGetStateReceiverIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        try {
            getActivity().registerReceiver(mGetStateBroadcastReceiver, mGetStateReceiverIntentFilter);
        }catch (Exception e){
            PillpopperLog.exception(e.getMessage());
        }
    }

    private void setAdapter() {
        if (!PillpopperRunTime.getInstance().isFirstTimeSyncDone()) {
            return;
        }
        displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        initHomeCards();
    }

    public void scrollToPosition(final int position) {
        if (mCardList != null) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mCardList.getLayoutManager();
            linearLayoutManager.scrollToPositionWithOffset(position, 50);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public interface ChangeNavigation {
        void changeNavigation(int i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_setup_reminders:
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.BULK_REMINDER_ADD,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.QUICK_ACCESS);
                RunTimeData.getInstance().setIsNewScheduleRequired(true);
                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
                if(null!=getActivity()){
                    ((HomeContainerActivity)getActivity()).onSetUpReminderQuickActionClicked();
                }
                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
                break;
            case R.id.btn_add_med:
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.ADD_MEDS,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.QUICK_ACCESS);

                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
                Intent intent = new Intent(getActivity(), AddOrEditMedicationActivity.class);
                intent.putExtra(PillpopperConstants.LAUNCH_SOURCE, PillpopperConstants.LAUNCH_SOURCE_SCHEDULE);
                intent.putExtra(PillpopperConstants.LAUNCH_MODE, PillpopperConstants.ACTION_CREATE_PILL);
                PillpopperConstants.setCanShowMedicationList(true);
                startActivity(intent);
                break;
            case R.id.btn_refill_medications:
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.REFILL_MEDS,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.QUICK_ACCESS);
                launchLocatorFragment =false;
                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
                if (null != getActivity()) {
                    ((HomeContainerActivity) getActivity()).onRxRefillMedicationsQuickActionClicked();
                }
                break;

            case R.id.btn_create_refill_remind:
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.REFILL_REMINDER_CREATE,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.QUICK_ACCESS);
                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
                ((HomeContainerActivity)getActivity()).onCreateARefillQuickActionClicked();
                break;

            case R.id.btn_find_pharmacy:
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.FIND_PHARMACY,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.QUICK_ACCESS);
                ((HomeContainerActivity)getActivity()).onFindAPharmacyQuickActionClicked();
                break;

            case R.id.guide_container:
                ((HomeContainerActivity)getActivity()).onGuideQuickActionClicked();
                break;
        }
    }


    /**
     * Dismisses the loading indicator.
     */
    private void dismissLoadingActivity() {
        if (null != getActivity()) {
            getActivity().finishActivity(0);
        }
    }

    private void launchRxRefillLocatorFragment() {
        Intent intent = new Intent(mContext, RxRefillHomeContainerActivity.class);
        intent.putExtra("launchPharmacyLocatorFragment",launchLocatorFragment);
        startActivity(intent);
    }

    private void updateGreeting() {
        txtHomeDate.setText(Util.getHomeDate());
        getTxtHomeGreeting.setText(Util.getHomeGreeting(getActivity()));
    }

    public void refreshHomeCards() {
        if (RunTimeData.getInstance().isInitialGetStateCompleted()) {
            setAdapter();
        }
    }

    // refreshing the cards when a refill reminder alarm is triggered in the home screen
    private BroadcastReceiver refreshRefillCards = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (RunTimeData.getInstance().isAppInExpandedCard()) {
                RunTimeData.getInstance().setRefreshHomeCardsPending(true);
            } else {
                if (RunTimeData.getInstance().isFirstTimeLandingOnHomeScreen()) {
                    RunTimeData.getInstance().setRefreshHomeCardsPending(false);
                    setAdapter();
                }
            }
        }
    };

    private BroadcastReceiver refreshCurrentReminderCards = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (RunTimeData.getInstance().isAppInExpandedCard()) {
                RunTimeData.getInstance().setRefreshHomeCardsPending(true);
            } else {
                RunTimeData.getInstance().setRefreshHomeCardsPending(false);
                try {
                    setAdapter();
                } catch (Exception e) {
                    PillpopperLog.say(e);
                }
            }
        }
    };

    private BroadcastReceiver refreshReminderCards = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAdapter();
        }
    };

    private BroadcastReceiver teenProxyHomeCard = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAdapter();
        }
    };

    private BroadcastReceiver refreshGenericHomeCard = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAdapter();
        }
    };

    private BroadcastReceiver refreshKPHCCardsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAdapter();
        }
    };

    private void removeRegistrationPopup() {
        Intent initialGetFailedBroadcastIntent = new Intent();
        initialGetFailedBroadcastIntent.setAction(BROADCAST_REMOVE_REGISTRATION_POPUP);
        if(getActivity() != null) {
            getActivity().sendBroadcast(initialGetFailedBroadcastIntent);
        }
        RunTimeData.getInstance().setInitialGetStateCompleted(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        RunTimeData.getInstance().setHomeCardsShown(true);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mGetStateBroadcastReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshRefillCards);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshCurrentReminderCards);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshReminderCards);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(teenProxyHomeCard);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshGenericHomeCard);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshKPHCCardsReceiver);
        context = null;
        getActivity().finishActivity(0);
        super.onDestroy();
    }


    private void initTermsAndConditionBanner(View view) {
        if (mSharedPrefManager.getBoolean(AppConstants.TC_BANNER_TO_BE_SHOWN, false)) {
            mTermsAndConditionsBanner = view.findViewById(R.id.terms_and_conditions_card);
            mTermsAndConditionsBanner.setVisibility(View.VISIBLE);
            mView = view.findViewById(R.id.btn_view);
            mDismiss = view.findViewById(R.id.btn_dismiss);
            mDismiss.setOnClickListener(v -> removeTCInterruptBanner());
            mView.setOnClickListener(v -> {
                removeTCInterruptBanner();
                Intent i = new Intent(getContext(), PrivacyAndTC.class);
                i.putExtra("Type","Interrupt TnC");
                i.putExtra("url", getResources().getString(R.string.lbl_term_and_conditions));
                startActivity(i);
            });
        }
    }

    private void removeTCInterruptBanner() {
        mSharedPrefManager.putBoolean(AppConstants.TC_BANNER_TO_BE_SHOWN, false, false);
        Animation animate = AnimationUtils.loadAnimation(mContext, R.anim.signin_banner_slide_up);
        animate.reset();
        mTermsAndConditionsBanner.clearAnimation();
        animate.setFillAfter(true);
        mTermsAndConditionsBanner.startAnimation(animate);
        mBannerRunnable = () -> mTermsAndConditionsBanner.setVisibility(View.GONE);
        mBannerHandler.postDelayed(mBannerRunnable, 500);
    }
}
