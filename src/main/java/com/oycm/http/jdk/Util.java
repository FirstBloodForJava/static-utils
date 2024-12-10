package com.oycm.http.jdk;

import org.springframework.util.Assert;

import java.io.*;

/**
 * @author ouyangcm
 * create 2024/11/29 15:48
 */
public class Util {

    public static final int BUFFER_SIZE = 4096;

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

    public static byte[] responseToByte(Response response) throws IOException {
        if (response == null) {
            return new byte[0];
        }
        if (response.body() == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(response.body().asInputStream(), out);
        response.close();
        return out.toByteArray();
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        Assert.notNull(in, "No InputStream specified");
        Assert.notNull(out, "No OutputStream specified");

        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }


}
