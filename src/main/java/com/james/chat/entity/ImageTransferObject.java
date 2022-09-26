package com.james.chat.entity;

import lombok.Data;

@Data
public class ImageTransferObject {
    private Integer frameType = 13;
    private String messageId;
    private String fromUserId;
    private String toUserId;
    private byte[] textContent;
    private String fileType;
    private String token;
}
