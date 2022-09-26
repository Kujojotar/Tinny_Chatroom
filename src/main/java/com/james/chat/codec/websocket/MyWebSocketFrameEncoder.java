package com.james.chat.codec.websocket;

import com.james.chat.codec.websocket.frame.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class MyWebSocketFrameEncoder extends MessageToMessageEncoder<WebSocketFrame> {

    private static final byte OPCODE_CONT = 0x0;
    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;
    private static final byte OPCODE_CLOSE = 0x8;
    private static final byte OPCODE_PING = 0x9;
    private static final byte OPCODE_PONG = 0xA;

    private static final int FRAGMENT_THRESHOLD = 1024;
    byte[] partialContent = new byte[FRAGMENT_THRESHOLD];

    @Override
    protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> list) throws Exception {
        byte opcode = 0;
        if (msg instanceof TextWebSocketFrame) {
            opcode |= OPCODE_TEXT;
        } else if (msg instanceof BinaryWebSocketFrame) {
            opcode |= OPCODE_BINARY;
        } else if (msg instanceof ContinueWebSocketFrame) {
            opcode = OPCODE_CONT;
            list.add(getShortWebsocketBuf(ctx, opcode));
            return ;
        } else if (msg instanceof CloseWebSocketFrame) {
            opcode = OPCODE_CLOSE;
            list.add(getShortWebsocketBuf(ctx, opcode));
            return ;
        } else if (msg instanceof  PingWebSocketFrame) {
            opcode = OPCODE_PING;
            list.add(getShortWebsocketBuf(ctx, opcode));
            return ;
        } else if (msg instanceof  PongWebSocketFrame) {
            opcode = OPCODE_PONG;
            list.add(getShortWebsocketBuf(ctx, opcode));
            return ;
        } else {
            throw new UnsupportedOperationException("Cannot encode frame of type: " + msg.getClass().getName());
        }
        ByteBuf content = msg.content();
        int totalSize = content.readableBytes();
        int cum = 0;
        if (totalSize < FRAGMENT_THRESHOLD) {
            if (totalSize < 126) {
                ByteBuf buf = ctx.alloc().buffer(2 + totalSize);
                buf.writeByte(0x80+opcode);
                buf.writeByte(totalSize);
                buf.writeBytes(content, cum * FRAGMENT_THRESHOLD, cum * FRAGMENT_THRESHOLD + totalSize);
                list.add(buf);
            } else {
                ByteBuf buf = ctx.alloc().buffer(4 + totalSize);
                buf.writeByte(0x80+opcode);
                buf.writeByte(126);
                buf.writeShort(totalSize);
                buf.writeBytes(content, cum * FRAGMENT_THRESHOLD, cum * FRAGMENT_THRESHOLD + totalSize);
                list.add(buf);
            }
            return ;
        }
        list.add(getShortWebsocketBuf(ctx, opcode));
        while (totalSize > FRAGMENT_THRESHOLD) {
            ByteBuf buf = ctx.alloc().buffer(FRAGMENT_THRESHOLD + 4);
            buf.writeByte(0);
            buf.writeByte(126);
            buf.writeShort(FRAGMENT_THRESHOLD);
            content.readBytes(partialContent);
            buf.writeBytes(partialContent);
            totalSize -= FRAGMENT_THRESHOLD;
            list.add(buf);
        }
        if (totalSize < 126) {
            ByteBuf buf = ctx.alloc().buffer(2 + totalSize);
            buf.writeByte(0x80);
            buf.writeByte(totalSize);
            content.readBytes(partialContent,0,totalSize);
            buf.writeBytes(partialContent, 0, totalSize);
            list.add(buf);
        } else {
            ByteBuf buf = ctx.alloc().buffer(4 + totalSize);
            buf.writeByte(0x80);
            buf.writeByte(126);
            buf.writeShort(totalSize);
            content.readBytes(partialContent, 0, totalSize);
            buf.writeBytes(partialContent, 0, totalSize);
            list.add(buf);
        }
    }

    public ByteBuf getShortWebsocketBuf(ChannelHandlerContext ctx, byte opcode) {
        ByteBuf out = ctx.alloc().buffer(2);
        out.writeByte(opcode);
        out.writeByte(0);
        return out;
    }
}
