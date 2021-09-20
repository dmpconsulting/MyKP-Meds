package com.montunosoftware.pillpopper.android.view;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * @authorsc
 * Created by adhithyaravipati on 9/30/16.
 */
@SuppressLint("ValidFragment")
public class DateAndTimePickerDialog extends DialogFragment
    implements DialogInterface.OnClickListener{



    public interface OnDateAndTimeSetListener {
        void onDateAndTimeSet(PillpopperTime pillpopperTime);
    }

    private Context mContext;
    private OnDateAndTimeSetListener mOnDateAndTimeSetListener;


    private TimePicker mTimePicker;

    private LinearLayout mDateHolderLinearLayout;
    private TextView mDateTextView;
    private TextView mYearTextView;

    private PillpopperTime mReferencePillpopperTime;
    private PillpopperTime mPillpopperTime;
    private Calendar mCalendar;

    private boolean mIsFutureTimeValid;

    private int mMinuteSpinnerInterval;
    private String mPositiveBtnText = null;
    private boolean isFromHistory;
    private boolean mIsDateHolderNeedsDisabled = false;

    /*public DateAndTimePickerDialog(Context context, OnDateAndTimeSetListener onDateAndTimeSetListener, boolean isFutureTimeValid) {
        this.mContext = context;
        this.mOnDateAndTimeSetListener = onDateAndTimeSetListener;
        this.mIsFutureTimeValid = isFutureTimeValid;
        this.mPillpopperTime = PillpopperTime.now();
    }

    public DateAndTimePickerDialog(Context context, OnDateAndTimeSetListener onDateAndTimeSetListener, boolean isFutureTimeValid, int minuteSpinnedInterval) {
        this.mContext = context;
        this.mOnDateAndTimeSetListener = onDateAndTimeSetListener;
        this.mIsFutureTimeValid = isFutureTimeValid;
        this.mPillpopperTime = PillpopperTime.now();
        this.mMinuteSpinnerInterval = minuteSpinnedInterval;
    }*/

    public DateAndTimePickerDialog(Context context, OnDateAndTimeSetListener onDateAndTimeSetListener,
                                   boolean isFutureTimeValid, PillpopperTime initialDialogTime,
                                   boolean isFromHistory, int mMinuteSpinnerInterval, String positiveBtntxt, boolean isDateHolderNeedsDisabled) {
        this.mContext = context;
        this.mOnDateAndTimeSetListener = onDateAndTimeSetListener;
        this.mPillpopperTime = initialDialogTime;
        this.mIsFutureTimeValid = isFutureTimeValid;
        this.isFromHistory = isFromHistory;
        this.mMinuteSpinnerInterval = mMinuteSpinnerInterval;
        this.mPositiveBtnText = positiveBtntxt;
        this.mIsDateHolderNeedsDisabled = isDateHolderNeedsDisabled;
    }

    public DateAndTimePickerDialog(Context context, OnDateAndTimeSetListener onDateAndTimeSetListener,
                                   boolean isFutureTimeValid, PillpopperTime initialDialogTime,
                                   int mMinuteSpinnerInterval, String positiveBtntxt, boolean isDateHolderNeedsDisabled) {
        this.mContext = context;
        this.mOnDateAndTimeSetListener = onDateAndTimeSetListener;
        this.mPillpopperTime = initialDialogTime;
        this.mIsFutureTimeValid = isFutureTimeValid;
        this.mMinuteSpinnerInterval = mMinuteSpinnerInterval;
        this.mPositiveBtnText = positiveBtntxt;
        this.mIsDateHolderNeedsDisabled = isDateHolderNeedsDisabled;
        this.isFromHistory = false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder;

        if (null == mContext) {
            mContext = getActivity();
        }
        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View mainDialogView = inflater.inflate(R.layout.dialog_date_and_time_picker,
                null);

        builder = new AlertDialog.Builder(mContext, R.style.KPBlueTimeDialogTheme);

        initUI(mainDialogView);
        initCalendarReference();
        loadData(mPillpopperTime);
        builder.setView(mainDialogView);

        builder.setCancelable(false);

        if(!isFromHistory) {
            builder.setPositiveButton(mPositiveBtnText, this);
        }else{
            builder.setPositiveButton(mContext.getResources().getString(R.string._set), this);
        }

        builder.setNegativeButton(mContext.getResources().getString(R.string.cancel_text), this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                int hour = Util.getTimePickerHourWrapper(mTimePicker);
                int minute = Util.getTimePickerMinuteWrapper(mTimePicker);

                if(mMinuteSpinnerInterval != 0
                        && mMinuteSpinnerInterval != 1) {
                    minute = minute * mMinuteSpinnerInterval;
                }

                mCalendar.set(Calendar.HOUR_OF_DAY, hour);
                mCalendar.set(Calendar.MINUTE, minute);

                PillpopperTime callbackReturnValue = new PillpopperTime(mCalendar);

                if(!mIsFutureTimeValid) {
                    // this block will be executed for taken earlier in reminder's screen.
                    if(isFromHistory){
                        //Min allowed time for history action time edit is 12:00am of that day, max allowed time is current date and time.

                        //min time allowed is 12:00 am of scheduled day
                        Calendar scheduledDay = Calendar.getInstance();
                        scheduledDay.setTimeInMillis(mReferencePillpopperTime.getGmtMilliseconds());
                        scheduledDay.set(Calendar.HOUR_OF_DAY, 0);
                        scheduledDay.set(Calendar.MINUTE,  0);
                        scheduledDay.set(Calendar.SECOND, 0);
                        PillpopperTime minTimeAllowed = new PillpopperTime(scheduledDay.getTimeInMillis()/1000);

                        // max time allowed is PillpopperTime.now()
                        if (callbackReturnValue.equals(minTimeAllowed) || (callbackReturnValue.after(minTimeAllowed) && callbackReturnValue.before(PillpopperTime.now()))) {
                            returnToDateTimeSetCallback(callbackReturnValue);
                        } else {
                            Toast.makeText(mContext, "INVALID TIME SET", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        //existing logic for other screens
                        if (callbackReturnValue.before(mReferencePillpopperTime) || callbackReturnValue.equals(mReferencePillpopperTime)) {
                            returnToDateTimeSetCallback(callbackReturnValue);
                        } else {
                            Toast.makeText(mContext, "INVALID TIME SET", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    // this block will be executed for History Edit screen.
                    if(callbackReturnValue.after(mReferencePillpopperTime) || callbackReturnValue.equals(mReferencePillpopperTime)) {
                        returnToDateTimeSetCallback(callbackReturnValue);
                    } else {
                        Toast.makeText(mContext, "INVALID TIME SET", Toast.LENGTH_LONG).show();
                    }
                }

                break;

            case AlertDialog.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
        }

    }

    private void returnToDateTimeSetCallback(PillpopperTime callbackReturnValue) {
        if(null!=mOnDateAndTimeSetListener) {
            mOnDateAndTimeSetListener.onDateAndTimeSet(callbackReturnValue);
        }
    }

    private void initUI(View view)  {
        mDateHolderLinearLayout = view.findViewById(R.id.dialog_date_holder_linear_layout);
       // mDateTextView = view.findViewById(R.id.dialog_date_textview);
        mYearTextView = view.findViewById(R.id.header_text);
        mTimePicker = view.findViewById(R.id.dialog_time_picker);

        if(mMinuteSpinnerInterval == 0) {
            applyTimePickerStyling(mContext, mTimePicker);
        } else {
            applyTimePickerStyling(mContext, mTimePicker, mMinuteSpinnerInterval);
        }

        if(mIsDateHolderNeedsDisabled){
            mDateHolderLinearLayout.setEnabled(false);
        }

        if(isFromHistory){
            mDateHolderLinearLayout.setVisibility(View.GONE);
        }

        mDateHolderLinearLayout.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    mContext,
                    R.style.datepicker,
                    (view1, year, monthOfYear, dayOfMonth) -> {
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, monthOfYear);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        mPillpopperTime = new PillpopperTime(mCalendar);
                        updateDayAndDateText(mPillpopperTime);
                    },
                    mPillpopperTime.getLocalDay().getYear(),
                    mPillpopperTime.getLocalDay().getMonth(),
                    mPillpopperTime.getLocalDay().getDay()
            );
            datePickerDialog.show();
        });
    }

    private void initCalendarReference() {
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(mPillpopperTime.getGmtMilliseconds());
    }

    private void loadData(PillpopperTime pillpopperTime) {
        mReferencePillpopperTime = pillpopperTime;
       // updateDayAndDateText(pillpopperTime);
        setTimePickerTime(pillpopperTime.getLocalHourMinute().getHour(), pillpopperTime.getLocalHourMinute().getMinute());
    }

    private void updateDayAndDateText(PillpopperTime pillpopperTime) {
        String year = Integer.toString(pillpopperTime.getLocalDay().getYear());
        String date = getDayString(pillpopperTime.getLocalDay().getDayOfWeek().getDayNumber()) + ", " + getMonthString(pillpopperTime.getLocalDay().getMonth()) + " " + pillpopperTime.getLocalDay().getDay();

        setYearText(year);
        setDateText(date);
    }

    private void setYearText(String dayName) {
        mYearTextView.setText(dayName);
    }

    private void setDateText(String date) {
        mDateTextView.setText(date);
    }

    private void setTimePickerTime(int hour, int minute) {
        Util.setTimePickerHourWrapper(mTimePicker, hour);

        if(mMinuteSpinnerInterval != 0
                && mMinuteSpinnerInterval != 1) {
            Util.setTimePickerMinuteWrapper(mTimePicker, minute / mMinuteSpinnerInterval);
        } else {
            Util.setTimePickerMinuteWrapper(mTimePicker, minute);
        }

    }

    private String getMonthString(int month) {
        switch (month + 1) {
            case 1: return "Jan";
            case 2: return "Feb";
            case 3: return "Mar";
            case 4: return "Apr";
            case 5: return "May";
            case 6: return "Jun";
            case 7: return "Jul";
            case 8: return "Aug";
            case 9: return "Sep";
            case 10: return "Oct";
            case 11: return "Nov";
            case 12: return "Dec";
        }
        return null;
    }

    private String getDayString(int day) {
        switch (day) {
            case 1: return "Sun";
            case 2: return "Mon";
            case 3: return "Tue";
            case 4: return "Wed";
            case 5: return "Thu";
            case 6: return "Fri";
            case 7: return "Sat";
        }

        return null;
    }

    private void applyTimePickerStyling(Context context, TimePicker timePicker){
        Resources system = Resources.getSystem();
        int hourNumberPickerId = system.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = system.getIdentifier("minute", "id", "android");
        int ampmNumberPickerId = system.getIdentifier("amPm", "id", "android");

        NumberPicker hourNumberPicker = timePicker.findViewById(hourNumberPickerId);
        NumberPicker minuteNumberPicker = timePicker.findViewById(minuteNumberPickerId);
        NumberPicker ampmNumberPicker = timePicker.findViewById(ampmNumberPickerId);

        setNumberPickerDividerColour(context, hourNumberPicker);
        setNumberPickerDividerColour(context, minuteNumberPicker);
        setNumberPickerDividerColour(context, ampmNumberPicker);
    }

    private void applyTimePickerStyling(Context context, TimePicker timePicker, int minutesInterval){
        Resources system = Resources.getSystem();
        int hourNumberPickerId = system.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = system.getIdentifier("minute", "id", "android");
        int ampmNumberPickerId = system.getIdentifier("amPm", "id", "android");

        //NumberPicker hourNumberPicker = (NumberPicker) timePicker.findViewById(hourNumberPickerId);
        NumberPicker minuteNumberPicker = timePicker.findViewById(minuteNumberPickerId);
        //NumberPicker ampmNumberPicker = (NumberPicker) timePicker.findViewById(ampmNumberPickerId);

        minuteNumberPicker.setMinValue(0);
        minuteNumberPicker.setMaxValue((60/minutesInterval) - 1);
        List<String> displayedValues = new ArrayList<>();
        for(int i = 0; i < 60; i+= minutesInterval) {
            displayedValues.add(String.format("%02d",i));
        }
        minuteNumberPicker.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));

        applyTimePickerStyling(context,timePicker);
    }

    private void setNumberPickerDividerColour(Context context, NumberPicker number_picker) {
        final int count = number_picker.getChildCount();

        for (int i = 0; i < count; i++) {

            try {
                Field dividerField = number_picker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(Util.getColorWrapper(context, R.color.kp_theme_blue));
                dividerField.set(number_picker, colorDrawable);

                number_picker.invalidate();
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                PillpopperLog.say(e.getMessage());
            }
        }
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }
}
