package com.james.chat.codec.http;

import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

public class MyHttpMethod implements Comparable<MyHttpMethod>{

    public static final MyHttpMethod OPTIONS = new MyHttpMethod("OPTIONS", true);
    public static final MyHttpMethod GET = new MyHttpMethod("GET", true);
    public static final MyHttpMethod HEAD = new MyHttpMethod("HEAD", true);
    public static final MyHttpMethod POST = new MyHttpMethod("POST", true);
    public static final MyHttpMethod PUT = new MyHttpMethod("PUT", true);
    public static final MyHttpMethod PATCH = new MyHttpMethod("PATCH", true);
    public static final MyHttpMethod DELETE = new MyHttpMethod("DELETE", true);
    public static final MyHttpMethod TRACE = new MyHttpMethod("TRACE", true);
    public static final MyHttpMethod CONNECT = new MyHttpMethod("CONNECT", true);

    private final String name;
    private final byte[] bytes;

    private static final Map<String, MyHttpMethod> methodMap =
            new HashMap<String, MyHttpMethod>();

    static {
        methodMap.put(OPTIONS.toString(), OPTIONS);
        methodMap.put(GET.toString(), GET);
        methodMap.put(HEAD.toString(), HEAD);
        methodMap.put(POST.toString(), POST);
        methodMap.put(PUT.toString(), PUT);
        methodMap.put(PATCH.toString(), PATCH);
        methodMap.put(DELETE.toString(), DELETE);
        methodMap.put(TRACE.toString(), TRACE);
        methodMap.put(CONNECT.toString(), CONNECT);
    }

    public static MyHttpMethod valueOf(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        MyHttpMethod result = methodMap.get(name);
        if (result != null) {
            return result;
        } else {
            return new MyHttpMethod(name);
        }
    }
    public MyHttpMethod(String name) {
        this(name, false);
    }

    public MyHttpMethod(String name, boolean bytes) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        for (int i = 0; i < name.length(); i ++) {
            if (Character.isISOControl(name.charAt(i)) ||
                    Character.isWhitespace(name.charAt(i))) {
                throw new IllegalArgumentException("invalid character in name");
            }
        }

        this.name = name;
        if (bytes) {
            this.bytes = name.getBytes(CharsetUtil.US_ASCII);
        } else {
            this.bytes = null;
        }
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyHttpMethod)) {
            return false;
        }

        MyHttpMethod that = (MyHttpMethod) o;
        return name().equals(that.name());
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public int compareTo(MyHttpMethod o) {
        return name().compareTo(o.name());
    }
}
