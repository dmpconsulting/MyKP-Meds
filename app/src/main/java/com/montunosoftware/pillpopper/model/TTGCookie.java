package com.montunosoftware.pillpopper.model;

import java.io.Serializable;

public class TTGCookie implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String domain;
	private String name;
	private String path;
	private String value;
	private boolean secure;
	

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
