package org.kp.tpmg.mykpmeds.activation.model;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.AppData;

import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Map;

/**
 * Created by m1032896 on 2/22/2017.
 * Mindtree Ltd
 * Raghavendra.dg@mindtree.com
 */

@Implements(AppData.class)
public class AppDataShadow {


    @Implementation
    public String getHttpResponse(String url, String requestType, Map<String, String> params, Map<String, String> headers, JSONObject requestObj, Context cntext) {

        try {
            JSONObject pillpopperRequest = new JSONObject(requestObj.get("pillpopperRequest").toString());
            String action = pillpopperRequest.getString("action");
            if(action.equalsIgnoreCase("Register")){
                if(headers.get("ebizaccountRoles").startsWith("mrn")) {
                    return TestUtil.readFromResource("RegisterResponse-New.json");
                }else {
                    return TestUtil.readFromResource("RegisterResponse-Existing.json");
                }
            }


        }catch (JSONException e){
            System.out.println(e.getMessage());
        }


        return AppConstants.EMPTY_STRING;
    }


}
