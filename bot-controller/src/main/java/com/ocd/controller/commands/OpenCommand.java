package com.ocd.controller.commands;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ocd.controller.util.AuthorityUtil;
import com.ocd.controller.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * @author ch.hu
 * @date 2024/12/25 09:40
 * Description:
 */
@Slf4j
public class OpenCommand extends BotCommand {


    public OpenCommand() {
        super("open", "设置注册人数上限(仅限超管 ex: /open 1000)");
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        MessageUtil.INSTANCE.launchInGlobalScope((Runnable) -> {
            com.ocd.bean.mysql.User sqlUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().eq(com.ocd.bean.mysql.User::getTgId, user.getId()));
            if (sqlUser == null || !sqlUser.getSuperAdmin()) {
                try {
                    SendMessage sendMessage = new SendMessage(user.getId().toString(), "仅超管可操作");
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            } else {
                SendMessage sendMessage = new SendMessage(user.getId().toString(), "指令错误 ex: /open 最大注册人数");
                if (strings.length > 0) {
                    try {
                        AuthorityUtil.accountCount = Integer.parseInt(strings[0]);
                        sendMessage.setText("设置成功, 当前最大注册人数(此值需配合开放注册使用): " + AuthorityUtil.accountCount);
                    } catch (NumberFormatException ignored) {
                    }
                }
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
            return null;
        });
    }
}
