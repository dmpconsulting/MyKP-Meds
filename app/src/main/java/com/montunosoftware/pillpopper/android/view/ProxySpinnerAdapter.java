package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.model.User;

import java.util.List;

public class ProxySpinnerAdapter extends ArrayAdapter<User> {

    private List<User> spinnerData;
    LayoutInflater inflater;
    private Context context;

    public ProxySpinnerAdapter(Context context, int resourseId, List<User> usersList) {
        super(context, resourseId, usersList);
        this.context = context;
        spinnerData = usersList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return spinnerData.size();
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
            row = lytInflater.inflate(R.layout.user_spinner_item, parent, false);
        }
        User user = spinnerData.get(position);
        TextView txt_memberName = row.findViewById(R.id.proxy_name_textview);
        txt_memberName.setText(user.getFirstName());
        return row;
    }
}
