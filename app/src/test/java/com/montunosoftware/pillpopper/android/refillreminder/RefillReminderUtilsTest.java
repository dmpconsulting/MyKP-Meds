package com.montunosoftware.pillpopper.android.refillreminder;

import android.content.Context;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.KpBaseActivity;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class RefillReminderUtilsTest
{
    private Context context;
    private ShadowApplication application;
    private KpBaseActivity activity;

    @Before
public void setup() {
    activity = Robolectric.buildActivity(KpBaseActivity.class).create().get();
    context = activity.getApplicationContext();
    application = Shadows.shadowOf(activity.getApplication());
    assertNotNull(application);
}
    @Test
    public void testIsEmptyString()
    {
        String check="";
        String check1="Hii";

        assertTrue(RefillReminderUtils.isEmptyString(check));
        assertFalse(RefillReminderUtils.isEmptyString(check1));

    }
    @Test
    public void testGetAppVersion()
    {
        assertNotNull(RefillReminderUtils.getAppVersion(context));
    }
    @Test
    public void testConvertDateLongToIso()
    {
       /* String date="2019-05-29T10:45:00";
        String expectedDate = "20190529";
        String formattedDate = RefillReminderUtils.convertDateLongToIso(expectedDate);

        assertEquals(date,formattedDate);
*/

    }
    @Test
    public void testGetDrawableWrapper()
    {
        int drawable= R.drawable.delete_refill_button_background;
        assertNotNull(RefillReminderUtils.getDrawableWrapper(context,drawable));
    }

    @Test
    public void testSetFontStyle()
    {
        String textStyle= AppConstants.FONT_ROBOTO_REGULAR;
        assertNotNull(RefillReminderUtils.setFontStyle(context,textStyle));
    }

    @Test
    public void testGetLanguage()
    {
        assertNotNull(RefillReminderUtils.getLanguage());
    }




}
