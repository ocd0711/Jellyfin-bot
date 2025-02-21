package com.ocd.controller.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * @author OCD
 * @version 1.0
 * @brief Bots configurations
 * @date 2022/12/15 10:21
 */
@Component
@ConfigurationProperties(prefix = "bot")
class BotConfig {

    var jellyfin: Boolean = true

    lateinit var groupNick: String

    lateinit var token: String

    lateinit var name: String

    lateinit var groupId: String

    lateinit var channel: String

    lateinit var groupName: String

    var unblockPoints: Int = 100

    var expDay: Int = 14

    var delete: Boolean = false

    lateinit var notifyChannel: String

    lateinit var wikiName: String

    lateinit var wikiUrl: String

    lateinit var headPhoto: String

    var cleanTask: Boolean = true

    var checkMin: Int = 1

    var checkMax: Int = 10

    var openAutoRenewal: Boolean = false

    var expDelDay: Int = 7

    var allowDirectRegister: Boolean = true

    var cleanUnbindAccount: Boolean = true

    var enableExpLife: Boolean = false

    var allowUserUnlockAccount = false

    var enableUserNotInGroup = true
}