package com.ocd.bean.dto.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ch.hu
 * @date 2024/11/13 11:45
 * Description:
 */
@NoArgsConstructor
@Data
public class EmbyMediaFoldersResult {
    
    @JsonProperty("Name")
    private String name;
    @JsonProperty("ServerId")
    private String serverId;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Etag")
    private String etag;
    @JsonProperty("DateCreated")
    private String dateCreated;
    @JsonProperty("CanDelete")
    private Boolean canDelete;
    @JsonProperty("CanDownload")
    private Boolean canDownload;
    @JsonProperty("SortName")
    private String sortName;
    @JsonProperty("ExternalUrls")
    private List<String> externalUrls;
    @JsonProperty("Path")
    private String path;
    @JsonProperty("EnableMediaSourceDisplay")
    private Boolean enableMediaSourceDisplay;
    @JsonProperty("ChannelId")
    private Object channelId;
    @JsonProperty("Taglines")
    private List<String> taglines;
    @JsonProperty("Genres")
    private List<String> genres;
    @JsonProperty("RemoteTrailers")
    private List<String> remoteTrailers;
    @JsonProperty("ProviderIds")
    private ProviderIdsDTO providerIds;
    @JsonProperty("IsFolder")
    private Boolean isFolder;
    @JsonProperty("ParentId")
    private String parentId;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("People")
    private List<String> people;
    @JsonProperty("Studios")
    private List<String> studios;
    @JsonProperty("GenreItems")
    private List<String> genreItems;
    @JsonProperty("LocalTrailerCount")
    private Integer localTrailerCount;
    @JsonProperty("SpecialFeatureCount")
    private Integer specialFeatureCount;
    @JsonProperty("DisplayPreferencesId")
    private String displayPreferencesId;
    @JsonProperty("Tags")
    private List<String> tags;
    @JsonProperty("PrimaryImageAspectRatio")
    private Double primaryImageAspectRatio;
    @JsonProperty("CollectionType")
    private String collectionType;
    @JsonProperty("ImageTags")
    private ImageTagsDTO imageTags;
    @JsonProperty("BackdropImageTags")
    private List<String> backdropImageTags;
    @JsonProperty("ImageBlurHashes")
    private ImageBlurHashesDTO imageBlurHashes;
    @JsonProperty("LocationType")
    private String locationType;
    @JsonProperty("MediaType")
    private String mediaType;
    @JsonProperty("LockedFields")
    private List<String> lockedFields;
    @JsonProperty("LockData")
    private Boolean lockData;

    @NoArgsConstructor
    @Data
    public static class ProviderIdsDTO {
    }

    @NoArgsConstructor
    @Data
    public static class ImageTagsDTO {
        @JsonProperty("Primary")
        private String primary;
    }

    @NoArgsConstructor
    @Data
    public static class ImageBlurHashesDTO {
        @JsonProperty("Primary")
        private PrimaryDTO primary;

        @NoArgsConstructor
        @Data
        public static class PrimaryDTO {
            @JsonProperty("5b5e72fc7cea16284c4828bebe36c6d8")
            private String $5b5e72fc7cea16284c4828bebe36c6d8;
        }
    }
}
