package com.oycm.http.jdk;

import com.oycm.http.Client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author ouyangcm
 * create 2024/11/29 15:55
 */
public class JDKClient implements Client {

    private final SSLSocketFactory sslContextFactory;
    private final HostnameVerifier hostnameVerifier;

    public JDKClient(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
        this.sslContextFactory = sslContextFactory;
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {

        HttpURLConnection connection = convertAndSend(request, options);

        return convertResponse(connection).toBuilder().request(request).build();
    }

    HttpURLConnection convertAndSend(Request request, Request.Options options) throws IOException {
        // 创建HttpURLConnection
        final HttpURLConnection connection = (HttpURLConnection) new URL(request.url()).openConnection();

        // https处理
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection sslCon = (HttpsURLConnection) connection;
            if (sslContextFactory != null) {
                sslCon.setSSLSocketFactory(sslContextFactory);
            }
            if (hostnameVerifier != null) {
                sslCon.setHostnameVerifier(hostnameVerifier);
            }
        }
        // 连接超时时间
        connection.setConnectTimeout(options.connectTimeoutMillis());
        // 连接后到成功响应的超时时间
        connection.setReadTimeout(options.readTimeoutMillis());

        // 是否弹出交互界面
        connection.setAllowUserInteraction(false);

        // true 自动处理重定向
        connection.setInstanceFollowRedirects(true);
        // 设置请求方式
        connection.setRequestMethod(request.method());

        // 获取请求头的key=Content-Encoding的values，表明请求体的压缩方式
        Collection<String> contentEncodingValues = request.headers().get(Constants.CONTENT_ENCODING);

        // values包含gzip，请求体采用gzip的压缩方式
        boolean gzipEncodedRequest =
                contentEncodingValues != null && contentEncodingValues.contains(Constants.ENCODING_GZIP);
        // values包含 deflate
        boolean deflateEncodedRequest =
                contentEncodingValues != null && contentEncodingValues.contains(Constants.ENCODING_DEFLATE);

        // 请求头中是否有key=Accept(客户端接收服务端响应内容类型)
        boolean hasAcceptHeader = false;

        Integer contentLength = null;

        // addRequestProperty(String key, String value) 添加请求头属性
        for (String field : request.headers().keySet()) {
            if (field.equalsIgnoreCase("Accept")) {
                hasAcceptHeader = true;
            }
            for (String value : request.headers().get(field)) {

                // 请求头中key=Content-Length
                if (field.equals(Constants.CONTENT_LENGTH)) {
                    // 没有压缩方式, 获取请求头的Content-Length的value
                    if (!gzipEncodedRequest && !deflateEncodedRequest) {
                        contentLength = Integer.valueOf(value);
                        connection.addRequestProperty(field, value);
                    }
                } else {
                    connection.addRequestProperty(field, value);
                }
            }
        }
        // 没有设置的默认配置
        if (!hasAcceptHeader) {
            connection.addRequestProperty("Accept", "*/*");
        }

        if (request.body() != null) {
            if (contentLength != null) {
                // 设置请求体的长度
                connection.setFixedLengthStreamingMode(contentLength);
            } else {
                // 不知道固定的请全体长度
                connection.setChunkedStreamingMode(8196);
            }

            // 默认doOutput=false，设为true表示请求将携带请全体，可以通过getOutputStream()获取请求体的输出流
            connection.setDoOutput(true);

            // 获取到请求体的输出流，如果服务端只需要请求参数(requestParam)，则服务端已经开始处理请求了(Debug)
            // 没有请求体，则connection.getResponseCode()调用时才算建立连接
            OutputStream out = connection.getOutputStream();
            if (gzipEncodedRequest) {
                // gzip
                out = new GZIPOutputStream(out);
            } else if (deflateEncodedRequest) {
                // deflate
                out = new DeflaterOutputStream(out);
            }
            try {
                out.write(request.body());
            } finally {
                try {
                    out.close();
                } catch (IOException suppressed) {
                    // NOOP
                }
            }
        }
        return connection;
    }

    Response convertResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        String reason = connection.getResponseMessage();

        if (status < 0) {
            throw new IOException(CheckUtils.format("Invalid status(%s) executing %s %s", status,
                    connection.getRequestMethod(), connection.getURL()));
        }

        Map<String, Collection<String>> headers = new LinkedHashMap<String, Collection<String>>();
        // 获取响应头
        for (Map.Entry<String, List<String>> field : connection.getHeaderFields().entrySet()) {
            // response message
            if (field.getKey() != null) {
                headers.put(field.getKey(), field.getValue());
            }
        }

        Integer length = connection.getContentLength();
        if (length == -1) {
            length = null;
        }
        InputStream stream;
        if (status >= 400) {
            stream = connection.getErrorStream();
        } else {
            // 获取响应体的输入流
            stream = connection.getInputStream();
        }
        return Response.builder()
                .status(status)
                .reason(reason)
                .headers(headers)
                .body(stream, length)
                .build();
    }
}
