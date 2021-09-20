package com.montunosoftware.pillpopper.android.interrupts.secretquestions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.interrupts.InterruptsActivity;
import com.montunosoftware.pillpopper.android.interrupts.model.Questions;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class QuestionsListFragment extends Fragment implements QuestionsListAdapter.QuestionSelectedListener {

    private InterruptsActivity mActivity;
    private QuestionsListAdapter mAdapter;
    private TextView mTvChooseAQuestion;
    private List<Questions> mListOfQuestions;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (InterruptsActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( null != getArguments() && null != getArguments().getSerializable("groupQuestions")) {
            mListOfQuestions = (List<Questions>) getArguments().getSerializable("groupQuestions");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_questions, container, false);

        Util.hideKeyboard(mActivity, view);
        RecyclerView mRecyclerViewQuestions = view.findViewById(R.id.recycler_view_questionsList);
        mTvChooseAQuestion = view.findViewById(R.id.tv_chooseAQuestion);
        mTvChooseAQuestion.setTypeface(ActivationUtil.setFontStyle(mActivity, AppConstants.FONT_ROBOTO_REGULAR));

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mRecyclerViewQuestions.setLayoutManager(mLayoutManager);
        mRecyclerViewQuestions.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new QuestionsListAdapter(mListOfQuestions, mActivity, this);
        mRecyclerViewQuestions.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void selectedQuestion(int position, Questions model) {
        Intent intent = new Intent(mActivity, QuestionsListFragment.class);
        intent.putExtra("questionId", model.getQuestionId());
        intent.putExtra("questionText", model.getText());
        intent.putExtra("groupId", model.getGroupId());
        intent.putExtra("text", model.getText());
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
    }
}