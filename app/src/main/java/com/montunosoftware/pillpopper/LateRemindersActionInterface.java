package com.montunosoftware.pillpopper;

import android.content.Context;

/**
 * Created by M1023050 on 12/31/2017.
 */

public interface LateRemindersActionInterface {
    void doRefresh(String userID, boolean activityFinishRequired, Context ctx);
    void doButtonTextRefresh(Context context, String userId);
}
