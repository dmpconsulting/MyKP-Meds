package com.montunosoftware.pillpopper.android.interrupts.secretquestions;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.interrupts.InterruptsActivity;
import com.montunosoftware.pillpopper.android.interrupts.model.Questions;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class QuestionsListAdapterTest {
    private QuestionsListAdapter questionsListAdapter;
    private List<Questions> questionsList;
    private Context context;


    @Before
    public void setUp() {
        Intent intent = new Intent();
        intent.putExtra("mSignOnInterruptType", AppConstants.SIGNON_RESPONSE_INTERRUPT_SECRET_QUESTIONS);
        InterruptsActivity interruptsActivity = Robolectric.buildActivity(InterruptsActivity.class, intent).create().start().resume().visible().get();
        mockData();
        context = interruptsActivity.getAndroidContext();
        QuestionsListFragment questionsListFragment = new QuestionsListFragment();
        interruptsActivity.getSupportFragmentManager().beginTransaction().add(0, questionsListFragment).commit();
        questionsListAdapter = new QuestionsListAdapter(questionsList, interruptsActivity, questionsListFragment);
    }

    private void mockData() {
        questionsList = new ArrayList<>();
        Questions questions = new Questions();
        questions.setSelected(true);
        questions.setText("abc");
        questions.setQuestionId("1");
        questionsList.add(questions);

    }

    @Test
    public void adapterShouldNotNull() {
        assertNotNull(questionsListAdapter);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(1, questionsListAdapter.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(questionsListAdapter.onCreateViewHolder(new RelativeLayout(context), 0));
    }

    @Test
    public void testOnBindViewHolder() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.question_row_item, null, false);
        QuestionsListAdapter.QuestionViewHolder questionViewHolder = questionsListAdapter.new QuestionViewHolder(view);
        questionsListAdapter.onBindViewHolder(questionViewHolder, 0);

    }
}
