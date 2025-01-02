package com.isen.bean.constant

import java.util.regex.Pattern

/**
 * @author OCD
 * @date 2022/03/31 17:48
 * Description:
 * String 类型常量
 */
object ConstantStrings {

    val lineStr = "%s: IP: %s 端口: %s 可用状态: %s\n"

    val libraryLine = "电影总数: %s\n电视剧总数: %s\n剧集总数: %s"

    val groupIn = "administrator,creator,member"

    val adminCommands = arrayListOf(
        "pin", "unpin", "id"
    )

    val adminNoReplyCommands = arrayListOf(
        "channel", "find", "invite", "inviteh", "findin"
    )

    val sendUserCommands = arrayListOf(
        "gift", "mail"
    )

    val botCommandHide = arrayListOf("shop", "notify", "compensation")

    val notPermissionCommands = arrayListOf("pin", "unpin")

    fun isAdminCommands(command: String): Boolean {
        return adminCommands.contains(command)
    }

    fun isAdminNoReplyCommands(command: String): Boolean {
        return adminNoReplyCommands.contains(command)
    }

    fun isNotPermissionCommands(command: String): Boolean {
        return notPermissionCommands.contains(command)
    }

    /**
     * 验证Email
     * @param email email地址，格式：zhang@gmail.com，zhang@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */
    fun checkEmail(email: String?): Boolean {
        return Pattern.matches(
            "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$", email
        )
    }

    fun getRedisTypeKey(content: String, type: String): String? {
        return "$content$type"
    }

}