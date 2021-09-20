package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;

import static android.app.Activity.RESULT_OK;

/**
 * Created by M1032896 on 11/21/2017.
 */

public class RefillCard implements Parcelable,HomeCard {
    private int banner;
    private String description;
    private String title;
    private Context context;

    public RefillCard() {
    }

    @Override
    public void setContext(Context context) {
        this.context=context;
    }

    public RefillCard(Parcel in) {
        title = in.readString();
        description = in.readString();
        banner = in.readInt();
    }

    public static final Parcelable.Creator<RefillCard> CREATOR = new Parcelable.Creator<RefillCard>() {
        @Override
        public RefillCard createFromParcel(Parcel in) {
            return new RefillCard(in);
        }

        @Override
        public RefillCard[] newArray(int size) {
            return new RefillCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(banner);
        parcel.writeString(title);
        parcel.writeString(description);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.card_title_refill_from_phone);
    }

    @Override
    public int getCardView() {
        return R.layout.home_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_refill;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.card_refill_description);
    }

    @Override
    public int getBanner() {
        banner = R.drawable.card_tutorial_4;
        return banner;
    }

    @Override
    public int getRequestCode() {
        return PillpopperConstants.REQUEST_REFILL_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return view.getContext().getString(R.string.refill_from_your_phone_card);
    }

    public void onRefillClick(View view) {

        FireBaseAnalyticsTracker.getInstance().logEvent(context,
                FireBaseConstants.Event.REFILL_MEDS,
                FireBaseConstants.ParamName.SOURCE,
                FireBaseConstants.ParamValue.REFILL_FROM_YOUR_PHONE_CARD);
        ((HomeCardDetailActivity) view.getContext()).setResult(RESULT_OK);
        ((HomeCardDetailActivity) view.getContext()).finish();
    }
}
