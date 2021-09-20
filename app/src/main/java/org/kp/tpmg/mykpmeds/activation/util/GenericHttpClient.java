package org.kp.tpmg.mykpmeds.activation.util;

import android.webkit.CookieManager;
import android.webkit.URLUtil;

import com.montunosoftware.pillpopper.android.util.Util;

import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.GenericHttpResponse;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.kp.tpmg.ttgmobilelib.utilities.TTGLoggerUtil;
import org.kp.tpmg.ttgmobilelib.utilities.TTGSSLSocketFactory;
import org.kp.tpmg.ttgmobilelib.utilities.TTGUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class GenericHttpClient {
    private static GenericHttpClient genericHttpClient;
    private TTGSSLSocketFactory socketFactory;


    private GenericHttpClient() {

    }

    public static GenericHttpClient getInstance() {
        synchronized (GenericHttpClient.class) {
            if (null == genericHttpClient) {
                genericHttpClient = new GenericHttpClient();
            }
        }
        return genericHttpClient;
    }

    public synchronized GenericHttpResponse executeUrlRequest(String url, String requestType, Map<String, String> params, Map<String, String> headers, JSONObject requestObject, File file) {

        GenericHttpResponse data = new GenericHttpResponse();
        InputStream stream = null;
        OutputStream output = null;
        try {

            initializeSSL();
            stream = makeRequest(url, requestType, params, headers, requestObject);
            if (null != stream) {
                if (file != null) {
                    output = new FileOutputStream(file);
                    int bytesRead = 0;
                    byte _binarydata[] = new byte[1024];

                    while ((bytesRead = stream.read(_binarydata)) != -1) {
                        output.write(_binarydata, 0, bytesRead);
                    }
                    output.flush();
                }
                String response = TTGUtil.convertResponseTOString(stream);
                LoggerUtils.info("Response : " + response);
                data.setStatus(true);
                data.setData(response);
            } else {
                data.setStatus(false);
                data.setData(AppConstants.HTTP_DATA_ERROR);
            }
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            data.setStatus(false);
            data.setData(AppConstants.HTTP_DATA_ERROR);
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            data.setStatus(false);
            data.setData(AppConstants.HTTP_DATA_ERROR);
            LoggerUtils.exception(e.getMessage());
        } finally {
            Util.closeSilently(output);
            Util.closeSilently(stream);
        }
        return data;
    }

    // To-do
    public synchronized GenericHttpResponse executeHttpUrlRequest(String url, String requestType,
                                                                  Map<String, String> params, Map<String, String> headers) {

        // initializing the custom socket factory
        initializeSSL();
        GenericHttpResponse data = new GenericHttpResponse();
        InputStream stream = null;
        try {
            stream = makeRequest(url, requestType, params, headers);
            if (null != stream ) {
                String response = TTGUtil.convertResponseTOString(stream);
                LoggerUtils.info("Response : " + response);
                data.setStatus(true);
                data.setData(response);

            } else {
                String response = TTGUtil.convertResponseTOString(stream);
                data.setStatus(false);
                data.setData(AppConstants.HTTP_DATA_ERROR);
            }
        } catch (MalformedURLException e) {
            data.setStatus(false);
            data.setData(AppConstants.HTTP_DATA_ERROR);
            LoggerUtils.exception(e.getMessage());
        } catch (IOException e) {
            data.setStatus(false);
            data.setData(AppConstants.HTTP_DATA_ERROR);
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
            data.setStatus(false);
            data.setData(AppConstants.HTTP_DATA_ERROR);
        }
        return data;
    }

    public InputStream makeRequest(String url, String requestType,
                                   Map<String, String> params, Map<String, String> headers, JSONObject requestObj)
            throws IOException {

        if (url == null) {
            return null;
        }
        InputStream stream = null;
        URL _url = new URL(url);
        URLConnection connection = _url.openConnection();
        if (URLUtil.isHttpsUrl(url)) {
            HttpsURLConnection httpConnection = null;
            try {

                httpConnection = (HttpsURLConnection) connection;

                if(null!= RunTimeData.getInstance().getRuntimeSSOSessionID()){
                    String cookie = new StringBuilder(AppConstants.ConfigParams.getKeepAliveCookieName()+"="+
                            URLEncoder.encode(RunTimeData.getInstance().getRuntimeSSOSessionID(),"utf-8"))
                            .append("; domain=").append(AppConstants.ConfigParams.getKeepAliveCookieDomain())
                            .append("; path=").append(AppConstants.ConfigParams.getKeepAliveCookiePath())
                            .toString();

                    if (null != cookie) {
                        //For App profile call this keep_alive_cookie_domain_key is null so making this null check to avoid the crash as the key is returned in app profile response
                        if(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY)!=null) {
                            httpConnection.setRequestProperty("Cookie", cookie);
                        }
                    }
                }

                LoggerUtils.info("Triggering url : " + url);
                httpConnection.setConnectTimeout(45000);
                httpConnection.setReadTimeout(45000);
                httpConnection.setDoInput(true);
                if(!TTGMobileLibConstants.HTTP_METHOD_GET.equalsIgnoreCase(requestType)){
                    httpConnection.setDoOutput(true);
                }
                httpConnection.setRequestMethod(requestType);
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + "UTF-8");
                httpConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());

                // Adding the headers into request
                if (headers != null && headers.size() > 0) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        LoggerUtils.info(" And Headers : " + header.getKey() + " value: " + header.getValue());
                        httpConnection.addRequestProperty(header.getKey(), header.getValue());
                    }
                }

                // httpConnection.addRequestProperty("Cookie", getLocalCookies());
                // Adding the params into request body.

                OutputStream outputStream = null;
                BufferedWriter writer = null;
                outputStream = httpConnection.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                if (params != null) {
                    writer.write(prepareParamsString(params));
                }
                if (requestObj != null) {
                    writer.write(requestObj.toString());
                }
                writer.flush();
                writer.close();
                outputStream.close();

                int status = httpConnection.getResponseCode();



                if (status == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                LoggerUtils.exception(ex.getMessage());
            }
        } else {
            HttpURLConnection httpConnection = null;
            try {

                httpConnection = (HttpURLConnection) connection;
                LoggerUtils.info("Triggering url : " + url);
                httpConnection.setConnectTimeout(45000);
                httpConnection.setReadTimeout(45000);
                httpConnection.setDoInput(true);
                httpConnection.setDoOutput(true);
                httpConnection.setRequestMethod(requestType);
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + "UTF-8");

                // Adding the headers into request
                if (headers != null && headers.size() > 0) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        LoggerUtils.info(" And Headers : " + header.getKey() + " value: " + header.getValue());
                        httpConnection.addRequestProperty(header.getKey(), header.getValue());
                    }
                }

                httpConnection.addRequestProperty("Cookie", getLocalCookies());
                // Adding the params into request body.

                OutputStream outputStream = null;
                BufferedWriter writer = null;
                try {
                    //httpConnection.setRequestProperty("Content-Type", "application/json");
                    outputStream = httpConnection.getOutputStream();
                    writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    if (requestObj != null) {
                        writer.write(requestObj.toString());
                    }
                    if (params != null) {
                        writer.write(prepareParamsString(params));
                    }
                    writer.flush();
                    writer.close();
                    outputStream.close();
                } catch (Exception e) {
                    LoggerUtils.info(e.getMessage());
                }

                int status = httpConnection.getResponseCode();
                LoggerUtils.info("-- StatusCOde : " + status);
                if (status == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                LoggerUtils.exception(ex.getMessage());
            }
        }
        return stream;

    }

    // To do
    public InputStream makeRequest(String url, String requestType,
                                   Map<String, String> params, Map<String, String> headers)
            throws IOException {
        CookieManager cookieManager = CookieManager.getInstance();
        if(url==null){
            return null;
        }
        InputStream stream = null;
        URL _url = new URL(url);
        if (URLUtil.isHttpsUrl(url)) {
            URLConnection connection = _url.openConnection();
            HttpsURLConnection httpConnection = null;
            try {

                httpConnection = (HttpsURLConnection) connection;
                LoggerUtils.info("Triggering url : " + url);

                String cookie = cookieManager.getCookie(httpConnection.getURL()
                        .toString());

                if (null != cookie) {
                    //For App profile call this keep_alive_cookie_domain_key is null so making this null check to avoid the crash as the key is returned in app profile response
                    if(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY)!=null) {

                        httpConnection.setRequestProperty("Cookie", cookie + "; domain=" + TTGRuntimeData.getInstance().getConfigListParams().get(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY));
                        TTGLoggerUtil.info("-- cookie : " + cookie + "; domain=" + TTGRuntimeData.getInstance().getConfigListParams().get(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY));
                    }
                }

                httpConnection.setConnectTimeout(45000);
                httpConnection.setReadTimeout(45000);
                httpConnection.setDoInput(true);
                if(!TTGMobileLibConstants.HTTP_METHOD_GET.equalsIgnoreCase(requestType)){
                    httpConnection.setDoOutput(true);
                }
                httpConnection.setRequestMethod(requestType);
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + "UTF-8");
                httpConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());

                // Adding the headers into request
                if (headers != null && headers.size() > 0) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        LoggerUtils.info(" And Headers : " + header.getKey() + " value: " + header.getValue());
                        httpConnection.addRequestProperty(header.getKey(), /*URLEncoder.encode(*/header.getValue()/*, "utf-8")*/);
                        //httpConnection.addRequestProperty(header.getKey(),URLEncoder.encode(header.getValue(), "utf-8"));
                    }
                }
                // httpConnection.addRequestProperty("Cookie", getLocalCookies());

                // Adding the params into request body.
                if(null!=params){
                    OutputStream outputStream = null;
                    BufferedWriter writer = null;
                    try {
                        outputStream = httpConnection.getOutputStream();
                        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                        writer.write(prepareParamsString(params));
                        writer.flush();
                        writer.close();
                        outputStream.close();
                    } catch (Exception e) {
                        LoggerUtils.info(e.getMessage());
                    } /*finally{
						writer.close();
						outputStream.close();
					}*/
                }

                int status = httpConnection.getResponseCode();

                List<String> cookieList = httpConnection.getHeaderFields().get(
                        "Set-Cookie");
                if (cookieList != null) {
                    for (String cookieTemp : cookieList) {
                        cookieManager.setCookie(httpConnection.getURL().toString(),
                                cookieTemp);
                    }
                }

                if (status == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                LoggerUtils.exception(ex.getMessage());
            } /*finally{
				httpConnection.disconnect();
			}*/
            return stream;
        }else{
            URLConnection connection = _url.openConnection();
            HttpURLConnection httpConnection = null;
            try {

                httpConnection = (HttpURLConnection) connection;
                LoggerUtils.info("Triggering url : " + url);

                String cookie = cookieManager.getCookie(httpConnection.getURL()
                        .toString());

                if (null != cookie) {
                    if(Util.getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY)!=null) {

                        httpConnection.setRequestProperty("Cookie", cookie + "; domain=" + TTGRuntimeData.getInstance().getConfigListParams().get(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY));
                        TTGLoggerUtil.info("-- cookie : " + cookie + "; domain=" + TTGRuntimeData.getInstance().getConfigListParams().get(AppConstants.APP_PROFILE_KEEP_ALIVECOOKIE_DOMAIN_KEY));
                    }
                }
                httpConnection.setConnectTimeout(45000);
                httpConnection.setReadTimeout(45000);
                httpConnection.setDoInput(true);
                if(!TTGMobileLibConstants.HTTP_METHOD_GET.equalsIgnoreCase(requestType)){
                    httpConnection.setDoOutput(true);
                }
                httpConnection.setRequestMethod(requestType);
                httpConnection.setRequestProperty("Accept-Charset", "UTF-8");
                httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + "UTF-8");

                // Adding the headers into request
                if (headers != null && headers.size() > 0) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        LoggerUtils.info(" And Headers : " + header.getKey() + " value: " + header.getValue());
                        httpConnection.addRequestProperty(header.getKey(), /*URLEncoder.encode(*/header.getValue()/*, "utf-8")*/);
                        //	httpConnection.addRequestProperty(header.getKey(),URLEncoder.encode(header.getValue(), "utf-8"));
                    }
                }

                // Adding the params into request body.
                if(null!=params){
                    OutputStream outputStream = null;
                    BufferedWriter writer = null;
                    try {
                        outputStream = httpConnection.getOutputStream();
                        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                        writer.write(prepareParamsString(params));
                        writer.flush();
                        writer.close();
                        outputStream.close();
                    } catch (Exception e) {
                        LoggerUtils.info(e.getMessage());
                    } /*finally{
						writer.close();
						outputStream.close();
					}*/
                }

                int status = httpConnection.getResponseCode();

                List<String> cookieList = httpConnection.getHeaderFields().get(
                        "Set-Cookie");
                if (cookieList != null) {
                    for (String cookieTemp : cookieList) {
                        cookieManager.setCookie(httpConnection.getURL().toString(),
                                cookieTemp);
                    }
                }

                if (status == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                LoggerUtils.exception(ex.getMessage());
            } /*finally{
				httpConnection.disconnect();
			}*/
            return stream;
        }

    }

    private String prepareParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder paramsString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                paramsString.append("&");

            paramsString.append(entry.getKey());
            paramsString.append("=");
            paramsString.append(entry.getValue());
        }
        LoggerUtils.info("Params : " + paramsString.toString());
        return paramsString.toString();
    }

    public String getLocalCookies() {
        return CookieManager.getInstance().getCookie("https://" + AppConstants.ConfigParams.getKeepAliveCookieDomain());
    }

    public void initializeSSL(){
        createSocketFactory();
    }

    private SocketFactory createSocketFactory() {
        try {
            KeyStore trustStore = KeyStore.getInstance(TTGMobileLibConstants.CERTIFICATE_TYPE);
            trustStore.load(null, null);
            socketFactory = new TTGSSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

        } catch (KeyManagementException e) {
            LoggerUtils.exception("KeyManagementException", e);
        } catch (UnrecoverableKeyException e) {
            LoggerUtils.exception("UnrecoverableKeyException", e);
        } catch (KeyStoreException e) {
            LoggerUtils.exception("KeyStoreException", e);
        } catch (NoSuchAlgorithmException e) {
            LoggerUtils.exception("NoSuchAlgorithmException", e);
        } catch (CertificateException e) {
            LoggerUtils.exception("CertificateException", e);
        } catch (IOException e) {
            LoggerUtils.exception("IOException", e);
        }
        return socketFactory;
    }

}
