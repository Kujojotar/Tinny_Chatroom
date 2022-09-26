package com.james.chat.codec.http;

public class MyHttpRequest extends MyHttpMessage {

    private MyHttpMethod method;
    private String uri;

    public MyHttpRequest(MyHttpVersion httpVersion, MyHttpMethod method, String uri, boolean validateHeaders) {
        super(httpVersion);
        if (method == null) {
            throw new NullPointerException("method");
        }
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.method = method;
        this.uri = uri;
    }

    public MyHttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public MyHttpRequest setMethod(MyHttpMethod method) {
        if (method == null) {
            throw new NullPointerException("method");
        }
        this.method = method;
        return this;
    }

    public MyHttpRequest setUri(String uri) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.uri = uri;
        return this;
    }

    public void setHttpVersion(MyHttpVersion version) {
        super.setVersion(version);
    }
}
