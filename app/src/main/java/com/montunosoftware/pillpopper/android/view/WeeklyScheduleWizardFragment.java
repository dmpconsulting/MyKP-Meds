package com.montunosoftware.pillpopper.android.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.ScheduleWeeklySelectionItemBinding;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.ArrayList;
import java.util.Comparator;

public class WeeklyScheduleWizardFragment extends Fragment {
    private ScheduleWeeklySelectionItemBinding binding;
    private ArrayList<String> selectedDays;
    MutableLiveData<ArrayList<String>> daysList;
    private boolean isChanged;
    int selectedPosition;
    private boolean isLaunched;
    private int countSelectedWeek;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        selectedDays = new ArrayList<>();
        binding = DataBindingUtil.inflate(inflater,R.layout.schedule_weekly_selection_item,container,false);
        Typeface mFontMedium = ActivationUtil.setFontStyle(getActivity(),AppConstants.FONT_ROBOTO_MEDIUM);
        Typeface mFontBold = ActivationUtil.setFontStyle(getActivity(),AppConstants.FONT_ROBOTO_BOLD);
        binding.setWeeklyScheduleWizard(this);
        binding.daysLabel.setTypeface(mFontMedium);
        binding.sunday.setTypeface(mFontBold);
        binding.monday.setTypeface(mFontBold);
        binding.tuesday.setTypeface(mFontBold);
        binding.wednesday.setTypeface(mFontBold);
        binding.thursday.setTypeface(mFontBold);
        binding.friday.setTypeface(mFontBold);
        binding.saturday.setTypeface(mFontBold);
        if(null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
            initEditModeValue();
        }
        return binding.getRoot();

    }

    private void initEditModeValue() {
        String  days= RunTimeData.getInstance().getScheduleData().getSelectedDays();
        if(null != days) {
            for (int i = 0; i < days.length(); i++) {
                char daysAtSelection = days.charAt(i);
                onDaySelected(Integer.parseInt(daysAtSelection + ""));
            }
        }
    }

    public void setBackgroundColor(TextView view1, int selection)
    {
        if(0 == selection) {

            view1.setTextColor(Util.getColorWrapper(getContext(), R.color.black));
            view1.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.weekdays_background_circle));
        } else {
            view1.setTextColor(Util.getColorWrapper(getContext(), R.color.white));
            view1.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.weekdays_background_circle_blue));
        }
    }

    public void setSelection(TextView view,String day) {
        if (selectedDays.isEmpty()) {
            isLaunched = false;
            setBackgroundColor(view, 1);
            selectedDays.add(day);
            postSelectedDaysValue();
            return;
        } else {

            for (int i = 0; i < selectedDays.size(); i++) {
                if (day.equalsIgnoreCase(selectedDays.get(i))) {
                    isChanged = true;
                    selectedPosition = i;
                    break;
                }
            }
        }
        if (isChanged) {
            countSelectedWeek++;
            setBackgroundColor(view, 0);
            selectedDays.remove(selectedPosition);
        } else {
            countSelectedWeek++;
            setBackgroundColor(view, 1);
            selectedDays.add(day);
        }
        postSelectedDaysValue();

        if (null != RunTimeData.getInstance().getScheduleData() && null != RunTimeData.getInstance().getScheduleData().getSelectedDays()) {
            if (countSelectedWeek >= RunTimeData.getInstance().getScheduleData().getSelectedDays().length()) {
                RunTimeData.getInstance().setScheduleEdited(true);
            }
        }

    }

    Comparator<String> comparator = (s1, s2) -> Integer.parseInt(s1) - Integer.parseInt(s2);

    private void postSelectedDaysValue(){
        ScheduleViewModel scheduleViewModel = ViewModelProviders.of(getActivity()).get(ScheduleViewModel.class);
        MutableLiveData<ArrayList<String>> days = scheduleViewModel.getWeeklySelectedDays();
        selectedDays.sort(comparator);
        days.postValue(selectedDays);
    }

    //for displaying schedule reminder wile selecting days
    public void showreminder() {
        ScheduleViewModel scheduleViewModel = ViewModelProviders.of(getActivity()).get(ScheduleViewModel.class);
        MutableLiveData<Integer> isWeekelySelected = scheduleViewModel.getFrequencySelector();
        MutableLiveData<ArrayList<String>> days = scheduleViewModel.getWeeklySelectedDays();
        if (!isLaunched && 1 == selectedDays.size()) {
            isLaunched = true;
            isWeekelySelected.postValue(1);
        }
        if (selectedDays.isEmpty()) {
            isWeekelySelected.postValue(2);
        }
        days.postValue(selectedDays);
    }

    public void onDaySelected(int day) {
        isChanged = false;

        switch (day) {
            case 1:
                setSelection(binding.sunday, "1");
                break;
            case 2:
                setSelection(binding.monday, "2");
                break;
            case 3:
                setSelection(binding.tuesday, "3");
                break;
            case 4:
                setSelection(binding.wednesday, "4");
                break;
            case 5:
                setSelection(binding.thursday, "5");
                break;
            case 6:
                setSelection(binding.friday, "6");
                break;
            case 7:
                setSelection(binding.saturday, "7");
                break;
            default:
                break;
        }
    }
}
