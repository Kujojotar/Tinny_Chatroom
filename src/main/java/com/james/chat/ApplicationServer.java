package com.james.chat;

import com.james.chat.codec.http.HttpRequestDecoder;
import com.james.chat.codec.http.MyHttpObjectAggregator;
import com.james.chat.controller.MessageTransferController;
import com.james.chat.handler.HttpUpgradeHandler;
import com.james.chat.redis.RedisService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Component
public class ApplicationServer {
    private final RedisService redisService;
    private final MessageTransferController messageTransferController;

    private int port = 8088;
    public ApplicationServer(RedisService redisService, MessageTransferController messageTransferController) {
        this.redisService = redisService;
        this.messageTransferController = messageTransferController;
    }

    @PostConstruct
    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ApplicationServer.this.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void run() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                     .channel(NioServerSocketChannel.class)
                     .childHandler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         protected void initChannel(SocketChannel socketChannel) throws Exception {
                             SSLEngine sslEngine = prepareEngine();
                             ChannelPipeline pipeline = socketChannel.pipeline();
                             pipeline.addLast(new HttpRequestDecoder());
                             pipeline.addLast(new MyHttpObjectAggregator(4096));
                             pipeline.addLast(new HttpUpgradeHandler(redisService, messageTransferController));
                         }
                     });
            Channel ch = bootstrap.bind(port).sync().channel();
            System.out.println("Web socket server started at port " + port);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private SSLEngine prepareEngine() throws Exception{
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(createKeyManagers("C:\\data\\baojian.jks", "baojian", "baojian"),null,new SecureRandom());
        SSLSession dummySession = sslContext.createSSLEngine().getSession();
        dummySession.invalidate();
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        sslEngine.setWantClientAuth(false);
        return sslEngine;
    }

    protected KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream keyStoreIS = new FileInputStream(filepath);
        try {
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
        } finally {
            if (keyStoreIS != null) {
                keyStoreIS.close();
            }
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }
}
