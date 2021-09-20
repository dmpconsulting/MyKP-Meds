package com.montunosoftware.pillpopper.android.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.LateRemindersActionInterface;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by M1024581 on 11/23/2017.
 */

public class LateRemindersHomeCard implements Parcelable, HomeCard, LateRemindersActionInterface {
    private String userName;
    private Context context;
    private List<Drug> mPastReminderList;
    private String userID;
    private int isLastUser;
    private int cardIndex;

    private static final int TAKEN_CONTRACTED = 1;
    private static final int SKIPPED_CONTRACTED = 2;
    private static final int TAKEN_EXPANDED = 3;
    private static final int SKIPPED_EXPANDED = 4;

    private int initialDrugslistSize = 0;

    public LateRemindersHomeCard() {

    }

    public LateRemindersHomeCard(String userId, String username , List<Drug> pastReminderList, int isLastUser, int index) {
        this.userID = userId;
        this.userName = username;
        this.mPastReminderList = pastReminderList;
        this.isLastUser = isLastUser;
        this.cardIndex = index;
    }

    public LateRemindersHomeCard(Parcel in) {
        userName = in.readString();
        cardIndex = in.readInt();
        isLastUser = in.readInt();
        userID = in.readString();
        this.cardIndex = in.readInt();
    }

    @Override
    public void setContext(Context context) {
        this.context=context;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getCardView() {
        return R.layout.late_reminders_home_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_late_reminders;
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
        return PillpopperConstants.REQUEST_LATE_REMINDER_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return view.getContext().getString(R.string.late_reminders_card);
    }

    public static final Parcelable.Creator<LateRemindersHomeCard> CREATOR = new Parcelable.Creator<LateRemindersHomeCard>() {
        @Override
        public LateRemindersHomeCard createFromParcel(Parcel in) {
            return new LateRemindersHomeCard(in);
        }

        @Override
        public LateRemindersHomeCard[] newArray(int size) {
            return new LateRemindersHomeCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userName);
        parcel.writeInt(cardIndex);
        parcel.writeInt(isLastUser);
        parcel.writeString(userID);
        parcel.writeInt(cardIndex);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void initDetailView(RecyclerView recyclerView, Button takeButton, Button skipButton, String userId, int numberOfLateReminderUsers) {
        mPastReminderList = getPassedReminderDrugListByUserId(userId);
        initialDrugslistSize = mPastReminderList.size();
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        this.userID = userId;
       // this.isLastUser = numberOfLateReminderUsers==1?true:false;
        if (mPastReminderList.size() == 1) {
            takeButton.setText(R.string.take);
            skipButton.setText(R.string.skipped);
        } else {
            takeButton.setText(R.string.taken_all);
            skipButton.setText(R.string.skipped_all);
        }
        Util.saveCardIndex(context, cardIndex);
        recyclerView.setAdapter(new LateReminderDetailAdapter(mPastReminderList, context,userId,isLastUser));
    }


    private List<Drug> getPassedReminderDrugListByUserId(String userId) {
        List<Drug> lateReminderDrugs = new ArrayList<>();
        if(null != PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards()) {
            Map<Long, List<Drug>> drugsByTime = PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards().get(userId);
            if (null != drugsByTime && !drugsByTime.isEmpty()) {
                for (Map.Entry<Long, List<Drug>> entry : drugsByTime.entrySet()) {
                    Long key = entry.getKey();
                    List<Drug> list = entry.getValue();
                    for (Drug drug : list) {
                        PillpopperLog.say("Going to check past reminder : " + key);
                        if (!FrontController.getInstance(context).isHistoryEventAvailable(new PillpopperTime(key / 1000), drug.getGuid())) {
                            if (FrontController.getInstance(context).isEntryAvailableInPastReminder(drug.getGuid(), new PillpopperTime(key / 1000))) {
                                lateReminderDrugs.add(drug);
                            }
                        }
                    }
                }
            }
        }
        return lateReminderDrugs;
    }

    public String getUserID(){
        return userID;
    }

    public void setUserID(String userid){
        this.userID = userid;
    }

    /**
     * Taken All Button Click for Expanded card
     * @param view
     */
    public void onTakenAllInDetailCard(View view){

        PillpopperLog.say("LateReminder on Details on Taken Click "  + userID + " And mPastReminderList : " + mPastReminderList.size());
        PillpopperLog.say("LateReminder is not last user"  + userID);
        new performButtonsActionAsyncTask().execute(TAKEN_EXPANDED);
    }


    /**
     * Skipped All Button Click for Expanded card
     * @param view
     */
    public void onSkippedAllInDetailCard(View view){
        PillpopperLog.say("LateReminder on Details on Skipp Click " + userID + " And mPastReminderList : " + mPastReminderList.size());
        DialogHelpers.showSkipMedDialog(context, mPastReminderList.size() > 1 ? PillpopperConstants.ACTION_SKIP_ALL_PILL : PillpopperConstants.ACTION_SKIP_PILL, () -> {

            if (!mPastReminderList.isEmpty()) {
                new performButtonsActionAsyncTask().execute(SKIPPED_EXPANDED);
            }
        });
    }

    /**
     * TakenAll button click in Contract Card.
     * @param view
     */
    public void onTakenAllClick(View view){

        new performButtonsActionAsyncTask().execute(TAKEN_CONTRACTED);
    }

    /**
     * SkippAll button click in Contract Card.
     * @param view
     */
    public void onSkipAllClick(View view){
        DialogHelpers.showSkipMedDialog(context, mPastReminderList.size()>1?PillpopperConstants.ACTION_SKIP_ALL_PILL:PillpopperConstants.ACTION_SKIP_PILL, () -> {

            if (!mPastReminderList.isEmpty()) {
                new performButtonsActionAsyncTask().execute(SKIPPED_CONTRACTED);
            }
        });
    }

    /**
     * Binds the text with SkipAll button based on the drug list.
     * @return
     */
    public String getTakenButtonText(){
        return mPastReminderList.size()>1?getResourceString(R.string.taken_all):getResourceString(R.string.taken);
    }

    /**
     * Binds the text with TakenAll button based on the drug list.
     * @return
     */
    public String getSkippedButtonText(){
        return mPastReminderList.size()>1?getResourceString(R.string.skipped_all):getResourceString(R.string.drug_action_skipped);
    }

    /**
     * Returns the resource string based on the ID.
     * @return
     */
    public String getResourceString(int i){
        return context.getResources().getString(i);
    }

    private void updatePassedReminderTable(List<Drug> pastReminderList) {
        for(Drug drug : pastReminderList){
            FrontController.getInstance(context).removeActedPassedReminderFromReminderTable(drug.getGuid(),
                    String.valueOf(drug.getScheduledTime().getGmtMilliseconds()), context);
        }
    }

    private void refreshCards(){
        LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> tempPassedRemindersHashMapByUserId = PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards();
        if(null!=tempPassedRemindersHashMapByUserId && !tempPassedRemindersHashMapByUserId.isEmpty()){
            tempPassedRemindersHashMapByUserId.remove(userID);
            PillpopperRunTime.getInstance().setPassedRemindersByUserIdForCards(tempPassedRemindersHashMapByUserId);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_REMINDERS_CARDS_AFTER_ACTION"));
        PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
    }

    private boolean isLastUser(){
        LinkedHashMap<String,LinkedHashMap<Long,List<Drug>>> list = PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards();
        /*PillpopperLog.say("isLastUser - " + String.valueOf((null!=list && list.size()==1)?true:false));*/
        return null != list && list.size() == 1;
    }

    private boolean checkForPendingPastReminders(){
        LinkedHashMap<String,LinkedHashMap<Long,List<Drug>>> list = PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards();
        /*PillpopperLog.say("isLastUser - " + String.valueOf((null!=list && list.size()==1)?true:false));*/
        return null != list && list.isEmpty();
    }

    @Override
    public void doRefresh(String userID, boolean activityFinishRequired, Context ctx) {

        if(Util.isEmptyString(userID)){
            return;
        }
        RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
        PillpopperLog.say("LateReminder doing refresh "  + userID);
        LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersHashMapByUserId = PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards();
        if(null!=passedRemindersHashMapByUserId && !passedRemindersHashMapByUserId.isEmpty()) {
            for (Map.Entry<String, LinkedHashMap<Long, List<Drug>>> _entry : passedRemindersHashMapByUserId.entrySet()) {
                LinkedHashMap<Long, List<Drug>> list = _entry.getValue();
                if(_entry.getKey().equalsIgnoreCase(userID)){
                    PillpopperLog.say("LateReminder doing refresh for "  + _entry.getKey() + " : And " + userID);
                    for (Map.Entry<Long, List<Drug>> entry : list.entrySet()) {
                        List<Drug> drugsForAction = new ArrayList<>();
                        for (Drug drug : entry.getValue()) {
                            if (!FrontController.getInstance(context).isHistoryEventAvailable(drug.getScheduledTime(), drug.getGuid())) {
                                drugsForAction.add(drug);
                            }
                        }
                        updatePassedReminderTable(entry.getValue());
                        FrontController.getInstance(ctx).performTakeDrug_pastReminders(drugsForAction, PillpopperTime.now(), ctx, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                        passedRemindersHashMapByUserId.remove(userID);
                    }
                    PillpopperRunTime.getInstance().setPassedRemindersByUserIdForCards(passedRemindersHashMapByUserId);
                    LocalBroadcastManager.getInstance(ctx).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
                    PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
                    //saveCardIndex();
                    if(checkForPendingPastReminders()){
                        markNoPastRemindersPresent();
                    }
                    if (activityFinishRequired) {
                        markNoPastRemindersPresent();
                        delayFinish((Activity) ctx, RESULT_OK);
                    }
                }
            }
        }
        if (activityFinishRequired) {
            markNoPastRemindersPresent();
            delayFinish((Activity) ctx, RESULT_OK);
        }
    }

    @Override
    public void doButtonTextRefresh(Context context, String userId) {
        mPastReminderList = getPassedReminderDrugListByUserId(userId);
        if(null != mPastReminderList && mPastReminderList.size() == 1) {
            PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
        }
    }

    private void markNoPastRemindersPresent(){
        if(isLastUser()){
            FrontController.getInstance(context).updateAsNoPendingReminders(context);
        }
    }

    public boolean isButtonRefreshRequired(String userID) {

        if (initialDrugslistSize == 1) {
            return false;
        } else if(initialDrugslistSize > 1 ){
            mPastReminderList = getPassedReminderDrugListByUserId(userID);
            if(null != mPastReminderList && mPastReminderList.size() == 1) {
                return true;
            }
        }
        return false;
    }

    private class performButtonsActionAsyncTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((Activity)context).startActivityForResult(new Intent(context, LoadingActivity.class),0);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LoggerUtils.exception("Exception - onPreExecute ",e);
            }
            mPastReminderList = getPassedReminderDrugListByUserId(userID);
            switch (params[0]){
                case TAKEN_CONTRACTED:
                    PillpopperLog.say("LateReminder is not last user"  + userID);
                    updatePassedReminderTable(mPastReminderList);
                    FrontController.getInstance(context).performTakeDrug_pastReminders(mPastReminderList, PillpopperTime.now(), context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                    break;
                case SKIPPED_CONTRACTED:
                case SKIPPED_EXPANDED:
                    updatePassedReminderTable(mPastReminderList);
                    FrontController.getInstance(context).performSkipDrug_pastReminders(mPastReminderList, PillpopperTime.now(), context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                    markNoPastRemindersPresent();
                    break;
                case TAKEN_EXPANDED:
                    updatePassedReminderTable(mPastReminderList);
                    FrontController.getInstance(context).performTakeDrug_pastReminders(mPastReminderList, PillpopperTime.now(), context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
//                    FrontController.getInstance(context).updateAsPendingRemindersPresent(context);
                    break;
            }
            Util.saveCardIndex(context, cardIndex);
            refreshCards();
            return params[0];
        }

        @Override
        protected void onPostExecute(Integer param) {
            super.onPostExecute(param);
            switch (param){
                case TAKEN_CONTRACTED:
                case SKIPPED_CONTRACTED:
                    if(null != context) {
                        ((Activity) context).finishActivity(0);
                    }
                    break;
                case TAKEN_EXPANDED:
                case SKIPPED_EXPANDED:
                    delayFinish((Activity) context, RESULT_OK);
                    break;
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
}
