package com.ubtrobot.speech.understand;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractConverter {

    public static final String TAG = "AbstractConverter";
    private JSONObject mRoot;
    private String mPlatform;
    private Map<String, Map<String, Intent>> mMapper;

    public AbstractConverter(JSONObject root, String platform,
            Map<String, Map<String, Intent>> mapper) {
        this.mRoot = root;
        mPlatform = platform;
        mMapper = mapper;
    }

    public LegacyUnderstandResult convert(LegacyUnderstandResult.Builder builder) {
        UnderstandResult.Intent intent = convertIntent();
        UnderstandResult.Fulfillment fulfillment = convertFulfillment();
        List<UnderstandResult.Context> contexts = convertContextList();
        LegacyUnderstandResult.LegacyData legacyData = convertLegacyData();

        UnderstandResult.Intent.Builder mapIntentBuilder = new UnderstandResult.Intent.Builder(
                intent);
        String intentName = intent.getName();

        Intent mapIntent = mMapper.get(mPlatform).get(intentName);
        if (null != mapIntent) {
            String destName = mapIntent.getDestName();
            mapIntentBuilder.setName(destName);
            JSONObject parameters = intent.getParameters();
            mapIntentParameters(parameters, intentName);
        }

        List<UnderstandResult.Message> messages = fulfillment.getMessages();
        for (UnderstandResult.Message message : messages) {
            mapFulfillmentParameters(message.getParameters(), intentName);
        }

        builder.setIntent(mapIntentBuilder.build());
        builder.setContexts(contexts);
        builder.setFulfillment(fulfillment);

        builder.setLegacyData(legacyData);

        return builder.build();
    }

    public abstract UnderstandResult.Intent convertIntent();

    public abstract List<UnderstandResult.Message> convertMessage();

    public abstract UnderstandResult.Fulfillment convertFulfillment();

    public abstract UnderstandResult.Status convertStatus();

    public abstract List<UnderstandResult.Context> convertContextList();

    public abstract LegacyUnderstandResult.LegacyData convertLegacyData();

    public boolean isJsonArrayEmpty(JSONArray array) {
        if (null != array && array.length() != 0) {
            return false;
        }
        return true;
    }


    public boolean isJsonObject(String json) {
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        try {
            new JSONObject(json);
            return true;
        } catch (JsonParseException e) {
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("test", "bad json: " + json);
            return false;
        }
    }

    public JSONObject flatJSONObject(JSONObject input, JSONObject output) throws JSONException {
        Iterator<String> keys = input.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject object = input.optJSONObject(key);
            if (null != object) {
                //发现jsonobject
                flatJSONObject(object, output);
                continue;
            }

            JSONArray jsonArray = input.optJSONArray(key);
            if (null != jsonArray && jsonArray.length() != 0) {
                //发现array
                output.put(key, jsonArray);
                continue;
            }

            String value = input.optString(key);
            output.put(key, value);
        }
        return output;
    }

    public void mapIntentParameters(JSONObject parameters, String intentName) {
        Map<String, Object> mapHolder = new HashMap<>();
        Iterator<String> keys = parameters.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Map<String, Intent> intentMap = mMapper.get(mPlatform);
            if (null == intentMap) {
                return;
            }

            Intent intent = intentMap.get(intentName);
            if (null == intent) {
                return;
            }

            Intent.Param param = intent.getIntentParameterMap().get(key);
            if (null == param) {
                continue;
            }

            Object value = parameters.opt(key);
            String mapKey = param.getValue();
            String mapType = param.getType();
            if (TextUtils.isEmpty(mapKey)) {
                continue;
            }

            keys.remove();
            Object mapValue = value;
            if ("list".equals(mapType)) {
                //转换为[]
                if (!(value instanceof JSONArray)) {
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(value);
                    mapValue = jsonArray;
                }
            } else {
                //转换为单个元素
                if (value instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) value;
                    Object opt = jsonArray.opt(0);
                    mapValue = opt;
                }
            }
            mapHolder.put(mapKey, mapValue);
        }

        for (Map.Entry<String, Object> entry : mapHolder.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();
            try {
                parameters.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void mapFulfillmentParameters(JSONObject parameters, String intentName) {
        Map<String, Object> mapHolder = new HashMap<>();
        Iterator<String> keys = parameters.keys();
        while (keys.hasNext()) {
            String key = keys.next();

            Map<String, Intent> intentMap = mMapper.get(mPlatform);
            if (null == intentMap) {
                return;
            }

            Intent intent = intentMap.get(intentName);
            if (null == intent) {
                return;
            }

            String mapKey = intent.getFulfillment().get(key);

            if (TextUtils.isEmpty(mapKey)) {
                continue;
            }

            Object value = parameters.opt(key);
            keys.remove();
            mapHolder.put(mapKey, value);
        }

        for (Map.Entry<String, Object> entry : mapHolder.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();
            try {
                parameters.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
