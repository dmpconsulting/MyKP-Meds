package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.ScheduleWizardMedListAdapterBinding;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.List;

import static org.kp.tpmg.ttg.BR.medList;

public class ScheduleWizardMedicationListAdapter extends RecyclerView.Adapter<ScheduleWizardMedicationListAdapter.ViewHolder> {

    private  List<Drug> medListDetail;
    private LayoutInflater layoutInflater;
    private ScheduleWizardMedListAdapterBinding binding;
    private Context context;
    private Typeface mFontMedium;

    public void setData(List<Drug> medListDetail, FragmentActivity activity) {
        this.medListDetail = medListDetail;
        context = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        mFontMedium = ActivationUtil.setFontStyle(context,AppConstants.FONT_ROBOTO_MEDIUM);
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.schedule_wizard_med_list_adapter,parent,false);
        return new ScheduleWizardMedicationListAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        binding.setAdapterList(new ScheduleWizardFragment());
        ImageUILoaderManager.getInstance().loadDrugImage(context, medListDetail.get(position).getImageGuid(), medListDetail.get(position).getGuid(), binding.drugImage, Util.getDrawableWrapper(context, R.drawable.pill_default));
        viewHolder.bindData(medListDetail.get(position));
        binding.medName.setTypeface(mFontMedium);
    }

    @Override
    public int getItemCount() {
        return medListDetail.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull ScheduleWizardMedListAdapterBinding itemView) {
            super(itemView.getRoot());
        }

        public void bindData(Drug data)
        {
            binding.setVariable(medList,data);
            binding.executePendingBindings();

        }

    }
}
