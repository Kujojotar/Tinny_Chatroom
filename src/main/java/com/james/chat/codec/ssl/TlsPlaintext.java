package com.james.chat.codec.ssl;

public class TlsPlaintext {
    private static final int MAX_PAYLOAD = 1<<14;

    private TlsContentType type;
    private TlsProtocolVersion version;
    private int length;
    private byte[] content;

    public TlsPlaintext(TlsContentType type, TlsProtocolVersion version, int length, byte[] content) {
        this.type = type;
        this.version = version;
        this.length = length;
        this.content = content;
    }

    public TlsContentType getType() {
        return type;
    }

    public void setType(TlsContentType type) {
        this.type = type;
    }

    public TlsProtocolVersion getVersion() {
        return version;
    }

    public void setVersion(TlsProtocolVersion version) {
        this.version = version;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
