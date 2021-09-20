package com.montunosoftware.pillpopper.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Region{
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("pageList")
    @Expose
    private List<PageList> pageList = null;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<PageList> getPageList() {
        return pageList;
    }

    public void setPageList(List<PageList> pageList) {
        this.pageList = pageList;
    }

}
