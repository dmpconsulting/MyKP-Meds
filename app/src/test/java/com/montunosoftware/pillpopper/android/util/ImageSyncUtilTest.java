package com.montunosoftware.pillpopper.android.util;

import android.content.Context;

import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.service.images.sync.ImageSyncUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

/**
 * Created by M1023050 on 8/7/2018.
 */

@RunWith(RobolectricTestRunner.class)
@Config( sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, UniqueDeviceIdShadow.class})
public class ImageSyncUtilTest {

    private Context context;
    private PillpopperAppContext pillpopperAppContext;


    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext().getApplicationContext();
        pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(context);
    }

    @Test
    public void testGetHeaders(){
        try {
            ImageSyncUtil.getHeaders(context);
        } catch (PillpopperServer.ServerUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFDBHeaders(){
        try {
            ImageSyncUtil.getFdbHeader(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetImageDownloadUrl(){
        try {
            ImageSyncUtil.getImageDownloadUrl(context, TestConfigurationProperties.MOCK_IMAGE_GUID);
        } catch (PillpopperServer.ServerUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetImageByID(){
        ImageSyncUtil.getFdbImageDownloadByIDUrl("6453");
    }

    @Test
    public void testGetFdbImageNDCCodeDownloadUrl(){
        ImageSyncUtil.getFdbImageNDCCodeDownloadUrl(context);
    }

    @Test
    public void testGetImageUploadUrl(){
        try {
            ImageSyncUtil.getImageUploadUrl(context, TestConfigurationProperties.MOCK_PILL_ID, TestConfigurationProperties.MOCK_IMAGE_GUID);
        } catch (PillpopperServer.ServerUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetDeleteImageurl(){
        try {
            ImageSyncUtil.getDeleteImageurl(context, TestConfigurationProperties.MOCK_PILL_ID, TestConfigurationProperties.MOCK_IMAGE_GUID);
        } catch (PillpopperServer.ServerUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetLocalCookies(){
        ImageSyncUtil.getLocalCookies();
    }

    @Test
    public void testPrepareFdbImageRequestBody(){
        ImageSyncUtil.prepareFdbImageRequestBody("9653");
    }
}
