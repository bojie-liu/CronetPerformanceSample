package org.chromium.cronet_sample_apk.adapter;

import org.chromium.cronet_sample_apk.interfaces.IHttpCallback;
import org.chromium.net.UrlRequest;

import okhttp3.Request;

/**
 * Created by joshliu on 19年10月12日.
 */

public class HttpRequestAdapter extends EngineTypeHelper {
    private UrlRequest mCronetRequest;

    private Request mOkRequest;
    private IHttpCallback mHttpCallback;

    public HttpRequestAdapter(UrlRequest request) {
        mCronetRequest = request;
        setType(CRONET);
    }

    public HttpRequestAdapter(Request request, IHttpCallback httpCallback) {
        mOkRequest = request;
        mHttpCallback = httpCallback;
        setType(OKHTTP);
    }

    public int id() {
        if (isCronet()) {
            return mCronetRequest.hashCode();
        } else if (isOkHttp()) {
            return mOkRequest.hashCode();
        }
        throw newException();
    }

    protected UrlRequest cronetRequest() {
        if (isCronet())
            return mCronetRequest;
        throw newException();
    }

    protected Request okRequest() {
        if (isOkHttp())
            return mOkRequest;
        throw newException();
    }

    protected IHttpCallback okCallback() {
        return mHttpCallback;
    }
}
