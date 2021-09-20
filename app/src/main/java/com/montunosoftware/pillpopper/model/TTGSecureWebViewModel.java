package com.montunosoftware.pillpopper.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TTGSecureWebViewModel extends TTGWebviewModel implements
		Serializable {

	private static final long serialVersionUID = 1L;

	private List<TTGCookie> mCookies = new ArrayList<>();

	public List<TTGCookie> getCookies() {
		return mCookies;
	}

	public void setCookies(List<TTGCookie> mCookies) {
		this.mCookies = mCookies;
	}

}
