package com.ocd.controller.commands;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.isen.bean.constant.ConstantStrings;
import com.ocd.bean.mysql.Invitecode;
import com.ocd.controller.util.AuthorityUtil;
import com.ocd.controller.util.EmbyUtil;
import com.ocd.controller.util.MessageUtil;
import com.ocd.controller.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

/**
 * This commands starts the conversation with the bot
 *
 * @author OCD
 */
@Slf4j
public class StartCommand extends BotCommand {

    private final ICommandRegistry commandRegistry;

    public StartCommand(ICommandRegistry commandRegistry) {
        super("start", "开始对话/查看用户可用指令");
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        if (!chat.getId().equals(user.getId())) return;
        MessageUtil.INSTANCE.launchInGlobalScope((Runnable) -> {
            SendPhoto sendPhotoRequest = new SendPhoto(chat.getId().toString(), MessageUtil.INSTANCE.getHeadImageAsInputFile());
            String outString = AuthorityUtil.checkTgUserStart(user);
            if (outString == null)
                outString = AuthorityUtil.checkChatMember(user.getId(), chat.getId(), telegramClient);
            if (outString != null) {
                try {
                    sendPhotoRequest.setCaption(outString);
                    telegramClient.execute(sendPhotoRequest);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
                return null;
            }
            Object doing = RedisUtil.get(ConstantStrings.INSTANCE.getRedisTypeKey(user.getId().toString(), ""));
            if (doing != null) {
                RedisUtil.del(ConstantStrings.INSTANCE.getRedisTypeKey(user.getId().toString(), ""));
                outString = "已取消 " + doing + "操作, 如正在使用注册码, 重新点击注册码链接";
                try {
                    sendPhotoRequest.setCaption(outString);
                    telegramClient.execute(sendPhotoRequest);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
                return null;
            }

//        StringBuilder helpMessageBuilder = new StringBuilder();

//        sendPhotoRequest.setParseMode("HTML");
            com.ocd.bean.mysql.User sqlUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, user.getId()));
            if (sqlUser == null) {
                // 通过 tg id 创建用户信息
                sqlUser = new com.ocd.bean.mysql.User(user.getId());
                AuthorityUtil.userService.createUser(sqlUser);
            } else {
                sqlUser.setStartBot(true);
                AuthorityUtil.userService.userMapper.updateById(sqlUser);
            }
            if (strings == null || strings.length == 0) {
                final List<org.telegram.telegrambots.meta.api.objects.commands.BotCommand> botCommandList = new ArrayList<>();
                for (IBotCommand botCommand : commandRegistry.getRegisteredCommands()) {
                    if (!sqlUser.getAdmin() && ConstantStrings.INSTANCE.getBotCommandHide().contains(botCommand.getCommandIdentifier()))
                        continue;
//                helpMessageBuilder.append(botCommand.toString()).append("\n\n");
                    if (ConstantStrings.INSTANCE.getBotCommandHide().contains(botCommand.getCommandIdentifier()))
                        continue;
                    botCommandList.add(new org.telegram.telegrambots.meta.api.objects.commands.BotCommand(botCommand.getCommandIdentifier(), botCommand.getDescription()));
                }
//            if (sqlUser.getAdmin())
//                helpMessageBuilder.append("群内指令: id/pin/unpin/find/invite/channel 使用方式详看探花TV政工办");
//            sendPhotoRequest.setCaption(helpMessageBuilder.toString());

                sendPhotoRequest.setCaption(MessageUtil.INSTANCE.getStartMessage(user.getFirstName()));
                sendPhotoRequest.setReplyMarkup(MessageUtil.INSTANCE.getMainButton(sqlUser));

                try {
                    if (sqlUser.getAdmin()) {
                        SetMyCommands setMyCommands = new SetMyCommands(botCommandList);
                        telegramClient.execute(setMyCommands);
                    }
                    telegramClient.execute(sendPhotoRequest);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }

            // 兑换
            if (strings.length != 0) {
                Invitecode invitecode = AuthorityUtil.invitecodeService.invitecodeMapper.selectOne(new QueryWrapper<Invitecode>().lambda().eq(Invitecode::getInvitecode, strings[0]));
                if (invitecode == null) sendPhotoRequest.setCaption("邀请码无效");
                else if (invitecode.getUsed()) sendPhotoRequest.setCaption("邀请码已被使用");
                else if (sqlUser.haveEmby()) {
                    if (sqlUser.getUserType() == 2) {
                        sendPhotoRequest.setCaption("已是 " + AuthorityUtil.botConfig.groupNick + " ♾️ 用户, 无需续期/兑换");
                    } else if (invitecode.getDays() == 0) {
                        sendPhotoRequest.setCaption("此为注册码, 无法续期");
                    } else {
                        if (invitecode.getDays() == -1) sqlUser.setUserType(2);
                        else sqlUser.setUserType(1);
                        sqlUser.setExchange(invitecode.getInvitecode());
                        invitecode.sUse(sqlUser.getTgId());
                        EmbyUtil.getInstance().deactivateUser(sqlUser, false);
                        AuthorityUtil.invitecodeService.invitecodeMapper.updateById(invitecode);
                        sqlUser.addExpDate(invitecode.getDays());
                        AuthorityUtil.userService.userMapper.updateById(sqlUser);
                        String outStr = "兑换成功, /start 自行操作";
                        sendPhotoRequest.setCaption(outStr);
                    }
                } else {
                    if (!AuthorityUtil.botConfig.getAllowDirectRegister() && invitecode.getDays() != 0) {
                        sendPhotoRequest.setCaption("此为续期/白名单码, 无法注册");
                    } else if (sqlUser.getExchange() != null) {
                        sendPhotoRequest.setCaption("当前有未使用的兑换码, 先开号再使用新兑换码");
                    } else {
                        sqlUser.setExchange(invitecode.getInvitecode());
                        if (invitecode.getDays() == -1) sqlUser.setUserType(2);
                        else sqlUser.setUserType(1);
                        invitecode.sUse(sqlUser.getTgId());
                        AuthorityUtil.invitecodeService.invitecodeMapper.updateById(invitecode);
                        sqlUser.addExpDate(invitecode.getDays());
                        AuthorityUtil.userService.userMapper.updateById(sqlUser);
                        sendPhotoRequest.setCaption("兑换码使用成功, /start 自行注册");
                    }
                }

                try {
                    telegramClient.execute(sendPhotoRequest);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
            return null;
        });
    }
}