package com.montunosoftware.pillpopper.android;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

/**
 * @author Created by M1032185 on 7/8/2016.
 */
public class SupportFragmenBaseScreen extends Fragment implements View.OnClickListener {

    private View mView;

    private LinearLayout mLayoutAppSupport;

    private LinearLayout mLayoutAppointments;

    private LinearLayout mLayoutMedication;

    private TextView mTvPrivacyStatement;

    private TextView mTvTC;

    private TextView mAppsNotes;


    private TextView mTvVersion;
    ReminderListenerInterfaces mReminderShowListener;

    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.med_support_fragment, container, false);

        initUI();
        loadVersion();

        mLayoutAppSupport.setOnClickListener(this);
        mLayoutAppointments.setOnClickListener(this);
        mLayoutMedication.setOnClickListener(this);
        mTvPrivacyStatement.setOnClickListener(this);
        mTvTC.setOnClickListener(this);
        mAppsNotes.setText(getString(R.string.appointment_advice_mid));

        return mView;
    }

    private void loadVersion() {
        try {
            mTvVersion.setText(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            LoggerUtils.exception("PackageManager.NameNotFoundException", e);
        }
    }

    private void initUI() {
        mLayoutAppSupport = mView.findViewById(R.id.app_support_row);
        mLayoutAppointments = mView.findViewById(R.id.appointments_row);
        mLayoutMedication = mView.findViewById(R.id.medications_row);
        LinearLayout mlayoutJenkins = mView.findViewById(R.id.jenkins_row);
        mTvPrivacyStatement = mView.findViewById(R.id.txt_privacy_statement);
        mTvTC = mView.findViewById(R.id.txt_term_condition);
        mAppsNotes = mView.findViewById(R.id.txt_appointments_note);
        mTvVersion = mView.findViewById(R.id.tv_version);
        mProgressBar = mView.findViewById(R.id.progressBar);
        TextView mTvJenkins_build_no = mView.findViewById(R.id.tv_jenkins_build_no);

        mTvJenkins_build_no.setText(getString(R.string.jenkins_build_number));

        if (Util.isProductionBuild()) {
            mlayoutJenkins.setVisibility(View.GONE);
        } else {
            mlayoutJenkins.setVisibility(View.VISIBLE);
        }
        Spannable wordtoSpan = new SpannableString(getString(R.string.appointment_advice_phone));
        wordtoSpan.setSpan(new UnderlineSpan(), 0, wordtoSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mAppsNotes.setText(Html.fromHtml(getString(R.string.appointment_advice)));

    }

    @Override
    public void onResume() {
        super.onResume();
        mReminderShowListener.showReminder(true);
        RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mReminderShowListener = (ReminderListenerInterfaces) context;
        } catch (ClassCastException e) {
            PillpopperLog.say(context.toString() + " must implement ReminderListenerInterfaces", e);
            throw new ClassCastException(context.toString() + " must implement ReminderListenerInterfaces");
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (!(mProgressBar.getVisibility() == View.VISIBLE)) {
            if (viewId == R.id.app_support_row) {
                launchActivity(getResources().getString(R.string.lbl_app_support));
            } else if (viewId == R.id.appointments_row) {
                launchActivity(getResources().getString(R.string.lbl_appointments_and_advice));
            } else if (viewId == R.id.medications_row) {
                launchActivity(getResources().getString(R.string._pharmacy));
            } else if (viewId == R.id.txt_privacy_statement) {
                launchActivity(getResources().getString(R.string.lbl_privacy_statement));
            } else if (viewId == R.id.txt_term_condition) {
                launchActivity(getResources().getString(R.string.lbl_term_and_conditions));
            }
        }
    }

    private void launchActivity(String intentValue) {
        Intent i = new Intent(getContext(), PrivacyAndTC.class);
        i.putExtra("url", intentValue);
        startActivity(i);
    }

}
