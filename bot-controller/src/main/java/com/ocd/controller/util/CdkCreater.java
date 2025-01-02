package com.ocd.controller.util;

import java.util.*;

/**
 * @author OCD
 * @date 2023/02/16 2:32 PM
 * Description:
 */
public class CdkCreater {

    /**
     * 基准字符，共34位
     * 不确定显示时候的字体，为了辨识度，移除了o、l
     */
    private static final String BASE_CHAR = "abcdefghijkmnpqrstuvwxyz0123456789";

    /**
     * CDK随机字符长度
     */
    private int CDK_RANDOM_LENGTH = 3;

    /**
     * CDK批次号位数
     */
    private int CDK_BATCH_NO_LENGTH = 2;

    /**
     * 兑换码的最大碰撞概率
     */
    private double MAX_COLLISION_RATE = 0.01;

    public CdkCreater() {
    }

    /**
     * 指定参数构造
     *
     * @param cdkRandomLength  CDK随机字符长度
     * @param cdkBatchNoLength CDK批次号位数
     * @param maxCollisionRate 兑换码的最大碰撞概率
     */
    public CdkCreater(int cdkRandomLength, int cdkBatchNoLength, double maxCollisionRate) {
        this.CDK_RANDOM_LENGTH = cdkRandomLength;
        this.CDK_BATCH_NO_LENGTH = cdkBatchNoLength;
        this.MAX_COLLISION_RATE = maxCollisionRate;
    }

    /**
     * 检查CDK校验是否正常
     *
     * @param cdk
     * @return
     */
    public static boolean checkCdk(String cdk) {
        int checkSum = BASE_CHAR.indexOf(cdk.charAt(0));
        int sum = 0;
        for (int i = 1; i < cdk.length(); i++) {
            char c = cdk.charAt(i);
            int index = BASE_CHAR.indexOf(c);
            sum += index;
        }
        sum = sum % BASE_CHAR.length();
        if (checkSum == sum) {
            return true;
        }
        return false;
    }

    /**
     * 生成兑换码
     * codeSet 为存储兑换码
     * @return 下一次使用的batchNo
     */
    public int generator(int batchNo, int num, Set<String> codeSet) {
        double maxBatchNo = Math.pow(BASE_CHAR.length(), CDK_BATCH_NO_LENGTH);
        if (batchNo < 0 || batchNo > maxBatchNo) {
            throw new RuntimeException("batch_no_error");
        }
        /**
         * 控制碰撞的概率：
         * 根据生成数量与随机码的全排列数量之比计算理论碰撞率。
         * 通过增加batchNo编号，将生成数量num拆分到多个batchNo对应的兑换码中，
         * 从而将理论碰撞率控制在设定碰撞概率之下
         */
        double rate = num / Math.pow(BASE_CHAR.length(), CDK_RANDOM_LENGTH);
        int deltaNo = (int) (rate / MAX_COLLISION_RATE) + 1;
        if (batchNo + deltaNo > maxBatchNo) {
            throw new RuntimeException("batch_no_error");
        }
        // 将需要生成的num个兑换码，拆分为deltaNo次生成
        List<Integer> splitList = splitQuantity(num, deltaNo);
        for (Integer splitNum : splitList) {
            Set<String> code = getCodeSet(batchNo, splitNum);
            codeSet.addAll(code);
            batchNo++;
        }
        return batchNo;
    }

    /**
     * 将生成的num个兑换码，拆分为deltaNo次生成
     *
     * @param totalNum 总数量
     * @param delta    拆分总数
     * @return
     */
    private List<Integer> splitQuantity(int totalNum, int delta) {
        List<Integer> ret = new ArrayList<>();
        int baseNum = (int) (MAX_COLLISION_RATE * Math.pow(BASE_CHAR.length(), CDK_RANDOM_LENGTH));
        for (int i = 0; i < delta - 1; i++) {
            ret.add(baseNum);
            totalNum -= baseNum;
        }
        ret.add(totalNum);
        return ret;
    }

    /**
     * 获取指定数量的兑换码集合
     * 兑换码格式：checkSum + batchNo + randomChar
     *
     * @param batchNo
     * @param num
     * @return
     */
    private Set<String> getCodeSet(int batchNo, int num) {
        Set<String> set = new HashSet<>((int) (num / 0.75));
        int sum = 0;
        char[] batchNoChar = new char[CDK_BATCH_NO_LENGTH];
        for (int i = 0; i < CDK_BATCH_NO_LENGTH; i++) {
            int index = batchNo % BASE_CHAR.length();
            sum += index;
            batchNoChar[i] = BASE_CHAR.charAt(index);
            batchNo /= BASE_CHAR.length();
        }
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < num; ) {
            String cdk = nextCode(batchNoChar, sum, random);
            boolean success = set.add(cdk);
            if (success) {
                i++;
            }
        }
        return set;
    }

    /**
     * 获取下一个兑换码
     * 格式：checkSum + batchNo + randomChar
     *
     * @param sum
     * @return
     */
    private String nextCode(char[] batchNoChar, int sum, Random random) {
        char[] randomChar = new char[CDK_RANDOM_LENGTH];
        for (int i = 0; i < CDK_RANDOM_LENGTH; i++) {
            int index = random.nextInt(BASE_CHAR.length());
            sum += index;
            randomChar[i] = BASE_CHAR.charAt(index);
        }
        char checkSumChar = BASE_CHAR.charAt(sum % BASE_CHAR.length());
        StringBuilder sb = new StringBuilder();
        sb.append(checkSumChar).append(batchNoChar).append(randomChar);
        return sb.toString();
    }

}