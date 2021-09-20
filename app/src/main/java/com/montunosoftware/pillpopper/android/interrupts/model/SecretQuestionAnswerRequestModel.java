package com.montunosoftware.pillpopper.android.interrupts.model;

public class SecretQuestionAnswerRequestModel {

    String questionId;
    String answerText;
    String groupId;
    String questionText;

    public SecretQuestionAnswerRequestModel() {
    }

    public String getQuestionText() {
        return questionText == null ? "" : questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionId() {
        return questionId;

    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
