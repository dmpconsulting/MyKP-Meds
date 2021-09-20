package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.DrugListScheduleDialogeNewBinding;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.ComposableComparator;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.ScheduleListItemDataWrapper;
import com.montunosoftware.pillpopper.model.ScheduleMainDrug;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;

import java.util.Collections;
import java.util.List;

public class ScheduleFragmentNew extends Fragment {

    private PillpopperDay mFocusDay;
    private PillpopperActivity mPillpopperActivity;
    private DrugListScheduleDialogeNewBinding binding;
    private ReminderListenerInterfaces mReminderShowListener;
    private ScheduleFragmentDataAdapterNew mScheduleAdapter = new ScheduleFragmentDataAdapterNew();
    private MutableLiveData<List<ScheduleListItemDataWrapper>> itemList = new MutableLiveData<>();
    private int enableMedCount;
    private Context context;
    private List<User> mUserList;

    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(PillpopperConstants.KEY_ACTION) &&
                    intent.getStringExtra(PillpopperConstants.KEY_ACTION).equals(PillpopperConstants.ACTION_HISTORY_EVENTS)) {
                updateView();
                mReminderShowListener.showReminder(true);
            }
        }
    };

    protected void updateView() {
        updateFocusDay();
        getScheduleFromDatabase();
        updateDateTextWithFocusDay();
    }

    private void updateFocusDay() {
        mFocusDay = mPillpopperActivity.getState().getScheduleViewDay();
        RunTimeData.getInstance().setFocusDay(mFocusDay);
    }

    protected void updateDateTextWithFocusDay() {
        mFocusDay = mPillpopperActivity.getState().getScheduleViewDay();
        updateFocusDayText();
    }

    public void updateFocusDayText() {
        if(mFocusDay != null) {
            binding.druglistItemDay.setText(mFocusDay.getHeaderDateText());
        }
    }

    protected void getScheduleFromDatabase() {
        new GetScheduleScreenDataAsyncTask().execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        mPillpopperActivity = (PillpopperActivity) context;
        try {
            mReminderShowListener = (ReminderListenerInterfaces) context;
        } catch (ClassCastException e) {
            PillpopperLog.say(context.toString() + " must implement ReminderListenerInterfaces ", e);
            throw new ClassCastException(context.toString() + " must implement ReminderListenerInterfaces ");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getScheduleFromDatabase();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateView();
        mReminderShowListener.showReminder(true);
        RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);

        IntentFilter mGetStateReceiverIntentFilter = new IntentFilter();
        mGetStateReceiverIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        context.registerReceiver(mGetStateBroadcastReceiver,mGetStateReceiverIntentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.drug_list_schedule_dialoge_new, container, false);
        initValues();
        StateListenerActivity _thisActivity = (StateListenerActivity) getActivity();

        binding.getRoot().setVisibility(View.VISIBLE);
        updateDateTextWithFocusDay();
        setObserver();
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(mPillpopperActivity, FireBaseConstants.ScreenEvent.SCREEN_SCHEDULE_LIST);
        return binding.getRoot();
    }

    private void initValues() {
        mUserList = FrontController.getInstance(mPillpopperActivity).getAllEnabledUsers();
        for(User user:mUserList){
            int count = FrontController.getInstance(mPillpopperActivity).getDrugsListByUserId(user.getUserId()).size();
            enableMedCount = enableMedCount+count;
        }
       // enableMedCount = FrontController.getInstance(mPillpopperActivity).getEnableUsersMedicationCount();
        binding.setScheduleFragment(this);
        binding.setAdapter(mScheduleAdapter);
        binding.setMedCount(0);
    }

    private void setObserver() {
        itemList.observe(getViewLifecycleOwner(), this::setValue);
    }

    public class GetScheduleScreenDataAsyncTask extends
            AsyncTask<Void, Void, List<ScheduleListItemDataWrapper>> {
        @Override
        protected List<ScheduleListItemDataWrapper> doInBackground(Void... params) {
            return FrontController.getInstance(mPillpopperActivity).getMedicationScheduleForDay(mPillpopperActivity, mFocusDay);
        }
        @Override
        protected void onPostExecute(List<ScheduleListItemDataWrapper> result) {
            itemList.postValue(result);
        }
    }

    private void setValue(List<ScheduleListItemDataWrapper> result) {
        if (isAdded()) {
            if (!result.isEmpty()) {
                for (int i = 0; i < result.size(); i++) {
                    Collections.sort(result.get(i).getDrugList(), new ComposableComparator<ScheduleMainDrug>().by(new ScheduleListItemDataWrapper.AlphabeticalByNameComparator()));
                    result.get(i).setSetTimeVisibility(i >= 0);
                }
                Util.setVisibility(new View[]{binding.druglistListview, binding.druglistListlayout}, View.VISIBLE);
                Util.setVisibility(new View[]{binding.scheduleEmptyClockIconImageview, binding.scheduleEmptyTextTextview, binding.fragmentScheduleAddButton, binding.lrCreateNew}, View.GONE);
                mScheduleAdapter.setData(result, this);
                binding.setAdapter(mScheduleAdapter);

                if (mScheduleAdapter.getItemCount() > 0) {
                    binding.druglistListview.scrollToPosition(RunTimeData.getInstance().getScheduleRecyclerViewScrollPosition());
                }
                showMedList();
            } else {
                setupAddScheduleOrMedButton();
            }
        }
    }

    public void onAddMedSchedule() {
        if(enableMedCount<=0){
            FireBaseAnalyticsTracker.getInstance().logEvent(context,FireBaseConstants.Event.ADD_MEDS,
                    FireBaseConstants.ParamName.SOURCE,FireBaseConstants.ParamValue.SCHEDULE_LIST);
            Intent intent = new Intent(context, AddOrEditMedicationActivity.class);
            intent.putExtra(PillpopperConstants.LAUNCH_SOURCE, PillpopperConstants.LAUNCH_SOURCE_SCHEDULE);
            intent.putExtra(PillpopperConstants.LAUNCH_MODE, PillpopperConstants.ACTION_CREATE_PILL);
            startActivity(intent);
        }else {
            if(null!=getActivity()){
                FireBaseAnalyticsTracker.getInstance().logEvent(context,
                        FireBaseConstants.Event.BULK_REMINDER_ADD,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.SCHEDULE_SCREEN_EMPTY_STATE);
                ((HomeContainerActivity)getActivity()).onSetUpReminderQuickActionClicked();
            }
        }
    }

    private void setupAddScheduleOrMedButton(){
        Util.setVisibility(new View[]{binding.druglistListview}, View.GONE);
        Util.setVisibility(new View[]{binding.fragmentScheduleAddButton,binding.lrCreateNew,binding.scheduleEmptyClockIconImageview, binding.scheduleEmptyTextTextview, binding.druglistListlayout}, View.VISIBLE);
        binding.setMedCount(enableMedCount);
        if(enableMedCount >0) {
            showMedList();
        }
    }

    private void showMedList(){
        if(PillpopperConstants.isCanShowMedicationList()){
            PillpopperConstants.setCanShowMedicationList(false);
            ((HomeContainerActivity)mPillpopperActivity).selectItem(HomeContainerActivity.NavigationHome.MEDICATIONS.getPosition());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            context.unregisterReceiver(mGetStateBroadcastReceiver);
        } catch (Exception e) {
            PillpopperLog.say("ScheduleFragment.java: Unable to unregister receiver. ", e);
        }
    }

    public void onDrugClicked(ScheduleMainDrug drugData) {
        RunTimeData.getInstance().setMedDetailView(true);
        RunTimeData.getInstance().setFromArchive(false);
        Intent intent = new Intent(context, MedicationDetailActivity.class);
        intent.putExtra(PillpopperConstants.PILL_ID,drugData.getPillId());
        startActivity(intent);
    }
}
