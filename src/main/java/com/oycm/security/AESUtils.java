package com.oycm.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * @author ouyangcm
 * create 2025/7/22 15:33
 */
public class AESUtils {

    /**
     * AES 解密
     * @param content 密文
     * @param password 密钥
     * @return
     * @throws Exception
     */
    public static String decrypt(String content, String password) throws Exception {
        return manageContent(content, password, Cipher.DECRYPT_MODE);
    }

    /**
     * AES 加密
     * @param content 明文
     * @param password 密钥
     * @return
     * @throws Exception
     */
    public static String encrypt(String content, String password) throws Exception {
        return manageContent(content, password, Cipher.ENCRYPT_MODE);
    }

    /**
     *
     * @param content 明文/密文
     * @param password 密钥
     * @param cipherType 加解密类型: Cipher.DECRYPT_MODE 解密;  Cipher.ENCRYPT_MODE 加密
     * @return 明文 -> 密文; 密文 -> 明文
     * @throws Exception
     */
    private static String manageContent(String content, String password, int cipherType) throws Exception {
        byte[] contBytes;
        if (cipherType == Cipher.DECRYPT_MODE) {
            contBytes = DatatypeConverter.parseBase64Binary(content);
        } else {
            contBytes = content.getBytes(StandardCharsets.UTF_8);
        }
        //实例化
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        //使用密钥初始化，设置模式
        cipher.init(cipherType, getSecretKey(password.getBytes(StandardCharsets.UTF_8)));
        //操作
        byte[] cipherBytes = cipher.doFinal(contBytes);
        if (cipherType == Cipher.DECRYPT_MODE) {
            return new String(cipherBytes, StandardCharsets.UTF_8);
        } else {
            return DatatypeConverter.printBase64Binary(cipherBytes);
        }
    }

    /**
     *
     * @param pwd 密钥
     * @return 生成密钥
     * @throws Exception
     */
    private static SecretKeySpec getSecretKey(byte[] pwd) throws Exception {
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(pwd);
        //AES 要求密钥长度为 128
        kg.init(128, random);
        //生成一个密钥
        SecretKey secretKey = kg.generateKey();
        // 转换为AES专用密钥
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    public static void main(String[] args) throws Exception {

        String plaintext = "Hello World";
        String password = "674c2435107a4afe8b4b9456e919edc6";
        String ciphertext = encrypt(plaintext, password);
        System.out.println(ciphertext);
        System.out.println(decrypt(ciphertext, password));

    }
}
