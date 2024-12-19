package com.ocd.bean.dto.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ch.hu
 * @date 2024/11/13 10:20
 * Description:
 */
@NoArgsConstructor
@Data
public class EmbyUserRegistResult {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("ServerId")
    private String serverId;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("HasPassword")
    private Boolean hasPassword;
    @JsonProperty("HasConfiguredPassword")
    private Boolean hasConfiguredPassword;
    @JsonProperty("HasConfiguredEasyPassword")
    private Boolean hasConfiguredEasyPassword;
    @JsonProperty("EnableAutoLogin")
    private Boolean enableAutoLogin;
    @JsonProperty("Configuration")
    private EmbyUserConfigurationResult configuration;
    @JsonProperty("Policy")
    private EmbyUserPolicyResult policy;
}
