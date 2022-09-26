package com.james.chat.handler;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SslHandler extends ChannelInboundHandlerAdapter {

    private SSLEngine sslEngine;

    private boolean handshakeBegin;
    private boolean handshakeEnd;

    private ByteBuffer myAppData;
    private ByteBuffer myNetData;
    private ByteBuffer peerAppData;
    private ByteBuffer peerNetData;
    private boolean firstStarted;

    private Executor executor = Executors.newCachedThreadPool();

    private Object sign = new Object();

    private class OutWrapped extends ChannelOutboundHandlerAdapter {

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (handshakeEnd && msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                peerNetData.clear();
                sslEngine.wrap(buf.nioBuffer(), peerNetData);
                peerNetData.flip();
                ctx.writeAndFlush(Unpooled.wrappedBuffer(peerNetData)).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        promise.setSuccess();
                    }
                });
            } else {
                ctx.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        promise.setSuccess();
                    }
                });
            }
        }



    }

    public ChannelOutboundHandler getOutBoundHandler() {
        return new OutWrapped();
    }

    public SslHandler() throws Exception{
        this.sslEngine = prepareEngine();
    }

    private SSLEngine prepareEngine() throws Exception{
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(createKeyManagers("C:\\data\\baojian.crt", "baojian", "baojian"),null,new SecureRandom());
        SSLSession dummySession = sslContext.createSSLEngine().getSession();
        dummySession.invalidate();
        SSLEngine sslEngine = sslContext.createSSLEngine();
        myAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
        peerAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
        peerNetData = ByteBuffer.allocate(10*dummySession.getApplicationBufferSize());
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        sslEngine.setWantClientAuth(false);
        return sslEngine;
    }

    protected KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
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

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            if (!handshakeBegin) {
                handshakeBegin = true;
                sslEngine.beginHandshake();
            }
            if (!handshakeEnd) {
                this.myNetData = buf.nioBuffer();
                doHandshake(ctx);
            } else {
                myAppData.clear();
                sslEngine.unwrap(buf.nioBuffer(), myAppData);
                myAppData.flip();
                ctx.fireChannelRead(Unpooled.wrappedBuffer(myAppData));
            }
        }
    }

    private void doHandshake(ChannelHandlerContext ctx) {
        SSLEngineResult result;
        SSLEngineResult.HandshakeStatus status = sslEngine.getHandshakeStatus();
        while (true) {
            if (status == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                if (!myNetData.hasRemaining()) {
                    return ;
                }
                try {
                    result = sslEngine.unwrap(myNetData, myAppData);
                } catch (SSLException e) {
                    e.printStackTrace();
                } finally {
                    myAppData.clear();
                    status = sslEngine.getHandshakeStatus();
                }
            }
            if (status == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                Runnable task;
                while ((task = sslEngine.getDelegatedTask()) != null) {
                    executor.execute(task);
                }
                status = sslEngine.getHandshakeStatus();
            }
            if (status == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                try {
                    result = sslEngine.wrap(peerAppData, peerNetData);
                } catch (SSLException e) {
                    e.printStackTrace();
                }
                peerNetData.flip();
                byte[] arr = new byte[peerNetData.limit()];
                peerNetData.get(arr);
                ChannelFuture future = ctx.writeAndFlush(Unpooled.wrappedBuffer(arr));
                try {
                    future.sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                peerNetData.clear();
                status = sslEngine.getHandshakeStatus();
            }
            if (status == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                Runnable task;
                while ((task = sslEngine.getDelegatedTask()) != null) {
                    executor.execute(task);
                }
                status = sslEngine.getHandshakeStatus();
            }
            if (status == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                handshakeEnd = true;
                return;
            }
        }
    }
}
