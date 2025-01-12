package com.ocd.bean.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author OCD
 * @date 2023/02/14 2:39 PM
 * Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invitecode {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String invitecode;

    private Boolean used;

    private Integer days;

    private String tgId;

    public Invitecode(String invitecode, Integer days) {
        this.invitecode = invitecode;
        this.days = days;
    }

    public void sUse(String tgId) {
        this.tgId = tgId;
        this.used = true;
    }
}
