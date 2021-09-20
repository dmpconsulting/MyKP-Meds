package org.kp.tpmg.mykpmeds.activation.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.KpBaseActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class ActivationUtilTest
{
    private Context context;
    private ActivationUtil activationUtil;
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
    public void testIsNetworkAvailable()
    {
       assertTrue( ActivationUtil.isNetworkAvailable(context));
    }
    @Test
    public void testGetSecretKey()
    {
        assertNotNull(ActivationUtil.getSecretKey(context));
    }
    @Test
    public void testGetDeviceId()
    {
        assertNotNull(ActivationUtil.getDeviceId(context));
    }
/*    @Test
    public void testGetAndroidID()
    {
        if(!Util.isEmulator()) {
            assertNotNull(ActivationUtil.getAndroidID(context));
        }
    }
    @Test
    public void testGetBluetoothMac()
    {
        if(!Util.isEmulator()) {
            assertNotNull(ActivationUtil.getBluetoothMac(context));
        }
    }*/
    @Test
    public void testGetWifiMacAddress()
    {
        assertNotNull(ActivationUtil.getWifiMacAddress(context));
    }
    @Test
    public void testIsCallOptionAvailable()
    {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if(manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE)
        {
            assertTrue(ActivationUtil.isCallOptionAvailable(context));
        }
    }
    @Test
    public void testGetAppVersion()
    {
        assertNotNull(ActivationUtil.getAppVersion(context));
    }
    @Test
    public void testCallMailClient()
    {
        String recipientMailId="";
        String subject="My KP Meds Support:";
        String body="";

        assertNotNull(ActivationUtil.callMailClient(recipientMailId,subject,body));

    }
    @Test
    public void testSetFontStyle()
    {
        assertNotNull(ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_REGULAR));
    }
    @Test
    public void testGetColorWrapper()
    {
        int id=R.color.kp_next_color;
        assertEquals(context.getColor(id),ActivationUtil.getColorWrapper(context,id));
    }
}
