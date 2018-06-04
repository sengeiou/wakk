package com.ubtrobot.nlp.http;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface EmotibotService {
    @FormUrlEncoded
    @POST("api/ApiKey/openapi.php")
    Call<JsonObject> understand(
            @Field("appid") String appid,
            @Field("userid") String userid,
            @Field("text") String text, @Field("cmd") String cmd,
            @Field("location") String location);
}
