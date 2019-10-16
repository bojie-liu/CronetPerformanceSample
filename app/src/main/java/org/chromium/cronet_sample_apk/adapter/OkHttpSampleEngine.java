package org.chromium.cronet_sample_apk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.chromium.cronet_sample_apk.CronetSampleActivity;
import org.chromium.cronet_sample_apk.interfaces.IHttpCallback;
import org.chromium.cronet_sample_apk.interfaces.INetworkEngine;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by joshliu on 19年10月12日.
 */

public class OkHttpSampleEngine implements INetworkEngine {
    private static final String TAG = OkHttpSampleEngine.class.getSimpleName();

    private static OkHttpClient mOkHttpClient;

    public OkHttpSampleEngine(Context context) {
        OkHttpClient.Builder okHttpClientBuilder = getOkHttpClientBuilder();
        okHttpClientBuilder.connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .eventListenerFactory(OkEventListener.FACTORY)
                .cache(null);

        if (CronetSampleActivity.Config.H2) {
            List<Protocol> protocols = new ArrayList<>();
            protocols.add(Protocol.HTTP_1_1);
            protocols.add(Protocol.HTTP_2);
            okHttpClientBuilder.protocols(protocols);
        }
        mOkHttpClient = okHttpClientBuilder.build();
    }

    @Override
    public HttpRequestAdapter buildRequest(String url, Map<String, String> header, String body,
                             IHttpCallback callback) {
        okhttp3.HttpUrl httpUrl = buildUrl(url);
        if (httpUrl == null) {
            return null;
        }
        final Request.Builder requestBuilder = new Request.Builder();
        okhttp3.HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
//        if (params != null && params.size() > 0) {
//            for (Map.Entry<String, String> entry : params.entrySet()) {
//                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
//            }
//        }
        requestBuilder.url(urlBuilder.build());
        requestBuilder.get();
        if (header != null && header.size() > 0) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }
        return new HttpRequestAdapter(requestBuilder.build(), callback);
    }

    @Override
    public void start(HttpRequestAdapter requestAdapter) {
        enqueue(requestAdapter);
    }

    private okhttp3.HttpUrl buildUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return okhttp3.HttpUrl.parse(url);
    }

    private void enqueue(HttpRequestAdapter requestAdapter) {
        mOkHttpClient.newCall(requestAdapter.okRequest()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (requestAdapter.okCallback() != null) {
                    requestAdapter.okCallback().onFailure(requestAdapter, -1, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (requestAdapter.okCallback() != null) {
                    requestAdapter.okCallback().onResponse(requestAdapter, response.code(), new HttpResponseAdaptor(response));
                }
            }
        });
    }

    private OkHttpClient.Builder getOkHttpClientBuilder() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            X509TrustManager x509Tm = null;
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    x509Tm = (X509TrustManager) tm;
                    break;
                }
            }
            final X509TrustManager finalTm = x509Tm;
            X509TrustManager ignoreTimeExceptionTm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return finalTm.getAcceptedIssuers();
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                    for (X509Certificate cert : chain) {
                        try {
                            cert.checkValidity();
                        } catch (CertificateExpiredException e) {
                            //忽略时间异常
                            Log.w(TAG, "checkServerTrusted: CertificateExpiredException:" + e.getLocalizedMessage());
                            return;
                        } catch (CertificateNotYetValidException e) {
                            //忽略时间异常
                            Log.w(TAG, "checkServerTrusted: CertificateNotYetValidException:" + e.getLocalizedMessage());
                            return;
                        }
                    }
                    finalTm.checkServerTrusted(chain, authType);
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                    finalTm.checkClientTrusted(chain, authType);
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{ignoreTimeExceptionTm}, null);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, ignoreTimeExceptionTm);
            return builder;
        } catch (Exception e) {
            Log.d(TAG, "create OkHttpClientBuilder failed, create a default one instead");
            return new OkHttpClient.Builder();
        }
    }
}
