package com.ubtrobot.speech.understand;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EmotibotConverter extends AbstractConverter {

    public static final String TAG = "EmotibotConverter";
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

    public static final String PLATFORM = "zhujian";
    private JSONObject mRoot;
    private String mPlatform = PLATFORM;
    private int strategy;

    public EmotibotConverter(JSONObject root,
            Map<String, Map<String, Intent>> mapper) {
        super(root, PLATFORM, mapper);
        this.mRoot = root;
    }

    @Override
    public UnderstandResult.Intent convertIntent() {
        UnderstandResult.Intent intent = UnderstandResult.Intent.NULL;
        switch (strategy) {
            case STRATEGY_1:
                intent = convertIntentStrategy1();
                break;
            case STRATEGY_2:
                intent = convertIntentStrategy2();
                break;
            case STRATEGY_3:
                intent = convertIntentStrategy3();
                break;
            case STRATEGY_4:
                intent = convertIntentStrategy4();
                break;
        }
        return intent;
    }

    private UnderstandResult.Intent convertIntentStrategy1() {
        UnderstandResult.Intent.Builder builder = new UnderstandResult.Intent.Builder();
        try {
            JSONObject intent = getPresetIntentJsonObject();
            builder.setName(intent.optString("value"));
            builder.setScore(intent.optInt("score"));
            List<Pair<String, JSONArray>> slotParamPairs = getIntentSlotParamPairs(
                    intent);

            for (Pair<String, JSONArray> p : slotParamPairs) {
                String first = p.first;
                JSONArray second = p.second;
                builder.appendSlotsPair(first, second);
            }

            // 获取data下面的slot
            JSONArray data = mRoot.getJSONArray("data");
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
            Log.e(TAG, "convertIntentStrategy1 parse error");
            e.printStackTrace();
        }

        return builder.build();
    }

    private UnderstandResult.Intent convertIntentStrategy2() {
        UnderstandResult.Intent.Builder intentBuilder = new UnderstandResult.Intent.Builder();
        JSONArray intents = mRoot.optJSONArray("intent");
        if (!isJsonArrayEmpty(intents)) {
            JSONObject object = intents.optJSONObject(0);
            String value = object.optString("value");
            intentBuilder.setName(value);
            int score = object.optInt("score");
            intentBuilder.setScore(score);

            List<Pair<String, JSONArray>> slotParamPairs = null;
            try {
                slotParamPairs = getIntentSlotParamPairs(
                        object);
                for (Pair<String, JSONArray> p : slotParamPairs) {
                    String first = p.first;
                    JSONArray second = p.second;
                    intentBuilder.appendSlotsPair(first, second);
                }
            } catch (JSONException e) {
                Log.e(TAG, "convertIntentStrategy2 parse error");
                e.printStackTrace();
            }
        }
        return intentBuilder.build();
    }

    private UnderstandResult.Intent convertIntentStrategy3() {
        UnderstandResult.Intent.Builder intentBuilder = new UnderstandResult.Intent.Builder();
        try {
            JSONObject intent = getIntentJsonObject();
            intentBuilder.setName(intent.optString("value"));
            intentBuilder.setScore(intent.optLong("score"));
            List<Pair<String, JSONArray>> intentSlotParamPairs = getIntentSlotParamPairs(intent);
            for (Pair<String, JSONArray> p : intentSlotParamPairs) {
                String first = p.first;
                JSONArray second = p.second;
                //todo 这里按照映射表 进行list 的映射处理
                intentBuilder.appendSlotsPair(first, second);
            }
        } catch (JSONException e) {
            Log.e(TAG, "convertIntentStrategy3 parse error");
            e.printStackTrace();
        }
        return intentBuilder.build();
    }

    private JSONObject getIntentJsonObject() throws JSONException {
        JSONArray intents = mRoot.optJSONArray("intent");
        if (!isJsonArrayEmpty(intents)) {
            for (int i = 0; i < intents.length(); i++) {
                JSONObject intent = intents.getJSONObject(i);
                String value = intent.optString("value");
                if (!TextUtils.isEmpty(value)) {
                    return intent;
                }
            }
        }

        return null;
    }

    private UnderstandResult.Intent convertIntentStrategy4() {
        UnderstandResult.Intent.Builder intentBuilder = new UnderstandResult.Intent.Builder();
        JSONArray data = mRoot.optJSONArray("data");
        if (!isJsonArrayEmpty(data)) {
            JSONObject object = data.optJSONObject(0);
            String value = object.optString("value");
            if (isJsonObject(value)) {
                try {
                    JSONObject valueJSONObject = new JSONObject(value);
                    int appId = valueJSONObject.optInt("appid");
                    JSONObject dataObject = valueJSONObject.optJSONObject("data");
                    int page = dataObject.optInt("page");

                    String intentName = appId + "#" + page;
                    intentBuilder.setName(intentName);
                    intentBuilder.setScore(100);
                    intentBuilder.setParameters(new JSONObject());
                } catch (JSONException e) {
                    Log.e(TAG, "convertIntentStrategy4 parse error");
                    e.printStackTrace();
                }
            } else {
                //fallback
                intentBuilder.setName("fallback");
                intentBuilder.setScore(100);
                intentBuilder.setParameters(new JSONObject());
            }
        }

        return intentBuilder.build();
    }

    @Override
    public List<UnderstandResult.Message> convertMessage() {
        List<UnderstandResult.Message> message = null;
        switch (strategy) {
            case STRATEGY_1:
                message = convertMessageStrategy1();
                break;
            case STRATEGY_2:
                message = convertMessageStrategy2();
                break;
            case STRATEGY_3:
                message = convertMessageStrategy3();
                break;
            case STRATEGY_4:
                message = convertMessageStrategy4();
                break;
        }
        return message;
    }

    private List<UnderstandResult.Message> convertMessageStrategy1() {
        ArrayList<UnderstandResult.Message> messages = new ArrayList<>();
        messages.add(new UnderstandResult.Message.Builder().build());
        return messages;
    }

    private List<UnderstandResult.Message> convertMessageStrategy2() {
        ArrayList<UnderstandResult.Message> messages = new ArrayList<>();
        UnderstandResult.Message.Builder messageBuilder = new UnderstandResult.Message.Builder();
        messageBuilder.setType(UnderstandResult.Message.Builder.TYPE_TEXT);
        messageBuilder.setPlatform(mPlatform);
        messageBuilder.setParameters(getMessageJSONObject());
        messages.add(messageBuilder.build());

        return messages;
    }

    private JSONObject getMessageJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("speech", getSpeech());
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject put \"speech\" error");
            e.printStackTrace();
        }
        return object;
    }

    private String getSpeech() {
        String speech = "";
        JSONArray data = mRoot.optJSONArray("data");
        if (!isJsonArrayEmpty(data)) {
            JSONObject object = data.optJSONObject(0);
            speech = object.optString("value");
        }
        return speech;
    }

    private List<UnderstandResult.Message> convertMessageStrategy3() {
        List<UnderstandResult.Message> messages = new ArrayList<>();
        UnderstandResult.Message.Builder messageBuilder = new UnderstandResult.Message.Builder();
        messageBuilder.setType(UnderstandResult.Message.Builder.TYPE_USERDEFINE);
        messageBuilder.setPlatform(mPlatform);
        JSONObject dataJSONObject = getDataJSONObject();
        String value = dataJSONObject.optString("value");

        if (isJsonObject(value)) {
            //是json
            try {
                //这里确认是否需要平铺数据？？
                JSONObject input = new JSONObject(value);
                JSONObject output = new JSONObject();
                flatJSONObject(input, output);
                messageBuilder.setParameters(output);
            } catch (JSONException e) {
                Log.e(TAG, "flatJSONObject in convertMessageStrategy3 error");
                e.printStackTrace();
            }
        } else {
            //是speech
            messageBuilder.setType(UnderstandResult.Message.Builder.TYPE_TEXT);
            JSONObject param = new JSONObject();
            try {
                param.put("speech", value);
            } catch (JSONException e) {
                Log.e(TAG, "JSONObject put \"speech\" error");
                e.printStackTrace();
            }
            messageBuilder.setParameters(param);
        }
        messages.add(messageBuilder.build());

        return messages;
    }

    private List<UnderstandResult.Message> convertMessageStrategy4() {
        List<UnderstandResult.Message> messages = new ArrayList<>();
        UnderstandResult.Message.Builder messageBuilder = new UnderstandResult.Message.Builder();
        messageBuilder.setType(UnderstandResult.Message.Builder.TYPE_USERDEFINE);
        messageBuilder.setPlatform(mPlatform);
        JSONObject dataJSONObject = getDataJSONObject();
        String value = dataJSONObject.optString("value");

        if (isJsonObject(value)) {
            //是json
            try {
                JSONObject input = new JSONObject(value);
                JSONObject output = new JSONObject();
                flatJSONObject(input, output);
                messageBuilder.setParameters(output);
            } catch (JSONException e) {
                Log.e(TAG, "flatJSONObject in convertMessageStrategy4 error");
                e.printStackTrace();
            }
        } else {
            //是speech
            messageBuilder.setType(UnderstandResult.Message.Builder.TYPE_TEXT);
            JSONObject param = new JSONObject();
            try {
                param.put("speech", value);
            } catch (JSONException e) {
                Log.e(TAG, "JSONObject put \"speech\" error");
                e.printStackTrace();
            }
            messageBuilder.setParameters(param);
        }
        messages.add(messageBuilder.build());

        return messages;
    }

    @Override
    public UnderstandResult.Fulfillment convertFulfillment() {
        UnderstandResult.Fulfillment fulfillment = null;
        switch (strategy) {
            case STRATEGY_1:
                fulfillment = convertFulfillmentStrategy1();
                break;
            case STRATEGY_2:
                fulfillment = convertFulfillmentStrategy2();
                break;
            case STRATEGY_3:
                fulfillment = convertFulfillmentStrategy3();
                break;
            case STRATEGY_4:
                fulfillment = convertFulfillmentStrategy4();
                break;
        }
        return fulfillment;
    }

    private UnderstandResult.Fulfillment convertFulfillmentStrategy1() {
        UnderstandResult.Fulfillment.Builder builder = new UnderstandResult.Fulfillment.Builder();
        List<UnderstandResult.Message> messages = convertMessage();
        builder.setMessages(messages);
        UnderstandResult.Message.Builder legacyMessageBuilder =
                new UnderstandResult.Message.Builder();
        legacyMessageBuilder.setType(UnderstandResult.Message.Builder.TYPE_ORIGINAL);
        legacyMessageBuilder.setParameters(getDataJSONObject());
        legacyMessageBuilder.setPlatform(mPlatform);
        UnderstandResult.Status status = convertStatus();
        builder.setStatus(status);

        return builder.build();
    }

    private UnderstandResult.Fulfillment convertFulfillmentStrategy2() {
        UnderstandResult.Fulfillment.Builder fulfillmentBuilder =
                new UnderstandResult.Fulfillment.Builder();
        fulfillmentBuilder.setSpeech(getSpeech());
        List<UnderstandResult.Message> messages = convertMessage();
        fulfillmentBuilder.setMessages(messages);

        UnderstandResult.Message.Builder messageBuilder = new UnderstandResult.Message.Builder();
        messageBuilder.setType(UnderstandResult.Message.Builder.TYPE_ORIGINAL);
        messageBuilder.setPlatform(mPlatform);
        messageBuilder.setParameters(getDataJSONObject());

        UnderstandResult.Status status = convertStatus();
        fulfillmentBuilder.setStatus(status);

        return fulfillmentBuilder.build();
    }

    private UnderstandResult.Fulfillment convertFulfillmentStrategy3() {
        UnderstandResult.Fulfillment.Builder fulfillmentBuilder =
                new UnderstandResult.Fulfillment.Builder();
        List<UnderstandResult.Message> messages = convertMessage();
        fulfillmentBuilder.setMessages(messages);
        UnderstandResult.Status status = convertStatus();
        fulfillmentBuilder.setStatus(status);

        //根据是否有speech 字段 填充外层的speech
        String speech = checkMessageSpeech(messages);
        if (!TextUtils.isEmpty(speech)) {
            fulfillmentBuilder.setSpeech(speech);
        }

        return fulfillmentBuilder.build();
    }

    private UnderstandResult.Fulfillment convertFulfillmentStrategy4() {
        UnderstandResult.Fulfillment.Builder fulfillmentBuilder =
                new UnderstandResult.Fulfillment.Builder();
        List<UnderstandResult.Message> messages = convertMessage();
        fulfillmentBuilder.setMessages(messages);
        UnderstandResult.Status status = convertStatus();
        fulfillmentBuilder.setStatus(status);

        //根据是否有speech 字段 填充外层的speech
        String speech = checkMessageSpeech(messages);
        if (!TextUtils.isEmpty(speech)) {
            fulfillmentBuilder.setSpeech(speech);
        }

        return fulfillmentBuilder.build();
    }

    @Override
    public UnderstandResult.Status convertStatus() {
        UnderstandResult.Status status = null;
        switch (strategy) {
            case STRATEGY_1:
                status = convertStatusStrategy1();
                break;
            case STRATEGY_2:
                status = convertStatusStrategy2();
                break;
            case STRATEGY_3:
                status = convertStatusStrategy3();
                break;
            case STRATEGY_4:
                status = convertStatusStrategy4();
                break;
        }
        return status;
    }

    private UnderstandResult.Status convertStatusStrategy1() {
        UnderstandResult.Status.Builder builder = new UnderstandResult.Status.Builder();
        return builder.build();
    }

    private UnderstandResult.Status convertStatusStrategy2() {
        UnderstandResult.Status.Builder builder = new UnderstandResult.Status.Builder();
        builder.setCode(200);
        builder.setErrorMessage("success");
        builder.setErrorDetails("");
        return builder.build();
    }

    private UnderstandResult.Status convertStatusStrategy3() {
        UnderstandResult.Status.Builder builder = new UnderstandResult.Status.Builder();
        builder.setCode(200);
        builder.setErrorMessage("success");
        builder.setErrorDetails("");
        return builder.build();
    }

    private UnderstandResult.Status convertStatusStrategy4() {
        UnderstandResult.Status.Builder builder = new UnderstandResult.Status.Builder();
        builder.setCode(200);
        builder.setErrorMessage("success");
        builder.setErrorDetails("");
        return builder.build();
    }


    @Override
    public List<UnderstandResult.Context> convertContextList() {
        List<UnderstandResult.Context> contexts = null;
        switch (strategy) {
            case STRATEGY_1:
                contexts = convertContextStrategy1();
                break;
            case STRATEGY_2:
                contexts = convertContextStrategy2();
                break;
            case STRATEGY_3:
                contexts = convertContextStrategy3();
                break;
            case STRATEGY_4:
                contexts = convertContextStrategy4();
                break;
        }
        return contexts;
    }

    private List<UnderstandResult.Context> convertContextStrategy1() {
        ArrayList<UnderstandResult.Context> contexts = new ArrayList<>();
        return contexts;
    }

    private List<UnderstandResult.Context> convertContextStrategy2() {
        ArrayList<UnderstandResult.Context> contexts = new ArrayList<>();
        return contexts;
    }

    private List<UnderstandResult.Context> convertContextStrategy3() {
        ArrayList<UnderstandResult.Context> contexts = new ArrayList<>();
        return contexts;
    }

    private List<UnderstandResult.Context> convertContextStrategy4() {
        ArrayList<UnderstandResult.Context> contexts = new ArrayList<>();
        return contexts;
    }

    @Override
    public LegacyUnderstandResult.LegacyData convertLegacyData() {
        LegacyUnderstandResult.LegacyData legacyData = null;
        switch (strategy) {
            case STRATEGY_1:
                legacyData = convertLegacyDataStrategy1();
                break;
            case STRATEGY_2:
                legacyData = convertLegacyDataStrategy2();
                break;
            case STRATEGY_3:
                legacyData = convertLegacyDataStrategy3();
                break;
            case STRATEGY_4:
                legacyData = convertLegacyDataStrategy4();
                break;
        }
        return legacyData;
    }

    private LegacyUnderstandResult.LegacyData convertLegacyDataStrategy1() {
        return null;
    }

    private LegacyUnderstandResult.LegacyData convertLegacyDataStrategy2() {
        return null;
    }

    private LegacyUnderstandResult.LegacyData convertLegacyDataStrategy3() {
        LegacyUnderstandResult.LegacyData.Builder legacyBuilder =
                new LegacyUnderstandResult.LegacyData.Builder();
        JSONObject data = getDataJSONObject();
        String value = data.optString("value");
        legacyBuilder.setDataValue(value);

        if (isJsonObject(value)) {
            try {
                JSONObject valueJSONObject = new JSONObject(value);
                int appId = valueJSONObject.optInt("appid");
                legacyBuilder.setAppId(appId);
                String intent = valueJSONObject.optString("intent");
                legacyBuilder.setIntentName(intent);
            } catch (JSONException e) {
                Log.e(TAG, "create JSONObject error:" + value);
                e.printStackTrace();
            }
        }

        return legacyBuilder.build();
    }

    private LegacyUnderstandResult.LegacyData convertLegacyDataStrategy4() {
        LegacyUnderstandResult.LegacyData.Builder legacyBuilder =
                new LegacyUnderstandResult.LegacyData.Builder();
        JSONObject data = getDataJSONObject();
        String value = data.optString("value");
        legacyBuilder.setDataValue(value);

        if (isJsonObject(value)) {
            try {
                JSONObject valueJSONObject = new JSONObject(value);
                int appId = valueJSONObject.optInt("appid", -1);
                legacyBuilder.setAppId(appId);
                String intent = valueJSONObject.optString("intent");
                legacyBuilder.setIntentName(intent);
            } catch (JSONException e) {
                Log.e(TAG, "create JSONObject error:" + value);
                e.printStackTrace();
            }
        } else {
            //fallback
            legacyBuilder.setAppId(-1);
        }

        return legacyBuilder.build();
    }

    @Override
    public LegacyUnderstandResult convert() {
        strategy = getStrategy(mRoot);
        LegacyUnderstandResult convert = super.convert();

        return convert;
    }

    private int getStrategy(JSONObject root) {
        JSONArray intents = root.optJSONArray("intent");
        if (null != intents && intents.length() != 0) {
            //是否包含关键intent
            if (isContainKeyIntent(intents)) {
                return STRATEGY_1;
            }

            JSONArray data = root.optJSONArray("data");
            if (null != data && data.length() != 0) {
                JSONObject object = data.optJSONObject(0);
                String value = object.optString("value");
                if (!isJsonObject(value)) {
                    return STRATEGY_2;
                }
                return STRATEGY_3;
            }
        } else {
            //问答定制普通文字格式
            //appid#page作为intent.name；intent.socre = 1；intent.parameter 为{} value的jsonfulfillment,
            // 如果不是json就为fallbackIntent
            JSONArray data = root.optJSONArray("data");
            if (null != data && data.length() != 0) {
                JSONObject object = data.optJSONObject(0);
                String value = object.optString("value");
                if (!isJsonObject(value)) {
                    return STRATEGY_4;
                }
                return STRATEGY_4;
            }
        }
        return -1;
    }

    /**
     * {
     * "type": "text",
     * "value": "位置导航",
     * "score": 100,
     * "category": "userDefine",
     * "data": {
     * "param": {
     * "entities_root>>position": [
     * "饮水机"
     * ]
     * }
     * }
     * }
     * return slots  [key ,value]
     */
    public List<Pair<String, JSONArray>> getIntentSlotParamPairs(JSONObject jsonObject)
            throws JSONException {

        List<Pair<String, JSONArray>> pairs = new ArrayList<>();
        JSONObject data = jsonObject.optJSONObject("data");
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

    public JSONObject getDataJSONObject() {
        JSONArray data = mRoot.optJSONArray("data");
        if (null != data && data.length() != 0) {
            try {
                JSONObject dataOne = data.getJSONObject(0);
                return dataOne;
            } catch (JSONException e) {
                Log.e(TAG, "getDataJSONObject error:" + data.toString());
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    /**
     * check whether the fulfillment has speech text
     *
     * @return speech text or ""
     */
    public String checkMessageSpeech(List<UnderstandResult.Message> messages) {
        String speech = "";
        if (null == messages && messages.size() == 0) {
            return speech;
        }

        for (UnderstandResult.Message message : messages) {
            if (UnderstandResult.Message.Builder.TYPE_TEXT.equals(message.getType())) {
                speech = message.getParameters().optString("speech");
                break;
            }
        }
        return speech;
    }

    private boolean isContainKeyIntent(JSONArray array) {
        List<String> keys = new ArrayList<String>() {
            {
                add("flight");
            }
        };

        if (null == array || array.length() == 0) {
            return false;
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                String value = jsonObject.getString("value");
                if (keys.contains(value)) {
                    return true;
                }
            } catch (JSONException e) {
                Log.e(TAG, "isContainKeyIntent() error:");
                e.printStackTrace();
            }
        }
        return false;
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

    private JSONObject getPresetIntentJsonObject() throws JSONException {

        JSONArray intents = mRoot.optJSONArray("intent");

        for (int i = 0; i < intents.length(); i++) {
            JSONObject intent = intents.getJSONObject(i);
            String value = intent.optString("value");
            if ("flight".equals(value)) {
                return intent;
            }
        }

        return null;
    }
}
