package com.montunosoftware.pillpopper.android.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.BatteryOptimizerDetailsCardBinding;
import com.montunosoftware.mymeds.databinding.CardDetailKphcExpandedCardBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutCurrentReminderBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutDiscontinueKphcBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutLateRemindersBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutManageMemberBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutRefillBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutRefillReminderBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutSetupReminderBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutViewMedicationsBinding;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutViewReminderBinding;
import com.montunosoftware.mymeds.databinding.GenericHomeCardDetailLayoutBinding;
import com.montunosoftware.mymeds.databinding.TeenProxyHomeCardDetailLayoutBinding;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.GenericCardAndBannerUtility;
import com.montunosoftware.pillpopper.android.StateListenerActivity;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog;
import com.montunosoftware.pillpopper.android.view.ReminderSnoozePicker;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

public class HomeCardDetailActivity extends StateListenerActivity {

    private HomeCard homeCard;
    private View decorView;
    private HomeCardDetailActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = HomeCardDetailActivity.this;
        initializeUi();
    }

    private void initializeUi() {
        Intent intent = getIntent();
        homeCard = intent.getParcelableExtra("card");
        homeCard.setContext(this);

        switch (homeCard.getRequestCode()){
            case PillpopperConstants.REQUEST_SETUP_VIEW_MEDS_CARD_DETAIL:
                CardDetailLayoutViewMedicationsBinding medicationsBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                medicationsBinding.setHandler((ViewMedicationCard) homeCard);
                break;

            case PillpopperConstants.REQUEST_SETUP_MANAGE_MEMBERS_CARD_DETAIL:
                CardDetailLayoutManageMemberBinding manageMemberBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                manageMemberBinding.setHandler((ManageMemberCard) homeCard);
                final RecyclerView membersRecyclerView = findViewById(R.id.membersRecyclerView);
                ((ManageMemberCard) homeCard).initProxyUserList(membersRecyclerView);
                TextView descriptionText = findViewById(R.id.card_description);
                descriptionText.setMovementMethod(LinkMovementMethod.getInstance());
                break;

            case PillpopperConstants.REQUEST_SETUP_REMINDER_CARD_DETAIL:
                CardDetailLayoutSetupReminderBinding reminderBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                reminderBinding.setHandler((SetupReminderCard) homeCard);
                break;

            case PillpopperConstants.REQUEST_VIEW_REMINDER_CARD_DETAIL:
                CardDetailLayoutViewReminderBinding viewReminderBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                viewReminderBinding.setHandler((ViewReminderCard) homeCard);
                break;

            case PillpopperConstants.REQUEST_REFILL_CARD_DETAIL:
                CardDetailLayoutRefillBinding refillBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                refillBinding.setHandler((RefillCard) homeCard);
                break;

            case PillpopperConstants.REQUEST_NEW_KPHC_CARD_DETAIL:
            case PillpopperConstants.REQUEST_UPDATED_KPHC_CARD_DETAIL:
                CardDetailKphcExpandedCardBinding binding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                final RecyclerView kphcRecyclerView = findViewById(R.id.recycler_med_list);
                ((KPHCCards) homeCard).initKPHCDetailView(kphcRecyclerView,getIntent().getStringExtra("Id"));
                binding.setHandler((KPHCCards) homeCard);
                break;
            case PillpopperConstants.REQUEST_LATE_REMINDER_CARD_DETAIL:
                CardDetailLayoutLateRemindersBinding lateRemindersBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                final RecyclerView lateReminderRecyclerView = findViewById(R.id.recycler_late_reminder_list);
                final Button takeButton = findViewById(R.id.card_footer_taken_all);
                final Button skipButton = findViewById(R.id.card_footer_skip_all);
                ((LateRemindersHomeCard) homeCard).initDetailView(lateReminderRecyclerView, takeButton, skipButton, getIntent().getStringExtra("Id"),
                        getIntent().getIntExtra("numberOfLateReminderUsers", 0));
                lateRemindersBinding.setHandler((LateRemindersHomeCard) homeCard);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(refreshCurrentReminderCards,
                        new IntentFilter("REFRESH_CURRENT_REMINDERS_FROM_EXPANDED_CARD"));
                break;
            case PillpopperConstants.REQUEST_REFILL_REMINDER_CARD_DETAIL:
                CardDetailLayoutRefillReminderBinding refillReminderBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                ((RefillReminderOverdueCard) homeCard).initDetailView();
                refillReminderBinding.setHandler((RefillReminderOverdueCard)homeCard);
                break;
            case PillpopperConstants.REQUEST_CURRENT_REMINDER_CARD_DETAIL:
                CardDetailLayoutCurrentReminderBinding currentReminderBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                RecyclerView currentReminderRecyclerView = findViewById(R.id.recycler_current_reminder_list);
                ((CurrentReminderCard) homeCard).initDetailView(this,currentReminderRecyclerView);
                TextView remindMeLater = findViewById(R.id.footer_remind_later_text);
                TextView takenEarlier = findViewById(R.id.footer_taken_earlier);
                ((CurrentReminderCard) homeCard).setFooterButtons(remindMeLater, takenEarlier);
                currentReminderBinding.setHandler((CurrentReminderCard) homeCard);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(refreshCurrentReminderCards,
                        new IntentFilter("REFRESH_CURRENT_REMINDERS_FROM_EXPANDED_CARD"));
                break;

            case PillpopperConstants.REQUEST_KPHC_DISCONTINUED_CARD_DETAIL:
                CardDetailLayoutDiscontinueKphcBinding kphcDiscontinuedBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                RecyclerView kphcDiscontinuedRecyclerView = findViewById(R.id.recycler_kphc_discontinue_list);
                ((KPHCDiscontinueCard) homeCard).initDetailView(kphcDiscontinuedRecyclerView);
                kphcDiscontinuedBinding.setHandler((KPHCDiscontinueCard) homeCard);
                break;

            case PillpopperConstants.REQUEST_VIEW_BATTERY_OPTIMIZER_CARD_DETAIL:
                BatteryOptimizerDetailsCardBinding batteryOptimizerDetailsCardBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                batteryOptimizerDetailsCardBinding.setHandler((BatteryOptimizerInfoCard)homeCard);
                break;
            case PillpopperConstants.TEEN_PROXY_HOME_CARD_DETAIL:
                TeenProxyHomeCardDetailLayoutBinding teenProxyHomeCard = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                teenProxyHomeCard.setActivity((TeenProxyHomeCard) homeCard);
                break;
            case PillpopperConstants.GENERIC_HOME_CARD_DETAIL:
                FireBaseAnalyticsTracker.getInstance().logEvent(this, FireBaseConstants.Event.GENERIC_CARD_CLICK, FireBaseConstants.ParamName.SOURCE, FireBaseConstants.ScreenEvent.SCREEN_HOME);
                GenericHomeCardDetailLayoutBinding genericHomeCardBinding = DataBindingUtil.setContentView(this, homeCard.getDetailView());
                final WebView messageTextView = findViewById(R.id.description_text);
                final Button kpButton = findViewById(R.id.kpButton);
                final Button acknowledgeButton = findViewById(R.id.acknowledge_button);
                ((GenericHomeCard) homeCard).initDetailView(messageTextView,kpButton,acknowledgeButton);
                genericHomeCardBinding.setAnnouncement(((GenericHomeCard) homeCard).getAnnouncementCard());
                genericHomeCardBinding.setActivity((GenericHomeCard) homeCard);
                break;
        }

        decorView = getWindow().getDecorView();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener (visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        });

        ImageView closeButton = findViewById(R.id.card_detail_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(view -> {
                if (homeCard instanceof SetupReminderCard)
                    setResult(RESULT_OK);
                else if (homeCard instanceof ManageMemberCard)
                    setResult(RESULT_CANCELED);
                else if (homeCard instanceof LateRemindersHomeCard){
                    if(((LateRemindersHomeCard)homeCard).isButtonRefreshRequired(getIntent().getStringExtra("Id"))) {
                        setResult(AppConstants.LATE_REMINDERS_CONTRACTED_CARD_REFRESH_RESULT_CODE, getIntent());
                    }
                } else if(homeCard instanceof CurrentReminderCard){
                    if(RunTimeData.getInstance().isCurrentReminderCardRefreshRequired() || ((CurrentReminderCard) homeCard).isButtonRefreshRequired()){
                        setResult(AppConstants.CURRENT_REMINDERS_CONTRACTED_CARD_REFRESH_RESULT_CODE, getIntent());
                    }
                }
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                ActivityCompat.finishAfterTransition(mActivity);

                if(RunTimeData.getInstance().isRefreshHomeCardsPending()){
                    sendRefreshCardsBroadcastWithDelay();
                }
            });
        }
    }

    private BroadcastReceiver refreshCurrentReminderCards = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ActivityCompat.finishAfterTransition(mActivity);
            RunTimeData.getInstance().setRefreshHomeCardsPending(true);
            RunTimeData.getInstance().setBackFromExpandedCard(true);
            sendRefreshCardsBroadcastWithDelay();
        }
    };

    private void sendRefreshCardsBroadcastWithDelay() {
        RunTimeData.getInstance().resetRefreshCardsFlags();
        try {
            RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent("REFRESH_CURRENT_REMINDERS"));
        } catch (Exception ex) {
            RefillReminderLog.say(ex);
        }
    }

    @Override
    public void onBackPressed() {
        if (homeCard.getRequestCode() == PillpopperConstants.REQUEST_SETUP_REMINDER_CARD_DETAIL) {
            setResult(RESULT_OK);
        }
        ActivityCompat.finishAfterTransition(mActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            if (requestCode == PillpopperConstants.REQUEST_SETUP_REMINDER_CARD_DETAIL || requestCode == PillpopperConstants.REQUEST_SETUP_VIEW_MEDS_CARD_DETAIL) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }else if(requestCode == PillpopperConstants.REQUEST_CURRENT_REMINDER_CARD_DETAIL && resultCode == RESULT_OK){
            finish();
        }
        else if (resultCode == RESULT_OK){
            if (requestCode == PillpopperConstants.REQUEST_SETUP_REMINDER_CARD_DETAIL){
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if(homeCard instanceof CurrentReminderCard || homeCard instanceof LateRemindersHomeCard){
            AppConstants.IS_IN_EXPANDED_HOME_CARD = true;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideStatusAndNavigationBar();
    }

    private void hideStatusAndNavigationBar(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onStop() {
        RunTimeData.getInstance().setBackFromExpandedCard(true);
        getState().unregisterStateUpdatedListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RunTimeData.getInstance().setAppInExpandedCard(false);
        if(null != mActivity) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(refreshCurrentReminderCards);
        }
        if(RunTimeData.getInstance().isRefreshHomeCardsPending()){
            sendRefreshCardsBroadcastWithDelay();
        }
        mActivity = null;
        AppConstants.IS_IN_EXPANDED_HOME_CARD = false;
        if (homeCard instanceof GenericHomeCard) {
            GenericCardAndBannerUtility.dismissDialog();
        }
    }

    @Override
    public void onPause() {
        try {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("remind_later_time");
            if (prev != null) {
                ReminderSnoozePicker reminderSnoozePicker = (ReminderSnoozePicker) prev;
                reminderSnoozePicker.dismiss();
            }
            prev = getSupportFragmentManager().findFragmentByTag("taken_earlier_time");
            if (prev != null) {
                DateAndTimePickerDialog takeEarlierDialog = (DateAndTimePickerDialog) prev;
                takeEarlierDialog.dismiss();
            }
        } catch (Exception ex){
            LoggerUtils.error(ex.getMessage());
        }
        super.onPause();
    }
}
