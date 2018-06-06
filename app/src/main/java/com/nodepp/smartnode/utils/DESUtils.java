package com.nodepp.smartnode.utils;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * DES加解密算法
 * Created by yuyue on 2016/10/25.
 */
public class DESUtils {
    public static final String key = "nodepp.com";
    public static String encode(String data) throws Exception {
        return encode(key, data.getBytes());
    }

    public static String encode(String key, byte[] data) throws Exception {
        try {
            DESKeySpec e = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(e);
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(1, secretKey);
            byte[] bytes = cipher.doFinal(data);
            return Base64.encodeToString(bytes, 3);
        } catch (Exception var7) {
            throw new Exception(var7);
        }
    }

    public static byte[] decode(String key, byte[] data) throws Exception {
        try {
            DESKeySpec e = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(e);
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(2, secretKey);
            return cipher.doFinal(data);
        } catch (Exception var6) {
            throw new Exception(var6);
        }
    }

    public static String decodeValue( String data) {
        String value = null;

        try {
            byte[] datas;
            if(System.getProperty("os.name") == null || !System.getProperty("os.name").equalsIgnoreCase("sunos") && !System.getProperty("os.name").equalsIgnoreCase("linux")) {
                datas = decode(key, Base64.decode(data, 3));
            } else {
                datas = decode(key, Base64.decode(data, 3));
            }

            value = new String(datas);
        } catch (Exception var5) {

        }

        return value;
    }

}
