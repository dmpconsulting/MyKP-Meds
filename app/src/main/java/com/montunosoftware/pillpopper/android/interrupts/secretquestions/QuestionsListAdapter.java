package com.montunosoftware.pillpopper.android.interrupts.secretquestions;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.interrupts.model.Questions;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.List;

public class QuestionsListAdapter extends RecyclerView.Adapter<QuestionsListAdapter.QuestionViewHolder> {

    private List<Questions> mQuestionsList;
    private int mLastCheckedPosition = -1;

    QuestionSelectedListener listener;

    private Typeface mFontRegular;

    public QuestionsListAdapter(List<Questions> questionsList, Context mContext, QuestionSelectedListener listener) {
        this.mQuestionsList = questionsList;
        this.listener = listener;

        mFontRegular = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_REGULAR);
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_row_item, parent, false);

        return new QuestionViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull final QuestionViewHolder holder, final int position) {
        holder.radioButton.setText(mQuestionsList.get(position).getText());

        if (Integer.parseInt(mQuestionsList.get(position).getQuestionId()) == mLastCheckedPosition) {
            holder.radioButton.setChecked(true);
        } else {
            holder.radioButton.setChecked(false);
        }
        holder.radioButton.setOnClickListener(v -> {
            mLastCheckedPosition = Integer.parseInt(mQuestionsList.get(holder.getAdapterPosition()).getQuestionId());
            notifyItemRangeChanged(0, mQuestionsList.size());

            listener.selectedQuestion(holder.getAdapterPosition(), getSelectedItem());

        });
    }


    public Questions getSelectedItem() {
        for (Questions model: mQuestionsList){
            if (Integer.parseInt(model.getQuestionId())== mLastCheckedPosition){
                return model;
            }
        }
        return null;
    }

    public int selectedPosition() {
        return mLastCheckedPosition;
    }


    @Override
    public int getItemCount() {
        return mQuestionsList.size();
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {

        RadioButton radioButton;

        //TextView textView;
        public QuestionViewHolder(View itemView) {
            super(itemView);

            radioButton = itemView.findViewById(R.id.radio_button);
            radioButton.setTypeface(mFontRegular);

        }
    }


    public interface QuestionSelectedListener {
        void selectedQuestion(int position, Questions model);
    }
}
