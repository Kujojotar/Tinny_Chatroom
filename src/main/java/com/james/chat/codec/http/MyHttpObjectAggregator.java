package com.james.chat.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

public class MyHttpObjectAggregator extends MessageToMessageDecoder<MyHttpObject> {
    // 默认最大处理块大小
    public static final int DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS = 1024;

    private final int maxContentLength;
    private int maxCumulationBufferComponents = DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS;
    private AggregatedFullHttpMessage message;

    public MyHttpObjectAggregator(int maxContentLength) {
        if (maxContentLength < 0) {
            throw new IllegalArgumentException("max ContentLength should not less than zero!");
        }
        this.maxContentLength = maxContentLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, MyHttpObject msg, List<Object> out) throws Exception {
        if (msg instanceof MyHttpMessage) {
            if (message != null) {
                message.release();
                message = null;
                throw new IllegalStateException("It should not received a message when there exists message unfinished!");
            }
            MyHttpMessage receiveMsg = (MyHttpMessage) msg;
            if (MyHttpHeaders.is100ContinueExpected(receiveMsg)) {
                // TODO:处理HTTP 100状态
            }

            if (!receiveMsg.getDecoderResult().isSuccess()) {
                MyHttpHeaders.removeTransferEncodingChunked(receiveMsg);
                return ;
            }
            if (receiveMsg instanceof MyHttpRequest) {
                message = new AggregatedFullHttpRequest(receiveMsg, ctx.alloc().compositeBuffer(maxCumulationBufferComponents), null);
            }
            if (MyHttpHeaders.getContentLength(receiveMsg, 0) == 0) {
                AggregatedFullHttpMessage currentMessage = this.message;
                this.message = null;
                out.add(currentMessage);
                return ;
            }
            MyHttpHeaders.removeTransferEncodingChunked(receiveMsg);
        } else if (msg instanceof MyHttpContent) {
            if (message == null) {
                return ;
            }
            MyHttpContent chunk = (MyHttpContent) msg;
            CompositeByteBuf content = (CompositeByteBuf) message.getContent();
            if (content.readableBytes() > maxContentLength - chunk.content().readableBytes()) {
                // 报文过长
                message.release();
                message = null;
                throw new TooLongFrameException("Http content size exceeds " + maxContentLength + " bytes!");
            }
            if (chunk.content().isReadable() && chunk.content().readableBytes()>0) {
                content.addComponent(true, chunk.content().retain());
            }
            final boolean last;
            if (!chunk.getDecoderResult().isSuccess()) {
                message.getMessage().setDecoderResult(DecoderResult.failure(chunk.getDecoderResult().cause()));
                last = true;
            } else {
                last = chunk instanceof MyLastHttpContent;
            }
            if (last) {
                if (chunk instanceof MyLastHttpContent) {
                    MyLastHttpContent lastContent = (MyLastHttpContent) chunk;
                    message.setTrailingHeaders(lastContent.trailingHeaders());
                } else {
                    message.setTrailingHeaders(null);
                }

                // 如果没有设置 Content-Length头，根据接收到的信息自行设置
                if (!MyHttpHeaders.isContentLengthSet(message.getMessage())) {
                    message.getMessage().getHeaders()
                            .set(HttpHeaderCommons.Names.CONTENT_LENGTH, String.valueOf(message.content.readableBytes()));
                }
                AggregatedFullHttpMessage currentMessage = this.message;
                this.message = null;
                out.add(currentMessage);
            }
        } else {
            // 无法处理其他信息
            throw new Error();
        }
    }

    private abstract static class AggregatedFullHttpMessage {
        protected final MyHttpMessage message;
        private final ByteBuf content;
        private MyHttpHeaders trailingHeaders;

        AggregatedFullHttpMessage(MyHttpMessage message, ByteBuf content, MyHttpHeaders trailingHeaders) {
            this.content = content;
            this.message = message;
            this.trailingHeaders = trailingHeaders;
        }

        public MyHttpMessage getMessage() {
            return message;
        }

        public ByteBuf getContent() {
            return content;
        }

        public MyHttpHeaders getTrailingHeaders() {
            return trailingHeaders;
        }

        public boolean release(){
            return content.release();
        }

        public void setTrailingHeaders(MyHttpHeaders trailingHeaders) {
            this.trailingHeaders = trailingHeaders;
        }
    }

    private static class AggregatedFullHttpRequest extends AggregatedFullHttpMessage implements MyFullHttpRequest{


        AggregatedFullHttpRequest(MyHttpMessage message, ByteBuf content, MyHttpHeaders trailingHeaders) {
            super(message, content, trailingHeaders);
        }

        @Override
        public void setProtocolVersion(MyHttpVersion version) {
            ((MyHttpRequest)message).setHttpVersion(version);
        }

        @Override
        public void setMethod(MyHttpMethod method) {
            ((MyHttpRequest)message).setMethod(method);
        }

        @Override
        public void setUri(String uri) {
            ((MyHttpRequest)message).setUri(uri);
        }

        @Override
        public MyHttpMethod getMethod() {
            return ((MyHttpRequest)message).getMethod();
        }

        @Override
        public String getUri() {
            return ((MyHttpRequest)message).getUri();
        }

        @Override
        public MyHttpHeaders getHeaders() {
            return ((MyHttpRequest)message).getHeaders();
        }

        @Override
        public MyHttpVersion getVersion() {
            return message.getVersion();
        }
    }
}
