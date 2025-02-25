package com.ocd.controller.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author ch.hu
 * @date 2025/02/25 13:41
 * Description:
 */
@Component
@ConfigurationProperties(prefix = "emby")
class EmbyConfig {

    lateinit var url: String

    lateinit var apikey: String

    lateinit var limitNet: String

    lateinit var deviceCount: String
}