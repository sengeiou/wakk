package com.ubtrobot.speech.understand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class AbstractConverter {
    //flight
    public static final int STRATEGY_1 = 1;
    // chat，故事
    public static final int STRATEGY_2 = 2;
    //意图引擎
    public static final int STRATEGY_3 = 3;
    //问答定制生成appid#
    public static final int STRATEGY_4 = 4;
    //问答定制fallback
    public static final int STRATEGY_5 = 5;

    private JSONObject root;

    public AbstractConverter(JSONObject root) {
        this.root = root;
    }

    public UnderstandResult convert() {
        UnderstandResult.Builder builder = new UnderstandResult.Builder();
        UnderstandResult.Intent intent = convertIntent();
        UnderstandResult.Fulfillment fulfillment = convertFulfillment();
        List<UnderstandResult.Context> contexts = convertContextList();
        builder.setIntent(intent);
        builder.setContexts(contexts);
        builder.setFulfillment(fulfillment);

        return builder.build();
    }

    public abstract UnderstandResult.Intent convertIntent();

    public abstract List<UnderstandResult.Message> convertMessage();

    public abstract UnderstandResult.Fulfillment convertFulfillment();

    public abstract UnderstandResult.Status convertStatus();

    public abstract List<UnderstandResult.Context> convertContextList();

    public JSONObject getLegacyMessageJSONObject() {
        JSONArray data = root.optJSONArray("data");
        if (null != data && data.length() != 0) {
            try {
                JSONObject dataOne = data.getJSONObject(0);
                return dataOne;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    public boolean isJsonArrayEmpty(JSONArray array) {
        if (null != array && array.length() != 0) {
            return false;
        }
        return true;
    }

}
