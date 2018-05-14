package com.ubtrobot.speech.understand;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Converter2 extends AbstractConverter {

    private JSONObject root;
    private String platform;

    public Converter2(JSONObject root, String platform) {
        super(root);
        this.root = root;
        this.platform = platform;
    }

    @Override
    public UnderstandResult.Intent convertIntent() {
        UnderstandResult.Intent.Builder intentBuilder = new UnderstandResult.Intent.Builder();
        JSONArray intents = root.optJSONArray("intent");
        if (!isJsonArrayEmpty(intents)) {
            JSONObject object = intents.optJSONObject(0);
            String value = object.optString("value");
            intentBuilder.setName(value);
            intentBuilder.setScore(1);
        }
        return intentBuilder.build();
    }

    @Override
    public List<UnderstandResult.Message> convertMessage() {
        ArrayList<UnderstandResult.Message> messages = new ArrayList<>();
        UnderstandResult.Message.Builder messageBuilder = new UnderstandResult.Message.Builder();
        messageBuilder.setType(UnderstandResult.Message.Builder.TYPE_TEXT);
        messageBuilder.setPlatform(platform);
        messageBuilder.setParameters(getMessageJSONObject());
        messages.add(messageBuilder.build());

        return messages;
    }

    @Override
    public UnderstandResult.Fulfillment convertFulfillment() {

        UnderstandResult.Fulfillment.Builder fulfillmentBuilder =
                new UnderstandResult.Fulfillment.Builder();
        fulfillmentBuilder.setSpeech(getSpeech());
        List<UnderstandResult.Message> messages = convertMessage();
        fulfillmentBuilder.setMessages(messages);

        UnderstandResult.Message.Builder messageBuilder = new UnderstandResult.Message.Builder();
        messageBuilder.setType(UnderstandResult.Message.Builder.TYPE_ORIGINAL);
        messageBuilder.setPlatform(platform);
        messageBuilder.setParameters(getLegacyMessageJSONObject());

        fulfillmentBuilder.setLegacyMessage(messageBuilder.build());
        UnderstandResult.Status status = convertStatus();
        fulfillmentBuilder.setStatus(status);

        return fulfillmentBuilder.build();
    }

    private String getSpeech() {
        String speech = "";
        JSONArray data = root.optJSONArray("data");
        if (!isJsonArrayEmpty(data)) {
            JSONObject object = data.optJSONObject(0);
            speech = object.optString("value");
        }
        return speech;
    }

    private JSONObject getMessageJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("speech", getSpeech());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public UnderstandResult.Status convertStatus() {
        UnderstandResult.Status.Builder builder = new UnderstandResult.Status.Builder();
        builder.setCode(200);
        builder.setErrorMessage("success");
        builder.setErrorDetails("");
        return builder.build();
    }

    @Override
    public List<UnderstandResult.Context> convertContextList() {
        ArrayList<UnderstandResult.Context> contexts = new ArrayList<>();
        return contexts;
    }
}
