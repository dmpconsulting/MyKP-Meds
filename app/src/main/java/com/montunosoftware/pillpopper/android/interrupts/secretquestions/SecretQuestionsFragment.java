package com.montunosoftware.pillpopper.android.interrupts.secretquestions;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.interrupts.InterruptsActivity;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionAnswerRequestModel;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionsResponse;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 *
 */
public class SecretQuestionsFragment extends Fragment implements SecretQuestionsView, View.OnClickListener {

    private List<SecretQuestionAnswerRequestModel> mRequestModelList;

    private static final int QUESTION_ONE_REQUEST_CODE = 1;
    private static final int QUESTION_TWO_REQUEST_CODE = 2;
    private static final int QUESTION_THREE_REQUEST_CODE = 3;

    private TextView mTvQuestion1;
    private TextView mTvQuestion2;
    private TextView mTvQuestion3;
    private TextView mTVQuestionHelpMsg;

    private EditText mEditAnswer1;
    private EditText mEditAnswer2;
    private EditText mEditAnswer3;
    private Button mBtnSave;
    private LinearLayout mErrorLayout;
    private ScrollView mScrollView;

    private SecretQuestionsPresenterIntractor mPresenter;
    private InterruptsActivity mActivity;
    private SecretQuestionAnswerRequestModel mRequestModelForQ1, mRequestModelForQ2, mRequestModelForQ3;

    private SecretQuestionsResponse mResponse;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (InterruptsActivity) context;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestModelList = new ArrayList<>();
        mRequestModelForQ1 = new SecretQuestionAnswerRequestModel();
        mRequestModelForQ3 = new SecretQuestionAnswerRequestModel();
        mRequestModelForQ2 = new SecretQuestionAnswerRequestModel();

        mPresenter = new SecretQuestionsPresenterIntractor(this, mActivity);

        FireBaseAnalyticsTracker.getInstance().logScreenEvent(mActivity, FireBaseConstants.ScreenEvent.SCREEN_INTERRUPT_SECRET_QUESTIONS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_secret_questions, container, false);
        initUI(view);
        return view;
    }

    @Override
    public void showAppProfileUrlNotFoundError() {
        getActivity().finish();
    }

    private void initUI(View view) {

        Typeface mFontRegular = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_REGULAR);
        Typeface mFontMedium = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_MEDIUM);
        Typeface mFontBold = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_BOLD);

        mErrorLayout = view.findViewById(R.id.errorLayout);
        mScrollView = view.findViewById(R.id.scroll_view);
        RelativeLayout question1Layout = view.findViewById(R.id.question1Layout);
        RelativeLayout question2Layout = view.findViewById(R.id.question2Layout);
        RelativeLayout question3Layout = view.findViewById(R.id.question3Layout);

        TextView tv_errorHeader = view.findViewById(R.id.tv_errorHeader);
        TextView tv_errorMsgWithBullets = view.findViewById(R.id.tv_errorMsgWithBullets);

        tv_errorHeader.setTypeface(mFontBold);
        tv_errorMsgWithBullets.setTypeface(mFontRegular);

        TextView mTV_questionTitle = view.findViewById(R.id.tv_secretQuestionsTitle);
        mTVQuestionHelpMsg = view.findViewById(R.id.tv_questionsMsg);

        TextView tv_question1PlaceHolder = view.findViewById(R.id.tv_question1PlaceHolder);
        TextView tv_answer1PlaceHolder = view.findViewById(R.id.tv_answer1PlaceHolder);

        TextView tv_question2PlaceHolder = view.findViewById(R.id.tv_question2PlaceHolder);
        TextView tv_answer2PlaceHolder = view.findViewById(R.id.tv_answer2PlaceHolder);

        TextView tv_question3PlaceHolder = view.findViewById(R.id.tv_question3PlaceHolder);
        TextView tv_answer3PlaceHolder = view.findViewById(R.id.tv_answer3PlaceHolder);

        mTvQuestion1 = view.findViewById(R.id.tv_question1);
        mTvQuestion2 = view.findViewById(R.id.tv_question2);
        mTvQuestion3 = view.findViewById(R.id.tv_question3);

        mEditAnswer1 = view.findViewById(R.id.edit_answer1);
        mEditAnswer2 = view.findViewById(R.id.edit_answer2);
        mEditAnswer3 = view.findViewById(R.id.edit_answer3);
        mBtnSave = view.findViewById(R.id.btn_save);

        mTV_questionTitle.setTypeface(mFontRegular);
        mTVQuestionHelpMsg.setTypeface(mFontRegular);
        tv_question1PlaceHolder.setTypeface(mFontRegular);
        tv_answer1PlaceHolder.setTypeface(mFontRegular);

        tv_question2PlaceHolder.setTypeface(mFontRegular);
        tv_answer2PlaceHolder.setTypeface(mFontRegular);

        tv_question3PlaceHolder.setTypeface(mFontRegular);
        tv_answer3PlaceHolder.setTypeface(mFontRegular);

        mTvQuestion1.setTypeface(mFontRegular);
        mEditAnswer1.setTypeface(mFontRegular);

        mTvQuestion2.setTypeface(mFontRegular);
        mEditAnswer2.setTypeface(mFontRegular);

        mTvQuestion3.setTypeface(mFontRegular);
        mEditAnswer3.setTypeface(mFontRegular);

        mBtnSave.setTypeface(mFontMedium);

        mBtnSave.setOnClickListener(this);

        mTvQuestion1.setOnClickListener(this);
        question1Layout.setOnClickListener(this);

        mTvQuestion2.setOnClickListener(this);
        question2Layout.setOnClickListener(this);

        mTvQuestion3.setOnClickListener(this);
        question3Layout.setOnClickListener(this);

        mBtnSave.setEnabled(false);

        mEditAnswer1.addTextChangedListener(mWatcher);
        mEditAnswer2.addTextChangedListener(mWatcher);
        mEditAnswer3.addTextChangedListener(mWatcher);

        mPresenter.fetchQuesitonsList();
    }

    private void replaceFragment(Fragment fragment, int requestCode) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        fragment.setTargetFragment(SecretQuestionsFragment.this, requestCode);
        ft.add(R.id.fragment_container, fragment, fragment.getTag());
        ft.hide(SecretQuestionsFragment.this);
        ft.addToBackStack(SecretQuestionsFragment.class.getName());
        ft.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == QUESTION_ONE_REQUEST_CODE) {
                mRequestModelForQ1.setQuestionId(data.getStringExtra("questionId"));
                mRequestModelForQ1.setGroupId(data.getStringExtra("groupId"));
                //String addID = data.getStringExtra("questionId");
                String questionText = data.getStringExtra("questionText");
                mTvQuestion1.setText(questionText);
                mEditAnswer1.setFocusableInTouchMode(true);
                mEditAnswer1.requestFocus();

            } else if (requestCode == QUESTION_TWO_REQUEST_CODE) {
                mRequestModelForQ2.setQuestionId(data.getStringExtra("questionId"));
                mRequestModelForQ2.setGroupId(data.getStringExtra("groupId"));
                String questionText = data.getStringExtra("questionText");
                mTvQuestion2.setText(questionText);
                mEditAnswer2.setFocusableInTouchMode(true);
                mEditAnswer2.requestFocus();

            } else if (requestCode == QUESTION_THREE_REQUEST_CODE) {
                mRequestModelForQ3.setQuestionId(data.getStringExtra("questionId"));
                mRequestModelForQ3.setGroupId(data.getStringExtra("groupId"));
                String questionText = data.getStringExtra("questionText");
                mTvQuestion3.setText(questionText);
                mEditAnswer3.setFocusableInTouchMode(true);
                mEditAnswer3.requestFocus();
            }

            checkValidation();
        }
    }

    @Override
    public void validateQuestion1(String msg) {

        Util.hideSoftKeyboard(mActivity);
        new Handler().postDelayed(() -> {
            mScrollView.smoothScrollTo(0, mTVQuestionHelpMsg.getBottom());
            mErrorLayout.setVisibility(View.VISIBLE);
        }, 300);
    }

    @Override
    public void validateAnswer1(String msg) {

        Util.hideSoftKeyboard(mActivity);
        new Handler().postDelayed(() -> {
            mScrollView.smoothScrollTo(0, mTVQuestionHelpMsg.getBottom());
            mErrorLayout.setVisibility(View.VISIBLE);
        }, 300);

    }

    @Override
    public void getQuestionsData(SecretQuestionsResponse response) {
        this.mResponse = response;
    }

    @Override
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        Fragment fragment = new QuestionsListFragment();
        switch (view.getId()) {
            case R.id.tv_question1:
            case R.id.question1Layout:
                mTvQuestion1.setNextFocusDownId(R.id.edit_answer1);
                Util.hideKeyboard(mActivity, mEditAnswer1);
                bundle.putSerializable("groupQuestions", (Serializable) mResponse.getSecretQuestionsMap().get("1"));
                fragment.setArguments(bundle);
                replaceFragment(fragment, QUESTION_ONE_REQUEST_CODE);
                break;
            case R.id.tv_question2:
            case R.id.question2Layout:
                mTvQuestion2.setNextFocusDownId(R.id.edit_answer2);
                Util.hideKeyboard(mActivity, mEditAnswer2);
                bundle.putSerializable("groupQuestions", (Serializable) mResponse.getSecretQuestionsMap().get("2"));
                fragment.setArguments(bundle);
                replaceFragment(fragment, QUESTION_TWO_REQUEST_CODE);
                break;
            case R.id.tv_question3:
            case R.id.question3Layout:
                mTvQuestion3.setNextFocusDownId(R.id.edit_answer3);
                Util.hideKeyboard(mActivity, mEditAnswer3);
                bundle.putSerializable("groupQuestions", (Serializable) mResponse.getSecretQuestionsMap().get("3"));
                fragment.setArguments(bundle);
                replaceFragment(fragment, QUESTION_THREE_REQUEST_CODE);
                break;
            case R.id.btn_save:
                mRequestModelForQ1.setAnswerText(mEditAnswer1.getText().toString().trim());
                mRequestModelForQ2.setAnswerText(mEditAnswer2.getText().toString().trim());
                mRequestModelForQ3.setAnswerText(mEditAnswer3.getText().toString().trim());

                mRequestModelList = new ArrayList<>();
                mRequestModelList.add(mRequestModelForQ1);
                mRequestModelList.add(mRequestModelForQ2);
                mRequestModelList.add(mRequestModelForQ3);

                mPresenter.submitQuestionsAnswer(mRequestModelList);
                break;
        }
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
        if ((((TextUtils.isEmpty(mEditAnswer1.getText())) || mEditAnswer1.getText().toString().trim().length() < 2)
                || (TextUtils.isEmpty(mEditAnswer2.getText()) || mEditAnswer2.getText().toString().trim().length() < 2)
                || (TextUtils.isEmpty(mEditAnswer3.getText()) || mEditAnswer3.getText().toString().trim().length() < 2))
                || ((null == mRequestModelForQ1.getQuestionId())
                || (null == mRequestModelForQ2.getQuestionId())
                || (null == mRequestModelForQ3.getQuestionId()))) {
            mBtnSave.setEnabled(false);
            mBtnSave.setBackground(mActivity.getDrawable(R.drawable.save_round_button_style));
            mBtnSave.setTextColor(ContextCompat.getColor(getActivity(), R.color.kp_theme_blue));
        } else {
            mBtnSave.setEnabled(true);
            mBtnSave.setBackground(mActivity.getDrawable(R.drawable.blue_round_button_style));
            mBtnSave.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }
}
