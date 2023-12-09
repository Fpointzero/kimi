package xyz.fpointzero.android.utils.crypto;

import android.util.Log;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public final static String TAG = "MD5Util";
    public static String stringToMD5(String input) {
        try {
            // 计算 MD5 哈希值
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // 将哈希值转换为十六进制字符串
            StringBuilder hexBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexBuilder.append(hex);
            }
            return hexBuilder.toString();
        } catch (Exception e) {
            Log.e(TAG, "stringToMD5: ", e);
            return null;
        }

        
    }

    public static String bytesToMD5(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input);
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = String.format("%02x", b);
            hexBuilder.append(hex);
        }

        return hexBuilder.toString();
    }
}
