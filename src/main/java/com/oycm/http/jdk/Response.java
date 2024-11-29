package com.oycm.http.jdk;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author ouyangcm
 * http请求的想要对象
 * create 2024/11/29 15:20
 */
public final class Response implements Closeable {

    private final int status;
    private final String reason;
    private final Map<String, Collection<String>> headers;
    private final Body body;
    private final Request request;

    private Response(Builder builder) {
        // status(状态码) < 200 异常
        CheckUtils.checkState(builder.status >= 200, "Invalid status code: %s", builder.status);
        this.status = builder.status;
        this.reason = builder.reason;
        this.headers = Collections.unmodifiableMap(caseInsensitiveCopyOf(builder.headers));
        this.body = builder.body;
        this.request = builder.request;
    }


    public Builder toBuilder(){
        return new Builder(this);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static final class Builder {
        int status;
        String reason;
        Map<String, Collection<String>> headers;
        Body body;
        Request request;

        Builder() {
        }

        Builder(Response source) {
            this.status = source.status;
            this.reason = source.reason;
            this.headers = source.headers;
            this.body = source.body;
            this.request = source.request;
        }


        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder headers(Map<String, Collection<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(Body body) {
            this.body = body;
            return this;
        }

        public Builder body(InputStream inputStream, Integer length) {
            this.body = InputStreamBody.orNull(inputStream, length);
            return this;
        }


        public Builder body(byte[] data) {
            this.body = ByteArrayBody.orNull(data);
            return this;
        }


        public Builder body(String text, Charset charset) {
            this.body = ByteArrayBody.orNull(text, charset);
            return this;
        }

        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }

    /**
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
     * 状态码
     * @return
     */
    public int status() {
        return status;
    }


    /**
     * https://github.com/http2/http2-spec/issues/202
     * @return
     */
    public String reason() {
        return reason;
    }


    public Map<String, Collection<String>> headers() {
        return headers;
    }


    public Body body() {
        return body;
    }


    public Request request() {
        return request;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("HTTP/1.1 ").append(status);
        if (reason != null) builder.append(' ').append(reason);
        builder.append('\n');
        for (String field : headers.keySet()) {
            for (String value : CheckUtils.valuesOrEmpty(headers, field)) {
                builder.append(field).append(": ").append(value).append('\n');
            }
        }
        if (body != null) builder.append('\n').append(body);
        return builder.toString();
    }

    @Override
    public void close() {
        Util.ensureClosed(body);
    }

    public interface Body extends Closeable {

        /**
         * 有值的情况下是字节的长度，为null的情况是未知或大于Integer.MAX_VALUE(2GB)
         * @return
         */
        Integer length();

        /**
         * 是否可重复读取的流
         * @return
         */
        boolean isRepeatable();


        InputStream asInputStream() throws IOException;


        Reader asReader() throws IOException;
    }

    private static final class InputStreamBody implements Response.Body {

        private final InputStream inputStream;
        private final Integer length;
        private InputStreamBody(InputStream inputStream, Integer length) {
            this.inputStream = inputStream;
            this.length = length;
        }

        private static Body orNull(InputStream inputStream, Integer length) {
            if (inputStream == null) {
                return null;
            }
            return new InputStreamBody(inputStream, length);
        }

        @Override
        public Integer length() {
            return length;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public InputStream asInputStream() throws IOException {
            return inputStream;
        }

        @Override
        public Reader asReader() throws IOException {
            return new InputStreamReader(inputStream, Constants.UTF_8);
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }

    private static final class ByteArrayBody implements Response.Body {

        private final byte[] data;

        public ByteArrayBody(byte[] data) {
            this.data = data;
        }

        private static Body orNull(byte[] data) {
            if (data == null) {
                return null;
            }
            return new ByteArrayBody(data);
        }

        private static Body orNull(String text, Charset charset) {
            if (text == null) {
                return null;
            }
            CheckUtils.checkNotNull(charset, "charset");
            return new ByteArrayBody(text.getBytes(charset));
        }

        @Override
        public Integer length() {
            return data.length;
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public InputStream asInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        @Override
        public Reader asReader() throws IOException {
            return new InputStreamReader(asInputStream(), Constants.UTF_8);
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public String toString() {
            return CharacterUtils.decodeOrDefault(data, Constants.UTF_8, "Binary data");
        }
    }

    /**
     *
     * @param headers
     * @return 复制headers到不区分大小写可排序的TreeMap,key全部转小写
     */
    private static Map<String, Collection<String>> caseInsensitiveCopyOf(Map<String, Collection<String>> headers) {
        // 创建一个可排序的map 排序的key不区分大小写
        Map<String, Collection<String>> result = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            if (!result.containsKey(headerName)) {
                // key全部转小写
                result.put(headerName.toLowerCase(Locale.ROOT), new LinkedList<String>());
            }
            result.get(headerName).addAll(entry.getValue());
        }
        return result;
    }
}

