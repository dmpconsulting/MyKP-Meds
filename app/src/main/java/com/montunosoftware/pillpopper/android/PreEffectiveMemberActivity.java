package com.montunosoftware.pillpopper.android;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.PreffectiveMemberLayoutBinding;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

public class PreEffectiveMemberActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreffectiveMemberLayoutBinding mPreffectiveMemberLayoutBinding = DataBindingUtil.setContentView(this, R.layout.preffective_member_layout);

        mPreffectiveMemberLayoutBinding.setRobotoMedium(ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM));
        mPreffectiveMemberLayoutBinding.setRobotoRegular(ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_REGULAR));
        mPreffectiveMemberLayoutBinding.setRobotoBold(ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_BOLD));

        mPreffectiveMemberLayoutBinding.visitKP.setOnClickListener(v ->
                Util.loadExternalBrowser(this, AppConstants.PRE_EFFECTIVE_MEMBER_URL));

        mPreffectiveMemberLayoutBinding.closeButton.setOnClickListener(v -> finish());
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(this, FireBaseConstants.ScreenEvent.SCREEN_PRE_EFFECTIVE_MEMBER_PROMPT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}