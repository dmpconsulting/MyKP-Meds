package com.montunosoftware.pillpopper.android.home;

import android.content.Intent;
import android.webkit.WebView;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class GenericHomeCardTest {
    private GenericHomeCard genericHomeCard;
    private HomeCardDetailActivity homeCardDetailActivity;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        RunTimeData.getInstance().setAnnouncements(TestUtil.getAnnouncementsResponse());
        genericHomeCard = new GenericHomeCard(RunTimeData.getInstance().getAnnouncements().getAnnouncements().get(0), 4);
        Intent intent = new Intent();
        intent.putExtra("card", (GenericHomeCard) genericHomeCard);
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().start().get();
    }

    @Test
    public void genericCardNotNull() {
        assertNotNull(genericHomeCard);
    }

    @Test
    public void testGetCardTitleNotNull() {
        assertNotNull(genericHomeCard.getCardTitle());
    }

    @Test
    public void testGetCardSubTitleNotNull() {
        assertNotNull(genericHomeCard.getCardSubTitle());
    }

   /* @Test
    public void testOnButtonClick() {
        WebView messageTextView = homeCardDetailActivity.findViewById(R.id.description_text);
        Button kpButton = homeCardDetailActivity.findViewById(R.id.kpButton);
        Button acknowledgeButton = homeCardDetailActivity.findViewById(R.id.acknowledge_button);
        genericHomeCard.initDetailView(messageTextView, kpButton, acknowledgeButton);
        assertThat(kpButton.performClick()).isTrue();
        assertThat(acknowledgeButton.performClick()).isTrue();
    }*/
}
