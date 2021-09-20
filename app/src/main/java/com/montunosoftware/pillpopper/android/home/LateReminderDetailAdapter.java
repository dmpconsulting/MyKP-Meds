package com.montunosoftware.pillpopper.android.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by M1023050 on 12/30/2017.
 */

public class LateReminderDetailAdapter extends RecyclerView.Adapter<LateReminderDetailAdapter.LateReminderItemBaseViewHolder> {

    private static final int VIEW_HOLDER_WITH_TIME_HEADER = 0;
    private static final int VIEW_HOLDER_WITHOUT_TIME_HEADER = 1;

    private List<Drug> drugs;
    private Context mContext;
    private String userID;

    private boolean doDelayFinish;

    public LateReminderDetailAdapter(List<Drug> drugs, Context context, String useriD, int islastUser) {
        this.drugs = drugs;
        this.mContext = context;
        this.userID = useriD;
    }

    @Override
    public LateReminderItemBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case VIEW_HOLDER_WITH_TIME_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.late_reminder_item_with_header, parent,false);
                return new LateReminderItemWithTimeHeaderViewHolder(view);

            case VIEW_HOLDER_WITHOUT_TIME_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.late_reminder_item_without_header, parent,false);
                return new LateReminderItemWithoutTimeHeaderViewHolder(view);
        }
        return new LateReminderItemBaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LateReminderItemBaseViewHolder holder, final int position) {
        int itemViewType = getItemViewType(position);
        final int currentPosition = position;
        switch (itemViewType) {
            case VIEW_HOLDER_WITH_TIME_HEADER:
                final LateReminderItemWithTimeHeaderViewHolder viewHolder = (LateReminderItemWithTimeHeaderViewHolder) holder;
                // to make the visibility of first item's top divider gone
                if (position == 0) {
                    viewHolder.mDivider.setVisibility(View.GONE);
                }else{
                    viewHolder.mDivider.setVisibility(View.VISIBLE);
                }
                String headerTime = Util.getTime(drugs.get(position).getScheduledTime().getGmtMilliseconds()).
                        concat(" " + isYesterday(drugs.get(position).getScheduledTime().getGmtMilliseconds()));
                viewHolder.mHeaderTimeTextView.setText(headerTime);
                viewHolder.mDrugName.setText(drugs.get(position).getFirstName());
                viewHolder.mDrugDosage.setText(drugs.get(position).getDose());
                viewHolder.mOverFlowActionsImageView.setOnClickListener(v -> {

                    final PopupMenu popupMenu = new PopupMenu(mContext, v);
                    popupMenu.getMenuInflater().inflate(R.menu.reminder_change_menu, popupMenu.getMenu());

                    popupMenu.setOnDismissListener(popupMenu1 -> popupMenu1.dismiss());
                    popupMenu.setOnMenuItemClickListener(item -> {

                        if (item.getItemId() == R.id.dose_take) {
                            new performGroupActionsAsyncTask().execute(PillpopperConstants.TAKEN, position);
                        } else if (item.getItemId() == R.id.dose_skip) {
                            final LayoutInflater inflater = LayoutInflater.from(mContext);
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialog);
                            builder.setView(inflater.inflate(getEligibleDrugs(drugs, drugs.get(position).getScheduledTime()).size() == 1 ? R.layout.skip_alert : R.layout.skipall_alert, null));
                            builder.setCancelable(false);
                            builder.setPositiveButton("OKAY", (dialog, which) -> new performGroupActionsAsyncTask().execute(PillpopperConstants.SKIPPED, position));
                            builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
                            AlertDialog alertDialog = builder.create();
                            RunTimeData.getInstance().setAlertDialogInstance(alertDialog);
                            alertDialog.show();
                        }

                        return true;
                    });
                    popupMenu.show();
                });

                break;
            case VIEW_HOLDER_WITHOUT_TIME_HEADER:
                LateReminderItemWithoutTimeHeaderViewHolder viewHolderWithoutHeader = (LateReminderItemWithoutTimeHeaderViewHolder) holder;
                viewHolderWithoutHeader.mDrugName.setText(drugs.get(position).getFirstName());
                viewHolderWithoutHeader.mDrugDosage.setText(drugs.get(position).getDose());
                break;
        }
    }

    private void skipGroupMedications(int position) {
        if (drugs != null) {
            PillpopperTime scheduleTime = drugs.get(position).getScheduledTime();

            List<Drug> actedDrugList = getEligibleDrugs(drugs, scheduleTime);
            updatePassedReminderTable(actedDrugList);
            FrontController.getInstance(mContext).performSkipDrug_pastReminders(actedDrugList, PillpopperTime.now(), mContext, true, FireBaseConstants.ParamValue.FOCUS_CARD);
            PillpopperRunTime.getInstance().setPassedRemindersByUserIdForCards(removeUserMedsFromRuntimeData(userID, scheduleTime));
            if(drugs.size() - actedDrugList.size() == 0){
                PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
                if(isLastUser()){
                    FrontController.getInstance(mContext).updateAsNoPendingReminders(mContext);
                }
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_REMINDERS_CARDS_AFTER_ACTION"));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
                doDelayFinish = true;
            } else {
                drugs.removeAll(actedDrugList);
                if(null == drugs || (null != drugs && drugs.size() == 0)){
                    doDelayFinish = true;
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_REMINDERS_CARDS_AFTER_ACTION"));
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
                }
            }
        }

    }

    private void takeGroupMedications(int position) {
        if (drugs != null) {
            PillpopperTime scheduleTime = drugs.get(position).getScheduledTime();
            List<Drug> actedDrugList = getEligibleDrugs(drugs, scheduleTime);
            boolean isLastUser = isLastUser();
            updatePassedReminderTable(actedDrugList);
            FrontController.getInstance(mContext).performTakeDrug_pastReminders(actedDrugList, PillpopperTime.now(), mContext, true, FireBaseConstants.ParamValue.FOCUS_CARD);
            PillpopperRunTime.getInstance().setPassedRemindersByUserIdForCards(removeUserMedsFromRuntimeData(userID, scheduleTime));

            if(drugs.size() - actedDrugList.size() == 0){
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_REMINDERS_CARDS_AFTER_ACTION"));
                PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
                doDelayFinish = true;
            }else{
                drugs.removeAll(actedDrugList);
                if(null == drugs || (null != drugs && drugs.size() == 0)){
                    doDelayFinish = true;
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_REMINDERS_CARDS_AFTER_ACTION"));
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("REFRESH_KPHC_CARDS"));
                }
            }
        }
    }

    private void setActionButtonsText(){
        Button takeButton = ((HomeCardDetailActivity)mContext).findViewById(R.id.card_footer_taken_all);
        Button skipButton = ((HomeCardDetailActivity)mContext).findViewById(R.id.card_footer_skip_all);
        if (takeButton != null && skipButton != null) {
            if (drugs.size() == 1) {
                takeButton.setText(R.string.take);
                skipButton.setText(R.string.skipped);
            } else {
                takeButton.setText(R.string.taken_all);
                skipButton.setText(R.string.skipped_all);
            }
        }
    }

    private String isYesterday(long scheduledTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/MMM/yyyy");
        String compareDate = sdf.format(new Date(scheduledTime));
        Date date = null;
        try {
            date = sdf.parse(compareDate);
            if (date.before(sdf.parse(sdf.format(Calendar.getInstance().getTimeInMillis())))) {
                return mContext.getResources().getString(R.string.late_reminder_header_yesterday);
            }
        } catch (ParseException e) {
            LoggerUtils.exception("- isYesterday -", e);
        }
        return AppConstants.EMPTY_STRING;
    }

    private boolean isLastUser(){
        LinkedHashMap<String,LinkedHashMap<Long,List<Drug>>> list = PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards();
        return null != list && list.size() == 1;
    }

    private LinkedHashMap<String,LinkedHashMap<Long,List<Drug>>> removeUserMedsFromRuntimeData(String userID, PillpopperTime scheduleTime) {
        LinkedHashMap<String,LinkedHashMap<Long,List<Drug>>> list = PillpopperRunTime.getInstance().getPassedRemindersByUserIdForCards();
        if(null!=list && !list.isEmpty()) {
            for (Map.Entry<String, LinkedHashMap<Long, List<Drug>>> _entry : list.entrySet()) {
                if(_entry.getKey().equalsIgnoreCase(userID)) {
                    LinkedHashMap<Long,List<Drug>> copyDrugs = new LinkedHashMap<>(_entry.getValue());
                    for(Map.Entry<Long, List<Drug>> entry : _entry.getValue().entrySet()){
                        if(new PillpopperTime(entry.getKey()/1000).equals(scheduleTime)){
                            copyDrugs.remove(entry.getKey());
                        }
                    }
                    if (!copyDrugs.isEmpty()) {
                        list.put(_entry.getKey(), copyDrugs);
                    }
                }
            }
        }
        return list;
    }

    private void updatePassedReminderTable(List<Drug> pastReminderList) {
        for(Drug drug : pastReminderList){
            FrontController.getInstance(mContext).removeActedPassedReminderFromReminderTable(drug.getGuid(),
                    String.valueOf(drug.getScheduledTime().getGmtMilliseconds()), mContext);
        }
    }

    private List<Drug> getEligibleDrugs(List<Drug> drugList, PillpopperTime pillpopperTime){
        List<Drug> filteredDrugList = new ArrayList<>();
        List<Drug> updatedDrugList = new ArrayList<>();

        for(Drug drug : drugList){
            if(drug.getScheduledTime().equals(pillpopperTime)){
                /*PillpopperLog.say("LateReminder :  pillpopperTime : " + PillpopperTime.getDebugString(pillpopperTime)
                + " And drugSchedule Time is : " + PillpopperTime.getDebugString(drug.getScheduledTime()));*/
                filteredDrugList.add(drug);
            }else{
                updatedDrugList.add(drug);
            }
        }
//        drugs.removeAll(filteredDrugList);
        return filteredDrugList;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return VIEW_HOLDER_WITH_TIME_HEADER;
        } else if (position > 0 &&
                drugs.get(position - 1).getScheduledTime().getGmtSeconds() != drugs.get(position).getScheduledTime().getGmtSeconds()) {
            return VIEW_HOLDER_WITH_TIME_HEADER;
        }
        return VIEW_HOLDER_WITHOUT_TIME_HEADER;
    }

    @Override
    public int getItemCount() {
        return drugs.size();
    }


    public class LateReminderItemBaseViewHolder extends RecyclerView.ViewHolder {
        public LateReminderItemBaseViewHolder(View view) {
            super (view);
        }
    }

    public class LateReminderItemWithTimeHeaderViewHolder extends LateReminderItemBaseViewHolder {
        private TextView mHeaderTimeTextView;
        private TextView mDrugName;
        private TextView mDrugDosage;
        private ImageView mOverFlowActionsImageView;
        private View mDivider;

        public LateReminderItemWithTimeHeaderViewHolder(View view) {
            super (view);
            mHeaderTimeTextView = view.findViewById(R.id.late_reminder_time);
            mDrugName = view.findViewById(R.id.med_item_name);
            mDrugDosage = view.findViewById(R.id.med_item_dose);
            mOverFlowActionsImageView = view.findViewById(R.id.lr_overflow_actions_icon);
            mDivider = view.findViewById(R.id.late_reminder_divider);
        }
    }

    public class LateReminderItemWithoutTimeHeaderViewHolder extends LateReminderItemBaseViewHolder {
        private TextView mDrugName;
        private TextView mDrugDosage;
        private LateReminderItemWithoutTimeHeaderViewHolder(View view) {
            super (view);
            mDrugName = view.findViewById(R.id.med_item_name);
            mDrugDosage = view.findViewById(R.id.med_item_dose);
        }
    }

    private class performGroupActionsAsyncTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((Activity) mContext).startActivityForResult(new Intent(mContext, LoadingActivity.class),0);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LoggerUtils.exception("Exception - onPreExecute ",e);
            }
            if(params[0] == PillpopperConstants.TAKEN){
                takeGroupMedications(params[1]);
            }else if(params[0] == PillpopperConstants.SKIPPED){
                skipGroupMedications(params[1]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (null != mContext) {
                ((Activity) mContext).finishActivity(0);
            }
            if(doDelayFinish){
                new Handler().post(() -> ((HomeCardDetailActivity) mContext).finish());
            }else {
                if (null == drugs || (null != drugs && drugs.size() == 0)) {
                    if(null != mContext) {
                        ((HomeCardDetailActivity) mContext).finish();
                    }
                } else{
                    notifyDataSetChanged();
                    setActionButtonsText();
                }
            }
        }
    }
}