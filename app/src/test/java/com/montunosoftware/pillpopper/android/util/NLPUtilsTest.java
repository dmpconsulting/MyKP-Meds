package com.montunosoftware.pillpopper.android.util;

import android.content.Context;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.NLPReminder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class NLPUtilsTest {
    private Context context;
    private HomeContainerActivity homeContainerActivity;

    @Before
    public void setup() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().get();
        context = homeContainerActivity.getApplicationContext();
    }

    @Test
    public void testGetScheduledFrequency() {
        assertEquals("DAILY", NLPUtils.getScheduledFrequency("D"));
        assertEquals("WEEKLY", NLPUtils.getScheduledFrequency("W"));
        assertNotEquals("MONTHLY", NLPUtils.getScheduledFrequency("D"));
        assertEquals("MONTHLY", NLPUtils.getScheduledFrequency("M"));
    }

    @Test
    public void testGetNLPFormattedDate() {
        String date = "2016-02-11";
        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String todayString = formatter.format(todayDate);
        assertNotEquals(date, NLPUtils.getNLPFormattedDate());
        assertEquals(todayString, NLPUtils.getNLPFormattedDate());
    }


    @Test
    public void testGetScheduledEvery() {
        Drug drug = new Drug();
        drug.setName("abc");
        drug.setUserID("121");
        assertNotNull(NLPUtils.getScheduledEvery(context, drug, "D", 12, "1"));
        assertNotNull(NLPUtils.getScheduledEvery(context, drug, "W", 16, "2"));
        assertNotNull(NLPUtils.getScheduledEvery(context, drug, "M", 7, "2"));
    }

    @Test
    public void testIsResponseMatched() {
        NLPReminder nlpReminder = new NLPReminder();
        nlpReminder.setStartDate("12344");
        nlpReminder.setEndDate("12455");
        nlpReminder.setFrequency("1");
        nlpReminder.setEvery("12");
        ArrayList<String> list = new ArrayList<>();
        list.add("1234");
        nlpReminder.setReminderTimes(list);
        NLPReminder usersChoiceReminder = new NLPReminder();
        usersChoiceReminder.setStartDate("12344");
        usersChoiceReminder.setEndDate("12455");
        usersChoiceReminder.setFrequency("1");
        usersChoiceReminder.setEvery("12");
        usersChoiceReminder.setReminderTimes(list);
        assertTrue(NLPUtils.isResponseMatched(nlpReminder, usersChoiceReminder));
    }

    @Test
    public void testGetNLPFormattedDates() {
        assertNotNull(NLPUtils.getNLPFormattedDate("12345"));
    }

    @Test
    public void testGetScheduleFormattedDate() {
        assertNotNull(NLPUtils.getScheduleFormattedDate("123456"));
    }

    @Test
    public void testInitializeSSL() {
        NLPUtils.initializeSSL();
    }
    @Test
    public void testPrepareMobileResponse()
    {
        NLPReminder nlpReminder = new NLPReminder();
        nlpReminder.setStartDate("12344");
        nlpReminder.setEndDate("12455");
        nlpReminder.setFrequency("1");
        nlpReminder.setEvery("12");
        ArrayList<String> list = new ArrayList<>();
        list.add("1234");
        nlpReminder.setDosage("12");
        nlpReminder.setMedicine("abc");
        nlpReminder.setSigId("1234");
        nlpReminder.setReminderTimes(list);
        assertNotNull(NLPUtils.prepareMobileResponse(nlpReminder));
    }

    @Test
    public void testConvertHHMMtoTimeFormat()
    {
        assertNotNull(NLPUtils.convertHHMMtoTimeFormat("1120"));
    }
    @Test
    public void testGetScheduleFormattedReminders()
    {
        ArrayList<String>remainderList=new ArrayList<>();
        remainderList.add("1234");
        remainderList.add("1211");
        remainderList.add("1233");
        assertNotNull(NLPUtils.getScheduleFormattedReminders(remainderList));
    }

}
