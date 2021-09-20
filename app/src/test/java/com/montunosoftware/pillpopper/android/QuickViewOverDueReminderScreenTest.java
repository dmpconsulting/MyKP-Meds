package com.montunosoftware.pillpopper.android;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class QuickViewOverDueReminderScreenTest {
    private QuickViewOverDueReminderScreen quickViewOverDueReminderScreen;
    private ActivityController<QuickViewOverDueReminderScreen> controller;
    private Context context;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        context = ApplicationProvider.getApplicationContext();
        mockData();
        controller = Robolectric.buildActivity(QuickViewOverDueReminderScreen.class);
        quickViewOverDueReminderScreen = controller.create().resume().visible().get();
    }

    private void mockData() {
        UniqueDeviceId.init(context);
        Drug drug = new Drug();
        drug.setName("Med Name1");
        drug.setUserID("");
        drug.setDose("2");
        drug.setIsTempHeadr(true);
        Drug drug2 = new Drug();
        drug2.setDose("7");
        drug2.setName("Med Name2");
        drug2.setUserID("");
        drug2.setNotes("abc");
        Drug drug3 = new Drug();
        drug3.setName("Med Name3");
        drug3.setUserID("");
        drug3.setDose("4");
        List<Drug> drugList = new ArrayList<>();
        drugList.add(drug);
        drugList.add(drug2);
        drugList.add(drug3);
        PillpopperRunTime.getInstance().setQuickViewReminderDrugs(drugList);
        /*Long remainderTime = Long.parseLong(TestConfigurationProperties.MOCK_LATE_REMINDER_TIME);
        LinkedHashMap<Long, List<Drug>> dummyMap = new LinkedHashMap<>();
        dummyMap.put(remainderTime, drugList);
        PillpopperRunTime.getInstance().setmCurrentRemindersMap(dummyMap);*/
    }

    @Test
    public void checkActivityShouldNull() {
        assertNotNull(quickViewOverDueReminderScreen);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.destroy();
    }
}
