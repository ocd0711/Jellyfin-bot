package com.ocd.bean.dto.moviepilot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ch.hu
 * @date 2025/02/18 10:36
 * Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviepilotDownResult {

    @JsonProperty("downloader")
    private String downloader;
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("title")
    private String title;
    @JsonProperty("name")
    private String name;
    @JsonProperty("year")
    private String year;
    @JsonProperty("season_episode")
    private String seasonEpisode;
    @JsonProperty("size")
    private Double size;
    @JsonProperty("progress")
    private Double progress;
    @JsonProperty("state")
    private String state;
    @JsonProperty("upspeed")
    private String upspeed;
    @JsonProperty("dlspeed")
    private String dlspeed;
    @JsonProperty("media")
    private MediaDTO media;
    @JsonProperty("userid")
    private String userid;
    @JsonProperty("username")
    private String username;
    @JsonProperty("left_time")
    private String leftTime;

    @NoArgsConstructor
    @Data
    public static class MediaDTO {
        @JsonProperty("tmdbid")
        private Integer tmdbid;
        @JsonProperty("type")
        private String type;
        @JsonProperty("title")
        private String title;
        @JsonProperty("season")
        private String season;
        @JsonProperty("episode")
        private String episode;
        @JsonProperty("image")
        private String image;
    }
}
