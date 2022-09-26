package com.james.chat.entity;

import lombok.Data;

@Data
public class RequestVo {
    String fromUserId;
    String toUserId;
    String reason;
}
