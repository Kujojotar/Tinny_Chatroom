package com.james.chat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            handleHttpRequest((HttpRequest)msg);
        }
        if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame((WebSocketFrame) msg);
        }
    }

    private void handleWebSocketFrame(WebSocketFrame msg) {

    }

    private void handleHttpRequest(HttpRequest msg) {

    }
}
