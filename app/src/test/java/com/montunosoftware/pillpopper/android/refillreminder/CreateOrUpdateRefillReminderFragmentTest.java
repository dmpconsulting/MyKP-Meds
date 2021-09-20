package com.montunosoftware.pillpopper.android.refillreminder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.refillreminder.views.CreateOrUpdateRefillReminderFragment;
import com.montunosoftware.pillpopper.android.refillreminder.views.RefillRemindersHomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class CreateOrUpdateRefillReminderFragmentTest {
    private CreateOrUpdateRefillReminderFragment createOrUpdateRefillReminderFragment;
    private View view;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        setFragment();
    }

    private void setFragment() {
        RefillReminder refillReminder = new RefillReminder();
        refillReminder.setReminderNote("Take Medicine");
        refillReminder.setNextReminderDate("1245672");
        refillReminder.setReminderEndDate("1234523");
        refillReminder.setRecurring(true);
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedRefillReminder", refillReminder);
        createOrUpdateRefillReminderFragment = new CreateOrUpdateRefillReminderFragment();
        createOrUpdateRefillReminderFragment.setArguments(bundle);
        SupportFragmentTestUtil.startFragment(createOrUpdateRefillReminderFragment, RefillRemindersHomeContainerActivity.class);
        view = createOrUpdateRefillReminderFragment.getView();
    }

    @Test
    public void createOrUpdateRefillReminderFragmentShouldNotNull() {
        assertThat(createOrUpdateRefillReminderFragment != null);
    }

    @Test
    public void fragmentViewShouldNotBeNull() {
        assertThat(createOrUpdateRefillReminderFragment.getView()).isNotNull();
    }

    @Test
    public void testGetTime() {
        assertNotNull(createOrUpdateRefillReminderFragment.getTime());
    }

    @Test
    public void testGetAmPmTimeFromHrMin() {
        assertNotNull(createOrUpdateRefillReminderFragment.getAmPmTimeFromHrMin(4, 25));
    }

    @Test
    public void testEndDateLayoutVisibility() {
        RelativeLayout endDateLayout = view.findViewById(R.id.rl_end_date_layout);
        assertEquals(View.VISIBLE, endDateLayout.getVisibility());
    }

    @Test
    public void testOnClickRepeatLayout() {
        RelativeLayout repeatLayout = view.findViewById(R.id.rl_repeat_layout);
        assertThat(repeatLayout.performClick()).isTrue();
        TextView refillEndDateTxt = view.findViewById(R.id.tv_end_date_value);
        assertEquals("Never", refillEndDateTxt.getText());
    }

    @Test
    public void testOnClickRefillRepeatFrequencyText() {
        TextView refillRepeatFrequencyText = view.findViewById(R.id.tv_repeat_frequency_text);
        assertThat(refillRepeatFrequencyText.performClick()).isTrue();
        RelativeLayout repeatFrequencyLayout = view.findViewById(R.id.rl_repeat_frequency_layout);
        assertEquals(View.VISIBLE, repeatFrequencyLayout.getVisibility());

    }

    @Test
    public void testOnClickRefillDate() {
        TextView refillDate = view.findViewById(R.id.tv_date);
        assertThat(refillDate.performClick()).isTrue();
    }

    @Test
    public void testOnClickRefillTime() {
        TextView refillTime = view.findViewById(R.id.tv_time);
        assertThat(refillTime.performClick()).isTrue();
    }

    @Test
    public void testOnClickRefillEndDateTxt() {
        TextView refillEndDateTxt = view.findViewById(R.id.tv_end_date_value);
        assertThat(refillEndDateTxt.performClick()).isTrue();
    }

    @Test
    public void testOnClickRefillRepeatFrequency() {
        TextView refillRepeatFrequencyText = view.findViewById(R.id.tv_repeat_frequency_text);
        LinearLayout refillRepeatFrequencyThirty = view.findViewById(R.id.thirty);
        assertThat(refillRepeatFrequencyThirty.performClick()).isTrue();
        assertEquals("30 days", refillRepeatFrequencyText.getText());
        LinearLayout refillRepeatFrequencySixty = view.findViewById(R.id.sixty);
        assertThat(refillRepeatFrequencySixty.performClick()).isTrue();
        assertEquals("60 days", refillRepeatFrequencyText.getText());
        LinearLayout refillRepeatFrequencyNinety = view.findViewById(R.id.ninety);
        assertThat(refillRepeatFrequencyNinety.performClick()).isTrue();
        assertEquals("90 days", refillRepeatFrequencyText.getText());
        Button refillCustomFrequency = view.findViewById(R.id.custom_frequency);
        assertThat(refillCustomFrequency.performClick()).isTrue();
        TextView hintText = view.findViewById(R.id.tv_hint_text);
        assertThat(hintText.performClick()).isTrue();
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        createOrUpdateRefillReminderFragment.onPause();
    }
}
