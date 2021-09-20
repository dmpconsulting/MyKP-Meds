package com.montunosoftware.pillpopper.model;

import android.database.Cursor;

import com.montunosoftware.pillpopper.database.DatabaseConstants;

/**
 * Created by adhithyaravipati on 1/27/17.
 */

public class PendingImageRequest {

    private String pillId = null;
    private String imageGuid = null;
    private boolean needsUpload = false;
    private boolean needsDelete = false;

    public static PendingImageRequest getFromCursor(Cursor cursor) {
        PendingImageRequest pendingImageRequest = new PendingImageRequest();
        if(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_PILL_ID) != -1) {
            pendingImageRequest.setPillId(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_PILL_ID)));
        }
        if(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_IMAGE_GUID) != -1) {
            pendingImageRequest.setImageGuid(cursor.getString(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_IMAGE_GUID)));
        }
        if(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_NEEDS_UPLOAD) != -1) {
            if(cursor.getInt(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_NEEDS_UPLOAD)) == 1) {
                pendingImageRequest.setNeedsUpload(true);
            }
        }
        if(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_NEEDS_DELETE) != -1) {
            if(cursor.getInt(cursor.getColumnIndex(DatabaseConstants.PENDING_IMAGE_SYNC_REQUESTS_NEEDS_DELETE)) == 1) {
                pendingImageRequest.setNeedsDelete(true);
            }
        }

        return pendingImageRequest;
    }

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }

    public String getImageGuid() {
        return imageGuid;
    }

    public void setImageGuid(String imageGuid) {
        this.imageGuid = imageGuid;
    }

    public boolean isNeedsUpload() {
        return needsUpload;
    }

    public void setNeedsUpload(boolean needsUpload) {
        this.needsUpload = needsUpload;
    }

    public boolean isNeedsDelete() {
        return needsDelete;
    }

    public void setNeedsDelete(boolean needsDelete) {
        this.needsDelete = needsDelete;
    }
}
