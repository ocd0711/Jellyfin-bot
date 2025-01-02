package com.ocd.bean.dto.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ch.hu
 * @date 2024/11/13 10:22
 * Description:
 */
@Data
@NoArgsConstructor
public class EmbyUserConfigurationResult {

    @JsonProperty("PlayDefaultAudioTrack")
    private Boolean playDefaultAudioTrack;
    @JsonProperty("SubtitleLanguagePreference")
    private String subtitleLanguagePreference;
    @JsonProperty("DisplayMissingEpisodes")
    private Boolean displayMissingEpisodes;
    @JsonProperty("GroupedFolders")
    private List<String> groupedFolders;
    @JsonProperty("SubtitleMode")
    private String subtitleMode;
    @JsonProperty("DisplayCollectionsView")
    private Boolean displayCollectionsView;
    @JsonProperty("EnableLocalPassword")
    private Boolean enableLocalPassword;
    @JsonProperty("OrderedViews")
    private List<String> orderedViews;
    @JsonProperty("LatestItemsExcludes")
    private List<String> latestItemsExcludes;
    @JsonProperty("MyMediaExcludes")
    private List<String> myMediaExcludes;
    @JsonProperty("HidePlayedInLatest")
    private Boolean hidePlayedInLatest;
    @JsonProperty("RememberAudioSelections")
    private Boolean rememberAudioSelections;
    @JsonProperty("RememberSubtitleSelections")
    private Boolean rememberSubtitleSelections;
    @JsonProperty("EnableNextEpisodeAutoPlay")
    private Boolean enableNextEpisodeAutoPlay;
    @JsonProperty("CastReceiverId")
    private String castReceiverId;
}
