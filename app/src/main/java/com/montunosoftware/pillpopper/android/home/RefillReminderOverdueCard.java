package com.montunosoftware.pillpopper.android.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderUtils;
import com.montunosoftware.pillpopper.android.refillreminder.RequestWrapper;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;

import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by M1030430 on 3/5/2018.
 */

public class RefillReminderOverdueCard implements Parcelable,HomeCard {

    private int mCardIndex;
    private int banner;

    private String title;
    private String description;

    private Context mContext;

    private RefillReminder mRefillReminder;

    public RefillReminderOverdueCard(RefillReminder refillReminder, int cardIndex) {
        this.mRefillReminder = refillReminder;
        this.mCardIndex = cardIndex;
    }

    public RefillReminderOverdueCard(){

    }

    @Override
    public void setContext(Context context) {
        this.mContext=context;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getCardView() {
        return R.layout.refill_reminder_overdue_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_refill_reminder;
    }

    @Override
    public String getDescription() {
        return mContext.getString(R.string.card_refill_reminder_description);
    }

    @Override
    public int getBanner() {
        return 0;
    }

    @Override
    public int getRequestCode() {
        return PillpopperConstants.REQUEST_REFILL_REMINDER_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return view.getResources().getString(R.string.refill_reminder_overdue_card);
    }

    public String getRefillOverdueDate() {
        return getDisplayText("EEEE, MMMM d");
    }

    public String getRefillOverdueTime() {
        return getDisplayText("h:mm a");
    }

    private String getDisplayText(String format){
        Calendar nextReminder = Calendar.getInstance();
        nextReminder.setTimeInMillis(Long.parseLong(mRefillReminder.getOverdueReminderDate()) * 1000L);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(nextReminder.getTime()).toUpperCase();
    }

    public void initDetailView() {
        Util.saveCardIndex(mContext, mCardIndex);
    }

    public void setBanner(int banner) {
        this.banner = banner;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RefillReminder getRefillReminder() {
        return mRefillReminder;
    }

    public void setRefillReminder(RefillReminder refillReminder) {
        this.mRefillReminder = refillReminder;
    }

    protected RefillReminderOverdueCard(Parcel in) {
        banner = in.readInt();
        description = in.readString();
        title = in.readString();
        mRefillReminder = (RefillReminder) in.readSerializable();
        mCardIndex = in.readInt();
    }

    public static final Creator<RefillReminderOverdueCard> CREATOR = new Creator<RefillReminderOverdueCard>() {
        @Override
        public RefillReminderOverdueCard createFromParcel(Parcel in) {
            return new RefillReminderOverdueCard(in);
        }

        @Override
        public RefillReminderOverdueCard[] newArray(int size) {
            return new RefillReminderOverdueCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(banner);
        dest.writeString(description);
        dest.writeString(title);
        dest.writeSerializable(mRefillReminder);
        dest.writeInt(mCardIndex);
    }

    /**
     * Click Event for Dismiss button in Refill Reminder Exapanded Card.
     * @param view
     */
    public void onDismissInDetailCard(View view){

        new AcknowledgeRefillReminderAsyncTask().execute();
    }

    public void onRefillNowInDetailCard(View view){

        FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                FireBaseConstants.Event.REFILL_MEDS,
                FireBaseConstants.ParamName.SOURCE,
                FireBaseConstants.ParamValue.REFILL_REMINDER_CARD);
        RunTimeData.getInstance().setNavigateToRefillScreen(true);
        new AcknowledgeRefillReminderAsyncTask().execute();
    }

    private void addLogEntryForAcknowledgeRefillReminder() {
        mRefillReminder.setLastAcknowledgeDate(mRefillReminder.getOverdueReminderDate());
        mRefillReminder.setOverdueReminderDate("null");
        mRefillReminder.setLastAcknowledgeTzSecs(String.valueOf(RefillReminderUtils.getTzOffsetSecs()));
        acknowledgeRefillReminder(mRefillReminder);
        JSONObject acknowledgeRefillRequest = new RequestWrapper(mContext).createAcknowledgeRefillRequest(mRefillReminder);
        String replayId = acknowledgeRefillRequest.optJSONObject("pillpopperRequest").optString("replayId");
        if (!RefillReminderUtils.isEmptyString(replayId)) {
            LogEntryModel logEntryModel = new LogEntryModel();
            logEntryModel.setDateAdded(System.currentTimeMillis());
            logEntryModel.setReplyID(replayId);
            logEntryModel.setEntryJSONObject(acknowledgeRefillRequest, mContext);
            FrontController.getInstance(mContext).addLogEntry(mContext, logEntryModel);
        }
        RefillReminderLog.say("cardIndex - " + mCardIndex);
    }

    /**
     * invokes controller's acknowledgeRefillReminder method
     * Updates overdue date as null
     * Updates last_ack_date with most recent overdue_date, updates last_ack_tz_secs.
     * @param refillReminder object
     */
    private void acknowledgeRefillReminder(RefillReminder refillReminder) {
        RefillReminderController.getInstance(mContext).acknowledgeRefillReminder(refillReminder);
    }

    private class AcknowledgeRefillReminderAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((Activity) mContext).startActivityForResult(new Intent(mContext, LoadingActivity.class),0);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            addLogEntryForAcknowledgeRefillReminder();
            //delay of 1 sec
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                RefillReminderLog.say(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            delayFinish(0);
        }
    }

    private void delayFinish(final int resultCode) {
        ((AppCompatActivity) mContext).finishActivity(0);
        new Handler().post(() -> {
            try {
                ((AppCompatActivity) mContext).setResult(resultCode);
                ((AppCompatActivity) mContext).finish();
            } catch (Exception e) {
                PillpopperLog.say(e);
            }
            RunTimeData.getInstance().setAppInExpandedCard(false);
            PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
            RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_REFILL_REMINDERS"));
        });
    }
}
