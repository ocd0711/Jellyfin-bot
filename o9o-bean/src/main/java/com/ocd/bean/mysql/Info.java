package com.ocd.bean.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author OCD
 * @date 2022/12/24 1:02
 * Description:
 */
@Data
public class Info {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String message;

}
