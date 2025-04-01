package com.ocd.bean.dto.moviepilot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ch.hu
 * @date 2025/02/12 22:18
 * Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviepilotResult {

    private Integer index;

    @JsonProperty("meta_info")
    private MetaInfoDTO metaInfo;
    @JsonProperty("torrent_info")
    private TorrentInfoDTO torrentInfo;
    @JsonProperty("media_info")
    private Object mediaInfo;

    @NoArgsConstructor
    @Data
    public static class MetaInfoDTO {
        @JsonProperty("org_string")
        private String orgString;
        @JsonProperty("subtitle")
        private String subtitle;
        @JsonProperty("isfile")
        private Boolean isfile;
        @JsonProperty("_source")
        private String source;
        @JsonProperty("_effect")
        private List<String> effect;
        @JsonProperty("tokens")
        private TokensDTO tokens;
        @JsonProperty("en_name")
        private String enName;
        @JsonProperty("_last_token_type")
        private String lastTokenType;
        @JsonProperty("_continue_flag")
        private Boolean continueFlag;
        @JsonProperty("_unknown_name_str")
        private String unknownNameStr;
        @JsonProperty("year")
        private String year;
        @JsonProperty("_stop_name_flag")
        private Boolean stopNameFlag;
        @JsonProperty("_last_token")
        private String lastToken;
        @JsonProperty("resource_pix")
        private String resourcePix;
        @JsonProperty("video_encode")
        private String videoEncode;
        @JsonProperty("audio_encode")
        private String audioEncode;
        @JsonProperty("resource_type")
        private String resourceType;
        @JsonProperty("cn_name")
        private Object cnName;
        @JsonProperty("resource_team")
        private Object resourceTeam;
        @JsonProperty("customization")
        private Object customization;
        @JsonProperty("title")
        private String title;
        @JsonProperty("apply_words")
        private List<String> applyWords;
        @JsonProperty("type")
        private String type;
        @JsonProperty("season_episode")
        private String seasonEpisode;
        @JsonProperty("edition")
        private String edition;
        @JsonProperty("name")
        private String name;
        @JsonProperty("episode_list")
        private List<String> episodeList;

        @NoArgsConstructor
        @Data
        public static class TokensDTO {
            @JsonProperty("_text")
            private String text;
            @JsonProperty("_tokens")
            private List<String> tokens;
            @JsonProperty("_index")
            private Integer index;
        }
    }

    @NoArgsConstructor
    @Data
    public static class TorrentInfoDTO {
        @JsonProperty("site")
        private Integer site;
        @JsonProperty("site_name")
        private String siteName;
        @JsonProperty("site_cookie")
        private String siteCookie;
        @JsonProperty("site_ua")
        private String siteUa;
        @JsonProperty("site_proxy")
        private Integer siteProxy;
        @JsonProperty("site_order")
        private Integer siteOrder;
        @JsonProperty("site_downloader")
        private String siteDownloader;
        @JsonProperty("title")
        private String title;
        @JsonProperty("description")
        private String description;
        @JsonProperty("imdbid")
        private String imdbid;
        @JsonProperty("enclosure")
        private String enclosure;
        @JsonProperty("page_url")
        private String pageUrl;
        @JsonProperty("size")
        private Long size;
        @JsonProperty("seeders")
        private Integer seeders;
        @JsonProperty("peers")
        private Integer peers;
        @JsonProperty("grabs")
        private Integer grabs;
        @JsonProperty("pubdate")
        private String pubdate;
        @JsonProperty("date_elapsed")
        private String dateElapsed;
        @JsonProperty("freedate")
        private Object freedate;
        @JsonProperty("uploadvolumefactor")
        private Integer uploadvolumefactor;
        @JsonProperty("downloadvolumefactor")
        private Double downloadvolumefactor;
        @JsonProperty("hit_and_run")
        private Boolean hitAndRun;
        @JsonProperty("labels")
        private List<String> labels;
        @JsonProperty("pri_order")
        private Integer priOrder;
        @JsonProperty("category")
        private String category;
        @JsonProperty("volume_factor")
        private String volumeFactor;
        @JsonProperty("freedate_diff")
        private String freedateDiff;
    }

    public String formatResourceInfo() {
        StringBuilder text = new StringBuilder();
        text.append(String.format("资源编号: `%d`\n标题：%s", index, metaInfo.title));

        // 年份信息
        if (metaInfo.year != null)
            text.append(String.format("\n年份：%s", metaInfo.year));

        // 类型信息
        String typeInfo = metaInfo.type != null && !"未知".equals(metaInfo.type)
                ? metaInfo.type : "电影";
        text.append(String.format("\n类型：%s", typeInfo));

        // 大小信息
        if (torrentInfo.size != null) {
            long sizeInBytes = Long.parseLong(torrentInfo.size.toString());
            double sizeInGB = sizeInBytes / (1024.0 * 1024.0 * 1024.0);
            text.append(String.format("\n大小：%.2f GB", sizeInGB));
        }

        // 标签信息
        if (torrentInfo.labels != null) {
            text.append(String.format("\n标签：%s", torrentInfo.labels));
        }

        // 资源组信息
        if (torrentInfo.seeders != null) {
            text.append(String.format("\n种子数：%s", torrentInfo.seeders));
        }

        // 媒体信息
        List<String> mediaInfo = new ArrayList<>();
        if (metaInfo.resourcePix != null) {
            mediaInfo.add(metaInfo.resourcePix);
        }
        if (metaInfo.videoEncode != null) {
            mediaInfo.add(metaInfo.videoEncode);
        }
        if (metaInfo.audioEncode != null) {
            mediaInfo.add(metaInfo.audioEncode);
        }
        if (!mediaInfo.isEmpty()) {
            text.append(String.format("\n媒体信息：%s", String.join(" | ", mediaInfo)));
        }

        // 描述信息
        if (torrentInfo.description != null) {
            text.append(String.format("\n描述：%s", torrentInfo.description));
        }

        return text.toString();
    }
}