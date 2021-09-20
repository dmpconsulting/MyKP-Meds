package com.montunosoftware.pillpopper.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.model.State;

public interface PillpopperReplyContext
{
	/*
	 * Generally used by the static call-out methods of Pillpopper activities.
	 */
	State getState();
	
	/*
	 * Needed to create the Intent.
	 */
	Context getAndroidContext();
	
	/*
	 * Needed to populate argument structures
	 */
	PillpopperAppContext getPillpopperContext();
	
	/*
	 * Used to populate log info field "actor" in PillpopperAppContext
	 */
	String getDebugName();
	
	/*
	 * Needed to start the Intent -- but it's done differently depending on whether we want
	 * the reply to come back to an Activity or a Fragment. (This is the common interface
	 * Android should have provided.)
	 */
	void startActivityForResult(Intent i, int resultCode);

	/*
	 * Needed to get help string in InputNumberActivity.selectNumber()
	 * and ToggleSelectorActivity.selectToggle().
	 */
	Resources getAndroidResources();

	Activity getActivityForMenu();
}
