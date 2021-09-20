package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

public class FrequencySpinnerAdapter extends ArrayAdapter<String> {
    private String[] spinnerData;
    LayoutInflater inflater;
    private Context context;
    private Typeface mRobotoMedium;

    public FrequencySpinnerAdapter(Context context, int resourseId, String[] frequency) {
        super(context, resourseId, frequency);
        this.context = context;
        spinnerData = frequency;
        mRobotoMedium = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_MEDIUM);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return spinnerData.length;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    public View getCustomView(int position, View convertView, final ViewGroup parent, boolean isDropDownView) {

        View row = convertView;
        if (row == null) {
            LayoutInflater lytInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = lytInflater.inflate(R.layout.custom_frequency_spinner_layout, parent, false);
        }
        TextView txt_memberName = row.findViewById(R.id.proxy_name_textview);
        txt_memberName.setText(spinnerData[position]);
        txt_memberName.setTypeface(mRobotoMedium);
        return row;
    }
}
