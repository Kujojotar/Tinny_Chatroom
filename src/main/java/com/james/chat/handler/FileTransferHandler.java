package com.james.chat.handler;

import com.james.chat.codec.websocket.frame.BinaryWebSocketFrame;
import com.james.chat.codec.websocket.frame.TextWebSocketFrame;
import com.james.chat.dao.StorageMessageMapper;
import com.james.chat.entity.ApplicationTextPacket;
import com.james.chat.util.JacksonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileTransferHandler extends ChannelInboundHandlerAdapter {

    private final String name;

    private final NioSocketChannel nioSocketChannel;

    private final StorageMessageMapper storageMessageMapper;

    public FileTransferHandler(String name, NioSocketChannel nioSocketChannel, StorageMessageMapper storageMessageMapper) {
        this.name = name;
        this.nioSocketChannel = nioSocketChannel;
        this.storageMessageMapper = storageMessageMapper;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
            System.out.println(frame.content().readableBytes());
            if (nioSocketChannel != null && !nioSocketChannel.isShutdown()) {
                Path path = Paths.get("C:\\data\\"+name);
                File file = new File("C:\\data\\"+name);
                if (!file.exists()) {
                    file.createNewFile();
                }
                AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
                frame.content().nioBuffer().flip();
                fileChannel.write(frame.content().nioBuffer(), 0, frame.content().nioBuffer(), new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        System.out.println("success");
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {

                    }
                });
            } else {

            }
            ctx.channel().pipeline().remove(this);
        } else {
            ctx.channel().pipeline().remove(this);
            ctx.fireChannelRead(msg);
        }
    }


}
