
package com.montunosoftware.pillpopper.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Phone {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;
    @SerializedName("shortDescription")
    @Expose
    private String shortDescription;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }


}
