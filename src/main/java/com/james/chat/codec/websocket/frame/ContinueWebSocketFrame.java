package com.james.chat.codec.websocket.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class ContinueWebSocketFrame extends WebSocketFrame{

    public ContinueWebSocketFrame() {
        this(Unpooled.buffer(0));
    }

    public ContinueWebSocketFrame(ByteBuf binaryData) {
        super(binaryData);
    }

    public ContinueWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(finalFragment, rsv, binaryData);
    }

    public ContinueWebSocketFrame(boolean finalFragment, int rsv, String text) {
        this(finalFragment, rsv, fromText(text));
    }

    public String text() {
        return content().toString(CharsetUtil.UTF_8);
    }

    private static ByteBuf fromText(String text) {
        if (text == null || text.isEmpty()) {
            return Unpooled.EMPTY_BUFFER;
        } else {
            return Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
        }
    }

    @Override
    public ContinueWebSocketFrame copy() {
        return new ContinueWebSocketFrame(isFinalFragment(), rsv(), content().copy());
    }

    @Override
    public ContinueWebSocketFrame duplicate() {
        return new ContinueWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
    }

    @Override
    public ContinueWebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public ContinueWebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }
}
