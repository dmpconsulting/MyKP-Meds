package com.montunosoftware.pillpopper.android.interrupts;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kp.tpmg.mykpmeds.activation.util.TestUtil.prepareSecretQuestionAnswerMockData;

import android.content.Intent;
import android.view.MenuItem;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.interrupts.email.EmailMismatchFragment;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionAnswerRequestModel;
import com.montunosoftware.pillpopper.android.interrupts.secretquestions.SecretQuestionsFragment;
import com.montunosoftware.pillpopper.android.interrupts.temporaryPassword.TemporaryPasswordFragment;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class InterruptsActivityTest {

    private ActivityController<InterruptsActivity> controller;
    private InterruptsActivity interruptsActivity;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
    }

    @Test
    public void startActivityForTempPassword() {
        startInterruptsActivity(AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD);
        assertFalse(interruptsActivity.getSupportFragmentManager().getFragments().isEmpty());
        assertTrue(interruptsActivity.getSupportFragmentManager().getFragments().get(0) instanceof TemporaryPasswordFragment);
    }

    @Test
    public void startActivityForEmailMismatch() {
        startInterruptsActivity(AppConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH);
        assertFalse(interruptsActivity.getSupportFragmentManager().getFragments().isEmpty());
        assertTrue(interruptsActivity.getSupportFragmentManager().getFragments().get(0) instanceof EmailMismatchFragment);
    }

    @Test
    public void startActivityForSecretQuestions() {
        startInterruptsActivity(AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS);
        assertFalse(interruptsActivity.getSupportFragmentManager().getFragments().isEmpty());
        assertTrue(interruptsActivity.getSupportFragmentManager().getFragments().get(0) instanceof SecretQuestionsFragment);
    }

    @Test
    public void interruptActivityNotNull() {
        startInterruptsActivity(AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD);
        assertNotNull(interruptsActivity);
    }

    @Test
    public void performClickTestForTempPassword() {
        startInterruptsActivity(AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD);
        interruptsActivity.onConfirmClick(getJsonRequest(), AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD);
    }

    @Test
    public void performClickTestForEmailMismatch() {
        startInterruptsActivity(AppConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH);
        interruptsActivity.onConfirmClick("abc@abc.com", AppConstants.SIGNON_RESPONSE_INTERRUPT_EMAIL_MISMATCH);
    }

    @Test
    public void performClickTestForSecretQuestions() {
        startInterruptsActivity(AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS);
        interruptsActivity.onConfirmClick(prepareRequestJson(prepareSecretQuestionAnswerMockData()), AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS);
    }



    private void startInterruptsActivity(String intentValue) {
        Intent intent = new Intent(RuntimeEnvironment.systemContext, InterruptsActivity.class);
        intent.putExtra("mSignOnInterruptType", intentValue);
        controller = Robolectric.buildActivity(InterruptsActivity.class, intent);
        interruptsActivity = controller.create().start().resume().destroy().visible().get();
    }


    private String getJsonRequest() {
        JSONObject outerObject = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            JSONObject innerObject = new JSONObject();
            innerObject.put("fname", "password");
            innerObject.put("password", "abcd");
            array.put(innerObject);
            outerObject.put("requestMetaData", array);
        } catch (JSONException e) {
            LoggerUtils.exception(e.getMessage());
        }
        return outerObject.toString();
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
                jsonArray.put(innerObject);
            }
            rootObject.put("secretQuestions", jsonArray);
        } catch (JSONException e) {
            LoggerUtils.info(e.getMessage());
        }
        return rootObject.toString();
    }

    @Test
    public void onClickHomeMenu() {
        startInterruptsActivity(AppConstants.SIGNON_RESPONSE_INTERRUPT_TEMP_PWD);
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        interruptsActivity.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }
}
