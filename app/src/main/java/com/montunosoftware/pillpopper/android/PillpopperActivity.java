package com.montunosoftware.pillpopper.android;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.State;

import org.kp.tpmg.mykpmeds.activation.KpBaseActivity;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

@SuppressLint("Registered")
public class PillpopperActivity
	// extends Activity
	extends KpBaseActivity
	implements PillpopperReplyContext
{
	protected final PillpopperActivity _thisActivity = this;
//	protected PillpopperAppContext _globalAppContext; // _globalAppContext of KpBaseactivity will be used
	protected LayoutInflater _inflater;

	public PillpopperAppContext getGlobalAppContext()
	{
		return get_globalAppContext();
	}

	protected void setCancelButton(int cancelButtonId)
	{
		View v = this.findViewById(cancelButtonId);
		v.setOnClickListener(v1 -> {
            _thisActivity.setResult(RESULT_CANCELED);
            _thisActivity.finish();
        });
	}

	// This function sets up a callback to be called when either the "save" button
	// is pressed, or the hardware "back" button is pressed.
	OnClickListener _onSaveListener = null;
	protected void setSaveButton(int saveButtonId, OnClickListener onSaveListener)
	{
		_onSaveListener = onSaveListener;

		View v = this.findViewById(saveButtonId);

		if (v != null) {
			v.setOnClickListener(onSaveListener);
		}
	}

	protected void setSaveButtons(int saveButtonId1, int saveButtonId2, OnClickListener onSaveListener)
	{
		setSaveButton(saveButtonId1, onSaveListener);
		setSaveButton(saveButtonId2, onSaveListener);
	}


	// Post a task to scroll a scrollview to the top of a view.
	protected void scrollTo(final ScrollView scrollView, final View scrollDestination)
	{
		// For some reason, scrollTo can't be run from here; it has to be run later.  maybe because
		// _updateView invalidates the layout, thus scrollto can't be run until the layout has been recomputed.
		//
		// http://stackoverflow.com/questions/3263259/scrollview-scrollto-not-working-saving-scrollview-position-on-rotation
		if (scrollView != null && scrollDestination != null) {
			scrollView.post(() -> scrollView.scrollTo(0, scrollDestination.getTop()));
		}
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		set_globalAppContext(PillpopperAppContext.getGlobalAppContext(this));
		PillpopperLog.say("Creating " + this.getClass().getName());
		super.onCreate(bundle);
		_inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		PillpopperLog.say("Resuming " + this.getClass().getName());

		// KP only: launch the login screen if needed, or timed out
		get_globalAppContext().kpMaybeLaunchLoginScreen(this);

		registerPushNotificationReceiver();
	}

	private void registerPushNotificationReceiver() {
		LoggerUtils.info("FCM -- registerPushNotificationReceiver");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("PUSH_NOTIFICATION_RECEIVER");
		try {
			_thisActivity.registerReceiver(mPushNotificationReceiver, intentFilter);
		} catch (Exception e){
			PillpopperLog.say(e);
		}
	}

	private BroadcastReceiver mPushNotificationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LoggerUtils.info("FCM -- registerPushNotificationReceiver onReceive");
			if(null != intent) {
				String title = intent.getStringExtra("title");
				String body = intent.getStringExtra("body");
				if (!Util.isEmptyString(title) && !Util.isEmptyString(body)) {
					LoggerUtils.info("FCM -- registerPushNotificationReceiver show dialog");
					GenericAlertDialog alertDialog = new GenericAlertDialog(PillpopperActivity.this,
							title,
							body,
							getResources().getString(R.string.ok_text),
							(dialogInterface, i) -> dialogInterface.dismiss());
					alertDialog.showDialog();
				} else {
					LoggerUtils.info("FCM -- Push notification missing required information");
				}
			}
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && _onSaveListener != null) {
			_onSaveListener.onClick(null);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		PillpopperLog.say("Pausing " + this.getClass().getName());
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		PillpopperLog.say("Destroying container activity " + this.getClass().getName());
		
		// kill the arguments that were passed to us
		if (isFinishing()) {
			get_globalAppContext().killArguments(this, getIntent());
		}
	}

	public LayoutInflater getInflater()
	{
		return _inflater;
	}

	public State getState()
	{
		return get_globalAppContext().getState(this);
	}

	public Context getAndroidContext()
	{
		return _thisActivity;
	}

	@Override
	public PillpopperAppContext getPillpopperContext()
	{
		return get_globalAppContext();
	}

	@Override
	public String getDebugName()
	{
		return getClass().getSimpleName();
	}

	@Override
	public Activity getActivityForMenu()
	{
		return this;
	}
	
	///
	// ActionBar support
	///
	
	protected void useActionBar(int title_id, int banner_to_hide)
	{
		ActionBar actionbar = getActionBar();
		// actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		/*
		 * If you NullPointerException here, it's because you didn't add
		 * android:theme="@android:style/Theme.Holo" to the activity's node in AndroidManifest.xml.
		 */
		actionbar.setDisplayUseLogoEnabled(true);
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setDisplayHomeAsUpEnabled(true);
		// actionbar.setIcon(null); // API level 14
		actionbar.setTitle(title_id);
		actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);

		/*
		 * If you NullPointerException here, it's because you called useActionBar() before setContentView().
		 */
		this.findViewById(banner_to_hide).setVisibility(View.GONE);
	}

	/*
	 * subclasses override to determine what the "<" button does in the ActionBar.
	 */
	protected void handleUpButton()
	{
		_thisActivity.setResult(RESULT_CANCELED);
		_thisActivity.finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	handleUpButton();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	/*
	 * This interface lets subclass code launch activities without knowing whether its an activity
	 * or a fragment. If activity code just uses "thisActivity", then when its later turned into
	 * a fragment, it'll end up getting replies sent back to the parent activity, not the fragment.
	 * Using this interface means such code keeps working meaningfully even after its enclosing class
	 * changes inheritance to a PillpopperFragment.
	 */
	public PillpopperReplyContext getReplyContext() {
		return this;
	}
	
	public Activity getActivity()
	{
		return this;
	}
	
	public PillpopperActivity getPillpopperActivity()
	{
		return this;
	}

	@Override
	public Resources getAndroidResources()
	{
		return getResources();
	}


}
