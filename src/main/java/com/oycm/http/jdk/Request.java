package com.oycm.http.jdk;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

/**
 * @author ouyangcm
 * create 2024/11/29 14:51
 */
public final class Request {

    /**
     * 除了body, charset其余参数不能为空
     * @param method 请求方式
     * @param url 请求url地址
     * @param headers 请求头
     * @param body 请求体字节数组
     * @param charset 编码格式
     * @return Request
     */
    public static Request create(String method, String url, Map<String, Collection<String>> headers,
                                 byte[] body, Charset charset) {
        return new Request(method, url, headers, body, charset);
    }

    private final String method;
    private final String url;
    private final Map<String, Collection<String>> headers;
    private final byte[] body;
    private final Charset charset;

    Request(String method, String url, Map<String, Collection<String>> headers, byte[] body,
            Charset charset) {
        this.method = CheckUtils.checkNotNull(method, "method of %s", url);
        this.url = CheckUtils.checkNotNull(url, "url");
        this.headers = CheckUtils.checkNotNull(headers, "headers of %s %s", method, url);
        this.body = body;
        this.charset = charset;
    }

    public String method() {
        return method;
    }

    public String url() {
        return url;
    }

    public Map<String, Collection<String>> headers() {
        return headers;
    }

    /**
     * 请求body的编码格式
     * @return
     */
    public Charset charset() {
        return charset;
    }

    /**
     * 请求体
     * @return
     */
    public byte[] body() {
        return body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(method).append(' ').append(url).append(" HTTP/1.1\n");
        for (String field : headers.keySet()) {
            for (String value : CheckUtils.valuesOrEmpty(headers, field)) {
                builder.append(field).append(": ").append(value).append('\n');
            }
        }
        if (body != null) {
            builder.append('\n').append(charset != null ? new String(body, charset) : "Binary data");
        }
        return builder.toString();
    }

    // 控制每个请求的设置
    public static class Options {


        private static final Options DEFAULT = new Options();

        private final int connectTimeoutMillis;
        private final int readTimeoutMillis;

        public Options(int connectTimeoutMillis, int readTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
            this.readTimeoutMillis = readTimeoutMillis;
        }

        public Options() {
            this(10 * 1000, 60 * 1000);
        }

        /**
         * 默认10s超时时间，0表示不限制超时时间
         *
         * @see java.net.HttpURLConnection#getConnectTimeout()
         */
        public int connectTimeoutMillis() {
            return connectTimeoutMillis;
        }

        /**
         * 默认60s超时时间，0表示不限制超时时间
         *
         * @see java.net.HttpURLConnection#getReadTimeout()
         */
        public int readTimeoutMillis() {
            return readTimeoutMillis;
        }

        public static Options options() {
            return DEFAULT;
        }
    }
}
