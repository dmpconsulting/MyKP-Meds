package com.montunosoftware.pillpopper.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.montunosoftware.mymeds.R;

import org.w3c.dom.Text;

/**
 * @author
 * Created by M1025283 on 5/5/2016.
 */
public class ReminderSnoozePicker extends DialogFragment implements View.OnClickListener {

    private Button mBtnDialogDismiss;
    private HourMinutePickedListener mDialogListener;
    int[] hhmm = new int[2];

    public void setHourMinutePickedListener(HourMinutePickedListener mDialogListener){
        this.mDialogListener = mDialogListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.remind_me_dialog, container);

        TextView txtViewMinOne = view.findViewById(R.id.txtSnoozetimeOne);
        TextView txtViewMinTwo = view.findViewById(R.id.txtSnoozetimeTwo);
        TextView txtViewMinThree = view.findViewById(R.id.txtSnoozetimeThree);
        TextView txtViewMinFour = view.findViewById(R.id.txtSnoozetimeFour);
        mBtnDialogDismiss = view.findViewById(R.id.btn_snooze_cancel);

        txtViewMinOne.setOnClickListener(this);
        txtViewMinTwo.setOnClickListener(this);
        txtViewMinThree.setOnClickListener(this);
        txtViewMinFour.setOnClickListener(this);
        mBtnDialogDismiss.setOnClickListener(this);
        setCancelable(false);

        return view;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btn_snooze_cancel){
            dismiss();
        }

        if(v.getId() == R.id.txtSnoozetimeOne){
            hhmm[0] = 0;
            hhmm[1] = 15;
            onPostponeOptionSelected(hhmm);
        }
        if(v.getId() == R.id.txtSnoozetimeTwo){
            hhmm[0] = 0;
            hhmm[1] = 30;
            onPostponeOptionSelected(hhmm);
        }
        if(v.getId() == R.id.txtSnoozetimeThree){
            hhmm[0] = 0;
            hhmm[1] = 45;
            onPostponeOptionSelected(hhmm);
        }
        if(v.getId() == R.id.txtSnoozetimeFour){
            hhmm[0] = 1;
            hhmm[1] = 0;
            onPostponeOptionSelected(hhmm);
        }
    }

    private void onPostponeOptionSelected(int[] hhmm) {
        if (null != mDialogListener) {
            mDialogListener.onPostponeDialogPositiveClick(hhmm);
        }
        if (null != getDialog()) {
            getDialog().dismiss();
        }
    }

    public interface HourMinutePickedListener
    {
        void onPostponeDialogPositiveClick(int[] hhmm);
    }


}
