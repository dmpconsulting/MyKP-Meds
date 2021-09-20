package com.montunosoftware.pillpopper.android.interrupts.secretquestions;


import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionsResponse;

public interface SecretQuestionsView {

    void validateQuestion1(String msg);

    void validateAnswer1(String msg);

    void getQuestionsData(SecretQuestionsResponse response);

    void showAppProfileUrlNotFoundError();
}
