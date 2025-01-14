package com.ocd.bean.mysql;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.ocd.bean.dto.excel.DevicesExcel;
import com.ocd.bean.dto.result.EmbyUserResult;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class User {

    public User() {
    }

    public User(Long tgId) {
        this.tgId = tgId.toString();
    }

    /**
     * 用户 id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * tg id
     */
    private String tgId;

    private Boolean hideMedia;

    /**
     * emby id
     */
    @TableField(insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private String embyId;

    /**
     * 作死次数
     */
    private Integer banCount;

    /**
     * 警告次数
     */
    private Integer warnCount;

    /**
     * 0:预留账户 1:启用账号 2:白名单账号 3:封禁用户
     */
    private Integer userType;

    /**
     * 是否为管理员
     */
    private Boolean admin = false;

    /**
     * 超级管理
     */
    private Boolean superAdmin = false;

    /**
     * 是否启用 bot
     */
    private Boolean startBot;

    /**
     * emby 是否停用
     */
    private Boolean deactivate;

    /**
     * emby name
     */
    @TableField(insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private String embyName;

    /**
     * 是否使用兑换注册
     */
    @TableField(insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private String exchange;

    /**
     * 用户积分
     */
    private Integer points = 0;

    /**
     * 过期时间
     */
    @TableField(insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private Date expTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    public void ban() {
        cleanEmby();
        points = 0;
        userType = 3;
        embyName = "封禁用户";
        embyId = "封禁用户";
    }

    public void unban() {
        if (userType == 3)
            cleanEmby();
        banCount = 0;
        warnCount = 0;
    }

    public void cleanEmby() {
        userType = 0;
        embyId = null;
        embyName = null;
        deactivate = false;
        exchange = null;
    }

    public void cleanWhite() {
        if (userType == 2) userType = 1;
        else if (userType == 1) userType = 1;
        else userType = 0;
        exchange = null;
    }

    public boolean haveEmby() {
        return userType != null && (userType == 1 || userType == 2) && embyId != null && embyName != null;
    }

    public void updateEmbyByEmbyUser(EmbyUserResult embyUserResult) {
        this.embyName = embyUserResult.getName();
        this.embyId = embyUserResult.getId();
        this.userType = 1;
    }

    public DevicesExcel getDeviceExcel() {
        DevicesExcel devicesExcel = new DevicesExcel();
        BeanUtils.copyProperties(this, devicesExcel);
        return devicesExcel;
    }

    public void updateByUser(User user) {
        this.embyName = user.getEmbyName();
        this.embyId = user.getEmbyId();
        this.userType = user.getUserType();
        this.deactivate = user.getDeactivate();
        this.expTime = user.expTime;
        this.points += user.points;
    }

    public void addExpDate(Integer days) {
        if (expTime == null)
            expTime = new Date();
        expTime = DateUtil.offsetDay(expTime, days);
    }
}