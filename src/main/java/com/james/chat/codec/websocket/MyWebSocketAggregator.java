package com.james.chat.codec.websocket;

import com.james.chat.codec.websocket.frame.*;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MyWebSocketAggregator extends MessageToMessageDecoder<WebSocketFrame> {
    private WebSocketFrame cachedWebSocketFrame;

    private final int maxTextFrameSize;
    private final int maxBinaryFrameSize;

    public MyWebSocketAggregator(int maxBinaryFrameSize, int maxTextFrameSize) {
        this.maxBinaryFrameSize = maxBinaryFrameSize;
        this.maxTextFrameSize = maxTextFrameSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (frame instanceof PingWebSocketFrame || frame instanceof PongWebSocketFrame || frame instanceof CloseWebSocketFrame) {
            out.add(frame.retain());
            return ;
        }
        WebSocketFrame cachedFrame = cachedWebSocketFrame;
        if (((frame instanceof TextWebSocketFrame && !frame.isFinalFragment())
                || (frame instanceof BinaryWebSocketFrame && !frame.isFinalFragment()))
                && cachedFrame != null) {
            throw new UnsupportedOperationException("无法同时分块传输两个及以上的报文");
        }
        if (frame instanceof TextWebSocketFrame && frame.isFinalFragment()) {
            out.add(frame.retain());
            return ;
        }
        if (frame instanceof BinaryWebSocketFrame && frame.isFinalFragment()) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer("{\"final\":\"true\"}".getBytes(StandardCharsets.UTF_8))));
            out.add(frame.retain());
            return ;
        }
        if (frame instanceof ContinueWebSocketFrame) {
            if (cachedFrame == null) {
                throw new RuntimeException("缓冲报文不应该为null");
            } else {
                CompositeByteBuf buffer = (CompositeByteBuf) cachedWebSocketFrame.content();
                int maxSize = cachedWebSocketFrame instanceof TextWebSocketFrame? maxTextFrameSize : maxBinaryFrameSize;
                if (maxSize < frame.content().readableBytes() + buffer.readableBytes()) {
                    throw new Exception("报文长度过长");
                }
                buffer.addComponent(true, frame.content().retain());
                if (frame.isFinalFragment()) {
                    WebSocketFrame resultFrame = cachedWebSocketFrame;
                    cachedWebSocketFrame = null;
                    out.add(resultFrame);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer("{\"final\":\"true\"}".getBytes(StandardCharsets.UTF_8))));
                    return;
                } else {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(("{\"cumulateSize\":\""+frame.content().readableBytes()+"\"}").getBytes(StandardCharsets.UTF_8))));
                }
            }
        } else {
            if (frame instanceof TextWebSocketFrame) {
                CompositeByteBuf buffer = ctx.alloc().compositeBuffer(maxTextFrameSize);
                if (frame.content().readableBytes() > maxTextFrameSize) {
                    throw new Exception("报文长度过长");
                } else {
                    buffer.addComponent(frame.content().retain());
                }
                cachedWebSocketFrame = new TextWebSocketFrame(buffer);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(("{\"cumulateSize\":\""+frame.content().readableBytes()+"\"}").getBytes(StandardCharsets.UTF_8))));
            } else {
                CompositeByteBuf buffer = ctx.alloc().compositeBuffer(maxBinaryFrameSize);
                if (frame.content().readableBytes() > maxBinaryFrameSize) {
                    throw new Exception("报文长度过长");
                } else {
                    buffer.addComponent(true, frame.content().retain());
                }
                cachedWebSocketFrame = new BinaryWebSocketFrame(buffer);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(("{\"cumulateSize\":\""+frame.content().readableBytes()+"\"}").getBytes(StandardCharsets.UTF_8))));
            }
        }
    }
}
