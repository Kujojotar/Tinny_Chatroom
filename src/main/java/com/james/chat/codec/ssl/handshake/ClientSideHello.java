package com.james.chat.codec.ssl.handshake;

import com.james.chat.codec.ssl.TlsProtocolVersion;

public class ClientSideHello {
    private TlsProtocolVersion clientVersion;
    private long gmtUnixTime;
    private byte[] random;
    private int sessionIdLength;
    private byte[] sessionId;
    private int cipherSuitesLength;
    private byte[] cipherSuites;
    private int compressionMethodLength;
    private byte[] compressionMethod;
    private int extensionLength;
    private byte[] extensiond;

    public TlsProtocolVersion getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(TlsProtocolVersion clientVersion) {
        this.clientVersion = clientVersion;
    }

    public long getGmtUnixTime() {
        return gmtUnixTime;
    }

    public void setGmtUnixTime(long gmtUnixTime) {
        this.gmtUnixTime = gmtUnixTime;
    }

    public byte[] getRandom() {
        return random;
    }

    public void setRandom(byte[] random) {
        this.random = random;
    }

    public int getSessionIdLength() {
        return sessionIdLength;
    }

    public void setSessionIdLength(int sessionIdLength) {
        this.sessionIdLength = sessionIdLength;
    }

    public byte[] getSessionId() {
        return sessionId;
    }

    public void setSessionId(byte[] sessionId) {
        this.sessionId = sessionId;
    }

    public int getCipherSuitesLength() {
        return cipherSuitesLength;
    }

    public void setCipherSuitesLength(int cipherSuitesLength) {
        this.cipherSuitesLength = cipherSuitesLength;
    }

    public byte[] getCipherSuites() {
        return cipherSuites;
    }

    public void setCipherSuites(byte[] cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    public int getCompressionMethodLength() {
        return compressionMethodLength;
    }

    public void setCompressionMethodLength(int compressionMethodLength) {
        this.compressionMethodLength = compressionMethodLength;
    }

    public byte[] getCompressionMethod() {
        return compressionMethod;
    }

    public void setCompressionMethod(byte[] compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    public int getExtensionLength() {
        return extensionLength;
    }

    public void setExtensionLength(int extensionLength) {
        this.extensionLength = extensionLength;
    }

    public byte[] getExtensiond() {
        return extensiond;
    }

    public void setExtensiond(byte[] extensiond) {
        this.extensiond = extensiond;
    }
}
