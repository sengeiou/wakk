package com.ubtrobot.upgrade.sal;

import android.text.TextUtils;

import com.ubtrobot.async.CancelableAsyncTask;
import com.ubtrobot.http.rest.URestException;
import com.ubtrobot.okhttp.interceptor.sign.AuthorizationInterceptor;
import com.ubtrobot.okhttp.interceptor.sign.HttpSignInterceptor;
import com.ubtrobot.retrofit.adapter.urest.URestCall;
import com.ubtrobot.retrofit.adapter.urest.URestCallAdapterFactory;
import com.ubtrobot.retrofit.adapter.urest.URestCallback;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.Firmware;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import com.ubtrobot.upgrade.sal.impl.http.DTPackage;
import com.ubtrobot.upgrade.sal.impl.http.UpgradeHttpService;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class AbstractPlatformUpgradeService extends AbstractUpgradeService {

    private UpgradeHttpService mUpgradeService;

    private final String mFirmwarePackageGroupName;

    public AbstractPlatformUpgradeService() {
        final String deviceId = deviceId();
        final String appId = appId();
        final String appKey = appKey();
        mFirmwarePackageGroupName = firmwarePackageGroupName();

        if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey)
                || TextUtils.isEmpty(mFirmwarePackageGroupName)) {
            throw new IllegalStateException("deviceId(), appId(), appKey() or " +
                    "firmwarePackageGroupName() returns an empty string.");
        }

        OkHttpClient client = new OkHttpClient.Builder().
                addInterceptor(new AuthorizationInterceptor(
                        new AuthorizationInterceptor.AuthenticationInfoSource() {
                            @Override
                            public String getAuthentication(Request request) {
                                return null;
                            }

                            @Override
                            public String getDeviceId(Request request) {
                                return deviceId;
                            }
                        })).
                addInterceptor(new HttpSignInterceptor(appId, appKey)).
                build();

        Retrofit retrofit = new Retrofit.Builder().
                addConverterFactory(GsonConverterFactory.create()).
                addCallAdapterFactory(URestCallAdapterFactory.create()).
                client(client).
                baseUrl(UpgradeHttpService.SERVICE_URL).
                build();

        mUpgradeService = retrofit.create(UpgradeHttpService.class);
    }

    protected abstract String deviceId();

    protected abstract String appId();

    protected abstract String appKey();

    protected abstract String firmwarePackageGroupName();

    @Override
    protected CancelableAsyncTask<FirmwarePackageGroup, DetectException, Void>
    createDetectingFromRemoteSourceTask(long timeoutMills) {
        return new CancelableAsyncTask<FirmwarePackageGroup, DetectException, Void>() {

            private URestCall<List<DTPackage>> mRestCall;

            @Override
            protected synchronized void onCancel() {
                mRestCall.cancel();
                mRestCall = null;
            }

            @Override
            protected synchronized void onStart() {
                if (isCanceled()) {
                    return;
                }

                StringBuilder packageNames = new StringBuilder();
                StringBuilder versions = new StringBuilder();

                final String separator = ",";
                for (Firmware firmware : getFirmwareList()) {
                    packageNames.append(firmware.getName());
                    packageNames.append(separator);

                    versions.append(firmware.getVersion());
                    versions.append(separator);
                }

                packageNames.deleteCharAt(packageNames.length() - 1);
                versions.deleteCharAt(versions.length() - 1);

                mRestCall = mUpgradeService.detectUpgrade(mFirmwarePackageGroupName,
                        packageNames.toString(), versions.toString());

                mRestCall.enqueue(new URestCallback<List<DTPackage>>() {
                    @Override
                    public void
                    onSuccess(URestCall<List<DTPackage>> uRestCall, List<DTPackage> dtPackages) {
                        FirmwarePackageGroup.Builder builder = new FirmwarePackageGroup.Builder(
                                mFirmwarePackageGroupName);
                        long releaseTime = 0;
                        for (DTPackage dtPackage : dtPackages) {
                            try {
                                dtPackage.validate();
                            } catch (URestException e) {
                                reject(new DetectException.Factory().internalError("")); // TODO
                                return;
                            }

                            if (dtPackage.isForced) {
                                builder.setForced(true);
                            }

                            if (dtPackage.releaseTime > releaseTime) {
                                releaseTime = dtPackage.releaseTime;
                            }

                            FirmwarePackage.Builder packageBuilder = new FirmwarePackage.Builder(
                                    dtPackage.moduleName, dtPackage.versionName).
                                    setGroup(mFirmwarePackageGroupName).
                                    setForced(dtPackage.isForced).
                                    setIncremental(dtPackage.isIncremental).
                                    setPackageUrl(dtPackage.packageUrl).
                                    setPackageMd5(dtPackage.packageMd5).
                                    setIncrementUrl(dtPackage.incrementUrl).
                                    setIncrementMd5(dtPackage.incrementMd5).
                                    setReleaseTime(dtPackage.releaseTime).
                                    setReleaseNote(dtPackage.releaseNote);
                            if (dtPackage.isIncremental) {
                                packageBuilder.setIncrementUrl(dtPackage.incrementUrl).
                                        setIncrementSize(dtPackage.incrementSize).
                                        setIncrementMd5(dtPackage.incrementMd5);
                            } else {
                                packageBuilder.setPackageUrl(dtPackage.packageUrl).
                                        setPackageSize(dtPackage.packageSize).
                                        setPackageMd5(dtPackage.packageMd5);
                            }

                            builder.addPackage(packageBuilder.build());
                        }

                        builder.setReleaseTime(releaseTime);
                        resolve(builder.build());
                    }

                    @Override
                    public void onFailure(URestCall<List<DTPackage>> uRestCall, URestException e) {
                        reject(new DetectException.Factory().internalError("")); // TODO
                    }
                });
            }
        };
    }

    @Override
    protected CancelableAsyncTask<FirmwarePackageGroup, DetectException, Void>
    createDetectingFromLocalSourceTask(String sourcePath, long timeoutMills) {
        return null;
    }
}