package com.montunosoftware.pillpopper.android.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.KphcDrug;

import java.util.List;

/**
 * Created by M1032896 on 12/14/2017.
 */

public class KPHCDetailAdapter extends RecyclerView.Adapter<KPHCDetailAdapter.ViewHolder> {

    private List<KphcDrug> drugs;

    public KPHCDetailAdapter(List<KphcDrug> drugs) {
        this.drugs = drugs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_update_kphc_med_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.drugName.setText(Util.getFirstName(drugs.get(position).getPillName()));
        holder.drugDosage.setText(drugs.get(position).getDose());
        holder.drugDescription.setText(drugs.get(position).getInstruction());
        if (drugs.get(position).getInstruction() == null || drugs.get(position).getInstruction().trim().isEmpty()) {
            holder.drugDescription.setVisibility(View.GONE);
        } else {
            holder.drugDescription.setVisibility(View.VISIBLE);
        }
        if (drugs.get(position).getPrescriptionId() == null || drugs.get(position).getPrescriptionId().trim().isEmpty()) {
            holder.drugPrescriptionId.setVisibility(View.GONE);
        } else {
            holder.drugPrescriptionId.setVisibility(View.VISIBLE);
            holder.drugPrescriptionId.setText("Rx# "+drugs.get(position).getPrescriptionId());
        }
    }

    @Override
    public int getItemCount() {
        return drugs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView drugName, drugDosage, drugDescription, drugPrescriptionId;

        public ViewHolder(View itemView) {
            super(itemView);
            drugName = itemView.findViewById(R.id.kphc_med_item_name);
            drugDosage = itemView.findViewById(R.id.kphc_med_item_dose);
            drugDescription = itemView.findViewById(R.id.kphc_med_item_description);
            drugPrescriptionId = itemView.findViewById(R.id.kphc_med_item_number);
        }
    }
}
