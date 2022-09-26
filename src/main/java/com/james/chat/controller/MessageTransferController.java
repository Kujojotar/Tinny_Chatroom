package com.james.chat.controller;

import com.james.chat.codec.websocket.frame.*;
import com.james.chat.dao.GroupMessageMapper;
import com.james.chat.dao.StorageMessageMapper;
import com.james.chat.entity.ApplicationTextPacket;
import com.james.chat.entity.StorageMessage;
import com.james.chat.handler.FileTransferHandler;
import com.james.chat.redis.RedisService;
import com.james.chat.util.ApplicationChannelTracer;
import com.james.chat.util.JacksonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class MessageTransferController {

    private final RedisService redisService;
    private static final int MAX_TEXT_CONTENT_LENGTH = 4096;

    private final GroupMessageMapper groupMessageMapper;
    private final StorageMessageMapper storageMessageMapper;

    public MessageTransferController(RedisService redisService, GroupMessageMapper groupMessageMapper, StorageMessageMapper storageMessageMapper) {
        this.redisService = redisService;
        this.groupMessageMapper = groupMessageMapper;
        this.storageMessageMapper = storageMessageMapper;
    }


    public void handleWebSocketFrame(ChannelHandlerContext ctx ,NioSocketChannel sourceChannel, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            handleTextWebSocketFrame(ctx, (TextWebSocketFrame) frame, sourceChannel);
        } else if (frame instanceof PingWebSocketFrame) {
            handlePingWebSocketFrame((PingWebSocketFrame) frame, sourceChannel);
        } else if (frame instanceof CloseWebSocketFrame) {
            sourceChannel.writeAndFlush(new CloseWebSocketFrame());
            sourceChannel.close();
        } else if (frame instanceof BinaryWebSocketFrame) {
            System.out.println(frame.isFinalFragment());
            System.out.println(frame.content().readableBytes());
        } else if (frame instanceof ContinueWebSocketFrame) {
            System.out.println("continue frame");
            System.out.println(frame.isFinalFragment());
            System.out.println(frame.content().readableBytes());
        }
    }

    private void handleTextWebSocketFrame(ChannelHandlerContext ctx, TextWebSocketFrame textFrame, NioSocketChannel channel) {
        int contentSize = textFrame.content().readableBytes();
        if (contentSize > MAX_TEXT_CONTENT_LENGTH) {
            TextWebSocketFrame rejectFrame = new TextWebSocketFrame(Unpooled.copiedBuffer("报文内容过长，请尝试进行分片发送!".getBytes(StandardCharsets.UTF_8)));
            channel.writeAndFlush(channel);
            return ;
        }
        byte[] content = new byte[contentSize];
        textFrame.content().readBytes(content);
        ApplicationTextPacket packet = JacksonUtil.getObject(content, ApplicationTextPacket.class);
        if (packet == null) {
            TextWebSocketFrame rejectFrame = new TextWebSocketFrame(Unpooled.copiedBuffer("报文内容格式错误!".getBytes(StandardCharsets.UTF_8)));
            channel.writeAndFlush(channel);
            return ;
        }
        if (packet.getFrameType() == ApplicationTextPacket.PacketType.TRANSFER_FILE) {
            ctx.pipeline().addAfter("WebSocket-Encoder","file-receive-handler", new FileTransferHandler(packet.getTextContent(),channel, storageMessageMapper));
            return ;
        }
        if (!packet.isGroupMessage()) {
            if (!packet.isAck()) {
                handleOneToOneMessage(packet, channel, content);
            } else {
                handleOneToOneAckMessage(packet, channel, content);
            }
        } else if (packet.isGroupMessage()) {

        }
        if (packet.isGroupMessage()) {
            if (!packet.isAck()) {
                handleGroupMessage(packet, channel, content);
            } else {

            }
        }
    }

    private void handleOneToOneMessage(ApplicationTextPacket packet, NioSocketChannel channel, byte[] content) {
        String toUserId = packet.getToUserId();
        TextWebSocketFrame sendFrame = new TextWebSocketFrame(Unpooled.copiedBuffer(content));
        if (ApplicationChannelTracer.isUserOnline(toUserId)) {
            ApplicationChannelTracer.writeToNioSocketChannelIfPresent(sendFrame, toUserId);
        } else {
            // Store in db
            Long msgId = storageMessageMapper.getTmpMessageId(packet.getFromUserId(), packet.getToUserId());
            if (msgId == null) {
                msgId = 0L;
            } else {
                msgId++;
            }
            StorageMessage storageMessage = new StorageMessage();
            storageMessage.setFromusername(packet.getFromUserId());
            storageMessage.setTousername(toUserId);
            storageMessage.setMsgid(msgId);
            storageMessage.setPublishdate(new Date());
            storageMessage.setText(packet.getTextContent());
            storageMessageMapper.insert(storageMessage);
        }
    }

    private void handleOneToOneAckMessage(ApplicationTextPacket packet, NioSocketChannel channel, byte[] content) {
        String messageId = packet.getMessageId();
        handleOneToOneMessage(packet, channel, content);
    }

    private void handleGroupMessage(ApplicationTextPacket packet, NioSocketChannel channel, byte[] content) {
        String fromUserId = packet.getFromUserId();
        String toUserId = packet.getToUserId();
        List<String> users = groupMessageMapper.getGroupUsernames(toUserId);
        users.forEach(x->{
            if (ApplicationChannelTracer.isUserOnline(x) && !fromUserId.equals(x)) {
                TextWebSocketFrame sendFrame = new TextWebSocketFrame(Unpooled.copiedBuffer(content));
                ApplicationChannelTracer.writeToNioSocketChannelIfPresent(sendFrame, x);
            }
        });
    }

    private void handleGroupAckMessage(ApplicationTextPacket packet, NioSocketChannel channel, byte[] content) {

    }

    private String getTrueUserId(String sid) {
        return sid;
    }

    private void handlePingWebSocketFrame(PingWebSocketFrame pingFrame, NioSocketChannel channel) {
        WebSocketFrame pongFrame = new PongWebSocketFrame(pingFrame.content());
        channel.writeAndFlush(pongFrame);
    }

}
