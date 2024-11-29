package com.oycm.http.jdk;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

/**
 * @author ouyangcm
 * create 2024/11/29 15:44
 */
public class CharacterUtils {

    /**
     * 字节数组使用charset转字符串
     * @param data
     * @param charset
     * @param defaultValue
     * @return
     */
    public static String decodeOrDefault(byte[] data, Charset charset, String defaultValue) {
        if (data == null) {
            return defaultValue;
        }
        CheckUtils.checkNotNull(charset, "charset");
        try {
            return charset.newDecoder().decode(ByteBuffer.wrap(data)).toString();
        } catch (CharacterCodingException ex) {
            return defaultValue;
        }
    }
}
