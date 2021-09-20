package com.montunosoftware.pillpopper.android.refillreminder.views;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderUtils;
import com.montunosoftware.pillpopper.android.refillreminder.RequestWrapper;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;

import org.kp.tpmg.mykpmeds.activation.AppConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by M1024581 on 2/21/2018.
 */

public class RefillRemindersRecyclerViewAdapter extends RecyclerView.Adapter<RefillRemindersRecyclerViewAdapter.ViewHolder> {


    private final List<RefillReminder> mListItemData;
    private final Context mContext;
    private final Typeface mFontRegular;
    private boolean isEditModeEnabled;
    private List<RefillReminder> mRefillDeletionList = new ArrayList<>();
    private HomeContainerActivity refillReminderInterface;


    public RefillRemindersRecyclerViewAdapter(Context context, List<RefillReminder> listData, boolean isEditModeEnabled) {
        this.mListItemData = listData;
        mContext = context;
        this.isEditModeEnabled = isEditModeEnabled;
        refillReminderInterface  = (HomeContainerActivity) context;
        mFontRegular = RefillReminderUtils.setFontStyle(context, AppConstants.FONT_ROBOTO_REGULAR);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.refill_reminder_recycler_item, parent, false);
        RefillRemindersRecyclerViewAdapter.ViewHolder viewHolder = new RefillRemindersRecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.txtRefillReminderDate.setText(getFormattedDate(mListItemData.get(position).getNextReminderDate()));
        holder.txtRefillReminderTime.setText(getFormattedTime(mListItemData.get(position).getNextReminderDate()));
        holder.txtRefillReminderNotes.setText(mListItemData.get(position).getReminderNote());
        if (mListItemData.get(position).isRecurring()) {
            holder.repeatImage.setVisibility(View.VISIBLE);
        } else {
            holder.repeatImage.setVisibility(View.GONE);
        }
        holder.currentRefillReminder = mListItemData.get(position);
        holder.rootView.setContentDescription(getContentDescriptionByPosition(position));
        if(isEditModeEnabled){
            holder.mRlCheckBox.setVisibility(View.VISIBLE);
        }else{
            holder.mRlCheckBox.setVisibility(View.GONE);
        }
        holder.mCheckBox.setOnClickListener(v -> {

            if (holder.mCheckBox.isChecked()) {
                mRefillDeletionList.add(mListItemData.get(position));
                holder.mCheckBox.setTag("selected");
            } else {
                mRefillDeletionList.remove(mListItemData.get(position));
                holder.mCheckBox.setTag("unselected");
            }
            mChangeDeleteBtnInterface.changeDeleteButton();
        });
    }

    private String getContentDescriptionByPosition(int position){
        String space = " ";
        StringBuilder builder = new StringBuilder();
        builder.append(getFormattedDate(mListItemData.get(position).getNextReminderDate()));
        builder.append(space);
        builder.append(mListItemData.get(position).getReminderNote());
        builder.append(space);
        builder.append(mContext.getString(R.string.edit_refill_reminder));
        return builder.toString();
    }

    public List<RefillReminder> getDeleteReminderList(){
        return mRefillDeletionList;
    }

    public void deleteRefillReminder(){
        List<String> listOfGuid = new ArrayList<>();
        RequestWrapper requestWrapper = new RequestWrapper(mContext);
        if(null != mRefillDeletionList && mRefillDeletionList.size() != 0) {
            for (RefillReminder obj : mRefillDeletionList) {
                listOfGuid.add(obj.getReminderGuid());
                mListItemData.remove(obj);
                RefillReminderController.getInstance(mContext).deleteRefillReminderByReminderGUID(obj.getReminderGuid());
                refillReminderInterface.addLogEntryForRefillReminderUpdate(requestWrapper.createDeleteRefillReminderRequest(obj.getReminderGuid()));
            }
            RefillReminderNotificationUtil.getInstance(mContext).createNextRefillReminderAlarms(mContext);
        }
    }

    private String getFormattedDate(String nextReminderDate) {
        Date date = new Date();
        date.setTime(Long.parseLong(nextReminderDate) * 1000);
        return RefillReminderUtils.getDate(date);
    }

    private String getFormattedTime(String nextReminderDate) {
        Date date = new Date();
        date.setTime(Long.parseLong(nextReminderDate) * 1000);
        return RefillReminderUtils.getTime(date);
    }

    @Override
    public int getItemCount() {
        return mListItemData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RefillReminder currentRefillReminder;
        private TextView txtRefillReminderDate;
        private TextView txtRefillReminderTime;
        private TextView txtRefillReminderNotes;
        private ImageView repeatImage;
        private RelativeLayout mRlCheckBox;
        private CheckBox mCheckBox;
        private LinearLayout rootView;
        public ViewHolder(View v) {
            super(v);
            txtRefillReminderDate = v.findViewById(R.id.tv_next_refill_date);
            txtRefillReminderTime = v.findViewById(R.id.tv_next_refill_time);
            txtRefillReminderNotes = v.findViewById(R.id.tv_refill_notes);
            repeatImage = v.findViewById(R.id.refill_repeat);
            mRlCheckBox = v.findViewById(R.id.rl_checkbox);
            mCheckBox = v.findViewById(R.id.refill_check_box);
            txtRefillReminderDate.setTypeface(mFontRegular);
            txtRefillReminderNotes.setTypeface(mFontRegular);
            rootView = v.findViewById(R.id.parentView);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (!isEditModeEnabled) {
                if (null != currentRefillReminder) {
                    mOnItemClickListener.onItemClick(currentRefillReminder);
                }
            } else {
                mCheckBox.setChecked(!mCheckBox.isChecked());
                if (mCheckBox.getTag().toString().equalsIgnoreCase("unselected")) {
                    mCheckBox.setTag("selected");
                    mRefillDeletionList.add(mListItemData.get(getAdapterPosition()));
                } else {
                    mCheckBox.setTag("unselected");
                    mRefillDeletionList.remove(mListItemData.get(getAdapterPosition()));
                }
                mChangeDeleteBtnInterface.changeDeleteButton();
            }
        }
    }

    private RefillRemindersRecyclerViewAdapter.OnItemClickListener mOnItemClickListener;
    private RefillRemindersRecyclerViewAdapter.ChangeDeleteButton mChangeDeleteBtnInterface;

    public interface OnItemClickListener {
        void onItemClick(RefillReminder refillReminder);
    }

    public void setOnItemClickListener(RefillRemindersRecyclerViewAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface ChangeDeleteButton{
        void changeDeleteButton();
    }

    public void setChangeDeleteButtonListener(RefillRemindersRecyclerViewAdapter.ChangeDeleteButton changeDeleteButton) {
        mChangeDeleteBtnInterface = changeDeleteButton;
    }
}
