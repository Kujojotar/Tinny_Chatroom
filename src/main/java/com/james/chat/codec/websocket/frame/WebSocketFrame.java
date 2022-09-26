package com.james.chat.codec.websocket.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

public abstract class WebSocketFrame extends DefaultByteBufHolder {

    private final boolean finalFragment;
    private final int rsv;

    protected WebSocketFrame(ByteBuf binaryData) {
        this(true, 0, binaryData);
    }

    protected WebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(binaryData);
        this.finalFragment = finalFragment;
        this.rsv = rsv;
    }

    public boolean isFinalFragment() {
        return finalFragment;
    }

    public int rsv() {
        return rsv;
    }

    @Override
    public abstract WebSocketFrame copy();

    @Override
    public abstract WebSocketFrame duplicate();

    @Override
    public WebSocketFrame retain() {
        super.retain();
        return this;
    }

    public WebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }
}
