package com.montunosoftware.pillpopper.android.fingerprint;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceIdShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, UniqueDeviceIdShadow.class})
public class FingerprintUtilsTest {
    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
    }
    @Test
    public void testisDeviceEligibleForFingerprintOptIn(){
        assertSame(false,FingerprintUtils.isDeviceEligibleForFingerprintOptIn(context));
    }
    @Test
    public void testisDeviceEligibleForFingerprintSignIn(){
        assertSame(false,FingerprintUtils.isDeviceEligibleForFingerprintSignIn(context));
    }
    @Test
    public void testencryptAndStorePassword(){
        assertSame(false,FingerprintUtils.encryptAndStorePassword(context,"wyte123656"));
    }
    @Test
    public void testgetDecryptedPassword(){
        assertNotNull(FingerprintUtils.getDecryptedPassword(context,"273hhrgegrjv"));
    }
    @Test
    public void testisCredentialsSaved(){
        assertSame(false,FingerprintUtils.isCredentialsSaved(context));
    }

}
