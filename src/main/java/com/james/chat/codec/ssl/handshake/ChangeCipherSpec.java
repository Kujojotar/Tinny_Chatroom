package com.james.chat.codec.ssl.handshake;

public class ChangeCipherSpec extends TlsHeader {
    private byte b;

    public byte getB() {
        return b;
    }

    public void setB(byte b) {
        this.b = b;
    }
}
