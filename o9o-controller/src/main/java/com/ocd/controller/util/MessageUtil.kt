package com.ocd.controller.util

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.isen.bean.constant.ConstantStrings
import com.ocd.bean.dto.result.EmbyUserResult
import com.ocd.controller.config.BotConfig
import com.ocd.util.FormatUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import org.springframework.core.io.ClassPathResource
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import oshi.SystemInfo
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.random.Random

/**
 * @author OCD
 * @date 2022/12/20 11:11
 * Description:
 */
object MessageUtil {

    fun sendUserMessage(
        user: User,
        message: Message,
        processingResults: String,
        command: String,
        sendMessage: SendMessage
    ): String {
        sendMessage.enableMarkdownV2(true)
        val adminMarkdown = "[${escapeQueryChars(user.firstName)}](tg://user?id=${user.id})"
        val userMarkdown =
            "[${if (message.isReply) escapeQueryChars(message.replyToMessage.from.firstName) else "非回复消息, 无对应用户"}](tg://user?id=${if (message.isReply) message.replyToMessage.from.id else ""})"
        val userId = if (message.isReply) message.replyToMessage.from.id else ""
        val userName = if (message.isReply) "@" + escapeQueryChars(message.replyToMessage.from.userName) else ""
        return "用户: $userMarkdown\\($userId\\) $userName\n" +
                "处理结果: $processingResults\n" +
                "管理信息: \\#$command\n" +
                "操作人员: $adminMarkdown"
    }

    fun sendUserMessage(
        user: User,
        useUser: User?,
        processingResults: String,
        command: String,
        sendMessage: SendMessage
    ): String {
        sendMessage.enableMarkdownV2(true)
        val adminMarkdown = "[${escapeQueryChars(user.firstName)}](tg://user?id=${user.id})"
        val userMarkdown =
            "[${escapeQueryChars(useUser?.firstName) ?: "非回复消息, 无对应用户"}](tg://user?id=${useUser?.id ?: ""})"
        val userId = useUser?.id ?: ""
        val userName = if (useUser != null) "@${escapeQueryChars(useUser.userName)}" else ""
        return "用户: $userMarkdown\\($userId\\) $userName\n" +
                "处理结果: $processingResults\n" +
                "管理信息: \\#$command\n" +
                "操作人员: $adminMarkdown"
    }

    fun sendUserMessage(
        user: com.ocd.bean.mysql.User,
        chatMember: ChatMember?,
        processingResults: String,
        command: String,
        sendMessage: SendMessage
    ): String {
        sendMessage.enableMarkdownV2(true)
        val useUser = chatMember?.user
        val adminMarkdown = "[${user.tgId}](tg://user?id=${user.tgId})"
        val userMarkdown =
            "[${escapeQueryChars(useUser?.firstName) ?: "非回复消息, 无对应用户"}](tg://user?id=${useUser?.id ?: ""})"
        val userId = useUser?.id ?: ""
        val userName = if (useUser != null && useUser.userName != null) "@${escapeQueryChars(useUser.userName)}" else ""
        return "用户: $userMarkdown\\($userId\\) $userName\n" +
                "处理结果: $processingResults\n" +
                "管理信息: \\#$command\n" +
                "操作人员: $adminMarkdown"
    }

    fun escapeQueryChars(s: String?): String? {
        if (s == null || StringUtils.isBlank(s)) {
            return s
        }
        val sb = StringBuilder()
        // 查询字符串一般不会太长，挨个遍历也花费不了多少时间
        for (element in s) {
            // These characters are part of the query syntax and must be escaped
//            if (element == '\\' || element == '>' || element == '+' || element == '_' || element == '-' || element == '!' || element == '(' || element == ')' || element == ':' || element == '^' || element == '[' || element == ']' || element == '\"' || element == '{' || element == '}' || element == '~' || element == '*' || element == '?' || element == '|' || element == '&' || element == ';' || element == '/' || element == '.' || element == '$' || element == '=' || Character.isWhitespace(element)) {
            if (element == '_' || element == '*' || element == '[' || element == ']' || element == '(' || element == ')' || element == '~' || element == '`' || element == '>' || element == '#' || element == '+' || element == '-' || element == '=' || element == '|' || element == '{' || element == '}' || element == '.' || element == '!') {
                sb.append('\\')
            }
            sb.append(element)
        }
        return sb.toString()
    }

    fun getUserInfoButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("👥个人信息")
        info.callbackData = "info " + user.tgId
        return info
    }

    fun getLineInfoButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("🌐线路信息")
        info.callbackData = "line " + user.tgId
        return info
    }

    fun getInviteButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("🛍邀请码生成")
        info.callbackData = "shop " + user.tgId
        return info
    }

    fun getOpenRegisterButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("🎈开放注册(当前:${if (AuthorityUtil.openRegister) "开" else "关"})")
        info.callbackData = "openRegister " + user.tgId
        return info
    }

    fun getCreateButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("🎉注册 jellyfin 账户")
        info.callbackData = "create " + user.tgId
        return info
    }

    fun getBindButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("🗝️绑定 jellyfin 账户")
        info.callbackData = "bind " + user.tgId
        return info
    }

    fun getResetButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("😵重置密码")
        info.callbackData = "reset " + user.tgId
        return info
    }

    fun getHideButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("🫣隐藏部分分类(当前: ${if (user.hideMedia) "开" else "关"})")
        info.callbackData = "hide " + user.tgId
        return info
    }

    fun getUnblockButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("️‍🫧申请解封账号")
        info.callbackData = "unblock " + user.tgId
        return info
    }

    fun getCheckinButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("🎊签到")
        info.callbackData = "checkin " + user.tgId
        return info
    }

    fun getDevicesButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("📱设备管理")
        info.callbackData = "device " + user.tgId
        return info
    }

    fun getFlushButton(user: com.ocd.bean.mysql.User): InlineKeyboardButton {
        var info = InlineKeyboardButton("😳刷新用户状态")
        info.callbackData = "flush " + user.tgId
        return info
    }

    fun getMainButton(user: com.ocd.bean.mysql.User): InlineKeyboardMarkup {
        val rows = ArrayList<InlineKeyboardRow>()
        val inlineKeyboardMarkup = InlineKeyboardMarkup(rows)
        val rowLineOne = InlineKeyboardRow()
        val rowLineTwo = InlineKeyboardRow()
        val rowLineDown = InlineKeyboardRow()
        val rowLineAdmin = InlineKeyboardRow()

        // 插入按钮
        if (user.haveEmby()) {
            rowLineOne.add(getUserInfoButton(user))
            rowLineOne.add(getLineInfoButton(user))
            rowLineOne.add(getResetButton(user))
            rowLineTwo.add(getHideButton(user))
            rowLineTwo.add(getDevicesButton(user))
        } else {
            rowLineOne.add(getCreateButton(user))
            rowLineOne.add(getBindButton(user))
        }

        rowLineDown.add(getFlushButton(user))

        rows.add(rowLineOne)
        rows.add(rowLineTwo)
        rows.add(rowLineDown)

        if (user.admin) {
            rowLineAdmin.add(getInviteButton(user))
            if (user.superAdmin)
                rowLineAdmin.add(getOpenRegisterButton(user))
            rows.add(rowLineAdmin)
        }

        inlineKeyboardMarkup.keyboard = rows
        return inlineKeyboardMarkup
    }

    @JvmOverloads
    fun getAllDevicesButton(user: com.ocd.bean.mysql.User, uuid: String? = null): List<InlineKeyboardRow> {
        val embyDeviceResults = EmbyUtil.getInstance().viewingEquipment(user.embyId)
        val inlineKeyboardRow = ArrayList<InlineKeyboardRow>()
        embyDeviceResults.stream().filter { embyDeviceResult ->
            embyDeviceResult.lastUserId != null && embyDeviceResult.lastUserId.equals(user.embyId)
        }
            .forEach { embyDeviceResult ->
                if (uuid != null && embyDeviceResult.id == uuid) return@forEach
                val deviceRow = InlineKeyboardRow();
                val deviceButton =
                    InlineKeyboardButton("${embyDeviceResult.appName} ${embyDeviceResult.name}")
                deviceButton.callbackData = "logout ${user.tgId} ${embyDeviceResult.id}"
                if (deviceButton.callbackData.length > 64) {
                    EmbyUtil.getInstance().deleteDevice(embyDeviceResult.id)
                } else {
                    deviceRow.add(deviceButton)
                    inlineKeyboardRow.add(deviceRow)
                }
            }
        return inlineKeyboardRow
    }

    @JvmOverloads
    fun getStartMessage(firstName: String, isFlush: Boolean = false): String {
        val embyCount = EmbyUtil.getInstance().getAllEmbyUser().size
        return String.format(
            """
✨ 只有你想见我的时候我们的相遇才有意义

🚪 开放注册状态: %s
👤 用户总数: %s
🏖️ 活跃: %s
👻 ${if (BotConfig.getInstance().ISDELETE) "待杀(七天内)" else "停用"}: %s
💨 允许注册数: %s

🍉你好鸭 %s 请选择功能${if (isFlush) "(用户状态已刷新)" else ""}👇
""",
            if (AuthorityUtil.openRegister) "开" else "关",
            embyCount,
            AuthorityUtil.userService.userMapper.selectCount(
                QueryWrapper<com.ocd.bean.mysql.User>().lambda().isNotNull(com.ocd.bean.mysql.User::getEmbyId)
                    .`in`(com.ocd.bean.mysql.User::getUserType, listOf<Int>(1, 2))
                    .eq(com.ocd.bean.mysql.User::getDeactivate, 0)
            ),
            AuthorityUtil.userService.userMapper.selectCount(
                QueryWrapper<com.ocd.bean.mysql.User>().lambda().isNotNull(com.ocd.bean.mysql.User::getEmbyId)
                    .`in`(com.ocd.bean.mysql.User::getUserType, listOf<Int>(1, 2))
                    .eq(com.ocd.bean.mysql.User::getDeactivate, 1)
            ),
            EmbyUtil.getInstance().getCanRegisterSize(),
            firstName
        )
    }

    fun launchInGlobalScope(block: suspend () -> Unit) {
        GlobalScope.launch {
            block()
        }
    }

    fun getServerStats(): String {
        val cpuUsage = getCpuUsage()
        val memoryUsage = getMemoryUsage()

        return """
· 💫 CPU | ${String.format("%.1f", cpuUsage)}%
· 🌩️ 内存 | $memoryUsage
    """.trimIndent()
    }

    // CPU Usage
    fun getCpuUsage(): Double {
        val si = SystemInfo()
        val hal = si.hardware
        val processor = hal.processor

        // 第一次采样
        val prevTicks = processor.systemCpuLoadTicks

        // 等待一段时间，通常是1秒
        Thread.sleep(1000)

        // 计算CPU负载百分比
        val load = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100

        return load
    }

    // Memory Usage
    fun getMemoryUsage(): String {
        val si = SystemInfo()
        val hal = si.hardware
        val memory = hal.memory

        // 等待一段时间，通常是1秒
        Thread.sleep(500)

        // 获取内存信息
        val availableMemory = memory.available / (1024 * 1024)
        val totalMemory = memory.total / (1024 * 1024)

        return "${(totalMemory - availableMemory) / 1024} GiB / ${totalMemory / 1024} GiB, ${((totalMemory - availableMemory) / totalMemory.toDouble()) * 100}%"
    }

    fun getNetworkInfo(): String {
        val systemInfo = SystemInfo()
        val hal = systemInfo.hardware
        val networkIFs = hal.networkIFs

        // 获取当前和上次的接收/发送字节数
        val prevRecvBytes = networkIFs[0].bytesRecv
        val prevSentBytes = networkIFs[0].bytesSent

        // 等待一秒钟来计算速率
        Thread.sleep(1000)

        // 获取新的接收/发送字节数
        val currRecvBytes = networkIFs[0].bytesRecv
        val currSentBytes = networkIFs[0].bytesSent

        // 计算每秒的上传/下载速度
        val downloadSpeed = currRecvBytes - prevRecvBytes
        val uploadSpeed = currSentBytes - prevSentBytes

        // 计算已用流量
        val totalDownload = currRecvBytes / (1024 * 1024 * 1024 * 1024)
        val totalUpload = currSentBytes / (1024 * 1024 * 1024 * 1024)

        // 输出结果
        return "· ⚡ 网速 | ↓" + downloadSpeed / 1024 + "M/s  ↑" + uploadSpeed / 1024 + "M/s\n· \uD83C\uDF0A 流量 | ↓\" + totalDownload + \"T  ↑\" + totalUpload + \"T"
    }

    @JvmOverloads
    fun getUserInfo(
        embyUserDto: EmbyUserResult?,
        cacheUser: com.ocd.bean.mysql.User,
        isManage: Boolean = false
    ): String {
        val activityLog =
            if (embyUserDto == null) null else AuthorityUtil.activityLogsService.getLastPlay(embyUserDto.id)?.dateCreated
        var out =
            "用户名称: ${if (embyUserDto == null || cacheUser.getEmbyName() == null) "无号" else cacheUser.getEmbyName()}\n" +
                    "绑定 tg id: ${cacheUser.tgId}\n" +
                    "超管: ${cacheUser.superAdmin}\n" +
                    "管理: ${cacheUser.admin}\n" +
                    "部分分类状态: ${if (cacheUser.hideMedia) "隐藏" else "显示"}\n" +
                    BotConfig.getInstance().GROUP_NICK + " 启用状态: ${if (embyUserDto == null || !cacheUser.haveEmby()) "无号" else (if (cacheUser.deactivate) "过期停用" else "正常")}\n" +
                    "bot 绑定时间: ${FormatUtil.dateToString(cacheUser.createTime)}\n" +
                    "最后登录时间: ${
                        if (embyUserDto == null || !cacheUser.haveEmby()) "无号" else FormatUtil.formatOtherStringTimeToDateStr(
                            embyUserDto.lastLoginDate
                        )
                    }\n" +
                    "最后活动时间: ${
                        if (embyUserDto == null || !cacheUser.haveEmby()) "无号" else FormatUtil.formatOtherStringTimeToDateStr(
                            embyUserDto.lastActivityDate
                        )
                    }\n" +
                    "最后观看时间: ${FormatUtil.dateToString(activityLog)}\n" +
                    "积分: ${cacheUser.points}\n"
        if (isManage)
            out = out + "登录设备数量: ${AuthorityUtil.devicesService.getDeviceCount(cacheUser.embyId)}\n"
        out = out + (if (BotConfig.getInstance().ISDELETE)
            "保号规则: ${if (cacheUser.userType == 2) "白名单 ♾️" else "${BotConfig.getInstance().EXPDAY} 天内有观看记录(无记录删号)"}"
        else "保号规则: ${if (cacheUser.userType == 2) "白名单 ♾️" else "${BotConfig.getInstance().EXPDAY} 天内有观看记录(每周五自助解封/${BotConfig.getInstance().UNBLOCKPOINTS} 积分解封)"}")
        return out
    }

    fun getRandomNumber(cacheUser: com.ocd.bean.mysql.User): String {
        val redisKey = ConstantStrings.getRedisTypeKey(cacheUser.tgId, "points")
        if (RedisUtil.contain(redisKey)) return "今日已签到"
        RedisUtil.set(redisKey, Date(), secondsUntilNextMidnight())
        val num = Random.nextInt(5, 10)
        cacheUser.points += num
        AuthorityUtil.userService.userMapper.updateById(cacheUser)
        return "签到获得积分: $num\n当前积分: ${cacheUser.points}\n"
    }

    fun isCheck(cacheUser: com.ocd.bean.mysql.User): Boolean {
        val redisKey = ConstantStrings.getRedisTypeKey(cacheUser.tgId, "points")
        return RedisUtil.contain(redisKey)
    }

    fun secondsUntilNextMidnight(): Long {
        val now = LocalDateTime.now()
        val nextMidnight = now.plusDays(1).toLocalDate().atStartOfDay()
        return nextMidnight.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC)
    }

    fun getAccountMessage(user: com.ocd.bean.mysql.User, embyUser: EmbyUserResult?): String {
        val action = if (BotConfig.getInstance().ISDELETE && !user.haveEmby()) {
            "删除账户"
        } else {
            "禁用账户" + if (BotConfig.getInstance().ISDELETE) "(7 天内未解封删除用户)" else ""
        }
        val returnStr = escapeMarkdownV2("#ACCOUNT ${BotConfig.getInstance().EXPDAY} 天未观看 $action")

        val embyName = escapeMarkdownV2(embyUser?.name ?: "Unknown User")
        val embyId = escapeMarkdownV2(embyUser?.id ?: "N/A")
        val tgId = escapeMarkdownV2(user.tgId ?: "0")

        val endRes = """
[$embyName](tg://user?id=$tgId), $embyId
$returnStr
    """
        return endRes
    }

    fun escapeMarkdownV2(text: String): String {
        val specialChars =
            listOf("\\", "_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!")
        val sb = StringBuilder()
        text.forEach { char ->
            if (specialChars.contains(char.toString())) {
                sb.append("\\")
            }
            sb.append(char)
        }
        return sb.toString()
    }

    fun generateRandomCode(length: Int): String {
        var code = StringBuilder()
        for (i in 0 until length) {
            code.append(('A' + (Math.random() * 26).toInt()).toChar())
        }
        return code.toString()
    }

    fun getHeadImageAsInputFile(): InputFile {
        val resource = ClassPathResource("head.jpeg")
        val inputStream = resource.inputStream
        val imageBytes = inputStream.readBytes()
        return InputFile(ByteArrayInputStream(imageBytes), "head.jpeg")
    }
}