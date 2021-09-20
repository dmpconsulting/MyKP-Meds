package com.montunosoftware.pillpopper.android.interrupts.secretquestions;


import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionAnswerRequestModel;

import java.util.List;

public interface SecretQuestionsPresenter {

    void submitQuestionsAnswer(List<SecretQuestionAnswerRequestModel> model);

    void fetchQuesitonsList();
}
