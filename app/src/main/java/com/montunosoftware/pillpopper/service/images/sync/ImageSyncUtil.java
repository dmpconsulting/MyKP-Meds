package com.montunosoftware.pillpopper.service.images.sync;

import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.CookieManager;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.State;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adhithyaravipati on 11/21/16.
 */

public class ImageSyncUtil {

    public static Map<String, String> getHeaders(Context context)
            throws PillpopperServer.ServerUnavailableException {
        Map<String, String> headers = new HashMap<>();

        PillpopperAppContext globalAppContext = PillpopperAppContext.getGlobalAppContext(context);
        ActivationController activationController = ActivationController.getInstance();
        if (null != activationController.getSSOSessionId(context)) {
            headers.put("secureToken", activationController.getSSOSessionId(context));
        }
        headers.put("hardwareId", UniqueDeviceId.getHardwareId(context));

        if (RunTimeData.getInstance().getRegistrationResponse() != null) {
            headers.put("guid", SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME).getString(AppConstants.KP_GUID, null));
        }

        State currState = globalAppContext.getState(context);

        if (currState.getAccountId() != null) {
            headers.put("userId", currState.getAccountId());
        }

        headers.put("Cookie", getLocalCookies());
        return headers;
    }

    public static Map<String, String> getFdbHeader(Context context) {
        Map<String, String> headers = new HashMap<>();
       // headers.put("Content-Type", "application/json");
        headers.put("Authorization", getAuthorization(context));
        headers.put("x-deviceinfo",Util.getClientInfo(context));
        headers.put("Cookie", getLocalCookies());
        return headers;
    }


    private static String getAuthorization(Context context) {
       StringBuilder authorization = new StringBuilder();
       ActivationController activationController = ActivationController.getInstance();
       LoggerUtils.info("Access token  -- " + activationController.getAccessToken(context));
       if (null != activationController.getAccessToken(context)) {
            authorization = new StringBuilder();
            authorization.append(activationController.getTokenType(context));
            authorization.append(" ");
            authorization.append(activationController.getAccessToken(context));
       }
       return authorization.toString();
    }

    public static String getImageDownloadUrl(Context context, String imageGuid)
            throws PillpopperServer.ServerUnavailableException {
        StringBuilder downloadImageUrlBuilder = new StringBuilder(AppConstants.ConfigParams.getWsPillpopperSecuredBaseURL());
        downloadImageUrlBuilder.append(ImageSyncConstants.DOWNLOAD_IMAGE_SERVER_PATH);
        downloadImageUrlBuilder.append("?");
        downloadImageUrlBuilder.append(ImageSyncConstants.IMAGE_URL_ARGUMENT_IMAGE_GUID);
        downloadImageUrlBuilder.append("=");
        downloadImageUrlBuilder.append(imageGuid);
        return downloadImageUrlBuilder.toString();
    }

    public static String getFdbImageDownloadByIDUrl(String id) {
        StringBuilder downloadFdbImageUrlBuilder = new StringBuilder(AppConstants.ConfigParams.getFdbImageURL());
        downloadFdbImageUrlBuilder.append("/").append(id);
        return downloadFdbImageUrlBuilder.toString();
    }

    public static String getFdbImageNDCCodeDownloadUrl(Context context) {
        StringBuilder downloadFdbImageUrlBuilder = new StringBuilder(AppConstants.ConfigParams.getFdbImageURL());
        return downloadFdbImageUrlBuilder.toString();
    }

    public static String getImageUploadUrl(Context context, String pillId, String imageGuid)
            throws PillpopperServer.ServerUnavailableException {

        StringBuilder uploadImageUrlBuilder = new StringBuilder(AppConstants.ConfigParams.getWsPillpopperSecuredBaseURL());
        uploadImageUrlBuilder.append(ImageSyncConstants.UPLOAD_IMAGE_SERVER_PATH);
        uploadImageUrlBuilder.append("?");
        uploadImageUrlBuilder.append(ImageSyncConstants.IMAGE_URL_ARGUMENT_IMAGE_GUID);
        uploadImageUrlBuilder.append("=");
        uploadImageUrlBuilder.append(imageGuid);
        uploadImageUrlBuilder.append("&");
        uploadImageUrlBuilder.append(ImageSyncConstants.IMAGE_URL_ARGUMENT_PILL_ID);
        uploadImageUrlBuilder.append("=");
        uploadImageUrlBuilder.append(pillId);
        return uploadImageUrlBuilder.toString();
    }

    public static String getDeleteImageurl(Context context, String pillId, String imageGuid)
            throws PillpopperServer.ServerUnavailableException {

        StringBuilder deleteImageUrlBuilder = new StringBuilder(AppConstants.ConfigParams.getWsPillpopperSecuredBaseURL());
        deleteImageUrlBuilder.append(ImageSyncConstants.DELETE_IMAGE_SERVER_PATH);
        deleteImageUrlBuilder.append("?");
        deleteImageUrlBuilder.append(ImageSyncConstants.IMAGE_URL_ARGUMENT_IMAGE_GUID);
        deleteImageUrlBuilder.append("=");
        deleteImageUrlBuilder.append(imageGuid);
        deleteImageUrlBuilder.append("&");
        deleteImageUrlBuilder.append(ImageSyncConstants.IMAGE_URL_ARGUMENT_PILL_ID);
        deleteImageUrlBuilder.append("=");
        deleteImageUrlBuilder.append(pillId);
        return deleteImageUrlBuilder.toString();
    }

    public static byte[] getImageByteArrayForUpload(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static String getLocalCookies() {
        return CookieManager.getInstance().getCookie("https://" + AppConstants.ConfigParams.getKeepAliveCookieDomain());
    }

    public static JSONObject prepareFdbImageRequestBody(String ndcCode) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ndcCode", ndcCode);
            jsonObject.put("cachedImage", "false");
        } catch (JSONException e) {
            PillpopperLog.say(e);
        }
        return jsonObject;
    }


}
