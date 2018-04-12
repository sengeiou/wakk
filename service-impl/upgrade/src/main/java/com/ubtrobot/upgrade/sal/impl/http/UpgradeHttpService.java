package com.ubtrobot.upgrade.sal.impl.http;

import com.ubtrobot.retrofit.adapter.urest.URestCall;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UpgradeHttpService {

    String SERVICE_URL = "http://10.10.20.71:8032/v1/";

    @GET("upgrade-rest/version/upgradable")
    URestCall<List<DTPackage>> detectUpgrade(
            @Query("productName") String packageGroup,
            @Query("moduleNames") String packageNames,
            @Query("versionNames") String versions
    );
}