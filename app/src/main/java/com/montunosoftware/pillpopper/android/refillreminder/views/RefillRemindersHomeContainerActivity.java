package com.montunosoftware.pillpopper.android.refillreminder.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.StateListenerActivity;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;

import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.RefillReminderInterface;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

/**
 * Created by M1024581 on 2/20/2018.
 */

public class RefillRemindersHomeContainerActivity extends StateListenerActivity implements RefillReminderInterface{


    private final int CREATE_OR_UPDATE = 1;
//    private final int LIST = 2;
    private static int mLastFragmentCode;
    private RefillReminder mSelectedRefillReminder;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.refill_reminders_home_layout);
        initToolBar();
    }

    private void initToolBar() {
        Toolbar mToolbar = findViewById(R.id.refill_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.refill_reminder));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        if (getRefillRemindersCount() > 0) {
//            if(mLastFragmentCode == CREATE_OR_UPDATE) {
        if(null != getIntent().getExtras()){
            Bundle bundle = getIntent().getExtras();
            mSelectedRefillReminder = (RefillReminder) bundle.get("selectedRefillReminder");
        }
        selectFragment(CREATE_OR_UPDATE);
          /*  } else {
                selectFragment(LIST);
            }
        } else {
            selectFragment(CREATE_OR_UPDATE);
        }*/
    }

    private void selectFragment(int fragmentCode){
        switch (fragmentCode){
            case CREATE_OR_UPDATE:
                CreateOrUpdateRefillReminderFragment createOrUpdateRefillReminderFragment = new CreateOrUpdateRefillReminderFragment();
                if (null != mSelectedRefillReminder) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("selectedRefillReminder", mSelectedRefillReminder);
                    createOrUpdateRefillReminderFragment.setArguments(bundle);
                    mSelectedRefillReminder = null;
                }
                install_fragment(createOrUpdateRefillReminderFragment);
                break;
            /*case LIST:
                install_fragment(new RefillRemindersListFragment());
                break;*/
        }
        mLastFragmentCode = fragmentCode;
    }

    private int getRefillRemindersCount() {
        return RefillReminderController.getInstance(this).getRefillRemindersCount();
    }

    private void install_fragment(Fragment f) {
        if (!isFinishing()) {
            FragmentTransaction fragment_transaction = getSupportFragmentManager().beginTransaction();
            fragment_transaction.replace(R.id.fragment_container, f);
            fragment_transaction.commit();
        }
    }

    @Override
    public void addLogEntryForRefillReminderUpdate(JSONObject obj) {
        prepareAndAddLogEntryModel(obj);
        showRefillListFragment();
    }

    private void showRefillListFragment() {
        // in case from Quick Action Button, Close activity and show Refill ListFragment
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("REFRESH_REFILL_REMINDERS"));
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void addLogEntryForRefillReminderDelete(JSONObject obj) {
        prepareAndAddLogEntryModel(obj);
    }

    private void prepareAndAddLogEntryModel(JSONObject obj){
        String replayId = obj.optJSONObject("pillpopperRequest").optString("replayId");
        if (null != replayId && replayId.length() > 0) {
            LogEntryModel logEntryModel = new LogEntryModel();
            logEntryModel.setDateAdded(System.currentTimeMillis());
            logEntryModel.setReplyID(replayId);
            logEntryModel.setEntryJSONObject(obj, this);
            FrontController.getInstance(this).addLogEntry(this, logEntryModel);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        /*if (mLastFragmentCode == CREATE_OR_UPDATE && getRefillRemindersCount() > 0) {
            selectFragment(LIST);
        } else {*/
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        new Handler().postDelayed(() -> {
            try {
                RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
                LocalBroadcastManager.getInstance(RefillRemindersHomeContainerActivity.this).sendBroadcast(new Intent("REFRESH_REFILL_REMINDERS"));
            } catch (Exception e) {
                PillpopperLog.say(e);
            }
        }, 500);

//        }
    }
}
