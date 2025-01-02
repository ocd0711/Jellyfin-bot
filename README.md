# Jellyfin Telegram Bot 管理工具

## 📜 项目说明

- **用 Telegram 管理 Jellyfin 用户**（开服）的一个小工具
- **推荐使用 docker 搭建, Jellyfin 由于使用 mysql 魔改版本, 使用官方版本可能需要自行修改部分代码 [魔改版 Jellyfin Dockerhub](https://hub.docker.com/repository/docker/ocd0711/jellyfin/general)**
- 此项目考虑并发和其他问题未使用 python
- 由于此项目最开始基于 emby, 且用于私服, 项目新建时间为 2022 年中间弃坑 n 久, 导致代码很乱, 并且有无用业务残留, 凑合着看吧
- 由于上面这个原因此项目没有保留旧的 commit 信息, 请谅解
- 反馈请尽量 issue，看到会处理

> **声明：本项目仅供学习交流使用，仅作为辅助工具借助 tg 平台方便用户管理自己的媒体库成员，对用户的其他行为及内容毫不知情**

## 项目使用

### Docker 部署方式

- 环境: `docker`, `docker-compose`
- 新建文件夹, 并且将后面 [点击跳转](https://github.com/ocd0711/Jellyfin-bot/tree/master/o9o-controller) 地址下的文件全部放在这个文件夹下
- 修改 `application.yml` 配置文件
- `docker compose up -d` 即可运行

### 自行编译部署方式

- 环境: `java 17`, `maven 3.x`, `docker`, `redis`
- 修改 `o9o-controller/src/main/resources/application-prod.yml` 配置文件
- 编译参考 `mvn package install -P prod`
- 启动可以参考 `./start`/`o9o-controller/docker-compose-prod.yml`
- 文档暂不完善, 请谅解

## 💐 Our Contributors

<a href="https://github.com/ocd0711/Jellyfin-bot/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ocd0711/Jellyfin-bot" />
</a>  

## 特别感谢（排序不分先后)

- [TelegramBots](https://github.com/rubenlagus/TelegramBots)

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=ocd0711/Jellyfin-bot&type=Date)](https://star-history.com/#ocd0711/Jellyfin-bot)