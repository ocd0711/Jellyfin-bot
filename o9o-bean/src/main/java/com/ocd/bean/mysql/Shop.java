package com.ocd.bean.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author OCD
 * @date 2023/02/14 2:04 PM
 * Description:
 */
@Data
public class Shop {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 月份
     */
    private Integer month;

}
