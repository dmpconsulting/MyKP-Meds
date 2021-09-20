package com.montunosoftware.pillpopper.android.home;

import android.content.Intent;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class KPHCCardsTest {
    private KPHCCards kphcCards;
    private HomeCardDetailActivity homeCardDetailActivity;

    @Before
    public void setUp() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        User user = TestUtil.prepareUserObject();
        kphcCards = new KPHCCards(user, true);
        Intent intent = new Intent();
        intent.putExtra("Id", user.getUserId());
        intent.putExtra("card", (KPHCCards) kphcCards);
        intent.putExtra("NewKPHC", true);
        intent.putExtra("UserName", user.getFirstName());
        homeCardDetailActivity = Robolectric.buildActivity(HomeCardDetailActivity.class, intent).create().resume().start().get();
    }

    @Test
    public void cardNotNull() {
        assertNotNull(kphcCards);
    }
    @Test
    public void testNewKPHCUser(){
        assertTrue(kphcCards.hasNewKPHCUser());
    }

    @Test
    public void testGetCardSubTitleNotNull() {
        assertNotNull(kphcCards.getDetailCardTitle());
    }

}