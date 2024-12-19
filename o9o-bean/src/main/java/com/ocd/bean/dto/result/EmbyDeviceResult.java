package com.ocd.bean.dto.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author ch.hu
 * @date 2024/11/25 15:51
 * Description:
 */
@NoArgsConstructor
@Data
public class EmbyDeviceResult {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("LastUserName")
    private String lastUserName;
    @JsonProperty("AppName")
    private String appName;
    @JsonProperty("AppVersion")
    private String appVersion;
    @JsonProperty("LastUserId")
    private String lastUserId;
    @JsonProperty("DateLastActivity")
    private String dateLastActivity;
    @JsonProperty("Capabilities")
    private CapabilitiesDTO capabilities;

    @NoArgsConstructor
    @Data
    public static class CapabilitiesDTO {
        @JsonProperty("PlayableMediaTypes")
        private List<?> playableMediaTypes;
        @JsonProperty("SupportedCommands")
        private List<?> supportedCommands;
        @JsonProperty("SupportsMediaControl")
        private Boolean supportsMediaControl;
        @JsonProperty("SupportsPersistentIdentifier")
        private Boolean supportsPersistentIdentifier;
    }
}
