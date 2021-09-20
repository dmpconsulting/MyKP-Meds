package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.Util;

import static android.app.Activity.RESULT_OK;

/**
 * Created by M1032896 on 11/21/2017.
 */

public class ViewReminderCard implements Parcelable, HomeCard {
    private int banner;
    private String description;
    private String title;
    private Context context;

    public ViewReminderCard() {

    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    protected ViewReminderCard(Parcel in) {
        title = in.readString();
        description = in.readString();
        banner = in.readInt();
    }

    public static final Parcelable.Creator<ViewReminderCard> CREATOR = new Parcelable.Creator<ViewReminderCard>() {
        @Override
        public ViewReminderCard createFromParcel(Parcel in) {
            return new ViewReminderCard(in);
        }

        @Override
        public ViewReminderCard[] newArray(int size) {
            return new ViewReminderCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeInt(banner);
    }


    @Override
    public int getRequestCode() {
        return PillpopperConstants.REQUEST_VIEW_REMINDER_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return view.getContext().getString(R.string.view_reminder_details_card);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.card_title_view_reminder);
    }

    @Override
    public int getCardView() {
        return R.layout.home_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_view_reminder;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.card_view_reminder_description);

    }

    @Override
    public int getBanner() {
        banner = R.drawable.card_tutorial_3;
        return banner;
    }


    /**
     * Called on tapping on NO button in expanded card.
     * Will prepare the log entry with setPreferences action
     */
    public void onClickNo(View view) {
        FireBaseAnalyticsTracker.getInstance().logEvent(context,
                FireBaseConstants.Event.QUICKVIEW_TOGGLE,
                FireBaseConstants.ParamName.TOGGLE,
                FireBaseConstants.ParamValue.OFF);

        Util.updateOptinSelection(PillpopperConstants.QUICKVIEW_OPTED_OUT, context);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
        ((HomeCardDetailActivity) context).setResult(RESULT_OK);
        ((HomeCardDetailActivity) context).finish();
    }

    /**
     * Called on tapping on YES button in expanded card.
     * Will prepare the log entry with setPreferences action
     */
    public void onClickYes(View view) {

        FireBaseAnalyticsTracker.getInstance().logEvent(context,
                FireBaseConstants.Event.QUICKVIEW_TOGGLE,
                FireBaseConstants.ParamName.TOGGLE,
                FireBaseConstants.ParamValue.ON);
        Util.updateOptinSelection(PillpopperConstants.QUICKVIEW_OPTED_IN, context);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
        ((HomeCardDetailActivity) context).setResult(RESULT_OK);
        ((HomeCardDetailActivity) context).finish();
    }
}
