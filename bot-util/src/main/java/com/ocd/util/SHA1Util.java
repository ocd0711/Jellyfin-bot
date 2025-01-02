package com.ocd.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SHA1Util {
    private static String SD_KEY= "sd_110_key";
    private static Map<String, String> MAP = new HashMap<>();
    private SHA1Util() {
    }

    static {
        MAP.put("11000", SD_KEY);
        MAP.put("12000", "isentech_3348_key");
    }

    private static final Logger log = LoggerFactory.getLogger(SHA1Util.class);
    /**
     * SHA1 安全加密算法
     * @param maps 参数key-value map集合
     * @return
     * @throws DigestException
     */
    public static String SHA1(Map<String,Object> maps) throws DigestException {
        //获取信息摘要 - 参数字典排序后字符串
        String decrypt = getOrderByLexicographic(maps);
        try {
            //指定sha1算法
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(decrypt.getBytes());
            //获取字节数组
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString().toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            log.error("error", e);
            throw new DigestException("签名错误！");
        }
    }
    /**
     * 获取参数的字典排序
     * @param maps 参数key-value map集合
     * @return String 排序后的字符串
     */
    private static String getOrderByLexicographic(Map<String,Object> maps) {
        return splitParams(maps);
    }
    /**
     * 拼接排序好的参数名称和参数值
     * @param maps 参数key-value map集合
     * @return String 拼接后的字符串
     */
    private static String splitParams(Map<String,Object> maps) {
        StringBuilder paramStr = new StringBuilder();
        List<String> paramNames = new ArrayList<>(maps.keySet());
        Collections.sort(paramNames);
        String appId = (String)maps.get("appId");
        for (String paramName : paramNames) {
            paramStr.append(paramName);
            Object val = maps.get(paramName);
            if(null != val) {
                paramStr.append("=" + String.valueOf(val) + "&");
            }

        }
        String key = MAP.get(appId) == null ? SD_KEY : MAP.get(appId);
        paramStr.append("key=" + key);
        return paramStr.toString();
    }

}
