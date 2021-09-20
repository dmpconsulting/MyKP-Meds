package com.montunosoftware.pillpopper.service.images.sync.model;

import java.util.List;

/**
 * Created by M1032896 on 6/19/2018.
 */

public class FdbRoot {

    private String ndcCode;
    private String description;
    private List<FdbImage> images;
    private String normalizedNDCCode;

    public String getNdcCode() {
        return ndcCode;
    }

    public void setNdcCode(String ndcCode) {
        this.ndcCode = ndcCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FdbImage> getImages() {
        return images;
    }

    public void setImages(List<FdbImage> images) {
        this.images = images;
    }

    public String getNormalizedNDCCode() {
        return normalizedNDCCode;
    }

    public void setNormalizedNDCCode(String normalizedNDCCode) {
        this.normalizedNDCCode = normalizedNDCCode;
    }
}
