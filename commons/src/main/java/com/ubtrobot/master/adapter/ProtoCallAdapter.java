package com.ubtrobot.master.adapter;

import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.call.ConvenientCallable;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Response;

public class ProtoCallAdapter {

    private CallAdapter mCallAdapter;

    public ProtoCallAdapter(ConvenientCallable callable, Handler handler) {
        mCallAdapter = new CallAdapter(callable, handler);
    }

    public void syncCall(String path) throws CallException {
        syncCall(path, (Message) null);
    }

    public void syncCall(String path, Message protoParam) throws CallException {
        mCallAdapter.callable().call(
                path, protoParam == null ? null : ProtoParam.create(protoParam));
    }

    public <T extends Message> T
    syncCall(String path, Class<T> resParamClass) throws CallException {
        return syncCall(path, resParamClass);
    }

    public <T extends Message> T
    syncCall(String path, Message protoParam, Class<T> resParamClass) throws CallException {
        Response response = mCallAdapter.callable().call(
                path, protoParam == null ? null : ProtoParam.create(protoParam));

        try {
            return ProtoParam.from(response.getParam(), resParamClass).getProtoMessage();
        } catch (ProtoParam.InvalidProtoParamException e) {
            throw new CallException(CallGlobalCode.INTERNAL_ERROR,
                    "Response illegal protobuf message.", e);
        }
    }

    public <D, M extends Message, F extends Exception> Promise<D, F, Void>
    call(String path, DFProtoConverter<D, M, F> converter) {
        return call(path, null, converter);
    }

    public <D, M extends Message, F extends Exception> Promise<D, F, Void>
    call(String path, Message protoParam, final DFProtoConverter<D, M, F> converter) {
        return mCallAdapter.call(
                path,
                protoParam == null ? null : ProtoParam.create(protoParam),
                new CallAdapter.DFConverter<D, F>() {
                    @Override
                    public D convertDone(Param param) throws F {
                        try {
                            return converter.convertDone(ProtoParam.from(
                                    param, converter.doneProtoClass()).getProtoMessage());
                        } catch (ProtoParam.InvalidProtoParamException e) {
                            throw convertFail(new CallException(CallGlobalCode.INTERNAL_ERROR,
                                    "Response illegal protobuf message.", e));
                        }
                    }

                    @Override
                    public F convertFail(CallException e) {
                        return converter.convertFail(e);
                    }
                }
        );
    }

    public <F extends Exception> Promise<Void, F, Void>
    call(String path, Message protoParam, CallAdapter.FConverter<F> converter) {
        return mCallAdapter.call(path, protoParam == null ? null : ProtoParam.create(protoParam),
                converter);
    }

    public <F extends Exception> Promise<Void, F, Void>
    call(String path, CallAdapter.FConverter<F> converter) {
        return mCallAdapter.call(path, null, converter);
    }

    public interface DFProtoConverter<D, DM extends Message, F extends Exception> {

        Class<DM> doneProtoClass();

        D convertDone(DM protoParam);

        F convertFail(CallException e);
    }
}