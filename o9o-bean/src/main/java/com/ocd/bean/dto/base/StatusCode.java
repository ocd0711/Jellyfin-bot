package com.ocd.bean.dto.base;

/**
 * 返回码
 */
public class StatusCode {
    public static final int OK = 1;// 成功
    public static final int WARN = 0;// 成功
    public static final int ERROR = -1;// 失败
    public static final int LOGINERROR = -123;// 用户名或密码错误
    public static final int ACCESSERROR = 20003;// 权限不足
    public static final int REMOTEERROR = 20004;// 远程调用失败
    public static final int REPERROR = 20005;// 重复操作
}
