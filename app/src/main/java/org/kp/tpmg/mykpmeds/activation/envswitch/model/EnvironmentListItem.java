package org.kp.tpmg.mykpmeds.activation.envswitch.model;

public class EnvironmentListItem {

    private String environmentLabel;

    private String environmentEndpointUrl;

    private boolean isSelected;

    public String getEnvironmentLabel() {
        return environmentLabel;
    }

    public void setEnvironmentLabel(String environmentLabel) {
        this.environmentLabel = environmentLabel;
    }

    public String getEnvironmentEndpointUrl() {
        return environmentEndpointUrl;
    }

    public void setEnvironmentEndpointUrl(String environmentEndpointUrl) {
        this.environmentEndpointUrl = environmentEndpointUrl;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
