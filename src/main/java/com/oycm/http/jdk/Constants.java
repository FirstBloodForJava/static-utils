package com.oycm.http.jdk;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author ouyangcm
 * create 2024/11/29 15:28
 */
public class Constants {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static final String CONTENT_ENCODING = "Content-Encoding";

    public static final String ENCODING_GZIP = "gzip";

    public static final String ENCODING_DEFLATE = "deflate";

    public static final String CONTENT_LENGTH = "Content-Length";
}
