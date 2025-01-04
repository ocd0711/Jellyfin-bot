package com.ocd.controller.util;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.isen.bean.constant.ConstantStrings;
import com.ocd.bean.dto.jellby.PlaybackRecord;
import com.ocd.bean.dto.result.EmbyUserResult;
import com.ocd.bean.mysql.Info;
import com.ocd.controller.config.BotConfig;
import com.ocd.service.mysql.*;
import com.ocd.util.HttpUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

@Component
@Log4j2
public class AuthorityUtil {

    @Autowired
    public AuthorityUtil(UserService userService, LineService lineService, InvitecodeService invitecodeService, InfoService infoService, HideMediaService hideMediaService, ShopService shopService) {
        AuthorityUtil.userService = userService;
        AuthorityUtil.lineService = lineService;
        AuthorityUtil.invitecodeService = invitecodeService;
        AuthorityUtil.infoService = infoService;
        AuthorityUtil.hideMediaService = hideMediaService;
        AuthorityUtil.shopService = shopService;
    }

    public static UserService userService;

    public static LineService lineService;

    public static InvitecodeService invitecodeService;

    public static InfoService infoService;

    public static HideMediaService hideMediaService;

    public static ShopService shopService;

    public static boolean openRegister = false;

    public static Integer accountCount = null;

    public static String checkTgUser(User user) {
        String sendMessage = EmbyUtil.getInstance().checkServerHealth();
        if (sendMessage != null) return sendMessage;
        com.ocd.bean.mysql.User sqlUser = userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().eq("tg_id", user.getId()));
        if (sqlUser != null) if (sqlUser.getUserType() == 3) return "黑名单用户, 自己看着办";
        else return null;
        return "未启用 bot, 请重新发送 /start";
    }

    public static String checkTgUserStart(User user) {
        String sendMessage = EmbyUtil.getInstance().checkServerHealth();
        if (sendMessage != null) return sendMessage;
        com.ocd.bean.mysql.User sqlUser = userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, user.getId()));
        if (sqlUser != null) if (sqlUser.getUserType() == 3) return "黑名单用户, 自己看着办";
        else return null;
        return null;
    }

    public static String checkChatMember(long tgId, long chatId, TelegramClient telegramClient) {
        GetChatMember getChatMember = new GetChatMember(String.valueOf(chatId), tgId);
        getChatMember.setChatId(BotConfig.getInstance().GROUP_ID);
        getChatMember.setUserId(tgId);
        String out = "请先加入聊天群组和通知频道！\n" + "\n" + "公告频道：" + BotConfig.getInstance().CHANNEL + "\n" + "聊天吹水群：" + BotConfig.getInstance().GROUPNAME;
        try {
            ChatMember chatMember = telegramClient.execute(getChatMember);
            if (!ConstantStrings.INSTANCE.getGroupIn().contains(chatMember.getStatus()))
                return out;
        } catch (TelegramApiException e) {
            return null;
        }
        return null;
    }

    public static ChatMember checkChatMemberBean(long tgId, TelegramClient telegramClient) {
        GetChatMember getChatMember = new GetChatMember(BotConfig.getInstance().GROUP_ID.toString(), tgId);
        try {
            ChatMember chatMember = telegramClient.execute(getChatMember);
            return chatMember;
        } catch (TelegramApiException ignored) {
        }
        return null;
    }

    public static String chatOther(String message) {
        String outMessage = HttpUtil.getInstance().restTemplate().getForObject("http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + message, String.class);
        try {
            return JSON.parseObject(outMessage).getString("content");
        } catch (Exception e) {
            return JSON.parseObject(outMessage).getString("别叫我.jpg");
        }
    }

    public static void initAllPolicy() {
        userService.userMapper.selectList(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getSuperAdmin, 0).isNotNull(com.ocd.bean.mysql.User::getEmbyId)).forEach(user -> {
            EmbyUtil.getInstance().initUser(user);
        });
    }

    public static void cleanTask(TelegramClient telegramClient) {
        SendMessage sendMessage = new SendMessage("", "");
        List<EmbyUserResult> embyUserDtos = EmbyUtil.getInstance().getAllEmbyUser();
        embyUserDtos.forEach(embyUserDto -> {
            if (!embyUserDto.getPolicy().getIsAdministrator()) {
                EmbyUtil.getInstance().initPolicy(embyUserDto.getId());
                com.ocd.bean.mysql.User user = userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getEmbyId, embyUserDto.getId()));
                if (user == null) {
                    // 未绑定 TG
                    try {
                        EmbyUtil.getInstance().deleteEmbyById(embyUserDto.getId());
//                        sendMessage.setChatId(BotConfig.getInstance().CHANNEL);
//                        sendMessage.setChatId(BotConfig.getInstance().GROUP_ID);
                        sendMessage.setChatId(BotConfig.getInstance().NOTIFY_CHANNEL);
                        sendMessage.setText(String.format("#bot检查扬号: Emby 账号 %s ( %s ) 已被扬, 原因: 未绑定 tg 用户", embyUserDto.getName(), embyUserDto.getId()));
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        log.error(e.toString());
                    }
                } else if (AuthorityUtil.checkChatMember(Long.parseLong(user.getTgId()), Long.parseLong(BotConfig.getInstance().GROUP_ID), telegramClient) != null) {
                    try {
                        EmbyUtil.getInstance().deleteUser(user);
//                        sendMessage.setChatId(BotConfig.getInstance().GROUP_ID);
                        sendMessage.setChatId(BotConfig.getInstance().NOTIFY_CHANNEL);
                        sendMessage.setText(String.format("#bot检查扬号: Emby 账号 %s ( %s ) 已被扬, 原因: %s 不在群内", embyUserDto.getName(), embyUserDto.getId(), user.getTgId()));
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        log.error(e.toString());
                    }
                } else if (user.getUserType() != 2) {
//                    DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
//                    if (dayOfWeek == DayOfWeek.FRIDAY) {
//                        if (user.getDeactivate()) {
//                            EmbyUtil.getInstance().deactivateUser(user, false);
//                            sendMessage.setChatId(user.getTgId());
//                            sendMessage.setText("周五大赦天下 - 解除");
//                            try {
//                                telegramClient.execute(sendMessage);
//                            } catch (TelegramApiException e) {
//                                // nothing
//                            }
//                        }
//                    } else {
                    boolean needSend = false;
                    try {
                        List<PlaybackRecord> activityLogs = EmbyUtil.getInstance().getUserPlayback(user.getEmbyId());
                        Long betweenDay = activityLogs == null ? null : DateUtil.betweenDay(activityLogs.get(0).getDateCreated(), new Date(), true);
                        if (betweenDay == null || betweenDay >= BotConfig.getInstance().getEXPDAY()) {
                            if (betweenDay == null || betweenDay >= BotConfig.getInstance().getEXPDAY() + 7) {
                                EmbyUtil.getInstance().deleteUser(user);
                                AuthorityUtil.userService.userMapper.updateById(user);
                                needSend = true;
                            } else {
                                boolean cache = user.getDeactivate();
                                EmbyUtil.getInstance().deactivateUser(user, true);
                                needSend = !cache;
                            }
                        } else {
                            user.setUserType(1);
                            EmbyUtil.getInstance().deactivateUser(user, false);
                        }
                    } catch (Exception e) {
                    }
                    if (needSend) {
                        sendMessage.setChatId(user.getTgId());
                        sendMessage.setText(BotConfig.getInstance().getEXPDAY() + " 天未观看" + ((BotConfig.getInstance().getISDELETE() && !user.haveEmby()) ? "删除账户" : ("禁用账户" + (BotConfig.getInstance().getISDELETE() ? "(7 天内未解封删除用户)" : ""))));
                        try {
                            telegramClient.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            // nothing
                        } finally {
                            sendMessage.setChatId(BotConfig.getInstance().NOTIFY_CHANNEL);
                            sendMessage.enableMarkdownV2(true);
                            sendMessage.setText(MessageUtil.INSTANCE.getAccountMessage(user, embyUserDto));
                            try {
                                telegramClient.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                // nothing
                            }
                        }
                    }
                }
//                }
//                else if (user.getUserType() != 2 && user.getExpiredTime() != null) {
//                    Date today = DateUtil.beginOfDay(new Date());
//                    Date embyEndDate = DateUtil.beginOfDay(user.getExpiredTime());
//                    if (today.after(embyEndDate)) {
//                        // 停用账号
//                        if (!user.getDeactivate()) {
//                            try {
//                                EmbyUtil.getInstance().deactivateUser(user, true);
//                                sendMessage.setChatId(user.getTgId());
//                                sendMessage.setText(String.format("#bot检查停用: Emby 账号 %s ( %s ) 已被停用, 原因: 过期", embyUserDto.getName(), embyUserDto.getId()));
//                                sender.execute(sendMessage);
//                            } catch (TelegramApiException e) {
//                                log.error(e.toString());
//                            }
//                        }
//                    } else {
//                        // 启用账号
//                        if (user.getDeactivate() && user.getStartBot()) {
//                            try {
//                                EmbyUtil.getInstance().deactivateUser(user, false);
//                                sendMessage.setChatId(user.getTgId());
//                                sendMessage.setText(String.format("#bot检查启用: Emby 账号 %s ( %s ) 已被启用, 原因: 有效期内", embyUserDto.getName(), embyUserDto.getId()));
//                                sender.execute(sendMessage);
//                            } catch (TelegramApiException e) {
//                                log.error(e.toString());
//                            }
//                        }
//                    }
//                }
            }
        });
        //        if (!embyUserDtos.isEmpty())
//            AuthorityUtil.userService.userMapper.selectList(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().isNotNull(com.ocd.bean.mysql.User::getEmbyId).eq(com.ocd.bean.mysql.User::getDeactivate, 0)).forEach(user -> {
//                if (embyUserDtos.stream().noneMatch(embyUserDto -> embyUserDto.getId().equals(user.getEmbyId()))) {
//                    user.cleanWhite();
//                    AuthorityUtil.userService.userMapper.updateById(user);
//                } else if (user.getExpiredTime() == null) {
//                    try {
//                        sendMessage.setChatId("5340385875");
//                        sendMessage.setText("用户信息异常" + (user.getUserType() == 2 ? ",白名单用户已处理" : "") + ": " + JSON.toJSONString(user));
//                        sender.execute(sendMessage);
//                        sendMessage.setChatId(user.getTgId());
//                        if (user.getUserType() == 2) {
//                            user.setExpiredTime(new Date());
//                            AuthorityUtil.userService.userMapper.updateById(user);
//                            sendMessage.setText("白名单用户: 此账号用户信息异常已修正, /info 可查看用户信息");
//                        } else {
//                            EmbyUtil.getInstance().deactivateUser(user, true);
//                            user.setDeactivate(true);
//                            AuthorityUtil.userService.userMapper.updateById(user);
//                            sendMessage.setText("此账号用户已停用");
//                        }
//                        sender.execute(sendMessage);
//                    } catch (TelegramApiException e) {
//                        log.error(e.toString());
//                    }
//                }
//            });
//        // 用户到期通知
//        AuthorityUtil.userService.userMapper.selectList(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().ne(com.ocd.bean.mysql.User::getUserType, 2).eq(com.ocd.bean.mysql.User::getDeactivate, 0).isNotNull(com.ocd.bean.mysql.User::getEmbyId).ge(com.ocd.bean.mysql.User::getExpiredTime, DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -3))).le(com.ocd.bean.mysql.User::getExpiredTime, DateUtil.endOfDay(new Date()))).forEach(user -> {
//            if (embyUserDtos.stream().noneMatch(embyUserDto -> embyUserDto.getId().equals(user.getEmbyId()))) {
//                try {
//                    sendMessage.setChatId(user.getTgId());
//                    sendMessage.setText(String.format("您的账号将于 %s 到期, 请注意续费时间\n本通知将在账号停用三天前每天 0 点通知一次(未续费账号封存, 续费后启用观看记录等用户信息不会丢失)", DateUtil.beginOfDay(user.getExpiredTime())));
//                    sender.execute(sendMessage);
//                } catch (TelegramApiException e) {
//                    log.error(e.toString());
//                }
//            }
//        });
        // 发送运行时间通知
        sendMessage.setChatId(BotConfig.getInstance().GROUP_ID);
        List<Info> infos = infoService.infoMapper.selectList(null);
        sendMessage.setText(
                String.format("欢迎来到" + BotConfig.getInstance().GROUP_NICK + " - 今日用户信息通知已发送完成\n活跃-%s 停用/待杀-%s" + (infos.isEmpty() ? "" : "\n" + infos.get(0).
                                getMessage()) + "\n%s",
                        AuthorityUtil.userService.userMapper.selectCount(
                                new QueryWrapper<com.ocd.bean.mysql.User>().lambda().isNotNull(com.ocd.bean.mysql.User::getEmbyId)
                                        .in(com.ocd.bean.mysql.User::getDeactivate, 0)
                        ),
                        AuthorityUtil.userService.userMapper.selectCount(
                                new QueryWrapper<com.ocd.bean.mysql.User>().lambda().isNotNull(com.ocd.bean.mysql.User::getEmbyId)
                                        .in(com.ocd.bean.mysql.User::getDeactivate, 1)
                        ),
                        EmbyUtil.getInstance().LibraryCountStr())
        );
        try {
            telegramClient.execute(sendMessage);
        } catch (
                TelegramApiException e) {
            log.error(e.toString());
        }
    }

    public static String invitecode() {
        Long count = invitecodeService.invitecodeMapper.selectCount(null);
        Set<String> codes = new HashSet<>();
        CdkCreater cdkCreater = new CdkCreater();
        cdkCreater.generator((int) (count / 1000), 1, codes);
        return new ArrayList<>(codes).get(0).toUpperCase(Locale.ROOT);
    }
}