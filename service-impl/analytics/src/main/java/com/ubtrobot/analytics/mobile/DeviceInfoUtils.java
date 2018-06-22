package com.ubtrobot.analytics.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;

public class DeviceInfoUtils {

    private static final String SP_NAME = "deviceId";
    private static final String SP_NAME_KEY = "uuid";

    private static final String MAC = "MAC";
    private static final String ANDROID_ID = "ANDROID_ID";
    private static final String UUID = "UUID";

    private static String mCoreDataFromName;
    private static String mCoreData;
    private static String mDeviceId;

    public static String getModel() {
        return Build.MODEL;
    }

    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getSimOperator(Context context) {
        // 此处返回运营商编号
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getSimOperator();
    }

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getTimezone() {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    }

    public static void setDeviceId(Context context, String deviceId) {
        saveSpDeviceId(context, deviceId);
    }

    public static String getDeviceId(Context context) {
        if (mDeviceId == null || mDeviceId.length() == 0) {
            mDeviceId = fromFile(context);
        }
        return mDeviceId;
    }

    private static String fromFile(Context context) {
        String rDevId = "";
        String spDevId = getSpDeviceId(context);
        if (spDevId == null || spDevId.length() == 0) {
            saveSpDeviceId(context, rDevId);
            rDevId = fromAutoGenerate(context);
        } else {
            rDevId = spDevId;
        }

        return rDevId;
    }

    private static String fromAutoGenerate(Context context) {
        getDeviceIdCoreData(context);

        StringBuffer randomCode = new StringBuffer();
        for (int i = 0; i < 5; i++) {
            randomCode.append(randomString());
        }

        String rDevId;
        long time = System.currentTimeMillis();
        String applicationId = context.getPackageName();
        boolean isGenerate = !TextUtils.isEmpty(mCoreDataFromName) && TextUtils.isEmpty(mCoreData);
        rDevId = !isGenerate ? "" : String.format("%s-%s-%s-%s-%s",
                applicationId,
                mCoreDataFromName, mCoreData,
                time, randomCode.toString());

        return md5(rDevId);
    }

    private static String md5(String s) {
        if (null == s) {
            return "";
        }

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};

        byte[] bytes = s.getBytes();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            byte[] md = digest.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            // 此处的异常不处理，因为最终返回长度为0的字符串
        }

        return "";
    }

    private static void saveSpDeviceId(Context context, String deviceId) {
        if (deviceId == null) {
            return;
        }

        SharedPreferences sp = context.getApplicationContext().
                getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_NAME_KEY, deviceId);
        editor.apply();
    }

    private static String getSpDeviceId(Context context) {
        SharedPreferences sp = context.getApplicationContext().
                getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(SP_NAME_KEY, "");
    }

    private static void getDeviceIdCoreData(Context context) {
        String macAddress = getMAC();
        if (!TextUtils.isEmpty(macAddress)) {
            mCoreDataFromName = MAC;
            mCoreData = macAddress;
            return;
        }

        String androidId = getAndroidId(context);
        if (!TextUtils.isEmpty(androidId)) {
            mCoreDataFromName = ANDROID_ID;
            mCoreData = androidId;
            return;
        }

        String uuid = getUuid();
        if (!TextUtils.isEmpty(uuid)) {
            mCoreDataFromName = UUID;
            mCoreData = uuid;
            return;
        }
    }

    private static String getMAC() {
        int version = Build.VERSION.SDK_INT;
        final int versionM = Build.VERSION_CODES.M;

        String mac = "";

        if (version >= versionM) {
            mac = getWifiMacFromInterface();

            if (TextUtils.isEmpty(mac)) {
                mac = getWifiMacFromFile();
            }
        }

        if ("02:00:00:00:00:00".equals(mac)) {
            mac = "";
        }

        return mac;
    }

    private static String getAndroidId(Context context) {
        String str = null;
        try {
            str = Settings.Secure.getString(context.getContentResolver(), "android_id");
            str = !TextUtils.isEmpty(str) && "9774d56d682e549c".equals(str) ? "" : str;
        } catch (Exception localException) {
            // 异常无需处理，没有获取到，即认为返回值为空
        }

        return str;
    }

    private static String getUuid() {
        String systemId = java.util.UUID.randomUUID().toString().
                replace("-", "");
        int length = 5;
        String[] chars = new String[length];
        int[] coordinates = new int[length];
        for (int i = 0; i < length; i++) {
            chars[i] = randomString();
            int coo = (int) (Math.random() * 10);
            coordinates[i] = coo;
        }

        StringBuilder builder = new StringBuilder(systemId);

        for (int i = 0; i < length; i++) {
            builder.insert(coordinates[i], chars[i]);
            builder.append(coordinates[i]);
        }

        return builder.toString();
    }

    private static String getWifiMacFromInterface() {
        try {
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) enumeration.nextElement();
                if (("wlan0".equals(networkInterface.getName())) ||
                        ("eth0".equals(networkInterface.getName()))) {
                    byte[] arrayOfByte1 = networkInterface.getHardwareAddress();
                    if ((arrayOfByte1 == null) || (arrayOfByte1.length == 0)) {
                        return "";
                    }
                    StringBuilder builder = new StringBuilder();
                    for (byte b1 : arrayOfByte1) {
                        builder.append(String.format("%02X:", new Object[]{Byte.valueOf(b1)}));
                    }
                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    return builder.toString().toLowerCase(Locale.getDefault());
                }
            }
        } catch (Throwable localThrowable) {
            // 异常无需处理，没有获取到，即认为返回值为空
        }
        return "";
    }

    private static String getWifiMacFromFile() {
        String[] arrayOfString = {"/sys/class/net/wlan0/address",
                "/sys/class/net/eth0/address", "/sys/devices/virtual/net/wlan0/address"};
        for (int i = 0; i < arrayOfString.length; i++) {
            String str = readFile(arrayOfString[i]);
            if (str == null || str.length() == 0) {
                return str;
            }
        }
        return "";
    }

    private static String readFile(String filePath) {
        String str = "";

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {

            fileReader = new FileReader(filePath);
            bufferedReader = null;
            if (fileReader != null) {
                bufferedReader = new BufferedReader(fileReader, 1024);
                str = bufferedReader.readLine();
            }

        } catch (Throwable localThrowable1) {
            str = "";
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (Throwable localThrowable4) {
                    fileReader = null;
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Throwable localThrowable5) {
                    bufferedReader = null;
                }
            }
        }

        return str;
    }

    private static String randomString() {
        String str = "";
        int index = (int) (Math.random() * 62);
        if (index < 10) {
            str = String.valueOf(index);
        } else if (index >= 10 && index < 36) {
            str = integerToString(65, index, 10);
        } else {
            str = integerToString(97, index, 36);
        }

        return str;
    }

    private static String integerToString(int firstLetter, int tigerIndex, int displacement) {
        int capIndex = firstLetter + (tigerIndex - displacement);
        char cap = (char) capIndex;
        return String.valueOf(cap);
    }

}
