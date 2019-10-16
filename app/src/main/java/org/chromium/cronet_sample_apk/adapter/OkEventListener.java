package org.chromium.cronet_sample_apk.adapter;

import android.util.Log;

import org.chromium.cronet_sample_apk.TimeUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by joshliu on 19年10月16日.
 */

class OkEventListener extends EventListener {
    private static final String TAG = OkHttpSampleEngine.class.getSimpleName();

    public static final Factory FACTORY = new Factory() {
        final AtomicLong nextCallId = new AtomicLong(1L);

        @Override public EventListener create(Call call) {
            long callId = nextCallId.getAndIncrement();
            Log.i(TAG, " callId " + callId + " url " + call.request().url());
            return new OkEventListener(callId);
        }
    };

    long callId;
    long lastCheckPoint;
    long callStart;

    public OkEventListener(long callId) {
        this.callId = callId;
        this.lastCheckPoint = TimeUtils.now();
        this.callStart = lastCheckPoint;
    }

    private void printEvent(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" callId " + callId + " event " + name +
                " toLastEvent " + String.valueOf(TimeUtils.now() - this.lastCheckPoint));
        if (name.equals("callEnd")) {
            stringBuilder.append(" total " + String.valueOf(TimeUtils.now() - callStart));
        }
        Log.i(TAG, stringBuilder.toString());
        this.lastCheckPoint = TimeUtils.now();
    }

    @Override public void callStart(Call call) {
        printEvent("callStart");
    }

    @Override public void dnsStart(Call call, String domainName) {
        printEvent("dnsStart");
    }

    @Override public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        printEvent("dnsEnd");
    }

    @Override public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        printEvent("connectStart");
    }

    @Override public void secureConnectStart(Call call) {
        printEvent("secureConnectStart");
    }

    @Override public void secureConnectEnd(Call call, Handshake handshake) {
        printEvent("secureConnectEnd");
    }

    @Override public void connectEnd(
            Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
        printEvent("connectEnd");
    }

    @Override public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy,
                                        Protocol protocol, IOException ioe) {
        printEvent("connectFailed");
    }

    @Override public void connectionAcquired(Call call, Connection connection) {
        printEvent("connectionAcquired");
    }

    @Override public void connectionReleased(Call call, Connection connection) {
        printEvent("connectionReleased");
    }

    @Override public void requestHeadersStart(Call call) {
        printEvent("requestHeadersStart");
    }

    @Override public void requestHeadersEnd(Call call, Request request) {
        printEvent("requestHeadersEnd");
    }

    @Override public void requestBodyStart(Call call) {
        printEvent("requestBodyStart");
    }

    @Override public void requestBodyEnd(Call call, long byteCount) {
        printEvent("requestBodyEnd");
    }

    @Override public void responseHeadersStart(Call call) {
        printEvent("responseHeadersStart");
    }

    @Override public void responseHeadersEnd(Call call, Response response) {
        printEvent("responseHeadersEnd");
    }

    @Override public void responseBodyStart(Call call) {
        printEvent("responseBodyStart");
    }

    @Override public void responseBodyEnd(Call call, long byteCount) {
        printEvent("responseBodyEnd");
    }

    @Override public void callEnd(Call call) {
        printEvent("callEnd");
    }

    @Override public void callFailed(Call call, IOException ioe) {
        printEvent("callFailed");
    }
}