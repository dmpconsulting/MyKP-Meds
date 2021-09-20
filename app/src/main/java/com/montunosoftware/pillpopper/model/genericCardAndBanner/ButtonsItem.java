package com.montunosoftware.pillpopper.model.genericCardAndBanner;

import java.io.Serializable;
import java.util.Comparator;

public class ButtonsItem implements Serializable {
    private String action;
    private String label;
    private int order;
    private String url;
    private String destination;
    private String button_color;
    private String font_color;

    public String getAction() {
        return action;
    }

    public String getLabel() {
        return label;
    }

    public Integer getOrder() {
        return order;
    }

    public String getUrl() {
        return url;
    }

    public String getDestination() {
        return destination;
    }

    public String getButtonColor() {
        return button_color;
    }

    public String getFontColor() {
        return font_color;
    }

    public static class ButtonItemComparator implements Comparator<ButtonsItem>
    {
        @Override
        public int compare(ButtonsItem lhs, ButtonsItem rhs)
        {
            return lhs.getOrder().compareTo(rhs.getOrder());
        }
    }
}
