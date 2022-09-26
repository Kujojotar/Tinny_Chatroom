package com.james.chat.codec.http;

public class HttpRequestDecoder extends MyHttpObjectDecoder {

    public HttpRequestDecoder() {
        super(4096, 8192, 8192, 128);
    }

    public HttpRequestDecoder(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize, 128);
    }

    public HttpRequestDecoder(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, int initialBufferSize) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize, initialBufferSize);
    }

    @Override
    protected MyHttpMessage createMessage(String[] strings) throws Exception {
        return new MyHttpRequest(MyHttpVersion.valueOf(strings[2]), MyHttpMethod.valueOf(strings[0]), strings[1], true);
    }

    @Override
    protected MyHttpMessage createInvalidMessage() {
        return new MyHttpRequest(MyHttpVersion.HTTP_1_0, MyHttpMethod.GET, "/bad-request", false);
    }

    @Override
    protected boolean isDecodingRequest() {
        return true;
    }
}
