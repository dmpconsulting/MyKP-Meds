package org.kp.tpmg.mykpmeds.activation.model;

public class SignonResponse {
	private SignonResult pillpopperResponse;

	public void setResponse(SignonResult response) {
		this.pillpopperResponse = response;
	}

	public SignonResult getResponse() {
		return pillpopperResponse;
	}
}