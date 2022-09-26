package com.james.chat.codec.websocket;

import com.james.chat.codec.websocket.frame.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.nio.ByteOrder;
import java.util.List;

public class MyWebSocketFrameDecoder extends ReplayingDecoder<MyWebSocketFrameDecoder.State> {
     //       0                   1                   2                   3
     //       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     //       +-+-+-+-+-------+-+-------------+-------------------------------+
     //       |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
     //       |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
     //       |N|V|V|V|       |S|             |   (if payload len==126/127)   |
     //       | |1|2|3|       |K|             |                               |
     //       +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
     //       |     Extended payload length continued, if payload len == 127  |
     //       + - - - - - - - - - - - - - - - +-------------------------------+
     //       |                               |Masking-key, if MASK set to 1  |
     //       +-------------------------------+-------------------------------+
     //       | Masking-key (continued)       |          Payload Data         |
     //       +-------------------------------- - - - - - - - - - - - - - - - +
     //       :                     Payload Data continued ...                :
     //       + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     //       |                     Payload Data continued ...                |
     //       +---------------------------------------------------------------+


    enum State {
        FRAME_START,    // 解析WebSocket帧的必备信息，例如FIN标志，RSV信息
        PAYLOAD_LENGTH, // 解析载荷长度
        MASKING_KEY,    // 解析WebSocket帧包含的MASKING_KEY
        PAYLOAD,        // 解析WebSocket帧的载荷
        BADMESSAGE,
    }

    /**
     * 定义WebSocket帧的类型，其余的目前为保留
     */
    private static final byte OPCODE_CONT = 0x0;   // Continue类型帧
    private static final byte OPCODE_TEXT = 0x1;   // Text类型帧
    private static final byte OPCODE_BINARY = 0x2; // Binary类型帧
    private static final byte OPCODE_CLOSE = 0x8;  // 关闭帧
    private static final byte OPCODE_PING = 0x9;   // Ping帧
    private static final byte OPCODE_PONG = 0xA;   // Pong帧

    private final long maxFramePayLoadLength;      // 允许WebSocket帧携带的最大载荷
    private boolean frameFinalFlag;                // 是否解析到带有FIN标志的帧，与后续对帧的处理有关
    private int frameRsv;                          // WebSocket帧中的RSV相关值
    private int frameOpcode;                       // WebSocket帧中的Opcode信息
    private int framePayLoadRead;                  // 已经读取的WebSocket帧的载荷数量
    private long frameContentLength;                // WebSocket帧中携带的载荷大小
    private byte[] maskingKey;                     // WebSocket中的MaskingKey
    private boolean handshakeFinished;             // 是否接收到closeHandshake报文
    private ByteBuf framePayload;                  // WebSocket帧中的载荷
    private ByteBuf framePayloadBuffer;
    private boolean frameMasked;                   // Masked标志位是否设置

    public MyWebSocketFrameDecoder(int maxFramePayLoadLength) {
        super(State.FRAME_START);
        this.maxFramePayLoadLength = maxFramePayLoadLength;
    }

    @Override
    protected synchronized void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (handshakeFinished) {
            // Websocket传输已经结束，忽略所有数据
            in.skipBytes(actualReadableBytes());
            return;
        }
        try {
            switch (state()) {
                case FRAME_START:
                    reset();
                    byte firstByte = in.readByte();
                    frameFinalFlag = (firstByte & 0x80) != 0;
                    frameRsv = (firstByte & 0x70) >> 4;
                    frameOpcode = firstByte & 0x0F;
                case PAYLOAD_LENGTH:
                    byte b = in.readByte();
                    frameMasked = (b & 0x80) != 0;
                    int frameLength = b & 0x7F;
                    if (frameLength == 126) {
                        frameContentLength = in.readUnsignedShort();
                    } else if (frameLength == 127) {
                        frameContentLength = in.readLong();
                    } else {
                        frameContentLength = frameLength;
                    }
                    checkpoint(State.MASKING_KEY);
                    if (!checkFrameStart(ctx, in)) {
                        checkpoint(State.BADMESSAGE);
                        return ;
                    } else {
                        checkpoint(State.PAYLOAD_LENGTH);
                    }
                case MASKING_KEY:
                    if (frameMasked) {
                        if (maskingKey == null) {
                            maskingKey = new byte[4];
                        }
                        in.readBytes(maskingKey);
                    }
                    checkpoint(State.PAYLOAD);
                case PAYLOAD:
                    int realBytes = actualReadableBytes();
                    long totalBytesCount = framePayLoadRead + realBytes;
                    if (totalBytesCount == frameContentLength) {
                        // 所有数据已经收到，可以获取载荷
                        framePayloadBuffer = ctx.alloc().buffer(realBytes);
                        framePayloadBuffer.writeBytes(in, realBytes);
                    } else if (totalBytesCount < frameContentLength) {
                        if (framePayload == null) {
                            framePayload = ctx.alloc().buffer(toFrameLength(frameContentLength));
                        }
                        framePayload.writeBytes(in, realBytes);
                        framePayLoadRead += realBytes;
                        return ;
                    } else {
                        if (framePayload == null) {
                            framePayload = ctx.alloc().buffer(toFrameLength(frameContentLength));
                        }
                        framePayload.writeBytes(in, toFrameLength(frameContentLength - framePayLoadRead));
                    }

                    checkpoint(State.FRAME_START);
                    if (framePayload == null) {
                        framePayload = framePayloadBuffer;
                        framePayloadBuffer = null;
                    } else if (framePayloadBuffer != null) {
                        framePayload.writeBytes(framePayloadBuffer);
                        framePayloadBuffer.release();
                        framePayloadBuffer = null;
                    }

                    // 如果设置了Key，解密报文体
                    if (frameMasked) {
                        unmask(framePayload);
                    }

                    if (frameOpcode == OPCODE_PING) {
                        out.add(new PingWebSocketFrame(frameFinalFlag, frameRsv, framePayload));
                        framePayload = null;
                        return ;
                    }
                    if (frameOpcode == OPCODE_PONG) {
                        out.add(new PongWebSocketFrame(frameFinalFlag, frameRsv, framePayload));
                        framePayload = null;
                        return ;
                    }
                    if (frameOpcode == OPCODE_CLOSE) {
                        handshakeFinished = true;
                        checkCloseFrameBody(ctx, framePayload);
                        out.add(new CloseWebSocketFrame(frameFinalFlag, frameRsv, framePayload));
                        framePayload = null;
                        return ;
                    }
                    if (frameOpcode == OPCODE_CONT) {
                        out.add(new ContinueWebSocketFrame(frameFinalFlag, frameRsv, framePayload));
                        framePayload = null;
                        return ;
                    }
                    if (frameOpcode == OPCODE_TEXT) {
                        out.add(new TextWebSocketFrame(frameFinalFlag, frameRsv, framePayload));
                        framePayload = null;
                        return ;
                    }
                    if (frameOpcode == OPCODE_BINARY) {
                        out.add(new BinaryWebSocketFrame(frameFinalFlag, frameRsv, framePayload));
                        framePayload = null;
                        return ;
                    } else {
                        throw new UnsupportedOperationException("Cannot decode web socket frame with opcode: " + frameOpcode);
                    }
                case BADMESSAGE:
                    in.readByte();
                    return ;
                default:
                    throw new Error("should not reach there");
            }
        } catch (Exception e) {
            if (framePayloadBuffer != null) {
                if (framePayloadBuffer.refCnt() > 0) {
                    framePayloadBuffer.release();
                }
                framePayloadBuffer = null;
            }
            if (framePayload != null) {
                if (framePayload.refCnt() > 0) {
                    framePayload.release();
                }
                framePayload = null;
            }
            throw e;
        }
    }

    private void unmask(ByteBuf frame) {
        int i = frame.readerIndex();
        int end = frame.writerIndex();
        ByteOrder order = frame.order();
        int intMask = ((maskingKey[0] & 0xFF) << 24)
                | ((maskingKey[1] & 0xFF) << 16)
                | ((maskingKey[2] & 0xFF) << 8)
                | (maskingKey[3] & 0xFF);
        if (order == ByteOrder.LITTLE_ENDIAN) {
            intMask = Integer.reverseBytes(intMask);
        }
        for (; i + 3 < end; i += 4) {
            int unmasked = frame.getInt(i) ^ intMask;
            frame.setInt(i, unmasked);
        }
        for (; i < end; i++) {
            frame.setByte(i, frame.getByte(i) ^ maskingKey[i % 4]);
        }
    }

    private static int toFrameLength(long l) {
        if (l > Integer.MAX_VALUE) {
            throw new TooLongFrameException("Length:" + l);
        } else {
            return (int) l;
        }
    }

    private void reset() {
        framePayLoadRead = 0;
        frameContentLength = -1;
        framePayload = null;
        framePayloadBuffer = null;
    }

    private boolean checkFrameStart(ChannelHandlerContext ctx, ByteBuf in) {
        if (frameRsv != 0 && false) {
            protocolViolation(ctx, "RSV != 0 and no extension negotiated, RSV:" + frameRsv);
            return false;
        }
        if (frameOpcode > 7) {
            if (!frameFinalFlag) {
                protocolViolation(ctx, "Cannot identify:" + frameOpcode);
                return false;
            }
            if (!(frameOpcode == OPCODE_CLOSE ||frameOpcode == OPCODE_PING || frameOpcode == OPCODE_PONG)) {
                protocolViolation(ctx, "Cannot identify:" + frameOpcode);
                return false;
            }
        } else {
            if (!(frameOpcode == OPCODE_CONT ||frameOpcode == OPCODE_TEXT || frameOpcode == OPCODE_BINARY)) {
                protocolViolation(ctx, "Cannot identify:" + frameOpcode);
                return false;
            }
        }
        if (frameContentLength > maxFramePayLoadLength) {
            protocolViolation(ctx, "PayloadSize is too big!"+frameContentLength);
            return false;
        }
        return true;
    }

    protected void checkCloseFrameBody(
            ChannelHandlerContext ctx, ByteBuf buffer) {
        if (buffer == null || !buffer.isReadable()) {
            return;
        }
        if (buffer.readableBytes() == 1) {
            protocolViolation(ctx, "should not has size in it's body!");
        }

        // Save reader index
        int idx = buffer.readerIndex();
        buffer.readerIndex(0);

        // Must have 2 byte integer within the valid range
        int statusCode = buffer.readShort();
        if (statusCode >= 0 && statusCode <= 999 || statusCode >= 1004 && statusCode <= 1006
                || statusCode >= 1012 && statusCode <= 2999) {
            protocolViolation(ctx, "Unitentified statusCode " + statusCode + "!");
        }

        // Restore reader index
        buffer.readerIndex(idx);
    }

    private void sendContentAndResetFrame(ChannelHandlerContext ctx, ByteBuf in) {
        ctx.fireChannelRead(in);
        in.readByte();
        in.readerIndex(in.writerIndex());
        in.discardReadBytes();
    }

    private void protocolViolation(ChannelHandlerContext ctx, String reason) {
        protocolViolation(ctx, new CorruptedFrameException(reason));
    }

    private void protocolViolation(ChannelHandlerContext ctx, CorruptedFrameException ex) {
        checkpoint(State.BADMESSAGE);
        if (ctx.channel().isActive()) {
            Object closeMessage;
            if (handshakeFinished) {
                closeMessage = Unpooled.EMPTY_BUFFER;
            } else {
                closeMessage = new CloseWebSocketFrame(1002, null);
            }
            ctx.writeAndFlush(closeMessage).addListener(ChannelFutureListener.CLOSE);
        }
        throw ex;
    }



}
