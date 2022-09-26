package com.james.chat.vo;

import lombok.Data;

@Data
public class GroupOrUserVo {
    private int type;
    private String name;

    public static class VoType{
        public static int TYPE_USER = 0;
        public static int TYPE_GROUP = 1;
    }
}
