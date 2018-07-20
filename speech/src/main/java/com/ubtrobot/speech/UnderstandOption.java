package com.ubtrobot.speech;

import android.util.Log;

import com.ubtrobot.validate.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

public class UnderstandOption {

    private static final String TAG = "UnderstandOption";
    public static final UnderstandOption DEFAULT = new UnderstandOption.Builder().build();
    public static final String LANGUAGE_CN = "zh-CN";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_TW = "zh-TW";
    private float timeout;
    private String language;
    private String sessionId;
    private JSONObject params;

    private UnderstandOption() {
    }

    public float getTimeout() {
        return timeout;
    }

    public String getLanguage() {
        return language;
    }

    public String getSessionId() {
        return sessionId;
    }

    public JSONObject getParams() {
        return params;
    }

    public static class Builder {

        private float timeout = 15000;
        private String language = "";
        private String sessionId = String.valueOf(System.currentTimeMillis());
        private JSONObject params = new JSONObject();

        public Builder() {
        }

        public Builder setTimeout(float timeout) {
            checkTimeout(timeout);
            this.timeout = timeout;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            Preconditions.checkStringNotEmpty(sessionId,
                    "UnderstandOption.Builder refuse null sessionId");
            this.sessionId = sessionId;
            return this;
        }

        private void checkTimeout(float timeout) {
            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout value must not be < 0");
            }
        }

        public Builder setLanguage(String language) {
            this.language = Preconditions.checkStringNotEmpty(language,
                    "UnderstandOption.Builder refuse empty language");
            return this;

        }

        public Builder appendStringParam(String key, String value) {
            try {
                params.putOpt(key, value);
            } catch (JSONException e) {
                Log.e(TAG, "appendString failed");
                e.printStackTrace();
            }
            return this;
        }

        public Builder appendIntParam(String key, int value) {
            try {
                params.putOpt(key, value);
            } catch (JSONException e) {
                Log.e(TAG, "appendInt failed");
                e.printStackTrace();
            }
            return this;
        }

        public Builder appendBooleanParam(String key, boolean value) {
            try {
                params.putOpt(key, value);
            } catch (JSONException e) {
                Log.e(TAG, "appendBoolean failed");
                e.printStackTrace();
            }
            return this;
        }

        public Builder setParams(JSONObject params) {
            this.params = Preconditions.checkNotNull(params,
                    "UnderstandOption.Builder refuse set null Params");
            return this;
        }

        public UnderstandOption build() {
            UnderstandOption understandOption = new UnderstandOption();
            understandOption.timeout = timeout;
            understandOption.language = language;
            understandOption.sessionId = sessionId;
            understandOption.params = params;
            return understandOption;
        }
    }
}
