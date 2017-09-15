package com.diagrams.lib.util.crypt;

import android.text.TextUtils;
import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by xudong.wang on 17/8/1.
 */
public class AES {

    //wifi万能钥匙合作的key
    public static final byte[] WIFI_KEY = "KuWoa+=bhL63WiFi".getBytes();
    private static final byte[] IV = "e1i3jYEB#SWLuzhY".getBytes();

    public static byte[] encrypt(byte[] key, byte[] data) throws Exception {
        if (key == null || data == null) {
            return null;
        }
        return cipher(Cipher.ENCRYPT_MODE, key, data);
    }

    private static String encrypt(String key, String data) throws Exception {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(data)) {
            return null;
        }
        byte[] encrypted = encrypt(key.getBytes(), data.getBytes());

        //此处使用BASE64做转码功能，同时能起到2次加密的作用。
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }

    private static byte[] cipher(int mode, byte[] key, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, skeySpec, new IvParameterSpec(IV));
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] key, byte[] data) throws Exception {
        if (key == null || data == null) {
            return null;
        }
        return cipher(Cipher.DECRYPT_MODE, key, data);
    }

    private static String decrypt(String key, String data) throws Exception {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(data)) {
            return null;
        }
        //先用base64解密
        byte[] decSrc = Base64.decode(data, Base64.NO_WRAP);

        //AES解密
        byte[] decrypted = decrypt(key.getBytes(), decSrc);
        return new String(decrypted);
    }
}
