package com.montunosoftware.pillpopper.android;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.ReminderTimeFragmentBinding;
import com.montunosoftware.pillpopper.SaveScheduleOnBackPressListener;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.NLPUtils;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.ViewClickHandler;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.ScheduleViewModel;
import com.montunosoftware.pillpopper.android.view.TimePickerDialog;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.HistoryEvent;
import com.montunosoftware.pillpopper.model.BulkSchedule;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.HourMinute;
import com.montunosoftware.pillpopper.model.NLPReminder;
import com.montunosoftware.pillpopper.model.NLPSigValidationRequestObj;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.service.NLPScheduleValidationService;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttg.utils.MultiClickViewPreventHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.montunosoftware.pillpopper.android.util.Util.convertTimeTo12HrFormat;
import static com.montunosoftware.pillpopper.android.util.Util.convertTimeTo24HrFormat;
import static com.montunosoftware.pillpopper.android.util.Util.get24FormatTimeFromHrMin;
import static com.montunosoftware.pillpopper.android.util.Util.getAmPmTimeFromHrMin;

public class ReminderTimeFragment extends Fragment implements SaveScheduleOnBackPressListener {

    private ReminderTimeFragmentBinding binding;
    private int reminderLayoutIdBase = 2000;
    private int reminderLayoutId = reminderLayoutIdBase;

    private int maxReminders = 10;
    private List<String> savingTime = new ArrayList<>();
    private int remHour;
    private int remMin;
    private List<Integer> hrMinSavingTime = new ArrayList<>();
    private String[] timingsArray = {"7:00 " + Util.getSystemAMFormat()};

    private PillpopperDay startReminderDay;
    private PillpopperDay endReminderDay;

    private DatePickerDialog datePicker;

    private ScheduleViewModel scheduleViewmodel;
    private String strNewTime;
    private String selectedTime;
    private int mPositionClicked = -1;
    private Typeface mFontRegular;
    private Typeface mFontMedium;
    private BulkSchedule bulkSchedule;
    private MutableLiveData<String> customFrequencyNumber;
    private MutableLiveData<ArrayList<String>> weeklySelectedDays;
    private FrontController mFrontController;
    private String days = "Days";
    private String weeks = "Weeks";

    private int START_DATE = 1;
    private AlertDialog alertScheduleSaveDialog;
    private SaveScheduleListener mSaveScheduleListener;
    private boolean scheduleSaved = false;

    private Handler delayHandler;
    private Handler navigateHandler;
    private Runnable dismissDialogRunnable;
    private Runnable navigateRunnable;
    private boolean savingFromOverlay = false;

    @Override
    public void saveScheduleOnBackPress(Context context) {
        saveSchedule(null);
    }

    public interface SaveScheduleListener {
        void onSaveScheduleClicked();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.reminder_time_fragment, container, false);
        initValues();
        initUI();
        if (null != RunTimeData.getInstance().getScheduleData() &&
                RunTimeData.getInstance().getScheduleData().isEditMedicationClicked() &&
                RunTimeData.getInstance().getScheduleData().isReminderAdded()) {
            initEditModeValue();
        } else if (null != RunTimeData.getInstance().getScheduleData() &&
                RunTimeData.getInstance().getScheduleData().isEditMedicationClicked() &&
                !RunTimeData.getInstance().getScheduleData().isReminderAdded()) {
            addNewReminder(timingsArray[0]);
            Util.resetScheduleData();
            resetEndDate();
            resetStartDate();
        }
        return binding.getRoot();
    }

    private void initEditModeValue() {
        ArrayList<String> scheduledTime = RunTimeData.getInstance().getScheduleData().getScheduleTime();
        if (null != scheduledTime && scheduledTime.size() > 0) {
            for (int index = 0; index < scheduledTime.size(); index++) {
                if (index < maxReminders) {
                    addNewReminder(scheduledTime.get(index));
                }
            }
        } else {
            addNewReminder(timingsArray[0]);
        }
    }

    private void initValues() {
        mFrontController = FrontController.getInstance(getActivity());
        binding.setReminderTimeFragment(this);
        binding.setStartDateVisibility(View.GONE);
        binding.setEndDateVisibility(View.GONE);
        scheduleViewmodel = RunTimeData.getInstance().getScheduleViewModel(getActivity());
        customFrequencyNumber = scheduleViewmodel.getCustomFrequencyNumber();
        weeklySelectedDays = scheduleViewmodel.getWeeklySelectedDays();
        binding.setIsMonthly(null != scheduleViewmodel.getFrequencySelector().getValue() && 0 != scheduleViewmodel.getFrequencySelector().getValue()
                && 4 == scheduleViewmodel.getFrequencySelector().getValue());
        mFontRegular = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_REGULAR);
        mFontMedium = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_MEDIUM);
        mSaveScheduleListener = (SaveScheduleListener) getActivity();
        binding.setRobotoMedium(mFontMedium);
        binding.setRobotoRegular(mFontRegular);
        setObServer();
    }

    private void setObServer()
    {
        scheduleViewmodel.bulkScheduleMutableLiveData.observe(getViewLifecycleOwner(), bulkScheduleData -> {
            if(null != bulkScheduleData && bulkScheduleData.getPillIdList().size() >0 && savingFromOverlay)
            {
                LoggerUtils.info("Confirm Schedule Overlay....");
                if (!checkForStartAndEndDatePastTimeError()) {
                    saveScheduleToDB(bulkScheduleData);
                }
            }

        } );
    }

    private void initUI() {
        binding.startDate.setText(PillpopperDay.getLocalizedDateStr(PillpopperDay.today(), true, R.string._not_set, getActivity()));
        if (null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
            if (null != RunTimeData.getInstance().getScheduleData().getStartDate() &&
                    null != RunTimeData.getInstance().getScheduleData().getEndDate()) {
                binding.startDate.setText(RunTimeData.getInstance().getScheduleData().getStartDate());
                if (!RunTimeData.getInstance().getScheduleData().getStartDate().equalsIgnoreCase(PillpopperDay.getLocalizedDateStr(PillpopperDay.today(), true, R.string._not_set, getActivity()))) {
                    binding.setStartDateVisibility(View.VISIBLE);
                }
                if (!RunTimeData.getInstance().getScheduleData().getEndDate().equalsIgnoreCase("Forever")) {
                    binding.endDate.setText(RunTimeData.getInstance().getScheduleData().getEndDate());
                    binding.setEndDateVisibility(View.VISIBLE);
                } else {
                    binding.endDate.setText(PillpopperDay.getLocalizedDateString(endReminderDay, true, R.string._never, getActivity()));
                }
            }
        } else {
            addNewReminder(timingsArray[0]);
        }
        scheduleViewmodel.getStartDateSelector().postValue(Integer.parseInt(binding.startDate.getText().toString().substring(4, binding.startDate.getText().toString().indexOf(','))));
    }

    private OnTimeSetListener changeTimeSetListener = new android.app.TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            if (DateFormat.is24HourFormat(getContext())) {
                strNewTime = get24FormatTimeFromHrMin(hourOfDay, minute);
            } else {
                strNewTime = getAmPmTimeFromHrMin(hourOfDay, minute);
            }
            if (!Util.isEmptyString(selectedTime) && !selectedTime.equalsIgnoreCase(strNewTime)) {
                //before adding check for duplicates
                if (savingTime.contains(strNewTime)) {
                    showDuplicateRemindersAlert();
                } else {
                    if (mPositionClicked != -1) {
                        savingTime.remove(mPositionClicked);
                        hrMinSavingTime.remove(mPositionClicked);
                    }
                    addNewReminder(strNewTime);
                }
            }
            RunTimeData.getInstance().setScheduleEdited(true);
            checkForStartAndEndDatePastTimeError();
        }
    };

    private void addReminderLayout(String formattedReminder, int remIndex) {
        //clear all rows, add reminder time, sort reminder times, add row layouts
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.add_reminder_row, null);
        TextView reminder = view.findViewById(R.id.reminder_id);
        reminder.setTypeface(mFontRegular);
        TextView reminderTime = view.findViewById(R.id.reminder_time);
        reminder.setTypeface(mFontRegular);
        reminderTime.setTypeface(mFontMedium);
        ImageView deleteReminder = view.findViewById(R.id.ic_remove);
        RelativeLayout reminderIdTimeLayout = view.findViewById(R.id.reminder_id_time_layout);
        reminderTime.setText(formattedReminder);

        reminderIdTimeLayout.setOnClickListener(v -> {
            // show the time picker dialog with the reminder time
            if (DateFormat.is24HourFormat(getContext())) {
                getNewReminderTimeIn24Format(reminderTime.getText().toString(), 0);
            } else {
                getNewReminderTime(reminderTime.getText().toString(), 0);
            }
            TimePickerDialog.showDialog(getActivity(), getString(R.string.set_reminder), remHour, remMin, false, changeTimeSetListener, onDismissListener, PillpopperConstants.TIME_PICKER_INTERVAL);
            ViewClickHandler.preventMultiClick(reminderTime);
            mPositionClicked = remIndex;
        });

        reminder.setText(getString(R.string.reminder).concat(" " + (remIndex + 1)));
        deleteReminder.setOnClickListener(view1 -> {
            mPositionClicked = remIndex;
            RunTimeData.getInstance().setScheduleEdited(true);
            hrMinSavingTime.remove(mPositionClicked);
            refreshReminders();
        });
        if (hrMinSavingTime.size() > 1) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, reminderLayoutId);
            view.setLayoutParams(params);
        } else {
            deleteReminder.setVisibility(View.INVISIBLE);
        }
        reminderLayoutId++;
        view.setId(reminderLayoutId);
        if (null != scheduleViewmodel.getFrequencySelector().getValue() && 0 != scheduleViewmodel.getFrequencySelector().getValue()
                && 4 == scheduleViewmodel.getFrequencySelector().getValue()) {
            reminder.setText(getString(R.string.reminder));
            deleteReminder.setVisibility(View.GONE);
            binding.remindersContainerMonthly.addView(view);
        } else {
            binding.remindersContainer.addView(view);
            binding.addReminderLayout.setVisibility((hrMinSavingTime.size() < maxReminders) ? View.VISIBLE : View.GONE);
        }
        Util.hideKeyboard(getContext(), binding.addReminderLayout);
        checkForStartAndEndDatePastTimeError();
    }

    /*** Resets StartDate and set it to today's date*/
    public void resetStartDate() {
        String startDate = PillpopperDay.getLocalizedDateStr(PillpopperDay.today(), true, R.string._not_set, getActivity());
        binding.startDate.setText(startDate);
        binding.setStartDateVisibility(View.GONE);
        binding.startDateRelativeLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.date_picker_active_border));
        startReminderDay = PillpopperDay.today();
        if (null != scheduleViewmodel.getFrequencySelector().getValue() && 0 != scheduleViewmodel.getFrequencySelector().getValue()
                && 4 == scheduleViewmodel.getFrequencySelector().getValue()) {
            scheduleViewmodel.getStartDateSelector().postValue(PillpopperDay.today().getDay());
        }
        RunTimeData.getInstance().setScheduleEdited(true);
        checkForStartAndEndDatePastTimeError();
    }

    /*** Reset the EndDate and set it to Never.*/
    public void resetEndDate() {
        endReminderDay = null;
        binding.setEndDateVisibility(View.GONE);
        if (null != RunTimeData.getInstance().getScheduleData()) {
            RunTimeData.getInstance().getScheduleData().setEndDate(null);
        }
        binding.endDateRelativeLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.date_picker_active_border));
        binding.endDate.setText(PillpopperDay.getLocalizedDateString(endReminderDay, true, R.string._never, getActivity()));
        RunTimeData.getInstance().setScheduleEdited(true);
        checkForStartAndEndDatePastTimeError();
    }

    public void addNewReminder(String newTime) {
        savingTime.add(newTime);
        String hrMinFormat = "" + giveBackFormattedHourMinute(newTime);
        hrMinFormat = hrMinFormat.replace(":", "");
        hrMinSavingTime.add(Integer.parseInt(hrMinFormat));
        if (hrMinSavingTime.size() > 1) {
            Collections.sort(hrMinSavingTime);
        }
        refreshReminders();
    }

    public void addReminderClick() {
        if (hrMinSavingTime.size() < maxReminders) {
            TextView lastReminderTimeView = binding.remindersContainer.findViewById(reminderLayoutId).findViewById(R.id.reminder_time);
            String lastReminderTime = lastReminderTimeView.getText().toString();
            // 5 hrs
            int timeToIncrease = 300;
            ViewClickHandler.preventMultiClick(binding.addReminderLayout);
            if (DateFormat.is24HourFormat(getContext())) {
                getNewReminderTimeIn24Format(lastReminderTime, timeToIncrease);
                strNewTime = get24FormatTimeFromHrMin(remHour, remMin);
            } else {
                getNewReminderTime(lastReminderTime, timeToIncrease);
                strNewTime = getAmPmTimeFromHrMin(remHour, remMin);
            }

            if (savingTime.contains(strNewTime)) {
                showDuplicateRemindersAlert();
            } else {
                addNewReminder(strNewTime);
            }
        }
        if (hrMinSavingTime.size() == maxReminders) {
            binding.addReminderLayout.setVisibility(View.GONE);
        }
        RunTimeData.getInstance().setScheduleEdited(true);
        Util.hideKeyboard(getContext(), binding.addReminderLayout);
    }

    public void onStartDateSelected(int btnType) {
        String startDate;
        switch (btnType) {
            case 0:
                startDate = PillpopperDay.getLocalizedDateStr(getMeDate(datePicker.getDatePicker().getYear(),
                        datePicker.getDatePicker().getMonth(), datePicker.getDatePicker().getDayOfMonth()), true, R.string._not_set, getActivity());
                binding.startDate.setText(startDate);
                DatePicker picker = datePicker.getDatePicker();
                startReminderDay = new PillpopperDay(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                if (startReminderDay.equals(PillpopperDay.today())) {
                    binding.setStartDateVisibility(View.GONE);
                    binding.startDateRelativeLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.date_picker_active_border));
                } else {
                    binding.setStartDateVisibility(View.VISIBLE);
                    binding.clearStartDate.setContentDescription(getActivity().getResources().getString(R.string.content_description_reset_values));
                    binding.startDateRelativeLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.schedule_background_style));
                }
                if (null != scheduleViewmodel.getFrequencySelector().getValue() && 0 != scheduleViewmodel.getFrequencySelector().getValue()
                        && 4 == scheduleViewmodel.getFrequencySelector().getValue()) {
                    scheduleViewmodel.getStartDateSelector().postValue(datePicker.getDatePicker().getDayOfMonth());
                }
                RunTimeData.getInstance().setScheduleEdited(true);
                break;
            case 1:
                break;
            case 2:
                binding.setStartDateVisibility(View.GONE);
                startDate = PillpopperDay.getLocalizedDateStr(PillpopperDay.today(), true, R.string._not_set, getActivity());
                binding.startDate.setText(startDate);
                startReminderDay = PillpopperDay.today();
                if (null != scheduleViewmodel.getFrequencySelector().getValue() && 0 != scheduleViewmodel.getFrequencySelector().getValue()
                        && 4 == scheduleViewmodel.getFrequencySelector().getValue()) {
                    scheduleViewmodel.getStartDateSelector().postValue(startReminderDay.getDay());
                }
                RunTimeData.getInstance().setScheduleEdited(true);
                break;
        }
        checkForStartAndEndDatePastTimeError();
    }

    public void showDatepicker(int from) {
        String neuralText;
        Util.hideKeyboard(getContext(), binding.addReminderLayout);
        if (START_DATE == from) {
            neuralText = getResources().getString(R.string.set_today);
        } else {
            neuralText = getResources().getString(R.string.set_never);
        }
        final DatePicker Picker = new DatePicker(getActivity());
        Picker.setCalendarViewShown(false);

        datePicker = new DatePickerDialog(getActivity(), R.style.datepicker, null, 0, 0, 0);
        datePicker.getDatePicker().setMinDate(PillpopperTime.now().getGmtMilliseconds());
        datePicker.setCancelable(false);
        datePicker.setCanceledOnTouchOutside(false);
        datePicker.getDatePicker().setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        datePicker.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string._set),
                (dialog, which) -> {
                    Picker.setFocusable(false);
                    Picker.setFocusableInTouchMode(false);
                    if (START_DATE == from) {
                        onStartDateSelected(0);
                    } else {
                        onEndDaySelected(0);
                    }
                    checkForStartAndEndDatePastTimeError();
                });
        datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string._cancel),
                (dialog, which) -> {
                    if (START_DATE == from) {
                        onStartDateSelected(1);
                    } else {
                        onEndDaySelected(1);
                    }
                });
        datePicker.setButton(DialogInterface.BUTTON_NEUTRAL, neuralText, (dialog, which) -> {
            Picker.setFocusable(false);
            Picker.setFocusableInTouchMode(false);
            if (START_DATE == from) {
                onStartDateSelected(2);
            } else {
                if (null != RunTimeData.getInstance().getScheduleData()) {
                    RunTimeData.getInstance().getScheduleData().setEndDate(null);
                }
                onEndDaySelected(2);
            }
        });
        datePicker.show();
        //setting date picker btns padding to 0 align it properly
        datePicker.getButton(Dialog.BUTTON_POSITIVE).setPadding(0, 0, 0, 0);
        datePicker.getButton(Dialog.BUTTON_NEGATIVE).setPadding(0, 0, 0, 0);
        datePicker.getButton(Dialog.BUTTON_NEUTRAL).setPadding(0, 0, 0, 0);
    }

    public void getNewReminderTime(String strStartTime, int iMinute) {
        selectedTime = strStartTime;
        String[] hm = strStartTime.split(":");
        int hour = Integer.parseInt(hm[0]);
        String strMinWPMAM = hm[1];
        String[] minAMPM = strMinWPMAM.split(" ");
        int min = Integer.parseInt(minAMPM[0]);
        String strPMAM = minAMPM[1];

        if (strPMAM.equalsIgnoreCase(Util.getSystemAMFormat())) {
            if (hour == 12) {
                hour = 0;
            }
        } else if (strPMAM.equalsIgnoreCase(Util.getSystemPMFormat()) && hour != 12) {//pm
            hour = hour + 12;
        }

        int totalTime = hour * 60 + min;
        totalTime += iMinute;

        remHour = (totalTime / 60) % 24;
        remMin = totalTime % 60;
    }

    public void getNewReminderTimeIn24Format(String strStartTime, int iMinute) {
        selectedTime = strStartTime;
        String[] hm = strStartTime.split(":");
        int hour = Integer.parseInt(hm[0]);
        int min = Integer.parseInt(hm[1]);

        int totalTime = hour * 60 + min;
        totalTime += iMinute;

        remHour = (totalTime / 60) % 24;
        remMin = totalTime % 60;
    }

    private void refreshReminders() {
        binding.remindersContainer.removeAllViews();
        binding.remindersContainerMonthly.removeAllViews();
        reminderLayoutId = reminderLayoutIdBase;
        savingTime.clear();
        for (int index = 0; index < hrMinSavingTime.size(); index++) {
            String sortedReminder = null;
            if (DateFormat.is24HourFormat(getActivity())) {
                sortedReminder = convertTimeTo24HrFormat(hrMinSavingTime.get(index));
            } else {
                sortedReminder = convertTimeTo12HrFormat(hrMinSavingTime.get(index));
            }
            if (null != sortedReminder) {
                savingTime.add(sortedReminder);
                addReminderLayout(sortedReminder, index);
            }
        }
    }


    public HourMinute giveBackFormattedHourMinute(String scheduletime) {
        String timeScheduled = scheduletime;
        if (scheduletime.contains("am") || scheduletime.contains("AM")
                || scheduletime.contains("pm") || scheduletime.contains("PM")) {
            StringBuilder sb = new StringBuilder();
            sb.append(scheduletime.substring(0, scheduletime.length() - 2));
            scheduletime = sb.toString();
        } else if (scheduletime.contains("a.m.") || scheduletime.contains("A.M.") || scheduletime.contains("p.m.") || scheduletime.contains("P.M.")) {
            StringBuilder sb = new StringBuilder();
            sb.append(scheduletime.substring(0, scheduletime.length() - 4));
            scheduletime = sb.toString();
        }
        int hour = 0;
        int minute = 0;

        if (scheduletime.contains(":")) {
            String[] time = scheduletime.split(":");
            try {
                if (null != time[0].trim()) {
                    hour = Integer.parseInt(time[0].trim());
                }
                if (null != time[1].trim()) {
                    minute = Integer.parseInt(time[1].trim());
                }
            } catch (Exception ne) {
                PillpopperLog.exception(ne.getMessage());
            }
        }

        if (timeScheduled.contains("PM") || timeScheduled.contains("pm") || timeScheduled.contains("P.M.") || timeScheduled.contains("p.m.")) {
            hour = hour == 12 ? 12 : hour + 12;
        } else {
            if (!DateFormat.is24HourFormat(getActivity())) {
                if (hour == 12) {
                    hour = 0;
                }
            } else {
                if (hour == 12 && (timeScheduled.contains("am") || timeScheduled.contains("AM") || timeScheduled.contains("a.m.") || timeScheduled.contains("A.M."))) {
                    hour = 0;
                }
            }
        }
        return getSelectedHourMinute(hour, minute);
    }

    public HourMinute getSelectedHourMinute(int hour, int min) {
        if (hour < 0 || min < 0) {
            PillpopperLog.say("Selected hourminute: none");
            return null;
        } else {
            return new HourMinute(hour, min, 0);
        }
    }

    private void onEndDaySelected(int btnType) {
        switch (btnType) {
            case 0:
                binding.setEndDateVisibility(View.VISIBLE);
                binding.clearEndDate.setContentDescription(getActivity().getResources().getString(R.string.content_description_reset_values));
                binding.endDateRelativeLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.schedule_background_style));
                binding.endDate.setText("");
                String endDate = PillpopperDay.getLocalizedDateStr(getMeDate(datePicker.getDatePicker().getYear(),
                        datePicker.getDatePicker().getMonth(), datePicker.getDatePicker().getDayOfMonth()), true, R.string._not_set, getActivity());
                binding.endDate.setText(endDate);
                DatePicker picker = datePicker.getDatePicker();
                endReminderDay = new PillpopperDay(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                RunTimeData.getInstance().setScheduleEdited(true);
                break;
            case 1:
                break;
            case 2:
                binding.setEndDateVisibility(View.GONE);
                binding.endDateRelativeLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.date_picker_active_border));
                endReminderDay = null;
                binding.endDate.setText(PillpopperDay.getLocalizedDateString(endReminderDay, true, R.string._never, getActivity()));
                RunTimeData.getInstance().setScheduleEdited(true);
                break;
            default:
                break;
        }
        checkForStartAndEndDatePastTimeError();
    }

    public void showDuplicateRemindersAlert() {
        DialogHelpers.showAlertDialogWithOkButton(getActivity(), getString(R.string.unable_to_schedule_title), getString(R.string.unable_to_schedule_message));
    }

    private final DialogInterface.OnDismissListener onDismissListener = DialogInterface::dismiss;


    private PillpopperDay getMeDate(int year, int month, int day) {
        if (year < 0 || month < 0 || day < 0) {
            return null;
        } else {
            return new PillpopperDay(year, month, day);
        }
    }

    private PillpopperDay getFormattedTime(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        try {
            Date endDate = simpleDateFormat.parse(date);
            return new PillpopperDay(Integer.parseInt((String) DateFormat.format("yyyy", endDate)), Integer.parseInt((String) DateFormat.format("MM", endDate)), Integer.parseInt((String) DateFormat.format("dd", endDate)));
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
            return null;
        }
    }

    //prepare a bulk schedule model and save the schedule to DB
    public void saveSchedule(View view) {

       /* if(null != view){
           RunTimeData.getInstance().setUserSelected(false);
            RunTimeData.getInstance().setScheduleEdited(false);
        }*/
        // to prevent multiclick on save schedule button
        if (null != view) {
            MultiClickViewPreventHandler.preventMultiClick(view);
        }

        Util.hideKeyboard(getContext(), binding.saveScheduleBtn);
        bulkSchedule = new BulkSchedule();
        PillpopperDay startDate = startReminderDay;
        PillpopperDay endDate = endReminderDay;

        if (null != RunTimeData.getInstance().getScheduleData() && !RunTimeData.getInstance().getScheduleData().getMeditationDuration().equalsIgnoreCase("Set Reminder")) {
            try {
                if (null == startDate) {
                    startDate = getFormattedTime(RunTimeData.getInstance().getScheduleData().getStartDate());
                    if (null != startDate) {
                        startReminderDay = new PillpopperDay(startDate.getYear(), startDate.getMonth() - 1, startDate.getDay());
                        startDate = startReminderDay;
                    }
                }
                if (null == endDate && null != RunTimeData.getInstance().getScheduleData().getEndDate()) {
                    endDate = getFormattedTime(RunTimeData.getInstance().getScheduleData().getEndDate());
                    if(null != endDate) {
                        endReminderDay = new PillpopperDay(endDate.getYear(), endDate.getMonth() - 1, endDate.getDay());
                        endDate = endReminderDay;
                    }
                }
            } catch (Exception ne) {
                LoggerUtils.info(ne.getMessage());
            }
        }
        if ((endDate != null && startDate == null && endDate.before(PillpopperDay.today())) ||
                (null != endDate && null != startDate && endDate.before(startDate))) {
            DialogHelpers.showAlertDialog(getActivity(), String.format(getString(R.string.end_must_be_after_start), binding.startDate.getText().toString()));
            // Only in the case when the start date is past the end date
            RunTimeData.getInstance().setScheduleEdited(true);
            RunTimeData.getInstance().setSaveButtonEnabled(true);
            RunTimeData.getInstance().setBulkMedsScheduleSaved(false); // used only in bulk schedule flow
        } else if (!checkForStartAndEndDatePastTimeError()) {
            if (null != scheduleViewmodel.getMedicationsList() && null != scheduleViewmodel.getMedicationsList().getValue()
                    && !scheduleViewmodel.getMedicationsList().getValue().isEmpty()) {
                List<String> pillIdList = new ArrayList<>();
                //Collecting all the drug ids
                for (Drug drug : scheduleViewmodel.getMedicationsList().getValue()) {
                    pillIdList.add(drug.getGuid());
                    if (Util.isEmptyString(bulkSchedule.getUserId())) {
                        bulkSchedule.setUserId(drug.getUserID());
                    }
                }
                bulkSchedule.setPillIdList(pillIdList);
                //startReminderDay will be null until user clicks on the start date card
                if (startReminderDay == null) {
                    bulkSchedule.setScheduledStartDate(Long.toString(PillpopperDay.today().atLocalTime(new HourMinute(0, 0)).getGmtSeconds()));
                } else {
                    bulkSchedule.setScheduledStartDate(Long.toString(startReminderDay.atLocalTime(new HourMinute(0, 0)).getGmtSeconds()));
                }
                //endReminderDay will be null or set to never for the first time
                if (endReminderDay == null || ("never").equalsIgnoreCase(binding.endText.getText().toString())) {
                    bulkSchedule.setScheduledEndDate(Long.toString(-1));
                } else {
                    bulkSchedule.setScheduledEndDate(Long.toString(endReminderDay.atLocalTime(new HourMinute(0, 0)).getGmtSeconds()));
                }
                bulkSchedule.setScheduledTimeList(hrMinSavingTime);

                //Collecting the Frequency selected
                if (null != scheduleViewmodel.getFrequencySelector() &&
                        null != scheduleViewmodel.getFrequencySelector().getValue()) {
                    bulkSchedule.setScheduledFrequency(getFrequency(scheduleViewmodel.getFrequencySelector().getValue()));
                    // day period and scheduled frequency for custom
                    final int weeklyDayPeriod = 7;
                    if (scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.CUSTOM) {
                        if (scheduleViewmodel.getCustomFrequency().equalsIgnoreCase(days)) {
                            bulkSchedule.setDayPeriod(scheduleViewmodel.getCustomFrequencyNumber().getValue());
                        } else if (null != scheduleViewmodel.getCustomFrequencyNumber()
                                && null != scheduleViewmodel.getCustomFrequencyNumber().getValue() &&
                                weeks.equalsIgnoreCase(scheduleViewmodel.getCustomFrequency())) {
                            bulkSchedule.setDayPeriod(String.valueOf(weeklyDayPeriod * Integer.parseInt(scheduleViewmodel.getCustomFrequencyNumber().getValue())));
                            if (scheduleViewmodel.getCustomFrequencyNumber().getValue().equalsIgnoreCase("1")) {
                                bulkSchedule.setDaysSelectedForWeekly(getDayNumber(null != startDate ? startDate.getDayName().toString() : PillpopperDay.today().getDayName().toString()));
                            }
                        }
                    } else if (scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.DAILY) {
                        int dayPeriod = 1;
                        bulkSchedule.setDayPeriod(String.valueOf(dayPeriod));
                    } else if (scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.WEEKLY) {
                        bulkSchedule.setDayPeriod(String.valueOf(weeklyDayPeriod));
                        StringBuilder daysSelectedForWeekly = new StringBuilder();
                        for (int i = 0; i < scheduleViewmodel.getWeeklySelectedDays().getValue().size(); i++) {
                            String selectedDay = scheduleViewmodel.getWeeklySelectedDays().getValue().get(i);
                            daysSelectedForWeekly.append(selectedDay);
                            if (i < scheduleViewmodel.getWeeklySelectedDays().getValue().size() - 1) {
                                daysSelectedForWeekly.append(",");
                            }
                        }
                        bulkSchedule.setDaysSelectedForWeekly(daysSelectedForWeekly.toString());
                    } else if (scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.MONTHLY) {
                        bulkSchedule.setDayPeriod(String.valueOf(30));
                    }
                }
                //DB operations and log entries

                if (AppConstants.BULK_MEDICATION.equalsIgnoreCase(RunTimeData.getInstance().getLaunchSource())) {
                    saveScheduleToDB(bulkSchedule);
                } else {
                    savingFromOverlay = true;
                    //MedicationOverlayConfirmationDialogFragment medicationOverlayConfirmationDialogFragment = new MedicationOverlayConfirmationDialogFragment(bulkSchedule);
                    //medicationOverlayConfirmationDialogFragment.show(getActivity().getSupportFragmentManager(), "reminder_fragment");
                    Intent intent =new Intent(requireContext(), MedicationOverlayConfirmationScheduleActivity.class);
                    intent.putExtra("BULK_SCHEDULE",bulkSchedule);
                    startActivity(intent);
                    RunTimeData.getInstance().setScheduleEdited(true);
                }
            }
        }
    }

    private void saveScheduleToDB(BulkSchedule bulkSchedule)
    {
        removePastRemindersForSelectedDrug(bulkSchedule.getPillIdList());
        for (String pillId : bulkSchedule.getPillIdList()) {
            Drug drug = FrontController.getInstance(getActivity()).getDrugByPillId(pillId);
            bulkSchedule.setScheduleGUID(Util.getRandomGuid());
            List<HistoryEvent> latestPostponeEvents = FrontController.getInstance(getActivity()).getActivePostponedEvents(drug.getGuid());
            if(null != latestPostponeEvents && !latestPostponeEvents.isEmpty()){
                for(HistoryEvent event : latestPostponeEvents){
                    Util.editPostponeEvent(drug, event,getActivity());
                }
            }
            FrontController.getInstance(getActivity()).updatePostponeHistoryAvailable(drug);
        }
        if (0 != mFrontController.updateSchedule(bulkSchedule, bulkSchedule.getPillIdList(), getPillTimeList())) {
            FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(getActivity(),
                    AppConstants.BULK_MEDICATION.equalsIgnoreCase(RunTimeData.getInstance().getLaunchSource())
                            ? FireBaseConstants.Event.BULK_REMINDER_SAVE
                            : FireBaseConstants.Event.MED_REMINDER_SAVE);
            addLogEntries(bulkSchedule.getPillIdList());
            for (Drug drug : scheduleViewmodel.getMedicationsList().getValue()) {
                if (RunTimeData.getInstance().getDrugsNLPRemindersList().containsKey(drug.getGuid())) {
                    prepareAndSendSigValidationData(drug,bulkSchedule);
                }
            }
            weeklySelectedDays.postValue(new ArrayList<>());
            showScheduleSavedAlertDialog();
        }
    }

    private void prepareAndSendSigValidationData(Drug drug,BulkSchedule scheduleForDrug) {
        NLPSigValidationRequestObj requestObject = new NLPSigValidationRequestObj();
        NLPReminder nlpAPIReminder = RunTimeData.getInstance().getDrugsNLPRemindersList().get(drug.getGuid());
        requestObject.setPillId(drug.getGuid());

        NLPReminder userChoiceReminder = new NLPReminder();
        userChoiceReminder.setSigId(nlpAPIReminder.getSigId());
        userChoiceReminder.setDosage(drug.getDose());
        userChoiceReminder.setMedicine(drug.getName());
        userChoiceReminder.setFrequency(NLPUtils.getScheduledFrequency(scheduleForDrug.getScheduledFrequency()));
        userChoiceReminder.setStartDate(NLPUtils.getNLPFormattedDate(scheduleForDrug.getScheduledStartDate()));
        userChoiceReminder.setEndDate(NLPUtils.getNLPFormattedDate(scheduleForDrug.getScheduledEndDate()));
        userChoiceReminder.setReminderTimes(NLPUtils.getFormattedReminderTimes(scheduleForDrug.getScheduledTimeList()));
        userChoiceReminder.setEvery(NLPUtils.getScheduledEvery(getActivity(), drug, scheduleForDrug.getScheduledFrequency(), Long.parseLong(scheduleForDrug.getDayPeriod()), scheduleForDrug.getDaysSelectedForWeekly()));
        boolean isResponseMatched = NLPUtils.isResponseMatched(nlpAPIReminder, userChoiceReminder);
        requestObject.setChangeInSchedule(!isResponseMatched ? "Yes" : "No");
        requestObject.setMobileResponse(NLPUtils.prepareMobileResponse(userChoiceReminder));
        LoggerUtils.info("NLP : " + requestObject.getMobileResponse());
        new NLPScheduleValidationService(getActivity(), requestObject).execute();
    }

    private void showScheduleSavedAlertDialog() {

        if (getActivity() == null) {
            return;
        }
        //resetting the flags on saving a schedule for DE22491
        RunTimeData.getInstance().setSaveButtonEnabled(false);
        RunTimeData.getInstance().setScheduleEdited(false);
        AlertDialog.Builder savedDialogBuilder = new AlertDialog.Builder(getActivity());
        View layoutView = getLayoutInflater().inflate(R.layout.save_schedule_alert, null);
        savedDialogBuilder.setView(layoutView);

        delayHandler = new Handler();
        dismissDialogRunnable = () -> {
            if (null != alertScheduleSaveDialog && alertScheduleSaveDialog.isShowing()) {
                alertScheduleSaveDialog.dismiss();
            }
        };

        navigateHandler = new Handler();
        try {
            if (!RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
                navigateRunnable = () -> mSaveScheduleListener.onSaveScheduleClicked();
            }
        } catch (NullPointerException ne) {
            navigateRunnable = () -> mSaveScheduleListener.onSaveScheduleClicked();
            LoggerUtils.exception(ne.getMessage());
        }
        savedDialogBuilder.setOnDismissListener(dialogInterface ->
        {
            delayHandler.removeCallbacks(dismissDialogRunnable);
            navigateHandler.removeCallbacks(dismissDialogRunnable);
            if (!RunTimeData.getInstance().isRestored() && null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
                RunTimeData.getInstance().getScheduleData().setIsFromScheduleMed(true);
                RunTimeData.getInstance().getScheduleData().setMedSavedClicked(true);
                RunTimeData.getInstance().getScheduleData().setEditMedicationClicked(true);
                RunTimeData.getInstance().setBulkMedsScheduleSaved(true);
                mSaveScheduleListener.onSaveScheduleClicked();
                return;
            }
            mSaveScheduleListener.onSaveScheduleClicked();
        });

        alertScheduleSaveDialog = savedDialogBuilder.create();
        alertScheduleSaveDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertScheduleSaveDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        RunTimeData.getInstance().setAlertDialogInstance(alertScheduleSaveDialog);
        alertScheduleSaveDialog.show();
        delayHandler.postDelayed(dismissDialogRunnable, 3000);
        navigateHandler.postDelayed(navigateRunnable, 3500);
    }

    private String getDayNumber(String toString) {
        switch (toString) {
            case "Sunday":
                return "1";
            case "Monday":
                return "2";
            case "Tuesday":
                return "3";
            case "Wednesday":
                return "4";
            case "Thursday":
                return "5";
            case "Friday":
                return "6";
            case "Saturday":
                return "7";
            default:
                return "";
        }
    }

    private String getFrequency(int frequency) {
        String dayFreqValue = "D";
        String weekFreqValue = "W";
        String monthFreqValue = "M";
        if (ScheduleWizardFragment.DAILY == frequency) {
            return dayFreqValue; //daily
        } else if (ScheduleWizardFragment.WEEKLY == frequency) {
            return weekFreqValue; //Weekly
        } else if (ScheduleWizardFragment.CUSTOM == frequency) {
            if (scheduleViewmodel.getCustomFrequency().equalsIgnoreCase(days)) {
                return dayFreqValue;
            } else if (scheduleViewmodel.getCustomFrequency().equalsIgnoreCase(weeks)) {
                return weekFreqValue;
            }
        } else if (ScheduleWizardFragment.MONTHLY == frequency) {
            return monthFreqValue; //Monthly
        }
        return "";
    }

    private void removePastRemindersForSelectedDrug(List<String> drugIds) {
        for (String drugId : drugIds) {
            mFrontController.removePillFromPassedReminderTable(getActivity(), drugId);
        }
        try {
            LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersHashMapByUserId = PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId();
            passedRemindersHashMapByUserId.remove(bulkSchedule.getUserId());
            PillpopperRunTime.getInstance().setPassedReminderersHashMapByUserId(passedRemindersHashMapByUserId);
        } catch (Exception e) {
            PillpopperLog.say(e);
        }
    }

    private List<String> getPillTimeList() {
        ArrayList<String> pillTimes = new ArrayList<>();
        for (int i = 0; i < (binding.getIsMonthly() ? 1 : binding.remindersContainer.getChildCount()); i++) {
            RelativeLayout eachRow = binding.getIsMonthly() ? (RelativeLayout) binding.remindersContainerMonthly.getChildAt(i) : (RelativeLayout) binding.remindersContainer.getChildAt(i);
            for (int j = 0; j < eachRow.getChildCount(); j++) {
                View singleView = eachRow.getChildAt(j);
                RelativeLayout dateTextContainer = null;
                if (singleView instanceof RelativeLayout) {
                    dateTextContainer = (RelativeLayout) singleView;
                    TextView dateText = (dateTextContainer.findViewById(R.id.reminder_time));
                    String scheduleTime = null;
                    if ((dateText.getText().length() > 0)) {
                        scheduleTime = dateText.getText().toString();
                        if (!DateFormat.is24HourFormat(getContext())) {
                            if (AppConstants.MID_DAY.equalsIgnoreCase(scheduleTime)) {
                                scheduleTime = AppConstants.MID_NIGHT + " " + Util.getSystemAMFormat();
                            }
                        }
                        String scheduleTime24Hr = "" + giveBackFormattedHourMinute(scheduleTime);
                        scheduleTime24Hr = scheduleTime24Hr.replace(":", "");
                        if (!pillTimes.contains(Util.convertHHMMtoTimeFormat(scheduleTime24Hr))) {
                            pillTimes.add(Util.convertHHMMtoTimeFormat(scheduleTime24Hr));
                        }
                    }
                }
            }
        }
        return pillTimes;
    }

    private void addLogEntries(List<String> drugIds) {
        for (String pillId : drugIds) {
            if (!("").equalsIgnoreCase(pillId)) {
                Drug drug = mFrontController.getDrugByPillId(pillId);
                drug.setScheduleAddedOrUpdated(true);
                if (null != scheduleViewmodel && null != scheduleViewmodel.getFrequencySelector() &&
                        null != scheduleViewmodel.getFrequencySelector().getValue()) {
                    drug.setScheduledFrequency(getFrequency(scheduleViewmodel.getFrequencySelector().getValue()));
                }
                drug.getPreferences().setPreference("scheduleChoice", AppConstants.SCHEDULE_CHOICE_SCHEDULED);
                try {
                    drug.setScheduleGuid(Util.getRandomGuid());
                    mFrontController.updateScheduleGUID(drug.getScheduleGuid(), drug.getGuid());
                    mFrontController.addLogEntry(getActivity(), Util.prepareLogEntryForAction("EditPill", drug, getActivity()));
                } catch (Exception e) {
                    PillpopperLog.say("Oops Exception while adding log entry", e);
                }
            }
        }
    }

    @Override
    public void onPause() {
        if (null != datePicker && datePicker.isShowing()) {
            datePicker.dismiss();
        }
        if (null != alertScheduleSaveDialog && alertScheduleSaveDialog.isShowing()) {
            delayHandler.removeCallbacks(dismissDialogRunnable);
            navigateHandler.removeCallbacks(navigateRunnable);
            scheduleSaved = true;
            alertScheduleSaveDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (scheduleSaved && null != alertScheduleSaveDialog) {
            alertScheduleSaveDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != scheduleViewmodel.getFrequencySelector().getValue() && scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.CUSTOM) {
            enableOrDisableSaveScheduleButton(false);
            setCustomFrequencyObserver();
        } else if (null != scheduleViewmodel.getFrequencySelector().getValue() && scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.WEEKLY) {
            enableOrDisableSaveScheduleButton(false);
            setWeeklyFrequencyObserver();
        } else if (!checkForStartAndEndDatePastTimeError()) {
            enableOrDisableSaveScheduleButton(null != scheduleViewmodel.getFrequencySelector().getValue() && scheduleViewmodel.getFrequencySelector().getValue() != 0);
        }

        //for bulk reminders save and discard alerts
        if (binding.saveScheduleBtn.isEnabled()) {
            RunTimeData.getInstance().setSaveButtonEnabled(true);
        }
        RunTimeData.getInstance().setBulkMedsScheduleSaved(false);
        refreshReminders();
    }

    private void setWeeklyFrequencyObserver() {
        weeklySelectedDays.observe(this, selectedDays -> {
            //Again doing frequency check because the observer is getting called even after changing the frequency to daily or monthly
            if (selectedDays.isEmpty() && null != scheduleViewmodel.getFrequencySelector().getValue() && scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.WEEKLY) {
                enableOrDisableSaveScheduleButton(false);
            } else {
                checkForStartAndEndDatePastTimeError();
            }
        });
    }

    private void setCustomFrequencyObserver() {
        customFrequencyNumber.observe(this, str -> {
            //Again doing frequency check because the observer is getting called even after changing the frequency to daily or monthly
            if (Util.isEmptyString(str) && null != scheduleViewmodel.getFrequencySelector().getValue() && scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.CUSTOM) {
                enableOrDisableSaveScheduleButton(false);
            } else {
                checkForStartAndEndDatePastTimeError();
            }
        });
    }

    private void enableOrDisableSaveScheduleButton(boolean enable) {
        binding.saveScheduleBtn.setBackground(Util.getDrawableWrapper(getActivity(),
                enable ? R.drawable.button_kphc_schedule : R.drawable.button_bg_disabled));
        binding.saveScheduleBtn.setTextColor(ContextCompat.getColor(getActivity(), enable ? R.color.white : R.color.kp_theme_blue));
        binding.saveScheduleBtn.setEnabled(enable);
        RunTimeData.getInstance().setSaveButtonEnabled(enable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mSaveScheduleListener.onSaveScheduleClicked();
        }
    }

    private boolean checkForStartAndEndDatePastTimeError() {
        boolean isPastTime = false;
        String today = PillpopperDay.getLocalizedDateStr(PillpopperDay.today(), true, R.string._not_set, getActivity());
        PillpopperDay endDay = getFormattedTime(binding.endDate.getText().toString());
        PillpopperDay currentDay = getFormattedTime(today);
        if (endDay != null && currentDay != null) {
            if (!(binding.endDate.getText().toString().equalsIgnoreCase("Never")) && endDay.before(currentDay)) {
                isPastTime = true;
                enableOrDisableSaveScheduleButton(false);
            }
        }

        if (binding.startDate.getText().toString().equalsIgnoreCase(binding.endDate.getText().toString())
                && binding.startDate.getText().toString().equalsIgnoreCase(today)) {
            try {
                int hour = PillpopperTime.now().getLocalHourMinute().getHour();
                int min = PillpopperTime.now().getLocalHourMinute().getMinute();
                String currentHourMinute = getAmPmTimeFromHrMin(hour, min);
                String hrMinFormat = "" + giveBackFormattedHourMinute(currentHourMinute);
                hrMinFormat = hrMinFormat.replace(":", "");
                for (int hourMinute : hrMinSavingTime) {
                    if (hourMinute <= Integer.parseInt(hrMinFormat)) {
                        isPastTime = true;
                        break;
                    }
                }
            } catch (Exception ex) {
                LoggerUtils.info(ex.getMessage());
            }
            if (isPastTime) {
                enableOrDisableSaveScheduleButton(false);
            }
        }
        if (!isPastTime) {
            if (null != scheduleViewmodel.getFrequencySelector().getValue() && scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.CUSTOM) {
                enableOrDisableSaveScheduleButton(null != customFrequencyNumber && null != customFrequencyNumber.getValue() && !customFrequencyNumber.getValue().isEmpty());
            } else if (null != scheduleViewmodel.getFrequencySelector().getValue() && scheduleViewmodel.getFrequencySelector().getValue() == ScheduleWizardFragment.WEEKLY) {
                enableOrDisableSaveScheduleButton(null != weeklySelectedDays && null != weeklySelectedDays.getValue() && !weeklySelectedDays.getValue().isEmpty());
            } else {
                enableOrDisableSaveScheduleButton(null != scheduleViewmodel.getFrequencySelector().getValue() && scheduleViewmodel.getFrequencySelector().getValue() != 0);
            }
        }
        return isPastTime;
    }

    @Override
    public void onDestroyView() {
        try {
            customFrequencyNumber.removeObserver((Observer<? super String>) this);
            weeklySelectedDays.removeObserver((Observer<? super ArrayList<String>>) this);
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
        }
        super.onDestroyView();
    }
}
