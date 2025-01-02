package com.ocd.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;

public abstract class AESCoder {
    /** 秘钥算法 */
    public static final String KEY_ALGORITHM = "AES";

    /**
     * 加密/加密算法 /工作模式/填充模式 java 6支持PKCS5PADDING填充模式 Bouncy Castle Bouncy
     * Castle支持PKCS7Padding填充方式 -------- 工作模式：ECB.CBC.PCBC,CTR,CTS,CFB等 -------
     * 填充方式包括 PKCS5Padding，NoPadding,ISO10126Padding
     */
    public static final String CIPHER_ALGORITHM = "AES/ECB/NoPadding";

    /**
     * 转换秘钥
     *
     * @param key
     *            二进制密钥
     * @return secretKey 密钥
     * @throws Exception
     */
    private static Key toKey(byte[] key) throws Exception {

        SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
        return secretKey;
    }

    /**
     * 解密数据
     *
     * @param data
     *            待解密数据
     * @param key
     *            密钥
     * @return byte[] 解密数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 还原密钥
        Key k = toKey(key);
        /**
         * 实例化 使用PKCS7Padding 填充方式，按如下方式实现：
         * Cipher.getInstence(CIPHER_ALGORITHM,"BC");
         */
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k);
        return cipher.doFinal(data);
    }

    /**
     * 解密数据
     *
     * @param data
     *            待解密数据
     * @param key
     *            密钥
     * @return byte[] 解密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 还原密钥
        Key k = toKey(key);
        /**
         * 实例化 使用PKCS7Padding 填充方式，按如下方式实现：
         * Cipher.getInstence(CIPHER_ALGORITHM,"BC");
         */
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k);
        return cipher.doFinal(data);
    }

    /**
     * 解密数据
     *
     * @param content
     *            待解密数据
     * @param password
     *            密钥
     * @return byte[] 解密数据
     * @throws Exception
     */
    public static byte[] encrypt(String content, String password) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom(password.getBytes()));
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");// 创建密码器
        byte[] byteContent = content.getBytes("utf-8");
        cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
        byte[] result = cipher.doFinal(byteContent);
        return result; // 加密
    }

    /**
     * 生成密钥
     *
     * @return byte[] 二进制密钥
     * @throws Exception
     */
    public static byte[] initKey(String keyPass) throws Exception {
        // 实例化
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        // AES 要求密钥长度为128,192,256位
        // Provider p = Security.getProvider("BC");
        SecureRandom sec = SecureRandom.getInstance("SHA1PRNG","Crypto");
        sec.setSeed(keyPass.getBytes());
        kg.init(128, sec);

        // kg.init(128, new SecureRandom(keyPass.getBytes()));
        // 生成秘密密钥
        SecretKey secretKey = kg.generateKey();
        // 获得密钥的二进制编码形式
//		MyLog.d("TAG", "initKey - " + parseByte2HexStr(secretKey.getEncoded()));
        return secretKey.getEncoded();
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            // System.out.println(buf[i]);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
}

