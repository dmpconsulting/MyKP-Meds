package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.ScheduleCustomSelectionItemBinding;
import com.montunosoftware.pillpopper.android.FrequencySpinnerAdapter;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

public class CustomScheduleWizardFragment extends Fragment {

    private ScheduleCustomSelectionItemBinding binding;
    private String[] customOptions;
    private String customId;
    private Context mContext;
    private ScheduleViewModel scheduleViewmodel;
    private int checkPositionOfSpinner = -1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.schedule_custom_selection_item, container, false);
        customOptions = getResources().getStringArray(R.array.custom_day_option);
        intiValues();
        initUIReferences();
        if(null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
            if(!RunTimeData.getInstance().getScheduleData().getMeditationDuration().equalsIgnoreCase("Daily")&&
            !RunTimeData.getInstance().getScheduleData().getMeditationDuration().contains("Weekly") &&
            !RunTimeData.getInstance().getScheduleData().getMeditationDuration().equalsIgnoreCase("Set Reminder")) {
                initEditModeValue();
            }
        }
        return binding.getRoot();
    }

    private void initEditModeValue() {
        if (null != RunTimeData.getInstance().getScheduleData()) {
            if (null != RunTimeData.getInstance().getScheduleData().getDurationType() &&
                    RunTimeData.getInstance().getScheduleData().getDurationType().equalsIgnoreCase("Weekly")) {
                binding.spinnerCustom.setSelection(1);
                binding.editTextCustomDays.setText(String.valueOf(RunTimeData.getInstance().getScheduleData().getDuration() / 7));
                RunTimeData.getInstance().setScheduleEdited(false);
                checkPositionOfSpinner = 1;
            } else {
                binding.editTextCustomDays.setText(String.valueOf(RunTimeData.getInstance().getScheduleData().getDuration()));
                if(RunTimeData.getInstance().getScheduleData().getDuration()!=0) {
                    RunTimeData.getInstance().setScheduleEdited(false);
                }
                checkPositionOfSpinner = 0;
            }
        }
    }

    private void intiValues() {
        binding.setRobotoMedium(ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_MEDIUM));
        binding.setRobotoRegular(ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_REGULAR));
        binding.setCustomScheduleWizard(this);
        binding.btnClear.setVisibility(View.GONE);
        scheduleViewmodel = RunTimeData.getInstance().getScheduleViewModel(getActivity());
    }

    public void clearDaysText() {
        binding.editTextCustomDays.setText("");
        binding.btnClear.setVisibility(View.GONE);
        scheduleViewmodel.getCustomFrequencyNumber().postValue("");
    }

    public void initUIReferences() {
        FrequencySpinnerAdapter adapter = new FrequencySpinnerAdapter(mContext, R.layout.custom_frequency_spinner_layout, customOptions);
        binding.spinnerCustom.setAdapter(adapter);

        binding.editTextCustomDays.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }

            @Override
            public void afterTextChanged(Editable str) {
                if (str.toString().length() == 0 || str.toString().startsWith("0")) {
                    str.clear();
                    binding.btnClear.setVisibility(View.GONE);
                } else {
                    binding.btnClear.setVisibility(View.VISIBLE);
                    binding.btnClear.setContentDescription(getActivity().getResources().getString(R.string.content_description_reset_values));
                }
                scheduleViewmodel.getCustomFrequencyNumber().postValue(str.toString());
                RunTimeData.getInstance().setScheduleEdited(true);
            }
        });

        if (null == customId)
            customId = customOptions[0];
        scheduleViewmodel.setCustomFrequency(customId);

        binding.spinnerCustom.setOnTouchListener((view, motionEvent) -> {
            Util.hideKeyboard(getActivity(), binding.editTextCustomDays);
            return false;
        });
        binding.spinnerCustom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                customId = customOptions[position];
                scheduleViewmodel.setCustomFrequency(customId);
              if(position != checkPositionOfSpinner)
              {
                  RunTimeData.getInstance().setScheduleEdited(true);
              }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.hideKeyboard(getContext(), binding.editTextCustomDays);
        binding.editTextCustomDays.clearFocus();
    }
}