package com.ocd.controller.task;

import com.ocd.bean.dto.result.PlaybackShowsResult;
import com.ocd.controller.config.BotConfig;
import com.ocd.controller.util.AuthorityUtil;
import com.ocd.controller.util.ChartUtil;
import com.ocd.controller.util.EmbyUtil;
import com.ocd.controller.util.MessageUtil;
import com.ocd.util.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Comparator;
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
     * 1. 整点判断 emby 库内用户是否绑定 tg
     * 2. 未绑定用户扬号
     * 3. 清吧/会所同步
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 0 0 * * ?")
    public void embyTask() {
        AuthorityUtil.cleanTask(new OkHttpTelegramClient(BotConfig.getInstance().COMMANDS_TOKEN));
    }

    /**
     * 每天 5:30 pm 点生成日榜
     */
    @Async("taskScheduler")
    @Scheduled(cron = "0 30 17 * * ?")
    public void generateDailyTop() {
        log.info("开始生成每日播放排行榜...");

        try {
            List<PlaybackShowsResult> movieShows = EmbyUtil.getInstance().getShowInfo(true, FormatUtil.INSTANCE.dateToShowString());
            List<PlaybackShowsResult> tvShows = EmbyUtil.getInstance().getShowInfo(false, FormatUtil.INSTANCE.dateToShowString());
            movieShows.sort(Comparator.comparingLong(PlaybackShowsResult::getCount).reversed());
            tvShows.sort(Comparator.comparingLong(PlaybackShowsResult::getCount).reversed());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<b>【播放日榜】</b>&#10;&#10;");
            stringBuilder.append("<b>▎电影:</b>&#10;");
            for (int i = 0; i < 10; i++) {
                stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", movieShows.get(i).getLabel(), movieShows.get(i).getCount()));
            }
            stringBuilder.append("&#10;<b>▎剧集:</b>&#10;");
            for (int i = 0; i < 10; i++) {
                stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", tvShows.get(i).getLabel(), tvShows.get(i).getCount()));
            }
            TelegramClient telegramClient = new OkHttpTelegramClient(BotConfig.getInstance().COMMANDS_TOKEN);
            SendPhoto sendPhoto = new SendPhoto(BotConfig.getInstance().GROUP_ID, MessageUtil.INSTANCE.getHeadImageAsInputFile());
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
            List<PlaybackShowsResult> movieShows = EmbyUtil.getInstance().getShowInfo(true, FormatUtil.INSTANCE.dateToShowString(), 7);
            List<PlaybackShowsResult> tvShows = EmbyUtil.getInstance().getShowInfo(false, FormatUtil.INSTANCE.dateToShowString(), 7);
            movieShows.sort(Comparator.comparingLong(PlaybackShowsResult::getCount).reversed());
            tvShows.sort(Comparator.comparingLong(PlaybackShowsResult::getCount).reversed());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<b>【播放周榜】</b>&#10;&#10;");
            stringBuilder.append("<b>▎电影:</b>&#10;");
            for (int i = 0; i < 10; i++) {
                stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", movieShows.get(i).getLabel(), movieShows.get(i).getCount()));
            }
            stringBuilder.append("&#10;<b>▎剧集:</b>&#10;");
            for (int i = 0; i < 10; i++) {
                stringBuilder.append(i + 1).append(". ").append(String.format("%s - %s&#10;", tvShows.get(i).getLabel(), tvShows.get(i).getCount()));
            }
            TelegramClient telegramClient = new OkHttpTelegramClient(BotConfig.getInstance().COMMANDS_TOKEN);
            SendPhoto sendPhoto = new SendPhoto(BotConfig.getInstance().GROUP_ID, MessageUtil.INSTANCE.getHeadImageAsInputFile());
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
            TelegramClient telegramClient = new OkHttpTelegramClient(BotConfig.getInstance().COMMANDS_TOKEN);
            SendPhoto sendPhoto = new SendPhoto(BotConfig.getInstance().GROUP_ID, MessageUtil.INSTANCE.getHeadImageAsInputFile());
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
            TelegramClient telegramClient = new OkHttpTelegramClient(BotConfig.getInstance().COMMANDS_TOKEN);
            SendPhoto sendPhoto = new SendPhoto(BotConfig.getInstance().GROUP_ID, MessageUtil.INSTANCE.getHeadImageAsInputFile());
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
            TelegramClient telegramClient = new OkHttpTelegramClient(BotConfig.getInstance().COMMANDS_TOKEN);
            SendPhoto sendPhoto = new SendPhoto(BotConfig.getInstance().GROUP_ID, ChartUtil.generatePieChartAsInputFile(deviceShows));
            sendPhoto.setParseMode("HTML");
            MessageUtil.INSTANCE.sendLongCaption(telegramClient, sendPhoto, stringBuilder.toString());
        } catch (Exception e) {
            log.error("生成设备每周排行榜失败", e);
        }
    }
}