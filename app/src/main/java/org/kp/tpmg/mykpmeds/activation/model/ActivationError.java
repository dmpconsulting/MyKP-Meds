package org.kp.tpmg.mykpmeds.activation.model;

import java.io.Serializable;

public class ActivationError implements Serializable {

	private static final long serialVersionUID = 1L;
	private int errorCode;
	private boolean errorStatus;
	private String title;
	private String message;
	
	public boolean getErrorStatus() {
		return errorStatus;
	}

	public void setErrorStatus(boolean errorStatus) {
		this.errorStatus = errorStatus;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
