package com.james.chat.codec.ssl.handshake;

public class ClientKeyExchange extends TlsHeader{
    private final int type = 16;
    private int length;
    private int publicKeyLength;
    private byte[] publicKey;

    public int getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPublicKeyLength() {
        return publicKeyLength;
    }

    public void setPublicKeyLength(int publicKeyLength) {
        this.publicKeyLength = publicKeyLength;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
