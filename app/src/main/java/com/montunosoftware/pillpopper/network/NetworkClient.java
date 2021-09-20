package com.montunosoftware.pillpopper.network;

import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;

import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttgmobilelib.TPMGTrustManager;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.kp.tpmg.ttgmobilelib.utilities.TTGSSLSocketFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by M1023050 on 13-Nov-18.
 */

public class NetworkClient {

    private Retrofit retrofit = null;
    private static NetworkClient mNetworkClient;
    private String TAG = NetworkClient.class.getSimpleName();
    TTGSSLSocketFactory socketFactory;
    TPMGTrustManager trustManager;

    public static NetworkClient getInstance() {
        if (mNetworkClient == null) {
            mNetworkClient = new NetworkClient();
        }
        return mNetworkClient;
    }

    public Retrofit prepareClient(String url) {
        initializeSSL();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        /*HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);*/
        try{
            builder.sslSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory(), trustManager);
            builder.followSslRedirects(true);
            builder.followRedirects(true);
            builder.connectTimeout(PillpopperConstants.CONNECTION_READ_TIME_OUT_SECONDS, TimeUnit.SECONDS);
            builder.readTimeout(PillpopperConstants.CONNECTION_READ_TIME_OUT_SECONDS, TimeUnit.SECONDS);
        }catch (Exception e){
            PillpopperLog.say(e.getMessage());
        }

        OkHttpClient okHttpClient = builder.build();

        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        return retrofit;
    }

    public interface ApiListener {
        void success(String strApiName, Object response);
        void error(String strApiName, String error);
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
            trustManager = new TPMGTrustManager(trustStore);

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
