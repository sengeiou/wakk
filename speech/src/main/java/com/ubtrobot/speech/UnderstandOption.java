package com.ubtrobot.speech;

import com.ubtrobot.validate.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

public class UnderstandOption {

    public static final UnderstandOption DEFAULT = new UnderstandOption.Builder().build();

    private float timeout;
    private JSONObject params;

    private UnderstandOption() {
    }

    public float getTimeout() {
        return timeout;
    }

    public JSONObject getParams() {
        return params;
    }

    public static class Builder {

        private float timeout = 15000;

        private JSONObject params = new JSONObject();

        public Builder() {
        }

        public Builder setTimeout(float timeout) {
            checkTimeout(timeout);
            this.timeout = timeout;
            return this;
        }

        private void checkTimeout(float timeout) {
            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout value must not be < 0");
            }
        }

        public Builder appendStringParam(String key, String value) {
            try {
                params.putOpt(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder appendIntParam(String key, int value) {
            try {
                params.putOpt(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder appendBooleanParam(String key, boolean value) {
            try {
                params.putOpt(key, value);
            } catch (JSONException e) {
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
            understandOption.params = params;
            return understandOption;
        }
    }
}
