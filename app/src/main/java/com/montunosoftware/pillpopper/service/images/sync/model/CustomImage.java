package com.montunosoftware.pillpopper.service.images.sync.model;

/**
 * Created by M1032896 on 7/2/2018.
 */

public class CustomImage {

    private String id;
    private String pillId;
    private String imageDesc;
    private String imageData;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }

    public String getImageDesc() {
        return imageDesc;
    }

    public void setImageDesc(String imageDesc) {
        this.imageDesc = imageDesc;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
