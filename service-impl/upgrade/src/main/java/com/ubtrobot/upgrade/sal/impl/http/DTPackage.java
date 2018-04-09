package com.ubtrobot.upgrade.sal.impl.http;

import android.text.TextUtils;

import com.ubtrobot.http.rest.UCodes;
import com.ubtrobot.http.rest.URestException;

import java.net.MalformedURLException;
import java.net.URL;

public class DTPackage {

    public String moduleName;
    public String versionName;
    public boolean isForced;
    public boolean isIncremental;
    public String packageUrl;
    public String packageMd5;
    public String incrementUrl;
    public String incrementMd5;
    public long releaseTime;
    public String releaseNote;

    public void validate() throws URestException {
        if (TextUtils.isEmpty(moduleName)) {
            throw new URestException(
                    UCodes.ERR_INTERNAL_SERVER_ERROR,
                    "Illegal module name. moduleName is empty."
            );
        }

        if (TextUtils.isEmpty(versionName)) {
            throw new URestException(
                    UCodes.ERR_INTERNAL_SERVER_ERROR,
                    "Illegal version name. versionName is empty."
            );
        }

        if (!isLegalUrl(packageUrl)) {
            throw new URestException(
                    UCodes.ERR_INTERNAL_SERVER_ERROR,
                    "Illegal package url. packageUrl=" + packageUrl
            );
        }

        if (!isLegalMd5(packageMd5)) {
            throw new URestException(
                    UCodes.ERR_INTERNAL_SERVER_ERROR,
                    "Illegal package md5. packageMd5=" + packageMd5
            );
        }

        if (isIncremental) {
            if (!isLegalUrl(incrementUrl)) {
                throw new URestException(
                        UCodes.ERR_INTERNAL_SERVER_ERROR,
                        "Illegal increment url. incrementUrl=" + incrementUrl
                );
            }

            if (!isLegalMd5(incrementMd5)) {
                throw new URestException(
                        UCodes.ERR_INTERNAL_SERVER_ERROR,
                        "Illegal increment md5. incrementMd5=" + incrementMd5
                );
            }
        }
    }

    private boolean isLegalUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private boolean isLegalMd5(String md5) {
        if (md5 == null) {
            return false;
        }

        return md5.matches("([0-9]|[a-f]|[A-F]){32}");
    }

    @Override
    public String toString() {
        return "DTPackage{" +
                "moduleName='" + moduleName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", isForced=" + isForced +
                ", isIncremental=" + isIncremental +
                ", packageUrl='" + packageUrl + '\'' +
                ", packageMd5='" + packageMd5 + '\'' +
                ", incrementUrl='" + incrementUrl + '\'' +
                ", incrementMd5='" + incrementMd5 + '\'' +
                ", releaseTime=" + releaseTime +
                ", releaseNote='" + releaseNote + '\'' +
                '}';
    }
}
