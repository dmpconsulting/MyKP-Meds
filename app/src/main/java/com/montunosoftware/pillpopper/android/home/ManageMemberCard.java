package com.montunosoftware.pillpopper.android.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableInt;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.activity.MembersAdapter;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.service.SetUpProxyEnableService;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by m1032896 on 11/21/2017.
 */

public class ManageMemberCard implements Parcelable,HomeCard,SetUpProxyEnableService.SetUpProxyEnableResponseListener {
    public static final String YES = "Y";
    public static final String NO = "N";
    private int banner;
    private String description;
    private String title;
    private Context context;
    public final ObservableInt trackCounter = new ObservableInt();
    private List<User> list;

    public ManageMemberCard() {

    }

    @Override
    public void setContext(Context context) {
        this.context = context;
        list= DatabaseUtils.getInstance(context).getAllUsers();
    }

    protected ManageMemberCard(Parcel in) {
        banner = in.readInt();
        title = in.readString();
        description = in.readString();
    }

    public static final Parcelable.Creator<ManageMemberCard> CREATOR = new Parcelable.Creator<ManageMemberCard>() {
        @Override
        public ManageMemberCard createFromParcel(Parcel in) {
            return new ManageMemberCard(in);
        }

        @Override
        public ManageMemberCard[] newArray(int size) {
            return new ManageMemberCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeInt(banner);
        parcel.writeString(description);
    }

    @Override
    public int getRequestCode() {
        return PillpopperConstants.REQUEST_SETUP_MANAGE_MEMBERS_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return view.getContext().getString(R.string.manage_your_family_medication_card);
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.card_title_manage_from_phone);
    }

    @Override
    public int getCardView() {
        return R.layout.home_card;
    }

    @Override
    public int getDetailView() {
        return R.layout.card_detail_layout_manage_member;
    }

    @Override
    public String getDescription() {
        if(list.size() == 1 && context instanceof HomeCardDetailActivity){
            return String.format(context.getString(R.string.card_manage_member_second_description),AppConstants.KP_MANAGE_MEMBER_URL);
        }else {
            return context.getString(R.string.card_manage_member_description);
        }
    }

    @Override
    public int getBanner() {
        banner = R.drawable.card_tutorial_5;
        return banner;
    }


    public void initProxyUserList(RecyclerView membersRecyclerView){

        if((list==null || list.isEmpty()) && null!= RunTimeData.getInstance().getRegistrationResponse()){
            list = RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers();
        }
        initAdapter(membersRecyclerView);
    }

    private void initAdapter(RecyclerView membersRecyclerView) {
        if(list==null) return;
        removeTeenUser(RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers());
        final MembersAdapter membersAdapter = new MembersAdapter(list, trackCounter, context);
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(membersRecyclerView.getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(membersRecyclerView.getContext(),DividerItemDecoration.VERTICAL);
        membersRecyclerView.addItemDecoration(dividerItemDecoration);
        membersRecyclerView.setAdapter(membersAdapter);
    }


    public void onClickSaveMembers(){

        Intent intent = new Intent(context, LoadingActivity.class);
        ((AppCompatActivity)context).startActivityForResult(intent, 0);
        List<User> selectedUsers=new ArrayList<>();
        for(User userItem:list) {
            final String medicationsEnabled = userItem.isSelected()? YES : NO;
            userItem.setEnabled(medicationsEnabled);
            saveDataToDB(userItem.getUserId(),medicationsEnabled,medicationsEnabled);
            if(("Y").equals(medicationsEnabled)) selectedUsers.add(userItem);
        }

        if(!selectedUsers.isEmpty()) {
            SetUpProxyEnableService setUpProxyEnableService = new SetUpProxyEnableService(context, selectedUsers, this);
            setUpProxyEnableService.execute(AppConstants.getPillSetProxyEnableURL());
        }else{
            onSetUpProxyResponseReceived(0);
        }

    }

    private void saveDataToDB(String userId,String medicationsEnabled, String remindersEnabled) {
        FrontController.getInstance(context).updateMemberPreferencesToDB(userId,medicationsEnabled,remindersEnabled);
    }

    @Override
    public void onSetUpProxyResponseReceived(int result) {
        if (context!=null) {
            switch (result) {
                case 0:
                    RunTimeData.getInstance().setEnabledUsersList(FrontController.getInstance(context).getAllEnabledUsers());
                    RunTimeData.getInstance().setSelectedUsersList(FrontController.getInstance(context).getEnabledUserIds());
                    checkLogEntryAndInitiateAPICalls();
                    break;
                case -1:
                    ((HomeCardDetailActivity)context).finishActivity(0);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.unable_to_update));
                    builder.setMessage(context.getString(R.string.no_internet_connection));
                    builder.setCancelable(false);
                    builder.setPositiveButton(context.getString(R.string.ok_text), (dialog, which) -> dialog.dismiss());

                    AlertDialog alert = builder.create();
                    if(!(((HomeCardDetailActivity)context).isFinishing())) {
                        RunTimeData.getInstance().setAlertDialogInstance(alert);
                        alert.show();
                    }

                    Button btnPositive = alert.findViewById(android.R.id.button1);
                    btnPositive.setTextColor(ActivationUtil.getColorWrapper(context, R.color.kp_theme_blue));
                    break;
                default:
                    break;
            }
        }
    }

    private void checkLogEntryAndInitiateAPICalls() {
        //registering broadcast receiver
        IntentFilter initialGetStateIntentFilter = new IntentFilter();
        initialGetStateIntentFilter.addAction(StateDownloadIntentService.BROADCAST_REFRESH_KPHC_FOR_MANAGE_MEMBERS_SELECTION);
        context.registerReceiver(mGetStateBroadcastReceiver,initialGetStateIntentFilter);
        //check for pending log entries
        StateDownloadIntentService.handleHistoryFailure(false);
        if(FrontController.getInstance(context).isLogEntryAvailable()){
            PillpopperLog.say("Starting Intermediate Sync, ");
            StateDownloadIntentService.startActionIntermediateGetState(context.getApplicationContext());
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(showDialogReceiver,new IntentFilter());
        PillpopperLog.say("Starting Get State and Get History Events");
        StateDownloadIntentService.startActionGetState(context.getApplicationContext());
        StateDownloadIntentService.startActionGetHistoryEvents(context.getApplicationContext());
        StateDownloadIntentService.startActionForDaylightSavingAdjustmentNeeded(context.getApplicationContext());

    }

    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleServicesComplete();
        }
    };

    private void handleServicesComplete() {
        PillpopperLog.say("SettingsManageMembersActivity --- Services Completed..");
        context.unregisterReceiver(mGetStateBroadcastReceiver);
        PillpopperRunTime.getInstance().setHistorySyncDone(true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
        delayFinish();
    }

    private BroadcastReceiver showDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != context && !((HomeContainerActivity)context).isFinishing()) {
                Util.showSessionexpireAlert(context, PillpopperAppContext.getGlobalAppContext(context.getApplicationContext()));
            }
            if(null != context)
                LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(showDialogReceiver);
        }
    };

    //Required to avoid black screen flick while going back to HomeContainerActivity screen to refresh homecard adapter
    private void delayFinish() {
        ((AppCompatActivity) context).finishActivity(0);
        new Handler().post(() -> {
            try {
                ((AppCompatActivity) context).setResult(RESULT_OK);
                ((AppCompatActivity) context).finish();
            } catch (Exception e) {
                PillpopperLog.say(e);
            }
        });
    }

    public void removeTeenUser(List<User> userList){
        List<User> notTeenUsers = new ArrayList<>(userList);
        for(User user : userList){
            if(user.isTeen() && !Util.getTeenToggleEnabled()){

                notTeenUsers.remove(user);
            }
        }
        list.clear();
        list=new ArrayList<>(notTeenUsers);
    }
}