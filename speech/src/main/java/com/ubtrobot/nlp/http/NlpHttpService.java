package com.ubtrobot.nlp.http;

import com.ubtrobot.retrofit.adapter.urest.URestCall;

import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NlpHttpService {
    @POST("nlp/ubtUniformNlp")
    URestCall<DTPackage> understand(@Body Param param);
}