package com.montunosoftware.pillpopper.android.interrupts.secretquestions;

import android.content.Context;

import com.montunosoftware.pillpopper.android.interrupts.OnConfirmClickListenerInterface;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionAnswerRequestModel;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionsResponse;
import com.montunosoftware.pillpopper.android.interrupts.service.GetSecretQuestionsListAsyncTask;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecretQuestionsPresenterIntractor implements SecretQuestionsPresenter, GetSecretQuestionsListAsyncTask.GetQuestionsListener{

    private SecretQuestionsView view;
    private Context mContext;
    private OnConfirmClickListenerInterface listener;

    public SecretQuestionsPresenterIntractor(SecretQuestionsView view, Context context) {
        this.view = view;
        this.mContext = context;
        listener = (OnConfirmClickListenerInterface) context;
    }

    @Override
    public void submitQuestionsAnswer(List<SecretQuestionAnswerRequestModel> model) {

        for (SecretQuestionAnswerRequestModel answerModel : model) {
            if (null == answerModel.getQuestionId()) {
                view.validateQuestion1("");
                return;
            }

            if (!getSpecialCharacterCount(answerModel.getAnswerText())) {
                view.validateAnswer1("");
                return;
            }

        }

        LoggerUtils.info(model.toString());
        String requestData = prepareRequestJson(model);
        listener.onConfirmClick(requestData, AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS);

    }

    private String prepareRequestJson(List<SecretQuestionAnswerRequestModel> model) {

        JSONObject rootObject = new JSONObject();
        try {

            JSONArray jsonArray = new JSONArray();
            for (SecretQuestionAnswerRequestModel requestModel : model) {
                JSONObject innerObject = new JSONObject();
                innerObject.put("questionId", requestModel.getQuestionId());
                innerObject.put("groupId", requestModel.getGroupId());
                innerObject.put("answer", requestModel.getAnswerText());
//                innerObject.put("text", requestModel.getQuestionText());
                jsonArray.put(innerObject);
            }
            rootObject.put("secretQuestions", jsonArray);
        } catch (JSONException e) {
            LoggerUtils.info(e.getMessage());
        }
    return rootObject.toString();
    }

    private boolean getSpecialCharacterCount(String s) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile("^[-A-Za-z0-9!@#$%^*()+=:;\".\',/&? ]+$");
        Matcher m = p.matcher(s);
        return m.find();
    }

    @Override
    public void fetchQuesitonsList() {
        if (null != Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_SIGNON_SECRECT_QUESTION_URL_KEY)){
            new GetSecretQuestionsListAsyncTask(mContext, this).execute();
        }else{
            RunTimeData.getInstance().setInterruptScreenBackButtonClicked(true);
            RunTimeData.getInstance().setAppProfileKeyOrValueMissing(true);
            view.showAppProfileUrlNotFoundError();
        }
    }

    @Override
    public void getSecretQuestions(SecretQuestionsResponse secretQuestionsResponse) {
        view.getQuestionsData(secretQuestionsResponse);
    }

}
