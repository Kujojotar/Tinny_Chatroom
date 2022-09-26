package com.james.chat.codec.http;

import io.netty.buffer.ByteBuf;

public interface MyFullHttpRequest {

    void setProtocolVersion(MyHttpVersion version);

    void setMethod(MyHttpMethod method);

    void setUri(String uri);


    MyHttpMethod getMethod();

    String getUri();

    MyHttpHeaders getHeaders();

    ByteBuf getContent();

    MyHttpVersion getVersion();
}
