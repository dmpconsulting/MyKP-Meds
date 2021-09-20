package com.montunosoftware.pillpopper.android;
/*
 * The class holds all the data with respect to application.
 * 
 * Created to replace all the static fields of the pillpopperapplication uopn FindBugs fixes.
 * 
 * */
public class RunTimeConstants
{
	
	private static RunTimeConstants thisInstance;

	private boolean backPressDrugDetailAct;
	private boolean notificationSuppressor;
	
	private RunTimeConstants()
	{
		backPressDrugDetailAct=false;
		notificationSuppressor=true;
	}
	
	public static final RunTimeConstants getInstance(){
		if (thisInstance==null) {
			thisInstance = new RunTimeConstants();
		}
		return thisInstance;
	}

	public boolean isBackPressDrugDetailAct() {
		return backPressDrugDetailAct;
	}

	public void setBackPressDrugDetailAct(boolean backPressDrugDetailAct) {
		this.backPressDrugDetailAct = backPressDrugDetailAct;
	}

	public boolean isNotificationSuppressor() {
		return notificationSuppressor;
	}

	public void setNotificationSuppressor(boolean notificationSuppressor) {
		this.notificationSuppressor = notificationSuppressor;
	}
}
