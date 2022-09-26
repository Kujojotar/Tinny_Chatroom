package com.james.chat.codec.ssl.handshake;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ServerHelloDone extends TlsHeader{
    private final int type = 14;
    private final int length = 0;

    public ByteBuf getBuffer(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer(9);
        buffer.writeByte(this.getContentType());
        buffer.writeByte(this.getVersion().getMajor());
        buffer.writeByte(this.getVersion().getMinor());
        buffer.writeShort(this.getTotalLength());
        buffer.writeByte(type);
        buffer.writeMedium(length);
        return buffer;
    }
}
