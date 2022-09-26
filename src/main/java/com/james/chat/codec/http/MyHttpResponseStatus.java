package com.james.chat.codec.http;

import io.netty.util.CharsetUtil;

public class MyHttpResponseStatus implements Comparable<MyHttpResponseStatus>{
    private final int code;
    private final String reasonPhase;
    private final byte[] bytes;

    public MyHttpResponseStatus(int code, String reasonPhase) {
        this(code, reasonPhase, false);
    }

    public MyHttpResponseStatus(int code, String reasonPhrase, boolean bytes) {
        if (code < 0) {
            throw new IllegalArgumentException(
                    "code: " + code + " (expected: 0+)");
        }
        if (reasonPhrase == null) {
            throw new NullPointerException("reasonPhrase");
        }
        for (int i = 0; i < reasonPhrase.length(); i ++) {
            char c = reasonPhrase.charAt(i);
            // Check prohibited characters.
            switch (c) {
                case '\n': case '\r':
                    throw new IllegalArgumentException(
                            "reasonPhrase contains one of the following prohibited characters: " +
                                    "\\r\\n: " + reasonPhrase);
            }
        }
        this.code = code;
        this.reasonPhase = reasonPhrase;
        if (bytes) {
            this.bytes = (code + " " + reasonPhrase).getBytes(CharsetUtil.US_ASCII);
        } else {
            this.bytes = null;
        }
    }

    public int code() {
        return code;
    }

    public String reasonPhrase() {
        return reasonPhase;
    }

    @Override
    public int hashCode() {
        return code();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyHttpResponseStatus)) {
            return false;
        }

        return code() == ((MyHttpResponseStatus) o).code();
    }

    @Override
    public int compareTo(MyHttpResponseStatus o) {
        return code() - o.code();
    }

    @Override
    public String toString() {
        return new StringBuilder(reasonPhase.length() + 5)
                .append(code)
                .append(' ')
                .append(reasonPhase)
                .toString();
    }


    /**
     * 100 Continue
     */
    public static final MyHttpResponseStatus CONTINUE = new MyHttpResponseStatus(100, "Continue", true);

    /**
     * 101 Switching Protocols
     */
    public static final MyHttpResponseStatus SWITCHING_PROTOCOLS =
            new MyHttpResponseStatus(101, "Switching Protocols", true);

    /**
     * 102 Processing (WebDAV, RFC2518)
     */
    public static final MyHttpResponseStatus PROCESSING = new MyHttpResponseStatus(102, "Processing", true);

    /**
     * 200 OK
     */
    public static final MyHttpResponseStatus OK = new MyHttpResponseStatus(200, "OK", true);

    /**
     * 201 Created
     */
    public static final MyHttpResponseStatus CREATED = new MyHttpResponseStatus(201, "Created", true);

    /**
     * 202 Accepted
     */
    public static final MyHttpResponseStatus ACCEPTED = new MyHttpResponseStatus(202, "Accepted", true);

    /**
     * 203 Non-Authoritative Information (since HTTP/1.1)
     */
    public static final MyHttpResponseStatus NON_AUTHORITATIVE_INFORMATION =
            new MyHttpResponseStatus(203, "Non-Authoritative Information", true);

    /**
     * 204 No Content
     */
    public static final MyHttpResponseStatus NO_CONTENT = new MyHttpResponseStatus(204, "No Content", true);

    /**
     * 205 Reset Content
     */
    public static final MyHttpResponseStatus RESET_CONTENT = new MyHttpResponseStatus(205, "Reset Content", true);

    /**
     * 206 Partial Content
     */
    public static final MyHttpResponseStatus PARTIAL_CONTENT = new MyHttpResponseStatus(206, "Partial Content", true);

    /**
     * 207 Multi-Status (WebDAV, RFC2518)
     */
    public static final MyHttpResponseStatus MULTI_STATUS = new MyHttpResponseStatus(207, "Multi-Status", true);

    /**
     * 300 Multiple Choices
     */
    public static final MyHttpResponseStatus MULTIPLE_CHOICES = new MyHttpResponseStatus(300, "Multiple Choices", true);

    /**
     * 301 Moved Permanently
     */
    public static final MyHttpResponseStatus MOVED_PERMANENTLY = new MyHttpResponseStatus(301, "Moved Permanently", true);

    /**
     * 302 Found
     */
    public static final MyHttpResponseStatus FOUND = new MyHttpResponseStatus(302, "Found", true);

    /**
     * 303 See Other (since HTTP/1.1)
     */
    public static final MyHttpResponseStatus SEE_OTHER = new MyHttpResponseStatus(303, "See Other", true);

    /**
     * 304 Not Modified
     */
    public static final MyHttpResponseStatus NOT_MODIFIED = new MyHttpResponseStatus(304, "Not Modified", true);

    /**
     * 305 Use Proxy (since HTTP/1.1)
     */
    public static final MyHttpResponseStatus USE_PROXY = new MyHttpResponseStatus(305, "Use Proxy", true);

    /**
     * 307 Temporary Redirect (since HTTP/1.1)
     */
    public static final MyHttpResponseStatus TEMPORARY_REDIRECT = new MyHttpResponseStatus(307, "Temporary Redirect", true);

    /**
     * 400 Bad Request
     */
    public static final MyHttpResponseStatus BAD_REQUEST = new MyHttpResponseStatus(400, "Bad Request", true);

    /**
     * 401 Unauthorized
     */
    public static final MyHttpResponseStatus UNAUTHORIZED = new MyHttpResponseStatus(401, "Unauthorized", true);

    /**
     * 402 Payment Required
     */
    public static final MyHttpResponseStatus PAYMENT_REQUIRED = new MyHttpResponseStatus(402, "Payment Required", true);

    /**
     * 403 Forbidden
     */
    public static final MyHttpResponseStatus FORBIDDEN = new MyHttpResponseStatus(403, "Forbidden", true);

    /**
     * 404 Not Found
     */
    public static final MyHttpResponseStatus NOT_FOUND = new MyHttpResponseStatus(404, "Not Found", true);

    /**
     * 405 Method Not Allowed
     */
    public static final MyHttpResponseStatus METHOD_NOT_ALLOWED = new MyHttpResponseStatus(405, "Method Not Allowed", true);

    /**
     * 406 Not Acceptable
     */
    public static final MyHttpResponseStatus NOT_ACCEPTABLE = new MyHttpResponseStatus(406, "Not Acceptable", true);

    /**
     * 407 Proxy Authentication Required
     */
    public static final MyHttpResponseStatus PROXY_AUTHENTICATION_REQUIRED =
            new MyHttpResponseStatus(407, "Proxy Authentication Required", true);

    /**
     * 408 Request Timeout
     */
    public static final MyHttpResponseStatus REQUEST_TIMEOUT = new MyHttpResponseStatus(408, "Request Timeout", true);

    /**
     * 409 Conflict
     */
    public static final MyHttpResponseStatus CONFLICT = new MyHttpResponseStatus(409, "Conflict", true);

    /**
     * 410 Gone
     */
    public static final MyHttpResponseStatus GONE = new MyHttpResponseStatus(410, "Gone", true);

    /**
     * 411 Length Required
     */
    public static final MyHttpResponseStatus LENGTH_REQUIRED = new MyHttpResponseStatus(411, "Length Required", true);

    /**
     * 412 Precondition Failed
     */
    public static final MyHttpResponseStatus PRECONDITION_FAILED =
            new MyHttpResponseStatus(412, "Precondition Failed", true);

    /**
     * 413 Request Entity Too Large
     */
    public static final MyHttpResponseStatus REQUEST_ENTITY_TOO_LARGE =
            new MyHttpResponseStatus(413, "Request Entity Too Large", true);

    /**
     * 414 Request-URI Too Long
     */
    public static final MyHttpResponseStatus REQUEST_URI_TOO_LONG =
            new MyHttpResponseStatus(414, "Request-URI Too Long", true);

    /**
     * 415 Unsupported Media Type
     */
    public static final MyHttpResponseStatus UNSUPPORTED_MEDIA_TYPE =
            new MyHttpResponseStatus(415, "Unsupported Media Type", true);

    /**
     * 416 Requested Range Not Satisfiable
     */
    public static final MyHttpResponseStatus REQUESTED_RANGE_NOT_SATISFIABLE =
            new MyHttpResponseStatus(416, "Requested Range Not Satisfiable", true);

    /**
     * 417 Expectation Failed
     */
    public static final MyHttpResponseStatus EXPECTATION_FAILED =
            new MyHttpResponseStatus(417, "Expectation Failed", true);

    /**
     * 422 Unprocessable Entity (WebDAV, RFC4918)
     */
    public static final MyHttpResponseStatus UNPROCESSABLE_ENTITY =
            new MyHttpResponseStatus(422, "Unprocessable Entity", true);

    /**
     * 423 Locked (WebDAV, RFC4918)
     */
    public static final MyHttpResponseStatus LOCKED =
            new MyHttpResponseStatus(423, "Locked", true);

    /**
     * 424 Failed Dependency (WebDAV, RFC4918)
     */
    public static final MyHttpResponseStatus FAILED_DEPENDENCY = new MyHttpResponseStatus(424, "Failed Dependency", true);

    /**
     * 425 Unordered Collection (WebDAV, RFC3648)
     */
    public static final MyHttpResponseStatus UNORDERED_COLLECTION =
            new MyHttpResponseStatus(425, "Unordered Collection", true);

    /**
     * 426 Upgrade Required (RFC2817)
     */
    public static final MyHttpResponseStatus UPGRADE_REQUIRED = new MyHttpResponseStatus(426, "Upgrade Required", true);

    /**
     * 428 Precondition Required (RFC6585)
     */
    public static final MyHttpResponseStatus PRECONDITION_REQUIRED =
            new MyHttpResponseStatus(428, "Precondition Required", true);

    /**
     * 429 Too Many Requests (RFC6585)
     */
    public static final MyHttpResponseStatus TOO_MANY_REQUESTS = new MyHttpResponseStatus(429, "Too Many Requests", true);

    /**
     * 431 Request Header Fields Too Large (RFC6585)
     */
    public static final MyHttpResponseStatus REQUEST_HEADER_FIELDS_TOO_LARGE =
            new MyHttpResponseStatus(431, "Request Header Fields Too Large", true);

    /**
     * 500 Internal Server Error
     */
    public static final MyHttpResponseStatus INTERNAL_SERVER_ERROR =
            new MyHttpResponseStatus(500, "Internal Server Error", true);

    /**
     * 501 Not Implemented
     */
    public static final MyHttpResponseStatus NOT_IMPLEMENTED = new MyHttpResponseStatus(501, "Not Implemented", true);

    /**
     * 502 Bad Gateway
     */
    public static final MyHttpResponseStatus BAD_GATEWAY = new MyHttpResponseStatus(502, "Bad Gateway", true);

    /**
     * 503 Service Unavailable
     */
    public static final MyHttpResponseStatus SERVICE_UNAVAILABLE =
            new MyHttpResponseStatus(503, "Service Unavailable", true);

    /**
     * 504 Gateway Timeout
     */
    public static final MyHttpResponseStatus GATEWAY_TIMEOUT = new MyHttpResponseStatus(504, "Gateway Timeout", true);

    /**
     * 505 HTTP Version Not Supported
     */
    public static final MyHttpResponseStatus HTTP_VERSION_NOT_SUPPORTED =
            new MyHttpResponseStatus(505, "HTTP Version Not Supported", true);

    /**
     * 506 Variant Also Negotiates (RFC2295)
     */
    public static final MyHttpResponseStatus VARIANT_ALSO_NEGOTIATES =
            new MyHttpResponseStatus(506, "Variant Also Negotiates", true);

    /**
     * 507 Insufficient Storage (WebDAV, RFC4918)
     */
    public static final MyHttpResponseStatus INSUFFICIENT_STORAGE =
            new MyHttpResponseStatus(507, "Insufficient Storage", true);

    /**
     * 510 Not Extended (RFC2774)
     */
    public static final MyHttpResponseStatus NOT_EXTENDED = new MyHttpResponseStatus(510, "Not Extended", true);

    /**
     * 511 Network Authentication Required (RFC6585)
     */
    public static final MyHttpResponseStatus NETWORK_AUTHENTICATION_REQUIRED =
            new MyHttpResponseStatus(511, "Network Authentication Required", true);
}
