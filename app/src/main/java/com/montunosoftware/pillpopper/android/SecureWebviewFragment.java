package com.montunosoftware.pillpopper.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.model.TTGCookie;
import com.montunosoftware.pillpopper.model.TTGSecureWebViewModel;

import java.net.URLEncoder;
import java.util.List;

public class SecureWebviewFragment extends NonSecureWebviewFragment {

	private String mCookieDomainStr;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		createWebViewCookie();
		super.onActivityCreated(savedInstanceState);
	}

    private void createWebViewCookie() {
        try {
            final CookieManager cookieManager = CookieManager.getInstance();
            List<TTGCookie> cookieList = ((TTGSecureWebViewModel) mWebViewModel)
                    .getCookies();
            //Below if blocked - Reason - Before it use to sync only one cookie. So implemented for loop to sync all the cookie to cookie manager.
            for (int i = 0; i < cookieList.size(); i++) {
                TTGCookie mCookie = cookieList.get(i);
                if (mCookie == null || mCookie.getDomain() == null
                        || mCookie.getValue() == null) {
                    return;
                }
                if (!(mCookie.getDomain().startsWith("https://") || mCookie
                        .getDomain().startsWith("http://"))) {
                    mCookieDomainStr = "https://" + mCookie.getDomain();
                }
                String mCookieValurStr = mCookie.getName() + "="
                        + URLEncoder.encode(mCookie.getValue(), "utf-8");
                cookieManager.setAcceptCookie(true);
                cookieManager.setCookie(mCookieDomainStr, mCookieValurStr);
                cookieManager.flush();
            }
        } catch (Exception e) {
            PillpopperLog.say(e.getMessage());
        }
    }
}
