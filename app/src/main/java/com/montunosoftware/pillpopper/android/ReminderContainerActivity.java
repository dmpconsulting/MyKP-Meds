package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.kotlin.lateremider.LateRemindersActivity;
import com.montunosoftware.pillpopper.kotlin.quickview.CurrentReminderActivityNew;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.util.ArrayList;
import java.util.List;

public class ReminderContainerActivity extends PillpopperActivity {

    private List<Drug> drugListForQuickview = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_REFRESH);
        _thisActivity.registerReceiver(mReminderReceiver, filter, PillpopperAppContext.PILLPOPPER_BROADCAST_PERMISSION, null);
        if(null != getIntent() && null != getIntent().getStringExtra("launch")){

            // to launch new design page
            startActivity(new Intent(this,
                    "CurrentReminderActivity".equalsIgnoreCase(getIntent().getStringExtra("launch")) ? CurrentReminderActivityNew.class : LateRemindersActivity.class));
        }
    }

    BroadcastReceiver mReminderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(AppConstants.isByPassLogin()) {
                drugListForQuickview.clear();
                PillpopperTime now = PillpopperTime.now();
                ArrayList<Drug> overduedrugs = new ArrayList<>();
                int count = 0;
                for (Drug d : FrontController.getInstance(_thisActivity).getDrugListForOverDue(_thisActivity)) {
                    d.computeDBDoseEvents(context, d, now, 60);
                    if (d.isoverDUE() && (null == d.getSchedule().getEnd() || (d.getSchedule().getEnd().equals(PillpopperDay.today()) || d.getSchedule().getEnd().after(PillpopperDay.today())))) {
                        overduedrugs.add(d);
                        count++;
                    }
                    drugListForQuickview.addAll(overduedrugs);
                    overduedrugs.clear();
                }
                AppConstants.setByPassLogin(true);
                Intent quickViewIntent = new Intent(_thisActivity, QuickViewOverDueReminderScreen.class);
                quickViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                quickViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PillpopperRunTime.getInstance().setQuickViewReminderDrugs(drugListForQuickview);
                startActivity(quickViewIntent);
                finish();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if(RunTimeData.getInstance().isNotificationGenerated()){
            launchSplash();
        }
    }

    private void launchSplash() {
        Intent intent = new Intent(this, Splash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _thisActivity.unregisterReceiver(mReminderReceiver);
    }
}
