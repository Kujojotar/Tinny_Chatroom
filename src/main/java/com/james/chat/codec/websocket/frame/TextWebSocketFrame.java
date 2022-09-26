package com.james.chat.codec.websocket.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class TextWebSocketFrame extends WebSocketFrame{

    public TextWebSocketFrame() {
        super(Unpooled.buffer(0));
    }

    public TextWebSocketFrame(String text) {
        super(fromText(text));
    }

    public TextWebSocketFrame(ByteBuf binaryData) {
        super(binaryData);
    }

    public TextWebSocketFrame(boolean finalFragment, int rsv, String text) {
        super(finalFragment, rsv, fromText(text));
    }

    private static ByteBuf fromText(String text) {
        if (text == null || text.isEmpty()) {
            return Unpooled.EMPTY_BUFFER;
        } else {
            return Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
        }
    }

    public TextWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(finalFragment, rsv, binaryData);
    }

    public String text() {
        return content().toString(CharsetUtil.UTF_8);
    }

    @Override
    public TextWebSocketFrame copy() {
        return new TextWebSocketFrame(isFinalFragment(), rsv(), content().copy());
    }

    @Override
    public TextWebSocketFrame duplicate() {
        return new TextWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
    }

    @Override
    public TextWebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public TextWebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }

}
