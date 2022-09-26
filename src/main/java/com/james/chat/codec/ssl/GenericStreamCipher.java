package com.james.chat.codec.ssl;

public class GenericStreamCipher implements Cipher{

    private byte[] content;
    private byte[] mac;

    public GenericStreamCipher(byte[] content, byte[] mac) {
        this.content = content;
        this.mac = mac;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }
}
