package com.ocd.controller.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ocd.bean.dto.jellby.PlaybackRecord;
import com.ocd.bean.dto.jellby.PlaybackUserRecord;
import com.ocd.bean.dto.result.EmbyUserResult;
import com.ocd.bean.dto.result.PlaybackShowsResult;
import com.ocd.bean.mysql.User;
import com.ocd.controller.util.*;
import com.ocd.util.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author OCD
 * @date 2022/04/12 16:01
 * Description:
 * 定时任务处理
 */
@Component
@Slf4j
public class TimerTask {

    private static Logger logger = LoggerFactory.getLogger(TimerTask.class);

    /**
     * 0 点判断 emby 库内用户是否绑定 tg
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanUnbindAccount() {
        List<EmbyUserResult> allEmbyUser = EmbyUtil.getInstance().getAllEmbyUser();
        List<User> allUser = AuthorityUtil.userService.userMapper.selectList(new QueryWrapper<User>().lambda().isNotNull(User::getEmbyId));
        TelegramClient telegramClient = new OkHttpTelegramClient(AuthorityUtil.botConfig.token);
        SendMessage sendMessage = new SendMessage(AuthorityUtil.botConfig.notifyChannel, "");
        if (AuthorityUtil.botConfig.getCleanUnbindAccount())
            allEmbyUser.stream().filter(embyUserResult -> !allUser.stream().map(User::getEmbyId).toList().contains(embyUserResult.getId())).forEach(embyUserResult -> {
                try {
                    EmbyUtil.getInstance().deleteEmbyById(embyUserResult.getId());
                    sendMessage.setText(String.format("#bot检查扬号: 观影账号 %s ( %s ) 已被扬, 原因: 未绑定 tg 用户", embyUserResult.getName(), embyUserResult.getId()));
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.toString());
                }
            });
    }

    /**
     * 0 点判断保号
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkUser() {
        Date expDate = DateUtil.beginOfDay(new Date());
        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda()
                .eq(User::getUserType, 1);
//                .lt(User::getExpTime, expDate);
        List<User> expEmbyUser = AuthorityUtil.userService.userMapper.selectList(queryWrapper);
        TelegramClient telegramClient = new OkHttpTelegramClient(AuthorityUtil.botConfig.token);
        SendMessage sendMessage = new SendMessage("", "");
        expEmbyUser.stream().filter(User::haveEmby).forEach(user -> {
            Date expUserDate = user.getExpTime();
            if (AuthorityUtil.botConfig.getEnableUserNotInGroup() && Boolean.FALSE.equals(AuthorityUtil.checkUserInChatMember(Long.parseLong(user.getTgId()), AuthorityUtil.botConfig.groupId, telegramClient))) {
                EmbyUtil.getInstance().deleteUser(user);
                user.cleanEmby();
                AuthorityUtil.userService.userMapper.updateById(user);
                sendMessage.enableMarkdownV2(false);
                sendMessage.setChatId(AuthorityUtil.botConfig.notifyChannel);
                sendMessage.setText(MessageUtil.INSTANCE.getAccountNotInGroupMessage(user));
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    // nothing
                } finally {
                    sendMessage.setChatId(user.getTgId());
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        log.error(e.toString());
                    }
                }
                return;
            }
            String embyNameCache = user.getEmbyName();
            String embyIdCache = user.getEmbyId();
            EmbyUtil.getInstance().initPolicy(user.getEmbyId(), user.getDeactivate());
            List<PlaybackUserRecord> activityLogs = EmbyUtil.getInstance().getUserPlayback(user.getEmbyId());
            Long betweenPlayDay = activityLogs.isEmpty() ? null : DateUtil.betweenDay(activityLogs.get(0).getDateCreated(), new Date(), true);
            Long betweenExpDay = DateUtil.betweenDay(user.getExpTime(), new Date(), true);
            String lastDate = activityLogs.isEmpty() ? "无" : FormatUtil.INSTANCE.dateToString(activityLogs.get(0).getDateCreated());
            if (AuthorityUtil.botConfig.getEnableExpLife()) {
                if (user.getExpTime() == null) {
                    user.addExpDate(AuthorityUtil.botConfig.getExpDay());
                    AuthorityUtil.userService.userMapper.updateById(user);
                    sendMessage.enableMarkdownV2(false);
                    sendMessage.setChatId(user.getTgId());
                    sendMessage.setText("现开启账户到期时间, 无过期时间用户赠送 " + AuthorityUtil.botConfig.getExpDay() + " 天");
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        // nothing
                    }
                } else {
                    if (expDate.after(user.getExpTime())) {
                        if (AuthorityUtil.botConfig.getDelete() && betweenExpDay >= AuthorityUtil.botConfig.getExpDelDay()) {
                            EmbyUtil.getInstance().deleteUser(user);
                            user.cleanEmby();
                        } else {
                            EmbyUtil.getInstance().deactivateUser(user, true);
                            user.setDeactivate(true);
                        }
                        sendMessage.enableMarkdownV2(true);
                        sendMessage.setChatId(AuthorityUtil.botConfig.notifyChannel);
                        sendMessage.setText(MessageUtil.INSTANCE.getAccountMessage(embyNameCache, embyIdCache, user, lastDate, expUserDate, false));
                        try {
                            telegramClient.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            log.error(e.toString());
                        } finally {
                            sendMessage.setChatId(user.getTgId());
                            try {
                                telegramClient.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                log.error(e.toString());
                            }
                        }
                    }
                }
                return;
            }
            if (AuthorityUtil.botConfig.getOpenAutoRenewal() && expDate.after(user.getExpTime()) || !AuthorityUtil.botConfig.getOpenAutoRenewal())
                if (AuthorityUtil.botConfig.getCleanTask() && (betweenPlayDay == null || betweenPlayDay >= AuthorityUtil.botConfig.getExpDay())) {
                    if (AuthorityUtil.botConfig.getDelete() && (betweenPlayDay == null || betweenPlayDay >= AuthorityUtil.botConfig.getExpDelDay() + AuthorityUtil.botConfig.getExpDelDay())) {
                        EmbyUtil.getInstance().deleteUser(user);
                        user.cleanEmby();
                    } else {
                        EmbyUtil.getInstance().deactivateUser(user, true);
                        user.setDeactivate(true);
                    }
                    sendMessage.enableMarkdownV2(true);
                    sendMessage.setChatId(AuthorityUtil.botConfig.notifyChannel);
                    sendMessage.setText(MessageUtil.INSTANCE.getAccountMessage(embyNameCache, embyIdCache, user, lastDate, expUserDate, true));
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        log.error(e.toString());
                    } finally {
                        sendMessage.setChatId(user.getTgId());
                        try {
                            telegramClient.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            log.error(e.toString());
                        }
                    }
                    return;
                }
            if (AuthorityUtil.botConfig.getOpenAutoRenewal() && user.getExpTime() == null) {
                user.addExpDate(AuthorityUtil.botConfig.getExpDay());
                AuthorityUtil.userService.userMapper.updateById(user);
                sendMessage.enableMarkdownV2(false);
                sendMessage.setChatId(user.getTgId());
                sendMessage.setText("现开启积分保号, 无过期时间用户赠送 " + AuthorityUtil.botConfig.getExpDay() + " 天");
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    // nothing
                }
            } else if (AuthorityUtil.botConfig.getOpenAutoRenewal() && expDate.after(user.getExpTime())) {
                if (user.getPoints() < AuthorityUtil.botConfig.getUnblockPoints()) {
                    if (AuthorityUtil.botConfig.getDelete() && betweenExpDay >= AuthorityUtil.botConfig.getExpDelDay()) {
                        EmbyUtil.getInstance().deleteUser(user);
                        user.cleanEmby();
                    } else {
                        EmbyUtil.getInstance().deactivateUser(user, true);
                        user.setDeactivate(true);
                    }
                    sendMessage.enableMarkdownV2(true);
                    sendMessage.setChatId(AuthorityUtil.botConfig.notifyChannel);
                    sendMessage.setText(MessageUtil.INSTANCE.getAccountMessage(embyNameCache, embyIdCache, user, lastDate, expUserDate, false));
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        log.error(e.toString());
                    } finally {
                        sendMessage.setChatId(user.getTgId());
                        try {
                            telegramClient.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            log.error(e.toString());
                        }
                    }
                } else {
                    user.setPoints(user.getPoints() - AuthorityUtil.botConfig.getUnblockPoints());
                    user.addExpDate(AuthorityUtil.botConfig.getExpDay());
                    AuthorityUtil.userService.userMapper.updateById(user);
                    sendMessage.enableMarkdownV2(false);
                    sendMessage.setChatId(user.getTgId());
                    sendMessage.setText("使用 " + AuthorityUtil.botConfig.getUnblockPoints() + " 自动续期 " + AuthorityUtil.botConfig.getExpDay() + " 天");
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        log.error(e.toString());
                    }
                }
                return;
            }
        });
    }

    /**
     * 每天 5:30 pm 点生成日榜
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 30 17 * * ?")
    public void generateDailyTop() {
        log.info("开始生成每日播放排行榜...");

        try {
            List<PlaybackRecord> movieShows = EmbyUtil.getInstance().getPlaybackInfo(true, new Date());
            List<PlaybackRecord> tvShows = EmbyUtil.getInstance().getPlaybackInfo(false, new Date());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<b>【播放日榜】</b>&#10;&#10;");
            stringBuilder.append("<b>▎电影:</b>&#10;");
            if (movieShows != null)
                for (int i = 0; i < movieShows.size(); i++) {
                    stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", movieShows.get(i).getItemName(), movieShows.get(i).getCount()));
                }
            stringBuilder.append("&#10;<b>▎剧集:</b>&#10;");
            if (tvShows != null)
                for (int i = 0; i < tvShows.size(); i++) {
                    stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", tvShows.get(i).getName(), tvShows.get(i).getCount()));
                }
            TelegramClient telegramClient = new OkHttpTelegramClient(AuthorityUtil.botConfig.token);
            SendPhoto sendPhoto = new SendPhoto(AuthorityUtil.botConfig.groupId, ImageGenerator.generateRankingImage(false, movieShows.size() > 5 ? movieShows.subList(0, 5) : movieShows, tvShows.size() > 5 ? tvShows.subList(0, 5) : tvShows, 1280));
            sendPhoto.setParseMode("HTML");
            MessageUtil.INSTANCE.sendLongCaption(telegramClient, sendPhoto, stringBuilder.toString());
        } catch (Exception e) {
            log.error("生成每日排行榜失败", e);
        }
    }

    /**
     * 每周六 6 pm 点生成周榜
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 0 18 * * SAT")
    public void generateWeeklyTop() {
        log.info("开始生成每周播放排行榜...");

        try {
            List<PlaybackRecord> movieShows = EmbyUtil.getInstance().getPlaybackInfo(true, new Date(), 7);
            List<PlaybackRecord> tvShows = EmbyUtil.getInstance().getPlaybackInfo(false, new Date(), 7);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<b>【播放周榜】</b>&#10;&#10;");
            stringBuilder.append("<b>▎电影:</b>&#10;");
            if (movieShows != null)
                for (int i = 0; i < movieShows.size(); i++) {
                    stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", movieShows.get(i).getItemName(), movieShows.get(i).getCount()));
                }
            stringBuilder.append("&#10;<b>▎剧集:</b>&#10;");
            if (tvShows != null)
                for (int i = 0; i < tvShows.size(); i++) {
                    stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", tvShows.get(i).getName(), tvShows.get(i).getCount()));
                }
            TelegramClient telegramClient = new OkHttpTelegramClient(AuthorityUtil.botConfig.token);
            SendPhoto sendPhoto = new SendPhoto(AuthorityUtil.botConfig.groupId, ImageGenerator.generateRankingImage(true, movieShows.size() > 5 ? movieShows.subList(0, 5) : movieShows, tvShows.size() > 5 ? tvShows.subList(0, 5) : tvShows, 1280));
            sendPhoto.setParseMode("HTML");
            MessageUtil.INSTANCE.sendLongCaption(telegramClient, sendPhoto, stringBuilder.toString());
        } catch (Exception e) {
            log.error("生成每周排行榜失败", e);
        }
    }

    /**
     * 每天 7 pm 点生成用户日榜
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 0 19 * * ?")
    public void generateUserDailyTop() {
        log.info("开始生成用户每日播放排行榜...");

        try {
            List<PlaybackShowsResult> userShows = EmbyUtil.getInstance().getUserShowInfo(FormatUtil.INSTANCE.dateToShowString());
            userShows.sort(Comparator.comparingLong(PlaybackShowsResult::getTime).reversed());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<b>【用户播放日榜】</b>&#10;");
            for (int i = 0; i < 10; i++) {
                stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", userShows.get(i).getLabel(), ChartUtil.INSTANCE.formatSecondsToTime(userShows.get(i).getTime())));
            }
            TelegramClient telegramClient = new OkHttpTelegramClient(AuthorityUtil.botConfig.token);
            SendPhoto sendPhoto = new SendPhoto(AuthorityUtil.botConfig.groupId, MessageUtil.INSTANCE.getHeadImageAsInputFile());
            sendPhoto.setParseMode("HTML");
            MessageUtil.INSTANCE.sendLongCaption(telegramClient, sendPhoto, stringBuilder.toString());
        } catch (Exception e) {
            log.error("生成用户每日排行榜失败", e);
        }
    }

    /**
     * 每周六 7:30 pm 点生成用户周榜
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 30 19 * * SAT")
    public void generateUserWeeklyTop() {
        log.info("开始生成用户每周播放排行榜...");

        try {
            List<PlaybackShowsResult> userShows = EmbyUtil.getInstance().getUserShowInfo(FormatUtil.INSTANCE.dateToShowString(), 7);
            userShows.sort(Comparator.comparingLong(PlaybackShowsResult::getTime).reversed());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<b>【用户播放周榜】</b>&#10;");
            for (int i = 0; i < 10; i++) {
                stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", userShows.get(i).getLabel(), ChartUtil.INSTANCE.formatSecondsToTime(userShows.get(i).getTime())));
            }
            TelegramClient telegramClient = new OkHttpTelegramClient(AuthorityUtil.botConfig.token);
            SendPhoto sendPhoto = new SendPhoto(AuthorityUtil.botConfig.groupId, MessageUtil.INSTANCE.getHeadImageAsInputFile());
            sendPhoto.setParseMode("HTML");
            MessageUtil.INSTANCE.sendLongCaption(telegramClient, sendPhoto, stringBuilder.toString());
        } catch (Exception e) {
            log.error("生成用户每周排行榜失败", e);
        }
    }

    /**
     * 每周六 7:40 pm 点生成设备周榜
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 40 19 * * SAT")
    public void generateDeviceWeeklyTop() {
        log.info("开始生成设备每周播放排行榜...");

        try {
            List<PlaybackShowsResult> deviceShows = EmbyUtil.getInstance().getDeviceShowInfo(FormatUtil.INSTANCE.dateToShowString(), 7);
            deviceShows.sort(Comparator.comparingLong(PlaybackShowsResult::getCount).reversed());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<b>【设备播放周榜】</b>&#10;");
            for (int i = 0; i < deviceShows.size(); i++) {
                stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", deviceShows.get(i).getLabel(), deviceShows.get(i).getCount()));
            }
            TelegramClient telegramClient = new OkHttpTelegramClient(AuthorityUtil.botConfig.token);
            SendPhoto sendPhoto = new SendPhoto(AuthorityUtil.botConfig.groupId, ChartUtil.generatePieChartAsInputFile(deviceShows));
            sendPhoto.setParseMode("HTML");
            MessageUtil.INSTANCE.sendLongCaption(telegramClient, sendPhoto, stringBuilder.toString());
        } catch (Exception e) {
            log.error("生成设备每周排行榜失败", e);
        }
    }
}