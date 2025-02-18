package com.ocd.bean.dto.moviepilot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ch.hu
 * @date 2025/02/18 16:08
 * Description:
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
public class MoviepilotTransferResult {

    @JsonProperty("dest_fileitem")
    private DestFileitemDTO destFileitem;
    @JsonProperty("imdbid")
    private String imdbid;
    @JsonProperty("status")
    private Boolean status;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("mode")
    private String mode;
    @JsonProperty("tvdbid")
    private Object tvdbid;
    @JsonProperty("src_storage")
    private String srcStorage;
    @JsonProperty("type")
    private String type;
    @JsonProperty("doubanid")
    private Object doubanid;
    @JsonProperty("errmsg")
    private Object errmsg;
    @JsonProperty("category")
    private String category;
    @JsonProperty("seasons")
    private String seasons;
    @JsonProperty("date")
    private String date;
    @JsonProperty("src")
    private String src;
    @JsonProperty("title")
    private String title;
    @JsonProperty("episodes")
    private String episodes;
    @JsonProperty("files")
    private List<String> files;
    @JsonProperty("dest")
    private String dest;
    @JsonProperty("year")
    private String year;
    @JsonProperty("image")
    private String image;
    @JsonProperty("src_fileitem")
    private SrcFileitemDTO srcFileitem;
    @JsonProperty("tmdbid")
    private Integer tmdbid;
    @JsonProperty("downloader")
    private String downloader;
    @JsonProperty("dest_storage")
    private String destStorage;
    @JsonProperty("download_hash")
    private String downloadHash;

    @NoArgsConstructor
    @Data
    public static class DestFileitemDTO {
        @JsonProperty("storage")
        private String storage;
        @JsonProperty("type")
        private String type;
        @JsonProperty("path")
        private String path;
        @JsonProperty("name")
        private String name;
        @JsonProperty("basename")
        private String basename;
        @JsonProperty("extension")
        private String extension;
        @JsonProperty("size")
        private Long size;
        @JsonProperty("modify_time")
        private Double modifyTime;
        @JsonProperty("children")
        private List<String> children;
        @JsonProperty("fileid")
        private Object fileid;
        @JsonProperty("parent_fileid")
        private Object parentFileid;
        @JsonProperty("thumbnail")
        private Object thumbnail;
        @JsonProperty("pickcode")
        private Object pickcode;
        @JsonProperty("drive_id")
        private Object driveId;
        @JsonProperty("url")
        private Object url;
    }

    @NoArgsConstructor
    @Data
    public static class SrcFileitemDTO {
        @JsonProperty("storage")
        private String storage;
        @JsonProperty("type")
        private String type;
        @JsonProperty("path")
        private String path;
        @JsonProperty("name")
        private String name;
        @JsonProperty("basename")
        private String basename;
        @JsonProperty("extension")
        private String extension;
        @JsonProperty("size")
        private Long size;
        @JsonProperty("modify_time")
        private Double modifyTime;
        @JsonProperty("children")
        private List<String> children;
        @JsonProperty("fileid")
        private Object fileid;
        @JsonProperty("parent_fileid")
        private Object parentFileid;
        @JsonProperty("thumbnail")
        private Object thumbnail;
        @JsonProperty("pickcode")
        private Object pickcode;
        @JsonProperty("drive_id")
        private Object driveId;
        @JsonProperty("url")
        private Object url;
    }
}
