package com.james.chat.util;

import com.james.chat.codec.websocket.frame.PingWebSocketFrame;
import com.james.chat.redis.RedisService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationChannelTracer {
    private static final int CHANNELS_EXPECTED_SIZE = 1000;

    private static RedisService redisService;
    private static final ConcurrentHashMap<String, NioSocketChannel> activeChannels = new ConcurrentHashMap<>(CHANNELS_EXPECTED_SIZE);

    static {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, NioSocketChannel>> iterators = activeChannels.entrySet().iterator();
                while (iterators.hasNext()) {
                    Map.Entry<String, NioSocketChannel> entry = iterators.next();
                    if (!entry.getValue().isShutdown()) {
                        PingWebSocketFrame pingFrame = new PingWebSocketFrame();
                        entry.getValue().writeAndFlush(pingFrame);
                    } else {
                        iterators.remove();
                    }
                }
            }
        }, 2*10*1000L);
    }

    public static void addChannel(NioSocketChannel channel, String userId) {
        activeChannels.put(userId, channel);
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                activeChannels.remove(userId);
            }
        });
    }

    public static boolean writeToNioSocketChannelIfPresent(Object e, String userId) {
        NioSocketChannel targetChannel = activeChannels.getOrDefault(userId, null);
        if (targetChannel == null) {
            return false;
        }
        if (targetChannel.isShutdown()) {
            activeChannels.remove(targetChannel);
        }
        targetChannel.writeAndFlush(e);
        return true;
    }

    public static void tryRemoveFromSet(String userId) {
        if (redisService != null) {
            redisService.removeFromSet(userId);
        }
    }

    public static void setRedisService(RedisService redisService) {
        ApplicationChannelTracer.redisService = redisService;
    }

    public static boolean isUserOnline(String userId) {
        return activeChannels.containsKey(userId);
    }

}
