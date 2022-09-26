package com.james.chat.codec.ssl;

public class TlsProtocolVersion {

    public static final TlsProtocolVersion SSL_3_3 = new TlsProtocolVersion(3,3);

    private int major;
    private int minor;
    private byte[] arr = new byte[2];

    public TlsProtocolVersion(int major,int minor) {
        this.major = major;
        this.minor = minor;
        arr[0] = (byte)major;
        arr[1] = (byte)minor;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public byte[] getArr() {
        return arr;
    }

    public void setArr(byte[] arr) {
        this.arr = arr;
    }
}
