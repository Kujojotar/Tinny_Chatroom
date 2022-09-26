package com.james.chat.codec.ssl.handshake;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CertificateFrame extends TlsHeader {
    private final int type = 11;
    private int length ;
    private int certificatesLength;

    private int[] certificatesLengthArr;
    private byte[] certificates;

    public void setLength(int length) {
        this.length = length;
    }

    public void setCertificatesLength(int certificatesLength) {
        this.certificatesLength = certificatesLength;
    }

    public void setCertificatesLengthArr(int[] certificatesLengthArr) {
        this.certificatesLengthArr = certificatesLengthArr;
    }

    public void setCertificates(byte[] certificates) {
        this.certificates = certificates;
    }

    public ByteBuf getBuffer(ChannelHandlerContext ctx) {
        int sum = 0;
        for (int elem: certificatesLengthArr) {
            sum+=elem;
        }
        ByteBuf buffer = ctx.alloc().buffer(12+3*certificatesLengthArr.length+sum);
        buffer.writeByte(this.getContentType());
        buffer.writeByte(this.getVersion().getMajor());
        buffer.writeByte(this.getVersion().getMinor());
        buffer.writeShort(this.getTotalLength());
        buffer.writeByte(type);
        buffer.writeMedium(length);
        certificatesLength = length-3;
        buffer.writeMedium(certificatesLength);
        int cum = 0;
        if (certificatesLength > 0) {
            for (int i=0;i<certificatesLengthArr.length;i++) {
                buffer.writeMedium(certificatesLengthArr[i]);
                buffer.writeBytes(certificates,cum,cum+certificatesLengthArr[i]);
            }
        }
        return buffer;
    }

    public int getLength() {
        return length;
    }
}
