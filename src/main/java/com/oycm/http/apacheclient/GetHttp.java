package com.oycm.http.apacheclient;

public class GetHttp {

    public static void main(String[] args) {
        String url = "http://127.0.0.1:8080/getCus";

        String result = ApacheHttpClientUtils.sendHttpGet(url);

        System.out.println(result);
    }

}
