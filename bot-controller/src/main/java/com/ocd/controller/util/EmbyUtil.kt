package com.ocd.controller.util

import cn.hutool.core.date.DateUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.isen.bean.constant.ConstantStrings
import com.ocd.bean.dto.jellby.PlaybackData
import com.ocd.bean.dto.jellby.PlaybackRecord
import com.ocd.bean.dto.jellby.PlaybackUserRecord
import com.ocd.bean.dto.result.*
import com.ocd.bean.mysql.HideMedia
import com.ocd.bean.mysql.Line
import com.ocd.bean.mysql.User
import com.ocd.util.HttpUtil
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import javax.imageio.ImageIO

/**
 * @author OCD
 * @date 2022/12/14 14:31
 * Description:
 * emby 工具类
 */
@Component
class EmbyUtil {

    private object EmbyUtilHoder {
        @JvmStatic
        var mInstance: EmbyUtil = EmbyUtil()
    }

    companion object {
        @JvmStatic
        fun getInstance(): EmbyUtil {
            return EmbyUtilHoder.mInstance
        }
    }

    @PostConstruct
    fun init() {
        EmbyUtilHoder.mInstance = this
    }

    @Value("\${emby.url}")
    lateinit var url: String

    @Value("\${emby.apikey}")
    private lateinit var apikey: String

    @Value("\${emby-jump}")
    private lateinit var embyJump: String

    fun checkUrl(line: Line): Boolean {
        try {
            val str = if (StringUtils.equals(
                    line.port, "443"
                )
            ) "https://${line.ip}:${line.port}/" else "http://${line.ip}:${line.port}/"
            HttpUtil.getInstance().restTemplate().getForObject(str, String::class.java)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun checkServerHealth(): String? {
        try {
            HttpUtil.getInstance().restTemplate().getForObject(url, String::class.java)
        } catch (e: Exception) {
            return "emby 服务器异常, 群内看看消息?"
        }
        return null
    }

    fun checkServerHealthString(): String? {
        var status: StringBuilder = StringBuilder()
        try {
            HttpUtil.getInstance().restTemplate().getForObject(url, String::class.java)
            status.append("${AuthorityUtil.botConfig.groupNick}: 正常")
        } catch (e: Exception) {
            status.append("${AuthorityUtil.botConfig.groupNick}: 异常")
        }
        return status.toString()
    }

    @JvmOverloads
    fun getAllEmbyUser(isHidden: Boolean? = null, isDisabled: Boolean? = null): List<EmbyUserResult> {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Users")
            if (isHidden != null)
                uri.queryParam("isHidden", isHidden)
            if (isDisabled != null)
                uri.queryParam("isDisabled", isDisabled)
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            JSONArray.parseArray(
                response.body,
                EmbyUserResult::class.java
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun register(user: User, embyUser: String): Boolean {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val map: HashMap<String, Any> = HashMap()
            map["Name"] = embyUser
            val entity = HttpEntity(map, headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Users/New")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            var embyUserDto: EmbyUserRegistResult = JSONArray.parseObject(
                response.body,
                EmbyUserRegistResult::class.java
            )
            if (user.userType == 0)
                user.userType = 1
            user.deactivate = false
            user.embyName = embyUserDto.name
            user.embyId = embyUserDto.id
            AuthorityUtil.userService.userMapper.updateById(user)
            initUser(user)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun resetPass(user: User, pass: String?) {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val map: HashMap<String, Any> = HashMap()
            if (pass != null) {
                map["CurrentPassword"] = pass
                map["CurrentPw"] = pass
                map["NewPw"] = pass
                map["ResetPassword"] = false
            } else {
                map["ResetPassword"] = true
            }
            val entity = HttpEntity(map, headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl(if (AuthorityUtil.botConfig.jellyfin) "${url}Users/Password" else "${url}Users/${user.embyId}/Password")
                    .queryParam("userId", user.embyId)
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
        } catch (e: Exception) {
            println("重置密码异常(${JSON.toJSONString(user)}): $e")
        }
    }

    fun deleteUser(user: User) {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Users/${user.embyId}")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.DELETE,
                    entity,
                    String::class.java
                )
            user.cleanEmby()
            AuthorityUtil.userService.userMapper.updateById(user)
        } catch (e: Exception) {
            // 啥都不用干
        }
    }

    fun deleteEmbyById(id: String) {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Users/$id")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.DELETE,
                    entity,
                    String::class.java
                )
        } catch (e: Exception) {
            // 啥都不用干
        }
    }

    fun deleteEmbyByName(embyName: String) {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val entity = HttpEntity<String>(headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}Users/${getAllEmbyUser().filter { it.name.equals(embyName) }[0].id}")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.DELETE,
                    entity,
                    String::class.java
                )
        } catch (e: Exception) {
            println(e.toString())
            println("emby 同名账户移除异常 $embyName")
        }
    }

    /**
     * 账户停用/启用
     */
    fun deactivateUser(user: User, deactivate: Boolean) {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val map: HashMap<String, Any> = HashMap()
            map["IsDisabled"] = deactivate
            map["EnableAudioPlaybackTranscoding"] = false
            map["EnableVideoPlaybackTranscoding"] = false
            map["EnablePlaybackRemuxing"] = false
            map["EnableContentDownloading"] = false
            map["MaxActiveSessions"] = 3
            map["RemoteClientBitrateLimit"] = 80000000
            map["SyncPlayAccess"] = "None"
            if (!AuthorityUtil.botConfig.jellyfin) {
                map["IsHidden"] = true
                map["IsHiddenFromUnusedDevices"] = true
                map["EnableSyncTranscoding"] = false
                map["EnableMediaConversion"] = false
                map["EnablePublicSharing"] = false
                map["SimultaneousStreamLimit"] = 3
                map["AllowCameraUpload"] = false
            } else {
                map["AuthenticationProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider"
                map["PasswordResetProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider"
            }
            val entity = HttpEntity(map, headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}Users/${user.embyId}/Policy")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            user.deactivate = deactivate
            AuthorityUtil.userService.userMapper.updateById(user)
        } catch (e: Exception) {
            // 啥都不用干
        }
    }

    fun initUser(user: User) {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val map: HashMap<String, Any> = HashMap()
            map["EnableAudioPlaybackTranscoding"] = false
            map["EnableVideoPlaybackTranscoding"] = false
            map["EnablePlaybackRemuxing"] = false
            map["EnableContentDownloading"] = false
            map["MaxActiveSessions"] = 3
            map["RemoteClientBitrateLimit"] = 80000000
            map["SyncPlayAccess"] = "None"
            map["IsAdministrator"] = user.superAdmin
            if (!AuthorityUtil.botConfig.jellyfin) {
                map["IsHidden"] = true
                map["IsHiddenFromUnusedDevices"] = true
                map["EnableSyncTranscoding"] = false
                map["EnableMediaConversion"] = false
                map["EnablePublicSharing"] = false
                map["SimultaneousStreamLimit"] = 3
                map["AllowCameraUpload"] = false
            } else {
                map["AuthenticationProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider"
                map["PasswordResetProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider"
            }
            val entity = HttpEntity(map, headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}Users/${user.embyId}/Policy")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            filterNsfw(user)
        } catch (e: Exception) {
            println(e.toString())
            println("initUser 出错")
        }
    }

    @JvmOverloads
    fun initPolicy(embyId: String, isDisabled: Boolean = false) {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val map: HashMap<String, Any> = HashMap()
            map["IsDisabled"] = isDisabled
            map["EnableAudioPlaybackTranscoding"] = false
            map["EnableVideoPlaybackTranscoding"] = false
            map["EnablePlaybackRemuxing"] = false
            map["EnableContentDownloading"] = false
            map["MaxActiveSessions"] = 3
            map["RemoteClientBitrateLimit"] = 80000000
            map["SyncPlayAccess"] = "None"
            if (!AuthorityUtil.botConfig.jellyfin) {
                map["IsHidden"] = true
                map["IsHiddenFromUnusedDevices"] = true
                map["EnableSyncTranscoding"] = false
                map["EnableMediaConversion"] = false
                map["EnablePublicSharing"] = false
                map["SimultaneousStreamLimit"] = 3
                map["AllowCameraUpload"] = false
            } else {
                map["AuthenticationProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider"
                map["PasswordResetProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider"
            }
            val entity = HttpEntity(map, headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}Users/${embyId}/Policy")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
        } catch (e: Exception) {
            println(e.toString())
            println("initPolicy 出错")
        }
    }

    fun filterNsfw(user: User): Boolean {
        try {
            val embyUserDto = getUserByEmbyId(user.embyId)
            val policy = embyUserDto!!.policy
            val embyMediaFoldersDtos = searchAllMediaLibraries()
            val hideMediaList =
                AuthorityUtil.hideMediaService.hideMediaMapper.selectList(null).stream().map(HideMedia::getName)
                    .collect(Collectors.toList())
            policy.sNfsw(
                user,
                user.hideMedia,
                embyMediaFoldersDtos.filter { embyMediaFoldersDto -> !hideMediaList.contains(embyMediaFoldersDto.name) }
                    .stream().map { (if (AuthorityUtil.botConfig.jellyfin) it.id else it.guid) }
                    .collect(Collectors.toList())
            )

            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val entity = HttpEntity(policy, headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}Users/${user.embyId}/Policy")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            user.hideMedia = !user.hideMedia
            AuthorityUtil.userService.userMapper.updateById(user)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * 查询所有媒体库
     */
    fun searchAllMediaLibraries(): List<EmbyMediaFoldersResult> {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Library/MediaFolders")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            return JSON.parseArray(
                JSON.parseObject(response.body).getString("Items") ?: "",
                EmbyMediaFoldersResult::class.java
            )
        } catch (e: Exception) {
            return emptyList()
        }
    }

    /**
     * 用户账号密码验证
     */
    fun authenticateByName(username: String, password: String?): EmbyUserResult? {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
                set(
                    HttpHeaders.AUTHORIZATION,
                    "MediaBrowser Client=\"OCD BOT\", Device=\"OCD\", DeviceId=\"OCD_BOT\", Version=\"10.11.0\""
                )
            }
            val map: HashMap<String, Any> = HashMap()
            map["Username"] = username
            map["Pw"] = password ?: ""
            val entity = HttpEntity(map, headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}Users/AuthenticateByName")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            JSON.parseObject(JSON.parseObject(response.body).getString("User"), EmbyUserResult::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 用户 id 验证
     */
    fun getUserByEmbyId(embyId: String?): EmbyUserResult? {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Users/$embyId")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            return JSONArray.parseObject(
                response.body,
                EmbyUserResult::class.java
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 观看设备
     */
    fun viewingEquipment(embyId: String?): List<EmbyDeviceResult> {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl(if (AuthorityUtil.botConfig.jellyfin) "${url}Devices?userId=$embyId" else "${url}Devices")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            val devices = JSONArray.parseArray<EmbyDeviceResult>(
                JSON.parseObject(response.body).get("Items").toString() ?: "",
                EmbyDeviceResult::class.java
            )
            return if (embyId != null) devices.filter { it.lastUserId == embyId } else devices
        } catch (e: Exception) {
            emptyList()
        }
    }


    /**
     * 删除设备
     */
    fun deleteDevice(deviceId: String?): Boolean {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Devices?id=$deviceId")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.DELETE,
                    entity,
                    String::class.java
                )
            return response.statusCode == HttpStatus.NO_CONTENT
        } catch (e: Exception) {
            return false
        }
    }

    fun LibraryCountStr(): String {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Items/Counts")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            val embyItemCountsResult = JSONArray.parseObject(
                response.body,
                EmbyItemCountsResult::class.java
            )
            String.format(
                ConstantStrings.libraryLine,
                embyItemCountsResult?.movieCount ?: "查询失败",
                embyItemCountsResult?.seriesCount ?: "查询失败",
                embyItemCountsResult?.episodeCount ?: "查询失败"
            )
        } catch (e: Exception) {
            "Library Count search error"
        }
    }

    fun getUuid(uuid: String): String {
        return uuid.replaceFirst(
            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w+)".toRegex(),
            "$1-$2-$3-$4-$5"
        )
    }

    @JvmOverloads
    fun getShowInfo(isMovie: Boolean, date: String, days: Int = 1, timezoneOffset: Int = 8): List<PlaybackShowsResult> {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}user_usage_stats/${if (isMovie) "MoviesReport" else "GetTvShowsReport"}")
            uri.queryParam("date", date)
            uri.queryParam("days", days)
            uri.queryParam("timezoneOffset", timezoneOffset)
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            return JSONArray.parseArray<PlaybackShowsResult>(
                response.body,
                PlaybackShowsResult::class.java
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    @JvmOverloads
    fun getUserShowInfo(date: String, days: Int = 1, timezoneOffset: Int = 8): List<PlaybackShowsResult> {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}user_usage_stats/UserId/BreakdownReport")
            uri.queryParam("date", date)
            uri.queryParam("days", days)
            uri.queryParam("timezoneOffset", timezoneOffset)
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            return JSONArray.parseArray<PlaybackShowsResult>(
                response.body,
                PlaybackShowsResult::class.java
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    @JvmOverloads
    fun getDeviceShowInfo(date: String, days: Int = 1, timezoneOffset: Int = 8): List<PlaybackShowsResult> {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}user_usage_stats/ClientName/BreakdownReport")
            uri.queryParam("date", date)
            uri.queryParam("days", days)
            uri.queryParam("timezoneOffset", timezoneOffset)
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            return JSONArray.parseArray<PlaybackShowsResult>(
                response.body,
                PlaybackShowsResult::class.java
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCanRegisterSize(): Int {
        return if (AuthorityUtil.accountCount == null) 0 else AuthorityUtil.accountCount - getAllEmbyUser().size
    }

    @JvmOverloads
    fun getUserPlayback(embyId: String, limitCount: Int? = 1): List<PlaybackUserRecord>? {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val map: HashMap<String, Any> = HashMap()
            map["CustomQueryString"] =
                "SELECT * FROM PlaybackActivity WHERE UserId = '$embyId' ORDER BY DateCreated DESC ${if (limitCount != null) "LIMIT $limitCount" else ""}"
            map["ReplaceUserId"] = false
            val entity = HttpEntity(map, headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}user_usage_stats/submit_custom_query")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            if (response.statusCode != HttpStatus.OK && response.statusCode != HttpStatus.NO_CONTENT)
                return null
            val playbackData = JSON.parseObject(response.body, PlaybackData::class.java)
            return playbackData.mapUserResultsToPlaybackRecords()
        } catch (e: Exception) {
            return null
        }
    }

    @JvmOverloads
    fun getItemBackdrop(itemId: String, width: Int = 1280, quality: Int = 70): BufferedImage? {
        return try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
            }
            val entity = HttpEntity<String>(headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${url}emby/Items/$itemId/Images/Primary")
            uri.queryParam("maxWidth", width)
            uri.queryParam("quality", quality)
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    ByteArray::class.java
                )

            val imageBytes = response.body
            if (imageBytes != null) {
                val inputStream = ByteArrayInputStream(imageBytes)
                ImageIO.read(inputStream)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    @JvmOverloads
    fun getPlaybackInfo(
        isMovie: Boolean,
        date: Date,
        days: Int = 1,
        limitCount: Int = 10
    ): List<PlaybackRecord>? {
        try {
            val startDate = DateUtil.offsetDay(date, -days)
            val startStr = DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss:SSSSSS")
            val endStr = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss:SSSSSS")
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val map: HashMap<String, Any> = HashMap()
            map["CustomQueryString"] =
                """
                    SELECT *, substr(ItemName,0, instr(ItemName, ' - ')) AS name, SUM(PlayDuration) AS total_duarion, COUNT(1) AS count FROM PlaybackActivity
                    WHERE ItemType = '${if (isMovie) "Movie" else "Episode"}'
                    AND DateCreated >= '$startStr'
                    AND DateCreated <= '$endStr'
                    GROUP BY ItemName
                    ORDER BY count
                    DESC LIMIT $limitCount
                """.trimIndent()
            map["ReplaceUserId"] = false
            val entity = HttpEntity(map, headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${url}user_usage_stats/submit_custom_query")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            if (response.statusCode != HttpStatus.OK && response.statusCode != HttpStatus.NO_CONTENT)
                return null
            val playbackData = JSON.parseObject(response.body, PlaybackData::class.java)
            return playbackData.mapResultsToPlaybackRecords()
        } catch (e: Exception) {
            return null
        }
    }
}