package com.ocd.controller.commands;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ocd.controller.util.AuthorityUtil;
import com.ocd.util.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

/**
 * @author OCD
 * @date 2022/12/22 9:37
 * Description:
 */
@Slf4j
public class RenewAllCommand extends BotCommand {

    public RenewAllCommand() {
        super("renewall", "一键派送天数给所有未封禁的用户(超管)(/rewnewall days, ex: /renewall 10)");
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
                if (sqlUser == null) outString = "不是" + AuthorityUtil.botConfig.groupNick + "用户!";
                else if (!sqlUser.getSuperAdmin()) {
                    outString = "超管才允许使用";
                } else {
                    SendMessage outUser = new SendMessage("", "");
                    if (strings.length < 1) {
                        outString = "指令参数错误, /rewnewall days, ex: /renewall 10";
                    } else {
                        try {
                            int days = Integer.parseInt(strings[0]);
                            outString = "未收到完成指令/重启 bot 前, 不要重复调用此指令";
                            answer.setText(outString);
                            try {
                                telegramClient.execute(answer);
                            } catch (TelegramApiException e) {
                                log.error(e.getLocalizedMessage(), e);
                            }
                            AuthorityUtil.userService.userMapper.selectList(new QueryWrapper<com.ocd.bean.mysql.User>().lambda()
                                    .in(com.ocd.bean.mysql.User::getUserType, List.of(1, 2)).eq(com.ocd.bean.mysql.User::getDeactivate, 0)).forEach(it -> {
                                try {
                                    it.addExpDate(days);
                                    AuthorityUtil.userService.userMapper.updateById(it);
                                    outUser.setChatId(it.getTgId());
                                    outUser.setText(String.format("超管 %s 批量赠送时长 %s 天, 当前有效期至 %s", user.getFirstName(), days, FormatUtil.INSTANCE.dateToString(it.getExpTime())));
                                    outUser.setProtectContent(true);
                                    telegramClient.execute(outUser);
                                } catch (TelegramApiException e) {
                                    log.error("RenewAllCommand 发送异常: " + e.getLocalizedMessage(), e);
                                }
                            });
                            outString = "RenewAllCommand 全部发送完成";
                        } catch (NumberFormatException e) {
                            outString = "指令参数错误, /rewnewall days, ex: /renewall 10";
                        }
                    }
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