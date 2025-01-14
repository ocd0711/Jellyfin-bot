package com.ocd.controller.commands;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ocd.bean.dto.excel.DevicesExcel;
import com.ocd.bean.dto.result.EmbyDeviceResult;
import com.ocd.controller.util.AuthorityUtil;
import com.ocd.controller.util.EmbyUtil;
import com.ocd.controller.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.ttzero.excel.entity.ListSheet;
import org.ttzero.excel.entity.Workbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This commands starts the conversation with the bot
 *
 * @author OCD
 */
@Slf4j
public class StatisticsCommand extends BotCommand {


    public StatisticsCommand() {
        super("st", "统计用户设备(仅限超管)");
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
                try {
                    SendMessage sendMessage = new SendMessage(user.getId().toString(), "处理完成后会私发给操作人");
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
                List<com.ocd.bean.mysql.User> userList = AuthorityUtil.userService.userMapper.selectList(new QueryWrapper<com.ocd.bean.mysql.User>().lambda().in(com.ocd.bean.mysql.User::getUserType, List.of(1, 2)));
                ArrayList<DevicesExcel> devicesExcelList = new java.util.ArrayList<>(userList.stream().map(com.ocd.bean.mysql.User::getDeviceExcel).toList());
                devicesExcelList.forEach(devicesExcel -> {
                    List<EmbyDeviceResult> playbackRecords = EmbyUtil.getInstance().viewingEquipment(devicesExcel.getEmbyId());
                    devicesExcel.setDeviceCount(playbackRecords.stream().count());
                    devicesExcel.setDeviceInfo(String.join("\n", playbackRecords.stream().map(EmbyDeviceResult::toString).toList()));
                });
                devicesExcelList.sort(Comparator.comparingLong(DevicesExcel::getDeviceCount).reversed());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    new Workbook("用户设备统计")
                            .addSheet(new ListSheet<>(devicesExcelList))
                            .writeTo(byteArrayOutputStream);
                    SendDocument sendDocument = new SendDocument(user.getId().toString(), new InputFile(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "devices_report.xlsx"));
                    sendDocument.setCaption("用户设备统计文件");
                    telegramClient.execute(sendDocument);
                } catch (IOException | TelegramApiException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
            return null;
        });
    }
}