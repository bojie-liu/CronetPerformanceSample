package org.chromium.cronet_sample_apk.adapter;

import android.content.Context;
import android.util.Log;

import org.chromium.cronet_sample_apk.CronetSampleActivity;
import org.chromium.cronet_sample_apk.interfaces.IHttpCallback;
import org.chromium.cronet_sample_apk.interfaces.INetworkEngine;
import org.chromium.net.CronetEngine;
import org.chromium.net.CronetException;
import org.chromium.net.ExperimentalCronetEngine;
import org.chromium.net.ExperimentalUrlRequest;
import org.chromium.net.RequestFinishedInfo;
import org.chromium.net.UploadDataProviders;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by joshliu on 19年10月12日.
 */

public class CronetSampleEngine implements INetworkEngine {
    private static final String TAG = CronetSampleEngine.class.getSimpleName();

    private ExperimentalCronetEngine mCronetEngine;
    private Executor mExecutor = Executors.newCachedThreadPool();

    public CronetSampleEngine(Context context) {
        ExperimentalCronetEngine.Builder myBuilder = new ExperimentalCronetEngine.Builder(context);
        myBuilder.enableHttpCache(CronetEngine.Builder.HTTP_CACHE_DISABLED, 100 * 1024);

        if (CronetSampleActivity.Config.H2) {
            myBuilder.enableHttp2(true).enableQuic(true);
        }

        mCronetEngine = myBuilder.build();
        mCronetEngine.addRequestFinishedListener(new RequestFinishedInfo.Listener(mExecutor) {
            @Override
            public void onRequestFinished(RequestFinishedInfo requestFinishedInfo) {
                RequestFinishedInfo.Metrics metrics = requestFinishedInfo.getMetrics();
                Log.e(TAG, requestFinishedInfo.getUrl() + " onRequestFinished " +
                        " connection " + interval(metrics.getConnectStart(), metrics.getConnectEnd()) +
                        " dns " + interval(metrics.getDnsStart(), metrics.getDnsEnd()) +
                        " request " + interval(metrics.getRequestStart(), metrics.getRequestEnd())+
                        " ssl " + interval(metrics.getSslStart(), metrics.getSslEnd())+
                        " sending " + interval(metrics.getSendingStart(), metrics.getSendingEnd())+
                        " total " + metrics.getTotalTimeMs());
            }
        });
    }

    @Override
    public HttpRequestAdapter buildRequest(String url, Map<String, String> header,
                                           String body,
                                           IHttpCallback httpCallback) {
        UrlRequest.Callback callback = new SimpleUrlRequestCallback(httpCallback);
        ExperimentalUrlRequest.Builder builder = mCronetEngine.newUrlRequestBuilder(url, callback, mExecutor);
        applyPostDataToUrlRequestBuilder(builder, mExecutor, body);
        return new HttpRequestAdapter(builder.build());
    }

    @Override
    public void start(HttpRequestAdapter requestAdapter) {
        requestAdapter.cronetRequest().start();
    }

    private void applyPostDataToUrlRequestBuilder(
            UrlRequest.Builder builder, Executor executor, String postData) {
        if (postData != null && postData.length() > 0) {
            builder.setHttpMethod("POST");
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.setUploadDataProvider(
                    UploadDataProviders.create(postData.getBytes()), executor);
        }
    }

    // Starts writing NetLog to disk. startNetLog() should be called afterwards.
    private void startNetLog(Context context) {
        mCronetEngine.startNetLogToFile(context.getCacheDir().getPath() + "/netlog.json", false);
    }

    // Stops writing NetLog to disk. Should be called after calling startNetLog().
    // NetLog can be downloaded afterwards via:
    //   adb root
    //   adb pull /data/data/org.chromium.cronet_sample_apk/cache/netlog.json
    // netlog.json can then be viewed in a Chrome tab navigated to chrome://net-internals/#import
    private void stopNetLog() {
        mCronetEngine.stopNetLog();
    }

    private static long interval(Date start, Date end) {
        return end.getTime() - start.getTime();
    }

    class SimpleUrlRequestCallback extends UrlRequest.Callback {
        private ByteArrayOutputStream mBytesReceived = new ByteArrayOutputStream();
        private WritableByteChannel mReceiveChannel = Channels.newChannel(mBytesReceived);
        private IHttpCallback mHttpCallback;

        public SimpleUrlRequestCallback(IHttpCallback httpCallback) {
            mHttpCallback = httpCallback;
        }

        @Override
        public void onRedirectReceived(
                UrlRequest request, UrlResponseInfo info, String newLocationUrl) {
            Log.i(TAG, "****** onRedirectReceived ******");
            request.followRedirect();
        }

        @Override
        public void onResponseStarted(UrlRequest request, UrlResponseInfo info) {
            Log.i(TAG, "****** Response Started ******");
            Log.i(TAG, "*** Headers Are *** " + info.getAllHeaders());

            request.read(ByteBuffer.allocateDirect(32 * 1024));
        }

        @Override
        public void onReadCompleted(
                UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) {
            byteBuffer.flip();
            Log.i(TAG, "****** onReadCompleted ******" + byteBuffer);

            try {
                mReceiveChannel.write(byteBuffer);
            } catch (IOException e) {
                Log.i(TAG, "IOException during ByteBuffer read. Details: ", e);
            }
            byteBuffer.clear();
            request.read(byteBuffer);
        }

        @Override
        public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
            Log.i(TAG, "****** Request Completed, status code is " + info.getHttpStatusCode()
                    + ", total received bytes is " + info.getReceivedByteCount());

            if (mHttpCallback != null)
                mHttpCallback.onResponse(new HttpRequestAdapter(request),
                        info.getHttpStatusCode(),
                        new HttpResponseAdaptor(info, mBytesReceived.toByteArray()));
        }

        @Override
        public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {
            Log.i(TAG, "****** onFailed, error is: " + error.getMessage());

            if (mHttpCallback != null)
                mHttpCallback.onResponse(new HttpRequestAdapter(request),
                        info.getHttpStatusCode(), new HttpResponseAdaptor(info, null));
        }
    }

}
