package com.james.chat.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String writeObject(Object val) {
        String str;
        try {
            str = objectMapper.writeValueAsString(val);
        } catch (JsonProcessingException e) {
            str = null;
        }
        return str;
    }

    public static <T> T getObject(String str, Class<T> clazz) {
        T obj;
        try {
            obj = objectMapper.readValue(str, clazz);
        } catch (JsonProcessingException e) {
            obj = null;
        }
        return obj;
    }

    public static <T> T getObject(byte[] arr, Class<T> clazz) {
        T obj;
        try {
            obj = objectMapper.readValue(arr, clazz);
        } catch (Exception e) {
            obj = null;
        }
        return obj;
    }

}
