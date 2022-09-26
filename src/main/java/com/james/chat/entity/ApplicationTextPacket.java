package com.james.chat.entity;

import lombok.Data;

@Data
public class ApplicationTextPacket {
    private Integer frameType;
    private String messageId;
    private String fromUserId;
    private String toUserId;
    private String textContent;
    private String token;
    private String avatarUrl;

    public boolean isGroupMessage() {
        return frameType >= PacketType.GROUP_SEND_TEXT_FRAME && frameType <= PacketType.GROUP_SEND_TEXT_ACK;
    }

    public boolean isAck() {
        return (frameType >= PacketType.FORWARD_TEXT_ACK && frameType <= PacketType.SEND_TEXT_ACK) ||
                (frameType >= PacketType.GROUP_FORWARD_TEXT_ACK && frameType <= PacketType.GROUP_SEND_TEXT_ACK);
    }

    public static class PacketType{
        public static int SEND_TEXT_FRAME = 0;
        public static int FORWARD_TEXT_FRAME = 1;
        public static int FORWARD_TEXT_ACK = 2;
        public static int SEND_TEXT_ACK = 3;

        public static int GROUP_SEND_TEXT_FRAME = 4;
        public static int GROUP_FORWARD_TEXT_FRAME = 5;
        public static int GROUP_FORWARD_TEXT_ACK = 6;
        public static int GROUP_SEND_TEXT_ACK = 7;

        public static int SERVER_PROXY_MESSAGE = 8;
        public static int CLIENT_RESPONSE_PROXY = 9;

        public static int TRANSFER_FILE = 10;
        public static int TRANSFER_FILE_ACK = 11;
        public static int TRANSFER_FILE_PROXY_FAIL = 12;
    }
}
