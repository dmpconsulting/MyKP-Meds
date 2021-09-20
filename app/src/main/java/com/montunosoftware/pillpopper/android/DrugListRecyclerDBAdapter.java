package com.montunosoftware.pillpopper.android;

import android.Manifest;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DrugDetailRoundedImageView;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.Schedule;
import com.montunosoftware.pillpopper.model.State;
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.utils.MultiClickViewPreventHandler;

import java.util.ArrayList;
import java.util.List;


public class DrugListRecyclerDBAdapter extends RecyclerView.Adapter<DrugListRecyclerDBAdapter.ViewHolder> {

    private PillpopperActivity mPillpopperActivity;
    private List<Drug> mDrugList;
    private boolean mIsEditMode;
    private State mState;
    private static final int HEADER = 0;
    private List<Drug> mArchiveDrugList = new ArrayList<>();
    private static final int DRUG = 1;
    private FrontController mFrontController;
    private OnItemClickListener mItemClickListener;
    private onRefillClickListener mRefillClickListener;
    private int userSharePosition;
    public DrugListRecyclerDBAdapter(PillpopperActivity thisActivity, List<Drug> list, boolean isEditModeActive, State state/*, List<String> noDrugUsers*/) {
        this.mPillpopperActivity = thisActivity;
        this.mDrugList = list;
        this.mIsEditMode = isEditModeActive;
        this.mState = state;
    }

    @Override
    public int getItemViewType(int position) {
        if (mDrugList.get(position).isTempHeadr()) {
            return HEADER;
        } else {
            return DRUG;
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drug_recycler_member_name, parent, false);
            return new ViewHolderHeader(v);
        } else if (viewType == DRUG) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drug_recycler_item, parent, false);
            return new ViewHolder(v);
        } else {
            throw new RuntimeException("Could not inflate layout");
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        mFrontController = FrontController.getInstance(mPillpopperActivity);
        if (holder instanceof ViewHolderHeader) {
            ((ViewHolderHeader) holder).memberHeaderLayout.setVisibility(View.VISIBLE);
            if (isUserWithNoDrugs(mDrugList.get(position).getUserID())) {
                ((ViewHolderHeader) holder).noDrugsTextView.setVisibility(View.VISIBLE);
               /* if (null != primaryUserId && primaryUserId.equalsIgnoreCase(mDrugList.get(position).getUserID())) {*/
                    ((ViewHolderHeader) holder).noDrugsTextView.setText(R.string.no_medication_available);
                    ((ViewHolderHeader) holder).shareImage.setEnabled(false);
                     ((ViewHolderHeader) holder).shareImage.setAlpha(new Float("0.5"));

                ((ViewHolderHeader) holder).noDrugsTextView.setTextColor(Util.getColorWrapper(mPillpopperActivity, R.color.app_info_color));
               /* }*/
            } else {
                ((ViewHolderHeader) holder).noDrugsTextView.setVisibility(View.GONE);
                if(!mIsEditMode) {
                    ((ViewHolderHeader) holder).shareImage.setEnabled(true);
                    ((ViewHolderHeader) holder).shareImage.setAlpha(new Float("1.0"));
                }
            }

            ((ViewHolderHeader) holder).memberNameText.setText(mDrugList.get(position).getMemberFirstName());
            ((ViewHolderHeader) holder).refillText.setContentDescription(mPillpopperActivity.getString(R.string.content_description_refill_medication));
            ((ViewHolderHeader) holder).refillText.setOnClickListener(view -> {
                if (null != mRefillClickListener) {
                    if(!mIsEditMode) {
                        RefillRuntimeData.getInstance().setUserIdToLoadRxRefill(mDrugList.get(position).getUserID());
                        mRefillClickListener.onRefillClick();
                        FireBaseAnalyticsTracker.getInstance().logEvent(mPillpopperActivity,
                                FireBaseConstants.Event.REFILL_MEDS,
                                FireBaseConstants.ParamName.SOURCE,
                                FireBaseConstants.ParamValue.MED_LIST);
                    }
                }
            });
            ((ViewHolderHeader) holder).shareImage.setContentDescription(mPillpopperActivity.getString(R.string.content_description_email_medication));
            ((ViewHolderHeader) holder).shareImage.setOnClickListener(view -> {
                if(!mIsEditMode) {
                    userSharePosition = position;
                    if (PermissionUtils.checkVersionCode()) {
                        if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, mPillpopperActivity.getAndroidContext())) {
                            shareMedications();
                        }
                    } else {
                        shareMedications();
                    }
                }
            }

            );
        } else {
            Drug drug = mDrugList.get(position);

            // Name
            if (null != drug.getName()) {
                holder.drugNameText.setText(drug.getFirstName());
                if (!TextUtils.isEmpty(drug.getGenericName())) {
                    holder.drugGenericText.setVisibility(View.VISIBLE);
                    holder.drugGenericText.setText(drug.getGenericName());
                } else {
                    holder.drugGenericText.setVisibility(View.GONE);
                }
            }

            // Dosage
            if (!TextUtils.isEmpty(drug.getDose())) {
                holder.drugDosageText.setVisibility(View.VISIBLE);
                holder.drugDosageText.setText(drug.getDose());
            }else{
                holder.drugDosageText.setVisibility(View.GONE);
            }

            if (mIsEditMode) {
                holder.drugSelectionCheckbox.setVisibility(View.VISIBLE);
                holder.emptyView.setVisibility(View.VISIBLE);
            }else{
                holder.drugSelectionCheckbox.setVisibility(View.GONE);
                holder.emptyView.setVisibility(View.GONE);
            }

            if (mDrugList.get(position).getPendingManagedChange() == Drug.PendingManagedChange.Add
                    || mDrugList.get(position).getPendingManagedChange() == Drug.PendingManagedChange.Change) {
                // KPHC Medication
                if(mDrugList.get(position).getPendingManagedChange() == Drug.PendingManagedChange.Add) {
                    holder.kphcBarIndicator.setVisibility(View.VISIBLE);
                    holder.drugReminderInfoText.setVisibility(View.VISIBLE);
                    holder.drugReminderInfoText.setText(R.string.managed_drug_changed_header);
                    holder.drugReminderInfoText.setTextColor(Util.getColorWrapper(mPillpopperActivity, R.color.new_from_kphc_bar_clr));
                }else if( mDrugList.get(position).getPendingManagedChange() == Drug.PendingManagedChange.Change){
                    holder.kphcBarIndicator.setVisibility(View.VISIBLE);
                    holder.drugReminderInfoText.setVisibility(View.VISIBLE);
                    holder.drugReminderInfoText.setText(R.string.managed_drug_updated_header);
                    holder.drugReminderInfoText.setTextColor(Util.getColorWrapper(mPillpopperActivity, R.color.new_from_kphc_bar_clr));
                }
            } else {
                holder.kphcBarIndicator.setVisibility(View.GONE);
                if (mDrugList.get(position).getScheduleCount() == 1) {
                    // Reminders has been set
                    // We dont have to show "scheduled" sub text if the medication schedules are expired.
                    if(null == mDrugList.get(position).getSchedule().getEnd() || mDrugList.get(position).getSchedule().getEnd().after(PillpopperDay.today()) ||
                            mDrugList.get(position).getSchedule().getEnd().equals(PillpopperDay.today())) {
                        holder.drugReminderInfoText.setVisibility(View.VISIBLE);
                        holder.drugReminderInfoText.setText(R.string.scheduled);
                        holder.drugReminderInfoText.setTextColor(ContextCompat.getColor(mPillpopperActivity, R.color.medication_schedule_status));
                    }else if(mDrugList.get(position).getSchedule().getEnd().before(PillpopperDay.today())){
                        holder.drugReminderInfoText.setVisibility(View.GONE);
                    }
                } else {
                    // No Reminders has been set
                    holder.drugReminderInfoText.setVisibility(View.GONE);
                }

				if(!Util.getScheduleChoice(mDrugList.get(position)).equalsIgnoreCase(AppConstants.SCHEDULE_CHOICE_SCHEDULED)) {
                    int historyCount = mFrontController.getPillHistoryEventCountForToday(mDrugList.get(position).getGuid());
                    String dailyLimit = mDrugList.get(position).getPreferences().getPreference("maxNumDailyDoses");
                    if (dailyLimit != null && !("").equalsIgnoreCase(dailyLimit) && !("-1").equalsIgnoreCase(dailyLimit) && !("0").equalsIgnoreCase(dailyLimit) && historyCount >= Util.handleParseInt(dailyLimit)) {
                        holder.drugDailyLimitText.setVisibility(View.VISIBLE);
                        holder.drugDailyLimitText.setText(R.string.at_limit);
                    } else {
                        holder.drugDailyLimitText.setVisibility(View.GONE);
                    }
                }else{
                    holder.drugDailyLimitText.setVisibility(View.GONE);
                }
            }

            //to show notes icon
            if ((null !=mDrugList.get(position).getNotes()) && (!mDrugList.get(position).getNotes().isEmpty())){
                holder.med_notes.setVisibility(View.VISIBLE);
            }else {
                holder.med_notes.setVisibility(View.INVISIBLE);
            }

            ImageUILoaderManager.getInstance().loadDrugImage(mPillpopperActivity, drug.getImageGuid(),drug.getGuid(), holder.drugImage, Util.getDrawableWrapper(mPillpopperActivity, R.drawable.pill_default));

            holder.drugSelectionCheckbox.setChecked(mDrugList.get(position).isChecked());

            holder.drugSelectionCheckbox.setOnClickListener(v -> {
                CheckBox cb = (CheckBox) v;
                mDrugList.get(position).setIsChecked(cb.isChecked());
                if (mDrugList.get(position).isChecked()) {
                     mArchiveDrugList.add(mDrugList.get(position));
                } else {
                    mArchiveDrugList.remove(mDrugList.get(position));
                }
                if (null != mItemClickListener) {
                    mItemClickListener.onItemClick(holder.drugSelectionCheckbox, position, mArchiveDrugList);
                }
            });
            holder.itemView.setTag(drug);
        }
    }

    private List<Drug> getDrugsListForShareByUserId(String userID) {
        List<Drug> drugsList = new ArrayList<>();
        for (int i = 0; i < mDrugList.size(); i++) {
            Drug drug = mDrugList.get(i);
            if(drug.getUserID().equalsIgnoreCase(userID)){
                if (drug.getGuid() != null) {
                    drugsList.add(drug);
                }
            }
        }
        return drugsList;
    }

    private boolean isUserWithNoDrugs(String userID) {
        for (int i = 0; i < mDrugList.size(); i++) {
            if (mDrugList.get(i).getUserID().equalsIgnoreCase(userID) && mDrugList.get(i).getGuid() != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return mDrugList.size();
    }

    public void shareMedications(){
        List<Drug> memberDrugList = getDrugsListForShareByUserId(mDrugList.get(userSharePosition).getUserID());
        mState.getDrugList().emailDrugListAsHtml(mPillpopperActivity, memberDrugList);
        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mPillpopperActivity, FireBaseConstants.Event.MED_LIST_SHARE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView drugNameText;
        private TextView drugGenericText;
        private TextView drugDosageText;
        private TextView drugReminderInfoText;
        private TextView kphcBarIndicator;
        private CheckBox drugSelectionCheckbox;
        private TextView drugDailyLimitText;
        private TextView emptyView;
        private ImageView med_notes;
        private DrugDetailRoundedImageView drugImage;
        public ViewHolder(View itemView) {
            super(itemView);
            drugNameText = itemView.findViewById(R.id.druglist_item_drugname);
            drugGenericText = itemView.findViewById(R.id.druglist_item_drug_generic_name);
            drugDosageText = itemView.findViewById(R.id.druglist_item_dosage);
            drugReminderInfoText = itemView.findViewById(R.id.druglist_item_reminder_status);
            kphcBarIndicator = itemView.findViewById(R.id.druglist_item_managed_change_bar);
            drugSelectionCheckbox = itemView.findViewById(R.id.drug_select_img_btn);
            drugDailyLimitText = itemView.findViewById(R.id.druglist_item_daily_limit);
            emptyView = itemView.findViewById(R.id.empty_view);
            med_notes = itemView.findViewById(R.id.med_notes);

            drugImage = itemView.findViewById(R.id.druglist_item_drug_image);
            if(drugImage!=null) {
                drugImage.setDefaultImage(R.drawable.pill_default);
            }

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            MultiClickViewPreventHandler.preventMultiClick(v);
            if (!mIsEditMode) {
                RunTimeData.getInstance().setMedDetailView(true);
                RunTimeData.getInstance().setFromArchive(false);
                Intent intent = new Intent(mPillpopperActivity, MedicationDetailActivity.class);
                intent.putExtra(PillpopperConstants.PILL_ID, mDrugList.get(getAdapterPosition()).getGuid());
                mPillpopperActivity.startActivity(intent);

            }else if(mIsEditMode){
                CheckBox cb = v.findViewById(R.id.drug_select_img_btn);
                cb.setChecked(!cb.isChecked());
                mDrugList.get(getAdapterPosition()).setIsChecked(cb.isChecked());

                if (("unselected").equalsIgnoreCase(cb.toString())) {
                    cb.setTag("selected");
                    mArchiveDrugList.add(mDrugList.get(getAdapterPosition()));
                } else {
                    cb.setTag("unselected");
                    mArchiveDrugList.remove(mDrugList.get(getAdapterPosition()));
                }
                if (null != mItemClickListener) {
                    mItemClickListener.onItemClick(cb, getAdapterPosition(), mArchiveDrugList);
                }
            }
        }
    }

    public class ViewHolderHeader extends ViewHolder {
        private TextView memberNameText;
        private TextView refillText;
        private ImageView shareImage;
        private RelativeLayout memberHeaderLayout;
        private TextView noDrugsTextView;

        public ViewHolderHeader(View itemView) {
            super(itemView);
            memberNameText = itemView.findViewById(R.id.member_name_txtView);
            refillText = itemView.findViewById(R.id.refill_txtView);
            shareImage = itemView.findViewById(R.id.share_med_list);
            memberHeaderLayout = itemView.findViewById(R.id.drugList_member_header);
            noDrugsTextView = itemView.findViewById(R.id.no_drugs_found);
            if(mIsEditMode){
                shareImage.setAlpha(new Float("0.5"));
                shareImage.setEnabled(false);
                refillText.setAlpha(new Float("0.5"));
                refillText.setEnabled(false);
            }
            itemView.setOnClickListener(null);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, List<Drug> archivedDrugCount);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setCallBackForRefill(final onRefillClickListener mRefillListener) {
        this.mRefillClickListener = mRefillListener;
    }

    public interface onRefillClickListener {
        void onRefillClick();
    }

    public int getDrugsCount(){
        int count = 0;
        for(Drug drug : mDrugList){
            if(!drug.isTempHeadr()){
                count++;
            }
        }
        return count;
    }
}
