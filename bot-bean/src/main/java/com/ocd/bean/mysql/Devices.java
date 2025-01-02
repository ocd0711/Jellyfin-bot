package com.ocd.bean.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author ch.hu
 * @date 2024/12/17 09:32
 * Description:
 */
@Data
@TableName("Devices")
public class Devices {

    @TableId(type = IdType.AUTO)
    @TableField("Id")
    private Integer id;

    @TableField("UserId")
    private String userId;

    @TableField("AccessToken")
    private String accessToken;

    @TableField("AppName")
    private String appName;

    @TableField("AppVersion")
    private String appVersion;

    @TableField("DeviceName")
    private String deviceName;

    @TableField("DeviceId")
    private String deviceId;

    @TableField("IsActive")
    private String isActive;

    @TableField("DateCreated")
    private String dateCreated;

    @TableField("DateModified")
    private String dateModified;

    @TableField("DateLastActivity")
    private String dateLastActivity;

    @Override
    public String toString() {
        return "Devices{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", appName='" + appName + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", isActive='" + isActive + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", dateModified='" + dateModified + '\'' +
                ", dateLastActivity='" + dateLastActivity + '\'' +
                '}';
    }
}
