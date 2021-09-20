package org.kp.tpmg.mykpmeds.activation.util;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class ErrorMessageUtilTest {

    private ErrorMessageUtil errorMessageUtil;

    @Before
    public void setup() {
        errorMessageUtil = new ErrorMessageUtil();
    }

    @Test
    public void testGetErrorDetails() {

        assertNotNull(errorMessageUtil.getErrorDetails(-1));
        assertEquals("A network connection cannot be established. Please try again later.",
                errorMessageUtil.getErrorDetails(-1).getMessage());
        assertNotNull(errorMessageUtil.getErrorDetails(-2));
        assertEquals("Network Error",
                errorMessageUtil.getErrorDetails(-1).getTitle());
        assertNotNull(errorMessageUtil.getErrorDetails(2));
        assertEquals("Your device has been deactivated and your data has been cleared from the device.",
                errorMessageUtil.getErrorDetails(2).getMessage());
        assertNotNull(errorMessageUtil.getErrorDetails(3));
        assertEquals("Force Upgrade",
                errorMessageUtil.getErrorDetails(3).getMessage());
        assertNotNull(errorMessageUtil.getErrorDetails(5));
        assertEquals("Authorization failed",
                errorMessageUtil.getErrorDetails(5).getTitle());
        assertNotNull(errorMessageUtil.getErrorDetails(6));
        assertEquals("Locked out",
                errorMessageUtil.getErrorDetails(6).getTitle());
        assertNotNull(errorMessageUtil.getErrorDetails(7));
        assertNotNull(errorMessageUtil.getErrorDetails(8));
        assertNotNull(errorMessageUtil.getErrorDetails(9));
        assertNotNull(errorMessageUtil.getErrorDetails(11));
        assertNotNull(errorMessageUtil.getErrorDetails(20));
        assertNotNull(errorMessageUtil.getErrorDetails(100));
        assertNotNull(errorMessageUtil.getErrorDetails(101));
        assertNotNull(errorMessageUtil.getErrorDetails(110));
        assertNotNull(errorMessageUtil.getErrorDetails(125));
        assertNotNull(errorMessageUtil.getErrorDetails(200));
    }

    @Test
    public void testGetErrorDetailsMessage() {
        assertNotNull(errorMessageUtil.getErrorDetails(-1, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(-2, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(2, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(3, "Error Message"));
        assertEquals("Error Message", errorMessageUtil.getErrorDetails(3, "Error Message").getMessage());
        assertNotNull(errorMessageUtil.getErrorDetails(5, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(6, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(7, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(8, "Message"));
        assertEquals("Message", errorMessageUtil.getErrorDetails(8, "Message").getMessage());
        assertNotNull(errorMessageUtil.getErrorDetails(11, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(9, null));
        assertNotNull(errorMessageUtil.getErrorDetails(20, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(100, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(125, ""));
        assertNotNull(errorMessageUtil.getErrorDetails(101, null));
        assertNotNull(errorMessageUtil.getErrorDetails(110, "Message"));
        assertEquals("Message", errorMessageUtil.getErrorDetails(110, "Message").getMessage());
        assertNotNull(errorMessageUtil.getErrorDetails(200, ""));
    }
}
