package com.oycm.http;

import com.oycm.http.jdk.Request;
import com.oycm.http.jdk.Response;

import java.io.IOException;

public interface Client {

    /**
     * 根据要求发送http请求
     * @param request 请求的信息
     * @param options 请求的连接配置信息
     * @return 请求的响应
     * @throws IOException 发起网络连接的异常
     */
    Response execute(Request request, Request.Options options) throws IOException;

}
