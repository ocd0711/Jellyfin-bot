package com.ocd.bean.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author OCD
 * @date 2023/02/16 1:31 PM
 * Description:
 */
@Data
public class HideMedia {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;
}
