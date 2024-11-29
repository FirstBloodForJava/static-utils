package com.oycm.http.jdk;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author ouyangcm
 * create 2024/11/29 17:11
 */
public class Test {

    public static void main(String[] args) throws IOException {
        Request request = Request.create("GET", "", new HashMap<>(), null, null);

        JDKClient jdkClient = new JDKClient(null, null);

        Response execute = jdkClient.execute(request, Request.Options.options());

        System.out.println(execute);
    }
}
