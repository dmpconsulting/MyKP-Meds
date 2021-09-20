package com.montunosoftware.pillpopper.android.inAppReminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.CardDetailLayoutCurrentReminderBinding;
import com.montunosoftware.pillpopper.android.StateListenerActivity;
import com.montunosoftware.pillpopper.android.home.CurrentReminderCard;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog;
import com.montunosoftware.pillpopper.android.view.ReminderSnoozePicker;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CurrentReminderDetailActivity extends StateListenerActivity {

    private CardDetailLayoutCurrentReminderBinding binding;
    private CurrentReminderCard currentReminderCard;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this, R.layout.card_detail_layout_current_reminder);
        initUI();
    }

    private void initUI() {
        initCurrentReminderCard();
        RecyclerView currentReminderRecyclerView = findViewById(R.id.recycler_current_reminder_list);
        if (null != currentReminderCard) {
            currentReminderCard.setContext(this);
            currentReminderCard.initDetailView(this, currentReminderRecyclerView);
            TextView remindMeLater = findViewById(R.id.footer_remind_later_text);
            TextView takenEarlier = findViewById(R.id.footer_taken_earlier);
            currentReminderCard.setFooterButtons(remindMeLater, takenEarlier);
            binding.setHandler(currentReminderCard);
            ImageView closeButton = findViewById(R.id.card_detail_close);
            closeButton.setOnClickListener(view -> {
                if (RunTimeData.getInstance().isCurrentReminderCardRefreshRequired() || (currentReminderCard).isButtonRefreshRequired()) {
                    setResult(AppConstants.CURRENT_REMINDERS_CONTRACTED_CARD_REFRESH_RESULT_CODE, getIntent());
                }

                if (RunTimeData.getInstance().isRefreshHomeCardsPending()) {
                    sendRefreshCardsBroadcastWithDelay();
                }
                RunTimeData.getInstance().setHistoryMedChanged(true); // to refresh history if in history screen
                finish();
            });
            LocalBroadcastManager.getInstance(this).registerReceiver(refreshCurrentReminderCards,
                    new IntentFilter("REFRESH_CURRENT_REMINDERS_FROM_EXPANDED_CARD"));
        } else {
            // something went wrong
            finish();
        }
    }

    private void initCurrentReminderCard() {
        try {
            List<Drug> drugList = getOverDueDrugList();
            if (!drugList.isEmpty()) {
                insertEligiblePastRemindersToDB();
                Util.getInstance().prepareRemindersMapData(drugList, this);
                if (FrontController.getInstance(this).getPassedReminderDrugs(this).size() > 0) {
                    FrontController.getInstance(this).updateAsPendingRemindersPresent(this);
                } else {
                    FrontController.getInstance(this).updateAsNoPendingReminders(this);
                }

                PillpopperRunTime.getInstance().setCurrentRemindersByUserIdForCard(PillpopperRunTime.getInstance().getmCurrentRemindersMap());
                for (Map.Entry<Long, List<Drug>> entry : PillpopperRunTime.getInstance().
                        getCurrentRemindersByUserIdForCard().entrySet()) {
                    currentReminderCard = new CurrentReminderCard(this, entry.getKey(), 0);
                }
            }
        } catch (Exception e) {
            PillpopperLog.say(e);
        }
    }

    private List<Drug> getOverDueDrugList() {
        List<Drug> overDrugList = new ArrayList<>();
        for (final Drug d : FrontController.getInstance(this).getDrugListForOverDue(this)) {
            d.computeDBDoseEvents(this, d, PillpopperTime.now(), 60);
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
                || (FrontController.getInstance(this).isEntryAvailableInPastReminder(drug.getGuid(), drug.getOverdueDate()))
                && (null != drug.getOverdueDate() && null != drug.getCreated() && !drug.getOverdueDate().before(drug.getCreated()))) {
            return true;
        }
        return false;
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
                Util.getInstance().insertPastRemindersPillIdsIntoDB(this, passedRemindersHashMapByUserId);
            }
        }
    }

    private BroadcastReceiver refreshCurrentReminderCards = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ActivityCompat.finishAfterTransition(CurrentReminderDetailActivity.this);
            RunTimeData.getInstance().setRefreshHomeCardsPending(true);
            RunTimeData.getInstance().setBackFromExpandedCard(true);
            sendRefreshCardsBroadcastWithDelay();
        }
    };

    private void sendRefreshCardsBroadcastWithDelay() {
        RunTimeData.getInstance().resetRefreshCardsFlags();
        try {
            RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("REFRESH_CURRENT_REMINDERS"));
        } catch (Exception ex) {
            RefillReminderLog.say(ex);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppConstants.IS_IN_EXPANDED_HOME_CARD = true;
    }

    @Override
    public void onBackPressed() {
        if (currentReminderCard.getRequestCode() == PillpopperConstants.REQUEST_SETUP_REMINDER_CARD_DETAIL) {
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onStop() {
        RunTimeData.getInstance().setBackFromExpandedCard(true);
        getState().unregisterStateUpdatedListener(this);
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        AppConstants.IS_IN_EXPANDED_HOME_CARD = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshCurrentReminderCards);
        RunTimeData.getInstance().setAppInExpandedCard(false);
        if (RunTimeData.getInstance().isRefreshHomeCardsPending()) {
            sendRefreshCardsBroadcastWithDelay();
        }
        super.onDestroy();
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
        } catch (Exception ex) {
            LoggerUtils.error(ex.getMessage());
        }
        RunTimeData.getInstance().setHistoryMedChanged(true); // to refresh history if in history screen
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PillpopperConstants.REQUEST_CURRENT_REMINDER_CARD_DETAIL && resultCode == RESULT_OK) {
            finish();
        }
    }
}