package com.ocd.bean.dto.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ch.hu
 * @date 2024/11/13 10:42
 * Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmbyUserResult extends EmbyUserRegistResult {

    @JsonProperty("LastLoginDate")
    private String lastLoginDate;
    @JsonProperty("LastActivityDate")
    private String lastActivityDate;
}
