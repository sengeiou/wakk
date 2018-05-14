package com.ubtrobot.nlp.http;

import android.util.Log;

import com.ubtrobot.okhttp.interceptor.sign.AuthorizationInterceptor;
import com.ubtrobot.okhttp.interceptor.sign.HttpSignInterceptor;
import com.ubtrobot.retrofit.adapter.urest.URestCallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitWrapper {
    private static volatile RetrofitWrapper mInstance;
    private Retrofit retrofit;
    private OkHttpClient client;

    private Retrofit retrofit1;
    private OkHttpClient client1;

    final String deviceId = "";
    final String appId = "20010";
    final String appKey = "d4277920aae16f8087801b7ed4e17ff8";

    final String appId1 = "20020";
    final String appKey1 = "15fcf9be76bf4739bff9c66c8ed813fd";

    final String appId2 = "20030";
    final String appKey2 = "5d29eb0b9a2b427d8f7818ff9b6e2717";

    private RetrofitWrapper() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("HttpLog", message);
            }
        });

        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new AuthorizationInterceptor(new AuthorizationInterceptor.AuthenticationInfoSource() {
                    @Override
                    public String getAuthentication(Request request) {
                        return null;
                    }

                    @Override
                    public String getDeviceId(Request request) {
                        return deviceId;
                    }
                }))
                //.addInterceptor(new HttpSignInterceptor(appId, appKey))
                .build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(NlpConfig.UBT_URL_PATH)      //访问主机地址
                .addConverterFactory(GsonConverterFactory.create())  //解析方式
                .addCallAdapterFactory(URestCallAdapterFactory.create())
                .build();


        HttpLoggingInterceptor httpLoggingInterceptor1 = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("HttpLog", message);
            }
        });

        httpLoggingInterceptor1.setLevel(HttpLoggingInterceptor.Level.BODY);
        client1 = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor1)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        retrofit1 = new Retrofit.Builder()
                .client(client1)
                .baseUrl(NlpConfig.URL_PATH)      //访问主机地址
                .addConverterFactory(GsonConverterFactory.create())  //解析方式
                //.addCallAdapterFactory(URestCallAdapterFactory.create())
                .build();
    }

    public static RetrofitWrapper get() {
        if (null == mInstance) {
            synchronized (RetrofitWrapper.class) {
                if (null == mInstance) {
                    mInstance = new RetrofitWrapper();
                }
            }
        }
        return mInstance;
    }

    public <T> T create(final Class<T> service) {
        return retrofit.create(service);
    }
    public  EmotibotService createEmotibotService(final Class<EmotibotService> service) {
        return retrofit1.create(service);
    }
}
