# Jellyfin/Emby Telegram Bot 管理工具

## 📜 项目说明

- **用 Telegram 管理 Jellyfin/Emby 用户**（开服）的一个小工具
- ~~**推荐使用 docker 搭建, Jellyfin 由于使用 mysql 魔改版本,
  使用官方版本可能需要自行修改部分代码 [魔改版 Jellyfin Dockerhub](https://hub.docker.com/repository/docker/ocd0711/jellyfin/general)
  **~~ 已兼容 emby/jellyfin 官方版
- 此项目考虑并发和其他问题未使用 python
- 由于此项目最开始基于 emby, 且用于私服, 项目新建时间为 2022 年中间弃坑 n 久, 导致代码很乱, 并且有无用业务残留, 凑合着看吧
- 由于上面这个原因此项目没有保留旧的 commit 信息, 请谅解
- 反馈请尽量 issue，看到会处理

> **声明：本项目仅供学习交流使用，仅作为辅助工具借助 tg 平台方便用户管理自己的媒体库成员，对用户的其他行为及内容毫不知情**

## 项目使用

### 群内/聊天指令(此项目指令头并没有带符号, 为避免和其他 bot 冲突)

- `channel` 发送消息至频道(ex: channel xxxxxx)
- `find` 通过 tgId/embyName 查找用户(ex: find 123456)
- `findin` 通过邀请码查询用户 (ex: findin 123456)
- `invite` 私密群组可以通过此指令生成要邀请的链接, 可配置生成的是否转发到频道和到期时间(invite 邀请人数 邀请有效天数
  是否转发到频道ex: invite 1 1 1)
- `inviteh` 等同于 invite, 但是时间参数是小时(inviteh 邀请人数 邀请有效小时数 是否转发到频道ex: invite 1 1 1)

### 回复对应用户消息使用的指令

- `id` 调出发送这条消息的用户管理菜单
- `pin` 置顶此消息
- `unpin` 取消置顶此消息

## 项目部署

### 前提

- `mysql` 数据库(需自建, 建标语句在这 [点击跳转](https://github.com/ocd0711/Jellyfin-bot/tree/master/init.sql))
- `redis` 缓存(可使用自建, 也可直接修改 docker compose 里的配置)

### Docker 部署方式

- 环境: `docker`, `docker-compose`
- 参考 [点击跳转](https://github.com/ocd0711/Jellyfin-bot/tree/master/docker) 的 docker compose 配置(配置内有 `redis`
  容器, 也可自己对接)
- 修改 `application.yml` 配置文件
- `docker compose up -d` 即可运行
- `mysql` 需要自建, 建标语句在 [点击跳转](https://github.com/ocd0711/Jellyfin-bot/tree/master/init.sql)

### 自行编译部署方式(不推荐, 需要知道的有点多)

- 环境: `java 17`, `maven 3.x`, `docker`, `redis`
- 修改 `bot-controller/src/main/resources/application-prod.yml` 配置文件
- 编译参考 `mvn package install -P prod`
- 启动可以参考 `./start`/`bot-controller/docker-compose-prod.yml`
- 文档暂不完善, 请谅解

## 💐 Our Contributors

<a href="https://github.com/ocd0711/Jellyfin-bot/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ocd0711/Jellyfin-bot" />
</a>  

## 特别感谢（排序不分先后)

- [TelegramBots](https://github.com/rubenlagus/TelegramBots)

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=ocd0711/Jellyfin-bot&type=Date)](https://star-history.com/#ocd0711/Jellyfin-bot)