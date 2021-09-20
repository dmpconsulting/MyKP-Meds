package com.montunosoftware.pillpopper.model;

import android.widget.ImageView;

import java.util.List;

/**
 * @author
 * Created by M1027850 on 2/19/2016.
 */
public class Child {

    String proxyName;
    List<DoseEvent> dose;
    ImageView image;

    public Child() {
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public List<DoseEvent> getDose() {
        return dose;
    }

    public void setDose(List<DoseEvent> dose) {
        this.dose = dose;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }
}
