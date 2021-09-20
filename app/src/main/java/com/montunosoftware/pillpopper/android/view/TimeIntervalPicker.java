package com.montunosoftware.pillpopper.android.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import androidx.fragment.app.DialogFragment;

import com.montunosoftware.mymeds.R;

public class TimeIntervalPicker extends DialogFragment implements DialogInterface.OnClickListener
{

	private HourMinutePickedListener mDialogListener;
	private Context mContext;
	private NumberPicker mNPHours;
	private NumberPicker mNPMin;
	private String[] mMinIntervals;
	private int mIntervalArray;
	private String mDialogHeader;
	private OnClickListener negetiveClick=null;

	private long initialSetTime = -1;
	

	private int tempMinVal;
	private int tempHourVal;
	
	public void setInitTime(long setTime){
		initialSetTime = setTime;
	}
	
	
	public void setNegetiveActionListener(OnClickListener lstnr){
		negetiveClick = lstnr;
	}

	
	public interface HourMinutePickedListener
	{
		void onPostponeDialogPositiveClick(int[] hhmm);
	}

	public TimeIntervalPicker(){

	}

	@SuppressLint("ValidFragment")
	public TimeIntervalPicker(Context context, String header, HourMinutePickedListener callback, int intervalArrayRef)
	{
		mDialogListener = callback;
		mContext = context;
		mIntervalArray = intervalArrayRef;
		mDialogHeader = header;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(mDialogHeader);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.postpone_by_dialog, new LinearLayout(mContext), false);
		builder.setView(dialogView).setPositiveButton(R.string.setAlert, this).setNegativeButton(R.string.cancelAlert, this);
		initAndloadValuesforNumberPickers(dialogView);
		return builder.create();
	}

	private void initAndloadValuesforNumberPickers(View dialogView)
	{
		mMinIntervals = getResources().getStringArray(mIntervalArray);

		mNPHours = dialogView.findViewById(R.id.npHours);
		mNPHours.setMinValue(0);
		mNPHours.setMaxValue(1);
		mNPHours.setValue(0);
		mNPHours.setWrapSelectorWheel(false);

		mNPMin = dialogView.findViewById(R.id.npMins);
		mNPMin.setDisplayedValues(mMinIntervals);
		mNPMin.setMinValue(0);
		mNPMin.setMaxValue(2);
		mNPMin.setValue(1);
		mNPMin.setWrapSelectorWheel(false);

		mNPMin.setOnValueChangedListener(HourPickerListener);
		mNPHours.setOnValueChangedListener(HourPickerListener);

		mNPHours.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
		mNPMin.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
		
		if (initialSetTime>=0) {
			long hours = initialSetTime/3600;
			long min = (initialSetTime-(hours*3600))/60;
			
			min = (int) (min/15);
			
			mNPMin.setValue((int) min);
			mNPHours.setValue((int) hours);

			tempHourVal = (int) hours;
			tempMinVal = (int) min;
			
		}else {
			tempHourVal = 0;
			tempMinVal = 1;
		}
	}

	private OnValueChangeListener HourPickerListener = new OnValueChangeListener() {
		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal)
		{
			if (picker==mNPHours) {
				if (newVal==0 && tempMinVal==0) {
					tempHourVal =newVal;
					tempMinVal = 1;
					mNPMin.setValue(1);
				}else if (tempMinVal!=0 && newVal!=0) {
					mNPMin.setValue(0);
					tempMinVal=0;
					tempHourVal=1;
				}else {
					tempHourVal =newVal;
				}
			}else {
				if (newVal==0 && tempHourVal==0) {
					tempHourVal =newVal;
					tempMinVal = 1;
					mNPMin.setValue(1);
				}else if (tempHourVal!=0 && newVal !=0) {
					mNPHours.setValue(0);
					tempHourVal = 0;
					tempMinVal=newVal;
				}else {
					tempMinVal = newVal;
				}
			}
		}
	};

	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		switch (which) {
		case AlertDialog.BUTTON_POSITIVE:
			int[] hhmm = new int[2];

			hhmm[0] = mNPHours.getValue();
			hhmm[1] = Integer.parseInt(mNPMin.getDisplayedValues()[mNPMin.getValue()]);
			
			mDialogListener.onPostponeDialogPositiveClick(hhmm);
			break;
		case AlertDialog.BUTTON_NEGATIVE:
			if (negetiveClick!=null) {
				negetiveClick.onClick(dialog, which);
			}
			dialog.dismiss();
			break;
		}
	}
}
