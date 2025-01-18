package com.ocd.controller.util;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.isen.bean.constant.ConstantStrings;
import com.ocd.controller.config.BotConfig;
import com.ocd.service.mysql.*;
import com.ocd.util.HttpUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
@Log4j2
public class AuthorityUtil {

    @Autowired
    public AuthorityUtil(UserService userService, LineService lineService, InvitecodeService invitecodeService, InfoService infoService, HideMediaService hideMediaService, BotConfig botConfig) {
        AuthorityUtil.userService = userService;
        AuthorityUtil.lineService = lineService;
        AuthorityUtil.invitecodeService = invitecodeService;
        AuthorityUtil.infoService = infoService;
        AuthorityUtil.hideMediaService = hideMediaService;
        AuthorityUtil.botConfig = botConfig;
    }

    public static UserService userService;

    public static LineService lineService;

    public static InvitecodeService invitecodeService;

    public static InfoService infoService;

    public static HideMediaService hideMediaService;

    public static boolean openRegister = false;

    public static Integer accountCount = null;

    public static BotConfig botConfig;

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
        getChatMember.setChatId(AuthorityUtil.botConfig.groupId);
        getChatMember.setUserId(tgId);
        String out = "请先加入聊天群组和通知频道！\n" + "\n" + "公告频道：" + AuthorityUtil.botConfig.channel + "\n" + "聊天吹水群：" + AuthorityUtil.botConfig.groupName;
        try {
            ChatMember chatMember = telegramClient.execute(getChatMember);
            if (!ConstantStrings.INSTANCE.getGroupIn().contains(chatMember.getStatus()))
                return out;
        } catch (TelegramApiException e) {
            return null;
        }
        return null;
    }

    public static Boolean checkUserInChatMember(long tgId, String chatId, TelegramClient telegramClient) {
        GetChatMember getChatMember = new GetChatMember(String.valueOf(chatId), tgId);
        try {
            ChatMember chatMember = telegramClient.execute(getChatMember);
            if (!ConstantStrings.INSTANCE.getGroupIn().contains(chatMember.getStatus()))
                return false;
        } catch (TelegramApiException e) {
            return null;
        }
        return true;
    }

    public static ChatMember checkChatMemberBean(long tgId, TelegramClient telegramClient) {
        GetChatMember getChatMember = new GetChatMember(AuthorityUtil.botConfig.groupId.toString(), tgId);
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

    public static String invitecode() {
        Long count = invitecodeService.invitecodeMapper.selectCount(null);
        Set<String> codes = new HashSet<>();
        CdkCreater cdkCreater = new CdkCreater();
        cdkCreater.generator((int) (count / 1000), 1, codes);
        return new ArrayList<>(codes).get(0).toUpperCase(Locale.ROOT);
    }
}