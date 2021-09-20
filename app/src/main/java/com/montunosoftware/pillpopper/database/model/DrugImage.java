package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 3/29/2016.
 */
public class DrugImage {

    private String imageGUID;
    private String imagePath;
    private boolean needsUpload;
    private boolean needsDelete;

    public String getImageGUID() {
        return imageGUID;
    }

    public void setImageGUID(String imageGUID) {
        this.imageGUID = imageGUID;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
