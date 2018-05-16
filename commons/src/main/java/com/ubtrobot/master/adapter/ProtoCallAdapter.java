package com.ubtrobot.master.adapter;

import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.ProgressivePromise;
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
        return syncCall(path, null, resParamClass);
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

    public <D, M extends Message, F extends Exception> Promise<D, F>
    call(String path, DFProtoConverter<D, M, F> converter) {
        return call(path, null, converter);
    }

    public <D, M extends Message, F extends Exception> Promise<D, F>
    call(String path, Message protoParam, final DFProtoConverter<D, M, F> converter) {
        return mCallAdapter.call(
                path,
                protoParam == null ? null : ProtoParam.create(protoParam),
                new CallAdapter.DFConverter<D, F>() {
                    @Override
                    public D convertDone(Param param) throws F {
                        if (param.isEmpty()) {
                            return null;
                        }

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

    public <F extends Exception> Promise<Void, F>
    call(String path, Message protoParam, CallAdapter.FConverter<F> converter) {
        return mCallAdapter.call(path, protoParam == null ? null : ProtoParam.create(protoParam),
                converter);
    }

    public <F extends Exception> Promise<Void, F>
    call(String path, CallAdapter.FConverter<F> converter) {
        return mCallAdapter.call(path, null, converter);
    }

    public <D, DM extends Message, F extends Exception, P, PM extends Message> ProgressivePromise<D, F, P>
    callStickily(String path, Message protoParam, final DFPProtoConverter<D, DM, F, P, PM> converter) {
        return mCallAdapter.callStickily(
                path,
                protoParam == null ? null : ProtoParam.create(protoParam),
                new CallAdapter.DFPConverter<D, F, P>() {
                    @Override
                    public P convertProgress(Param param) throws F {
                        try {
                            return converter.convertProgress(ProtoParam.from(
                                    param, converter.progressProtoClass()).getProtoMessage());
                        } catch (ProtoParam.InvalidProtoParamException e) {
                            throw convertFail(new CallException(CallGlobalCode.INTERNAL_ERROR,
                                    "Response illegal protobuf message.", e));
                        }
                    }

                    @Override
                    public D convertDone(Param param) throws F {
                        if (param.isEmpty()) {
                            return null;
                        }

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

    public <D, DM extends Message, F extends Exception, P, PM extends Message> ProgressivePromise<D, F, P>
    callStickily(String path, DFPProtoConverter<D, DM, F, P, PM> converter) {
        return callStickily(path, null, converter);
    }

    public <F extends Exception, P, PM extends Message> ProgressivePromise<Void, F, P>
    callStickily(String path, Message protoParam, FPProtoConverter<F, P, PM> converter) {
        return callStickily(path, protoParam, (DFPProtoConverter<Void, Message, F, P, PM>) converter);
    }

    public <F extends Exception, P, PM extends Message> ProgressivePromise<Void, F, P>
    callStickily(String path, FPProtoConverter<F, P, PM> converter) {
        return callStickily(path, null, (DFPProtoConverter<Void, Message, F, P, PM>) converter);
    }

    public interface DFProtoConverter<D, DM extends Message, F extends Exception> {

        Class<DM> doneProtoClass();

        D convertDone(DM protoParam);

        F convertFail(CallException e);
    }

    public interface DFPProtoConverter<D, DM extends Message, F extends Exception, P, PM extends Message> {

        Class<DM> doneProtoClass();

        D convertDone(DM protoParam);

        F convertFail(CallException e);

        Class<PM> progressProtoClass();

        P convertProgress(PM protoParam);
    }

    public abstract static class FPProtoConverter<F extends Exception, P, PM extends Message>
            implements DFPProtoConverter<Void, Message, F, P, PM> {

        @Override
        public final Class<Message> doneProtoClass() {
            return null;
        }

        @Override
        public final Void convertDone(Message protoParam) {
            return null;
        }
    }
}