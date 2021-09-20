package com.montunosoftware.pillpopper.network;


import com.montunosoftware.pillpopper.network.model.GetTokenResponseObj;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by M1023050 on 13-Nov-18.
 */

public interface NetworkAPI {

    @FormUrlEncoded
    @POST("/token")
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
    })
    Call<GetTokenResponseObj> getAccessTokenService(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/revoke")
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
    })
    Call<ResponseBody> getRevokeTokenService(@HeaderMap Map<String, String> headers, @FieldMap Map<String, String> params);

    //  if you really want your base URL to be the full path you can use @GET(".") to declare that your final URL is the same as your base URL
    @GET(".")
    Call<ResponseBody> getRegionsContacts( @HeaderMap Map<String,String> headers);

}
