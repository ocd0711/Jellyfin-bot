package com.ocd.bean.dto.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ch.hu
 * @date 2024/12/17 17:04
 * Description:
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PlaybackShowsResult {

    @JsonProperty("label")
    private String label;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("time")
    private Integer time;
}
