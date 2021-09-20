package com.montunosoftware.pillpopper.network.utils;

import android.content.Context;
import android.webkit.CookieManager;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by M1023050 on 17-Nov-18.
 */

public class RequestUtils {


    public static Map<String,String> prepareHeaders(Context context){
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Cookie", prepareCookies(context));
        return headersMap;
    }


    public static String prepareCookies(Context context){
        try {
            if(null!= ActivationController.getInstance().getSSOSessionId(context)){
                String setCookie = new StringBuilder(AppConstants.ConfigParams.getKeepAliveCookieName()+"="+ URLEncoder.encode(ActivationController.getInstance().getSSOSessionId(context),"utf-8"))
                        .append("; domain=").append(AppConstants.ConfigParams.getKeepAliveCookieDomain())
                        .append("; path=").append(AppConstants.ConfigParams.getKeepAliveCookiePath())
                        .toString();
                PillpopperServer.storeCookies(setCookie);
                return CookieManager.getInstance().getCookie("https://" + AppConstants.ConfigParams.getKeepAliveCookieDomain());
            }
        } catch (Exception e){
            PillpopperLog.exception(e.getMessage());
        }
        return "";
    }

}
