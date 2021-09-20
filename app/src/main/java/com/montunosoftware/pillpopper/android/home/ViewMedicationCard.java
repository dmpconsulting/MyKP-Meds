package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;

import static android.app.Activity.RESULT_OK;

/**
 * Created by m1032896 on 11/20/2017.
 */

public class ViewMedicationCard implements Parcelable,HomeCard {

    private int banner;
    private String description;
    private String title;
    private Context context;

    public ViewMedicationCard() {
    }

    @Override
    public void setContext(Context context) {
        this.context=context;
    }

    public ViewMedicationCard(Parcel in) {
        title = in.readString();
        description = in.readString();
        banner = in.readInt();
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.card_title_view_medication);
    }

    @Override
    public int getRequestCode() {
        return PillpopperConstants.REQUEST_SETUP_VIEW_MEDS_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return view.getContext().getString(R.string.view_your_medications_card);
    }

    @Override
    public int getCardView() {
        return R.layout.home_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_view_medications;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.card_view_medication_description);
    }

    @Override
    public int getBanner() {
        banner = R.drawable.card_tutorial_1;
        return banner;
    }

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

    public static final Creator<ViewMedicationCard> CREATOR = new Creator<ViewMedicationCard>() {
        @Override
        public ViewMedicationCard createFromParcel(Parcel in) {
            return new ViewMedicationCard(in);
        }

        @Override
        public ViewMedicationCard[] newArray(int size) {
            return new ViewMedicationCard[size];
        }
    };

    public void onClickShowMedications(View view) {

        ((HomeCardDetailActivity) view.getContext()).setResult(RESULT_OK);
        ((HomeCardDetailActivity) view.getContext()).finish();
    }


}
