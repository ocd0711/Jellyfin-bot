package com.ocd.bean.dto.base;

import lombok.Data;

import java.util.Collections;

@Data
public class Result<T> {

    /**
     * 接口查询状态
     */
    private int status;

    /**
     * 接口查询状态描述
     */
    private String statusMsg;

    /**
     * 接口查询结果
     */
    private T data;

    public Result() {
    }

    public Result(int status, String statusMsg) {
        this.status = status;
        this.statusMsg = statusMsg;
        this.data = (T) Collections.EMPTY_LIST;
    }

    public Result(int status, String statusMsg, T data) {
        this.status = status;
        this.statusMsg = statusMsg;
        this.data = data;
    }
}
