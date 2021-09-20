package com.montunosoftware.pillpopper.android;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.model.ScheduleListItemDataWrapper;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.List;

import static com.montunosoftware.mymeds.BR.drugdapter;
import static com.montunosoftware.mymeds.BR.robotoMedium;
import static com.montunosoftware.mymeds.BR.robotoRegular;
import static com.montunosoftware.mymeds.BR.scheduleList;

public class ScheduleFragmentDataAdapterNew extends RecyclerView.Adapter<ScheduleFragmentDataAdapterNew.ScheduleFragmentViewHolder> {

    private List<ScheduleListItemDataWrapper> mScheduleListItemData;
    private LayoutInflater layoutInflator;
    private ViewDataBinding binding;
//    private ScheduleFragmentDosesDataAdapterNew adapter = new ScheduleFragmentDosesDataAdapterNew();
    private ScheduleFragmentNew activity;
    private Typeface mFontMedium;
    private Typeface mFontRegular;

    public void setData(List<ScheduleListItemDataWrapper> scheduleListItemDataList, ScheduleFragmentNew activity) {
        this.mScheduleListItemData = scheduleListItemDataList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ScheduleFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        mFontMedium = ActivationUtil.setFontStyle(parent.getContext(), AppConstants.FONT_ROBOTO_MEDIUM);
        mFontRegular = ActivationUtil.setFontStyle(parent.getContext(),AppConstants.FONT_ROBOTO_REGULAR);

        if(null == layoutInflator) layoutInflator = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(layoutInflator, R.layout.schedule_list_item_header_layout_new,parent,false);
        return new ScheduleFragmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleFragmentViewHolder viewHolder, int position) {
        ScheduleFragmentDosesDataAdapterNew adapter = new ScheduleFragmentDosesDataAdapterNew();
        adapter.setValue(mScheduleListItemData.get(position).getDrugList(),activity);
        viewHolder.bind(mScheduleListItemData.get(position), adapter);
        LinearLayout lrParent = binding.getRoot().findViewById(R.id.lr_parent);
        LinearLayout lrChild = binding.getRoot().findViewById(R.id.lr_child);
        if (1 == mScheduleListItemData.get(position).getDrugList().size() || 2 == mScheduleListItemData.get(position).getDrugList().size()) {
            lrParent.setVisibility(View.GONE);
            lrChild.setVisibility(View.VISIBLE);
        } else {
            lrParent.setVisibility(View.VISIBLE);
            lrChild.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.mScheduleListItemData.size();
    }

    public class ScheduleFragmentViewHolder extends RecyclerView.ViewHolder {
        public ScheduleFragmentViewHolder(@NonNull ViewDataBinding itemView) {
            super(itemView.getRoot());
        }
        public void bind(ScheduleListItemDataWrapper data, ScheduleFragmentDosesDataAdapterNew adapter)
        {
            binding.setVariable(robotoMedium,mFontMedium);
            binding.setVariable(robotoRegular,mFontRegular);
            binding.setVariable(scheduleList,data);
            binding.setVariable(drugdapter,adapter);
            binding.executePendingBindings();
        }
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
