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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.GreatJobAlertForTakenAllActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DateAndTimePickerDialog;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.ReminderSnoozePicker;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import static android.app.Activity.RESULT_OK;

public class CurrentReminderExpandedAdapter extends RecyclerView.Adapter<CurrentReminderExpandedAdapter.ViewHolder> {

    private int cardIndex;
    private Long mReminderTime;
    private Context context;
    private User[] user;
    private long takenEarlierTime;
    private int postponeTimeSeconds;
    private List<Drug> finalDrugs = new ArrayList<>();
    private TreeMap<User, LinkedList<Drug>> currentReminderByUserName;
    private int lastAction = -1;


    public CurrentReminderExpandedAdapter(Context context, TreeMap<User, LinkedList<Drug>> currentReminderByUserName, Long reminderTime, int cardIndex) {
        this.context = context;
        this.cardIndex = cardIndex;
        this.currentReminderByUserName = currentReminderByUserName;
        user = currentReminderByUserName.keySet().toArray(new User[currentReminderByUserName.keySet().size()]);
        mReminderTime = reminderTime;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_reminder_expanded_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.drugUserName.setText(user[position].getDisplayName());
        setUserLevelPopupMenu(holder, position);
        holder.drugContainer.removeAllViews();
        boolean pendingActionForDrug = false;
        for (Drug drug : currentReminderByUserName.get(user[position])) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout row = (RelativeLayout) inflater.inflate(R.layout.current_reminder_expanded_sub_item, null);
            ((TextView) row.findViewById(R.id.med_item_name)).setText(drug.getFirstName());
            if (drug.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                ((TextView) row.findViewById(R.id.med_item_dose)).setText(drug.getDose());
                pendingActionForDrug = true;
            } else {
                row.findViewById(R.id.med_item_3dot_icon).setEnabled(false);
                row.findViewById(R.id.med_item_3dot_icon).setVisibility(View.GONE);
                ((TextView) row.findViewById(R.id.med_item_dose)).setText(getAction(drug.getmAction()));
            }
            setMedicationLevelPopUpMenu(holder, row, position, drug);
            holder.drugContainer.addView(row);
        }

        if (pendingActionForDrug) {
            holder.userLevelOverflowIcon.setVisibility(View.VISIBLE);
        } else {
            holder.userLevelOverflowIcon.setVisibility(View.GONE);
            RunTimeData.getInstance().setCurrentReminderCardRefreshRequired(true);
        }

        if (position != (user.length - 1)) {
            holder.drugDivider.setVisibility(View.VISIBLE);
        } else {
            holder.drugDivider.setVisibility(View.GONE);
        }
    }

    private void setUserLevelPopupMenu(ViewHolder holder, final int position) {
        ImageView userLevelOverflowIcon = holder.userLevelOverflowIcon;
        userLevelOverflowIcon.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            if (getCurrentReminderDrugsCount(user[position]) > 1) {
                popupMenu.getMenuInflater().inflate(R.menu.cur_rem_user_level_overflow_menu_2, popupMenu.getMenu());
            } else {
                popupMenu.getMenuInflater().inflate(R.menu.cur_rem_user_level_overflow_menu, popupMenu.getMenu());
            }

            if (Calendar.getInstance().getTimeInMillis() - mReminderTime > PillpopperConstants.LATE_REMINDER_INTERVAL) {
                popupMenu.getMenu().findItem(R.id.taken_earlier).setVisible(true);
                popupMenu.getMenu().findItem(R.id.take_later).setVisible(false);
            } else {
                popupMenu.getMenu().findItem(R.id.taken_earlier).setVisible(false);
                popupMenu.getMenu().findItem(R.id.take_later).setVisible(true);
            }
            popupMenu.setOnDismissListener(popupMenu1 -> {

            });
            popupMenu.setOnMenuItemClickListener(item -> {

                final List<Drug> drugs = currentReminderByUserName.get(user[position]);

                if (item.getItemId() == R.id.dose_skip) {

                    DialogHelpers.showSkipMedDialog(context, drugs.size() == 1 ? PillpopperConstants.ACTION_SKIP_PILL : PillpopperConstants.ACTION_SKIP_ALL_PILL, () -> {
                        lastAction = -1;
                        updateDrug(drugs, PillpopperConstants.SKIPPED);
                    });

                } else if (item.getItemId() == R.id.dose_take) {
                    lastAction = PillpopperConstants.TAKEN;
                    updateDrug(drugs, PillpopperConstants.TAKEN);

                } else if (item.getItemId() == R.id.take_later) {

                    ReminderSnoozePicker reminderSnoozePicker = new ReminderSnoozePicker();
                    reminderSnoozePicker.setHourMinutePickedListener(hhmm -> {
                        postponeTimeSeconds = (hhmm[0] * 60 + hhmm[1]) * 60;
                        String postponeError = Drug.validatePostpones(drugs, postponeTimeSeconds, context);
                        if (postponeError != null) {
                            DialogHelpers.showPostponeErrorAlert(context);
                        } else {
                            lastAction = -1;
                            updateDrug(drugs, PillpopperConstants.TAKE_LATER);
                        }
                    });

                    reminderSnoozePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "remind_later_time");

                } else if (item.getItemId() == R.id.taken_earlier) {

                    DateAndTimePickerDialog dateAndTimePickerDialog = new DateAndTimePickerDialog(
                            context,
                            pillpopperTime -> {
                                takenEarlierTime = pillpopperTime.getGmtSeconds();
                                lastAction = PillpopperConstants.TAKE_EARLIER;
                                updateDrug(drugs, PillpopperConstants.TAKE_EARLIER);
                            }, false, PillpopperTime.now(), 15, context.getResources().getString(R.string.taken_text), true
                    );

                    dateAndTimePickerDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "taken_earlier_time");

                }

                return true;
            });
            popupMenu.show();
        });
    }

    private void setMedicationLevelPopUpMenu(ViewHolder holder, RelativeLayout row, int position, final Drug drug) {
        ImageView dotsIcon = row.findViewById(R.id.med_item_3dot_icon);
        dotsIcon.setOnClickListener(view -> {
            {
                PopupMenu popupMenu = new PopupMenu(context, view);
                if (Calendar.getInstance().getTimeInMillis() - mReminderTime > PillpopperConstants.LATE_REMINDER_INTERVAL) {
                    //the value is changed to 10mins for regression later will be reverted to 1hr
                    popupMenu.getMenuInflater().inflate(R.menu.reminder_screen_late_menu, popupMenu.getMenu());
                } else {
                    popupMenu.getMenuInflater().inflate(R.menu.reminder_screen_menu, popupMenu.getMenu());
                }
                popupMenu.setOnDismissListener(popupMenu1 -> {

                });
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.dose_skip) {

                        DialogHelpers.showSkipMedDialog(context, PillpopperConstants.ACTION_SKIP_PILL, () -> updateDrug(drug, PillpopperConstants.SKIPPED));

                    } else if (item.getItemId() == R.id.dose_take) {
                        updateDrug(drug, PillpopperConstants.TAKEN);

                    } else if (item.getItemId() == R.id.dose_remind_later) {
                        ReminderSnoozePicker reminderSnoozePicker = new ReminderSnoozePicker();
                        reminderSnoozePicker.setHourMinutePickedListener(hhmm -> {
                            postponeTimeSeconds = (hhmm[0] * 60 + hhmm[1]) * 60;
                            List<Drug> selectedDrug = new ArrayList<>();
                            selectedDrug.add(drug);
                            String postponeError = Drug.validatePostpones(selectedDrug, postponeTimeSeconds, context);
                            if (postponeError != null) {
                                DialogHelpers.showPostponeErrorAlert(context);
                            } else {
                                updateDrug(drug, PillpopperConstants.TAKE_LATER);
                            }
                        });
                        reminderSnoozePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "remind_later_time");

                    } else if (item.getItemId() == R.id.taken_earlier) {
                        DateAndTimePickerDialog dateAndTimePickerDialog = new DateAndTimePickerDialog(
                                context,
                                pillpopperTime -> {
                                    takenEarlierTime = pillpopperTime.getGmtSeconds();
                                    updateDrug(drug, PillpopperConstants.TAKE_EARLIER);
                                }, false, PillpopperTime.now(), 15, context.getResources().getString(R.string.taken_text), true
                        );
                        dateAndTimePickerDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "taken_earlier_time");
                    }

                    return true;
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return user.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View drugDivider;
        TextView drugUserName;
        LinearLayout drugContainer;
        ImageView userLevelOverflowIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            drugDivider = itemView.findViewById(R.id.current_reminder_divider);
            drugUserName = itemView.findViewById(R.id.drug_username);
            drugContainer = itemView.findViewById(R.id.drug_container);
            userLevelOverflowIcon = itemView.findViewById(R.id.drug_username_action);
        }
    }

    private void updateDrug(List<Drug> drugs, int action) {
        finalDrugs.clear();
        for (Drug drug : drugs) {
            // selecting only those drugs for which action is not taken
            if (drug.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                drug.setmAction(action);
                finalDrugs.add(drug);
            }
        }
        new UpdateDrugState().execute(action);
    }

    private void updateDrug(Drug drug, int action) {
        finalDrugs.clear();
        drug.setmAction(action);
        finalDrugs.add(drug);
        new UpdateDrugState().execute(action);
    }


    class UpdateDrugState extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((Activity) context).startActivityForResult(new Intent(context, LoadingActivity.class), 0);
        }

        @Override
        protected Boolean doInBackground(Integer... action) {
            boolean canClose = true;
            try {
                switch (action[0]) {
                    case PillpopperConstants.TAKEN:
                        FrontController.getInstance(context).performTakeDrug(finalDrugs, null, context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                        break;
                    case PillpopperConstants.SKIPPED:
                        FrontController.getInstance(context).performSkipDrug(finalDrugs, PillpopperTime.now(), context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                        break;
                    case PillpopperConstants.TAKE_LATER:
                        FrontController.getInstance(context).performPostponeDrugs(finalDrugs, postponeTimeSeconds, context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                        break;
                    case PillpopperConstants.TAKE_EARLIER:
                        FrontController.getInstance(context).performAlreadyTakenDrugs(finalDrugs, new PillpopperTime(takenEarlierTime), context, true, FireBaseConstants.ParamValue.FOCUS_CARD);
                        break;
                }

                for (User user : currentReminderByUserName.keySet()) {
                    List<Drug> listOfDrugsToBeTaken = currentReminderByUserName.get(user);
                    if (listOfDrugsToBeTaken != null && !listOfDrugsToBeTaken.isEmpty()) {
                        for (Drug d : listOfDrugsToBeTaken) {
                            if (d.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                                canClose = false;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                PillpopperLog.say(e);
            }
            return canClose;
        }

        @Override
        protected void onPostExecute(Boolean canClose) {
            super.onPostExecute(canClose);
            notifyDataSetChanged();
            ((Activity) context).finishActivity(0);
            if (canClose) {
                Util.saveCardIndex(context, cardIndex);
                if (FrontController.getInstance(context).getPassedReminderDrugs(context).size() > 0) {
                    FrontController.getInstance(context).updateAsPendingRemindersPresent(context);
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_REMINDERS_CARDS_AFTER_ACTION"));
                PillpopperRunTime.getInstance().setCardAdjustmentRequired(true);
                closeCurrentReminder();
            } else {
                setActionButtonsText();
            }
        }
    }

    private String getAction(int drugAction) {
        String action = context.getString(R.string.skipped);
        switch (drugAction) {
            case PillpopperConstants.TAKEN:
            case PillpopperConstants.TAKE_EARLIER:
                action = context.getString(R.string.taken);
                break;
            case PillpopperConstants.TAKE_LATER:
                action = context.getString(R.string.drug_action_postponed);
                break;
        }
        return action;
    }

    private void closeCurrentReminder() {
        if (lastAction != -1 && (lastAction == PillpopperConstants.TAKEN || lastAction == PillpopperConstants.TAKE_EARLIER) && (null == PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId() || PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId().isEmpty())) {
            ((FragmentActivity)context).startActivityForResult(new Intent(context, GreatJobAlertForTakenAllActivity.class), PillpopperConstants.REQUEST_CURRENT_REMINDER_CARD_DETAIL);
        } else {
            ((Activity) context).finishActivity(0);
            new Handler().post(() -> {
                try {
                    ((Activity) context).setResult(RESULT_OK);
                    ((Activity) context).finish();
                } catch (Exception e) {
                    PillpopperLog.say(e);
                }
            });
        }
    }

    private void setActionButtonsText(){
        Button takeButton = ((FragmentActivity)context).findViewById(R.id.card_footer_taken_all);
        Button skipButton = ((FragmentActivity)context).findViewById(R.id.card_footer_skip_all);
        TextView takenEarlier = ((FragmentActivity)context).findViewById(R.id.footer_taken_earlier);
        if (takeButton != null && skipButton != null) {
            if (getCurrentReminderDrugsCount() == 1) {
                takeButton.setText(R.string.take);
                skipButton.setText(R.string.skipped);
                if(takenEarlier.getVisibility() == View.VISIBLE){
                    takenEarlier.setText(context.getString(R.string.card_taken_earlier));
                }
            } else {
                takeButton.setText(R.string.taken_all);
                skipButton.setText(R.string.skipped_all);
                if(takenEarlier.getVisibility() == View.VISIBLE){
                    takenEarlier.setText(context.getString(R.string.taken_all_earlier));
                }
            }
        }
    }

    public int getCurrentReminderDrugsCount() {
        int drugsCount = 0;
        if(null != currentReminderByUserName) {
            User[] users = currentReminderByUserName.keySet().toArray(new User[currentReminderByUserName.keySet().size()]);
            for (User user : users) {
                for (Drug drug : currentReminderByUserName.get(user)) {
                    if (drug.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                        drugsCount++;
                    }
                }
            }
        }
        return drugsCount;
    }

    public int getCurrentReminderDrugsCount(User user) {
        int drugsCount = 0;
        if(null != currentReminderByUserName) {
            for (Drug drug : currentReminderByUserName.get(user)) {
                if (drug.getmAction() == PillpopperConstants.NO_ACTION_TAKEN) {
                    drugsCount++;
                }
            }
        }
        return drugsCount;
    }
}
