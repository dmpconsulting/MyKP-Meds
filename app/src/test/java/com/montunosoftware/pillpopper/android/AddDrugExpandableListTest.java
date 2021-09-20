package com.montunosoftware.pillpopper.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.FDADrugDatabase;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.PillpopperRunTimeShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentServiceShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {StateDownloadIntentServiceShadow.class, PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperRunTimeShadow.class})

public class AddDrugExpandableListTest {
    private AddDrugExpandableList addDrugExpandableList;
    private PillpopperActivity activity;
    private List<String> listDataHeader;
    private HashMap<String, List<FDADrugDatabase.DatabaseDrugVariant>> listChildData;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(PillpopperActivity.class).create().get();
        mockData();
        addDrugExpandableList = new AddDrugExpandableList(activity, listDataHeader, listChildData);
    }

    private void mockData() {
        listDataHeader = Arrays.asList("abc", "xyz");
        listChildData = new HashMap<>();
    }

    @Test
    public void adapterNotNull() {
        assertNotNull(addDrugExpandableList);
    }

    @Test
    public void activityShouldNotNull() {
        assertNotNull(activity);
    }

    @Test
    public void testGetChildId() {
        assertEquals(1, addDrugExpandableList.getChildId(0, 1));
    }

    @Test
    public void testGetGroup() {
        assertEquals("abc", addDrugExpandableList.getGroup(0));
    }

    @Test
    public void testGetGroupCount() {
        assertEquals(2, addDrugExpandableList.getGroupCount());
    }

}
