package com.montunosoftware.pillpopper.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RegionResponse {
    @SerializedName("regions")
    @Expose
    private ArrayList<Region> regions = null;

    public ArrayList<Region> getRegions() {
        return regions;
    }

    public void setRegions(ArrayList<Region> regions) {
        this.regions = regions;
    }
}
