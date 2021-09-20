package com.montunosoftware.pillpopper.android.home;

import android.content.Intent;
import android.widget.Button;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public  class SetupReminderCardTest {

    private SetupReminderCard setupReminderCard;
    private HomeCardDetailActivity homeCardDetailActivity;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        setupReminderCard = new SetupReminderCard();
        Intent intent = new Intent();
        intent.putExtra("card", (SetupReminderCard) setupReminderCard);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().start().get();
    }

    @Test
    public void genericCardNotNull() {
        assertNotNull(setupReminderCard);
    }

    @Test
    public void testGetCardTitleNotNull() {
        assertNotNull(setupReminderCard.getTitle());
    }

    @Test
    public void testGetDetailView() {
        assertThat(setupReminderCard.getDetailView());
    }
    @Test
    public void testGetBanner(){
        assertThat(setupReminderCard.getBanner());
    }
    @Test
    public void testGetDescription(){
        assertNotNull(setupReminderCard.getDescription());
    }
      @Test
    public void testOnButtonClick() {
        setupReminderCard.getDetailView();
        Button createBtn = homeCardDetailActivity.findViewById(R.id.card_btn_view_meds);
        createBtn.performClick();
    }
}