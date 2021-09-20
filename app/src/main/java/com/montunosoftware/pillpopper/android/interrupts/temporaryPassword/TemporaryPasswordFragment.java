package com.montunosoftware.pillpopper.android.interrupts.temporaryPassword;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.interrupts.InterruptsActivity;
import com.montunosoftware.pillpopper.android.interrupts.OnConfirmClickListenerInterface;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemporaryPasswordFragment extends Fragment {

    private EditText mNewPassword;
    private EditText mConfirmPassword;
    private TextView mStrengthText;
    private InterruptsActivity mActivity;
    private TextView mStrengthBar;
    private OnConfirmClickListenerInterface mOnConfirmClickListener;
    private GenericAlertDialog mAlertDialog;
    private String strength = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_new_password, container, false);
        initUI(view);
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(mActivity, FireBaseConstants.ScreenEvent.SCREEN_INTERRUPT_TEMP_PWD);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (InterruptsActivity) context;
        mOnConfirmClickListener = (OnConfirmClickListenerInterface) context;
    }

    private void initUI(final View view) {

        Typeface mFontRegular = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_REGULAR);
        Typeface mFontMedium = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_MEDIUM);

        TextView mHeader = view.findViewById(R.id.create_password_title);
        mHeader.setTypeface(mFontRegular);

        TextView mSummaryText = view.findViewById(R.id.password_suggestion);
        mSummaryText.setTypeface(mFontRegular);

        TextView mNewPasswordHeader = view.findViewById(R.id.new_password_title);
        mNewPasswordHeader.setTypeface(mFontRegular);

        TextView mConfirmPasswordHeader = view.findViewById(R.id.confirm_password_title);
        mConfirmPasswordHeader.setTypeface(mFontRegular);

        mNewPassword = view.findViewById(R.id.new_password_edittext);
        mConfirmPassword = view.findViewById(R.id.confirm_password);
        Button mConfirmButton = view.findViewById(R.id.confirm_btn);
        mStrengthText = view.findViewById(R.id.password_strength_text);
        mStrengthText.setVisibility(View.GONE);
        mStrengthBar = view.findViewById(R.id.progress_strength_bar);
        mStrengthBar.setVisibility(View.GONE);

        mNewPassword.setTypeface(mFontRegular);
        mConfirmPassword.setTypeface(mFontRegular);
        mConfirmButton.setTypeface(mFontMedium);
        mStrengthText.setTypeface(mFontRegular);

        mNewPassword.addTextChangedListener(mWatcher);
        mConfirmButton.setOnClickListener(v -> {
            Util.hideKeyboard(mActivity, v);
            if(TextUtils.isEmpty(mNewPassword.getText().toString()) || TextUtils.isEmpty(mConfirmPassword.getText().toString())){
                if (TextUtils.isEmpty(mNewPassword.getText().toString())) {
                    showNoPasswordAlert("Please enter new password");
                }else {
                    showNoPasswordAlert("Please enter confirm password");
                }
                if (mAlertDialog != null && !mAlertDialog.isShowing()) {
                    mAlertDialog.showDialog();
                }
            }else {
                if (!areSamePasswords()) {
                    showNotMatchingAlert();
                } else {
                    mOnConfirmClickListener.onConfirmClick(getJsonRequest(), AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD);
                }
            }
        });
    }

    private void showNotMatchingAlert() {
        showNoPasswordAlert("Passwords do not match");
        if (mAlertDialog != null && !mAlertDialog.isShowing()) {
            mAlertDialog.showDialog();
        }
    }

    private boolean areSamePasswords() {
        return mNewPassword.getText().toString().equals(mConfirmPassword.getText().toString());
    }

    private String getJsonRequest() {
        JSONObject outerObject = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            JSONObject innerObject = new JSONObject();
            innerObject.put("fname", "password");
            innerObject.put("password", mNewPassword.getText().toString());
            array.put(innerObject);
            outerObject.put("requestMetaData", array);
        } catch (JSONException e) {
            LoggerUtils.exception(e.getMessage());
        }
        return outerObject.toString();
    }

    TextWatcher mWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkValidation();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void checkValidation() {

        if (mNewPassword.getText().toString().length() < 1) {
            mStrengthBar.setVisibility(View.GONE);
            mStrengthText.setVisibility(View.GONE);
        } else if (mNewPassword.getText().toString().length() >= 1) {
            mStrengthBar.setVisibility(View.VISIBLE);
            mStrengthText.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (mNewPassword.getText().toString().length() <= 7) {
                mLayoutParams = new RelativeLayout.LayoutParams(Util.convertToDp(80, mActivity), Util.convertToDp(4, mActivity));
                mStrengthBar.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.red_color));
                mStrengthText.setText(mActivity.getResources().getString(R.string.weak_text));
            } else if (mNewPassword.getText().toString().length() >= 8) {
                validatePassword();
                if (strength.equalsIgnoreCase(mActivity.getResources().getString(R.string.strong_text))) {
                    mLayoutParams = new RelativeLayout.LayoutParams(Util.convertToDp(308, mActivity), Util.convertToDp(4, mActivity));
                    mStrengthBar.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.green_color));
                } else if (strength.equalsIgnoreCase(mActivity.getResources().getString(R.string.moderate_text))) {
                    mLayoutParams = new RelativeLayout.LayoutParams(Util.convertToDp(152, mActivity), Util.convertToDp(4, mActivity));
                    mStrengthBar.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.amber_color));
                } else {
                    mLayoutParams = new RelativeLayout.LayoutParams(Util.convertToDp(80, mActivity), Util.convertToDp(4, mActivity));
                    mStrengthBar.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.red_color));
                }
                mStrengthText.setText(strength);
            }
            mLayoutParams.setMargins(Util.convertToDp(2, mActivity), 0, 0, Util.convertToDp(2, mActivity));
            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mStrengthBar.setLayoutParams(mLayoutParams);
        }
    }

    private void validatePassword() {

        String inputString = mNewPassword.getText().toString();
        boolean number = false;
        boolean lowerCase = false;
        boolean upperCase = false;
        boolean splChar = false;

        if (inputString.length() > 7) {
            Pattern pattern = Pattern.compile("[!@#$&*]");
            Matcher matcher = pattern.matcher(inputString);
            if (matcher.find()) {
                splChar = true;
            }
            for (Character c : inputString.toCharArray()) {
                if (Character.isDigit(c)) {
                    number = true;
                }

                if (Character.isUpperCase(c)) {
                    upperCase = true;
                }

                if (Character.isLowerCase(c)) {
                    lowerCase = true;
                }
            }

            if ((lowerCase || upperCase) && splChar && number) {
                strength = mActivity.getResources().getString(R.string.strong_text);
            } else if ((lowerCase || upperCase) && (splChar || number)) {
                strength = mActivity.getResources().getString(R.string.moderate_text);
            } else {
                strength = mActivity.getResources().getString(R.string.weak_text);
            }
        } else {
            strength = mActivity.getResources().getString(R.string.weak_text);
        }
    }

    private void showNoPasswordAlert(String message) {
        mAlertDialog = new GenericAlertDialog(mActivity, null, message, "Ok", (dialog, which) -> dialog.dismiss(), null, null);
    }
}
