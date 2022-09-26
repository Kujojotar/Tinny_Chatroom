package com.james.chat.handler;

import com.james.chat.codec.http.*;
import com.james.chat.codec.websocket.MyWebSocketAggregator;
import com.james.chat.codec.websocket.MyWebSocketFrameDecoder;
import com.james.chat.codec.websocket.MyWebSocketFrameEncoder;
import com.james.chat.codec.websocket.frame.WebSocketFrame;
import com.james.chat.controller.MessageTransferController;
import com.james.chat.redis.RedisService;
import com.james.chat.redis.RedisUserInfo;
import com.james.chat.util.ApplicationChannelTracer;
import com.james.chat.util.JacksonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

/**
 * 进行WebSocket握手，并且在握手后将Http服务的相关信息替换为
 */
public class HttpUpgradeHandler extends ChannelInboundHandlerAdapter {
    private static final String APPEND_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private final RedisService redisService;
    private final MessageTransferController messageTransferController;

    public HttpUpgradeHandler(RedisService redisService, MessageTransferController messageTransferController) {
        this.redisService = redisService;
        this.messageTransferController = messageTransferController;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MyFullHttpRequest) {
            MyFullHttpRequest request = (MyFullHttpRequest) msg;
            if (request.getMethod() != null && "OPTIONS".equalsIgnoreCase(request.getMethod().toString())) {
                MyHttpResponse response = new MyHttpResponse(request.getVersion(), MyHttpResponseStatus.OK);
                MyHttpHeaders headersToAdd = response.getHeaders();
                headersToAdd.set("Access-Control-Allow-Headers", "Authorization,Content-Type,token");
                headersToAdd.set("Access-Control-Allow-Methods", "GET,POST,DELETE,UPDATE");
                headersToAdd.set("Access-Control-Allow-Origin", "*");
                headersToAdd.set("Access-Allow-Control-Credentials","*");
                headersToAdd.set("Access-Allow-Max-Age","36000");
                int size = response.getVersion().toString().length() + response.getStatus().toString().length();
                Iterator<Map.Entry<String,String>> iterator = response.getHeaders().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    size += entry.getKey().length() + 3 + entry.getValue().length();
                }
                size += 2;
                ByteBuf buffer = ctx.alloc().buffer(size);
                buffer.writeBytes((response.getVersion().toString() + " ").getBytes());
                buffer.writeBytes(response.getStatus().toString().getBytes());
                buffer.writeBytes("\r\n".getBytes());
                headersToAdd.forEach(x->buffer.writeBytes((x.getKey() + ":" + x.getValue() + "\r\n").getBytes()));
                buffer.writeBytes("\r\n".getBytes());
                ctx.writeAndFlush(buffer);
                return;
            }
            if (containsWebSocketUpgradeHeader(request)) {
                if (!validate(request)) {
                    return;
                }
                boolean res = handleHandShaking(ctx, request);
                if (res) {
                    ctx.pipeline().replace(MyHttpObjectDecoder.class, "WebSocket-Decoder", new MyWebSocketFrameDecoder(140000));
                    ctx.pipeline().remove(MyHttpObjectAggregator.class);
                    ctx.pipeline().addAfter("WebSocket-Decoder", "WebSocket-Aggregator", new MyWebSocketAggregator(1<<30, 30000));
                    ctx.pipeline().addAfter("WebSocket-Aggregator",  "WebSocket-Encoder",new MyWebSocketFrameEncoder());
                    System.out.println();
                }
                /*
                String userInfo = redisService.get(request.getHeaders().get("sid"));
                RedisUserInfo info = JacksonUtil.getObject(userInfo,RedisUserInfo.class);
                ApplicationChannelTracer.addChannel((NioSocketChannel) ctx.channel(), info.getUsername());
                redisService.addZSetMember(info.getUsername());
                 */
                String userInfo = redisService.get(request.getHeaders().get("sid"));
                RedisUserInfo info = JacksonUtil.getObject(userInfo,RedisUserInfo.class);
                ApplicationChannelTracer.addChannel((NioSocketChannel) ctx.channel(), info.getUsername());
                return ;
            }
        }
        if (msg instanceof WebSocketFrame) {
            System.out.println(msg.getClass().getName());
            // TODO: 将报文信息交给线程池，分派到业务处理线程中进行业务
            messageTransferController.handleWebSocketFrame(ctx, (NioSocketChannel) ctx.channel(), (WebSocketFrame) msg);
            ((WebSocketFrame) msg).release();
            System.out.println(((WebSocketFrame) msg).refCnt());
        }
    }

    private boolean validate(MyFullHttpRequest request) {
        String cookieStr = request.getHeaders().get("Cookie");
        if (cookieStr == null) {
            return false;
        }
        String key = cookieStr.substring(0, cookieStr.indexOf("="));
        String value = cookieStr.substring(cookieStr.indexOf("=") + 1, cookieStr.length());
        if ("Authorization".equalsIgnoreCase(key)) {
            request.getHeaders().set("sid", value);
            return true;
        }
        return redisService.exists(request.getHeaders().get("sid"));
    }

    private boolean handleHandShaking(ChannelHandlerContext ctx ,MyFullHttpRequest request) {
        MyHttpResponse response = new MyHttpResponse(request.getVersion(), MyHttpResponseStatus.SWITCHING_PROTOCOLS);
        MyHttpHeaders headersToAdd = response.getHeaders();
        headersToAdd.add(HttpHeaderCommons.Names.CONNECTION, HttpHeaderCommons.Values.UPGRADE);
        headersToAdd.add(HttpHeaderCommons.Names.UPGRADE, HttpHeaderCommons.Values.WEBSOCKET);
        String key = null;
        try {
            key = generateKey(request.getHeaders().get(HttpHeaderCommons.Names.SEC_WEBSOCKET_KEY) + APPEND_STRING);
        } catch (Exception e) {
            return false;
        }
        headersToAdd.add(HttpHeaderCommons.Names.SEC_WEBSOCKET_ACCEPT, key);
        headersToAdd.add(HttpHeaderCommons.Names.SEC_WEBSOCKET_VERSION, 13);
        int size = response.getVersion().toString().length() + response.getStatus().toString().length();
        Iterator<Map.Entry<String,String>> iterator = response.getHeaders().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            size += entry.getKey().length() + 3 + entry.getValue().length();
        }
        size += 2;
        ByteBuf buffer = ctx.alloc().buffer(size);
        buffer.writeBytes((response.getVersion().toString() + " ").getBytes());
        buffer.writeBytes(response.getStatus().toString().getBytes());
        buffer.writeBytes("\r\n".getBytes());
        headersToAdd.forEach(x->buffer.writeBytes((x.getKey() + ":" + x.getValue() + "\r\n").getBytes()));
        buffer.writeBytes("\r\n".getBytes());
        ctx.writeAndFlush(buffer);
        return true;
    }

    public static String generateKey(String s) throws Exception{
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        String key = Base64.getEncoder().encodeToString(digest.digest(s.getBytes()));
        return key;
    }

    private boolean containsWebSocketUpgradeHeader(MyFullHttpRequest request) {
        MyHttpHeaders headers = request.getHeaders();
        return headers != null && "UPGRADE".equalsIgnoreCase(headers.get(HttpHeaderCommons.Names.CONNECTION))
                               && "WebSocket".equalsIgnoreCase(headers.get(HttpHeaderCommons.Names.UPGRADE))
                               && MyHttpMethod.GET.equals(request.getMethod());
    }
}
