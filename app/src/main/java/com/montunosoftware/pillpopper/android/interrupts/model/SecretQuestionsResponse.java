package com.montunosoftware.pillpopper.android.interrupts.model;


import java.util.List;
import java.util.Map;

public class SecretQuestionsResponse {

    private Map<String, List<Questions>> secretQuestionsMap;

    public Map<String, List<Questions>> getSecretQuestionsMap() {
        return secretQuestionsMap;
    }

    public void setSecretQuestionsMap(Map<String, List<Questions>> secretQuestionsMap) {
        this.secretQuestionsMap = secretQuestionsMap;
    }
}
