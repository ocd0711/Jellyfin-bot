package com.ocd.controller.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * @author OCD
 * @version 1.0
 * @brief Bots configurations
 * @date 2022/12/15 10:21
 */
@Component
class BotConfig {

    private object BotConfigUtilHoder {
        @JvmStatic
        var mInstance: BotConfig = BotConfig()
    }

    companion object {
        @JvmStatic
        fun getInstance(): BotConfig {
            return BotConfigUtilHoder.mInstance
        }
    }

    @PostConstruct
    fun init() {
        BotConfigUtilHoder.mInstance = this
    }

    @Value("\${bot.token}")
    lateinit var COMMANDS_TOKEN: String

    @Value("\${bot.name}")
    lateinit var COMMANDS_USER: String

    @Value("\${bot.groupId}")
    lateinit var GROUP_ID: String

    @Value("\${bot.channel}")
    lateinit var CHANNEL: String

    @Value("\${bot.groupName}")
    lateinit var GROUPNAME: String

    @Value("\${bot.unblockPoints}")
    lateinit var UNBLOCKPOINTS: String

    @Value("\${bot.expDay}")
    val EXPDAY: Int = 14

    @Value("\${bot.isDelete}")
    val ISDELETE: Boolean = false

    @Value("\${bot.notify_channel}")
    lateinit var NOTIFY_CHANNEL: String

    @Value("\${bot.group_nick}")
    lateinit var GROUP_NICK: String

    @Value("\${bot.head_photo}")
    lateinit var HEAD_PHOTO: String
}