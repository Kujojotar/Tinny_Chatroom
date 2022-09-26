package com.james.chat.codec.websocket.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PingWebSocketFrame extends WebSocketFrame {

    public PingWebSocketFrame() {
        super(true, 0, Unpooled.buffer(0));
    }

    public PingWebSocketFrame(ByteBuf binaryData) {
        super(binaryData);
    }

    public PingWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(finalFragment, rsv, binaryData);
    }

    @Override
    public PingWebSocketFrame copy() {
        return new PingWebSocketFrame(isFinalFragment(), rsv(), content().copy());
    }

    @Override
    public PingWebSocketFrame duplicate() {
        return new PingWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
    }

    @Override
    public PingWebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public PingWebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }
}
