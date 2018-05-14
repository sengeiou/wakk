package com.ubtrobot.speech.understand;

import org.json.JSONObject;

import java.util.List;

public class Converter3 extends AbstractConverter {

    JSONObject root;
    private String platform;

    public Converter3(JSONObject root,String platform) {
        super(root);
        this.root = root;
        this.platform = platform;
    }

    @Override
    public UnderstandResult.Intent convertIntent() {
        return null;
    }

    @Override
    public List<UnderstandResult.Message> convertMessage() {
        return null;
    }

    @Override
    public UnderstandResult.Fulfillment convertFulfillment() {
        return null;
    }

    @Override
    public UnderstandResult.Status convertStatus() {
        return null;
    }

    @Override
    public List<UnderstandResult.Context> convertContextList() {
        return null;
    }
}
