package com.montunosoftware.pillpopper.android.refillreminder.views;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderAlertDialog;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderUtils;
import com.montunosoftware.pillpopper.android.refillreminder.RequestWrapper;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderNotificationUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.ViewClickHandler;
import com.montunosoftware.pillpopper.android.view.TimePickerDialog;
import com.montunosoftware.pillpopper.controller.FrontController;

import org.kp.tpmg.mykpmeds.activation.RefillReminderInterface;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by M1024581 on 2/19/2018.
 */

public class CreateOrUpdateRefillReminderFragment extends Fragment implements View.OnClickListener {

    private RefillReminderInterface refillReminderInterface;


    private TextView mRefillDate;
    private TextView mRefillTime;
    private TextView mRefillEndDateTxt;
    private TextView mRefillRepeatTitle;
    private TextView mRefillRepeatFrequencyText;
    private TextView mRefillNextReminderHeader;
    private TextView mEmptyText;
    private LinearLayout mRefillRepeatFrequencyThirty;
    private LinearLayout mRefillRepeatFrequencySixty;
    private LinearLayout mRefillRepeatFrequencyNinty;

    private EditText mRemindText;
    private SwitchCompat mRepeatSwitch;
    private RelativeLayout mEndDateLayout;
    private RelativeLayout mRepeatLayout;
    private RelativeLayout mRepeatFrequencyLayout;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mRefillHour;
    private int mRefillMin;

    private String mTime;
    private String changedTime;
    private String refillTime;

    private Calendar mRefillMinDate;
    private Calendar mRefillEndDate;
    private String mNotesErrorMessage;
    private Context mContext;

    private int newDailyLimit = 1;
    private NumberPicker numberPicker;
    private Button mRefillCustomFrequency;

    private RefillReminder mSelectedListItemRefill;
    private DatePickerDialog startDatePickerDialog;
    private DatePickerDialog endDatePickerDialog;
    private AlertDialog numberPickerAlertDialog;
    private TextView mHintText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        refillReminderInterface = (RefillRemindersHomeContainerActivity) mContext;
        if (null != getArguments() && getArguments().size() > 0) {
            mSelectedListItemRefill = (RefillReminder) getArguments().getSerializable("selectedRefillReminder");
        }
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_REFILL_SETUP);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_update_refill_reminder_layout, container, false);
        setHasOptionsMenu(true);
        initUI(view);
        if(null != mSelectedListItemRefill){
            loadSelectedRefillRemindersData();
        }
        return view;
    }

    private void loadSelectedRefillRemindersData() {
        mRemindText.setText(mSelectedListItemRefill.getReminderNote());

        Calendar nextRefillDate = Calendar.getInstance();
        nextRefillDate.setTimeInMillis(Long.parseLong(mSelectedListItemRefill.getNextReminderDate()) * 1000);
        mRefillDate.setText(getDate(nextRefillDate.getTime()));
        mRefillDate.setContentDescription(getDate(nextRefillDate.getTime())+getString(R.string.double_tap_to_edit));
        mRefillTime.setText(getTime(nextRefillDate.getTime()));
        mRefillTime.setContentDescription(getTime(nextRefillDate.getTime())+getString(R.string.double_tap_to_edit));
        mRepeatSwitch.setChecked(mSelectedListItemRefill.isRecurring());
        if(mRepeatSwitch.isChecked()){
            mRepeatFrequencyLayout.setVisibility(View.GONE);
            mEndDateLayout.setVisibility(View.VISIBLE);
            if(!RefillReminderUtils.isEmptyString(mSelectedListItemRefill.getReminderEndDate()) && !mSelectedListItemRefill.getReminderEndDate().equals("-1")) {
                Date refillEndDate = new Date();
                refillEndDate.setTime(Long.parseLong(mSelectedListItemRefill.getReminderEndDate()) * 1000);
                mRefillEndDateTxt.setText(getDate(refillEndDate));
                mRefillEndDateTxt.setContentDescription(getDate(refillEndDate)+getString(R.string.double_tap_to_edit));
            }
            newDailyLimit = mSelectedListItemRefill.getFrequency();
            mRefillRepeatFrequencyText.setText(newDailyLimit + (newDailyLimit > 1 ? getString(R.string.days_space) : getString(R.string.day_space)));
            mRefillRepeatFrequencyText.setContentDescription(newDailyLimit + (newDailyLimit > 1 ? getString(R.string.days_space) : getString(R.string.day_space))+getString(R.string.double_tap_to_edit));
        }
        setRefillHourMin(nextRefillDate.getTime());
        mDay = nextRefillDate.get(Calendar.DAY_OF_MONTH);
        mMonth = nextRefillDate.get(Calendar.MONTH);
        mYear = nextRefillDate.get(Calendar.YEAR);
    }

    private void setRefillHourMin(Date nextRefillDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String strTime = simpleDateFormat.format(nextRefillDate);
        String hrMin[] = strTime.split(":");
        mRefillHour = Integer.parseInt(hrMin[0]);
        mRefillMin = Integer.parseInt(hrMin[1]);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void initUI(View view) {
        mRefillDate = view.findViewById(R.id.tv_date);
        mRefillTime = view.findViewById(R.id.tv_time);
        mRepeatSwitch = view.findViewById(R.id.repeat_switch);
        mRemindText = view.findViewById(R.id.et_notes);
        mRefillRepeatTitle = view.findViewById(R.id.tv_repeat);
        mRepeatLayout = view.findViewById(R.id.rl_repeat_layout);
        mEndDateLayout = view.findViewById(R.id.rl_end_date_layout);
        mRepeatFrequencyLayout = view.findViewById(R.id.rl_repeat_frequency_layout);
        mRefillEndDateTxt = view.findViewById(R.id.tv_end_date_value);
        mRefillRepeatFrequencyText = view.findViewById(R.id.tv_repeat_frequency_text);
        mRefillRepeatFrequencyThirty = view.findViewById(R.id.thirty);
        mRefillRepeatFrequencySixty = view.findViewById(R.id.sixty);
        mRefillRepeatFrequencyNinty = view.findViewById(R.id.ninety);
        mRefillCustomFrequency = view.findViewById(R.id.custom_frequency);
        mRefillNextReminderHeader = view.findViewById(R.id.tv_next_reminder);
        mEmptyText = view.findViewById(R.id.empty_text_view);
        mHintText = view.findViewById(R.id.tv_hint_text);

        mRefillMinDate = Calendar.getInstance();
        mRefillEndDate = Calendar.getInstance();
        mDay = mRefillMinDate.get(Calendar.DAY_OF_MONTH);
        mMonth = mRefillMinDate.get(Calendar.MONTH);
        mYear = mRefillMinDate.get(Calendar.YEAR);

        mRefillDate.setText(getDate(mRefillMinDate.getTime()));
        mRefillDate.setContentDescription(getDate(mRefillMinDate.getTime())+getString(R.string.double_tap_to_edit));
        mRefillTime.setText(getTime());
        mRefillTime.setContentDescription(getTime()+getString(R.string.double_tap_to_edit));

        mRefillDate.setOnClickListener(this);
        mRefillTime.setOnClickListener(this);
        mRefillEndDateTxt.setOnClickListener(this);
        mEndDateLayout.setOnClickListener(this);
        mRefillRepeatFrequencyThirty.setOnClickListener(this);
        mRefillRepeatFrequencySixty.setOnClickListener(this);
        mRefillRepeatFrequencyNinty.setOnClickListener(this);
        mRefillCustomFrequency.setOnClickListener(this);
        mRefillNextReminderHeader.setOnClickListener(this);
        mEmptyText.setOnClickListener(this);
        mHintText.setOnClickListener(this);
        mRefillRepeatFrequencyThirty.setContentDescription(getString(R.string.thirty_days)+getString(R.string.double_tap_to_select));
        mRefillRepeatFrequencySixty.setContentDescription(getString(R.string.sixty_days)+getString(R.string.double_tap_to_select));
        mRefillRepeatFrequencyNinty.setContentDescription(getString(R.string.ninety_days)+getString(R.string.double_tap_to_select));
        RefillReminderUtils.hideKeyboard(mContext, mRemindText);

        mRepeatLayout.setEnabled(false);
        mRepeatSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            RefillReminderUtils.hideKeyboard(mContext, mRepeatSwitch);
            if (isChecked) {
                newDailyLimit = 30;
                mRefillRepeatFrequencyText.setText(getString(R.string.thirty_days));
                mRefillRepeatFrequencyText.setContentDescription(getString(R.string.thirty_days)+getString(R.string.double_tap_to_edit));
                mRefillRepeatTitle.setText(getText(R.string.repeat_every_txt));
                mRefillRepeatFrequencyText.setVisibility(View.VISIBLE);
                mEndDateLayout.setVisibility(View.VISIBLE);
                mRepeatFrequencyLayout.setVisibility(View.VISIBLE);
                mRepeatLayout.setEnabled(true);
            } else {
                mRepeatFrequencyLayout.setVisibility(View.GONE);
                mRefillRepeatTitle.setText(getText(R.string.repeat_txt));
                mRefillRepeatFrequencyText.setVisibility(View.GONE);
                mRefillEndDateTxt.setText(getString(R.string._never));
                mRefillEndDateTxt.setContentDescription(getString(R.string._never)+getString(R.string.double_tap_to_edit));
                mEndDateLayout.setVisibility(View.GONE);
            }
        });

        mRepeatLayout.setOnClickListener(view12 -> {
            if (mRepeatFrequencyLayout.getVisibility() == View.VISIBLE
                    || mRefillRepeatFrequencyText.getVisibility() == View.VISIBLE) {
                mRepeatFrequencyLayout.setVisibility(View.GONE);
                mEndDateLayout.setVisibility(View.GONE);
                mRefillRepeatFrequencyText.setVisibility(View.GONE);
                mRepeatSwitch.setChecked(false);
                mRefillEndDateTxt.setText(getString(R.string._never));
                mRefillEndDateTxt.setContentDescription(getString(R.string._never)+getString(R.string.double_tap_to_edit));
            } else {
                mRepeatSwitch.setChecked(true);
                //mRepeatFrequencyLayout.setVisibility(View.VISIBLE);
                mEndDateLayout.setVisibility(View.VISIBLE);
                mRefillRepeatFrequencyText.setVisibility(View.VISIBLE);
                showDefaultFrequency();
            }
        });

        mRefillRepeatFrequencyText.setOnClickListener(view1 -> {
            if (mRepeatFrequencyLayout.getVisibility() == View.VISIBLE) {
                mRepeatFrequencyLayout.setVisibility(View.GONE);
            } else {
                mRepeatFrequencyLayout.setVisibility(View.VISIBLE);
                mEndDateLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Shows the default frequency 30 days
     */
    private void showDefaultFrequency() {
        newDailyLimit = 30;
        mRefillRepeatFrequencyText.setText(getString(R.string.thirty_days));
        mRefillRepeatFrequencyText.setContentDescription(getString(R.string.thirty_days)+getString(R.string.double_tap_to_edit));
        mRefillRepeatFrequencyText.setVisibility(View.VISIBLE);
    }

    private String getDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return simpleDateFormat.format(date);
    }

    private String getTime(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        return simpleDateFormat.format(date);
    }

    public String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm:a");
        Calendar calendar = Calendar.getInstance();
        mTime = simpleDateFormat.format(calendar.getTime());
        String[] splitString = mTime.split(":");

        // Adjusting the hour/min. For eg if the time is 11:30PM we should display 12:00AM
        if (Integer.parseInt(splitString[1]) >= 0) {
            splitString[1] = "00";
            if (Integer.parseInt(splitString[0]) == 11 && (("AM").equalsIgnoreCase(splitString[2]) || ("A.M.").equalsIgnoreCase(splitString[2]))) {
                splitString[0] = "12";
                splitString[2] = "PM";
            } else if (Integer.parseInt(splitString[0]) == 11 && (("PM").equalsIgnoreCase(splitString[2]) || ("P.M.").equalsIgnoreCase(splitString[2]))) {
                splitString[0] = "12";
                splitString[2] = "AM";
                roundUpDayToNextDay();
            } else if (Integer.parseInt(splitString[0]) == 12 && (("AM").equalsIgnoreCase(splitString[2]) || ("A.M.").equalsIgnoreCase(splitString[2]))) {
                splitString[0] = "0";
                splitString[2] = "AM";
                splitString[0] = String.valueOf(Integer.parseInt(splitString[0]) + 1);
            } else if (Integer.parseInt(splitString[0]) == 12 && (("PM").equalsIgnoreCase(splitString[2]) || ("P.M.").equalsIgnoreCase(splitString[2]))) {
                splitString[0] = "12";
                splitString[2] = "PM";
                splitString[0] = String.valueOf(Integer.parseInt(splitString[0]) + 1);
            } else {
                splitString[0] = String.valueOf(Integer.parseInt(splitString[0]) + 1);
            }
        }
        // to adjust the AM/PM spinner in the time picker dialog
        if (splitString[2].equalsIgnoreCase(Util.getSystemAMFormat()) && Integer.parseInt(splitString[0]) == RefillReminderConstants.REFILL_12HRS) {
            mRefillHour = 0;
        } else if (splitString[2].equalsIgnoreCase(Util.getSystemPMFormat()) && Integer.parseInt(splitString[0]) < RefillReminderConstants.REFILL_12HRS) {//pm
            mRefillHour = Integer.parseInt(splitString[0]) + RefillReminderConstants.REFILL_12HRS;
        } else {
            mRefillHour = Integer.parseInt(splitString[0]);
        }
        mRefillMin = Integer.parseInt(splitString[1]);
        if (Integer.parseInt(splitString[0]) > RefillReminderConstants.REFILL_12HRS) {
            splitString[0] = String.valueOf(Integer.parseInt(splitString[0]) - RefillReminderConstants.REFILL_12HRS);
        }
        return prepareFormattedString(splitString);
    }

    private void roundUpDayToNextDay() {
        Calendar roundUpDay = Calendar.getInstance();
        roundUpDay.add(Calendar.HOUR, 1);
        mRefillDate.setText(getDate(roundUpDay.getTime()));
        mRefillDate.setContentDescription(getDate(roundUpDay.getTime())+getString(R.string.double_tap_to_edit));
        mYear = roundUpDay.get(Calendar.YEAR);
        mMonth = roundUpDay.get(Calendar.MONTH);
        mDay = roundUpDay.get(Calendar.DAY_OF_MONTH);
        mRefillMinDate = roundUpDay;
    }

    private String prepareFormattedString(String... splitString) {
        StringBuilder builder = new StringBuilder();
        builder.append(splitString[0]);
        builder.append(":");
        builder.append(splitString[1]);
        builder.append(" ");
        builder.append(splitString[2]);
        return builder.toString();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_next_reminder:
            case R.id.tv_date:
                ViewClickHandler.preventMultiClick(v);
                RefillReminderUtils.hideKeyboard(mContext, mRemindText);
                startDatePickerDialog = new DatePickerDialog(mContext, R.style.datepicker, (view, year, month, dayOfMonth) -> {
                    mRefillDate.setText(getDateInFormat(month, dayOfMonth, year));
                    mRefillDate.setContentDescription(getDateInFormat(month, dayOfMonth, year)+getString(R.string.double_tap_to_edit));
                    if (mRepeatSwitch.isChecked()) {
                        updateEndDate();
                    } else {
                        mRefillEndDateTxt.setText(getString(R.string._never));
                        mRefillEndDateTxt.setContentDescription(getString(R.string._never)+getString(R.string.double_tap_to_edit));
                    }
                    mYear = year;
                    mMonth = month;
                    mDay = dayOfMonth;
                    if (mDay == mRefillMinDate.get(Calendar.DAY_OF_MONTH) && mMonth == mRefillMinDate.get(Calendar.MONTH) &&
                            mYear == mRefillMinDate.get(Calendar.YEAR) && needToIncreaseDate(mRefillHour,mRefillMin)) {
                        mRefillTime.setText(getTime());
                        mRefillTime.setContentDescription(getTime()+getString(R.string.double_tap_to_edit));
                    }
                }, mYear, mMonth, mDay);
                startDatePickerDialog.setCanceledOnTouchOutside(false);
                startDatePickerDialog.getDatePicker().setMinDate(mRefillMinDate.getTimeInMillis());
                startDatePickerDialog.show();
                break;
            case R.id.empty_text_view:
            case R.id.tv_time:
                ViewClickHandler.preventMultiClick(v);
                mRefillMinDate = Calendar.getInstance();
                TimePickerDialog.showDialog(mContext, getString(R.string.set_reminder), mRefillHour, mRefillMin,
                        false, changeTimeSetListener, onDismissListener, PillpopperConstants.REFILL_TIME_PICKER_INTERVAL);
                break;
            case R.id.rl_end_date_layout:
            case R.id.tv_end_date_value:
                ViewClickHandler.preventMultiClick(v);
                RefillReminderUtils.hideKeyboard(mContext, mRemindText);
                if (!TextUtils.isEmpty(mRefillEndDateTxt.getText().toString()) && !mRefillEndDateTxt.getText().toString().equalsIgnoreCase(getString(R.string._never))) {
                    mRefillEndDate.setTime(getEndDate(mRefillEndDateTxt.getText().toString()));
                } else {
                    mRefillEndDate.setTime(getEndDate(mRefillDate.getText().toString()));
                }
                if (mRefillEndDateTxt.getText().toString().equalsIgnoreCase(getString(R.string._never))) {
                    mRefillEndDate.add(Calendar.DATE, 1);
                }
                showEndDatePicker();

                break;
            case R.id.thirty:
                newDailyLimit = 30;
                mRefillRepeatFrequencyText.setText(getString(R.string.thirty_days));
                mRefillRepeatFrequencyText.setContentDescription(getString(R.string.thirty_days)+getString(R.string.double_tap_to_edit));
                mRefillRepeatFrequencyText.setVisibility(View.VISIBLE);
                mRepeatFrequencyLayout.setVisibility(View.GONE);
                break;
            case R.id.sixty:
                newDailyLimit = 60;
                mRefillRepeatFrequencyText.setText(getString(R.string.sixty_days));
                mRefillRepeatFrequencyText.setContentDescription(getString(R.string.sixty_days)+getString(R.string.double_tap_to_edit));
                mRefillRepeatFrequencyText.setVisibility(View.VISIBLE);
                mRepeatFrequencyLayout.setVisibility(View.GONE);
                break;
            case R.id.ninety:
                newDailyLimit = 90;
                mRefillRepeatFrequencyText.setText(getString(R.string.ninety_days));
                mRefillRepeatFrequencyText.setContentDescription(getString(R.string.ninety_days)+getString(R.string.double_tap_to_edit));
                mRefillRepeatFrequencyText.setVisibility(View.VISIBLE);
                mRepeatFrequencyLayout.setVisibility(View.GONE);
                break;
            case R.id.custom_frequency:
                ViewClickHandler.preventMultiClick(v);
                RefillReminderUtils.hideKeyboard(mContext, mRemindText);
                daysPicker();
                break;
            case R.id.tv_hint_text:
                mRemindText.requestFocus();
        }
    }
    private void showEndDatePicker() {
        endDatePickerDialog = new DatePickerDialog(mContext,
                R.style.datepicker, null, mRefillEndDate.get(Calendar.YEAR), mRefillEndDate.get(Calendar.MONTH), mRefillEndDate.get(Calendar.DAY_OF_MONTH));

        endDatePickerDialog.setCanceledOnTouchOutside(false);
        mRefillEndDate.setTime(getEndDate(mRefillDate.getText().toString()));
        mRefillEndDate.add(Calendar.DATE, 1);
        endDatePickerDialog.getDatePicker().setMinDate(mRefillEndDate.getTimeInMillis());
        endDatePickerDialog.getDatePicker().setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);

        endDatePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.ok_text),
                (dialog, which) -> {
                    mRefillEndDateTxt.setText(getDateInFormat(endDatePickerDialog.getDatePicker().getMonth(),
                            endDatePickerDialog.getDatePicker().getDayOfMonth(), endDatePickerDialog.getDatePicker().getYear()));
                    mRefillEndDateTxt.setContentDescription(getDateInFormat(endDatePickerDialog.getDatePicker().getMonth(),
                            endDatePickerDialog.getDatePicker().getDayOfMonth(), endDatePickerDialog.getDatePicker().getYear())+getString(R.string.double_tap_to_edit));
                });

        endDatePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.set_to_never), (dialogInterface, i) -> {
            mRefillEndDateTxt.setText(getString(R.string._never));
            mRefillEndDateTxt.setContentDescription(getString(R.string._never)+getString(R.string.double_tap_to_edit));
            dialogInterface.dismiss();
        });

        endDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        if(!endDatePickerDialog.isShowing()){
            endDatePickerDialog.show();
        }
        endDatePickerDialog.getButton(Dialog.BUTTON_POSITIVE).setPadding(0, 0, 0, 0);
        endDatePickerDialog.getButton(Dialog.BUTTON_NEGATIVE).setPadding(0, 0, 0, 0);
        endDatePickerDialog.getButton(Dialog.BUTTON_NEUTRAL).setPadding(0, 0, 0, 0);
    }

    private String getDateInFormat(int month, int dayOfMonth, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return getDate(cal.getTime());
    }

    android.app.TimePickerDialog.OnTimeSetListener changeTimeSetListener = new android.app.TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mRefillHour = hourOfDay;
            mRefillMin = minute;
            refillTime = getAmPmTimeFromHrMin(hourOfDay, minute);
            mRefillTime.setText(refillTime);
            mRefillTime.setContentDescription(refillTime+getString(R.string.double_tap_to_edit));
            if (mDay == mRefillMinDate.get(Calendar.DAY_OF_MONTH) && mMonth == mRefillMinDate.get(Calendar.MONTH) && mYear == mRefillMinDate.get(Calendar.YEAR)
                    && needToIncreaseDate(mRefillHour,mRefillMin)) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 1);

                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);
                mRefillDate.setText(getDate(cal.getTime()));
                mRefillDate.setContentDescription(getDate(cal.getTime())+getString(R.string.double_tap_to_edit));
                updateEndDate();
            }
        }
    };

    private boolean needToIncreaseDate(int selectedHour, int selectedMinute){
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.HOUR_OF_DAY,selectedHour);
        selectedDate.set(Calendar.MINUTE,selectedMinute);
        return selectedDate.getTime().before(Calendar.getInstance().getTime());
    }

    // Handling the 24hr format.
    public String getAmPmTimeFromHrMin(int hour, int min) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm", Locale.getDefault());
        Date dtNewTime;
        try {
            String amPMStr;
            if (hour >= RefillReminderConstants.REFILL_12HRS && hour < RefillReminderConstants.REFILL_24HRS) {
                amPMStr = Util.getSystemPMFormat();
            } else if (hour == 0) {
                amPMStr = Util.getSystemAMFormat();
                hour = RefillReminderConstants.REFILL_12HRS;
            } else {
                amPMStr = Util.getSystemAMFormat();
            }

            dtNewTime = sdf.parse(hour + ":" + min);
            String newTime = sdf.format(dtNewTime);
            changedTime = newTime + " " + amPMStr;

        } catch (ParseException e) {
            LoggerUtils.exception("ParseException" + e.getMessage());
        }
        return changedTime;
    }

    DialogInterface.OnDismissListener onDismissListener = dialog -> dialog.dismiss();

    @Override
    public void onPause() {
        super.onPause();
        RefillReminderUtils.hideKeyboard(mContext, mRemindText);
        if (null != startDatePickerDialog && startDatePickerDialog.isShowing()) {
            startDatePickerDialog.dismiss();
        }
        if (null != endDatePickerDialog && endDatePickerDialog.isShowing()) {
            endDatePickerDialog.dismiss();
        }
        if(null!= numberPickerAlertDialog && numberPickerAlertDialog.isShowing()){
            numberPickerAlertDialog.dismiss();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.refill_reminder_save_menu, menu);
        menu.findItem(R.id.save_menu_item).setOnMenuItemClickListener(menuItem -> {
            FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(mContext,FireBaseConstants.Event.REFILL_REMINDER_SAVE);
            validateAndSave();
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * this method is for validation of refill reminder date selection
     * and of any URL, or SQL statements in the notes section
     */
    private void validateAndSave() {
        if (!isValidRefillDateTime()) {
            RefillReminderAlertDialog errorRefillDateAlertDialog = new RefillReminderAlertDialog(mContext,
                    getString(R.string.refill_error_title), getString(R.string.refill_error_message_datetime),
                    getString(R.string.refill_alert_ok), errorRefillDateAlertListener, null, null);
            errorRefillDateAlertDialog.showDialog();
        } else if (!isValidRefillNotes()) {
            RefillReminderAlertDialog errorNotesAlertDialog = new RefillReminderAlertDialog(mContext,
                    getString(R.string.refill_error_title), mNotesErrorMessage,
                    getString(R.string.refill_alert_ok), errorNotesAlertListener, null, null);
            errorNotesAlertDialog.showDialog();
        } else {
            //Save Refill Reminder - for now, it is just create refill.
            // for update we shall retrieve the object
            RefillReminder refillReminder;
            if(mSelectedListItemRefill == null) {
                refillReminder = new RefillReminder();
                prepareRefillReminderObject(refillReminder);
                RefillReminderController.getInstance(mContext.getApplicationContext()).addRefillReminder(mContext.getApplicationContext(), refillReminder);
            }else{
                refillReminder  = mSelectedListItemRefill;
                //cancel the old Alarm for this refill reminder
                RefillReminderNotificationUtil.getInstance(mContext.getApplicationContext()).cancelRefillReminder(mSelectedListItemRefill.getNextReminderDate(), mContext.getApplicationContext());
                prepareRefillReminderObject(refillReminder);
                RefillReminderController.getInstance(mContext.getApplicationContext()).updateRefillReminder(refillReminder);
                RefillReminderNotificationUtil.getInstance(mContext.getApplicationContext()).createNextRefillReminderAlarms(mContext.getApplicationContext());
            }
            RequestWrapper requestWrapper = new RequestWrapper(mContext);
            refillReminderInterface.addLogEntryForRefillReminderUpdate(requestWrapper.createRefillReminderRequestObject(refillReminder));
        }
    }

    private boolean isValidRefillDateTime() {
        Date currentDateTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm:a");
        Date baselineDateTime = getDateTime(getDate(currentDateTime), getFormattedTime(dateFormat.format(currentDateTime)));
        Date selectedDateTime = getDateTime(mRefillDate.getText().toString(), getFormattedTime(mRefillTime.getText().toString()));

        return !(selectedDateTime.before(baselineDateTime) || (selectedDateTime.getTime() == baselineDateTime.getTime() && currentDateTime.after(baselineDateTime)) || selectedDateTime.before(currentDateTime));
    }

    /**
     * @param time
     * @return h:mm:a formatted time
     */
    private String getFormattedTime(String time) {
        time = time.replace(' ', ':');
        String[] timeArray = time.split(":");
        StringBuilder formattedTime = new StringBuilder();
        if (timeArray.length == 2) {
            formattedTime.append(" "); // space between date and time
            formattedTime.append(timeArray[0]);
            formattedTime.append(":");
            formattedTime.append(timeArray[1].substring(0, 2));
            formattedTime.append(":");
            formattedTime.append(timeArray[1].substring(2));
        } else if (timeArray.length == 3) {
            formattedTime.append(" "); // space between date and time
            formattedTime.append(timeArray[0]);
            formattedTime.append(":");
            formattedTime.append(timeArray[1]);
            formattedTime.append(":");
            formattedTime.append(timeArray[2]);
        }
        return formattedTime.toString();
    }

    private Date getDateTime(String date, String time) {
        SimpleDateFormat dateFormat;
        if(!RefillReminderUtils.isEmptyString(time)) {
            dateFormat = new SimpleDateFormat(
                    "MMMM dd, yyyy h:mm:a");
        }else{
            dateFormat = new SimpleDateFormat(
                    "MMMM dd, yyyy");
        }
        try {
            return dateFormat.parse(date.concat(time));
        } catch (ParseException ex) {
            RefillReminderLog.say(ex);
        }
        return new Date();
    }

    private Date getEndDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            RefillReminderLog.say(ex);
        }
        return new Date();
    }

    private final android.content.DialogInterface.OnClickListener errorRefillDateAlertListener = (dialog, which) -> {
        dialog.dismiss();
        isValidRefillNotes();
    };

    private void prepareRefillReminderObject(RefillReminder refillReminder) {
        if(null == refillReminder.getReminderGuid()) {
            refillReminder.setReminderGuid(RefillReminderUtils.getRandomGuid());
        }
        String deviceDefaultTzSecs = String.valueOf(RefillReminderUtils.getTzOffsetSecs());
        refillReminder.setUserId(FrontController.getInstance(mContext).getPrimaryUserIdIgnoreEnabled());
        refillReminder.setRecurring(mRepeatSwitch.isChecked());
        refillReminder.setReminderNote(mRemindText.getText().toString());
        refillReminder.setNextReminderDate(getNextReminderDate());
        refillReminder.setRecurring(mRepeatSwitch.isChecked());
        refillReminder.setNextReminderTzSecs(deviceDefaultTzSecs);
        refillReminder.setFrequency(newDailyLimit);
        refillReminder.setReminderEndDate(getRefillReminderEndDate());
        refillReminder.setReminderEndTzSecs(String.valueOf(RefillReminderUtils.getTzOffsetSecs(TimeZone.getDefault())));

        if(null == mSelectedListItemRefill) {
            refillReminder.setLastAcknowledgeDate("-1");
            refillReminder.setLastAcknowledgeTzSecs(deviceDefaultTzSecs);
            refillReminder.setOverdueReminderDate("-1");
            refillReminder.setOverdueReminderTzSecs(deviceDefaultTzSecs);
        }
    }

    private String getNextReminderDate() {
        Date nextReminderDate = getDateTime(mRefillDate.getText().toString(), getFormattedTime(mRefillTime.getText().toString()));
        return String.valueOf(nextReminderDate.getTime() / 1000);
    }

    private String getRefillReminderEndDate() {
        if (!mRefillEndDateTxt.getText().toString().equalsIgnoreCase(getString(R.string._never))) {
            Date endDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat =  new SimpleDateFormat("MMMM dd, yyyy h:mm:a");
            try {
                String endDateValue = mRefillEndDateTxt.getText().toString().concat(getFormattedTime(mRefillTime.getText().toString()));
                endDate.setTime(dateFormat.parse(endDateValue).getTime());
            } catch (ParseException ex) {
                RefillReminderLog.say(ex);
            }
            return String.valueOf(endDate.getTime() / 1000);
        }
        return null;
    }

    private String prepareNextRefillReminderISODate() {
        Calendar refillCalender = Calendar.getInstance();
        refillCalender.set(mYear, mMonth, mDay, mRefillHour, mRefillMin);
        return String.valueOf(refillCalender.getTimeInMillis() / 1000);
    }


    private boolean isValidRefillNotes() {
        Pattern webLinkREGEX = Pattern.compile(RefillReminderConstants.WEB_LINK_REGEX);
        Matcher webLinkMatcher = webLinkREGEX.matcher(mRemindText.getText().toString());
        Pattern htmlTagPattern = Pattern.compile(RefillReminderConstants.HTML_TAG_PATTERN);
        Matcher htmlTagMatcher = htmlTagPattern.matcher(mRemindText.getText().toString());
        Pattern invalidCharsPattern = Pattern.compile(RefillReminderConstants.INVALID_CHARS_PATTERN);
        Matcher invalidCharsMatcher = invalidCharsPattern.matcher(mRemindText.getText().toString());
        if (webLinkMatcher.find() || htmlTagMatcher.find() || invalidCharsMatcher.find()) {
            mNotesErrorMessage = getString(R.string.textfield_error_message);
            return false;
        } else {
            return true;
        }
    }

    private final android.content.DialogInterface.OnClickListener errorNotesAlertListener = (dialog, which) -> dialog.dismiss();

    private void daysPicker() {
        View npView = getLayoutInflater().inflate(R.layout.choose_number, null);
        numberPicker = npView.findViewById(R.id.numberPicker1);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(99);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(newDailyLimit);
        Util.colorNumberPickerText(numberPicker, Util.getColorWrapper(getActivity(), R.color.text_content));
        numberPickerAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(getResources().getString(R.string.set_repeat_frequency)).setView(npView)
                .setPositiveButton(getResources().getString(R.string._set_caps), (dialog, whichButton) -> {
                    newDailyLimit = numberPicker.getValue();
                    mRepeatFrequencyLayout.setVisibility(View.GONE);
                    mRefillRepeatFrequencyText.setVisibility(View.VISIBLE);
                    mRefillRepeatFrequencyText.setText(newDailyLimit + (newDailyLimit > 1 ? getString(R.string.days_space) : getString(R.string.day_space)));
                    mRefillRepeatFrequencyText.setContentDescription(newDailyLimit + (newDailyLimit > 1 ? getString(R.string.days_space) : getString(R.string.day_space))+getString(R.string.double_tap_to_edit));
                }).setNegativeButton(R.string.cancel_text, (dialog, whichButton) -> dialog.dismiss()).create();
        if (!getActivity().isFinishing()) {
            numberPickerAlertDialog.show();
        }
    }

    private void updateEndDate(){
        Date nextReminderDate = getDateTime(mRefillDate.getText().toString(), getFormattedTime(mRefillTime.getText().toString()));
        Date refillEndDate = null;
        if (!TextUtils.isEmpty(mRefillEndDateTxt.getText().toString()) && !mRefillEndDateTxt.getText().toString().equalsIgnoreCase(getString(R.string._never))) {
            refillEndDate = getDateTime(mRefillEndDateTxt.getText().toString(),"");
        }

        if (!TextUtils.isEmpty(mRefillEndDateTxt.getText().toString()) && mRefillEndDateTxt.getText().toString().equalsIgnoreCase(getString(R.string._never))) {
            mRefillEndDateTxt.setText(getString(R.string._never));
            mRefillEndDateTxt.setContentDescription(getString(R.string._never)+getString(R.string.double_tap_to_edit));
        } else if (nextReminderDate.after(refillEndDate)) {
            mRefillEndDate.setTime(nextReminderDate);
            mRefillEndDate.add(Calendar.DATE, 1);
            mRefillEndDateTxt.setText(getDateInFormat(mRefillEndDate.get(Calendar.MONTH),
                    mRefillEndDate.get(Calendar.DAY_OF_MONTH), mRefillEndDate.get(Calendar.YEAR)));
            mRefillEndDateTxt.setContentDescription(getDateInFormat(mRefillEndDate.get(Calendar.MONTH),
                    mRefillEndDate.get(Calendar.DAY_OF_MONTH), mRefillEndDate.get(Calendar.YEAR))+getString(R.string.double_tap_to_edit));
        }
    }
}
