package com.james.chat.codec.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.unix.Buffer;

import java.nio.ByteBuffer;

public class TlsServerHello {
    private int content;
    private TlsProtocolVersion version;
    private int totalLength;
    private int handshakeType;
    private int length;
    private long time;
    private byte[] random;
    private int sessionIdLength;
    private byte[] sessionId;
    private CipherSuite cipherSuite;
    private int compressLength;
    private byte[] compress;
    private int extensionLength;
    private byte[] extensions;

    public int getContent() {
        return content;
    }

    public void setContent(int content) {
        this.content = content;
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

    public int getHandshakeType() {
        return handshakeType;
    }

    public void setHandshakeType(int handshakeType) {
        this.handshakeType = handshakeType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    public void setCipherSuite(CipherSuite cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    public int getCompressLength() {
        return compressLength;
    }

    public void setCompressLength(int compressLength) {
        this.compressLength = compressLength;
    }

    public byte[] getCompress() {
        return compress;
    }

    public void setCompress(byte[] compress) {
        this.compress = compress;
    }

    public int getExtensionLength() {
        return extensionLength;
    }

    public void setExtensionLength(int extensionLength) {
        this.extensionLength = extensionLength;
    }

    public byte[] getExtensions() {
        return extensions;
    }

    public void setExtensions(byte[] extensions) {
        this.extensions = extensions;
    }

    public ByteBuf getByteBuffer(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer(43+1+sessionIdLength+2+1+compressLength+2+extensionLength);
        buffer.writeByte(content);
        buffer.writeByte(version.getMajor());
        buffer.writeByte(version.getMinor());
        buffer.writeShort(totalLength);
        buffer.writeByte(handshakeType);
        buffer.writeMedium(length);
        buffer.writeByte(version.getMajor());
        buffer.writeByte(version.getMinor());
        buffer.writeLong(time);
        buffer.writeBytes(random);
        buffer.writeByte(sessionIdLength);
        if (sessionIdLength > 0) {
            buffer.writeBytes(sessionId);
        }
        buffer.writeByte(cipherSuite.getB1());
        buffer.writeByte(cipherSuite.getB2());
        buffer.writeByte(compressLength);
        if (compressLength > 0) {
            buffer.writeBytes(compress);
        }
        buffer.writeShort(extensionLength);
        if (extensionLength > 0) {
            buffer.writeBytes(extensions);
        }
        return buffer;
    }
}

