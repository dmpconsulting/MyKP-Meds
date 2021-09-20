package org.kp.tpmg.mykpmeds.activation.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.RunTimeConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperApplication;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class LoadingActivity extends Activity {
	private Intent mIntent;
	@SuppressWarnings("unused")
	private LoadingActivity mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AppConstants.isSecureFlg()) {
			getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE);
		}
		RunTimeData.getInstance().setHomeButtonPressed(0);
		mIntent = getIntent();
		mContext = this;
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(mContext, AppConstants.AUTH_CODE_PREF_NAME);
		setContentView(R.layout.actlib_activity_transparent_frame);
		RunTimeData.getInstance().setLoadingInProgress(true);
		loadData();
	}

	private void loadData() {

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
		RunTimeConstants.getInstance().setNotificationSuppressor(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mContext = this;
		getWindow().getDecorView().setSystemUiVisibility(
				SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		RunTimeData.getInstance().setAppVisibleFlg(true);
		RunTimeConstants.getInstance().setNotificationSuppressor(false);
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

	@Override
	protected void onDestroy() {
		RunTimeData.getInstance().setLoadingInProgress(false);
		mContext = null;
		super.onDestroy();
	}
}