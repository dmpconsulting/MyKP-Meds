package com.montunosoftware.pillpopper.android.interrupts.secretquestions;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.interrupts.InterruptsActivity;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionAnswerRequestModel;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.After;
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

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class SecretQuestionsFragmentTest {
    private ActivityController<InterruptsActivity> controller;
    private View view;
    private List<SecretQuestionAnswerRequestModel> secretQuestionAnswerRequestModelList;
    private SecretQuestionsFragment secretQuestionsFragment;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        secretQuestionAnswerRequestModelList = TestUtil.prepareSecretQuestionAnswerMockData();
        Intent intent = new Intent(RuntimeEnvironment.systemContext, InterruptsActivity.class);
        intent.putExtra("mSignOnInterruptType", AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS);
        controller = Robolectric.buildActivity(InterruptsActivity.class, intent);
        InterruptsActivity interruptsActivity = controller.create().start().resume().visible().get();
        secretQuestionsFragment = (SecretQuestionsFragment) interruptsActivity.getSupportFragmentManager().getFragments().get(0);
        view = secretQuestionsFragment.getView();
    }


    @Test
    public void fragmentViewShouldNotBeNull() {
        assertNotNull(view);
    }

    @Test
    public void testOnActivityResult() {
        Intent intent = new Intent();
        intent.putExtra("questionId", secretQuestionAnswerRequestModelList.get(0).getQuestionId());
        intent.putExtra("groupId", secretQuestionAnswerRequestModelList.get(0).getGroupId());
        intent.putExtra("questionText", secretQuestionAnswerRequestModelList.get(0).getQuestionText());
        secretQuestionsFragment.onActivityResult(1, Activity.RESULT_OK, intent);
        TextView question1 = view.findViewById(R.id.tv_question1);
        assertEquals("Question1", question1.getText());
        secretQuestionsFragment.onActivityResult(2, Activity.RESULT_OK, intent);
        TextView question2 = view.findViewById(R.id.tv_question2);
        assertEquals("Question1", question2.getText());
        secretQuestionsFragment.onActivityResult(3, Activity.RESULT_OK, intent);
        TextView question3 = view.findViewById(R.id.tv_question3);
        assertEquals("Question1", question3.getText());
        LoggerUtils.warning("question field not empty");
    }


    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
    }
}
