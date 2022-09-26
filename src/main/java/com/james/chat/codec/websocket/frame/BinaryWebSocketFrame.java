package com.james.chat.codec.websocket.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BinaryWebSocketFrame extends WebSocketFrame{

    public BinaryWebSocketFrame() {
        super(Unpooled.buffer(0));
    }

    public BinaryWebSocketFrame(ByteBuf binaryData) {
        super(binaryData);
    }

    public BinaryWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(finalFragment, rsv, binaryData);
    }

    @Override
    public BinaryWebSocketFrame copy() {
        return new BinaryWebSocketFrame(isFinalFragment(), rsv(), content().copy());
    }

    @Override
    public BinaryWebSocketFrame duplicate() {
        return new BinaryWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
    }

    @Override
    public BinaryWebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public BinaryWebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }
}
