package com.ocd.util;


/**
 * @author Administrator
 */
public class AesEncrypt {
    private static String keyString = "nz1tvnc0hzjqSqy";
    private static final int RANDOM_DIV = 81353130;


    private static byte[] getKeyString() {

        return parseHexStr2Byte(keyString);
    }

    public static String entryData(String inputData) throws Exception {

        byte[] res = AESCoder.encrypt(inputData.getBytes(), getKeyString());
        return parseByte2HexStr(res);
    }

    public static String entryData(byte[] inputData) throws Exception {

        byte[] res = AESCoder.encrypt(inputData, getKeyString());
        return parseByte2HexStr(res);
    }

    public static byte[] entryDataByte(byte[] inputData) throws Exception {

        byte[] res = AESCoder.encrypt(inputData, getKeyString());
        return res;
    }

    public static String decryptData(String data) {

        byte[] res;
        try {
            res = AESCoder.decrypt(parseHexStr2Byte(data), getKeyString());
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
        return new String(res);
    }

    /**
     * int整数转换为4字节的byte数组
     *
     * @param size
     * @return byte数组
     */
    public static byte[] randomToBytes(long size) {
        long random = (long) size;
        if (size < 0) {
            random = 1024L * 1024L * 1024L * 4L + (long) size;
        }

        long i = random % RANDOM_DIV;
        byte[] targets = new byte[16];
        targets[0] = (byte) (i & 0xFF);
        targets[1] = (byte) (i >> 8 & 0xFF);
        targets[2] = (byte) (i >> 16 & 0xFF);
        targets[3] = (byte) (i >> 24 & 0xFF);
        return targets;
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
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将十六进制转换成二进制
     *
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static final byte[] getInputBytes(String mac, String hwver, String appver, String binver, int module, long time) {
        int totalLen = mac.getBytes().length + hwver.getBytes().length + appver.getBytes().length + binver.getBytes().length + 4 + 8;



        return null;
    }

    public static byte[] longToBytesBE(long values) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }

    public static byte[] longToBytesLE(long values) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = i * 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }

    public static long bytesToLongBE(byte[] buffer) {
        long  values = 0;
        for (int i = 0; i < 8; i++) {
            values <<= 8; values|= (buffer[i] & 0xff);
        }
        return values;
    }

}