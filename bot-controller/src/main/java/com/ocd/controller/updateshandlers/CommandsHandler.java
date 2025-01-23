package com.ocd.controller.updateshandlers;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.isen.bean.constant.ConstantStrings;
import com.ocd.bean.dto.result.EmbyUserResult;
import com.ocd.bean.mysql.Invitecode;
import com.ocd.bean.mysql.Line;
import com.ocd.controller.commands.*;
import com.ocd.controller.util.AuthorityUtil;
import com.ocd.controller.util.EmbyUtil;
import com.ocd.controller.util.MessageUtil;
import com.ocd.controller.util.RedisUtil;
import com.ocd.service.mysql.LineService;
import com.ocd.service.mysql.UserService;
import com.ocd.util.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * This handler mainly works with commands to demonstrate the Commands feature of the API
 *
 * @author OCD
 */
@Component
@Slf4j
public class CommandsHandler extends CommandLongPollingTelegramBot {

    List<String> userButtons = Arrays.asList("info", "main", "line", "openRegister", "bind", "create", "reset", "hide", "unblock", "checkin", "device", "logout", "shop", "flush");

    /**
     * Constructor.
     */
    public CommandsHandler(
            @Value("${bot.token}") String botToken,
            @Value("${bot.name}") String botUsername,
            UserService userService, LineService lineService) {
        super(new OkHttpTelegramClient(botToken), true, () -> botUsername);
        StartCommand startCommand = new StartCommand(this);
        register(startCommand);
        register(new StatisticsCommand());
        register(new UserNotifyCommand());
        register(new OpenCommand());
        register(new RenewAllCommand());

        registerDefaultAction((absSender, message) -> {
            SendMessage commandUnknownMessage = new SendMessage(message.getChatId().toString(), "");
            commandUnknownMessage.setChatId(Long.toString(message.getChatId()));
            if (StringUtils.equals(message.getChatId().toString(), message.getFrom().getId().toString())) {
                commandUnknownMessage.setText("指令 '" + message.getText() + "' 这个机器人不知道。这里有一些帮助: ⬇️");
                try {
                    absSender.execute(commandUnknownMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
                startCommand.execute(absSender, message.getFrom(), message.getChat(), new String[]{});
            }
        });
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        MessageUtil.INSTANCE.launchInGlobalScope((Runnable) -> {
            // 是否为按钮回应
            if (update.hasCallbackQuery()) {
                SendMessage sendMessageRequest = new SendMessage(update.getCallbackQuery().getMessage().getChatId().toString(), "");
//            if (update.getCallbackQuery().getMessage().isTopicMessage())
//                sendMessageRequest.setMessageThreadId(update.getCallbackQuery().getMessage().getMessageThreadId());
                StringBuilder outString = null;
                com.ocd.bean.mysql.User operatorsUser = null;
                com.ocd.bean.mysql.User cacheUser = null;
                String command = null;
                ChatMember chatMember = null;
                try {
                    AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.getCallbackQuery().getId());
                    answerCallbackQuery.setShowAlert(true);
                    operatorsUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, update.getCallbackQuery().getFrom().getId()));
                    String[] datas = update.getCallbackQuery().getData().split(" ");
                    if (operatorsUser != null && datas.length > 1) {
                        command = datas[0];
                        Long userId = Long.valueOf(datas[1]);
                        GetChatMember getChatMember = new GetChatMember(update.getCallbackQuery().getMessage().getChatId().toString(), userId);
                        chatMember = telegramClient.execute(getChatMember);
                        cacheUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, userId));
                        DeleteMessage deleteMessage = new DeleteMessage(update.getCallbackQuery().getMessage().getChatId().toString(), update.getCallbackQuery().getMessage().getMessageId());
                        String nextMessageId = null;
                        if (operatorsUser.getAdmin() && !userButtons.contains(command)) {
                            telegramClient.execute(deleteMessage);
                            switch (command) {
                                case "warn":
                                    if (cacheUser.getAdmin()) {
                                        outString = new StringBuilder("管理也别自相残杀啊, 换个人玩");
                                    } else if (cacheUser.getWarnCount() > 3) {
                                        BanChatMember banChatMemberWarn = new BanChatMember(update.getCallbackQuery().getMessage().getChatId().toString(), userId);
                                        banChatMemberWarn.setUntilDate(0);
                                        banChatMemberWarn.setRevokeMessages(true);
                                        telegramClient.execute(banChatMemberWarn);
                                        if (cacheUser != null) {
                                            EmbyUtil.getInstance().deleteUser(cacheUser);
                                            cacheUser.ban();
                                            AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                        }
//                                        outString = "已 ban 且 emby 号已扬: \n" + JSON.toJSONString(replyUser) + "\n" + JSON.toJSONString(cacheUser);
                                        outString = new StringBuilder("他现在被警告了 " + cacheUser.getWarnCount() + "次" + " 触发 ban");
                                    } else {
                                        cacheUser.setWarnCount(cacheUser.getWarnCount() + 1);
                                        AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                        outString = new StringBuilder("他现在被警告了 " + cacheUser.getWarnCount() + "次");
                                    }
                                    break;
                                case "ban":
                                    if (cacheUser.getAdmin()) {
                                        outString = new StringBuilder("管理也别自相残杀啊, 换个人玩");
                                    } else {
                                        sendMessageRequest.setReplyMarkup(
                                                InlineKeyboardMarkup
                                                        .builder()
                                                        .keyboardRow(new InlineKeyboardRow(
                                                                InlineKeyboardButton.builder().text("\uD83C\uDD71️ 解封").callbackData("unban " + userId).build()
                                                        ))
                                                        .build()
                                        );

                                        BanChatMember banChatMember = new BanChatMember(update.getCallbackQuery().getMessage().getChatId().toString(), userId);
                                        banChatMember.setUntilDate(0);
                                        banChatMember.setRevokeMessages(true);
                                        telegramClient.execute(banChatMember);
                                        if (cacheUser != null) {
                                            EmbyUtil.getInstance().deleteUser(cacheUser);
                                            cacheUser.ban();
                                            AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                        }
//                                        outString = "已 ban 且 emby 号已扬: \n" + JSON.toJSONString(replyUser) + "\n" + JSON.toJSONString(cacheUser);
                                        outString = new StringBuilder("已 ban 且观影号已扬");
                                    }
                                    break;
                                case "unban":
                                    UnbanChatMember unbanChatMember = new UnbanChatMember(update.getCallbackQuery().getMessage().getChatId().toString(), userId);
                                    unbanChatMember.setOnlyIfBanned(true);
                                    telegramClient.execute(unbanChatMember);
                                    Integer cacheBan = null;
                                    if (cacheUser != null) {
                                        cacheBan = cacheUser.getUserType();
                                        cacheUser.unban();
                                    }
                                    AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                    if (cacheUser == null)
                                        outString = new StringBuilder("空用户 emmm 那得联系 OCD 看看了");
                                    else if (cacheBan == 3) {
//                                        outString = "解 ban: \n" + JSON.toJSONString(replyUser) + "\n" + JSON.toJSONString(cacheUser);
                                        outString = new StringBuilder("解 ban");
                                    } else {
                                        outString = new StringBuilder("不是黑户就别玩了撒(已清除 ban 统计, 如已绑定观影账号不会清除信息)");
                                    }
                                    break;
                                case "admin":
                                    if (operatorsUser.getSuperAdmin()) {
                                        if (cacheUser == null)
                                            outString = new StringBuilder("此用户无法授予管理权限, 用户未启用 bot");
                                        else {
                                            cacheUser.setAdmin(true);
                                            AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                            outString = new StringBuilder("已授予管理权");
                                        }
                                    } else {
                                        answerCallbackQuery.setText("仅超管可用");
                                        try {
                                            telegramClient.execute(answerCallbackQuery);
                                            return null;
                                        } catch (TelegramApiException e) {
                                            outString = new StringBuilder("异常情况: " + e);
                                        }
                                    }
                                    break;
                                case "unadmin":
                                    if (operatorsUser.getSuperAdmin()) {
                                        if (cacheUser == null)
                                            outString = new StringBuilder("此用户无法取消管理权限, 用户未启用 bot");
                                        else {
                                            cacheUser.setAdmin(false);
                                            AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                            outString = new StringBuilder("已取消管理权");
                                        }
                                    } else {
                                        answerCallbackQuery.setText("仅超管可用");
                                        try {
                                            telegramClient.execute(answerCallbackQuery);
                                            return null;
                                        } catch (TelegramApiException e) {
                                            outString = new StringBuilder("异常情况: " + e);
                                        }
                                    }
                                    break;
                                case "block":
                                    if (cacheUser.getSuperAdmin()) {
                                        answerCallbackQuery.setText("不允许操作超管账号");
                                        try {
                                            telegramClient.execute(answerCallbackQuery);
                                            return null;
                                        } catch (TelegramApiException e) {
                                            outString = new StringBuilder("异常情况: " + e);
                                        }
                                    } else if (!cacheUser.haveEmby()) {
                                        answerCallbackQuery.setText("用户无账户");
                                        try {
                                            telegramClient.execute(answerCallbackQuery);
                                            return null;
                                        } catch (TelegramApiException e) {
                                            outString = new StringBuilder("异常情况: " + e);
                                        }
                                    } else {
                                        EmbyUtil.getInstance().deactivateUser(cacheUser, !cacheUser.getDeactivate());
                                        outString = new StringBuilder("用户账户状态: " + !cacheUser.getDeactivate());
                                    }
                                    break;
                                case "del":
                                    if (cacheUser.getSuperAdmin()) {
                                        answerCallbackQuery.setText("不允许操作超管账号");
                                        try {
                                            telegramClient.execute(answerCallbackQuery);
                                            return null;
                                        } catch (TelegramApiException e) {
                                            outString = new StringBuilder("异常情况: " + e);
                                        }
                                    } else if (!cacheUser.haveEmby()) {
                                        answerCallbackQuery.setText("用户无账户");
                                        try {
                                            telegramClient.execute(answerCallbackQuery);
                                            return null;
                                        } catch (TelegramApiException e) {
                                            outString = new StringBuilder("异常情况: " + e);
                                        }
                                    } else {
                                        EmbyUtil.getInstance().deleteUser(cacheUser);
                                        AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                        outString = new StringBuilder("💣 已经注销观影账号, 可联系管理重新开号");
                                    }
                                    break;
                                case "gift":
                                    if (cacheUser == null || !cacheUser.getStartBot()) {
                                        outString = new StringBuilder("用户未启用 bot 无法赠送账号");
                                    } else if (chatMember.getUser().getIsBot()) {
                                        outString = new StringBuilder("此为机器人无法赠送");
                                    } else {
                                        if (cacheUser.getUserType() == 3) outString = new StringBuilder("封禁用户哦!");
                                        else {
                                            Invitecode invitecode = new Invitecode(AuthorityUtil.invitecode(), 0);
                                            AuthorityUtil.invitecodeService.invitecodeMapper.insert(invitecode);
                                            SendMessage giftMessage = new SendMessage(userId.toString(), "");
                                            giftMessage.setText("注册码: https://t.me/" + AuthorityUtil.botConfig.name + "?start=" + invitecode.getInvitecode());
                                            try {
                                                telegramClient.execute(giftMessage);
                                            } catch (TelegramApiException e) {
                                                log.error(e.toString());
                                            }
                                            outString = new StringBuilder("注册码发送成功, 用户自行查看 bot 私信, 留档: " + invitecode.getId());
                                        }
                                    }
                                    break;
                                case "mail":
                                    if (cacheUser == null || !cacheUser.getStartBot()) {
                                        outString = new StringBuilder("用户未启用 bot 无法赠送帐号");
                                    } else if (chatMember.getUser().getIsBot()) {
                                        outString = new StringBuilder("此为机器人禁止赠送");
                                    } else {
                                        if (cacheUser.getUserType() == 3) outString = new StringBuilder("封禁用户哦!");
                                        else if (cacheUser.getUserType() == 2)
                                            outString = new StringBuilder("用户已是 ♾️");
                                        else {
                                            Invitecode invitecode = new Invitecode(AuthorityUtil.invitecode(), -1);
                                            AuthorityUtil.invitecodeService.invitecodeMapper.insert(invitecode);
                                            SendMessage giftMessage = new SendMessage(userId.toString(), "");
                                            giftMessage.setText("♾️: https://t.me/" + AuthorityUtil.botConfig.name + "?start=" + invitecode.getInvitecode());
                                            try {
                                                telegramClient.execute(giftMessage);
                                            } catch (TelegramApiException e) {
                                                log.error(e.toString());
                                            }
                                            outString = new StringBuilder("♾️ 赠送兑换码成功, 用户自行查看 bot 私信, 留档: " + invitecode.getId());
                                        }
                                    }
                                    break;
                                case "changeBind":
                                    outString = new StringBuilder(datas[2] + " 账户已换绑");
                                    com.ocd.bean.mysql.User removeEmbyUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getEmbyName, datas[2]));
                                    if (removeEmbyUser != null) {
                                        outString.append("原账户: ").append(removeEmbyUser.getTgId());
                                        cacheUser.updateByUser(removeEmbyUser);
                                        removeEmbyUser.cleanEmby();
                                        AuthorityUtil.userService.userMapper.updateById(removeEmbyUser);
                                    }
                                    AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                    break;
                                case "noChangeBind":
                                    outString = new StringBuilder(datas[2] + " 账户拒绝换绑");
                                    break;
                            }
                        } else {
                            if (!chatMember.getUser().getIsBot()) {
                                EditMessageCaption editMessageCaption = new EditMessageCaption();
                                editMessageCaption.setChatId(userId);
                                editMessageCaption.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                                // 初始化按钮菜单
                                List<InlineKeyboardRow> rows = new ArrayList<>();
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);
                                String check = AuthorityUtil.checkTgUser(update.getCallbackQuery().getFrom());
                                if (check == null) {
                                    switch (command) {
                                        case "openRegister":
                                            if (update.getCallbackQuery().getFrom().getIsBot())
                                                editMessageCaption.setCaption("bot 滚蛋");
                                            else {
                                                if (cacheUser == null)
                                                    editMessageCaption.setCaption("不是" + AuthorityUtil.botConfig.groupNick + "用户!");
                                                else if (!cacheUser.getSuperAdmin()) {
                                                    editMessageCaption.setCaption("超管才允许使用, 别试了");
                                                } else {
                                                    AuthorityUtil.openRegister = !AuthorityUtil.openRegister;
                                                    editMessageCaption.setCaption(MessageUtil.INSTANCE.getStartMessage(update.getCallbackQuery().getFrom().getFirstName()));
                                                }
                                            }
                                            break;
                                        case "main":
                                            RedisUtil.del(ConstantStrings.INSTANCE.getRedisTypeKey(userId.toString(), ""));
                                            editMessageCaption.setCaption(MessageUtil.INSTANCE.getStartMessage(update.getCallbackQuery().getFrom().getFirstName()));
                                            if (datas.length > 2) {
                                                try {
                                                    DeleteMessage deleteMessageCache = new DeleteMessage(editMessageCaption.getChatId(), Integer.parseInt(datas[2]));
                                                    telegramClient.execute(deleteMessageCache);
                                                } catch (Exception e) {
                                                    // nothing
                                                }
                                            }
                                            break;
                                        case "line":
                                            StringBuilder editCaptionLine = new StringBuilder();
                                            if (cacheUser == null)
                                                editCaptionLine.append("不是" + AuthorityUtil.botConfig.groupNick + "用户!");
                                            else if (!cacheUser.haveEmby()) {
                                                editCaptionLine.append("无账号无法查看");
                                            } else if (cacheUser.getDeactivate()) {
                                                editCaptionLine.append("账户停用无法查看");
                                            } else {
                                                List<Line> lines = AuthorityUtil.lineService.lineMapper.selectList(null);
                                                StringBuffer stringBuffer = new StringBuffer();
                                                if (cacheUser.haveEmby()) {
                                                    lines.forEach(line -> stringBuffer.append(String.format(ConstantStrings.INSTANCE.getLineStr(), line.getMessage(), line.getIp(), line.getPort(), EmbyUtil.getInstance().checkUrl(line) ? "✅" : "❌")));
                                                    stringBuffer.append("\n").append(MessageUtil.INSTANCE.getServerStats());
                                                } else
                                                    stringBuffer.append("无'" + AuthorityUtil.botConfig.groupNick + "'账号\n");
                                                editCaptionLine.append(stringBuffer);
                                            }
                                            editMessageCaption.setCaption(editCaptionLine.toString());
                                            break;
                                        case "hide":
                                            StringBuilder editCaptionHide = new StringBuilder();
                                            if (cacheUser == null)
                                                editCaptionHide.append("不是" + AuthorityUtil.botConfig.groupNick + "用户!");
                                            else if (!cacheUser.haveEmby() || cacheUser.getDeactivate()) {
                                                editCaptionHide.append("无账号/过期无法操作");
                                            } else {
                                                editCaptionHide.append("点击下面按钮将隐藏对应分类(重新展示所有分类需点击`显示所有分类`, 否则新增分类默认不隐藏)");
                                                if (datas.length > 2) {
                                                    String id = datas[2];
                                                    EmbyUtil.getInstance().filterFolder(cacheUser, id);
                                                    rows.addAll(MessageUtil.INSTANCE.getAllFolderButton(cacheUser));
                                                    answerCallbackQuery.setText("切换成功");
                                                    try {
                                                        telegramClient.execute(answerCallbackQuery);
                                                    } catch (TelegramApiException e) {
                                                        outString = new StringBuilder("异常情况: " + e);
                                                    }
                                                } else {
                                                    rows.addAll(MessageUtil.INSTANCE.getAllFolderButton(cacheUser));
                                                }
                                            }
                                            editMessageCaption.setCaption(editCaptionHide.toString());
                                            break;
                                        case "device":
                                            StringBuilder editCaptionDevice = new StringBuilder();
                                            if (cacheUser == null)
                                                editCaptionDevice.append("不是" + AuthorityUtil.botConfig.groupNick + "用户!");
                                            else if (!cacheUser.haveEmby() || cacheUser.getDeactivate()) {
                                                editCaptionDevice.append("无账号/过期无法操作");
                                            } else {
                                                editCaptionDevice.append("点击下面按钮将强制登出设备");
                                                rows.addAll(MessageUtil.INSTANCE.getAllDevicesButton(cacheUser));
                                            }
                                            editMessageCaption.setCaption(editCaptionDevice.toString());
                                            break;
                                        case "logout":
                                            if (!cacheUser.haveEmby()) {
                                                answerCallbackQuery.setText("无号用户禁止操作");
                                                try {
                                                    telegramClient.execute(answerCallbackQuery);
                                                    return null;
                                                } catch (TelegramApiException e) {
                                                    outString = new StringBuilder("异常情况: " + e);
                                                }
                                            } else {
                                                EmbyUtil.getInstance().deleteDevice(datas[2]);
                                                answerCallbackQuery.setText("设备移除成功");
                                                try {
                                                    telegramClient.execute(answerCallbackQuery);
                                                } catch (TelegramApiException e) {
                                                    outString = new StringBuilder("异常情况: " + e);
                                                }
                                                rows.addAll(MessageUtil.INSTANCE.getAllDevicesButton(cacheUser, datas[2]));
                                            }
                                            break;
                                        case "info":
                                            EmbyUserResult embyUserDto = EmbyUtil.getInstance().getUserByEmbyId(cacheUser.getEmbyId());
                                            String editCaptionInfo = MessageUtil.INSTANCE.getUserInfo(embyUserDto, cacheUser);
                                            editMessageCaption.setCaption(editCaptionInfo);
                                            InlineKeyboardRow rowUnblock = new InlineKeyboardRow();
                                            if (AuthorityUtil.botConfig.getAllowUserUnlockAccount() && cacheUser.getDeactivate()) {
                                                rowUnblock.add(MessageUtil.INSTANCE.getUnblockButton(cacheUser));
                                            }
                                            rowUnblock.add(MessageUtil.INSTANCE.getCheckinButton(cacheUser));
                                            rows.add(rowUnblock);
                                            break;
                                        case "flush":
                                            RedisUtil.del(ConstantStrings.INSTANCE.getRedisTypeKey(userId.toString(), ""));
                                            editMessageCaption.setCaption(MessageUtil.INSTANCE.getStartMessage(update.getCallbackQuery().getFrom().getFirstName(), true));
                                            if (cacheUser.getUserType() == 0 && EmbyUtil.getInstance().getUserByEmbyId(cacheUser.getEmbyId()) != null) {
                                                cacheUser.setUserType(1);
                                                AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                            }
                                            if (AuthorityUtil.botConfig.getOpenAutoRenewal() && cacheUser.getExpTime() == null) {
                                                cacheUser.addExpDate(AuthorityUtil.botConfig.getExpDay());
                                                AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                            }
                                            break;
                                        case "unblock":
                                            if (!cacheUser.haveEmby()) {
                                                editMessageCaption.setCaption("无账户, 无法操作");
                                            } else {
                                                EmbyUserResult embyUserDtoUnblock = EmbyUtil.getInstance().getUserByEmbyId(cacheUser.getEmbyId());
//                                                DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
//                                                if (dayOfWeek == DayOfWeek.FRIDAY) {
//                                                    EmbyUtil.getInstance().deactivateUser(cacheUser, false);
//                                                    editMessageCaption.setCaption(MessageUtil.INSTANCE.getUserInfo(embyUserDtoUnblock, cacheUser) + "\n周五大赦天下 - 解除");
//                                                } else {
                                                if (cacheUser.getPoints() >= AuthorityUtil.botConfig.getUnblockPoints()) {
                                                    EmbyUtil.getInstance().deactivateUser(cacheUser, false);
                                                    cacheUser.setPoints(cacheUser.getPoints() - AuthorityUtil.botConfig.getUnblockPoints());
                                                    AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                                    editMessageCaption.setCaption(MessageUtil.INSTANCE.getUserInfo(embyUserDtoUnblock, cacheUser));
                                                } else {
//                                                    editMessageCaption.setCaption("非周五无法解封/积分不足");
                                                    editMessageCaption.setCaption("积分不足 " + AuthorityUtil.botConfig.getUnblockPoints());
                                                }
//                                                }
                                            }
                                            break;
                                        case "checkin":
                                            if (!cacheUser.haveEmby()) {
                                                editMessageCaption.setCaption("无账户, 无法操作");
                                            } else if (datas.length == 4) {
                                                editMessageCaption.setCaption(datas[2].equals("true") ? MessageUtil.INSTANCE.getRandomNumber(cacheUser) : "验证码错误");
                                                DeleteMessage deleteMessageCode = new DeleteMessage(editMessageCaption.getChatId(), Integer.parseInt(datas[3]));
                                                telegramClient.execute(deleteMessageCode);
                                            } else {
                                                if (MessageUtil.INSTANCE.isCheck(cacheUser)) {
                                                    editMessageCaption.setCaption("今日已签到");
                                                } else {
                                                    String code;
                                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                                    LineCaptcha lineService = CaptchaUtil.createLineCaptcha(1280, 720, 4, 300);
                                                    code = lineService.getCode();
                                                    lineService.write(outputStream);
                                                    InputFile inputFile = new InputFile();
                                                    inputFile.setMedia(new ByteArrayInputStream(outputStream.toByteArray()), "image.png");
                                                    SendPhoto sendPhoto = new SendPhoto(editMessageCaption.getChatId(), inputFile);
                                                    Message responseMessage = telegramClient.execute(sendPhoto);
                                                    nextMessageId = responseMessage.getMessageId().toString();
                                                    // 按钮处理
                                                    List<String> codes = new ArrayList<>();
                                                    codes.add(code);
                                                    while (codes.size() < 4) {
                                                        String randomCode = MessageUtil.INSTANCE.generateRandomCode(4);
                                                        if (!codes.contains(randomCode)) {
                                                            codes.add(randomCode);
                                                        }
                                                    }
                                                    Collections.shuffle(codes);
                                                    codes.forEach(it -> {
                                                        InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();
                                                        InlineKeyboardButton cache = new InlineKeyboardButton(it);
                                                        cache.setCallbackData("checkin " + userId + " " + it.equals(code) + " " + responseMessage.getMessageId());
                                                        inlineKeyboardRow.add(cache);
                                                        rows.add(inlineKeyboardRow);
                                                    });
                                                    editMessageCaption.setCaption("请选择正确的验证码");
                                                }
                                            }
                                            break;
                                        case "bind":
                                            if (cacheUser.haveEmby()) {
                                                editMessageCaption.setCaption("已有账户, 无需重复操作");
                                            } else {
                                                editMessageCaption.setCaption("请输入账户密码, 空格分隔(例: name pass)");
                                                RedisUtil.set(ConstantStrings.INSTANCE.getRedisTypeKey(userId.toString(), ""), command, null);
                                            }
                                            break;
                                        case "create":
                                            if (cacheUser.haveEmby()) {
                                                editMessageCaption.setCaption("已有账户, 无需重复操作");
                                            } else {
                                                editMessageCaption.setCaption("请输入账户名称, 新建用户为空密码(例: name)");
                                                RedisUtil.set(ConstantStrings.INSTANCE.getRedisTypeKey(userId.toString(), ""), command, null);
                                            }
                                            break;
                                        case "reset":
                                            if (!cacheUser.haveEmby()) {
                                                editMessageCaption.setCaption("无账户, 无法操作");
                                            } else {
                                                editMessageCaption.setCaption("请输入新密码(例: pass)");
                                                RedisUtil.set(ConstantStrings.INSTANCE.getRedisTypeKey(userId.toString(), ""), command, null);
                                            }
                                            break;
                                        case "shop":
                                            if (!cacheUser.getAdmin()) {
                                                editMessageCaption.setCaption("注册码生成仅管理可以用");
                                            } else {
                                                StringBuilder shopStringBuild = new StringBuilder();
                                                shopStringBuild.append("输入指令, 指令说明(数量 天数)(ex: 1 1)\n天数:\n-1 为白名单\n0 为注册码(注册后的剩余天数" + AuthorityUtil.botConfig.getExpDay() + ")\n大于 0 为续期天数");
                                                editMessageCaption.setCaption(shopStringBuild.toString());
                                                RedisUtil.set(ConstantStrings.INSTANCE.getRedisTypeKey(userId.toString(), ""), command, null);
                                            }
                                            break;
                                    }
                                } else editMessageCaption.setCaption(check);
                                if (!operatorsUser.getAdmin() && !userButtons.contains(command)) {
                                    answerCallbackQuery.setText("不是管理别瞎鸡儿点!");
                                    try {
                                        telegramClient.execute(answerCallbackQuery);
                                        return null;
                                    } catch (TelegramApiException e) {
                                        // nothing
                                    }
                                }
                                try {
                                    if (!command.equals("main") && !command.equals("flush") && !command.equals("openRegister")) {
                                        InlineKeyboardRow rowLineHome = new InlineKeyboardRow();
                                        // 添加主页按钮
                                        InlineKeyboardButton home = new InlineKeyboardButton("🏠返回主菜单");
                                        home.setCallbackData("main " + userId + (nextMessageId != null ? " " + nextMessageId : ""));
                                        rowLineHome.add(home);
                                        rows.add(rowLineHome);
                                        inlineKeyboardMarkup.setKeyboard(rows);
                                        editMessageCaption.setReplyMarkup(inlineKeyboardMarkup);
                                    } else {
                                        editMessageCaption.setReplyMarkup(MessageUtil.INSTANCE.getMainButton(cacheUser));
                                    }
                                    telegramClient.execute(editMessageCaption);
//                            sendMessageRequest.setText(outString.toString());
//                            try {
//                                telegramClient.execute(sendMessageRequest);
//                                switch (command) {
//                                    case "logoutDevice":
//                                        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
//                                        editMessageReplyMarkup.setChatId(update.getCallbackQuery().getMessage().getChatId());
//                                        editMessageReplyMarkup.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
//                                        List<EmbyDeviceDto> embyDeviceDtos = EmbyUtil.getInstance().viewingEquipment();
//                                        // 初始化按钮菜单
//                                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
//                                        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
//                                        String embyId = cacheUser.getEmbyVipId();
//                                        String tgId = cacheUser.getTgId();
//                                        embyDeviceDtos.stream().filter(embyDeviceDto -> embyDeviceDto.getLastUserId() != null && embyDeviceDto.getLastUserId().equals(embyId)).forEach(embyDeviceDto -> {
//                                            List<InlineKeyboardButton> rowLine = new ArrayList<>();
//                                            InlineKeyboardButton device = new InlineKeyboardButton();
//                                            device.setText(embyDeviceDto.getName() + " - " + embyDeviceDto.getAppName() + " " + embyDeviceDto.getAppVersion());
//                                            device.setCallbackData("logoutDevice " + tgId + " " + embyDeviceDto.getId());
//                                            rowLine.add(device);
//                                            rows.add(rowLine);
//                                        });
//                                        inlineKeyboardMarkup.setKeyboard(rows);
//                                        editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
//                                        telegramClient.execute(editMessageReplyMarkup);
//                                        break;
//                                }
//                                return;
                                } catch (TelegramApiException e) {
                                    log.error(e.toString());
                                }
                                return null;
                            }
                        }
                    } else {
                        answerCallbackQuery.setText("不是管理别瞎鸡儿点!");
                        try {
                            telegramClient.execute(answerCallbackQuery);
                            return null;
                        } catch (TelegramApiException e) {
                            outString = new StringBuilder("异常情况: " + e);
                        }
                    }
                } catch (TelegramApiException e) {
                    sendMessageRequest.setChatId("5340385875");
                    outString = new StringBuilder("异常情况: " + e);
                }
                if (StringUtils.isNotBlank(outString)) {
                    outString = new StringBuilder(MessageUtil.INSTANCE.sendUserMessage(operatorsUser, chatMember, outString.toString(), command, sendMessageRequest));
                    sendMessageRequest.setReplyToMessageId(null);
                    sendMessageRequest.setText(outString.toString());
                    try {
                        telegramClient.execute(sendMessageRequest);
                        // 发送信息给用户
                        if (ConstantStrings.INSTANCE.getSendUserCommands().contains(command)) {
                            sendMessageRequest.setChatId(cacheUser.getTgId());
                            telegramClient.execute(sendMessageRequest);
                        }
                    } catch (TelegramApiException e) {
                        // 开摆
                    }
                }
            }
            // 检查更新是否有消息
            if (update.hasMessage()) {
                Message message = update.getMessage();
                // check if the message has text. it could also contain for example a location ( message.hasLocation() )
                if (message.hasText()) {
                    if (message.getText().contains("线路") || message.getText().toLowerCase().contains("wiki")) {
                        SendMessage sendMessageRequest = new SendMessage(message.getChatId().toString(), "");
                        if (message.isTopicMessage())
                            sendMessageRequest.setMessageThreadId(message.getMessageThreadId());
                        sendMessageRequest.setReplyToMessageId(message.getMessageId());
                        sendMessageRequest.setText("查看线路在 bot 内, 自己看指令");
                        sendMessageRequest.setProtectContent(true);
                        List<InlineKeyboardRow> rows = new ArrayList<>();
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);


                        InlineKeyboardRow rowLine1 = new InlineKeyboardRow();
                        InlineKeyboardButton wiki = new InlineKeyboardButton(AuthorityUtil.botConfig.wikiName);
                        wiki.setUrl(AuthorityUtil.botConfig.wikiUrl);
                        rowLine1.add(wiki);
                        rows.add(rowLine1);
                        inlineKeyboardMarkup.setKeyboard(rows);
                        sendMessageRequest.setReplyMarkup(inlineKeyboardMarkup);
                        try {
                            telegramClient.execute(sendMessageRequest);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (message.getText().contains(AuthorityUtil.botConfig.groupNick)) {
                        SendMessage sendMessageRequest = new SendMessage(message.getChatId().toString(), "");
                        if (message.isTopicMessage())
                            sendMessageRequest.setMessageThreadId(message.getMessageThreadId());
                        sendMessageRequest.setReplyToMessageId(message.getMessageId());
                        sendMessageRequest.setText("叫我做什?");
                        try {
                            telegramClient.execute(sendMessageRequest);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (!message.isReply() && ConstantStrings.INSTANCE.isAdminNoReplyCommands(message.getText().split(" ")[0])) {
                        String outString = "仅管理可使用此命令";
                        // 获取发送人与转发信息的用户信息
                        User sendUser = message.getFrom();
                        // create a object that contains the information to send back the message
                        SendMessage sendMessageRequest = new SendMessage(message.getChatId().toString(), "");
                        if (message.isTopicMessage())
                            sendMessageRequest.setMessageThreadId(message.getMessageThreadId());
//                    sendMessageRequest.setReplyToMessageId(message.getMessageId());
                        DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
                        ChatMember chatMember = null;
                        boolean needForward = false;
                        // 使用者判断
                        com.ocd.bean.mysql.User sqlUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, sendUser.getId()));
                        if (sqlUser.getAdmin()) {
                            String messageData = message.getText().replaceAll("['\n']", " ");
                            String[] messageDatas = messageData.split(" ");
                            String content = messageData.replace(messageDatas[0] + " ", "");
                            if (messageDatas[0].equals("channel")) {
                                SendMessage sendMessage = new SendMessage(AuthorityUtil.botConfig.channel, "");
                                sendMessage.setText(content);
                                List<InlineKeyboardRow> rows = new ArrayList<>();
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);
                                InlineKeyboardRow rowLine1 = new InlineKeyboardRow();
                                InlineKeyboardButton ban = new InlineKeyboardButton("💩️ 删除");
                                ban.setCallbackData("cancel " + sendUser.getId());
                                rowLine1.add(ban);
                                rows.add(rowLine1);
                                inlineKeyboardMarkup.setKeyboard(rows);
                                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    throw new RuntimeException(e);
                                }
                                return null;
                            } else if (messageDatas[0].equals("find")) {
                                com.ocd.bean.mysql.User cacheUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getEmbyName, content));
                                if (cacheUser == null)
                                    cacheUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, content));
                                if (cacheUser == null) {
                                    outString = "无此用户";
                                } else {
                                    GetChatMember getChatMember = new GetChatMember(AuthorityUtil.botConfig.groupId, Long.parseLong(cacheUser.getTgId()));
                                    try {
                                        chatMember = telegramClient.execute(getChatMember);
                                        outString = id(cacheUser, chatMember.getUser(), deleteMessage, outString, sendMessageRequest, message);
                                        outString = MessageUtil.INSTANCE.escapeQueryChars(outString);
                                    } catch (TelegramApiException e) {
                                        outString = "查找异常, 找开发者解决";
                                    }
                                }
                            } else if (messageDatas[0].equals("findin")) {
                                Invitecode invitecode = AuthorityUtil.invitecodeService.invitecodeMapper.selectOne(new QueryWrapper<Invitecode>().lambda().eq(Invitecode::getInvitecode, content));
                                if (invitecode != null) {
                                    com.ocd.bean.mysql.User cacheUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, invitecode.getTgId()));
                                    if (cacheUser == null) {
                                        outString = "无此用户";
                                    } else {
                                        GetChatMember getChatMember = new GetChatMember(AuthorityUtil.botConfig.groupId, Long.parseLong(cacheUser.getTgId()));
                                        try {
                                            chatMember = telegramClient.execute(getChatMember);
                                            outString = id(cacheUser, chatMember.getUser(), deleteMessage, outString, sendMessageRequest, message);
                                            outString = MessageUtil.INSTANCE.escapeQueryChars(outString);
                                        } catch (TelegramApiException e) {
                                            outString = "查找异常, 找开发者解决";
                                        }
                                    }
                                } else outString = content + " 邀请码未存储使用记录";
                            } else if (messageDatas[0].equals("invite")) {
                                String[] inviteParams = content.split(" ");
                                if (inviteParams.length != 3) {
                                    outString = "参数错误 正确指令 ex: 'invite 邀请人数 邀请有效天数 是否转发到频道 \\= invite 1 1 1'";
                                } else {
                                    try {
                                        Integer num = Integer.valueOf(inviteParams[0]);
                                        Integer dateOffset = Integer.valueOf(inviteParams[1]);
                                        Integer forward = Integer.valueOf(inviteParams[2]);
                                        // 多用户邀请/是否发送群组
                                        CreateChatInviteLink createChatInviteLink = new CreateChatInviteLink(AuthorityUtil.botConfig.groupId);
                                        createChatInviteLink.setMemberLimit(num);
                                        Date date = DateUtil.endOfDay(DateUtil.offsetDay(new Date(), dateOffset));
                                        createChatInviteLink.setExpireDate((int) (date.getTime() / 1000));
                                        try {
                                            telegramClient.execute(deleteMessage);
                                            ChatInviteLink chatInviteLink = telegramClient.execute(createChatInviteLink);
                                            outString = "[" + num + "人邀请链接](" + chatInviteLink.getInviteLink() + ") 链接有效期至: " + FormatUtil.INSTANCE.dateToString(date);
                                            needForward = forward > 0;
                                        } catch (TelegramApiException e) {
                                            outString = "invite 出现异常" + e;
                                        }
                                    } catch (Exception e) {
                                        outString = "参数错误 正确指令 ex: 'invite 邀请人数 邀请有效天数 是否转发到频道 \\= invite 1 1 1'";
                                    }
                                }
                            } else if (messageDatas[0].equals("inviteh")) {
                                String[] inviteParams = content.split(" ");
                                if (inviteParams.length != 3) {
                                    outString = "参数错误 正确指令 ex: 'inviteh 邀请人数 邀请有效小时数 是否转发到频道 \\= inviteh 1 1 1'";
                                } else {
                                    try {
                                        Integer num = Integer.valueOf(inviteParams[0]);
                                        Integer dateOffset = Integer.valueOf(inviteParams[1]);
                                        Integer forward = Integer.valueOf(inviteParams[2]);
                                        // 多用户邀请/是否发送群组
                                        CreateChatInviteLink createChatInviteLink = new CreateChatInviteLink(AuthorityUtil.botConfig.groupId);
                                        createChatInviteLink.setMemberLimit(num);
                                        Date date = DateUtil.offsetHour(new Date(), dateOffset);
                                        createChatInviteLink.setExpireDate((int) (date.getTime() / 1000));
                                        try {
                                            telegramClient.execute(deleteMessage);
                                            ChatInviteLink chatInviteLink = telegramClient.execute(createChatInviteLink);
                                            outString = "[" + num + "人邀请链接](" + chatInviteLink.getInviteLink() + ") 链接有效期至: " + FormatUtil.INSTANCE.dateToString(date);
                                            needForward = forward > 0;
                                        } catch (TelegramApiException e) {
                                            outString = "invite 出现异常" + e;
                                        }
                                    } catch (Exception e) {
                                        outString = "参数错误 正确指令 ex: 'inviteh 邀请人数 邀请有效小时数 是否转发到频道 \\= inviteh 1 1 1'";
                                    }
                                }
                            }
                        }
                        outString = StringUtils.equals("仅管理可使用此命令", outString) ? outString : MessageUtil.INSTANCE.sendUserMessage(sendUser, chatMember == null ? null : chatMember.getUser(), outString, message.getText(), sendMessageRequest);
                        sendMessageRequest.setReplyToMessageId(null);
                        sendMessageRequest.setText(outString);
                        try {
                            Message messageEnd = telegramClient.execute(sendMessageRequest);
                            if (needForward) {
                                ForwardMessage forwardMessage = new ForwardMessage(AuthorityUtil.botConfig.channel, messageEnd.getChatId().toString(), messageEnd.getMessageId());
                                telegramClient.execute(forwardMessage);
                            }
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (message.isReply() && ConstantStrings.INSTANCE.isAdminCommands(message.getText())) {
                        String outString = "仅管理可使用此命令";
                        // 获取发送人与转发信息的用户信息
                        User sendUser = message.getFrom();
                        User replyUser = message.getReplyToMessage().getFrom();
                        // create a object that contains the information to send back the message
                        SendMessage sendMessageRequest = new SendMessage(message.getChatId().toString(), "");
                        if (message.isTopicMessage())
                            sendMessageRequest.setMessageThreadId(message.getMessageThreadId());
//                    sendMessageRequest.setReplyToMessageId(message.getMessageId());
                        DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
                        // 使用者判断
                        com.ocd.bean.mysql.User sqlUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().eq("tg_id", sendUser.getId()));
                        if (sqlUser.getAdmin()) {
                            try {
                                com.ocd.bean.mysql.User cacheUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().eq("tg_id", replyUser.getId()));
                                String[] datas = message.getText().split(" ");
                                switch (datas[0]) {
                                    case "id":
                                        outString = id(cacheUser, replyUser, deleteMessage, outString, sendMessageRequest, message);
                                        break;
                                    case "pin":
                                        PinChatMessage pinChatMessage = new PinChatMessage(message.getChatId().toString(), message.getReplyToMessage().getMessageId());
                                        pinChatMessage.setDisableNotification(false);
                                        telegramClient.execute(pinChatMessage);
                                        telegramClient.execute(deleteMessage);
                                        break;
                                    case "unpin":
                                        UnpinChatMessage unpinChatMessage = new UnpinChatMessage(message.getChatId().toString(), message.getReplyToMessage().getMessageId(), "");
                                        telegramClient.execute(unpinChatMessage);
                                        telegramClient.execute(deleteMessage);
                                        break;
                                    case "renew":
                                        try {
                                            int days = Integer.parseInt(datas[1]);
                                            if (cacheUser.haveEmby() || cacheUser.getDeactivate())
                                                outString = "用户暂无观影账号/账号已停用无法操作";
                                            else {
                                                Date cacheDate = cacheUser.getExpTime();
                                                cacheUser.setExpTime(DateUtil.offsetDay(cacheDate, days));
                                                AuthorityUtil.userService.userMapper.updateById(cacheUser);
                                                outString = String.format("用户 %s 账号有效期调整由 %s 调整至 %s",
                                                        cacheUser.getTgId(), FormatUtil.INSTANCE.dateToString(cacheDate), cacheUser.getExpTime());
                                            }
                                        } catch (NumberFormatException e) {
                                            outString = "renew 参数错误";
                                        }
                                        break;
                                }
                            } catch (TelegramApiException e) {
//                                outString = "ban 失败 可能已经被 ban 过了?: " + JSON.toJSONString(replyUser);
                                sendMessageRequest.setChatId("5340385875");
                                outString = "异常情况: " + e;
                            }
                        }
                        if (!ConstantStrings.INSTANCE.isNotPermissionCommands(message.getText())) {
                            outString = StringUtils.equals("仅管理可使用此命令", outString) ? outString : MessageUtil.INSTANCE.sendUserMessage(sendUser, message, MessageUtil.INSTANCE.escapeQueryChars(outString), message.getText(), sendMessageRequest);
                            sendMessageRequest.setReplyToMessageId(null);
                            sendMessageRequest.setText(outString);
                            try {
                                telegramClient.execute(sendMessageRequest);
                            } catch (TelegramApiException e) {
                                // 开摆
                            }
                        }
                    } else if (update.getMessage().getChat().isUserChat()) {
                        Object doing = RedisUtil.get(ConstantStrings.INSTANCE.getRedisTypeKey(update.getMessage().getFrom().getId().toString(), ""));
                        com.ocd.bean.mysql.User operatorsUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, update.getMessage().getFrom().getId()));
                        if (operatorsUser != null && doing != null) {
                            String[] datas = update.getMessage().getText().split(" ");
                            SendMessage sendMessage = new SendMessage(update.getMessage().getFrom().getId().toString(), "");
                            String outDoing = doing + "已完成";
                            String cache = AuthorityUtil.checkTgUser(update.getMessage().getFrom());
                            if (cache != null)
                                outDoing = cache;
                            else
                                switch (doing.toString()) {
                                    case "bind":
                                        if (datas.length != 2) {
                                            outDoing = "参数错误! ex: name pass";
                                        } else {
                                            String embyUsername = datas[0];
                                            String embyPassword = datas[1];
                                            EmbyUserResult embyUserResult = EmbyUtil.getInstance().authenticateByName(embyUsername, embyPassword);
                                            if (embyUserResult == null || StringUtils.isBlank(embyUserResult.getId()))
                                                outDoing = "校验失败, 请确认账号密码";
                                            else if (operatorsUser.haveEmby())
                                                outDoing = "已有" + AuthorityUtil.botConfig.groupNick + "账号, 无需重复绑定";
                                            else if (AuthorityUtil.userService.userMapper.exists(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getEmbyName, embyUsername).ne(com.ocd.bean.mysql.User::getTgId, operatorsUser.getTgId()))) {
                                                outDoing = "此账号已被他人绑定, 换绑申请已发群内, 联系管理处理";
                                                MessageUtil.INSTANCE.sendChangeBindMessage(telegramClient, operatorsUser, embyUsername);
                                            } else {
                                                operatorsUser.updateEmbyByEmbyUser(embyUserResult);
                                                AuthorityUtil.userService.userMapper.updateById(operatorsUser);
                                                outDoing = "成功绑定" + AuthorityUtil.botConfig.groupNick + ", /start 操作面板";
                                            }
                                        }
                                        break;
                                    case "create":
                                        String embyName = datas[0];
                                        com.ocd.bean.mysql.User sqlUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, update.getMessage().getFrom().getId()));
                                        if (sqlUser.haveEmby())
                                            outDoing = "tg 已绑定过观影账号, /start 查看信息";
                                        else if (sqlUser.getExchange() == null && !AuthorityUtil.openRegister || ((AuthorityUtil.openRegister && EmbyUtil.getInstance().getCanRegisterSize() <= 0))) {
                                            outDoing = "无注册权限";
                                        } else {
                                            if (!EmbyUtil.getInstance().register(sqlUser, embyName)) {
                                                outDoing = "名称已占用, 请重新开始注册";
                                            } else {
                                                if (sqlUser.getExchange() != null) {
                                                    Invitecode invitecode = AuthorityUtil.invitecodeService.invitecodeMapper.selectOne(new QueryWrapper<Invitecode>().lambda().eq(Invitecode::getInvitecode, sqlUser.getExchange()));
                                                    if (invitecode == null) {
                                                        outDoing = "账单异常, 联系开发者处理";
                                                    } else {
                                                        outDoing = "开号成功, /start 查看信息\n默认密码空, 请及时修改密码";
                                                    }
                                                } else {
                                                    sqlUser.addExpDate(AuthorityUtil.botConfig.getExpDay());
                                                    outDoing = "开号成功, /start 查看信息\n默认密码空, 请及时修改密码";
                                                }
                                            }
                                        }
                                        break;
                                    case "shop":
                                        if (datas.length != 2)
                                            outDoing = "参数错误";
                                        else
                                            try {
                                                Integer count = Integer.parseInt(datas[0]);
                                                Integer days = Integer.parseInt(datas[1]);
                                                com.ocd.bean.mysql.User shopUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, update.getMessage().getFrom().getId()));
                                                if (!shopUser.getAdmin()) {
                                                    outDoing = "注册码生成仅管理可以用";
                                                } else {
                                                    StringBuilder cacheString = new StringBuilder();
                                                    for (int i = 0; i < count; i++) {
                                                        Invitecode invitecode = new Invitecode(AuthorityUtil.invitecode(), days);
                                                        AuthorityUtil.invitecodeService.invitecodeMapper.insert(invitecode);
                                                        if (days == -1)
                                                            cacheString.append("生成邀请链接(♾️)  " + " https://t.me/" + AuthorityUtil.botConfig.name + "?start=" + invitecode.getInvitecode() + "\n");
                                                        if (days == 0)
                                                            cacheString.append("生成邀请链接(注册码)  " + " https://t.me/" + AuthorityUtil.botConfig.name + "?start=" + invitecode.getInvitecode() + "\n");
                                                        else
                                                            cacheString.append("生成邀请链接(续期码 " + days + " 天)  " + " https://t.me/" + AuthorityUtil.botConfig.name + "?start=" + invitecode.getInvitecode() + "\n");
                                                    }
                                                    outDoing = cacheString.toString();
                                                }
                                            } catch (NumberFormatException e) {
                                                outDoing = "参数错误";
                                            }
                                        break;
                                    case "reset":
                                        if (!operatorsUser.haveEmby()) outDoing = "用户未注册";
                                        else {
                                            EmbyUtil.getInstance().resetPass(operatorsUser, datas.length > 0 ? datas[0] : null);
                                            outDoing = datas.length > 0 ? "密码已修改" : "密码已清除, 请尽快登录网页填写新密码";
                                        }
                                        break;
                                }
                            RedisUtil.del(ConstantStrings.INSTANCE.getRedisTypeKey(update.getMessage().getFrom().getId().toString(), ""));
                            sendMessage.setText(outDoing);
                            try {
                                telegramClient.execute(sendMessage);
                            } catch (TelegramApiException ignored) {
                            }
                        }
                    } else if (message.isReply() && AuthorityUtil.botConfig.token.contains(message.getReplyToMessage().getFrom().getId().toString())) {
                        // 随便聊天的 api
                        SendMessage sendMessageRequest = new SendMessage(message.getChatId().toString(), "");
                        if (message.isTopicMessage())
                            sendMessageRequest.setMessageThreadId(message.getMessageThreadId());
                        sendMessageRequest.setReplyToMessageId(message.getMessageId());
                        sendMessageRequest.setText(AuthorityUtil.chatOther(message.getText()));
                        try {
                            telegramClient.execute(sendMessageRequest);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            return null;
        });
    }

    public String id(com.ocd.bean.mysql.User cacheUser, User replyUser, DeleteMessage deleteMessage, String
            outString, SendMessage sendMessageRequest, Message message) throws TelegramApiException {
        if (cacheUser == null) {
            com.ocd.bean.mysql.User user = new com.ocd.bean.mysql.User(replyUser.getId());
            user.setStartBot(false);
            int id = AuthorityUtil.userService.createUser(user);
            cacheUser = AuthorityUtil.userService.userMapper.selectById(id);
        }
        telegramClient.execute(deleteMessage);
        EmbyUserResult embyUserDto = EmbyUtil.getInstance().getUserByEmbyId(cacheUser.getEmbyId());
        outString = MessageUtil.INSTANCE.getUserInfo(embyUserDto, cacheUser, true);
        sendMessageRequest.setProtectContent(true);
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);

        InlineKeyboardRow rowLine1 = new InlineKeyboardRow();
        if (cacheUser.haveEmby()) {
            InlineKeyboardButton block = new InlineKeyboardButton(cacheUser.getDeactivate() ? "🔓解封观影账号" : "🔒封禁观影账号");
            block.setCallbackData("block " + replyUser.getId());
            rowLine1.add(block);
            InlineKeyboardButton del = new InlineKeyboardButton("❗ 删除观影账号");
            del.setCallbackData("del " + replyUser.getId());
            if (!cacheUser.getAdmin()) rowLine1.add(del);
        }

        InlineKeyboardRow rowLine2 = new InlineKeyboardRow();
        InlineKeyboardButton warn = new InlineKeyboardButton("⚠️ 警告");
        warn.setCallbackData("warn " + replyUser.getId());
        InlineKeyboardButton ban = new InlineKeyboardButton("");
        GetChatMember getChatMember = new GetChatMember(message.getChatId().toString(), replyUser.getId());
        ChatMember chatMember = telegramClient.execute(getChatMember);
        if (StringUtils.equals(chatMember.getStatus(), "kicked") || cacheUser.getUserType() == 3) {
            ban.setText("\uD83C\uDD71️ 解封");
            ban.setCallbackData("unban " + replyUser.getId());
        } else {
            rowLine2.add(warn);
            ban.setText("\uD83C\uDD71️ 封禁");
            ban.setCallbackData("ban " + replyUser.getId());
        }
        rowLine2.add(ban);

        InlineKeyboardRow rowLine3 = new InlineKeyboardRow();
        InlineKeyboardButton admin = new InlineKeyboardButton("");
        if (!cacheUser.getAdmin() && !replyUser.getIsBot()) {
            admin.setText("⭐ 增加管理");
            admin.setCallbackData("admin " + replyUser.getId());
        } else {
            admin.setText("\uD83C\uDE1A 移除管理");
            admin.setCallbackData("unadmin " + replyUser.getId());
        }
        if (!cacheUser.getSuperAdmin()) rowLine3.add(admin);

        InlineKeyboardRow rowLine4 = new InlineKeyboardRow();
        InlineKeyboardButton gift = new InlineKeyboardButton("\uD83C\uDF00 赠号(注册码)");
        gift.setCallbackData("gift " + replyUser.getId());
        InlineKeyboardButton mail = new InlineKeyboardButton("\uD83C\uDF00 赠号(白名单)");
        mail.setCallbackData("mail " + replyUser.getId());
        if (cacheUser.getUserType() != 2) {
            rowLine4.add(gift);
            rowLine4.add(mail);
        }

        InlineKeyboardRow rowLineLast = new InlineKeyboardRow();
        InlineKeyboardButton cancel = new InlineKeyboardButton("✖️ 取消");
        cancel.setCallbackData("cancel " + replyUser.getId());
        rowLineLast.add(cancel);

        rows.add(rowLine1);
        if (!cacheUser.getAdmin()) rows.add(rowLine2);
        rows.add(rowLine3);
        rows.add(rowLine4);
        rows.add(rowLineLast);
        inlineKeyboardMarkup.setKeyboard(rows);
        sendMessageRequest.setReplyMarkup(inlineKeyboardMarkup);
        return outString;
    }
}