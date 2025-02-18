package com.ocd.controller.commands

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author ch.hu
 * @date 2025/02/10 10:46
 * Description:
 */
@Component
@ConfigurationProperties(prefix = "moviepilot")
class MoviepilotConfig {

    var openMp: Boolean = false

    var multipleRate: Int = 0

    lateinit var url: String

    lateinit var username: String

    lateinit var password: String

    lateinit var imdb: String

    fun imdbUrl(imdbId: String?): String {
        return "$imdb$imdbId"
    }
}