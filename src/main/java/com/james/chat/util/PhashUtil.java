package com.james.chat.util;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.prng.RandomGenerator;
import org.bouncycastle.crypto.tls.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class PhashUtil {

    public static String PRF(String secret, String label, String seed, int minSize) {
        String res;
        try {
            res = new String(TlsUtils.PRF(getMockedContext(), secret.getBytes(), label, seed.getBytes(), minSize));
        } catch (Exception e) {
            e.printStackTrace();
            res = null;
        }
        return res;
    }

    public static String pHash(String secret, String seed, int ensureCapacity) throws Exception{
        String prevStr;
        String res = "";
        prevStr = encodeHmac(secret, seed);                    // A1
        while (res.length() < ensureCapacity) {
            res += encodeHmac(secret, prevStr + seed);
            prevStr = encodeHmac(secret, prevStr);
        }
        return res;
    }

    public static String encodeHmac(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return new String(sha256_HMAC.doFinal(data.getBytes()));
    }

    public static byte[] PRF(byte[] secret, byte[] label, byte[] seed, int minSize) {
        byte[] res;
        try {
            res = TlsUtils.PRF(getMockedContext(), secret, new String(label,StandardCharsets.US_ASCII), seed, minSize);
        } catch (Exception e) {
            res = null;
        }
        return res;
    }

    public static byte[] pHash(byte[] secret, byte[] seed, int ensureCapacity) throws Exception{
        byte[] prevStr;
        byte[] res = {};
        prevStr = encodeHmac(seed, secret);                    // A1
        while (res.length < ensureCapacity) {
            byte[] tmpArr = new byte[prevStr.length + seed.length];
            System.arraycopy(prevStr, 0, tmpArr, 0, prevStr.length);
            System.arraycopy(seed, 0, tmpArr, prevStr.length, seed.length);
            byte[] encodedArr = encodeHmac(tmpArr,secret);
            tmpArr = new byte[res.length + encodedArr.length];
            System.arraycopy(res, 0, tmpArr, 0, res.length);
            System.arraycopy(encodedArr, 0, tmpArr, res.length, encodedArr.length);
            res = tmpArr;
            prevStr = encodeHmac(prevStr, secret);
        }
        return res;
    }

    public static byte[] encodeHmac(byte[] key, byte[] data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return sha256_HMAC.doFinal(data);
    }


    private static TlsContext getMockedContext() {
        return new TlsContext() {
            @Override
            public RandomGenerator getNonceRandomGenerator() {
                return null;
            }

            @Override
            public SecureRandom getSecureRandom() {
                return null;
            }

            @Override
            public SecurityParameters getSecurityParameters() {
                SecurityParameters securityParameters = new SecurityParameters();
                try {
                    Field field = securityParameters.getClass().getDeclaredField("prfAlgorithm");
                    field.setAccessible(true);
                    field.set(securityParameters, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    securityParameters = null;
                }
                return securityParameters;
            }

            @Override
            public boolean isServer() {
                return false;
            }

            @Override
            public ProtocolVersion getClientVersion() {
                return null;
            }

            @Override
            public ProtocolVersion getServerVersion() {
                return ProtocolVersion.TLSv12;
            }

            @Override
            public TlsSession getResumableSession() {
                return null;
            }

            @Override
            public Object getUserObject() {
                return null;
            }

            @Override
            public void setUserObject(Object o) {

            }

            @Override
            public byte[] exportKeyingMaterial(String s, byte[] bytes, int i) {
                return new byte[0];
            }
        };
    }

    public static void main(String[] args) {
        String res = new String(PhashUtil.PRF("jamesis2re".getBytes(StandardCharsets.US_ASCII),"master secret".getBytes(StandardCharsets.US_ASCII), "huhuhuhaha".getBytes(StandardCharsets.US_ASCII), 48),StandardCharsets.US_ASCII);
        System.out.println(res.length());
        for (byte b: "jamesis2re".getBytes()) {
            System.out.printf("%x",b);
        }
        System.out.println();
        for (byte b: "master secrethuhuhuhaha".getBytes()) {
            System.out.printf("%x",b);
        }
        System.out.println();
        for (byte b: res.getBytes()) {
            System.out.printf("%x",b);
        }
        System.out.println();
        byte[] resD = TlsUtils.PRF(getMockedContext(), "jamesis2re".getBytes(StandardCharsets.US_ASCII), "master secret", "huhuhuhaha".getBytes(StandardCharsets.US_ASCII),128);
        for (byte b: resD) {
            System.out.printf("%x",b);
        }
        System.out.println();
    }

    public static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length+ bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }
}
