package com.ocd.controller.commands;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ocd.controller.util.AuthorityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * @author OCD
 * @date 2022/12/22 9:37
 * Description:
 */
@Slf4j
public class UserNotifyCommand extends BotCommand {

    public UserNotifyCommand() {
        super("notify", "仅管理可用");
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        if (!user.getId().equals(chat.getId())) return;
        SendMessage answer = new SendMessage(chat.getId().toString(), "");
        answer.setProtectContent(true);
        String outString = AuthorityUtil.checkTgUser(user);

        if (outString == null) {
            if (user.getIsBot()) outString = "bot 滚蛋";
            else {
                com.ocd.bean.mysql.User sqlUser = AuthorityUtil.userService.userMapper.selectOne(new QueryWrapper<com.ocd.bean.mysql.User>().eq("tg_id", user.getId()));
                if (sqlUser == null) outString = "不是探花用户!";
                else if (!sqlUser.getSuperAdmin()) {
                    outString = "政工办管理才允许使用, 别试了";
                } else {
                    SendMessage outUser = new SendMessage("", "");
                    AuthorityUtil.userService.userMapper.selectList(new QueryWrapper<com.ocd.bean.mysql.User>().eq("start_bot", 1).eq("vip_deactivate", 0).isNotNull("emby_vip_id")).forEach(it -> {
                        try {
                            outUser.setChatId(it.getTgId());
                            outUser.setText("🪧 公告: " + StringUtils.join(strings, " "));
                            outUser.setProtectContent(true);
                            telegramClient.execute(outUser);
                        } catch (TelegramApiException e) {
                            log.error("全频通知发送异常: " + e.getLocalizedMessage(), e);
                        }
                    });
                    outString = "全频通知发送完成";
                }
            }
        }

        answer.setText(outString);
        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}