package com.montunosoftware.pillpopper.android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.TTGWebviewModel;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;


public class NonSecureWebviewFragment extends Fragment{

	protected Context mContext;
	protected Menu mMenu;
	protected TTGWebviewModel mWebViewModel;
	protected View mView;
	protected WebView mWebView;
	protected TextView mTxtVwError;
	protected Menu menu;
	protected boolean isWebLoadingIndicatorRunning;
	protected boolean mErrorFlg;
	private ProgressDialog mProgressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.lib_layout_webview, container,false);
		mContext = getActivity();
		mWebViewModel = (TTGWebviewModel) getArguments().getSerializable("webViewModel");
		mProgressDialog = new ProgressDialog(getActivity(), R.style.loading_theme);
		mProgressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
		mProgressDialog.setCancelable(false);

		return mView;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(mWebViewModel==null){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(getActivity().getString(R.string.unable_to_load_webview));
			builder.setTitle(getResources().getString(R.string._error));
			builder.setPositiveButton(getResources().getString(R.string._ok), (dialog, which) -> {
				dialog.dismiss();
				getActivity().finish();
			});

		}else{
			initUI();
			loadData(mWebViewModel);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	protected void initUI() {
		mWebView = mView.findViewById(R.id.lib_webview);
		mTxtVwError = mView.findViewById(R.id.lib_error);
		mWebView.getSettings().setCacheMode(
				WebSettings.LOAD_NO_CACHE);
		mWebView.setWebViewClient(new KpWebViewClient());
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mWebViewModel.getTitle());

		setHasOptionsMenu(true);
		mWebView.setVisibility(View.GONE);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAllowFileAccess(false);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.clearCache(true);
		PillpopperRunTime.getInstance().setWebViewInstance(mWebView);
	}

	protected void loadData(TTGWebviewModel model) {
		String url = model.getUrl();
		if(null!=url){
			if(getActivity()!=null && !getActivity().isFinishing() && !mProgressDialog.isShowing())
				mProgressDialog.show();
			isWebLoadingIndicatorRunning = true;
			mWebView.loadUrl(url);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(mWebViewModel.getCloseImageViewId() != TTGWebviewModel.NO_CLOSE_OPTION){
			inflater.inflate(R.menu.refill_close, menu);
		}
		if(mWebViewModel.getRefreshImageViewId() != TTGWebviewModel.NO_REFRESH_OPTION){
			inflater.inflate(R.menu.refill_refresh, menu);
		}

		this.mMenu = menu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			getActivity().finish();
		} else if (item.getItemId() == R.id.refresh_icon) {
			if (ActivationUtil.isNetworkAvailable(mContext)) {
				isWebLoadingIndicatorRunning = true;
				mTxtVwError.setVisibility(View.GONE);
				if (getActivity()!=null && !getActivity().isFinishing() && !mProgressDialog.isShowing())
					mProgressDialog.show();
				mWebView.loadUrl(mWebViewModel.getUrl());
			}else{
				if (null != getActivity() && getActivity().getSupportFragmentManager() != null
						&& getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
					DialogHelpers.showAlertDialogWithHeader(mContext, "Data Unavailable", "A network connection cannot be established. Please try again later.", () -> {

					});
				} else {
					Toast.makeText(
							mContext,
							"Previous action aborted",
							Toast.LENGTH_LONG).show();
				}
				Toast.makeText(getActivity().getApplicationContext(),
						"Refresh.....", Toast.LENGTH_SHORT).show();
			} }else if (item.getItemId() == R.menu.refill_close) {
			Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string._close),
					Toast.LENGTH_SHORT).show();
		}

		return true;
	}

	private boolean isPendingFlg;

	public class KpWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			view.clearView();
			isPendingFlg = false;
			if(!isWebLoadingIndicatorRunning && getActivity()!=null && !getActivity().isFinishing() && !mProgressDialog.isShowing()){
				mProgressDialog.show();
				isWebLoadingIndicatorRunning=true;
				isPendingFlg = false;
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, String url) {
			isPendingFlg = true;
			// mocking this url for now, since upgrade button in html page form action serving the url related to MDO.
			if(url.contains("details?id=org.kp.tpmg.android.mykpmeds")){
				try {
					// Try to launch the market url using installed play store application.
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.MARKET_URL)));
				} catch (Exception exception) {
					// If there is no play store application installed in device, launch the absolute url in browser.
					PillpopperLog.exception(exception.getMessage());
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.PLAY_STORE_URL)));
				}
			}else{
				if(!isWebLoadingIndicatorRunning && getActivity()!=null && !getActivity().isFinishing() && !mProgressDialog.isShowing()){
					mProgressDialog.show();
					isWebLoadingIndicatorRunning=true;
					isPendingFlg = false;
				}
				view.loadUrl(url);
			}
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			LoggerUtils.info("Webview -- URL onPageFinished -- " + url);
			if (url.equalsIgnoreCase("https://fmp.medimpact.com/mp/public/custom_okta_auth.jsp?iss=https%3A%2F%2Fmedimpact.okta.com") ||
					url.equalsIgnoreCase("https://fmp.medimpact.com/mp/public/custom_okta_auth.jsp?iss=https://Fmedimpact.okta.com")) {
				view.loadUrl("https://fmp.medimpact.com/mp/SSOLogin.do");
				RunTimeData.getInstance().isRxRefillAEMErrorPageLoaded = true;
			} else if(url.equalsIgnoreCase("https://www.optumrx.com/sso/kaiser/login.html?DeepLink=drugPrice")){
				view.loadUrl("https://www.optumrx.com/content/rxmember/default/en_us/angular-free/optumrx/public-errorpage.html");
				RunTimeData.getInstance().isRxRefillAEMErrorPageLoaded = true;
			} else {
				if (!isPendingFlg) {
					if (getActivity() != null && !getActivity().isFinishing() && null != mProgressDialog && mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}
					isWebLoadingIndicatorRunning = false;
				}
				if (!mErrorFlg) {
					mTxtVwError.setVisibility(View.GONE);
					view.setVisibility(View.VISIBLE);
					mWebViewModel.setUrl(url);
				}
			}
		}

		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
			super.onReceivedError(view, request, error);
			LoggerUtils.info("Webview -- URL onReceivedError -- " + request.getUrl().toString());
			if( (!request.getUrl().toString().contains("kp-icons.ttf"))){
				if (!request.getUrl().toString().contains("kp-icons.woff"))
				{
					if(!request.getUrl().toString().contains("favicon")) {
						handleOnReceiveError(view);
					}
				}
			}
		}

		private void handleOnReceiveError(WebView view) {
			mErrorFlg = true;
			mTxtVwError.setVisibility(View.VISIBLE);
			view.setVisibility(View.GONE);
			if (getActivity() != null && !getActivity().isFinishing() && null != mProgressDialog && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	}

	public WebView getWebView()
	{
		return mWebView;
	}

	@Override
	public void onDestroy() {
		if (getActivity() != null && !getActivity().isFinishing() && null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		super.onDestroy();
	}
}
