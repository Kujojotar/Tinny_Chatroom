package com.james.chat.util;

import javax.net.ssl.SSLEngine;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class HandshakeUtil {

    public static class SSLEngineResultDTO {
        public SSLEngine sslEngine;
        public SocketChannel socketChannel;
        public ByteBuffer myAppBuf;
        public ByteBuffer myNetBuf;
        public ByteBuffer peerAppBuf;
        public ByteBuffer peerNetBuf;

        public SSLEngineResultDTO(SSLEngine sslEngine, SocketChannel socketChannel, int appBufferSize, int netBufferSize) {
            this.sslEngine = sslEngine;
            this.socketChannel = socketChannel;
            this.myAppBuf = this.peerAppBuf = ByteBuffer.allocate(appBufferSize);
            this.myNetBuf = this.peerNetBuf = ByteBuffer.allocate(netBufferSize);
        }

        public void clearAllBuffer() {
            this.myAppBuf.clear();
            this.myNetBuf.clear();
            this.peerAppBuf.clear();
            this.peerNetBuf.clear();
        }
    }
}
