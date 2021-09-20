package com.montunosoftware.pillpopper.service.images.sync;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.network.model.FailedImageObj;
import com.montunosoftware.pillpopper.service.TokenService;
import com.montunosoftware.pillpopper.service.images.sync.model.FdbImage;
import com.montunosoftware.pillpopper.service.images.sync.model.FdbRoot;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by adhithyaravipati on 11/21/16.
 */

public class ImageSyncManager implements ImageSynchronizer {

    private static ImageSynchronizer mImageSynchronizer = null;

    private Context mContext;

    private RequestQueue mRequestQueue;
    private int imageAPICalledCounter = 0;
    private int mFDBRetryCounter = 0;

    //Protected constructor to avoid instantiation
    protected ImageSyncManager(Context context) {
        this.mContext = context;
        this.mRequestQueue = getRequestQueue(mContext);
    }

    public static ImageSynchronizer getInstance(Context context) {
        if (mImageSynchronizer == null) {
            mImageSynchronizer = new ImageSyncManager(context);
        }
        return mImageSynchronizer;
    }

    @Override
    public void downloadImage(String pillId, String imageGuid) {
        try {
            if(!Util.isEmptyString(pillId) && !Util.isEmptyString(imageGuid)) {
                performDownloadImage(pillId, imageGuid);
            }else{
                PillpopperLog.say("ImageSynchronizer --  Error downloadImage: PillId/ImageGuid is empty -- ");
            }
        } catch (PillpopperServer.ServerUnavailableException e) {
            PillpopperLog.say("ImageSynchronizer --  Error downloading image: Server unavailable -- " + e.getMessage());
        }
    }

    @Override
    public void uploadImage(String pillId, String imageGuid) {
        try {
            if(!Util.isEmptyString(pillId) && !Util.isEmptyString(imageGuid)) {
                performUploadImage(pillId, imageGuid);
            }else{
                PillpopperLog.say("ImageSynchronizer --  Error uploadImage either PillID or imageGuid is null -- ");
            }
        } catch (PillpopperServer.ServerUnavailableException e) {
            PillpopperLog.say("ImageSynchronizer --  Error uploading image: Server unavailable -- " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String pillId, String imageGuid) {
        try {
            if(!Util.isEmptyString(pillId) && !Util.isEmptyString(imageGuid)) {
                performDeleteImage(pillId, imageGuid);
            } else{
                PillpopperLog.say("ImageSynchronizer --  Error deleting image: PillId/ImageGuid is empty -- ");
            }
        } catch (PillpopperServer.ServerUnavailableException e) {
            PillpopperLog.say("ImageSynchronizer --  Error deleting image: Server unavailable -- " + e.getMessage());
        }
    }

    @Override
    public void downloadFdbImageById(String pillId, String serviceImageId) {
        try {
            if(!Util.isEmptyString(pillId) && !Util.isEmptyString(serviceImageId)){
                performDownloadFdbImageById(pillId, serviceImageId);
            }else{
                PillpopperLog.say("ImageSynchronizer --  Error downloadFdbImageById either PillID or serviceImageId is null -- ");
            }
        } catch (PillpopperServer.ServerUnavailableException e) {
            PillpopperLog.say("ImageSynchronizer --  Error while download FDB Image by id: Server unavailable -- " + e.getMessage());
        }
    }

    @Override
    public void downloadFdbImageByNdcCode(String pillId, String ndcCode) {
        try {
            if(!Util.isEmptyString(pillId) && !Util.isEmptyString(ndcCode)){
                performDownloadFdbImageByNDCCode(pillId, ndcCode);
            }else{
                PillpopperLog.say("ImageSynchronizer --  Error while downloadFdbImageByNdcCode either PillID or ndcCode is null" );
            }
        } catch (PillpopperServer.ServerUnavailableException e) {
            PillpopperLog.say("ImageSynchronizer --  Error while download FDB Image by NDC code: Server unavailable -- " + e.getMessage());
        }
    }

    private void performDownloadImage(final String pillId, final String imageGuid)
            throws PillpopperServer.ServerUnavailableException {
            Response.Listener<Bitmap> onResponseListener = response -> {
            PillpopperLog.say("ImageSyncManager -- performDownloadImage -- Image Download Successful -- ImageGuid: " + imageGuid);
            saveImageToFileSystem(imageGuid, response);
            Drug kphcDrug = FrontController.getInstance(mContext).getDrugByPillId(pillId);
            kphcDrug.getPreferences().setPreference("defaultImageChoice",AppConstants.IMAGE_CHOICE_CUSTOM);
            FrontController.getInstance(mContext).updatePillImagePreferences(pillId, AppConstants.IMAGE_CHOICE_CUSTOM, kphcDrug.getPreferences().getPreference("defaultServiceImageID"));
            FrontController.getInstance(mContext).addLogEntry(mContext, Util.prepareLogEntryForAction("EditPill", kphcDrug, mContext));

            try {
                // Check the entry in Retry table and delete if required
                FrontController.getInstance(mContext).deleteEntryFromRetryTable(pillId, imageGuid);
            }catch (Exception e){
                PillpopperLog.say(e.getMessage());
            }
            checkAndInitiateAccessTokenCall();
        };

        Response.ErrorListener onErrorListener = error -> {
            NetworkResponse response = error.networkResponse;
            if (response != null) {
                switch (response.statusCode) {
                    case HttpsURLConnection.HTTP_UNAUTHORIZED:
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        PillpopperLog.say("ImageSyncManager StatusCode 401 for " + imageGuid);
                        saveDownloadFailedImageDetails(pillId, imageGuid, PillpopperConstants.IMAGE_TYPE_CUSTOM);
                        break;
                }
            }
            checkAndInitiateAccessTokenCall();
            PillpopperLog.say("ImageSyncManager -- performDownloadImage -- ERROR: Image Download Unsuccessful -- ImageGuid: " + imageGuid);
        };

        ImageRequest downloadImageRequest = buildDownloadImageRequest(imageGuid, onResponseListener, onErrorListener);
        addRequestToQueue(downloadImageRequest);

    }

    /**
     *
     * @param pillId
     * @param imageId - could be image serviceId or ndc Code
     * @param imageType - could be C - Custom, N - NDC, S - service ID
     * Saves the failed image due to access token into the IMAGE_FAILURE_ENTRIES Table
     */
    private void saveDownloadFailedImageDetails(String pillId, String imageId, String imageType) {
        FailedImageObj failedImageObj = new FailedImageObj();
        failedImageObj.setPillID(pillId);
        failedImageObj.setImageId(imageId);
        failedImageObj.setImageType(imageType);

        boolean isTabelExist = FrontController.getInstance(mContext).isTableExist(DatabaseConstants.IMAGE_FAILURE_ENTRIES_TABLE);
        if(isTabelExist) {
            FrontController.getInstance(mContext).saveFailedImage(failedImageObj);
        }
    }

    private void performDownloadFdbImageById(final String pillID, final String serviceImageId) throws PillpopperServer.ServerUnavailableException {

        String downloadUrl = ImageSyncUtil.getFdbImageDownloadByIDUrl(serviceImageId);

        Response.Listener<String> onResponseListener = response -> {
            Gson gson = new Gson();
            FdbImage fdbImage = gson.fromJson(response, FdbImage.class);
            fdbImage.setPillId(pillID);
            FrontController.getInstance(mContext).saveFdbImage(mContext, fdbImage);
            PillpopperLog.say("ImageSyncManager -- performDownloadFdbImageById -- Image Download Successful -- ImageGuid: " + response);
            try {
                // Check the entry in Retry table and delete if required
                FrontController.getInstance(mContext).deleteEntryFromRetryTable(pillID, serviceImageId);
            }catch (Exception e){
                PillpopperLog.say(e.getMessage());
            }
            checkAndInitiateAccessTokenCall();
        };

        Response.ErrorListener onErrorListener = error -> {
            NetworkResponse response = error.networkResponse;
            if (response != null) {
                switch (response.statusCode) {
                    case HttpsURLConnection.HTTP_UNAUTHORIZED:
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        PillpopperLog.say("ImageSyncManager StatusCode 401 for " + serviceImageId);
                        saveDownloadFailedImageDetails(pillID, serviceImageId, PillpopperConstants.IMAGE_TYPE_SERVICE_ID);
                        break;
                }
            }
            checkAndInitiateAccessTokenCall();
            PillpopperLog.say("ImageSyncManager -- performDownloadFdbImageById -- ERROR: Image Download Unsuccessful -- Image NDC: " + serviceImageId);
        };

        StringRequest fdbImageRequest = buildFdbImageByIdDownloadRequest(downloadUrl, onResponseListener, onErrorListener);
        addRequestToQueue(fdbImageRequest);
    }

    private void performDownloadFdbImageByNDCCode(final String pillId, final String ndcCode) throws PillpopperServer.ServerUnavailableException {

        String downloadUrl = ImageSyncUtil.getFdbImageNDCCodeDownloadUrl(mContext);
        final Gson gson = new Gson();

        Response.Listener<String> onResponseListener = response -> {
            Drug editDrug  = FrontController.getInstance(mContext).getDrugByPillId(pillId);
            FdbRoot fdbRoot = gson.fromJson(response, FdbRoot.class);
            if (fdbRoot.getImages() != null && !fdbRoot.getImages().isEmpty()) {
                fdbRoot.getImages().get(0).setPillId(pillId);
                FrontController.getInstance(mContext).saveFdbImage(mContext, fdbRoot.getImages().get(0));
                if(null != editDrug.getPreferences().getPreference("defaultImageChoice") && !editDrug.getPreferences().getPreference("defaultImageChoice").equalsIgnoreCase(AppConstants.IMAGE_CHOICE_UNDEFINED)) {
                    FrontController.getInstance(mContext).updatePillImagePreferences(pillId, editDrug.getPreferences().getPreference("defaultImageChoice"), fdbRoot.getImages().get(0).getId());
                } else{
                    FrontController.getInstance(mContext).updatePillImagePreferences(pillId, AppConstants.IMAGE_CHOICE_FDB, fdbRoot.getImages().get(0).getId());
                }
            } else {
                if(null != editDrug.getPreferences().getPreference("defaultImageChoice") && !editDrug.getPreferences().getPreference("defaultImageChoice").equalsIgnoreCase(AppConstants.IMAGE_CHOICE_UNDEFINED)) {
                    FrontController.getInstance(mContext).updatePillImagePreferences(pillId, editDrug.getPreferences().getPreference("defaultImageChoice"), AppConstants.IMAGE_NOT_FOUND);
                } else{
                    FrontController.getInstance(mContext).updatePillImagePreferences(pillId, AppConstants.IMAGE_CHOICE_FDB, AppConstants.IMAGE_NOT_FOUND);
                }
            }
            // set needFDBUpdate for pillId to false and then add Edit Pill LogEntry
            if(Boolean.parseBoolean(editDrug.getPreferences().getPreference("needFDBUpdate"))){
                FrontController.getInstance(mContext).updateNoNeedFDBImageUpdate(pillId);
            }
            // getDrug details with preferences after update, again for log entry
            editDrug = FrontController.getInstance(mContext).getDrugByPillId(pillId);
            FrontController.getInstance(mContext).addLogEntry(mContext, Util.prepareLogEntryForAction("EditPill", editDrug, mContext));
            PillpopperLog.say("ImageSyncManager -- performDownloadFdbImageByNDCCode -- Image Download Successful -- ImageGuid: " + response);

            try {
                // Check the entry in Retry table and delete if required
                FrontController.getInstance(mContext).deleteEntryFromRetryTable(pillId, ndcCode);
            } catch (Exception e){
                PillpopperLog.say(e.getMessage());
            }
            checkAndInitiateAccessTokenCall();
        };

        Response.ErrorListener onErrorListener = error -> {
            NetworkResponse response = error.networkResponse;
            if(response != null){
                switch(response.statusCode){
                    case HttpsURLConnection.HTTP_UNAUTHORIZED:
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        PillpopperLog.say("ImageSyncManager StatusCode 401 for " + ndcCode);
                        saveDownloadFailedImageDetails(pillId,ndcCode, PillpopperConstants.IMAGE_TYPE_NDC);
                        break;
                }
            }
            checkAndInitiateAccessTokenCall();
            PillpopperLog.say("ImageSyncManager -- performDownloadFdbImageByNDCCode -- ERROR: Image Download Unsuccessful -- Image NDC: " + ndcCode);
        };

        StringRequest fdbImageRequest = buildFdbImageByNdcCodeDownloadRequest(downloadUrl, ndcCode, onResponseListener, onErrorListener);
        addRequestToQueue(fdbImageRequest);
    }

    private void checkAndInitiateAccessTokenCall() {
        imageAPICalledCounter++;
        if (imageAPICalledCounter == RunTimeData.getInstance().getImageAPIDownloadCounter() && !FrontController.getInstance(mContext).getFailedImageEntryList().isEmpty()
                && mFDBRetryCounter < PillpopperConstants.FDB_RETRY_COUNt) {
            mFDBRetryCounter++;
            RunTimeData.getInstance().setImageAPIDownloadCounter(0); //reset the counter
            RunTimeData.getInstance().setAccessTokenCalledForFailedFDBImages(true);
            LoggerUtils.info("--Access token-- Image sync Manager");
            TokenService.startGetAccessTokenService(mContext);
        } else {
            LoggerUtils.info("--API manager-- Number of API - " + RunTimeData.getInstance().getImageAPIDownloadCounter() + "Number of API's called - "+imageAPICalledCounter);
        }
    }

    private void performUploadImage(final String pillId, final String imageGuid)
            throws PillpopperServer.ServerUnavailableException {
        String uploadImageUrl = ImageSyncUtil.getImageUploadUrl(mContext, pillId, imageGuid);

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        String encodedImage = FrontController.getInstance(mContext).getCustomImage(imageGuid);
        byte[] imageData = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData,0,imageData.length, bitmapOptions);

        Response.Listener<String> onResponseListener = response -> {
            PillpopperLog.say("ImageSyncManager -- performUploadImage -- Image upload successful. Image GUID: " + imageGuid);
            PillpopperLog.say("ImageSyncManager -- performUploadImage -- Removing the pending image sync request from the database - Pill ID: " + pillId + " , Image GUID: " + imageGuid);
            FrontController.getInstance(mContext).deletePendingImageRequest(pillId, imageGuid, true, false);
        };

        Response.ErrorListener onErrorListener = error -> PillpopperLog.say("ImageSyncManager -- performUploadImage -- ERROR: Image upload unsuccessful. Image GUID: " + imageGuid + ". Waiting for 30 seconds before next attempt.");

        StringRequest imageUploadRequest = buildImageStringForUploadRequest(uploadImageUrl, imageBitmap, onResponseListener, onErrorListener);
        addRequestToQueue(imageUploadRequest);

    }

    private void performDeleteImage(final String pillId, final String imageGuid)
            throws PillpopperServer.ServerUnavailableException {
        String deleteImageUrl = ImageSyncUtil.getDeleteImageurl(mContext, pillId, imageGuid);
        Response.Listener<String> onResponseListener = response -> {
            PillpopperLog.say("ImageSyncManager -- performDeleteImage -- Image delete successful. Image GUID: " + imageGuid);
            FrontController.getInstance(mContext).deletePendingImageRequest(pillId, imageGuid, false, true);
        };

        Response.ErrorListener onErrorListener = error -> PillpopperLog.say("ImageSyncManager -- performDeleteImage -- ERROR: Image upload unsuccessful. Image GUID: " + imageGuid + ". Waiting for 30 seconds before next attempt.");

        StringRequest deleteImageRequest = buildImageDeleteRequest(deleteImageUrl, onResponseListener, onErrorListener);
        addRequestToQueue(deleteImageRequest);


    }

    private ImageRequest buildDownloadImageRequest(String imageGuid, Response.Listener onResponseListener, Response.ErrorListener onErrorListener)
            throws PillpopperServer.ServerUnavailableException {
        String downloadImageUrl = ImageSyncUtil.getImageDownloadUrl(mContext, imageGuid);

        return buildImageRequest(downloadImageUrl, onResponseListener, onErrorListener);
    }


    private ImageRequest buildImageRequest(String url, Response.Listener onResponseListener, Response.ErrorListener onErrorListener) {
        ImageRequest imageRequest = new ImageRequest(url, onResponseListener, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, onErrorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                try {
                    return ImageSyncUtil.getHeaders(mContext);
                } catch (PillpopperServer.ServerUnavailableException e) {
                    PillpopperLog.exception("ImageSyncManager -- buildImageRequest -- Server Unavailable Exception -- Unable to get Headers -- " + e.getMessage());
                    return null;
                }
            }
        };

        imageRequest.setRetryPolicy(new DefaultRetryPolicy(
                45000,  //Timeout for HTTPConnection
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        return imageRequest;
    }

    private StringRequest buildFdbImageByIdDownloadRequest(String url, Response.Listener onResponseListener, Response.ErrorListener onErrorListener) {
        StringRequest fdbImageRequest = new StringRequest(Request.Method.GET, url, onResponseListener, onErrorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return ImageSyncUtil.getFdbHeader(mContext);
            }
        };

        fdbImageRequest.setRetryPolicy(new DefaultRetryPolicy(
                45000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        return fdbImageRequest;
    }

    private StringRequest buildFdbImageByNdcCodeDownloadRequest(String url, final String ndcCode, Response.Listener onResponseListener, Response.ErrorListener onErrorListener) {
        StringRequest fdbImageRequest = new StringRequest(Request.Method.POST, url, onResponseListener, onErrorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return ImageSyncUtil.getFdbHeader(mContext);
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return ImageSyncUtil.prepareFdbImageRequestBody(ndcCode).toString().getBytes();
            }


            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        fdbImageRequest.setRetryPolicy(new DefaultRetryPolicy(
                45000,  //Timeout for HTTPConnection
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        return fdbImageRequest;
    }

    private StringRequest buildImageStringForUploadRequest(String url, Bitmap imageToUpload, Response.Listener onResponseListener, Response.ErrorListener onErrorListener) {
        final byte[] imageByteArray = ImageSyncUtil.getImageByteArrayForUpload(imageToUpload);

        return new StringRequest(Request.Method.POST, url, onResponseListener, onErrorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                try {
                    return ImageSyncUtil.getHeaders(mContext);
                } catch (PillpopperServer.ServerUnavailableException e) {
                    PillpopperLog.exception("ImageSyncManager -- buildImageStringForUploadRequest -- Server Unavailable Exception -- Unable to get Headers -- " + e.getMessage());
                    return null;
                }
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                if (imageByteArray != null && imageByteArray.length > 0) {
                    return imageByteArray;
                } else {
                    return null;
                }
            }


        };

    }

    private StringRequest buildImageDeleteRequest(String url, Response.Listener onResponseListener, Response.ErrorListener onErrorListener) {

        return new StringRequest(Request.Method.POST, url, onResponseListener, onErrorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                try {
                    return ImageSyncUtil.getHeaders(mContext);
                } catch (PillpopperServer.ServerUnavailableException e) {
                    PillpopperLog.exception("ImageSyncManager -- buildImageStringForUploadRequest -- Server Unavailable Exception -- Unable to get Headers -- " + e.getMessage());
                    return null;
                }
            }


        };

    }

    private RequestQueue getRequestQueue(Context context) {
        if(null == mRequestQueue) {
            mRequestQueue = Volley.newRequestQueue(context, getHurlStack());
        }
        return mRequestQueue;
    }

    private <T> void addRequestToQueue(Request<T> request) {
        getRequestQueue(mContext).add(request);
    }

    private HurlStack getHurlStack() {
        SSLSocketFactory sslSocketFactory = TTGRuntimeData.getInstance().getSslSocketFactory();

        HurlStack hurlStack = new HurlStack(null, sslSocketFactory) {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                httpsURLConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());
                return httpsURLConnection;
            }
        };
        return hurlStack;
    }

    private void saveImageToFileSystem(String imageGuid, Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream  = null;
        try {
            FrontController frontController = FrontController.getInstance(mContext);
            byteArrayOutputStream  = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream );
            String encodedImage = Base64.encodeToString(byteArrayOutputStream .toByteArray(), Base64.DEFAULT);
            frontController.saveCustomImage(imageGuid,encodedImage);
        } catch (Exception e) {
            PillpopperLog.exception("StateDownloadIntentService -- FileNotFoundException -- Pill image file not found. " + e.getMessage());
        } finally {
            try {
                if (byteArrayOutputStream  != null) {
                    byteArrayOutputStream .close();
                }
            } catch (IOException e) {
                PillpopperLog.exception("StateDownloadIntentService -- IOException -- Error closing FileOutputStream. " + e.getMessage());
            }
        }
    }

    /**
     * Captures the instances in breadcrumbs related to failed image operations.
     * @param message
     */
   /* private void captureBreadCrumbs(String message){
        if(null!=mContext){
            AppDynamicsController.getInstance(mContext).captureBreadCrumbs(message);
        }
    } */
}
