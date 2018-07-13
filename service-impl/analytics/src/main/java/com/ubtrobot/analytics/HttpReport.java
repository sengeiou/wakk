package com.ubtrobot.analytics;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.ubtrobot.http.rest.UCodes;
import com.ubtrobot.http.rest.URestException;
import com.ubtrobot.okhttp.interceptor.sign.AuthorizationInterceptor;
import com.ubtrobot.okhttp.interceptor.sign.HttpSignInterceptor;
import com.ubtrobot.retrofit.adapter.urest.URestCall;
import com.ubtrobot.retrofit.adapter.urest.URestCallAdapterFactory;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class HttpReport implements EventReporter {

    //    private static final String BASE_URL = "https://apis.ubtrobot.com/v1/collect-rest/collected/";
    private static final String BASE_URL = "http://10.10.20.71:8033/v1/collect-rest/collected/";

    private final Context mContext;
    private final String mAppId;
    private final String mAppKey;
    private final String mDeviceId;

    private final ReportService mReportService;

    public HttpReport(Context context, String appId, String appKey, String deviceId) {
        mContext = context;
        mAppId = appId;
        mAppKey = appKey;

        if (deviceId == null || deviceId.length() == 0) {
            throw new IllegalArgumentException("deviceId is not null.");
        }
        mDeviceId = deviceId;
        mReportService = createHttpService();
    }

    private ReportService createHttpService() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpSignInterceptor(mAppId, mAppKey))
                .addInterceptor(new AuthorizationInterceptor(
                        new AuthorizationInterceptor.AuthenticationInfoSource() {  // 添加设备拦截器
                            @Override
                            public String getAuthentication(Request request) {
                                return null;
                            }

                            @Override
                            public String getDeviceId(Request request) {
                                return mDeviceId;
                            }
                        }))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(URestCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        return retrofit.create(ReportService.class);
    }

    @Override
    public void reportEvents(List<Event> events) throws ReportException {
        try {
            mReportService.reportEvent(events).execute();
        } catch (URestException e) {
            if (UCodes.ERR_FAILED_TO_ESTABLISH_CONNECTION == e.getCode()) {
                if (isNetworkDisconnected(mContext)) {
                    throw ReportException.Factory.networkDisconnected(e);
                } else {
                    throw ReportException.Factory.internalServerError(e);
                }
            }

            if (UCodes.ERR_SOCKET_TIMEOUT == e.getCode()) {
                throw ReportException.Factory.timeout(e);
            }

            if (UCodes.ERR_NETWORK_PERMISSION_DENIED == e.getCode()) {
                throw new IllegalStateException(
                        "Should add permission(android.permission.INTERNET).", e);
            }

            throw ReportException.Factory.internalServerError(e);
        }
    }

    /**
     * 网络连接断开？ true: 断开的
     *
     * @param context
     * @return
     */
    private boolean isNetworkDisconnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        return networkInfo.isConnected();
    }

    public interface ReportService {

        @POST("events")
        URestCall<Void> reportEvent(@Body List<Event> events);
    }

}
