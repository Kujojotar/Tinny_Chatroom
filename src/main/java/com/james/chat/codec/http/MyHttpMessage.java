package com.james.chat.codec.http;

import io.netty.handler.codec.DecoderResult;

public class MyHttpMessage implements MyHttpObject {

    private static final int HASH_CODE_PRIME = 31;
    private MyHttpVersion version;
    private MyHttpHeaders myHttpHeaders;
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    public MyHttpMessage() {
        this.version = MyHttpVersion.HTTP_1_1;
        this.myHttpHeaders = new MyHttpHeaders();
    }

    public MyHttpMessage(MyHttpVersion httpVersion) {
        this.version = httpVersion;
        this.myHttpHeaders = new MyHttpHeaders();
    }

    public MyHttpHeaders getHeaders() {
        return myHttpHeaders;
    }

    public DecoderResult getDecoderResult() {
        return this.decoderResult;
    }

    public void setDecoderResult(DecoderResult decoderResult) {
        if (decoderResult == null) {
            throw new NullPointerException("decoderResult");
        }
        this.decoderResult = decoderResult;
    }

    public MyHttpVersion getVersion() {
        return version;
    }

    public void setVersion(MyHttpVersion version) {
        this.version = version;
    }


}
