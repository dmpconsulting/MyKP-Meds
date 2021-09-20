package com.montunosoftware.pillpopper.android.interrupts.email;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.interrupts.OnConfirmClickListenerInterface;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.regex.Matcher;

public class EmailMismatchFragment extends Fragment implements View.OnClickListener {

    private Button mConfirmBtn;
    private EditText mEmailAddress;
    private GenericAlertDialog mAlertDialog;
    private OnConfirmClickListenerInterface mOnConfimClickListener;
    private Bundle mBundle;
    private TextView mTitle;
    private TextView mStayInTouchDesc;
    private String mInterruptType;

    public EmailMismatchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments()) {
            mBundle  = getArguments();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.email_mismatch_layout, container, false);
        mTitle = view.findViewById(R.id.confirm_email_title);
        mStayInTouchDesc = view.findViewById(R.id.confirm_email_desc);
        mConfirmBtn = view.findViewById(R.id.confirm_btn);
        mConfirmBtn.setOnClickListener(this);
        if(null != mBundle && null != mBundle.getString("mSignOnInterruptType")) {
            mInterruptType = mBundle.getString("mSignOnInterruptType");
            if (AppConstants.SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH.equalsIgnoreCase(mInterruptType)) {
                mTitle.setText(getResources().getString(R.string.contact_email));
                mStayInTouchDesc.setText(getResources().getString(R.string.contact_email_desc));
                mStayInTouchDesc.setVisibility(View.VISIBLE);
                mConfirmBtn.setText(getResources().getString(R.string.submit));
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_INTERRUPT_STAY_IN_TOUCH);
            } else {
                mStayInTouchDesc.setVisibility(View.GONE);
                mConfirmBtn.setText(getResources().getString(R.string.btn_confirm));
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_INTERRUPT_EMAIL_MISMATCH);
            }
        }else{
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_INTERRUPT_EMAIL_MISMATCH);
            mStayInTouchDesc.setVisibility(View.GONE);
            mConfirmBtn.setText(getResources().getString(R.string.btn_confirm));
        }

        mEmailAddress = view.findViewById(R.id.email_address);
        if (null != RunTimeData.getInstance().getUserResponse()) {
            if(null != RunTimeData.getInstance().getUserResponse().getEpicEmail()) {
                mEmailAddress.setText(RunTimeData.getInstance().getUserResponse().getEpicEmail());
                mEmailAddress.setSelection(RunTimeData.getInstance().getUserResponse().getEpicEmail().length());
            } else{
                /*
                Email should be populated in case of null kpEmail only in case of STAY_IN_TOUCH so is the check for interrupt type,
                since same screen is used for the email mismatch.
                 */
                try {
                    if (AppConstants.SIGNON_RESPONSE_INTERRUPT_STAY_IN_TOUCH.equalsIgnoreCase(mInterruptType) &&
                            null != RunTimeData.getInstance().getUserResponse().getEmail()) {

                        mEmailAddress.setText(RunTimeData.getInstance().getUserResponse().getEmail());
                        mEmailAddress.setSelection(RunTimeData.getInstance().getUserResponse().getEmail().length());
                    }
                } catch (Exception npe) {
                    mEmailAddress.setText("");
                    LoggerUtils.exception(npe.getMessage());
                }
            }
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnConfimClickListener = (OnConfirmClickListenerInterface) context;
    }

    @Override
    public void onClick(View v) {
        String emailAddress = mEmailAddress.getText().toString();
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(emailAddress);
        if(!matcher.matches()){
            mAlertDialog = new GenericAlertDialog(getActivity(), null, getString(R.string.enter_valid_email_text), getString(R.string.ok_text), (dialog, which) -> dialog.dismiss(), null, null);

            if (mAlertDialog != null && !mAlertDialog.isShowing()) {
                mAlertDialog.showDialog();
            }
        } else {
            mOnConfimClickListener.onConfirmClick(emailAddress, mInterruptType);
        }
    }
}
