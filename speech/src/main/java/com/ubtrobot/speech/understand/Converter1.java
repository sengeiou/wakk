package com.ubtrobot.speech.understand;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * convert1：
 * flight 意图,
 * intent ：intent字段里面获取 name = value;score;param 获取词槽，data.data 里面获取词槽
 */
public class Converter1 extends AbstractConverter {
    private JSONObject root;
    private String platform;

    public Converter1(JSONObject root, String platform) {
        super(root);
        this.root = root;
        this.platform = platform;
    }

    @Override
    public UnderstandResult.Intent convertIntent() {
        UnderstandResult.Intent.Builder builder = new UnderstandResult.Intent.Builder();
        try {
            JSONObject intent = getIntentJsonObject();
            builder.setScore(intent.optInt("score"));
            builder.setName(intent.optString("value"));
            List<Pair<String, JSONArray>> slotParamPairs = getIntentSlotParamPairs(
                    intent);

            for (Pair<String, JSONArray> p : slotParamPairs) {
                String first = p.first;
                JSONArray second = p.second;
                //todo 这里按照映射表 进行list 的映射处理
                builder.appendSlotsPair(first, second);
            }

            // 获取data下面的slot
            JSONArray data = root.getJSONArray("data");
            if (null != data && data.length() != 0) {
                JSONObject dataOne = data.optJSONObject(0);
                List<Pair<String, String>> dataSlotParamPairs = getDataSlotParamPairs(dataOne);
                for (Pair<String, String> p : dataSlotParamPairs) {
                    String first = p.first;
                    String second = p.second;
                    builder.appendSlotsPair(first, second);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return builder.build();
    }

    private JSONObject getIntentJsonObject() throws JSONException {

        JSONArray intents = root.optJSONArray("intent");

        for (int i = 1; i < intents.length(); i++) {
            JSONObject intent = intents.getJSONObject(i);
            String value = intent.optString("value");
            if ("flight".equals(value)) {
                return intent;
            }
        }

        return null;
    }

    private List<Pair<String, JSONArray>> getIntentSlotParamPairs(JSONObject jsonObject)
            throws JSONException {

        List<Pair<String, JSONArray>> pairs = new ArrayList<>();
        JSONObject data = jsonObject.getJSONObject("data");
        if (null == data) {
            return pairs;
        }
        JSONObject param = data.optJSONObject("param");
        if (null == param) {
            return pairs;
        }
        Iterator<String> keys = param.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONArray jsonArray = param.optJSONArray(key);
            Pair<String, JSONArray> pair = new Pair<>(key, jsonArray);
            pairs.add(pair);
        }
        return pairs;
    }

    private List<Pair<String, String>> getDataSlotParamPairs(JSONObject jsonObject)
            throws JSONException {
        List<Pair<String, String>> pairs = new ArrayList<>();
        JSONObject data = jsonObject.getJSONObject("data");
        if (null == data) {
            return pairs;
        }

        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = data.optString(key);
            Pair<String, String> pair = new Pair<>(key, value);
            pairs.add(pair);
        }
        return pairs;
    }

    @Override
    public List<UnderstandResult.Message> convertMessage() {
        ArrayList<UnderstandResult.Message> messages = new ArrayList<>();
        messages.add(new UnderstandResult.Message.Builder().build());
        return messages;
    }

    @Override
    public UnderstandResult.Fulfillment convertFulfillment() {
        UnderstandResult.Fulfillment.Builder builder = new UnderstandResult.Fulfillment.Builder();
        List<UnderstandResult.Message> messages = convertMessage();
        builder.setMessages(messages);
        UnderstandResult.Message.Builder legacyMessageBuilder =
                new UnderstandResult.Message.Builder();
        legacyMessageBuilder.setType(UnderstandResult.Message.Builder.TYPE_ORIGINAL);
        legacyMessageBuilder.setParameters(getLegacyMessageJSONObject());
        legacyMessageBuilder.setPlatform(platform);
        builder.setLegacyMessage(legacyMessageBuilder.build());
        UnderstandResult.Status status = convertStatus();
        builder.setStatus(status);

        return builder.build();
    }


    @Override
    public UnderstandResult.Status convertStatus() {
        UnderstandResult.Status.Builder builder = new UnderstandResult.Status.Builder();
        return builder.build();
    }

    @Override
    public List<UnderstandResult.Context> convertContextList() {
        ArrayList<UnderstandResult.Context> contexts = new ArrayList<>();
        return contexts;
    }
}
