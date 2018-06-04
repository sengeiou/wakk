package com.ubtrobot.nlp.http;

/**
 * Created by cxdan on 2018/5/3.
 */

public class Param {

    /**
     * transactionId  : AB_001_09ad981b
     * requestTime  : 1524142051573
     * ubtNlpAppId  :  AB_001
     * deviceId : xxxx
     * inputValue : 感冒发烧咳嗽流鼻涕
     * inputType : 1
     * len :
     * sessionId : xxxx
     * location  : xxxx
     * apiVersion : V_01
     */

    private String transactionId;
    private String requestTime;
    private String ubtNlpAppId;
    private String deviceId;
    private String inputValue;
    private String inputType;
    private String len;
    private String sessionId;
    private String location;
    private String apiVersion;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getUbtNlpAppId() {
        return ubtNlpAppId;
    }

    public void setUbtNlpAppId(String ubtNlpAppId) {
        this.ubtNlpAppId = ubtNlpAppId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getLen() {
        return len;
    }

    public void setLen(String len) {
        this.len = len;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
