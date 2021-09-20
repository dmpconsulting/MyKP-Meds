package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.view.View;

/**
 * Created by m1032896 on 11/15/2017.
 */

public interface HomeCard {

    void setContext(Context context);

    String getTitle();

    int getCardView();

    int getDetailView();

    String getDescription();

    int getBanner();

    int getRequestCode();

    String getContentDescription(View view);

}
