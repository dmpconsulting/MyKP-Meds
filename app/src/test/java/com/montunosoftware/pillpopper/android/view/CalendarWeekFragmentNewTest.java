package com.montunosoftware.pillpopper.android.view;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperTime;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class CalendarWeekFragmentNewTest {
    CalendarWeekFragmentNew calendarWeekFragmentNew = new CalendarWeekFragmentNew();

    @Before
    public void setup(){
        TestUtil.setupTestEnvironment();
        CalendarWeekFragmentNew.newInstance(PillpopperDay.today(), RuntimeEnvironment.systemContext, new ArrayList<>(),0);
        calendarWeekFragmentNew.setWeekStartDay(PillpopperDay.today());
        SupportFragmentTestUtil.startFragment(calendarWeekFragmentNew, HomeContainerActivity.class);
    }
    @Test
    public void fragmentShouldNotNull() {
        assertThat(calendarWeekFragmentNew != null);
    }

    @Test
    public void fragmentViewShouldNotBeNull() {
        Assertions.assertThat(calendarWeekFragmentNew.getView()).isNotNull();
    }
    @Test
    public void testgetmParentViewPagerAdapter(){
        calendarWeekFragmentNew.setmParentViewPagerAdapter(new ArrayList<>());
        assertNotNull(calendarWeekFragmentNew.getmParentViewPagerAdapter());
    }
    @Test
    public void testgetPositionInParentViewPager(){
        assertNotEquals(2,calendarWeekFragmentNew.getPositionInParentViewPager());
    }
    @Test
    public void testgetContext(){
        assertNotNull(calendarWeekFragmentNew.getmContext());
    }
    @Test
    public void testgetWeekStartDay(){
        assertNotNull(calendarWeekFragmentNew.getWeekStartDay());
    }
}
