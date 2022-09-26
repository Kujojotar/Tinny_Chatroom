package com.james.chat.codec.http;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * HTTP协议的版本以及其底层的协议
 * 比如 RTSP http://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol
 * 比如 ICAP http://en.wikipedia.org/wiki/Internet_Content_Adaptation_Protocol
 */
public class MyHttpVersion implements Comparable<MyHttpVersion>{


    private static final String HTTP_1_0_STRING = "HTTP/1.0";
    private static final String HTTP_1_1_STRING = "HTTP/1.1";

    /**
     * HTTP/1.0
     */
    public static final MyHttpVersion HTTP_1_0 = new MyHttpVersion("HTTP", 1, 0, false, true);

    /**
     * HTTP/1.1
     */
    public static final MyHttpVersion HTTP_1_1 = new MyHttpVersion("HTTP", 1, 1, true, true);

    /**
     * 返回一个与方法入参相同的HTTP协议版本，如果匹配HTTP/1.0则返回1.0，匹配HTTP/1.1则返回1.1
     * 如果无法匹配则返回null
     */
    public static MyHttpVersion valueOf(String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        text = text.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("text is empty (we do not back up HTTP/0.9 protocol)");
        }

        if (HTTP_1_0_STRING.equalsIgnoreCase(text)) {
            return HTTP_1_0;
        }
        if (HTTP_1_1_STRING.equalsIgnoreCase(text)) {
            return HTTP_1_1;
        }
        return null;
    }

    private final String protocolName;
    private final int majorVersion;
    private final int minorVersion;
    private final String text;
    private final boolean keepAliveDefault;
    private final byte[] bytes;

    public MyHttpVersion(String protocolName, int majorVersion, int minorVersion, boolean keepAliveDefault) {
        this(protocolName, majorVersion, minorVersion, keepAliveDefault, false);
    }

    public MyHttpVersion(String protocolName, int majorVersion, int minorVersion, boolean keepAliveDefault, boolean bytes) {
        protocolName = protocolName.trim().toUpperCase(Locale.ROOT);
        this.protocolName = "HTTP";
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        text = protocolName + '/' + majorVersion + '.' +minorVersion;
        this.keepAliveDefault = keepAliveDefault;
        if (bytes) {
            this.bytes = text.getBytes(StandardCharsets.US_ASCII);
        } else {
            this.bytes = null;
        }
    }

    public String protocolName() {
        return protocolName;
    }

    public int majorVersion() {
        return majorVersion;
    }

    public int minorVersion() {
        return minorVersion;
    }

    public String text() {
        return text;
    }

    public boolean isKeepAliveDefault() {
        return keepAliveDefault;
    }

    @Override
    public String toString() {
        return text();
    }

    @Override
    public int hashCode() {
        return (protocolName().hashCode() * 31 + majorVersion()) * 31 +
                minorVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyHttpVersion)) {
            return false;
        }

        MyHttpVersion that = (MyHttpVersion) o;
        return minorVersion() == that.minorVersion() &&
                majorVersion() == that.majorVersion() &&
                protocolName().equals(that.protocolName());
    }

    @Override
    public int compareTo(MyHttpVersion o) {
        int v = protocolName().compareTo(o.protocolName());
        if (v != 0) {
            return v;
        }

        v = majorVersion() - o.majorVersion();
        if (v != 0) {
            return v;
        }

        return minorVersion() - o.minorVersion();
    }

    public String encodeToString() {
        return protocolName + '/' + majorVersion + '/' + minorVersion;
    }
}
