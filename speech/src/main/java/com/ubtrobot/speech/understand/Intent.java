package com.ubtrobot.speech.understand;

import java.util.HashMap;
import java.util.Map;

public class Intent {

    private String destName;
    private String destAction;
    private Map<String, Param> intentParameterMap = new HashMap<>();

    private Map<String, String> fulfillment = new HashMap<>();

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public String getDestAction() {
        return destAction;
    }

    public void setDestAction(String destAction) {
        this.destAction = destAction;
    }

    public Map<String, Param> getIntentParameterMap() {
        return intentParameterMap;
    }

    public void setIntentParameterMap(Map<String, Param> intentParameterMap) {
        this.intentParameterMap = intentParameterMap;
    }

    public Map<String, String> getFulfillment() {
        return fulfillment;
    }

    public void setFulfillment(Map<String, String> fulfillment) {
        this.fulfillment = fulfillment;
    }

    public static class Param {
        private String value = "";
        private String type = "";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

}
