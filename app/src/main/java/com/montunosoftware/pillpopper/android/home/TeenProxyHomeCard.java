package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import static android.app.Activity.RESULT_OK;

public class TeenProxyHomeCard implements Parcelable, HomeCard  {
    private Context context;

    public TeenProxyHomeCard(Context context) {
        this.context = context;
    }

    public TeenProxyHomeCard(Parcel in) {

    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getCardView() {
        return R.layout.teen_proxy_home_card;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getBanner() {
        return 0;
    }

    @Override
    public int getDetailView() {
        return R.layout.teen_proxy_home_card_detail_layout;
    }

    @Override
    public String getDescription() {
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    @Override
    public int getRequestCode() {
        return PillpopperConstants.TEEN_PROXY_HOME_CARD_DETAIL;
    }

    public void onOkClicked() {
        SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME).putBoolean("showTeenCard", false, false);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_TEEN_PROXY_HOME_CARD"));
        delayFinish();
    }

    @Override
    public String getContentDescription(View view) {
        return view.getContext().getString(R.string.content_description_teen_proxy_card);
    }

    public static final Parcelable.Creator<TeenProxyHomeCard> CREATOR = new Parcelable.Creator<TeenProxyHomeCard>() {
        @Override
        public TeenProxyHomeCard createFromParcel(Parcel in) {
            return new TeenProxyHomeCard(in);
        }

        @Override
        public TeenProxyHomeCard[] newArray(int size) {
            return new TeenProxyHomeCard[size];
        }
    };
    private void delayFinish() {
        ((AppCompatActivity) context).finishActivity(0);
        new Handler().post(() -> {
            try {
                ((AppCompatActivity) context).setResult(RESULT_OK);
                ((AppCompatActivity) context).finish();
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
            }
        });
    }

}
