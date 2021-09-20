package org.kp.tpmg.mykpmeds.activation.util;

import android.webkit.CookieManager;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.ttgmobilelib.controller.TTGCallBackInterfaces;
import org.kp.tpmg.ttgmobilelib.controller.TTGSignonController;
import org.kp.tpmg.ttgmobilelib.model.TTGKeepAliveRequestObj;
import org.kp.tpmg.ttgmobilelib.model.TTGSignonRequestDataObj;
import org.kp.tpmg.ttgmobilelib.model.TTGUserResponse;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.UUID;

/**
 * Created by m1032896 on 2/20/2017.
 * Mindtree Ltd
 * Raghavendra.dg@mindtree.com
 */

@Implements(TTGSignonController.class)
public class TTGSignonControllerShadow {

    @Implementation
    public void performSignon(TTGSignonRequestDataObj signonData, TTGCallBackInterfaces.Signon signonInterface){
        TTGUserResponse userResponse = new TTGUserResponse();
        signonInterface.onSignOnSuccess(userResponse);
    }

    @Implementation
    public void performKeepAlive(TTGKeepAliveRequestObj keepAliveRequestObj, TTGCallBackInterfaces.KeepAlive keepAliveInterface){
        String randomCookie = UUID.randomUUID().toString();
        CookieManager cookieManager = CookieManager.getInstance();
        String url = "https://"+ AppConstants.ConfigParams.getKeepAliveCookiePath();
        cookieManager.setCookie(url, randomCookie);
        keepAliveInterface.onKeepAliveSuccess(randomCookie);
    }
}
