package com.james.chat.codec.ssl.handshake;

import com.james.chat.codec.ssl.TlsProtocolVersion;

public class TlsHeader {
    private int contentType = 22;
    private TlsProtocolVersion version = new TlsProtocolVersion(3,3);
    private int totalLength;

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public TlsProtocolVersion getVersion() {
        return version;
    }

    public void setVersion(TlsProtocolVersion version) {
        this.version = version;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }
}
