package com.james.chat.codec.http;

import io.netty.util.concurrent.FastThreadLocal;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 该DateFormat将3种类型的日期解析为Date，但是仅仅将Date编码为一种格式
 * 详情见RFC种关于日期类型的描述
 */
public class HttpHeaderDateFormat extends SimpleDateFormat {
    private static final long serialVersionUID = -925286159755905325L;

    private final SimpleDateFormat format1 = new HttpHeaderDateFormatObsolete1();
    private final SimpleDateFormat format2 = new HttpHeaderDateFormatObsolete2();

    private static final FastThreadLocal<HttpHeaderDateFormat> dateFormatThreadLocal =
            new FastThreadLocal<HttpHeaderDateFormat>() {
                @Override
                protected HttpHeaderDateFormat initialValue() throws Exception {
                    return new HttpHeaderDateFormat();
                }
            };

    public static HttpHeaderDateFormat get() {
        return dateFormatThreadLocal.get();
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        Date date = super.parse(text, pos);
        if (date == null) {
            date = format1.parse(text, pos);
        }
        if (date == null) {
            date = format2.parse(text, pos);
        }
        return date;
    }

    /**
     * 标准的日期格式
     * Sun, 06 Nov 1994 08:49:37 GMT -> E, d MMM yyyy HH:mm:ss z
     */
    private HttpHeaderDateFormat() {
        super("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * 非标准日期格式1
     */
    private static final class HttpHeaderDateFormatObsolete1 extends SimpleDateFormat {
        private static final long serialVersionUID = -3178072504225114298L;

        HttpHeaderDateFormatObsolete1() {
            super("E, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH);
            setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }

    /**
     * 非标准日期格式2
     */
    private static final class HttpHeaderDateFormatObsolete2 extends SimpleDateFormat {
        private static final long serialVersionUID = 3010674519968303714L;

        HttpHeaderDateFormatObsolete2() {
            super("E MMM d HH:mm:ss yyyy", Locale.ENGLISH);
            setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }
}
