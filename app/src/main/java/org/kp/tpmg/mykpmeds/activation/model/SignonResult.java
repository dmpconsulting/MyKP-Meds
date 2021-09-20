package org.kp.tpmg.mykpmeds.activation.model;

import java.util.List;

public class SignonResult {
	private String message;//
	private String mrn;
	private String statusCode;//
	private String title;
	
	//the new reg api calls contain list of users, so no sep userId
	// private String userId;
	private String introCompleteFl;//
	private String switchDeviceFlag;//
	private String switchUserFlag;
	private String lockoutStatus;
	private String mKporgCookieName;
	private String mKporgCookieDomain;
	private String mKporgCookiePath;

    public String getSetUpCompleteFl() {
        return setUpCompleteFl;
    }

    public void setSetUpCompleteFl(String setUpCompleteFl) {
        this.setUpCompleteFl = setUpCompleteFl;
    }

    private String setUpCompleteFl;
	
	//additional
	private String pillpopperVersion;
	public String getPillpopperVersion() {
		return pillpopperVersion;
	}

	public void setPillpopperVersion(String pillpopperVersion) {
		this.pillpopperVersion = pillpopperVersion;
	}

	public String getKpGUID() {
		return kpGUID;
	}

	public void setKpGUID(String kpGUID) {
		this.kpGUID = kpGUID;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	private String kpGUID;
	private List<User> users;
	
	public String getmKporgCookieName() {
		return mKporgCookieName;
	}

	public void setmKporgCookieName(String mKporgCookieName) {
		this.mKporgCookieName = mKporgCookieName;
	}

	public String getmKporgCookieDomain() {
		return mKporgCookieDomain;
	}

	public void setmKporgCookieDomain(String mKporgCookieDomain) {
		this.mKporgCookieDomain = mKporgCookieDomain;
	}

	public String getmKporgCookiePath() {
		return mKporgCookiePath;
	}

	public void setmKporgCookiePath(String mKporgCookiePath) {
		this.mKporgCookiePath = mKporgCookiePath;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getSwitchDeviceFlag() {
		return switchDeviceFlag;
	}

	public void setSwitchDeviceFlag(String switchDeviceFlag) {
		this.switchDeviceFlag = switchDeviceFlag;
	}

	public String getSwitchUserFlag() {
		return switchUserFlag;
	}

	public void setSwitchUserFlag(String switchUserFlag) {
		this.switchUserFlag = switchUserFlag;
	}

	public String getLockoutStatus() {
		return lockoutStatus;
	}

	public void setLockoutStatus(String lockoutStatus) {
		this.lockoutStatus = lockoutStatus;
	}

	/*public void setUserId(String userId) {
		this.userId = userId;
	}*/

	public String getMessage() {
		return message;
	}

	public String getMrn() {
		return mrn;
	}

	public String getStatusCode() {
		return statusCode;
	}

/*	public String getUserId() {
		return userId;
	}*/

	public String getIntroCompleteFl() {
		return introCompleteFl;
	}

	public void setIntroCompleteFl(String introCompleteFl) {
		this.introCompleteFl = introCompleteFl;
	}

	public String getPrimaryUserId(){
		if (users!=null) {
			for(User user : users){
				if (("primary").equalsIgnoreCase(user.getUserType().trim())) {
					return user.getUserId();
				}
			}
		}
		return null;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}