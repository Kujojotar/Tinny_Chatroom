package com.james.chat.codec.ssl;

public class CbcBlockCipher {
    private byte[] IV;
    private byte[] content;
    private byte[] mac;
    private byte[] padding;
    private int paddingLength;

    public CbcBlockCipher(byte[] IV, byte[] content, byte[] mac, byte[] padding) {
        this.IV = IV;
        this.content = content;
        this.mac = mac;
        this.padding = padding;
        this.paddingLength = padding.length;
    }

    public byte[] getIV() {
        return IV;
    }

    public void setIV(byte[] IV) {
        this.IV = IV;
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

    public byte[] getPadding() {
        return padding;
    }

    public void setPadding(byte[] padding) {
        this.padding = padding;
    }

    public int getPaddingLength() {
        return paddingLength;
    }

    public void setPaddingLength(int paddingLength) {
        this.paddingLength = paddingLength;
    }
}
