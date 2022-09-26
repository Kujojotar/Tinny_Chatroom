package com.james.chat.codec.websocket.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PongWebSocketFrame extends WebSocketFrame {

    public PongWebSocketFrame() {
        super(true, 0, Unpooled.buffer(0));
    }

    public PongWebSocketFrame(ByteBuf binaryData) {
        super(binaryData);
    }

    public PongWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(finalFragment, rsv, binaryData);
    }

    @Override
    public PongWebSocketFrame copy() {
        return new PongWebSocketFrame(isFinalFragment(), rsv(), content().copy());
    }

    @Override
    public PongWebSocketFrame duplicate() {
        return new PongWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
    }

    @Override
    public PongWebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public PongWebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }
}
