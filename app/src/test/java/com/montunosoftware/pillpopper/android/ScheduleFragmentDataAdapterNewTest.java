package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.widget.RelativeLayout;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.ScheduleListItemDataWrapper;
import com.montunosoftware.pillpopper.model.ScheduleMainDrug;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class ScheduleFragmentDataAdapterNewTest {
    private ScheduleFragmentDataAdapterNew scheduleFragmentDataAdapterNew;
    private List<ScheduleListItemDataWrapper> scheduleListItemDataWrapperList;
    private Context context;

    @Before
    public void setUp() {
        ScheduleFragmentNew scheduleFragmentNew = new ScheduleFragmentNew();
        scheduleFragmentDataAdapterNew = new ScheduleFragmentDataAdapterNew();
        context = ApplicationProvider.getApplicationContext();
        mockData();
        scheduleFragmentDataAdapterNew.setData(scheduleListItemDataWrapperList, scheduleFragmentNew);
    }

    private void mockData() {
        scheduleListItemDataWrapperList = new ArrayList<>();
        ScheduleListItemDataWrapper scheduleListItemDataWrapper = new ScheduleListItemDataWrapper();
        scheduleListItemDataWrapper.setUserId("1234");
        scheduleListItemDataWrapper.setUserFirstName("xyz");
        scheduleListItemDataWrapper.setPossibleNextActiveListItem(true);
        ScheduleMainDrug scheduleMainDrug = new ScheduleMainDrug();
        scheduleMainDrug.setDose("1");
        scheduleMainDrug.setPillName("abc");
        scheduleMainDrug.setDayPeriod("2");
        List<ScheduleMainDrug> scheduleMainDrugList = new ArrayList<>();
        scheduleMainDrugList.add(scheduleMainDrug);
        scheduleListItemDataWrapper.setDrugList(scheduleMainDrugList);
        scheduleListItemDataWrapperList.add(scheduleListItemDataWrapper);
    }

    @Test
    public void adapterShouldNotNull() {
        assertNotNull(scheduleFragmentDataAdapterNew);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(scheduleListItemDataWrapperList.size(), scheduleFragmentDataAdapterNew.getItemCount());
    }

    @Test
    public void testOnCreateViewHolder() {
        assertNotNull(scheduleFragmentDataAdapterNew.onCreateViewHolder(new RelativeLayout(context), 0));
    }
}
