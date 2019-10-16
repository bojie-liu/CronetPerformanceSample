package org.chromium.cronet_sample_apk.interfaces;

import org.chromium.cronet_sample_apk.adapter.HttpRequestAdapter;

import java.util.Map;

/**
 * Created by joshliu on 19年10月12日.
 */

public interface INetworkEngine {
    public HttpRequestAdapter buildRequest(String url, Map<String, String> header,
                                           String body,
                                           IHttpCallback callback);

    public void start(HttpRequestAdapter httpRequestAdapter);
}
