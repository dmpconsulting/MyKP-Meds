package com.montunosoftware.pillpopper.android;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.PageList;
import com.montunosoftware.pillpopper.model.Phone;
import com.montunosoftware.pillpopper.model.Region;
import com.montunosoftware.pillpopper.network.NetworkAPI;
import com.montunosoftware.pillpopper.network.NetworkClient;
import com.montunosoftware.pillpopper.network.utils.RequestUtils;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Created by M1030430 on 7/13/2016.
 */
public class PrivacyAndTCFragment extends Fragment {
    private WebView mWebViewHolder;
    private TextView mTxtVwError;
    private String mIndexUrl = "";
    private boolean mIsLoadingFromLocal = false;
    private ProgressDialog mProgressDialog;
    private boolean mErrorFlg;
    private StringBuilder mLoadingURL = new StringBuilder();
    private Intent mIntent;
    private LinearLayout mPharmacyCenter;
    private LinearLayout mSupportContainer;
    private AlertDialog mDialog;
    private TextView mPharmacyHeader;
    private TextView mPharmacyNumber;
    private TextView mPharmacyTimings;
    private TextView mPharmacyInfo;
    private Typeface mFontRegularTypeFace;
    private Typeface mFontMediumTypeFace;
    private LinearLayout mApptNAdvice;
    private LinearLayout mApptNAdviceRoot;
    private LinearLayout mFooterLayout;
    private TextView mFooterView;
    private TextView mHeaderTitle;
    private ClickableSpan mClickableSpan;
    private SpannableStringBuilder mSpannableStringBuilder;
    private Region mRegion;
    private final String FILE_NAME = "RegionContacts.txt";
    private View inflatorView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        inflatorView = inflater.inflate(R.layout.generic_webview,container,false);

        mIntent = getActivity().getIntent();
        mIndexUrl = getArguments().getString("url");
        initActionBar();
        initUI();
        loadData();
        mFontMediumTypeFace = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_MEDIUM);
        mFontRegularTypeFace = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_REGULAR);
        return inflatorView;
    }

    private void initUI() {
        mTxtVwError = inflatorView.findViewById(R.id.error_txt);
        mWebViewHolder =  inflatorView.findViewById(R.id.web_view);
        mWebViewHolder.getSettings().setJavaScriptEnabled(Util.checkForJavaScriptEnablingOption(getArguments().getString("Type")));
        mWebViewHolder.getSettings().setAllowFileAccessFromFileURLs(true);
        mWebViewHolder.getSettings().setUserAgentString(AppConstants.URL_BASE_PARAMETERS);
        mWebViewHolder.getSettings().setDomStorageEnabled(true);
        mWebViewHolder.setWebViewClient(new KpWebViewClient());
        mSupportContainer =  inflatorView.findViewById(R.id.ll_support_web_container);
        mPharmacyCenter =  inflatorView.findViewById(R.id.ll_MID_call_center);
        mPharmacyHeader =  inflatorView.findViewById(R.id.pharmacy_header);
        mPharmacyTimings =  inflatorView.findViewById(R.id.availability);
        mPharmacyNumber =  inflatorView.findViewById(R.id.pharmacy_ph_no);
        mPharmacyInfo = inflatorView.findViewById(R.id.pharmacy_info);
        mApptNAdvice = inflatorView.findViewById(R.id.appt_n_adv);
        mApptNAdviceRoot = inflatorView.findViewById(R.id.appt_n_adv_root);
        mFooterLayout = inflatorView.findViewById(R.id.footer_layout);
        mFooterView = inflatorView.findViewById(R.id.footer_view);
        mHeaderTitle = inflatorView.findViewById(R.id.page_description);
        mPharmacyHeader.setTypeface(mFontMediumTypeFace);
        mPharmacyNumber.setTypeface(mFontRegularTypeFace);
        mPharmacyTimings.setTypeface(mFontRegularTypeFace);
        mPharmacyInfo.setTypeface(mFontRegularTypeFace);
    }

    public void initActionBar() {
        Toolbar mToolbar = inflatorView.findViewById(R.id.app_bar);
        mToolbar.setVisibility(View.GONE);
    }

    private void loadData() {
        if ("guide".equalsIgnoreCase(getArguments().getString("Type"))) {
            mWebViewHolder.loadUrl(mIndexUrl, ActivationUtil.getGuideHeaders(getActivity()));
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_GUIDE);
        } else {
            showProgressBar();
            if (mIndexUrl.equals(getResources().getString(R.string.lbl_app_support))) {

                mPharmacyCenter.setVisibility(View.GONE);
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_APP_SUPPORT);

                if (ActivationUtil.isNetworkAvailable(getActivity())) {
                    mWebViewHolder.loadUrl(AppConstants.getAppSupportURL(getActivity()));
                }

            } else if (mIndexUrl.equals(getResources().getString(R.string.lbl_privacy_statement))) {

                mPharmacyCenter.setVisibility(View.GONE);
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_PRIVACY_STATEMENT);
                if (ActivationUtil.isNetworkAvailable(getActivity())) {
                    mWebViewHolder.loadUrl(AppConstants.getPrivacyPracticeURL(getActivity()));
                }
            } else if (mIndexUrl.equals(getResources().getString(R.string.lbl_term_and_conditions))) {

                FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_TERMS_CONDITIONS);
                if (ActivationUtil.isNetworkAvailable(getActivity())) {
                    mWebViewHolder.loadUrl(AppConstants.getTermsNConditionsURL(getActivity()));
                }
            } else if (mIndexUrl.equals(getResources().getString(R.string.lbl_appointments_and_advice))) {
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_APPOINTMENT_ADVICE);
                performRegionContactsAPICall(getResources().getString(R.string.lbl_appointments_and_advice));
            } else if (mIndexUrl.equals(getResources().getString(R.string._pharmacy))) {
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_PHARMACY_CALL_CENTER);
                performRegionContactsAPICall(getResources().getString(R.string._pharmacy));
            }
        }
    }

    private void performRegionContactsAPICall(String screenName) {
        if (RunTimeData.getInstance().isRegionContactAPICallRequired()) {
            loadRegions(screenName);
        } else {
            loadFileContent(screenName);
        }
    }

    private void loadFileContent(String screenName) {
        if (!"".equalsIgnoreCase(Util.readFileAsString(getContext(), FILE_NAME))) {
            mRegion = Util.parseRegionJson(Util.readFileAsString(getContext(), FILE_NAME), ActivationController.getInstance().fetchUserRegion(getContext()));
            if (screenName.equalsIgnoreCase(getResources().getString(R.string.lbl_appointments_and_advice))) {
                showPhoneContent(getResources().getString(R.string.lbl_appointments_and_advice));
            } else {
                showPhoneContent(getResources().getString(R.string._pharmacy));
            }
        } else {
            //show alert with retry cancel
            showAPIFailedAlert(screenName);
        }
    }

    private void loadRegions(String screenName) {

        if (Util.isEmptyString(Util.getKeyValueFromAppProfileRuntimeData("regionalContactsURL"))) {
            return;
        }
        if (Util.isNetworkAvailable(getContext())) {
            NetworkAPI regionResponseCallService = NetworkClient.getInstance().prepareClient(Util.getKeyValueFromAppProfileRuntimeData("regionalContactsURL").concat("/"))
                    .create(NetworkAPI.class);
            Call<ResponseBody> regionResponseCall = regionResponseCallService.getRegionsContacts(RequestUtils.prepareHeaders(getContext()));
            regionResponseCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            RunTimeData.getInstance().setRegionContactAPICallRequired(false);
                            if (null != response.body()) {
                                String jsonResponse = response.body().string();
                                mRegion = Util.getRegion(jsonResponse, ActivationController.getInstance().fetchUserRegion(getContext()));
                                //Writing the JSON response in file.
                                Util.writeStringAsFile(getContext(), jsonResponse, FILE_NAME);
                                dismissProgressDialogue();
                                showPhoneContent(screenName);
                            }
                        } catch (Exception e) {
                            LoggerUtils.exception(e.getMessage());
                        }
                    } else {
                        showAPIFailedAlert(screenName);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                if there is no key/value pair in AppProfile or the file doesn't exist.
                    dismissProgressDialogue();
                    GenericAlertDialog dialog = new GenericAlertDialog(getContext(), null,
                            getResources().getString(R.string.region_error_alert_message), getResources().getString(R.string.ok_text),
                            ((dialogInterface, i) -> {
                                dismissProgressDialogue();
                                dialogInterface.dismiss();
                                getActivity().finish();
                            }), null, null);

                    if (!dialog.isShowing()) {
                        dialog.showDialog();
                    }
                }
            });
        } else {
            loadFileContent(screenName);
        }
    }

    private void showAPIFailedAlert(String screenName) {
        GenericAlertDialog dialog = new GenericAlertDialog(getContext(), null,
                getResources().getString(R.string.region_error_alert_message), PrivacyAndTCFragment.this.getString(R.string.retry_btn_text),
                ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    loadRegions(screenName);
                }),
                getResources().getString(R.string.cancel),
                (dialogInterface, i) -> {
                    dismissProgressDialogue();
                    dialogInterface.dismiss();
                    getActivity().finish();
                });

        if (!dialog.isShowing()) {
            dialog.showDialogWithoutBtnPadding();
        }
    }

    private void showPhoneContent(String department) {

        mProgressDialog.dismiss();
        mSupportContainer.setVisibility(View.GONE);
        mPharmacyCenter.setVisibility(View.GONE);
        mApptNAdviceRoot.setVisibility(View.VISIBLE);

        if (null != department) {
            populatePhoneNumber(department);
        }
    }

    private void populatePhoneNumber(String department) {
        if (null != mRegion && !mRegion.getPageList().isEmpty()) {
            for (PageList pageList : mRegion.getPageList()) {
                if (getResources().getString(R.string.lbl_appointments_and_advice).equalsIgnoreCase(department)) {
                    if ("AACC".equalsIgnoreCase(pageList.getPageType())) {
                        setHeaderAndFooter(department, pageList);
                    }
                } else {
                    if ("pharmacy".equalsIgnoreCase(pageList.getPageType())) {
                        setHeaderAndFooter(department, pageList);
                    }
                }
            }
        }
    }

    private void setHeaderAndFooter(String department, PageList pageList) {
        if (TextUtils.isEmpty(pageList.getPageDescription())) {
            mHeaderTitle.setVisibility(View.GONE);
        } else {
            mHeaderTitle.setVisibility(View.VISIBLE);
            mHeaderTitle.setText(pageList.getPageDescription());
        }

        for (Phone ph : pageList.getPhones()) {
            inflateDynamicView(ph);
        }

        if (TextUtils.isEmpty(pageList.getPageFooter())) {
            mFooterLayout.setVisibility(View.GONE);
        } else {
            mFooterLayout.setVisibility(View.VISIBLE);
            String footerText = pageList.getPageFooter();
            Spanned result = null;
            if(!Util.isEmptyString(footerText)){
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(footerText, Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(footerText);
                }
            }
            mFooterView.setText(result);
            mFooterView.setMovementMethod(LinkMovementMethod.getInstance());

        }
    }


    private void inflateDynamicView(Phone phone) {
        LayoutInflater inflator = getLayoutInflater();
        View childView = inflator.inflate(R.layout.child_phn_layout, null);
        TextView mTitle = childView.findViewById(R.id.title);
        TextView mPhoneNumber = childView.findViewById(R.id.phn_number);
        TextView mShortDesc = childView.findViewById(R.id.short_desc);
        mApptNAdvice.addView(childView);
        mPhoneNumber.setOnClickListener(view -> makeCall(phone.getPhoneNumber(), "Guide"));
        mTitle.setText(phone.getTitle());
        mPhoneNumber.setText(phone.getPhoneNumber());
        if (TextUtils.isEmpty(phone.getShortDescription())) {
            mShortDesc.setVisibility(View.GONE);
        } else {
            mShortDesc.setVisibility(View.VISIBLE);
            mShortDesc.setText(phone.getShortDescription());
        }

        mClickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ContextCompat.getColor(getContext(), R.color.login_subtitle_color));
                ds.setTypeface(Typeface.DEFAULT);
                ds.setUnderlineText(true);
            }

            @Override
            public void onClick(View view) {
            }
        };

        mSpannableStringBuilder = new SpannableStringBuilder(phone.getPhoneNumber());
        mSpannableStringBuilder.setSpan(
                mClickableSpan, // Span to add
                0, // Start of the span (inclusive)
                phone.getPhoneNumber().length(), // End of the span (exclusive)
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE // Do not extend the span when text add later
        );
        mPhoneNumber.setText(mSpannableStringBuilder);
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("guide".equalsIgnoreCase(mIntent.getStringExtra("Type"))) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.web_menu, menu);
        }
        return true;
    }
*/

    private void makeCall(String phoneNoStr, String title) {
        if (mDialog == null || !mDialog.isShowing()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setTitle(title);
            alertDialog.setMessage(phoneNoStr);
            alertDialog.setCancelable(true);
            alertDialog.setPositiveButton(getResources().getString(R.string.call),
                    (dialog, which) -> {
                        dialog.dismiss();

                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNoStr.replace("-", "")));
                        RunTimeData.getInstance().setRunTimePhoneNumber(phoneNoStr);
                        if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_PHONE_CALL_PHONE, Manifest.permission.CALL_PHONE, getActivity())) {
                            startActivity(intent);
                        }

                    });
            alertDialog.setNegativeButton(getResources().getString(R.string.cancelAlert),
                    (dialog, which) -> dialog.dismiss());

            mDialog = alertDialog.create();
            RunTimeData.getInstance().setAlertDialogInstance(mDialog);
            mDialog.show();
            RunTimeData.getInstance().setAlertDialogInstance(mDialog);
            TextView tv = mDialog.findViewById(android.R.id.message);
            tv.setGravity(Gravity.CENTER);
            Button btnPositive = mDialog.findViewById(android.R.id.button1);
            Button btnNegative = mDialog.findViewById(android.R.id.button2);

            btnPositive.setTextColor(Util.getColorWrapper(getContext(), R.color.kp_theme_blue));
            btnNegative.setTextColor(Util.getColorWrapper(getContext(), R.color.kp_theme_blue));
        }
    }

    private void clearWebView() {
        mWebViewHolder.loadUrl("about:blank");
    }

    public class KpWebViewClient extends WebViewClient {
        private boolean isPending;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            view.setVisibility(View.VISIBLE);
            isPending = false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {

            if ((url.startsWith("tel:") || url.startsWith("mobilecare:phone:"))) {

                String mPhoneNoStr = url.substring(url.lastIndexOf('=') + 1);
                String mNumber = mPhoneNoStr.replace("tel:", "");

                if (mIndexUrl.equalsIgnoreCase(getResources().getString(R.string.lbl_app_support))) {
                    makeCall(mNumber, getResources().getString(R.string.lbl_app_support));
                } else if (mIndexUrl.equalsIgnoreCase(getResources().getString(R.string.lbl_privacy_statement))) {
                    makeCall(mNumber, getResources().getString(R.string.lbl_privacy_statement));
                } else{
                    makeCall(mNumber, "");//in any other screen
                }

            } else if (getResources().getString(R.string.lbl_app_support).equalsIgnoreCase(mIndexUrl) && url.startsWith("mailto:")) {
                Intent email = ActivationUtil.callMailClient(url.replace("mailto:", ""), getSubjectLine(getString(R.string.subject_line)), "");
                startActivity(email);
                return true;
            } else if ("Terms and Conditions".equalsIgnoreCase(mIndexUrl) && (url.contains("healthy.kaiserpermanente.org") ||
                    url.contains("kaiserpermanente.org") || url.contains("kp.org"))) {
                launchExternalBrowser(url);
                return true;
            } else if (getResources().getString(R.string.lbl_privacy_statement).equalsIgnoreCase(mIndexUrl) && (url.startsWith("http"))) {
                isPending = true;
                view.loadUrl(url);
            } else if (url.startsWith("mobilecare:privacy")) {
                mIntent.putExtra("url", getResources().getString(R.string.lbl_privacy_statement));
                startActivity(mIntent);
            } else if(url.startsWith("https:") || url.startsWith("http:")){
                launchExternalBrowser(url);
            }
            return true;
        }

        private void launchExternalBrowser(String url) {
            try {
                isPending = true;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch(Exception ex){
                LoggerUtils.exception(ex.getMessage());
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (null != getActivity()) {
                getActivity().finishActivity(0);
            }
            if (null != mProgressDialog && mProgressDialog.isShowing() && !isPending) {
                mProgressDialog.dismiss();
            }
            if (!mErrorFlg && null != getContext()) {
                if (ActivationUtil.isNetworkAvailable(getContext())) {
                    mTxtVwError.setVisibility(View.GONE);
                    view.setVisibility(View.VISIBLE);
                } else {
                    if (mIsLoadingFromLocal) {
                        mTxtVwError.setVisibility(View.GONE);
                        view.setVisibility(View.VISIBLE);
                    } else {
                        mTxtVwError.setVisibility(View.VISIBLE);
                        view.setVisibility(View.GONE);
                    }
                }
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (null != mProgressDialog && mProgressDialog.isShowing() && !isPending) {
                mProgressDialog.dismiss();
            }
            mErrorFlg = true;
            mTxtVwError.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        }

    }

    private String getSubjectLine(String message) {
        StringBuilder subjectLine = new StringBuilder();
        subjectLine.append(message).append(" ").append(Util.getUserRegionValue(ActivationController.getInstance().fetchUserRegion(getContext()))).append(":");
        subjectLine.append(" v").append(Util.getAppVersion(getContext()));
        subjectLine.append(", ").append(AppConstants.PHONE_ANDROID);
        subjectLine.append(Util.getOSVersion()).append(", ").append(AppConstants.ANDROID_DEVICE_MAKE);
        return subjectLine.toString();
    }

    private void showProgressBar() {
        mTxtVwError.setVisibility(View.GONE);
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage("Please Wait...");
        mProgressDialog.setCancelable(true);
//      if the condition is removed it will throw bad token exception.
        if (getActivity().isFinishing()) {
            mProgressDialog.dismiss();
        } else {
            if (null != mProgressDialog && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }
    }

    private void dismissProgressDialogue() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close:
                getActivity().finish();
                return true;
            case android.R.id.home:
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                clearWebView();
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
