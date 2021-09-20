package com.montunosoftware.pillpopper.android.interrupts.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Questions implements Serializable{
    @SerializedName("questionId")
    private String questionId;
    @SerializedName("groupId")
    private String groupId;
    @SerializedName("text")
    private String text;
    @SerializedName("textSp")
    private String textSp;
    @SerializedName("answer")
    private Object answer;

    private boolean isSelected = false;

    @SerializedName("questionId")
    public String getQuestionId() {
        return questionId;
    }

    @SerializedName("questionId")
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    @SerializedName("groupId")
    public String getGroupId() {
        return groupId;
    }

    @SerializedName("groupId")
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @SerializedName("text")
    public String getText() {
        return text;
    }

    @SerializedName("text")
    public void setText(String text) {
        this.text = text;
    }

    @SerializedName("textSp")
    public String getTextSp() {
        return textSp;
    }

    @SerializedName("textSp")
    public void setTextSp(String textSp) {
        this.textSp = textSp;
    }

    @SerializedName("answer")
    public Object getAnswer() {
        return answer;
    }

    @SerializedName("answer")
    public void setAnswer(Object answer) {
        this.answer = answer;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
