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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;

import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by M1023050 on 5/7/2018.
 */

public class KPHCDiscontinueCard implements HomeCard,Parcelable {

    private Context mContext;
    private List<DiscontinuedDrug> mDiscontinuedDrugList;

    public KPHCDiscontinueCard(List<DiscontinuedDrug> discontinuedDrugList, Context context) {
        this.mContext = context;
        this.mDiscontinuedDrugList = discontinuedDrugList;
    }

    protected KPHCDiscontinueCard(Parcel in) {
    }

    public static final Creator<KPHCDiscontinueCard> CREATOR = new Creator<KPHCDiscontinueCard>() {
        @Override
        public KPHCDiscontinueCard createFromParcel(Parcel in) {
            return new KPHCDiscontinueCard(in);
        }

        @Override
        public KPHCDiscontinueCard[] newArray(int size) {
            return new KPHCDiscontinueCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    @Override
    public void setContext(Context context) {
        this.mContext = context;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getCardView() {
        return R.layout.discontinue_kphc_home_card;
    }

    public String getUserNames(){
        List<String> uniqueUsersList = new ArrayList<>();
        for(int position=0; position<mDiscontinuedDrugList.size(); position++){
            if(position==0){
                uniqueUsersList.add(mDiscontinuedDrugList.get(position).getUserFirstName());
            }else if (!mDiscontinuedDrugList.get(position).getUserId()
                    .equals(mDiscontinuedDrugList.get(position - 1).getUserId())){
                uniqueUsersList.add(mDiscontinuedDrugList.get(position).getUserFirstName());
            }
        }
        return buildNames(uniqueUsersList);
    }

    private String buildNames(List<String> uniqueUsersList) {
        if(uniqueUsersList.isEmpty()){
            return "";
        }
        StringBuilder namesBuilder = new StringBuilder();
        for(String userFirstName : uniqueUsersList){
            namesBuilder.append(userFirstName).append(", ");
        }
        return namesBuilder.toString().substring(0,namesBuilder.toString().trim().length()-1);
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_discontinue_kphc;
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
        return PillpopperConstants.REQUEST_KPHC_DISCONTINUED_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return view.getContext().getString(R.string.kphc_discontinue_card);
    }

    public void initDetailView(RecyclerView recyclerView) {
        mDiscontinuedDrugList = FrontController.getInstance(mContext).getDiscontinuedMedications();
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new KPHCDiscontinueCardExpandedAdapter(recyclerView.getContext(), mDiscontinuedDrugList));
    }

    public void onOKButtonClick(View view) {
        if(!mDiscontinuedDrugList.isEmpty()){
             new AcknowledgeDiscontinuedMedicationsTask().execute(mDiscontinuedDrugList);
        }
    }

    public class AcknowledgeDiscontinuedMedicationsTask
            extends AsyncTask<List<DiscontinuedDrug>, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((Activity) mContext).startActivityForResult(new Intent(mContext, LoadingActivity.class), 0);
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<DiscontinuedDrug>... params) {
            FrontController.getInstance(mContext).acknowledgeDiscontinuedDrugs(params[0]);
            for (DiscontinuedDrug drug : params[0]) {
                FrontController.getInstance(mContext).addLogEntry(mContext, Util.prepareAcknowledgeDiscontinuedDrugsLogEntry(drug, mContext));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                PillpopperLog.say(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
            delayFinish(RESULT_OK);
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
        });
    }
}
