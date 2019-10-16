package org.chromium.cronet_sample_apk.interfaces;

import org.chromium.cronet_sample_apk.adapter.HttpRequestAdapter;
import org.chromium.cronet_sample_apk.adapter.HttpResponseAdaptor;

/**
 * Created by joshliu on 19年10月12日.
 */

public interface IHttpCallback {
    void onFailure(HttpRequestAdapter request, int code, String msg);

    // request may not be the same object as the initial request.
    void onResponse(HttpRequestAdapter request, int code, HttpResponseAdaptor response);
}
