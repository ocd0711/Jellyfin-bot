package com.ocd.controller.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.isen.bean.constant.ConstantStrings
import com.ocd.bean.dto.result.*
import com.ocd.bean.mysql.HideMedia
import com.ocd.bean.mysql.Line
import com.ocd.bean.mysql.User
import com.ocd.controller.config.BotConfig
import com.ocd.util.HttpUtil
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.util.stream.Collectors
import javax.annotation.PostConstruct

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
            status.append("${BotConfig.getInstance().GROUP_NICK}: 正常")
        } catch (e: Exception) {
            status.append("${BotConfig.getInstance().GROUP_NICK}: 异常")
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
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Users/Password")
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
            map["AuthenticationProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider"
            map["PasswordResetProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider"
            map["EnableAudioPlaybackTranscoding"] = false
            map["EnableVideoPlaybackTranscoding"] = false
            map["EnablePlaybackRemuxing"] = false
            map["EnableContentDownloading"] = false
            map["MaxActiveSessions"] = 3
            map["RemoteClientBitrateLimit"] = 80000000
            map["SyncPlayAccess"] = "None"
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
            map["AuthenticationProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider"
            map["PasswordResetProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider"
            map["EnableAudioPlaybackTranscoding"] = false
            map["EnableVideoPlaybackTranscoding"] = false
            map["EnablePlaybackRemuxing"] = false
            map["EnableContentDownloading"] = false
            map["MaxActiveSessions"] = 3
            map["RemoteClientBitrateLimit"] = 80000000
            map["SyncPlayAccess"] = "None"
            map["IsAdministrator"] = user.superAdmin
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

    fun initPolicy(embyId: String) {
        try {
            val headers = HttpHeaders().apply {
                set("X-Emby-Token", apikey)
                contentType = MediaType.APPLICATION_JSON
            }
            val map: HashMap<String, Any> = HashMap()
            map["AuthenticationProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider"
            map["PasswordResetProviderId"] = "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider"
            map["EnableAudioPlaybackTranscoding"] = false
            map["EnableVideoPlaybackTranscoding"] = false
            map["EnablePlaybackRemuxing"] = false
            map["EnableContentDownloading"] = false
            map["MaxActiveSessions"] = 3
            map["RemoteClientBitrateLimit"] = 80000000
            map["SyncPlayAccess"] = "None"
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
                    .stream().map { it.id }.collect(Collectors.toList())
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
            val uri = UriComponentsBuilder.fromHttpUrl("${url}Devices?userId=$embyId")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            return JSONArray.parseArray<EmbyDeviceResult>(
                JSON.parseObject(response.body).get("Items").toString() ?: "",
                EmbyDeviceResult::class.java
            )
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

    /**
     * curl 'https://tanhuatv.site/user_usage_stats/user_activity?days=42&end_date=2024-12-06&stamp=1733546602946' \
     * -X 'GET' \
     * -H 'Accept: application/json' \
     * -H 'Sec-Fetch-Site: same-origin' \
     * -H 'Cookie: cf_clearance=DtR0EssrfeQMY8z_yOFIrNdNF6x5A20yAKdJRXC4FuY-1733546406-1.2.1.1-gWazkaut9NTGEZeLyE5gEsRJhPe8pYzCTuaCMff8xmQH6Lq853cW8s8Cm.cTDETBNtzdCOgk3BJpqFZQOxPrERWK03HScaYTDMAbvAlEUvEiRmeRR2Vs4Qa0ki_3C1y1sX5fXueZtC4GY4zqK7lmP69bkmcvF0bYQyiqNd0SiJovBYb2qS4BouHd7Ntg1ciEeWQa3xvPEY6IRFWQC8dk.qYkr7js5tEgiDrpbh2fM8S.UGOfDJat.q36oQ.y1KEHT7m6HweQIOHaF9I2ML_zLnjkDnLuz_1Riw16NW8WSLp18KkiRYBir83wjobh9Cpr9E2FIY.7sG.IDx34adnoy4FmqRvnG43xOCAF7tpwWPlOq8KC708hGcDuVbltQ1desYrzJ7gauP127V33YE69Ew' \
     * -H 'Sec-Fetch-Dest: empty' \
     * -H 'Accept-Language: zh-CN,zh-Hans;q=0.9' \
     * -H 'Sec-Fetch-Mode: cors' \
     * -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.2 Safari/605.1.15' \
     * -H 'Accept-Encoding: gzip, deflate, br' \
     * -H 'Authorization: MediaBrowser Client="Jellyfin Web", Device="Safari", DeviceId="TW96aWxsYS81LjAgKE1hY2ludG9zaDsgSW50ZWwgTWFjIE9TIFggMTBfMTVfNykgQXBwbGVXZWJLaXQvNjA1LjEuMTUgKEtIVE1MLCBsaWtlIEdlY2tvKSBWZXJzaW9uLzE4LjIgU2FmYXJpLzYwNS4xLjE1fDE3MzEzOTI5NzU5NTY1", Version="10.11.0", Token="f2af1951e5f7406d8b27911074f605d4"' \
     * -H 'Priority: u=3, i'
     *
     * async def emby_cust_commit(self, user_id=None, days=7, method=None):
     *         _url = f'{self.url}/emby/user_usage_stats/submit_custom_query'
     *         sub_time = datetime.now(timezone(timedelta(hours=8)))
     *         start_time = (sub_time - timedelta(days=days)).strftime("%Y-%m-%d %H:%M:%S")
     *         end_time = sub_time.strftime("%Y-%m-%d %H:%M:%S")
     *         sql = ''
     *         if method == 'sp':
     *             sql += "SELECT UserId, SUM(PlayDuration - PauseDuration) AS WatchTime FROM PlaybackActivity "
     *             sql += f"WHERE DateCreated >= '{start_time}' AND DateCreated < '{end_time}' GROUP BY UserId ORDER BY WatchTime DESC"
     *         elif user_id != 'None':
     *             sql += "SELECT MAX(DateCreated) AS LastLogin,SUM(PlayDuration - PauseDuration) / 60 AS WatchTime FROM PlaybackActivity "
     *             sql += f"WHERE UserId = '{user_id}' AND DateCreated >= '{start_time}' AND DateCreated < '{end_time}' GROUP BY UserId"
     *         data = {"CustomQueryString": sql, "ReplaceUserId": True}  # user_name
     *         # print(sql)
     *         resp = r.post(_url, headers=self.headers, json=data, timeout=30)
     *         if resp.status_code == 200:
     *             # print(resp.json())
     *             rst = resp.json()["results"]
     *             return rst
     *         else:
     *             return None
     */
}