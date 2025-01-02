package com.ocd.bean.dto.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ch.hu
 * @date 2024/11/13 12:27
 * Description:
 */
@NoArgsConstructor
@Data
public class EmbyItemCountsResult {

    @JsonProperty("MovieCount")
    private Integer movieCount;
    @JsonProperty("SeriesCount")
    private Integer seriesCount;
    @JsonProperty("EpisodeCount")
    private Integer episodeCount;
    @JsonProperty("ArtistCount")
    private Integer artistCount;
    @JsonProperty("ProgramCount")
    private Integer programCount;
    @JsonProperty("TrailerCount")
    private Integer trailerCount;
    @JsonProperty("SongCount")
    private Integer songCount;
    @JsonProperty("AlbumCount")
    private Integer albumCount;
    @JsonProperty("MusicVideoCount")
    private Integer musicVideoCount;
    @JsonProperty("BoxSetCount")
    private Integer boxSetCount;
    @JsonProperty("BookCount")
    private Integer bookCount;
    @JsonProperty("ItemCount")
    private Integer itemCount;
}
