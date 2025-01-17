package com.oycm.http.apacheclient;

import feign.Request;
import feign.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.pool.PoolStats;
import org.apache.http.util.EntityUtils;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author ouyangcm
 * create 2024/12/4 14:56
 */
public class ApacheHttpClientUtils {
    private final static Log log = LogFactory.getLog(ApacheHttpClientUtils.class);
    private static final String ACCEPT_HEADER_NAME = "Accept";

    private static final ContentType APPLICATION_FORM_URLENCODED = ContentType.create("application/x-www-form-urlencoded", StandardCharsets.UTF_8);

    private static final Timer connectionManagerTimer = new Timer(
            "ApacheHttpClientUtils.connectionManager", true);

    private static volatile CloseableHttpClient httpClient;

    public static final Request.Options DEFAULT_OPTIONS = new Request.Options(60000, 60000);

    public static HttpClientConnectionManager connectionManager(
            FeignHttpClientProperties httpClientProperties) {

        httpClientProperties.setTimeToLive(30);

        ApacheHttpClientConnectionManagerFactory connectionManagerFactory = new DefaultApacheHttpClientConnectionManagerFactory();

        // 管理连接池
        final HttpClientConnectionManager connectionManager = connectionManagerFactory
                .newConnectionManager(
                        httpClientProperties.isDisableSslValidation(),
                        httpClientProperties.getMaxConnections(),
                        httpClientProperties.getMaxConnectionsPerRoute(),
                        httpClientProperties.getTimeToLive(), // 连接存活的时间
                        httpClientProperties.getTimeToLiveUnit(), // 时间单位
                        null);

        connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
        connectionManagerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                PoolStats poolStats = ((PoolingHttpClientConnectionManager)connectionManager).getTotalStats();

                System.out.println(Thread.currentThread().getName() + "-before: " + JacksonUtils.objectToJson(poolStats));

                //connectionManager.closeExpiredConnections();

                System.out.println(Thread.currentThread().getName() + "-after: " + JacksonUtils.objectToJson(poolStats));
            }
        }, 3000, httpClientProperties.getConnectionTimerRepeat());
        return connectionManager;
    }

    /**
     * 双重检测锁
     * @return
     */
    public static CloseableHttpClient httpClient() {

        if (httpClient == null) {
            synchronized (ApacheHttpClientUtils.class) {
                if (httpClient == null) {
                    FeignHttpClientProperties httpClientProperties = new FeignHttpClientProperties();


                    HttpClientConnectionManager httpClientConnectionManager = connectionManager(httpClientProperties);

                    ApacheHttpClientFactory httpClientFactory =  new DefaultApacheHttpClientFactory(HttpClientBuilder.create());

                    RequestConfig defaultRequestConfig = RequestConfig.custom()
                            .setConnectTimeout(60000)
                            .setRedirectsEnabled(httpClientProperties.isFollowRedirects())
                            .build();

                    httpClient = httpClientFactory.createBuilder().
                            setConnectionManager(httpClientConnectionManager).
                            setDefaultRequestConfig(defaultRequestConfig).build();
                }
            }
        }

        return httpClient;
    }

    private static HttpUriRequest buildHttpUriRequest(String url, String method, Map<String, Collection<String>> requestHeaders,
                                               Map<String,String> queryParams, byte[] body, Request.Options options) throws URISyntaxException {
        RequestBuilder requestBuilder = RequestBuilder.create(method);

        //设置每个请求的超时时间
        if (options != null) {
            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setConnectTimeout(options.connectTimeoutMillis())
                    .setSocketTimeout(options.readTimeoutMillis())
                    .build();
            requestBuilder.setConfig(requestConfig);
        }

        URI uri = new URIBuilder(url).build();

        requestBuilder.setUri(uri.getScheme() + "://" + uri.getAuthority() + uri.getRawPath());

        // 解析url后面的请求参数
        List<NameValuePair> urlQueryParams = URLEncodedUtils.parse(uri, requestBuilder.getCharset().name());
        for (NameValuePair queryParam: urlQueryParams) {
            requestBuilder.addParameter(queryParam);
        }

        // 设置请求参数
        if (queryParams != null && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> keyValue : queryParams.entrySet()) {
                requestBuilder.addParameter(new BasicNameValuePair(keyValue.getKey(), keyValue.getValue()));
            }
        }

        // 请求头处理
        boolean hasAcceptHeader = false;
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            for (Map.Entry<String, Collection<String>> headerEntry : requestHeaders.entrySet()) {
                String headerName = headerEntry.getKey();
                if (headerName.equalsIgnoreCase(ACCEPT_HEADER_NAME)) {
                    hasAcceptHeader = true;
                }

                if (headerName.equalsIgnoreCase(Util.CONTENT_LENGTH)) {
                    // The 'Content-Length' header is always set by the Apache client and it doesn't like us to set it as well.
                    // 不能设置请求头Content-Length的值
                    continue;
                }

                for (String headerValue : headerEntry.getValue()) {
                    requestBuilder.addHeader(headerName, headerValue);
                }
            }
        }

        // some servers choke on the default accept string, so we'll set it to anything
        // 没有设置Accept头，则设置为*/*
        if (!hasAcceptHeader) {
            requestBuilder.addHeader(ACCEPT_HEADER_NAME, "*/*");
        }

        // 请求体
        if (body != null) {
            requestBuilder.setEntity(new ByteArrayEntity(body));
        }else {
            requestBuilder.setEntity(new ByteArrayEntity(new byte[0]));
        }

        return requestBuilder.build();
    }

    private static ContentType getContentType(Request request) {
        ContentType contentType = ContentType.DEFAULT_TEXT;
        for (Map.Entry<String, Collection<String>> entry : request.headers().entrySet())
            if (entry.getKey().equalsIgnoreCase("Content-Type")) {
                Collection<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    contentType = ContentType.parse(values.iterator().next());
                    if (contentType.getCharset() == null) {
                        contentType = contentType.withCharset(request.charset());
                    }
                    break;
                }
            }
        return contentType;
    }

    public static String sendHttpGet(String url) {

        return sendHttpGet(url, null, null, null);
    }

    public static String sendHttpGet(String url, Map<String, Collection<String>> requestHeaders) {

        return sendHttpGet(url, requestHeaders, null, null);
    }

    public static String sendHttpGet(String url, Map<String, Collection<String>> requestHeaders, Map<String,String> queryParams) {

        return sendHttpGet(url, requestHeaders, queryParams, null);
    }

    public static String sendHttpGet(String url, Map<String, Collection<String>> requestHeaders, Map<String,String> queryParams, Map<String, String> requestBody) {
        String errResponse = null;
        CloseableHttpClient client = httpClient();
        byte[] body = null;
        if (requestBody != null) {

            body = JacksonUtils.objectToJson(requestBody).getBytes(StandardCharsets.UTF_8);

            // 设置请求头
            if (requestHeaders == null) {
                requestHeaders = new HashMap<>();

                List<String> contentType = new ArrayList<>();
                contentType.add("application/json");
                requestHeaders.put("Content-Type", contentType);
            }else {

            }
        }

        try (CloseableHttpResponse response = client.execute(buildHttpUriRequest(url, "GET", requestHeaders, queryParams, body, DEFAULT_OPTIONS))){
            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            errResponse = e.getMessage();
        } catch (URISyntaxException e) {
            errResponse = e.getMessage();
            log.error("url解析错误", e);
        }

        return errResponse;
    }

    public static String sendHttpPost(String url, Map<String, Collection<String>> requestHeaders, Map<String, String> requestBody) {
        String errResponse;
        CloseableHttpClient client = httpClient();
        byte[] body = null;
        if (requestBody != null && !requestBody.isEmpty()) {

            body = JacksonUtils.objectToJson(requestBody).getBytes(StandardCharsets.UTF_8);

            // 设置请求头
            if (requestHeaders == null) {
                requestHeaders = new HashMap<>();

                List<String> contentType = new ArrayList<>();
                contentType.add("application/json");
                requestHeaders.put("Content-Type", contentType);
            }else {
                boolean hasContentType = false;
                for (Map.Entry<String, Collection<String>> keyValue : requestHeaders.entrySet()) {
                    if ("Content-Type".equalsIgnoreCase(keyValue.getKey())) {
                        keyValue.getValue().clear();
                        keyValue.getValue().add("application/json");
                        break;
                    }
                }
                if (!hasContentType) {
                    List<String> contentType = new ArrayList<>();
                    contentType.add("application/json");
                    requestHeaders.put("Content-Type", contentType);
                }
            }
        }
        try (CloseableHttpResponse response = client.execute(buildHttpUriRequest(url, "POST", requestHeaders, null, body, DEFAULT_OPTIONS))){
            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            errResponse = e.getMessage();
        } catch (URISyntaxException e) {
            errResponse = e.getMessage();
            log.error("url解析错误", e);
        }

        return errResponse;
    }

    public static String sentHttpFormPost(String url, Map<String, Collection<String>> requestHeaders, Map<String, String> paramMap) {
        String errResponse;
        CloseableHttpClient client = httpClient();
        byte[] body = null;
        if (paramMap != null && !paramMap.isEmpty()) {

            List<NameValuePair> nameValuePairs = new ArrayList<>();

            for (Map.Entry<String, String> keyValue : paramMap.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(keyValue.getKey(), keyValue.getValue()));
            }

            try {
                body = EntityUtils.toByteArray(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error("", e);
                return "参数转流失败";
            }

            // 设置请求头
            if (requestHeaders == null) {
                requestHeaders = new HashMap<>();

                List<String> contentType = new ArrayList<>();
                contentType.add("application/x-www-form-urlencoded");
                requestHeaders.put("Content-Type", contentType);
            }else {
                boolean hasContentType = false;
                for (Map.Entry<String, Collection<String>> keyValue : requestHeaders.entrySet()) {
                    if ("Content-Type".equalsIgnoreCase(keyValue.getKey())) {
                        keyValue.getValue().clear();
                        keyValue.getValue().add("application/x-www-form-urlencoded");
                        break;
                    }
                }
                if (!hasContentType) {
                    List<String> contentType = new ArrayList<>();
                    contentType.add("application/x-www-form-urlencoded");
                    requestHeaders.put("Content-Type", contentType);
                }
            }
        }
        try (CloseableHttpResponse response = client.execute(buildHttpUriRequest(url, "POST", requestHeaders, null, body, DEFAULT_OPTIONS))){
            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            errResponse = e.getMessage();
        } catch (URISyntaxException e) {
            errResponse = e.getMessage();
            log.error("url解析错误", e);
        }

        return errResponse;
    }



    public static void destroy() throws Exception {
        connectionManagerTimer.cancel();
        if(httpClient != null) {
            httpClient.close();
        }
    }
}
