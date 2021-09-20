package org.kp.tpmg.mykpmeds.activation.model;

public class PillpopperResponse {

	private String dataSyncResult;
	private String errorStatus;
	private String pillpopperVersion;
	private String replayId;
	private String action;

	public String getDataSyncResult() {
		return dataSyncResult;
	}

	public void setDataSyncResult(String dataSyncResult) {
		this.dataSyncResult = dataSyncResult;
	}

	public String getErrorStatus() {
		return errorStatus;
	}

	public void setErrorStatus(String errorStatus) {
		this.errorStatus = errorStatus;
	}

	public String getPillpopperVersion() {
		return pillpopperVersion;
	}

	public void setPillpopperVersion(String pillpopperVersion) {
		this.pillpopperVersion = pillpopperVersion;
	}

	public String getReplayId() {
		return replayId;
	}

	public void setReplayId(String replayId) {
		this.replayId = replayId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
