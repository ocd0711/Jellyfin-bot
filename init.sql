-- invitecode 存储
CREATE TABLE `invitecode`
(
    `id`                   int(11)               NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `invitecode`           varchar(200)          NOT NULL COMMENT '️邀请码',
    `used`                 int(1)                DEFAULT 0 COMMENT '是否被使用',
    `tg_id`                varchar(100)          DEFAULT NULL COMMENT 'tg id',
    `days`                 int(100)              DEFAULT NULL COMMENT '续期天数, -1 为白名单, 0 为注册码(注册后的剩余天数由 expDay 决定), 大于 0 为续期天数',
    `update_time`          datetime              DEFAULT NULL COMMENT '更新时间',
    `create_time`          datetime              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE  KEY invitecode (`invitecode`),
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

-- 加点提示语
CREATE TABLE `line`
(
    `id`                   int(11)               NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `message`              varchar(1000)         DEFAULT NULL COMMENT '线路信息',
    `ip`                   varchar(30)           DEFAULT NULL COMMENT 'ip 地址',
    `port`                 varchar(30)           DEFAULT NULL COMMENT '端口',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

-- 加点提示语
CREATE TABLE `info`
(
    `id`                   int(11)               NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `message`              varchar(1000)         DEFAULT NULL COMMENT '提示语',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

-- 用户表
CREATE TABLE `user`
(
    `id`                   int(11)               NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `tg_id`                varchar(100)          NOT NULL COMMENT 'telegram 用户 id',
    `emby_id`              varchar(100)          DEFAULT NULL COMMENT 'emby 用户 id',
    `emby_name`            varchar(100)          DEFAULT NULL COMMENT 'emby 用户 名称',
    `ban_count`            int(1)                DEFAULT 0 COMMENT '作死次数',
    `warn_count`           int(1)                DEFAULT 0 COMMENT '警告次数',
    `admin`                int(1)                DEFAULT 0 COMMENT '管理员',
    `user_type`            int(1)                DEFAULT 0 COMMENT '0:预留账户 1:启用账号 2:白名单账号 3:封禁用户',
    `super_admin`          int(1)                DEFAULT 0 COMMENT '超级管理',
    `start_bot`            int(1)                DEFAULT 1 COMMENT '是否启用 bot',
    `hide_media`           int(1)                DEFAULT 0 COMMENT '是否隐藏部分媒体库',
    `deactivate`           int(1)                DEFAULT 0 COMMENT 'emby 是否停用',
    `exchange`             varchar(200)          DEFAULT NULL COMMENT '使用的注册码',
    `exp_time`             datetime              DEFAULT NULL COMMENT '过期时间',
    `create_time`          datetime              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `points`               int(11)               NOT NULL DEFAULT 0 COMMENT '用户积分',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `moviepilot`
(
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `parent`       BIGINT         NOT NULL COMMENT '父用户',
    `org_string`   longtext       NOT NULL COMMENT '名称',
    `category`     VARCHAR(255)   DEFAULT NULL COMMENT '影片类型',
    `param`        longtext       DEFAULT NULL COMMENT 'moviepilot 提交参数',
    `imdb`         VARCHAR(255)   DEFAULT NULL COMMENT 'imdb 预览链接',
    `page_url`     VARCHAR(255)   DEFAULT NULL COMMENT '资源链接',
    `status`       INT(1)         NOT NULL DEFAULT 0 COMMENT '状态: -0:等待下载 1:下载完成',
    `down_id`      VARCHAR(255)   DEFAULT NULL COMMENT '下载 id',
    PRIMARY KEY (`id`),
    CONSTRAINT `moviepilot_parent_user_id` FOREIGN KEY (`parent`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT = '求片记录';