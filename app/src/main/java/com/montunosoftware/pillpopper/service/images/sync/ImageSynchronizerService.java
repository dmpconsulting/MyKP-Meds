package com.montunosoftware.pillpopper.service.images.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.PendingImageRequest;

import java.util.List;

public class ImageSynchronizerService extends IntentService {

    Context mContext;

    private static final String ACTION_SYNC_IMAGES = "com.montunosoftware.pillpopper.service.images.sync.action.SYNC_IMAGES";

    public ImageSynchronizerService() {
        super("ImageSynchronizerService");
    }

    public static void startImageSynchronization(Context context) {
        Intent intent = new Intent(context, ImageSynchronizerService.class);
        intent.setAction(ACTION_SYNC_IMAGES);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC_IMAGES.equals(action)) {
                handleActionSynchronizeImages();
            }
        }
    }


    private void handleActionSynchronizeImages() {
        mContext = getApplicationContext();

        ImageSynchronizer imageSynchronizer = ImageSyncManager.getInstance(mContext);
        List<PendingImageRequest> pendingImageRequests = FrontController.getInstance(mContext).getAllPendingImageRequests();

        for(PendingImageRequest pendingImageRequest : pendingImageRequests) {
            if(pendingImageRequest.isNeedsUpload()) {
                imageSynchronizer.uploadImage(pendingImageRequest.getPillId(), pendingImageRequest.getImageGuid());
            } else if(pendingImageRequest.isNeedsDelete()) {
                imageSynchronizer.deleteImage(pendingImageRequest.getPillId(), pendingImageRequest.getImageGuid());
            }
        }
    }

}
