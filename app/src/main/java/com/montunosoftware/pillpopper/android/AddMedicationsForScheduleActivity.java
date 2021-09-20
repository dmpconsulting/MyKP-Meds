package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.ViewClickHandler;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.ScheduleViewModel;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.ArrayList;
import java.util.List;

import static org.kp.tpmg.mykpmeds.activation.AppConstants.BROADCAST_REFRESH_FOR_MED_IMAGES;

public class AddMedicationsForScheduleActivity extends StateListenerActivity implements AddMedicationForScheduleRecyclerAdapter.CheckBoxSelectionListener {

    private RecyclerView mDrugRecyclerView;
    private String mSelectedUserId;
    private Button mSaveMedicationBtn;
    private List<Drug> mSelectedDrugs;
    private List<String> pillIdList = new ArrayList<>();
    private FrontController mFrontController;
    private ScheduleViewModel scheduleViewmodel;
    private Typeface mFontMedium;
    private AddMedicationForScheduleRecyclerAdapter adapter;


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onBackPressed();
    }


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        setContentView(R.layout.add_medication_for_schedule);
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_BULK_REMINDER_ADD_MEDICATIONS);
        scheduleViewmodel = RunTimeData.getInstance().getScheduleViewModel(this);
        mFontMedium = ActivationUtil.setFontStyle(getActivity(),AppConstants.FONT_ROBOTO_MEDIUM);
        initToolBar();
        initUI();
        loadPillList();
        loadRecyclerAdapter();
        initBroadCastReceivers();
        enableOrDisableSaveMedicationsButton();
    }

    private void initBroadCastReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshMedicationsImage, new IntentFilter(BROADCAST_REFRESH_FOR_MED_IMAGES));
    }

    private void loadPillList() {

        if (null != scheduleViewmodel.getMedicationsList() && null != scheduleViewmodel.getMedicationsList().getValue()
                && !scheduleViewmodel.getMedicationsList().getValue().isEmpty()) {
            for (Drug drug : scheduleViewmodel.getMedicationsList().getValue()) {
                if(!pillIdList.contains(drug.getGuid())) {
                    pillIdList.add(drug.getGuid());
                }
            }
        }
    }

    private void initUI() {
        TextView medicationsLabel = findViewById(R.id.genericText);
        medicationsLabel.setTypeface(mFontMedium);
        mFrontController = FrontController.getInstance(this);
        mSaveMedicationBtn = findViewById(R.id.btn_save_medications);
        mSaveMedicationBtn.setTypeface(mFontMedium);
        mSaveMedicationBtn.setOnClickListener(view1 -> {
            ViewClickHandler.preventMultiClick(view1);
            mSelectedDrugs = new ArrayList<>();
            for (String pillId : pillIdList) {
                mSelectedDrugs.add(FrontController.getInstance(AddMedicationsForScheduleActivity.this).getDrugByPillId(pillId));
            }
            for (Drug drug : mSelectedDrugs) {
                if (!"".equalsIgnoreCase(drug.getSchedule().getTimeList().toString())) {
                    showAlertForChangeInSchedule();
                    return;
                }
            }
            scheduleViewmodel.getMedicationsList().postValue(mSelectedDrugs);
            onBackPressed();
        });

        TextView memberName = findViewById(R.id.member_name);
        memberName.setTypeface(mFontMedium);
        mDrugRecyclerView = findViewById(R.id.drug_list_recycler_view);
        mDrugRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDrugRecyclerView.setHasFixedSize(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && null != bundle.getString("selectedUserId")) {
            mSelectedUserId = bundle.getString("selectedUserId");
            memberName.setVisibility(View.VISIBLE);
            memberName.setText(FrontController.getInstance(getActivity()).getUserFirstNameByUserId(mSelectedUserId) + "'s Medication");
        } else {
            memberName.setVisibility(View.GONE);
        }

        loadRecyclerAdapter();
    }

    private void loadRecyclerAdapter() {
        List<Drug> usersDrugList = mFrontController.getDrugsListByUserId(mSelectedUserId);
        // Check if there are any medications selected before.
        List<Drug> selectedDrugList = scheduleViewmodel.getMedicationsList().getValue();
        pillIdList.clear();
        if (null != selectedDrugList && !selectedDrugList.isEmpty()) {
            for (Drug d : selectedDrugList) {
                for (Drug drug : usersDrugList) {
                    if (d.getGuid().equals(drug.getGuid())) {
                        pillIdList.add(d.getGuid());
                        drug.setChecked(true);
                    }
                }
            }
            enableOrDisableSaveMedicationsButton();
        }
        if (!usersDrugList.isEmpty()) {
            adapter = new AddMedicationForScheduleRecyclerAdapter(this, usersDrugList, AddMedicationsForScheduleActivity.this);
            mDrugRecyclerView.setAdapter(adapter);
        }
    }

    public void initToolBar() {
        Toolbar mToolbar = findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.add_medication_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCheckedOrUnChecked(Boolean checked, String pillId) {
        if (checked) {
            if (!pillIdList.contains(pillId)) {
                pillIdList.add(pillId);
            }
        } else {
            pillIdList.remove(pillId);
        }
        enableOrDisableSaveMedicationsButton();
    }

    private void showAlertForChangeInSchedule() {
        DialogHelpers.showAlertWithConfirmCancelListeners(this, R.string.__blank, R.string.alert_message_for_reschedule,
                new DialogHelpers.Confirm_CancelListener() {
                    @Override
                    public void onConfirmed() {
                        scheduleViewmodel.getMedicationsList().postValue(mSelectedDrugs);
                        onBackPressed();
                    }

                    @Override
                    public void onCanceled() {
                        // Do nothing, just dismiss the alert.
                    }
                });
    }

    private void enableOrDisableSaveMedicationsButton() {
        mSaveMedicationBtn.setBackground(Util.getDrawableWrapper(this,
                pillIdList.isEmpty() ? R.drawable.rounded_corner_white_button : R.drawable.blue_round_button_style));
        mSaveMedicationBtn.setTextColor(ContextCompat.getColor(this, pillIdList.isEmpty() ? R.color.kp_theme_blue : R.color.white));
        mSaveMedicationBtn.setClickable(!pillIdList.isEmpty());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    private BroadcastReceiver refreshMedicationsImage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<Drug> usersDrugList = mFrontController.getDrugsListByUserId(mSelectedUserId);
            for (Drug drug : usersDrugList){
                if(pillIdList.contains(drug.getGuid())){
                    drug.setChecked(true);
                }
            }
            if (!usersDrugList.isEmpty()) {
                adapter = new AddMedicationForScheduleRecyclerAdapter(AddMedicationsForScheduleActivity.this, usersDrugList, AddMedicationsForScheduleActivity.this);
                mDrugRecyclerView.setAdapter(adapter);
            }
            enableOrDisableSaveMedicationsButton();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        getState().unregisterStateUpdatedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isFinishing() && null != refreshMedicationsImage) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshMedicationsImage);
        }
    }
}