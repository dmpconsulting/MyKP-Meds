package com.montunosoftware.pillpopper.service.images.sync;

/**
 * Created by adhithyaravipati on 11/21/16.
 */

public interface ImageSynchronizer {

    void downloadImage(String pillId, String imageGuid);
    void uploadImage(String pillId, String imageGuid);
    void deleteImage(String pillId, String imageGuid);
    void downloadFdbImageById(String pillId, String ndcCode);
    void downloadFdbImageByNdcCode(String pillId, String ndcCOde);
}
