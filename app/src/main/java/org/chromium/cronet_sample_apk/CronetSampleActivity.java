// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.cronet_sample_apk;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.chromium.cronet_sample_apk.adapter.CronetSampleEngine;
import org.chromium.cronet_sample_apk.adapter.HttpRequestAdapter;
import org.chromium.cronet_sample_apk.adapter.HttpResponseAdaptor;
import org.chromium.cronet_sample_apk.adapter.OkHttpSampleEngine;
import org.chromium.cronet_sample_apk.interfaces.IHttpCallback;
import org.chromium.cronet_sample_apk.interfaces.INetworkEngine;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Activity for managing the Cronet Sample.
 */
public class CronetSampleActivity extends Activity {
    private static final String TAG = CronetSampleActivity.class.getSimpleName();
    private static final String RESTFUL_URL = "https://jsonplaceholder.typicode.com/posts/42";
    private static final String CDN_URL = "https://stgwhttp2.kof.qq.com/1.jpg";

    // Config options
    public class Config {
        public static final String TEST_URL = RESTFUL_URL;
        public static final int CONNECTION_NUM = 1;
        public static final boolean H2 = false;
    }

    private String mUrl;
    private INetworkEngine mEngine;

    private TextView mResultText;
    private TextView mReceiveDataText;

    private Map<Integer, Long> mRequestTime = new ConcurrentHashMap<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResultText = (TextView) findViewById(R.id.resultView);
        mReceiveDataText = (TextView) findViewById(R.id.dataView);
        mEngine = new OkHttpSampleEngine(this);

        // for CronetSampleTest
//        String appUrl = (getIntent() != null ? getIntent().getDataString() : null);
        String appUrl = Config.TEST_URL;
        if (appUrl == null) {
            promptForURL("https://");
        } else {
            startWithURL(appUrl, null, Config.CONNECTION_NUM);
        }
    }

    private void promptForURL(String url) {
        Log.i(TAG, "No URL provided via intent, prompting user...");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter a URL");
        LayoutInflater inflater = getLayoutInflater();
        View alertView = inflater.inflate(R.layout.dialog_url, null);
        final EditText urlInput = (EditText) alertView.findViewById(R.id.urlText);
        urlInput.setText(url);
        final EditText postInput = (EditText) alertView.findViewById(R.id.postText);
        alert.setView(alertView);

        alert.setPositiveButton("Load", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {
                String url = urlInput.getText().toString();
                String postData = postInput.getText().toString();
                startWithURL(url, postData, 1);
            }
        });
        alert.show();
    }

    private void startWithURL(String url, String postData, int connectionNum) {
        Log.i(TAG, "Cronet started: " + url);
        mUrl = url;

        for (int i = 0; i < connectionNum; ++i) {
            HttpRequestAdapter requestAdapter = mEngine.buildRequest(url, null, postData, new IHttpCallback() {
                @Override
                public void onFailure(HttpRequestAdapter request, int code, String msg) {

                }

                @Override
                public void onResponse(HttpRequestAdapter request, int code, HttpResponseAdaptor response) {
                    Log.e(TAG, String.valueOf(code) + " " +
                            response.getNegotiatedProtocol() + " " +
                            response.getUrl() + " ResponseTime " +
                            String.valueOf(TimeUtils.now() - mRequestTime.get(request.id())));

                    final URL url = response.getUrl();
                    final StringBuilder textBuilder = new StringBuilder();
                    textBuilder.append("Completed " + url.toString() + " (" + response.getHttpStatusCode() + ")" +
                            System.lineSeparator());
                    textBuilder.append("Protocol " + response.getNegotiatedProtocol() + System.lineSeparator());
                    for (Map.Entry<String, List<String>> item : response.getAllHeaders().entrySet()) {
                        textBuilder.append(item.getKey() + ":" + TextUtils.join(";", item.getValue())
                                + System.lineSeparator());
                    }

                    setUIInfo(textBuilder.toString(), response.getBodyString());
                }
            });
            mRequestTime.put(requestAdapter.id(), TimeUtils.now());
            mEngine.start(requestAdapter);
        }
    }

    private void setUIInfo(final String resultText, final String bodyText) {
        CronetSampleActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mResultText.setText(resultText);
                mReceiveDataText.setText(bodyText);
//                    promptForURL(url);
            }
        });
    }
}
