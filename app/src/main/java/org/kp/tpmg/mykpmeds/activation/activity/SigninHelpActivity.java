package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;

public class SigninHelpActivity extends AppCompatActivity implements OnClickListener {

    private TextView mVersionText;
    private Context mContext;
    private RecyclerView mRegionsRecyclerView;
    private LinearLayout mRegionslayout;
    private Typeface mRobotoBold;
    private Typeface mRobotoRegular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppConstants.isSecureFlg()) {
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.sign_in_help);
        mContext = this;

        RunTimeData.getInstance().setHomeButtonPressed(0);
        RunTimeData.getInstance().setClickFlg(false);
        mRobotoBold = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_BOLD);
        mRobotoRegular = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_REGULAR);
        initActionBar();
        initUI();
        setVersion();
        populateRegions();
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_SIGN_IN_HELP);
    }

    private void populateRegions() {
        String regions = Util.getKeyValueFromAppProfileRuntimeData("signInHelpRegionList");
        String[] regionsArray;
        if (!TextUtils.isEmpty(regions)) {
            regionsArray = regions.split(",");
            mRegionslayout.setVisibility(View.VISIBLE);
            RegionsAdapter mAdapter = new RegionsAdapter(mContext, regionsArray);
            mRegionsRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
            mRegionsRecyclerView.setHasFixedSize(true);
            mRegionsRecyclerView.addItemDecoration(new SpaceItemDecoration(Util.convertToDp(4, mContext)));
            mRegionsRecyclerView.setAdapter(mAdapter);
        } else {
            mRegionslayout.setVisibility(View.GONE);
        }
    }

    private void initActionBar() {
        Toolbar mToolbar = findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.signin_help_text_underlined));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setVersion() {
        try {
            String versionString = mContext.getResources().getString(R.string.version) + " " +
                    mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            mVersionText.setText(versionString);
        } catch (PackageManager.NameNotFoundException e) {
            LoggerUtils.exception(e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RunTimeData.getInstance().setHomeButtonPressed(0);
                onBackPressed();
                break;
            default:
                finish();
                break;
        }
        return true;
    }

    /**
     * Initializing the UI fields.
     */
    private void initUI() {
        TextView registerBtn = findViewById(R.id.tv_register);
        TextView forgotUseridBtn = findViewById(R.id.tv_forgot_user);
        TextView forgotPwdBtn = findViewById(R.id.tv_forgot_pwd);
        TextView memberServices = findViewById(R.id.tv_member_Services);
        TextView termsConditions = findViewById(R.id.tv_terms_conditions);
        TextView privacyPolicy = findViewById(R.id.tv_privacy_policy);
        mRegionsRecyclerView = findViewById(R.id.regions_table);
        mVersionText = findViewById(R.id.tv_version);
        mRegionslayout = findViewById(R.id.ll_regions);
        TextView mRegionsHeader = findViewById(R.id.region_header);
        TextView mQuestionOne = findViewById(R.id.tv_question_one);
        TextView mQuestionTwo = findViewById(R.id.tv_question_two);
        TextView mAnswerOne = findViewById(R.id.tv_answer_one);
        TextView mAnswerTwo = findViewById(R.id.tv_answer_two);
        mRegionsHeader.setTypeface(mRobotoBold);
        mQuestionOne.setTypeface(mRobotoBold);
        mQuestionTwo.setTypeface(mRobotoBold);
        memberServices.setTypeface(mRobotoRegular);
        mAnswerOne.setTypeface(mRobotoRegular);
        mAnswerTwo.setTypeface(mRobotoRegular);
        registerBtn.setTypeface(mRobotoRegular);
        forgotUseridBtn.setTypeface(mRobotoRegular);
        forgotPwdBtn.setTypeface(mRobotoRegular);
        mVersionText.setTypeface(mRobotoRegular);
        registerBtn.setOnClickListener(this);
        forgotUseridBtn.setOnClickListener(this);
        forgotPwdBtn.setOnClickListener(this);
        memberServices.setOnClickListener(this);
        termsConditions.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RunTimeData.getInstance().setClickFlg(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RunTimeData.getInstance().setClickFlg(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (!RunTimeData.getInstance().isClickFlg()) {
            RunTimeData.getInstance().setClickFlg(true);
            Intent intent = null;
            if (id == R.id.tv_register) {
                if (ActivationUtil.checkNetworkAvailablity(SigninHelpActivity.this)) {
                    if (!Util.isEmptyString(ActivationUtil.handleStrNull(AppConstants.getRegisterUrl()))) {
                        Uri uri = Uri.parse(AppConstants.getRegisterUrl());
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } else {
                        showAppProfileUrlErrorAlert();
                    }
                }

            } else if (id == R.id.tv_forgot_user) {

                if (ActivationUtil.checkNetworkAvailablity(SigninHelpActivity.this)) {
                    if (!Util.isEmptyString(ActivationUtil.handleStrNull(AppConstants.getUserIDHelpUrl()))) {
                        Uri uri = Uri.parse(AppConstants.getUserIDHelpUrl());
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } else {
                        showAppProfileUrlErrorAlert();
                    }
                }

            } else if (id == R.id.tv_forgot_pwd) {

                if (ActivationUtil.checkNetworkAvailablity(SigninHelpActivity.this)) {
                    if (!Util.isEmptyString(ActivationUtil.handleStrNull(AppConstants.getPasswordHelpUrl()))) {
                        Uri uri = Uri.parse(AppConstants.getPasswordHelpUrl());
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } else {
                        showAppProfileUrlErrorAlert();
                    }
                }

            } else if (id == R.id.tv_member_Services) {
                if (ActivationUtil.checkNetworkAvailablity(SigninHelpActivity.this)) {
                    if (!Util.isEmptyString(ActivationUtil.handleStrNull(AppConstants.getMemberServicesUrl()))) {
                        Uri uri = Uri.parse(AppConstants.getMemberServicesUrl());
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } else {
                        showAppProfileUrlErrorAlert();
                    }
                }
            } else if(id == R.id.tv_terms_conditions){
                if(null!=Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_WS_NON_SECURED_BASE_URL)) {
                    intent = new Intent(SigninHelpActivity.this,
                            WebViewActivity.class);
                    intent.putExtra("Type", "terms");
                    intent.putExtra("Source", "SignInHelp");
                    startActivity(intent);
                } else {
                    showAppProfileUrlErrorAlert();
                }
            }else if(id == R.id.tv_privacy_policy){
                if(null!=Util.getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_WS_NON_SECURED_BASE_URL)) {
                    intent = new Intent(SigninHelpActivity.this, WebViewActivity.class);
                    intent.putExtra("Type", "privacy");
                    intent.putExtra("Source", "SignInHelp");
                    startActivity(intent);
                }else{
                    showAppProfileUrlErrorAlert();
                }
            }
        }
    }

    private void showAppProfileUrlErrorAlert(){
        DialogHelpers.showAlertDialog(SigninHelpActivity.this, getString(R.string.app_profile_error_msg_2));
        RunTimeData.getInstance().setClickFlg(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == AppConstants.PERMISSION_PHONE_CALL_PHONE) {
                //permission obtained for making call
                //PermissionUtils.invokeCall(KpBaseActivity.this);
                if (ActivationUtil.isCallOptionAvailable(SigninHelpActivity.this)) {
                    PermissionUtils.invokeACall(SigninHelpActivity.this);
                }
            }
        } else {
            if (requestCode == AppConstants.PERMISSION_PHONE_CALL_PHONE) {
                if (permissions.length > 0) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        onPermissionDenied(requestCode);
                    } else {
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
}
