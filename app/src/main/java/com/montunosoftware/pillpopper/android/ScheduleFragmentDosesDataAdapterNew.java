package com.montunosoftware.pillpopper.android;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.ScheduleFragmentDrugListAdapterBinding;
import com.montunosoftware.pillpopper.model.ScheduleMainDrug;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.List;

import static com.montunosoftware.mymeds.BR.doseListscheduleFragment;
import static com.montunosoftware.mymeds.BR.drugListData;

public class ScheduleFragmentDosesDataAdapterNew extends RecyclerView.Adapter<ScheduleFragmentDosesDataAdapterNew.ScheduleFragmentDosesDataAdapterNewViewHolder> {

    private List<ScheduleMainDrug> drugList;
    private LayoutInflater inflator;
    private ScheduleFragmentDrugListAdapterBinding binding;
    private ScheduleFragmentNew activity;
    private Typeface mFontItalic;
    private Typeface mFontMedium;

    public void setValue(List<ScheduleMainDrug> drugList, ScheduleFragmentNew activity) {
        this.drugList = drugList;
        this.activity = activity;
        mFontItalic = ActivationUtil.setFontStyle(activity.getContext(), AppConstants.FONT_ROBOTO_ITALIC);
        mFontMedium = ActivationUtil.setFontStyle(activity.getContext(), AppConstants.FONT_ROBOTO_MEDIUM);
    }

    @NonNull
    @Override
    public ScheduleFragmentDosesDataAdapterNewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        if(null == inflator) inflator = LayoutInflater.from(parent.getContext());
        binding = DataBindingUtil.inflate(inflator, R.layout.schedule_fragment_drug_list_adapter,parent,false);
        return new ScheduleFragmentDosesDataAdapterNewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleFragmentDosesDataAdapterNewViewHolder viewHolder, int position) {
        viewHolder.bind(drugList.get(position));
        binding.setRobotoItalic(mFontItalic);
        binding.setRobotoMedium(mFontMedium);
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    public class ScheduleFragmentDosesDataAdapterNewViewHolder extends RecyclerView.ViewHolder {
        public ScheduleFragmentDosesDataAdapterNewViewHolder(@NonNull ViewDataBinding itemView) {
            super(itemView.getRoot());
        }
        public void bind(ScheduleMainDrug data)
        {
            binding.setVariable(drugListData,data);
            binding.setVariable(doseListscheduleFragment,activity);
            binding.executePendingBindings();
        }
    }
}
