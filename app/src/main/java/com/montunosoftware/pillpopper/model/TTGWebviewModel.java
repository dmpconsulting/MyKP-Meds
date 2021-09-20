package com.montunosoftware.pillpopper.model;

import java.io.Serializable;

public class TTGWebviewModel implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final int NO_CLOSE_OPTION=0;
	public static final int NO_REFRESH_OPTION=0;
	private int mCloseImageViewId;
	private int mRefreshImageViewId;
	private String mTitle;
	private String mUrl;

	public int getCloseImageViewId() {
		return mCloseImageViewId;
	}

	public void setCloseImageViewId(int closeImageViewId) {
		this.mCloseImageViewId = closeImageViewId;
	}

	public int getRefreshImageViewId() {
		return mRefreshImageViewId;
	}

	public void setRefreshImageViewId(int refreshImageViewId) {
		this.mRefreshImageViewId = refreshImageViewId;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		this.mUrl = url;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	

}
