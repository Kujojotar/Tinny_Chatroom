package com.james.chat.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;

public class MyHttpContent implements MyHttpObject {
    private final ByteBuf content;
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    public MyHttpContent(ByteBuf content) {
        if (content == null) {
            throw new NullPointerException("content");
        }
        this.content = content;
    }

    public ByteBuf content() {
        return content;
    }

    public DecoderResult getDecoderResult() {
        return decoderResult;
    }

    public void setDecoderResult(DecoderResult decoderResult) {
        if (decoderResult == null) {
            throw new NullPointerException("decoderResult");
        }
        this.decoderResult = decoderResult;
    }
}
