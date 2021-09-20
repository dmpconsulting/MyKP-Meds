package org.kp.tpmg.mykpmeds.activation.model;

public class GenericHttpResponse {
	private boolean status; // connection success - true, failure - false
	private String data;
	public GenericHttpResponse() {
		super();
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public boolean getStatus() {
		return status;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}
}