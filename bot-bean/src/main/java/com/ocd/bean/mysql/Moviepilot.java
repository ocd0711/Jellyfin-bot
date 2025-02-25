package com.ocd.bean.mysql;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ocd.bean.dto.moviepilot.MoviepilotDownResult;
import com.ocd.bean.dto.moviepilot.MoviepilotResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ch.hu
 * @date 2025/02/12 22:37
 * Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Moviepilot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parent;

    private String orgString;

    private String category;

    private String param;

    private String imdb;

    private String pageUrl;

    private Integer status;

    private String downId;

    public MoviepilotResult moviepilotResult() {
        return JSON.parseObject(param, MoviepilotResult.class);
    }

    public void updateStatus(boolean pass) {
        status = pass ? 1 : -1;
    }

    public Moviepilot(Long userId, MoviepilotResult moviepilotResult, String imdbUrl, String downloadId) {
        this.parent = userId;
        this.orgString = moviepilotResult.getMetaInfo().getOrgString();
        this.category = moviepilotResult.getTorrentInfo().getCategory();
        this.param = JSON.toJSONString(moviepilotResult);
        this.imdb = imdbUrl;
        this.pageUrl = moviepilotResult.getTorrentInfo().getPageUrl();
        this.downId = downloadId;
    }

    public void updateByMoviepilot(MoviepilotResult moviepilotResult, String imdbUrl) {
        this.category = moviepilotResult.getTorrentInfo().getCategory();
        this.param = JSON.toJSONString(moviepilotResult);
        this.imdb = imdbUrl;
        this.pageUrl = moviepilotResult.getTorrentInfo().getPageUrl();
    }

    public String getInfoStr(MoviepilotDownResult moviepilotDownResult) {
        double progress = moviepilotDownResult.getProgress() != null ? moviepilotDownResult.getProgress() : 0;
        String progressText = "";
        if (progress == 0) {
            progressText = "未知";
        } else {
            progress = Math.round(progress * 10.0) / 10.0;
            StringBuilder leftProgress = new StringBuilder();
            StringBuilder rightProgress = new StringBuilder();
            for (int i = 0; i < progress / 10; i++) {
                leftProgress.append("🟩");
            }
            for (int i = 0; i < 10 - (int) (progress / 10); i++) {
                rightProgress.append("⬜️");
            }
            progressText = String.format("%s%s %.1f%%", leftProgress, rightProgress, progress);
        }

        String downloadStateText = "正在排队";
        switch (moviepilotDownResult.getDownloader()) {
            case "downloading":
                downloadStateText = "正在下载";
                break;
            case "completed":
                downloadStateText = "已完成";
                break;
            case "failed":
                downloadStateText = "下载失败";
                break;
        }

        return String.format("%s \n状态：%s %s\n 剩余时间：%s\n",
                moviepilotDownResult.getName(), downloadStateText, progressText, moviepilotDownResult.getLeftTime());
    }
}
