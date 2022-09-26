package com.james.chat.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {
    public static String ALGORITHM = "AES";

    public static String AES_CBC_PADDING = "AES/CBC/PKCS5Padding";

    public static byte[] encrypt(final byte[] key, final byte[] IV, final byte[] message) throws Exception {
        return AesUtil.encryptDecrypt(Cipher.ENCRYPT_MODE, key, IV, message);
    }

    public static byte[] decrypt(final byte[] key, final byte[] IV, final byte[] message) throws Exception {
        return AesUtil.encryptDecrypt(Cipher.DECRYPT_MODE, key, IV, message);
    }

    private static byte[] encryptDecrypt(final int mode, final byte[] key,
                                         final byte[] IV, final byte[] message) throws Exception {
        final Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
        final SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        final IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(mode, keySpec, ivSpec);
        return cipher.doFinal(message);
    }

    public static String getHex(byte[] data, int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            String hexStr = Integer.toHexString(((int) data[i]) & 0xFF);
            if (hexStr.length() < 2) {
                sb.append("0").append(hexStr.toUpperCase());
            } else {
                sb.append(hexStr.toUpperCase());
            }
        }
        return sb.toString();
    }
}
