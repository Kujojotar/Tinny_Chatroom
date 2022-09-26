package com.james.chat.codec.http;

import java.text.ParseException;
import java.util.*;

public class MyHttpHeaders implements Iterable<Map.Entry<String, String>>{

    // 这里参考HashMap的实现，与Netty源码实现有一些差别
    private static final int BUCKET_SIZE = 32;

    public static int index(int hash) {
        return hash & (BUCKET_SIZE-1);
    }

    private final HeaderEntry[] entries = new HeaderEntry[BUCKET_SIZE];
    private final HeaderEntry head = new HeaderEntry();
    protected final boolean validate;

    public MyHttpHeaders() {
        this(true);
    }

    public MyHttpHeaders(boolean validate) {
        this.validate = validate;
        head.before = head.after = head;
    }


    public MyHttpHeaders add(MyHttpHeaders headers) {
        if (headers == this) {
            throw new IllegalArgumentException("I cannot add myself :)!");
        }
        HeaderEntry e = headers.head.after;
        while (e != headers.head) {
            add(e.key, e.value);
            e = e.after;
        }
        return this;
    }

    public MyHttpHeaders add(final CharSequence name, final Object value) {
        CharSequence strVal;
        if (validate) {
            strVal = toCharSequence(value);
        } else {
            strVal = toCharSequence(value);
        }
        int h = hash(name);
        int i = index(h);
        add0(h, i, name, strVal);
        return this;
    }

    private void add0(int h, int i, final CharSequence name, final CharSequence value) {
        HeaderEntry e = entries[i];
        HeaderEntry newEntry;
        entries[i] = newEntry = new HeaderEntry(h, name, value);
        newEntry.next = e;
        newEntry.addBefore(head);
    }

    public MyHttpHeaders remove(final String name) {
        return remove((CharSequence) name);
    }

    public MyHttpHeaders remove(final CharSequence name) {
        if (name == null) {
            return null;
        }
        int h = hash(name);
        int i = index(h);
        remove0(h, i, name);
        return this;
    }

    private void remove0(int h, int i, CharSequence name) {
        HeaderEntry e = entries[i];
        if (e == null) {
            return ;
        }
        for (;;) {
            if (e.hash == h && equalsIgnoreCase(name, e.key)) {
                e.remove();
                HeaderEntry next = e.next;
                if (next != null) {
                    entries[i] = next;
                    e = next;
                } else {
                    entries[i] = null;
                    return;
                }
            } else {
                break;
            }
        }
        for (;;) {
            HeaderEntry next = e.next;
            if (next == null) {
                break;
            }
            if (next.hash == h && equalsIgnoreCase(name, next.key)) {
                e.next = next.next;
                next.remove();
            } else {
                e = next;
            }
        }
    }

    public MyHttpHeaders set(final String name, final Object value) {
        return set((CharSequence) name, value);
    }

    public MyHttpHeaders set(final CharSequence name, final Object value) {
        CharSequence strVal;
        if (validate) {
            validateHeaderName0(name);
            strVal = toCharSequence(value);
            validateHeaderValue(strVal);
        } else {
            strVal = toCharSequence(value);
        }
        int h = hash(name);
        int i = index(h);
        remove0(h, i, name);
        add0(h, i, name, strVal);
        return this;
    }

    public String get(final String name) {
        return get((CharSequence) name);
    }

    public String get(final CharSequence name) {
        if (name == null) {
            return null;
        }

        int h = hash(name);
        int i = index(h);
        HeaderEntry e = entries[i];
        CharSequence value = null;
        // loop until the first header was found
        while (e != null) {
            if (e.hash == h && equalsIgnoreCase(name, e.key)) {
                value = e.value;
            }

            e = e.next;
        }
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public List<String> getAll(final String name) {
        return getAll((CharSequence) name);
    }

    public List<String> getAll(final CharSequence name) {
        if (name == null) {
            return null;
        }

        LinkedList<String> values = new LinkedList<String>();

        int h = hash(name);
        int i = index(h);
        HeaderEntry e = entries[i];
        while (e != null) {
            if (e.hash == h && equalsIgnoreCase(name, e.key)) {
                values.addFirst(e.getValue());
            }
            e = e.next;
        }
        return values;
    }

    public boolean contains(String name) {
        return get(name) != null;
    }

    public boolean contains(CharSequence name) {
        return get(name) != null;
    }

    public boolean contains(String name, String value, boolean ignoreCaseValue) {
        return contains((CharSequence) name, (CharSequence) value, ignoreCaseValue);
    }

    public boolean contains(CharSequence name, CharSequence value, boolean ignoreCase) {
        if (name == null) {
            return false;
        }
        int h = hash(name);
        int i = index(h);
        HeaderEntry e = entries[i];
        while (e != null) {
            if (e.hash == h && equalsIgnoreCase(name, e.key)) {
                if (ignoreCase) {
                    if (equalsIgnoreCase(e.value, value)) {
                        return true;
                    }
                } else {
                    if (e.value.equals(value)) {
                        return true;
                    }
                }
            }
            e = e.next;
        }
        return false;
    }

    private static CharSequence toCharSequence(Object value) {
        if (value instanceof CharSequence) {
            return (CharSequence) value;
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Date) {
            return HttpHeaderDateFormat.get().format((Date) value);
        }
        if (value instanceof Calendar) {
            return HttpHeaderDateFormat.get().format(((Calendar) value).getTime());
        }
        return value.toString();
    }

    private static int hash(CharSequence name) {
        int h = 0;
        for (int i = name.length()-1;i >=0; i--) {
            char c = name.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 32;
            }
            h = 31*h + c;
        }
        if (h > 0) {
            return h;
        } else if (h == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return -h;
        }
    }

    protected void validateHeaderName0(CharSequence name) {

    }

    protected void validateHeaderValue(CharSequence name) {

    }

    public static boolean equalsIgnoreCase(CharSequence name1, CharSequence name2) {
        if (name1 == name2) {
            return true;
        }

        if (name1 == null || name2 == null) {
            return false;
        }

        int nameLen = name1.length();
        if (nameLen != name2.length()) {
            return false;
        }

        for (int i = nameLen - 1; i >= 0; i --) {
            char c1 = name1.charAt(i);
            char c2 = name2.charAt(i);
            if (c1 != c2) {
                if (c1 >= 'A' && c1 <= 'Z') {
                    c1 += 32;
                }
                if (c2 >= 'A' && c2 <= 'Z') {
                    c2 += 32;
                }
                if (c1 != c2) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new HeaderIterator();
    }

    private final class HeaderIterator implements Iterator<Map.Entry<String, String>> {

        private HeaderEntry current = head;

        @Override
        public boolean hasNext() {
            return current.after != head;
        }

        @Override
        public Map.Entry<String, String> next() {
            current = current.after;

            if (current == head) {
                throw new NoSuchElementException();
            }

            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class HeaderEntry implements Map.Entry<String, String> {
        final int hash;
        final CharSequence key;
        CharSequence value;
        HeaderEntry next;
        HeaderEntry before, after;

        HeaderEntry(int hash, CharSequence key, CharSequence value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        HeaderEntry() {
            hash = -1;
            key = null;
            value = null;
        }

        void remove() {
            before.after = after;
            after.before = before;
        }

        // 这个跟看起来有些差别，应该是将该节点插入到e的前面
        void addBefore(HeaderEntry e) {
            after = e;
            before = e.before;
            before.after = this;
            after.before = this;
        }

        @Override
        public String getKey() {
            return key.toString();
        }

        @Override
        public String getValue() {
            return value.toString();
        }

        @Override
        public String setValue(String value) {
            if (value == null) {
                throw new NullPointerException("value");
            }
            CharSequence oldValue = this.value;
            this.value = value;
            return oldValue.toString();
        }


        @Override
        public String toString() {
            return key.toString() + '=' + value.toString();
        }

    }

    /**
     * 返回HttpMessage是否可以keep-alive
     * 这个方法通过检查Connection头以及HttpVersion来判断是否默认支持持久连接
     */
    public static boolean isKeepAlive(MyHttpMessage message) {
        String connection = message.getHeaders().get(HttpHeaderCommons.Names.CONNECTION);
        if (connection != null && equalsIgnoreCase(HttpHeaderCommons.Values.CLOSE, connection)) {
            return false;
        }
        if (message.getVersion().isKeepAliveDefault()) {
            return !equalsIgnoreCase(HttpHeaderCommons.Values.CLOSE, connection);
        } else {
            return equalsIgnoreCase(HttpHeaderCommons.Values.KEEP_ALIVE, connection);
        }
    }

    /**
     * 尝试将HttpMessage设置为Keep-Alive
     */
    public static void setKeepAlive(MyHttpMessage message, boolean keepAlive) {
        if (message.getVersion().isKeepAliveDefault()) {
            if (keepAlive) {
                // 由于HTTP/1.1默认支持Keep-Alive，因此直接删除Connection头即可
                message.getHeaders().remove(HttpHeaderCommons.Names.CONNECTION);
            } else {
                message.getHeaders().set(HttpHeaderCommons.Names.CONNECTION, HttpHeaderCommons.Values.CLOSE);
            }
        } else {
            if (keepAlive) {
                message.getHeaders().set(HttpHeaderCommons.Names.CONNECTION, HttpHeaderCommons.Values.KEEP_ALIVE);
            } else {
                message.getHeaders().remove(HttpHeaderCommons.Names.CONNECTION);
            }
        }
    }

    public static Date getDateHeader(MyHttpMessage message, CharSequence name) throws ParseException {
        String value = message.getHeaders().get(name);
        if (value == null) {
            throw new ParseException("header not found: " + name, 0);
        }
        return HttpHeaderDateFormat.get().parse(value);
    }

    public static boolean isTransferEncodingChunked(MyHttpMessage message) {
        return message.getHeaders().contains(HttpHeaderCommons.Names.TRANSFER_ENCODING, HttpHeaderCommons.Values.CHUNKED, true);
    }

    public static long getContentLength(MyHttpMessage message, long defaultValue) {
        String contentLength = message.getHeaders().get(HttpHeaderCommons.Names.CONTENT_LENGTH);
        if (contentLength != null) {
            try {
                return Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    public static void removeTransferEncodingChunked(MyHttpMessage msg) {
        List<String> vals = msg.getHeaders().getAll(HttpHeaderCommons.Names.TRANSFER_ENCODING);
        if (vals.size() == 0) {
            msg.getHeaders().remove(HttpHeaderCommons.Names.TRANSFER_ENCODING);
            return ;
        }
        int index = vals.indexOf(HttpHeaderCommons.Values.CHUNKED);
        if (index != -1) {
            vals.remove(index);
        }
        msg.getHeaders().remove(HttpHeaderCommons.Names.TRANSFER_ENCODING);
        vals.forEach( x-> msg.getHeaders().set(HttpHeaderCommons.Names.TRANSFER_ENCODING, x));
    }

    public static boolean is100ContinueExpected(MyHttpMessage message) {
        if (!(message instanceof MyHttpRequest)) {
            return false;
        }

        // HTTP 100 仅仅在 HTTP/1.1以后有效
        if (!MyHttpVersion.HTTP_1_1.equals(message.getVersion())) {
            return false;
        }
        // 在大部分时候通过Expect字段获取信息
        String value = message.getHeaders().get(HttpHeaderCommons.Names.EXPECT);
        if (value == null) {
            return false;
        }
        return equalsIgnoreCase(value, HttpHeaderCommons.Values.CONTINUE);
    }

    public static boolean isContentLengthSet(MyHttpMessage message) {
        return message.getHeaders().contains(HttpHeaderCommons.Names.CONTENT_LENGTH);
    }

    public static String getHost(MyHttpMessage message) {
        return message.getHeaders().get(HttpHeaderCommons.Names.HOST);
    }


}
