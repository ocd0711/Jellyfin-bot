package com.ocd.bean.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author ch.hu
 * @date 2024/11/16 21:39
 * Description:
 */
@Data
@TableName("ActivityLogs")
public class ActivityLogs {

    @TableId(type = IdType.AUTO)
    @TableField("Id")
    private Integer id;

    @TableField("Name")
    private String name;

    @TableField("Overview")
    private String overview;

    @TableField("ShortOverview")
    private String shortOverview;

    @TableField("Type")
    private String type;

    @TableField("UserId")
    private String userId;

    @TableField("DateCreated")
    private Date dateCreated;

    @TableField("LogSeverity")
    private Integer logSeverity;

    @TableField("RowVersion")
    private Integer rowVersion;
}
