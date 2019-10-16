package org.chromium.cronet_sample_apk.adapter;

/**
 * Created by joshliu on 19年10月14日.
 */

public class EngineTypeHelper {
    static final String CRONET = "CRONET";
    static final String OKHTTP = "OKHTTP";

    private String mType = "NONE";

    protected void setType(final String type) {
        mType = type;
    }

    protected boolean isCronet() {
        return mType.equals(CRONET);
    }

    protected boolean isOkHttp() {
        return mType.equals(OKHTTP);
    }

    protected RuntimeException newException() {
        return newException("");
    }

    protected RuntimeException newException(final String msg) {
        return new RuntimeException(msg);
    }
}
