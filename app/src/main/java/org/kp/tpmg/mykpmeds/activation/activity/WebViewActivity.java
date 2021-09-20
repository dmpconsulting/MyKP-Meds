package org.kp.tpmg.mykpmeds.activation.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebViewActivity extends AppCompatActivity {
    private boolean mErrorFlg;
    private String mClassTypeStr;
    private WebView mWebViewHolder;
    private Intent mIntent;
    private TextView mTxtVwError;
    private String mSource;
    private boolean isFromTermAndCondition;
    private int backCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppConstants.isSecureFlg()) {
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.actlib_activity_web_view);
        RunTimeData.getInstance().setHomeButtonPressed(0);

        Toolbar mToolbar = findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setIcon(android.R.color.transparent);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();
        initUI();
        loadData();

    }

    private void loadData() {
        mIntent = getIntent();
        mClassTypeStr = mIntent.getStringExtra("Type");
        mSource = mIntent.getStringExtra("Source");
        Intent intent = new Intent(WebViewActivity.this, LoadingActivity.class);
        intent.putExtra("needHomeButtonEvent", true);
        intent.putExtra("type", "simple");
        intent.putExtra("homeButtonEvent", 7);
        startActivityForResult(intent, 0);
        if (mClassTypeStr != null) {
            if ("privacy".equalsIgnoreCase(mClassTypeStr)) {

                getSupportActionBar().setTitle(R.string.actionbar_title_privacy_practice);
                mWebViewHolder.loadUrl(AppConstants.getPrivacyPracticeURL(this));

            } else if ("terms".equalsIgnoreCase(mClassTypeStr)) {

                isFromTermAndCondition = true;
                backCount = 0;

                getSupportActionBar().setTitle(R.string.termsNcon);
                mWebViewHolder.loadUrl(AppConstants.getTermsNConditionsURL(getApplicationContext()));
                LoggerUtils.info("Privacy practice url" + AppConstants.getTermsNConditionsURL(this));

                LoggerUtils.info("s has been loaded successfully.");
            } else if ("guide".equalsIgnoreCase(mClassTypeStr)) {
                getSupportActionBar().setTitle(R.string.txt_guide);
                mWebViewHolder.loadUrl(AppConstants.ConfigParams.getFaqURL());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("guide".equalsIgnoreCase(mClassTypeStr)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.web_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RunTimeData.getInstance().setHomeButtonPressed(0);
                onBackPressed();
                break;
            case R.id.close:
                onBackPressed();
                break;
            default:
                finish();
                break;
        }
        return true;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initUI() {
        LoggerUtils.info("Starting to initialize web view.");
        mTxtVwError = findViewById(R.id.error);
        mWebViewHolder = findViewById(R.id.web_view);
        mWebViewHolder.setVisibility(View.GONE);
        mWebViewHolder.setWebViewClient(new KpWebViewClient());
        mWebViewHolder.getSettings().setJavaScriptEnabled(true);
        mWebViewHolder.getSettings().setAllowFileAccess(false);
    }

    public class KpWebViewClient extends WebViewClient {
        String mPhoneNoStr;
        private boolean isPending;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            view.clearView();
            isPending = false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!isPending) {
                finishActivity(0);
                //mWebViewHolder.setVisibility(View.VISIBLE);
            }
            if (!mErrorFlg) {
                mTxtVwError.setVisibility(View.GONE);
                view.setVisibility(View.VISIBLE);
            }
        }

        @SuppressWarnings("ResourceType")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if ((url.startsWith("tel:") || url.startsWith("mobilecare:phone:")) && "privacy".equalsIgnoreCase(mClassTypeStr)) {

                try {
                    mPhoneNoStr = url.substring(url.lastIndexOf('=') + 1);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(WebViewActivity.this);
                    alertDialog.setTitle(getString(R.string.actionbar_title_privacy_practice));
                    final String formattedNumber = formatPhoneNumber(mPhoneNoStr.replace("tel:", ""));
                    alertDialog.setMessage(formattedNumber);
                    alertDialog.setCancelable(false);

                    alertDialog.setPositiveButton("Call",
                            (dialog, which) -> {
                                dialog.dismiss();
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(mPhoneNoStr));
                                RunTimeData.getInstance().setRunTimePhoneNumber(formattedNumber);
                                if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_PHONE_CALL_PHONE, Manifest.permission.CALL_PHONE, WebViewActivity.this)) {
                                    startActivity(intent);
                                }
                            });
                    alertDialog.setNegativeButton("Cancel",
                            (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = alertDialog.create();
                    if (!isFinishing() && !dialog.isShowing()) {
                        dialog.show();
                        RunTimeData.getInstance().setAlertDialogInstance(dialog);
                    }
                    TextView tv = dialog.findViewById(android.R.id.message);
                    tv.setGravity(Gravity.CENTER);

                    Button btnPositive = dialog.findViewById(android.R.id.button1);
                    Button btnNegative = dialog.findViewById(android.R.id.button2);

                    btnPositive.setTextColor(ActivationUtil.getColorWrapper(WebViewActivity.this, R.color.kp_next_color));
                    btnNegative.setTextColor(ActivationUtil.getColorWrapper(WebViewActivity.this, R.color.kp_next_color));
                }catch (Exception e){
                    LoggerUtils.exception(e.getMessage());
                }
            } else if ("terms".equalsIgnoreCase(mClassTypeStr) && url.startsWith("mailto:")) {
                if (url.contains("tpmgcopyrightagent")) {
                    return true;
                }
                Intent email = ActivationUtil.callMailClient(url.replace("mailto:", ""), "My KP Meds Support", "");
                startActivity(email);
                return true;

            } else if ("terms".equalsIgnoreCase(mClassTypeStr) && (url.contains("healthy.kaiserpermanente.org") ||
                    url.contains("kaiserpermanente.org") || url.contains("kp.org"))) {
                isPending = true;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            } else if ("privacy".equalsIgnoreCase(mClassTypeStr) && (url.startsWith("http"))) {
                isPending = true;
                view.loadUrl(url);
                if(isFromTermAndCondition)
                {
                    backCount = 1;
                }

            } else if (url.startsWith("mobilecare:privacy")) {
                mIntent.putExtra("Type", "privacy");
                loadData();
                isPending = true;
            }
            return true;
        }


        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            mErrorFlg = true;
            mTxtVwError.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }
    }

    public static String formatPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile("\\d-\\d{3}-\\d{3}-\\d{4}");
        Matcher matcher = pattern.matcher(phoneNumber);
        if (!matcher.matches()) {
            MessageFormat phoneFormat = new MessageFormat("{0}-{1}-{2}-{3}");
            String[] phoneNumberChunks = {phoneNumber.substring(0, 1), phoneNumber.substring(1, 4), phoneNumber.substring(4, 7), phoneNumber.substring(7, 11)};
            return phoneFormat.format(phoneNumberChunks);
        }
        return phoneNumber;
    }

    @Override
    public void onBackPressed() {

        if(isFromTermAndCondition && backCount == 0)
        {
            mClassTypeStr = "terms";
        }
        if(backCount == 1)
        {
            mClassTypeStr = "privacy";
            backCount = 0;
        }

        if (mWebViewHolder.canGoBack()) {
            mWebViewHolder.goBack();
            // getSupportActionBar().setTitle(R.string.termsNcon);

            if ("privacy".equalsIgnoreCase(mClassTypeStr)) {
                getSupportActionBar().setTitle(R.string.actionbar_title_privacy_practice);
            } else  {
                getSupportActionBar().setTitle(R.string.termsNcon);
            }
            // mClassTypeStr = "terms";
        } else {
            super.onBackPressed();
            RunTimeData.getInstance().setHomeButtonPressed(0);
            if (mClassTypeStr.equalsIgnoreCase("guide")) {
                finish();
            } else {
                finish();
                Intent loginIntent = new Intent(this, (null!=mSource && "SignIn".equalsIgnoreCase(mSource))?LoginActivity.class:SigninHelpActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * if (RunTimeData.getInstance().getHomeButtonPressed() == 7 && !new
         * UserSessionManager().isSessionActive(WebViewActivity.this)) {
         * RunTimeData.getInstance().setHomeButtonPressed(0); Intent intent =
         * new Intent(WebViewActivity.this, LoginActivity.class);
         * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
         * Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent); finish(); }
         */
    }

    @Override
    protected void onStop() {
        super.onStop();
        RunTimeData.getInstance().setHomeButtonPressed(7);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RunTimeData.getInstance().setHomeButtonPressed(0);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == AppConstants.PERMISSION_PHONE_CALL_PHONE) {
                //permission obtained for making call
                //PermissionUtils.invokeCall(KpBaseActivity.this);
                if (ActivationUtil.isCallOptionAvailable(this)) {
                    PermissionUtils.invokeACall(this);
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
}

