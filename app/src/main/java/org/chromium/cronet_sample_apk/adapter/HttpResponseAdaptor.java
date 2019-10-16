package org.chromium.cronet_sample_apk.adapter;

import android.support.annotation.NonNull;

import org.chromium.net.UrlResponseInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/**
 * Created by joshliu on 19年10月12日.
 */

public class HttpResponseAdaptor extends EngineTypeHelper {
    private UrlResponseInfo mCronetResponse;
    private byte[] mBody;

    private Response mOkResponse;

    public HttpResponseAdaptor(@NonNull UrlResponseInfo response, final byte[] body) {
        mCronetResponse = response;
        mBody = body;
        setType(CRONET);
    }

    public HttpResponseAdaptor(@NonNull Response response) {
        mOkResponse = response;
        setType(OKHTTP);
    }

    public URL getUrl() {
        if (isCronet()) {
            try {
                return new URL(mCronetResponse.getUrl());
            } catch (MalformedURLException e) {
                throw  new RuntimeException();
            }
        } else if (isOkHttp()) {
            return mOkResponse.request().url().url();
        }
        throw newException();
    }

    public int getHttpStatusCode() {
        if (isCronet()) {
            return mCronetResponse.getHttpStatusCode();
        } else if (isOkHttp()) {
            return mOkResponse.code();
        }
        throw newException();
    }

    public String getNegotiatedProtocol() {
        if (isCronet()) {
            return mCronetResponse.getNegotiatedProtocol();
        } else if (isOkHttp()) {
            return mOkResponse.protocol().toString();
        }
        throw newException();
    }

    public Map<String, List<String>> getAllHeaders() {
        if (isCronet()) {
            return mCronetResponse.getAllHeaders();
        } else if (isOkHttp()) {
            return mOkResponse.headers().toMultimap();
        }
        throw newException();
    }

    public List<String> getHeader(final String key) {
        if (isCronet()) {
            if (mCronetResponse.getAllHeaders().containsKey(key))
                return mCronetResponse.getAllHeaders().get(key);
            else
                Collections.<String>emptyList();
        } else if (isOkHttp()) {
            return mOkResponse.headers(key);
        }
        throw newException();
    }

    public String getBodyString() {
        if (isCronet()) {
            return mBody == null ? "" : new String(mBody, extractCharset(this));
        } else if (isOkHttp()) {
            try {
                return mOkResponse.body() == null ? "" : mOkResponse.body().string();
            } catch (IOException e) {
                throw newException();
            }
        }
        throw newException();
    }

    private static Charset extractCharset(HttpResponseAdaptor mttResponse) {
        try {
            List<String> contentType = mttResponse.getHeader("Content-Type");
            for (String item : contentType) {
                int idx = item.indexOf("charset=");
                if (idx > 0) {
                    String charset = item.substring(idx + "charset=".length());
                    return Charset.forName(charset);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Charset.forName("utf-8");
    }
}
