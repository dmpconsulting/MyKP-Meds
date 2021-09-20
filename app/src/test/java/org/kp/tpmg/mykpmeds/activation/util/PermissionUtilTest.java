package org.kp.tpmg.mykpmeds.activation.util;

import android.content.Context;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceIdShadow;
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

@RunWith(RobolectricTestRunner.class)
@Config( sdk = TestConfigurationProperties.BUILD_SDK_VERSION, manifest="AndroidManifest.xml", application = PillpopperApplicationShadow.class, shadows = {PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, UniqueDeviceIdShadow.class})
public class PermissionUtilTest
{
    private Context context;
    private PermissionUtils mPermissionUtils;
    private ShadowApplication application;
    private KpBaseActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(KpBaseActivity.class).get();
        context = activity.getApplicationContext();
        application = Shadows.shadowOf(activity.getApplication());
        assertNotNull(application);
    }
    @Test
    public void testPermissionDeniedMessage()
    {
        assertEquals(PermissionUtils.permissionDeniedMessage(AppConstants.PERMISSION_CONTACTS_READ, context),
                context.getResources().getString(R.string.contact_permission));
        assertEquals(PermissionUtils.permissionDeniedMessage(AppConstants.PERMISSION_CAMERA, context),
                context.getResources().getString(R.string.camera_permission));
        assertEquals(PermissionUtils.permissionDeniedMessage(AppConstants.PERMISSION_WRITE_EXTERNAL_STORAGE, context),
                context.getResources().getString(R.string.storage_permission));
        assertEquals(PermissionUtils.permissionDeniedMessage(AppConstants.PERMISSION_READ_EXTERNAL_STORAGE, context),
                context.getResources().getString(R.string.storage_permission));
        assertEquals(PermissionUtils.permissionDeniedMessage(AppConstants.PERMISSION_PHONE_CALL_PHONE, context),
                context.getResources().getString(R.string.call_permission));

    }

  /*  @Test
    public void testCheckRuntimePermission()
    {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };
        boolean hasPermissions = PermissionUtils.checkRuntimePermission(
                AppConstants.PERMISSION_CAMERA, Manifest.permission.CAMERA, context);
        assertEquals(false, hasPermissions);

        application.grantPermissions(permissions);
        boolean permissions1 = PermissionUtils.checkRuntimePermission(
                AppConstants.PERMISSION_CAMERA, Manifest.permission.CAMERA, context);
        assertEquals(true, permissions1);
    }*/
}
