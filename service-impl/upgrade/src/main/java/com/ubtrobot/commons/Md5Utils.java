package com.ubtrobot.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

    private Md5Utils() {
    }

    public static String calculateFileMd5(String file) throws IOException {
        InputStream inputStream = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            inputStream = new FileInputStream(file);

            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }

            String md5 = new BigInteger(1, digest.digest()).toString(16);
            return String.format("%32s", md5).replace(' ', '0');
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}
