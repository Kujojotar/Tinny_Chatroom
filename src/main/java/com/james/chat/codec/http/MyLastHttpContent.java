package com.james.chat.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;

import java.util.Map;

public class MyLastHttpContent extends MyHttpContent{
    private final MyHttpHeaders trailingHeaders;
    private final boolean validateHeaders;

    public MyLastHttpContent() {
        this(Unpooled.buffer(0));
    }

    public MyLastHttpContent(ByteBuf content) {
        this(content, true);
    }

    public MyLastHttpContent(ByteBuf content, boolean validateHeaders) {
        super(content);
        trailingHeaders = new TrailingHeaders(validateHeaders);
        this.validateHeaders = validateHeaders;
    }

    public MyHttpHeaders trailingHeaders() {
        return trailingHeaders;
    }

    private void appendHeaders(StringBuilder buf) {
        for (Map.Entry<String, String> e: trailingHeaders()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }

    private static final class TrailingHeaders extends MyHttpHeaders {
        TrailingHeaders(boolean validate) {
            super(validate);
        }

        @Override
        protected void validateHeaderName0(CharSequence name) {
            super.validateHeaderName0(name);
            if (equalsIgnoreCase(HttpHeaderCommons.Names.CONTENT_LENGTH, name) ||
                    equalsIgnoreCase(HttpHeaderCommons.Names.TRANSFER_ENCODING, name) ||
                    equalsIgnoreCase(HttpHeaderCommons.Names.TRAILER, name)) {
                throw new IllegalArgumentException(
                        "prohibited trailing header: " + name);
            }
        }
    }
}
