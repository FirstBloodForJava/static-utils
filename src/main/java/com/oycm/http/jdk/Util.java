package com.oycm.http.jdk;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author ouyangcm
 * create 2024/11/29 15:48
 */
public class Util {

    public static void ensureClosed(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // NOOP
                // 注释表示就是无操作
            }
        }
    }
}
