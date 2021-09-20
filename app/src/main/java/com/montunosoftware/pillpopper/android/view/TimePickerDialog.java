package com.montunosoftware.pillpopper.android.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.UIUtils;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

/**
 * @author
 * Created by adhithyaravipati on 9/30/16.
 */
public class TimePickerDialog extends DialogFragment
    implements DialogInterface.OnClickListener {

    public static final String BUNDLE_HOUR_OF_DAY = "TimePickerDialog.BUNDLE_HOUR_OF_DAY";
    public static final String BUNDLE_MINUTE = "TimePickerDialog.BUNDLE_MINUTE";
    public static final String BUNDLE_MINUTE_SPINNER_INTERVAL = "TimePickerDialog.BUNDLE_MINUTE_SPINNER_INTERVAL";
    public static final String BUNDLE_IS_24_HOUR = "TimePickerDialog.BUNDLE_IS_24_HOUR";
    public static final String BUNDLE_TITLE = "TimePickerDialog.BUNDLE_TITLE";

    private android.app.TimePickerDialog.OnTimeSetListener mOnTimeSetListener;
    private android.app.TimePickerDialog.OnDismissListener mOnDismissListener;

    private TimePicker mTimePicker;

    private int mTimePickerHour;
    private int mTimePickerMinute;
    private int mMinuteSpinnerInterval;

    private String mDialogTitle;

    private boolean mIs24Hour = false;

    public static void showDialog(Context context, String title ,int hourOfDay, int minute, boolean is24Hour, android.app.TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        TimePickerDialog timePickerDialog = new TimePickerDialog();
        timePickerDialog.setmOnTimeSetListener(onTimeSetListener);

        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_HOUR_OF_DAY, hourOfDay);
        bundle.putInt(BUNDLE_MINUTE, minute);
        bundle.putBoolean(BUNDLE_IS_24_HOUR, is24Hour);
        bundle.putString(BUNDLE_TITLE, title);

        timePickerDialog.setArguments(bundle);

        timePickerDialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "TimePickerDialog");
    }

    public static void showDialog(Context context,String title ,int hourOfDay, int minute, boolean is24Hour, android.app.TimePickerDialog.OnTimeSetListener onTimeSetListener, android.app.TimePickerDialog.OnDismissListener onDismissListener, int minuteSpinnerInterval) {
        TimePickerDialog timePickerDialog = new TimePickerDialog();
        timePickerDialog.setmOnTimeSetListener(onTimeSetListener);
        timePickerDialog.setmonDismissListener(onDismissListener);
        timePickerDialog.setCancelable(false);

        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_HOUR_OF_DAY, hourOfDay);
        bundle.putInt(BUNDLE_MINUTE, minute);
        bundle.putInt(BUNDLE_MINUTE_SPINNER_INTERVAL, minuteSpinnerInterval);
        bundle.putBoolean(BUNDLE_IS_24_HOUR, is24Hour);
        bundle.putString(BUNDLE_TITLE, title);

        timePickerDialog.setArguments(bundle);

        timePickerDialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "TimePickerDialog");
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context mContext = getContext();
        Bundle bundle = getArguments();

        if(bundle != null) {
            mTimePickerHour = bundle.getInt(BUNDLE_HOUR_OF_DAY, PillpopperTime.now().getLocalHourMinute().getHour());
            mTimePickerMinute = bundle.getInt(BUNDLE_MINUTE, PillpopperTime.now().getLocalHourMinute().getMinute());
            mMinuteSpinnerInterval = bundle.getInt(BUNDLE_MINUTE_SPINNER_INTERVAL, 0);
            mDialogTitle = bundle.getString(BUNDLE_TITLE, null);
        } else {
            mTimePickerHour = PillpopperTime.now().getLocalHourMinute().getHour();
            mTimePickerMinute = PillpopperTime.now().getLocalHourMinute().getMinute();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.KPBlueTimeDialogTheme);

        LayoutInflater dialogViewInflater = LayoutInflater.from(mContext);
        View mainDialogView = dialogViewInflater.inflate(R.layout.dialog_time_picker, null);

        TextView mDialogTitleTextView = mainDialogView.findViewById(R.id.dialog_time_picker_title);
        if(mDialogTitle != null) {
            mDialogTitleTextView.setText(mDialogTitle);
            mDialogTitleTextView.setVisibility(View.VISIBLE);
        } else {
            mDialogTitleTextView.setVisibility(View.GONE);
        }

        mTimePicker = mainDialogView.findViewById(R.id.dialog_time_picker);
        Util.setTimePickerHourWrapper(mTimePicker, mTimePickerHour);
        mTimePicker.setIs24HourView(mIs24Hour);

        if(mMinuteSpinnerInterval != 0
                && mMinuteSpinnerInterval != 1) {
            Util.setTimePickerMinuteWrapper(mTimePicker, mTimePickerMinute/mMinuteSpinnerInterval);
            UIUtils.applyDefaultTimePickerStyle(mContext, mTimePicker, mMinuteSpinnerInterval);
        } else {
            Util.setTimePickerMinuteWrapper(mTimePicker, mTimePickerMinute);
            UIUtils.applyDefaultTimePickerStyle(mContext, mTimePicker);
        }

        builder.setView(mainDialogView);

        builder.setCancelable(false);

        builder.setPositiveButton(mContext.getResources().getString(R.string._set), this);
        builder.setNegativeButton(mContext.getResources().getString(R.string.cancel_text), this);

        return builder.create();

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                mTimePicker.clearFocus();//Due to bug [date entered do not reflect on click of SET] with soft keyboard entry in timepicker
                mTimePickerHour = Util.getTimePickerHourWrapper(mTimePicker);
                mTimePickerMinute = Util.getTimePickerMinuteWrapper(mTimePicker);

                if(mMinuteSpinnerInterval != 0
                        && mMinuteSpinnerInterval != 1) {
                    mTimePickerMinute = mTimePickerMinute *mMinuteSpinnerInterval;
                }
                if (null != mOnTimeSetListener) {
                    mOnTimeSetListener.onTimeSet(mTimePicker, mTimePickerHour, mTimePickerMinute);
                }
                dialog.dismiss();
                break;

            case AlertDialog.BUTTON_NEGATIVE:
                if (null != mOnDismissListener) {
                    mOnDismissListener.onDismiss(dialog);
                }
                dialog.dismiss();
                break;
        }

    }

    private void initDialogTitle() {

    }

    public void setmOnTimeSetListener(android.app.TimePickerDialog.OnTimeSetListener mOnTimeSetListener) {
        this.mOnTimeSetListener = mOnTimeSetListener;
    }

    public void setmonDismissListener(android.app.TimePickerDialog.OnDismissListener mOnDismissListener) {
        this.mOnDismissListener = mOnDismissListener;
    }

    @Override
    public void onPause() {
        try {
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("TimePickerDialog");
            if (prev != null) {
                TimePickerDialog timePickerDialog = (TimePickerDialog) prev;
                timePickerDialog.dismiss();
            }
        } catch (Exception ex){
            LoggerUtils.error(ex.getMessage());
        }
        super.onPause();
    }
}
