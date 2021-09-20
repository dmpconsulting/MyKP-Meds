package com.montunosoftware.pillpopper.network.model;

/**
 * Created by M1023050 on 18-Nov-18.
 */

public class FailedImageObj {

    private String pillID;
    private String imageId;
    private String imageType;

    public String getPillID() {
        return pillID;
    }

    public void setPillID(String pillID) {
        this.pillID = pillID;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}
