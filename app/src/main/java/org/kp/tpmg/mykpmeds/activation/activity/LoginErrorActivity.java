package org.kp.tpmg.mykpmeds.activation.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.ActivationError;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ErrorMessageUtil;

public class LoginErrorActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AppConstants.isSecureFlg()) {
			getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.actlib_error_alert_layout);
		initUI();
	}

	private void initUI() {
		ErrorMessageUtil errorMessageHandler = new ErrorMessageUtil();
		ActivationError errorDetails = null;
		String title = "";
		String msg = getIntent().getStringExtra("errormsg");
		if(msg != null && msg.length() >0)
		{
			 title= getIntent().getStringExtra("errortitle");
		}else{
			errorDetails = errorMessageHandler.getErrorDetails(getIntent().getIntExtra("status", -1));
			title = errorDetails.getTitle();
			msg = errorDetails.getMessage();
		}
		
		TextView alertMsgTxtVw = findViewById(R.id.textview_msg);
		TextView alertTitleTxtVw = findViewById(R.id.textview_title);
		alertMsgTxtVw.setText(msg);
		alertTitleTxtVw.setText(title);
		Button btnSignin = findViewById(R.id.cancel_button);
		btnSignin.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		startActivity(new Intent(LoginErrorActivity.this,
				LoginActivity.class));
		finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		RunTimeData.getInstance().setHomeButtonPressed(0);
		startActivity(new Intent(LoginErrorActivity.this,
				LoginActivity.class));
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (RunTimeData.getInstance().getHomeButtonPressed() == 9
				&& !new ActivationController().isSessionActive(LoginErrorActivity.this)) {
			RunTimeData.getInstance().setHomeButtonPressed(0);
			startActivity(new Intent(LoginErrorActivity.this,
					LoginActivity.class));
			finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		RunTimeData.getInstance().setHomeButtonPressed(9);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		RunTimeData.getInstance().setHomeButtonPressed(0);
	}
}