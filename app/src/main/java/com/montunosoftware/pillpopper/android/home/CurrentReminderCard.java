package com.montunosoftware.pillpopper.android.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.GreatJobAlertForTakenAllActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.ReminderSnoozePicker;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.app.Activity.RESULT_OK;


public class CurrentReminderCard implements HomeCard, Parcelable, CurrentReminderRefreshInterface {

    private Context context;
    private Long reminderTime;
    private TreeMap<User, LinkedList<Drug>> currentReminderByUserName;
    private List<Drug> finalDrugs;
    private List<Drug> listOfDrugsToBeTaken;
    private long takenEarlierTime;
    private long postponeTimeSeconds;
    private int cardIndex;

    private static final int TAKEN_CONTRACTED = 1;
    private static final int SKIPPED_CONTRACTED = 2;
    private static final int TAKEN_EXPANDED = 3;
    private static final int SKIPPED_EXPANDED = 4;
    private static final int TAKEN_EARLIER = 5;
    private static final int REMINDER_LATER = 6;
    private int initialDrugslistSize = 0;

    public CurrentReminderCard() {

    }

    public CurrentReminderCard(Context context, Long reminderTime, int cardIndex) {
        this.reminderTime = reminderTime;
        this.context = context;
        this.currentReminderByUserName = getCurrentReminderByUserName();
        this.cardIndex = cardIndex;
    }

    protected CurrentReminderCard(Parcel in) {
        this.reminderTime = in.readLong();
        this.cardIndex = in.readInt();
    }

    public static final Creator<CurrentReminderCard> CREATOR = new Creator<CurrentReminderCard>() {
        @Override
        public CurrentReminderCard createFromParcel(Parcel in) {
            return new CurrentReminderCard(in);
        }

        @Override
        public CurrentReminderCard[] newArray(int size) {
            return new CurrentReminderCard[size];
        }
    };

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getCardView() {
        return R.layout.current_reminder_home_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_current_reminder;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getBanner() {
        return 0;
    }

    @Override
    public int getRequestCode() {
        return PillpopperConstants.REQUEST_CURRENT_REMINDER_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getReminderDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMMM dd");
        Date reminderDate = new Date();
        reminderDate.setTime(reminderTime);
        return simpleDateFormat.format(reminderDate);
    }

    public String getReminderTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        Date reminderDate = new Date();
        reminderDate.setTime(reminderTime);
        return simpleDateFormat.format(reminderDate);
    }

    public String getUserNames() {
        String separator = ", ";
        StringBuilder sb = new StringBuilder();
        for (User user : getCurrentReminderByUserName().keySet()) {
            sb.append(user.getDisplayName()).append(separator);
        }
        return sb.substring(0, sb.toString().trim().length() - 1);
    }


    public String getTakenText() {
        return isMultipleDrugAvailable() ?
                context.getString(R.string.taken_all) :
                context.getString(R.string.taken);
    }

    public String getSkipText() {
        return isMultipleDrugAvailable() ?
                context.getString(R.string.skipped_all) :
                context.getString(R.string.skipped);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(reminderTime);
        dest.writeInt(cardIndex);
    }

    private boolean isMultipleDrugAvailable() {
        int drugCount = 0;
        for (Map.Entry<User, LinkedList<Drug>> userLinkedListEntry : getCurrentReminderByUserName().entrySet()) {
            int listSize = userLinkedListEntry.getValue().size();
            if (listSize > 1) {
                return true;
            } else {
                drugCount += listSize;
            }
        }
        return drugCount > 1;
    }

    private synchronized TreeMap<User, LinkedList<Drug>> getCurrentReminderByUserName() {
        if (currentReminderByUserName == null) {
            currentReminderByUserName = new TreeMap<>();
            List<Drug> drugs = PillpopperRunTime.getInstance().getCurrentRemindersByUserIdForCard().get(reminderTime);
            FrontController frontController = FrontController.getInstance(context);
            if (null != drugs && !drugs.isEmpty()) {
                for (Drug drug : drugs) {
                    User user = frontController.getUserById(drug.getUserID());
                    if (!currentReminderByUserName.containsKey(user)) {
                        currentReminderByUserName.put(user, new LinkedList<>());
                    }
                    currentReminderByUserName.get(user).add(drug);
                }
            }
        }
        return currentReminderByUserName;
    }

    public void initDetailView(Context context, RecyclerView recyclerView) {
        RunTimeData.getInstance().setCurrentReminderCardRefreshRequired(false);
        initialDrugslistSize = getCurrentReminderDrugsCount();
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new CurrentReminderExpandedAdapter(context, getCurrentReminderByUserName(), reminderTime, cardIndex));
        Util.saveCardIndex(context, cardIndex);
    }

    public int getCurrentReminderDrugsCount() {
        int drugsCount = 0;
        TreeMap<User, LinkedList<Drug>> currentReminderByUserName = getCurrentReminderByUserName();
        if(null != currentReminderByUserName) {
            User[] users = currentReminderByUserName.keySet().toArray(new User[currentReminderByUserName.keySet().size()]);
            for (User user : users) {
                for (Drug drug : currentReminderByUserName.get(user)) {
                    if (drug.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                        drugsCount++;
                    }
                }
            }
        }
        return drugsCount;
    }

    public void setFooterButtons(TextView takeLater, TextView takeEarlier) {
        try {
            if (reminderTime != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(reminderTime);

                if ((Calendar.getInstance().getTime()).after(cal.getTime()) && (Calendar.getInstance().getTimeInMillis() - reminderTime) > PillpopperConstants.LATE_REMINDER_INTERVAL) {
                    takeLater.setVisibility(View.GONE);
                    takeEarlier.setVisibility(View.VISIBLE);
                    if (!isMultipleDrugAvailable()) {
                        takeEarlier.setText(context.getResources().getString(R.string.card_taken_earlier));
                    } else {
                        takeEarlier.setText(context.getResources().getString(R.string.taken_all_earlier));
                    }
                } else {
                    takeEarlier.setVisibility(View.GONE);
                    takeLater.setVisibility(View.VISIBLE);
                    takeLater.setText(context.getResources().getString(R.string.remind_later_footer_text));
                }
            }
        } catch (Exception e) {
            PillpopperLog.say("Exception Occurs", e);
        }
    }

    public void onTaken(View view) {
        createFinalDrugListForAction();
        if (view.getTag().equals(context.getString(R.string.contract))) {
            new performButtonsActionAsyncTask().execute(TAKEN_CONTRACTED);
        } else if (view.getTag().equals(context.getString(R.string.expanded))) {
            new performButtonsActionAsyncTask().execute(TAKEN_EXPANDED);
        }
    }

    public void onSkipped(final View view) {
        createFinalDrugListForAction();
        DialogHelpers.showSkipMedDialog(context, finalDrugs.size() ==1 ? PillpopperConstants.ACTION_SKIP_PILL : PillpopperConstants.ACTION_SKIP_ALL_PILL, () -> {
            if (view.getTag().equals(context.getString(R.string.contract))) {
                new performButtonsActionAsyncTask().execute(SKIPPED_CONTRACTED);
            } else if (view.getTag().equals(context.getString(R.string.expanded))) {
                new performButtonsActionAsyncTask().execute(SKIPPED_EXPANDED);
            }
        });
    }

    public void onTakenEarlier(final View view) {
        createFinalDrugListForAction();
        DateAndTimePickerDialog dateAndTimePickerDialog = new DateAndTimePickerDialog(
                context,
                pillpopperTime -> {
                    takenEarlierTime = pillpopperTime.getGmtSeconds();
                    new performButtonsActionAsyncTask().execute(TAKEN_EARLIER);

                }, false, PillpopperTime.now(), 15, context.getResources().getString(R.string.taken_text), true
        );
        dateAndTimePickerDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "taken_earlier_time");
    }

    public void onRemindLater(View view) {
        createFinalDrugListForAction();
        ReminderSnoozePicker reminderSnoozePickerDialog = new ReminderSnoozePicker();
        reminderSnoozePickerDialog.setHourMinutePickedListener(hhmm -> {
            if (hhmm == null) {
                return;
            }
            postponeTimeSeconds = (hhmm[0] * 60 + hhmm[1]) * 60;
            String postponeError = Drug.validatePostpones(finalDrugs, postponeTimeSeconds, context);
            if (postponeError != null) {
                DialogHelpers.showPostponeErrorAlert(context);
            } else {
                new performButtonsActionAsyncTask().execute(REMINDER_LATER);
            }
        });
        reminderSnoozePickerDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "remind_later_time");
    }

    private void createFinalDrugListForAction() {
        if (finalDrugs == null) {
            finalDrugs = new ArrayList<>();
        } else {
            finalDrugs.clear();
        }
        listOfDrugsToBeTaken = PillpopperRunTime.getInstance().getCurrentRemindersByUserIdForCard().get(reminderTime);
        if (listOfDrugsToBeTaken != null && !listOfDrugsToBeTaken.isEmpty()) {
            for (Drug d : listOfDrugsToBeTaken) {
                if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                    finalDrugs.add(d);
                }
            }
        }
    }


    private class performButtonsActionAsyncTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((Activity) context).startActivityForResult(new Intent(context, LoadingActivity.class), 0);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LoggerUtils.exception("Exception - onPreExecute ", e);
            }
            switch (params[0]) {
                case TAKEN_CONTRACTED:
                case TAKEN_EXPANDED:
                    Util.saveCardIndex(context, cardIndex);
                    if (finalDrugs.size() != 0) {
                        FrontController.getInstance(context).performTakeDrug(finalDrugs, null, context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                    }
                    break;
                case SKIPPED_CONTRACTED:
                    if (finalDrugs.size() != 0) {
                        FrontController.getInstance(context).performSkipDrug(finalDrugs, PillpopperTime.now(), context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                    }
                    Util.saveCardIndex(context, cardIndex);
                    break;
                case SKIPPED_EXPANDED:
                    Util.saveCardIndex(context, cardIndex);
                    if (finalDrugs.size() != 0) {
                        FrontController.getInstance(context).performSkipDrug(finalDrugs, PillpopperTime.now(), context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                    }
                    break;
                case TAKEN_EARLIER:
                    Util.saveCardIndex(context, cardIndex);
                    FrontController.getInstance(context).performAlreadyTakenDrugs(finalDrugs, new PillpopperTime(takenEarlierTime), context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                    break;
                case REMINDER_LATER:
                    Util.saveCardIndex(context, cardIndex);
                    FrontController.getInstance(context).performPostponeDrugs(finalDrugs, postponeTimeSeconds, context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                    if (FrontController.getInstance(context).getPassedReminderDrugs(context).size() > 0) {
                        FrontController.getInstance(context).updateAsPendingRemindersPresent(context);
                    }
                    break;
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Integer param) {
            super.onPostExecute(param);
            if (null != context) {
                ((Activity) context).finishActivity(0);
            }
            if (FrontController.getInstance(context).getPassedReminderDrugs(context).size() > 0) {
                FrontController.getInstance(context).updateAsPendingRemindersPresent(context);
            }
            if (null != context) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_REMINDERS_CARDS_AFTER_ACTION"));
            }
            PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
            if (null != context) {
                switch (param) {
                    case TAKEN_CONTRACTED:
                        if ((null == PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId() || PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId().isEmpty())) {
                            context.startActivity(new Intent(context, GreatJobAlertForTakenAllActivity.class));
                        }
                        break;
                    case TAKEN_EXPANDED:
                    case TAKEN_EARLIER:
                        if ((null == PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId() || PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId().isEmpty())) {
                            ((Activity) context).startActivityForResult(new Intent(context, GreatJobAlertForTakenAllActivity.class), getRequestCode());
                        } else {
                            delayFinish((Activity) context, RESULT_OK);
                        }
                        break;
                    case SKIPPED_CONTRACTED:
                        break;
                    case SKIPPED_EXPANDED:
                        delayFinish((Activity) context, RESULT_OK);
                        break;
                    case REMINDER_LATER:
                        PillpopperConstants.setIsCurrentReminderRefreshRequired(true);
                        delayFinish((Activity) context, RESULT_OK);
                        break;
                }
            }
        }
    }

    private void delayFinish(final Activity activity, final int resultCode) {
        activity.finishActivity(0);
        new Handler().post(() -> {
            try {
                activity.setResult(resultCode);
                activity.finish();
            } catch (Exception e) {
                PillpopperLog.say(e);
            }
        });
    }

    public void doRefresh() {
        new Handler().postDelayed(() -> {
            PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
            RunTimeData.getInstance().resetRefreshCardsFlags();
            try {
                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_CURRENT_REMINDERS"));
            } catch(Exception ex){
                PillpopperLog.say(ex);
            }
        }, 250);
    }

    public boolean isButtonRefreshRequired() {

        if (initialDrugslistSize == 1) {
            return false;
        } else if(initialDrugslistSize > 1 ){
            if(getCurrentReminderDrugsCount() == 1) {
                return true;
            }
        }
        return false;
    }
}
