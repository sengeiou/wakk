package com.ubtrobot.master.adapter;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

public class ProtoParamParser {

    private ProtoParamParser() {
    }

    public static <T extends Message> T parseParam(
            Request request,
            Class<T> clazz,
            Responder responder) {
        try {
            return ProtoParam.from(request.getParam(), clazz).getProtoMessage();
        } catch (ProtoParam.InvalidProtoParamException e) {
            responder.respondFailure(CallGlobalCode.BAD_REQUEST, "Bad request. Illegal proto " +
                    "param. cause=" + e.getMessage());
            return null;
        }
    }

    public static String parseStringParam(Request request, Responder responder) {
        StringValue stringValue = parseParam(request, StringValue.class, responder);
        if (stringValue == null) {
            return null;
        }

        return stringValue.getValue();
    }
}