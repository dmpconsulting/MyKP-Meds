package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.controller.FrontController;

import static android.app.Activity.RESULT_OK;


/**
 * Created by M1023050 on 28-Apr-19.
 */

public class BatteryOptimizerInfoCard implements HomeCard,Parcelable {

    private Context mContext;

    public BatteryOptimizerInfoCard(Context context){
        this.mContext = context;
    }


    protected BatteryOptimizerInfoCard(Parcel in) {

    }

    public static final Creator<BatteryOptimizerInfoCard> CREATOR = new Creator<BatteryOptimizerInfoCard>() {
        @Override
        public BatteryOptimizerInfoCard createFromParcel(Parcel in) {
            return new BatteryOptimizerInfoCard(in);
        }

        @Override
        public BatteryOptimizerInfoCard[] newArray(int size) {
            return new BatteryOptimizerInfoCard[size];
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
        return R.layout.battery_optimizer_contract_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.battery_optimizer_details_card;
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
        return PillpopperConstants.REQUEST_VIEW_BATTERY_OPTIMIZER_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return null;
    }

    public void onSettingsButtonClick(View view){

        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext, FireBaseConstants.Event.BATTERY_OPTIMIZATION_SETTINGS);

        try {
            mContext.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        } catch (Exception e){
            PillpopperLog.exception(e.getMessage());
        }
        delayFinish();
    }

    public void onDismissButtonClick(View view){
        FrontController.getInstance(mContext).disableBatteryOptimizationAlert(mContext);
        FrontController.getInstance(mContext).disableBatteryOptimizationCard(mContext);
        delayFinish();
    }

    private void delayFinish() {
        ((AppCompatActivity) mContext).finishActivity(0);
        new Handler().post(() -> {
            try {
                ((AppCompatActivity) mContext).setResult(RESULT_OK);
                ((AppCompatActivity) mContext).finish();
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
            }
        });
    }

    public boolean isSamSungDevice(){
        return getDeviceInfo(mContext.getString(R.string.samsung));
    }

    public boolean isGoogleDevice(){
        return getDeviceInfo(mContext.getString(R.string.google));
    }

    public boolean isOtherDevice(){
        return !isSamSungDevice() && !isGoogleDevice();
    }

    private boolean getDeviceInfo(String modelName){
        String manufacturerName = Build.MANUFACTURER;
        return ((null!=manufacturerName && manufacturerName.equalsIgnoreCase(modelName)) || (null!=manufacturerName && manufacturerName.contains(modelName)));
    }


}
