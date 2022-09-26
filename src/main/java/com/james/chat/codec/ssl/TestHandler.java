package com.james.chat.codec.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            byte[] arr = new byte[buf.readableBytes()];
            buf.readBytes(arr);
            System.out.println(new String(arr));
            ByteBuf buf2 = Unpooled.copiedBuffer("HTTP/1.1 200 OK\r\nContent-Type:UTF-8\r\nContent-Length:16\r\n\r\nHello SSLEngine!".getBytes());
            ctx.writeAndFlush(buf2);
        }
    }
}
