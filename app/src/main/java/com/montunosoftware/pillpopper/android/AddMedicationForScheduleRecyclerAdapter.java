package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.ViewClickHandler;
import com.montunosoftware.pillpopper.android.view.DrugDetailRoundedImageView;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager;

import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.util.List;

public class AddMedicationForScheduleRecyclerAdapter extends RecyclerView.Adapter<AddMedicationForScheduleRecyclerAdapter.MedicationHolder> {

    private Context context;
    private List<Drug> drugList;

    private CheckBoxSelectionListener checkBoxSelectionListener;

    public interface CheckBoxSelectionListener {
        void onCheckedOrUnChecked(Boolean checked, String pillId);
    }

    public AddMedicationForScheduleRecyclerAdapter(Context context, List<Drug> drugList, CheckBoxSelectionListener callback) {
        this.context = context;
        this.drugList = drugList;
        checkBoxSelectionListener = callback;
    }

    @NonNull
    @Override
    public MedicationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_list_item, viewGroup, false);
        return new MedicationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationHolder medicationHolder, int position) {
        Drug drug = drugList.get(position);
        ImageUILoaderManager.getInstance().loadDrugImage(context, drug.getImageGuid(), drug.getGuid(), medicationHolder.drugImage, Util.getDrawableWrapper(context, R.drawable.pill_default));
        medicationHolder.drugName.setText(drug.getFirstName());
        StringBuilder genericNameDosage = new StringBuilder();
        if (Util.isEmptyString(drug.getGenericName()) && Util.isEmptyString(drug.getDose())) {
            medicationHolder.drugGenericNameDosage.setVisibility(View.GONE);
        } else {
            if (!Util.isEmptyString(drug.getGenericName())) {
                genericNameDosage.append(drug.getGenericName()).append(" ");
            }
            if (!Util.isEmptyString(drug.getDose())) {
                genericNameDosage.append(drug.getDose());
            }
            medicationHolder.drugGenericNameDosage.setVisibility(View.VISIBLE);
            medicationHolder.drugGenericNameDosage.setText(genericNameDosage.toString());
        }
        medicationHolder.checkBox.setChecked(drug.isChecked());
        medicationHolder.checkBox.setOnClickListener(view -> {
            CheckBox checkBox = (CheckBox) view;
            drug.setChecked(checkBox.isChecked());
            checkBoxSelectionListener.onCheckedOrUnChecked(checkBox.isChecked(), drug.getGuid());
        });
        medicationHolder.reminderInfo.setVisibility(drug.getSchedule().getTimeList().length() > 0 &&
                (null == drug.getSchedule().getEnd() || !drug.getSchedule().getEnd().before(PillpopperDay.today())) ? View.VISIBLE : View.GONE);

        medicationHolder.drugImage.setOnClickListener(view -> EnlargeImageActivity.expandPillImage(context, drug.getGuid(), drug.getImageGuid()));
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    public class MedicationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private DrugDetailRoundedImageView drugImage;
        private TextView drugName;
        private TextView drugGenericNameDosage;
        private CheckBox checkBox;
        private RelativeLayout medicationRow;
        private TextView reminderInfo;

        MedicationHolder(@NonNull View itemView) {
            super(itemView);
            drugImage = itemView.findViewById(R.id.drug_image);
            drugName = itemView.findViewById(R.id.drug_name);
            drugGenericNameDosage = itemView.findViewById(R.id.drug_generic_name_dosage);
            checkBox = itemView.findViewById(R.id.check_box);
            medicationRow = itemView.findViewById(R.id.medication_row);
            reminderInfo = itemView.findViewById(R.id.drug_reminder_info);
            drugName.setOnClickListener(this);
            medicationRow.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.drug_name) {
                ViewClickHandler.preventMultiClick(view);
                RunTimeData.getInstance().setMedDetailView(false);
                RunTimeData.getInstance().setFromArchive(false);
                Intent intent = new Intent(context, MedicationDetailActivity.class);
                intent.putExtra(PillpopperConstants.PILL_ID, drugList.get(getAdapterPosition()).getGuid());
                context.startActivity(intent);
            } else if (view.getId() == R.id.medication_row) {
                checkBox.setChecked(!checkBox.isChecked());
                /**
                 * the below lines contain checkBox.isChecked() instead of "!"
                 * because the checkbox will be updated in the above line with the check status.
                 */
                drugList.get(getAdapterPosition()).setChecked(checkBox.isChecked());
                checkBoxSelectionListener.onCheckedOrUnChecked(checkBox.isChecked(), drugList.get(getAdapterPosition()).getGuid());
            }
        }
    }

}
