package com.montunosoftware.pillpopper.android.refillreminder.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderUtils;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.List;

/**
 * Created by M1024581 on 2/20/2018.
 */

public class RefillRemindersListFragment extends Fragment implements View.OnClickListener, RefillRemindersRecyclerViewAdapter.OnItemClickListener, RefillRemindersRecyclerViewAdapter.ChangeDeleteButton{

    private Button mCreateRefillReminder;
    private Button mBtnDeleteRefillReminder;
    private CreateRefillListenerInterface mCallBackListerner;
    private Context mContext;
    private RecyclerView mRefillRemindersRecyclerView;
    private RelativeLayout mDeleteRefillReminder;
    private Menu mDeleteMenu;
    private boolean isEditModeEnabled;
    private RefillRemindersRecyclerViewAdapter adapter;
    private AlertDialog mDeleteAlert;

    @Override
    public void changeDeleteButton() {
        if(adapter.getDeleteReminderList().isEmpty()){
            mBtnDeleteRefillReminder.setBackground(RefillReminderUtils.getDrawableWrapper(mContext, R.drawable.delete_refill_button_background));
            mBtnDeleteRefillReminder.setTextColor(ContextCompat.getColor(mContext, R.color.delete_refill_border_color));
            mBtnDeleteRefillReminder.setEnabled(false);
        }else{
            mBtnDeleteRefillReminder.setBackground(RefillReminderUtils.getDrawableWrapper(mContext, R.drawable.button_delete_refill_list));
            mBtnDeleteRefillReminder.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            mBtnDeleteRefillReminder.setEnabled(true);
        }
    }

    public interface CreateRefillListenerInterface{
        void onCreateRefillReminderClicked();
        void onRefillReminderItemClicked(RefillReminder refillReminder);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.refill_reminders_list_layout,container,false);
        mContext =  getActivity();
        initUI(view);
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_REFILL_REMINDER_LIST);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mDeleteMenu = menu;
        inflater.inflate(R.menu.refill_reminder_edit_list_menu, menu);
        menu.findItem(R.id.refill_reminder_edit).setOnMenuItemClickListener(menuItem -> {
            isEditModeEnabled = true;
            menuItem.setTitle(R.string._edit);
            enableEditMode(menuItem);
            new GetRefillRemindersAsyncTask().execute();
            return true;
        });

        menu.findItem(R.id.refill_reminder_done).setOnMenuItemClickListener(menuItem -> {
            isEditModeEnabled = false;
            disableEditMode(menuItem);
            menuItem.setTitle(R.string.refill_menu_done);
            new GetRefillRemindersAsyncTask().execute();
            return true;
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (null != mDeleteMenu) {
            if (RefillReminderController.getInstance(mContext).getFutureRefillReminders().size() == 0) {
                mDeleteMenu.findItem(R.id.refill_reminder_edit).setVisible(false);
            }
            if (null != mDeleteRefillReminder && mDeleteRefillReminder.getVisibility() == View.VISIBLE) {
                mDeleteMenu.findItem(R.id.refill_reminder_edit).setVisible(false);
                mDeleteMenu.findItem(R.id.refill_reminder_done).setVisible(true);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallBackListerner = (HomeContainerActivity) context;
        } catch (Exception ex){
            RefillReminderLog.say(ex);
        }
    }

    private void initUI(View view) {
        mRefillRemindersRecyclerView = view.findViewById(R.id.rv_refill_reminders);
        LinearLayoutManager recyclerViewLinearLayoutManager = new LinearLayoutManager(mContext) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRefillRemindersRecyclerView.setLayoutManager(recyclerViewLinearLayoutManager);
        mRefillRemindersRecyclerView.setNestedScrollingEnabled(false);

        mCreateRefillReminder = view.findViewById(R.id.btn_create_refill_reminder);
        mDeleteRefillReminder = view.findViewById(R.id.rl_delete_refill_reminder);
        mBtnDeleteRefillReminder = view.findViewById(R.id.btn_delete);
        NestedScrollView mNestedScrollView = view.findViewById(R.id.nested_scroll);
        mCreateRefillReminder.setOnClickListener(this);
        mBtnDeleteRefillReminder.setOnClickListener(this);
        mCreateRefillReminder.setVisibility(View.GONE);
        new GetRefillRemindersAsyncTask().execute();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_create_refill_reminder:
                FireBaseAnalyticsTracker.getInstance().logEvent(mContext,
                        FireBaseConstants.Event.REFILL_REMINDER_CREATE,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.REFILL_REMINDER_LIST);
                mCallBackListerner.onCreateRefillReminderClicked();
                break;

            case R.id.btn_delete:
                if (null != adapter.getDeleteReminderList() && adapter.getDeleteReminderList().size() != 0) {
                    mDeleteAlert = DialogHelpers.showAlertWithConfirmCancelListeners(mContext,
                            R.string.delete_refill_title,
                            R.string.delete_refill_message,
                            new DialogHelpers.Confirm_CancelListener() {
                                @Override
                                public void onConfirmed() {
                                    adapter.deleteRefillReminder();
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_REFILL_REMINDERS"));
                                    refreshRefillReminderList();
                                }
                                @Override
                                public void onCanceled() {
                                    refreshRefillReminderList();
                                }
                    });
                }
                break;
        }
    }

    /**
     * Refreshes the refill reminder list. This will be invoked when user try to delete or cancel the refill reminders
     */
    private void refreshRefillReminderList(){
        isEditModeEnabled = false;
        disableEditMode(mDeleteMenu.findItem(R.id.refill_reminder_done));
        new GetRefillRemindersAsyncTask().execute();
        /*RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, RefillReminderUtils.convertToDp(56, mContext), 0, 0);
        mNestedScrollView.setLayoutParams(params);*/
    }

    private void disableEditMode(MenuItem menuItem) {
        /*RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, RefillReminderUtils.convertToDp(56,mContext),0, 0);
        mNestedScrollView.setLayoutParams(params);*/
        menuItem.setVisible(false).setEnabled(false);
        mDeleteMenu.findItem(R.id.refill_reminder_edit).setVisible(true).setEnabled(true).setTitle(R.string._edit);
        mDeleteRefillReminder.setVisibility(View.GONE);
        mCreateRefillReminder.setVisibility(View.VISIBLE);
    }

    private void enableEditMode(MenuItem menuItem) {
        menuItem.setVisible(false).setEnabled(false);
        mDeleteMenu.findItem(R.id.refill_reminder_done).setVisible(true).setEnabled(true).setTitle(R.string.refill_menu_done);
        mDeleteRefillReminder.setVisibility(View.VISIBLE);
        if(adapter.getDeleteReminderList().isEmpty()){
            mBtnDeleteRefillReminder.setBackground(RefillReminderUtils.getDrawableWrapper(mContext, R.drawable.delete_refill_button_background));
            mBtnDeleteRefillReminder.setTextColor(ContextCompat.getColor(mContext, R.color.delete_refill_border_color));
            mBtnDeleteRefillReminder.setEnabled(false);
        }else{
            mBtnDeleteRefillReminder.setBackground(RefillReminderUtils.getDrawableWrapper(mContext, R.drawable.button_delete_refill_list));
            mBtnDeleteRefillReminder.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            mBtnDeleteRefillReminder.setEnabled(true);
        }
        mCreateRefillReminder.setVisibility(View.GONE);
    }

    public class GetRefillRemindersAsyncTask extends
            AsyncTask<Void, Void, List<RefillReminder>> {

        @Override
        protected List<RefillReminder> doInBackground(Void... params) {
            return RefillReminderController.getInstance(mContext).getFutureRefillReminders();
        }

        @Override
        protected void onPostExecute(List<RefillReminder> result) {
            if (result.size() > 0) {
                adapter = new RefillRemindersRecyclerViewAdapter(mContext, result, isEditModeEnabled);
                mRefillRemindersRecyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(RefillRemindersListFragment.this);
                adapter.setChangeDeleteButtonListener(RefillRemindersListFragment.this);
                if (!isEditModeEnabled) {
                    mCreateRefillReminder.setVisibility(View.VISIBLE);
                } else {
                    mCreateRefillReminder.setVisibility(View.GONE);
                }
            }else{
                // empty list
                adapter = new RefillRemindersRecyclerViewAdapter(mContext, result, isEditModeEnabled);
                mRefillRemindersRecyclerView.setAdapter(adapter);
                onPrepareOptionsMenu(mDeleteMenu);
                mCreateRefillReminder.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemClick(RefillReminder refillReminder) {
        mCallBackListerner.onRefillReminderItemClicked(refillReminder);
    }

    private BroadcastReceiver refreshRefillList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isEditModeEnabled) {
                if(null!=mDeleteAlert && mDeleteAlert.isShowing()){
                    mDeleteAlert.cancel();
                }
                disableEditMode(mDeleteMenu.findItem(R.id.refill_reminder_done));
                isEditModeEnabled = false;
            }
            new GetRefillRemindersAsyncTask().execute();
            try {
                getActivity().invalidateOptionsMenu();
            } catch (Exception e) {
                LoggerUtils.exception(e.getMessage());
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshRefillList,
                new IntentFilter("REFRESH_REFILL_REMINDERS"));
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshRefillList);
        super.onDestroy();
    }
}
