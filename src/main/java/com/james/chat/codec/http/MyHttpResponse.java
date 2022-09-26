package com.james.chat.codec.http;

public class MyHttpResponse extends MyHttpMessage{
    private MyHttpResponseStatus status;

    public MyHttpResponse(MyHttpVersion version, MyHttpResponseStatus status) {
        if (status == null) {
            throw new NullPointerException("status");
        }
        this.status = status;
    }

    public MyHttpResponseStatus getStatus() {
        return status;
    }

    public MyHttpResponse setStatus(MyHttpResponseStatus status) {
        if (status == null) {
            throw new NullPointerException("status");
        }
        this.status = status;
        return this;
    }



}
