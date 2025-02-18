package com.ocd.controller.util

import com.alibaba.fastjson.JSON
import com.isen.bean.constant.ConstantStrings
import com.ocd.bean.dto.moviepilot.MoviepilotDownResult
import com.ocd.bean.dto.moviepilot.MoviepilotResult
import com.ocd.bean.dto.moviepilot.MoviepilotTransferResult
import com.ocd.controller.commands.MoviepilotConfig
import com.ocd.util.HttpUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

/**
 * @author ch.hu
 * @date 2025/02/13 10:06
 * Description:
 */
@Component
class MoviepilotUtil {

    private val log = LoggerFactory.getLogger(MoviepilotUtil::class.java)

    @Autowired
    private lateinit var moviepilotConfig: MoviepilotConfig

    private var authKey: String? = null

    private fun updateApikey(): Boolean {
        return try {
            val headers = HttpHeaders().apply {
                set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            }
            val body = "username=${moviepilotConfig.username}&password=${moviepilotConfig.password}"
            val entity = HttpEntity(body, headers)
            val uri =
                UriComponentsBuilder.fromHttpUrl("${moviepilotConfig.url}api/v1/login/access-token").build().toUri()
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            if (response.statusCode == HttpStatus.OK) {
                val jsonResponse = JSON.parseObject(response.body)
                authKey = jsonResponse.getString("access_token")
                log.info("API key updated successfully")
                true
            } else {
                log.error("Failed to update API key")
                false
            }
        } catch (e: Exception) {
            log.error("moviepilot 服务异常")
            false
        }
    }

    /**
     * 查询影片
     */
    @JvmOverloads
    fun searchFilm(keyword: String, retryCount: Int = 3): List<MoviepilotResult>? {
        if (authKey == null && !updateApikey())
            return null
        return try {
            val headers = HttpHeaders().apply {
                set(HttpHeaders.AUTHORIZATION, "${ConstantStrings.AUTHENTICATION_PREFIX}$authKey")
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${moviepilotConfig.url}api/v1/search/title")
            uri.queryParam("keyword", keyword)
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            when (response.statusCode) {
                HttpStatus.OK, HttpStatus.NO_CONTENT -> {
                    val jsonObject = JSON.parseObject(response.body)
                    if (jsonObject.getBoolean("success")) {
                        JSON.parseArray(jsonObject["data"].toString(), MoviepilotResult::class.java)
                    } else {
                        emptyList()
                    }
                }

                HttpStatus.FORBIDDEN -> {
                    log.warn("API key might be expired (403), trying to update API key")
                    if (retryCount > 0 && updateApikey()) {
                        searchFilm(keyword, retryCount - 1)
                    } else {
                        null
                    }
                }

                else -> {
                    log.error("Request failed with status code: ${response.statusCode}")
                    null
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 下载影片
     */
    @JvmOverloads
    fun downFilm(moviepilotResult: MoviepilotResult, retryCount: Int = 3): String? {
        if (authKey == null && !updateApikey())
            return null
        return try {
            val headers = HttpHeaders().apply {
                set(HttpHeaders.AUTHORIZATION, "${ConstantStrings.AUTHENTICATION_PREFIX}$authKey")
            }
            val map = HashMap<String, Any>()
            map["torrent_in"] = moviepilotResult.torrentInfo
            val entity = HttpEntity(map, headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${moviepilotConfig.url}api/v1/download/add")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    String::class.java
                )
            when (response.statusCode) {
                HttpStatus.OK, HttpStatus.NO_CONTENT -> {
                    val jsonObject = JSON.parseObject(response.body)
                    if (jsonObject.getBoolean("success")) {
                        JSON.parseObject(jsonObject.getString("data")).getString("download_id")
                    } else {
                        null
                    }
                }

                HttpStatus.FORBIDDEN -> {
                    log.warn("API key might be expired (403), trying to update API key")
                    if (retryCount > 0 && updateApikey()) {
                        downFilm(moviepilotResult, retryCount - 1)
                    } else {
                        null
                    }
                }

                else -> {
                    log.error("Request failed with status code: ${response.statusCode}")
                    null
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 查询下载进度
     */
    @JvmOverloads
    fun downStateFilm(retryCount: Int = 3): List<MoviepilotDownResult>? {
        if (authKey == null && !updateApikey())
            return null
        return try {
            val headers = HttpHeaders().apply {
                set(HttpHeaders.AUTHORIZATION, "${ConstantStrings.AUTHENTICATION_PREFIX}$authKey")
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${moviepilotConfig.url}api/v1/download")
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            when (response.statusCode) {
                HttpStatus.OK, HttpStatus.NO_CONTENT -> {
                    JSON.parseArray(response.body, MoviepilotDownResult::class.java)
                }

                HttpStatus.FORBIDDEN -> {
                    log.warn("API key might be expired (403), trying to update API key")
                    if (retryCount > 0 && updateApikey()) {
                        downStateFilm(retryCount - 1)
                    } else {
                        null
                    }
                }

                else -> {
                    log.error("Request failed with status code: ${response.statusCode}")
                    null
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 查询转移结果
     */
    @JvmOverloads
    fun transferStateFilm(title: String, retryCount: Int = 3, page: Int, count: Int): List<MoviepilotTransferResult>? {
        if (authKey == null && !updateApikey())
            return null
        return try {
            val headers = HttpHeaders().apply {
                set(HttpHeaders.AUTHORIZATION, "${ConstantStrings.AUTHENTICATION_PREFIX}$authKey")
            }
            val entity = HttpEntity<String>(headers)
            val uri = UriComponentsBuilder.fromHttpUrl("${moviepilotConfig.url}api/v1/history/transfer")
            uri.queryParam("title", title)
            uri.queryParam("page", page)
            uri.queryParam("count", count)
            val response = HttpUtil.getInstance()
                .restTemplate()
                .exchange(
                    uri.build().toUri(),
                    HttpMethod.GET,
                    entity,
                    String::class.java
                )
            when (response.statusCode) {
                HttpStatus.OK, HttpStatus.NO_CONTENT -> {
                    val jsonObject = JSON.parseObject(response.body)
                    if (jsonObject.getBoolean("success")) {
                        JSON.parseArray(
                            JSON.parseObject(jsonObject.getString("data")).getString("list"),
                            MoviepilotTransferResult::class.java
                        )
                    } else {
                        emptyList()
                    }
                }

                HttpStatus.FORBIDDEN -> {
                    log.warn("API key might be expired (403), trying to update API key")
                    if (retryCount > 0 && updateApikey()) {
                        downStateFilm(retryCount - 1)
                    } else {
                        null
                    }
                }

                else -> {
                    log.error("Request failed with status code: ${response.statusCode}")
                    null
                }
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }
}