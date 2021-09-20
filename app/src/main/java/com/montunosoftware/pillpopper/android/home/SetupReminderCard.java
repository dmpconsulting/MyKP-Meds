package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;

/**
 * Created by m1032896 on 11/21/2017.
 */

public class SetupReminderCard implements Parcelable,HomeCard {
    private int banner;
    private String description;
    private String title;
    private Context context;

    public SetupReminderCard() {
    }

    @Override
    public void setContext(Context context) {
        this.context=context;
    }

    protected SetupReminderCard(Parcel in) {
        title = in.readString();
        banner = in.readInt();
        description = in.readString();

    }

    public static final Parcelable.Creator<SetupReminderCard> CREATOR = new Parcelable.Creator<SetupReminderCard>() {
        @Override
        public SetupReminderCard createFromParcel(Parcel in) {
            return new SetupReminderCard(in);
        }

        @Override
        public SetupReminderCard[] newArray(int size) {
            return new SetupReminderCard[size];
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
        return context.getString(R.string.card_title_setup_reminder);
    }

    @Override
    public int getCardView() {
        return R.layout.home_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_setup_reminder;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.card_setup_reminder_description);
    }

    @Override
    public int getBanner() {
        banner = R.drawable.card_tutorial_2;
        return banner;
    }

    @Override
    public int getRequestCode() {
        return PillpopperConstants.REQUEST_SETUP_REMINDER_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return view.getContext().getString(R.string.set_up_reminders_card);
    }


    public void onCreateClick(View view) {

        FireBaseAnalyticsTracker.getInstance().logEvent(context,
                FireBaseConstants.Event.BULK_REMINDER_ADD,
                FireBaseConstants.ParamName.SOURCE,
                FireBaseConstants.ParamValue.SET_UP_REMINDERS_CARD);
        HomeFragment.invokeBulkSchedule(context, true);
    }
}
