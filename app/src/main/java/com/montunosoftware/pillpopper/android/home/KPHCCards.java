package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.KphcDrug;
import com.montunosoftware.pillpopper.service.LogEntryUpdateAsyncTask;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;

import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by M1024581 on 11/23/2017.
 */

public class KPHCCards implements HomeCard,Parcelable {
    private int banner;
    private String description;
    private String title;
    private Context context;
    private User user;
    private String userName;
    private String detailCardTitle;
    private boolean isNewKPHCMeds;

    List<KphcDrug> kphcDrugList;

    public KPHCCards(User user, boolean isNewKPHCMeds) {
        this.setUser(user);
        this.isNewKPHCMeds=isNewKPHCMeds;
    }


    protected KPHCCards(Parcel in) {
        banner = in.readInt();
        description = in.readString();
        title = in.readString();
        userName = in.readString();
        detailCardTitle = in.readString();
        user=in.readParcelable(User.class.getClassLoader());
        isNewKPHCMeds = in.readByte()!=0; //true if byte !=0
    }

    public static final Creator<KPHCCards> CREATOR = new Creator<KPHCCards>() {
        @Override
        public KPHCCards createFromParcel(Parcel in) {
            return new KPHCCards(in);
        }

        @Override
        public KPHCCards[] newArray(int size) {
            return new KPHCCards[size];
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
        return isNewKPHCMeds?R.layout.new_kphc_card:R.layout.update_kphc_home_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_kphc_expanded_card;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getBanner() {
        return 0;
    }

    public String getUserName() {
        return user.getFirstName();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDetailCardTitle() {
        return hasNewKPHCUser()?context.getString(R.string.new_medications_kphc_card):context.getString(R.string.updated_medications_card);
    }

    public void setDetailCardTitle(String detailCardTitle) {
        this.detailCardTitle = detailCardTitle;
    }


    @Override
    public int getRequestCode() {
        return hasNewKPHCUser()? PillpopperConstants.REQUEST_NEW_KPHC_CARD_DETAIL:PillpopperConstants.REQUEST_UPDATED_KPHC_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return hasNewKPHCUser()? view.getContext().getString(R.string.new_from_kphc_card):view.getContext().getString(R.string.update_from_kphc_card);
    }


    public User getUser() {
        return user;
    }


    public boolean hasNewKPHCUser() {
        return isNewKPHCMeds;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(banner);
        parcel.writeString(description);
        parcel.writeString(title);
        parcel.writeString(userName);
        parcel.writeString(detailCardTitle);
        parcel.writeParcelable(user,0);
        parcel.writeByte((byte) (isNewKPHCMeds ? 1 : 0));   //1= true:::0=false
    }

    public void initKPHCDetailView(RecyclerView kphcList,String userId){
        kphcDrugList = DatabaseUtils.getInstance(context).getKPHCDrugListByUser(userId,isNewKPHCMeds);
        kphcList.setLayoutManager(new LinearLayoutManager(kphcList.getContext()));
        kphcList.setAdapter(new KPHCDetailAdapter(kphcDrugList));
    }

    public void onScheduleClick(View view) {
        FireBaseAnalyticsTracker.getInstance().logEvent(context,
                FireBaseConstants.Event.BULK_REMINDER_ADD,
                FireBaseConstants.ParamName.SOURCE,
                FireBaseConstants.ParamValue.NEW_MEDICATION_CARD);

        acknowledgeKPHCMeds(view, RESULT_OK, false);
        RunTimeData.getInstance().setKphcSelectedUserID(user.getUserId());
        HomeFragment.invokeBulkSchedule(context, false);
    }

    private void acknowledgeKPHCMeds(View view, int optionalResultCode, boolean isDelayFinishRequired) {
        if(!kphcDrugList.isEmpty()){
            DatabaseUtils.getInstance(context).updateKPHCDrugs(kphcDrugList);
        }
        PillpopperActivity pillpopperActivity = (PillpopperActivity) view.getContext();
        addLogEntries(kphcDrugList, pillpopperActivity, view.getContext());
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
            if(isDelayFinishRequired) {
                delayFinish(optionalResultCode);
            }
    }

    //This listener functionality being same, it is called for Dismiss and okay button click
    public void onDismissClick(View view){
       /* if (!isNewKPHCMeds) {
            AppDynamicsController.getInstance(context).captureBreadCrumbs(AppConstants.BREAD_CRUMB_UPDATE_KPHC_EXP_CARD +" - Okay button clicked");
        } else {
            AppDynamicsController.getInstance(context).captureBreadCrumbs(AppConstants.BREAD_CRUMB_NEW_KPHC_EXP_CARD + " - Dismiss button clicked");
        } */
        acknowledgeKPHCMeds(view, RESULT_CANCELED, true);
    }

    private void addLogEntries(List<KphcDrug> kphcDrugList, PillpopperActivity pillpopperActivity, Context context) {
        for (KphcDrug kphcDrug : kphcDrugList){
            addLogEntryForEdit(FrontController.getInstance(context).getDrugByPillId(kphcDrug.getPillId()),
                    pillpopperActivity);
        }
    }

    private void delayFinish(final int resultCode) {
        ((AppCompatActivity) context).finishActivity(0);
        new Handler().post(() -> {
            try {
                ((AppCompatActivity) context).setResult(resultCode);
                ((AppCompatActivity) context).finish();
            } catch (Exception e) {
                PillpopperLog.say(e);
            }
        });
    }

    private void addLogEntryForEdit(Drug drug, PillpopperActivity pillpopperActivity) {
        try {
            LogEntryUpdateAsyncTask logEntryUpdateAsyncTask = new LogEntryUpdateAsyncTask(pillpopperActivity, "EditPill", drug);
            logEntryUpdateAsyncTask.execute();
        } catch (Exception e) {
            PillpopperLog.say("Exception while adding log entry for edit ", e);
        }
    }
}
