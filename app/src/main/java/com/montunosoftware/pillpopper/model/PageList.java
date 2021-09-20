package com.montunosoftware.pillpopper.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PageList {
    @SerializedName("pageType")
    @Expose
    private String pageType;
    @SerializedName("pageTitle")
    @Expose
    private String pageTitle;
    @SerializedName("pageDescription")
    @Expose
    private String pageDescription;
    @SerializedName("phones")
    @Expose
    private List<Phone> phones = null;
    @SerializedName("pageFooter")
    @Expose
    private String pageFooter;

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getPageDescription() {
        return pageDescription;
    }

    public void setPageDescription(String pageDescription) {
        this.pageDescription = pageDescription;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public String getPageFooter() {
        return pageFooter;
    }

    public void setPageFooter(String pageFooter) {
        this.pageFooter = pageFooter;
    }

}
