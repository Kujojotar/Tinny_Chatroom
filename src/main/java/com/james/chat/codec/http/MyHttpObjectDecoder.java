package com.james.chat.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;

import java.util.List;

public abstract class MyHttpObjectDecoder extends ByteToMessageDecoder {
    private static final String EMPTY_VALUE = "";
    private final int maxChunkSize;
    private long chunkSize;
    private long contentLength = Long.MIN_VALUE;
    private volatile boolean resetRequest;
    private State currentState;
    private LineParser lineParser;
    private HeaderParser headerParser;
    private CharSequence name;
    private CharSequence value;
    private MyHttpMessage message;
    private MyLastHttpContent trailer;

    protected MyHttpObjectDecoder(int initialBufferSize,int maxChunkSize) {
        this.contentLength = -9223372036854775808L;
        this.currentState = State.SKIP_CONTROL_CHARS;
        this.maxChunkSize = maxChunkSize;
        AppendableCharSequence seq = new AppendableCharSequence(initialBufferSize);
        this.lineParser = new LineParser(seq, 4096);
        this.headerParser = new HeaderParser(seq, 8192);
    }

    protected MyHttpObjectDecoder(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, int initialBufferSize) {
        if (maxInitialLineLength <= 0) {
            throw new IllegalArgumentException(
                    "maxInitialLineLength must be a positive integer: " +
                            maxInitialLineLength);
        }
        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException(
                    "maxHeaderSize must be a positive integer: " +
                            maxHeaderSize);
        }
        if (maxChunkSize <= 0) {
            throw new IllegalArgumentException(
                    "maxChunkSize must be a positive integer: " +
                            maxChunkSize);
        }
        this.currentState = State.SKIP_CONTROL_CHARS;
        AppendableCharSequence seq = new AppendableCharSequence(initialBufferSize);
        lineParser = new LineParser(seq, maxInitialLineLength);
        headerParser = new HeaderParser(seq, maxHeaderSize);
        this.maxChunkSize = maxChunkSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception{
        // ?????????????????????resetRequest????????????????????????????????????
        if (this.resetRequest) {
            this.resetNow();
        }
        int toRead;
        AppendableCharSequence line;
        switch (this.currentState) {
            case SKIP_CONTROL_CHARS:
                if (!skipControlCharacters(buffer)) {
                    return ;
                }
                this.currentState = State.READ_INITIAL;
            case READ_INITIAL:
                try {
                    line = this.lineParser.parse(buffer);
                    if (line == null) {
                        return ;
                    }
                    String[] initialLine = splitInitialLine(line);
                    if (initialLine.length < 3) {
                        // ?????????HTTP/0.9???????????????,???????????????HTTP/0.9?????????
                        this.currentState = State.SKIP_CONTROL_CHARS;
                        return ;
                    }
                    // ??????????????????????????????HttpMessage??????
                    this.message = this.createMessage(initialLine);
                    this.currentState = State.READ_HEADER;
                } catch (Exception e) {
                    out.add(invalidMessage(buffer, e));
                    return ;
                }
            case READ_HEADER:
                try {
                    State nextState = this.readHeaders(buffer);
                    if (nextState == null) {
                        return ;
                    }
                    this.currentState = nextState;
                    switch (nextState) {
                        case SKIP_CONTROL_CHARS:
                            // ??????HTTP????????????????????????????????????????????????????????????out???
                            out.add(message);
                            out.add(LastHttpContent.EMPTY_LAST_CONTENT);
                            this.resetNow();
                            return ;
                        case READ_CHUNK_SIZE:
                            // ??????????????????HTTP???????????????out???
                            out.add(message);
                            return ;
                        default:
                            /**
                             * ??????????????????????????????transfer-encoding????????????content-length??????
                             * ??????response??????????????????????????????????????????
                             */
                            long contentLength = contentLength();
                            if (contentLength != 0L && (contentLength != -1L || !this.isDecodingRequest())) {
                                assert nextState == State.READ_FIXED_LENGTH_CONTENT || nextState == State.READ_VARIABLE_LENGTH_CONTENT;

                                out.add(message);
                                if (nextState == State.READ_FIXED_LENGTH_CONTENT) {
                                    this.chunkSize = contentLength;
                                }

                                return ;
                            }
                            out.add(message);
                            out.add(LastHttpContent.EMPTY_LAST_CONTENT);
                            this.resetNow();
                            return ;
                    }
                } catch (Exception e) {
                    out.add(invalidMessage(buffer, e));
                    return ;
                }
            case READ_VARIABLE_LENGTH_CONTENT:
                toRead = Math.min(buffer.readableBytes(), this.maxChunkSize);
                if (toRead > 0) {
                    ByteBuf content = buffer.readRetainedSlice(toRead);
                    out.add(new MyHttpContent(content));
                } else {
                    out.add(new MyLastHttpContent());
                }
                return ;
            case READ_FIXED_LENGTH_CONTENT:
                toRead = buffer.readableBytes();
                if (toRead == 0) {
                    return ;
                }
                toRead = Math.min(toRead, this.maxChunkSize);
                if ((long) toRead > this.chunkSize) {
                    toRead = (int) this.chunkSize;
                }

                ByteBuf content = buffer.readRetainedSlice(toRead);
                this.chunkSize -= (long) toRead;
                if (this.chunkSize == 0L) {
                    out.add(new MyHttpContent(content));
                    out.add(new MyLastHttpContent());
                    this.resetNow();
                } else {
                    out.add(new MyHttpContent(content));
                }
                return;
            case READ_CHUNK_SIZE:
                try {
                    line = this.lineParser.parse(buffer);
                    if (line == null) {
                        return ;
                    }
                    int chunkSize = getChunkSize(line.toString());
                    this.chunkSize = chunkSize;
                    if (chunkSize == 0) {
                        currentState = State.READ_CHUNK_FOOTER;
                        return;
                    }
                    currentState = State.READ_CHUNKED_CONTENT;
                } catch (Exception e) {
                    out.add(invalidMessage(buffer, e));
                    return ;
                }
            case READ_CHUNKED_CONTENT:
                assert this.chunkSize <= Integer.MAX_VALUE;

                toRead = Math.min((int)this.chunkSize, this.maxChunkSize);
                toRead = Math.min(toRead, buffer.readableBytes());
                if (toRead == 0) {
                    return;
                }
                MyHttpContent chunk = new MyHttpContent(buffer.readSlice(toRead).retain());
                this.chunkSize -= toRead;
                out.add(chunk);
                if (chunkSize != 0) {
                    return ;
                }
                this.currentState = State.READ_CHUNK_DELIMITER;
            case READ_CHUNK_DELIMITER:
                final int wIndex = buffer.writerIndex();
                int rIndex = buffer.readerIndex();
                while (wIndex > rIndex) {
                    byte next = buffer.getByte(rIndex++);
                    if (next == (byte)10) {
                        currentState = State.READ_CHUNK_SIZE;
                        break;
                    }
                }
                buffer.readerIndex(rIndex);
                return ;
            case READ_CHUNK_FOOTER:
                MyLastHttpContent trailer = readTrailingHeaders(buffer);
                if (trailer == null) {
                    return ;
                }
                out.add(trailer);
                resetNow();
                return ;
            case BAD_MESSAGE:
                buffer.skipBytes(buffer.readableBytes());
                break;
            case UPGRADE:
                toRead = buffer.readableBytes();
                if (toRead > 0) {
                    // ???????????????????????????DecoderException
                    // ?????????????????????handler?????????upgraded protocol codec??????????????????codec
                    out.add(buffer.readBytes(toRead));
                }
            default:
        }
    }

    private long contentLength() {
        if (contentLength == Long.MIN_VALUE) {
            contentLength = MyHttpHeaders.getContentLength(message, -1);
        }
        return contentLength;
    }

    protected void resetNow() {
        MyHttpMessage message = this.message;
        this.message = null;
        name = null;
        value = null;
        contentLength = Long.MIN_VALUE;
        lineParser.reset();
        headerParser.reset();
        trailer = null;
        if (!isDecodingRequest()) {

        }

        resetRequest = false;
        currentState = State.SKIP_CONTROL_CHARS;
    }

    private static int getChunkSize(String hex) {
        hex = hex.trim();

        for(int i = 0; i < hex.length(); ++i) {
            char c = hex.charAt(i);
            if (c == ';' || Character.isWhitespace(c) || Character.isISOControl(c)) {
                hex = hex.substring(0, i);
                break;
            }
        }

        return Integer.parseInt(hex, 16);
    }

    private static String[] splitInitialLine(AppendableCharSequence sb) {
        int aStart = findNonWhitespace(sb, 0);
        int aEnd = findWhitespace(sb, aStart);
        int bStart = findNonWhitespace(sb, aEnd);
        int bEnd = findWhitespace(sb, bStart);
        int cStart = findNonWhitespace(sb, bEnd);
        int cEnd = findEndOfString(sb);
        return new String[]{sb.subStringUnsafe(aStart, aEnd), sb.subStringUnsafe(bStart, bEnd), cStart < cEnd ? sb.subStringUnsafe(cStart, cEnd) : ""};
    }


    private State readHeaders(ByteBuf buffer) {
        MyHttpHeaders headers = message.getHeaders();
        AppendableCharSequence line = this.headerParser.parse(buffer);
        if (line == null) {
            return null;
        } else {
            if (line.length() > 0) {
                do {
                    char firstChar = line.charAtUnsafe(0);
                    if (this.name != null && firstChar == ' ' || firstChar == '\t') {
                        // ???????????????HTTP????????????????????????header????????????
                        String trimmedLine = line.toString().trim();
                        String valueStr = String.valueOf(this.value);
                        this.value = valueStr + ' ' + trimmedLine;
                    } else {
                        if (this.name != null) {
                            headers.add(this.name, this.value);
                        }
                        this.splitHeader(line);
                    }

                    line = this.headerParser.parse(buffer);
                    if (line == null) {
                        return null;
                    }
                } while (line.length() > 0);
            }

            if (this.name != null) {
                headers.add(this.name, this.value);
            }

            this.name = null;
            this.value = null;

            State nextState;
            if (isContentAlwaysEmpty(message)) {
                // HTTP????????????????????????????????????
                nextState = State.SKIP_CONTROL_CHARS;
            } else if (MyHttpHeaders.isTransferEncodingChunked(message)) {
                // HTTP?????????CHUNK??????
                nextState = State.READ_CHUNK_SIZE;
            } else if (this.contentLength() >= 0L) {
                // ??????ContentLength?????????????????????????????????????????????
                nextState = State.READ_FIXED_LENGTH_CONTENT;
            } else {
                // ?????????ContentLength???????????????????????????
                nextState = State.READ_VARIABLE_LENGTH_CONTENT;
            }
            return nextState;
        }
    }

    /**
     * ??????buffer????????????????????????
     * @param buffer ?????????????????????????????????????????????
     * @return ????????????
     */
    private static boolean skipControlCharacters(ByteBuf buffer) {
        boolean skiped = false;
        int writeIndex = buffer.writerIndex();
        int readIndex = buffer.readerIndex();
        while (writeIndex > readIndex) {
            int c = buffer.getUnsignedByte(readIndex++);
            if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
                --readIndex;
                skiped = true;
                break;
            }
        }

        buffer.readerIndex(readIndex);
        return skiped;
    }

    private static final class LineParser extends HeaderParser {
        LineParser(AppendableCharSequence seq, int maxLength) {
            super(seq, maxLength);
        }

        @Override
        public AppendableCharSequence parse(ByteBuf buffer) {
            this.reset();
            return super.parse(buffer);
        }

        @Override
        protected TooLongFrameException newException(int maxLength) {
            return new TooLongFrameException("An HTTP line is larger than " + maxLength + " bytes.");
        }
    }

    private void splitHeader(AppendableCharSequence sb) {
        int length = sb.length();
        int nameStart = findNonWhitespace(sb, 0);

        int nameEnd;
        for(nameEnd = nameStart; nameEnd < length; ++nameEnd) {
            char ch = sb.charAtUnsafe(nameEnd);
            if (ch == ':' || !this.isDecodingRequest() && Character.isWhitespace(ch)) {
                break;
            }
        }

        int colonEnd;
        for(colonEnd = nameEnd; colonEnd < length; ++colonEnd) {
            if (sb.charAtUnsafe(colonEnd) == ':') {
                ++colonEnd;
                break;
            }
        }

        this.name = sb.subStringUnsafe(nameStart, nameEnd);
        int valueStart = findNonWhitespace(sb, colonEnd);
        if (valueStart == length) {
            this.value = "";
        } else {
            int valueEnd = findEndOfString(sb);
            this.value = sb.subStringUnsafe(valueStart, valueEnd);
        }

    }

    private static int findNonWhitespace(AppendableCharSequence sb, int offset) {
        for(int result = offset; result < sb.length(); ++result) {
            if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
                return result;
            }
        }

        return sb.length();
    }

    private static int findWhitespace(AppendableCharSequence sb, int offset) {
        for(int result = offset; result < sb.length(); ++result) {
            if (Character.isWhitespace(sb.charAtUnsafe(result))) {
                return result;
            }
        }

        return sb.length();
    }

    private static int findEndOfString(AppendableCharSequence sb) {
        for(int result = sb.length() - 1; result > 0; --result) {
            if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
                return result + 1;
            }
        }
        return 0;
    }

    protected boolean isContentAlwaysEmpty(MyHttpMessage msg) {
        return false;
    }

    private MyHttpMessage invalidMessage(ByteBuf in, Exception cause) {
        currentState = State.BAD_MESSAGE;
        in.skipBytes(in.readableBytes());
        if (message != null) {
            message.setDecoderResult(DecoderResult.failure(cause));
        } else {
            message = createInvalidMessage();
            message.setDecoderResult(DecoderResult.failure(cause));
        }
        MyHttpMessage ret = message;
        message = null;
        return ret;
    }

    private static class HeaderParser implements ByteProcessor{
        private final AppendableCharSequence seq;
        private final int maxLength;
        private int size;

        HeaderParser(AppendableCharSequence seq, int maxLength) {
            this.seq = seq;
            this.maxLength = maxLength;
        }

        public AppendableCharSequence parse(ByteBuf buffer) {
            int oldSize = this.size;
            this.seq.reset();
            int i = buffer.forEachByte(this);
            if (i == -1) {
                this.size = oldSize;
                return null;
            } else {
                buffer.readerIndex(i + 1);
                return this.seq;
            }
        }

        public void reset() {
            this.size = 0;
        }

        @Override
        public boolean process(byte value) throws Exception {
            char nextByte = (char)(value & 255);
            if (nextByte == '\r') {
                return true;
            } else if (nextByte == '\n') {
                return false;
            } else if (++this.size > this.maxLength) {
                throw this.newException(this.maxLength);
            } else {
                this.seq.append(nextByte);
                return true;
            }
        }

        protected TooLongFrameException newException(int maxLength) {
            return new TooLongFrameException("HTTP header is larger than " + maxLength + " bytes.");
        }
    }

    private static enum State {
        // ?????????????????????????????????
        SKIP_CONTROL_CHARS,
        // ???????????????
        READ_INITIAL,
        // ??????header???
        READ_HEADER,
        READ_VARIABLE_LENGTH_CONTENT,
        READ_FIXED_LENGTH_CONTENT,
        READ_CHUNK_SIZE,
        READ_CHUNKED_CONTENT,
        READ_CHUNK_DELIMITER,
        READ_CHUNK_FOOTER,
        BAD_MESSAGE,
        // ?????????????????????
        UPGRADE;
        private State() {
            // ???????????????????????????
        }
    }

    private MyLastHttpContent readTrailingHeaders(ByteBuf buffer) {
        AppendableCharSequence line = headerParser.parse(buffer);
        if (line == null) {
            return null;
        }
        CharSequence lastHeader = null;
        if (line.length() > 0) {
            MyLastHttpContent trailer = this.trailer;
            if (trailer == null) {
                trailer = this.trailer = new MyLastHttpContent(Unpooled.EMPTY_BUFFER, true);
            }
            do {
                char firstChar = line.charAt(0);
                if (lastHeader != null && (firstChar == ' ') || firstChar == '\t') {
                    List<String> current = trailer.trailingHeaders().getAll(lastHeader);
                    if (!current.isEmpty()) {
                        int lastPos = current.size() - 1;
                        //please do not make one line from below code
                        //as it breaks +XX:OptimizeStringConcat optimization
                        String lineTrimmed = line.toString().trim();
                        String currentLastPos = current.get(lastPos);
                        current.set(lastPos, currentLastPos + lineTrimmed);
                    }
                } else {
                    splitHeader(line);
                    CharSequence headerName = name;
                    if (!MyHttpHeaders.equalsIgnoreCase(HttpHeaderCommons.Names.CONTENT_LENGTH, headerName)
                    && !MyHttpHeaders.equalsIgnoreCase(HttpHeaderCommons.Names.TRANSFER_ENCODING, headerName)
                    && !MyHttpHeaders.equalsIgnoreCase(HttpHeaderCommons.Names.TRAILER, headerName)) {
                        trailer.trailingHeaders().add(headerName, value);
                    }
                    lastHeader = name;
                    name = null;
                    value = null;
                }
                line = headerParser.parse(buffer);
                if (line == null) {
                    return null;
                }
            } while (line.length() > 0);
            this.trailer = null;
            return trailer;
        }
        return new MyLastHttpContent();
    }


    protected abstract boolean isDecodingRequest();
    protected abstract MyHttpMessage createMessage(String[] initialLine) throws Exception;
    protected abstract MyHttpMessage createInvalidMessage();


}
