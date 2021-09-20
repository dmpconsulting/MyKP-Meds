package org.kp.tpmg.mykpmeds.activation.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperApplication;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

public class TransparentLoadingActivity extends Activity {
	private Intent mIntent;
	@SuppressWarnings("unused")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AppConstants.isSecureFlg()) {
			getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE);
		}
		RunTimeData.getInstance().setHomeButtonPressed(0);
		mIntent = getIntent();
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(this, AppConstants.AUTH_CODE_PREF_NAME);
		setContentView(R.layout.actlib_activity_transparent_frame);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		((PillpopperApplication)getApplication()).setAppIsInBackground(!hasFocus);
	}

	@Override
	public void onBackPressed() {
		return;
	}

	@Override
	protected void onPause() {
		super.onPause();
		RunTimeData.getInstance().setAppVisibleFlg(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		RunTimeData.getInstance().setAppVisibleFlg(true);
		RunTimeData.getInstance().setHomeButtonPressed(0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (getIntent().getBooleanExtra("needHomeButtonEvent", false) && !"simple".equalsIgnoreCase(mIntent.getStringExtra("type"))) {
			RunTimeData.getInstance().setHomeButtonPressed(
					getIntent().getIntExtra("homeButtonEvent", 0));
			finish();
		}
	}
}