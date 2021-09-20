package org.kp.tpmg.mykpmeds.activation;


import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

@SuppressLint("Registered")
public class KpBaseActivity extends AppCompatActivity {

	private SharedPreferenceManager manager;
	private ActivationController mActivationController;
	private PillpopperAppContext _globalAppContext;

	public PillpopperAppContext getGlobalAppContext()
	{
		return get_globalAppContext();
	}

	@Override
	protected void onPause() {
		super.onPause();
		RunTimeData.getInstance().setAppVisibleFlg(false);		
	}

	@Override
	protected void onResume() {
		super.onResume();
		get_globalAppContext().kpMaybeLaunchLoginScreen(this);
		RunTimeData.getInstance().setAppVisibleFlg(true);	
		manager = SharedPreferenceManager.getInstance(this,AppConstants.AUTH_CODE_PREF_NAME);
		LoggerUtils.info(" KP Base activity onResume ");
		if (Util.isActiveInterruptSession() && !mActivationController.isSessionActive(this) ) {
			mActivationController.performSignoff(this);
            RunTimeData.getInstance().setInturruptScreenVisible(false);
			manager.remove(AppConstants.SSO_SESSION_ID);
			finish();
		}
	}

	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
		mActivationController.restartTimer(this);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AppConstants.isSecureFlg()) {
			getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE);
		}
		set_globalAppContext(PillpopperAppContext.getGlobalAppContext(this));
		mActivationController = ActivationController.getInstance();
		LoggerUtils.info("Starting the Timer ");
		mActivationController.startTimer(this);
		//}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			if (requestCode == AppConstants.PERMISSION_PHONE_CALL_PHONE) {
				//permission obtained for making call
				//PermissionUtils.invokeCall(KpBaseActivity.this);
				if (ActivationUtil.isCallOptionAvailable(KpBaseActivity.this)) {
					PermissionUtils.invokeACall(KpBaseActivity.this);
				}
			}
		} else {
			if (requestCode == AppConstants.PERMISSION_PHONE_CALL_PHONE) {
				if (permissions.length > 0) {
					if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
						//permission Denied with never ask again not checked
						onPermissionDenied(requestCode);

					} else {
						//permission Denied with never ask again checked
						onPermissionDeniedNeverAskAgain(requestCode);
					}
				}
			}

		}

	}

	public void onPermissionDeniedNeverAskAgain(int requestCode) {
		String message = PermissionUtils.permissionDeniedMessage(requestCode, this);
		PermissionUtils.permissionDeniedDailogueForNeverAskAgain(this, message);
	}

	public void onPermissionDenied(int requestCode) {
		String message = PermissionUtils.permissionDeniedMessage(requestCode, this);
		PermissionUtils.permissionDeniedDailogue(this, message);
	}

	public PillpopperAppContext get_globalAppContext() {
		return _globalAppContext;
	}

	public void set_globalAppContext(PillpopperAppContext _globalAppContext) {
		this._globalAppContext = _globalAppContext;
	}

}