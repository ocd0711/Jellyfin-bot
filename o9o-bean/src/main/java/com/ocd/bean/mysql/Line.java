package com.ocd.bean.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author OCD
 * @date 2022/12/25 12:15
 * Description:
 */
@Data
public class Line {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String message;

    private String ip;

    private String port;

    @Override
    public String toString() {
        return "Line{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                '}';
    }

}
