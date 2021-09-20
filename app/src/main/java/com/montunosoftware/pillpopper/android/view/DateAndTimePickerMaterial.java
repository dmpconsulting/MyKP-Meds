package com.montunosoftware.pillpopper.android.view;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.Calendar;

@SuppressLint("ValidFragment")
public class DateAndTimePickerMaterial extends DialogFragment implements DialogInterface.OnClickListener,
                                                                            View.OnClickListener {

    private Context mContext;
    private TakenTimeSetListener mDialogButtonsListener;

    private TimePicker mCustomTimePicker;
    private TextView mMainDialogDateTextView;
    private TextView mMainDialogYearTextView;
    private LinearLayout mMainDialogDateYearHolderLinearLayout;

    private String mDialogTitle;

    private PillpopperTime mPillpopperTime;
    private Calendar mCalendar;

    private boolean mIsFutureTimeValid;
    private String mPositiveBtnText;

    public interface TakenTimeSetListener {
        void onTakenTimeDialogPositiveClick(PillpopperTime pillpopperTime);
    }


    public DateAndTimePickerMaterial(Context context, String title, TakenTimeSetListener callback,boolean isFutureTimeValid) {
        this.mContext = context;
        this.mDialogButtonsListener = callback;
        this.mDialogTitle = title;
        this.mPillpopperTime = new PillpopperTime(PillpopperTime.now());
        this.mIsFutureTimeValid = isFutureTimeValid;
    }

    public DateAndTimePickerMaterial(Context context, String title, TakenTimeSetListener callback,
                                     PillpopperTime pillpopperTime, boolean isFutureTimeValid, String positiveBtnText) {
        this.mContext = context;
        this.mDialogButtonsListener = callback;
        this.mDialogTitle = title;
        this.mPillpopperTime = pillpopperTime;
        this.mIsFutureTimeValid = isFutureTimeValid;
        this.mPositiveBtnText = positiveBtnText;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogMaterialTheme_Reminders);

        View mainDialogView = getActivity().getLayoutInflater().inflate(R.layout.reminders_screen_date_time_picker_dialog,
                                                                            new LinearLayout(mContext), false);
        builder.setView(mainDialogView);
        initDialogUi(mainDialogView);

        if(mDialogTitle != null) {
            builder.setTitle(mDialogTitle);
        }


        if(null != mPositiveBtnText) {
            builder.setPositiveButton(mPositiveBtnText, null);
        }else{
            builder.setPositiveButton(getActivity().getResources().getString(R.string._set), null);
        }
        builder.setNegativeButton(getActivity().getResources().getString(R.string.cancel_text), this);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button dialogPositiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            dialogPositiveButton.setOnClickListener(view -> {
                mCalendar.set(Calendar.HOUR_OF_DAY, Util.getTimePickerHourWrapper(mCustomTimePicker));
                mCalendar.set(Calendar.MINUTE, Util.getTimePickerMinuteWrapper(mCustomTimePicker));
                PillpopperTime callbackReturnValue = new PillpopperTime(mCalendar);
                if (!mIsFutureTimeValid) {
                    if (callbackReturnValue.before(mPillpopperTime) || callbackReturnValue.equals(mPillpopperTime)) {
                        mDialogButtonsListener.onTakenTimeDialogPositiveClick(callbackReturnValue);
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(mContext, "INVALID TIME SET", Toast.LENGTH_LONG).show();
                    }

                } else {
                    mDialogButtonsListener.onTakenTimeDialogPositiveClick(callbackReturnValue);
                    alertDialog.dismiss();
                }
            });
        });
        RunTimeData.getInstance().setAlertDialogInstance(alertDialog);
        return alertDialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int dialogButton) {
        LoggerUtils.info("OverdueDosesDialag: Taken At Time set");
        switch (dialogButton) {
            case AlertDialog.BUTTON_NEGATIVE:
                dialog.dismiss();

        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.dialog_date_year_holder_linearlayout) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                                                                     R.style.DialogMaterialTheme_Reminders, (datePicker, i, i1, i2) -> {
                                                                         Calendar lastSelectedDate = Calendar.getInstance();
                                                                         lastSelectedDate.setTime(mCalendar.getTime());
                                                                         mCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                                                                         mCalendar.set(Calendar.MONTH, datePicker.getMonth());
                                                                         mCalendar.set(Calendar.YEAR, datePicker.getYear());

                                                                         if(("history_creation_time").equalsIgnoreCase(DateAndTimePickerMaterial.this.getTag())){
                                                                             if(isPastDateSelected()){
                                                                                 Toast.makeText(mContext,"INVALID DATE SET",Toast.LENGTH_LONG).show();
                                                                                 mCalendar.setTime(lastSelectedDate.getTime());
                                                                             }else{
                                                                                 setDateMonthUI();
                                                                                 setYearUI();
                                                                             }
                                                                         }else {
                                                                             setDateMonthUI();
                                                                             setYearUI();
                                                                         }
                                                                     },mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH));


            datePickerDialog.show();
        }
    }

    private boolean isPastDateSelected() {
        PillpopperTime selectedDate = new PillpopperTime(mCalendar);
        return selectedDate.getLocalDay().before(mPillpopperTime.getLocalDay());
    }

    private void initDialogUi(View view) {
        mMainDialogDateYearHolderLinearLayout = view.findViewById(R.id.dialog_date_year_holder_linearlayout);
        mMainDialogDateTextView = mMainDialogDateYearHolderLinearLayout.findViewById(R.id.dialog_date_textview);
        mMainDialogYearTextView = mMainDialogDateYearHolderLinearLayout.findViewById(R.id.dialog_year_textview);
        mCustomTimePicker = view.findViewById(R.id.dialog_time_picker);

        this.mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(mPillpopperTime.getGmtMilliseconds());

        setDateMonthUI();
        setYearUI();

        mMainDialogDateYearHolderLinearLayout.setOnClickListener(this);

        Util.setTimePickerHourWrapper(mCustomTimePicker, mPillpopperTime.getLocalHourMinute().getHour());
        Util.setTimePickerMinuteWrapper(mCustomTimePicker, mPillpopperTime.getLocalHourMinute().getMinute());


    }

    private void setDateMonthUI() {
        StringBuilder dateMonthBuilder = new StringBuilder();
        dateMonthBuilder.append(getMonthNameString(mCalendar.get(Calendar.MONTH)));
        dateMonthBuilder.append(" ");
        dateMonthBuilder.append(mCalendar.get(Calendar.DAY_OF_MONTH));
        mMainDialogDateTextView.setText(dateMonthBuilder.toString());
    }

    private void setYearUI() {
        StringBuilder yearBuilder = new StringBuilder(", ");
        yearBuilder.append(mCalendar.get(Calendar.YEAR));
        mMainDialogYearTextView.setText(yearBuilder.toString());
    }

    private String getMonthNameString(int month) {
        switch (month + 1) {
            case 1: return "JAN";
            case 2: return "FEB";
            case 3: return "MAR";
            case 4: return "APR";
            case 5: return "MAY";
            case 6: return "JUN";
            case 7: return "JUL";
            case 8: return "AUG";
            case 9: return "SEP";
            case 10: return "OCT";
            case 11: return "NOV";
            case 12: return "DEC";
        }
        return null;
    }
}