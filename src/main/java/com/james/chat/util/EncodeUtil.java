package com.james.chat.util;

import java.security.MessageDigest;

public class EncodeUtil {

    public static byte[] getMd5EncodedStr(String key) throws Exception{
        MessageDigest digest = MessageDigest.getInstance("MD5");
        return digest.digest(key.getBytes());
    }

    public static String encodeStr(String str) throws Exception{
        byte[] arr = getMd5EncodedStr(str);
        String newStr = new String(arr);
        newStr += newStr.substring(newStr.length()-3, newStr.length());
        newStr += newStr.substring(0,4);
        arr = getMd5EncodedStr(newStr);
        return new String(arr).substring(0,12);
    }

}
